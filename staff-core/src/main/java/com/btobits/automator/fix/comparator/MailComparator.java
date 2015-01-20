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

package com.btobits.automator.fix.comparator;

import com.btobits.automator.fix.utils.FixUtils;
import com.btobits.automator.fix.utils.MessageDifferenceException;
import com.btobits.automator.fix.utils.fix.FixMessageType;
import com.ericdaugherty.mail.server.services.smtp.SMTPMessage;
import org.apache.commons.lang.StringUtils;
import org.apache.tools.ant.BuildException;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

/**
 * @author Volodymyr_Biloshkurs
 */
public class MailComparator {
    protected static final String FIX_MESSAGE_FLD = "fixMessage";

    protected MailComparator() {
    }

    public static void compare(final FixMessageType inMsg,
                               final SMTPMessage inSmtpMessage) throws Exception {

        final List<String> errors = new ArrayList<String>();
        try {
            // compare header field             
            for (final FixMessageType.Field field : inMsg.getFields()) {
                if (!field.isGroup()) {
                    final String value = getHeaderField(field.getName(), inSmtpMessage);
                    if (StringUtils.isBlank(value) &&
                            StringUtils.isBlank(field.getValue())) {
                        continue;
                    } else if (StringUtils.isBlank(value)) {
                        errors.add("Field [" + field.getName() + "] is absend in receive email.");
                    } else if (!StringUtils.equals(field.getValue(), value) &&
                            StringUtils.startsWith(value, "<") &&
                            StringUtils.endsWith(value, ">")) {

                        final String subValue = StringUtils.substringBetween(value, "<", ">");
                        if (StringUtils.isBlank(subValue)) {
                            errors.add("Field [" + field.getName() +
                                    "] is not eq receive email field ['" + field.getValue() + " != " + value + "'].");
                        } else if (!StringUtils.equalsIgnoreCase(subValue, field.getValue())) {
                            errors.add("Field [" + field.getName() +
                                    "] is not eq receive email field ['" + field.getValue() + " != " + value + "'].");
                        }
                    } else if (!StringUtils.equals(field.getValue(), value)) {
                        errors.add("Field [" + field.getName() +
                                "] is not eq receive email field ['" + field.getValue() + " != " + value + "'].");
                    }
                } else {
                    // compare group
                    final List<String> body = getDataFields(inSmtpMessage);
                    final LinkedList<FixMessageType.Field> fields = field.getFields();
                    int count = 0;
                    for (FixMessageType.Field grFld : fields) {
                        final String value = getValueOnPosition(body, count);
                        if (StringUtils.equals(grFld.getName(), FIX_MESSAGE_FLD)) {
                            String fixMessage = getFixField("Original FIX message:", inSmtpMessage);
                            if (!StringUtils.equals(fixMessage, grFld.getValue())) {
                                errors.add("Data fix field [" + grFld.getName() +
                                        "] is not eq receive email field ['" + grFld.getValue() + " != " + fixMessage + "'].");
                            }
                        } else {
                            if (StringUtils.isBlank(value) &&
                                    StringUtils.isBlank(grFld.getValue())) {
                            } else {
                                if (!StringUtils.equals(value, grFld.getValue())) {
                                    errors.add("Data field [" + grFld.getName() +
                                            "] is not eq receive email field ['" + grFld.getValue() + " != " + value + "'].");
                                }
                            }
                        }
                        count++;
                    }
                }
            }
        } catch (Exception ex) {
            throw new BuildException("Error compare message", ex);
        }

        if (!errors.isEmpty()) {
            throw new MessageDifferenceException(FixUtils.toString(errors));
        }
    }

    public static final String getHeaderField(final String inName, final SMTPMessage inSmtpMessage) {
        final List<Object> lines = inSmtpMessage.getDataLines();
        for (final Object obj : lines) {
            final String val = (String) obj;
            final String header = inName + ": ";
            if (!StringUtils.isBlank(val)) {
                if (StringUtils.startsWith(val, header)) {
                    return StringUtils.substringAfter(val, header);
                }
            } else {
                break; // data field
            }
        }
        return null;
    }

    public static final String getFixField(final String inName, final SMTPMessage inSmtpMessage) {
        final List<Object> lines = inSmtpMessage.getDataLines();
        int index = 0;
        final String header = inName;
        for (final Object obj : lines) {
            final String val = (String) obj;
            if (!StringUtils.isBlank(val)) {
                if (StringUtils.startsWith(val, header)) {
                    break;
                }
            }
            index++;
        }

        if (index == lines.size()) {
            return null;
        }

        final StringBuilder sb = new StringBuilder();
        for (int i = index; i < lines.size(); i++) {
            final String line = lines.get(i) + "";
            if (i == index) {
                sb.append(StringUtils.substringAfter(line, header));
            } else {
                if (StringUtils.isBlank(line)) {
                    break; // end email message
                }
                sb.append(line.trim());
            }
        }

        return sb.toString().replaceAll("/u0001", "\u0001");
    }

    public static List<String> getDataFields(final SMTPMessage inSmtpMessage) {
        int index = 0;
        for (int i = 0; i < inSmtpMessage.getDataLines().size(); i++) {
            String value = inSmtpMessage.getDataLines().get(i) + "";
            if (StringUtils.isBlank(value)) {
                index = i + 1;
                break;
            }
        }

        return inSmtpMessage.getDataLines().subList(index, inSmtpMessage.getDataLines().size() - 1);
    }

    public static final String getValueOnPosition(final List<String> inValues, final int inPosition) {
        if (inPosition >= 0 && inPosition <= inValues.size() - 1) {
            return inValues.get(inPosition);
        } else {
            return null;
        }
    }
}
