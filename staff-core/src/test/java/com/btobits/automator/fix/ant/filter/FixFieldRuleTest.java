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

import org.junit.Test;
import static org.junit.Assert.*;

import quickfix.Group;
import quickfix.Message;

/**
 * @author Mykhailo_Sereda
 */
public class FixFieldRuleTest {

    @Test
    public void testReqFalseFiledIsAbsent() throws Exception {
        FixFieldRule instance = createInstance(55, "aaa", FilterFiledCondition.EQUALS, false);
        Message msg = createMessage();
        assertTrue("Result must be true. Message: " + msg + ", FiledRule: " + instance, instance.checkMsgRule(msg));
    }

    @Test
    public void testReqFalseFiledIsPresentConditionIsTrue() throws Exception {
        FixFieldRule instance = createInstance(55, "a", FilterFiledCondition.EQUALS, false);
        Message msg = createMessage();
        msg.setChar(55, 'a');
        assertTrue("Result must be true. Message: " + msg + ", FiledRule: " + instance, instance.checkMsgRule(msg));
    }

    @Test
    public void testReqFalseFiledIsPresentConditionIsFalse() throws Exception {
        FixFieldRule instance = createInstance(55, "a", FilterFiledCondition.EQUALS, false);
        Message msg = createMessage();
        msg.setChar(55, 'b');
        assertFalse("Result must be false. Message: " + msg + ", FiledRule: " + instance, instance.checkMsgRule(msg));
    }

    @Test
    public void testConditionIsFalse() throws Exception {
        FixFieldRule instance = createInstance(55, "a", FilterFiledCondition.EQUALS, true);
        Message msg = createMessage();
        msg.setChar(55, 'b');
        assertFalse("Result must be false. Message: " + msg + ", FiledRule: " + instance, instance.checkMsgRule(msg));
    }

    @Test
    public void testConditionIsTrue() throws Exception {
        FixFieldRule instance = createInstance(55, "a", FilterFiledCondition.NOT_EQUALS, true);
        Message msg = createMessage();
        msg.setChar(55, 'b');
        assertTrue("Result must be true. Message: " + msg + ", FiledRule: " + instance, instance.checkMsgRule(msg));
    }

    @Test
    public void testFiledInMsgHeaderConditionIsTrue() throws Exception {
        FixFieldRule instance = createInstance(49, "sender", FilterFiledCondition.EQUALS, true);
        Message msg = createMessage();
        msg.getHeader().setString(49, "sender");
        assertTrue("Result must be true. Message: " + msg + ", FiledRule: " + instance, instance.checkMsgRule(msg));
    }

    @Test
    public void testSearchFiledInGroupMsgBody() throws Exception {
        FixFieldRule instance = createInstance(58, "Line of Text 2", "LinesOfText:2", FilterFiledCondition.EQUALS, true);
        Message msg = createMessage();
        Group group1 = new Group(33,58);
        group1.setString(58, "Line of Text 1");
        msg.addGroup(group1);
        Group group2 = new Group(33,58);
        group2.setString(58, "Line of Text 2");
        msg.addGroup(group2);
        assertTrue("Result must be true. Message: " + msg + ", FiledRule: " + instance, instance.checkMsgRule(msg));
    }

    @Test
    public void testSearchFiledInNestedGroupOfGroupMsgBody() throws Exception {
        FixFieldRule instance = createInstance(455, "Line of Text 1", "LinesOfText:1/NoSecurityAltID:1", FilterFiledCondition.EQUALS, true);
        Message msg = createMessage();
        Group group1 = new Group(33, 58);
        group1.setString(58, "some line");
        Group nestedGroup = new Group(454, 455);
        nestedGroup.setString(455, "Line of Text 1");
        group1.addGroup(nestedGroup);
        msg.addGroup(group1);
        assertTrue("Result must be true. Message: " + msg + ", FiledRule: " + instance, instance.checkMsgRule(msg));
    }

    @Test
    public void testSearchFiledInGroupButFiledNotInGroup() throws Exception {
        FixFieldRule instance = createInstance(58, "Line of Text 1", "NoRelatedSym:1", FilterFiledCondition.EQUALS, true);
        Message msg = createMessage();
        msg.setString(58, "Line of Text 1");
        Group group1 = new Group(146,55);
        group1.setString(55, "A");
        msg.addGroup(group1);
        assertFalse("Result must be false. Message: " + msg + ", FiledRule: " + instance, instance.checkMsgRule(msg));
    }


    private Message createMessage() {
        Message msg = new Message();
        msg.setString(8, "FIX.4.4");
        msg.setChar(34, 'C');
        return msg;
    }

    private FixFieldRule createInstance(int tag, String ruleValue, FilterFiledCondition condition, boolean required) throws Exception {
        return createInstance(tag, ruleValue, null, condition, required);
    }

    private FixFieldRule createInstance(int tag, String ruleValue, String group, FilterFiledCondition condition, boolean required) throws Exception {
        FixFieldRule instance = new FixFieldRule();
        instance.tag = tag;
        instance.valueRule = ruleValue;
        instance.groupName = group;
        instance.fieldCondition = condition;
        instance.required = required;
        instance.validate();
        return instance;
    }
}
