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
package org.eclipse.ptp.etfw;

/**
 * @author wspear
 * Constants used in ETFw tool definitions
 */
public interface IToolLaunchConfigurationConstants {

	public static final String ATTR_PERFORMANCEBUILD_CONFIGURATION_NAME = "ATTR_PERFORMANCEBUILD_CONFIGURATION_NAME"; //$NON-NLS-1$

	public static final String BUILDONLY = "build_but_do_not_run_instrumented_executable"; //$NON-NLS-1$
	public static final boolean BUILDONLY_DEF = false;

	public static final String ANALYZEONLY = "analyze_existing_perf_data_in_specified_location_build/run_nothing"; //$NON-NLS-1$
	public static final boolean ANALYZEONLY_DEF = false;

	/**
	 * @since 5.0
	 */
	public static final String INTERNAL = "internal"; //$NON-NLS-1$
	/**
	 * @since 5.0
	 */
	public static final String FILE_SWAP = "%%FILE%%"; //$NON-NLS-1$
	/**
	 * @since 5.0
	 */
	public static final String FILENAME_SWAP = "%%FILENAME%%"; //$NON-NLS-1$

	/**
	 * @since 5.0
	 */
	public static final String PROJECT_DIR = "%%PROJECT_DIR%%"; //$NON-NLS-1$

	/**
	 * @since 7.0
	 */
	public static final String ETFW_VERSION = "ETFW_VERSION"; //$NON-NLS-1$

	/**
	 * @since 7.0
	 */
	public static final String USE_SAX_PARSER = "sax-parser"; //$NON-NLS-1$

	/**
	 * @since 7.0
	 */
	public static final String USE_JAXB_PARSER = "jaxb-parser"; //$NON-NLS-1$
	/**
	 * ID for boolean: true = keep instrumented executable
	 */
	// public static final String NOCLEAN = "keep_instrumented_executable";
	// public static final boolean NOCLEAN_DEF = false;

	/**
	 * The name appended to new build configurations designated by performance tool recompilation
	 */
	public static final String EXTOOL_RECOMPILE = "true_if_recompiling_for_analysis"; //$NON-NLS-1$
	public static final String TOOLCONFNAME = "performance_tool_build_configuration_name_modifier."; //$NON-NLS-1$
	public static final String DEFAULT_TOOLCONFNAME = "PerformanceAnalysis"; //$NON-NLS-1$

	public static final String SELECTED_TOOL = "selected_performance_tool"; //$NON-NLS-1$

	// public static final String COMPILER_REPLACE="totally_replace_default_compiler";
	// public static final boolean COMPILER_REPLACE_DEF=false;
	//
	// public static final String CC_COMPILER="performance_tool_cc_compiler";
	// public static final String CXX_COMPILER="performance_tool_cxx_compiler";
	// public static final String F90_COMPILER="performance_tool_f90_compiler";
	public static final String EMPTY_STRING = ""; //$NON-NLS-1$

	/**
	 * ID for the application prepended to the execution command
	 */
	// public static final String EXEC_UTIL_LIST="list_of_performance_analysis_execution_utilities";
	// public static final String DEF_EXEC_UTIL=null;
	//
	public static final String USE_EXEC_UTIL = "true_if_using_performance_analysis_execution_utility"; //$NON-NLS-1$
	//
	// public static final String EXEC_UTIL_ARGS="ID_for_list_of_arguments_to_the_execution_utilities";
	// public static final String DEF_EXEC_UTIL_ARGS=EMPTY_STRING;

	// public static final String SAVE_PROGRAM="origional_program_being_exicuted";
	// public static final String SAVE_ARGS="origional_args_being_passed";

	/**
	 * ID for List of post-launch performance analysis commands
	 * TODO: This needs to be made more featureful/general
	 */
	// public static final String TOOL_LIST="performance_analysis_tool_and_argument_list";
	// public static final String TOOL_ARGS="arguments_to_tools_in_TOOL_LIST";

	/**
	 * ID for String: location of the xml file defining tools
	 */
	public static final String XMLLOCID = "tool_def_xml_location"; //$NON-NLS-1$

	/*
	 * ID for boolean: true = use TAU launch system rather than xml-driven toolset
	 */
	// public static final String TAULAUNCH="use_tau_launch_system";//TODO: This is a stop-gap until TAU support is made fully
	// modular
	// public static final boolean TAULAUNCH_DEF=false;

	/**
	 * This string plus the tool ID is the string associated with tool's bin path in the workspace preferences
	 */
	public static final String TOOL_BIN_ID = "performance_tool_bin_directory"; //$NON-NLS-1$

	// public static final String BUILDCONF_PRFX_ID="performance_build_configuration_name.";
	/**
	 * Key in which which executable name will be stored in the ILaunchConfiguration
	 */
	public static final String EXTOOL_EXECUTABLE_NAME = "perf_executable_name"; //$NON-NLS-1$
	public static final String EXTOOL_PROJECT_NAME = "perf_project_name"; //$NON-NLS-1$
	public static final String EXTOOL_ATTR_ARGUMENTS_TAG = "perf_framework_attribute_for_arguments_value"; //$NON-NLS-1$
	public static final String EXTOOL_EXECUTABLE_NAME_TAG = "perf_executable_name_tag_for_name_value"; //$NON-NLS-1$
	public static final String EXTOOL_PROJECT_NAME_TAG = "perf_project_name_tag_for_name_value"; //$NON-NLS-1$
	public static final String EXTOOL_EXECUTABLE_PATH_TAG = "perf_executable_path_tag_for_path_value"; //$NON-NLS-1$

	/**
	 * @since 5.0
	 */
	public static final String EXTOOL_JAXB_ATTR_ARGUMENTS_TAG = "perf_framework_attribute_for_JAXB_arguments_value"; //$NON-NLS-1$
	/**
	 * @since 5.0
	 */
	public static final String EXTOOL_JAXB_EXECUTABLE_PATH_TAG = "perf_executable_path_tag_for_JAXB_path_value"; //$NON-NLS-1$

	/**
	 * @since 7.0
	 */
	public static final String EXTOOL_JAXB_EXECUTABLE_DIRECTORY_TAG = "perf_executable_directory_tag_for_JAXB_exec_dir_value"; //$NON-NLS-1$

	/**
	 * @since 5.0
	 */
	public static final String DOT = "."; //$NON-NLS-1$
	/**
	 * @since 5.0
	 */
	public static final String SPACE = " "; //$NON-NLS-1$
	/**
	 * @since 5.0
	 */
	public static final String NEWLINE = "\n"; //$NON-NLS-1$
	/**
	 * @since 5.0
	 */
	public static final String EMPTY = ""; //$NON-NLS-1$

	/**
	 * @since 5.0
	 */
	public static final String UNIX_SLASH = "/"; //$NON-NLS-1$

	// The following are for parametric test values

	public static final String PARA_NUM_PROCESSORS = "processors to use in parametric study"; //$NON-NLS-1$
	public static final String PARA_OPT_LEVELS = "compiler optimization levels to use in parametric study"; //$NON-NLS-1$

	public static final String PARA_ALL_COMBO = "use simple weak scaling in perf parametric tests"; //$NON-NLS-1$

	public static final String PARA_ARG_NAMES = "argument names use in parametric study"; //$NON-NLS-1$
	public static final String PARA_ARG_BOOLS = "tells which para args are checked"; //$NON-NLS-1$

	public static final String PARA_ARG_VALUES = "argument values use in parametric study"; //$NON-NLS-1$

	public static final String PARA_VAR_NAMES = "env-var names to use in parametric study"; //$NON-NLS-1$
	public static final String PARA_VAR_BOOLS = "tells which para vars are checked"; //$NON-NLS-1$

	public static final String PARA_VAR_VALUES = "env-var values to use in parametric study"; //$NON-NLS-1$

	public static final String PARA_PERF_SCRIPT = "path to the perfexplorer script to use in parametric study"; //$NON-NLS-1$
	// public static final String PARA_VAR_VALUES="env-var values to use in parametric study";

	public static final String PARA_USE_PARAMETRIC = "use the parametric analysis system"; //$NON-NLS-1$

	public static final String EXTOOL_LAUNCH_PERFEX = "launch perfexplorer along with other tau profile management activity"; //$NON-NLS-1$
	public static final String EXTOOL_EXPERIMENT_APPEND = "string that will override the default experiment identifier used in perfdmf storage"; //$NON-NLS-1$
	public static final String EXTOOL_XML_METADATA = "string representation of xml metadata file for perfexplorer"; //$NON-NLS-1$

}
