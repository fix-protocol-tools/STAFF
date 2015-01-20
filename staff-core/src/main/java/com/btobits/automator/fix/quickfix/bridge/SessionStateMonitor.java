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
import com.btobits.automator.fix.exception.FixEngineException;
import com.btobits.automator.fix.exception.GenericBusinessLogicException;
import com.btobits.automator.misc.StackTraceUtil;
import org.apache.log4j.Logger;
import quickfix.Message;
import quickfix.Session;
import quickfix.SessionID;

import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

/**
 * @author Kirill_Mukhoiarov
 */

public final class SessionStateMonitor {
    public final static String svnSignature = "$$Rev: 67588 $$ $$Date: 2014-10-28 14:02:42 +0200 (Вт, 28 окт 2014) $$ $$LastChangedBy: Alexander_Sereda $$";
    private static final Logger log = Logger.getLogger( SessionStateMonitor.class );

    private final Lock mutex = new ReentrantLock();

    private final Condition cond = mutex.newCondition();

    private final Set<SessionID> sessionIDs = new HashSet<SessionID>();

    public void onConnected(final SessionID inSessionID) {
        mutex.lock();
        try {
            sessionIDs.add( inSessionID );
            cond.signalAll();
        } finally {
            mutex.unlock();
        }
    }

    public void onDisconnected(final SessionID inSessionID) {
        mutex.lock();
        try {
            sessionIDs.remove( inSessionID );
            cond.signalAll();
        } finally {
            mutex.unlock();
        }
    }

    public void waitForLogin(final SessionID inSessionID,
                             final TimeValue inTimeout) throws GenericBusinessLogicException {
        mutex.lock();
        try {
            try {
                while ( !sessionIDs.contains( inSessionID ) ) {
                    if ( !cond.await( inTimeout.getDuration(), inTimeout.getUnit() ) ) {
                        throw new FixEngineException( "FIX session was not established during " + inTimeout );
                    }
                }
            } catch ( final InterruptedException e ) {
                log.error( "catch generic exception: " + e );
                log.error( StackTraceUtil.getStackTrace( e ) );
                throw new GenericBusinessLogicException( "Execution was interrupted.", e );
            }
        } finally {
            mutex.unlock();
        }
    }

    public void waitForLogout(final SessionID inSessionID, final boolean inSendLogout, final TimeValue inTimeout)
            throws GenericBusinessLogicException {
        mutex.lock();
        try {
            if ( sessionIDs.contains( inSessionID ) ) {
                if ( inSendLogout ) {
                    final Session session = Session.lookupSession( inSessionID );
                    final Message fixMsg = new Message();
                    fixMsg.getHeader().setString( 8, inSessionID.getBeginString() );
                    fixMsg.getHeader().setInt( 35, 5 );
                    fixMsg.setString( 58, "STAFF fixWaitForLogoutTask execution" );
                    session.send( fixMsg );
                }

                try {
                    while ( sessionIDs.contains( inSessionID ) ) {
                        if ( !cond.await( inTimeout.getDuration(), inTimeout.getUnit() ) ) {
                            throw new FixEngineException( "FIX session was not closed during " + inTimeout
                                    + ". SendLogout: " + inSendLogout );
                        }
                    }
                } catch ( final InterruptedException e ) {
                    log.error( "catch generic exception: " + e );
                    log.error( StackTraceUtil.getStackTrace( e ) );
                    throw new GenericBusinessLogicException( "Execution was interrupted.", e );
                }
            } else {
                throw new FixEngineException( "FIX session was not registered" );
            }
        } finally {
            mutex.unlock();
        }
    }
}
