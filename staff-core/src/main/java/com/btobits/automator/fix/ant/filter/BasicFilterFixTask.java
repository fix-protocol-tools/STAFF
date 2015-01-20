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

import com.btobits.automator.ant.annotation.AutoParamBool;
import quickfix.FieldNotFound;
import quickfix.IntField;
import quickfix.Message;
import quickfix.field.MsgSeqNum;

import java.util.Arrays;
import java.util.List;

/**
 * @author Mykhailo_Sereda
 */
public abstract class BasicFilterFixTask extends BasicFilterTask {
    /**
     * Auto fill SeqNum by filter. Default value is true.
     * If this flag is 'false' than control of the SeqNum return to transport engine
     */
    @AutoParamBool(xmlAttr = "filterFillSeqNum")
    public Boolean filterFillSeqNum = true;

    protected List<Message> filterFixMsg(final Message inMessage, final int inDesiredSeqNum) {
        List<Message> resultMsg = Arrays.asList(inMessage);
        boolean msgModified = false;
        for(FixFilterRule rule: fixRules) {
            if (rule.active) {
                if (rule.filter(inMessage)) {
                    resultMsg = rule.actionFixMsg(resultMsg);
                    msgModified= true;
                }
            }
        }
        // TODO: move this flat to actions level
        boolean ignoreSeqNumFromMsg = false;
        if (filterFillSeqNum!=null && filterFillSeqNum && msgModified) {
            affixSeqNum(resultMsg, inDesiredSeqNum, ignoreSeqNumFromMsg);
        }

        if (isDebugEnabled() && msgModified) {
            writeToLogInfo("Message(s) after filter:\n" + toString(resultMsg));
        }

        return resultMsg;
    }

    protected void writeToLogInfo(String msg) {
        log(msg);
        if (log.isDebugEnabled()) {
            log.debug(msg);
        }
    }

    protected void writeToLogWarn(String msg) {
        // TODO: set warning log level to Ant logger
        log(msg);

        log.warn(msg);
    }

    protected String toString(List<Message> msgList) {
        if (msgList!=null && !msgList.isEmpty()) {
            StringBuilder strBuilder = new StringBuilder();
            int counter = 1;
            for (Message msg : msgList) {
                if (counter!=1) {
                    strBuilder.append("\n");
                }
                strBuilder.append("Message#").append(counter).append(":").append(msg);
                counter++;
            }
            return strBuilder.toString();
        } else {
            return "Message list is empty";
        }
    }

    private void affixSeqNum(final List<Message> listMsg, final int firstSeqNum, boolean ignoreSeqNumFromMsg) {
        int currentNum = firstSeqNum;
        for (Message msg : listMsg) {
            int msgSeqNum = extractSeqNum(msg);
            if (msgSeqNum == 0) {
                msg.getHeader().setInt(MsgSeqNum.FIELD, currentNum++);
            } else {
                if (currentNum==msgSeqNum &&  msgSeqNum!=0) {
                    // expected SeqNum already used
                    currentNum = msgSeqNum + 1;
                } else {
                    if (ignoreSeqNumFromMsg) {
                        log.warn("SeqNum is not null in message, but enabled mode 'ignoreSeqNumInMsg' " +
                                "For this message will be assigned the current seqNum. Message:" + msg);
                        msg.getHeader().setInt(MsgSeqNum.FIELD, currentNum++);
                    } else {
                        log.warn("In message present unexpected SeqNum. SeqNum from it message will be apply as the current SeqNum. " +
                                "Normal behavior is not guaranteed. Message: " + msg);
                        currentNum = msgSeqNum + 1;
                    }
                }
            }
        }
    }

    protected int extractSeqNum(final Message inMsg) {
        int result = 0;
        try {
            result = inMsg.getHeader().getField(new IntField(MsgSeqNum.FIELD)).getValue();
        } catch (final FieldNotFound inFieldNotFound) {
            // do nothing
        }
        return result;
    }

}
