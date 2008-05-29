/**********************************************************************
 * Copyright (c) 2005,2008 IBM Corporation.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.ptp.pldt.openmp.core.editorHelp;

import java.io.FileOutputStream;
import java.io.IOException;
import java.io.PrintStream;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import org.eclipse.cdt.ui.IFunctionSummary.IFunctionPrototypeSummary;
import org.eclipse.ptp.pldt.common.editorHelp.CHelpBookImpl;
import org.eclipse.ptp.pldt.common.editorHelp.FunctionPrototypeSummaryImpl;
import org.eclipse.ptp.pldt.common.editorHelp.FunctionSummaryImpl;
import org.eclipse.ptp.pldt.openmp.core.OpenMPPlugin;

/**
 * Provide help for OpenMP APIs.<br>
 * This includes hover help, F1 help, and content assist (ctrl-space)
 * @author tibbitts
 *
 */
public class OpenMPCHelpBook extends CHelpBookImpl {
	private static final String TITLE = "OpenMP C and Fortran Help Book";

	public OpenMPCHelpBook() {
		super(OpenMPPlugin.getPluginId());
		// populate func map
		funcName2FuncInfo=makeFunctionMap();

		// set title
		setTitle(TITLE);
	}

	/**
	 * Make function map of API names and info.<br>
	 * Note this is also used for (one-time) generation of the html
	 * files by the main() and generateHTML() methods below.
	 * @return hashmap of the function names and associated info
	 */
	private static Map makeFunctionMap() {
		Map funcMap= new HashMap();
		funcMap.put("omp_set_num_threads",
						new FunctionSummaryImpl("omp_set_num_threads","",
								"Sets the number of threads that will be used in the next parallel region",
								new FunctionPrototypeSummaryImpl(
										"omp_set_num_threads", "void",
										"int num_threads"), null));

		funcMap.put("omp_get_num_threads", new FunctionSummaryImpl(
				"omp_get_num_threads", "", "This function returns the number of threads currently in the team executing the parallel region from which it is called.",
				new FunctionPrototypeSummaryImpl("omp_get_num_threads",
						"int", "int num_threads"), null));
		
		funcMap.put("omp_get_max_threads", new FunctionSummaryImpl(
				"omp_get_max_threads", "", "This function returns the maximum value that can be returned by calls to omp_get_num_threads.",
				new FunctionPrototypeSummaryImpl("omp_get_max_threads",
						"int", "void"), null));
		funcMap.put("omp_get_thread_num", new FunctionSummaryImpl(
				"get_thread_num", "", "This function returns the thread number, within its team, of the thread executing the function. The thread number lies between 0 and omp_get_num_threads()-1, inclusive. The master thread of the team is thread 0.",
				new FunctionPrototypeSummaryImpl("get_thread_num",
						"int", "void"), null));
		funcMap.put("omp_get_num_procs", new FunctionSummaryImpl(
				"get_num_procs", "", "This function returns the maximum number of processors that could be assigned to the program.",
				new FunctionPrototypeSummaryImpl("get_num_procs", "int",
						"void"), null));
		funcMap.put("omp_in_parallel", new FunctionSummaryImpl(
				"omp_in_parallel", "",
				"This function returns non-zero if it is called within the dynamic extent of a parallel region executing in parallel; otherwise, it returns 0.",
				new FunctionPrototypeSummaryImpl("omp_in_parallel", "int",
						"void"), null));

		funcMap.put("omp_set_dynamic", new FunctionSummaryImpl(
				"omp_set_dynamic", "", "This function enables or disables dynamic adjustment of the number of threads available for execution of parallel regions.",
				new FunctionPrototypeSummaryImpl("omp_set_dynamic",
						"void", "int dynamic_threads"), null));
		funcMap.put("omp_get_dynamic", new FunctionSummaryImpl(
				"omp_get_dynamic", "", "This function returns non-zero if dynamic thread adjustments enabled and returns 0 otherwise.",
				new FunctionPrototypeSummaryImpl("omp_get_dynamic",
						"int", "void"), null));

		funcMap.put("omp_set_nested", new FunctionSummaryImpl(
				"omp_set_nested", "", "This function enables or disables nested parallelism.",
				new FunctionPrototypeSummaryImpl("omp_set_nested",
						"void", "int nested"), null));
		funcMap.put("omp_get_nested", new FunctionSummaryImpl(
				"omp_get_nested", "", "omp_get_nested description",
				new FunctionPrototypeSummaryImpl("This function returns non-zero if nested parallelism is enabled and 0 if it is disabled.",
						"int", "void"), null));
		funcMap.put("omp_init_lock", new FunctionSummaryImpl(
				"omp_init_lock", "", "These functions (this and omp_init_nest_lock) provide the only means of initializing a lock. Each function initializes the lock associated with the parameter lock for use in subsequent calls.",
				new FunctionPrototypeSummaryImpl("omp_init_lock", "void",
						"omp_lock_t *lock"), null));
		funcMap.put("omp_init_nest_lock", new FunctionSummaryImpl(
				"omp_init_nest_lock", "", "These functions (this and omp_init_lock) provide the only means of initializing a lock. Each function initializes the lock associated with the parameter lock for use in subsequent calls.",
				new FunctionPrototypeSummaryImpl("omp_init_nest_lock", "void",
						"omp_nest_lock_t *lock"), null));
		
		funcMap.put("omp_destroy_lock", new FunctionSummaryImpl(
				"omp_destroy_lock", "", "Ensures that the specified lock variable lock is uninitialized.",
				new FunctionPrototypeSummaryImpl("omp_destroy_lock",
						"void", "omp_lock_t *lock"), null));
		funcMap.put("omp_destroy_nest_lock", new FunctionSummaryImpl(
				"omp_destroy_nest_lock", "", "Ensures that the specified lock variable lock is uninitialized.",
				new FunctionPrototypeSummaryImpl("omp_destroy_nest_lock",
						"void", "omp_nest_lock_t *lock"), null));
		
		funcMap.put("omp_set_lock", new FunctionSummaryImpl(
				"omp_set_lock", "", "Blocks the thread executing the function until the specified lock is available and then sets the lock. A simple lock is available if it is unlocked. A nestable lock is available if it is unlocked or if it is already owned by the thread executing the function.",
				new FunctionPrototypeSummaryImpl("omp_set_lock", "void",
						"omp_lock_t *lock"), null));
		funcMap.put("omp_set_nest_lock", new FunctionSummaryImpl(
				"omp_set_nest_lock", "", "Blocks the thread executing the function until the specified lock is available and then sets the lock. A simple lock is available if it is unlocked. A nestable lock is available if it is unlocked or if it is already owned by the thread executing the function.",
				new FunctionPrototypeSummaryImpl("omp_set_nest_lock", "void",
						"omp_nest_lock_t *lock"), null));
		
		funcMap.put("omp_test_lock", new FunctionSummaryImpl(
				"omp_test_lock", "", "Attempts to set a lock but do not block execution of the thread.",
				new FunctionPrototypeSummaryImpl("omp_test_lock", "int",
						"omp_lock_t *lock2"), null));
		funcMap.put("omp_test_nest_lock", new FunctionSummaryImpl(
				"omp_test_nest_lock", "", "Attempts to set a lock but do not block execution of the thread.",
				new FunctionPrototypeSummaryImpl("omp_test_nest_lock", "int",
						"omp_nest_lock_t *lock"), null));
		
		funcMap.put("omp_unset_lock", new FunctionSummaryImpl(
				"omp_unset_lock", "", "Provides the means of releasing ownership of a lock.",
				new FunctionPrototypeSummaryImpl("omp_unset_lock",
						"void", "omp_lock_t *lock"), null));
		funcMap.put("omp_unset_nest_lock", new FunctionSummaryImpl(
				"omp_unset_nest_lock", "", "Provides the means of releasing ownership of a lock.",
				new FunctionPrototypeSummaryImpl("omp_unset_nest_lock",
						"void", "omp_nest_lock_t *lock"), null));
		
		funcMap.put("omp_get_wtime", new FunctionSummaryImpl(
				"omp_get_wtime", "", "Returns the time elapsed from a fixed starting time. The value of the fixed starting time is determined at the start of the current program, and remains constant throughout program execution.",
				new FunctionPrototypeSummaryImpl("omp_get_wtime", "double",
						"void"), null));
		funcMap.put("omp_get_wtick", new FunctionSummaryImpl(
				"omp_get_wtick", "", "Returns the number of seconds between clock ticks.",
				new FunctionPrototypeSummaryImpl("omp_get_wtick", "double",
						"void"), null));

		// try to define constants too...
//		funcMap.put("OMP_DYNAMIC", new FunctionSummaryImpl(
//				"OMP_DYNAMIC", "", "OMP_DYNAMIC description",
//				new FunctionPrototypeSummaryImpl("OMP_DYNAMIC", "ret",
//						"int arg1, int arg2"), null));
		
		return funcMap;
		}
	
	private static final String HTML_DIR = "C:\\ews\\ptpJan06\\org.eclipse.ptp.pldt.openmp.core\\html\\";

	/**
	 * generate html documents (one-time)
	 */
	public static void generateHTML(Map fMap) {
		int count=0;
		Collection fColl = fMap.values();
		for (Iterator iter = fColl.iterator(); iter.hasNext();) {
			FunctionSummaryImpl fsi = (FunctionSummaryImpl) iter.next();
			IFunctionPrototypeSummary fspi = fsi.getPrototype();
			String proto = fspi.getPrototypeString(false);
			String args = fspi.getArguments();
			String rt = fspi.getReturnType();

			if (fsi != null) {
				PrintStream html = null;
				String filename=HTML_DIR+fsi.getName() + ".html";
				try {
					html = new PrintStream(new FileOutputStream(filename));
					html.print("<html><head><title>");
					html.print(fsi.getName());
					html.println("</title>");
					html.println("<body>");
					html.print("<H3>");
					html.print(fsi.getName());
					html.println("</H3>");
					html.println("<PRE>");
					html.println(fspi.getPrototypeString(false));
					html.println("</PRE>");
					html.println("<DL><DD>");
					html.println(fsi.getDescription());
					html.println("<P></DL></body></html>");
				} catch (IOException e) {
					e.printStackTrace();
				} finally {
					if (html != null)
						html.close();
					System.out.println("wrote "+filename);
					count++;
				}
			}
		}
		System.out.println("Wrote "+count+" html files.");
	}
	/**
	 * Run as Java app to generate the HTML files that will be used
	 * for F1 help.
	 * @param args unused
	 */
	public static void main(String[] args) {
		Map map = makeFunctionMap();
		generateHTML(map);
		
	}

}
