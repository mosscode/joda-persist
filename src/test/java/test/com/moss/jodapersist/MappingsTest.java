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

import java.sql.ResultSet;
import java.sql.Statement;
import java.util.TimeZone;

import junit.framework.Assert;
import junit.framework.TestCase;

import org.apache.log4j.BasicConfigurator;
import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.cfg.Configuration;
import org.joda.time.Chronology;
import org.joda.time.DateTimeZone;
import org.joda.time.Duration;
import org.joda.time.TimeOfDay;
import org.joda.time.YearMonthDay;
import org.joda.time.chrono.ZonedChronology;

import com.moss.jodapersist.NumericYearMonthDayUserType;

public class MappingsTest extends TestCase {
	private static final Logger log = Logger.getLogger(MappingsTest.class);
	private static final YearMonthDay YMD = new YearMonthDay(2006,5,1);
	
	private SessionFactory sessionFactory;
	private Session session;
	
	protected void setUp() throws Exception {
		Logger.getLogger("org.hibernate").setLevel(Level.WARN);
		Logger.getLogger("net.sf").setLevel(Level.OFF);
		BasicConfigurator.configure();
		
		Configuration cfg = new Configuration()
			.setProperty("hibernate.connection.driver_class","org.hsqldb.jdbcDriver")
			.setProperty("hibernate.dialect","org.hibernate.dialect.HSQLDialect")
			.setProperty("hibernate.connection.url","jdbc:hsqldb:mem:test")
			.setProperty("hibernate.connection.username","sa")
			.setProperty("hibernate.connection.password","")
			.setProperty("hibernate.connection.pool_size", "1")
			.setProperty("hibernate.show_sql","false")
			.setProperty("hibernate.hbm2ddl.auto","update")
			.addClass(PersistedDates.class)
			.addClass(NumericYearMonthDay.class)
			.addClass(PersistedTimesOfDay.class)
			.addClass(PersistedDuration.class);
		
		sessionFactory = cfg.buildSessionFactory();
		session = sessionFactory.openSession();
	}

	protected void tearDown() throws Exception {
		session.close();
		sessionFactory.close();
	}
	
	private void setupTimeOffsetData() {
		PersistedDates persistedDates = new PersistedDates();
		persistedDates.setYearMonthDay(YMD);
		persistedDates.setYearMonthDayNoon(YMD);
		session.beginTransaction();
		session.save(persistedDates);
		session.getTransaction().commit();
	}
	
	/**
	 * This test makes sure that the timeOffsets that YearMonthDayUserType
	 * supports are persisted correctly. 
	 */
	public void testTimeOffsets() throws Exception {
		setupTimeOffsetData();
		
		Statement s = session.connection().createStatement();
		ResultSet rs = s.executeQuery("select * from persisted_dates");
		
		rs.next();
		
		long expectedMidnightInstant = YMD.toDateTimeAtMidnight().toInstant().getMillis();
		long midnightInstant = rs.getTimestamp("ymd").getTime();
		
		assertEquals(expectedMidnightInstant, midnightInstant);

		long expectedNoonInstant = YMD.toDateTime(new TimeOfDay(12,0)).toInstant().getMillis();
		long noonInstant = rs.getTimestamp("noon_ymd").getTime();
		
		assertEquals(expectedNoonInstant, noonInstant);
	}
	
	private void setupNumericData() {
		NumericYearMonthDay numericYearMonthDay = new NumericYearMonthDay();
		numericYearMonthDay.setYearMonthDay(YMD);

		session.beginTransaction();
		session.save(numericYearMonthDay);
		session.getTransaction().commit();
	}
	
	public void testTimestampTimeOfDay() throws Exception {
		PersistedTimesOfDay ptod = new PersistedTimesOfDay();

		TimeZone timeZone = TimeZone.getTimeZone("EST");
		DateTimeZone zone = DateTimeZone.forTimeZone(timeZone);
		ptod.setTimestampTimeOfDay(new TimeOfDay(5, 30, 0, 002, ZonedChronology.getISO(zone)));
		
		session.beginTransaction();
		session.saveOrUpdate(ptod);
		session.flush();
		session.clear();
		session.getTransaction().commit();
		
		session.beginTransaction();
		TimeOfDay timeOfDay = (TimeOfDay) session.createQuery("select ptod.timestampTimeOfDay from " + 
					PersistedTimesOfDay.class.getName()  + " ptod where ptod.id=:id")
					.setParameter("id", ptod.getId())
					.uniqueResult();
		session.getTransaction().commit();
		
		assertEquals(002, timeOfDay.getMillisOfSecond());
		assertEquals(0, timeOfDay.getSecondOfMinute());
		assertEquals(30, timeOfDay.getMinuteOfHour());
		assertEquals(5, timeOfDay.getHourOfDay());
	}
	
	public void testNumericTimeOfDay() throws Exception {
		PersistedTimesOfDay ptod = new PersistedTimesOfDay();
		ptod.setTimestampTimeOfDay(new TimeOfDay(5, 30, 0, 002));
		
		session.beginTransaction();
		session.saveOrUpdate(ptod);
		session.flush();
		session.clear();
		session.getTransaction().commit();
		
		session.beginTransaction();
		TimeOfDay timeOfDay = (TimeOfDay) session.createQuery("select ptod.timestampTimeOfDay from " + 
					PersistedTimesOfDay.class.getName()  + " ptod where ptod.id=:id")
					.setParameter("id", ptod.getId())
					.uniqueResult();
		session.getTransaction().commit();
		
		assertEquals(5, timeOfDay.getHourOfDay());
		assertEquals(30, timeOfDay.getMinuteOfHour());
		assertEquals(0, timeOfDay.getSecondOfMinute());
		assertEquals(002, timeOfDay.getMillisOfSecond());
	}
	
	public void testDurationUserType() throws Exception {
		session.beginTransaction();
		
		PersistedDuration thousand = new PersistedDuration(new Duration(1000));
		PersistedDuration zero = new PersistedDuration(new Duration(0));
		PersistedDuration nulled = new PersistedDuration(null);
		
		session.save(thousand);
		session.save(zero);
		session.save(nulled);
		
		session.flush();
		session.getTransaction().commit();
		session.clear();
		
		session.beginTransaction();
		
		thousand = (PersistedDuration) session.get(PersistedDuration.class, thousand.id);
		zero = (PersistedDuration) session.get(PersistedDuration.class, zero.id);
		nulled = (PersistedDuration) session.get(PersistedDuration.class, nulled.id);
		
		session.getTransaction().commit();
		
		assertEquals(1000, thousand.duration.getMillis());
		assertEquals(0, zero.duration.getMillis());
		assertNull(nulled.duration);
	}
	
	/**
	 * 
	 */
	public void testNumericPersistence() throws Exception {
		setupNumericData();
		
		Statement s = session.connection().createStatement();
		ResultSet rs = s.executeQuery("select * from numericymd");
		
		rs.next();
		
		long expectedYmdLong = new NumericYearMonthDayUserType().yearMonthDayToLong(YMD);
		long ymdLong = rs.getLong("ymd");
		
		Assert.assertEquals(expectedYmdLong, ymdLong);
	}
}
