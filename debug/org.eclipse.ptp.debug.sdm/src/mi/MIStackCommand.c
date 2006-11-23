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

#include "list.h"
#include "MIString.h"
#include "MICommand.h"

MICommand *
MIStackSelectFrame(int level)
{
	MICommand *	cmd;
	
	cmd = MICommandNew("-stack-select-frame", MIResultRecordDONE);
	MICommandAddOption(cmd, MIIntToCString(level), NULL);
	return cmd;
}

MICommand *
MIStackListFrames(int low, int high)
{
	MICommand *	cmd;
	
	cmd = MICommandNew("-stack-list-frames", MIResultRecordDONE);
	//MICommandAddOption(cmd, MIIntToCString(low), MIIntToCString(high));
	MICommandAddOption(cmd, MIIntToCString(low), NULL);
	MICommandAddOption(cmd, MIIntToCString(high), NULL);
	return cmd;
}

MICommand *
MIStackListAllFrames(void)
{
	return MICommandNew("-stack-list-frames", MIResultRecordDONE);
}

MICommand *
MIStackListLocals(int vals)
{
	MICommand *	cmd;
	
	cmd = MICommandNew("-stack-list-locals", MIResultRecordDONE);
	MICommandAddOption(cmd, MIIntToCString(vals), NULL);
	return cmd;
}

MICommand *
MIStackListArguments(int vals, int low, int high)
{
	MICommand *	cmd;
	
	cmd = MICommandNew("-stack-list-arguments", MIResultRecordDONE);
	MICommandAddOption(cmd, MIIntToCString(vals), NULL);
	MICommandAddOption(cmd, MIIntToCString(low), MIIntToCString(high));
	return cmd;
}

MICommand *
MIStackListAllArguments(int vals)
{
	MICommand *	cmd;
	
	cmd = MICommandNew("-stack-list-arguments", MIResultRecordDONE);
	MICommandAddOption(cmd, MIIntToCString(vals), NULL);
	return cmd;
}

MICommand *
MIStackInfoFrame(void)
{
	MICommand * cmd;
	cmd = MICommandNew("-stack-info-frame", MIResultRecordDONE);
	return cmd;
}

MICommand *
MIStackInfoDepth(void)
{
	MICommand * cmd;
	cmd = MICommandNew("-stack-info-depth", MIResultRecordDONE);
	return cmd;
}
