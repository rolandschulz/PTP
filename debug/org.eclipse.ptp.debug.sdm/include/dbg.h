#ifndef DBG_H_
#define DBG_H_

#include "procset.h"
#include "breakpoint.h"
#include "stackframe.h"

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
int DbgInit(void);

/*
 * Breakpoint operations
 */
int DbgSetLineBP(procset *set, char *file, int line, breakpoint *bp);
int DbgSetFuncBP(procset *set, char *file, char *func, breakpoint *bp);
int DbgDeleteBP(procset *set, breakpoint *bp);

/*
 * Process control operations
 */
int DbgContinue(procset *set);
int DbgStep(procset *set, int count, int type);

/*
 * Stack frame operations
 */
int DbgListStackFrame(int proc, stackframelist *frame);
int DbgMoveStackFrame(int proc, int count, int dir, stackframe *frame);

/*
 * Expression/variable operations
 */
int DbgEvalExpr(int proc, char *exp);
int DbgListVariables(int proc, stackframe *frame);
int DbgListGlobalVariables(int proc);

/*
 * Event Handling
 */
int DbgWaitEvent(void);

#endif /*DBG_H_*/
