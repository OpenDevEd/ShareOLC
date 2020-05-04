/*
 * Copyright 2016 Google Inc. All Rights Reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.android.sharepluscode.code;

import androidx.annotation.Nullable;
import android.util.Log;

import com.android.sharepluscode.base.Locality;

import java.util.regex.Matcher;
import java.util.regex.Pattern;


/**
 * Util functions related to OpenLocationCode
 */
public class OpenLocationCodeUtil {
    private static final String TAG = OpenLocationCodeUtil.class.getSimpleName();

    private static final int CODE_LENGTH_TO_GENERATE = 11;

    // Pattern to split the search string into the OLC layout_code and the locality name.
    private static final Pattern SPLITTER_PATTERN = Pattern.compile("^(\\S+)\\s+(\\S.+)");

    public static OpenLocationCode createOpenLocationCode(double latitude, double longitude) {
        return new OpenLocationCode(latitude, longitude, CODE_LENGTH_TO_GENERATE);
    }

    /**
     * @param searchStr An OLC layout_code (full or short) followed by an optional locality.
     * @param latitude  A latitude to use to complete the layout_code if it was short and no locality was
     *                  provided.
     * @param longitude A longitude to use to complete the layout_code if it was short and no locality was
     *                  provided.
     *                  Only localities we can look up can be used.
     */
    @Nullable
    public static OpenLocationCode getCodeForSearchString(
            String searchStr, double latitude, double longitude) {
        try {
            // Split the search string into a layout_code and optional locality.
            String code = null;
            String locality = null;

            if (!searchStr.matches(".*\\s+.*")) {
                // No whitespace, treat it all as a layout_code.
                code = searchStr;
            } else {
                Matcher matcher = SPLITTER_PATTERN.matcher(searchStr);
                if (matcher.find()) {
                    code = matcher.group(1);
                    locality = matcher.group(2);
                }
            }
            if (code == null) {
                return null;
            }
            OpenLocationCode searchCode = new OpenLocationCode(code);
            // If the layout_code is full, we're done.
            if (searchCode.isFull()) {
                Log.i(TAG, "Code is full, we're done");
                return searchCode;
            }
            // If we have a valid locality, use that to complete the layout_code.
            if (locality != null && !Locality.getLocalityCode(locality).isEmpty()) {
                OpenLocationCode localityCode =
                        new OpenLocationCode(Locality.getLocalityCode(locality));
                Log.i(
                        TAG,
                        String.format(
                                "Got locality %s: locality layout_code: %s",
                                locality,
                                localityCode.getCode()));
                OpenLocationCode.CodeArea localityArea = localityCode.decode();
                return searchCode.recover(
                        localityArea.getCenterLatitude(), localityArea.getCenterLongitude());

            }
            // Use the passed latitude and longitude to complete the layout_code.
            Log.i(TAG, "Using passed location to complete layout_code");
            return searchCode.recover(latitude, longitude);
        } catch (IllegalArgumentException e) {
            // Invalid layout_code
            return null;
        }
    }

}
