/**
 * Copyright (c) 2010-2017 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.theweathercompany;

import java.util.Set;

import org.eclipse.smarthome.core.thing.ThingTypeUID;

import com.google.common.collect.ImmutableSet;

/**
 * The {@link TheWeatherCompanyBindingConstants} class defines common constants, which are
 * used across the whole binding.
 *
 * @author Peter Kristensen - Initial contribution
 */
public class TheWeatherCompanyBindingConstants {

    private static final String BINDING_ID = "theweathercompany";

    // List all Thing Type UIDs, related to 'The Weather Company' Binding
    public final static ThingTypeUID THING_TYPE_WEATHER = new ThingTypeUID(BINDING_ID, "weather");

    public final static Set<ThingTypeUID> SUPPORTED_THING_TYPES_UIDS = ImmutableSet.of(THING_TYPE_WEATHER);

    // List all configuration parameters
    public static final String PARAM_HOSTNAME = "hostname";
    public static final String PARAM_AUTHKEY = "authkey";
    public static final String PARAM_AUTHTOKEN = "authtoken";
    public static final String PARAM_LOCATION = "location";
    public static final String PARAM_REFRESH = "refresh";
    public static final String PARAM_SECURE = "secure";

    // List all channels
    public static final String CHANNEL_TEMPERATURE = "temperature";
    public static final String CHANNEL_MAX_TEMPERATURE = "maxTemp";
    public static final String CHANNEL_MIN_TEMPERATURE = "minTemp";
    public static final String CHANNEL_TEMPERATURE_TREND = "temperatureTrend";
    public static final String CHANNEL_HUMIDITY = "humidity";
    public static final String CHANNEL_TIMESTAMP = "timeStamp";
    public static final String CHANNEL_UNITS = "unit";
    public static final String CHANNEL_HUMIDEX = "humidex";
    public static final String CHANNEL_FEELS_LIKE = "feelsLike";
    public static final String CHANNEL_HEAT_INDEX = "heatIndex";
    public static final String CHANNEL_PRESSURE = "pressure";
    public static final String CHANNEL_UVINDEX = "uvindex";
    public static final String CHANNEL_DEWPOINT = "dewPoint";
    public static final String CHANNEL_DEWPOINT_DEPRESSION = "dewPointDepression";
    public static final String CHANNEL_WINDANGLE = "windAngle";
    public static final String CHANNEL_WINDSTRENGTH = "windStrength";
    public static final String CHANNEL_GUSTSTRENGTH = "gustStrength";
    public static final String CHANNEL_FORECAST_48HOUR = "forecast48hour";

    // The Weather Company properties
    public static final String TWC_TEMPERATURE = "temp";
    public static final String TWC_HUMIDITY = "rh";
    public static final String TWC_MAXTEMP = "max_temp";
    public static final String TWC_MINTEMP = "min_temp";
    public static final String TWC_TIMESTAMP = "rh";
    public static final String TWC_UNITS = "units";
    public static final String TWC_FEELS_LIKE = "feels_like";
    public static final String TWC_HEAT_INDEX = "heat_index";
    public static final String TWC_PRESSURE = "pressure";
    public static final String TWC_UVINDEX = "uv_index";
    public static final String TWC_DEWPOINT = "dewPt";
    public static final String TWC_WINDANGLE = "wdir";
    public static final String TWC_WINDSTRENGTH = "wspd";
    public static final String TWC_GUSTSTRENGTH = "gust";
}
