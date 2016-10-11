/*
 * Copyright (C) 2016 The SlimRoms Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.slim.settings.search;

import android.provider.SearchIndexableResource;

import com.slim.settings.fragments.*;
import com.slim.settings.R;

import java.util.Collection;
import java.util.HashMap;

public final class SlimSearchIndexableResources {

    public static int NO_DATA_RES_ID = 0;

    private static HashMap<String, SearchIndexableResource> sResMap = new HashMap<>();

    static {
        sResMap.put(InterfaceSettings.class.getName(),
                new SearchIndexableResource(
                    1,
                    R.xml.slim_interface_settings,
                    InterfaceSettings.class.getName(),
                    R.drawable.ic_settings_interface));
    }

    private SlimSearchIndexableResources() {
    }

    public static int size() {
        return sResMap.size();
    }

    public static SearchIndexableResource getResourceByName(String className) {
        return sResMap.get(className);
    }

    public static Collection<SearchIndexableResource> values() {
        return sResMap.values();
    }
}
