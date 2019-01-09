/*
 * Matrix Welcome Bot - Greet your new room members with a message
 * Copyright (C) 2019 Kamax Sarl
 *
 * https://www.kamax.io/
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */

package io.kamax.matrix.bot.welcome;

import io.kamax.matrix.MatrixErrorInfo;
import io.kamax.matrix.MatrixID;
import io.kamax.matrix._MatrixID;
import io.kamax.matrix.client.*;
import io.kamax.matrix.client.regular.MatrixHttpClient;
import io.kamax.matrix.client.regular.SyncOptions;
import io.kamax.matrix.event._MatrixEvent;
import io.kamax.matrix.event._MatrixStateEvent;
import io.kamax.matrix.hs._MatrixRoom;
import io.kamax.matrix.json.GsonUtil;
import io.kamax.matrix.json.MatrixJsonEventFactory;
import io.kamax.matrix.json.event.MatrixJsonRoomMembershipEvent;
import io.kamax.matrix.room.ReceiptType;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

public class WelcomeBot {

    private final Logger log = LoggerFactory.getLogger(WelcomeBot.class);

    private Config cfg;
    private _MatrixClient client;

    private List<String> justJoinedRooms = new ArrayList<>();

    public WelcomeBot(Config cfg) {
        this.cfg = cfg;
    }

    public void start() {
        if (StringUtils.isBlank(cfg.getStoreFile())) {
            throw new RuntimeException("Store file is not given");
        }

        File store = new File(cfg.getStoreFile());
        if (!store.exists()) {
            try {
                if (!store.createNewFile()) {
                    log.debug("Store file already exists but was supposed not to");
                }
            } catch (IOException e) {
                throw new RuntimeException(e);
            }

            if (!store.canRead()) {
                throw new RuntimeException("Store file is not readable");
            }

            if (!store.canWrite()) {
                throw new RuntimeException("Store file is not writable");
            }
        }

        if (Objects.isNull(cfg.getMxid())) {
            throw new RuntimeException("Matrix ID of the user is not set");
        }

        if (StringUtils.isAllBlank(cfg.getMsgTextFile(), cfg.getMsgHtmlFile())) {
            throw new RuntimeException("Message is not set");
        }

        if (Objects.isNull(cfg.getBaseUrl())) {
            client = new MatrixHttpClient(cfg.getMxid().getDomain());
            client.discoverSettings();
        } else {
            client = new MatrixHttpClient(cfg.getBaseUrl());
        }

        client.login(new MatrixPasswordCredentials(cfg.getMxid().getLocalPart(), cfg.getPassword()));
        cfg.setPassword("");

        log.info("Welcome bot is running");
        while (!Thread.currentThread().isInterrupted()) {
            try {
                FileReader fr = new FileReader(cfg.getStoreFile());
                String syncToken = StringUtils.defaultIfBlank(IOUtils.toString(fr), null);
                fr.close();

                log.debug("Sync token: {}", syncToken);
                _SyncData data = client.sync(SyncOptions.build().setTimeout(30000).setSince(syncToken).get());
                log.debug("Sync call returned");

                data.getRooms().getInvited().forEach(invitedRoom -> {
                    _MatrixRoom r = client.getRoom(invitedRoom.getId());

                    Optional<_MatrixStateEvent> invEvOpt = invitedRoom.getState().getEvents().stream()
                            .filter(ev -> StringUtils.equals(cfg.getMxid().getId(), ev.getStateKey()))
                            .findFirst();
                    if (!invEvOpt.isPresent()) {
                        log.warn("Invited to {} but could not find a matching state event, declining...", r.getId());
                        r.leave();
                    } else {
                        _MatrixStateEvent invEv = invEvOpt.get();
                        if (!StringUtils.equals(cfg.getMxid().getDomain(), invEv.getSender().getDomain())) {
                            log.info("Invited to {} but {} is not allowed to invite us. Declining...", r.getId(), invEv.getSender().getId());
                            r.leave();
                        } else {
                            log.info("Invited to {} by {}", r.getAddress(), invEv.getSender());
                            try {
                                r.join();
                                justJoinedRooms.add(r.getId());
                            } catch (MatrixClientRequestException e) {
                                MatrixErrorInfo err = e.getError().orElseGet(() -> new MatrixErrorInfo("Unknown error"));
                                log.warn("Unable to join {}: {} - {}", r.getAddress(), err.getErrcode(), err.getError());
                                r.leave();
                            }
                        }
                    }
                });

                data.getRooms().getJoined().forEach(joinedRoom -> {
                    if (justJoinedRooms.contains(joinedRoom.getId())) {
                        log.info("We joined room {} after being invited, skipping first timeline", joinedRoom.getId());
                        justJoinedRooms.remove(joinedRoom.getId());
                        return;
                    }

                    joinedRoom.getTimeline().getEvents().forEach(evJson -> {
                        _MatrixEvent rawEv = MatrixJsonEventFactory.get(evJson.getJson());
                        if (rawEv instanceof MatrixJsonRoomMembershipEvent) {
                            MatrixJsonRoomMembershipEvent ev = (MatrixJsonRoomMembershipEvent) rawEv;
                            if (StringUtils.equals(cfg.getMxid().getId(), ev.getSender().getId())) {
                                log.debug("Ignoring event about ourselves");
                                return;
                            }

                            if (StringUtils.equals("join", ev.getMembership())) {
                                String prevMembership = GsonUtil.findObj(ev.getJson(), "unsigned")
                                        .flatMap(u -> GsonUtil.findObj(u, "prev_content"))
                                        .flatMap(pc -> GsonUtil.findString(pc, "membership"))
                                        .orElse("leave");

                                if (!StringUtils.equals(ev.getMembership(), prevMembership)) {
                                    try {
                                        log.debug("Sending message for event {} in room {}", ev.getId(), joinedRoom.getId());

                                        String textMsg = IOUtils.toString(new FileReader(cfg.getMsgTextFile()))
                                                .replace("%JOINED_USER%", ev.getInvitee().getId());
                                        String htmlMsg = IOUtils.toString(new FileReader(cfg.getMsgHtmlFile()))
                                                .replace("%JOINED_USER%", ev.getInvitee().getId());

                                        client.getRoom(joinedRoom.getId()).sendReceipt(ReceiptType.Read, ev.getId());
                                        client.getRoom(joinedRoom.getId()).sendFormattedText(htmlMsg, textMsg);
                                        log.info("Sent message for event {} in room {}", ev.getId(), joinedRoom.getId());
                                    } catch (IOException e) {
                                        log.error("Unable to read template messages: {}", e.getMessage(), e);
                                    }
                                }
                            }
                        }
                    });
                });

                data.getRooms().getLeft().forEach(r -> log.info("Left {}", r.getId()));

                try {
                    FileWriter fw = new FileWriter(cfg.getStoreFile(), false);
                    IOUtils.write(data.nextBatchToken(), fw);
                    fw.close();
                    log.debug("Stored sync token to store");
                } catch (IOException e) {
                    log.error("Unable to write sync token to store");
                    e.printStackTrace();
                    System.exit(1);
                }
            } catch (RuntimeException e) {
                log.warn("Error during sync, cooling off until next sync", e.getMessage(), e);
                try {
                    Thread.sleep(5000);
                } catch (InterruptedException e1) {
                    log.debug("Interrupted while waiting for next sync");
                }
            } catch (IOException e) {
                log.error("Could not read the latest sync token", e);
                System.exit(1);
            }
        }

        log.info("Exiting");
    }

}
