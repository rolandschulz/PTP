/**********************************************************************
 * Copyright (c) 2007,2010 IBM Corporation.
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
import org.eclipse.ptp.pldt.lapi.Activator;
import org.eclipse.ptp.pldt.lapi.messages.Messages;

public class LapiCHelpBook extends CHelpBookImpl {
	private static final String TITLE = Messages.LapiCHelpBook_LAPI_c_help_book_title;

	public LapiCHelpBook() {
		super(Activator.getPluginId());
		// populate func map
		funcName2FuncInfo.put("LAPI_Addr_get", new FunctionSummaryImpl("LAPI_Addr_get", "", Messages.LapiCHelpBook_LAPI_Addr_get, //$NON-NLS-1$  //$NON-NLS-2$  //$NON-NLS-3$
				new FunctionPrototypeSummaryImpl("LAPI_Addr_get", "int", "lapi_handle_t hndl, void **addr, int addr_hndl"), null)); //$NON-NLS-1$  //$NON-NLS-2$  //$NON-NLS-3$
		funcName2FuncInfo.put("LAPI_Addr_set", new FunctionSummaryImpl("LAPI_Addr_set", "", Messages.LapiCHelpBook_LAPI_Addr_set, //$NON-NLS-1$  //$NON-NLS-2$  //$NON-NLS-3$
				new FunctionPrototypeSummaryImpl("LAPI_Addr_set", "int", "lapi_handle_t hndl, void **addr, int addr_hndl"), null)); //$NON-NLS-1$  //$NON-NLS-2$  //$NON-NLS-3$
		funcName2FuncInfo.put("LAPI_Address", new FunctionSummaryImpl("LAPI_Address", "", Messages.LapiCHelpBook_LAPI_Address, //$NON-NLS-1$  //$NON-NLS-2$  //$NON-NLS-3$
				new FunctionPrototypeSummaryImpl("LAPI_Address", "int", "void  *my_addr, ulong *ret_addr"), null)); //$NON-NLS-1$  //$NON-NLS-2$  //$NON-NLS-3$
		funcName2FuncInfo.put(
				"LAPI_Address_init", new FunctionSummaryImpl("LAPI_Address_init", "", Messages.LapiCHelpBook_LAPI_Address_init, //$NON-NLS-1$  //$NON-NLS-2$  //$NON-NLS-3$
						new FunctionPrototypeSummaryImpl(
								"LAPI_Address_init", "int", "lapi_handle_t  hndl, void *my_addr, void *add_tab[ ]"), null)); //$NON-NLS-1$  //$NON-NLS-2$  //$NON-NLS-3$
		funcName2FuncInfo
				.put("LAPI_Address_init64", new FunctionSummaryImpl("LAPI_Address_init64", "", Messages.LapiCHelpBook_LAPI_Address_init64, //$NON-NLS-1$  //$NON-NLS-2$  //$NON-NLS-3$
								new FunctionPrototypeSummaryImpl(
										"LAPI_Address_init64", "int", "lapi_handle_t  hndl, lapi_long_t my_addr, lapi_long_t *add_tab"), null)); //$NON-NLS-1$  //$NON-NLS-2$  //$NON-NLS-3$
		funcName2FuncInfo
				.put("LAPI_Amsend", new FunctionSummaryImpl("LAPI_Amsend", "", Messages.LapiCHelpBook_LAPI_Amsend, //$NON-NLS-1$  //$NON-NLS-2$  //$NON-NLS-3$
								new FunctionPrototypeSummaryImpl(
										"LAPI_Amsend", "int", "lapi_handle_t hndl, uint tgt, void *hdr_hdl, void *uhdr, uint uhdr_len, void *udata, ulong udata_len, lapi_cntr_t *tgt_cntr, lapi_cntr_t *org_cntr, lapi_cntr_t *cmpl_cntr"), null)); //$NON-NLS-1$  //$NON-NLS-2$  //$NON-NLS-3$
		funcName2FuncInfo
				.put("LAPI_Amsendv", new FunctionSummaryImpl("LAPI_Amsendv", "", Messages.LapiCHelpBook_LAPI_Amsendv, //$NON-NLS-1$  //$NON-NLS-2$  //$NON-NLS-3$
								new FunctionPrototypeSummaryImpl(
										"LAPI_Amsendv", "int", "lapi_handle_t hndl, uint tgt, void *hdr_hdl, void *uhdr, uint uhdr_len, lapi_vec_t *org_vec, lapi_cntr_t *tgt_cntr, lapi_cntr_t *org_cntr, lapi_cntr_t *cmpl_cntr"), null)); //$NON-NLS-1$  //$NON-NLS-2$  //$NON-NLS-3$
		funcName2FuncInfo.put("LAPI_Fence", new FunctionSummaryImpl("LAPI_Fence", "", Messages.LapiCHelpBook_LAPI_Fence, //$NON-NLS-1$  //$NON-NLS-2$  //$NON-NLS-3$
				new FunctionPrototypeSummaryImpl("LAPI_Fence", "int", "lapi_handle_t hndl"), null)); //$NON-NLS-1$  //$NON-NLS-2$  //$NON-NLS-3$
		funcName2FuncInfo
				.put("LAPI_Get", new FunctionSummaryImpl("LAPI_Get", "", Messages.LapiCHelpBook_LAPI_Get, //$NON-NLS-1$  //$NON-NLS-2$  //$NON-NLS-3$
								new FunctionPrototypeSummaryImpl(
										"LAPI_Get", "int", "lapi_handle_t hndl, uint tgt, ulong len, void *tgt_addr, void *org_addr, lapi_cntr_t *tgt_cntr, lapi_cntr_t *org_cntr"), null)); //$NON-NLS-1$  //$NON-NLS-2$  //$NON-NLS-3$
		funcName2FuncInfo.put("LAPI_Getcntr", new FunctionSummaryImpl("LAPI_Getcntr", "", Messages.LapiCHelpBook_LAPI_Getcntr, //$NON-NLS-1$  //$NON-NLS-2$  //$NON-NLS-3$
				new FunctionPrototypeSummaryImpl("LAPI_Getcntr", "int", "lapi_handle_t hndl, lapi_cntr_t *cntr, int *val"), null)); //$NON-NLS-1$  //$NON-NLS-2$  //$NON-NLS-3$
		funcName2FuncInfo
				.put("LAPI_Getv", new FunctionSummaryImpl("LAPI_Getv", "", Messages.LapiCHelpBook_LAPI_Getv, //$NON-NLS-1$  //$NON-NLS-2$  //$NON-NLS-3$
								new FunctionPrototypeSummaryImpl(
										"LAPI_Getv", "int", "lapi_handle_t hndl, uint tgt, lapi_vec_t *tgt_vec, lapi_vec_t *org_vec, lapi_cntr_t *tgt_cntr, lapi_cntr_t *org_cntr"), null)); //$NON-NLS-1$  //$NON-NLS-2$  //$NON-NLS-3$
		funcName2FuncInfo.put("LAPI_Gfence", new FunctionSummaryImpl("LAPI_Gfence", "", Messages.LapiCHelpBook_LAPI_Gfence, //$NON-NLS-1$  //$NON-NLS-2$  //$NON-NLS-3$
				new FunctionPrototypeSummaryImpl("LAPI_Gfence", "int", "lapi_handle_t hndl"), null)); //$NON-NLS-1$  //$NON-NLS-2$  //$NON-NLS-3$
		funcName2FuncInfo.put("LAPI_Init", new FunctionSummaryImpl("LAPI_Init", "", Messages.LapiCHelpBook_LAPI_Init, //$NON-NLS-1$  //$NON-NLS-2$  //$NON-NLS-3$
				new FunctionPrototypeSummaryImpl("LAPI_Init", "int", "lapi_handle_t *hndl, lapi_info_t *lapi_info"), null)); //$NON-NLS-1$  //$NON-NLS-2$  //$NON-NLS-3$
		funcName2FuncInfo.put(
				"LAPI_Msg_string", new FunctionSummaryImpl("LAPI_Msg_string", "", Messages.LapiCHelpBook_LAPI_Msg_string, //$NON-NLS-1$  //$NON-NLS-2$  //$NON-NLS-3$
						new FunctionPrototypeSummaryImpl("LAPI_Msg_string", "int", "int error_code, void *buf"), null)); //$NON-NLS-1$  //$NON-NLS-2$  //$NON-NLS-3$
		funcName2FuncInfo.put("LAPI_Msgpoll", new FunctionSummaryImpl("LAPI_Msgpoll", "", Messages.LapiCHelpBook_LAPI_Msgpoll, //$NON-NLS-1$  //$NON-NLS-2$  //$NON-NLS-3$
						new FunctionPrototypeSummaryImpl(
								"LAPI_Msgpoll", "int", "lapi_handle_t hndl, uint cnt, lapi_msg_info_t  *info"), null)); //$NON-NLS-1$  //$NON-NLS-2$  //$NON-NLS-3$
		funcName2FuncInfo.put("LAPI_Probe", new FunctionSummaryImpl("LAPI_Probe", "", Messages.LapiCHelpBook_LAPI_Probe, //$NON-NLS-1$  //$NON-NLS-2$  //$NON-NLS-3$
				new FunctionPrototypeSummaryImpl("LAPI_Probe", "int", "lapi_handle_t hndl"), null)); //$NON-NLS-1$  //$NON-NLS-2$  //$NON-NLS-3$
		funcName2FuncInfo
				.put("LAPI_Put", new FunctionSummaryImpl("LAPI_Put", "", Messages.LapiCHelpBook_LAPI_Put, //$NON-NLS-1$  //$NON-NLS-2$  //$NON-NLS-3$
								new FunctionPrototypeSummaryImpl(
										"LAPI_Put", "int", "lapi_handle_t hndl, uint tgt, ulong len, void *tgt_addr, void *org_addr, lapi_cntr_t *tgt_cntr, lapi_cntr_t *org_cntr, lapi_cntr_t *cmpl_cntr"), null)); //$NON-NLS-1$  //$NON-NLS-2$  //$NON-NLS-3$
		funcName2FuncInfo
				.put("LAPI_Putv", new FunctionSummaryImpl("LAPI_Putv", "", Messages.LapiCHelpBook_LAPI_Putv, //$NON-NLS-1$  //$NON-NLS-2$  //$NON-NLS-3$
								new FunctionPrototypeSummaryImpl(
										"LAPI_Putv", "int", "lapi_handle_t hndl, uint tgt, lapi_vec_t *tgt_vec, lapi_vec_t *org_vec, lapi_cntr_t *tgt_cntr, lapi_cntr_t *org_cntr, lapi_cntr_t *cmpl_cntr"), null)); //$NON-NLS-1$  //$NON-NLS-2$  //$NON-NLS-3$
		funcName2FuncInfo
				.put("LAPI_Qenv", new FunctionSummaryImpl("LAPI_Qenv", "", Messages.LapiCHelpBook_LAPI_Qenv, //$NON-NLS-1$  //$NON-NLS-2$  //$NON-NLS-3$
								new FunctionPrototypeSummaryImpl(
										"LAPI_Qenv", "int", "lapi_handle_t hndl, lapi_query_t query, int *ret_val"), null)); //$NON-NLS-1$  //$NON-NLS-2$  //$NON-NLS-3$
		funcName2FuncInfo
				.put("LAPI_Rmw", new FunctionSummaryImpl("LAPI_Rmw", "", Messages.LapiCHelpBook_LAPI_Rmw, //$NON-NLS-1$  //$NON-NLS-2$  //$NON-NLS-3$
								new FunctionPrototypeSummaryImpl(
										"LAPI_Rmw", "int", "lapi_handle_t hndl, RMW_ops_t op, uint tgt, int *tgt_var, int *in_val, int *prev_tgt_val, lapi_cntr_t *org_cntr"), null)); //$NON-NLS-1$  //$NON-NLS-2$  //$NON-NLS-3$
		funcName2FuncInfo
				.put("LAPI_Rmw64", new FunctionSummaryImpl("LAPI_Rmw64", "", Messages.LapiCHelpBook_LAPI_Rmw64, //$NON-NLS-1$  //$NON-NLS-2$  //$NON-NLS-3$
								new FunctionPrototypeSummaryImpl(
										"LAPI_Rmw64", "int", "lapi_handle_t hndl, Rmw_ops_t op, uint tgt, long long *tgt_var, long long *in_val, long long *prev_tgt_val, lapi_cntr_t *org_cntr"), null)); //$NON-NLS-1$  //$NON-NLS-2$  //$NON-NLS-3$
		funcName2FuncInfo.put("LAPI_Senv", new FunctionSummaryImpl("LAPI_Senv", "", Messages.LapiCHelpBook_LAPI_Senv, //$NON-NLS-1$  //$NON-NLS-2$  //$NON-NLS-3$
				new FunctionPrototypeSummaryImpl("LAPI_Senv", "int", "lapi_handle_t hndl, lapi_query_t query, int set_val"), null)); //$NON-NLS-1$  //$NON-NLS-2$  //$NON-NLS-3$
		funcName2FuncInfo.put("LAPI_Setcntr", new FunctionSummaryImpl("LAPI_Setcntr", "", Messages.LapiCHelpBook_LAPI_Setcntr, //$NON-NLS-1$  //$NON-NLS-2$  //$NON-NLS-3$
				new FunctionPrototypeSummaryImpl("LAPI_Setcntr", "int", "lapi_handle_t hndl, lapi_cntr_t *cntr, int val"), null)); //$NON-NLS-1$  //$NON-NLS-2$  //$NON-NLS-3$
		funcName2FuncInfo.put("LAPI_Term", new FunctionSummaryImpl("LAPI_Term", "", Messages.LapiCHelpBook_LAPI_Term, //$NON-NLS-1$  //$NON-NLS-2$  //$NON-NLS-3$
				new FunctionPrototypeSummaryImpl("LAPI_Term", "int", "lapi_handle_t hndl"), null)); //$NON-NLS-1$  //$NON-NLS-2$  //$NON-NLS-3$
		funcName2FuncInfo.put("LAPI_Util", new FunctionSummaryImpl("LAPI_Util", "", Messages.LapiCHelpBook_LAPI_Util, //$NON-NLS-1$  //$NON-NLS-2$  //$NON-NLS-3$
				new FunctionPrototypeSummaryImpl("LAPI_Util", "int", "lapi_handle_t hndl, lapi_util_t *util_cmd"), null)); //$NON-NLS-1$  //$NON-NLS-2$  //$NON-NLS-3$
		funcName2FuncInfo.put("LAPI_Waitcntr", new FunctionSummaryImpl("LAPI_Waitcntr", "", Messages.LapiCHelpBook_LAPI_Waitcntr, //$NON-NLS-1$  //$NON-NLS-2$  //$NON-NLS-3$
				new FunctionPrototypeSummaryImpl(
						"LAPI_Waitcntr", "int", "lapi_handle_t hndl, lapi_cntr_t *cntr, int val, int *cur_cntr_val"), null)); //$NON-NLS-1$  //$NON-NLS-2$  //$NON-NLS-3$
		funcName2FuncInfo.put("LAPI_Xfer", new FunctionSummaryImpl("LAPI_Xfer", "", Messages.LapiCHelpBook_LAPI_Xfer, //$NON-NLS-1$  //$NON-NLS-2$  //$NON-NLS-3$
				new FunctionPrototypeSummaryImpl("LAPI_Xfer", "int", "lapi_handle_t hndl, lapi_xfer_t *xfer_cmd"), null)); //$NON-NLS-1$  //$NON-NLS-2$  //$NON-NLS-3$
		funcName2FuncInfo
				.put("LAPI_Nopoll_wait", new FunctionSummaryImpl("LAPI_Nopoll_wait", "", Messages.LapiCHelpBook_LAPI_Nopoll_wait, //$NON-NLS-1$  //$NON-NLS-2$  //$NON-NLS-3$
								new FunctionPrototypeSummaryImpl(
										"LAPI_Nopoll_wait", "int", "lapi_handle_t hndl, lapi_cntr_t *cntr_ptr, int val, int *cur_cntr_val"), null)); //$NON-NLS-1$  //$NON-NLS-2$  //$NON-NLS-3$
		funcName2FuncInfo.put(
				"LAPI_Purge_totask", new FunctionSummaryImpl("LAPI_Purge_totask", "", Messages.LapiCHelpBook_LAPI_Purge_totask, //$NON-NLS-1$  //$NON-NLS-2$  //$NON-NLS-3$
						new FunctionPrototypeSummaryImpl("LAPI_Purge_totask", "int", "lapi_handle_t hndl, uint dest"), null)); //$NON-NLS-1$  //$NON-NLS-2$  //$NON-NLS-3$
		funcName2FuncInfo.put(
				"LAPI_Resume_totask", new FunctionSummaryImpl("LAPI_Resume_totask", "", Messages.LapiCHelpBook_LAPI_Resume_totask, //$NON-NLS-1$  //$NON-NLS-2$  //$NON-NLS-3$
						new FunctionPrototypeSummaryImpl("LAPI_Resume_totask", "int", "lapi_handle_t hndl, uint dest"), null)); //$NON-NLS-1$  //$NON-NLS-2$  //$NON-NLS-3$
		funcName2FuncInfo
				.put("LAPI_Setcntr_wstatus", new FunctionSummaryImpl("LAPI_Setcntr_wstatus", "", Messages.LapiCHelpBook_LAPI_Setcntr_wstatus, //$NON-NLS-1$  //$NON-NLS-2$  //$NON-NLS-3$
								new FunctionPrototypeSummaryImpl(
										"LAPI_Setcntr_wstatus", "int", "lapi_handle_t hndl, lapi_cntr_t *cntr, int num_dest, uint *dest_list, int *dest_status"), null)); //$NON-NLS-1$  //$NON-NLS-2$  //$NON-NLS-3$

		// set title
		setTitle(TITLE);
	}
}
