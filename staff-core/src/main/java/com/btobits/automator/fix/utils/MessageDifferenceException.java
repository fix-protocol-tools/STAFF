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

import org.apache.tools.ant.BuildException;
import org.apache.tools.ant.Location;

/**
 * @author Mykhailo_Sereda
 */
public class MessageDifferenceException extends BuildException {
    public MessageDifferenceException(final String message) {
        super(message);
    }

    public MessageDifferenceException(final String message, final Throwable cause) {
        super(message, cause);
    }

    public MessageDifferenceException(final String message, final Throwable cause, final Location location) {
        super(message, cause, location);
    }

}
