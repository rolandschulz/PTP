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

#ifndef _RUNTIME_H_
#define _RUNTIME_H_

extern int runtime_init(int *size, int *rank);
extern int runtime_finalize();
extern int runtime_send(char *buf, int len, int dest, int tag);
extern int runtime_recv(char *buf, int len, int source, int *tag);
extern int runtime_probe(int *avail, int *source, int *tag, int *count);
extern int runtime_setup_environment(int nprocs, int id, int job_id, char ***envp);

#endif /* _RUNTIME_H_ */
