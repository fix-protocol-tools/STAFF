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

import com.btobits.automator.fix.utils.MessageBuilder;
import org.apache.commons.lang.StringUtils;
import quickfix.*;
import quickfix.field.ApplVerID;

/**
 * @author Mykhailo_Sereda
 */
public class FixMessageConverter implements MessageConverter {
    private DataDictionaryProvider ddProvider;
    private ApplVerID defaultApplicationVersionID;
    private MessageFactory messageFactory;
    private String senderCompId;
    private String targetCompId;
    private String fixVersion;

    public FixMessageConverter(DataDictionaryProvider ddProvider,
                               ApplVerID defaultApplicationVersionID,
                               MessageFactory messageFactory,
                               String senderCompId,
                               String targetCompId,
                               String fixVersion) {
        String errInitMsg = "Can't initialize message converter. ";
        if (ddProvider != null) {
            this.ddProvider= ddProvider;
        } else {
            throw new NullPointerException(errInitMsg + "DataDictionaryProvider is null");
        }
        this.defaultApplicationVersionID = defaultApplicationVersionID;
        if (messageFactory != null) {
            this.messageFactory = messageFactory;
        } else {
            throw new NullPointerException(errInitMsg + "MessageFactory is null");
        }
        if (StringUtils.isNotBlank(senderCompId)) {
            this.senderCompId = senderCompId;
        } else {
            throw new NullPointerException(errInitMsg + "SenderCompId is null");
        }
        if (StringUtils.isNotBlank(targetCompId)) {
            this.targetCompId = targetCompId;
        } else {
            throw new NullPointerException(errInitMsg + "TargetCompId is null");
        }
        if (StringUtils.isNotBlank(fixVersion)) {
            this.fixVersion = fixVersion;
        } else {
            throw new NullPointerException(errInitMsg + "FixVersion is null");
        }
    }

    public Message parseMessage(final String msgStr) throws InvalidMessage {
        return MessageUtils2.parseMessage(messageFactory, ddProvider, defaultApplicationVersionID, msgStr);
    }

    public Message convertToMessage(FixMessageType ourMsg) throws Exception {
        return MessageBuilder.build(senderCompId, targetCompId, fixVersion,
                messageFactory, ddProvider, defaultApplicationVersionID, ourMsg, null);
    }

}
