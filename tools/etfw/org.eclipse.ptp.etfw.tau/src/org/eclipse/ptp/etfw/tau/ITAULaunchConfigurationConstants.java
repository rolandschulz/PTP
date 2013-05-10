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
package org.eclipse.ptp.etfw.tau;

import org.eclipse.ptp.etfw.tau.messages.Messages;
import org.eclipse.ptp.etfw.toolopts.ToolsOptionsConstants;

/**
 * Constant values used by the TAU launch configuration system
 * 
 * @author wspear
 * 
 */
public interface ITAULaunchConfigurationConstants {
	public static final String MPI = "use_mpi"; //$NON-NLS-1$
	public static final String TAUINC = "use_tauinc_script"; //$NON-NLS-1$
	public static final String CALLPATH = "use_callpath_profiling"; //$NON-NLS-1$
	public static final String MEMORY = "use_memory_profiling"; //$NON-NLS-1$
	public static final String PAPI = "use_papi_library"; //$NON-NLS-1$
	public static final String PERF = "use_perf_library"; //$NON-NLS-1$
	public static final String TRACE = "use_tau_tracing"; //$NON-NLS-1$
	public static final String PDT = "use_tau_with_PDT"; //$NON-NLS-1$
	public static final String PHASE = "use_tau_phases"; //$NON-NLS-1$
	public static final String OPENMP = "use_openmp"; //$NON-NLS-1$
	public static final String OPARI = "use_opari"; //$NON-NLS-1$
	public static final String EPILOG = "use_epilog"; //$NON-NLS-1$
	public static final String VAMPIRTRACE = "use_vampirtrace"; //$NON-NLS-1$
	public static final String COMPILER = "use_this_compiler"; //$NON-NLS-1$

	public static final String NOPARRUN = "auto_select_BUILDONLY_for_MPI_makefiles"; //$NON-NLS-1$
	public static final String KEEPPROFS = "keep_profile_files"; //$NON-NLS-1$

	public static final String PROFSUMMARY = "print_profile_summary_only"; //$NON-NLS-1$

	public static final String PORTAL = "upload_profiles_to_portal"; //$NON-NLS-1$

	public static final String ENVVARS = "environment_variable_map"; //$NON-NLS-1$

	/** This is for internally restoring the last selected makefile */
	public static final String TAU_MAKENAME = "filename_of_last_selected_makefile"; //$NON-NLS-1$

	public static final String TAU_MAKEFILE = "tau_makefile" + ToolsOptionsConstants.TOOL_PANE_ID_SUFFIX;//.performance.options.configuration_id";//"selected_tau_makefile"; //$NON-NLS-1$

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

	public static final String COMPILER_DEF = ""; //$NON-NLS-1$
	public static final String MAKEFILE_DEF = ""; //$NON-NLS-1$

	public static final boolean KEEPPROFS_DEF = false;

	public static final String PAPISELECT = "papi_counter_type_selection"; //$NON-NLS-1$

	public static final String PDTSELECT = "pdt_or_compiler_inst_selection"; //$NON-NLS-1$

	/**
	 * ID of the int specifying no, internally generated, or external-file-defined selective instrumentation
	 */
	public static final String SELECT = "selective_instrumentation"; //$NON-NLS-1$
	/**
	 * ID of the full string of the selective instrumentation command
	 */
	public static final String SELECT_COMMAND = "selective_instrumentation_arg" + ToolsOptionsConstants.TOOL_PANE_ID_SUFFIX;//.performance.options.configuration_id"; //$NON-NLS-1$
	/**
	 * ID of the selective instrumentation file shown in the UI, if any
	 */
	public static final String SELECT_FILE = "selective_instrumentation_file_path_shown"; //$NON-NLS-1$

	/**
	 * ID of the selective instrumentation file actually used
	 */
	// public static final String INTERNAL_SELECTIVE_FILE = "selective_instrumentation_file_path_used_by_TAU";

	/**
	 * ID of variable to indicate use of automatic selective instrumentation file generation with tau_reduce
	 */
	public static final String TAU_REDUCE = "use_automatic_tau_reduce"; //$NON-NLS-1$

	/**
	 * ID of the perfdmf database
	 */
	public static final String PERFDMF_DB = "perfdmf_database_configuration"; //$NON-NLS-1$

	/**
	 * Simple name of the perfdmf database
	 */
	public static final String PERFDMF_DB_NAME = "perfdmf_database_simple_name"; //$NON-NLS-1$

	/**
	 * String constant message used if no database names are found
	 */
	public static final String NODB = Messages.ITAULaunchConfigurationConstants_NoDatabasesAvailable;

	public static final String TAU_CHECK_AUTO_OPT = "TAUCheckForAutoOptions";
	public static final String TAU_CHECK_AIX_OPT = "TAUCheckForAIXOptions";

	// public static final String PARA_PERF_SCRIPT="path to the perfexplorer script to use in parametric study";
	// public static final String PARA_VAR_VALUES="env-var values to use in parametric study";

	// public static final String EXTOOL_LAUNCH_PERFEX="launch perfexplorer along with other tau profile management activity";

	// /**
	// * Constant for tracking the global TAU arch directory path preference
	// */
	// public static final String TAU_ARCH_PATH="TAUCDTArchPath";
	//
	// /**
	// * Constant for tracking the global TAU bin directory path preference
	// */
	// public static final String TAU_BIN_PATH="performance_tool_bin_directory.tau";
}
