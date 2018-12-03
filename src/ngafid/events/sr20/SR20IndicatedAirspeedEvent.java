package org.ngafid.events.sr20;

import java.util.ArrayList;

import org.ngafid.events.Event;

//Condition: Excessive Speed: indicated_airspeed > 200
public class SR20IndicatedAirspeedEvent extends Event {

    private static final int indicatedAirspeedColumn = 10;
    private static final double indicatedAirspeedLimit = 200;

    public SR20IndicatedAirspeedEvent(String startTime, String endTime, int startLine, int endLine) {
        super(startTime, endTime, startLine, endLine, 5);
    }

    public static boolean isOccuring(ArrayList<String> lineValues) {
        double indicatedAirspeed = Double.parseDouble(lineValues.get(indicatedAirspeedColumn));

        if (indicatedAirspeed > indicatedAirspeedLimit) {
            return true;
        } else {
            return false;
        }
    }

    public String toString() {
        return "SR20 INDICATED AIR SPEED EVENT " + super.toString();
    }
}