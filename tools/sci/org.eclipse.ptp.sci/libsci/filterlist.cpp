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

 Classes: FilterList

 Description: Filter management (Note: STL does not guarantee the safety of 
              several readers & one writer cowork together, and user threads
              can query filter information at runtime, so it's necessary 
              to add a lock to protect these read & write operations).
   
 Author: Nicole Nie

 History:
   Date     Who ID    Description
   -------- --- ---   -----------
   05/24/08 nieyy        Initial code (F156654)

****************************************************************************/

#include "filterlist.hpp"
#include <assert.h>

#include "filter.hpp"

FilterList* FilterList::instance = NULL;
FilterList * FilterList::getInstance()
{
    if (instance == NULL) {
        instance = new FilterList();
    }
    return instance;
}

FilterList::FilterList()
{
    filterInfo.clear();

    ::pthread_mutex_init(&mtx, NULL);
}

FilterList::~FilterList()
{
    // delete all loaded filters
    FILTER_MAP::iterator fit = filterInfo.begin();
    for (; fit != filterInfo.end(); fit++) {
        delete (*fit).second;
    }
    filterInfo.clear();

    ::pthread_mutex_destroy(&mtx);

    instance = NULL;
}

int FilterList::loadFilter(int filter_id, Filter * filter, bool invoke)
{
    int rc = SCI_SUCCESS;
    if (invoke) {
        // call init func
        rc = filter->load();
    }
    
    if (rc == SCI_SUCCESS) {
        lock();
        filterInfo[filter_id] = filter;
        unlock();
    }

    return rc;
}

Filter * FilterList::getFilter(int filter_id)
{
    Filter *filter = NULL;

    lock();
    FILTER_MAP::iterator fit = filterInfo.find(filter_id);
    if (fit != filterInfo.end()) {
        filter = (*fit).second;
    }
    unlock();

    return filter;
}

int FilterList::unloadFilter(int filter_id, bool invoke)
{
    Filter *filter = NULL;

    lock();
    FILTER_MAP::iterator fit = filterInfo.find(filter_id);
    if (fit != filterInfo.end()) {
        filter = (*fit).second;
    } else {
        unlock();
        return SCI_ERR_FILTER_NOTFOUND;
    }

    filterInfo.erase(filter_id);
    unlock();
    
    int rc = SCI_SUCCESS;
    if (invoke) {
        // call term_func
        rc = filter->unload();
    }
    delete filter;
    
    return rc;
}

int FilterList::numOfFilters()
{
    int size;

    lock();
    size = filterInfo.size();
    unlock();

    return size;
}

void FilterList::retrieveFilterList(int * ret_val)
{
    int i = 0;

    lock();
    FILTER_MAP::iterator it = filterInfo.begin();
    for (; it!=filterInfo.end(); ++it) {
        ret_val[i++] = (*it).first;
    }
    unlock();
}

void FilterList::lock()
{
    ::pthread_mutex_lock(&mtx);
}

void FilterList::unlock()
{
    ::pthread_mutex_unlock(&mtx);
}

