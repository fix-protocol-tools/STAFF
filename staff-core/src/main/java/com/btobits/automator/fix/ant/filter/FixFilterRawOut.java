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

import com.btobits.automator.ant.annotation.AutoParamBean;
import org.junit.Assert;
import quickfix.InvalidMessage;
import quickfix.SessionID;
import quickfix.mina.filter.IRawMsgFilterOut;
import quickfix.mina.filter.SkipMessageSignal;

import java.util.LinkedList;
import java.util.List;

/**
 * @author Kirill_Mukhoiarov
 */

@AutoParamBean
public final class FixFilterRawOut extends BasicFilterTask implements IRawMsgFilterOut {
    public final static String svnSignature = "$$Rev: 67588 $$ $$Date: 2014-10-28 14:02:42 +0200 (Вт, 28 окт 2014) $$ $$LastChangedBy: Alexander_Sereda $$";

    @Override
    protected void runTestInstructionsFilter() throws Exception {
        session.setRawFilter(this);
    }

    @Override
    public List<String> filter(final SessionID inSessionID, final String inRawMessageString) throws SkipMessageSignal {
        if (debug) {
            if (log.isDebugEnabled()) {
                log.debug(this + "[" + inSessionID + "]: " + inRawMessageString);
            }
        }
        List<String> result = new LinkedList<String>();
        result.add(inRawMessageString);
        for(FixFilterRule rule: fixRules) {
            if (rule.active) {
                if (isFilterOk(inRawMessageString, rule)) {
                    result = rule.actionStrMsg(result);
                }
            }
        }
        return result;
    }

    protected boolean isFilterOk(final String message, final FixFilterRule rule) {
        try {
            return rule.filter(message);
        } catch (InvalidMessage invalidMessage) {
            // FIXME: stop test
            log.error(invalidMessage.getMessage(), invalidMessage);
            return false;
        }
    }

    @Override
    public String toString() {
        return "raw_out{id=" + id +
                (debugFullSign ? ", papa.id=" + refId + ", papa.is="
                        + (session == null ? "N/A" : session.getClass().getSimpleName()): "") + "}";
    }
}
