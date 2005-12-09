#include <stdio.h>
#include <stdlib.h>
#include <string.h>

#include "MIString.h"
#include "MISession.h"
#include "MIError.h"
#include "MIResultRecord.h"
#include "MIBreakpoint.h"

void
cmd_callback(MIResultRecord *rr)
{
	MIString *str = MIResultRecordToString(rr);
	printf("res> %s\n", MIStringToCString(str));
	MIStringFree(str);
	
	rr->resultClass
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
	
	while (!MICommandCompleted(cmd)) {
		MISessionProgress();
	}
	
	MICommandFree(cmd);
}

int main(int argc, char *argv[])
{
	MISession *sess;
	MICommand *cmd;
	
	sess = MISessionLocal();
	if (sess == NULL) {
		fprintf(stderr, "%s", MIGetErrorStr());
		return 1;
	}
	
	MISessionRegisterCompletedCallback(sess, cmd_completed);
	MISessionRegisterConsoleCallback(sess, console_callback);
	MISessionRegisterLogCallback(sess, log_callback);
	
	cmd = MICommandNew("help", cmd_callback);
	
	sendcmd_wait(sess, cmd);
	
	MIGDBSet(sess, "confirm", "off");
	
	wait_for_cmd();
	
	MIBreakInsert(sess, 0, 0, NULL, 0, "4", 0);
	
	sendcmd_wait(sess, cmd);
		
	cmd = MICommandNew("quit", cmd_callback);
	
	sendcmd_wait(sess, cmd);

	return 0;
}