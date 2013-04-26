/**
 * Copyright (c) 2000-2013 Liferay, Inc. All rights reserved.
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

package com.liferay.weather.portlet;

import com.liferay.portal.kernel.util.Validator;
import com.liferay.weather.model.Weather;
import com.liferay.weather.util.WeatherUtil;

import java.io.IOException;

import java.util.ArrayList;
import java.util.List;

import javax.portlet.PortletPreferences;
import javax.portlet.PreferencesValidator;
import javax.portlet.ValidatorException;

/**
 * @author Brian Wing Shun Chan
 */
public class WeatherPreferencesValidator implements PreferencesValidator {

	public void validate(PortletPreferences preferences)
		throws ValidatorException {

		String apiKey = preferences.getValue("apiKey", null);

		validateAPIKey(apiKey);

		List<String> badZips = new ArrayList<String>();

		String[] zips = preferences.getValues("zips", new String[0]);

		for (String zip : zips) {
			Weather weather = WeatherUtil.getWeather(apiKey, zip);

			if (weather == null) {
				badZips.add(zip);
			}
		}

		if (badZips.size() > 0) {
			throw new ValidatorException(
				WeatherUtil.MESSAGE_INVALID_ZIP, badZips);
		}
	}

	public void validateAPIKey(String apiKey) throws ValidatorException {
		if (Validator.isNull(apiKey)) {
			throw new ValidatorException(WeatherUtil.MESSAGE_INVALID_KEY, null);
		}

		String responseBody;

		try {
			responseBody = WeatherUtil.getWeatherResponseBody(
				apiKey, WeatherUtil.DEFAULT_CITY_LONDON);
		} catch (IOException e) {
			throw new ValidatorException(
				WeatherUtil.MESSAGE_INVALID_NETWORK, null);
		}

		if (responseBody.equals("<h1>Developer Inactive</h1>")) {
			throw new ValidatorException(WeatherUtil.MESSAGE_INVALID_KEY, null);
		}
	}

}