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

import com.liferay.calendar.CalendarEventDurationException;
import com.liferay.calendar.CalendarEventEndDateException;
import com.liferay.calendar.CalendarEventStartDateException;
import com.liferay.calendar.CalendarEventTitleException;
import com.liferay.calendar.model.CalendarBooking;
import com.liferay.calendar.model.CalendarEvent;
import com.liferay.calendar.service.base.CalendarEventLocalServiceBaseImpl;
import com.liferay.portal.kernel.exception.PortalException;
import com.liferay.portal.kernel.exception.SystemException;
import com.liferay.portal.kernel.util.LocaleUtil;
import com.liferay.portal.kernel.util.TimeZoneUtil;
import com.liferay.portal.kernel.util.Validator;
import com.liferay.portal.model.ResourceConstants;
import com.liferay.portal.model.User;
import com.liferay.portal.service.ServiceContext;
import com.liferay.portal.util.PortalUtil;

import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.Map;

/**
 * @author Eduardo Lundgren
 */
public class CalendarEventLocalServiceImpl
	extends CalendarEventLocalServiceBaseImpl {

	public CalendarEvent addCaledarEvent(
			long userId, Map<Locale, String> titleMap,
			Map<Locale, String> descriptionMap, String location,
			int startDateMonth, int startDateDay, int startDateYear,
			int startDateHour, int startDateMinute, int endDateMonth,
			int endDateDay, int endDateYear, int endDateHour, int endDateMinute,
			int durationHour, int durationMinute, boolean allDay,
			String recurrence, String type, int remindBy, int firstReminder,
			int secondReminder, ServiceContext serviceContext)
		throws PortalException, SystemException {

		// Calendar event

		User user = userPersistence.findByPrimaryKey(userId);
		long groupId = serviceContext.getScopeGroupId();
		Date now = new Date();

		if (allDay) {
			startDateHour = 0;
			startDateMinute = 0;
			endDateHour = 23;
			endDateMinute = 59;
			durationHour = 23;
			durationMinute = 59;
		}

		validate(
			titleMap, startDateMonth, startDateDay, startDateYear, endDateMonth,
			endDateDay, endDateYear, durationHour, durationMinute, allDay);

		Date startDate = PortalUtil.getDate(
			startDateMonth, startDateDay, startDateYear, startDateHour,
			startDateMinute, TimeZoneUtil.getDefault(), null);
		Date endDate = PortalUtil.getDate(
			endDateMonth, endDateDay, endDateYear, endDateHour, endDateMinute,
			TimeZoneUtil.getDefault(), null);

		long calendarEventId = counterLocalService.increment();

		CalendarEvent calendarEvent = calendarEventPersistence.create(
			calendarEventId);

		calendarEvent.setUuid(serviceContext.getUuid());
		calendarEvent.setGroupId(groupId);
		calendarEvent.setCompanyId(user.getCompanyId());
		calendarEvent.setUserId(user.getUserId());
		calendarEvent.setUserName(user.getFullName());
		calendarEvent.setCreateDate(serviceContext.getCreateDate(now));
		calendarEvent.setModifiedDate(serviceContext.getModifiedDate(now));
		calendarEvent.setTitleMap(titleMap);
		calendarEvent.setDescriptionMap(descriptionMap);
		calendarEvent.setLocation(location);
		calendarEvent.setStartDate(startDate);
		calendarEvent.setEndDate(endDate);
		calendarEvent.setDurationHour(durationHour);
		calendarEvent.setDurationMinute(durationMinute);
		calendarEvent.setAllDay(allDay);
		calendarEvent.setRecurrence(recurrence);
		calendarEvent.setType(type);
		calendarEvent.setRemindBy(remindBy);
		calendarEvent.setFirstReminder(firstReminder);
		calendarEvent.setSecondReminder(secondReminder);

		calendarEventPersistence.update(calendarEvent, false);

		// Resources

		if (serviceContext.getAddCommunityPermissions() ||
			serviceContext.getAddGuestPermissions()) {

			addCalendarEventResources(
				calendarEvent, serviceContext.getAddCommunityPermissions(),
				serviceContext.getAddGuestPermissions());
		}
		else {
			addCalendarEventResources(
				calendarEvent, serviceContext.getCommunityPermissions(),
				serviceContext.getGuestPermissions());
		}

		return calendarEvent;
	}

	public void addCalendarEventResources(
			CalendarEvent calendarEvent, boolean addCommunityPermissions,
			boolean addGuestPermissions)
		throws PortalException, SystemException {

		resourceLocalService.addResources(
			calendarEvent.getCompanyId(), calendarEvent.getGroupId(),
			calendarEvent.getUserId(), CalendarEvent.class.getName(),
			calendarEvent.getCalendarEventId(), false, addCommunityPermissions,
			addGuestPermissions);
	}

	public void addCalendarEventResources(
			CalendarEvent calendarEvent, String[] communityPermissions,
			String[] guestPermissions)
		throws PortalException, SystemException {

		resourceLocalService.addModelResources(
			calendarEvent.getCompanyId(), calendarEvent.getGroupId(),
			calendarEvent.getUserId(), CalendarEvent.class.getName(),
			calendarEvent.getCalendarEventId(), communityPermissions,
			guestPermissions);
	}

	public void deleteCalendarEvent(CalendarEvent calendarEvent)
		throws PortalException, SystemException {

		// Calendar event

		calendarEventPersistence.remove(calendarEvent);

		// Resources

		resourceLocalService.deleteResource(
			calendarEvent.getCompanyId(), CalendarEvent.class.getName(),
			ResourceConstants.SCOPE_INDIVIDUAL,
			calendarEvent.getCalendarEventId());

		// Calendar bookings

		deleteCalendarBookings(calendarEvent);
	}

	public void deleteCalendarEvent(long calendarEventId)
		throws PortalException, SystemException {

		CalendarEvent calendarEvent = calendarEventPersistence.findByPrimaryKey(
			calendarEventId);

		deleteCalendarEvent(calendarEvent);
	}

	public CalendarEvent getCalendarEvent(long calendarEventId)
		throws PortalException, SystemException {

		return calendarEventPersistence.findByPrimaryKey(calendarEventId);
	}

	public CalendarEvent updateCalendarEvent(
			long calendarEventId, Map<Locale, String> titleMap,
			Map<Locale, String> descriptionMap, String location,
			int startDateMonth, int startDateDay, int startDateYear,
			int startDateHour, int startDateMinute, int endDateMonth,
			int endDateDay, int endDateYear, int endDateHour, int endDateMinute,
			int durationHour, int durationMinute, boolean allDay,
			String recurrence, String type, int remindBy, int firstReminder,
			int secondReminder, ServiceContext serviceContext)
		throws PortalException, SystemException {

		// Calendar event

		if (allDay) {
			startDateHour = 0;
			startDateMinute = 0;
			endDateHour = 23;
			endDateMinute = 59;
			durationHour = 23;
			durationMinute = 59;
		}

		validate(
			titleMap, startDateMonth, startDateDay, startDateYear, endDateMonth,
			endDateDay, endDateYear, durationHour, durationMinute, allDay);

		Date startDate = PortalUtil.getDate(
			startDateMonth, startDateDay, startDateYear, startDateHour,
			startDateMinute, TimeZoneUtil.getDefault(), null);
		Date endDate = PortalUtil.getDate(
			endDateMonth, endDateDay, endDateYear, endDateHour, endDateMinute,
			TimeZoneUtil.getDefault(), null);

		CalendarEvent calendarEvent = calendarEventPersistence.findByPrimaryKey(
			calendarEventId);

		calendarEvent.setModifiedDate(serviceContext.getModifiedDate(null));
		calendarEvent.setTitleMap(titleMap);
		calendarEvent.setDescriptionMap(descriptionMap);
		calendarEvent.setLocation(location);
		calendarEvent.setStartDate(startDate);
		calendarEvent.setEndDate(endDate);
		calendarEvent.setDurationHour(durationHour);
		calendarEvent.setDurationMinute(durationMinute);
		calendarEvent.setAllDay(allDay);
		calendarEvent.setRecurrence(recurrence);
		calendarEvent.setType(type);
		calendarEvent.setRemindBy(remindBy);
		calendarEvent.setFirstReminder(firstReminder);
		calendarEvent.setSecondReminder(secondReminder);

		calendarEventPersistence.update(calendarEvent, false);

		// Resources

		if ((serviceContext.getCommunityPermissions() != null) ||
			(serviceContext.getGuestPermissions() != null)) {

			updateCalendarEventResources(
				calendarEvent, serviceContext.getCommunityPermissions(),
				serviceContext.getGuestPermissions());
		}

		// Calendar bookings

		calendarBookingLocalService.updateCalendarBookings(
			calendarEventId, serviceContext);

		return calendarEvent;
	}

	public void updateCalendarEventResources(
			CalendarEvent calendarEvent, String[] communityPermissions,
			String[] guestPermissions)
		throws PortalException, SystemException {

		resourceLocalService.updateResources(
			calendarEvent.getCompanyId(), calendarEvent.getGroupId(),
			CalendarEvent.class.getName(), calendarEvent.getCalendarEventId(),
			communityPermissions, guestPermissions);
	}

	protected void deleteCalendarBookings(CalendarEvent calendarEvent)
		throws PortalException, SystemException {

		List<CalendarBooking> calendarBookings =
			calendarBookingPersistence.findByCalendarEventId(
				calendarEvent.getCalendarEventId());

		for (CalendarBooking calendarBooking : calendarBookings) {
			calendarBookingLocalService.deleteCalendarBooking(calendarBooking);
		}
	}

	protected void validate(
			Map<Locale, String> titleMap, int startDateMonth, int startDateDay,
			int startDateYear, int endDateMonth, int endDateDay,
			int endDateYear, int durationHour, int durationMinute,
			boolean allDay)
		throws PortalException {

		Locale locale = LocaleUtil.getDefault();

		String title = titleMap.get(locale);

		if (Validator.isNull(title)) {
			throw new CalendarEventTitleException();
		}

		if (!Validator.isDate(startDateMonth, startDateDay, startDateYear)) {
			throw new CalendarEventStartDateException();
		}

		if (!Validator.isDate(endDateMonth, endDateDay, endDateYear)) {
			throw new CalendarEventEndDateException();
		}

		if (!allDay && (durationHour <= 0) && (durationMinute <= 0)) {
			throw new CalendarEventDurationException();
		}
	}

}