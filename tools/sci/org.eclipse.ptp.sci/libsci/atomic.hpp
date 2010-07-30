#ifndef _PRAGMA_COPYRIGHT_
#define _PRAGMA_COPYRIGHT_
#pragma comment(copyright, "%Z% %I% %W% %D% %T%\0")
#endif /* _PRAGMA_COPYRIGHT_ */
/****************************************************************************

* Copyright (c) 2008, 2010 IBM Corporation.
* All rights reserved. This program and the accompanying materials
* are made available under the terms of the Eclipse Public License v1.0s
* which accompanies this distribution, and is available at
* http://www.eclipse.org/legal/epl-v10.html

 Classes: None

 Description: Atomic operations
   
 Author: Nicole Nie

 History:
   Date     Who ID    Description
   -------- --- ---   -----------
   04/01/09 nieyy        Initial code (From LAPI)

****************************************************************************/

#ifndef _ATOMIC_HPP
#define _ATOMIC_HPP

#ifdef HAVE_CONFIG_H
#include "config.h"
#endif
#include <assert.h>
#include <pthread.h>

#ifdef _SCI_LINUX // Linux

/**********************************************************************
 *
 *  Atomic operations
 *
 **********************************************************************/
typedef int          *atomic_p;
typedef long long   *atomic_l;
typedef int          boolean_t;
typedef unsigned int uint;

#ifndef __64BIT__
static __inline__ boolean_t compare_and_swaplp(atomic_l v, int *old, int new_v)
{
    assert(0);
}
static __inline__ int  fetch_and_addlp(atomic_l v, int a) { assert(0); }
static __inline__ uint fetch_and_andlp(atomic_l v, uint a) { assert(0); }
static __inline__ uint fetch_and_orlp(atomic_l v, uint a) { assert(0); }
#endif


#ifdef POWER_ARCH

/*
  For Power architecture, isync is only necessary when entering a 
  critical section to discard any instruction prefetch and possible
  execution on stale data. 

  It's handy to put it in _check_lock but not other routines.
*/

static __inline__ 
int compare_and_swap(atomic_p dest, int *comp_addr, int exch)
{
    int        old, comp;
    boolean_t  rc; 

    __asm__ __volatile__(
            "1: lwarx   %[old], 0, %[dest]       \n\t"
            "   lwz     %[comp], 0(%[comp_addr]) \n\t"
            "   cmplw   cr0, %[old], %[comp]     \n\t"
            "   bne-    2f                       \n\t"
            "   li      %[rc], 1                 \n\t"
            "   stwcx.  %[exch], 0, %[dest]      \n\t"
            "   beq-    3f                       \n\t"
            "   b       1b                       \n\t"
            "2: li      %[rc], 0                 \n\t"
            "   stw     %[old], 0(%[comp_addr])  \n\t"
            "3:                                  \n\t"
            : [old] "=&r" (old), [comp] "=&r" (comp), [rc] "=&r" (rc)
            : [dest] "r" (dest), [comp_addr] "r" (comp_addr), [exch] "r" (exch)
            : "%0", "cc", "memory");
    return rc;
}

static __inline__ 
int fetch_and_add(atomic_p dest, int val)
{
    int old, sum;
    __asm__ __volatile__(
            "1: lwarx   %[old], 0, %[dest]      \n\t"
            "   add     %[sum], %[old], %[val]  \n\t"
            "   stwcx.  %[sum], 0, %[dest]      \n\t"
            "   bne-    1b                      \n\t"
            : [sum] "=&r" (sum), [old] "=&r" (old)
            : [val] "r" (val), [dest] "r" (dest)
            : "%0", "cc", "memory");
    return old;
}

static __inline__ 
uint fetch_and_and(atomic_p dest, uint val)
{
    int old, sum;
    __asm__ __volatile__(
            "1: lwarx   %[old], 0, %[dest]      \n\t"
            "   and     %[sum], %[old], %[val]  \n\t"
            "   stwcx.  %[sum], 0, %[dest]      \n\t"
            "   bne-    1b                      \n\t"
            : [sum] "=&r" (sum), [old] "=&r" (old)
            : [val] "r" (val), [dest] "r" (dest)
            : "%0", "cc", "memory");
    return old;
}

static __inline__ 
uint fetch_and_or(atomic_p dest, uint val)
{
    int old, sum;
    __asm__ __volatile__(
            "1: lwarx   %[old], 0, %[dest]      \n\t"
            "   or      %[sum], %[old], %[val]  \n\t"
            "   stwcx.  %[sum], 0, %[dest]      \n\t"
            "   bne-    1b                      \n\t"
            : [sum] "=&r" (sum), [old] "=&r" (old)
            : [val] "r" (val), [dest] "r" (dest)
            : "%0", "cc", "memory");
    return old;
}

#ifdef __64BIT__
static __inline__
int compare_and_swaplp(atomic_l dest, long long *comp_addr, long long exch)
{
    long long  old, comp;
    boolean_t  rc;

    __asm__ __volatile__(
            "1: ldarx   %[old], 0, %[dest]       \n\t"
            "   ld     %[comp], 0(%[comp_addr]) \n\t"
            "   cmpld   cr0, %[old], %[comp]     \n\t"
            "   bne-    2f                       \n\t"
            "   li      %[rc], 1                 \n\t"
            "   stdcx.  %[exch], 0, %[dest]      \n\t"
            "   beq-    3f                       \n\t"
            "   b       1b                       \n\t"
            "2: li      %[rc], 0                 \n\t"
            "   std     %[old], 0(%[comp_addr])  \n\t"
            "3:                                  \n\t"
            : [old] "=&r" (old), [comp] "=&r" (comp), [rc] "=&r" (rc)
            : [dest] "r" (dest), [comp_addr] "r" (comp_addr), [exch] "r" (exch)
            : "%0", "cc", "memory");

    return rc;
}

static __inline__
long long fetch_and_addlp(atomic_l dest, long long val)
{
    long long old, sum;
    __asm__ __volatile__(
            "1: ldarx   %[old], 0,  %[dest]     \n\t"
            "   add     %[sum], %[old], %[val]  \n\t"
            "   stdcx.  %[sum], 0, %[dest]      \n\t"
            "   bne-    1b                      \n\t"
            : [sum] "=&r" (sum), [old] "=&r" (old)
            : [val] "r" (val), [dest] "r" (dest)
            : "%0", "cc", "memory");

    return old;
}

static __inline__
unsigned long long fetch_and_andlp(atomic_l dest, unsigned long long val)
{
    long long old, sum;
    __asm__ __volatile__(
            "1: ldarx   %[old], 0, %[dest]      \n\t"
            "   and     %[sum], %[old], %[val]  \n\t"
            "   stdcx.  %[sum], 0, %[dest]      \n\t"
            "   bne-    1b                      \n\t"
            : [sum] "=&r" (sum), [old] "=&r" (old)
            : [val] "r" (val), [dest] "r" (dest)
            : "%0", "cc", "memory");
    return old;
}

static __inline__
unsigned long long fetch_and_orlp(atomic_l dest, unsigned long long val)
{
    long old, sum;
    __asm__ __volatile__(
            "1: ldarx   %[old], 0, %[dest]      \n\t"
            "   or      %[sum], %[old], %[val]  \n\t"
            "   stdcx.  %[sum], 0, %[dest]      \n\t"
            "   bne-    1b                      \n\t"
            : [sum] "=&r" (sum), [old] "=&r" (old)
            : [val] "r" (val), [dest] "r" (dest)
            : "%0", "cc", "memory");
    return old;
}

#endif

static __inline__ 
boolean_t _check_lock(atomic_p dest, int comp, int exch)
{
    int        old;
    boolean_t  rc; 

    __asm__ __volatile__(
            "1: lwarx   %[old], 0, %[dest]    \n\t"
            "   cmplw   cr0, %[old], %[comp]  \n\t"
            "   bne-    2f                    \n\t"
            "   li      %[rc], 0              \n\t"
            "   stwcx.  %[exch], 0, %[dest]   \n\t"
            "   beq-    3f                    \n\t"
            "   b       1b                    \n\t"
            "2: li      %[rc], 1              \n\t"
            "3: isync                         \n\t"
            : [old] "=&r" (old), [rc] "=&r" (rc)
            : [dest] "r" (dest), [comp] "r" (comp), [exch] "r" (exch)
            : "%0", "cc", "memory");

    return rc;
}

static __inline__ 
void _clear_lock(atomic_p ptr, int new_val)
{
        __asm__ __volatile__("lwsync");
        *ptr = new_val;
}

#endif /* POWER_ARCH */

#ifdef INTEL_ARCH
/*
 Note: Inlining cmpxchg2 and compare_and_swap doesn't generate
       correct code!
 */
static //__inline__ 
boolean_t cmpxchg2(atomic_p dest, int comp, int exch)
{
    unsigned int old;
    __asm__ __volatile__(
        "lock; cmpxchgl %[exch], %[dest]"
        : [dest] "=m" (*dest), "=a" (old)
        : [exch] "r" (exch), "m"  (*dest), "a"  (comp)
        : "memory" );
    return (old == comp);
}

static //__inline__
boolean_t compare_and_swap(atomic_p dest, int *comp_addr, int exch)
{
    unsigned int old;
    __asm__ __volatile__(
        "lock; cmpxchgl %[exch], %[dest]"
        : [dest] "=m" (*dest), "=a" (old)
        : [exch] "r" (exch), "m" (*dest), "a" (*comp_addr)
        : "memory" );
    if (*comp_addr == old)
        return 1;
    else {
        *comp_addr = old;
        return 0;
    }
}

static __inline__ 
int fetch_and_add(atomic_p ptr, int val)
{
    int prev;
    do prev = *ptr;
    while (!cmpxchg2(ptr, prev, (prev+val)));
    return prev;
}

static __inline__ 
uint fetch_and_and(atomic_p ptr, uint val)
{
    uint prev;
    do prev = *ptr;
    while (!cmpxchg2(ptr, prev, (prev&val)));
    return prev;
}

static __inline__ 
uint fetch_and_or(atomic_p ptr, uint val)
{
    uint prev;
    do prev = *ptr;
    while (!cmpxchg2(ptr, prev, (prev|val)));
    return prev;
}

#ifdef __64BIT__
static 
boolean_t cmpxchg2lp(atomic_l dest, long long comp, long long exch)
{
    unsigned long long old;
    __asm__ __volatile__(
        "lock; cmpxchgq %[exch], %[dest]"
        : [dest] "=m" (*dest), "=a" (old)
        : [exch] "r" (exch), "m"  (*dest), "a"  (comp)
        : "memory" );
    return (old == comp);
}

static //__inline__
boolean_t compare_and_swaplp(atomic_l dest, long long *comp_addr, long long exch)
{
    unsigned long long old;
    __asm__ __volatile__(
        "lock; cmpxchgq %[exch], %[dest]"
        : [dest] "=m" (*dest), "=a" (old)
        : [exch] "r" (exch), "m" (*dest), "a" (*comp_addr)
        : "memory" );
    if (*comp_addr == old)
        return 1;
    else {
        *comp_addr = old;
        return 0;
    }
}

static __inline__
long long fetch_and_addlp(atomic_l ptr, long long val)
{
    long long prev;
    do prev = *ptr;
    while (!cmpxchg2lp(ptr, prev, (prev+val)));
    return prev;
}

static __inline__
unsigned long long fetch_and_andlp(atomic_l ptr, unsigned long long val)
{
    unsigned long long prev;
    do prev = *ptr;
    while (!cmpxchg2lp(ptr, prev, (prev&val)));
    return prev;
}

static __inline__
unsigned long long fetch_and_orlp(atomic_l ptr, unsigned long long val)
{
    unsigned long long prev;
    do prev = *ptr;
    while (!cmpxchg2lp(ptr, prev, (prev|val)));
    return prev;
}

#endif

static __inline__ 
boolean_t _check_lock(atomic_p ptr, int old_val, int new_val)
{
    return !cmpxchg2(ptr, old_val, new_val);
}

static __inline__ 
void _clear_lock(atomic_p ptr, int new_val)
{
    *ptr = new_val;
}

#endif /* INTEL_ARCH */


/**********************************************************************
 *
 *  Sync. functions
 *
 **********************************************************************/

static __inline__ void lwsync()
{
#ifdef POWER_ARCH
    __asm__ __volatile__ ("lwsync");
#endif /* POWER_ARCH */
}

static __inline__ void hwsync()
{
#ifdef POWER_ARCH
    __asm__ __volatile__ ("sync");
#endif /* POWER_ARCH */
}

static __inline__ void isync()
{
#ifdef POWER_ARCH
    __asm__ __volatile__ ("isync");
#endif /* POWER_ARCH */
}

/**********************************************************************
 *
 *  Lock functions
 *
 **********************************************************************/

typedef pthread_t lw_mutex_t;

extern "C" {
#ifdef POWER_ARCH
static __inline__ 
void lw_mutex_lock(lw_mutex_t *lock, pthread_t tid)
{
    int   val;
    __asm__ __volatile__ (
            "1: lwarx  %[val], 0, %[lock]  \n\t"
            "   cmpwi  %[val], 0           \n\t"
            "   bne-   1b                  \n\t"
            "   stwcx. %[tid], 0, %[lock]  \n\t"
            "   bne-   1b                  \n\t"
            "   isync                      \n\t"
            : [val] "=&r" (val)
            : [lock] "r" (lock), [tid] "r" (tid)
            : "%0", "cc", "memory");
}

static __inline__ 
int  lw_mutex_trylock(lw_mutex_t *lock, pthread_t tid)
{
    int    val;
    __asm__ __volatile__ (
            "1: lwarx  %[val], 0, %[lock]  \n\t"
            "   cmpwi  %[val], 0           \n\t"
            "   bne-   2f                  \n\t"
            "   stwcx. %[tid], 0, %[lock]  \n\t"
            "   bne-   1b                  \n\t"
            "   isync                      \n\t"
            "2:                            \n\t"  
            : [val] "=&r" (val)
            : [lock] "r" (lock), [tid] "r" (tid)
            : "%0", "cc", "memory");
    return val;
}
#endif /* POWER_ARCH */

#ifdef INTEL_ARCH
static __inline__ 
void lw_mutex_lock(lw_mutex_t *lock, pthread_t tid)
{
    while (_check_lock((atomic_p)lock, 0, tid));
}

static __inline__ 
int  lw_mutex_trylock(lw_mutex_t *lock, pthread_t tid)
{
    return _check_lock((atomic_p)lock, 0, tid);
}
#endif /* INTEL_ARCH */

static __inline__ 
void lw_mutex_unlock(lw_mutex_t *lock)
{
    lwsync();
    *lock = 0;
}
}

/**********************************************************************
 *
 *  Misc. functions
 *
 **********************************************************************/

static __inline__ int _count_leading_zeros(unsigned word)
{
    int res;
#ifdef INTEL_ARCH
    if (!word)
        return 32;
    __asm__("bsr %[res], %[word]" : [res] "=d" (res): [word] "d" (word));
    return (31-res);
#endif /* INTEL_ARCH */
#ifdef POWER_ARCH
    __asm__ ("cntlzw %[res], %[word]" : [res] "=r" (res) : [word] "r" (word));
    return res;
#endif /* POWER_ARCH */
}

#else // AIX

#include <sys/atomic_op.h>

typedef pthread_t   lw_mutex_t;

extern "C" {
void lw_mutex_lock(lw_mutex_t *lock, pthread_t tid);
void lw_mutex_unlock(lw_mutex_t *lock);
int  lw_mutex_trylock(lw_mutex_t *lock, pthread_t tid);
}

#endif

#endif

