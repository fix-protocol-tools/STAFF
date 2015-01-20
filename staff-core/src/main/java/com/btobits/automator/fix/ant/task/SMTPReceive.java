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

package com.btobits.automator.fix.ant.task;

import com.btobits.automator.fix.comparator.MailComparator;
import com.btobits.automator.fix.utils.fix.FixMessageType;
import com.btobits.automator.fix.utils.fix.SMTPAcceptor;
import com.ericdaugherty.mail.server.services.smtp.SMTPMessage;
import org.apache.tools.ant.BuildException;
import org.apache.tools.ant.Task;

import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.TimeUnit;

/**
 * @author Volodymyr_Biloshkurs
 */
public class SMTPReceive extends Task {
    private String refid;
    private Long timeout = 5L;
    private long repeat = 1L;
    private long count = 1L;
    private String sender;
    private String target;
    private String type;
    private boolean backCheckHeader = true;
    private boolean backCheckTrailer = true;
    private boolean backCheckBody = true;
    private boolean checkGroups = true;
    private boolean strictOrdering = false;
    private boolean validateIncoming = true;

    private final LinkedList<FixMessageType> messages = new LinkedList<FixMessageType>();

    public void addSmtpMessage(FixMessageType msg) {
        messages.addLast(msg);
    }

    public void setRefid(String value) {
        refid = value;
    }

    public void setSender(String value) {
        sender = value;
    }

    public void setTarget(String value) {
        target = value;
    }

    public void setType(String value) {
        type = value;
    }

    public void setTimeout(String value) {
        timeout = Long.parseLong(value);
    }

    public void setRepeat(String value) {
        repeat = Long.parseLong(value);
    }

    public void setCount(String value) {
        count = Long.parseLong(value);
    }

    public void setBackCheckHeader(String s) {
        backCheckHeader = Boolean.parseBoolean(s);
    }

    public void setBackCheckTrailer(String s) {
        backCheckTrailer = Boolean.parseBoolean(s);
    }

    public void setBackCheckBody(String s) {
        backCheckBody = Boolean.parseBoolean(s);
    }

    public void setCheckGroups(String s) {
        checkGroups = Boolean.parseBoolean(s);
    }

    public boolean isStrictOrdering() {
        return strictOrdering;
    }

    public void setStrictOrdering(boolean strictOrdering) {
        this.strictOrdering = strictOrdering;
    }

    public boolean isValidateIncoming() {
        return validateIncoming;
    }

    public void setValidateIncoming(String validateIncoming) {
        this.validateIncoming = Boolean.parseBoolean(validateIncoming);
    }

    public void execute() throws BuildException {
        try {
            for (long i = 0; i < repeat; ++i) {
                if (messages.size() > 0) {
                    for (FixMessageType msg : messages) {
                        for (long c = 0; c < count; ++c) {
                            SMTPMessage smtpMessage = SMTPAcceptor.messages.poll(timeout, TimeUnit.SECONDS);

                            if (smtpMessage == null) {
                                throw new Exception(
                                        "Can not received message from SMTP server - timeout expired ("
                                                + timeout + " seconds)");
                            }

                            List<Object> lines = smtpMessage.getDataLines();
                            System.out.println("-> We have smtp, message:[" + smtpMessage.toString() + "].");
                            // compare message
                            MailComparator.compare(msg, smtpMessage);
                        }
                    }
                } else {
                    for (long c = 0; c < count; ++c) {
                        SMTPMessage smtpMessage = SMTPAcceptor.messages.poll(timeout, TimeUnit.SECONDS);

                        if (smtpMessage == null) {
                            throw new Exception(
                                    "Can not received message from SMTP server - timeout expired ("
                                            + timeout + " seconds)");
                        }

                        System.out.println("-> We have smtp, message:[" + smtpMessage.toString() + "].");
                    }
                }
            }
        } catch (Exception ex) {
            throw new BuildException("Error receiving message from the SMTP session: " + ex);
        }
    }
}
