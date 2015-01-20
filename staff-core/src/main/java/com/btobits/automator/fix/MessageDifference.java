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

package com.btobits.automator.fix;

import java.util.ArrayList;
import java.util.List;

import com.btobits.automator.fix.utils.MessageDifferenceException;
import quickfix.*;

import com.btobits.automator.fix.comparator.AbstractComparator;
import com.btobits.automator.fix.comparator.BodyComparator2;
import com.btobits.automator.fix.comparator.HeaderComparator;
import com.btobits.automator.fix.comparator.TrailerComparator;
import com.btobits.automator.fix.utils.FixUtils;
import com.btobits.automator.fix.utils.fix.FixMessageType;
import quickfix.field.ApplVerID;
import quickfix.field.BeginString;

public class MessageDifference {

	public MessageDifference() {}


    public static void compare2(final Message inMsg,
                                final FixMessageType inModel,
                                final DataDictionaryProvider ddProvider,
                                final ApplVerID defaultApplVerID,
                                boolean checkHeader,
                                boolean checkTrailer,
                                boolean checkBody,
                                boolean checkOrderGroups) throws Exception {
        DataDictionary sessionDict = MessageUtils2.getSessionDataDictionary(inMsg.getHeader().getString(BeginString.FIELD), ddProvider);
        DataDictionary applicationDict = MessageUtils2.getApplicationDataDictionary(inMsg, ddProvider, defaultApplVerID);

        compare2(inMsg, inModel, applicationDict, sessionDict, checkHeader, checkTrailer, checkBody, checkOrderGroups);
    }

	public static void compare2(final Message inMsg, final FixMessageType inModel, final DataDictionary appDict,
                                final DataDictionary sessionDict, boolean checkHeader, boolean checkTrailer,
                                boolean checkBody, boolean checkOrderGroups) throws Exception {

		final List<String> diff = new ArrayList<String>();

		// get message type
		final String msgType = inMsg.getHeader().getString(FixUtils.getFieldId(FixUtils.MSG_TYPE_FIELD));
		final FixMessageType fixModel = FixUtils.getMessageType(inMsg, appDict, sessionDict);

		if (checkHeader) {
			// check header
			final HeaderComparator comparator = new HeaderComparator(sessionDict, diff);
			comparator.setModel(inModel);
			comparator.compare(fixModel);
		}

		if (checkBody) {
			BodyComparator2 comparator2 = new BodyComparator2(appDict, sessionDict, msgType, diff);
			comparator2.setModel(inModel);

			if (checkOrderGroups) {
				comparator2.compare2Order(fixModel);
			} else {
				comparator2.compare2(fixModel);
			}
		}

		if (checkTrailer) {
			final AbstractComparator comparator = new TrailerComparator(sessionDict, msgType, diff);
			comparator.setModel(inModel);
			comparator.compare(fixModel);
		}

		if (!diff.isEmpty()) { throw new MessageDifferenceException(FixUtils.toString(diff)); }

		//        fixModel.getFields().clear();
		//        fixModel.getFields().addAll(fixModel.getFields());
		//        // copy all fields
		//        for(final FixMessageType.Field fld : fixModel.getFields()) {
		//            if(!isExistField(fld, inModel.getFields())) {
		//                inModel.getFields().add(fld);
		//            }
		//        }

		inModel.getFields().clear();
		inModel.getFields().addAll(fixModel.getFields());
	}

//	private final static boolean isExistField(FixMessageType.Field inField, final List<FixMessageType.Field> inFields) {
//		int id1 = FixUtils.getFieldId(inField);
//		for (final FixMessageType.Field fld : inFields) {
//			int id2 = FixUtils.getFieldId(fld);
//			if (id2 == id1 && (inField instanceof FixMessageType.Group == fld instanceof FixMessageType.Group)) { return true; }
//		}
//		return false;
//	}
}
