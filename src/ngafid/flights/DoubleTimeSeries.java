package org.ngafid.flights;

import java.io.ByteArrayInputStream;
import java.io.DataInputStream;
import java.io.ObjectOutputStream;
import java.io.IOException;

import java.nio.ByteBuffer;

import java.sql.Blob;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import java.util.ArrayList;

import javax.sql.rowset.serial.SerialBlob;

public class DoubleTimeSeries {
    private String name;
    private String dataType;
    private ArrayList<Double> timeSeries;

    private double min = Double.MAX_VALUE;
    private int validCount;
    private double avg;
    private double max = -Double.MAX_VALUE;

    public DoubleTimeSeries(String name, String dataType) {
        this.name = name;
        this.dataType = dataType;
        timeSeries = new ArrayList<Double>();

        min = Double.NaN;
        avg = Double.NaN;
        max = Double.NaN;

        validCount = 0;
    }

    public DoubleTimeSeries(String name, String dataType, ArrayList<String> stringTimeSeries) {
        this.name = name;
        this.dataType = dataType;

        timeSeries = new ArrayList<Double>();

        int emptyValues = 0;
        avg = 0.0;
        validCount = 0;

        for (int i = 0; i < stringTimeSeries.size(); i++) {
            String currentValue = stringTimeSeries.get(i);
            if (currentValue.length() == 0) {
                //System.err.println("WARNING: double column '" + name + "' value[" + i + "] is empty.");
                timeSeries.add(Double.NaN);
                emptyValues++;
                continue;
            }
            double currentDouble = Double.parseDouble(stringTimeSeries.get(i));

            timeSeries.add(currentDouble);

            if (Double.isNaN(currentDouble)) continue;
            avg += currentDouble;
            validCount++;

            if (currentDouble > max) max = currentDouble;
            if (currentDouble < min) min = currentDouble;
        }

        if (emptyValues > 0) {
            //System.err.println("WARNING: double column '" + name + "' had " + emptyValues + " empty values.");
            if (emptyValues == stringTimeSeries.size()) {
                System.err.println("WARNING: double column '" + name + "' only had empty values.");
                min = Double.NaN;
                avg = Double.NaN;
                max = Double.NaN;
            }
        }

        avg /= validCount;
    }

    public DoubleTimeSeries(ResultSet resultSet) throws SQLException {
        int id = resultSet.getInt(1);
        int flightId = resultSet.getInt(2);
        name = resultSet.getString(3);
        int length = resultSet.getInt(4);
        validCount = resultSet.getInt(5);
        min = resultSet.getDouble(6);
        avg = resultSet.getDouble(7);
        max = resultSet.getDouble(8);

        Blob values = resultSet.getBlob(9);
        byte[] bytes = values.getBytes(1, (int)values.length());
        values.free();

        System.out.println("id: " + id + ", flightId: " + flightId + ", name: " + name + ", length: " + length + ", validLength: " + validCount + ", min: " + min + ", avg: " + avg + ", max: " + max);

        timeSeries = new ArrayList<Double>();
        try {
            DataInputStream inputStream = new DataInputStream(new ByteArrayInputStream(bytes));
            while (inputStream.available() > 0) {
                double d = inputStream.readDouble();
                timeSeries.add(d);
                //System.out.print(" " + d);
            }
            //System.out.println();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public String toString() {
        return "[DoubleTimeSeries '" + name + "' size: " + timeSeries.size() + ", validCount: " + validCount + ", min: " + min + ", avg: " + avg + ", max: " + max + "]";
    }


    public void add(double d) {
        if (Double.isNaN(d)) {
            timeSeries.add(d);
            return;
        }

        if (validCount == 0) {
            min = Double.MAX_VALUE;
            max = -Double.MAX_VALUE;
            avg = 0;

            timeSeries.add(d);
            avg = d;
            max = d;
            min = d;
            validCount = 1;
        } else {
            timeSeries.add(d);

            if (d > max) max = d;
            if (d < min) min = d;

            avg = avg * ((double)validCount / (double)(validCount + 1)) + (d / (double)(validCount + 1));

            validCount++;
        }
    }

    public double get(int i) {
        return timeSeries.get(i);
    }

    public String getDataType() {
        return dataType;
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
            PreparedStatement preparedStatement = connection.prepareStatement("INSERT INTO double_series (flight_id, name, data_type, length, valid_length, min, avg, max, `values`) VALUES (?, ?, ?, ?, ?, ?, ?, ?)");

            preparedStatement.setInt(1, flightId);
            preparedStatement.setString(2, name);
            preparedStatement.setString(3, dataType);

            preparedStatement.setInt(4, timeSeries.size());
            preparedStatement.setInt(5, validCount);

            if (Double.isNaN(min)) {
                preparedStatement.setNull(6, java.sql.Types.DOUBLE);
            } else {
                preparedStatement.setDouble(6, min);
            }

            if (Double.isNaN(avg)) {
                preparedStatement.setNull(7, java.sql.Types.DOUBLE);
            } else {
                preparedStatement.setDouble(7, avg);
            }

            if (Double.isNaN(max)) {
                preparedStatement.setNull(8, java.sql.Types.DOUBLE);
            } else {
                preparedStatement.setDouble(8, max);
            }


            ByteBuffer byteBuffer = ByteBuffer.allocate(timeSeries.size() * 8);
            for (int i = 0; i < timeSeries.size(); i++) {
                byteBuffer.putDouble(timeSeries.get(i));
            }
            byte[] byteArray = byteBuffer.array();

            System.err.println(preparedStatement);

            Blob seriesBlob = new SerialBlob(byteArray);

            preparedStatement.setBlob(9, seriesBlob);
            preparedStatement.executeUpdate();
            preparedStatement.close();

        } catch (SQLException e) {
            e.printStackTrace();
            System.exit(1);
        }
    }
}
