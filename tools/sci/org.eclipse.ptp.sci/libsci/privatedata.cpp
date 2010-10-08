#include "privatedata.hpp"
#include "filterproc.hpp"
#include "routerproc.hpp"
#include "routinglist.hpp"
#include "filterlist.hpp"

PrivateData::PrivateData(RoutingList *rt, FilterList *fl, FilterProcessor *fp, RouterProcessor *rp)
    : routingList(rt), filterList(fl), routerProc(rp), filterProc(fp)
{
}

PrivateData::~PrivateData()
{
}

FilterProcessor * PrivateData::getFilterProcessor()
{
    return filterProc;
}

RouterProcessor * PrivateData::getRouterProcessor()
{
    return routerProc;
}

RoutingList * PrivateData::getRoutingList()
{
    return routingList;
}

Topology * PrivateData::getTopology()
{
    return routingList->getTopology();
}

FilterList * PrivateData::getFilterList()
{
    return filterList;
}

