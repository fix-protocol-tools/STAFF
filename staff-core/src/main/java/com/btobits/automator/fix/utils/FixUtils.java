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

import com.btobits.automator.fix.utils.fix.FixMessageType;
import com.sun.org.apache.regexp.internal.RE;
import org.apache.commons.lang.StringUtils;
import quickfix.*;

import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

/** 
 * @author Volodymyr_Biloshkurs
 */
public class FixUtils {

    public static final String MSG_TYPE_FIELD = "MsgType";
    public static final int TRGT = 49;
    public static final int SNDR = 56;
    public static final int MSGTYPE = 35;
    public static final int TYPE = 8;

    public static int getFieldId(final FixMessageType.Field inField) {
        try {
            final quickfix.Field<?> v =
                    (quickfix.Field<?>) (Class.forName("quickfix.field." + inField.getName())).newInstance();
            return v.getField();
        } catch (Exception e) {
            return Integer.parseInt(inField.getName());
        }
    }

    public static int getFieldId(final String inField) {
        try {
            final quickfix.Field<?> v =
                    (quickfix.Field<?>) (Class.forName("quickfix.field." + inField)).newInstance();
            return v.getField();
        } catch (Exception e) {
            return Integer.parseInt(inField);
        }
    }

    public static boolean isRequireField(final Field inField,
                                         final String inMessageType,
                                         final DataDictionary inDataDictionary) {
        return inDataDictionary.isRequiredField(inMessageType, inField.getTag());
    }

    public static boolean compareFields(final Field inField1, final Field inField2) {
        if (inField1.getTag() != inField2.getTag()) {
            return false;
        } else if (inField1.getObject() == null || inField2.getObject() == null) {
            return false;
        } else {
            return inField1.getObject().toString().equals(inField2.getObject().toString());
        }
    }

    public static Field getField(final Iterator<Field<?>> inFieldsIterator, final Field inField) {
        while (inFieldsIterator.hasNext()) {
            final Field field = inFieldsIterator.next();
            if (field.getTag() == inField.getTag()) {
                return field;
            }
        }
        return null;
    }

    public static FixMessageType.Field getModelField(final int inId,
                                                           final LinkedList<FixMessageType.Field> inFields,
                                                           final boolean inIsLeading) {
        for (final FixMessageType.Field field : inFields) {
            if (field.isGroup()) {
                continue;
            }

            if (field.isLeading() && !inIsLeading) {
                continue;
            }

            if (getFieldId(field) == inId) {
                return field;
            }
        }

        return null;
    }

    public static String getMessageType(final Message inMessage) {
        try {
            return inMessage.getHeader().getString(FixUtils.getFieldId(FixUtils.MSG_TYPE_FIELD));
        } catch (Exception ex) {
            //ex.printStackTrace();
        }
        return null;
    }

    public static FixMessageType getMessageType(final Message inMessage, final DataDictionary dict) throws Exception {
        return getMessageType(inMessage, dict, dict);
    }

    public static FixMessageType getMessageType(final Message inMessage, final DataDictionary appDict,
                                                final DataDictionary sessionDict) throws Exception {
        final FixMessageType msgType = new FixMessageType();
        final String msgTypeField = inMessage.getHeader().getString(FixUtils.MSGTYPE);

        //copy header field        
        Iterator<Field<?>> fields = inMessage.getHeader().iterator();
        while (fields.hasNext()) {
            final Field<?> field = fields.next();
            msgType.addField(new FixMessageType.Field(field, false));
        }

        //copy trailer
        fields = inMessage.getTrailer().iterator();
        while (fields.hasNext()) {
            final Field<?> field = fields.next();
            msgType.addField(new FixMessageType.Field(field, false));
        }

        fields = inMessage.iterator();
        while (fields.hasNext()) {
            final Field<?> field = fields.next();

            boolean isGroup = appDict.isGroup(msgTypeField, field.getTag());
            boolean isNumInGroup = FieldType.NumInGroup == appDict.getFieldTypeEnum(field.getTag());
            boolean isDataField = appDict.isDataField(field.getTag());

            if( sessionDict.isHeaderField(field.getTag()) ||
                sessionDict.isTrailerField(field.getTag())) {
                continue;
            }            

//            if( isGroup && isDataField ||
//                (FieldType.NumInGroup == inDic.getFieldTypeEnum(field.getTag())) && isDataField ) {
//                continue;
//            }

            /* if(inDic.getFieldTypeEnum(field.getTag()).getJavaType() == Double.class) {
              if(field.getObject() != null) {
                  final Double val = Double.parseDouble(field.getObject()+"");
                  if(val==0.0) {
                      final FixMessageType.Field f = new FixMessageType.Field(field, (isGroup || isNumInGroup) && !isDataField);
                      f.setValue("0.0");
                      msgType.addField(f);
                  } else {
                      msgType.addField(
                              new FixMessageType.Field(field, (isGroup || isNumInGroup) && !isDataField));
                  }
              }
          } else {*/
            msgType.addField(
                    new FixMessageType.Field(field, (isGroup || isNumInGroup) && !isDataField));
            //}
        }

        final Iterator<Integer> grIter = inMessage.groupKeyIterator();
        while (grIter.hasNext()) {
            final Integer grId = grIter.next();
            final List<Group> groups = inMessage.getGroups(grId);

            for (final Group group : groups) {
                final FixMessageType.Group gr = new FixMessageType.Group();
                gr.setName(group.getFieldTag() + "");
                fillGroup(gr, group, appDict);
                msgType.addGroup(gr);
            }
        }

        return msgType;
    }

    private static void fillGroup(final FixMessageType.Group inGroup,
                                  final Group inFixGroup,
                                  final DataDictionary inDic) {
        final Iterator<Field<?>> fields = inFixGroup.iterator();
        while (fields.hasNext()) {
            final Field<?> field = fields.next();

            //     boolean isGroup      = inDic.isGroup(msgTypeField, field.getTag());
            boolean isNumInGroup = FieldType.NumInGroup == inDic.getFieldTypeEnum(field.getTag());
            boolean isDataField = inDic.isDataField(field.getTag());


            /*if((FieldType.NumInGroup == inDic.getFieldTypeEnum(field.getTag()))) {
                continue;
            }*/

            inGroup.addField(new FixMessageType.Field(field, isNumInGroup && !isDataField));
        }

        //work with group
        final Iterator<Integer> grIter = inFixGroup.groupKeyIterator();
        while (grIter.hasNext()) {
            final Integer grId = grIter.next();
            final List<Group> groups = inFixGroup.getGroups(grId);

            for (final Group group : groups) {
                final FixMessageType.Group groupFill = new FixMessageType.Group();
                groupFill.setName(group.getFieldTag() + "");
                inGroup.addGroup(groupFill);
                fillGroup(groupFill, group, inDic);
            }
        }
    }

    public static boolean compareFields(final FixMessageType.Field inField1, final FixMessageType.Field inField2) {
        if (StringUtils.isBlank(inField1.getValue())) {
            return true;
        } else if (inField1.isValueRegGxp()) {
            return new RE(inField1.getValue()).match(inField2.getValue());
        } else {
            if (isDouble(inField1.getValue()) || isDouble(inField2.getValue())) {
                // compare double value
                return compareDouble(inField1.getValue(), inField2.getValue());
            } else {
                return StringUtils.equals(inField1.getValue(), inField2.getValue());
            }
        }
    }

    private static boolean compareDouble(String inDouble1, String inDouble2) {
        if (StringUtils.equals(inDouble1, inDouble2)) return true;

        if (inDouble1 == null || inDouble2 == null) return false;

        Double a = getDouble(inDouble1);
        Double b = getDouble(inDouble2);

        return a.equals(b);
    }

    private static boolean isDouble(final String inValue) {
        try {
            if (inValue == null) return false;
            Double.parseDouble(inValue);
            return true;
        } catch (Exception ex) {
            return false;
        }
    }

    private static Double getDouble(String inValue) {
        try {
            if (inValue == null) return null;
            return Double.parseDouble(inValue);
        } catch (Exception ex) {
            return null;
        }
    }

    public static String toString(final List<String> inMessages) {
        if (inMessages.isEmpty()) {
            return "No errors detected";
        }

        final StringBuilder builder = new StringBuilder();
        Integer index = 0;
        for (String msg : inMessages) {
            if (index == 0) {
                builder.append('\n');
            }
            builder.append(++index).append(") ").append(msg)
                    .append('\n');
        }
        return builder.toString();
    }
}