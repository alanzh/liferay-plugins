/**
 * Copyright (c) 2000-2011 Liferay, Inc. All rights reserved.
 *
 * This library is free software; you can redistribute it and/or modify it under
 * the terms of the GNU Lesser General Public License as published by the Free
 * Software Foundation; either version 2.1 of the License, or (at your option)
 * any later version.
 *
 * This library is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. See the GNU Lesser General Public License for more
 * details.
 */

package com.liferay.calendar.service.impl;

import com.liferay.calendar.model.CalendarBooking;
import com.liferay.calendar.model.CalendarEvent;
import com.liferay.calendar.model.CalendarResource;
import com.liferay.calendar.service.base.CalendarBookingLocalServiceBaseImpl;
import com.liferay.calendar.util.CalendarUtil;
import com.liferay.portal.kernel.dao.orm.Disjunction;
import com.liferay.portal.kernel.dao.orm.DynamicQuery;
import com.liferay.portal.kernel.dao.orm.DynamicQueryFactoryUtil;
import com.liferay.portal.kernel.dao.orm.Junction;
import com.liferay.portal.kernel.dao.orm.Property;
import com.liferay.portal.kernel.dao.orm.PropertyFactoryUtil;
import com.liferay.portal.kernel.dao.orm.RestrictionsFactoryUtil;
import com.liferay.portal.kernel.exception.PortalException;
import com.liferay.portal.kernel.exception.SystemException;
import com.liferay.portal.kernel.util.OrderByComparator;
import com.liferay.portal.kernel.util.StringPool;
import com.liferay.portal.kernel.util.Validator;
import com.liferay.portal.model.User;
import com.liferay.portal.service.ServiceContext;
import com.liferay.portal.util.PortalUtil;

import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author Eduardo Lundgren
 */
public class CalendarBookingLocalServiceImpl
	extends CalendarBookingLocalServiceBaseImpl {

	public CalendarBooking addCalendarBooking(
			long userId, long calendarEventId, long calendarResourceId,
			boolean required, ServiceContext serviceContext)
		throws PortalException, SystemException {

		User user = userPersistence.findByPrimaryKey(userId);
		long groupId = serviceContext.getScopeGroupId();
		CalendarEvent calendarEvent = calendarEventPersistence.findByPrimaryKey(
			calendarEventId);
		CalendarResource calendarResource =
			calendarResourcePersistence.findByPrimaryKey(calendarResourceId);
		Date now = new Date();

		long calendarBookingId = counterLocalService.increment();

		CalendarBooking calendarBooking = calendarBookingPersistence.create(
			calendarBookingId);

		calendarBooking.setUuid(serviceContext.getUuid());
		calendarBooking.setGroupId(groupId);
		calendarBooking.setCompanyId(user.getCompanyId());
		calendarBooking.setUserId(user.getUserId());
		calendarBooking.setUserName(user.getFullName());
		calendarBooking.setCreateDate(serviceContext.getCreateDate(now));
		calendarBooking.setModifiedDate(serviceContext.getModifiedDate(now));
		calendarBooking.setCalendarEventId(calendarEventId);
		calendarBooking.setCalendarResourceId(calendarResourceId);
		calendarBooking.setClassNameId(calendarResource.getClassNameId());
		calendarBooking.setClassPK(calendarResource.getClassPK());
		calendarBooking.setTitleMap(calendarEvent.getTitleMap());
		calendarBooking.setNameMap(calendarResource.getNameMap());
		calendarBooking.setDescriptionMap(calendarEvent.getDescriptionMap());
		calendarBooking.setLocation(calendarEvent.getLocation());
		calendarBooking.setStartDate(calendarEvent.getStartDate());
		calendarBooking.setEndDate(calendarEvent.getEndDate());
		calendarBooking.setDurationHour(calendarEvent.getDurationHour());
		calendarBooking.setDurationMinute(calendarEvent.getDurationMinute());
		calendarBooking.setRecurrence(calendarEvent.getRecurrence());
		calendarBooking.setType(calendarEvent.getType());
		calendarBooking.setRequired(required);

		calendarBookingPersistence.update(calendarBooking, false);

		return calendarBooking;
	}

	public void deleteCalendarBooking(CalendarBooking calendarBooking)
		throws PortalException, SystemException {

		calendarBookingPersistence.remove(calendarBooking);
	}

	public void deleteCalendarBooking(long calendarBookingId)
		throws PortalException, SystemException {

		CalendarBooking calendarBooking =
			calendarBookingPersistence.findByPrimaryKey(calendarBookingId);

		deleteCalendarBooking(calendarBooking);
	}

	public CalendarBooking getCalendarBooking(long calendarBookingId)
		throws PortalException, SystemException {

		return calendarBookingPersistence.findByPrimaryKey(calendarBookingId);
	}

	public List<CalendarBooking> getCalendarBookings(
			String className, long classPK, int start, int end,
			OrderByComparator orderByComparator)
		throws SystemException {

		long classNameId = PortalUtil.getClassNameId(className);

		return calendarBookingPersistence.findByC_C(
			classNameId, classPK, start, end, orderByComparator);
	}

	public int getCalendarBookingsCount(
			String className, long classPK)
		throws SystemException {

		long classNameId = PortalUtil.getClassNameId(className);

		return calendarBookingPersistence.countByC_C(
			classNameId, classPK);
	}

	public List<CalendarBooking> getCalendarEventCalendarBookings(
			long calendarEventId, int start, int end,
			OrderByComparator orderByComparator)
		throws SystemException {

		return calendarBookingPersistence.findByCalendarEventId(
			calendarEventId, start, end, orderByComparator);
	}

	public int getCalendarEventCalendarBookingsCount(long calendarEventId)
		throws SystemException {

		return calendarBookingPersistence.countByCalendarEventId(
			calendarEventId);
	}

	public List<CalendarBooking> getCalendarResourceCalendarBookings(
			long calendarResourceId, int start, int end,
			OrderByComparator orderByComparator)
		throws SystemException {

		return calendarBookingPersistence.findByCalendarResourceId(
			calendarResourceId, start, end, orderByComparator);
	}

	public int getCalendarResourceCalendarBookingsCount(long calendarResourceId)
		throws SystemException {

		return calendarBookingPersistence.countByCalendarResourceId(
			calendarResourceId);
	}

	public List<CalendarBooking> search(
			long calendarResourceId, String title, String description,
			String type, boolean andOperator, int start, int end,
			OrderByComparator orderByComparator)
		throws SystemException {

		DynamicQuery dynamicQuery = buildDynamicQuery(
			calendarResourceId, title, description, type, andOperator);

		return dynamicQuery(dynamicQuery, start, end, orderByComparator);
	}

	public int searchCount(
			long calendarResourceId, String title, String description,
			String type, boolean andOperator)
		throws SystemException {

		DynamicQuery dynamicQuery = buildDynamicQuery(
			calendarResourceId, title, description, type, andOperator);

		return (int)dynamicQueryCount(dynamicQuery);
	}

	public CalendarBooking updateCalendarBooking(
			long calendarBookingId, boolean required,
			ServiceContext serviceContext)
		throws PortalException, SystemException {

		CalendarBooking calendarBooking =
			calendarBookingPersistence.findByPrimaryKey(calendarBookingId);

		calendarBooking.setModifiedDate(serviceContext.getModifiedDate(null));
		calendarBooking.setRequired(required);

		calendarBookingPersistence.update(calendarBooking, false);

		return calendarBooking;
	}

	public CalendarBooking updateCalendarBooking(
			long calendarBookingId, long calendarEventId,
			ServiceContext serviceContext)
		throws PortalException, SystemException {

		CalendarEvent calendarEvent = calendarEventPersistence.findByPrimaryKey(
			calendarEventId);

		CalendarBooking calendarBooking =
			calendarBookingPersistence.findByPrimaryKey(calendarBookingId);

		calendarBooking.setModifiedDate(serviceContext.getModifiedDate(null));
		calendarBooking.setCalendarEventId(calendarEventId);
		calendarBooking.setTitleMap(calendarEvent.getTitleMap());
		calendarBooking.setDescriptionMap(calendarEvent.getDescriptionMap());
		calendarBooking.setLocation(calendarEvent.getLocation());
		calendarBooking.setStartDate(calendarEvent.getStartDate());
		calendarBooking.setEndDate(calendarEvent.getEndDate());
		calendarBooking.setDurationHour(calendarEvent.getDurationHour());
		calendarBooking.setDurationMinute(calendarEvent.getDurationMinute());
		calendarBooking.setRecurrence(calendarEvent.getRecurrence());
		calendarBooking.setType(calendarEvent.getType());

		calendarBookingPersistence.update(calendarBooking, false);

		return calendarBooking;
	}

	public void updateCalendarBookings(
			long calendarEventId, ServiceContext serviceContext)
		throws PortalException, SystemException {

		List<CalendarBooking> calendarBookings =
			calendarBookingPersistence.findByCalendarEventId(calendarEventId);

		for (CalendarBooking calendarBooking : calendarBookings) {
			updateCalendarBooking(
				calendarBooking.getCalendarBookingId(), calendarEventId,
				serviceContext);
		}
	}

	protected DynamicQuery buildDynamicQuery(
		long calendarResourceId, String title, String description, String type,
		boolean andOperator) {

		Junction junction = null;

		if (andOperator) {
			junction = RestrictionsFactoryUtil.conjunction();
		}
		else {
			junction = RestrictionsFactoryUtil.disjunction();
		}

		Map<String, String> terms = new HashMap<String, String>();

		if (Validator.isNotNull(title)) {
			terms.put("title", title);
		}

		if (Validator.isNotNull(description)) {
			terms.put("description", description);
		}

		if (Validator.isNotNull(type)) {
			terms.put("type", type);
		}

		for (Map.Entry<String, String> entry : terms.entrySet()) {
			Disjunction disjunction = RestrictionsFactoryUtil.disjunction();

			for (String s : CalendarUtil.splitKeywords(entry.getValue())) {
				String value = StringPool.PERCENT + s + StringPool.PERCENT;

				disjunction.add(
					RestrictionsFactoryUtil.ilike(entry.getKey(), value));
			}

			junction.add(disjunction);
		}

		DynamicQuery dynamicQuery = DynamicQueryFactoryUtil.forClass(
			CalendarBooking.class, getClass().getClassLoader());

		if (calendarResourceId > 0) {
			Property property = PropertyFactoryUtil.forName(
				"calendarResourceId");

			dynamicQuery.add(property.eq(calendarResourceId));
		}

		return dynamicQuery.add(junction);
	}

}