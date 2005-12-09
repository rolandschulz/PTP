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
	
	MISessionRegisterConsoleCallback(sess, console_callback);
	MISessionRegisterLogCallback(sess, log_callback);

printf("help command\n");
	cmd = MICommandNew("help");
	MICommandRegisterCallback(cmd, cmd_callback);
	sendcmd_wait(sess, cmd);
printf("set command\n");	
	cmd = MIGDBSet("confirm", "off");
	MICommandRegisterCallback(cmd, cmd_callback);
	sendcmd_wait(sess, cmd);
printf("break command\n");	
	cmd = MIBreakInsert(0, 0, NULL, 0, "4", 0);
	MICommandRegisterCallback(cmd, cmd_callback);
	sendcmd_wait(sess, cmd);
printf("quit command\n");		
	cmd = MICommandNew("quit");
	MICommandRegisterCallback(cmd, cmd_callback);
	sendcmd_wait(sess, cmd);

	return 0;
}