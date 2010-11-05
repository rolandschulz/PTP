#include <stdio.h>
#include <stdlib.h>
#include <string.h>

#include "MIList.h"
#include "MIString.h"
#include "MISession.h"
#include "MIError.h"
#include "MIResultRecord.h"
#include "MIBreakpoint.h"

void
cmd_callback(MIResultRecord *rr, void *sess)
{
	MIString *str = MIResultRecordToString(rr);
	printf("res> %s\n", MIStringToCString(str));
	MIStringFree(str);
}

void
console_callback(char *str)
{
	printf("cons> %s\n", str);
}

void
log_callback(char *str)
{
	printf("log> %s\n", str);
}

void
sendcmd_wait(MISession *sess, MICommand *cmd)
{
	MISessionSendCommand(sess, cmd);
	do {
		MISessionProgress(sess);
	} while (!MICommandCompleted(cmd));
}

int main(int argc, char *argv[])
{
	MISession *sess;
	MICommand *cmd;
	MIList *bpts;
	MIBreakpoint *bpt;
	
	sess = MISessionNew();
	MISessionSetDebug(1);
	if (MISessionStartLocal(sess, "test_mi") < 0) {
		fprintf(stderr, "%s", MIGetErrorStr());
		return 1;
	}
	
	MISessionRegisterConsoleCallback(sess, console_callback);
	MISessionRegisterLogCallback(sess, log_callback);

printf("help command\n");
	cmd = MICommandNew("help", MIResultRecordDONE);
	MICommandRegisterCallback(cmd, cmd_callback, sess);
	sendcmd_wait(sess, cmd);
	if (!MICommandResultOK(cmd))
		fprintf(stderr, "command failed\n");
	MICommandFree(cmd);
printf("set command\n");	
	cmd = MIGDBSet("confirm", "off");
	MICommandRegisterCallback(cmd, cmd_callback, sess);
	sendcmd_wait(sess, cmd);
	if (!MICommandResultOK(cmd))
		fprintf(stderr, "command failed\n");
	MICommandFree(cmd);
printf("break command\n");	
	cmd = MIBreakInsert(0, 0, NULL, 0, "4", 0);
	MICommandRegisterCallback(cmd, cmd_callback, sess);
	sendcmd_wait(sess, cmd);
	if (!MICommandResultOK(cmd))
		fprintf(stderr, "command failed\n");
	bpts = MIBreakpointGetBreakInsertInfo(cmd);
	if (bpts != NULL)
		for (MIListSet(bpts); (bpt = (MIBreakpoint *)MIListGet(bpts)) != NULL; )
			printf("bpt id = %d\n", bpt->number);
	MICommandFree(cmd);
printf("quit command\n");		
	cmd = MIGDBExit();
	MICommandRegisterCallback(cmd, cmd_callback, sess);
	sendcmd_wait(sess, cmd);
	if (!MICommandResultOK(cmd))
		fprintf(stderr, "command failed\n");
	MICommandFree(cmd);

	return 0;
}
