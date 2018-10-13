package org.ngafid.flights;

import java.io.ObjectOutputStream;
import java.io.IOException;

import java.sql.Blob;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;

import java.util.ArrayList;

public class StringTimeSeries {
    private String name;
    private ArrayList<String> timeSeries;
    private int validCount;


    public StringTimeSeries(String name) {
        this.name = name;
        this.timeSeries = new ArrayList<String>();

        validCount = 0;
    }

    public StringTimeSeries(String name, ArrayList<String> timeSeries) {
        this.name = name;
        this.timeSeries = timeSeries;

        validCount = 0;
        for (int i = 0; i < timeSeries.size(); i++) {
            if (!timeSeries.get(i).equals("")) {
                validCount++;
            }
        }
    }

    public String toString() {
        return "[StringTimeSeries '" + name + "' size: " + timeSeries.size() + ", validCount: " + validCount + "]";
    }

    public void add(String s) {
        if (!s.equals("")) validCount++;
        timeSeries.add(s);
    }

    public String get(int i) {
        return timeSeries.get(i);
    }

    public String getFirstValid() {
        int position = 0;
        while (position < timeSeries.size()) {
            String current = timeSeries.get(position);
            if (current.equals("")) {
                position++;
            } else {
                return current;
            }
        }
        return null;
    }

    public String getLastValid() {
        int position = timeSeries.size() - 1;
        while (position >= 0) {
            String current = timeSeries.get(position);
            if (current.equals("")) {
                position--;
            } else {
                return current;
            }
        }
        return null;
    }

    public int size() {
        return timeSeries.size();
    }

    public int validCount() {
        return validCount;
    }

    public void updateDatabase(Connection connection, int flightId) {
        //System.out.println("Updating database for " + this);

        try {
            PreparedStatement preparedStatement = connection.prepareStatement("INSERT INTO string_series (flight_id, name, length, valid_length, `values`) VALUES (?, ?, ?, ?, ?)");

            preparedStatement.setInt(1, flightId);
            preparedStatement.setString(2, name);
            preparedStatement.setInt(3, timeSeries.size());
            preparedStatement.setInt(4, validCount);

            Blob seriesBlob = connection.createBlob();
            final ObjectOutputStream oos = new ObjectOutputStream(seriesBlob.setBinaryStream(1));
            for (int i = 0; i < timeSeries.size(); i++) {
                if (timeSeries.get(i) == null || timeSeries.get(i).length() == 0) {
                    oos.writeInt(0);
                } else {
                    oos.writeInt(timeSeries.get(i).length());
                    oos.writeChars(timeSeries.get(i));
                }
            }
            oos.close();

            System.err.println(preparedStatement);

            preparedStatement.setBlob(5, seriesBlob);
            preparedStatement.executeUpdate();
            preparedStatement.close();

        } catch (SQLException e) {
            e.printStackTrace();
            System.exit(1);
        } catch (IOException e) {
            e.printStackTrace();
            System.exit(1);
        }
    }

}
