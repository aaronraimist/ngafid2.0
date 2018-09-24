package org.ngafid.events;

import java.util.ArrayList;

import org.ngafid.events.Event;
//Condition: Low Oil Pressure: eng_1_oil_press < 20 AND eng_1_rpm > 100
public class C182LowOilPressureEvent extends Event {

    private static final int c182LowOilPressurePressColumn = 27;
    private static final int c182LowOilPressureRpmColumn = 28;
    private static final double c182LowOilPressurePressLimit = 20;
    private static final double c182LowOilPressureRpmLimit = 100;

    public C182LowOilPressureEvent(String startTime, String endTime, int startLine, int endLine) {
        super(startTime, endTime, startLine, endLine, 10);
    }

    public static boolean isOccuring(ArrayList<String> lineValues) {
        double c182LowOilPressurePress = Double.parseDouble(lineValues.get(c182LowOilPressurePressColumn));
        double c182LowOilPressureRpm = Double.parseDouble(lineValues.get(c182LowOilPressureRpmColumn));

        if (c182LowOilPressurePress < c182LowOilPressurePressLimit && c182LowOilPressureRpm > c182LowOilPressureRpmLimit) {
            return true;
        } else {
            return false;
        }
    }

    public String toString() {
        return "LOW OIL PRESSURE EVENT " + super.toString();
    }
}