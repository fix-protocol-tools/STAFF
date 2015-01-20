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

package com.btobits.automator.fix.quickfix.transport;

public interface ITcpProccesor {
    /**
     * Check is that server or client.
     */
    boolean isServer();

    /**
     * Open proccesor.
     */
    void open() throws Exception;

    /**
     * Close proccesor
     */
    void close() throws Exception;

    /**
     * Send message.
     */
    void sendMessage(final ITcpMessage inTcpMessage) throws Exception;

    /**
     * Send message.
     */
    void sendMessage(final ITcpMessage inTcpMessage, int timeout) throws Exception;

    /**
     * Write message to log file.
     */
    void log(final String inLine, final boolean in);

    /**
     * Read message;
     * */
    Message nextMessage() throws Exception;

    /**
     * Read message with time interval;
     * */
    Message nextMessage(int seconds) throws Exception;
}
