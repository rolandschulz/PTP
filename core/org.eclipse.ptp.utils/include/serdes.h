/*******************************************************************************
 * Copyright (c) 2008 IBM Corporation.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 * IBM Corporation - Initial API and implementation
 *******************************************************************************/

#ifndef SERDES_H_
#define SERDES_H_

extern unsigned int	hex_str_to_int(char *str, int len, char **end);
extern void			int_to_hex_str(unsigned int val, char *str, int len, char **end);

#endif /* SERDES_H_ */
