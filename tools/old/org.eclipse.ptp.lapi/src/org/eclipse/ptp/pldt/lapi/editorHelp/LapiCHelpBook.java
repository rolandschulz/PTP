/**********************************************************************
 * Copyright (c) 2007 IBM Corporation.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.ptp.pldt.lapi.editorHelp;

import org.eclipse.ptp.pldt.common.editorHelp.CHelpBookImpl;
import org.eclipse.ptp.pldt.common.editorHelp.FunctionPrototypeSummaryImpl;
import org.eclipse.ptp.pldt.common.editorHelp.FunctionSummaryImpl;
import org.eclipse.ptp.pldt.lapi.LapiPlugin;


public class LapiCHelpBook extends CHelpBookImpl {
	private static final String TITLE = "LAPI C Help Book";

	@SuppressWarnings("unchecked")
	public LapiCHelpBook() {
        super(LapiPlugin.getPluginId());
		// populate func map
        funcName2FuncInfo.put("LAPI_Addr_get", new FunctionSummaryImpl("LAPI_Addr_get", "", "Retrieves a function address that was previously registered using LAPI_Addr_set.",
                new FunctionPrototypeSummaryImpl("LAPI_Addr_get", "int", "lapi_handle_t hndl, void **addr, int addr_hndl"), null));
        funcName2FuncInfo.put("LAPI_Addr_set", new FunctionSummaryImpl("LAPI_Addr_set", "", "Registers the address of a function.",
                new FunctionPrototypeSummaryImpl("LAPI_Addr_set", "int", "lapi_handle_t hndl, void **addr, int addr_hndl"), null));
        funcName2FuncInfo.put("LAPI_Address", new FunctionSummaryImpl("LAPI_Address", "", "Returns an unsigned long value for a specified user address.",
                new FunctionPrototypeSummaryImpl("LAPI_Address", "int", "void  *my_addr, ulong *ret_addr"), null));
        funcName2FuncInfo.put("LAPI_Address_init", new FunctionSummaryImpl("LAPI_Address_init", "", "Creates a remote address table.",
                new FunctionPrototypeSummaryImpl("LAPI_Address_init", "int", "lapi_handle_t  hndl, void *my_addr, void *add_tab[ ]"), null));
        funcName2FuncInfo.put("LAPI_Address_init64", new FunctionSummaryImpl("LAPI_Address_init64", "", "Creates a 64-bit remote address table.",
                new FunctionPrototypeSummaryImpl("LAPI_Address_init64", "int", "lapi_handle_t  hndl, lapi_long_t my_addr, lapi_long_t *add_tab"), null));
        funcName2FuncInfo.put("LAPI_Amsend", new FunctionSummaryImpl("LAPI_Amsend", "", "Transfers a user message to a remote task, obtaining the target address on the remote task from a user-specified header handler.",
                new FunctionPrototypeSummaryImpl("LAPI_Amsend", "int", "lapi_handle_t hndl, uint tgt, void *hdr_hdl, void *uhdr, uint uhdr_len, void *udata, ulong udata_len, lapi_cntr_t *tgt_cntr, lapi_cntr_t *org_cntr, lapi_cntr_t *cmpl_cntr"), null));
        funcName2FuncInfo.put("LAPI_Amsendv", new FunctionSummaryImpl("LAPI_Amsendv", "", "Transfers a user vector to a remote task, obtaining the target address on the remote task from a user-specified header handler.",
                new FunctionPrototypeSummaryImpl("LAPI_Amsendv", "int", "lapi_handle_t hndl, uint tgt, void *hdr_hdl, void *uhdr, uint uhdr_len, lapi_vec_t *org_vec, lapi_cntr_t *tgt_cntr, lapi_cntr_t *org_cntr, lapi_cntr_t *cmpl_cntr"), null));
        funcName2FuncInfo.put("LAPI_Fence", new FunctionSummaryImpl("LAPI_Fence", "", "Enforces order on LAPI calls.",
                new FunctionPrototypeSummaryImpl("LAPI_Fence", "int", "lapi_handle_t hndl"), null));
        funcName2FuncInfo.put("LAPI_Get", new FunctionSummaryImpl("LAPI_Get", "", "Copies data from a remote task to a local task.",
                new FunctionPrototypeSummaryImpl("LAPI_Get", "int", "lapi_handle_t hndl, uint tgt, ulong len, void *tgt_addr, void *org_addr, lapi_cntr_t *tgt_cntr, lapi_cntr_t *org_cntr"), null));
        funcName2FuncInfo.put("LAPI_Getcntr", new FunctionSummaryImpl("LAPI_Getcntr", "", "Gets the integer value of a specified LAPI counter.",
                new FunctionPrototypeSummaryImpl("LAPI_Getcntr", "int", "lapi_handle_t hndl, lapi_cntr_t *cntr, int *val"), null));
        funcName2FuncInfo.put("LAPI_Getv", new FunctionSummaryImpl("LAPI_Getv", "", "Copies vectors of data from a remote task to a local task.",
                new FunctionPrototypeSummaryImpl("LAPI_Getv", "int", "lapi_handle_t hndl, uint tgt, lapi_vec_t *tgt_vec, lapi_vec_t *org_vec, lapi_cntr_t *tgt_cntr, lapi_cntr_t *org_cntr"), null));
        funcName2FuncInfo.put("LAPI_Gfence", new FunctionSummaryImpl("LAPI_Gfence", "", "Enforces order on LAPI calls across all tasks and provides barrier synchronization among them.",
                new FunctionPrototypeSummaryImpl("LAPI_Gfence", "int", "lapi_handle_t hndl"), null));
        funcName2FuncInfo.put("LAPI_Init", new FunctionSummaryImpl("LAPI_Init", "", "Initializes a LAPI context.",
                new FunctionPrototypeSummaryImpl("LAPI_Init", "int", "lapi_handle_t *hndl, lapi_info_t *lapi_info"), null));
        funcName2FuncInfo.put("LAPI_Msg_string", new FunctionSummaryImpl("LAPI_Msg_string", "", "Retrieves the message that is associated with a subroutine return code.",
                new FunctionPrototypeSummaryImpl("LAPI_Msg_string", "int", "int error_code, void *buf"), null));
        funcName2FuncInfo.put("LAPI_Msgpoll", new FunctionSummaryImpl("LAPI_Msgpoll", "", "Allows the calling thread to check communication progress.",
                new FunctionPrototypeSummaryImpl("LAPI_Msgpoll", "int", "lapi_handle_t hndl, uint cnt, lapi_msg_info_t  *info"), null));
        funcName2FuncInfo.put("LAPI_Probe", new FunctionSummaryImpl("LAPI_Probe", "", "Transfers control to the communication subsystem to check for arriving messages and to make progress in polling mode.",
                new FunctionPrototypeSummaryImpl("LAPI_Probe", "int", "lapi_handle_t hndl"), null));
        funcName2FuncInfo.put("LAPI_Put", new FunctionSummaryImpl("LAPI_Put", "", "Transfers data from a local task to a remote task.",
                new FunctionPrototypeSummaryImpl("LAPI_Put", "int", "lapi_handle_t hndl, uint tgt, ulong len, void *tgt_addr, void *org_addr, lapi_cntr_t *tgt_cntr, lapi_cntr_t *org_cntr, lapi_cntr_t *cmpl_cntr"), null));
        funcName2FuncInfo.put("LAPI_Putv", new FunctionSummaryImpl("LAPI_Putv", "", "Transfers vectors of data from a local task to a remote task.",
                new FunctionPrototypeSummaryImpl("LAPI_Putv", "int", "lapi_handle_t hndl, uint tgt, lapi_vec_t *tgt_vec, lapi_vec_t *org_vec, lapi_cntr_t *tgt_cntr, lapi_cntr_t *org_cntr, lapi_cntr_t *cmpl_cntr"), null));
        funcName2FuncInfo.put("LAPI_Qenv", new FunctionSummaryImpl("LAPI_Qenv", "", "Used to query LAPI for runtime task information.",
                new FunctionPrototypeSummaryImpl("LAPI_Qenv", "int", "lapi_handle_t hndl, lapi_query_t query, int *ret_val"), null));
        funcName2FuncInfo.put("LAPI_Rmw", new FunctionSummaryImpl("LAPI_Rmw", "", "Provides data synchronization primitives.",
                new FunctionPrototypeSummaryImpl("LAPI_Rmw", "int", "lapi_handle_t hndl, RMW_ops_t op, uint tgt, int *tgt_var, int *in_val, int *prev_tgt_val, lapi_cntr_t *org_cntr"), null));
        funcName2FuncInfo.put("LAPI_Rmw64", new FunctionSummaryImpl("LAPI_Rmw64", "", "Provides data synchronization primitives for 64-bit applications.",
                new FunctionPrototypeSummaryImpl("LAPI_Rmw64", "int", "lapi_handle_t hndl, Rmw_ops_t op, uint tgt, long long *tgt_var, long long *in_val, long long *prev_tgt_val, lapi_cntr_t *org_cntr"), null));
        funcName2FuncInfo.put("LAPI_Senv", new FunctionSummaryImpl("LAPI_Senv", "", "Used to set a runtime variable.",
                new FunctionPrototypeSummaryImpl("LAPI_Senv", "int", "lapi_handle_t hndl, lapi_query_t query, int set_val"), null));
        funcName2FuncInfo.put("LAPI_Setcntr", new FunctionSummaryImpl("LAPI_Setcntr", "", "Used to set a counter to a specified value.",
                new FunctionPrototypeSummaryImpl("LAPI_Setcntr", "int", "lapi_handle_t hndl, lapi_cntr_t *cntr, int val"), null));
        funcName2FuncInfo.put("LAPI_Term", new FunctionSummaryImpl("LAPI_Term", "", "Terminates and cleans up a LAPI context.",
                new FunctionPrototypeSummaryImpl("LAPI_Term", "int", "lapi_handle_t hndl"), null));
        funcName2FuncInfo.put("LAPI_Util", new FunctionSummaryImpl("LAPI_Util", "", "DESC",
                new FunctionPrototypeSummaryImpl("LAPI_Util", "int", "lapi_handle_t hndl, lapi_util_t *util_cmd"), null));
        funcName2FuncInfo.put("LAPI_Waitcntr", new FunctionSummaryImpl("LAPI_Waitcntr", "", "Waits until a specified counter reaches the value specified.",
    		  	new FunctionPrototypeSummaryImpl("LAPI_Waitcntr", "int", "lapi_handle_t hndl, lapi_cntr_t *cntr, int val, int *cur_cntr_val"), null));
        funcName2FuncInfo.put("LAPI_Xfer", new FunctionSummaryImpl("LAPI_Xfer", "", "Serves as a wrapper function for LAPI data transfer functions.",
    		  	new FunctionPrototypeSummaryImpl("LAPI_Xfer", "int", "lapi_handle_t hndl, lapi_xfer_t *xfer_cmd"), null));
        funcName2FuncInfo.put("LAPI_Nopoll_wait", new FunctionSummaryImpl("LAPI_Nopoll_wait", "", "Waits for a counter update without polling.",
                new FunctionPrototypeSummaryImpl("LAPI_Nopoll_wait", "int", "lapi_handle_t hndl, lapi_cntr_t *cntr_ptr, int val, int *cur_cntr_val"), null));
        funcName2FuncInfo.put("LAPI_Purge_totask", new FunctionSummaryImpl("LAPI_Purge_totask", "", "Allows a task to cancel messages to a given destination.",
                new FunctionPrototypeSummaryImpl("LAPI_Purge_totask", "int", "lapi_handle_t hndl, uint dest"), null));
        funcName2FuncInfo.put("LAPI_Resume_totask", new FunctionSummaryImpl("LAPI_Resume_totask", "", "Re-enables the sending of messages to the task.",
                new FunctionPrototypeSummaryImpl("LAPI_Resume_totask", "int", "lapi_handle_t hndl, uint dest"), null));
        funcName2FuncInfo.put("LAPI_Setcntr_wstatus", new FunctionSummaryImpl("LAPI_Setcntr_wstatus", "", "Used to set a counter to a specified value and to set the associated destination list array and destination status array to the counter.",
                new FunctionPrototypeSummaryImpl("LAPI_Setcntr_wstatus", "int", "lapi_handle_t hndl, lapi_cntr_t *cntr, int num_dest, uint *dest_list, int *dest_status"), null));
//        funcName2FuncInfo.put("LAPI_Fence", new FunctionSummary("LAPI_Fence", "", "DESC",
//                new FunctionPrototypeSummary("LAPI_Fence", "int", "ARGS"), null));

        // set title
        setTitle(TITLE);
	}
}
