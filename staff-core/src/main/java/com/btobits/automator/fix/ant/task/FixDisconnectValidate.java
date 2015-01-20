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
import com.btobits.automator.ant.annotation.AutoParamStr;
import com.btobits.automator.fix.exception.FixEngineException;
import junit.framework.Assert;
import org.apache.commons.lang.StringUtils;
import quickfix.Session;

import java.util.concurrent.CountDownLatch;

/**
 * @author Kirill_Mukhoiarov
 */

@AutoParamBean
public final class FixDisconnectValidate extends BasicAntTask {
    public final static String svnSignature = "$$Rev: 67588 $$ $$Date: 2014-10-28 14:02:42 +0200 (Вт, 28 окт 2014) $$ $$LastChangedBy: Alexander_Sereda $$";

    /**
     * Any project element can be assigned an identifier using
     * its id attribute. In most cases the element can subsequently
     * be referenced by specifying the refid attribute on an
     * element of the same type.
     */
    @AutoParamStr(xmlAttr = "refid")
    public String refId = null;

    private FixDisconnectExpect expectInstance = null;

    @Override
    protected void validate() {
        Assert.assertTrue( "Reference to FixDisconnectExpect is not specified", StringUtils.isNotBlank( refId ) );
        final Object obj = getProject().getReference( refId );
        Assert.assertNotNull( "RefId[" + refId + "]. Failed to get FixDisconnectExpect instance.", obj );
        Assert.assertTrue( "RefId[" + refId + "]. Referred object is not FixDisconnectExpect instance: "
                + obj.getClass().getSimpleName(), ( obj instanceof FixDisconnectExpect ) );
        expectInstance = (FixDisconnectExpect) obj;
    }

    @Override
    protected void runTestInstructions() throws Exception {
        final String sessionID = expectInstance.getSessionID();
        if (expectInstance.isDisconnectHappen()) {
            // test passed
            log("Session [" + sessionID + "] has been disconnected");
        } else {
            throw new FixEngineException("Session [" + sessionID + "] - no disconnect in " + expectInstance.timeout);
        }
    }
}
