/*******************************************************************************
 * Copyright (c) 2011-2014 University of Illinois All rights reserved. This program
 * and the accompanying materials are made available under the terms of the
 * Eclipse Public License v1.0 which accompanies this distribution, and is
 * available at http://www.eclipse.org/legal/epl-v10.html 
 * 
 * Contributors: 
 * 	Albert L. Rossi - design and implementation
 * 
 * Modified by:
 * 		Claudia Knobloch, Forschungszentrum Juelich GmbH
 ******************************************************************************/
package org.eclipse.ptp.rm.lml.core;

import org.eclipse.ptp.internal.rm.lml.core.LMLCorePlugin;

public interface ILMLCoreConstants {
	public static final String PLUGIN_ID = LMLCorePlugin.getUniqueIdentifier();
	public static final String PREFIX = PLUGIN_ID + "."; //$NON-NLS-1$

	public static final String ID_ACTIVE_JOBS_VIEW = "org.eclipse.ptp.rm.lml.ui.ActiveJobsView"; //$NON-NLS-1$
	public static final String ID_INACTIVE_JOBS_VIEW = "org.eclipse.ptp.rm.lml.ui.InactiveJobsView"; //$NON-NLS-1$
	// Names of attributes for LML_DA for activating / deactivating the cache
	public static final String LMLDACACHEINTERVAL_ATTRIBUTE = "cacheinterval"; //$NON-NLS-1$
	public static final String LMLDACACHE_ATTRIBUTE = "cache"; //$NON-NLS-1$

	public static final int UNDEFINED = -1;

	public static final int COPY_BUFFER_SIZE = 64 * 1024;
	public static final int STREAM_BUFFER_SIZE = 8 * 1024;
	public static final int EOF = -1;
	public static final long MINUTE_IN_MS = 60 * 60 * 1000;
	public static final long VALIDATE_TIMER = 500;
	public static final long STANDARD_WAIT = 1000;
	public static final int READY_FILE_BLOCK = 60;

	/* CHARACTERS */
	public static final String LEN = "N";//$NON-NLS-1$
	public static final String ZEROSTR = "";//$NON-NLS-1$
	public static final String TAB = "\t"; //$NON-NLS-1$
	public static final String SP = " ";//$NON-NLS-1$
	public static final String EQ = "=";//$NON-NLS-1$
	public static final String NEQ = "!=";//$NON-NLS-1$
	public static final String SI = "=~";//$NON-NLS-1$
	public static final String NSI = "!~";//$NON-NLS-1$
	public static final String QT = "\"";//$NON-NLS-1$
	public static final String QM = "?";//$NON-NLS-1$
	public static final String PD = "#";//$NON-NLS-1$
	public static final String PDRX = "[#]";//$NON-NLS-1$
	public static final String CM = ",";//$NON-NLS-1$
	public static final String CO = ":";//$NON-NLS-1$
	public static final String SC = ";";//$NON-NLS-1$
	public static final String LT = "<"; //$NON-NLS-1$
	public static final String LE = "<="; //$NON-NLS-1$
	public static final String LTS = "</";//$NON-NLS-1$
	public static final String GT = ">";//$NON-NLS-1$
	public static final String GE = ">=";//$NON-NLS-1$
	public static final String GTLT = "><";//$NON-NLS-1$
	public static final String HYPH = "-";//$NON-NLS-1$
	public static final String AT = "@";//$NON-NLS-1$
	public static final String DOL = "$";//$NON-NLS-1$
	public static final String PIP = "|";//$NON-NLS-1$
	public static final String REGPIP = "[|]";//$NON-NLS-1$
	public static final String DOT = ".";//$NON-NLS-1$
	public static final String Z3 = "000";//$NON-NLS-1$
	public static final String Z2 = "00";//$NON-NLS-1$
	public static final String OPENP = "(";//$NON-NLS-1$
	public static final String OPENSQ = "[";//$NON-NLS-1$
	public static final String OPENV = "${";//$NON-NLS-1$
	public static final String OPENVRM = "${rm:";//$NON-NLS-1$
	public static final String OPENVLT = "${lt:";//$NON-NLS-1$
	public static final String VRM = "rm:";//$NON-NLS-1$
	public static final String VLC = "lc:";//$NON-NLS-1$
	public static final String CLOSP = ")";//$NON-NLS-1$
	public static final String CLOSSQ = "]";//$NON-NLS-1$
	public static final String CLOSV = "}";//$NON-NLS-1$
	public static final String CLOSVAL = "#value}";//$NON-NLS-1$
	public static final String BKESC = "\\\\";//$NON-NLS-1$
	public static final String BKBKESC = "\\\\\\\\";//$NON-NLS-1$
	public static final String DLESC = "\\$";//$NON-NLS-1$
	public static final String DLESCESC = "\\\\\\$";//$NON-NLS-1$
	public static final String SPESC = "\\\\s";//$NON-NLS-1$
	public static final String LNSEPESC = "\\\\n";//$NON-NLS-1$
	public static final String TBESC = "\\t";//$NON-NLS-1$
	public static final String TBESCESC = "\\\\t";//$NON-NLS-1$
	public static final String LNESC = "\\n";//$NON-NLS-1$
	public static final String RTESC = "\\r";//$NON-NLS-1$
	public static final String LINE_SEP = System.getProperty("line.separator"); //$NON-NLS-1$
	public static final String REMOTE_LINE_SEP = "\n"; //$NON-NLS-1$
	public static final String REMOTE_PATH_SEP = "/"; //$NON-NLS-1$
	public static final String PATH_SEP = System.getProperty("file.separator"); //$NON-NLS-1$

	/* KEY WORDS */
	public static final String TRUE = "true";//$NON-NLS-1$
	public static final String FALSE = "false";//$NON-NLS-1$
	public static final String YES = "yes";//$NON-NLS-1$
	public static final String NO = "no";//$NON-NLS-1$
	public static final String NOT = "not";//$NON-NLS-1$
	public static final String OR = "or";//$NON-NLS-1$
	public static final String AND = "and";//$NON-NLS-1$
	public static final String xEQ = "EQ";//$NON-NLS-1$
	public static final String xLT = "LT";//$NON-NLS-1$
	public static final String xLT_LC = "lt";//$NON-NLS-1$
	public static final String xGT = "GT";//$NON-NLS-1$
	public static final String xGT_LC = "gt";//$NON-NLS-1$
	public static final String xLE = "LE";//$NON-NLS-1$
	public static final String xLE_LC = "le";//$NON-NLS-1$
	public static final String xGE = "GE";//$NON-NLS-1$
	public static final String xGE_LC = "ge";//$NON-NLS-1$
	public static final String GET = "get";//$NON-NLS-1$
	public static final String SET = "set";//$NON-NLS-1$
	public static final String IS = "is";//$NON-NLS-1$
	public static final String CLASS = "class";//$NON-NLS-1$
	public static final String STRING = "string";//$NON-NLS-1$
	public static final String NAME = "name";//$NON-NLS-1$
	public static final String VALUE = "value";//$NON-NLS-1$
	public static final String BASIC = "basic";//$NON-NLS-1$
	public static final String CHOICE = "choice";//$NON-NLS-1$
	public static final String sDEFAULT = "default";//$NON-NLS-1$
	public static final String DESC = "description";//$NON-NLS-1$
	public static final String MAX = "max";//$NON-NLS-1$
	public static final String MIN = "min";//$NON-NLS-1$
	public static final String READONLY = "readOnly";//$NON-NLS-1$
	public static final String STATUS = "status";//$NON-NLS-1$
	public static final String TOOLTIP = "tooltip";//$NON-NLS-1$
	public static final String TYPE = "type";//$NON-NLS-1$
	public static final String VALIDATOR = "validator";//$NON-NLS-1$
	public static final String SELECTED = "visible";//$NON-NLS-1$
	public static final String LOCAL = "local";//$NON-NLS-1$
	public static final String JOB_ID_TAG = "@jobId";//$NON-NLS-1$
	public static final String NAME_TAG = AT + NAME;
	public static final String VALUE_TAG = AT + VALUE;
	public static final String EMPTY = ""; //$NON-NLS-1$

	/* TYPE MATCHING */
	public static final String NT = "nt";//$NON-NLS-1$
	public static final String BOOL = "bool";//$NON-NLS-1$
	public static final String ET = "et";//$NON-NLS-1$
	public static final String IST = "ist";//$NON-NLS-1$
	public static final String ECTOR = "ector";//$NON-NLS-1$

	/* STANDARD PROPERTIES */
	public static final String ID = "id";//$NON-NLS-1$
	public static final String JAVA_USER_HOME = "user.home";//$NON-NLS-1$
	public static final String JAVA_TMP_DIR = "java.io.tmpdir";//$NON-NLS-1$
	public static final String FILE_SCHEME = "file";//$NON-NLS-1$
	public static final String XMLSchema = "http://www.w3.org/2001/XMLSchema"; //$NON-NLS-1$
	public static final String SCHEMA = "schema/"; //$NON-NLS-1$
	public static final String RM_XSD = SCHEMA + "lgui.xsd";//$NON-NLS-1$
	public static final String JAXB = "JAXB";//$NON-NLS-1$
	public static final String JAXB_CONTEXT = "org.eclipse.ptp.rm.jaxb.core.data";//$NON-NLS-1$
	public static final String RM_CONFIG_PROPS = "rm_configurations.properties";//$NON-NLS-1$
	public static final String RM_XSD_PATH = "rm_schema_path";//$NON-NLS-1$
	public static final String RM_XSD_URL = "rm_schema_url";//$NON-NLS-1$
	public static final String PREV_RM_XSD_PATH = "prev_rm_schema_path";//$NON-NLS-1$
	public static final String EXTERNAL_RM_XSD_PATHS = "external_rm_schema_paths";//$NON-NLS-1$
	public static final String CHECKED_ATTRIBUTES = "checked_attributes";//$NON-NLS-1$
	public static final String SHOW_ONLY_CHECKED = "show_only_checked";//$NON-NLS-1$
	public static final String IS_PRESET = "is_preset";//$NON-NLS-1$
	public static final String SCRIPT_PATH = "script_path";//$NON-NLS-1$
	public static final String SCRIPT = "script";//$NON-NLS-1$
	public static final String SCRIPT_FILE = "managed_file_for_script";//$NON-NLS-1$
	public static final String CSH = "csh";//$NON-NLS-1$
	public static final String SH = ".sh";//$NON-NLS-1$
	public static final String SETENV = "setenv";//$NON-NLS-1$
	public static final String EXPORT = "export";//$NON-NLS-1$
	public static final String DATE_FORMAT = "yyyy/MM/dd HH:mm:ss";//$NON-NLS-1$

	public static final String RESOURCE_MANAGERS = "resourceManagers";//$NON-NLS-1$
	public static final String CUSTOM = "custom";//$NON-NLS-1$
	public static final String CONTROL_USER_NAME = "controlUserName";//$NON-NLS-1$
	public static final String MONITOR_USER_NAME = "monitorUserName";//$NON-NLS-1$
	public static final String CONTROL_CONNECTION_NAME = "controlConnectionName";//$NON-NLS-1$
	public static final String MONITOR_CONNECTION_NAME = "monitorConnectionName";//$NON-NLS-1$
	public static final String LOCALHOST = "localhost";//$NON-NLS-1$
	public static final String CONTROL_PATH = "controlPath"; //$NON-NLS-1$
	public static final String MONITOR_PATH = "monitorPath"; //$NON-NLS-1$
	public static final String CONTROL_OPTIONS = "controlOptions"; //$NON-NLS-1$
	public static final String MONITOR_OPTIONS = "monitorOptions"; //$NON-NLS-1$
	public static final String CONTROL_INVOCATION_OPTIONS = "controlInvocationOptions"; //$NON-NLS-1$
	public static final String MONITOR_INVOCATION_OPTIONS = "monitorInvocationOptions"; //$NON-NLS-1$
	public static final String CONTROL_ADDRESS = "controlAddress"; //$NON-NLS-1$
	public static final String LOCAL_ADDRESS = "localAddress"; //$NON-NLS-1$
	public static final String MONITOR_ADDRESS = "monitorAddress"; //$NON-NLS-1$
	public static final String CONTROL_USER_VAR = "control.user.name";//$NON-NLS-1$
	public static final String CONTROL_ADDRESS_VAR = "control.address";//$NON-NLS-1$
	public static final String MONITOR_USER_VAR = "monitor.user.name";//$NON-NLS-1$
	public static final String MONITOR_ADDRESS_VAR = "monitor.address";//$NON-NLS-1$
	public static final String ARPA = ".in-addr.arpa";//$NON-NLS-1$
	//	public static final String ECLIPSESETTINGS = ".eclipsesettings";//$NON-NLS-1$
	public static final String DEBUG_PACKAGE = "org.eclipse.debug";//$NON-NLS-1$
	public static final String PTP_PACKAGE = "org.eclipse.ptp";//$NON-NLS-1$

	//	public static final String STARTUP = "OnStartUp";//$NON-NLS-1$
	//	public static final String SHUTDOWN = "OnShutDown";//$NON-NLS-1$
	//	public static final String DISCATTR = "DiscoverAttributes";//$NON-NLS-1$
	//	public static final String JOBSTATUS = "GetJobStatus";//$NON-NLS-1$
	//	public static final String VALIDATE = "ValidateJob";//$NON-NLS-1$

	public static final String JOB_ATTRIBUTE = "jobAttribute";//$NON-NLS-1$
	public static final String ATTRIBUTE = "attribute";//$NON-NLS-1$
	public static final String PROPERTY = "property";//$NON-NLS-1$
	public static final String QUEUES = "available_queues";//$NON-NLS-1$
	public static final String JOB_ID = "job_id";//$NON-NLS-1$
	public static final String RM_ID = "rm_id";//$NON-NLS-1$
	public static final String STDOUT_REMOTE_FILE = "stdout_remote_path";//$NON-NLS-1$
	public static final String STDERR_REMOTE_FILE = "stderr_remote_path";//$NON-NLS-1$
	public static final String EXEC_PATH = "executablePath";//$NON-NLS-1$
	public static final String PROG_ARGS = "progArgs";//$NON-NLS-1$
	public static final String DIRECTORY = "directory";//$NON-NLS-1$
	public static final String MPI_CMD = "mpiCommand";//$NON-NLS-1$
	public static final String MPI_ARGS = "mpiArgs";//$NON-NLS-1$

	public static final String CASE_INSENSITIVE = "CASE_INSENSITIVE";//$NON-NLS-1$
	public static final String MULTILINE = "MULTILINE";//$NON-NLS-1$
	public static final String DOTALL = "DOTALL";//$NON-NLS-1$
	public static final String UNICODE_CASE = "UNICODE_CASE";//$NON-NLS-1$
	public static final String CANON_EQ = "CANON_EQ";//$NON-NLS-1$
	public static final String LITERAL = "LITERAL";//$NON-NLS-1$
	public static final String COMMENTS = "COMMENTS";//$NON-NLS-1$

	public static final String DOT_XML = ".xml";//$NON-NLS-1$
	public static final String TOKENIZER_EXT_PT = "streamParserTokenizer";//$NON-NLS-1$
	public static final String TAIL = "tail";//$NON-NLS-1$
	public static final String MINUS_F = "-F";//$NON-NLS-1$
	public static final String CONFIGURATION_FILE_ATTRIBUTE = "configurationFile"; //$NON-NLS-1$
	public static final String RM_CONFIG_EXTENSION_POINT = "org.eclipse.ptp.rm.jaxb.core.JAXBResourceManagerConfigurations"; //$NON-NLS-1$
	public static final String IMPORTED_JAXB_CONFIG = "org.eclipse.ptp.rm.jaxb.ImportedConfigurations"; //$NON-NLS-1$
	/*
	 * EFS Attributes
	 */
	public static final String ATTRIBUTE_READ_ONLY = "ATTRIBUTE_READ_ONLY";//$NON-NLS-1$
	public static final String ATTRIBUTE_IMMUTABLE = "ATTRIBUTE_IMMUTABLE";//$NON-NLS-1$
	public static final String ATTRIBUTE_OWNER_READ = "ATTRIBUTE_OWNER_READ";//$NON-NLS-1$
	public static final String ATTRIBUTE_OWNER_WRITE = "ATTRIBUTE_OWNER_WRITE";//$NON-NLS-1$
	public static final String ATTRIBUTE_OWNER_EXECUTE = "ATTRIBUTE_OWNER_EXECUTE";//$NON-NLS-1$
	public static final String ATTRIBUTE_GROUP_READ = "ATTRIBUTE_GROUP_READ";//$NON-NLS-1$
	public static final String ATTRIBUTE_GROUP_WRITE = "ATTRIBUTE_GROUP_WRITE";//$NON-NLS-1$
	public static final String ATTRIBUTE_GROUP_EXECUTE = "ATTRIBUTE_GROUP_EXECUTE";//$NON-NLS-1$
	public static final String ATTRIBUTE_OTHER_READ = "ATTRIBUTE_OTHER_READ";//$NON-NLS-1$
	public static final String ATTRIBUTE_OTHER_WRITE = "ATTRIBUTE_OTHER_WRITE";//$NON-NLS-1$
	public static final String ATTRIBUTE_OTHER_EXECUTE = "ATTRIBUTE_OTHER_EXECUTE";//$NON-NLS-1$
	public static final String ATTRIBUTE_EXECUTABLE = "ATTRIBUTE_EXECUTABLE";//$NON-NLS-1$
	public static final String ATTRIBUTE_ARCHIVE = "ATTRIBUTE_ARCHIVE";//$NON-NLS-1$
	public static final String ATTRIBUTE_HIDDEN = "ATTRIBUTE_HIDDEN";//$NON-NLS-1$
	public static final String ATTRIBUTE_SYMLINK = "ATTRIBUTE_SYMLINK";//$NON-NLS-1$
	public static final String ATTRIBUTE_LINK_TARGET = "ATTRIBUTE_LINK_TARGET";//$NON-NLS-1$

	/*
	 * IRemoteProcessBuilder
	 */
	public static final String TAG_NONE = "NONE";//$NON-NLS-1$
	public static final String TAG_ALLOCATE_PTY = "ALLOCATE_PTY";//$NON-NLS-1$
	public static final String TAG_FORWARD_X11 = "FORWARD_X11";//$NON-NLS-1$

	/*
	 * Element keywords
	 */
	public static final String INCLUDE_ELEMENT = "include"; //$NON-NLS-1$
	public static final String SELECT_ELEMENT = "select"; //$NON-NLS-1$
	public static final String TABLE_ELEMENT = "table"; //$NON-NLS-1$
	public static final String USAGEBAR_ELEMENT = "usagebar"; //$NON-NLS-1$
	public static final String TEXT_ELEMENT = "text"; //$NON-NLS-1$
	public static final String INFOBOX_ELEMENT = "infobox"; //$NON-NLS-1$
	public static final String CHART_ELEMENT = "chart"; //$NON-NLS-1$
	public static final String CHARTGROUP_ELEMENT = "chartgroup"; //$NON-NLS-1$
	public static final String NODEDISPLAY_ELEMENT = "nodedisplay"; //$NON-NLS-1$
	public static final String ABSLAYOUT_ELEMENT = "abslayout"; //$NON-NLS-1$
	public static final String SPLITLAYOUT_ELEMENT = "splitlayout"; //$NON-NLS-1$
	public static final String TABLELAYOUT_ELEMENT = "tablelayout"; //$NON-NLS-1$
	public static final String COMPONENTLAYOUT_ELEMENT = "componentlayout"; //$NON-NLS-1$
	public static final String NODEDISPLAYLAYOUT_ELEMENT = "nodedisplaylayout"; //$NON-NLS-1$
	public static final String TITLE_PREFIX = "title_"; //$NON-NLS-1$
	public static final String MANDATORY = "mandatory"; //$NON-NLS-1$
	public static final String SYSTEM = "system"; //$NON-NLS-1$
	public static final String ERROR = "error"; //$NON-NLS-1$
	public static final String MOTD = "motd"; //$NON-NLS-1$
	public static final String OWNER = "owner"; //$NON-NLS-1$
	public static final String TABLECOLUMN_ALPHA = "alpha"; //$NON-NLS-1$
	public static final String TABLECOLUMN_NUMERIC = "numeric"; //$NON-NLS-1$
	public static final String TABLECOLUMN_DATE = "date"; //$NON-NLS-1$

	public static final String LOG_NO_TABLE_1 = "No table found for gid \""; //$NON-NLS-1$
	public static final String LOG_NO_TABLE_2 = "\"!"; //$NON-NLS-1$
}
