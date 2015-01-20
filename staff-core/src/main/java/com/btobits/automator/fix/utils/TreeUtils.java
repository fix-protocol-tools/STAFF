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

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

import quickfix.DataDictionary;

import com.btobits.automator.fix.utils.fix.FixMessageType;

/**
 * @author Volodymyr_Biloshkurs
 */
public final class TreeUtils {

    public static boolean compareTreeNode(final FixMessageType.Field inNode1, final FixMessageType.Field inNode2,
                                          final DataDictionary inDic, final String inMsgType, final List<String> inMessageErrors) {
        final List<String> messageErrors = new ArrayList<String>();
        if (inNode1 == null) {
            return true;
        }

        if (inNode2 == null) {
            messageErrors.add("Field [" + inNode1.getName() + ", " + inNode1.getValue() + "] does not exist in received message");
        } else {

            if (!inNode1.isGroup() && !FixUtils.compareFields(inNode1, inNode2)) {
                messageErrors.add("Field [" + inNode1.getName() + ", " + inNode1.getValue() + "] != [" + inNode2.getName() + ", "
                        + inNode2.getValue() + "]  does not equal in received message");
            }

            for (final FixMessageType.Field fld : inNode1.getFields()) {
                if (fld.isGroup()) {

                    final FixMessageType.Group findGroup = getModelGroup(fld, inNode2.getFields(), inDic, inMsgType);

                    if (findGroup == null) {
                        messageErrors.add("Group [" + fld.getName() + ", " + fld.getValue()
                                + "] or nested one does not exist/equal in received message");
                    } else if (compareTreeNode(fld, findGroup, inDic, inMsgType, messageErrors)) {
                        //inNode2.getFields().remove(findGroup);
                    }
                } else {
                    final FixMessageType.Field fildNode2 = getNodeByNameAndValue(fld.getName(), fld.getValue(), inNode2, true);
                    if (fildNode2 == null) {
                        messageErrors.add("Field [" + fld.getName() + ", " + fld.getValue() + "] does not exist in received message");
                    } else if (compareTreeNode(fld, fildNode2, inDic, inMsgType, messageErrors)) {
                        // inNode2.getFields().remove(fildNode2);
                    }
                }
            }
        }

        inMessageErrors.addAll(messageErrors);
        return messageErrors.isEmpty();
    }

    public static boolean compareTreeNodeWithGroupOrder(final FixMessageType.Field inNode1, final FixMessageType.Field inNode2,
                                                        final DataDictionary inDic, final String inMsgType, final List<String> inMessageErrors) {

        final List<String> messageErrors = new ArrayList<String>();
        if (inNode1 == null) {
            return true;
        }

        if (inNode2 == null) {
            messageErrors.add("Field [" + inNode1.getName() + ", " + inNode1.getValue() + "] does not exist in received message");
        } else {

            if (!inNode1.isGroup() && !FixUtils.compareFields(inNode1, inNode2)) {
                messageErrors.add("Field [" + inNode1.getName() + ", " + inNode1.getValue() + "] != [" + inNode2.getName() + ", "
                        + inNode2.getValue() + "]  does not equal in received message");
            }

            for (final FixMessageType.Field fld : inNode1.getFields()) {
                if (fld.isGroup()) {
                    final int position = getGroupPosition(fld, inNode1);
                    final FixMessageType.Field grByPosition = getGroupByPosition(position, FixUtils.getFieldId(fld.getName()), inNode2);

                    if (grByPosition == null) {
                        messageErrors.add("Group [" + fld.getName() + ", " + fld.getValue()
                                + "] or nested one does not exist/equal in received message");
                    } else {
                        compareTreeNodeWithGroupOrder(fld, grByPosition, inDic, inMsgType, messageErrors);
                    }
                } else {
                    final FixMessageType.Field fildNode2 = getNodeByNameAndValue(fld.getName(), fld.getValue(), inNode2, true);
                    if (fildNode2 == null) {
                        messageErrors.add("Group [" + fld.getName() + ", " + fld.getValue()
                                + "] or nested one does not exist/equal in received message");
                    } else if (!FixUtils.compareFields(fld, fildNode2)) {
                        messageErrors.add("Field [" + fld.getName() + ", " + fld.getValue() + "] != [" + fildNode2.getName() + ", "
                                + fildNode2.getValue() + "]  does not equal in received message");
                    }
                }
            }
        }

        inMessageErrors.addAll(messageErrors);
        return messageErrors.isEmpty();
    }

    public static final FixMessageType.Field getNodeByNameAndValue(final String inName, final String inValue,
                                                                   final FixMessageType.Field inNode, final boolean inIsLeading) {

        return FixUtils.getModelField(FixUtils.getFieldId(inName), inNode.getFields(), inIsLeading);
    }

    public static final int getGroupPosition(final FixMessageType.Field inField, final FixMessageType.Field inNode) {
        return getGroupPosition(inField, inNode.getFields());
    }

    public static final int getGroupPosition(final FixMessageType.Field inField, final LinkedList<FixMessageType.Field> inFields) {

        int count = 0;
        final int fieldId = FixUtils.getFieldId(inField.getName());
        for (final FixMessageType.Field field : inFields) {
            if (!field.isGroup() && !(field instanceof FixMessageType.Block)) {
                continue;
            }

            if (field.isLeading()) {
                continue;
            }

            if (field == inField) {
                break;
            }

            if (fieldId == FixUtils.getFieldId(field)) {
                count++;
            }
        }

        return count;
    }

    public static final FixMessageType.Field getGroupByPosition(final int inPosition, final int inGroupId, final FixMessageType.Field inNode) {
        return getGroupByPosition(inPosition, inGroupId, inNode.getFields());
    }

    public static final FixMessageType.Field getGroupByPosition(final int inPosition, final int inGroupId,
                                                                final LinkedList<FixMessageType.Field> inFields) {

        int count = 0;
        for (final FixMessageType.Field field : inFields) {
            final int groupId = FixUtils.getFieldId(field.getName());
            if (!(field.isGroup()) || field.isLeading()) continue;
            if (inPosition == count && inGroupId == groupId) return field;
            if (inGroupId == groupId) count++;
        }
        return null;
    }

    public static FixMessageType.Group getModelGroup(final FixMessageType.Field inNode1, final LinkedList<FixMessageType.Field> inFields,
                                                     final DataDictionary inDic, final String inMsgType) {
        final int id1 = FixUtils.getFieldId(inNode1);
        for (final FixMessageType.Field field : inFields) {
            final int id2 = FixUtils.getFieldId(field);
            if (id1 != id2 || !field.isGroup()) continue;
            if (field.isLeading()) continue;
            if (compareSubTreeFunc(inNode1, field, inDic, inMsgType)) return (FixMessageType.Group) field;
        }
        return null;
    }

    private static boolean compareSubTreeFunc(final FixMessageType.Field inNode1, final FixMessageType.Field inNode2,
                                              final DataDictionary inDic, final String inMsgType) {
        if (inNode2 == null) return false;
        final int id1 = FixUtils.getFieldId(inNode1.getName());
        final int id2 = FixUtils.getFieldId(inNode2.getName());
        if (id1 != id2) return false;
        if (!(inNode1.isGroup()) && !FixUtils.compareFields(inNode1, inNode2)) return false;
        for (final FixMessageType.Field field : inNode1.getFields()) {
            if (field.isGroup()) {
                final FixMessageType.Group grByPosition = getModelGroup(field, inNode2.getFields(), inDic, inMsgType);
                if (grByPosition == null) return false;
                if (!compareSubTreeFunc(field, grByPosition, inDic, inMsgType)) return false;
            } else {
                final FixMessageType.Field fildNode2 = getNodeByNameAndValue(field.getName(), field.getValue(), inNode2, true);
                if (fildNode2 == null) return false;
                if (!compareSubTreeFunc(field, fildNode2, inDic, inMsgType)) return false;
            }
        }
        return true;
    }
}
