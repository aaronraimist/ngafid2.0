package org.ngafid;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.InputStream;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.PreparedStatement;
import java.sql.SQLException;


import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

import org.ngafid.flights.FlightAlreadyExistsException;
import org.ngafid.flights.FatalFlightFileException;

import org.ngafid.flights.Flight;
import org.ngafid.flights.DoubleTimeSeries;
import org.ngafid.flights.StringTimeSeries;

import org.ngafid.events.Event;
import org.ngafid.events2.PitchEvent;

public class ProcessFileLatAc {
    // Lateral Acceleration: lateral_acceleration < -1.7 OR lateral_acceleration > 4.4
    static double minValue = -1.7;
    static double maxValue = 4.4;

    public static void testFile(String filename) {
        try {
            Flight flight = new Flight(filename, null);

            DoubleTimeSeries LatAcSeries = flight.getDoubleTimeSeries("LatAc");
            StringTimeSeries timeSeries = flight.getStringTimeSeries("Lcl Time");

            int startLineNo = -1;
            String startTime = null;
            int endLine= -1;
            String endTime = null;
            int count =0;

            List<Event> eventList = new ArrayList<>();

            int lineNumber = 0;
            int bufferTime = 5;
            // I set here to avoid memory issues
            double current;
            for (int i = 0; i < LatAcSeries.size(); i++) {
                lineNumber = i + 4;
                current = LatAcSeries.get(i);
                //String time = timeSeries.get(i);
                if (current < minValue || current > maxValue) {
                    if (startTime == null) {
                        startTime = timeSeries.get(i);
                        startLineNo = lineNumber;
                    }
                    // I am setting endLine and endTime in here because i want to check to see if the buffer condition is met or not
                    endLine = lineNumber;
                    System.out.println("LatAc in line: " + lineNumber + " Value: " + "[" + current + "]");

                    endTime = timeSeries.get(i);
                    count =0;
                } else {
                    // i am providing two condition to see which one meet. 
                    if (startTime != null)
                        count ++;
                    else
                        //if start time is null then i am making my count to zero
                        count = 0;
                    if (startTime != null) 
                        System.out.println("count: " + count + " with value: " + "[" + current + "]" + " in line: " + lineNumber );
                        
                    if (count == bufferTime){
                        System.err.println("Exceed the bufer range and New event created!!");
                    }

                    // in here i am saying if start time is not null and if the count number is hit the buffer or not 
                    if (startTime !=null && count == bufferTime){
                        Event event = new Event (startTime, endTime, startLineNo, endLine, 0){};
                        eventList.add(event);
                        startTime = null;
                        startLineNo = -1;
                        endLine = -1;
                        endTime = null;
                    }
                    // eventList.add(event);
                    // startTime = null;
                    // startLineNo = -1;
                }
            }

            if (startTime != null) {
                Event event = new Event(startTime, endTime , startLineNo, endLine, 0){};
                eventList.add( event );
            }
            System.out.println("");
            System.out.println("*****************************************************************************************");
            System.out.println("List of all the created Lateral Acceleration events with updated end-time and end-line");
            System.out.println("*****************************************************************************************");

            for( int i = 0; i < eventList.size(); i++ ){
                Event event = eventList.get(i);
                System.out.println( "Event : [line:" + event.getStartLine() + " to " + event.getEndLine() + ", time: " + event.getStartTime() + " to " + event.getEntTime() + "]" );
            }
            System.out.println("I am here");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void main(String[] arguments) {
        String filename = "/Users/fa3019/Code/ngafid2.0/example_data/C172/log_110812_095915_KCKN.csv";
        //String filename = "./example_data/C172/log_110812_095915_KCKN.csv";

        //File folder = new File("/Users/travisdesell/Data/ngafid/und_single_week/C172/N507ND/");

        //File folder = new File("/Users/fa3019/Data/UNDSingleWeekData/NGAFID_Data_Single_Week/C172/N507ND/");
        //File[] listOfFiles = folder.listFiles();

        long startMillis = System.currentTimeMillis();

        testFile(filename);

        // for (int i = 0; i < listOfFiles.length; i++) {
        //     if (listOfFiles[i].isFile()) {
        //     String filename = listOfFiles[i].toString();
        //     ////System.out.println(filename);

        //     testFile(filename);
        //     }
        // }

        long endMillis = System.currentTimeMillis();

        System.out.println("It took " + (endMillis - startMillis) + " ms to run the code");

        System.err.println("finished!");
    }

}
