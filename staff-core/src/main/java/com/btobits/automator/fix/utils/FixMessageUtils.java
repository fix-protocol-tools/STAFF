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

import com.btobits.automator.fix.ant.container.GroupDescription;
import quickfix.*;

import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

/**
 * @author Mykhailo_Sereda
 */
public class FixMessageUtils {

    /**
     * Look in Header, Body and Trailer message and their inners groups
     * @param tag
     * @param message
     * @return list with Field which have such tag Id or null if message don't have fields with such tag Id
     */
    public static List<Field> findFieldsInMsg(final int tag, final Message message) {
        List<Field> listFields = new LinkedList<Field>();
        addFieldsFromFieldMap(tag, message.getHeader(), listFields);
        addFieldsFromFieldMap(tag, message, listFields);
        addFieldsFromFieldMap(tag, message.getTrailer(), listFields);
        return listFields.isEmpty() ? null : listFields;
    }

    /**
     * Look in filedMap body and inners groups
     * @param tag
     * @param fieldMap
     * @return list with Field which have such tag Id or null if fieldMap don't have fields with such tag Id
     */
    public static List<Field> findFields(final int tag, final FieldMap fieldMap) {
        List<Field> listFields = new LinkedList<Field>();
        addFieldsFromFieldMap(tag, fieldMap, listFields);
        return listFields.isEmpty() ? null : listFields;
    }

    private static Field getField(final int tag, final Iterator<Field<?>> iterator) {
        while (iterator.hasNext()) {
            Field field = iterator.next();
            if (tag == field.getTag()) {
                return field; 
            }
        }
        return null;
    }

    private static void addFieldsFromInnerGroups(final int tag, final FieldMap fieldMap, final List<Field> listFields) {
        final Iterator<Integer> groupKeyIterator = fieldMap.groupKeyIterator();
        while (groupKeyIterator.hasNext()) {
            addFieldsFromGroup(tag, fieldMap.getGroups(groupKeyIterator.next()), listFields);
        }
    }

    private static void addFieldsFromGroup(final int tag, final List<Group> groups, final List<Field> listFields) {
        for (Group group : groups) {
            addFieldsFromFieldMap(tag, group, listFields);
        }
    }

    private static void addFieldsFromFieldMap(final int tag, final FieldMap fieldMap, final List<Field> listFields) {
        if (fieldMap.isSetField(tag)) {
            Field field = getField(tag, fieldMap.iterator());
            if (field!=null) {
                listFields.add(field);
            }
        }
        if (fieldMap.hasGroup(tag)) {
            listFields.add(new IntField(tag, fieldMap.getGroupCount(tag)));
        }
        addFieldsFromInnerGroups(tag, fieldMap, listFields);
    }

    public static Field findField(final int tag, final List<GroupDescription> groupLocation, final Message message) {
        Field field = findField(tag, groupLocation, (FieldMap)message);
        if (field==null) {
            field = findField(tag, groupLocation, message.getHeader());
        } else if (field==null) {
            field = findField(tag, groupLocation, message.getTrailer());
        }
        return field;
    }

    public static Field findField(final int tag, final List<GroupDescription> groupLocation, final FieldMap fieldMap) {
        if (groupLocation==null || groupLocation.isEmpty()) {
            return getField(tag, fieldMap.iterator());
        } else {
            FieldMap currentGroup = fieldMap;
            for (GroupDescription groupDescr : groupLocation) {
                int groupTagId = FixUtils.getFieldId(groupDescr.getName());
                int groupIndex = groupDescr.getIndex();
                currentGroup = findGroup(groupTagId, groupIndex, currentGroup);
                if (currentGroup==null) {
                    return null;
                }
            }
            return getField(tag, currentGroup.iterator());
        }
    }

    private static Group findGroup(int groupTagId, int groupIndex, final FieldMap fieldMap) {
        if (fieldMap.hasGroup(groupIndex, groupTagId)) {
            return fieldMap.getGroups(groupTagId).get(groupIndex-1);
        } else {
            return null;
        }
    }
}