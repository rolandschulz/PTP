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
package org.eclipse.ptp.tau.core.internal;

public interface ITAULaunchConfigurationConstants {
	public static final String TAU_MAKEFILE = "tau_makefile";
	public static final String TAU_RUN_OPTS = "tau_runtime_options";
	public static final String TAU_ARCH_DIR = "tau_archetecture_directory";
	
	public static final String MPI = "use_mpi";
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
	
	public static final String BUILDONLY = "build_but_do_not_run_instrumented_executable";
	public static final String NOCLEAN = "keep_tau_instrumented_executable";
	public static final String KEEPPROFS = "keep_profile_files";
	
	public static final String ENVVARS = "environment_variable_map";
	
	public static final String MAKEFILE = "selected_tau_makefile";
	
	public static final boolean MPI_DEF = true;
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
	
	public static final boolean BUILDONLY_DEF = false;
	public static final boolean NOCLEAN_DEF = false;
	public static final boolean KEEPPROFS_DEF = false;
	
	public static final String SELECT = "selective_instrumentation";
	public static final String PAPISELECT = "papi_counter_type_selection";
	public static final String SELECT_FILE = "selective_instrumentation_FILE";
}
