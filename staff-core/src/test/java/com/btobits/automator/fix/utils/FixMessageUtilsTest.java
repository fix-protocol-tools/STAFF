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

package com.btobits.automator.fix.utils;

import org.junit.Test;
import quickfix.Field;
import quickfix.Group;
import quickfix.Message;

import java.util.List;

import static org.junit.Assert.*;
import static org.junit.Assert.assertEquals;

/**
 * @author Mykhailo_Sereda
 */
public class FixMessageUtilsTest {

    @Test
    public void testNotFoundFiledInMsgBody() {
        int tag = 22;
        String fieldValue = "StringValue";
        Message msg = createDefaultMessage();
        msg.setString(tag, fieldValue);
        List<Field> fields = FixMessageUtils.findFieldsInMsg(456, msg);
        assertNull("Message="+tag+" don't have field with tag="+tag, fields);
    }

    @Test
    public void testFindFiledInMsgBody() {
        int tag = 22;
        String fieldValue = "StringValue";
        Message msg = createDefaultMessage();
        msg.setString(tag, fieldValue);
        List<Field> fields = FixMessageUtils.findFieldsInMsg(tag, msg);
        assertResult(fields, msg, tag, fieldValue);
    }

    @Test
    public void testFindFiledInBlockMsgBody() {
        int tag = 250;
        String fieldValue = "StringValue";
        Message msg = createDefaultMessage();
        Group group = new Group(555, 1);
        group.setString(tag, fieldValue);
        msg.addGroup(group);
        List<Field> fields = FixMessageUtils.findFieldsInMsg(tag, msg);
        assertResult(fields, msg, tag, fieldValue);
    }

    @Test
    public void testFind2FiledsInBlockMsgBody() {
        int tag = 250;
        String fieldValue1 = "StringValue1";
        String fieldValue2 = "StringValue1";

        Message msg = createDefaultMessage();

        Group group1 = new Group(555, 2);
        group1.setString(tag, fieldValue1);
        msg.addGroup(group1);

        Group group2 = new Group(555, 2);
        group2.setString(tag, fieldValue2);
        msg.addGroup(group2);

        List<Field> fields = FixMessageUtils.findFieldsInMsg(tag, msg);

        assertNotNull("Field with tag="+tag+" was not found in message="+msg, fields);
        assertEquals("Must be 2 filed with tag="+tag+" in message="+msg, 2, fields.size());

        Field filed1 = fields.get(0);
        Field filed2 = fields.get(1);
        assertEquals(fieldValue1, filed1.getObject());
        assertEquals(fieldValue2, filed2.getObject());
    }

    @Test
    public void testFindFiledInMsgHeader() {
        int tag = 22;
        String fieldValue = "StringValue";
        Message msg = createDefaultMessage();
        msg.getHeader().setString(tag, fieldValue);
        List<Field> fields = FixMessageUtils.findFieldsInMsg(tag, msg);
        assertResult(fields, msg, tag, fieldValue);
    }

    @Test
    public void testFindFiledInBlockMsgHeader() {
        int tag = 250;
        String fieldValue = "StringValue";
        Message msg = createDefaultMessage();
        Group group = new Group(555, 1);
        group.setString(tag, fieldValue);
        msg.getHeader().addGroup(group);
        List<Field> fields = FixMessageUtils.findFieldsInMsg(tag, msg);
        assertResult(fields, msg, tag, fieldValue);
    }

    @Test
    public void testFindFiledInMsgTrailer() {
        int tag = 22;
        String fieldValue = "StringValue";
        Message msg = createDefaultMessage();
        msg.getTrailer().setString(tag, fieldValue);
        List<Field> fields = FixMessageUtils.findFieldsInMsg(tag, msg);
        assertResult(fields, msg, tag, fieldValue);
    }

    protected void assertResult(List<Field> fields, Message msg, int tag, String value) {
        assertNotNull("Fields with tag=" + tag + " was not found in message=" + msg, fields);
        assertFalse("Fields with tag=" + tag + " was not found in message=" + msg, fields.isEmpty());
        assertEquals("Must be found one filed with tag=" + tag + " in message=" + msg, 1, fields.size());
        Field field = fields.get(0);
        assertNotNull("Field with tag=" + tag + " was not found in message=" + msg, field);
        assertEquals("Must be found field with tag=" + tag + " but was found field with tag=" + field.getTag(), tag, field.getTag());
        assertEquals("Value of field with tag=" + tag + " is not equals with expected", value, field.getObject().toString());
    }

    protected Message createDefaultMessage() {
        Message msg = new Message();
        msg.setString(8, "FIX.4.4");
        msg.setChar(34, 'C');
        return msg;
    }

}

