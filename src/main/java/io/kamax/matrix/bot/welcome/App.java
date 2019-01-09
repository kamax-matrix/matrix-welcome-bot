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

import io.kamax.matrix.MatrixID;
import org.apache.commons.lang3.StringUtils;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.Optional;

public class App {

    public static void main(String[] args) {
        Config cfg = new Config();
        cfg.setStoreFile(StringUtils.defaultIfBlank(System.getenv("WB_STORE_FILE"), "data.json"));
        cfg.setMxid(MatrixID.asAcceptable(System.getenv("WB_USER_MXID")));
        cfg.setPassword(System.getenv("WB_USER_PASSWORD"));
        cfg.setMsgTextFile(System.getenv("WB_MESSAGE_TEXT_FILE"));
        cfg.setMsgHtmlFile(System.getenv("WB_MESSAGE_HTML_FILE"));
        Optional.ofNullable(System.getenv("WB_HS_URL")).ifPresent(url -> {
            try {
                cfg.setBaseUrl(new URL(url));
            } catch (MalformedURLException e) {
                throw new RuntimeException("Invalid HS URL: " + url, e);
            }
        });

        new WelcomeBot(cfg).start();
    }

}
