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

package com.btobits.automator.fix.ant.filter;

import com.btobits.automator.ant.annotation.AutoParamBean;
import com.btobits.automator.ant.annotation.AutoParamEnum;
import com.btobits.automator.fix.ant.task.BasicAntTask;
import com.btobits.automator.fix.utils.fix.FixMessageType;
import com.btobits.automator.fix.utils.fix.MessageConverter;
import org.apache.commons.lang.StringUtils;
import org.junit.Assert;
import quickfix.Message;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

/**
 * @author Mykhailo_Sereda
 */
@AutoParamBean
public class FixFilterAction extends BasicAntTask {

    /**
     * Filter instruction operation: insertBefore | insertAfter | replace
     */
    @AutoParamEnum(xmlAttr = "operation", enumClass = FilterActionOperation.class)
    public FilterActionOperation operation;

    private final List<FixMessageType> listMessages = new LinkedList<FixMessageType>();
    private List<Message> listFixMsgAction;
    private List<String> listStrMsgAction;

    public void addFixMessage(final FixMessageType msg) {
        listMessages.add(msg);
    }

    @Override
    protected void validate() throws Exception {
        Assert.assertNotNull("FixFilterAction attribute 'operation' is required parameter. " +
                "Valid value list: insertBefore||insertAfter||replace.", operation);
    }

    @Override
    protected void runTestInstructions() throws Exception {
        // do nothing
    }

    public void initAction(MessageConverter converter) throws Exception {
        listFixMsgAction = convertToTransportMsgList(listMessages, converter);
        listStrMsgAction = convertToString(listMessages, converter);
    }

    public List<Message> doFixMsgAction(List<Message> origMsg){
        switch (operation) {
            case INSERTBEFORE:
                List<Message> resultInsertBefore = new LinkedList<Message>(getCopyListFixMsgAction());
                resultInsertBefore.addAll(origMsg);
                return resultInsertBefore;
            case INSERTAFTER:
                List<Message> resultInsertAfter = new LinkedList<Message>(origMsg);
                resultInsertAfter.addAll(getCopyListFixMsgAction());
                return resultInsertAfter;
            case REPLACE:
                return getCopyListFixMsgAction();
            default:
                throw new IllegalArgumentException("Unsupported filter operation. Operation: " + operation);
        }
    }

    public List<String> doStrMsgAction(List<String> origMsgStr) {
        switch (operation) {
            case INSERTBEFORE:
                List<String> resultInsertBefore = new LinkedList<String>(listStrMsgAction);
                resultInsertBefore.addAll(origMsgStr);
                return resultInsertBefore;
            case INSERTAFTER:
                List<String> resultInsertAfter = new LinkedList<String>(origMsgStr);
                resultInsertAfter.addAll(listStrMsgAction);
                return resultInsertAfter;
            case REPLACE:
                return listStrMsgAction;
            default:
                throw new IllegalArgumentException("Unsupported filter operation. Operation: " + operation);
        }
    }

    private List<Message> getCopyListFixMsgAction() {
        List<Message> copyList = new ArrayList<Message>();
        for(Message msg :listFixMsgAction) {
            copyList.add((Message)msg.clone());
        }
        return copyList;
    }

    protected List<String> convertToString(List<FixMessageType> listOurMessages, MessageConverter converter) throws Exception {
        List<String> result = new ArrayList<String>();
        for (FixMessageType ourMsg : listOurMessages) {
            result.add(convertToString(ourMsg, converter));
        }
        return result;
    }

    protected List<Message> convertToTransportMsgList(List<FixMessageType> listOurMessages, MessageConverter converter) throws Exception {
        List<Message> result = new ArrayList<Message>();
        for (FixMessageType ourMsg : listOurMessages) {
            result.add(convertToTransportMsg(ourMsg, converter));
        }
        return result;
    }

    protected Message convertToTransportMsg(FixMessageType ourMsg, MessageConverter converter) throws Exception {
        try {
            if (StringUtils.isBlank(ourMsg.getData())) {
                return converter.convertToMessage(ourMsg);
            } else {
                return converter.parseMessage(ourMsg.getData().replace("/u0001", "\u0001"));
            }
        } catch (Exception e) {
            log.warn("Can't convert message from filter action to transport messages. Message: " + ourMsg +
                    ". Cause: " + e.getMessage());
            throw e;
        }
    }

    protected String convertToString(FixMessageType ourMsg, MessageConverter converter) throws Exception {
        try {
            if (StringUtils.isBlank(ourMsg.getData())) {
                return converter.convertToMessage(ourMsg).toString();
            } else {
                return ourMsg.getData().replace("/u0001", "\u0001");
            }
        } catch (Exception e) {
            log.warn("Can't convert message from filter action. Message: " + ourMsg +
                    ". Cause: " + e.getMessage());
            throw e;
        }
    }

}
