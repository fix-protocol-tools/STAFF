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

import com.btobits.automator.fix.ant.filter.comparators.NotEqualsComparator;
import com.btobits.automator.fix.ant.filter.comparators.EqualsComparator;
import com.btobits.automator.fix.ant.filter.comparators.FilterComparator;
import com.btobits.automator.fix.ant.filter.comparators.RegexpComparator;

/**
 * Created by IntelliJ IDEA.
 * User: Mykhailo_Sereda
 * Date: 7.12.2010
 * Time: 13:21:55
 * To change this template use File | Settings | File Templates.
 */
public enum FilterFiledCondition {
    EQUALS("equals", new EqualsComparator()),
    NOT_EQUALS("notEquals", new NotEqualsComparator()),
    REGEXP("regexp", new RegexpComparator());
    private final String value;
    private final FilterComparator comparator;

    FilterFiledCondition(String value, FilterComparator comparator) {
        this.value = value;
        this.comparator = comparator;
    }

    public String value() {
        return value;
    }

    public static FilterFiledCondition fromValue(String v) {
        for (FilterFiledCondition c: FilterFiledCondition.values()) {
            if (c.value.equals(v)) {
                return c;
            }
        }
        throw new IllegalArgumentException(v);
    }


    public boolean compare(String value1, String value2) {
        return comparator.compare(value1, value2);
    }
}
