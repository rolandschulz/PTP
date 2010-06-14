/******************************************************************************
 * Copyright (c) 2010 IBM Corporation and others.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0s
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 ******************************************************************************/


#include <stdio.h>
#include <stdlib.h>
#include <string.h>
#include <ctype.h>

#include "signal_info.h"

signal_info *
NewSignalInfo(void) {
	signal_info *	sig = (signal_info *)malloc(sizeof(signal_info));
	
	sig->name = NULL;
	sig->stop = 0;
	sig->print = 0;
	sig->pass = 0;
	sig->desc = NULL;

	return sig;
}

void	
FreeSignalInfo(signal_info *sig) {
	if (sig->name != NULL)
		free(sig->name);
	if (sig->desc != NULL)
		free(sig->desc);
	free(sig);
}

