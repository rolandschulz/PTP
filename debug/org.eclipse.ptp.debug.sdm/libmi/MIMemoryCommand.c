/*******************************************************************************
 * Copyright (c) 2005 The Regents of the University of California. 
 * This material was produced under U.S. Government contract W-7405-ENG-36 
 * for Los Alamos National Laboratory, which is operated by the University 
 * of California for the U.S. Department of Energy. The U.S. Government has 
 * rights to use, reproduce, and distribute this software. NEITHER THE 
 * GOVERNMENT NOR THE UNIVERSITY MAKES ANY WARRANTY, EXPRESS OR IMPLIED, OR 
 * ASSUMES ANY LIABILITY FOR THE USE OF THIS SOFTWARE. If software is modified 
 * to produce derivative works, such modified software should be clearly marked, 
 * so as not to confuse it with the version available from LANL.
 * 
 * Additionally, this program and the accompanying materials 
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * LA-CC 04-115
 *******************************************************************************/
 
 /**
 * @author Clement chu
 * 
 */

#include <stdio.h>
#include <stdlib.h>
#include <string.h>

#include "MIString.h"
#include "MICommand.h"

MICommand *
MIDataReadMemory(long offset, char* address, char* format, int wordSize, int rows, int cols, char* asChar)
{
	MICommand * cmd;
	cmd = MICommandNew("-data-read-memory", MIResultRecordDONE);
	
	if (offset != 0) {
		MICommandAddOption(cmd, "-o", MIIntToCString(offset));
	}
	MICommandAddOption(cmd, address, NULL);
	MICommandAddOption(cmd, format, NULL);
	MICommandAddOption(cmd, MIIntToCString(wordSize), NULL);
	MICommandAddOption(cmd, MIIntToCString(rows), NULL);
	MICommandAddOption(cmd, MIIntToCString(cols), NULL);
	if (asChar != NULL) {
		MICommandAddOption(cmd, asChar, NULL);
	}
	return cmd;
}

MICommand *
MIDataWriteMemory(long offset, char* address, char* format, int wordSize, char* value)
{
	MICommand * cmd;
	cmd = MICommandNew("-data-write-memory", MIResultRecordDONE);
	
	if (offset != 0) {
		MICommandAddOption(cmd, "-o", MIIntToCString(offset));
	}
	MICommandAddOption(cmd, address, NULL);
	MICommandAddOption(cmd, format, NULL);
	MICommandAddOption(cmd, MIIntToCString(wordSize), NULL);
	MICommandAddOption(cmd, value, NULL);
	return cmd;
}

MICommand *
MIDataReadDisassemble(char* startAddr, char* endAddr, char* format)
{
	MICommand * cmd;
	cmd = MICommandNew("-data-disassemble", MIResultRecordDONE);
	
	if (startAddr != 0) {
		MICommandAddOption(cmd, "-s", startAddr);
	}

	if (endAddr != 0) {
		MICommandAddOption(cmd, "-e", endAddr);
	}

	if (format != 0) {
		MICommandAddOption(cmd, "--", format);
	}
	
	return cmd;
}

