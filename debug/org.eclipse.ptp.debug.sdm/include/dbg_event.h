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
 
#ifndef _DBG_EVENT_H_
#define _DBG_EVENT_H_

#include <aif.h>

#include "breakpoint.h"
#include "stackframe.h"
#include "memoryinfo.h"
#include "signalinfo.h"
#include "bitset.h"
#include "list.h"
#include "proxy_msg.h"

#define DBG_EV_WAITALL	0
#define DBG_EV_WAITSOME	100000

#define DBG_EV_OFFSET		100
#define DBGEV_EXIT			DBG_EV_OFFSET + 2
#define 	DBGEV_EXIT_NORMAL	0
#define 	DBGEV_EXIT_SIGNAL	1
#define DBGEV_BPSET			DBG_EV_OFFSET + 4
#define DBGEV_FRAMES		DBG_EV_OFFSET + 5
#define DBGEV_DATA			DBG_EV_OFFSET + 6
#define DBGEV_TYPE			DBG_EV_OFFSET + 7
#define DBGEV_VARS			DBG_EV_OFFSET + 8
#define DBGEV_ARGS			DBG_EV_OFFSET + 9
#define DBGEV_INIT			DBG_EV_OFFSET + 10 /* deprecated */
#define DBGEV_OK			DBG_EV_OFFSET + 11
#define DBGEV_ERROR			DBG_EV_OFFSET + 12
#define DBGEV_SUSPEND		DBG_EV_OFFSET + 13
#define 	DBGEV_SUSPEND_BPHIT		0
#define 	DBGEV_SUSPEND_SIGNAL	1
#define 	DBGEV_SUSPEND_STEP		2
#define 	DBGEV_SUSPEND_INT		3
#define DBGEV_THREADS		DBG_EV_OFFSET + 14
#define DBGEV_THREAD_SELECT	DBG_EV_OFFSET + 15
#define DBGEV_STACK_DEPTH	DBG_EV_OFFSET + 16
#define DBGEV_DATAR_MEM		DBG_EV_OFFSET + 17
#define DBGEV_DATAW_MEM		DBG_EV_OFFSET + 18
#define DBGEV_SIGNALS		DBG_EV_OFFSET + 19

#define DBGEV_DATA_EVA_EX	DBG_EV_OFFSET + 20 /* deprecated */
#define DBGEV_PARTIAL_AIF	DBG_EV_OFFSET + 21


struct dbg_suspend_event {
	int	reason;
	
	union {
		int				bpid;	/* DBGEV_SUSPEND_BPHIT */
		signalinfo *	sig;	/* DBGEV_SUSPEND_SIGNAL */
	} ev_u;
	
	int				thread_id;
	stackframe *	frame;
	int				depth;
	
	List *			changed_vars;
};
typedef struct dbg_suspend_event	dbg_suspend_event;

struct dbg_exit_event {
	int	reason;
	
	union {
		int				exit_status;	/* DBGEV_EXIT_NORMAL */
		signalinfo *	sig;			/* DBGEV_EXIT_SIGNAL */
	} ev_u;
};
typedef struct dbg_exit_event	dbg_exit_event;

struct dbg_error_event {
	int		error_code;
	char *	error_msg;
};
typedef struct dbg_error_event	dbg_error_event;

struct dbg_bpset_event {
	int				bpid;
	breakpoint *	bp;
};
typedef struct dbg_bpset_event	dbg_bpset_event;
	
struct dbg_data_event {
	char *	type_desc;
	AIF *	data;
};
typedef struct dbg_data_event	dbg_data_event;

struct dbg_threads_event {
	List *	list;
	int		thread_id;
};
typedef struct dbg_threads_event	dbg_threads_event;

struct dbg_thread_select_event {
	stackframe *	frame;
	int				thread_id;
};
typedef struct dbg_thread_select_event	dbg_thread_select_event;

struct dbg_partial_aif_event {
	AIF *	data;
	char *	type_desc;
	char *	name;
};
typedef struct dbg_partial_aif_event dbg_partial_aif_event;

struct dbg_event {
	int			event_id;
	int			trans_id;
	bitset *	procs;
	
	union {
		/*
		 * DBGEV_INIT
		 */
		int					num_servers;
		
		/*
		 * DBGEV_BPSET
		 */
		dbg_bpset_event		bpset_event;
		
		/*
		 * DBGEV_FRAMES, DBGEV_VARS, DBGEV_ARGS, DBGEV_SIGNALS
		 */
		List *				list;
		
		/*
		 * DBGEV_TYPE
		 */
		char *				type_desc;
		
		/*
		 * DBGEV_DATA
		 */
		dbg_data_event		data_event;
			
		/*
		 * DBGEV_EXIT
		 */
		dbg_exit_event		exit_event;
		
		/*
		 * DBGEV_ERROR
		 */
		dbg_error_event		error_event;
	
		/*
		 * DBGEV_SUSPEND
		 */
		dbg_suspend_event	suspend_event;
		
		/* 
		 * DBGEV_STACK_DEPTH
		 */
		int					stack_depth;
		 
		/*
		 * DBGEV_DATAR_MEM
		 */
		memoryinfo *		meminfo;
		
		/*
		 * DBGEV_THREADS
		 */
		dbg_threads_event	threads_event;
		
		/*
		 * DBGEV_THREAD_SELECT
		 */
		dbg_thread_select_event	thread_select_event;
		
		/*
		 * DBGEV_DATA_EVA_EX
		 */
		char * data_expression;
		
		/*
		 * DBGEV_PARTIAL_AIF
		 */
		dbg_partial_aif_event partial_aif_event;

	} dbg_event_u;
};
typedef struct dbg_event dbg_event;

extern int 			DbgDeserializeEvent(int, int, char **, dbg_event **);
extern int 			DbgSerializeEvent(dbg_event *, char **);
extern dbg_event *	NewDbgEvent(int);
extern void			FreeDbgEvent(dbg_event *);
extern dbg_event *	DbgErrorEvent(int, char *);
#endif /* _DBG_EVENT_H_ */
