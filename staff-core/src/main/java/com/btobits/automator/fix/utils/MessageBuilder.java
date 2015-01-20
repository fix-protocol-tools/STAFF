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

package com.btobits.automator.fix.utils;

import com.btobits.automator.fix.utils.fix.FixMessageType;
import quickfix.*;
import quickfix.field.ApplVerID;
import quickfix.field.BeginString;
import quickfix.field.DefaultApplVerID;

import java.util.LinkedList;

/**
 * @author Volodymyr_Biloshkurs
 */
public final class MessageBuilder {

    private MessageBuilder() {
    }

    public static final Message build(final String inSender,
                                      final String inTarget,
                                      final String version,
                                      final Session inSession,
                                      final ApplVerID defaultApplVerID,
                                      final FixMessageType inMsg,
                                      final String inDefaultMsgType) throws Exception {
        return build(inSender, inTarget, version, inSession.getMessageFactory(), inSession.getDataDictionaryProvider(),
                defaultApplVerID, inMsg, inDefaultMsgType);
    }

    public static final Message build(final String inSender,
                                      final String inTarget,
                                      final String version,
                                      final MessageFactory messageFactory,
                                      final DataDictionaryProvider ddProvider,
                                      final ApplVerID defaultApplVerID,
                                      final FixMessageType inMsg,
                                      final String inDefaultMsgType) throws Exception {
        String beginString = version != null ? version : getStringField(inMsg, 8, "BeginString");
        String msgType = getMsgType(inMsg, inDefaultMsgType);
        ApplVerID applVerID;
        if (FixVersions.BEGINSTRING_FIXT11.equals(beginString)) {
            applVerID = getApplVerID(defaultApplVerID, msgType, inMsg);
        } else {
            applVerID = MessageUtils.toApplVerID(beginString);
        }

        final DataDictionary sessionDataDictionary = ddProvider == null ? null : ddProvider
                .getSessionDataDictionary(beginString);
        final DataDictionary applicationDataDictionary = ddProvider == null ? null : ddProvider
                .getApplicationDataDictionary(applVerID);

        final DataDictionary payloadDictionary = MessageUtils.isAdminMessage(msgType)
                ? sessionDataDictionary
                : applicationDataDictionary;

        return getFixMessage(inSender, inTarget, version, messageFactory, inMsg,
                sessionDataDictionary, payloadDictionary, inDefaultMsgType);
    }

    public static final Message build(final String inSender,
                                      final String inTarget,
                                      final String version,
                                      final MessageFactory messageFactory,
                                      final FixMessageType inMsg,
                                      final DataDictionary sessionDataDictionary,
                                      final DataDictionary applicationDataDictionary,
                                      final String inDefaultMsgType) throws Exception {

        return getFixMessage(inSender, inTarget, version,
                messageFactory, inMsg, sessionDataDictionary, applicationDataDictionary, inDefaultMsgType);
    }

    private static Message getFixMessage(final String inSender,
                                         final String inTarget,
                                         final String version,
                                         final MessageFactory messageFactory,
                                         final FixMessageType inMsg,
                                         final DataDictionary sessionDataDictionary,
                                         final DataDictionary applicationDataDictionary,
                                         final String inDefaultMsgType)
            throws Exception {
        try {
            final String msgType = getMsgType(inMsg, inDefaultMsgType);
            Message fixMsg = new Message();
            fixMsg.getHeader().setField(new quickfix.field.SenderCompID(inSender));
            fixMsg.getHeader().setField(new quickfix.field.TargetCompID(inTarget));
            fixMsg.getHeader().setString(BeginString.FIELD, version);

            fill(fixMsg.getHeader(), inMsg.getFields(), version, messageFactory, msgType, sessionDataDictionary, true, false, false);
            fill(fixMsg.getTrailer(), inMsg.getFields(), version, messageFactory, msgType, sessionDataDictionary, false, true, false);
            fillBody(fixMsg, inMsg.getFields(), version, messageFactory, msgType, applicationDataDictionary, sessionDataDictionary);

            return fixMsg;
        } catch (Exception e) {
            throw new Exception("Error creating message: " + e.getMessage(), e);
        }
    }

    private static String getMsgType(FixMessageType inMsg, String inDefaultMsgType) throws Exception {
        final FixMessageType.Field fldMsgType = inMsg.getMsgTypeField();
        if (fldMsgType == null && inDefaultMsgType == null) {
            throw new Exception("Cann't determinate message type");
        }
        return (fldMsgType == null) ? inDefaultMsgType : fldMsgType.getValue();
    }

    private static void fill(final FieldMap inGroup,
                             final LinkedList<FixMessageType.Field> inFields,
                             final String version,
                             final MessageFactory messageFactory,
                             final String inMsgType,
                             final DataDictionary inDictionary,
                             boolean inFillHeader,
                             boolean inFillTrailer,
                             boolean inFillBody)
            throws Exception {

        for (final FixMessageType.Field field : inFields) {
            final int fixField = FixUtils.getFieldId(field.getName());
            if (field.getValue() == null) {
                if (canFill(fixField, inMsgType, inDictionary, inFillHeader, inFillTrailer, inFillBody)) {
                    final Group subGroup = messageFactory.create(version, inMsgType, fixField);
                    if (subGroup == null) {
                        throw new Exception("Undefined group [" + fixField +
                                "] for message type [" + inMsgType + "]");
                    }

                    fill(subGroup, field.getFields(), version, messageFactory, inMsgType,
                            inDictionary, inFillHeader, inFillTrailer, inFillBody);
                    inGroup.addGroup(subGroup);
                }
            } else {
                if (canFill(fixField, inMsgType, inDictionary, inFillHeader, inFillTrailer, inFillBody)) {
                    if (!inGroup.isSetField(fixField)) {
                        inGroup.setField(new StringField(fixField, field.getValue()));
                    }
                }
            }
        }
    }

    private static final boolean canFill(int inField,
                                         final String inMsgType,
                                         final DataDictionary inDict,
                                         boolean inFillHeader,
                                         boolean inFillTrailer,
                                         boolean inFillBody) {
        if (inDict.isHeaderField(inField)) {
            return inFillHeader;
        } else if (inDict.isTrailerField(inField)) {
            return inFillTrailer;
        } else {
            return inFillBody;
        }
    }

    private static final void fillBody(final FieldMap inGroup,
                                       final LinkedList<FixMessageType.Field> inFields,
                                       final String version,
                                       final MessageFactory messageFactory,
                                       final String inMsgType,
                                       final DataDictionary appDictionary,
                                       final DataDictionary sessionDictionary) throws Exception {
        for (final FixMessageType.Field field : inFields) {
            final int fixField = FixUtils.getFieldId(field.getName());

            if (sessionDictionary.isHeaderField(fixField) ||
                    sessionDictionary.isTrailerField(fixField)) {
                continue;
            }

            boolean isGroup = false;
            if (version.endsWith("4.0") || version.endsWith("4.1") ||
                    version.endsWith("4.2")) {
                // only for fix 4.0 4.1 4.2
                isGroup = (FieldType.NumInGroup == appDictionary.getFieldTypeEnum(fixField) ||
                        appDictionary.isGroup(inMsgType, fixField)) ||
                        (field.isGroup());
            } else {
                boolean grType = appDictionary.isGroup(inMsgType, fixField);
                FieldType fType = appDictionary.getFieldTypeEnum(fixField);
                if (fType != FieldType.NumInGroup && !grType && field.isGroup()) {
                    isGroup = true; // if user write group inmeadiatly
                } else {
                    isGroup = (FieldType.NumInGroup == appDictionary.getFieldTypeEnum(fixField) ||
                            appDictionary.isGroup(inMsgType, fixField)) &&
                            (field.isGroup());
                }
            }

            if (isGroup) {
                final Group subGroup = messageFactory.create(version, inMsgType, fixField);
                if (subGroup == null) {
                    throw new Exception("Undefined group [" + fixField +
                            "] for message type [" + inMsgType + "]");
                }

                fillBody(subGroup, field.getFields(), version, messageFactory, inMsgType, appDictionary, sessionDictionary);
                inGroup.addGroup(subGroup);
            } else if (!inGroup.isSetField(fixField)) {
                inGroup.setField(new StringField(fixField, field.getValue()));
            }
        }
    }

    private static ApplVerID getApplVerID(ApplVerID defaultApplVerID, String type, FixMessageType msg)
            throws InvalidMessage {
        ApplVerID applVerID = null;

        final String applVerIdString = getStringField(msg, ApplVerID.FIELD, "ApplVerID");
        if (applVerIdString != null) {
            applVerID = new ApplVerID(applVerIdString);
        }

        if (applVerID == null) {
            applVerID = defaultApplVerID;
        }

        if (applVerID == null && "A".equals(type)) {
            final String defaultApplVerIdString = getStringField(msg, DefaultApplVerID.FIELD, "DefaultApplVerID");
            if (defaultApplVerIdString != null) {
                applVerID = new ApplVerID(defaultApplVerIdString);
            }
        }

        if (applVerID == null) {
            throw new InvalidMessage("Can't determine ApplVerID for message");
        }

        return applVerID;
    }

    private static String getStringField(FixMessageType msg, final int tagId, final String name) {
        final String tagIdStr = Integer.toString(tagId);
        for (FixMessageType.Field f : msg.getFields()) {
            if (tagIdStr.equals(f.getName()) || name.equals(f.getName())) {
                return f.getValue();
            }
        }
        return null;
    }
}

