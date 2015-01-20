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

package quickfix;

import quickfix.field.*;

/**
 * @author Mykhailo_Sereda
 */
public class MessageUtils2 {

    public static Message parseMessage(final MessageFactory messageFactory, final DataDictionaryProvider ddProvider,
                                       ApplVerID defaultApplicationVersionID, final String msgStr) throws InvalidMessage {
        final String beginString = MessageUtils.getStringField(msgStr, BeginString.FIELD);
        final String msgType = MessageUtils.getMessageType(msgStr);

        final quickfix.Message message = messageFactory.create(beginString, msgType);
        final DataDictionary payloadDictionary = getApplicationDataDictionary(msgStr, ddProvider, defaultApplicationVersionID);

        message.parse(msgStr, getSessionDataDictionary(beginString, ddProvider), payloadDictionary,
                      payloadDictionary != null);
        return message;
    }

    public static DataDictionary getSessionDataDictionary(final String beginString, final DataDictionaryProvider ddProvider) {
        return ddProvider == null ? null : ddProvider
                .getSessionDataDictionary(beginString);
    }

    public static DataDictionary getApplicationDataDictionary(final String msgStr, final DataDictionaryProvider ddProvider,
                                                              final ApplVerID defaultApplicationVersionID) throws InvalidMessage {
        final String beginString = MessageUtils.getStringField(msgStr, BeginString.FIELD);
        final String msgType = MessageUtils.getMessageType(msgStr);

        ApplVerID applVerID = null;
        if (FixVersions.BEGINSTRING_FIXT11.equals(beginString)) {
            applVerID = getApplVerID(defaultApplicationVersionID, msgStr);
        } else {
            applVerID = MessageUtils.toApplVerID(beginString);
        }

        final DataDictionary sessionDataDictionary = ddProvider == null ? null : ddProvider
                .getSessionDataDictionary(beginString);
        final DataDictionary applicationDataDictionary = ddProvider == null ? null : ddProvider
                .getApplicationDataDictionary(applVerID);

        return MessageUtils.isAdminMessage(msgType)
                ? sessionDataDictionary
                : applicationDataDictionary;
    }

    public static DataDictionary getApplicationDataDictionary(final Message msg, final DataDictionaryProvider ddProvider,
                                                              final ApplVerID defaultApplicationVersionID) throws InvalidMessage, FieldNotFound {
        final String beginString = msg.getHeader().getString(BeginString.FIELD);
        final String msgType = msg.getHeader().getString(MsgType.FIELD);

        ApplVerID applVerID = null;
        if (FixVersions.BEGINSTRING_FIXT11.equals(beginString)) {
            applVerID = getApplVerID(defaultApplicationVersionID, msg);
        } else {
            applVerID = MessageUtils.toApplVerID(beginString);
        }

        final DataDictionary sessionDataDictionary = ddProvider == null ? null : ddProvider
                .getSessionDataDictionary(beginString);
        final DataDictionary applicationDataDictionary = ddProvider == null ? null : ddProvider
                .getApplicationDataDictionary(applVerID);

        return MessageUtils.isAdminMessage(msgType)
                ? sessionDataDictionary
                : applicationDataDictionary;
    }

    public static Message parse(Session session, String msgStr) throws InvalidMessage {
        if(session.getSessionID().isFIXT()) {
            return MessageUtils.parse(session, msgStr);
        } else {
            return MessageUtils.parse(session.getMessageFactory(), session.getDataDictionary(), msgStr);
        }
    }

    public static void validate(final Message message,
                                final DataDictionary dataDictionary) throws FieldNotFound, IncorrectTagValue, IncorrectDataFormat {
        dataDictionary.validate(message, true);
    }

    public static void validate(final Message message,
                                final SessionID sessionID,
                                final DataDictionaryProvider dataDictionaryProvider,
                                final ApplVerID defaultApplVerID) throws FieldNotFound, IncorrectTagValue, IncorrectDataFormat {
        final Message.Header header = message.getHeader();
        final String msgType = header.getString(MsgType.FIELD);

        final String beginString = header.getString(BeginString.FIELD);

        if (!beginString.equals(sessionID.getBeginString())) {
            throw new UnsupportedVersion(sessionID.getBeginString());
        }

        if (msgType.equals(MsgType.LOGON)) {

            // QFJ-648
            if (message.isSetField(HeartBtInt.FIELD)) {
                if (message.getInt(HeartBtInt.FIELD) < 0) {
                    throw new IncorrectTagValue("HeartBtInt must not be negative");
                }
            }
        }

        final DataDictionary sessionDataDictionary = dataDictionaryProvider
                .getSessionDataDictionary(beginString);

        final ApplVerID applVerID = header.isSetField(ApplVerID.FIELD) ? new ApplVerID(
                header.getString(ApplVerID.FIELD)) : defaultApplVerID;

        final DataDictionary applicationDataDictionary = MessageUtils
                .isAdminMessage(msgType) ? dataDictionaryProvider
                .getSessionDataDictionary(beginString) : dataDictionaryProvider
                .getApplicationDataDictionary(applVerID);

        DataDictionary.validate(message, sessionDataDictionary,
                applicationDataDictionary);

    }

    private static ApplVerID getApplVerID(ApplVerID defaultApplicationVersionID, String messageString)
            throws InvalidMessage {
        ApplVerID applVerID = null;

        final String applVerIdString = MessageUtils.getStringField(messageString, ApplVerID.FIELD);
        if (applVerIdString != null) {
            applVerID = new ApplVerID(applVerIdString);
        }

        if (applVerID == null) {
            applVerID = defaultApplicationVersionID;
        }

        if (applVerID == null && MessageUtils.isLogon(messageString)) {
            final String defaultApplVerIdString = MessageUtils.getStringField(messageString,
                    DefaultApplVerID.FIELD);
            if (defaultApplVerIdString != null) {
                applVerID = new ApplVerID(defaultApplVerIdString);
            }
        }

        if (applVerID == null) {
            throw new InvalidMessage("Can't determine ApplVerID for message");
        }

        return applVerID;
    }

    private static ApplVerID getApplVerID(ApplVerID defaultApplicationVersionID, Message message)
            throws InvalidMessage, FieldNotFound {
        ApplVerID applVerID = null;

        if (message.getHeader().isSetField(ApplVerID.FIELD)) {
            applVerID = new ApplVerID(message.getHeader().getString(ApplVerID.FIELD));
        }

        if (applVerID == null) {
            applVerID = defaultApplicationVersionID;
        }

        if (applVerID == null && "A".equals(message.getString(MsgType.FIELD))) {
            final String defaultApplVerIdString = message.getString(DefaultApplVerID.FIELD);
            if (defaultApplVerIdString != null) {
                applVerID = new ApplVerID(defaultApplVerIdString);
            }
        }

        if (applVerID == null) {
            throw new InvalidMessage("Can't determine ApplVerID for message");
        }

        return applVerID;
    }

}
