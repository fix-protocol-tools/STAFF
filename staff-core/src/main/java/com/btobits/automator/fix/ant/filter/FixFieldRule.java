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

import com.btobits.automator.ant.annotation.*;
import com.btobits.automator.fix.ant.container.GroupDescription;
import com.btobits.automator.fix.ant.task.BasicAntTask;
import com.btobits.automator.fix.utils.FixMessageUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.builder.ToStringBuilder;
import org.apache.tools.ant.BuildException;
import org.junit.Assert;
import quickfix.*;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

/**
 * Created by IntelliJ IDEA.
 * User: Mykhailo_Sereda
 * Date: 7.12.2010
 * Time: 13:07:45
 * To change this template use File | Settings | File Templates.
 */
@AutoParamBean
public class FixFieldRule extends BasicAntTask {

    /**
     * Field tag which will be looking in filtered message
     */
    @AutoParamLong(xmlAttr = "tag")
    public long tag;

    @AutoParamStr(xmlAttr = "value")
    public String valueRule;

    @AutoParamEnum(xmlAttr = "condition", enumClass = FilterFiledCondition.class)
    public FilterFiledCondition fieldCondition;

    @AutoParamBool(xmlAttr = "req", defaultValue = true)
    public boolean required;

    /**
     * TODO: add description
     */
    @AutoParamStr(xmlAttr = "group" )
    public String groupName = null;

    private LinkedList<GroupDescription> groupDescription;

    protected void validate() throws Exception {
        Assert.assertTrue("Field rule 'tag' is required parameter and must be more than 0", tag > 0);
        Assert.assertTrue("Field rule 'value' is required parameter", StringUtils.isNotBlank(valueRule));
        Assert.assertNotNull("Field rule 'condition' is required parameter", fieldCondition);
        if (groupName != null) {
            initGroupDescr(groupName);
            // TODO: make check list 'group' - must be not empty
        }
    }

    @Override
    protected void runTestInstructions() throws Exception {
        // do nothing
    }

    public boolean checkMsgRule(final Message message) {
        List<String> listResult;
        if (isGroupAssigned()) {
            listResult = new ArrayList<String>();
            String value = findValue(message);
            if (value!=null) {
                listResult.add(value);
            }
        } else {
            listResult= findValues(message);
        }
        return checkFieldValueRule(listResult);
    }

    private boolean isGroupAssigned() {
        return groupDescription == null ? false : !groupDescription.isEmpty();
    }

    protected boolean checkFieldValueRule(List<String> valueFromMsg) {
        if (valueFromMsg != null && !valueFromMsg.isEmpty()) {
            return compareAtLeastOne(valueFromMsg);
        } else {
            // if filed is required, but it is absent than return false
            // if filed is not required anf it is absent than return true
            return !required;
        }
    }

    protected boolean compareAtLeastOne(List<String> valuesFromMsg) {
        for (String strValue : valuesFromMsg) {
            if (fieldCondition.compare(strValue, valueRule)) {
                return true;
            }
        }
        return false;
    }

    protected String findValue(final Message msg) {
        Field field = FixMessageUtils.findField((int)tag, groupDescription, msg);
        if (field!=null) {
            return field.getObject().toString();
        } else {
            return null;
        }
    }

    protected List<String> findValues(final Message msg) {
        List<Field> fields = FixMessageUtils.findFieldsInMsg((int) tag, msg);
        if (fields != null) {
            List<String> values = new LinkedList<String>();
            for(Field field : fields) {
                values.add(field.getObject().toString());
            }
            return values;
        } else {
            return null;
        }
    }

    protected void initGroupDescr(final String inStr) {
        groupDescription = new LinkedList<GroupDescription>();
        final String[] groups = StringUtils.split(inStr, '/');
        for (final String rawDesc : groups) {
            final String[] items = StringUtils.split(rawDesc, ':');
            boolean isGood = true;
            if (items.length != 2 || items[0].length() <= 0) {
                isGood = false;
            }
            int index = 0;
            if (isGood) {
                index = Integer.parseInt(items[1]);
                if (index < 1) {
                    isGood = false;
                }
            }
            if (!isGood) {
                throw new BuildException("Bad group format specified: " + inStr);
            }
            final GroupDescription desc = new GroupDescription();
            desc.setName(items[0]);
            desc.setIndex(index);
            groupDescription.add(desc);
        }
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this).
                append("tag", tag).
                append("filterValue", valueRule).
                append("group", groupDescription).
                append("condition", fieldCondition).
                append("required", required).toString();
    }
}
