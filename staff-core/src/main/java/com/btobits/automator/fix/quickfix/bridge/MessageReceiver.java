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

package com.btobits.automator.fix.quickfix.bridge;

import com.btobits.automator.ant.types.TimeValue;
import org.apache.log4j.Logger;
import quickfix.Message;
import quickfix.SessionID;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.LinkedBlockingQueue;

/**
 * @author Kirill_Mukhoiarov
 */

public final class MessageReceiver {
    public final static String svnSignature = "$$Rev: 67588 $$ $$Date: 2014-10-28 14:02:42 +0200 (Вт, 28 окт 2014) $$ $$LastChangedBy: Alexander_Sereda $$";
    private final Logger log = Logger.getLogger(getClass());

    private final Map<SessionID, LinkedBlockingQueue<Message>> queues = new HashMap<SessionID, LinkedBlockingQueue<Message>>();

    public MessageReceiver() {
    }

    private LinkedBlockingQueue<Message> getQueue(SessionID sessionID) {
        LinkedBlockingQueue<Message> queue = null;
        synchronized (queues) {
            if (!queues.containsKey(sessionID)) {
                queues.put(sessionID, new LinkedBlockingQueue<Message>());
            }
            queue = queues.get(sessionID);
        }
        return queue;
    }

    public void onMessageReceived(Message msg, SessionID sessionID) {
        try {
            getQueue(sessionID).put(msg);
        } catch (InterruptedException e) {
        }
    }

    public Message getNextMessage(SessionID sessionID, TimeValue inTimeout) throws Exception {
        return getQueue(sessionID).poll(inTimeout.getDuration(), inTimeout.getUnit());
    }

    public void cleanQueue(SessionID sessionID) throws Exception {
        getQueue(sessionID).clear();
    }
}
