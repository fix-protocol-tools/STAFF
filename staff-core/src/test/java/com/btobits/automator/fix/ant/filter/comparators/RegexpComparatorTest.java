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

package com.btobits.automator.fix.ant.filter.comparators;

import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

/**
 * @author Mykhailo_Sereda
 */
public class RegexpComparatorTest {
    private RegexpComparator instance;
    @Before
    public void initInstance() {
        instance = new RegexpComparator();
    }

    @Test
    public void testEquals() {
        String s1 = "aaa";
        String s2 = "aaa";
        assertTrue("This strings are equals. Result must be true", instance.compare(s1, s2));
    }

    @Test(expected= Exception.class)
    public void testFirstIsNull() {
        String s1 = null;
        String s2 = "bbb";
        assertFalse("Result must be false", instance.compare(s1, s2));
    }

    @Test
    public void testRegexpAsterisk() {
        String s1 = "aaaab";
        String s2 = "a*b";
        assertTrue("String value=" + s1 + ", pattern value=" + s2 + ". Result must be true", instance.compare(s1, s2));
    }

    @Test
    public void testRegexpBackslash() {
        // last char must be 'a'
        String s1 = "bbba";
        String s2 = "a\\b";
        assertTrue("String value=" + s1 + ", pattern value=" + s2 + ". Result must be true", instance.compare(s1, s2));
    }

    @Test(expected= Exception.class)
    public void testSecondIsNull() {
        String s1 = "aaa";
        String s2 = null;
        instance.compare(s1, s2);
    }
}
