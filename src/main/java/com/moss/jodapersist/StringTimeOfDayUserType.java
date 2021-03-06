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

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.GregorianCalendar;

import org.hibernate.HibernateException;
import org.joda.time.DateTimeZone;
import org.joda.time.TimeOfDay;

/**
 * Persists to/from strings of the format hh:mm (no milliseconds or seconds persisted)
 * @author stu
 *
 */
public class StringTimeOfDayUserType extends TimeOfDayUserType{
	private DateFormat formatter = new SimpleDateFormat("hh:mm");
	
	public int[] sqlTypes() {
		return TimeOfDayUserType.DB_FORMAT_STRING_TYPES;
	}
	
	public Object nullSafeGet(ResultSet resultSet, String[] names, Object arg2) throws HibernateException, SQLException {
		Calendar calendar = new GregorianCalendar(timeZone);
		
		String time = resultSet.getString(names[0]);
		if(time==null) return null;
		String[] pieces = time.split(":");
		if(pieces.length!=2) throw new HibernateException("Invalid format (should be hh:mm) \"" + time + "\"");
		
		return new TimeOfDay(Integer.parseInt(pieces[0]), Integer.parseInt(pieces[1]));
	}
	
	public void nullSafeSet(PreparedStatement statement, Object value, int index) throws HibernateException, SQLException 
	{
		
		if(value==null){
			statement.setString(index, null);
		}else{
			TimeOfDay tmd = (TimeOfDay)value;
			long millisInZone = tmd.toDateTimeToday(DateTimeZone.forTimeZone(timeZone)).getMillis();
			Timestamp timestamp = new Timestamp(millisInZone);
			
			statement.setString(index, formatter.format(tmd.toDateTimeToday(DateTimeZone.forTimeZone(super.timeZone)).toDate()));	
		}
	}
	
	
	
}
