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

import com.btobits.automator.ant.annotation.AutoParamStr;
import com.btobits.automator.fix.utils.fix.IControl;
import junit.framework.Assert;
import org.apache.commons.lang.StringUtils;

/**
 * @author Volodymyr_Biloshkurs
 */
public class SMTPStartTask extends BasicAntTask {
     /**
     * Any project element can be assigned an identifier using
     * its id attribute. In most cases the element can subsequently
     * be referenced by specifying the refid attribute on an
     * element of the same type.
     */
    @AutoParamStr(xmlAttr = "refid")
    public String refId = null;

    @Override
    protected void validate() throws Exception {
        Assert.assertTrue("Reference to SMTP acceptor is not specified", StringUtils.isNotBlank(refId));
    }

    @Override
    protected void runTestInstructions() throws Exception {
        final Object obj = getProject().getReference(refId);
        Assert.assertNotNull("RefId[" + refId + "]. Failed to get SMTP acceptor.", obj);
        Assert.assertTrue("RefId[" + refId + "]. Unknown SMTP acceptor mode: "
                + obj.getClass().getSimpleName(), (obj instanceof IControl));
        ((IControl) obj).start();
    }
}
