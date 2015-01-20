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
import com.btobits.automator.ant.annotation.AutoParamStr;
import com.btobits.automator.ant.annotation.AutoParamTimeValue;
import com.btobits.automator.ant.types.TimeValue;
import com.btobits.automator.fix.quickfix.bridge.FixConnectivity;
import com.btobits.automator.fix.quickfix.bridge.IConnectionListener;
import com.btobits.automator.fix.utils.fix.FixSession;
import junit.framework.Assert;
import org.apache.commons.lang.StringUtils;
import quickfix.Session;
import quickfix.SessionID;

import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

/**
 * @author Kirill_Mukhoiarov
 */

@AutoParamBean
public final class FixDisconnectExpect extends BasicAntTask implements IConnectionListener {
    public final static String svnSignature = "$$Rev: 67588 $$ $$Date: 2014-10-28 14:02:42 +0200 (Вт, 28 окт 2014) $$ $$LastChangedBy: Alexander_Sereda $$";

    /**
     * Any project element can be assigned an identifier using
     * its id attribute. In most cases the element can subsequently
     * be referenced by specifying the refid attribute on an
     * element of the same type.
     */
    @AutoParamStr(xmlAttr = "refid")
    public String refId = null;

    @AutoParamTimeValue
    public TimeValue timeout = null;

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

    private FixConnectivity conn = null;
    private FixSession fixSession = null;

    private final CountDownLatch waitDoneSignal = new CountDownLatch(1);

    private long startTime = 0L;

    @Override
    protected void validate() {
        Assert.assertTrue("Reference to FIX session is not specified", StringUtils.isNotBlank(refId));
        final Object obj = getProject().getReference(refId);
        Assert.assertNotNull("RefId[" + refId + "]. Failed to get FIX session.", obj);
        Assert.assertTrue("RefId[" + refId + "]. Unknown FIX session mode: "
                + obj.getClass().getSimpleName(), (obj instanceof FixSession));
        fixSession = (FixSession) obj;
        // get FIX connectivity object
        conn = fixSession.getConnectivity();
        Assert.assertNotNull("RefId[" + refId + "]. getConnectivity() return NULL", conn);
    }

    @Override
    protected void runTestInstructions() throws Exception {
        Assert.assertTrue("FIX session with parameters [" + fixSession.getSessionID() + "] is not active", FixConnectivity.isSessionActive(fixSession.getSessionID()));
        conn.addConnectionListener(this);
        startTime = System.nanoTime();
    }

    public String getSessionID() {
        return fixSession.getSessionID().toString();
    }

    public boolean isDisconnectHappen() {
        try {
            final long sleepTime = TimeUnit.NANOSECONDS.convert(timeout.getDuration(), timeout.getUnit()) - (System.nanoTime() - startTime);
            log("Session [" + getSessionID() + "] disconnect waiting for period: " + TimeUnit.MILLISECONDS.convert(sleepTime, TimeUnit.NANOSECONDS) + " ms");
            return waitDoneSignal.await(sleepTime, TimeUnit.NANOSECONDS);
        } catch (InterruptedException e) {
            return false;
        } finally {
            conn.removeConnectionListener(this);
        }
    }

    @Override
    public void sessionConnected(final List<SessionID> inSessionIDList) {
    }

    @Override
    public void sessionDisconnected(final List<SessionID> inSessionIDList) {
        for (final SessionID nextSessionID : inSessionIDList) {
            if (nextSessionID.equals(fixSession.getSessionID())) {
                waitDoneSignal.countDown();
            }
        }
    }
}
