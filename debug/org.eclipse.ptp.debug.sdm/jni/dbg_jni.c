#include <jni.h>
#include <stdio.h>
#include <unistd.h>

#include <stdbool.h>

/***********************************************************************
 * THE JNI FUNCTIONS THAT THE RUNTIME ENVIRONMENT WILL CALL - THESE ARE
 * JUST STUBS THAT WILL CALL ADDITIONAL FUNCTIONS TO DO THE 'REAL'
 * WORK
 **********************************************************************/

JNIEXPORT int JNICALL
Java_org_eclipse_ptp_debug_external_debugger_ParallelDebugger_DbgGo(JNIEnv *env, jintArray a)
{
    	return 0;
}
