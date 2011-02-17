/*******************************************************************************
 * Copyright (c) 2010 University of Illinois All rights reserved. This program
 * and the accompanying materials are made available under the terms of the
 * Eclipse Public License v1.0 which accompanies this distribution, and is
 * available at http://www.eclipse.org/legal/epl-v10.html 
 * 	
 * Contributors: 
 * 	Albert L. Rossi - original API
 ******************************************************************************/
package org.eclipse.ptp.rm.pbs.core;

/**
 * Gathers all internal, unmodifiable string constants into a single place for
 * convenience and code clarity.
 * 
 * @since 5.0
 */
public interface IPBSNonNLSConstants {

	/* CHARACTERS */
	String ZEROSTR = ""; //$NON-NLS-1$
	String SP = " ";//$NON-NLS-1$
	String EQ = "=";//$NON-NLS-1$
	String QT = "\"";//$NON-NLS-1$
	String QM = "?";//$NON-NLS-1$
	String PD = "#";//$NON-NLS-1$
	String CM = ",";//$NON-NLS-1$
	String CO = ":";//$NON-NLS-1$
	String SC = ";";//$NON-NLS-1$
	String LT = "<"; //$NON-NLS-1$
	String LTS = "</";//$NON-NLS-1$
	String GT = ">";//$NON-NLS-1$
	String BKESC = "\\\\";//$NON-NLS-1$
	String BKBKESC = "\\\\\\\\";//$NON-NLS-1$
	String DLESC = "\\$";//$NON-NLS-1$
	String DLESCESC = "\\\\\\$";//$NON-NLS-1$
	String SPESC = "\\\\s";//$NON-NLS-1$
	String LNSEPESC = "\\\\n";//$NON-NLS-1$
	String TBESC = "\\t";//$NON-NLS-1$
	String TBESCESC = "\\\\t";//$NON-NLS-1$
	String LINE_SEP = System.getProperty("line.separator"); //$NON-NLS-1$
	String REMOTE_LINE_SEP = "\n"; //$NON-NLS-1$
	String REMOTE_PATH_SEP = "/"; //$NON-NLS-1$
	String PATH_SEP = System.getProperty("file.separator"); //$NON-NLS-1$

	String TRUE = "true";//$NON-NLS-1$
	String FALSE = "false";//$NON-NLS-1$
	String YES = "yes";;//$NON-NLS-1$
	String NO = "no";;//$NON-NLS-1$

	/* BATCH SCRIPT TEMPLATE */
	String MARKER = "@";//$NON-NLS-1$
	String END_MARKER = ".*@";//$NON-NLS-1$
	String PBS = "PBS";//$NON-NLS-1$
	String PBSDIRECTIVE = PD + PBS + SP;
	String TAG_INTERNAL = "INTERNAL_EXTENSION";//$NON-NLS-1$
	String TAG_MPICMD = "mpiCommand";//$NON-NLS-1$
	String TAG_MPIOPT = "mpiOptions";//$NON-NLS-1$
	String TAG_SCRIPT = "script";//$NON-NLS-1$
	String TAG_PRECMD = "prependedBash";//$NON-NLS-1$
	String TAG_PSTCMD = "postpendedBash";//$NON-NLS-1$
	String MPICORES_FLAG = "-n";//$NON-NLS-1$
	String MPIOPT_DEFAULT = MPICORES_FLAG + " 1";//$NON-NLS-1$
	String MPICMD_DEFAULT = "mpiexec";//$NON-NLS-1$
	String MPICMDS[] = { ZEROSTR, MPICMD_DEFAULT, "mpirun" };//$NON-NLS-1$

	String TAG_ENV = "env";//$NON-NLS-1$
	String TAG_EXPORT = "export";//$NON-NLS-1$
	String TAG_EXECMD = "executablePath";//$NON-NLS-1$
	String TAG_PRARGS = "progArgs";//$NON-NLS-1$

	/*
	 * PBS ATTRIBUTES WHICH MUST BE KNOWN AHEAD OF MODEL DEFINITION
	 */
	String TAG_CHGDIR = "directory";//$NON-NLS-1$
	String TAG_QUEUE = "destination";//$NON-NLS-1$
	String TAG_EXPORT_ENV = "export_all";//$NON-NLS-1$
	String TAG_NCPUS = "Resource_List.ncpus";//$NON-NLS-1$
	String TAG_NODES = "Resource_List.nodes";//$NON-NLS-1$
	String TAG_PPN = "ppn";//$NON-NLS-1$
	String TAG_NDSEP = "[+]";//$NON-NLS-1$
	String ENV_PLACEHOLDER = MARKER + TAG_ENV + MARKER;
	String EXECMD_PLACEHOLDER = MARKER + TAG_EXECMD + MARKER;
	String MPICMD_PLACEHOLDER = MARKER + TAG_MPICMD + MARKER;
	String MPIOPT_PLACEHOLDER = MARKER + TAG_MPIOPT + MARKER;
	String PRECMD_PLACEHOLDER = MARKER + TAG_PRECMD + MARKER;
	String PSTCMD_PLACEHOLDER = MARKER + TAG_PSTCMD + MARKER;
	String PRARGS_PLACEHOLDER = MARKER + TAG_PRARGS + MARKER;
	String CHGDIR_CMD = "cd " + MARKER + TAG_CHGDIR + MARKER;//$NON-NLS-1$

	/* ATTRIBUTE DATA TAGS */
	String ATTRIBUTES = "pbs-job-attributes"; //$NON-NLS-1$
	String ATTRIBUTE = "pbs-job-attribute"; //$NON-NLS-1$
	String DEFINITION = "attribute-definition"; //$NON-NLS-1$
	String FLAG = "qsub-flag"; //$NON-NLS-1$
	String TOOLTIP = "tooltip"; //$NON-NLS-1$
	String DEFAULT = "default"; //$NON-NLS-1$
	String MINSET = "minSet"; //$NON-NLS-1$
	String DESCRIPTION = "description"; //$NON-NLS-1$
	String ID = "id"; //$NON-NLS-1$
	String NAME = "name"; //$NON-NLS-1$
	String TYPE = "type"; //$NON-NLS-1$
	String DISPLAY = "display"; //$NON-NLS-1$
	String MIN = "min"; //$NON-NLS-1$
	String MAX = "max"; //$NON-NLS-1$
	String FORMAT = "format";//$NON-NLS-1$
	String BOOLEAN = "boolean"; //$NON-NLS-1$
	String STRING = "string"; //$NON-NLS-1$
	String DOUBLE = "double"; //$NON-NLS-1$
	String DATE = "date"; //$NON-NLS-1$
	String INTEGER = "integer"; //$NON-NLS-1$
	String CHOICE = "choice";//$NON-NLS-1$

	/* DISPLAY */
	String COURIER = "Courier";//$NON-NLS-1$

	/* FIXED PATHS & FILE NAMES */
	String SRC = "src";//$NON-NLS-1$
	String DATA = "data";//$NON-NLS-1$
	String TEMPLATE_SUFFIX = "_template";//$NON-NLS-1$
	String FULL_TEMPLATE = "full" + TEMPLATE_SUFFIX;//$NON-NLS-1$
	String MIN_TEMPLATE = "min" + TEMPLATE_SUFFIX;//$NON-NLS-1$
	String TMP_ATTR_XML = "all_job_attributes.xml";//$NON-NLS-1$
	String RM_CONFIG_PROPS = "rm_configurations.properties";//$NON-NLS-1$

	/* SERIALIZATION */
	String INDENTATION = "{http://xml.apache.org/xslt}indent-amount";//$NON-NLS-1$
	String INDENT_SPACES = "3";//$NON-NLS-1$

	/* RM & LAUNCH CONFIGURATION */
	String TEMPLATE_PREFIX = "TMPL_";//$NON-NLS-1$
	String TEMPLATE_NAMES = TEMPLATE_PREFIX + "names";//$NON-NLS-1$
	String CURR_TEMPLATE = "_current" + TEMPLATE_SUFFIX; //$NON-NLS-1$
	String PROXY_CONFIG_TYPE = "PROXYTYPE";//$NON-NLS-1$

	/* INTERNAL JOBS */
	String TEMPLATE_CHANGE = "template change";//$NON-NLS-1$
	String REPOPULATE_TEMPLATES = "repopulate templates";//$NON-NLS-1$
	String EDIT_TEMPLATES = "edit templates";//$NON-NLS-1$
	String GET_ATTRIBUTES = "getAttributes";//$NON-NLS-1$
}
