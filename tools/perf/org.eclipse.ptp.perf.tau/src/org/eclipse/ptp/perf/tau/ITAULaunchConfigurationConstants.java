/****************************************************************************
 *			Tuning and Analysis Utilities
 *			http://www.cs.uoregon.edu/research/paracomp/tau
 ****************************************************************************
 * Copyright (c) 1997-2006
 *    Department of Computer and Information Science, University of Oregon
 *    Advanced Computing Laboratory, Los Alamos National Laboratory
 *    Research Center Juelich, ZAM Germany	
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Wyatt Spear - initial API and implementation
 ****************************************************************************/
package org.eclipse.ptp.perf.tau;

import org.eclipse.ptp.perf.toolopts.ToolsOptionsConstants;

/**
 * Constant values used by the TAU launch configuration system
 * @author wspear
 *
 */
public interface ITAULaunchConfigurationConstants {
	public static final String MPI = "use_mpi";
	public static final String TAUINC = "use_tauinc_script";
	public static final String CALLPATH = "use_callpath_profiling";
	public static final String MEMORY = "use_memory_profiling";
	public static final String PAPI = "use_papi_library";
	public static final String PERF = "use_perf_library";
	public static final String TRACE = "use_tau_tracing";
	public static final String PHASE = "use_tau_phases";
	public static final String OPENMP = "use_openmp";
	public static final String OPARI = "use_opari";
	public static final String EPILOG = "use_epilog";
	public static final String VAMPIRTRACE = "use_vampirtrace";
	public static final String COMPILER = "use_this_compiler";
	
	public static final String NOPARRUN = "auto_select_BUILDONLY_for_MPI_makefiles";
	public static final String KEEPPROFS = "keep_profile_files";
	
	public static final String PORTAL = "upload_profiles_to_portal";
	
	public static final String ENVVARS = "environment_variable_map";
	
	/**This is for internally restoring the last selected makefile*/
	public static final String TAU_MAKENAME="filename_of_last_selected_makefile";
	
	public static final String TAU_MAKEFILE = "tau_makefile"+ToolsOptionsConstants.TOOL_PANE_ID_SUFFIX;//.performance.options.configuration_id";//"selected_tau_makefile";
	
	public static final boolean MPI_DEF = true;
	public static final boolean TAUINC_DEF = false;
	public static final boolean CALLPATH_DEF = false;
	public static final boolean MEMORY_DEF = false;
	public static final boolean PAPI_DEF = false;
	public static final boolean PERF_DEF = false;
	public static final boolean TRACE_DEF = false;
	public static final boolean PHASE_DEF = false;
	
	public static final boolean EPILOG_DEF = false;
	public static final boolean VAMPIRTRACE_DEF = false;
	
	public static final String COMPILER_DEF = "";
	public static final String MAKEFILE_DEF = "";
	
	public static final boolean KEEPPROFS_DEF = false;
	
	public static final String PAPISELECT = "papi_counter_type_selection";
	
	/**
	 * ID of the int specifying no, internally generated, or external-file-defined selective instrumentation
	 */
	public static final String SELECT = "selective_instrumentation";
	/**
	 * ID of the full string of the selective instrumentation command
	 */
	public static final String SELECT_COMMAND = "selective_instrumentation_arg"+ToolsOptionsConstants.TOOL_PANE_ID_SUFFIX;//.performance.options.configuration_id";
	/**
	 * ID of the selective instrumentation file
	 */
	public static final String SELECT_FILE = "selective_instrumentation_file_path";
	
	/**
	 * ID of the perfdmf database
	 */
	public static final String PERFDMF_DB="perfdmf_database_configuration";
	
	/**
	 * String constant message used if no database names are found
	 */
	public static final String NODB="No databases available";
	
//	/**
//	 * Constant for tracking the global TAU arch directory path preference
//	 */
//	public static final String TAU_ARCH_PATH="TAUCDTArchPath";
//	
//	/**
//	 * Constant for tracking the global TAU bin directory path preference
//	 */
	//public static final String TAU_BIN_PATH="performance_tool_bin_directory.tau";
}
