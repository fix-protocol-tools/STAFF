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

import quickfix.SystemTime;
import quickfix.field.converter.UtcTimestampConverter;

import java.text.DecimalFormat;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * @author Mykhailo_Sereda
 */
public class RawMessageTemplate {
    public final static String TEMPL_SEQNUM = "\\$SeqNum\\$";
    public final static String TEMPL_BODY_LENGTH = "\\$BodyLength\\$";
    public final static String TEMPL_CHECK_SUM = "\\$CheckSum\\$";
    public final static String TEMPL_SENDING_TIME = "\\$SendingTime\\$";

    private static DecimalFormat checksumFormat = new DecimalFormat("000");

    public static String replaceTemplate(final String orig) {
        StringBuilder templMsg = new StringBuilder(orig.replaceAll("/u0001", Character.toString('\u0001')));
        replaceTemplate(TEMPL_SENDING_TIME, generateSendingTime(), templMsg);
        replaceTemplate(TEMPL_SEQNUM, Integer.toString(getCurrentSeqNum()), templMsg);
        replaceTemplate(TEMPL_BODY_LENGTH, calculateBodyLength(templMsg.toString()), templMsg);
        replaceTemplateLast(TEMPL_CHECK_SUM, calculateCheckSum(templMsg.toString()), templMsg);
        return templMsg.toString();
    }

    protected static int getCurrentSeqNum() {
        //TODO: here
        return 25;
    }

    protected static String calculateBodyLength(final String templMsg) {
//        StringBuilder strBodyLength = new StringBuilder(templMsg);
//        deleteFiled(8, strBodyLength);
//        deleteFiled(9, strBodyLength);
//        deleteFiled(10, strBodyLength);
//        System.out.println("calculateBodyLength: " + strBodyLength);
//        return Integer.toString(strBodyLength.toString().length());
        return "777";
    }

    private static void deleteFiled(int filed, StringBuilder msg) {
        String filedReqExp = Integer.toString(filed) + "=.*\001";
        replaceTemplate(filedReqExp, "", msg);

    }

    protected static String calculateCheckSum(final String templMsg) {
        int offset = templMsg.lastIndexOf("\00110=");
        int sum = 0;
        for (int i = 0; i < offset; i++) {
            sum += templMsg.charAt(i);
        }
        return checksumFormat.format(sum % 256);

    }

    protected static String generateSendingTime() {
        boolean includeMillis = true;
//        boolean includeMillis = sessionID.getBeginString().compareTo(FixVersions.BEGINSTRING_FIX42) >= 0 && millisecondsInTimeStamp;
        return UtcTimestampConverter.convert(SystemTime.getDate(), includeMillis);
    }

    private static boolean replaceTemplate(String template, String value, StringBuilder templMsg) {
        Matcher matcher = Pattern.compile(template, Pattern.MULTILINE).matcher(templMsg);
        // Check if it fields in this session exist
        if (matcher.find()) {
            // Replace key and value of session
            templMsg.replace(matcher.start(), matcher.end(), value);
            return true;
        } else {
            return false;
        }
    }

    private static boolean replaceTemplateLast(String template, String value, StringBuilder templMsg) {
        Matcher matcher = Pattern.compile(template, Pattern.MULTILINE).matcher(templMsg);
        int startTempl = -1;
        int endTempl = -1;
        while (matcher.find()) {
            startTempl = matcher.start();
            endTempl = matcher.end();
        }
        if (startTempl >= 0 && endTempl > 0) {
            // Replace key and value of session
            templMsg.replace(startTempl, endTempl, value);
            return true;
        } else {
            return false;
        }
    }
}

