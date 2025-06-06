/*
 * Copyright (c) 2022, 2025, Oracle and/or its affiliates.
 *
 * This program is free software; you can redistribute it and/or modify it under the terms of the GNU General Public License, version 2.0, as published by
 * the Free Software Foundation.
 *
 * This program is designed to work with certain software that is licensed under separate terms, as designated in a particular file or component or in
 * included license documentation. The authors of MySQL hereby grant you an additional permission to link the program and your derivative works with the
 * separately licensed software that they have either included with the program or referenced in the documentation.
 *
 * Without limiting anything contained in the foregoing, this file, which is part of MySQL Connector/J, is also subject to the Universal FOSS Exception,
 * version 1.0, a copy of which can be found at http://oss.oracle.com/licenses/universal-foss-exception.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or
 * FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License, version 2.0, for more details.
 *
 * You should have received a copy of the GNU General Public License along with this program; if not, write to the Free Software Foundation, Inc.,
 * 51 Franklin St, Fifth Floor, Boston, MA 02110-1301 USA
 */

package com.mysql.cj.protocol.a;

import java.time.LocalDate;
import java.time.LocalDateTime;

import com.mysql.cj.BindValue;
import com.mysql.cj.Messages;
import com.mysql.cj.exceptions.ExceptionFactory;
import com.mysql.cj.exceptions.WrongArgumentException;
import com.mysql.cj.protocol.InternalDate;
import com.mysql.cj.protocol.InternalTimestamp;
import com.mysql.cj.protocol.Message;
import com.mysql.cj.protocol.a.NativeConstants.IntegerDataType;
import com.mysql.cj.protocol.a.NativeConstants.StringSelfDataType;
import com.mysql.cj.util.StringUtils;
import com.mysql.cj.util.TimeUtil;

public class LocalDateValueEncoder extends AbstractValueEncoder {

    @Override
    public String getString(BindValue binding) {
        switch (binding.getMysqlType()) {
            case NULL:
                return "null";
            case DATE:
                StringBuilder sb = new StringBuilder("'");
                sb.append(((LocalDate) binding.getValue()).format(TimeUtil.DATE_FORMATTER));
                sb.append("'");
                return sb.toString();
            case DATETIME:
            case TIMESTAMP:
                sb = new StringBuilder("'");
                sb.append(LocalDateTime.of((LocalDate) binding.getValue(), TimeUtil.DEFAULT_TIME).format(TimeUtil.DATETIME_FORMATTER_WITH_OPTIONAL_MICROS));
                sb.append("'");
                return sb.toString();
            case YEAR:
                return String.valueOf(((LocalDate) binding.getValue()).getYear());
            case CHAR:
            case VARCHAR:
            case TINYTEXT:
            case TEXT:
            case MEDIUMTEXT:
            case LONGTEXT:
                sb = new StringBuilder("'");
                sb.append(binding.getValue().toString());
                sb.append("'");
                return sb.toString();
            default:
                throw ExceptionFactory.createException(WrongArgumentException.class,
                        Messages.getString("PreparedStatement.67", new Object[] { binding.getValue().getClass().getName(), binding.getMysqlType().toString() }),
                        this.exceptionInterceptor);
        }
    }

    @Override
    public void encodeAsBinary(Message msg, BindValue binding) {
        NativePacketPayload intoPacket = (NativePacketPayload) msg;
        switch (binding.getMysqlType()) {
            case DATE:
                writeDate(msg, InternalDate.from((LocalDate) binding.getValue()));
                return;
            case DATETIME:
            case TIMESTAMP:
                writeDateTime(msg, InternalTimestamp.from(LocalDateTime.of((LocalDate) binding.getValue(), TimeUtil.DEFAULT_TIME)));
                return;
            case YEAR:
                intoPacket.writeInteger(IntegerDataType.INT4, ((LocalDate) binding.getValue()).getYear());
                return;
            case CHAR:
            case VARCHAR:
            case TINYTEXT:
            case TEXT:
            case MEDIUMTEXT:
            case LONGTEXT:
                intoPacket.writeBytes(StringSelfDataType.STRING_LENENC, StringUtils.getBytes(binding.getValue().toString(), this.charEncoding.getValue()));
                return;
            default:
                throw ExceptionFactory.createException(WrongArgumentException.class,
                        Messages.getString("PreparedStatement.67", new Object[] { binding.getValue().getClass().getName(), binding.getMysqlType().toString() }),
                        this.exceptionInterceptor);
        }
    }

    @Override
    public void encodeAsQueryAttribute(Message msg, BindValue binding) {
        writeDate(msg, InternalDate.from((LocalDate) binding.getValue()));
    }

}
