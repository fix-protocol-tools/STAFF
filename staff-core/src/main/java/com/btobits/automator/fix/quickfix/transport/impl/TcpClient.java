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

import com.btobits.automator.fix.quickfix.transport.ITcpMessage;
import com.btobits.automator.fix.quickfix.transport.ITcpProccesor;
import com.btobits.automator.fix.quickfix.transport.Message;
import org.apache.log4j.Logger;
import org.apache.log4j.PropertyConfigurator;
import org.apache.mina.common.ConnectFuture;
import org.apache.mina.common.IoSession;
import org.apache.mina.common.RuntimeIOException;
import org.apache.mina.filter.LoggingFilter;
import org.apache.mina.filter.codec.ProtocolCodecFilter;
import org.apache.mina.filter.codec.serialization.ObjectSerializationCodecFactory;
import org.apache.mina.transport.socket.nio.SocketConnector;
import org.apache.mina.transport.socket.nio.SocketConnectorConfig;
import org.apache.tools.ant.BuildException;

import java.net.InetSocketAddress;

public class TcpClient extends ClientHandler implements ITcpProccesor {
    private Logger logger = Logger.getLogger(TcpClient.class);
    private String host;
    private int port;
    private String name;

    private boolean useFixCodec = true;

    SocketConnector connector = new SocketConnector();

    private static final int CONNECT_TIMEOUT = 30; // seconds

    private IoSession session;

    public void setUseFixCodec(String useFixCodec) {
        this.useFixCodec = Boolean.parseBoolean(useFixCodec);
    }

    /**
     * Set client name.
     *
     * @param inName name of transport client
     */
    public void setId(final String inName) {
        name = inName;
    }

    public TcpClient() throws Exception {
        PropertyConfigurator.configure("log4j.properties");
        logger = Logger.getLogger(TcpClient.class);
    }

    /**
     * Set client host.
     *
     * @param inHost name of connected host
     */
    public void setHost(final String inHost) {
        this.host = inHost;
    }

    /**
     * Set client port.
     *
     * @param inPort port number
     */
    public void setPort(final String inPort) {
        this.port = Integer.parseInt(inPort);
    }

    /**
     * Open client socket.
     * <p/>
     * throws Exception on errors
     */
    public void open() throws Exception {
        if(logger.isDebugEnabled()) {
            logger.debug("Start logger.");
        }

        connector.setWorkerTimeout(10000);

        // Configure the service.
        SocketConnectorConfig cfg = new SocketConnectorConfig();
        cfg.setConnectTimeout(CONNECT_TIMEOUT);
        cfg.setConnectTimeout(10);

        if(useFixCodec) {
            cfg.getFilterChain().addLast("codec",
                new ProtocolCodecFilter(new ServerProtocolCodecFactory()));
        } else {
            cfg.getFilterChain().addLast("codec", new ProtocolCodecFilter(
                new ObjectSerializationCodecFactory()));
        }

        cfg.getFilterChain().addLast("logger", new LoggingFilter());

        for (int i=0; i < 20; i++) {
            try {
                System.out.println("Try connect.");
                final ConnectFuture future =
                connector.connect(new InetSocketAddress(host, port), this, cfg);

                future.join();
                session = future.getSession();
                return;
            } catch (RuntimeIOException e) {
                Thread.sleep(5000);
            }
        }

        throw new BuildException("Failed connect.");
    }

    /**
     * Close client socket.
     */
    public void close() throws Exception {
        if(logger.isDebugEnabled()) {
            logger.debug("Stop logger.");
        }
    }

    /**
     * Send transport message to server.
     *
     * @param inTcpMessage message
     */
    public void sendMessage(final ITcpMessage inTcpMessage) throws Exception {
        if(logger.isInfoEnabled()) {
            logger.info(inTcpMessage.toRawMessage());
        }

        System.out.println("Send message: [" + new String(inTcpMessage.toRawMessage()) + "]");

        session.write(inTcpMessage.toRawMessage());
    }

    /**
     * Check is that server.
     *
     * @return false - client
     */
    public boolean isServer() {
        return false;
    }

    /**
     * Set client name.
     *
     * @param inLine line write to socket.
     * @param in     in - true write "<-", else "->"
     */
    public void log(final String inLine, final boolean in) {
        if (in) {
            logger.info(name + " <- " + inLine);
        } else {
            logger.info(name + " -> " + inLine);
        }
    }

    public void sendMessage(final ITcpMessage inTcpMessage, final int timeout) throws Exception {
        if(logger.isInfoEnabled()) {
            logger.info(inTcpMessage.toRawMessage());
        }

        System.out.println("Send message: [" + new String(inTcpMessage.toRawMessage()) + "]");

        session.write(inTcpMessage.toRawMessage());
    }

    public Message nextMessage() throws Exception {
        return next();
    }

    public Message nextMessage(int seconds) throws Exception {
        return next(seconds);
    }
}
