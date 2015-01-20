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

package com.btobits.automator.fix;

import quickfix.DefaultMessageFactory;
import quickfix.Group;
import quickfix.Message;

/**
 * @author Volodymyr_Biloshkurskyi
 */
public class CustomFactory extends DefaultMessageFactory {
    private quickfix.fix44.MessageFactory fix44Factory =
            new quickfix.fix44.MessageFactory();

    @Override
    public Message create(String beginString, String msgType) {
        if (beginString.equals("FIX.4.0") || beginString.equals("FIX.4.1") ||
                beginString.equals("FIX.4.2") || beginString.equals("FIX.4.3") ||
                beginString.equals("FIX.4.4") || beginString.equals("FIX.5.0")) {
            return super.create(beginString, msgType);
        } else {
            Message message = fix44Factory.create(beginString, msgType);
            message.getHeader().setString(8, beginString);
            return message;
        }
    }

    @Override
    public Group create(String beginString, String msgType, int correspondingFieldID) {
        if (beginString.equals("FIX.4.0") || beginString.equals("FIX.4.1") ||
                beginString.equals("FIX.4.2") || beginString.equals("FIX.4.3") ||
                beginString.equals("FIX.4.4") || beginString.equals("FIX.5.0")) {
            return super.create(beginString, msgType, correspondingFieldID);
        } else {
            Group group = fix44Factory.create(beginString, msgType, correspondingFieldID);
            return group;
        }
    }
}
