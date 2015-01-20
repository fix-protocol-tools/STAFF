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

import org.junit.Test;

import static org.junit.Assert.*;

/**
 * @author Mykhailo_Sereda
 */
public class FixFilterCounterTest {

    @Test
    public void testConditionFrom() throws Exception {
        FixFilterCounter filterCounter = createFilterCounter(2, -1);
        assertFalse("Next invocation must return false. FilterCounter: " + filterCounter, filterCounter.checkCounter());
        assertTrue("Next invocation must return true. FilterCounter: " + filterCounter, filterCounter.checkCounter());
        assertTrue("Next invocation must return true. FilterCounter: " + filterCounter, filterCounter.checkCounter());
    }

    @Test
    public void testConditionTo() throws Exception {
        FixFilterCounter filterCounter = createFilterCounter(-1, 1);
        assertTrue("Next invocation must return true. FilterCounter: " + filterCounter, filterCounter.checkCounter());
        assertFalse("Next invocation must return false. FilterCounter: " + filterCounter, filterCounter.checkCounter());
        assertFalse("Next invocation must return false. FilterCounter: " + filterCounter, filterCounter.checkCounter());
    }

    @Test
    public void testConditionFromTo() throws Exception {
        FixFilterCounter filterCounter = createFilterCounter(2, 3);
        assertFalse("Next invocation must return false. FilterCounter: " + filterCounter, filterCounter.checkCounter());
        assertTrue("Next invocation must return true. FilterCounter: " + filterCounter, filterCounter.checkCounter());
        assertTrue("Next invocation must return true. FilterCounter: " + filterCounter, filterCounter.checkCounter());
        assertFalse("Next invocation must return false. FilterCounter: " + filterCounter, filterCounter.checkCounter());
        assertFalse("Next invocation must return false. FilterCounter: " + filterCounter, filterCounter.checkCounter());
    }

    @Test(expected = AssertionError.class)
    public void testConditionNotInit() throws Exception {
        // on validate must be exception
        createFilterCounter(-1, -1);
        fail("Validation had to be exception but is wasn't");
    }

    @Test(expected = AssertionError.class)
    public void testConditionInitZero() throws Exception {
        // on validate must be exception
        createFilterCounter(0, 0);
        fail("Validation had to be exception but is wasn't");
    }

    protected FixFilterCounter createFilterCounter(int fromCounter, int toCounter) throws Exception {
        FixFilterCounter counter = new FixFilterCounter();
        counter.fromCounter = fromCounter;
        counter.toCounter = toCounter;
        counter.validate();
        return counter;
    }
}
