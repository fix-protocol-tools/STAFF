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
import com.btobits.automator.fix.utils.fix.FixMessageType;
import org.apache.tools.ant.Project;
import org.junit.Test;
import static org.junit.Assert.*;
import static org.junit.Assert.assertEquals;

import java.util.Calendar;

/**
 * @author Mykhailo_Sereda
 */
public class FixFieldTaskTest {

    @Test
    public void testGetFieldValueFromSimpleMsg() throws Exception {
        FixMessageType msg = new FixMessageType();
        msg.addField(new FixMessageType.Field("35", "X"));

        String propName = generatePropertyName("propName");
        FixFieldTask fieldTask = createFixFieldTask(msg, "35", FixFieldTask.ACTION_TYPE.GET, null, propName, null);

        assertEquals("X", fieldTask.getProject().getProperty(propName));
    }

    @Test
    public void testGetFieldValueFromRG() throws Exception {
        FixMessageType msg = new FixMessageType();
        msg.addField(new FixMessageType.Field("35", "X"));

        final String groupId = "268";

        FixMessageType.Group group1 = new FixMessageType.Group();
        group1.setName(groupId);
        group1.addField(new FixMessageType.Field("269", "0"));
        group1.addField(new FixMessageType.Field("270", "27.5"));
        msg.addGroup(group1);

        FixMessageType.Group group2 = new FixMessageType.Group();
        group2.setName(groupId);
        group2.addField(new FixMessageType.Field("269", "3"));
        group2.addField(new FixMessageType.Field("270", "55"));
        msg.addGroup(group2);

        String propName = generatePropertyName("propName");
        FixFieldTask fieldTask = createFixFieldTask(msg, "270", FixFieldTask.ACTION_TYPE.GET, null, propName, groupId+":2");

        assertEquals("55", fieldTask.getProject().getProperty(propName));
    }

    @Test
    public void testGetRGCounter() throws Exception {
        FixMessageType msg = new FixMessageType();
        msg.addField(new FixMessageType.Field("35", "X"));

        final String groupId = "268";

        FixMessageType.Group group1 = new FixMessageType.Group();
        group1.setName(groupId);
        group1.addField(new FixMessageType.Field("269", "0"));
        group1.addField(new FixMessageType.Field("270", "27.5"));
        msg.addGroup(group1);

        FixMessageType.Group group2 = new FixMessageType.Group();
        group2.setName(groupId);
        group2.addField(new FixMessageType.Field("269", "3"));
        group2.addField(new FixMessageType.Field("270", "55"));
        msg.addGroup(group2);

        String propName = generatePropertyName("propName");
        FixFieldTask fieldTask = createFixFieldTask(msg, groupId, FixFieldTask.ACTION_TYPE.GETGROUPCOUNT, null, propName, null);

        assertEquals("2", fieldTask.getProject().getProperty(propName));
    }

    @Test
    public void testSetFieldToMsgBody() throws Exception {
        FixMessageType msg = new FixMessageType();
        msg.addField(new FixMessageType.Field("35", "X"));
        String newTag = "262";
        String newValue = "valueStr";

        FixFieldTask fieldTask = createFixFieldTask(msg, newTag, FixFieldTask.ACTION_TYPE.SET, newValue, null, null);

        assertEquals("After add operation field in message must be 2 field", 2, msg.getFields().size());
        FixMessageType.Field addedField = msg.getFields().get(1);
        assertEquals(newTag, addedField.getName());
        assertEquals(newValue, addedField.getValue());
    }

    @Test
    public void testSetFieldToRG() throws Exception {
        FixMessageType msg = new FixMessageType();
        msg.addField(new FixMessageType.Field("35", "X"));
        String groupId = "268";
        String tagId = "270";
        String newValue = "55";

        FixFieldTask fieldTask = createFixFieldTask(msg, tagId, FixFieldTask.ACTION_TYPE.SET, newValue, null, groupId+":1");
        assertEquals("After add operation field in message must be 2 element", 2, msg.getFields().size());

        FixMessageType.Field addedGroup = msg.getFields().get(1);
        assertTrue("Second element in message must be Group", addedGroup instanceof FixMessageType.Group);
        assertTrue(addedGroup.isGroup());
        assertEquals(groupId, addedGroup.getName());
        assertNull(addedGroup.getValue());
        assertFalse(addedGroup.isLeading());
        assertEquals(1, addedGroup.getFields().size());

        FixMessageType.Field addedNestedField = addedGroup.getFields().get(0);
        assertEquals(tagId, addedNestedField.getName());
        assertEquals(newValue, addedNestedField.getValue());
    }

    @Test
    public void testSetFieldToInnerRG() throws Exception {
        String groupId = "711";
        String tagId = "311";
        String value = "TEST";

        String innerGroupId = "457";
        String innerTagId = "459";
        String innerTagValue = "B";

        FixMessageType msg = new FixMessageType();
        msg.addField(new FixMessageType.Field("35", "8"));

        FixMessageType.Group group = new FixMessageType.Group();
        group.setName(groupId);
        group.addField(new FixMessageType.Field(tagId, value));

        FixMessageType.Group innerGroup = new FixMessageType.Group();
        innerGroup.setName(innerGroupId);
        innerGroup.addField(new FixMessageType.Field("458", "AT0000606306"));
        group.addGroup(innerGroup);
        msg.addGroup(group);

        FixFieldTask fieldTask = createFixFieldTask(msg, innerTagId, FixFieldTask.ACTION_TYPE.SET, innerTagValue, null,
                groupId+":1/"+innerGroupId+":1");

        assertTrue("Second element in message must be Group", msg.getFields().get(1).isGroup());
        FixMessageType.Group addedGroup = (FixMessageType.Group) msg.getFields().get(1);

        assertTrue("Second element in group must be inner group", addedGroup.getFields().get(1).isGroup());
        FixMessageType.Group addedNestedGroup = (FixMessageType.Group) addedGroup.getFields().get(1);

        FixMessageType.Field addedNestedField = addedNestedGroup.getFields().get(1);
        assertEquals(innerTagId, addedNestedField.getName());
        assertEquals(innerTagValue, addedNestedField.getValue());
    }

    private FixFieldTask createFixFieldTask(final FixMessageType msg, final String fieldName,final FixFieldTask.ACTION_TYPE action,
                                            final String value, final String propertyName, final String groupRef) throws Exception {
        FixFieldTask fieldTask = new FixFieldTask();
        String sessionRef = generatePropertyName("sessionRef_");
        Project project = new Project();
        project.addReference(sessionRef, msg);
        fieldTask.setProject(project);
        fieldTask.refId = sessionRef;
        fieldTask.name = fieldName;
        fieldTask.value = value;
        fieldTask.action = action;
        fieldTask.property = propertyName;
        fieldTask.groupName = groupRef;

        fieldTask.validate();
        fieldTask.runTestInstructions();

        return fieldTask;
    }

    private String generatePropertyName(final String prefix) {
        return prefix + Long.toString(Calendar.getInstance().getTimeInMillis());
    }
}
