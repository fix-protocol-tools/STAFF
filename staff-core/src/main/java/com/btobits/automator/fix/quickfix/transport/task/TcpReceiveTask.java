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

package com.btobits.automator.fix.quickfix.transport.task;

import com.btobits.automator.fix.quickfix.transport.ITcpProccesor;
import com.btobits.automator.fix.quickfix.transport.Message;
import org.apache.log4j.Logger;
import org.apache.tools.ant.BuildException;
import org.apache.tools.ant.Task;

import java.util.regex.Matcher;
import java.util.regex.Pattern;


public class TcpReceiveTask extends Task {
    private Logger logger = Logger.getLogger(TcpReceiveTask.class);
    private String refid;
    private String pattern;
    private ITcpProccesor processor;
    private Integer timeout = 0;

    public void setTimeout(final String inTimeout) {
        timeout = Integer.parseInt(inTimeout);
    }

    public TcpReceiveTask() {
    }

    public void setRefid(final String refid) {
        this.refid = refid;
    }

    public void setPattern(final String inPattern) {
        this.pattern = inPattern.replaceAll("/u0001", "\u0001");
    }

    @Override
    public void execute() throws BuildException {
        try {
            if (refid == null) {
                throw new Exception("Reference to Tcp client is not specified");
            }

            final Object obj = getProject().getReference(refid);
            if (obj == null) {
                throw new Exception("Failed to get transport client by reference");
            }

            if (obj instanceof ITcpProccesor) {
                processor = (ITcpProccesor) obj;

                final Message message;
                if(timeout != 0) {
                    message = processor.nextMessage(timeout);
                } else {
                    message = processor.nextMessage();
                }

                if(message == null) {
                    throw new BuildException("Time out.");
                }

                if(logger.isInfoEnabled()) {
                    logger.info(message);
                }

                System.out.println("Message received: [" + message + "]");

                compareData((String)message.getMessage());
            } else {
                throw new Exception("Tcp client type is unknown.");
            }
        } catch (Exception e) {
            throw new BuildException("Error receive data: " + e);
        }
    }

    /***
     * compare data
     */
    private void compareData(String line) throws Exception {
        processor.log("->" + line, true);
        if (pattern == null) {
            return;
        }

        final Pattern  p = Pattern.compile(pattern);
        final Matcher m = p.matcher(line);
        if (!m.matches()) {
            throw new BuildException("Error compare ["
                    + line + "] != [" + pattern + "]");
        } else {
            System.out.println("Incomming message equals pattern [" + pattern + " == " + line + "]");
            processor.log("Incomming message equals pattern [" + pattern + " == " + line + "]", true);
        }
    }
}
