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

import com.btobits.automator.ant.annotation.AutoParamBean;
import com.btobits.automator.ant.annotation.AutoParamBool;
import com.btobits.automator.ant.annotation.AutoParamLong;
import com.btobits.automator.ant.annotation.AutoParamStr;
import com.btobits.automator.ant.annotation.AutoParamTimeValue;
import com.btobits.automator.ant.types.TimeValue;
import com.btobits.automator.fix.quickfix.bridge.FixConnectivity;
import com.btobits.automator.fix.utils.fix.FixMessageType;
import com.btobits.automator.fix.utils.fix.FixSession;
import org.apache.commons.lang.StringUtils;
import org.junit.Assert;

import java.util.LinkedList;

/**
 * @author Kirill_Mukhoiarov
 */

@AutoParamBean
public class FixReceiveTask extends BasicAntTask {
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
     * Incoming message wait timeout.
     */
    @AutoParamTimeValue(xmlAttr = "timeout")
    public TimeValue receiveTimeout = new TimeValue();

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
     * Validate message's header or not.
     */
    @AutoParamBool(xmlAttr = "backcheckheader")
    public Boolean backCheckHeader = true;

    /**
     * Validate message's trailer or not.
     */
    @AutoParamBool(xmlAttr = "backchecktrailer")
    public Boolean backCheckTrailer = true;

    /**
     * Validate message's body or not.
     */
    @AutoParamBool(xmlAttr = "backcheckbody")
    public Boolean backCheckBody = true;

    /**
     * Validate arrived incoming messages or not.
     */
    @AutoParamBool(xmlAttr = "validateIncoming")
    public Boolean validateIncoming = true;

    /**
     * Validate repeating group entities order or not.
     */
    @AutoParamBool(xmlAttr = "strictordering")
    public Boolean strictOrdering = false;

    private final LinkedList<FixMessageType> messages = new LinkedList<FixMessageType>();
    private FixConnectivity conn = null;
    private FixSession fixSession = null;

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

        conn = fixSession.getConnectivity();
        Assert.assertNotNull("RefId[" + refId + "]. getConnectivity() return NULL", conn);
    }

    @Override
    protected void runTestInstructions() throws Exception {
        for (long i = 0; i < numberOfOperationRepeat; ++i) {
            if (messages.size() > 0) {
                for (final FixMessageType msg : messages) {
                    for (long c = 0; c < numberOfMessageCopies; c++) {
                        compare(msg);
                    }
                }
            } else {
                for (long c = 0; c < numberOfMessageCopies; c++) {
                    compare(null);
                }
            }
        }
    }

    private void compare(final FixMessageType inMessage) throws Exception {
        conn.compareMessage2(fixSession.getSessionID(), receiveTimeout, inMessage, backCheckHeader,
                backCheckTrailer, backCheckBody, strictOrdering, validateIncoming);
    }
}
