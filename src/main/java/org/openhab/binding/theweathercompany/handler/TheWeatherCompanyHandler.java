/**
 * Copyright (c) 2010-2017 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.theweathercompany.handler;

import static org.openhab.binding.theweathercompany.TheWeatherCompanyBindingConstants.*;

import java.math.BigDecimal;
import java.security.KeyManagementException;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

import org.eclipse.smarthome.config.core.Configuration;
import org.eclipse.smarthome.config.core.status.ConfigStatusMessage;
import org.eclipse.smarthome.core.library.types.DecimalType;
import org.eclipse.smarthome.core.library.types.StringType;
import org.eclipse.smarthome.core.thing.ChannelUID;
import org.eclipse.smarthome.core.thing.Thing;
import org.eclipse.smarthome.core.thing.ThingStatus;
import org.eclipse.smarthome.core.thing.ThingStatusDetail;
import org.eclipse.smarthome.core.thing.binding.ConfigStatusThingHandler;
import org.eclipse.smarthome.core.types.Command;
import org.eclipse.smarthome.core.types.RefreshType;
import org.eclipse.smarthome.core.types.State;
import org.eclipse.smarthome.core.types.UnDefType;
import org.openhab.binding.theweathercompany.exception.TWCReSTException;
import org.openhab.binding.theweathercompany.internal.TWCAPIClient;
import org.openhab.binding.theweathercompany.internal.datatypes.Units;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

/**
 * The {@link TheWeatherCompanyHandler} is responsible for handling commands, which are
 * sent to one of the channels.
 *
 * @author Peter Kristensen - Initial contribution
 */
public class TheWeatherCompanyHandler extends ConfigStatusThingHandler {

    private final Logger logger = LoggerFactory.getLogger(TheWeatherCompanyHandler.class);

    private final int MAX_DATA_AGE = 3 * 60 * 60 * 1000; // 3h

    // How old can the weather conditions readings be before retrieving new data from The Weather Company
    private final int CACHE_EXPIRY = 20 * 1000; // 20s

    // Define how often a new 48 hour forecast is retrieved
    private final int FORECAST_EXPIRY = 1 * 60 * 60 * 1000; // 1h

    private long lastUpdateTime = 0;
    private long lastFcstTime = 0;

    private BigDecimal refresh;

    private JsonObject weatherData = new JsonObject();
    private JsonObject weatherFcst = new JsonObject();

    private Units units = null;

    private TWCAPIClient twcAPIClient = null;

    ScheduledFuture<?> refreshJob;

    public TheWeatherCompanyHandler(Thing thing) {
        super(thing);
    }

    @Override
    public void initialize() {
        logger.debug("Initializing The Weather Company handler.");

        Configuration config = getThing().getConfiguration();

        try {
            twcAPIClient = new TWCAPIClient(config);
        } catch (KeyManagementException e1) {
            e1.printStackTrace();
        } catch (NoSuchAlgorithmException e1) {
            e1.printStackTrace();
        }

        try {
            refresh = (BigDecimal) config.get(PARAM_REFRESH);
        } catch (Exception e) {
            logger.debug("Cannot set refresh parameter.", e);
        }

        if (refresh == null) {
            // let's go for the default
            refresh = new BigDecimal(60);
        }

        startAutomaticRefresh();

        updateStatus(ThingStatus.ONLINE);

    }

    @Override
    public void dispose() {
        refreshJob.cancel(true);
        twcAPIClient = null;
    }

    private void startAutomaticRefresh() {
        refreshJob = scheduler.scheduleAtFixedRate(() -> {
            try {
                boolean success = updateWeatherData();
                if (success) {
                    updateState(new ChannelUID(getThing().getUID(), CHANNEL_TEMPERATURE),
                            getWeatherState(TWC_TEMPERATURE));
                    updateState(new ChannelUID(getThing().getUID(), CHANNEL_MAX_TEMPERATURE),
                            getWeatherState(TWC_MAXTEMP));
                    updateState(new ChannelUID(getThing().getUID(), CHANNEL_MIN_TEMPERATURE),
                            getWeatherState(TWC_MINTEMP));
                    updateState(new ChannelUID(getThing().getUID(), CHANNEL_HUMIDITY), getWeatherState(TWC_HUMIDITY));
                    updateState(new ChannelUID(getThing().getUID(), CHANNEL_FEELS_LIKE),
                            getWeatherState(TWC_FEELS_LIKE));
                    updateState(new ChannelUID(getThing().getUID(), CHANNEL_HEAT_INDEX),
                            getWeatherState(TWC_HEAT_INDEX));
                    updateState(new ChannelUID(getThing().getUID(), CHANNEL_PRESSURE), getWeatherState(TWC_PRESSURE));
                    updateState(new ChannelUID(getThing().getUID(), CHANNEL_UVINDEX), getWeatherState(TWC_UVINDEX));
                    updateState(new ChannelUID(getThing().getUID(), CHANNEL_DEWPOINT), getWeatherState(TWC_DEWPOINT));
                    updateState(new ChannelUID(getThing().getUID(), CHANNEL_WINDANGLE), getWeatherState(TWC_WINDANGLE));
                    updateState(new ChannelUID(getThing().getUID(), CHANNEL_WINDSTRENGTH),
                            getWeatherState(TWC_WINDSTRENGTH));
                    updateState(new ChannelUID(getThing().getUID(), CHANNEL_GUSTSTRENGTH),
                            getWeatherState(TWC_GUSTSTRENGTH));
                }
            } catch (Exception e) {
                logger.debug("Exception occurred during execution: {}", e.getMessage(), e);
                updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.OFFLINE.COMMUNICATION_ERROR, e.getMessage());
            }
        }, 0, refresh.intValue(), TimeUnit.SECONDS);
    }

    @Override
    public void handleCommand(ChannelUID channelUID, Command command) {
        if (command instanceof RefreshType) {
            boolean success = updateWeatherData();
            if (success) {
                switch (channelUID.getId()) {
                    case CHANNEL_TEMPERATURE:
                        updateState(channelUID, getWeatherState(TWC_TEMPERATURE));
                        break;
                    case CHANNEL_MAX_TEMPERATURE:
                        updateState(channelUID, getWeatherState(TWC_MAXTEMP));
                        break;
                    case CHANNEL_MIN_TEMPERATURE:
                        updateState(channelUID, getWeatherState(TWC_MINTEMP));
                        break;
                    case CHANNEL_HUMIDITY:
                        updateState(channelUID, getWeatherState(TWC_HUMIDITY));
                        break;
                    case CHANNEL_PRESSURE:
                        updateState(channelUID, getWeatherState(TWC_PRESSURE));
                        break;
                    case CHANNEL_UVINDEX:
                        updateState(channelUID, getWeatherState(TWC_UVINDEX));
                        break;
                    case CHANNEL_DEWPOINT:
                        updateState(channelUID, getWeatherState(TWC_DEWPOINT));
                        break;
                    case CHANNEL_WINDANGLE:
                        updateState(channelUID, getWeatherState(TWC_WINDANGLE));
                        break;
                    case CHANNEL_WINDSTRENGTH:
                        updateState(channelUID, getWeatherState(TWC_WINDSTRENGTH));
                        break;
                    case CHANNEL_GUSTSTRENGTH:
                        updateState(channelUID, getWeatherState(TWC_GUSTSTRENGTH));
                        break;
                    case CHANNEL_UNITS:
                        updateState(channelUID, getUnits());
                        break;
                    case CHANNEL_FORECAST_48HOUR:
                        updateState(channelUID, getWeatherForecast());
                        break;
                    default:
                        logger.debug("Command received for an unknown channel: {}", channelUID.getId());
                        break;
                }
            }
        } else {
            logger.debug("Command {} is not supported for channel: {}", command, channelUID.getId());
        }
    }

    @Override
    public Collection<ConfigStatusMessage> getConfigStatus() {
        Collection<ConfigStatusMessage> configStatus = new ArrayList<>();

        return configStatus;

    }

    private synchronized boolean updateWeatherData() {
        JsonObject data;

        if (!isCacheExpired()) {
            // Cache has not expired and we have some data, just return and use existing data
            if (weatherData != null) {
                logger.debug("Continue use existing data {} < {}", lastUpdateTime + CACHE_EXPIRY,
                        System.currentTimeMillis());
                return true;
            }
        }

        try {
            data = twcAPIClient.getCurrentConditions();

            if (!data.has("observation")) {
                if (isCurrentDataExpired()) {
                    weatherData = null;
                    logger.trace(
                            "The Weather Company API did not return any data. Omiting the old result because it became too old.");
                    updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.OFFLINE.COMMUNICATION_ERROR,
                            "@text/offline.no-data");
                    return false;
                } else {
                    // simply keep the old data
                    logger.trace("The Weather Company API did not return any data. Keeping the old result.");
                    return false;
                }
            } else {
                lastUpdateTime = System.currentTimeMillis();
                JsonObject metaData = data.get("metadata").getAsJsonObject();

                setUnits(Units.valueOfString(metaData.get(TWC_UNITS).getAsString()));

                updateState(new ChannelUID(getThing().getUID(), CHANNEL_UNITS), getUnits());

                weatherData = data.get("observation").getAsJsonObject();

                if (isForecastExpired() || (weatherFcst == null)) {
                    weatherFcst = twcAPIClient.getHourlyForecast();
                    lastFcstTime = System.currentTimeMillis();

                    updateState(new ChannelUID(getThing().getUID(), CHANNEL_FORECAST_48HOUR), getWeatherForecast());
                }
            }
            updateStatus(ThingStatus.ONLINE);
            return true;
        } catch (TWCReSTException e) {
            logger.warn("Error accessing The Weather Company: {}");
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.OFFLINE.COMMUNICATION_ERROR,
                    "@text/offline.location [\"" + "latitude" + "," + "longitude" + "\"");
        }
        weatherData = null;
        return false;
    }

    private boolean isCurrentDataExpired() {
        return lastUpdateTime + MAX_DATA_AGE < System.currentTimeMillis();
    }

    private boolean isCacheExpired() {
        return lastUpdateTime + CACHE_EXPIRY < System.currentTimeMillis();
    }

    private boolean isForecastExpired() {
        return lastFcstTime + FORECAST_EXPIRY < System.currentTimeMillis();
    }

    private void setUnits(Units units) {
        this.units = units;
    }

    private State getUnits() {
        if (units != null) {
            return new StringType(units.get());
        } else {
            return UnDefType.UNDEF;
        }
    }

    private State getWeatherState(String parameter) {
        if (weatherData != null) {
            if (weatherData.has(parameter)) {
                JsonElement value = weatherData.get(parameter);
                if (!value.isJsonNull()) {
                    return new DecimalType(value.getAsBigDecimal());
                }
            }
        }
        return UnDefType.UNDEF;
    }

    public State getWeatherForecast() {
        if (weatherFcst != null) {
            return new StringType(weatherFcst.toString());
        }
        return UnDefType.UNDEF;
    }
}
