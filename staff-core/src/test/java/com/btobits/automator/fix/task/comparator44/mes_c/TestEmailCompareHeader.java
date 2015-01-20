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

package com.btobits.automator.fix.task.comparator44.mes_c;

import com.btobits.automator.fix.comparator.HeaderComparator;
import com.btobits.automator.fix.utils.FixUtils;
import com.btobits.automator.fix.utils.fix.FixMessageType;
import junit.framework.TestCase;
import quickfix.ConfigError;
import quickfix.DataDictionary;
import quickfix.Message;

import java.util.List;
import java.util.ArrayList;

/**
 * @author: Volodymyr_Biloshkurskyi
 */

public final class TestEmailCompareHeader extends TestCase {

    private static DataDictionary dataDictionary = null;
    
    private static final String PATH = "dictionary/FIX44.xml";

    final List<String> messageErrors = new ArrayList<String>();

    @Override
    protected void setUp() throws Exception {
        super.setUp();
        messageErrors.clear();
    }

    private DataDictionary getDataDictionary() throws ConfigError {
        if(dataDictionary == null) {
            dataDictionary = new DataDictionary(PATH);
        }

        return dataDictionary;
    }

    public void testCompareHeaderField8() throws Exception {
        final HeaderComparator comparator = new HeaderComparator(getDataDictionary(), messageErrors);
        
        Message fixMsg = new Message();
        fixMsg.getHeader().setString(8, "FIX.4.4");
        fixMsg.getHeader().setString(35, "C");

        final FixMessageType fixEtanolMessage = new FixMessageType();
        fixEtanolMessage.addField(new FixMessageType.Field("8", "FIX.4.4"));
        comparator.setModel(fixEtanolMessage);        

        final FixMessageType recivedMessage = FixUtils.getMessageType(fixMsg, getDataDictionary());
        comparator.compare(recivedMessage);

        assertTrue("Error comparation messages " + messageErrors, messageErrors.isEmpty());
    }

    public void testCompareHeaderField8Failed() throws Exception {
        final HeaderComparator comparator = new HeaderComparator(getDataDictionary(), messageErrors);

        Message fixMsg = new Message();
        fixMsg.getHeader().setString(0, "FIX.4.4");
        fixMsg.getHeader().setString(35, "C");

        final FixMessageType fixEtanolMessage = new FixMessageType();
        fixEtanolMessage.addField(new FixMessageType.Field("8", "FIX.4.4"));
        comparator.setModel(fixEtanolMessage);

        final FixMessageType recivedMessage = FixUtils.getMessageType(fixMsg, getDataDictionary());
        comparator.compare(recivedMessage);

        assertFalse("Error comparation messages " + messageErrors, messageErrors.isEmpty());
    }

    public void testCompareHeaderField35Failed() throws Exception {
        final HeaderComparator comparator = new HeaderComparator(getDataDictionary(), messageErrors);

        Message fixMsg = new Message();
        fixMsg.getHeader().setString(8, "FIX.4.4");
        fixMsg.getHeader().setString(35, "C");

        final FixMessageType fixEtanolMessage = new FixMessageType();
        fixEtanolMessage.addField(new FixMessageType.Field("8", "FIX.4.4"));
        fixEtanolMessage.addField(new FixMessageType.Field("35", "A"));
        comparator.setModel(fixEtanolMessage);

        final FixMessageType recivedMessage = FixUtils.getMessageType(fixMsg, getDataDictionary());
        comparator.compare(recivedMessage);

        assertFalse("Error comparation message type field: " + messageErrors, messageErrors.isEmpty());
    }

    public void testCompareHeaderField35() throws Exception {
        final HeaderComparator comparator = new HeaderComparator(getDataDictionary(), messageErrors);

        Message fixMsg = new Message();
        fixMsg.getHeader().setString(8, "FIX.4.4");
        fixMsg.getHeader().setString(35, "C");

        final FixMessageType fixEtanolMessage = new FixMessageType();
        fixEtanolMessage.addField(new FixMessageType.Field("8", "FIX.4.4"));
        fixEtanolMessage.addField(new FixMessageType.Field("35", "C"));
        comparator.setModel(fixEtanolMessage);

        final FixMessageType recivedMessage = FixUtils.getMessageType(fixMsg, getDataDictionary());
        comparator.compare(recivedMessage);

        assertTrue("Error comparation message type field " + messageErrors, messageErrors.isEmpty());
    }
}