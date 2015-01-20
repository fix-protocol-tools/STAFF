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

package com.btobits.automator.ant.sql.task;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.validator.routines.*;
import org.apache.tools.ant.BuildException;
import org.apache.tools.ant.Task;

import java.math.BigDecimal;
import java.sql.*;
import java.util.*;
import java.util.Date;

/**
 * @author Volodymyr_Biloshkurs
 */
public class SQLCompareTask extends Task {
    private String url;
    private String user;
    private String password;
    private String driver;
    private String query;

    final List<String> errors = new ArrayList<String>();

    private ResultSet resultSet;


    public final String getUrl() {
        return url;
    }

    public void setUrl(String inUrl) {
        url = inUrl;
    }

    public final String getUserid() {
        return user;
    }

    public void setUserid(String inUser) {
        user = inUser;
    }

    public final String getPassword() {
        return password;
    }

    public void setPassword(String inPassword) {
        password = inPassword;
    }

    public final String getDriver() {
        return driver;
    }

    public void setDriver(String inDriver) {
        driver = inDriver;
    }

    public final String getQuery() {
        return query;
    }

    public void setQuery(String inQuery) {
        query = inQuery;
    }

    public final ResultSet getResultSet() {
        return resultSet;
    }

    @Override
    public void execute() throws BuildException {
        validateParameter();

        Connection cnn = null;
        try {
            Class.forName(driver);
            cnn = DriverManager.getConnection(url, user, password);
            final Statement stmt = cnn.createStatement(ResultSet.TYPE_SCROLL_INSENSITIVE, ResultSet.CONCUR_READ_ONLY);
            resultSet = stmt.executeQuery(query);

            verify();

        } catch (Exception ex) {
            resultSet = null;
            throw new BuildException("Error build: " + ex.getMessage());
        } finally {
            try {
                cnn.close();
            } catch (Exception ex) {
            }
        }

        if (errors.size() > 0) {
            throw new BuildException(toString(errors));
        }
    }

    private void verify() throws Exception {
        final LinkedList<SQLCompareTask.VerifyCell> rows = impl.getVerifySqls();
        for (final SQLCompareTask.VerifyCell sqlRow : rows) {
            sqlRow.validate();

            switch (getColumnType(sqlRow)) {
                case Types.DOUBLE: {
                    final Double val = (Double) getRowValue(sqlRow);

                    if (val == null) {
                        errors.add("Error compare row, row not exist [" + sqlRow.toString() + "].");
                    }

                    final DoubleValidator doubleValidator = new DoubleValidator();
                    if (doubleValidator.isValid(sqlRow.getValue())) {
                        final Double dbValue = doubleValidator.validate(sqlRow.getValue());
                        if (!dbValue.equals(val)) {
                            errors.add("Error compare row [" +
                                    sqlRow.getRow() + "], field [" +
                                    sqlRow.getField() + "], value [" +
                                    sqlRow.getValue() + " != " + val + "]");
                        }
                    } else {
                        errors.add("Error cast field [" + sqlRow.toString() + "] to Double value.");
                    }
                }
                break;

                case Types.FLOAT: {
                    final Float val = (Float) getRowValue(sqlRow);

                    if (val == null) {
                        errors.add("Error compare row, row not exist [" + sqlRow.toString() + "].");
                    }

                    final FloatValidator validator = new FloatValidator();
                    if (validator.isValid(sqlRow.getValue())) {
                        final Float dbValue = validator.validate(sqlRow.getValue());
                        if (!dbValue.equals(val)) {
                            errors.add("Error compare row [" +
                                    sqlRow.getRow() + "], field [" +
                                    sqlRow.getField() + "], value [" +
                                    sqlRow.getValue() + " != " + val + "]");
                        }
                    } else {
                        errors.add("Error cast field [" + sqlRow.toString() + "] to Float value.");
                    }
                }
                break;

                case Types.DECIMAL: {
                    final BigDecimal val = (BigDecimal) getRowValue(sqlRow);

                    if (val == null) {
                        errors.add("Error compare row, row not exist [" + sqlRow.toString() + "].");
                    }

                    final BigDecimalValidator validator = new BigDecimalValidator();
                    if (validator.isValid(sqlRow.getValue())) {
                        BigDecimal dbValue = validator.validate(sqlRow.getValue());
                        dbValue = dbValue.setScale(val.scale());
                        if (!dbValue.equals(val)) {
                            errors.add("Error compare row [" +
                                    sqlRow.getRow() + "], field [" +
                                    sqlRow.getField() + "], value [" +
                                    sqlRow.getValue() + " != " + val + "]");
                        }
                    } else {
                        errors.add("Error cast field [" + sqlRow.toString() + "] to Decimal value.");
                    }
                }
                break;

                case Types.DATE: {
                    final Date val = (Date) getDateRowValue(sqlRow);

                    if (val == null) {
                        errors.add("Error compare row, row not exist [" + sqlRow.toString() + "].");
                    }

                    final DateValidator validator = DateValidator.getInstance();
                    if (validator.isValid(sqlRow.getValue(), "yyyy-MM-dd")) {
                        final Date dbValue = validator.validate(sqlRow.getValue(), "yyyy-MM-dd");
                        if (!dbValue.equals(val)) {
                            errors.add("Error compare row [" +
                                    sqlRow.getRow() + "], field [" +
                                    sqlRow.getField() + "], value [" +
                                    sqlRow.getValue() + " != " + val + "]");
                        }
                    } else {
                        errors.add("Error cast field [" + sqlRow.toString() + "] to Date value.");
                    }
                }
                break;

                case Types.TIME: {
                    final Date val = (Date) getTimeRowValue(sqlRow);
                    if (val == null) {
                        errors.add("Error compare row, row not exist [" + sqlRow.toString() + "].");
                    }

                    final TimeValidator validator = TimeValidator.getInstance();
                    if (validator.isValid(sqlRow.getValue(), "HH:mm:ss")) {
                        final Calendar dbValue = validator.validate(sqlRow.getValue(), "HH:mm:ss");
                        final Calendar dbVal = Calendar.getInstance();
                        dbVal.setTime(val);

                        if (validator.compareHours(dbValue, dbVal) != 0 ||
                                validator.compareMinutes(dbValue, dbVal) != 0 ||
                                validator.compareSeconds(dbValue, dbVal) != 0) {
                            errors.add("Error compare row [" +
                                    sqlRow.getRow() + "], field [" +
                                    sqlRow.getField() + "], value [" +
                                    sqlRow.getValue() + " != " + val + "]");
                        }
                    } else {
                        errors.add("Error cast field [" + sqlRow.toString() + "] to Time value.");
                    }
                }
                break;

                case Types.TIMESTAMP: {
                    final Date val = getDateTimeRowValue(sqlRow);
                    if (val == null) {
                        errors.add("Error compare row, row not exist [" + sqlRow.toString() + "].");
                    }

                    final CalendarValidator validatorDate = CalendarValidator.getInstance();
                    final TimeValidator validatorTime = TimeValidator.getInstance();
                    if (validatorDate.isValid(sqlRow.getValue(), "yyyy-MM-dd HH:mm:ss")) {
                        final Calendar dbValue = validatorDate.validate(sqlRow.getValue(), "yyyy-MM-dd HH:mm:ss");
                        final Calendar dbVal = Calendar.getInstance();
                        dbVal.setTimeInMillis(val.getTime());
                        if (validatorDate.compareDates(dbVal, dbValue) != 0 ||
                                validatorTime.compareHours(dbValue, dbVal) != 0 ||
                                validatorTime.compareMinutes(dbValue, dbVal) != 0 ||
                                validatorTime.compareSeconds(dbValue, dbVal) != 0) {
                            errors.add("Error compare row [" +
                                    sqlRow.getRow() + "], field [" +
                                    sqlRow.getField() + "], value [" +
                                    sqlRow.getValue() + " != " + val + "]");
                        }
                    } else {
                        errors.add("Error cast field [" + sqlRow.toString() + "] to Timestamp value.");
                    }
                }
                break;

                default: {
                    final String dbValue = getStringRowValue(sqlRow);
                    if (dbValue == null) {
                        errors.add("Error compare row, row not exist [" + sqlRow.toString() + "].");
                    } else if (!StringUtils.equals(sqlRow.getValue(), dbValue)) {
                        errors.add("Error compare row [" +
                                sqlRow.getRow() + "], field [" +
                                sqlRow.getField() + "], value [" +
                                sqlRow.getValue() + " != " + dbValue + "]");
                    }
                }
            }
        }
    }

    private Object getRowValue(final SQLCompareTask.VerifyCell inRowPos) throws Exception {
        Object result = null;

        long pos = Long.parseLong(inRowPos.getRow());
        long count = 0;
        while (resultSet.next()) {
            if (count == pos - 1) {
                result = resultSet.getObject(inRowPos.getField());
                return result;
            }
            count++;
        }

        return result;
    }

    private String getStringRowValue(final SQLCompareTask.VerifyCell inRowPos) throws Exception {
        String result = null;

        long pos = Long.parseLong(inRowPos.getRow());
        long count = 0;
        while (resultSet.next()) {
            if (count == pos - 1) {
                result = resultSet.getString(inRowPos.getField());
                return result;
            }
            count++;
        }

        return result;
    }

    private Date getDateRowValue(final SQLCompareTask.VerifyCell inRowPos) throws Exception {
        Date result = null;

        long pos = Long.parseLong(inRowPos.getRow());
        long count = 0;
        while (resultSet.next()) {
            if (count == pos - 1) {
                result = resultSet.getDate(inRowPos.getField());
                return result;
            }
            count++;
        }

        return result;
    }

    private Date getTimeRowValue(final SQLCompareTask.VerifyCell inRowPos) throws Exception {
        Date result = null;

        long pos = Long.parseLong(inRowPos.getRow());
        long count = 0;
        while (resultSet.next()) {
            if (count == pos - 1) {
                result = resultSet.getTime(inRowPos.getField());
                return result;
            }
            count++;
        }

        return result;
    }

    private Date getDateTimeRowValue(final SQLCompareTask.VerifyCell inRowPos) throws Exception {
        Date result = null;

        long pos = Long.parseLong(inRowPos.getRow());
        long count = 0;
        while (resultSet.next()) {
            if (++count == pos) {
                result = resultSet.getTimestamp(inRowPos.getField());
                return result;
            }
            count++;
        }

        return result;
    }

    private void validateParameter() throws BuildException {
        if (StringUtils.isBlank(driver)) {
            throw new BuildException("Driver must be specifed.");
        }

        if (StringUtils.isBlank(url)) {
            throw new BuildException("Url must be specifed.");
        }

        if (StringUtils.isBlank(user)) {
            throw new BuildException("User must be specifed.");
        }

        if (password == null) {
            throw new BuildException("Password cannot be null.");
        }

        if (StringUtils.isBlank(query)) {
            throw new BuildException("Query must be specifed.");
        }

        query = StringUtils.trim(query);
        if (!StringUtils.startsWithIgnoreCase(query, "SELECT")) {
            throw new BuildException("Query must start witch 'SELECT' statement.");
        }
    }

    public void addVerifyCell(final VerifyCell inRow) {
        impl.addVerifyCell(inRow);
    }

    ////////////////////////////////////
    private Impl impl = new Impl();

    public class Impl {
        private final LinkedList<SQLCompareTask.VerifyCell> rows = new LinkedList<SQLCompareTask.VerifyCell>();

        public Impl() {
        }

        public void addVerifyCell(final VerifyCell inRow) {
            if (inRow != null) {
                rows.addLast(inRow);
            }
        }

        public final LinkedList<SQLCompareTask.VerifyCell> getVerifySqls() {
            return rows;
        }
    }

    private int getColumnType(final SQLCompareTask.VerifyCell inRowPos) throws Exception {
        final IntegerValidator validator = new IntegerValidator();
        if (validator.isValid(inRowPos.getField())) {
            Integer id = validator.validate(inRowPos.getField());
            return resultSet.getMetaData().getColumnType(id);
        } else {
            for (int i = 0; i < resultSet.getMetaData().getColumnCount(); i++) {
                final String nameColumn = resultSet.getMetaData().getColumnName(i + 1);
                if (StringUtils.equalsIgnoreCase(nameColumn, inRowPos.getField())) {
                    return resultSet.getMetaData().getColumnType(i + 1);
                }
            }
        }
        throw new Exception("Unknown field name [" + inRowPos.getField() + "].");
    }

    public static class VerifyCell {
        private String row;
        private String field;
        private String value;

        public VerifyCell() {

        }

        public VerifyCell(final String inRow,
                          final String inField,
                          final String inValue) {
            row = inRow;
            field = inField;
            value = inValue;
        }

        public VerifyCell(final VerifyCell inData) {
            this.row = inData.getRow();
            this.field = inData.getField();
            this.value = inData.getValue();
        }

        public final String getRow() {
            return row;
        }

        public void setRow(final String inRow) {
            row = inRow;
        }

        public final String getField() {
            return field;
        }

        public void setField(String fieldin) {
            field = fieldin;
        }

        public final String getValue() {
            return value;
        }

        public void setValue(final String inValue) {
            value = inValue;
        }

        @Override
        public String toString() {
            return "VerifyCell{" +
                    "row='" + row + '\'' +
                    ", field='" + field + '\'' +
                    ", value='" + value + '\'' +
                    '}';
        }

        public void validate() throws BuildException {
            if (StringUtils.isBlank(row)) {
                throw new BuildException("Row not specifed [" + toString() + "].");
            }

            if (StringUtils.isBlank(field)) {
                throw new BuildException("Field not specified [" + toString() + "].");
            }

            if (StringUtils.isBlank(value)) {
                throw new BuildException("Value not specified [" + toString() + "].");
            }

            final Long rowPos = Long.parseLong(row);
            if (rowPos <= 0) {
                throw new BuildException("Row must be > 0, [" + toString() + "].");
            }
        }
    }

    private String toString(final List<String> inMessages) {
        if (inMessages.isEmpty()) {
            return "No errors detected";
        }

        final StringBuilder builder = new StringBuilder();
        Integer index = 0;
        for (String msg : inMessages) {
            if (index == 0) {
                builder.append('\n');
            }
            builder.append((++index).toString()).append(") ").append(msg)
                    .append('\n');
        }
        return builder.toString();
    }
}
