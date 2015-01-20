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

package com.btobits.automator.fix.task.comparator40.mes_c.mes_j;

import com.btobits.automator.fix.comparator.BodyComparator2;
import com.btobits.automator.fix.task.comparator.AbstractAllocInstructTestCase;
import com.btobits.automator.fix.utils.FixUtils;
import com.btobits.automator.fix.utils.fix.FixMessageType;
import quickfix.DataDictionary;
import quickfix.Group;
import quickfix.Message;

/**
 * @author: Volodymyr_Biloshkurskyi
 */

public class TestCompareBody extends AbstractAllocInstructTestCase {

    private static final String PATH = "dictionary/FIX40.xml";
    private static final String FIX_4_0 = "FIX.4.0";

    protected DataDictionary getDataDictionary() throws Exception {
        return getDataDictionary(PATH);
    }

    private final Message createMessageForTest() {
        final Message fixMsg = new Message();
        fixMsg.getHeader().setString(8, FIX_4_0);
        fixMsg.getHeader().setString(35, MES_TYPE);
        fixMsg.setString(164, "1");
        fixMsg.setString(70, "1");
        return fixMsg;
    }

    private FixMessageType createEtanolMessage(String [][] groupFields) {
        final FixMessageType fixEtanolMessage = new FixMessageType();
        insertFields(fixEtanolMessage, new String [][] {{"70", "1"}, {"8", FIX_4_0}});

        final FixMessageType.Group groupEtanol = new FixMessageType.Group();
        groupEtanol.setName("73");
        
        insertFields(groupEtanol, groupFields);
        fixEtanolMessage.addGroup(groupEtanol);
        
        return fixEtanolMessage;
    }

    public void testCompareBody() throws Exception {
        final BodyComparator2 comparator = new BodyComparator2(getDataDictionary(PATH), messageErrors);

        Message fixMsg = createMessageForTest();

        final FixMessageType fixEtanolMessage = new FixMessageType();
        insertFields(fixEtanolMessage, new String [][] {{"71", "11"}, {"8", FIX_4_0}});

        comparator.setModel(fixEtanolMessage);

        final FixMessageType recivedMessage = FixUtils.getMessageType(fixMsg, getDataDictionary(PATH));
        comparator.compare2(recivedMessage);

        assertFalse("Error comparation messages, error " + messageErrors, messageErrors.isEmpty());
    }

    public void testCompareBodyFailed() throws Exception {
        final BodyComparator2 comparator = new BodyComparator2(getDataDictionary(PATH), messageErrors);

        Message fixMsg = createMessageForTest();

        final FixMessageType fixEtanolMessage = new FixMessageType();
        insertFields(fixEtanolMessage, new String [][] {{"71", "10"}, {"8", FIX_4_0}});

        comparator.setModel(fixEtanolMessage);

        final FixMessageType recivedMessage = FixUtils.getMessageType(fixMsg, getDataDictionary(PATH));
        comparator.compare2(recivedMessage);

        assertFalse("Error comparation messages, error " + messageErrors, messageErrors.isEmpty());
    }

    public void testCompareBodyGroups() throws Exception {
        final BodyComparator2 comparator = new BodyComparator2(getDataDictionary(PATH), messageErrors);

        final Message fixMsg = createMessageForTest();

        fillFixGroups(73, fixMsg, new String[][]{{"11", "1"}});

        final FixMessageType fixEtanolMessage = new FixMessageType();
        insertFields(fixEtanolMessage, new String [][] {{"164", "1"}, {"8", FIX_4_0}});

        final FixMessageType.Group groupEtanol = new FixMessageType.Group();
        groupEtanol.setName("73");

        groupEtanol.addField(new FixMessageType.Field("11", "1"));
        fixEtanolMessage.addGroup(groupEtanol);

        comparator.setModel(fixEtanolMessage);

        final FixMessageType recivedMessage = FixUtils.getMessageType(fixMsg, getDataDictionary(PATH));
        comparator.compare2(recivedMessage);

        assertTrue("Error comparation messages, error " + messageErrors, messageErrors.isEmpty());
    }

    public void testCompareBodyGroupsFailed() throws Exception {
        final BodyComparator2 comparator = new BodyComparator2(getDataDictionary(PATH), messageErrors);

        final Message fixMsg = createMessageForTest();

        Group gr = createFixGroup(MES_TYPE, 73);
        fixMsg.addGroup(gr);

        final FixMessageType fixEtanolMessage = createEtanolMessage(new String[][]{{"73", "1"}});

        comparator.setModel(fixEtanolMessage);

        final FixMessageType recivedMessage = FixUtils.getMessageType(fixMsg, getDataDictionary(PATH));
        comparator.compare2(recivedMessage);

        assertFalse("Error comparation messages, error " + messageErrors, messageErrors.isEmpty());
    }

    public void testCompareBody2Groups() throws Exception {
        final BodyComparator2 comparator = new BodyComparator2(getDataDictionary(PATH), messageErrors);

        final Message fixMsg = createMessageForTest();
        fillFixGroups(73, fixMsg, new String[][]{{"11", "1"}});
        fillFixGroups(73, fixMsg, new String[][]{{"11", "122"}});

        final FixMessageType fixEtanolMessage = createEtanolMessage(new String[][]{{"11","122"}});
        comparator.setModel(fixEtanolMessage);

        final FixMessageType recivedMessage = FixUtils.getMessageType(fixMsg, getDataDictionary(PATH));
        comparator.compare2(recivedMessage);

        assertTrue("Error comparation messages, error " + messageErrors, messageErrors.isEmpty());
    }

    public void testCompareBody2GroupsFail() throws Exception {
        final BodyComparator2 comparator = new BodyComparator2(getDataDictionary(PATH), messageErrors);

        final Message fixMsg = createMessageForTest();

        fillFixGroups(73, fixMsg, new String[][]{{"58", "1"}});
        fillFixGroups(73, fixMsg, new String[][]{{"58", "122"}});

        final FixMessageType fixEtanolMessage = new FixMessageType();
        insertFields(fixEtanolMessage, new String [][] {{"164", "1"}, {"8", FIX_4_0}});

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

        final Message fixMsg = createMessageForTest();

        fillFixGroups(73, fixMsg, new String[][]{{"11", "1"}});
        fillFixGroups(73, fixMsg, new String[][]{{"11", "122"}});

        final FixMessageType fixEtanolMessage = new FixMessageType();
        insertFields(fixEtanolMessage, new String [][] {{"164", "1"}, {"8", FIX_4_0}});

        final FixMessageType.Group groupEtanol = new FixMessageType.Group();
        groupEtanol.setName("73");

        groupEtanol.addField(new FixMessageType.Field("11", "1"));
        fixEtanolMessage.addGroup(groupEtanol);

        comparator.setModel(fixEtanolMessage);

        final FixMessageType recivedMessage = FixUtils.getMessageType(fixMsg, getDataDictionary(PATH));
        comparator.compare2(recivedMessage);
        comparator.compare2Order(recivedMessage);

        assertTrue("Error comparation messages, error " + messageErrors, messageErrors.isEmpty());
    }

    public void testCompareBody2GroupsStrictOrderFail() throws Exception {
        final BodyComparator2 comparator = new BodyComparator2(getDataDictionary(PATH), messageErrors);

        final Message fixMsg = createMessageForTest();

        fillFixGroups(73, fixMsg, new String[][]{});

        fillFixGroups(73, fixMsg, new String[][]{{"58", "1"}});
        fillFixGroups(73,fixMsg, new String[][]{{"58", "122"}});

        final FixMessageType fixEtanolMessage = createEtanolMessage(new String [][]{{"58", "122"}});

        comparator.setModel(fixEtanolMessage);

        final FixMessageType recivedMessage = FixUtils.getMessageType(fixMsg, getDataDictionary(PATH));
        comparator.compare2(recivedMessage);
        comparator.compare2Order(recivedMessage);

        assertFalse("Error comparation messages, error " + messageErrors, messageErrors.isEmpty());
    }
}