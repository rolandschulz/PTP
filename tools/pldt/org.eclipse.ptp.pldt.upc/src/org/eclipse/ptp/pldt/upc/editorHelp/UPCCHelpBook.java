/**********************************************************************
 * Copyright (c) 2008,2010 IBM Corporation.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.ptp.pldt.upc.editorHelp;

import java.util.HashMap;
import java.util.Map;

import org.eclipse.cdt.ui.IFunctionSummary;
import org.eclipse.ptp.pldt.common.editorHelp.CHelpBookImpl;
import org.eclipse.ptp.pldt.common.editorHelp.FunctionPrototypeSummaryImpl;
import org.eclipse.ptp.pldt.common.editorHelp.FunctionSummaryImpl;
import org.eclipse.ptp.pldt.upc.UPCPlugin;
import org.eclipse.ptp.pldt.upc.messages.Messages;

/**
 * UPC help book - this is the information that is used for the hover help
 * <p>
 * <b>Note:</b> This (and F1/dynamic help as well) requires a fix to CDT post-5.0.0 release to
 * org.eclipse.cdt.internal.core.model.TranslationUnit - to recognize content-type of UPC to be a deriviative ("kindOf") the C
 * content type.
 * <p>
 * See https://bugs.eclipse.org/bugs/show_bug.cgi?id=237331
 * 
 * @author Beth Tibbitts
 * 
 */
public class UPCCHelpBook extends CHelpBookImpl {
	private static final String TITLE = Messages.UPCCHelpBook_upc_c_help_book_title;
	private Map<String, String> desc = new HashMap<String, String>();

	public UPCCHelpBook() {
		super(UPCPlugin.getPluginId());
		// populate description map - this just makes the funcName2FuncInfo init lines a bit more terse

		desc.put("upc_addrfield", Messages.UPCCHelpBook_upc_addrfield);//$NON-NLS-1$
		desc.put("upc_affinitysize", Messages.UPCCHelpBook_upc_affinitysize);//$NON-NLS-1$
		desc.put("upc_all_alloc", Messages.UPCCHelpBook_upc_all_alloc);//$NON-NLS-1$
		desc.put("upc_all_broadcast", Messages.UPCCHelpBook_upc_all_broadcast);//$NON-NLS-1$
		desc.put("upc_all_lock_alloc", Messages.UPCCHelpBook_upc_all_lock_alloc);//$NON-NLS-1$
		desc.put("upc_all_exchange", Messages.UPCCHelpBook_upc_all_exchange);//$NON-NLS-1$
		desc.put("upc_all_gather", Messages.UPCCHelpBook_upc_all_gather);//$NON-NLS-1$
		desc.put("upc_all_gather_all", Messages.UPCCHelpBook_upc_all_gather_all);//$NON-NLS-1$
		desc.put("upc_all_lock_alloc", Messages.UPCCHelpBook_upc_all_lock_alloc);//$NON-NLS-1$
		desc.put("upc_all_permute", Messages.UPCCHelpBook_upc_all_permute);//$NON-NLS-1$
		desc.put("upc_all_scatter", Messages.UPCCHelpBook_upc_all_scatter);//$NON-NLS-1$
		desc.put("upc_alloc", Messages.UPCCHelpBook_upc_alloc); //$NON-NLS-1$
		desc.put("upc_free", Messages.UPCCHelpBook_upc_free);//$NON-NLS-1$
		desc.put("upc_global_alloc", Messages.UPCCHelpBook_upc_global_alloc);//$NON-NLS-1$
		desc.put("upc_global_exit", Messages.UPCCHelpBook_upc_global_exit);//$NON-NLS-1$
		desc.put("upc_global_lock_alloc", Messages.UPCCHelpBook_upc_global_lock_alloc);//$NON-NLS-1$
		desc.put("upc_local_alloc", Messages.UPCCHelpBook_upc_local_alloc); //$NON-NLS-1$  
		desc.put("upc_lock_attempt", Messages.UPCCHelpBook_upc_lock_attempt);//$NON-NLS-1$
		desc.put("upc_lock_free", Messages.UPCCHelpBook_upc_lock_free);//$NON-NLS-1$
		desc.put("upc_lock_t", Messages.UPCCHelpBook_upc_lock_t);//$NON-NLS-1$
		desc.put("upc_lock", Messages.UPCCHelpBook_upc_lock);//$NON-NLS-1$
		desc.put("upc_memcpy", Messages.UPCCHelpBook_upc_memcpy);//$NON-NLS-1$
		desc.put("upc_memget", Messages.UPCCHelpBook_upc_memget);//$NON-NLS-1$
		desc.put("upc_memset", Messages.UPCCHelpBook_upc_memset);//$NON-NLS-1$
		desc.put("upc_phaseof", Messages.UPCCHelpBook_upc_phaseof);//$NON-NLS-1$
		desc.put("upc_resetphase", Messages.UPCCHelpBook_upc_resetphase);//$NON-NLS-1$
		desc.put("upc_threadof", Messages.UPCCHelpBook_upc_threadof);//$NON-NLS-1$
		desc.put("upc_unlock", Messages.UPCCHelpBook_upc_unlock); //$NON-NLS-1$
		desc.put("shared", Messages.UPCCHelpBook_upc_shared);//$NON-NLS-1$

		// populate func map
		// funcName2FuncInfo.put("upc_free", new FunctionSummaryImpl("upc_free", "", "upc_free test description",
		// new FunctionPrototypeSummaryImpl("upc_free", "void", "shared void *ptr"), null));
		String desc = ""; // will do map lookup for description in fps method. //$NON-NLS-1$
		funcName2FuncInfo.put("upc_addrfield", fps("upc_addrfield", "", desc, "size_t", "shared void *ptr"));//$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$ //$NON-NLS-5$
		funcName2FuncInfo
				.put("upc_affinitysize", fps("upc_affinitysize", "", desc, "size_t", "size_t totalsize, size_t nbytes, size_t threadid"));//$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$ //$NON-NLS-5$
		funcName2FuncInfo.put("upc_all_alloc", fps("upc_all_alloc", "", desc, "void", "size_t nblocks, size_t nbytes"));//$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$ //$NON-NLS-5$
		funcName2FuncInfo
				.put("upc_all_broadcast", fps("upc_all_broadcast", "", desc, "void", "shared void * restrict dst, shared const void * restrict src, size_t nbytes, upc_flag_t flags"));//$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$ //$NON-NLS-5$
		funcName2FuncInfo
				.put("upc_all_exchange", fps("upc_all_exchange", "", desc, "void", "shared void * restrict dst, shared const void * restrict src, size_t nbytes, upc_flag_t flags"));//$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$ //$NON-NLS-5$
		funcName2FuncInfo
				.put("upc_all_gather_all", fps("upc_all_gather_all", "", desc, "void", "shared void * restrict dst, shared const void * restrict src, size_t nbytes, upc_flag_t flags"));//$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$ //$NON-NLS-5$
		funcName2FuncInfo
				.put("upc_all_gather", fps("upc_all_gather", "", desc, "void", "shared void * restrict dst, shared const void * restrict src, size_t nbytes, upc_flag_t flags")); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$ //$NON-NLS-5$		
		funcName2FuncInfo.put("upc_all_lock_alloc", fps("upc_all_lock_alloc", "", desc, "upc_lock_t", "void"));//$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$ //$NON-NLS-5$
		funcName2FuncInfo
				.put("upc_all_permute", fps("upc_all_permute", "", desc, "void", "shared void * restrict dst, shared const void * restrict src, shared const int * restrict perm, size_t nbytes, upc_flag_t flags"));//$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$ //$NON-NLS-5$
		funcName2FuncInfo
				.put("upc_all_scatter", fps("upc_all_scatter", "", desc, "void", "shared void * restrict dst, shared const void * restrict src, size_t nbytes, upc_flag_t flags"));//$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$ //$NON-NLS-5$
		funcName2FuncInfo.put("upc_alloc", fps("upc_alloc", "", desc, "void", "size_t nbytes")); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$ //$NON-NLS-5$
		funcName2FuncInfo.put("upc_free", fps("upc_free", "", desc, "void", "shared void *ptr"));//$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$ //$NON-NLS-5$
		funcName2FuncInfo.put("upc_global_alloc", fps("upc_global_alloc", "", desc, "void", "int status"));//$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$ //$NON-NLS-5$
		funcName2FuncInfo.put("upc_global_exit", fps("upc_global_exit", "", desc, "void", "int status"));//$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$ //$NON-NLS-5$
		funcName2FuncInfo.put("upc_global_lock_alloc", fps("upc_global_lock_alloc", "", desc, "upc_lock_t", "void"));//$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$ //$NON-NLS-5$
		funcName2FuncInfo.put("upc_local_alloc", fps("upc_local_alloc", "", desc, "void", "size_t nblocks, size_t nbytes")); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$ //$NON-NLS-5$ 
		funcName2FuncInfo.put("upc_lock_attempt", fps("upc_lock_attempt", "", desc, "int", "upc_lock_t *ptr"));//$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$ //$NON-NLS-5$
		funcName2FuncInfo.put("upc_lock_free", fps("upc_lock_free", "", desc, "upc_lock_t", "void"));//$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$ //$NON-NLS-5$
		funcName2FuncInfo.put("upc_lock", fps("upc_lock", "", desc, "void", "upc_lock_t *ptr"));//$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$ //$NON-NLS-5$
		funcName2FuncInfo
				.put("upc_memcpy", fps("upc_memcpy", "", desc, "void", "shared void * restrict dst, shared const void * restrict src, size_t n")); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$ //$NON-NLS-5$  
		funcName2FuncInfo
				.put("upc_memget", fps("upc_memget", "", desc, "void", "void * restrict dst, shared const void * restrict src, size_t n"));//$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$ //$NON-NLS-5$
		funcName2FuncInfo.put("upc_memset", fps("upc_memset", "", desc, "void", "shared void *dst, int c, size_t n"));//$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$ //$NON-NLS-5$
		funcName2FuncInfo.put("upc_phaseof", fps("upc_memset", "", desc, "size_t", "shared void *ptr")); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$ //$NON-NLS-5$
		funcName2FuncInfo.put("upc_resetphase", fps("upc_resetphase", "", desc, "void", "shared void *ptr"));//$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$ //$NON-NLS-5$
		funcName2FuncInfo.put("upc_threadof", fps("upc_threadof", "", desc, "size_t", "shared void *ptr"));//$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$ //$NON-NLS-5$
		funcName2FuncInfo.put("upc_unlock", fps("upc_unlock", "", desc, "void", "upc_lock_t *ptr"));//$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$ //$NON-NLS-5$
		funcName2FuncInfo.put("shared", fps("shared", "", desc, "", ""));//$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$ //$NON-NLS-5$

		// set title
		setTitle(TITLE);
	}

	/**
	 * Convenience function for inputting these FunctionPrototypeSummary and FunctionSummary arguments.
	 * 
	 * @param name
	 * @param namespace
	 * @param description
	 *            if empty will do lookup in Map
	 * @param returnType
	 * @param args
	 * @return
	 */
	protected IFunctionSummary fps(String name, String namespace, String description, String returnType, String args) {
		if (description == null || description.length() == 0) {
			description = getDesc(name);
		}
		IFunctionSummary fps = new FunctionSummaryImpl(name, namespace, description,
				new FunctionPrototypeSummaryImpl(name, returnType, args), null);
		return fps;
	}

	protected String getDesc(String key) {
		String description = (String) desc.get(key);
		if (description == null)
			description = key + Messages.UPCCHelpBook_upc_description;
		return description;
	}

	/**
	 * Workaround for https://bugs.eclipse.org/bugs/show_bug.cgi?id=237331
	 * ("CHelpProvider not called for UPC")
	 * so that UPC help will get called.
	 * CDT bug fix required for this to work otherwise.
	 * For example, in TranslationUnit to make the UPC type 'inherit' from the C type.
	 * <p>
	 * This returns an invalid number as a workaround. This will cause the default part of the switch in
	 * CHelpBookDescriptor.matches() to execute and the UPC help will match for all files. Not pretty but it will work for now.
	 * 
	 * @see org.eclipse.cdt.internal.core.model.TranslationUnit
	 * 
	 */
	@SuppressWarnings("restriction")
	// just for the javadoc comment to not get warning :)
	@Override
	public int getCHelpType() {
		return -1;
	}
}
