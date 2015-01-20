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
import org.apache.mina.common.IoAcceptor;
import org.apache.mina.filter.LoggingFilter;
import org.apache.mina.filter.codec.ProtocolCodecFilter;
import org.apache.mina.filter.codec.serialization.ObjectSerializationCodecFactory;
import org.apache.mina.transport.socket.nio.SocketAcceptor;
import org.apache.mina.transport.socket.nio.SocketAcceptorConfig;

import java.net.InetSocketAddress;

public class TcpServer extends BasicHandler implements ITcpProccesor {
    private Logger logger;
    private int port;
    private Integer timeOut;
    private String name;
    private String pattern;

    private boolean useFixCodec = true;

    private IoAcceptor acceptor = new SocketAcceptor();

    public void setUseFixCodec(String useFixCodec) {
        this.useFixCodec = Boolean.parseBoolean(useFixCodec);
    }    

    public TcpServer() {
        PropertyConfigurator.configure("log4j.properties");
        logger = Logger.getLogger(TcpServer.class);
    }

    public String getPattern() {
        return pattern;
    }

    public void setPattern(String pattern) {
        this.pattern = pattern;
    }

    public void setId(String inName) {
        name = inName;
    }

    public void setPort(String port) {
        this.port = Integer.parseInt(port);
    }

    public void setTimeout(String timeout) {
        this.timeOut = Integer.parseInt(timeout);
    }

    public void open() throws Exception {
        if (logger.isDebugEnabled()) {
            logger.debug("Start logger.");
        }

        SocketAcceptorConfig cfg = new SocketAcceptorConfig();
        cfg.setReuseAddress(true);

        if(useFixCodec) {
            cfg.getFilterChain().addLast("codec",
                new ProtocolCodecFilter(new ServerProtocolCodecFactory()));
        } else {
            cfg.getFilterChain().addLast("codec",
                new ProtocolCodecFilter(new ObjectSerializationCodecFactory()));
        }

        cfg.getFilterChain().addLast("logger", new LoggingFilter());

        acceptor.bind(new InetSocketAddress(port), this, cfg);

        System.out.println("Listening on port " + port);
    }

    public void close() throws Exception {
        if (logger.isDebugEnabled()) {
            logger.debug("Stop logger.");
        }

        acceptor.unbindAll();
    }

    public void sendMessage(final ITcpMessage inTcpMessage) throws Exception {
        if(logger.isInfoEnabled()) {
            logger.info(inTcpMessage.toRawMessage());
        }

        System.out.println("Send message: [" + new String(inTcpMessage.toRawMessage()) + "]");
    }

    public void sendMessage(final ITcpMessage inTcpMessage, int inTimeOut) throws Exception {
        if(logger.isInfoEnabled()) {
            logger.info(inTcpMessage.toRawMessage());
        }

        System.out.println("Send message: [" + new String(inTcpMessage.toRawMessage()) + "]");
    }

    public boolean isServer() {
        return true;
    }

    public void log(final String inLine, boolean in) {
        if (in) {
            logger.info(name + " <- " + inLine);
        } else {
            logger.info(name + " -> " + inLine);
        }
    }

    public Message nextMessage() throws Exception {
        return next();
    }

    public Message nextMessage(int seconds) throws Exception {
        return next(seconds);
    }
}
