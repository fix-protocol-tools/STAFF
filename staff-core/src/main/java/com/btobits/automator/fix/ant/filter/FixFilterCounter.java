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

import com.btobits.automator.ant.annotation.AutoParamBean;
import com.btobits.automator.ant.annotation.AutoParamLong;
import com.btobits.automator.fix.ant.task.BasicAntTask;
import org.apache.commons.lang.builder.ToStringBuilder;
import org.junit.Assert;

/**
 * @author Mykhailo_Sereda
 */
@AutoParamBean
public class FixFilterCounter extends BasicAntTask {

    @AutoParamLong(xmlAttr = "from" )
    public long fromCounter;

    @AutoParamLong(xmlAttr = "to" )
    public long toCounter;

    @Override
    protected void validate() throws Exception {
        Assert.assertTrue("At least one of condition('from', 'to') must be more that 0.", fromCounter >= 0 || toCounter >= 0);
        if((fromCounter >= 0 && toCounter >= 0)) {
            Assert.assertTrue("'from' counter can't be less 'to' counter. Values:[from:"+fromCounter+", to:"+toCounter+"]",
                     (fromCounter < toCounter));
        }
    }

    @Override
    protected void runTestInstructions() throws Exception {
        // do nothing
    }

    private int currentCount;


    public boolean checkCounter() {
        return checkWithConditions(++currentCount);
    }

    protected boolean checkWithConditions(int currentCount) {
        return isFromConditionValid(currentCount) && isToConditionValid(currentCount);
    }

    protected boolean isFromConditionValid(int currentCount) {
        return fromCounter >= 0 ? fromCounter <= currentCount : true;
    }

    protected boolean isToConditionValid(int currentCount) {
        return toCounter >= 0 ? toCounter >= currentCount : true;
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this).
                append("fromCounter", fromCounter).
                append("toCounter", toCounter).
                append("currentCount", currentCount).toString();
    }

}