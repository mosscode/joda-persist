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
package test.com.moss.jodapersist;

import junit.framework.Assert;
import junit.framework.TestCase;

import org.joda.time.Instant;

import com.moss.jodapersist.InstantUserType;

public class InstantUserTypeTest extends TestCase {

	final InstantUserType userType = new InstantUserType();
	
	public void testEqualsBothNull() {
		Assert.assertTrue(userType.equals(null, null));
	}
	
	public void testEqualsFirstNull() {
		Instant instantB = new Instant();
		
		Assert.assertTrue(!userType.equals(null, instantB));
	}
	
	public void testEqualsSecondNull() {
		Instant instantA = new Instant();
		
		Assert.assertTrue(!userType.equals(instantA, null));
	}
	
	public void testEqualsSameObject() {
		Instant instantA = new Instant();
	
		Assert.assertTrue(userType.equals(instantA, instantA));
	}
	
	public void testEqualsDifferentObjectsSameValue() {
		Instant instantA = new Instant();
		Instant instantB = new Instant(instantA.getMillis());
		
		Assert.assertTrue(userType.equals(instantA, instantB));
	}
	
	public void testEqualsDifferentObjectsDifferentValue() {
		Instant instantA = new Instant();
		Instant instantB = new Instant(instantA.getMillis() + 100);
		
		Assert.assertTrue(!userType.equals(instantA, instantB));
	}
	
	/**
	 * {@link Instant} implements Serializable, so the assemble/disassemble methods
	 * of the InstantUserType should always return the same YearMonthDay variable
	 * that is passed to them.
	 */
	public void testSerializable() {
		InstantUserType t = new InstantUserType();
		Instant i = new Instant();
		
		assertEquals(i, t.disassemble(i));
		assertEquals(i, t.assemble(i, null));
	}
}
