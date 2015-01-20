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
import com.btobits.automator.ant.annotation.AutoParamBool;
import com.btobits.automator.ant.annotation.AutoParamStr;
import com.btobits.automator.fix.ant.task.BasicAntTask;
import com.btobits.automator.fix.utils.fix.FixMessageConverter;
import com.btobits.automator.fix.utils.fix.FixSession;
import com.btobits.automator.fix.utils.fix.MessageConverter;
import org.apache.commons.lang.StringUtils;
import org.junit.Assert;
import quickfix.*;

import java.util.LinkedList;
import java.util.List;

/**
 * @author Kirill_Mukhoiarov
 */

@AutoParamBean
public class FixFilterRule extends BasicAntTask {
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
     * Rule is active.
     */
    @AutoParamBool(xmlAttr = "active")
    public Boolean active = true;

    /**
     * Filter to switch will be added this filter rule if filter rule is active
     */
    private BasicFilterTask papaFilter = null;

    /**
     * List of field matcher
     */
    private final LinkedList<FixFieldRule> fieldRules = new LinkedList<FixFieldRule>();

    /**
     * List of actions
     */
    private final LinkedList<FixFilterAction> filterActions = new LinkedList<FixFilterAction>();

    /**
     * Filter counter. Apply after check all field Rules
     */
    private FixFilterCounter filterCounter;

    public void addFixFilterMatchField(final FixFieldRule rule) {
        fieldRules.add(rule);
    }

    public void addFixFilterAction(final FixFilterAction action) {
        filterActions.add(action);
    }

    public void addFixFilterMatchCounter(FixFilterCounter filterCounter) {
        if (this.filterCounter == null) {
            this.filterCounter = filterCounter;
        } else {
            throw new IllegalArgumentException("Filter counter already been assigned. It can be assigned only once");
        }
    }

    @Override
    protected void validate() throws Exception {
        Assert.assertTrue("Reference to FIXFilter is not specified", StringUtils.isNotBlank(refId));
        final Object obj = getProject().getReference(refId);
        Assert.assertNotNull("RefId[" + refId + "]. Failed to get FIXFilter instance.", obj);

        Assert.assertTrue("RefId[" + refId + "]. Referenced object is not FIXFilter instance: "
                + obj.getClass().getSimpleName(), (obj instanceof BasicFilterTask));
        papaFilter = (BasicFilterTask) obj;
        Assert.assertNotNull("RefId[" + refId + "]. Referenced object was not initialized.", papaFilter);
        if (papaFilter.active) {
            Assert.assertFalse("FixFilter debug is OFF, no actions - this filter configuration have no sense, " +
                    "please change filter configuration or set 'active=\"false\"'.",
                    (active && filterActions.isEmpty() && !papaFilter.debug));
        }
        for(FixFieldRule filedRule : fieldRules) {
            filedRule.validate();
        }
        if (filterCounter!=null) {
            filterCounter.validate();
        }
        for(FixFilterAction action : filterActions) {
            action.validate();
        }
    }

    @Override
    protected void runTestInstructions() throws Exception {
        if (active) {
            papaFilter.plusRule(this);
        }
    }

    public boolean isActive() {
        return active;
    }

    private MessageConverter messageConverter;

    public void initFilterRule(FixSession session) throws Exception {
        messageConverter = createMessageConverter(session);
        initFilterActions(messageConverter);
    }

    protected MessageConverter createMessageConverter(FixSession session) {
        SessionID sessionID = session.getSessionID();
        //TODO: make online message converter
         return new FixMessageConverter(session.getDataDictionaryProvider(), null, // session is not started. we don't know targetDefaultApplicationVersionID
                 session.getMessageFactory(), sessionID.getSenderCompID(), sessionID.getTargetCompID(), sessionID.getBeginString());
    }

    protected void initFilterActions(MessageConverter messageConverter) throws Exception {
        for(FixFilterAction action : filterActions) {
            action.initAction(messageConverter);
        }
    }

    public List<Message> actionFixMsg(final List<Message> messages) {
        if (log.isDebugEnabled()) {
            log.debug("Filter action on message: " + messages.toString());
        }
        List<Message> result = new LinkedList<Message>(messages);
        for (FixFilterAction filterAction : filterActions) {
            result = filterAction.doFixMsgAction(messages);
        }
        if (log.isDebugEnabled()) {
            log.debug("Message after actions: " + result.toString());
        }
        return result;
    }

    public List<String> actionStrMsg(List<String> messages) {
        if (log.isDebugEnabled()) {
            log.debug("Filter action on message: " + messages.toString());
        }
        List<String> result = new LinkedList<String>(messages);
        for (FixFilterAction filterAction : filterActions) {
            result = filterAction.doStrMsgAction(messages);
        }
        if (log.isDebugEnabled()) {
            log.debug("Message after actions: " + result.toString());
        }
        return result;
    }

    public boolean filter(final Message message) {
        if (log.isTraceEnabled()) {
            log.trace("Filter message: " + message);
        }

        return checkFilterRules(message) && checkFilterCounter();
    }

    public boolean filter(final String message) throws InvalidMessage {
        if (checkNeedFilter()) {
            if (log.isTraceEnabled()) {
                log.trace("Parse string message: " + message);
            }
            Message msg = parseMessage(message);
            if (log.isTraceEnabled()) {
                log.trace("Filter string message as fix message");
            }
            return filter(msg);
        } else {
            if (log.isDebugEnabled()) {
                log.debug("Filter rule don't have filters - filter return true");
            }
            return true;
        }
    }

    protected boolean checkFilterRules(final Message message) {
        for(FixFieldRule rule : fieldRules) {
            if (log.isTraceEnabled()) {
                log.trace("Check field rule: " + rule);
            }
            if (!rule.checkMsgRule(message)) {
                if (log.isTraceEnabled()) {
                    log.trace("Field rule is fail. Rule: " + rule);
                }
                return false;
            }
        }
        return true;
    }

    protected boolean checkFilterCounter() {
        if(filterCounter!=null) {
            boolean counterResult = filterCounter.checkCounter();
            if (!counterResult) {
                if (log.isTraceEnabled()) {
                    log.trace("Filter counter return false. FilterCounter: " + filterCounter.toString());
                }
            }
            return counterResult;
        } else {
            return true;
        }
    }

    private boolean checkNeedFilter() {
        return !fieldRules.isEmpty() || filterCounter!=null;
    }

    protected Message parseMessage(final String msgStr) throws InvalidMessage {
        return messageConverter.parseMessage(msgStr);
    }
}
