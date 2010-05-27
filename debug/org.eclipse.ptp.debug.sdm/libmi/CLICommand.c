/******************************************************************************
 * Copyright (c) 2005 The Regents of the University of California.
 * This material was produced under U.S. Government contract W-7405-ENG-36
 * for Los Alamos National Laboratory, which is operated by the University
 * of California for the U.S. Department of Energy. The U.S. Government has
 * rights to use, reproduce, and distribute this software. NEITHER THE
 * GOVERNMENT NOR THE UNIVERSITY MAKES ANY WARRANTY, EXPRESS OR IMPLIED, OR
 * ASSUMES ANY LIABILITY FOR THE USE OF THIS SOFTWARE. If software is modified
 * to produce derivative works, such modified software should be clearly
 * marked, so as not to confuse it with the version available from LANL.
 *
 * Additionally, this program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * LA-CC 04-115
 ******************************************************************************/

#include <stdio.h>
#include <stdlib.h>
#include <string.h>

#include "MICommand.h"

MICommand *
MIGDBVersion(void)
{
	MICommand * cmd;
	cmd = MICommandNew("-gdb-version", MIResultRecordDONE);
	return cmd;
}

MICommand *
CLIHandle(char *arg) {
	MICommand * cmd;
	cmd = MICommandNew("handle", MIResultRecordDONE);
	MICommandAddOption(cmd, arg, NULL);
	return cmd;
}

MICommand *
CLIInfoThreads(void) {
	MICommand * cmd;
	cmd = MICommandNew("info threads", MIResultRecordDONE);
	return cmd;
}

MICommand *
CLIPType(char *name) {
	MICommand * cmd;
	cmd = MICommandNew("ptype", MIResultRecordDONE);
	MICommandAddOption(cmd, name, NULL);
	return cmd;
}

MICommand *
CLIListSignals(char *name) {
	MICommand * cmd;
	cmd = MICommandNew("info signals", MIResultRecordDONE);
	if (name != NULL) {
		MICommandAddOption(cmd, name, NULL);
	}
	return cmd;
}

MICommand *
CLISignalInfo(char *arg) {
	MICommand * cmd;
	cmd = MICommandNew("signal", MIResultRecordDONE);
	MICommandAddOption(cmd, arg, NULL);
	return cmd;
}

MICommand *
CLIFrame(void) {
	MICommand * cmd;
	cmd = MICommandNew("frame", MIResultRecordDONE);
	return cmd;
}

MICommand *
CLIInfoProc(void) {
	MICommand * cmd;
#ifdef __APPLE__
	cmd = MICommandNew("info pid", MIResultRecordDONE);
#else /* __APPLE__ */
	cmd = MICommandNew("info proc", MIResultRecordDONE);
#endif /* __APPLE__ */
	return cmd;
}

