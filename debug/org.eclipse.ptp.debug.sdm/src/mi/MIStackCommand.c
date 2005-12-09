/*******************************************************************************
 * Copyright (c) 2000, 2004 QNX Software Systems and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     QNX Software Systems - Initial API and implementation
 *******************************************************************************/
 
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
