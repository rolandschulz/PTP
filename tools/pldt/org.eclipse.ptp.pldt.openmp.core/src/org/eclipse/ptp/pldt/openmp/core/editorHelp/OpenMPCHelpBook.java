/**********************************************************************
 * Copyright (c) 2005,2012 IBM Corporation.
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

import org.eclipse.cdt.ui.IFunctionSummary;
import org.eclipse.cdt.ui.IFunctionSummary.IFunctionPrototypeSummary;
import org.eclipse.ptp.pldt.common.editorHelp.CHelpBookImpl;
import org.eclipse.ptp.pldt.common.editorHelp.FunctionPrototypeSummaryImpl;
import org.eclipse.ptp.pldt.common.editorHelp.FunctionSummaryImpl;
import org.eclipse.ptp.pldt.openmp.core.OpenMPPlugin;

/**
 * Provide help for OpenMP APIs.<br>
 * This includes hover help, F1 help, and content assist (ctrl-space)
 * 
 * @author tibbitts
 * 
 */
public class OpenMPCHelpBook extends CHelpBookImpl {
	private static final String TITLE = Messages.OpenMPCHelpBook_omp_c_and_fortran_help_book_title;

	public OpenMPCHelpBook() {
		super(OpenMPPlugin.getPluginId());
		// populate func map
		funcName2FuncInfo = makeFunctionMap();

		// set title
		setTitle(TITLE);
	}

	/**
	 * Make function map of API names and info.<br>
	 * Note this is also used for (one-time) generation of the html
	 * files by the main() and generateHTML() methods below.
	 * 
	 * @return hashmap of the function names and associated info
	 */
	private static Map<String, IFunctionSummary> makeFunctionMap() {
		Map<String, IFunctionSummary> funcMap = new HashMap<String, IFunctionSummary>();
		funcMap.put("omp_set_num_threads", //$NON-NLS-1$
				new FunctionSummaryImpl("omp_set_num_threads", "", //$NON-NLS-1$ //$NON-NLS-2$
						Messages.OpenMPCHelpBook_set_num_threads,
						new FunctionPrototypeSummaryImpl("omp_set_num_threads", "void", //$NON-NLS-1$ //$NON-NLS-2$
								"int num_threads"), null)); //$NON-NLS-1$

		funcMap.put("omp_get_num_threads", new FunctionSummaryImpl( //$NON-NLS-1$
				"omp_get_num_threads", "", Messages.OpenMPCHelpBook_get_num_threads, //$NON-NLS-1$ //$NON-NLS-2$
				new FunctionPrototypeSummaryImpl("omp_get_num_threads", //$NON-NLS-1$
						"int", "int num_threads"), null)); //$NON-NLS-1$ //$NON-NLS-2$

		funcMap.put("omp_get_max_threads", new FunctionSummaryImpl( //$NON-NLS-1$
				"omp_get_max_threads", "", Messages.OpenMPCHelpBook_get_max_threads, //$NON-NLS-1$ //$NON-NLS-2$
				new FunctionPrototypeSummaryImpl("omp_get_max_threads", //$NON-NLS-1$
						"int", "void"), null)); //$NON-NLS-1$ //$NON-NLS-2$
		funcMap.put("omp_get_thread_num", new FunctionSummaryImpl( //$NON-NLS-1$
				"get_thread_num", "", Messages.OpenMPCHelpBook_get_thread_num, //$NON-NLS-1$ //$NON-NLS-2$
				new FunctionPrototypeSummaryImpl("get_thread_num", //$NON-NLS-1$
						"int", "void"), null)); //$NON-NLS-1$ //$NON-NLS-2$
		funcMap.put("omp_get_num_procs", new FunctionSummaryImpl( //$NON-NLS-1$
				"get_num_procs", "", Messages.OpenMPCHelpBook_get_num_procs, //$NON-NLS-1$ //$NON-NLS-2$
				new FunctionPrototypeSummaryImpl("get_num_procs", "int", //$NON-NLS-1$ //$NON-NLS-2$
						"void"), null)); //$NON-NLS-1$
		funcMap.put("omp_in_parallel", new FunctionSummaryImpl( //$NON-NLS-1$
				"omp_in_parallel", "", //$NON-NLS-1$ //$NON-NLS-2$
				Messages.OpenMPCHelpBook_in_parallel,
				new FunctionPrototypeSummaryImpl("omp_in_parallel", "int", //$NON-NLS-1$ //$NON-NLS-2$
						"void"), null)); //$NON-NLS-1$

		funcMap.put("omp_set_dynamic", new FunctionSummaryImpl( //$NON-NLS-1$
				"omp_set_dynamic", "", Messages.OpenMPCHelpBook_set_dynamic, //$NON-NLS-1$ //$NON-NLS-2$
				new FunctionPrototypeSummaryImpl("omp_set_dynamic", //$NON-NLS-1$
						"void", "int dynamic_threads"), null)); //$NON-NLS-1$ //$NON-NLS-2$
		funcMap.put("omp_get_dynamic", new FunctionSummaryImpl( //$NON-NLS-1$
				"omp_get_dynamic", "", Messages.OpenMPCHelpBook_get_dynamic, //$NON-NLS-1$ //$NON-NLS-2$
				new FunctionPrototypeSummaryImpl("omp_get_dynamic", //$NON-NLS-1$
						"int", "void"), null)); //$NON-NLS-1$ //$NON-NLS-2$

		funcMap.put("omp_set_nested", new FunctionSummaryImpl( //$NON-NLS-1$
				"omp_set_nested", "", Messages.OpenMPCHelpBook_set_nested, //$NON-NLS-1$ //$NON-NLS-2$
				new FunctionPrototypeSummaryImpl("omp_set_nested", //$NON-NLS-1$
						"void", "int nested"), null)); //$NON-NLS-1$ //$NON-NLS-2$
		funcMap.put("omp_get_nested", new FunctionSummaryImpl( //$NON-NLS-1$
				"omp_get_nested", "", Messages.OpenMPCHelpBook_get_nested, //$NON-NLS-1$ //$NON-NLS-2$ 
				new FunctionPrototypeSummaryImpl("omp_get_nested", //$NON-NLS-1$
						"int", "void"), null)); //$NON-NLS-1$ //$NON-NLS-2$

		funcMap.put("omp_init_lock", new FunctionSummaryImpl( //$NON-NLS-1$
				"omp_init_lock", "", Messages.OpenMPCHelpBook_init_lock, //$NON-NLS-1$ //$NON-NLS-2$
				new FunctionPrototypeSummaryImpl("omp_init_lock", "void", //$NON-NLS-1$ //$NON-NLS-2$
						"omp_lock_t *lock"), null)); //$NON-NLS-1$
		funcMap.put("omp_init_nest_lock", new FunctionSummaryImpl( //$NON-NLS-1$
				"omp_init_nest_lock", "", Messages.OpenMPCHelpBook_nest_lock, //$NON-NLS-1$ //$NON-NLS-2$
				new FunctionPrototypeSummaryImpl("omp_init_nest_lock", "void", //$NON-NLS-1$ //$NON-NLS-2$
						"omp_nest_lock_t *lock"), null)); //$NON-NLS-1$

		funcMap.put("omp_destroy_lock", new FunctionSummaryImpl( //$NON-NLS-1$
				"omp_destroy_lock", "", Messages.OpenMPCHelpBook_destroy_lock, //$NON-NLS-1$ //$NON-NLS-2$ 
				new FunctionPrototypeSummaryImpl("omp_destroy_lock", //$NON-NLS-1$
						"void", "omp_lock_t *lock"), null)); //$NON-NLS-1$ //$NON-NLS-2$
		funcMap.put("omp_destroy_nest_lock", new FunctionSummaryImpl( //$NON-NLS-1$
				"omp_destroy_nest_lock", "", Messages.OpenMPCHelpBook_destroy_nest_lock, //$NON-NLS-1$ //$NON-NLS-2$
				new FunctionPrototypeSummaryImpl("omp_destroy_nest_lock", //$NON-NLS-1$
						"void", "omp_nest_lock_t *lock"), null)); //$NON-NLS-1$ //$NON-NLS-2$

		funcMap.put("omp_set_lock", new FunctionSummaryImpl( //$NON-NLS-1$
				"omp_set_lock", "", Messages.OpenMPCHelpBook_set_lock, //$NON-NLS-1$ //$NON-NLS-2$
				new FunctionPrototypeSummaryImpl("omp_set_lock", "void", //$NON-NLS-1$ //$NON-NLS-2$
						"omp_lock_t *lock"), null)); //$NON-NLS-1$
		funcMap.put("omp_set_nest_lock", new FunctionSummaryImpl( //$NON-NLS-1$
				"omp_set_nest_lock", "", Messages.OpenMPCHelpBook_set_nest_lock, //$NON-NLS-1$ //$NON-NLS-2$
				new FunctionPrototypeSummaryImpl("omp_set_nest_lock", "void", //$NON-NLS-1$ //$NON-NLS-2$
						"omp_nest_lock_t *lock"), null)); //$NON-NLS-1$

		funcMap.put("omp_test_lock", new FunctionSummaryImpl( //$NON-NLS-1$
				"omp_test_lock", "", Messages.OpenMPCHelpBook_test_lock, //$NON-NLS-1$ //$NON-NLS-2$
				new FunctionPrototypeSummaryImpl("omp_test_lock", "int", //$NON-NLS-1$ //$NON-NLS-2$
						"omp_lock_t *lock2"), null)); //$NON-NLS-1$
		funcMap.put("omp_test_nest_lock", new FunctionSummaryImpl( //$NON-NLS-1$
				"omp_test_nest_lock", "", Messages.OpenMPCHelpBook_test_nest_lock, //$NON-NLS-1$ //$NON-NLS-2$
				new FunctionPrototypeSummaryImpl("omp_test_nest_lock", "int", //$NON-NLS-1$ //$NON-NLS-2$
						"omp_nest_lock_t *lock"), null)); //$NON-NLS-1$

		funcMap.put("omp_unset_lock", new FunctionSummaryImpl( //$NON-NLS-1$
				"omp_unset_lock", "", Messages.OpenMPCHelpBook_unset_lock, //$NON-NLS-1$ //$NON-NLS-2$
				new FunctionPrototypeSummaryImpl("omp_unset_lock", //$NON-NLS-1$
						"void", "omp_lock_t *lock"), null)); //$NON-NLS-1$ //$NON-NLS-2$
		funcMap.put("omp_unset_nest_lock", new FunctionSummaryImpl( //$NON-NLS-1$
				"omp_unset_nest_lock", "", Messages.OpenMPCHelpBook_unset_nest_lock, //$NON-NLS-1$ //$NON-NLS-2$
				new FunctionPrototypeSummaryImpl("omp_unset_nest_lock", //$NON-NLS-1$
						"void", "omp_nest_lock_t *lock"), null)); //$NON-NLS-1$ //$NON-NLS-2$

		funcMap.put("omp_get_wtime", new FunctionSummaryImpl( //$NON-NLS-1$
				"omp_get_wtime", "", Messages.OpenMPCHelpBook_get_wtime, //$NON-NLS-1$ //$NON-NLS-2$
				new FunctionPrototypeSummaryImpl("omp_get_wtime", "double", //$NON-NLS-1$ //$NON-NLS-2$
						"void"), null)); //$NON-NLS-1$
		funcMap.put("omp_get_wtick", new FunctionSummaryImpl( //$NON-NLS-1$
				"omp_get_wtick", "", Messages.OpenMPCHelpBook_get_wtick, //$NON-NLS-1$ //$NON-NLS-2$
				new FunctionPrototypeSummaryImpl("omp_get_wtick", "double", //$NON-NLS-1$ //$NON-NLS-2$
						"void"), null)); //$NON-NLS-1$
		// new for 6.0, OpenMP 3.1
		funcMap.put("omp_set_schedule", new FunctionSummaryImpl( //$NON-NLS-1$
				"omp_set_schedule", "", Messages.OpenMPCHelpBook_set_schedule, //$NON-NLS-1$ //$NON-NLS-2$
				new FunctionPrototypeSummaryImpl("omp_set_schedule", "void", //$NON-NLS-1$ //$NON-NLS-2$
						"omp_sched_t kind, int modifier"), null)); //$NON-NLS-1$

		funcMap.put("omp_get_schedule", new FunctionSummaryImpl( //$NON-NLS-1$
				"omp_get_schedule", "", Messages.OpenMPCHelpBook_get_schedule, //$NON-NLS-1$ //$NON-NLS-2$
				new FunctionPrototypeSummaryImpl("omp_get_schedule", "void", //$NON-NLS-1$ //$NON-NLS-2$
						"omp_sched_t kind, int modifier"), null)); //$NON-NLS-1$

		funcMap.put("omp_get_thread_limit", new FunctionSummaryImpl( //$NON-NLS-1$
				"omp_get_thread_limit", "", Messages.OpenMPCHelpBook_get_thread_limit, //$NON-NLS-1$ //$NON-NLS-2$
				new FunctionPrototypeSummaryImpl("omp_get_thread_limit", "int", //$NON-NLS-1$ //$NON-NLS-2$
						"void"), null)); //$NON-NLS-1$	

		funcMap.put("omp_set_max_active_levels", new FunctionSummaryImpl( //$NON-NLS-1$
				"omp_set_max_active_levels", "", Messages.OpenMPCHelpBook_set_max_active_levels, //$NON-NLS-1$ //$NON-NLS-2$
				new FunctionPrototypeSummaryImpl("omp_set_max_active_levels", "int", //$NON-NLS-1$ //$NON-NLS-2$
						"void"), null)); //$NON-NLS-1$

		funcMap.put("omp_get_max_active_levels", new FunctionSummaryImpl( //$NON-NLS-1$
				"omp_get_max_active_levels", "", Messages.OpenMPCHelpBook_get_max_active_levels, //$NON-NLS-1$ //$NON-NLS-2$
				new FunctionPrototypeSummaryImpl("omp_get_max_active_levels", "int", //$NON-NLS-1$ //$NON-NLS-2$
						"void"), null)); //$NON-NLS-1$

		funcMap.put("omp_get_level", new FunctionSummaryImpl( //$NON-NLS-1$
				"omp_get_level", "", Messages.OpenMPCHelpBook_get_level, //$NON-NLS-1$ //$NON-NLS-2$
				new FunctionPrototypeSummaryImpl("omp_get_level", "int", //$NON-NLS-1$ //$NON-NLS-2$
						"void"), null)); //$NON-NLS-1$

		funcMap.put("omp_get_ancestor_thread_num", new FunctionSummaryImpl( //$NON-NLS-1$
						Messages.OpenMPCHelpBook_get_ancestor_thread_num,
						"", "Returns, for a given nested level of the current thread, the thread number of the ancestor or the current thread", //$NON-NLS-1$ //$NON-NLS-2$
				new FunctionPrototypeSummaryImpl("omp_get_ancestor_thread_num", "int", //$NON-NLS-1$ //$NON-NLS-2$
						"int level"), null)); //$NON-NLS-1$

		funcMap.put(
				"omp_get_team_size", new FunctionSummaryImpl( //$NON-NLS-1$
						Messages.OpenMPCHelpBook_get_team_size,
						"", "Returns, for a given nested level of the current thread, the size of the thread team to which the ancestor or the current thread belongs", //$NON-NLS-1$ //$NON-NLS-2$
						new FunctionPrototypeSummaryImpl("omp_get_team_size", "int", //$NON-NLS-1$ //$NON-NLS-2$
								"int level"), null)); //$NON-NLS-1$

		funcMap.put("omp_get_active_level", new FunctionSummaryImpl( //$NON-NLS-1$
				Messages.OpenMPCHelpBook_get_active_level,
				"", "Returns the number of nested, active parallel regions enclosing the task that contains the call", //$NON-NLS-1$ //$NON-NLS-2$
				new FunctionPrototypeSummaryImpl("omp_get_active_level", "int", //$NON-NLS-1$ //$NON-NLS-2$
						"void"), null)); //$NON-NLS-1$

		funcMap.put("omp_in_final", new FunctionSummaryImpl( //$NON-NLS-1$
				Messages.OpenMPCHelpBook_in_final,
				"", "Returns true if the routine is executed in a final or included task region; otherwise, it returns false", //$NON-NLS-1$ //$NON-NLS-2$
				new FunctionPrototypeSummaryImpl("omp_in_final", "int", //$NON-NLS-1$ //$NON-NLS-2$
						"void"), null)); //$NON-NLS-1$

		// try to define constants too...
		// funcMap.put("OMP_DYNAMIC", new FunctionSummaryImpl(
		// "OMP_DYNAMIC", "", "OMP_DYNAMIC description",
		// new FunctionPrototypeSummaryImpl("OMP_DYNAMIC", "ret",
		// "int arg1, int arg2"), null));

		return funcMap;
	}

	/** one-time generation of html docs in generateHTML() uses this value */
	private static final String HTML_DIR = "C:\\ews\\ptpJan06\\org.eclipse.ptp.pldt.openmp.core\\html\\"; //$NON-NLS-1$

	/**
	 * generate html documents (one-time)
	 */
	public static void generateHTML(Map fMap) {
		int count = 0;
		Collection fColl = fMap.values();
		for (Iterator iter = fColl.iterator(); iter.hasNext();) {
			FunctionSummaryImpl fsi = (FunctionSummaryImpl) iter.next();
			IFunctionPrototypeSummary fspi = fsi.getPrototype();
			String proto = fspi.getPrototypeString(false);
			String args = fspi.getArguments();
			String rt = fspi.getReturnType();

			if (fsi != null) {
				PrintStream html = null;
				String filename = HTML_DIR + fsi.getName() + ".html"; //$NON-NLS-1$
				try {
					html = new PrintStream(new FileOutputStream(filename));
					html.print("<html><head><title>"); //$NON-NLS-1$
					html.print(fsi.getName());
					html.println("</title>"); //$NON-NLS-1$
					html.println("<body>"); //$NON-NLS-1$
					html.print("<H3>"); //$NON-NLS-1$
					html.print(fsi.getName());
					html.println("</H3>"); //$NON-NLS-1$
					html.println("<PRE>"); //$NON-NLS-1$
					html.println(fspi.getPrototypeString(false));
					html.println("</PRE>"); //$NON-NLS-1$
					html.println("<DL><DD>"); //$NON-NLS-1$
					html.println(fsi.getDescription());
					html.println("<P></DL></body></html>"); //$NON-NLS-1$
				} catch (IOException e) {
					e.printStackTrace();
				} finally {
					if (html != null)
						html.close();
					System.out.println("wrote " + filename); //$NON-NLS-1$
					count++;
				}
			}
		}
		System.out.println("Wrote " + count + " html files."); //$NON-NLS-1$ //$NON-NLS-2$
	}

	/**
	 * Run as Java app to generate the HTML files that will be used
	 * for F1 help.
	 * 
	 * @param args
	 *            unused
	 */
	public static void main(String[] args) {
		Map map = makeFunctionMap();
		generateHTML(map);

	}

}
