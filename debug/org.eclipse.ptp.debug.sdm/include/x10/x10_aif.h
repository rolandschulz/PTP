/*
 * Copyright (c) 2010 IBM Corporation.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
 
#ifndef _X10_AIF_H_
#define _X10_AIF_H_

#include "MI.h"
#include "x10_var.h"

/*
 * x10_aif.c
 */
extern AIF *	X10GetPartialAIF(MISession *session, char *expr, x10_var_t *var);
extern AIF *	X10GetAIF(MISession *session, char *expr, x10_var_t *var);

#endif /* _X10_AIF_H_ */
