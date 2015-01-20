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

package com.btobits.automator.fix.comparator;

import com.btobits.automator.fix.utils.FixUtils;
import com.btobits.automator.fix.utils.fix.FixMessageType;
import com.sun.org.apache.regexp.internal.RE;
import org.apache.commons.lang.StringUtils;
import quickfix.DataDictionary;

import java.util.LinkedList;
import java.util.List;


/**
 * @author Volodymyr_Biloshkurs
 */
public class HeaderComparator extends AbstractComparator {

    public HeaderComparator() {
    }

    public HeaderComparator(DataDictionary inDictionary, List<String> inMessageErrors) {
        super(inDictionary, inMessageErrors);
    }

    public HeaderComparator(DataDictionary inDictionary, String inMessageType, List<String> inMessageErrors) {
        super(inDictionary, inMessageType, inMessageErrors);
    }

    public boolean compare(final FixMessageType inFixModel) {

        final LinkedList<FixMessageType.Field> fields = model.getFields();
        for (final FixMessageType.Field f : fields) {
            final int fieldId = FixUtils.getFieldId(f);
            if (!dictionary.isHeaderField(fieldId)) {
                continue;
            }

            final FixMessageType.Field modelField = FixUtils.getModelField(fieldId, inFixModel.getFields(), false);
            if (modelField == null) {
                messageErrors.add("Header field [" + f.getName() + ", " + f.getValue() + "] does not exist in received message");
            } else {
                if (!StringUtils.isBlank(f.getValue())) {
                    if (!compareFiled(f, modelField)) {
                        String errMsg;
                        errMsg = "Header field [" + f.getName() + ", " + f.getValue() + "] ";
                        if (modelField.isValueRegGxp()) {
                            errMsg = errMsg + "does not meet the pattern from received message";
                        } else {
                            errMsg = errMsg + "not equal in received message";
                        }
                        errMsg = errMsg +  " [" + modelField.getName() + ", " + modelField.getValue() + "]";
                        messageErrors.add(errMsg);
                    }
                }
            }
        }
        return true;
    }

    protected boolean compareFiled(final FixMessageType.Field f, final FixMessageType.Field modelField) {
        if (modelField.isValueRegGxp()) {
            return new RE(modelField.getValue()).match(f.getValue());
        } else {
            return StringUtils.equals(f.getValue(), modelField.getValue());
        }
    }

}
