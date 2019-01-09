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

import io.kamax.matrix._MatrixID;

import java.net.URL;

public class Config {

    private _MatrixID mxid;
    private String password;
    private URL baseUrl;
    private String msgTextFile;
    private String msgHtmlFile;
    private String storeFile;

    public _MatrixID getMxid() {
        return mxid;
    }

    public void setMxid(_MatrixID mxid) {
        this.mxid = mxid;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public URL getBaseUrl() {
        return baseUrl;
    }

    public void setBaseUrl(URL baseUrl) {
        this.baseUrl = baseUrl;
    }

    public String getMsgTextFile() {
        return msgTextFile;
    }

    public void setMsgTextFile(String msgTextFile) {
        this.msgTextFile = msgTextFile;
    }

    public String getMsgHtmlFile() {
        return msgHtmlFile;
    }

    public void setMsgHtmlFile(String msgHtmlFile) {
        this.msgHtmlFile = msgHtmlFile;
    }

    public String getStoreFile() {
        return storeFile;
    }

    public void setStoreFile(String storeFile) {
        this.storeFile = storeFile;
    }

}
