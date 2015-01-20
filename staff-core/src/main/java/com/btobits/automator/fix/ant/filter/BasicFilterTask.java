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

package com.btobits.automator.fix.ant.filter;

import com.btobits.automator.ant.annotation.AutoParamBool;
import com.btobits.automator.ant.annotation.AutoParamStr;
import com.btobits.automator.fix.ant.task.BasicAntTask;
import com.btobits.automator.fix.utils.fix.FixAcceptor;
import com.btobits.automator.fix.utils.fix.FixInitiator;
import com.btobits.automator.fix.utils.fix.FixSession;
import org.apache.commons.lang.StringUtils;
import org.junit.Assert;

import java.util.ArrayList;
import java.util.List;

/**
 * @author Kirill_Mukhoiarov
 */

public abstract class BasicFilterTask extends BasicAntTask {
    /**
     * Any project element can be assigned an identifier using it's id attribute.
     */
    @AutoParamStr(xmlAttr = "id")
    public String id = null;

    /**
     * Any project element can be assigned an identifier using
     * its id attribute. In most cases the element can subsequently
     * be referenced by specifying the refid attribute on an
     * element of the same type.
     */
    @AutoParamStr(xmlAttr = "refid")
    public String refId = null;

    /**
     * Filter is active.
     */
    @AutoParamBool(xmlAttr = "active")
    public Boolean active = true;

    /**
     * Filter will do debug output.
     */
    @AutoParamBool(xmlAttr = "debug")
    public Boolean debug = false;

    /**
     * Filter will do debug with full signature.
     */
    @AutoParamBool(xmlAttr = "debugFullSign")
    public Boolean debugFullSign = false;

    /**
     * Session to which is assigned this filter
     */
    protected FixSession session = null;

    /**
     * List with filter rule of this filter
     */
    protected final List<FixFilterRule> fixRules = new ArrayList<FixFilterRule>();

    @Override
    protected void validate() throws Exception {
        Assert.assertTrue("Reference to FIXAcceptor or FIXInitiator is not specified", StringUtils.isNotBlank(refId));
        final Object obj = getProject().getReference(refId);
        Assert.assertNotNull("RefId[" + refId + "]. Failed to get FIX session.", obj);

        Assert.assertTrue("RefId[" + refId + "]. FIXAcceptor or FIXInitiatorUnknown FIX session mode: "
                + obj.getClass().getSimpleName(), (obj instanceof FixInitiator) || (obj instanceof FixAcceptor));
        session = (FixSession) obj; 
        Assert.assertTrue("filter task 'id' is required parameter", StringUtils.isNotBlank(id));
    }

    public boolean isActive() {
        return active;
    }

    public boolean isDebugEnabled() {
        return debug;
    }

    @Override
    protected void runTestInstructions() throws Exception {
        if (!active) {
            log.warn(this + " is no active, skip.");
        } else {
            if (debugFullSign) {
                if (log.isTraceEnabled()) {
                    log.trace("init " + this);
                }
            }
            runTestInstructionsFilter();
        }
    }

    protected abstract void runTestInstructionsFilter() throws Exception;

    public void plusRule(final FixFilterRule fixFilterRule) throws Exception {
        if (log.isTraceEnabled()) {
            log.trace("adding next instruction: " + fixFilterRule);
        }
        if (!fixRules.contains(fixFilterRule)) {
            fixFilterRule.initFilterRule(session);
            fixRules.add(fixFilterRule);
        }
    }
}
