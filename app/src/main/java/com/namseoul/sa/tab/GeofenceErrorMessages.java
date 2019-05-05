/*
 * Copyright 2017 Google Inc. All Rights Reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.namseoul.sa.tab;

import android.content.Context;
import android.content.res.Resources;

import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.location.GeofenceStatusCodes;

/**
 * Geofence error codes mapped to error messages.
 */
class GeofenceErrorMessages {
    /**
     * Prevents instantiation.
     */
    private GeofenceErrorMessages() {}

    /**
     * Returns the error string for a geofencing exception.
     */
    public static String getErrorString(Context context, Exception e) {
        if (e instanceof ApiException) {
            return getErrorString(context, ((ApiException) e).getStatusCode());
        } else {
            return "알 수 없는 오류입니다.";
        }
    }

    /**
     * Returns the error string for a geofencing error code.
     */
    public static String getErrorString(Context context, int errorCode) {
        Resources mResources = context.getResources();
        switch (errorCode) {
            case GeofenceStatusCodes.GEOFENCE_NOT_AVAILABLE:
                return "not available";
            case GeofenceStatusCodes.GEOFENCE_TOO_MANY_GEOFENCES:
                return "너무 많은 지오팬스 입니다.";
            case GeofenceStatusCodes.GEOFENCE_TOO_MANY_PENDING_INTENTS:
                return "너무 많은 팬딩 인텐트 입니다.";
            default:
                return "알 수 없는 오류입니다.";
        }
    }
}
