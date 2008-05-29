/**********************************************************************
 * Copyright (c) 2008 IBM Corporation.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.ptp.pldt.upc.editorHelp;

import org.eclipse.cdt.ui.FunctionPrototypeSummary;
import org.eclipse.cdt.ui.IFunctionSummary;
import org.eclipse.cdt.ui.IFunctionSummary.IFunctionPrototypeSummary;
import org.eclipse.ptp.pldt.common.editorHelp.CHelpBookImpl;
import org.eclipse.ptp.pldt.common.editorHelp.FunctionPrototypeSummaryImpl;
import org.eclipse.ptp.pldt.common.editorHelp.FunctionSummaryImpl;
import org.eclipse.ptp.pldt.upc.UPCPlugin;

/**
 * UPC help book - this is the information that is used for the hover help
 * @author Beth Tibbitts
 *
 */
public class UPCCHelpBook extends CHelpBookImpl {
	private static final String TITLE = "UPC C Help Book";

	
	public UPCCHelpBook() {
        super(UPCPlugin.getPluginId());
		// populate func map
        //funcName2FuncInfo.put("upc_free", new FunctionSummaryImpl("upc_free", "", "upc_free test description",
        //        new FunctionPrototypeSummaryImpl("upc_free", "void", "shared void *ptr"), null));
        
        funcName2FuncInfo.put("upc_free", fps("upc_free", "", "upc_free description", "void", "shared void *ptr"));
       
        funcName2FuncInfo.put("upc_global_exit", fps("upc_global_exit", "", "upc_global_exit description", "void", "int status"));
        funcName2FuncInfo.put("upc_global_alloc", fps("upc_global_alloc", "", "upc_global_alloc description", "void", "int status"));
        funcName2FuncInfo.put("upc_all_alloc", fps("upc_all_alloc", "", "upc_all_alloc description", "void", "size_t nblocks, size_t nbytes"));
        funcName2FuncInfo.put("upc_alloc", fps("upc_alloc", "", "upc_alloc description", "void", "size_t nbytes"));
        funcName2FuncInfo.put("upc_local_alloc", fps("upc_local_alloc", "", "upc_local_alloc description", "void", "size_t nblocks, size_t nbytes"));
        funcName2FuncInfo.put("upc_free", fps("upc_free", "", "upc_free description", "void", "shared void *ptr"));
        funcName2FuncInfo.put("upc_threadof", fps("upc_threadof", "", "upc_threadof description", "void", "shared void *ptr"));
        funcName2FuncInfo.put("upc_phaseof", fps("upc_phaseof", "", "upc_phaseof description", "void", "shared void *ptr"));
        funcName2FuncInfo.put("upc_resetphase", fps("upc_resetphase", "", "upc_resetphase description", 	"void", "shared void *ptr"));
        funcName2FuncInfo.put("upc_addrfield", fps("upc_addrfield", "", "upc_addrfield description", "void", "shared void *ptr"));
        funcName2FuncInfo.put("upc_affinitysize", fps("upc_affinitysize", "", "upc_affinitysize description", "size_t", "size_t totalsize, size_t nbytes, size_t threadid"));
        funcName2FuncInfo.put("upc_global_lock_alloc", fps("upc_global_lock_alloc", "", "upc_global_lock_alloc description", "upc_lock_t", "void"));
        funcName2FuncInfo.put("upc_all_lock_alloc", fps("upc_all_lock_alloc", "", "upc_all_lock_alloc description", "upc_lock_t", "void"));
        funcName2FuncInfo.put("upc_lock_free", fps("upc_lock_free", "", "upc_lock_free description", "upc_lock_t", "void"));
        funcName2FuncInfo.put("upc_lock", fps("upc_lock", "", "upc_lock description", "void", "upc_lock_t *ptr"));
        funcName2FuncInfo.put("upc_lock_attempt", fps("upc_lock_attempt", "", "upc_lock_attempt description", "int", "upc_lock_t *ptr"));
        funcName2FuncInfo.put("upc_unlock", fps("upc_unlock", "", "upc_unlock description", "void", "upc_lock_t *ptr"));
        funcName2FuncInfo.put("upc_memcpy", fps("upc_memcpy", "", "upc_memcpy description", "void", "args"));
        

        // set title
        setTitle(TITLE);
	}
	
	/**
	 * convenience function for inputting these
	 * @param name
	 * @param namespace
	 * @param description
	 * @param returnType
	 * @param args
	 * @return
	 */
	protected IFunctionSummary fps(String name, String namespace, String description, String returnType, String args){
		
		IFunctionSummary fps = new FunctionSummaryImpl(name, namespace, description,
                new FunctionPrototypeSummaryImpl(name, returnType, args), null);
		return fps;
	}
}
