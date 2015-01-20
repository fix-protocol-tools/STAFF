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

package com.btobits.automator.fix.ant.task;

import com.btobits.automator.ant.annotation.*;
import com.btobits.automator.ant.types.TimeValue;
import com.btobits.automator.fix.quickfix.bridge.FillHeaderTrailer;
import com.btobits.automator.fix.quickfix.bridge.FixConnectivity;
import com.btobits.automator.fix.utils.FileMessageFactory;
import com.btobits.automator.fix.utils.fix.FixMessageType;
import com.btobits.automator.fix.utils.fix.FixSession;
import com.btobits.automator.fix.utils.fix.IFixConnectivity;
import junit.framework.Assert;
import org.apache.commons.lang.StringUtils;

import java.io.File;
import java.util.LinkedList;
import java.util.concurrent.TimeUnit;

/**
 * @author Kirill_Mukhoiarov
 */

@AutoParamBean
public class FixSendTask extends BasicAntTask {
    public final static String svnSignature = "$$Rev: 67588 $$ $$Date: 2014-10-28 14:02:42 +0200 (Вт, 28 окт 2014) $$ $$LastChangedBy: Alexander_Sereda $$";

    /**
     * Any project element can be assigned an identifier using
     * its id attribute. In most cases the element can subsequently
     * be referenced by specifying the refid attribute on an
     * element of the same type.
     */
    @AutoParamStr(xmlAttr = "refid")
    public String refId = null;

    /**
     * FIX Session SenderCompID.
     */
    @Deprecated
    @AutoParamStr(xmlAttr = "sender")
    public String senderCompId = null;

    /**
     * FIX Session TargetCompID.
     */
    @Deprecated
    @AutoParamStr(xmlAttr = "target")
    public String targetCompId = null;

    /**
     * Session's FIX Protocol version.
     */
    @Deprecated
    @AutoParamStr(xmlAttr = "type")
    public String fixVersion = null;

    /**
     * How many times repeat this operation.
     */
    @AutoParamLong(xmlAttr = "repeat")
    public long numberOfOperationRepeat = 1;

    /**
     * How many times send each message.
     */
    @AutoParamLong(xmlAttr = "count")
    public long numberOfMessageCopies = 1;

    /**
     * Time to wait between performing operation.
     */
    @AutoParamTimeValue(xmlAttr = "repeatDelay")
    public TimeValue pauseBetweenOperationRepeat = new TimeValue();

    /**
     * Time to wait between message send operations.
     */
    @AutoParamTimeValue(xmlAttr = "sendDelay")
    public TimeValue pauseBetweenMsgSend = new TimeValue();

    /**
     * Validate outgoing messages before send or not.
     */
    @AutoParamBool(xmlAttr = "validateOutgoing")
    public Boolean validateOutgoing = false;

    /**
     * File containing FIX messages to be sent name. FIX *.in and *.out logs can be used
     * without pre-processing. If file is specified, then fixSend task shouldn't contain fixMessage inside.
     */
    @AutoParamStr(xmlAttr = "file")
    public String fileWithRawMessages = null;

    /**
     * FIX messages types to be skipped wile batch send from file. If not specified all messages will be sent.
     */
    @AutoParamStr(xmlAttr = "skip")
    public String skipMessageTypes = "";

    /**
     *
     */
    @AutoParamEnum(xmlAttr = "fillHeaderTrailer", enumClass = FillHeaderTrailer.class)
    public FillHeaderTrailer fillHeaderTrailer = FillHeaderTrailer.ONLYREQ;

    private final LinkedList<FixMessageType> messages = new LinkedList<FixMessageType>();
    private FixConnectivity conn = null;
    private FixSession fixSession;
    private boolean fileMode;

    public void addFixMessage(FixMessageType msg) {
        messages.addLast(msg);
    }

    @Override
    protected void validate() throws Exception {
        Assert.assertTrue("Reference to FIX session is not specified", StringUtils.isNotBlank(refId));
        final Object obj = getProject().getReference(refId);
        Assert.assertNotNull("RefId[" + refId + "]. Failed to get FIX session.", obj);
        Assert.assertTrue("RefId[" + refId + "]. Unknown FIX session mode: "
                + obj.getClass().getSimpleName(), (obj instanceof FixSession));

        fixSession = (FixSession) obj;
//        senderCompId = fixSession.getSenderCompId();
//        targetCompId = fixSession.getTargetCompId();
//        fixVersion = fixSession.getFixVersion();
        conn = fixSession.getConnectivity();
        Assert.assertNotNull("RefId[" + refId + "]. getConnectivity() return NULL", conn);

        Assert.assertNotNull("'fillHeaderTrailer' is null. " +
                "Valid value list: full||onlyReq||none.", fillHeaderTrailer);
        if (StringUtils.isNotBlank(fileWithRawMessages)) {
            Assert.assertTrue("RefId[" + refId + "]. File with FIX messages is not found: [" + fileWithRawMessages +
                    "]", new File(fileWithRawMessages).exists());
            fileMode = true;
        } else {
            Assert.assertTrue("RefId[" + refId + "]. FIX messages pool(task child tags) is empty.", !messages.isEmpty());
            fileMode = false;
        }
    }

    @Override
    protected void runTestInstructions() throws Exception {
        for (long i = 0; i < numberOfOperationRepeat; i++) {
            if (fileMode) {
                final FileMessageFactory messageFactory = new FileMessageFactory(fileWithRawMessages, skipMessageTypes);
                messageFactory.load();
                for (final String message : messageFactory.getMessages()) {
                    sendMessage(message);
                }
            } else {
                for (final FixMessageType msg : messages) {
                    for (int c = 0; c < numberOfMessageCopies; ++c) {
                        if (StringUtils.isBlank(msg.getData())) {
                            conn.sendMessage(fixSession.getSessionID(), msg, validateOutgoing);
                            if (pauseBetweenMsgSend.isNotZero()) {
                                Thread.sleep(TimeUnit.MILLISECONDS.convert(pauseBetweenMsgSend.getDuration(), pauseBetweenMsgSend.getUnit()));
                            }

                        } else {
                            sendMessage(msg.getData());
                        }
                    }
                }
            }
            if (pauseBetweenOperationRepeat.isNotZero()) {
                Thread.sleep(TimeUnit.MILLISECONDS.convert(pauseBetweenOperationRepeat.getDuration(), pauseBetweenOperationRepeat.getUnit()));
            }
        }
    }

    private void sendMessage(final String inMessage) throws Exception {
        conn.sendRawMessage(fixSession.getSessionID(), inMessage, validateOutgoing, fillHeaderTrailer);
        if (pauseBetweenMsgSend.isNotZero()) {
            Thread.sleep(TimeUnit.MILLISECONDS.convert(pauseBetweenMsgSend.getDuration(), pauseBetweenMsgSend.getUnit()));
        }
    }
}
