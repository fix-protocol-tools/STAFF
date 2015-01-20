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

import java.util.List;

import quickfix.DataDictionary;
import quickfix.Session;

import com.btobits.automator.fix.utils.FixUtils;
import com.btobits.automator.fix.utils.TreeUtils;
import com.btobits.automator.fix.utils.fix.FixMessageType;

/**
 * @author Volodymyr_Biloshkurs
 */
public final class BodyComparator2 {
	protected DataDictionary appDictionary;

    protected DataDictionary sessionDictionary;

	protected String messageType;

	protected String dictType;

	protected List<String> messageErrors;

	protected Session session;

	protected FixMessageType model;

	public BodyComparator2() {}

	public BodyComparator2(final DataDictionary sessionDictionary, final List<String> inMessageErrors) {
        this(sessionDictionary, sessionDictionary, null, inMessageErrors);
	}

	public BodyComparator2(final DataDictionary sessionDictionary, final String inMessageType, final List<String> inMessageErrors) {
        this(sessionDictionary, sessionDictionary, inMessageType, inMessageErrors);
	}

    public BodyComparator2(final DataDictionary appDictionary, final DataDictionary sessionDictionary, final String inMessageType, final List<String> inMessageErrors) {
        this.sessionDictionary = sessionDictionary;
        this.appDictionary = appDictionary;
        this.messageType = inMessageType;
        this.messageErrors = inMessageErrors;
    }

	public List<String> getMessageErrors() {
		return messageErrors;
	}

	public void setMessageErrors(List<String> messageErrors) {
		this.messageErrors = messageErrors;
	}

	public String getMessageType() {
		return messageType;
	}

	public void setMessageType(String messageType) {
		this.messageType = messageType;
	}

	public DataDictionary getAppDictionary() {
		return appDictionary;
	}

	public void setAppDictionary(DataDictionary appDictionary) {
		this.appDictionary = appDictionary;
	}

    public DataDictionary getSessionDictionary() {
        return sessionDictionary;
    }

    public void setSessionDictionary(DataDictionary sessionDictionary) {
        this.sessionDictionary = sessionDictionary;
    }

    public String getDictType() {
		return dictType;
	}

	public void setDictType(String dictType) {
		this.dictType = dictType;
	}

	public Session getSession() {
		return session;
	}

	public void setSession(Session session) {
		this.session = session;
	}

	public void setModel(FixMessageType inModel) {
		this.model = inModel;
	}

	public FixMessageType getModel() {
		return model;
	}

	public boolean compare2(final FixMessageType inMessageType) {

		for (final FixMessageType.Field field : model.getFields()) {
			final int fieldId = FixUtils.getFieldId(field.getName());

			if (sessionDictionary.isHeaderField(fieldId) || sessionDictionary.isTrailerField(fieldId)) {
				continue;
			}

			if ((field.isGroup())) {
				final FixMessageType.Group group = TreeUtils.getModelGroup(field, inMessageType.getFields(), getAppDictionary(),
						getMessageType());

				if (group == null) {
					messageErrors.add("Group [" + field.getName() + "] or nested one does not exist/equal in received message");
				} else if (TreeUtils.compareTreeNode(field, group, getAppDictionary(), getMessageType(), messageErrors)) {
					//inMessageType.getFields().remove(group);
				}
			} else {
				final FixMessageType.Field fld = FixUtils.getModelField(fieldId, inMessageType.getFields(), true);

				if (fld == null) {
					messageErrors.add("Field [" + field.getName() + ", " + field.getValue() + "] does not exist in received message");
				} else {
					TreeUtils.compareTreeNode(field, fld, getAppDictionary(), getMessageType(), messageErrors);
				}
			}
		}
		return true;
	}

	public boolean compare2Order(final FixMessageType inMessageType) {
		for (final FixMessageType.Field field : model.getFields()) {
			final int fieldId = FixUtils.getFieldId(field.getName());

			if (sessionDictionary.isHeaderField(fieldId) || sessionDictionary.isTrailerField(fieldId)) {
				continue;
			}

			if (field.isGroup()) {
				final FixMessageType.Group group = TreeUtils.getModelGroup(field, inMessageType.getFields(), getAppDictionary(),
						getMessageType());
				if (group == null) {
					messageErrors.add("Group [" + field.getName() + "] or nested one does not exist/equal in received message");
				} else {
					int position = TreeUtils.getGroupPosition(field, model.getFields());
					FixMessageType.Field grByPosition = TreeUtils.getGroupByPosition(position, FixUtils.getFieldId(field.getName()),
							inMessageType.getFields());

					if (grByPosition == null) {
						messageErrors.add("Group [" + field.getName() + ", " + field.getValue()
								+ "] or nested one does not exist/equal in received message");
					} else {
						TreeUtils.compareTreeNodeWithGroupOrder(field, grByPosition, getAppDictionary(), getMessageType(), messageErrors);
					}
				}
			} else {
				final FixMessageType.Field fld = FixUtils.getModelField(fieldId, inMessageType.getFields(), true);
				if (fld == null) {
					messageErrors.add("Field [" + field.getName() + ", " + field.getValue() + "] does not exist in received message");
				} else {
					if (TreeUtils.compareTreeNodeWithGroupOrder(field, fld, getAppDictionary(), getMessageType(), messageErrors)) {
						//inMessageType.getFields().remove(fld);
					}
				}
			}
		}
		return true;
	}
}