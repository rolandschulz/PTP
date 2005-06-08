#include <jni.h>
#include "ptp_ompi_jni.h"
#include <stdio.h>

JNIEXPORT void JNICALL 
Java_org_eclipse_ptp_rtmodel_ompi_OMPIRuntimeModel_testHelloWorld(JNIEnv *env, jobject obj) 
{
	printf("JNI (C) OMPI: Hello world!\n");
	fflush(stdout);
	return;
}
