/*
  * Copyright © 2011-2014 EPAM Systems/B2BITS® (http://www.b2bits.com).
 *
 * This file is part of STAFF.
 *
 * STAFF is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * STAFF is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with STAFF. If not, see <http://www.gnu.org/licenses/>.
 */

package com.btobits.automator.fix.quickfix.transport.impl;

import com.btobits.automator.fix.quickfix.transport.ITcpMessage;
import quickfix.Message;

public class TcpLoginMessage implements ITcpMessage {
    private String MES_ID = "A";
    private String user;
    private String password;
    private String msgType = "FIX.4.4";
    private String sender;
    private String target;

    public void setUser(String user) {
        this.user = user;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public void setMsgType(String msgType) {
        this.msgType = msgType;
    }

    public void setSender(String sender) {
        this.sender = sender;
    }

    public void setTarget(String target) {
        this.target = target;
    }

    public byte[] toRawMessage() {
        final Message fixMsg = new Message();

        fixMsg.getHeader().setField(new quickfix.field.SenderCompID(sender));
        fixMsg.getHeader().setField(new quickfix.field.TargetCompID(target));
        fixMsg.getHeader().setString(35, MES_ID);

        fixMsg.getHeader().setString(8, msgType);

        if (password != null) {
            fixMsg.setField(new quickfix.field.Password(password));
        }

        if (user != null) {
            fixMsg.setField(new quickfix.field.Username(user));
        }

        fixMsg.setField(new quickfix.field.EncryptMethod(quickfix.field.EncryptMethod.NONE_OTHER));
        return fixMsg.toString().getBytes();
    }

    public String getMessageAsString() {
        return new String(toRawMessage());
    }

    @Override
    public String toString() {
        return "TcpLoginMessage{" +
                ", user='" + user + '\'' +
                ", password='" + password + '\'' +
                ", msgType='" + msgType + '\'' +
                ", sender='" + sender + '\'' +
                ", target='" + target + '\'' +
                '}';
    }
}
