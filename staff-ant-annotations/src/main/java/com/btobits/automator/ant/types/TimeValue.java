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

package com.btobits.automator.ant.types;

import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;
import org.apache.tools.ant.BuildException;

import java.util.concurrent.TimeUnit;

/**
 * @author Kirill_Mukhoiarov
 */

public final class TimeValue {
    public final static String svnSignature = "$$Rev: 67588 $$ $$Date: 2014-10-28 14:02:42 +0200 (Вт, 28 окт 2014) $$ $$LastChangedBy: Alexander_Sereda $$";
    private final Logger log = Logger.getLogger(getClass());

    private long duration;
    private TimeUnit unit;

    public TimeValue() {
        duration = 0;
        unit = TimeUnit.SECONDS;
    }

    public TimeValue(final String inXmlValue) {
        this();
        final String value[] = StringUtils.split(StringUtils.trim(inXmlValue), " ");
        switch (value.length) {
            case 1:
                duration = Long.parseLong(StringUtils.trim(value[0]));
                break;
            case 2:
                try {
                    duration = Long.parseLong(StringUtils.trim(value[0]));
                    unit = TimeUnit.valueOf(StringUtils.upperCase(StringUtils.trim(value[1])));
                } catch (final IllegalArgumentException e) {
                    throw new BuildException("Unable to extract time unit name from [" + value[1]
                            + "]. Please use one of following: NANOSECONDS | MICROSECONDS | MILLISECONDS | SECONDS | MINUTES | HOURS | DAYS");
//                    log.error("catch generic exception: " + e);
//                    log.error(StackTraceUtil.getStackTrace(e));
                }
                break;
            default:
                throw new IllegalArgumentException("Unable to extract TimeValue from [" + inXmlValue + "]");
        }
    }

    public long getDuration() {
        return duration;
    }

    public void setDuration(final long inDuration) {
        duration = inDuration;
    }

    public TimeUnit getUnit() {
        return unit;
    }

    public void setUnit(final TimeUnit inUnit) {
        unit = inUnit;
    }

    public boolean isNotZero() {
        return unit != null && duration > 0;
    }
    
    @Override
    public String toString() {
        return "@T{" + duration + " " + unit + '}';
    }
}
