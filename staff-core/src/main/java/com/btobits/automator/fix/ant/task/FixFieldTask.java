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
import com.btobits.automator.ant.annotation.AutoParamBool;
import com.btobits.automator.ant.annotation.AutoParamEnum;
import com.btobits.automator.ant.annotation.AutoParamStr;
import com.btobits.automator.fix.ant.container.GroupDescription;
import com.btobits.automator.fix.ant.filter.FilterFiledCondition;
import com.btobits.automator.fix.utils.FixUtils;
import com.btobits.automator.fix.utils.fix.FixMessageType;
import org.apache.commons.lang.StringUtils;
import org.apache.tools.ant.BuildException;
import org.junit.Assert;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

/**
 * <p><h2>
 * fixField task accesses FIX message field. FIX groups are also supported.
 * </h2></p>
 * <p/>
 * <p>
 * <b>Sample:</b><br/>
 * <pre>
 * Set tag TransactTime of the message new_order_singleto the value of the new_order_trn_time propertty.
 * Note: in order to work with message fields, the message should be defined earlier.
 * <p/>
 * 	&lt;!-- define message --&gt;
 * 	&lt;fixMessage id="new_order_single"&gt;
 * 		&lt;field name="MsgType" value="D" /&gt;
 * 		&lt;field name="HandlInst" value="1" /&gt;
 * 		&lt;field name="Symbol" value="ZXZZT" /&gt;
 * 		&lt;field name="Side" value="1" /&gt;
 * 		&lt;field name="OrderQty" value="20000" /&gt;
 * 		&lt;field name="OrdType" value="2" /&gt;
 * 		&lt;field name="ClOrdID" value="Order#1" /&gt;
 * 		&lt;field name="Price" value="34.7" /&gt;
 * 	&lt;/fixMessage&gt;
 * 	&lt;!-- Generate transaction time and store to the new_order_trn_time property --&gt;
 * 	&lt;tstamp&gt;
 * 		&lt;format property="new_order_trn_time" pattern="yyyyMMdd-hh:mm:ss.SSS" locale="en,US" offset="-5" unit="hour" /&gt;
 * 	&lt;/tstamp&gt;
 * 	&lt;!-- set field value --&gt;
 * 	&lt;fixField refid="new_order_single" action="set" name="TransactTime" property="new_order_trn_time" /&gt;
 *  Set tag values of two entries of NoMDEntries group of the hand_made_msg message.
 * <p/>
 * 	&lt;fixMessage id="hand_made_msg"&gt;
 * 		&lt;field name="MsgType" value="X" /&gt;
 * 	&lt;/fixMessage&gt;
 * <p/>
 * 	&lt;property name="px_1" value="34.7" /&gt;
 * 	&lt;property name="px_2" value="34.6" /&gt;
 * <p/>
 * 	&lt;fixField refid="hand_made_msg" action="set" group="NoMDEntries:1" name="MDUpdateAction" value="0" /&gt;
 * 	&lt;fixField refid="hand_made_msg" action="set" group="NoMDEntries:1" name="MDEntryType" value="3" /&gt;
 * 	&lt;fixField refid="hand_made_msg" action="set" group="NoMDEntries:1" name="MDEntrySize" value="190" /&gt;
 * 	&lt;fixField refid="hand_made_msg" action="set" group="NoMDEntries:1" name="MDEntryPx" property="px_1" /&gt;
 * 	&lt;fixField refid="hand_made_msg" action="set" group="NoMDEntries:1" name="Symbol" value="CSCO" /&gt;
 * <p/>
 * 	&lt;fixField refid="hand_made_msg" action="set" group="NoMDEntries:2" name="MDUpdateAction" value="0" /&gt;
 * 	&lt;fixField refid="hand_made_msg" action="set" group="NoMDEntries:2" name="MDEntryType" value="3" /&gt;
 * 	&lt;fixField refid="hand_made_msg" action="set" group="NoMDEntries:2" name="MDEntrySize" value="250" /&gt;
 * 	&lt;fixField refid="hand_made_msg" action="set" group="NoMDEntries:2" name="MDEntryPx" value="${px_2}" /&gt;
 * 	&lt;fixField refid="hand_made_msg" action="set" group="NoMDEntries:2" name="Symbol" value="ZVZZT" /&gt;
 * <p/>
 *  Get values of tags from the received exec_report message and print them to console.
 * <p/>
 * 	&lt!-- receive the message --&gt;
 * 	&lt;fixReceive refid="simple_client" sender="TRGT" target="SNDR" type="FIX.4.4" repeat="1" count="1" timeout="10"&gt;
 *    	&lt;fixMessage id="exec_report"&gt;
 * 	        &lt;field name="MsgType" value="8" /&gt;
 * 	        &lt;field name="SendingTime" /&gt;
 * 	        &lt;field name="CheckSum" /&gt;
 * 	        &lt;field name="ExecID" /&gt;
 * 	        &lt;field name="OrderID" /&gt;
 * 		&lt;/fixMessage&gt;
 * 	&lt;/fixReceive&gt;
 * 	&lt;!-- get tags' values --&gt;
 * 	&lt;fixField refid="exec_report" action="get" name="SendingTime" property="er_time" /&gt;
 * 	&lt;fixField refid="exec_report" action="get" name="CheckSum" property="er_checksum" /&gt;
 * 	&lt;fixField refid="exec_report" action="get" name="ExecID" property="er_exec_id" /&gt;
 * 	&lt;fixField refid="exec_report" action="get" name="OrderID" property="er_order_id" /&gt;
 * 	&lt;!-- print the values --&gt;
 * 	&lt;echo&gt;Execution Report OrderID: ${er_order_id}&lt;/echo&gt;
 * 	&lt;echo&gt;Execution Report ExecID: ${er_exec_id}&lt;/echo&gt;
 * 	&lt;echo&gt;Execution Report CheckSum: ${er_checksum}&lt;/echo&gt;
 * 	&lt;echo&gt;Execution Report SendingTime: ${er_time}&lt;/echo&gt;
 *  </pre>
 * </p>
 * <p/>
 * <p/>
 *
 * @author Kirill_Mukhoiarov
 */

@AutoParamBean
public final class FixFieldTask extends BasicAntTask {
    public final static String svnSignature = "$$Rev: 67588 $$ $$Date: 2014-10-28 14:02:42 +0200 (Вт, 28 окт 2014) $$ $$LastChangedBy: Alexander_Sereda $$";

    /**
     * Any project element can be assigned an identifier using
     * its id attribute. In most cases the element can subsequently
     * be referenced by specifying the refid attribute on an
     * element of the same type.
     */
    @AutoParamStr(xmlAttr = "refid" )
    public String refId = null;

    /**
     * Name of the fixField to operate. Both tag name or tag number can be used as the value.
     */
    @AutoParamStr(xmlAttr = "name" )
    public String name = null;

    /**
     * Property name to be passed to action. Only one of property / value should be specified.
     */
    @AutoParamStr(xmlAttr = "property" )
    public String property = null;

    /**
     * Property name to be passed to action. Only one of property / value should be specified.
     */
    @AutoParamStr(xmlAttr = "value" )
    public String value = null;

    /**
     * TODO: add description
     */
    @AutoParamStr(xmlAttr = "group" )
    public String groupName = null;


    /**
     * TODO: add description
     */
    @AutoParamBool(xmlAttr = "nullable" )
    public Boolean nullable = false;

    /**
     * Action to be done with fixField value. Possible values are: get | set | getGroupCount
     */
    @AutoParamEnum(xmlAttr = "action", enumClass = ACTION_TYPE.class)
    public ACTION_TYPE action;

    private LinkedList<GroupDescription> group;
    private FixMessageType papaMessage;

    public enum ACTION_TYPE {
        GET, SET, GETGROUPCOUNT;
    }

    @Override
    protected void validate() throws Exception {
        Assert.assertTrue("Reference to FIX session is not specified", StringUtils.isNotBlank(refId));
        final Object obj = getProject().getReference(refId);
        Assert.assertNotNull("RefId[" + refId + "]. Failed to get FIX session.", obj);
        Assert.assertTrue("RefId[" + refId + "]. Referred object is not FIX message: "
                + obj.getClass().getSimpleName(), (obj instanceof FixMessageType));
        papaMessage = (FixMessageType) obj;
//        Assert.assertTrue("Required 'groupName' attribute is not specified", StringUtils.isNotBlank(groupName));
        if (groupName != null) {
            setGroupName(groupName);
            // TODO: make check list 'group' - must be not empty
        }

        Assert.assertNotNull("fixFiled attribute 'action' is required parameter. " +
                "Valid value list: get||set||getGroupCount.", action);
    }

    @Override
    protected void runTestInstructions() throws Exception {
        switch (action) {
            case GET:
                getField();
                break;
            case SET:
                setField();
                break;
            case GETGROUPCOUNT:
                getGroupCount();
                break;
        }
    }

    public void addText(String inValue) {
        value = inValue;
    }

    private void getField() {
        String filedValue = getFiledValue(whereLookValue());
        if (filedValue!=null) {
            writeProperty(filedValue);
        } else {
            if (!nullable) {
                throw new BuildException("Field '" + name + "' was not found in referenced message" );
            }
        }
    }

    private void getGroupCount() {
        int groupCount = getGroupCount(whereLookValue());
        writeProperty(Integer.toString(groupCount));
    }

    private List<FixMessageType.Field> whereLookValue() {
        if (isGroupAssigned()) {
            LinkedList<FixMessageType.Field> groupFields = getGroupFields(papaMessage.getFields(), false);
            if (groupFields != null) {
                return groupFields;
            } else {
                throw new BuildException("Group is assigned, but can't find in parent message. Grope(s): " + groupName + ". " +
                        "FixMessage: " + papaMessage);
            }
        } else {
            return papaMessage.getFields();
        }
    }

    private boolean isGroupAssigned() {
        return group == null ? false : !group.isEmpty();
    }

    private LinkedList<FixMessageType.Field> getGroupFields(final LinkedList<FixMessageType.Field> msgBody, boolean canCreate) {
        LinkedList<FixMessageType.Field> curGroupFields = msgBody;
        for (final GroupDescription desc : group) {
            FixMessageType.Group innerGroup = getGroup(curGroupFields, desc, canCreate);
            if (innerGroup == null) {
                throw new BuildException("Group '" + groupName + "' not found" );
            } else {
                curGroupFields = innerGroup.getFields();
            }
        }
        return curGroupFields;
    }

    private FixMessageType.Group getGroup(final LinkedList<FixMessageType.Field> inFields,
                                          final GroupDescription inDesc, boolean canCreate) {
        int curGroupIndex = 0;
        int leadingTagId = FixUtils.getFieldId(inDesc.getName());
        for (final FixMessageType.Field f : inFields) {
            int id1 = FixUtils.getFieldId(f.getName());
            if (f.isGroup() && id1 == leadingTagId) {
                ++curGroupIndex;
                if (curGroupIndex == inDesc.getIndex()) {
                    return (FixMessageType.Group) f;
                }
            }
        }
        FixMessageType.Group group = null;
        if (canCreate) {
            while (curGroupIndex != inDesc.getIndex()) {
                group = new FixMessageType.Group();
                group.setName(inDesc.getName());
                inFields.add(group);
                ++curGroupIndex;
            }
        }
        return group;
    }

    private void writeProperty(final String value) {
        getProject().setProperty(StringUtils.isNotBlank(property) ? property : name, value);
    }

    private String getFiledValue(final List<FixMessageType.Field> inFields) {
        String result = null;
        for (final FixMessageType.Field field : inFields) {
            int id1 = FixUtils.getFieldId(field.getName());
            int id2 = FixUtils.getFieldId(name);
            if (id1 == id2) {
                if (field.getValue() != null) {
                    result = field.getValue();
                    break;
                }
            }
        }
        return result;
    }

    private int getGroupCount(final List<FixMessageType.Field> inFields) {
        int count = 0;
        for (final FixMessageType.Field field : inFields) {
            int id1 = FixUtils.getFieldId(field.getName());
            int id2 = FixUtils.getFieldId(name);
            if (id1 == id2 && field.isGroup()) {
                ++count;
            }
        }
        return count;
    }

    private void setField() throws BuildException {
        final LinkedList<FixMessageType.Field> msgBody = papaMessage.getFields();
        if (isGroupAssigned()) {
            LinkedList<FixMessageType.Field> curGroupFields = msgBody;
            for (final GroupDescription desc : group) {
                FixMessageType.Group innerGroup = getGroup(curGroupFields, desc, true);
                if (innerGroup == null) {
                    throw new BuildException("Can't create group '" + desc + "'");
                } else {
                    curGroupFields = innerGroup.getFields();
                }
            }
            setField(curGroupFields);
        } else {
            setField(msgBody);
        }
    }

    private void setField(final LinkedList<FixMessageType.Field> fields) {
        for (final FixMessageType.Field f : fields) {
            int id1 = FixUtils.getFieldId(f.getName());
            int id2 = FixUtils.getFieldId(name);
            if (id1 == id2) {
                fields.remove(f);
                break;
            }
        }

        final String fieldValue = StringUtils.isNotBlank(value) ? value :
                StringUtils.isNotBlank(property) ? getProject().getProperty(property) : null;

        if (StringUtils.isNotBlank(fieldValue)) {
            final FixMessageType.Field field = new FixMessageType.Field();
            field.setName(name);
            field.setValue(fieldValue);
            fields.add(field);
        }
    }

    protected void setGroupName(final String inStr) {
        group = new LinkedList<GroupDescription>();
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
            group.add(desc);
        }
    }
}
