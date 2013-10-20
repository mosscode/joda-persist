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
package com.moss.hibernate.util;

import java.io.Serializable;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import org.hibernate.HibernateException;
import org.hibernate.usertype.UserType;

public abstract class QuickImmutableUserType implements UserType {
	private int sqlType;
	private int[] types;
	private Class clazz;
	
	public QuickImmutableUserType(Class clazz, int sqlType) {
		super();
		this.clazz = clazz;
		this.sqlType = sqlType;
		types = new int[]{sqlType};
	}

	public final int[] sqlTypes() { 
		return types;
	}
	
	public final boolean isMutable() {
		return false;
	}

	public final Object assemble(Serializable arg0, Object arg1) throws HibernateException {
		return arg0;
	}

	public final Object deepCopy(Object arg0) throws HibernateException {
		if(arg0==null)return null;
		else return arg0;
	}

	public final Serializable disassemble(Object arg0) throws HibernateException {
		return (Serializable)arg0;
	}

	public final boolean equals(Object arg0, Object arg1) throws HibernateException {

		if (arg0 == null && arg1 == null) {
			return true;
		}else if(arg0 == null || arg1 == null){
			return false;
		}else{
			return arg0.equals(arg1);
		}
	}

	public final int hashCode(Object arg0) throws HibernateException {
		return arg0.hashCode();
	}

	public final Object nullSafeGet(ResultSet resultSet, String[] names, Object arg2) throws HibernateException, SQLException {
		return getFromResultSet(resultSet, names[0]);
	}
	
	public abstract Object getFromResultSet(ResultSet results, String columnName) throws HibernateException, SQLException ;
	public abstract void setValueInStatement(PreparedStatement statement, Object value, int index) throws HibernateException, SQLException ;
	
	public final void nullSafeSet(PreparedStatement statement, Object value, int index) throws HibernateException, SQLException {
		if(value==null) 
			statement.setNull(index, sqlType);
		else
			setValueInStatement(statement, value, index);
	}
	
	public final Object replace(Object original, Object target, Object owner) throws HibernateException {
		return original;
	}

	public final Class returnedClass() {
		return clazz;
	}
}
