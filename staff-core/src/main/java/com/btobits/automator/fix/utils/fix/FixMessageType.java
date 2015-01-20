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

import com.btobits.automator.ReleaseInfo;
import com.btobits.automator.fix.utils.FixUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.tools.ant.BuildException;
import org.apache.tools.ant.Project;
import org.apache.tools.ant.types.DataType;

import java.util.LinkedList;

public class FixMessageType extends DataType {
    static {
        System.out.println("Software Testing Automation Framework for FIX version " + ReleaseInfo.getVersion());
    }

    public class Impl {
        private String data;
        private final LinkedList<FixMessageType.Field> fields = new LinkedList<FixMessageType.Field>();

        public Impl() {
        }

        public void addField(FixMessageType.Field field) {
            if (field != null) {
                fields.addLast(field);
            }
        }

        public LinkedList<FixMessageType.Field> getFields() {
            buildStructure();
            return fields;
        }

        public final FixMessageType.Field getMsgTypeField() {
            buildStructure();

            for (final FixMessageType.Field fld : fields) {
                if (StringUtils.equals(fld.getName(), FixUtils.MSG_TYPE_FIELD) ||
                        StringUtils.equals(fld.getName(), "35")) {
                    return fld;
                }
            }
            return null;
        }

        private void buildStructure() {
            int size = fields.size();
            for (int index = size; index > 0; index--) {
                FixMessageType.Field fld = fields.get(index - 1);
                if (fld instanceof FixMessageType.Block) {
                    fields.remove(fld);
                    for (FixMessageType.Field fldBl : fld.getFields()) {
                        fields.add(fldBl);
                    }
                }
            }
        }

        public String getData() {
            return data;
        }

        public void setData(final String inData) {
            data = inData;
        }
    }

    public static class Field {
        private String name;
        private String value = "";
        private int count = -1;
        private final LinkedList<FixMessageType.Field> fields = new LinkedList<FixMessageType.Field>();
        private boolean isLeading = false;

        private boolean isValueRegGxp = false;

        public Field() {
        }

        public Field(String inName, String inValue) {
            this.name = inName;
            this.value = inValue;
        }

        public Field(final quickfix.Field inField) {
            this(inField, false, false);
        }

        public Field(final quickfix.Field inField, boolean isLeading) {
            this(inField, isLeading, false);
        }

        public Field(final quickfix.Field inField, boolean isLeading, boolean isValueRegGxp) {
            this.name = Integer.toString(inField.getTag());
            if (inField.getObject() != null) {
                this.value = inField.getObject() + "";
            } else {
                this.value = null;
            }
            this.isLeading = isLeading;
            this.isValueRegGxp = isValueRegGxp;
        }

        public void addText(String inValue) {
            value = inValue;
        }

        public void setName(final String inV) {
            name = inV;
        }

        public final String getName() {
            return name;
        }

        public void setGroup(final String inGroup) {
            value = Boolean.parseBoolean(inGroup) ? null : "";
        }

        public void setValue(final String inValue) {
            value = inValue;
        }

        public final String getValue() {
            return value;
        }

        public int getCount() {
            return count;
        }

        public boolean isLeading() {
            return isLeading;
        }

        public void setCount(String inCount) {
            count = Integer.parseInt(inCount);
        }

        public void addField(final Field inField) {
            if (name == null) {
                throw new BuildException("Group name is not specified");
            }

            if (value != null) {
                throw new BuildException("Field '" + name + "' is not a group.");
            }

            if (inField != null) {
                fields.addLast(inField);
            }
        }

        public final LinkedList<FixMessageType.Field> getFields() {
            return fields;
        }

        public final boolean isGroup() {
            return this instanceof Group;
        }

        public void setValueRegGxp(boolean isValueRegGxp) {
            this.isValueRegGxp = isValueRegGxp;
        }

        public final boolean isValueRegGxp() {
            return isValueRegGxp;
        }
    }

    public static class Group extends FixMessageType.Field {

        public Group() {
            super();
            setValue(null);
        }

        public void addGroup(final Group inGroup) {
            addField(inGroup);
        }

        public void addText(String inText) {}
    }

    public static class Block extends FixMessageType.Field {
        Project project;

        public Block() {
            super();
            setValue(null);
        }

        public void addGroup(final Group inGroup) {
            addField(inGroup);
        }

        public void addText(String inText) {}

        public void addField(final Field inField) {
            addField(inField);
        }

        public void setSource(final String inSrc) throws BuildException {
            if (inSrc == null) {
                throw new BuildException("Reference to the FIX message is not specified");
            }

            final Object obj = project.getReference(inSrc);
            if (obj == null) {
                throw new BuildException("Failed to get FIX message by reference");
            }

            if (obj instanceof FixMessageType) {
                Impl impl = ((FixMessageType) obj).getImpl();
                getFields().addAll(impl.getFields());
            } else {
                throw new BuildException("Referenced message type is unknown");
            }
        }

        public void setProject(Project inProject) {
            project = inProject;
        }
    }

    private Impl impl = new Impl();

    public FixMessageType() {
        super();
    }

    public FixMessageType(final Project inProject) {
        super();
        setProject(inProject);
    }

    public void setSource(final String inSrc) throws BuildException {
        if (inSrc == null) {
            throw new BuildException("Reference to the FIX message is not specified");
        }

        final Object obj = getProject().getReference(inSrc);
        if (obj == null) {
            throw new BuildException("Failed to get FIX message by reference");
        }

        /*if (obj instanceof FixBlockType) {
            impl = ((FixMessageType) obj).getImpl();
        } else*/
        if (obj instanceof FixMessageType) {
            impl = ((FixMessageType) obj).getImpl();
        } else {
            throw new BuildException("Referenced message type is unknown");
        }
    }

    public void setData(String data) {
        getImpl().setData(data);
    }

    public void addField(final Field inField) {
        getImpl().addField(inField);
    }

    public void addGroup(final Group inGroup) {
        getImpl().addField(inGroup);
    }

    public void addBlock(final Block inBlock) {
        //final LinkedList<FixMessageType.Field> flds = inBlock.getFields();
        getImpl().addField(inBlock);
    }

    public final LinkedList<FixMessageType.Field> getFields() {
        return getImpl().getFields();
    }

    private Impl getImpl() {
        return impl;
    }

    public final FixMessageType.Field getMsgTypeField() {
        return getImpl().getMsgTypeField();
    }

    public final String getData() {
        return getImpl().getData();
    }
}
