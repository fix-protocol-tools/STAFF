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

package com.btobits.automator.fix.quickfix.transport.impl;

import org.apache.mina.common.IdleStatus;
import org.apache.mina.common.IoHandlerAdapter;
import org.apache.mina.common.IoSession;

import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingDeque;
import java.util.concurrent.TimeUnit;

import com.btobits.automator.fix.quickfix.transport.Message;

/**
 * @author Volodymyr_Biloshkurskyi
 */
public class BasicHandler extends IoHandlerAdapter {

    private BlockingQueue queue = new LinkedBlockingDeque();

    @Override
    public void sessionOpened(IoSession session) {
        // set idle time to 60 seconds
        session.setIdleTime(IdleStatus.BOTH_IDLE, 60);
    }

    @Override
    public void messageReceived(IoSession session, Object message) {
        try {
            if(message instanceof byte[]) {
                queue.put(new Message(new String((byte[])message), session));
            } else {
                queue.put(new Message(message, session));
            }
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void sessionIdle(IoSession session, IdleStatus status) {
        session.close();
    }

    @Override
    public void exceptionCaught(IoSession session, Throwable cause) {
        session.close();
    }

    public Message next() throws Exception {
        return (Message)queue.poll(30, TimeUnit.MINUTES);
    }

    public Message next(int sec) throws Exception {
        return (Message)queue.poll(sec, TimeUnit.SECONDS);
    }
}
