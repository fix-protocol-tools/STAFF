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
import com.btobits.automator.fix.MessageDifference;
import com.btobits.automator.fix.exception.GenericBusinessLogicException;
import com.btobits.automator.fix.utils.MessageBuilder;
import com.btobits.automator.fix.utils.MessageDifferenceException;
import com.btobits.automator.fix.utils.fix.FixMessageType;
import com.btobits.automator.misc.StackTraceUtil;
import junit.framework.Assert;
import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;
import quickfix.*;
import quickfix.field.MsgType;
import quickfix.mina.filter.IFixMsgFilterIn;
import quickfix.mina.filter.IFixMsgFilterOut;
import quickfix.mina.filter.IMessageFilterContainer;
import quickfix.mina.filter.IRawMsgFilterIn;
import quickfix.mina.filter.IRawMsgFilterOut;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;

public class FixConnectivity extends ApplicationAdapter implements IMessageFilterContainer, IConnectionListener {
    private final Logger log = Logger.getLogger(getClass());

    private final FactoryHolder manager = new FactoryHolder();

    private final MessageReceiver receiver = new MessageReceiver();

    private final SessionStateMonitor sessionStateMonitor = new SessionStateMonitor();

    private ControlLevel controlLevel = ControlLevel.APPLICATION;

    private IRawMsgFilterIn rawFilterIn = null;
    private IRawMsgFilterOut rawFilterOut = null;

    private IFixMsgFilterIn fixFilterIn = null;
    private IFixMsgFilterOut fixFilterOut = null;

    private final List<IConnectionListener> connectionListeners = Collections.synchronizedList( new ArrayList<IConnectionListener>( 5 ) );

    public void addConnectionListener(final IConnectionListener inListener) {
        if (!connectionListeners.contains( inListener )) {
            connectionListeners.add( inListener );
        }
    }

    public void removeConnectionListener(final IConnectionListener inListener) {
        if (connectionListeners.contains( inListener )) {
            connectionListeners.remove( inListener );
        }
    }

    @Override
    public void sessionConnected(final List<SessionID> inSessionIDList) {
        for (final IConnectionListener listener : connectionListeners) {
            listener.sessionConnected( inSessionIDList );
        }
    }

    @Override
    public void sessionDisconnected(final List<SessionID> inSessionIDList) {
        for (final IConnectionListener listener : connectionListeners) {
            listener.sessionDisconnected( inSessionIDList );
        }
    }

    public void configure(final String inFileName) throws Exception {
        manager.configure( inFileName );
    }

    public FactoryHolder getManager() {
        return manager;
    }

    public static boolean isSessionActive(final SessionID sessionID) {
        Assert.assertNotNull( "FIX sessionID is not specified", sessionID);
        return Session.lookupSession(sessionID)==null ? false : true;
    }

    public static Session getSession(final SessionID sessionID) throws SessionNotFound {
        Assert.assertNotNull( "FIX sessionID is not specified", sessionID);
        Assert.assertNotNull( "FIX senderCompID is not specified", sessionID.getSenderCompID());
        Assert.assertNotNull( "FIX targetCompID is not specified", sessionID.getTargetCompID());
        Assert.assertNotNull( "FIX version is not specified", sessionID.getBeginString());

        final Session session = Session.lookupSession(sessionID);
        if (session == null) {
            throw new SessionNotFound("SenderCompID=" + sessionID.getSenderCompID() + " TargetCompID=" + sessionID.getSenderCompID() +
                    " ProtocolType=" + sessionID.getBeginString());
        }
        return session;
    }

    public void waitForLogonEvent(final SessionID inSessionID, final TimeValue inTimeout)
            throws GenericBusinessLogicException {
        sessionStateMonitor.waitForLogin(inSessionID, inTimeout);
    }

    public void waitForLogoutEvent(final SessionID inSessionID, final boolean sendLogout, final TimeValue inTimeout)
            throws GenericBusinessLogicException {
        sessionStateMonitor.waitForLogout(inSessionID, sendLogout, inTimeout);
    }

    public void sequenceReset(final SessionID sessionID, final int newSeqNum) throws SessionNotFound, IOException {
        Session session = getSession(sessionID);
        session.sequenceReset(newSeqNum);
    }

    public void sendRawMessage(final SessionID sessionID, final String inMessage, final boolean inValidate, final FillHeaderTrailer appropriateHeaderTrailer) throws Exception {

        final String replaced = inMessage.replaceAll("/u0001", Character.toString('\u0001'));

//        if (fixVersion==null) {
//            fixVersion = MessageUtils.getStringField(replaced, FixUtils.TYPE);
//        }

        final Session session = getSession(sessionID);
//        if (appropriateHeaderTrailer) {
//            // target and sender takes from fixSend task
//            session = getSession(inSenderCompID, inTargetCompID, fixVersion);
//        } else {
//            // default values
//            // target and sender takes from raw messages
////            session = getSession(senderFromRawMes, targetFromRawMes, fixVersion);
//            session = getSession(inSenderCompID, inTargetCompID, fixVersion);
//        }

        // validate message
        if (inValidate) {
            try {
                final Message ourMessage = MessageUtils2.parse(session, replaced);
                if(sessionID.isFIXT()) {
                    MessageUtils2.validate(ourMessage, sessionID, session.getDataDictionaryProvider(),
                            session.getTargetDefaultApplicationVersionID());
                } else {
                    MessageUtils2.validate(ourMessage, session.getDataDictionary());
                }
            } catch (final Exception e) {
                log.error("validation: got generic exception: " + e, e);
                throw new Exception("Validation failed: " + e, e);
            }
        }
        boolean resultSending = false;
        System.out.println("Send fix message [" + replaced + "].");
        switch (appropriateHeaderTrailer) {
            case NONE:
                resultSending = session.sendAsRaw(replaced);
                break;
            case ONLYREQ:
                resultSending = session.sendAsRaw(MessageUtils2.parse(session, replaced), false);
                break;
            case FULL:
                resultSending = session.sendAsRaw(MessageUtils2.parse(session, replaced), true);
                break;
        }
        if (!resultSending) {
            throw new Exception("Write to FIX network layer was not successful");
        }
    }

    public static void sendMessage(final SessionID inSessionID, final FixMessageType inMsg, final boolean inValidate) throws Exception {
        Assert.assertNotNull("FIX message to send is not specified", inMsg);
        final Session session = getSession(inSessionID);
        // TODO: remove sender/target/version from parameters of MessageBuilder
        final Message fixMsg = MessageBuilder.build(inSessionID.getSenderCompID(), inSessionID.getTargetCompID(), inSessionID.getBeginString(),
                session, session.getTargetDefaultApplicationVersionID(), inMsg, null);
        if (inValidate) {
            try {
                MessageUtils2.validate(fixMsg, session.getSessionID(), session.getDataDictionaryProvider(),
                        session.getTargetDefaultApplicationVersionID());
            } catch (final Exception e) {
                throw new Exception("Validation failed: " + e);
            }
        }

        System.out.println("Send fix message [" + fixMsg.toString() + "].");

        if (!session.send(fixMsg)) {
            throw new Exception("Write to FIX network layer was not successful");
        }
    }

    public void delayBeforeNextMessage(final SessionID sessionsID, final TimeValue inTimeout) throws Exception {
        Assert.assertTrue("Session ["+sessionsID+"] is not active.", isSessionActive(sessionsID));
        final Message fixMsg = receiver.getNextMessage(sessionsID, inTimeout );
        if (fixMsg != null) {
            throw new Exception("Message received within " + inTimeout + " timeout.");
        }
    }

    public void compareMessage2(final SessionID sessionsID, TimeValue inTimeout,
                                final FixMessageType inModel, boolean inBackCheckHeader, boolean inBackCheckTrailer,
                                boolean inBackCheckBody, boolean inCheckGroupOrder, boolean inValidateMessage)
            throws Exception {

        final Session session = getSession(sessionsID);
        final Message fixMsg = receiver.getNextMessage(sessionsID, inTimeout);

        Assert.assertNotNull("Can not receive message from FIX session - timeout expired (" + inTimeout + " seconds)", fixMsg);
        // In STAFF 3.x.x we can use fixReceive for session level
//        Assert.assertFalse("We reject message, error [" + getRejectDescription(fixMsg) + "].", isRejectMessage(fixMsg));
        System.out.println("Receive fix message [" + fixMsg + "].");

        if (inValidateMessage) {
            try {
                if(sessionsID.isFIXT()) {
                    MessageUtils2.validate(fixMsg, sessionsID, session.getDataDictionaryProvider(),
                            session.getSenderDefaultApplicationVersionID());
                } else {
                    MessageUtils2.validate(fixMsg, session.getDataDictionary());
                }
            } catch (final Exception ex) {
                log.error("got generic exception: " + ex);
                log.error(StackTraceUtil.getStackTrace(ex));
                throw new Exception("Validation failed, incoming fix message [" + fixMsg + "] :", ex);
            }
        }

        if (inModel == null) {
            throw new Exception("Unexpected FIX message received: " + fixMsg.toString());
        }

        try {
            if (inModel.getData() != null && !"".equals(inModel.getData())) {
                final String fixStr = fixMsg.toString();
                String ourStr = inModel.getData();
                ourStr = ourStr.replace("/u0001", "\u0001");
                if (!ourStr.equals(fixStr)) {
                    throw new Exception("Message [" + ourStr + "] does not equals received message [" + fixStr
                            + "]");
                }
            } else {
                if(sessionsID.isFIXT()) {
                    MessageDifference.compare2(fixMsg, inModel, session.getDataDictionaryProvider(),
                            session.getSenderDefaultApplicationVersionID(), inBackCheckHeader,
                            inBackCheckTrailer, inBackCheckBody, inCheckGroupOrder);
                } else {
                    MessageDifference.compare2(fixMsg, inModel, session.getDataDictionary(),
                            session.getDataDictionary(), inBackCheckHeader,
                            inBackCheckTrailer, inBackCheckBody, inCheckGroupOrder);
                }
            }
        } catch (Exception e) {
            throw new MessageDifferenceException("Error comparing messages: " + fixMsg.toString() + ". Cause: "+ e.getMessage(), e);
        }
    }

    public void setControlLevel(final SessionID sessionID, ControlLevel controlLevel) throws Exception {
        // TODO: add synchronization: while change ControlLevel and clean queue, method add to queue must be blocked
        this.controlLevel = controlLevel;
        receiver.cleanQueue(sessionID);
    }

    @Override
    public void fromApp(final Message inMsg, final SessionID inSessionID)
            throws FieldNotFound, IncorrectDataFormat, IncorrectTagValue, UnsupportedMessageType {
        if(log.isDebugEnabled()) {
            log.debug("received application msg: " + inMsg.toString());
        }
        switch (controlLevel) {
            case BOTH:
            case APPLICATION:
                receiver.onMessageReceived(inMsg, inSessionID);
        }
    }

    @Override
    public void fromAdmin(final Message inMsg, final SessionID inSessionID)
            throws FieldNotFound, IncorrectDataFormat, IncorrectTagValue, RejectLogon {
        if(log.isDebugEnabled()) {
            log.debug("received admin msg: " + inMsg.toString());
        }
        switch (controlLevel) {
            case BOTH:
            case SESSION:
                receiver.onMessageReceived(inMsg, inSessionID);
        }
    }

    public void onIgnored(final Message inMsg, boolean isAdmin,final SessionID inSessionID)
            throws UnsupportedMessageType, IncorrectTagValue, FieldNotFound, IncorrectDataFormat, RejectLogon {
        if (isAdmin) {
            fromAdmin(inMsg, inSessionID);
        } else {
            fromApp(inMsg, inSessionID);
        }
    }

    @Override
    public void onLogon(SessionID sessionID) {
        if(log.isDebugEnabled()) {
            log.debug("onLogon: " + sessionID);
        }
        sessionStateMonitor.onConnected(sessionID);
    }

    @Override
    public void onLogout(SessionID sessionID) {
        if(log.isDebugEnabled()) {
            log.debug("onLogout: " + sessionID);
        }
        sessionStateMonitor.onDisconnected(sessionID);
    }

    @Override
    public void toAdmin(final Message inMessage, final SessionID inSessionID) {
        if(log.isDebugEnabled()) {
            log.debug("sending admin msg: " + inMessage.toString());
        }
        if (isMessageOfType(inMessage, MsgType.LOGON)) {
            String username;
            String password;
            try {
                username = manager.getSettings().getString("Username");
                if (username != null) {
                    inMessage.getHeader().setField(new StringField(553, username));
                }
            } catch (ConfigError e) {
            } catch (FieldConvertError e) {
            }
            try {
                password = manager.getSettings().getString("Password");
                if (password != null) {
                    inMessage.getHeader().setField(new StringField(554, password));
                }
            } catch (ConfigError e) {
            } catch (FieldConvertError e) {
            }
        }
        if (isRejectMessage(inMessage)) {
            receiver.onMessageReceived(inMessage, inSessionID);
        }
    }

    public void toApp(quickfix.Message message, quickfix.SessionID sessionId) throws quickfix.DoNotSend {
        if(log.isDebugEnabled()) {
            log.debug("sending app msg: " + message.toString());
        }
    }

    private static boolean isMessageOfType(final Message message, final String type) {
        try {
            return type.equals(message.getHeader().getField(new MsgType()).getValue());
        } catch (FieldNotFound e) {
            logErrorToSessionLog(message, e);
            return false;
        }
    }

    private static void logErrorToSessionLog(final Message message, final FieldNotFound e) {
        LogUtil.logThrowable(MessageUtils.getSessionID(message), e.getMessage(), e);
    }

    private static boolean isRejectMessage(final Message inMessage) {
        boolean result = false;
        try {
            Message.Header hdr = inMessage.getHeader();
            Iterator<Field<?>> fields = hdr.iterator();
            while (fields.hasNext()) {
                Field<?> flField = fields.next();
                if ("3".equals(flField.getObject()) && flField.getField() == 35) {
                    result = true;
                    break;
                }
            }
        } catch (Exception ex) {
        }

        return result;
    }

    private static String getRejectDescription(final Message inMessage) {
        String result;
        result = getFieldData(inMessage, 58) + " /" + getFieldData(inMessage, 371) + "/";
        return result;
    }

    private static String getFieldData(final Message inMessage, final int inFieldId) {
        String result = "";
        try {
            if (inMessage.isSetField(inFieldId)) {
                StringField fld = inMessage.getField(new StringField(inFieldId));
                if (fld.getValue() != null) {
                    result = StringUtils.trimToEmpty(fld.getValue());
                }
            }
        } catch (Exception ex) {
        }

        return result;
    }

    public void setRawFilter(final IRawMsgFilterIn inRawFilterIn) {
        rawFilterIn = inRawFilterIn;
    }

    public void setRawFilter(final IRawMsgFilterOut inRawFilterOut) {
        rawFilterOut = inRawFilterOut;
    }

    public void setFixFilter(final IFixMsgFilterIn inFixFilterIn) {
        fixFilterIn = inFixFilterIn;
    }

    public void setFixFilter(final IFixMsgFilterOut inFixFilterOut) {
        fixFilterOut = inFixFilterOut;
    }

    @Override
    public IRawMsgFilterIn getRawFilterIn() {
        return rawFilterIn;
    }

    @Override
    public IRawMsgFilterOut getRawFilterOut() {
        return rawFilterOut;
    }

    @Override
    public IFixMsgFilterIn getFixFilterIn() {
        return fixFilterIn;
    }

    @Override
    public IFixMsgFilterOut getFixFilterOut() {
        return fixFilterOut;
    }
}
