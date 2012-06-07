/**********************************************************************
 * Copyright (c) 2010 IBM Corporation.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/

package org.eclipse.ptp.pldt.openmp.core.editorHelp;

import org.eclipse.osgi.util.NLS;

public class Messages extends NLS {
	private static final String BUNDLE_NAME = "org.eclipse.ptp.pldt.openmp.core.editorHelp.messages"; //$NON-NLS-1$
	public static String OpenMPCHelpBook_destroy_lock;
	public static String OpenMPCHelpBook_destroy_nest_lock;
	public static String OpenMPCHelpBook_init_lock;
	public static String OpenMPCHelpBook_get_active_level;
	public static String OpenMPCHelpBook_get_ancestor_thread_num;
	public static String OpenMPCHelpBook_get_dynamic;
	public static String OpenMPCHelpBook_get_level;
	public static String OpenMPCHelpBook_get_max_active_levels;
	public static String OpenMPCHelpBook_get_max_threads;
	public static String OpenMPCHelpBook_get_nested;
	public static String OpenMPCHelpBook_get_num_procs;
	public static String OpenMPCHelpBook_get_num_threads;
	public static String OpenMPCHelpBook_get_schedule;
	public static String OpenMPCHelpBook_get_team_size;
	public static String OpenMPCHelpBook_get_thread_limit;
	public static String OpenMPCHelpBook_get_thread_num;
	public static String OpenMPCHelpBook_get_wtick;
	public static String OpenMPCHelpBook_get_wtime;
	public static String OpenMPCHelpBook_in_final;
	public static String OpenMPCHelpBook_in_parallel;
	public static String OpenMPCHelpBook_nest_lock;
	public static String OpenMPCHelpBook_omp_c_and_fortran_help_book_title;
	public static String OpenMPCHelpBook_set_dynamic;
	public static String OpenMPCHelpBook_set_lock;
	public static String OpenMPCHelpBook_set_max_active_levels;
	public static String OpenMPCHelpBook_set_nest_lock;
	public static String OpenMPCHelpBook_set_nested;
	public static String OpenMPCHelpBook_set_num_threads;
	public static String OpenMPCHelpBook_set_schedule;
	public static String OpenMPCHelpBook_test_lock;
	public static String OpenMPCHelpBook_test_nest_lock;
	public static String OpenMPCHelpBook_unset_lock;
	public static String OpenMPCHelpBook_unset_nest_lock;
	static {
		// initialize resource bundle
		NLS.initializeMessages(BUNDLE_NAME, Messages.class);
	}

	private Messages() {
	}
}
