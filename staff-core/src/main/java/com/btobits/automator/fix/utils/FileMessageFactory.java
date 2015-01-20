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

package com.btobits.automator.fix.utils;

import com.btobits.automator.fix.quickfix.transport.ITcpMessage;
import com.btobits.automator.fix.quickfix.transport.ITcpProccesor;
import org.apache.commons.lang.StringUtils;
import org.apache.tools.ant.BuildException;
import quickfix.MessageUtils;

import java.io.File;
import java.io.FileInputStream;
import java.net.SocketException;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

/** 
 * @author Volodymyr_Biloshkurs
 */
public class FileMessageFactory {
    private File file;
    private List<String> messages = new ArrayList<String>();
    private String[] skip;
    private String delimeter;


    public List<String> getMessages() {
        return messages;
    }

    public void setDelimeter(String inDelimeter) {
        this.delimeter = inDelimeter;
    }

    public FileMessageFactory(final String inFile, final String inSkip) throws Exception {
        try {
            skip = StringUtils.split(inSkip, "|");
            file = new File(inFile);
        } catch (Exception ex) {
            throw new Exception("Error open message file:", ex);
        }
    }

    public void load() throws Exception {
        try {
            Scanner scanner = new Scanner(new FileInputStream(file));

            while (scanner.hasNextLine()) {
                final String line = scanner.nextLine();

                if (StringUtils.isBlank(line)) {
                    continue;
                }

                int i = line.indexOf("8=FIX");
                if (i != -1) {
                    String s = line.substring(i);
                    final String type = MessageUtils.getStringField(s, FixUtils.MSGTYPE);
                    if (!isSkip(type)) {
                        messages.add(s);
                    }
                }
            }
        } catch (Exception ex) {
            throw new Exception("Error load from file", ex);
        }
    }

    public void loadInternal(ITcpProccesor inProcessor, int repeatDelay, String nameClient, int inTimeOut) throws Exception {
        try {
            Scanner scanner = new Scanner(new FileInputStream(file));
            //scanner.useDelimiter(delimeter);

            while (scanner.hasNextLine()) {
                final String line = scanner.nextLine();
                inProcessor.sendMessage(new ITcpMessage() {
                    public byte[] toRawMessage() {
                        return line.getBytes();
                    }

                    public String getMessageAsString() {
                        return line;
                    }
                }, inTimeOut);

                inProcessor.log(line, false);
                if (repeatDelay > 0) {
                    try {
                        Thread.sleep(repeatDelay);
                    } catch (Exception e) {
                    }
                }
            }
        } catch (SocketException ex) {
            throw new BuildException(ex.getMessage());
        } catch (Exception ex) {
            throw new Exception("Error send data from file, error: " + ex.getMessage(), ex);
        }
    }

    private boolean isSkip(final String inType) {
        if (inType == null) {
            return true;
        }
        if (skip == null) {
            return false;
        } else {
            for (String s : skip) {
                if (StringUtils.equals(inType, s)) {
                    return true;
                }
            }
        }
        return false;
    }
}
