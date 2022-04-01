package muppets.ext.fob;

/**
 * StandAloneFOB.java
 *
 * A Java Interface to the Ascension Flock of Birds (FOB) interface.
 * Copyright (C) Damian Eads. 2004-2006. All Rights Reserved.
 * 
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; either version 2
 * of the License, or (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA  
 * 02110-1301, USA.
 */

import java.io.*;

/**
 * This class interfaces with an Ascension Flock of Birds unit only in standalone mode.
 * In standalone mode, only one sensor may be used, and its address must be set to zero.
 * The unit operates at a baud rate of 115200 bps on COM1 (2000 ms read/write timeout) by
 * default. The DIP switches should be set to:
 * <pre>
 *   Switches:       Configurations:
 *   -------------------------------
 *   12345678        U=Up   (Off)
 *   DDDUUUUU        D=Down (On)
 * </pre>
 *
 * To open the flock of birds with the default settings:
 * <pre>
 *   StandAloneFOB.open();           // open a connection to the unit.
 *   StandAloneFOB.setDataFormat(StandAloneFOB.DATA_FORMAT_POSITION);
 *   StandAloneFOB.startStreaming(); // wake up the unit
 *   while ( someCondition ) {
 *       StandAloneFOB.refresh();
 *       System.err.println( "x = " + StandAlone.getScaledX() +
 *                           " y = " + StandAlone.getScaledY() +
 *                           " z = " + StandAlone.getScaledZ() ); 
 *   }
 *   StandAloneFOB.stopStreaming();  // allow the unit to sleep.
 *   StandAloneFOB.close();          // close the unit.
 * </pre>
 *
 * @author Damian Eads
 */
 
public class StandAloneFOB {
        static { // Load the MuppetsFOB library.
               System.loadLibrary("MuppetsFOB");
        }
	/** A static constant indicating that no data should be sent between the bird unit
	    and the receiving unit. */
	public static final int DATA_FORMAT_NO_DATA = 0;
	
	/** A static constant indicating that only position data should be sent between the bird unit
	    and the receiving unit. */
	public static final int DATA_FORMAT_POSITION = 1;
	
	/** A static constant indicating that only angle data should be sent between the bird unit
	    and the receiving unit. */
	public static final int DATA_FORMAT_ANGLES = 2;
	
	/** A static constant indicating that only a matrix data should be sent between the bird unit
	    and the receiving unit. */
	public static final int DATA_FORMAT_MATRIX = 3;
	
	/** A static constant indicating that position and angle data should be sent between the
	    bird unit and the receiving unit. */
	public static final int DATA_FORMAT_POSITION_AND_ANGLES = 4;
	
	/** A static constant indicating that position and matrix data should be sent between the
	    bird unit and the receiving unit. */
	public static final int DATA_FORMAT_POSITION_AND_MATRIX = 5;
	
	/** A static constant indicating that only quaternion data should be sent between the
	    bird unit and the receiving unit. */
	public static final int DATA_FORMAT_QUATERNION = 7;
	
	/** A static constant indicating that only quaternion and position data should be sent
	    between the bird unit and the receiving unit. */
	public static final int DATA_FORMAT_POSITION_AND_QUATERNION = 8;
        /** The index of the x coordinate in the data frame. */
	public static final int DATA_X = 0;
	
	/** The index of the y coordinate in the data frame. */
	public static final int DATA_Y = 1;
	
	/** The index of the z coordinate in the data frame. */
	public static final int DATA_Z = 2;
	
	/** The index of the azimuth in the data frame. */
	public static final int DATA_AZIMUTH = 3;
	
	/** The index of the elevation in the data frame. */
	public static final int DATA_ELEVATION = 4;
	
	/** The index of the roll in the data frame. */
	public static final int DATA_ROLL = 5;
	
	/** The index of the first component of the quaternion in the data frame. */
	public static final int DATA_Q0 = 6;
	
	/** The index of the second component of the quaternion in the data frame. */
	public static final int DATA_Q1 = 7;
	
	/** The index of the third component of the quaternion in the data frame. */
	public static final int DATA_Q2 = 8;
	
	/** The index of the fourth component of the quaternion in the data frame. */
	public static final int DATA_Q3 = 9;
	
	/** The index of the entry (i=1, j=1) in the matrix. */
	public static final int DATA_M11 = 10;
	
	/** The index of the entry (i=1, j=2) in the matrix. */
	public static final int DATA_M12 = 11;
	
	/** The index of the entry (i=1, j=3) in the matrix. */
	public static final int DATA_M13 = 12;
	
	/** The index of the entry (i=2, j=1) in the matrix. */
	public static final int DATA_M21 = 13;
	
	/** The index of the entry (i=2, j=2) in the matrix. */
	public static final int DATA_M22 = 14;
	
	/** The index of the entry (i=2, j=3) in the matrix. */
	public static final int DATA_M23 = 15;
	
	/** The index of the entry (i=3, j=1) in the matrix. */
	public static final int DATA_M31 = 16;
	
	/** The index of the entry (i=3, j=2) in the matrix. */
	public static final int DATA_M32 = 17;
	
	/** The index of the entry (i=3, j=3) in the matrix. */
	public static final int DATA_M33 = 18;
	
	/** The number of data values in the frame. */
	public static final int N_DATA_VALUES = 19;
	
	/** The denominator used to scale data values.*/
	public static final double SCALING_FACTOR = 32767.0;
	
	/** The default baud rate. */
	public static final int DEFAULT_BAUD_RATE = 115200;
	
	/** The default com port. */
	public static final int DEFAULT_COM_PORT = 1;
	
	/** The default read time out. */
	public static final int DEFAULT_READ_TIMEOUT = 2000;
	
	/** The default read time out. */
	public static final int DEFAULT_WRITE_TIMEOUT = 2000;
	
	/** Indicates whether the FOB is currently open. */
	private static boolean opened = false;
	
	/** The COM port being used. */
	private static int comPort;
	
	/** The baud rate being used. */
	private static int baudRate;
	
	/** The read timeout in milliseconds.*/
	private static int readTimeout;
	
	/** The write timeout in milliseconds.*/
	private static int writeTimeout;
        /** The data frame. */
	private static short data[] = new short[ N_DATA_VALUES ];
	
	/** The scaling factor to use for position data. */
	private static double pos_scale = 0.0;
	/**
	 * Opens a connection to the flock of birds unit in standalone mode with the default
	 * settings.
	 *
	 * @throws IOException If an error occurred while attempting to open the flock of birds
	 * device.
	 */
	public static void open() throws IOException {
		open(DEFAULT_COM_PORT, DEFAULT_BAUD_RATE,
		     DEFAULT_READ_TIMEOUT, DEFAULT_WRITE_TIMEOUT);
	}
	
	/**
	 * Opens a connection to the flock of birds unit in standalone mode.
	 *
	 * @param comPort     The com port of the flock of birds device.
	 * @param baudRate    The baud rate to use for the connection.
	 * @param readTimeout The read time out for the connection.
	 * @param writeTimeout The read time out for the connection.
	 *
	 * @throws IOException If an error occurred while attempting to open the flock of birds
	 * device.
	 */
	public static void open(int comPort, int baudRate, int readTimeout,
	                        int writeTimeout) throws IOException {
		if ( opened ) {
			throw new IOException( "The FOB is already open." );	
		}
		nOpen(comPort, baudRate, readTimeout, writeTimeout);
		opened = true;
		StandAloneFOB.comPort = comPort;
		StandAloneFOB.baudRate = baudRate;
		StandAloneFOB.readTimeout = readTimeout;
		StandAloneFOB.writeTimeout = writeTimeout;
	}
	
	/** Native helper for open. */
	private static native void nOpen(int comPort, int baudRate,
					 int readTimeout, int writeTimeout) throws IOException;
	
	/**
	 * Closes a connection to the flock of birds unit in standalone mode.
	 *
	 * @throws IOException If an error occurred while attempting to close the flock of birds
	 * device.
	 */
	public static void close() throws IOException {
		if ( !opened ) {
			throw new IOException( "The FOB was never opened." );	
		}
		nClose();
		opened = false;
	}
	
	/** Native helper for close. */
	private static native void nClose();
	
	/**
	 * Scales and returns the x part of the coordinate.
	 *
	 * @return The scaled x value.
	 */
	public static double getScaledX() {
		return (((double)data[DATA_X]) * pos_scale) / SCALING_FACTOR;
	}
	
	/**
	 * Scales and returns the y part of the coordinate.
	 *
	 * @return The scaled y value.
	 */
	public static double getScaledY() {
		return (((double)data[DATA_Y]) * pos_scale) / SCALING_FACTOR;
	}
	
	/**
	 * Scales and returns the z part of the coordinate.
	 *
	 * @return The scaled z value.
	 */
	public static double getScaledZ() {
		return (((double)data[DATA_Z]) * pos_scale) / SCALING_FACTOR;
	}
	
	/**
	 * Scales and returns the azimuth value in radians.
	 *
	 * @return The azimuth value.
	 */
	public static double getAzimuthRadians() {
		return (((double)data[DATA_AZIMUTH]) * Math.PI) / SCALING_FACTOR;
	}
	
	/**
	 * Scales and returns the roll value in radians.
	 *
	 * @return The roll value.
	 */
	public static double getRollRadians() {
		return (((double)data[DATA_ROLL]) * Math.PI) / SCALING_FACTOR;
	}
	
	/**
	 * Scales and returns the elevation value in radians.
	 *
	 * @return The elevation value.
	 */
	public static double getElevationRadians() {
		return (((double)data[DATA_ELEVATION]) * Math.PI) / SCALING_FACTOR;
	}
	
	/**
	 * Scales and returns the azimuth value in degrees.
	 *
	 * @return The azimuth value.
	 */
	public static double getAzimuthDegrees() {
		return getScaledAzimuth();
	}
	
	/**
	 * Scales and returns the roll value in degrees.
	 *
	 * @return The roll value.
	 */
	public static double getRollDegrees() {
		return getScaledRoll();
	}
	
	/**
	 * Scales and returns the elevation value in degrees.
	 *
	 * @return The elevation value.
	 */
	public static double getElevationDegrees() {
		return getScaledElevation();	
	}
	/**
	 * Scales and returns the azimuth value in degrees.
	 *
	 * @return The azimuth value.
	 */
	public static double getScaledAzimuth() {
		return (((double)data[DATA_AZIMUTH]) * 180.0) / SCALING_FACTOR;
	}
	
	/**
	 * Scales and returns the roll value in degrees.
	 *
	 * @return The roll value.
	 */
	public static double getScaledRoll() {
		return (((double)data[DATA_ROLL]) * 180.0) / SCALING_FACTOR;
	}
	
	/**
	 * Scales and returns the elevation value in degrees.
	 *
	 * @return The elevation value.
	 */
	public static double getScaledElevation() {
		return (((double)data[DATA_ELEVATION]) * 180.0) / SCALING_FACTOR;
	}
	/**
	 * Requests a new data frame from the flock of birds device. Until this method is called,
	 * the data values returned by various accessors remain unchanged. Note that only data
	 * specified in the currently set data format are updated. For example, if
	 * DATA_FORMAT_POSITION is chosen then only the position data will be updated.
	 */
	public static void refreshData() {
		pos_scale = nRefreshData(data);
	}
	
	/**
	 * A native helper for the refesh data method.
	 */
	private static native double nRefreshData(short data[]);
	/**
	 * Return the multiplicative scaling factor retrieved from the flock of birds device
	 * during the last call to refresh().
	 *
	 * @return The latest multiplicative scaling factor.
	 */
	public static double getScaling() {
		return pos_scale;	
	}
	/**
	 * Returns the X/Y/Z position vector of the sensor.
	 *
	 * @return The position vector.
	 */
	public static double[] getPositionVector() {
	    return new double[] { ((double)data[ DATA_X ]) / SCALING_FACTOR * pos_scale, 
				  ((double)data[ DATA_Y ]) / SCALING_FACTOR * pos_scale, 
				  ((double)data[ DATA_Z ]) / SCALING_FACTOR * pos_scale};
	}
	
	/**
	 * Returns the raw, unscaled X value of the sensor.
	 *
	 * @return The raw X value.
	 */
	public static short getRawX() {
		return data[DATA_X];	
	}
	/**
	 * Returns the raw, unscaled Y value of the sensor.
	 *
	 * @return The raw Y value.
	 */
	public static short getRawY() {
		return data[DATA_Y];
	}
	/**
	 * Returns the raw, unscaled Z value of the sensor.
	 *
	 * @return The raw Z value.
	 */
	public static short getRawZ() {
		return data[DATA_Z];
	}
	/**
	 * Returns the raw azimuth value of the sensor.
	 *
	 * @return The raw azimuth value.
	 */
	public static short getRawAzimuth() {
		return data[DATA_AZIMUTH];
	}
	
	/**
	 * Returns the raw roll value of the sensor.
	 *
	 * @return The raw roll value.
	 */
	public static short getRawRoll() {
		return data[DATA_ROLL];	
	}
	/**
	 * Returns the raw elevation value of the sensor.
	 *
	 * @return The raw elevation value.
	 */
	
	public static short getRawElevation() {
		return data[DATA_ELEVATION];	
	}
	/**
	 * Returns the raw matrix from the sensor.
	 *
	 * @return The raw matrix.
	 */
	public static short[][] getMatrix() {
		short [][]retval = new short[ 3 ][ 3 ];
		for ( int i = 0; i < retval.length; i++ ) {
			for ( int j = 0; j < retval.length; j++ ) {
				retval[ i ][ j ] = data[ DATA_M11 + (i*3 + j) ];
			}	
		}
		return retval;
	}
	/**
	 * Returns the scaled matrix from the sensor.
	 *
	 * @return The scaled matrix.
	 */
	public static double[][] getScaledMatrix() {
		double [][]retval = new double[ 3 ][ 3 ];
		for ( int i = 0; i < retval.length; i++ ) {
			for ( int j = 0; j < retval.length; j++ ) {
				retval[ i ][ j ] =
					((double)data[ DATA_M11 + (i*3 + j) ]) / SCALING_FACTOR;
			}
		}
		return retval;
	}
	/**
	 * Returns the raw quaternion from the sensor.
	 *
	 * @return The raw quaternion.
	 */
	public static short[] getQuaternion() {
		return new short[] { data[ DATA_Q0 ], data[ DATA_Q1 ],
				     data[ DATA_Q2 ], data[ DATA_Q3 ] };
	}
	/**
	 * Returns the scaled quaternion from the sensor.
	 *
	 * @return The scaled quaternion.
	 */
	public static double[] getScaledQuaternion() {
	    return new double[] { (double)(data[ DATA_Q0 ] / SCALING_FACTOR),
				  (double)(data[ DATA_Q1 ] / SCALING_FACTOR),
				  (double)(data[ DATA_Q2 ] / SCALING_FACTOR),
				  (double)(data[ DATA_Q3 ] / SCALING_FACTOR) };
	}
	
	/**
	 * A native helper method used to inquire whether a frame is ready to be received.
         */
	private static native boolean nFrameReady();
	/**
	 * Starts streaming data from the flock of birds device. This method introduces
	 * substantial delay so invoke it infrequently.
	 *
	 * @throws IOException If an I/O error occurs during the operation.
	 */
	public static void startStreaming() throws IOException {
		nStartStreaming();	
	}
	
	/**
	 * Stop streaming data from the flock of birds device.
	 *
	 * @throws IOException If an I/O error occurs during the operation.
	 */
	public static void stopStreaming() throws IOException {
		nStopStreaming();
	}
	/** A native helper method for the start streaming method. */
	public static native void nStartStreaming() throws IOException ;
	
	/** A native helper method for the stop streaming method. */
	public static native void nStopStreaming() throws IOException;
	/**
	 * Sets the data format for the flock of birds device. Valid parameters
	 * are the static constants DATA_FORMAT_* defined in this class. The descriptions
	 * for these static constants indicate the information that will be refreshed in the
	 * data frame during a refresh() operation.
	 *
	 * @param dataFormat The data format to use.
	 * @throws IOException If an error occurs while attempting to set the data format.
	 */
	public native static void setDataFormat( int dataFormat ) throws IOException;
	
	/**
	 * Returns the data format for the flock of birds device. Valid return values
	 * are the static constants DATA_FORMAT_* defined in this class. The descriptions
	 * for these static constants indicate the information that will be refreshed in the
	 * data frame during a refresh() operation.
	 *
	 * @return The data format that is currently in use by the device.
	 */
	public native static int getDataFormat();
	
	/**
	 * Returns the baud rate currently used in the connection to the flock of birds device.
	 *
	 * @return The current baud rate.
	 */
	public static int getBaudRate() {
		return baudRate;
	}
	
	/**
	 * Returns the COM port currently used in the connection to the flock of birds device.
	 *
	 * @return The current COM port.
	 */
	public static int getComPort() {
		return comPort;	
	}
	
	/**
	 * Returns the read timeout currently used in the connection to the flock of birds device.
	 * The value indicates the maximum time in milliseconds the driver will wait during a
	 * read transmission attempt.
	 *
	 * @return The current read timeout setting in milliseconds.
	 */
	public static int getReadTimeout() {
		return readTimeout;	
	}
	
	/**
	 * Returns the write timeout currently used in the connection to the flock of birds device.
	 * The value indicates the maximum time in milliseconds the driver will wait during a
	 * write transmission attempt.
	 *
	 * @return The current read timeout setting in milliseconds.
	 */
	public static int getWriteTimeout() {
		return writeTimeout;	
	}
}
