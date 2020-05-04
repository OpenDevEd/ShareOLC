package com.android.sharepluscode.direction;

import android.location.Location;
import com.android.sharepluscode.code.OpenLocationCode;
import com.android.sharepluscode.code.OpenLocationCodeUtil;


public class DirectionUtil {


    public static Direction getDirection(Location fromLocation, OpenLocationCode destinationCode,Location toLocation) {
        OpenLocationCode.CodeArea destinationArea = destinationCode.decode();
        //double toLatitude = destinationArea.getCenterLatitude();
        //double toLongitude = destinationArea.getCenterLongitude();

        double toLatitude = toLocation.getLatitude();
        double toLongitude = toLocation.getLongitude();

        float[] results = new float[3];
        Location.distanceBetween(
                fromLocation.getLatitude(),
                fromLocation.getLongitude(),
                toLatitude,
                toLongitude,
                results);

        // The device bearing in the location object is 0-360, the value returned from
        // distanceBetween is -180 to 180. Adjust the device bearing to be in the same range.
        float deviceBearing = fromLocation.getBearing();
        if (deviceBearing > 180) {
            deviceBearing = deviceBearing - 360;
        }

        // Compensate the initial bearing for the device bearing.
        results[1] = results[1] - deviceBearing;
        if (results[1] > 180) {
            results[1] = -360 + results[1];
        } else if (results[1] < -180) {
            results[1] = 360 + results[1];
        }

        return new Direction(
                OpenLocationCodeUtil.createOpenLocationCode(
                        fromLocation.getLatitude(), fromLocation.getLongitude()),
                destinationCode,
                results[0],
                results[1]);
    }

}
