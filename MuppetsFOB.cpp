// MuppetsFOB.cpp
//
// Authors: Damian Eads
//
// Thanks to James Tranovich for testing and fixes.
//
// Copyright (C) Damian Eads. 2004-2006.
// 
// This program is free software; you can redistribute it and/or
// modify it under the terms of the GNU General Public License
// as published by the Free Software Foundation; either version 2
// of the License, or (at your option) any later version.
// 
// This program is distributed in the hope that it will be useful,
// but WITHOUT ANY WARRANTY; without even the implied warranty of
// MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
// GNU General Public License for more details.
//
// You should have received a copy of the GNU General Public License
// along with this program; if not, write to the Free Software
// Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA  
// 02110-1301, USA.

#include <windows.h>
#include "FOBSA.h"
#include "external/Bird.h"

#define COM1 1
#define COM2 2
#define COM3 3
#define COM4 4
#define DEFAULT_BAUDRATE 115200
#define DEFAULT_COMPORT COM1
#define DATA_X 0
#define DATA_Y 1
#define DATA_Z 2
#define DATA_AZIMUTH 3
#define DATA_ELEVATION 4
#define DATA_ROLL 5
#define DATA_Q0 6
#define DATA_Q1 7
#define DATA_Q2 8
#define DATA_Q3 9
#define DATA_M11 10
#define DATA_M12 11
#define DATA_M13 12
#define DATA_M21 13
#define DATA_M22 14
#define DATA_M23 15
#define DATA_M31 16
#define DATA_M32 17
#define DATA_M33 18
static BIRDSYSTEMCONFIG sysconfig;
static BIRDDEVICECONFIG devconfig;
static BIRDFRAME frame;
static WORD _comports[1] = {(WORD)DEFAULT_COMPORT};
static DWORD _baud_rate = (WORD)DEFAULT_BAUDRATE;
static DWORD _read_timeout = (DWORD)2000;
static DWORD _write_timeout = (DWORD)2000;

JNIEXPORT void JNICALL Java_muppets_ext_fob_StandAloneFOB_nOpen
(JNIEnv *env, jclass cls, jint comport, jint baud, jint rto, jint wto) {
	_comports[0] = (WORD)comport;
	_baud_rate = baud;
	_read_timeout = rto;
	_write_timeout = wto;
	if ( birdRS232WakeUp(1, TRUE, 1, _comports, _baud_rate, _read_timeout, _write_timeout) == 0) {
		jclass newExcCls;
        env->ExceptionDescribe();
        env->ExceptionClear();
		newExcCls = env->FindClass("java/io/IOException");
		if (newExcCls == 0) { // Exception class couldn't be found
			return;
		}
		env->ThrowNew(newExcCls, "Error waking up the FOB device during open.");
	}
	if (birdGetSystemConfig(1,&sysconfig) == 0) {
		jclass newExcCls;
        env->ExceptionDescribe();
        env->ExceptionClear();
		newExcCls = env->FindClass("java/io/IOException");
		if (newExcCls == 0) { // Exception class couldn't be found
			return;
		}
		env->ThrowNew(newExcCls, "Error grabbing FOB system configuration during open.");
	}
	
	// get the device configuration
	if (birdGetDeviceConfig(1,0,&devconfig) == 0) {
		jclass newExcCls;
        env->ExceptionDescribe();
        env->ExceptionClear();
		newExcCls = env->FindClass("java/io/IOException");
		if (newExcCls == 0) { // Exception class couldn't be found
			return;
		}
		env->ThrowNew(newExcCls, "Error grabbing FOB device configuration during open.");
	}
}

JNIEXPORT void JNICALL Java_muppets_ext_fob_StandAloneFOB_nClose
(JNIEnv *env, jclass cls) {
	birdShutDown(1);
}

JNIEXPORT jboolean JNICALL Java_muppets_ext_fob_StandAloneFOB_nFrameReady
(JNIEnv *env, jclass cls) {	
	return birdFrameReady(0);
}


/*
 * Class:     muppets_ext_fob_StandAloneFOB
 * Method:    nRefreshData
 * Signature: ([S)V
 */

JNIEXPORT jdouble JNICALL Java_muppets_ext_fob_StandAloneFOB_nRefreshData
(JNIEnv *env, jclass cls, jshortArray a) {
	jshort *arr = 0;
	BIRDREADING *preading = 0;
	int i = 0, j = 0;
	double pos_scale;
	birdGetMostRecentFrame(1,&frame);
	preading = &frame.reading[0];

	arr = env->GetShortArrayElements(a, NULL);
	if ( arr == NULL ) {
		jclass newExcCls;
        env->ExceptionDescribe();
        env->ExceptionClear();
		newExcCls = env->FindClass("java/io/IOException");
		if (newExcCls == 0) { // Exception class couldn't be found
			return 0.0;
		}
		env->ThrowNew(newExcCls, "Could not modify data in MuppetsFOB object.");
	}
	pos_scale = devconfig.wScaling;
	
	// not needed or enabled.
	arr[ DATA_AZIMUTH ] = preading->angles.nAzimuth;
	arr[ DATA_ELEVATION ] = preading->angles.nElevation;
	arr[ DATA_ROLL ] = preading->angles.nRoll;

	arr[ DATA_Q0 ] = preading->quaternion.nQ0;
	arr[ DATA_Q1 ] = preading->quaternion.nQ1;
	arr[ DATA_Q2 ] = preading->quaternion.nQ2;
	arr[ DATA_Q3 ] = preading->quaternion.nQ3;

	/*
	//printf("azimuth: %f %f %f\n", arr[DATA_AZIMUTH], arr[DATA_ELEVATION], arr[DATA_ROLL]);

	printf("from within MuppetsFOB: %d %d %d %d\n", preading->quaternion.nQ0, preading->quaternion.nQ1, 
		preading->quaternion.nQ2, preading->quaternion.nQ3);
	//printf("from within FOB: %f %f %f %f\n", arr[DATA_Q0], arr[DATA_Q1], arr[DATA_Q2], arr[DATA_Q3]);
	*/
	arr[ DATA_X ] = preading->position.nX;
	arr[ DATA_Y ] = preading->position.nY;
	arr[ DATA_Z ] = preading->position.nZ;
	
	// hopefully this code works?
	for ( ; i < 3; i++ ) {
		for ( j = 0; j < 3; j++ ) {
			arr[ DATA_M11 + ((i*3)+j) ] = preading->matrix.n[ i ][ j ];
		}
	}

	env->ReleaseShortArrayElements(a, arr, JNI_COMMIT);
	return pos_scale;
}

/*
 * Class:     muppets_ext_fob_StandAloneFOB
 * Method:    nStartStreaming
 * Signature: ()V
 */
JNIEXPORT void JNICALL Java_muppets_ext_fob_StandAloneFOB_nStartStreaming
(JNIEnv *env, jclass cls) {
	if (birdStartFrameStream(1) == 0) {
		jclass newExcCls;
        env->ExceptionDescribe();
        env->ExceptionClear();
		newExcCls = env->FindClass("java/io/IOException");
		if (newExcCls == 0) { // Exception class couldn't be found
			return;
		}
		env->ThrowNew(newExcCls, "Error when attempting to start the streaming.");
	}
}

/*
 * Class:     muppets_ext_fob_StandAloneFOB
 * Method:    nStopStreaming
 * Signature: ()V
 */
JNIEXPORT void JNICALL Java_muppets_ext_fob_StandAloneFOB_nStopStreaming
(JNIEnv *env, jclass cls) {
	if (birdStopFrameStream(1) == 0) {
		jclass newExcCls;
        env->ExceptionDescribe();
        env->ExceptionClear();
		newExcCls = env->FindClass("java/io/IOException");
		if (newExcCls == 0) { // Exception class couldn't be found
			return;
		}
		env->ThrowNew(newExcCls, "Error when attempting to start the streaming.");
	}
}

/*
 * Class:     muppets_ext_fob_StandAloneFOB
 * Method:    setDataFormat
 * Signature: (I)V
 */
JNIEXPORT void JNICALL Java_muppets_ext_fob_StandAloneFOB_setDataFormat
(JNIEnv *env, jclass cls, jint val) {
	devconfig.byDataFormat = (BYTE)((int)val);

	//; then set device config.
	if (birdSetDeviceConfig(1,0,&devconfig) == 0) {
		jclass newExcCls;
        env->ExceptionDescribe();
        env->ExceptionClear();
		newExcCls = env->FindClass("java/io/IOException");
		if (newExcCls == 0) { // Exception class couldn't be found
			return;
		}
		env->ThrowNew(newExcCls, "Error setting the data format configuration.");
	}
}

/*
 * Class:     muppets_ext_fob_StandAloneFOB
 * Method:    getDataFormat
 * Signature: ()I
 */
JNIEXPORT jint JNICALL Java_muppets_ext_fob_StandAloneFOB_getDataFormat
(JNIEnv *env, jclass cls) {
	return devconfig.byDataFormat;
}
