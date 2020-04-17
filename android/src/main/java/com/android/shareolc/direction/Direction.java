package com.android.shareolc.direction;

import com.android.shareolc.code.OpenLocationCode;
import java.util.Locale;


public class Direction {

    private final int mDistanceInMeter;

    private final float mInitialBearing;

    private final OpenLocationCode mFromCode;

    private final OpenLocationCode mToCode;

    public Direction(
            OpenLocationCode fromCode,
            OpenLocationCode toCode,
            float distanceInMeter,
            float initialBearing) {
        mDistanceInMeter = (int) distanceInMeter;
        mInitialBearing = initialBearing;
        mFromCode = fromCode;
        mToCode = toCode;
    }

    /**
     * @return Bearing in degrees East of true North.
     */
    public float getInitialBearing() {
        return mInitialBearing;
    }

    /**
     * @return Distance in meter.
     */
    public int getDistance() {
        return mDistanceInMeter;
    }

    /**
     * @return The layout_code representing the origin location.
     */
    public OpenLocationCode getFromCode() {
        return mFromCode;
    }

    /**
     * @return The layout_code representing the destination location.
     */
    public OpenLocationCode getToCode() {
        return mToCode;
    }

    @Override
    public String toString() {
        return String.format(
                Locale.US,
                "Direction from layout_code %s to %s, distance %d, initial bearing %f",
                mFromCode,
                mToCode,
                mDistanceInMeter,
                mInitialBearing);
    }
}
