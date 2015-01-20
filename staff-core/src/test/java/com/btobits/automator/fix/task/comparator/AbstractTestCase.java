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

package com.btobits.automator.fix.task.comparator;

import junit.framework.TestCase;
import quickfix.DataDictionary;
import quickfix.ConfigError;
import quickfix.Message;
import quickfix.Group;
import com.btobits.automator.fix.utils.fix.FixMessageType;
import com.btobits.automator.fix.utils.FixUtils;

import java.util.List;
import java.util.ArrayList;

/**
 * @author: Volodymyr_Biloshkurskyi
 */
public abstract class AbstractTestCase extends TestCase {
    private static DataDictionary dataDictionary = null;
    public List<String> messageErrors = new ArrayList<String>();

    @Override
    protected void setUp() throws Exception {
        super.setUp();
        messageErrors.clear();
    }

    protected abstract DataDictionary getDataDictionary() throws Exception;
    protected abstract String getMesType() throws Exception;

    protected DataDictionary getDataDictionary(String path) throws ConfigError {
        if(dataDictionary == null) {
            dataDictionary = new DataDictionary(path);
        }

        return dataDictionary;
    }

    protected final Message createMessageForTest(String typy) {
        final Message fixMsg = new Message();
        fixMsg.getHeader().setString(8, typy);
        fixMsg.getHeader().setString(35, AbstractEmailTestCase.MES_TYPE);
        fixMsg.setString(164, "1");
        return fixMsg;
    }

    protected void insertFields(FixMessageType fixEtanolMessage, String [][] arr){
        for(int i=0; i<arr.length; i++){
            fixEtanolMessage.addField(new FixMessageType.Field(arr[i][0], arr[i][1]));
        }
    }

    protected final Group createFixGroup(String messType, int groupTag) throws Exception {
        final DataDictionary.GroupInfo groupInfo = getDataDictionary().getGroup(messType, groupTag);
        final Group group = new Group(groupTag, groupInfo.getDelimeterField());
        return group;
    }

    protected FixMessageType createEtanolMessage(String type, String [][] groupFields) {
        final FixMessageType fixEtanolMessage = new FixMessageType();
        insertFields(fixEtanolMessage, new String [][] {{"164", "1"}, {"8", type}});

        final FixMessageType.Group groupEtanol = new FixMessageType.Group();
        groupEtanol.setName("33");

        insertFields(groupEtanol, groupFields);
        fixEtanolMessage.addGroup(groupEtanol);
        return fixEtanolMessage;
    }

    public void insertFields(FixMessageType.Group fixEtanolMessage, String [][] arr){
        for(int i=0; i<arr.length; i++){
            fixEtanolMessage.addField(new FixMessageType.Field(arr[i][0], arr[i][1]));
        }
    }

    public void insertFields(Group fixGroup, String [][] arr){
        for(int i=0; i<arr.length; i++){
            fixGroup.setString(FixUtils.getFieldId(arr[i][0]), arr[i][1]);
        }
    }

    public final Group fillFixGroups(int groupTag, Message fixMsg, String [][] fields) throws Exception {
        Group group = createFixGroup(getMesType(), groupTag);
        insertFields(group, fields);
        fixMsg.addGroup(group);
        return group;
    }
}
