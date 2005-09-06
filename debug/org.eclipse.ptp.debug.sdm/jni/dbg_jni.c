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
Java_org_eclipse_ptp_debug_external_debugger_ParallelDebugger_DbgRun(JNIEnv *env, jintArray procs)
{
    	return DbgRun();
}

JNIEXPORT int JNICALL
Java_org_eclipse_ptp_debug_external_debugger_ParallelDebugger_DbgGo(JNIEnv *env, jintArray procs)
{
    	return DbgGo();
}

JNIEXPORT int JNICALL
Java_org_eclipse_ptp_debug_external_debugger_ParallelDebugger_DbgStep(JNIEnv *env, jintArray procs, jint count, jint type)
{
    	return DbgStep();
}

JNIEXPORT int JNICALL
Java_org_eclipse_ptp_debug_external_debugger_ParallelDebugger_DbgKill(JNIEnv *env, jintArray procs)
{
    	return DbgKill();
}

JNIEXPORT int JNICALL
Java_org_eclipse_ptp_debug_external_debugger_ParallelDebugger_DbgHalt(JNIEnv *env, jintArray procs)
{
    	return DbgHalt();
}

JNIEXPORT int JNICALL
Java_org_eclipse_ptp_debug_external_debugger_ParallelDebugger_DbgSetLineBreakpoint(JNIEnv *env, jintArray procs)
{
    	return DbgSetLineBreakpoint();
}

JNIEXPORT int JNICALL
Java_org_eclipse_ptp_debug_external_debugger_ParallelDebugger_DbgSetFuncBreakpoint(JNIEnv *env, jintArray procs)
{
    	return DbgSetFuncBreakpoint();

}

JNIEXPORT int JNICALL
Java_org_eclipse_ptp_debug_external_debugger_ParallelDebugger_DeleteBreakpoints(JNIEnv *env, jintArray procs)
{
    	return DbgDeleteBreakpoints();

}

JNIEXPORT int JNICALL
Java_org_eclipse_ptp_debug_external_debugger_ParallelDebugger_ListStackframes(JNIEnv *env, jintArray procs)
{
    	return DbgListStackframes();

}

JNIEXPORT int JNICALL
Java_org_eclipse_ptp_debug_external_debugger_ParallelDebugger_SetCurrentStackframe(JNIEnv *env, jintArray procs)
{
    	return DbgSetCurrentStackframe();

}

JNIEXPORT int JNICALL
Java_org_eclipse_ptp_debug_external_debugger_ParallelDebugger_ListLocalVariables(JNIEnv *env, jintArray procs)
{
    	return DbgListLocalVariables();

}

JNIEXPORT int JNICALL
Java_org_eclipse_ptp_debug_external_debugger_ParallelDebugger_ListArguments(JNIEnv *env, jintArray procs)
{
    	return DbgListArguments();

}

JNIEXPORT int JNICALL
Java_org_eclipse_ptp_debug_external_debugger_ParallelDebugger_ListGlobalVariables(JNIEnv *env, jintArray procs)
{
    	return DbgListGlobalVariables();

}

JNIEXPORT int JNICALL
Java_org_eclipse_ptp_debug_external_debugger_ParallelDebugger_EvaluateExpression(JNIEnv *env, jintArray procs)
{
    	return DbgEvaluateExpression();

}
