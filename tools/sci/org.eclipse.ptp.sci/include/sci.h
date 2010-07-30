#ifndef _SCI_H
#define _SCI_H
/***************************************************************************
"%Z% %I% %W% %D% %T%\0"
 Name: sci.h

 Description:

* Copyright (c) 2008, 2010 IBM Corporation.
* All rights reserved. This program and the accompanying materials
* are made available under the terms of the Eclipse Public License v1.0s
* which accompanies this distribution, and is available at
* http://www.eclipse.org/legal/epl-v10.html

***************************************************************************/

/*
** SCI Return/Error Codes
*/
#define SCI_SUCCESS                  (0)
#define SCI_ERR_INVALID_HOSTFILE     (-2001)
#define SCI_ERR_INVALID_ENDTYPE      (-2002)   
#define SCI_ERR_INITIALIZE_FAILED    (-2003)
#define SCI_ERR_INVALID_CALLER       (-2004)
#define SCI_ERR_GROUP_NOTFOUND       (-2005)
#define SCI_ERR_FILTER_NOTFOUND      (-2006)
#define SCI_ERR_INVALID_FILTER       (-2007)
#define SCI_ERR_BACKEND_NOTFOUND     (-2008)
#define SCI_ERR_UNKNOWN_INFO         (-2009)
#define SCI_ERR_UNINTIALIZED         (-2010)
#define SCI_ERR_GROUP_PREDEFINED     (-2011)
#define SCI_ERR_GROUP_EMPTY          (-2012)
#define SCI_ERR_INVALID_OPERATOR     (-2013)
#define SCI_ERR_FILTER_PREDEFINED    (-2014)
#define SCI_ERR_POLL_TIMEOUT         (-2015)
#define SCI_ERR_INVALID_JOBKEY       (-2016)
#define SCI_ERR_MODE                 (-2017)
#define SCI_ERR_FILTER_ID            (-2018)
#define SCI_ERR_INVALID_SUCCESSOR    (-2019)
#define SCI_ERR_BACKEND_EXISTED      (-2020)
#define SCI_ERR_NO_MEM               (-2021)
#define SCI_ERR_LAUNCH_FAILED        (-2022)
#define SCI_ERR_POLL_INVALID         (-2023)
#define SCI_ERR_INVALID_USER         (-2024)

#define SCI_ERR_PARENT_BROKEN        (-5000)
#define SCI_ERR_CHILD_BROKEN         (-5001)
#define SCI_ERR_RECOVERED            (-5002)
#define SCI_ERR_RECOVER_FAILED       (-5003)

/*
** SCI Structures and typedefs
*/
typedef int sci_group_t;

/*
** SCI Bcast & Upload message handler
*/
typedef int (SCI_msg_hndlr)(void *user_param, sci_group_t group, void *buf, int size);

/*
** SCI Error message handler
*/
typedef int (SCI_err_hndlr)(int err_code, int node_id, int num_bes);

/*
** SCI Filter message handler
*/
typedef int (filter_init_hndlr)(void **user_param);
typedef int (filter_input_hndlr)(void *user_param, sci_group_t group, void *buf, int size);
typedef int (filter_term_hndlr)(void *user_param);

/*
** SCI Predefined groups
*/
#define SCI_GROUP_ALL -1

/*
** SCI Predefined filter IDs
*/
#define SCI_FILTER_NULL -1

#pragma enum (int)
typedef enum {
    SCI_FRONT_END,
    SCI_BACK_END
} sci_end_type_t;

typedef enum {
    SCI_INTERRUPT,
    SCI_POLLING
} sci_mode_t;
#pragma enum (pop)

typedef struct {
    int              filter_id;
    char             *so_file;
} sci_filter_info_t;

typedef struct {
    int                 num;
    sci_filter_info_t   *filters;
} sci_filter_list_t;

typedef struct {
    sci_end_type_t       type;
    sci_mode_t           mode;
    SCI_msg_hndlr        *hndlr;
    void                 *param;
    SCI_err_hndlr        *err_hndlr;
    char                 *hostfile;
    char                 *bepath;
    char                 **beenvp;
    sci_filter_list_t    filter_list;    
    char                 reserve[52];
} sci_fe_info_t;

typedef struct {
    sci_end_type_t   type;
    sci_mode_t       mode;
    SCI_msg_hndlr    *hndlr;
    void             *param;
    SCI_err_hndlr    *err_hndlr;
    char             reserve[64];
} sci_be_info_t;

typedef union {
    sci_end_type_t   type;
    sci_fe_info_t    fe_info;
    sci_be_info_t    be_info;
} sci_info_t;

typedef struct {
    int              id;
    char             *hostname;
    int              level;
} sci_be_t;

typedef enum {
    JOB_KEY,
    NUM_BACKENDS,
    BACKEND_ID,
    POLLING_FD,
    NUM_FILTERS,
    FILTER_IDLIST,
    AGENT_ID,
    NUM_SUCCESSORS,
    SUCCESSOR_IDLIST,
    HEALTH_STATUS,
    AGENT_LEVEL
} sci_query_t;

typedef enum {
    GROUP_MEMBER_NUM,
    GROUP_MEMBER,
    GROUP_SUCCESSOR_NUM,
    GROUP_SUCCESSOR
} sci_group_query_t;

typedef enum {
    SCI_UNION,
    SCI_INTERSECTION,
    SCI_DIFFERENCE
} sci_op_t;


#ifdef __cplusplus
extern "C" {
#endif
/*
***************************************************************
****************** SCI C Externalized API's ******************
***************************************************************
*/
/*
** SCI Environment setup/terminate/query functions.
*/
int SCI_Initialize(sci_info_t *info);
int SCI_Terminate();
int SCI_Query(sci_query_t query, void *ret_val);

/*
** SCI Communication functions.
*/
int SCI_Bcast(int filter_id, sci_group_t group, int num_bufs, void *bufs[], int sizes[]);
int SCI_Upload(int filter_id, sci_group_t group, int num_bufs, void *bufs[], int sizes[]);
int SCI_Poll(int timeout);

/*
** SCI Group manipulation functions.
*/
int SCI_Group_create(int num_bes, int *be_list, sci_group_t *group);
int SCI_Group_free(sci_group_t group);
int SCI_Group_operate(sci_group_t group1, sci_group_t group2,
        sci_op_t op, sci_group_t *newgroup);
int SCI_Group_operate_ext(sci_group_t group, int num_bes, int *be_list, 
        sci_op_t op, sci_group_t *newgroup);
int SCI_Group_query(sci_group_t group, sci_group_query_t query, void *ret_val);

/*
** SCI Filter related functions.
*/
int SCI_Filter_load(sci_filter_info_t *filter_info); 
int SCI_Filter_unload(int filter_id);
int SCI_Filter_bcast(int filter_id, int num_successors, int *successor_list, int num_bufs,
        void *bufs[], int sizes[]);
int SCI_Filter_upload(int filter_id, sci_group_t group, int num_bufs, void *bufs[], int sizes[]);

/*
** SCI Dynamic add/remove back end.
*/
int SCI_BE_add(sci_be_t *be);
int SCI_BE_remove(int be_id);

#ifdef __cplusplus
}
#endif

#endif

