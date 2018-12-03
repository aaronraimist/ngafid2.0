package org.ngafid.events2;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import org.ngafid.flights.DoubleTimeSeries;


public class RollEvent {

    public static void calculateEvents(Connection connection) {
        try {
            String columnName = "Roll";
            double minValue = -60.0;
            double maxValue = 60.0;

            //grab the flight IDs that can have a Roll event
            String flightsQuery = "SELECT flights.id FROM flights WHERE EXISTS (SELECT double_series.id FROM double_series WHERE flights.id = double_series.flight_id AND (double_series.name = ? AND (double_series.min <= ? OR double_series.max >= ?)))";
            PreparedStatement flightsPS = connection.prepareStatement(flightsQuery);

            flightsPS.setString(1, columnName);
            flightsPS.setDouble(2, minValue);
            flightsPS.setDouble(3, maxValue);
            System.out.println(flightsPS);

            ResultSet flightsRS = flightsPS.executeQuery();
            while (flightsRS.next()) {
                //get the ID from each flight that had a Roll event
                int flightId = flightsRS.getInt(1);

                //grab the Roll double_series for that flight
                String seriesQuery = "SELECT id, flight_id, name, length, valid_length, min, avg, max, `values` FROM double_series WHERE flight_id = ? AND name = ?";

                PreparedStatement seriesPS = connection.prepareStatement(seriesQuery);

                seriesPS.setInt(1, flightId);
                seriesPS.setString(2, columnName);
                System.out.println(seriesPS);

                ResultSet resultSet = seriesPS.executeQuery();

                while (resultSet.next()) {
                    DoubleTimeSeries timeSeries = new DoubleTimeSeries(resultSet);

                    for (int i = 0; i < timeSeries.size(); i++) {
                        double current = timeSeries.get(i);
                        if (Double.isNaN(current)) continue;

                        if (current < minValue || current > maxValue) {
                            System.err.println("Roll exceedence at entry: " + i);

                        }
                    }
                }

                System.out.println();
                System.out.println();
            }

            System.err.println("finished!");
        } catch (SQLException e) {
            e.printStackTrace();
            System.exit(1);
        }
    }
}