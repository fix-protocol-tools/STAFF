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
import com.btobits.automator.ant.annotation.AutoParamEnum;
import com.btobits.automator.ant.annotation.AutoParamStr;
import com.btobits.automator.fix.quickfix.bridge.ControlLevel;
import com.btobits.automator.fix.quickfix.bridge.FixConnectivity;
import com.btobits.automator.fix.utils.fix.FixSession;
import org.apache.commons.lang.StringUtils;
import org.junit.Assert;

/**
 * @author Mykhailo_Sereda
 */
@AutoParamBean
public class SetControlLevel extends BasicAntTask {

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
     * TODO: add description
     */
    @AutoParamEnum(xmlAttr = "controlLevel", enumClass = ControlLevel.class)
    public ControlLevel controlLevel;

    private FixSession fixSession = null;
    private FixConnectivity conn = null;

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
        Assert.assertNotNull("'controlLevel' is required parameter. " +
                "Valid value list: application||session||both.", controlLevel);
    }

    @Override
    protected void runTestInstructions() throws Exception {
        conn.setControlLevel(fixSession.getSessionID(), controlLevel);
    }
}
