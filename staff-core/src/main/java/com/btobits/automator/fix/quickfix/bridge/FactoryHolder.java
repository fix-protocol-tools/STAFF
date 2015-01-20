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

import com.btobits.automator.fix.CustomFactory;
import org.apache.log4j.Logger;
import quickfix.FileLogFactory;
import quickfix.FileStoreFactory;
import quickfix.LogFactory;
import quickfix.MessageFactory;
import quickfix.MessageStoreFactory;
import quickfix.SessionSettings;

import java.io.FileInputStream;

/**
 * @author Kirill_Mukhoiarov
 */

public final class FactoryHolder {
    public final static String svnSignature = "$$Rev: 67588 $$ $$Date: 2014-10-28 14:02:42 +0200 (Вт, 28 окт 2014) $$ $$LastChangedBy: Alexander_Sereda $$";
    private final Logger log = Logger.getLogger(getClass());

    private SessionSettings settings;

    private MessageStoreFactory storeFactory;

    private LogFactory logFactory;

    private MessageFactory messageFactory = new CustomFactory();

    public SessionSettings getSettings() {
        return settings;
    }

    public void setSettings(final SessionSettings inSettings) {
        settings = inSettings;
    }

    public MessageStoreFactory getStoreFactory() {
        return storeFactory;
    }

    public void setStoreFactory(final MessageStoreFactory inStoreFactory) {
        storeFactory = inStoreFactory;
    }

    public LogFactory getLogFactory() {
        return logFactory;
    }

    public void setLogFactory(final LogFactory inLogFactory) {
        logFactory = inLogFactory;
    }

    public MessageFactory getMessageFactory() {
        return messageFactory;
    }

    public void setMessageFactory(final MessageFactory inMessageFactory) {
        messageFactory = inMessageFactory;
    }

    public void configure(final String inFileName) throws Exception {
        final FileInputStream inStream = new FileInputStream(inFileName);
        try {
            setSettings(new SessionSettings(inStream));
            setStoreFactory(new FileStoreFactory(getSettings()));
            setLogFactory(new FileLogFactory(getSettings()));
        } finally {
            inStream.close();
        }
    }
}
