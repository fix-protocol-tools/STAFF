/*******************************************************************************
 * Copyright (c) quickfixengine.org  All rights reserved.
 *
 * This file is part of the QuickFIX FIX Engine
 *
 * This file may be distributed under the terms of the quickfixengine.org
 * license as defined by quickfixengine.org and appearing in the file
 * LICENSE included in the packaging of this file.
 *
 * This file is provided AS IS with NO WARRANTY OF ANY KIND, INCLUDING
 * THE WARRANTY OF DESIGN, MERCHANTABILITY AND FITNESS FOR A
 * PARTICULAR PURPOSE.
 *
 * See http://www.quickfixengine.org/LICENSE for licensing information.
 *
 * Contact ask@quickfixengine.org if any conditions of this licensing
 * are not clear to you.
 ******************************************************************************/

package quickfix.mina.initiator;

import org.apache.mina.common.IoSession;
import quickfix.Message;
import quickfix.Session;
import quickfix.mina.AbstractIoHandler;
import quickfix.mina.EventHandlingStrategy;
import quickfix.mina.IoSessionResponder;
import quickfix.mina.NetworkingOptions;
import quickfix.mina.SessionConnector;

public class InitiatorIoHandler extends AbstractIoHandler {
    public final static String svnSignature = "$$Rev: 65481 $$ $$Date: 2014-06-26 13:33:53 +0300 (Чт, 26 июн 2014) $$ $$LastChangedBy: Alexander_Sereda $$";
    private final org.apache.log4j.Logger log4j = org.apache.log4j.Logger.getLogger(getClass());

    private final Session quickfixSession;
    private final EventHandlingStrategy eventHandlingStrategy;

    public InitiatorIoHandler(final Session inQuickFixSession, final NetworkingOptions inNetworkingOptions,
                              final EventHandlingStrategy inEventHandlingStrategy) {
        super(inNetworkingOptions);
        quickfixSession = inQuickFixSession;
        eventHandlingStrategy = inEventHandlingStrategy;
    }

    @Override
    public void sessionCreated(final IoSession inSession) throws Exception {
        super.sessionCreated(inSession);
        inSession.setAttribute(SessionConnector.QF_SESSION, quickfixSession);

        NetworkingOptions networkingOptions = getNetworkingOptions();
        quickfixSession.setResponder(new IoSessionResponder(inSession,
                networkingOptions.getSynchronousWrites(),
                networkingOptions.getSynchronousWriteTimeout()));
        log.info("MINA session created: " + inSession.getLocalAddress());
    }

    @Override
    protected void processMessage(final IoSession inProtocolSession, final Message inMessage) throws Exception {
        eventHandlingStrategy.onMessage(quickfixSession, inMessage);
    }
}
