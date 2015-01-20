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

package com.btobits.automator.fix.comparator;

import com.btobits.automator.fix.utils.fix.FixMessageType;
import quickfix.DataDictionary;
import quickfix.Session;

import java.util.List;

/**
 * @author Volodymyr_Biloshkurs
 */
public abstract class AbstractComparator implements IFixComparator {
    protected DataDictionary dictionary;
    protected String messageType;
    protected String dictType;
    protected List<String> messageErrors;
    protected Session session;
    protected FixMessageType model;

    public AbstractComparator() {

    }

    public AbstractComparator(final DataDictionary inDictionary,
                              final List<String> inMessageErrors) {

        this.dictionary = inDictionary;
        this.messageErrors = inMessageErrors;
    }

    public AbstractComparator(final DataDictionary inDictionary,
                              final String inMessageType, final List<String> inMessageErrors) {

        this.dictionary = inDictionary;
        this.messageType = inMessageType;
        this.messageErrors = inMessageErrors;
    }

    public List<String> getMessageErrors() {
        return messageErrors;
    }

    public void setMessageErrors(List<String> messageErrors) {
        this.messageErrors = messageErrors;
    }

    public String getMessageType() {
        return messageType;
    }

    public void setMessageType(String messageType) {
        this.messageType = messageType;
    }

    public DataDictionary getDictionary() {
        return dictionary;
    }

    public void setDictionary(DataDictionary dictionary) {
        this.dictionary = dictionary;
    }

    public String getDictType() {
        return dictType;
    }

    public void setDictType(String dictType) {
        this.dictType = dictType;
    }

    public Session getSession() {
        return session;
    }

    public void setSession(Session session) {
        this.session = session;
    }

    public void setModel(FixMessageType inModel) {
        this.model = inModel;
    }

    public FixMessageType getModel() {
        return model;
    }
}
