/*
 * Copyright (c) 2010 IBM Corporation.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */

#ifndef _THREADMAP_H_
#define _THREADMAP_H_

#define THREAD_STATUS_STOP_BP_HIT						0
#define THREAD_STATUS_STOP_STEP_STMT_HOOK_BP_HIT		1
#define THREAD_STATUS_STOP_STEP_OVER  					2
#define THREAD_STATUS_STEP_RETURN_REQUESTED             3
#define THREAD_STATUS_STOP_UNKNOWN 						-1

struct thread_entry {
	int 	thread_id;
	int 	statement_hook_bp_num;
	int 	stop_status;
	int 	skip_statement_hook;
	int 	step_nop;
	List *	stackframes;
};
typedef struct thread_entry	thread_entry;

extern thread_entry *	AddThreadMap(int threadID, int stopStatus, int statementHookBPNum, int stepNop);
extern void 			RemoveThreadMap(int id);
extern thread_entry *	FindThreadEntry(int threadID);
extern void 			ClearThreadMaps();

#endif /* _THREADMAP_H_ */
