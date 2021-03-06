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

package quickfix.mina.filter;

import quickfix.Message;
import quickfix.SessionID;

import java.util.List;

/**
 * @author Kirill_Mukhoiarov
 */

public interface IFixMsgFilterOut {
    boolean isActive();

    List<Message> filter(final SessionID inSessionID, final Message inMessage, final int inDesiredSeqNum)
            throws SkipMessageSignal;
}
