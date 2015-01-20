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

import com.btobits.automator.fix.quickfix.bridge.FixConnectivity;
import org.apache.tools.ant.Project;
import quickfix.*;
import quickfix.field.*;

public class FixExecutor extends FixAcceptor {
    private class MessageDispatcher extends FixConnectivity {
        public MessageDispatcher() {
        }

        @Override
        public void fromApp(Message msg, SessionID sessionID) throws FieldNotFound,
                IncorrectDataFormat, IncorrectTagValue, UnsupportedMessageType {
            cracker.crack(msg, sessionID);
        }
    }

    private final MessageDispatcher dispatcher = new MessageDispatcher();
    private final MessageCracker cracker = new MessageCracker() {

        private int m_orderID = 0;
        private int m_execID = 0;

        public OrderID genOrderID() {
            return new OrderID(new Integer(++m_orderID).toString());
        }

        public ExecID genExecID() {
            return new ExecID(new Integer(++m_execID).toString());
        }

        public void onMessage(quickfix.fix40.NewOrderSingle order,
                              SessionID sessionID) throws FieldNotFound, UnsupportedMessageType,
                IncorrectTagValue {
            Symbol symbol = new Symbol();
            Side side = new Side();
            OrdType ordType = new OrdType();
            OrderQty orderQty = new OrderQty();
            Price price = new Price();
            ClOrdID clOrdID = new ClOrdID();
            Account account = new Account();

            order.get(ordType);

            if (ordType.getValue() != OrdType.LIMIT)
                throw new IncorrectTagValue(ordType.getField());

            order.get(symbol);
            order.get(side);
            order.get(orderQty);
            order.get(price);
            order.get(clOrdID);

            quickfix.fix40.ExecutionReport executionReport = new quickfix.fix40.ExecutionReport(
                    genOrderID(), genExecID(), new ExecTransType(ExecTransType.NEW),
                    new OrdStatus(OrdStatus.FILLED), symbol, side, orderQty,
                    new LastShares(orderQty.getValue()), new LastPx(price.getValue()),
                    new CumQty(orderQty.getValue()), new AvgPx(price.getValue()));

            executionReport.set(clOrdID);

            if (executionReport.isSet(account))
                executionReport.setField(executionReport.get(account));

            try {
                Session.sendToTarget(executionReport, sessionID);
            } catch (SessionNotFound e) {
            }
        }

        public void onMessage(quickfix.fix41.NewOrderSingle order,
                              SessionID sessionID) throws FieldNotFound, UnsupportedMessageType,
                IncorrectTagValue {
            Symbol symbol = new Symbol();
            Side side = new Side();
            OrdType ordType = new OrdType();
            OrderQty orderQty = new OrderQty();
            Price price = new Price();
            ClOrdID clOrdID = new ClOrdID();
            Account account = new Account();

            order.get(ordType);

            if (ordType.getValue() != OrdType.LIMIT)
                throw new IncorrectTagValue(ordType.getField());

            order.get(symbol);
            order.get(side);
            order.get(orderQty);
            order.get(price);
            order.get(clOrdID);

            quickfix.fix41.ExecutionReport executionReport = new quickfix.fix41.ExecutionReport(
                    genOrderID(), genExecID(), new ExecTransType(ExecTransType.NEW),
                    new ExecType(ExecType.FILL), new OrdStatus(OrdStatus.FILLED), symbol,
                    side, orderQty, new LastShares(orderQty.getValue()), new LastPx(price
                            .getValue()), new LeavesQty(0), new CumQty(orderQty.getValue()),
                    new AvgPx(price.getValue()));

            executionReport.set(clOrdID);

            if (executionReport.isSet(account))
                executionReport.setField(executionReport.get(account));

            try {
                Session.sendToTarget(executionReport, sessionID);
            } catch (SessionNotFound e) {
            }
        }

        public void onMessage(quickfix.fix42.NewOrderSingle order,
                              SessionID sessionID) throws FieldNotFound, UnsupportedMessageType,
                IncorrectTagValue {
            Symbol symbol = new Symbol();
            Side side = new Side();
            OrdType ordType = new OrdType();
            OrderQty orderQty = new OrderQty();
            Price price = new Price();
            ClOrdID clOrdID = new ClOrdID();
            Account account = new Account();

            order.get(ordType);

            if (ordType.getValue() != OrdType.LIMIT)
                throw new IncorrectTagValue(ordType.getField());

            order.get(symbol);
            order.get(side);
            order.get(orderQty);
            order.get(price);
            order.get(clOrdID);

            quickfix.fix42.ExecutionReport executionReport = new quickfix.fix42.ExecutionReport(
                    genOrderID(), genExecID(), new ExecTransType(ExecTransType.NEW),
                    new ExecType(ExecType.FILL), new OrdStatus(OrdStatus.FILLED), symbol,
                    side, new LeavesQty(0), new CumQty(orderQty.getValue()), new AvgPx(
                            price.getValue()));

            executionReport.set(clOrdID);
            executionReport.set(orderQty);
            executionReport.set(new LastShares(orderQty.getValue()));
            executionReport.set(new LastPx(price.getValue()));

            if (executionReport.isSet(account))
                executionReport.setField(executionReport.get(account));

            try {
                Session.sendToTarget(executionReport, sessionID);
            } catch (SessionNotFound e) {
            }
        }

        public void onMessage(quickfix.fix43.NewOrderSingle order,
                              SessionID sessionID) throws FieldNotFound, UnsupportedMessageType,
                IncorrectTagValue {
            Symbol symbol = new Symbol();
            Side side = new Side();
            OrdType ordType = new OrdType();
            OrderQty orderQty = new OrderQty();
            Price price = new Price();
            ClOrdID clOrdID = new ClOrdID();
            Account account = new Account();

            order.get(ordType);

            if (ordType.getValue() != OrdType.LIMIT)
                throw new IncorrectTagValue(ordType.getField());

            order.get(symbol);
            order.get(side);
            order.get(orderQty);
            order.get(price);
            order.get(clOrdID);

            quickfix.fix43.ExecutionReport executionReport = new quickfix.fix43.ExecutionReport(
                    genOrderID(), genExecID(), new ExecType(ExecType.FILL),
                    new OrdStatus(OrdStatus.FILLED), side, new LeavesQty(0), new CumQty(
                            orderQty.getValue()), new AvgPx(price.getValue()));

            executionReport.set(clOrdID);
            executionReport.set(symbol);
            executionReport.set(orderQty);
            executionReport.set(new LastQty(orderQty.getValue()));
            executionReport.set(new LastPx(price.getValue()));

            if (executionReport.isSet(account))
                executionReport.setField(executionReport.get(account));

            try {
                Session.sendToTarget(executionReport, sessionID);
            } catch (SessionNotFound e) {
            }
        }

        public void onMessage(quickfix.fix44.NewOrderSingle order,
                              SessionID sessionID) throws FieldNotFound, UnsupportedMessageType,
                IncorrectTagValue {
            Symbol symbol = new Symbol();
            Side side = new Side();
            OrdType ordType = new OrdType();
            OrderQty orderQty = new OrderQty();
            Price price = new Price();
            ClOrdID clOrdID = new ClOrdID();
            Account account = new Account();

            order.get(ordType);

            if (ordType.getValue() != OrdType.LIMIT)
                throw new IncorrectTagValue(ordType.getField());

            order.get(symbol);
            order.get(side);
            order.get(orderQty);
            order.get(price);
            order.get(clOrdID);

            quickfix.fix44.ExecutionReport executionReport = new quickfix.fix44.ExecutionReport(
                    genOrderID(), genExecID(), new ExecType(ExecType.FILL),
                    new OrdStatus(OrdStatus.FILLED), side, new LeavesQty(0), new CumQty(
                            orderQty.getValue()), new AvgPx(price.getValue()));

            executionReport.set(clOrdID);
            executionReport.set(symbol);
            executionReport.set(orderQty);
            executionReport.set(new LastQty(orderQty.getValue()));
            executionReport.set(new LastPx(price.getValue()));

            if (executionReport.isSet(account))
                executionReport.setField(executionReport.get(account));

            try {
                Session.sendToTarget(executionReport, sessionID);
            } catch (SessionNotFound e) {
            }
        }
    };

    public FixExecutor() {
        super();
        setConnectivity(dispatcher);
    }

    public FixExecutor(Project project) {
        super(project);
        setConnectivity(dispatcher);
    }
}
