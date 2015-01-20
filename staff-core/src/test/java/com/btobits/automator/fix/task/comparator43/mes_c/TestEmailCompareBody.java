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

package com.btobits.automator.fix.task.comparator43.mes_c;

import com.btobits.automator.fix.comparator.BodyComparator2;
import com.btobits.automator.fix.task.comparator.AbstractEmailTestCase;
import com.btobits.automator.fix.utils.FixUtils;
import com.btobits.automator.fix.utils.fix.FixMessageType;
import quickfix.Group;
import quickfix.Message;
import quickfix.DataDictionary;

/**
 * @author: Volodymyr_Biloshkurskyi
 */

public class TestEmailCompareBody extends AbstractEmailTestCase {

    private static final String PATH = "dictionary/FIX43.xml";    
    private static final String FIX_4_3 = "FIX.4.3";

    protected DataDictionary getDataDictionary() throws Exception {
        return getDataDictionary(PATH);
    }

    public void testCompareBody() throws Exception {
        final BodyComparator2 comparator = new BodyComparator2(getDataDictionary(PATH), messageErrors);

        Message fixMsg = createMessageForTest(FIX_4_3);

        final FixMessageType fixEtanolMessage = new FixMessageType();
        insertFields(fixEtanolMessage, new String [][] {{"164", "11"}, {"8", FIX_4_3}});

        comparator.setModel(fixEtanolMessage);

        final FixMessageType recivedMessage = FixUtils.getMessageType(fixMsg, getDataDictionary(PATH));
        comparator.compare2(recivedMessage);

        assertFalse("Error comparation messages, error " + messageErrors, messageErrors.isEmpty());
    }

    public void testCompareBodyFailed() throws Exception {
        final BodyComparator2 comparator = new BodyComparator2(getDataDictionary(PATH), messageErrors);

        Message fixMsg = createMessageForTest(FIX_4_3);

        final FixMessageType fixEtanolMessage = new FixMessageType();
        insertFields(fixEtanolMessage, new String [][] {{"164", "11"}, {"8", FIX_4_3}});

        comparator.setModel(fixEtanolMessage);

        final FixMessageType recivedMessage = FixUtils.getMessageType(fixMsg, getDataDictionary(PATH));
        comparator.compare2(recivedMessage);

        assertFalse("Error comparation messages, error " + messageErrors, messageErrors.isEmpty());
    }

    public void testCompareBodyGroups() throws Exception {
        final BodyComparator2 comparator = new BodyComparator2(getDataDictionary(PATH), messageErrors);

        final Message fixMsg = createMessageForTest(FIX_4_3);

        fillFixGroups(33, fixMsg, new String[][]{{"58", "1"}});

        final FixMessageType fixEtanolMessage = new FixMessageType();
        insertFields(fixEtanolMessage, new String [][] {{"164", "1"}, {"8", FIX_4_3}});

        final FixMessageType.Group groupEtanol = new FixMessageType.Group();
        groupEtanol.setName("33");

        groupEtanol.addField(new FixMessageType.Field("58", "1"));
        fixEtanolMessage.addGroup(groupEtanol);

        comparator.setModel(fixEtanolMessage);

        final FixMessageType recivedMessage = FixUtils.getMessageType(fixMsg, getDataDictionary(PATH));
        comparator.compare2(recivedMessage);

        assertTrue("Error comparation messages, error " + messageErrors, messageErrors.isEmpty());
    }

    public void testCompareBodyGroupsFailed() throws Exception {
        final BodyComparator2 comparator = new BodyComparator2(getDataDictionary(PATH), messageErrors);

        final Message fixMsg = createMessageForTest(FIX_4_3);

        Group gr = createFixGroup(MES_TYPE, 33);
        fixMsg.addGroup(gr);

        final FixMessageType fixEtanolMessage = createEtanolMessage(FIX_4_3, new String[][]{{"58", "1"}});

        comparator.setModel(fixEtanolMessage);

        final FixMessageType recivedMessage = FixUtils.getMessageType(fixMsg, getDataDictionary(PATH));
        comparator.compare2(recivedMessage);

        assertFalse("Error comparation messages, error " + messageErrors, messageErrors.isEmpty());
    }

    public void testCompareBody2Groups() throws Exception {
        final BodyComparator2 comparator = new BodyComparator2(getDataDictionary(PATH), messageErrors);

        final Message fixMsg = createMessageForTest(FIX_4_3);
        fillFixGroups(33, fixMsg, new String[][]{{"58", "1"}});
        fillFixGroups(33, fixMsg, new String[][]{{"58", "122"}});

        final FixMessageType fixEtanolMessage = createEtanolMessage(FIX_4_3, new String[][]{{"58","122"}});
        comparator.setModel(fixEtanolMessage);

        final FixMessageType recivedMessage = FixUtils.getMessageType(fixMsg, getDataDictionary(PATH));
        comparator.compare2(recivedMessage);

        assertTrue("Error comparation messages, error " + messageErrors, messageErrors.isEmpty());
    }

    public void testCompareBody2GroupsFail() throws Exception {
        final BodyComparator2 comparator = new BodyComparator2(getDataDictionary(PATH), messageErrors);

        final Message fixMsg = createMessageForTest(FIX_4_3);

        fillFixGroups(33, fixMsg, new String[][]{{"58", "1"}});
        fillFixGroups(33, fixMsg, new String[][]{{"58", "122"}});

        final FixMessageType fixEtanolMessage = new FixMessageType();
        insertFields(fixEtanolMessage, new String [][] {{"164", "1"}, {"8", FIX_4_3}});

        final FixMessageType.Group groupEtanol = new FixMessageType.Group();
        groupEtanol.setName("33");

        groupEtanol.addField(new FixMessageType.Field("58", "1221"));
        fixEtanolMessage.addGroup(groupEtanol);

        comparator.setModel(fixEtanolMessage);

        final FixMessageType recivedMessage = FixUtils.getMessageType(fixMsg, getDataDictionary(PATH));
        comparator.compare2(recivedMessage);

        assertFalse("Error comparation messages, error " + messageErrors, messageErrors.isEmpty());
    }

    public void testCompareBody2GroupsStrictOrder() throws Exception {
        final BodyComparator2 comparator = new BodyComparator2(getDataDictionary(PATH), messageErrors);

        final Message fixMsg = createMessageForTest(FIX_4_3);

        fillFixGroups(33, fixMsg, new String[][]{{"58", "1"}});
        fillFixGroups(33, fixMsg, new String[][]{{"58", "122"}});

        final FixMessageType fixEtanolMessage = new FixMessageType();
        insertFields(fixEtanolMessage, new String [][] {{"164", "1"}, {"8", FIX_4_3}});

        final FixMessageType.Group groupEtanol = new FixMessageType.Group();
        groupEtanol.setName("33");

        groupEtanol.addField(new FixMessageType.Field("58", "1"));
        fixEtanolMessage.addGroup(groupEtanol);

        comparator.setModel(fixEtanolMessage);

        final FixMessageType recivedMessage = FixUtils.getMessageType(fixMsg, getDataDictionary(PATH));
        comparator.compare2(recivedMessage);
        comparator.compare2Order(recivedMessage);

        assertTrue("Error comparation messages, error " + messageErrors, messageErrors.isEmpty());
    }

    public void testCompareBody2GroupsStrictOrderFail() throws Exception {
        final BodyComparator2 comparator = new BodyComparator2(getDataDictionary(PATH), messageErrors);

        final Message fixMsg = createMessageForTest(FIX_4_3);

        fillFixGroups(33, fixMsg, new String[][]{});

        fillFixGroups(33, fixMsg, new String[][]{{"58", "1"}});
        fillFixGroups(33, fixMsg, new String[][]{{"58", "122"}});

        final FixMessageType fixEtanolMessage = createEtanolMessage(FIX_4_3, new String [][]{{"58", "122"}});

        comparator.setModel(fixEtanolMessage);

        final FixMessageType recivedMessage = FixUtils.getMessageType(fixMsg, getDataDictionary(PATH));
        comparator.compare2(recivedMessage);
        comparator.compare2Order(recivedMessage);

        assertFalse("Error comparation messages, error " + messageErrors, messageErrors.isEmpty());
    }
}