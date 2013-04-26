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

package com.liferay.weather.util;

import com.liferay.portal.kernel.util.HttpUtil;
import com.liferay.portal.kernel.util.StringBundler;
import com.liferay.portal.kernel.util.StringPool;
import com.liferay.portal.kernel.webcache.WebCacheItem;
import com.liferay.portal.kernel.webcache.WebCachePoolUtil;
import com.liferay.weather.model.Weather;

import java.io.IOException;

/**
 * @author Brian Wing Shun Chan
 */
public class WeatherUtil {

	public static final String DEFAULT_CITY_LONDON = "London";

	public static final String MESSAGE_INVALID_KEY = "invalid api key";

	public static final String MESSAGE_INVALID_NETWORK = "invalid network";

	public static final String MESSAGE_INVALID_ZIP =
		"invalid cities or zip codes";

	public static Weather getWeather(String apiKey, String zip) {
		StringBundler sb = new StringBundler(5);

		sb.append(WeatherUtil.class.getName());
		sb.append(StringPool.PERIOD);
		sb.append(apiKey);
		sb.append(StringPool.PERIOD);
		sb.append(zip);

		String key = sb.toString();

		WebCacheItem wci = new WeatherWebCacheItem(apiKey, zip);

		try {
			return (Weather)WebCachePoolUtil.get(key, wci);
		}
		catch (ClassCastException cce) {
			WebCachePoolUtil.remove(key);

			return (Weather)WebCachePoolUtil.get(key, wci);
		}
	}

	public static String getWeatherResponseBody(String apiKey, String zip)
		throws IOException {

		StringBundler sb = new StringBundler(5);

		sb.append(
			"http://api.worldweatheronline.com/free/v1/weather.ashx?key=");
		sb.append(apiKey);
		sb.append("&q=");
		sb.append(HttpUtil.encodeURL(zip));
		sb.append("&format=xml");

		return HttpUtil.URLtoString(sb.toString());
	}

}