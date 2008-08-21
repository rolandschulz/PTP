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

import java.util.HashMap;
import java.util.Map;

import org.eclipse.cdt.ui.IFunctionSummary;
import org.eclipse.ptp.pldt.common.editorHelp.CHelpBookImpl;
import org.eclipse.ptp.pldt.common.editorHelp.FunctionPrototypeSummaryImpl;
import org.eclipse.ptp.pldt.common.editorHelp.FunctionSummaryImpl;
import org.eclipse.ptp.pldt.upc.UPCPlugin;

/**
 * UPC help book - this is the information that is used for the hover help
 * <p>
 * <b>Note:</b> This (and F1/dynamic help as well) requires a fix to CDT post-5.0.0 release
 * to org.eclipse.cdt.internal.core.model.TranslationUnit  - to recognize content-type of UPC to be a deriviative ("kindOf") the C content type.
 * <p>See https://bugs.eclipse.org/bugs/show_bug.cgi?id=237331  
 * @author Beth Tibbitts
 *
 */
public class UPCCHelpBook extends CHelpBookImpl {
	private static final String TITLE = "UPC C Help Book";
	private Map<String,String> desc=new HashMap<String, String>();

	
	public UPCCHelpBook() {
        super(UPCPlugin.getPluginId());
        // populate description map - this just makes the funcName2FuncInfo init lines a bit more terse
      
        desc.put("upc_addrfield", "Returns an implementation-defined value reflecting the <q>local address</q>of the object pointed to by the pointer-to-shared argument.");
        desc.put("upc_affinitysize", "A convenience function which calculates the exact size of the local portion of the data in a shared object with affinity to <code>threadid</code>.");
        desc.put("upc_all_alloc", "Allocates shared space");
        desc.put("upc_all_broadcast", "Copies a block of memory with affinity to a single thread to a block of shared memory on each thread.");
        desc.put("upc_all_lock_alloc", "Dynamically allocates a lock and returns a pointer to it. The lock is created in an unlocked state.");
        desc.put("upc_all_exchange", "Copies the ith block of memory from a shared memory area that has affinity to thread j to the jth block of a shared memory area that has affinity to thread i");
        desc.put("upc_all_gather",     "Copies a block of shared memory that has affinity to the ith thread to the ith block of a shared memory area that has affinity to a single thread.");
        desc.put("upc_all_gather_all", "Copies a block of memory from one shared memory area with affinity to the ith thread to the ith block of a shared memory area on each thread.");
        desc.put("upc_all_lock_alloc", "Dynamically allocates a lock and returns a pointer to it. The lock is created in an unlocked state.");
        desc.put("upc_all_permute", "Copies a block of memory from a shared memory area that has affinity to the ith thread to a block of a shared memory that has affinity to thread perm[i].");
        desc.put("upc_all_scatter", "Copies the ith block of an area of shared memory with affinity to a single thread to a block of shared memory with affinity to the ith thread.");
        desc.put("upc_alloc", "Allocates shared space of at least nbytes with affinity to the calling thread.");      
        desc.put("upc_free", "Frees dynamically allocated shared storage");
        desc.put("upc_global_alloc","Allocates shared space");
        desc.put("upc_global_exit","Flushes all I/O, releases all storage, and terminates the execution for all active threads.");
        desc.put("upc_global_lock_alloc", "Dynamically allocates a lock and returns a pointer to it. The lock is created in an unlocked state.");
        desc.put("upc_local_alloc","Deprecated. Use upc_alloc instead.\nAllocates shared space.");      
        desc.put("upc_lock_attempt", "Attempts to set the state of a lock to locked. Return code indicates previous state.");
        desc.put("upc_lock_free", "Frees all resources associated with a lock.");
        desc.put("upc_lock_t", "UPC type for a lock");
        desc.put("upc_lock", "Sets the state of a lock to locked.");
        desc.put("upc_memcpy", "Copies n characters from a shared object having affinity with one thread to a shared object having affinity with the same or another thread.");
        desc.put("upc_memget", "Copies n characters from a shared object with affinity to any single thread to an object on the calling thread.");
        desc.put("upc_memset", "Copies a value, converted to an unsigned char, to a shared object with affinity to any single thread.");
        desc.put("upc_phaseof", "Returns the phase component of the pointer-to-shared argument.");
        desc.put("upc_resetphase", "Returns a pointer-to-shared which is identical to its input except that it has zero phase");
        desc.put("upc_threadof", "Returns the index of the thread that has affinity to the shared object pointed to by the argument.");
        desc.put("upc_unlock", "Sets the state of a lock to unlocked."); 
        desc.put("shared", "this is a shared UPC keyword");
        
        
		// populate func map
        //funcName2FuncInfo.put("upc_free", new FunctionSummaryImpl("upc_free", "", "upc_free test description",
        //        new FunctionPrototypeSummaryImpl("upc_free", "void", "shared void *ptr"), null));
        String desc = ""; // will do map lookup for description in fps method.
        funcName2FuncInfo.put("upc_addrfield", fps("upc_addrfield", "", desc, "size_t", "shared void *ptr"));
        funcName2FuncInfo.put("upc_affinitysize", fps("upc_affinitysize", "", desc, "size_t", "size_t totalsize, size_t nbytes, size_t threadid"));
        funcName2FuncInfo.put("upc_all_alloc", fps("upc_all_alloc", "", desc, "void", "size_t nblocks, size_t nbytes"));
        funcName2FuncInfo.put("upc_all_broadcast", fps("upc_all_broadcast", "", desc, "void", "shared void * restrict dst, shared const void * restrict src, size_t nbytes, upc_flag_t flags"));
        funcName2FuncInfo.put("upc_all_exchange", fps("upc_all_exchange", "", desc, "void", "shared void * restrict dst, shared const void * restrict src, size_t nbytes, upc_flag_t flags"));
        funcName2FuncInfo.put("upc_all_gather_all", fps("upc_all_gather_all", "", desc, "void", "shared void * restrict dst, shared const void * restrict src, size_t nbytes, upc_flag_t flags"));
        funcName2FuncInfo.put("upc_all_gather", fps("upc_all_gather", "", desc, "void", "shared void * restrict dst, shared const void * restrict src, size_t nbytes, upc_flag_t flags"));      		
        funcName2FuncInfo.put("upc_all_lock_alloc", fps("upc_all_lock_alloc", "", desc, "upc_lock_t", "void"));
        funcName2FuncInfo.put("upc_all_permute", fps("upc_all_permute", "", desc, "void", "shared void * restrict dst, shared const void * restrict src, shared const int * restrict perm, size_t nbytes, upc_flag_t flags"));
        funcName2FuncInfo.put("upc_all_scatter", fps("upc_all_scatter", "", desc, "void", "shared void * restrict dst, shared const void * restrict src, size_t nbytes, upc_flag_t flags"));
        funcName2FuncInfo.put("upc_alloc", fps("upc_alloc", "", desc, "void", "size_t nbytes"));      
        funcName2FuncInfo.put("upc_free", fps("upc_free", "", desc, "void", "shared void *ptr"));
        funcName2FuncInfo.put("upc_global_alloc", fps("upc_global_alloc", "", desc, "void", "int status"));
        funcName2FuncInfo.put("upc_global_exit", fps("upc_global_exit", "", desc, "void", "int status"));
        funcName2FuncInfo.put("upc_global_lock_alloc", fps("upc_global_lock_alloc", "", desc, "upc_lock_t", "void"));
        funcName2FuncInfo.put("upc_local_alloc", fps("upc_local_alloc", "", desc, "void", "size_t nblocks, size_t nbytes"));    
        funcName2FuncInfo.put("upc_lock_attempt", fps("upc_lock_attempt", "", desc, "int", "upc_lock_t *ptr"));
        funcName2FuncInfo.put("upc_lock_free", fps("upc_lock_free", "", desc, "upc_lock_t", "void"));
        funcName2FuncInfo.put("upc_lock", fps("upc_lock", "", desc, "void", "upc_lock_t *ptr"));
        funcName2FuncInfo.put("upc_memcpy", fps("upc_memcpy", "", desc, "void", "shared void * restrict dst, shared const void * restrict src, size_t n"));      
        funcName2FuncInfo.put("upc_memget", fps("upc_memget", "", desc, "void", "void * restrict dst, shared const void * restrict src, size_t n"));
        funcName2FuncInfo.put("upc_memset", fps("upc_memset", "", desc, "void", "shared void *dst, int c, size_t n"));
        funcName2FuncInfo.put("upc_phaseof", fps("upc_memset", "", desc, "size_t", "shared void *ptr"));     
        funcName2FuncInfo.put("upc_resetphase", fps("upc_resetphase", "", desc, "void", "shared void *ptr"));
        funcName2FuncInfo.put("upc_threadof", fps("upc_threadof", "", desc, "size_t", "shared void *ptr"));
        funcName2FuncInfo.put("upc_unlock", fps("upc_unlock", "", desc, "void", "upc_lock_t *ptr"));
        funcName2FuncInfo.put("shared", fps("shared", "", desc, "", ""));
        
        // set title
        setTitle(TITLE);
	}
	
	/**
	 * Convenience function for inputting these FunctionPrototypeSummary and FunctionSummary arguments.
	 * @param name
	 * @param namespace
	 * @param description if empty will do lookup in Map
	 * @param returnType
	 * @param args
	 * @return
	 */
	protected IFunctionSummary fps(String name, String namespace, String description, String returnType, String args){
		if(description==null || description.length()==0) {
			description=getDesc(name);
		}
		IFunctionSummary fps = new FunctionSummaryImpl(name, namespace, description,
                new FunctionPrototypeSummaryImpl(name, returnType, args), null);
		return fps;
	}
	protected String getDesc(String key) {
		String description = (String)desc.get(key);
		if(description==null)
			description=key+" description";
		return description;
	}
	
	/**
	 * Workaround for https://bugs.eclipse.org/bugs/show_bug.cgi?id=237331
	 * ("CHelpProvider not called for UPC")
	 * so that UPC help will get called.
	 * CDT bug fix required for this to work otherwise.
	 * For example, in TranslationUnit to make the UPC type 'inherit' from the C type.
	 * <p>
	 * This returns an invalid number as a workaround.
	 * This will cause the default part of the switch in CHelpBookDescriptor.matches()
	 * to execute and the UPC help will
	 * match for all files. Not pretty but it will work for now.
	 * @see org.eclipse.cdt.internal.core.model.TranslationUnit
	 * 
	 */
	@SuppressWarnings("restriction")// just for the javadoc comment to not get warning :)
	@Override
	public int getCHelpType() {
		return -1;
	}
}
