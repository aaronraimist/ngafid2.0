package org.ngafid.events;

import java.util.ArrayList;

import org.ngafid.events.Event;

//Condition: Excessive Speed: indicated_airspeed > 154
public class PA28IndicatedAirspeedEvent extends Event {

    private static final int indicatedAirspeedColumn = 10;
    private static final double indicatedAirspeedLimit = 154;

    public PA28IndicatedAirspeedEvent(String startTime, String endTime, int startLine, int endLine) {
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
        return "INDICATED AIR SPEED EVENT " + super.toString();
    }
}