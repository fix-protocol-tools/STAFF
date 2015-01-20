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

package com.btobits.automator.fix.utils.fix;

import com.btobits.automator.fix.ant.filter.FixFilterRawIn;
import com.btobits.automator.fix.ant.filter.FixFilterRawOut;
import com.btobits.automator.fix.quickfix.bridge.FactoryHolder;
import com.btobits.automator.fix.quickfix.bridge.FixConnectivity;
import org.apache.log4j.Logger;
import org.apache.tools.ant.Project;
import org.apache.tools.ant.types.DataType;
import quickfix.*;
import quickfix.field.DefaultApplVerID;
import quickfix.mina.filter.IFixMsgFilterIn;
import quickfix.mina.filter.IFixMsgFilterOut;

import java.util.Iterator;

/**
 * @author Mykhailo_Sereda
 */
public abstract class FixSession extends DataType implements IControl, IFixConnectivity {
    private final Logger log = Logger.getLogger(getClass());

    private FixConnectivity connectivity = new FixConnectivity();
    private Connector session;
    private boolean isRunning = false;
    private DefaultSessionFactory sessionFactory;
    private DefaultDataDictionaryProvider dataDictionaryProvider = null;
    private DefaultApplVerID senderDefaultApplVerID;
    private SessionID sessionID;

    public FixSession() {
        super();
    }

    public FixSession(final Project inProject) {
        super();
        setProject(inProject);
    }

    public synchronized void setConnectivity(final FixConnectivity inConnectivity) {
        if (connectivity == null) {
            throw new IllegalArgumentException("Connectivity is not specified");
        }
        if (isRunning) {
            throw new IllegalStateException("Session is running " + getSessionName());
        }
        connectivity = inConnectivity;
    }

    public synchronized void setSettingsFile(final String inFileName) throws Exception {
        if (isRunning) {
            throw new IllegalStateException("Session is running");
        }
        try {
            connectivity.configure(inFileName);
            final SessionSettings settings = connectivity.getManager().getSettings();
            sessionFactory = createSessionFactory(connectivity);
            final Iterator<SessionID> iterator = settings.sectionIterator();
            boolean sessionParametersConfigured = false;
            while (iterator.hasNext()) {
                if (!sessionParametersConfigured) {
                    sessionID = iterator.next();
                    dataDictionaryProvider = sessionFactory.getDataDictionaryProvider(sessionID, settings);
                    senderDefaultApplVerID = sessionFactory.getSenderDefaultApplVerID(sessionID, settings);
                    sessionParametersConfigured = true;
                } else {
                    log.error("Strange, more than 1 session registered..."  );
                }
            }
        } catch (Exception e) {
            throw new Exception("Configuration error: " + e);
        }
    }

    protected static DefaultSessionFactory createSessionFactory(FixConnectivity connectivity) {
        final FactoryHolder connectivityManager = connectivity.getManager();
        return new DefaultSessionFactory(connectivity, connectivityManager.getStoreFactory(), connectivityManager.getLogFactory(),
                connectivityManager.getMessageFactory());
    }

    @Override
    public synchronized void start() throws Exception {
        if (!isRunning) {
            try {
                session = createSession(connectivity);
            } catch (Exception e) {
                throw new Exception("Can not create FIX session: " + getSessionName() + ". Cause: " + e.getMessage(), e);
            }
            getSession().start();
            isRunning = true;
        }
    }

    protected abstract Connector createSession(FixConnectivity connectivity) throws ConfigError;

    public String getSessionName() {
        return getSenderCompId() + getTargetCompId();
    }

    public String getFixVersion() {
        return sessionID.getBeginString();
    }

    public String getSenderCompId() {
        return sessionID.getSenderCompID();
    }

    public String getTargetCompId() {
        return sessionID.getTargetCompID();
    }

    @Override
    public synchronized void stop() {
        stop(true);
    }

    @Override
    public synchronized void stop(final boolean needSendLogout) {
        if (isRunning) {
            getSession().stop(!needSendLogout);
            isRunning = false;
        }
    }

    @Override
    public synchronized FixConnectivity getConnectivity() {
        return connectivity;
    }

    public MessageFactory getMessageFactory() {
        return getConnectivity().getManager().getMessageFactory();
    }

    public DefaultDataDictionaryProvider getDataDictionaryProvider() {
        if (dataDictionaryProvider==null) {
            throw new NullPointerException("DefaultDataDictionaryProvider was not initialized");
        }
        return dataDictionaryProvider;
    }

    public DefaultApplVerID getSenderDefaultApplVerID() {
        if (senderDefaultApplVerID ==null) {
            throw new NullPointerException("SenderDefaultApplVerID was not initialized");
        }
        return senderDefaultApplVerID;
    }

    protected Connector getSession() {
        if (session == null) {
            throw new IllegalStateException("FIX Session is not configured " + getSessionName());
        }
        return session;
    }

    public SessionID getSessionID() {
        if (sessionID == null) {
            // FIXME: getSessionName() use sessionID
            throw new IllegalStateException("FIX Session is not configured " + getSessionName());
        }
        return sessionID;
    }

    public void setFixFilter(final IFixMsgFilterIn inFixFilter) {
        connectivity.setFixFilter(inFixFilter);
    }

    public void setFixFilter(final IFixMsgFilterOut inFixFilter) {
        connectivity.setFixFilter(inFixFilter);
    }

    public void setRawFilter(final FixFilterRawIn inFixFilter) {
        connectivity.setRawFilter(inFixFilter);
    }

    public void setRawFilter(final FixFilterRawOut inFixFilter) {
        connectivity.setRawFilter(inFixFilter);
    }
}
