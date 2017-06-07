/**
 * Copyright (c) 2010-2017 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.theweathercompany.internal;

import java.util.Collections;
import java.util.Set;

import org.eclipse.smarthome.core.thing.Thing;
import org.eclipse.smarthome.core.thing.ThingTypeUID;
import org.eclipse.smarthome.core.thing.binding.BaseThingHandlerFactory;
import org.eclipse.smarthome.core.thing.binding.ThingHandler;
import org.openhab.binding.theweathercompany.TheWeatherCompanyBindingConstants;
import org.openhab.binding.theweathercompany.handler.TheWeatherCompanyHandler;

/**
 * The {@link TheWeatherCompanyHandlerFactory} is responsible for creating things and thing
 * handlers.
 *
 * @author Peter Kristensen - Initial contribution
 */
public class TheWeatherCompanyHandlerFactory extends BaseThingHandlerFactory {

    private final static Set<ThingTypeUID> SUPPORTED_THING_TYPES_UIDS = Collections
            .singleton(TheWeatherCompanyBindingConstants.THING_TYPE_WEATHER);

    @Override
    public boolean supportsThingType(ThingTypeUID thingTypeUID) {
        return SUPPORTED_THING_TYPES_UIDS.contains(thingTypeUID);
    }

    @Override
    protected ThingHandler createHandler(Thing thing) {
        ThingTypeUID thingTypeUID = thing.getThingTypeUID();

        if (thingTypeUID.equals(TheWeatherCompanyBindingConstants.THING_TYPE_WEATHER)) {
            return new TheWeatherCompanyHandler(thing);
        }

        return null;
    }
}
