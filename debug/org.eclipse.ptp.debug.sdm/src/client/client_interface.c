#include <stdio.h>

#include "dbg.h"

/*
 * Process set operations
 */
void DbgCreateProcSet(procset *set);
void DbgDestroyProcSet(procset *set);
void DbgRemoveFromSet(procset *dst, procset *src);
void DbgAddToSet(procset *dst, procset *src);
void DbgAddProcToSet(procset *set, int pid);
void DbgRemoveProcFromSet(procset *set, int pid);

/*
 * Session initialization
 */
int 
DbgInit(void)
{
	return 0;
}

/*
 * Breakpoint operations
 */
int 
DbgSetLineBP(procset *set, char *file, int line, breakpoint *bp)
{
	int			rlen;
	int			status;
	char *		request;
	char *		result;
	char *		s;
	char			par[1024];
	
	if ( file == NULL )
		file = "<null>";
	        
	rlen = 1024 + strlen(file);
	request = (char *) malloc(rlen);
	
	snprintf(request, rlen-1, "SETLINEBREAK %s %d\n", file, line);
	
	HoldSignals();
	
	if ( proxy_send(request, &result, &status, NULL) < 0 )
	{
	        fprintf(stderr,"DbgSetLineBP failed\n");
	        free(request);
	        return DBGERR_RPC;
	}
	
	free(request);
	
	if ( status != 0 ) {
	        free(result);
	        return status;
	}
	
	s = result;
	
	s = getword(s,par);
	status = atoi(par);
	
	if (status != DBGEV_BPSET) {
		if (status == DBGEV_ERROR) {
			s = getword(s,par);
			s = skipspace(s);
			DbgSetErr(atoi(par), s);
		}
		free(result);
		return status;
	}

}

int 
DbgSetFuncBP(procset *set, char *file, char *func, breakpoint *bp)
{
}

int 
DbgDeleteBP(procset *set, breakpoint *bp)
{
}

/*
 * Process control operations
 */
int 
DbgContinue(procset *set)
{
}

int 
DbgStep(procset *set, int count, int type)
{
}

/*
 * Stack frame operations
 */
int 
DbgListStackFrame(int proc, stackframelist *frame)
{
}

int 
DbgMoveStackFrame(int proc, int count, int dir, stackframe *frame)
{
}

/*
 * Expression/variable operations
 */
int 
DbgEvalExpr(int proc, char *exp)
{
}

int 
DbgListVariables(int proc, stackframe *frame)
{
}

int 
DbgListGlobalVariables(int proc)
{
}

/*
 * Event handling
 */
int
DbgWaitEvent(void)
{
}
