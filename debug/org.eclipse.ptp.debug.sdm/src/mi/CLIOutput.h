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
 
#ifndef _CLISIGHANDLE_H_
#define _CLISIGHANDLE_H_

#include "list.h"
#include "MICommand.h"

struct CLIInfoThreadsInfo {
	int current_thread_id;
	List * thread_ids;
};
typedef struct CLIInfoThreadsInfo CLIInfoThreadsInfo;

extern void CLIGetSigHandleList(MICommand *cmd, List **signals);
extern double CLIGetGDBVersion(MICommand *cmd);
extern char *CLIGetPTypeInfo(MICommand *cmd);
extern CLIInfoThreadsInfo *CLIInfoThreadsInfoNew(void);
extern CLIInfoThreadsInfo *CLIGetInfoThreadsInfo(MICommand *cmd);

#endif /* _CLISIGHANDLE_H_ */
