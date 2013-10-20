/**
 * Copyright (C) 2013, Moss Computing Inc.
 *
 * This file is part of joda-persist.
 *
 * joda-persist is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 2, or (at your option)
 * any later version.
 *
 * joda-persist is distributed in the hope that it will be useful, but
 * WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with joda-persist; see the file COPYING.  If not, write to the
 * Free Software Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA
 * 02110-1301 USA.
 *
 * Linking this library statically or dynamically with other modules is
 * making a combined work based on this library.  Thus, the terms and
 * conditions of the GNU General Public License cover the whole
 * combination.
 *
 * As a special exception, the copyright holders of this library give you
 * permission to link this library with independent modules to produce an
 * executable, regardless of the license terms of these independent
 * modules, and to copy and distribute the resulting executable under
 * terms of your choice, provided that you also meet, for each linked
 * independent module, the terms and conditions of the license of that
 * module.  An independent module is a module which is not derived from
 * or based on this library.  If you modify this library, you may extend
 * this exception to your version of the library, but you are not
 * obligated to do so.  If you do not wish to do so, delete this
 * exception statement from your version.
 */
package com.moss.jodapersist;

import java.io.Serializable;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.sql.Types;
import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.Properties;
import java.util.TimeZone;

import org.hibernate.HibernateException;
import org.hibernate.usertype.EnhancedUserType;
import org.hibernate.usertype.ParameterizedType;
import org.hibernate.usertype.UserType;
import org.joda.time.DateTimeZone;
import org.joda.time.Duration;
import org.joda.time.Instant;
import org.joda.time.Period;
import org.joda.time.YearMonthDay;

/**
 * <p>
 * 	<b>
 * 	 <i>
 * 	  NOTE: Use one of the sqltype-specific subclasses
 * 	 </i>
 * 	</b>
 * </p>
 * <p>
 * 	Persists a org.joda.time.YearMonthDay as one of a variety of sql types, depending on
 * 	settings specified in a property's mapping parameters.
 * </p>
 * @see com.moss.jodapersist.TimestampYearMonthDayUserType
 * @see com.moss.jodapersist.NumericYearMonthDayUserType
 * @see com.moss.jodapersist.StringYearMonthDayUserType
 */
public abstract class YearMonthDayUserType implements EnhancedUserType, UserType, ParameterizedType, Serializable {
	private Calendar storageCalendar = new GregorianCalendar(TimeZone.getTimeZone("America/New_York"));
	
	public static final String 
		DB_FORMAT_DATETIME = "datetime", 
		DB_FORMAT_NUMERIC = "numeric",
		DB_FORMAT_STRING = "string";
	
	protected static final int[] 
	    DB_FORMAT_NUMERIC_TYPES = new int[] { Types.NUMERIC },
	    DB_FORMAT_DATETIME_TYPES = new int[] { Types.TIMESTAMP },
	    DB_FORMAT_STRING_TYPES = new int[] { Types.VARCHAR };
	
	private String sqltype = null;
	
	public static final String
		TIME_OFFSET_NONE = "none",
		TIME_OFFSET_NOON = "noon";
	
	private String timeOffset = TIME_OFFSET_NONE;
	
	public YearMonthDayUserType(){}
	
	/**
	 * For subclasses which are specific to a particular sql type
	 */
	YearMonthDayUserType(String sqltype) {
		this.sqltype = sqltype;
	}

	public Object fromXMLString(String xmlValue) {
		// TODO Auto-generated method stub
		return null;
	}

	public String objectToSQLString(Object value) {
		YearMonthDay ymd = (YearMonthDay) value;
		System.out.println("\n\nCalled YMD objectToSQLString\n\n");
		return "HELLO \"WORLD!!!!";
	}

	public String toXMLString(Object value) {
		return "IMPLEMENT ME!!";
	}

	public int[] sqlTypes() { 
		if (sqltype.equals(DB_FORMAT_NUMERIC)) {
			return DB_FORMAT_NUMERIC_TYPES;
		}
		else if (sqltype.equals(DB_FORMAT_DATETIME)) { 
			return DB_FORMAT_DATETIME_TYPES;
		}
		else if (sqltype.equals(DB_FORMAT_STRING)) {
			return DB_FORMAT_STRING_TYPES;
		}
		else throw new Error("No valid sqlType specified!");
	}
	
	public boolean isMutable() {
		return false;
	}

	public Object assemble(Serializable arg0, Object arg1) throws HibernateException {
		return arg0;
	}

	public Object deepCopy(Object arg0) throws HibernateException {
		if(arg0==null)return null;
		else return arg0;
	}

	public Serializable disassemble(Object arg0) throws HibernateException {
		return (Serializable)arg0;
	}

	public boolean equals(Object arg0, Object arg1) throws HibernateException {
		if(arg0==null && arg1==null) return true;
		if(arg0==null || arg1==null) return false;
		return arg0.equals(arg1);
	}

	public int hashCode(Object arg0) throws HibernateException {
		return arg0.hashCode();
	}

	public Object nullSafeGet(ResultSet resultSet, String[] names, Object arg2) throws HibernateException, SQLException {
		Object o = resultSet.getObject(names[0]);
		
		if (o == null) {
			return null;
		}
		else if (sqltype.equals(DB_FORMAT_NUMERIC)) {
			long ymd = resultSet.getLong(names[0]);
			return longToYearMonthDay(ymd);
		}
		else if (sqltype.equals(DB_FORMAT_DATETIME)) {
			Timestamp timestamp = resultSet.getTimestamp(names[0], storageCalendar);
			YearMonthDay ymd = new YearMonthDay(timestamp.getTime());
			return ymd;
		}
		else if (sqltype.equals(this.DB_FORMAT_STRING)) {
			String timeStr = resultSet.getString(names[0]);
			String[] parts = timeStr.split("-");
			
			YearMonthDay ymd = new YearMonthDay(
				Integer.parseInt(parts[0]),
				Integer.parseInt(parts[1]),
				Integer.parseInt(parts[2])
			);
			
			return ymd;
		}
		else throw new Error("No valid sqlType specified!");
	}

	public void nullSafeSet(PreparedStatement statement, Object value, int index) throws HibernateException, SQLException {
		YearMonthDay ymd = (YearMonthDay)value;
		
		if (ymd != null) {
			if (sqltype.equals(this.DB_FORMAT_NUMERIC)) {
				statement.setLong(index, yearMonthDayToLong(ymd));
			}
			else if (sqltype.equals(this.DB_FORMAT_DATETIME)) {
				Instant instant = ymd.toDateTimeAtMidnight().toInstant();
				
				if (timeOffset.equals(TIME_OFFSET_NOON)) {
					instant = instant.toDateTime(DateTimeZone.UTC).plusHours(12).toInstant();
				}
			
				statement.setTimestamp(index, new Timestamp(instant.getMillis()), storageCalendar);	
			}
			else if (sqltype.equals(this.DB_FORMAT_STRING)) {
				statement.setString(index, ymd.toString());
			}
			else throw new Error("No valid sqlType specified!");
		}
		else {
			statement.setNull(index, getNullType());
		}
	}
	
	public abstract int getNullType();
	
	public Object replace(Object original, Object target, Object owner) throws HibernateException {
		return original;
	}

	public Class returnedClass() {
		return YearMonthDay.class;
	}
	
	public long yearMonthDayToLong(YearMonthDay ymd) throws HibernateException {
		long l = 0;
		
		l += ymd.getYear() * 10000;
		l += ymd.getMonthOfYear() * 100;
		l += ymd.getDayOfMonth();
		
		return l;
	}
	
	public YearMonthDay longToYearMonthDay(long yearMonthDay) throws HibernateException {
		
		int year = (int)yearMonthDay / 10000 ;
		int month = (int)yearMonthDay / 100 % 100; 
		int day = (int)yearMonthDay % 100;

		return new YearMonthDay(year, month, day);
	}
	
	public void setParameterValues(Properties p) {
		if (p != null) {
			if(sqltype==null) sqltype = p.getProperty("sqltype", null);
			timeOffset = p.getProperty("timeOffset", TIME_OFFSET_NONE);
		}
	}
	
}
