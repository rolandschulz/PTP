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
 
/*
** Original copyright by Bob Jenkins, December 1996.
*/

/*
 * NOTE: This hash implementation is not synchronized across iteration (HashSet/HashGet). If there are two 
 * threads simultaneously accessing the hash, then one thread may affect the other's access. For instance, 
 * if thread 1 calls HashGet(), then thread 2 calls HashGet(), then the next time thread 1 calls
 * HashGet(), it will get the 3rd element in the hash, not the 2nd. Also, if two threads are accessing 
 * objects in the hash, and freeing data associated with those objects while traversing the hash, then this 
 * may cause problems where thread 1 frees data that thread 2 is about to access.
 * 
 * Applications wishing to use threads to access hashes in this way should provide functions that obtain 
 * and release a global lock on the hash which prevents any other thread from accessing the hash while 
 * that lock is held.
 */

#include <stdio.h>
#include <string.h>
#include <stdlib.h>

#include "compat.h"
#include "hash.h"

THREAD_DECL(hash);

/*
** mix 3 32-bit values reversibly.
** For every delta with one or two bit set, and the deltas of all three
**   high bits or all three low bits, whether the original value of a,b,c
**   is almost all zero or is uniformly distributed,
** If mix() is run forward or backward, at least 32 bits in a,b,c
**  have at least 1/4 probability of changing.
** If mix() is run forward, every bit of c will change between 1/3 and
**  2/3 of the time.  (Well, 22/100 and 78/100 for some 2-bit deltas.)
** mix() was built out of 36 single-cycle latency instructions in a 
**  structure that could supported 2x parallelism, like so:
**      a -= b; 
**      a -= c; x = (c>>13);
**      b -= c; a ^= x;
**      b -= a; x = (a<<8);
**      c -= a; b ^= x;
**      c -= b; x = (b>>13);
**      ...
**  Unfortunately, superscalar Pentiums and Sparcs can't take advantage 
**  of that parallelism.  They've also turned some of those single-cycle
**  latency instructions into multi-cycle latency instructions.  Still,
**  this is the fastest good hash I could find.  There were about 2^^68
**  to choose from.  I only looked at a billion or so.
*/
#define mix(a,b,c) \
{ \
  a -= b; a -= c; a ^= (c>>13); \
  b -= c; b -= a; b ^= (a<<8); \
  c -= a; c -= b; c ^= (b>>13); \
  a -= b; a -= c; a ^= (c>>12);  \
  b -= c; b -= a; b ^= (a<<16); \
  c -= a; c -= b; c ^= (b>>5); \
  a -= b; a -= c; a ^= (c>>3);  \
  b -= c; b -= a; b ^= (a<<10); \
  c -= a; c -= b; c ^= (b>>15); \
}

static void _hgrow(Hash *);

Hash *
HashCreate(int size)
{
	unsigned int	i;
	unsigned int	logsize = -1;
	Hash *	htab;

	htab = (Hash *)malloc(sizeof(Hash));

	/*
	 * Compute log2(size)
	 */
	for (i = (unsigned int)size; i > 0; i >>= 1)
		logsize++;
		
	/*
	** Maximum size is 2^31
	*/
	if ( logsize > BITSPERBYTE * sizeof(unsigned int) - 1 )
		logsize = BITSPERBYTE * sizeof(unsigned int) - 1;
		
	if ( logsize <= 0 )
		logsize = 2;

	htab->logsize = logsize;
	htab->size = (unsigned int)(1 << logsize);
	htab->mask = htab->size - 1;
	htab->count = 0;

	/* allocate memory and zero out */
	htab->table = (HashEntry **)malloc(htab->size * sizeof(HashEntry *));

	for ( i = 0 ; i < htab->size ; i++ )
		htab->table[i] = (HashEntry *)NULL;

	/* everything went alright */
	return htab;
}

void
HashDestroy(Hash *htab,  void (*destroy)(void *))
{
	int		i;
	HashEntry *	h;
	HashEntry *	hn;

	/* Test for correct arguments.  */
	if ( htab == NULL )
		return;

	THREAD_LOCK(hash);
	
	for ( i = 0 ; i < (int)htab->size ; i++ )
	{
		for ( h = htab->table[i] ; h != NULL ; )
		{
			if ( destroy != NULL )
				destroy(h->h_data);

			hn = h->h_next;
			free(h);
			h = hn;
		}
	}

	if (htab->table != NULL)
		free(htab->table);

	free(htab);
	
	THREAD_UNLOCK(hash);
}

/*
** findentry(tab, key, hv) -- hash a variable-length key into a 32-bit value
**
**  key: the key (the unaligned variable-length array of bytes)
**  hv : can be any 4-byte value
**
** Returns a 32-bit value.  Every bit of the key affects every bit of
** the return value.  Every 1-bit and 2-bit delta achieves avalanche.
** About 6len+35 instructions.
**
** The best hash table sizes are powers of 2.  There is no need to do
** mod a prime (mod is sooo slow!).  If you need less than 32 bits,
** use a bitmask.  For example, if you need only 10 bits, do
**   h = (h & hashmask(10));
** In which case, the hash table should have hashsize(10) elements.
** 
** If you are hashing n strings (ub1 **)k, do it like this:
**   for (i=0, h=0; i<n; ++i) h = findentry( k[i], h);
** 
** By Bob Jenkins, 1996.  bob_jenkins@burtleburtle.net.  You may use this
** code any way you wish, private, educational, or commercial.
** 
** See http://burtleburtle.net/bob/hash/evahash.html
** Use for hash table lookup, or anything where one collision in 2^32 is
** acceptable.  Do NOT use for cryptographic purposes.
*/

unsigned int
findentry(char *key, int len, int hv)
{
	unsigned int	a;
	unsigned int	b;
	unsigned int	c;
	unsigned int	l;

	if ( key == NULL )
		return 0;

	/* Set up the internal state */
	a = b = 0x9e3779b9;  /* the golden ratio; an arbitrary value */
	c = hv;           /* the previous hash value */

	/*---------------------------------------- handle most of the key */
	for ( l = len ; l >= 12 ; l -= 12 )
	{
		a += (key[0] +((unsigned int)key[1]<<8) +((unsigned int)key[2]<<16) +((unsigned int)key[3]<<24));
		b += (key[4] +((unsigned int)key[5]<<8) +((unsigned int)key[6]<<16) +((unsigned int)key[7]<<24));
		c += (key[8] +((unsigned int)key[9]<<8) +((unsigned int)key[10]<<16)+((unsigned int)key[11]<<24));

		mix(a,b,c);

		key += 12;
	}

	/*------------------------------------- handle the last 11 bytes */
	c += len;

	switch ( l )              /* all the case statements fall through */
	{
	case 11: c += ((unsigned int)key[10]<<24);
	case 10: c += ((unsigned int)key[9]<<16);
	case 9 : c += ((unsigned int)key[8]<<8);
	/* the first byte of c is reserved for the length */
	case 8 : b += ((unsigned int)key[7]<<24);
	case 7 : b += ((unsigned int)key[6]<<16);
	case 6 : b += ((unsigned int)key[5]<<8);
	case 5 : b += key[4];
	case 4 : a += ((unsigned int)key[3]<<24);
	case 3 : a += ((unsigned int)key[2]<<16);
	case 2 : a += ((unsigned int)key[1]<<8);
	case 1 : a += key[0];
	/* case 0: nothing left to add */
	}

	mix(a,b,c);

	return c;
}

unsigned int 
HashCompute(char *key, int len)
{
	return findentry(key, len, 0);
}

void *
HashSearch(Hash *htab, unsigned int idx)
{
	HashEntry *	h;
	
	THREAD_LOCK(hash);
	
	for ( h = htab->table[idx & htab->mask] ; h != NULL ; h = h->h_next)
	{
		if ( idx == h->h_hval ) {
			THREAD_UNLOCK(hash);
			return h->h_data;
		}
	}
	
	THREAD_UNLOCK(hash);

	return NULL;
}

void *
HashFind(Hash *htab, char *key)
{
	return HashSearch(htab, HashCompute(key, strlen(key)));
}

HashEntry *
HashInsert(Hash *htab, unsigned int idx, void *data)
{
	HashEntry *	h;
	HashEntry **	hp;
	
	THREAD_LOCK(hash);
	
	/*
	** Check if entry already exists
	*/
	for ( h = htab->table[idx & htab->mask] ; h != (HashEntry *)NULL ; h = h->h_next)
	{
		if ( idx == h->h_hval ) {
			THREAD_UNLOCK(hash);
			return NULL;
		}
	}

	/*
	** Grow hash table if it's getting full.
	*/

	if ( ++htab->count > htab->size )
		_hgrow(htab);

	h = (HashEntry *)malloc(sizeof(HashEntry));
	h->h_hval = idx;	
	h->h_data = data;

	hp = &htab->table[idx & htab->mask];
	h->h_next = *hp;
	*hp = h;

	THREAD_UNLOCK(hash);

	return h;
}

void
HashRemove(Hash *htab, unsigned int idx)
{
	HashEntry *		h;
	HashEntry **	hp;
	
	THREAD_LOCK(hash);
	
	/*
	** Find item.
	*/
	for ( hp = &htab->table[idx & htab->mask] ; *hp != NULL && (*hp)->h_hval != idx ; )
		hp = &(*hp)->h_next;

	/*
	 * Found item?
	 */
	if (*hp == NULL) {
		THREAD_UNLOCK(hash);
		return;
	}
	
	/*
	 * Update scan values
	 */
	if (htab->scan_entry == *hp)
		htab->scan_entry = htab->scan_entry->h_next;
	
	/*
	** Remove item.
	*/
	h = *hp;
	*hp = (*hp)->h_next;
	free(h);

	htab->count--;
	
	THREAD_UNLOCK(hash);
}

void
HashSet(Hash *htab)
{
	THREAD_LOCK(hash);
	htab->scan = 0;
	htab->scan_entry = NULL;
	THREAD_UNLOCK(hash);
}

HashEntry *
HashGet(Hash *htab)
{
	HashEntry *	h;
	
	THREAD_LOCK(hash);
	
	if ((h = htab->scan_entry) != NULL) {
		htab->scan_entry = htab->scan_entry->h_next;
		THREAD_UNLOCK(hash);
		return h;
	}
	
	while (htab->scan < htab->size) {
		if ((h = htab->table[htab->scan++]) != NULL) {
			htab->scan_entry = h->h_next;
			THREAD_UNLOCK(hash);
			return h;
		}
	}
	
	THREAD_UNLOCK(hash);
	
	return NULL;
}	

static void
_hgrow(Hash *htab)
{
	unsigned int	i;
	unsigned int	nsize;
	unsigned int	nmask;
	HashEntry **	ntab;

	nsize = (unsigned int)(1 << ++htab->logsize);
	nmask = nsize - 1;
	ntab = (HashEntry **)malloc(nsize * sizeof(HashEntry *));

	for ( i = 0 ; i < nsize ; i++ )
		ntab[i] = (HashEntry *)NULL;

	/*
	** Move old entries to new table.
	*/
	for ( i = 0 ; i < htab->size ; i++ )
	{
		HashEntry *		h;
		HashEntry *		h2;
		HashEntry **	hp;

		for ( h = htab->table[i] ; h != (HashEntry *)NULL ; )
		{
			h2 = h;
			h = h->h_next;

			hp = &ntab[h2->h_hval & nmask];
			h2->h_next = *hp;
			*hp = h2;
		}
	}

	free(htab->table);

	htab->table = ntab;
	htab->size = nsize;
	htab->mask = nmask;
}
