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

import com.btobits.automator.ant.annotation.*;
import com.btobits.automator.fix.exception.GenericBusinessLogicException;
import com.btobits.automator.fix.utils.MessageDifferenceException;
import com.btobits.automator.misc.StackTraceUtil;
import org.apache.log4j.Logger;
import org.apache.tools.ant.BuildException;
import org.apache.tools.ant.Task;

import java.lang.reflect.Field;

/**
 * @author Kirill_Mukhoiarov
 */

public abstract class BasicAntTask extends Task {
    public final static String svnSignature = "$$Rev: 67658 $$ $$Date: 2014-10-30 18:03:44 +0200 (Чт, 30 окт 2014) $$ $$LastChangedBy: Alexander_Sereda $$";
    protected final Logger log = Logger.getLogger(getClass());

    protected abstract void validate() throws Exception;

    protected abstract void runTestInstructions() throws Exception;

//    protected String extractFixVersion(final String inLocalFixVersion) {
//        String result;
//        final Project project = getProject();
//        final String initFixVersion = project.getProperty(FixInitiator.INITIATOR_FIX_VERSION_PARAMETER_NAME);
//        final String acceptFixVersion = project.getProperty(FixAcceptor.ACCEPTOR_FIX_VERSION_PARAMETER_NAME);
//
//        if (StringUtils.equalsIgnoreCase(initFixVersion, acceptFixVersion)) { // init & accept fix version ok
//            result = initFixVersion;
//        } else {
//            throw new BuildException("fixInitiatorTask fix version: " + initFixVersion
//                    + ", but fixAcceptorTask fix version: " + acceptFixVersion);
//        }
//
//        if (StringUtils.isNotBlank(inLocalFixVersion) && !StringUtils.equalsIgnoreCase(inLocalFixVersion, result)) {
//            // but in xml task definition configured wrong fix version
//            throw new BuildException("fixInitiatorTask and fixAcceptorTask fix version: " + initFixVersion
//                    + ", but this task configured fix version: " + inLocalFixVersion);
//        }
//        return result;
//    }
//
//    protected String extractSenderCompID(final boolean isAcceptor, final String inLocalSenderCompID) {
//        String result;
//        final Project project = getProject();
//        final String initSenderCompID = project.getProperty(FixInitiator.INITIATOR_SENDER_COMP_ID_PARAMETER_NAME);
//
//        final String acceptSenderCompID = project.getProperty(FixAcceptor.ACCEPTOR_SENDER_COMP_ID_PARAMETER_NAME);
//        final String acceptTargetCompID = project.getProperty(FixAcceptor.ACCEPTOR_TARGET_COMP_ID_PARAMETER_NAME);
//
//        if (StringUtils.equalsIgnoreCase(initSenderCompID, acceptTargetCompID)) { // init & accept comp id is ok
//            result = (isAcceptor ? acceptSenderCompID : initSenderCompID);
//        } else {
//            throw new BuildException("fixInitiatorTask senderCompId: " + initSenderCompID
//                    + ", but fixAcceptorTask targetCompId: " + acceptTargetCompID);
//        }
//
//        if (StringUtils.isNotBlank(inLocalSenderCompID) && !StringUtils.equalsIgnoreCase(inLocalSenderCompID, result)) {
//            // but in xml task definition configured wrong fix version
//            throw new BuildException((isAcceptor ? "fixAcceptorTask" : "fixInitiatorTask") + " senderCompId: " + result
//                    + ", but this task configured with senderCompId: " + inLocalSenderCompID);
//        }
//        return result;
//    }
//
//    protected String extractTargetCompID(final boolean isAcceptor, final String inLocalTargetCompID) {
//        String result;
//        final Project project = getProject();
//        final String initTargetCompID = project.getProperty(FixInitiator.INITIATOR_TARGET_COMP_ID_PARAMETER_NAME);
//
//        final String acceptSenderCompID = project.getProperty(FixAcceptor.ACCEPTOR_SENDER_COMP_ID_PARAMETER_NAME);
//        final String acceptTargetCompID = project.getProperty(FixAcceptor.ACCEPTOR_TARGET_COMP_ID_PARAMETER_NAME);
//
//        if (StringUtils.equalsIgnoreCase(initTargetCompID, acceptSenderCompID)) { // init & accept comp id is ok
//            result = (isAcceptor ? acceptTargetCompID : initTargetCompID);
//        } else {
//            throw new BuildException("fixInitiatorTask targetCompId: " + initTargetCompID
//                    + ", but fixAcceptorTask senderCompId: " + acceptSenderCompID);
//        }
//
//        if (StringUtils.isNotBlank(inLocalTargetCompID) && !StringUtils.equalsIgnoreCase(inLocalTargetCompID, result)) {
//            // but in xml task definition configured wrong fix version
//            throw new BuildException((isAcceptor ? "fixAcceptorTask" : "fixInitiatorTask") + " targetCompId: " + result
//                    + ", but this task configured with targetCompId: " + inLocalTargetCompID);
//        }
//        return result;
//    }
//
//    protected static boolean isAcceptor(final Object inObject) {
//        boolean verdict = false;
//        if (inObject != null) {
//            verdict = inObject instanceof FixAcceptor;
//        }
//        return verdict;
//    }

    @Override
    public void execute() throws BuildException {
        if (log.isTraceEnabled()) {
            log.trace("start task " + getTaskName() + "{" + getAutoparams() + "}");
        }

        try {
            validate();

            runTestInstructions();
        } catch (final AssertionError ae) {
            throw new BuildException("\n! Task " + getTaskName() + " flow error: " + ae.getMessage(), ae);
        } catch (final GenericBusinessLogicException gble) {
            throw new BuildException("\n! Task " + getTaskName() + " business logic error: "
                    + gble.getClass().getSimpleName() + " - " + gble.getMessage());
        } catch (final MessageDifferenceException mde) {
            throw new MessageDifferenceException("\n! Task " + getTaskName() + " message difference exception " + mde.getMessage(),
                    mde, mde.getLocation());
        } catch (final Exception e) {
            log.error("Task " + getTaskName() + " unexpected exception: " + e);
            log.error(StackTraceUtil.getStackTrace(e));
            throw new BuildException("\n! Task " + getTaskName() + " unexpected exception: "
                    + e.getClass().getSimpleName() + " - " + e.getMessage(), e);
        }

        if (log.isTraceEnabled()) {
            log.trace("end task " + getTaskName());
        }
    }

    protected String getAutoparams() {
        StringBuilder params = new StringBuilder();

        try {
            Field[] fields = getClass().getFields();
            for (Field field : fields) {
                AutoParamStr strAnnotation = field.getAnnotation(AutoParamStr.class);
                if (strAnnotation != null) {
                    params.append(strAnnotation.xmlAttr()).append("=").append(field.get(this)).append(",");
                    continue;
                }

                AutoParamTimeValue timeAnnotation = field.getAnnotation(AutoParamTimeValue.class);
                if (timeAnnotation != null) {
                    params.append(timeAnnotation.xmlAttr()).append("=").append(field.get(this)).append(",");
                    continue;
                }

                AutoParamLong longAnnotation = field.getAnnotation(AutoParamLong.class);
                if (longAnnotation != null) {
                    params.append(longAnnotation.xmlAttr()).append("=").append(field.getLong(this)).append(",");
                    continue;
                }

                AutoParamBool boolAnnotation = field.getAnnotation(AutoParamBool.class);
                if (boolAnnotation != null) {
                    params.append(boolAnnotation.xmlAttr()).append("=").append(field.get(this)).append(",");
                    continue;
                }

                AutoParamEnum enumAnnotation = field.getAnnotation(AutoParamEnum.class);
                if (enumAnnotation != null) {
                    params.append(enumAnnotation.xmlAttr()).append("=").append(field.get(this)).append(",");
                    continue;
                }
            }
        } catch (IllegalAccessException e) {
            log.trace("Can't access value", e);
        }

        if (params.length() > 0) {
            params.deleteCharAt(params.length()-1);
        }

        return params.toString();
    }
}
