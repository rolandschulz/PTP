/* <copyright_input start="2005" end="2007"> */
/********************************************************************/
/*                                                                  */
/* Licensed Materials - Property of IBM                             */
/* IBM XL UPC Alpha Edition V0.8 for AIX                            */
/*                                                                  */
/* (C) Copyright IBM Corp. 2005. All Rights Reserved.               */
/* US Government Users Restricted Rights - Use, duplication or      */
/* disclosure restricted by GSA ADP Schedule Contract with IBM Corp.*/
/*                                                                  */
/********************************************************************/
/* </copyright_input> */

/**
 * \file upc.h Application header. This file defines constants mandated by the
 * language.
 */
#ifndef __UPC_H
#define __UPC_H

#include <stddef.h>

/* The following defines are required by the UPC v1.0 specification. */
#define barrier        upc_barrier
#define barrier_notify upc_notify
#define barrier_wait   upc_wait
#define fence          upc_fence
#define forall         upc_forall

/** the number of threads in the program */
#ifndef THREADS
#define THREADS __xlupc_threads()
#endif

/** the id of the current thread */
#ifndef MYTHREAD
#define MYTHREAD __mythread
#endif

/** the UPC lock type and functions */
typedef struct __xlupc_lock_impl upc_lock_t;

#if __XLUPC_LONGLONGINDICES
typedef unsigned long long upc_array_t;
#else
typedef size_t upc_array_t;
#endif

#ifdef __cpluscplus
extern "C" {
#endif

void          upc_global_exit  (int status);

shared void * upc_global_alloc (upc_array_t nblocks, size_t nbytes);
shared void * upc_all_alloc    (upc_array_t nblocks, size_t nbytes);
shared void * upc_alloc        (size_t  nbytes);
shared void * upc_local_alloc  (size_t nblocks, size_t nbytes);
void          upc_free         (shared void       * handle);

size_t        upc_threadof     (shared void * var);
size_t        upc_phaseof      (shared void * var);
shared void * upc_resetphase   (shared void * var);
size_t        upc_addrfield    (shared void * var);
size_t        upc_affinitysize (size_t totalsize, size_t nbytes, size_t tid);

upc_lock_t  * upc_all_lock_alloc    (void);
upc_lock_t  * upc_global_lock_alloc (void);
void          upc_lock_init         (upc_lock_t *lock);
void          upc_lock              (upc_lock_t *lock);
int           upc_lock_attempt      (upc_lock_t *lock);
void          upc_lock_free         (upc_lock_t *lock);
void          upc_unlock            (upc_lock_t *lock);

void          upc_memcpy     (shared void * dst, shared void * src, size_t n);
void          upc_memget     (void        * dst, shared void * src, size_t n);
void          upc_memput     (shared void * dst, void        * src, size_t n);
void          upc_memset     (shared void * dst, int c, size_t n);

#ifdef __cplusplus
};
#endif

#endif /* __UPC_H */
