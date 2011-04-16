/*******************************************************************************
 * Copyright (c) 2011 University of Illinois All rights reserved. This program
 * and the accompanying materials are made available under the terms of the
 * Eclipse Public License v1.0 which accompanies this distribution, and is
 * available at http://www.eclipse.org/legal/epl-v10.html 
 * 	
 * Contributors: 
 * 	Albert L. Rossi - design and implementation
 ******************************************************************************/
package org.eclipse.ptp.rm.jaxb.core;

/**
 * Gathers all internal, unmodifiable string constants into a single place for
 * convenience and the interest of uncluttered code.
 * 
 * @since 5.0
 */
public interface IJAXBNonNLSConstants {

	int UNDEFINED = -1;
	int COPY_BUFFER_SIZE = 64 * 1024;
	int STREAM_BUFFER_SIZE = 8 * 1024;
	int EOF = -1;
	long MINUTE_IN_MS = 60 * 60 * 1000;
	long VALIDATE_TIMER = 500;
	long TAILF_PAUSE = 10 * 1000;

	/* CHARACTERS */
	String LEN = "N";//$NON-NLS-1$
	String ZEROSTR = "";//$NON-NLS-1$
	String TAB = "\t"; //$NON-NLS-1$
	String SP = " ";//$NON-NLS-1$
	String EQ = "=";//$NON-NLS-1$
	String QT = "\"";//$NON-NLS-1$
	String QM = "?";//$NON-NLS-1$
	String PD = "#";//$NON-NLS-1$
	String PDRX = "[#]";//$NON-NLS-1$
	String CM = ",";//$NON-NLS-1$
	String CO = ":";//$NON-NLS-1$
	String SC = ";";//$NON-NLS-1$
	String LT = "<"; //$NON-NLS-1$
	String LTS = "</";//$NON-NLS-1$
	String GT = ">";//$NON-NLS-1$
	String GTLT = "><";//$NON-NLS-1$
	String HYPH = "-";//$NON-NLS-1$
	String AT = "@";//$NON-NLS-1$
	String DOL = "$";//$NON-NLS-1$
	String PIP = "|";//$NON-NLS-1$
	String DOT = ".";//$NON-NLS-1$
	String Z3 = "000";//$NON-NLS-1$
	String OPENSQ = "[";//$NON-NLS-1$
	String OPENV = "${";//$NON-NLS-1$
	String OPENVRM = "${rm:";//$NON-NLS-1$
	String OPENVLT = "${lt:";//$NON-NLS-1$
	String VRM = "rm:";//$NON-NLS-1$
	String VLC = "lc:";//$NON-NLS-1$
	String CLOSSQ = "]";//$NON-NLS-1$
	String CLOSV = "}";//$NON-NLS-1$
	String CLOSVAL = "#value}";//$NON-NLS-1$
	String BKESC = "\\\\";//$NON-NLS-1$
	String BKBKESC = "\\\\\\\\";//$NON-NLS-1$
	String DLESC = "\\$";//$NON-NLS-1$
	String DLESCESC = "\\\\\\$";//$NON-NLS-1$
	String SPESC = "\\\\s";//$NON-NLS-1$
	String LNSEPESC = "\\\\n";//$NON-NLS-1$
	String TBESC = "\\t";//$NON-NLS-1$
	String TBESCESC = "\\\\t";//$NON-NLS-1$
	String LNESC = "\\n";//$NON-NLS-1$
	String RTESC = "\\r";//$NON-NLS-1$
	String LINE_SEP = System.getProperty("line.separator"); //$NON-NLS-1$
	String REMOTE_LINE_SEP = "\n"; //$NON-NLS-1$
	String REMOTE_PATH_SEP = "/"; //$NON-NLS-1$
	String PATH_SEP = System.getProperty("file.separator"); //$NON-NLS-1$

	/* KEY WORDS */
	String TRUE = "true";//$NON-NLS-1$
	String FALSE = "false";//$NON-NLS-1$
	String YES = "yes";//$NON-NLS-1$
	String NO = "no";//$NON-NLS-1$
	String NOT = "not";//$NON-NLS-1$
	String OR = "or";//$NON-NLS-1$
	String AND = "and";//$NON-NLS-1$
	String xEQ = "EQ";//$NON-NLS-1$
	String xLT = "LT";//$NON-NLS-1$
	String xGT = "GT";//$NON-NLS-1$
	String xLE = "LE";//$NON-NLS-1$
	String xGE = "GE";//$NON-NLS-1$
	String GET = "get";//$NON-NLS-1$
	String SET = "set";//$NON-NLS-1$
	String IS = "is";//$NON-NLS-1$
	String CLASS = "class";//$NON-NLS-1$
	String STRING = "string";//$NON-NLS-1$
	String NAME = "name";//$NON-NLS-1$
	String VALUE = "value";//$NON-NLS-1$
	String BASIC = "basic";//$NON-NLS-1$
	String CHOICE = "choice";//$NON-NLS-1$
	String sDEFAULT = "default";//$NON-NLS-1$
	String DESC = "description";//$NON-NLS-1$
	String MAX = "max";//$NON-NLS-1$
	String MIN = "min";//$NON-NLS-1$
	String READONLY = "readOnly";//$NON-NLS-1$
	String STATUS = "status";//$NON-NLS-1$
	String TOOLTIP = "tooltip";//$NON-NLS-1$
	String TYPE = "type";//$NON-NLS-1$
	String VALIDATOR = "validator";//$NON-NLS-1$
	String SELECTED = "visible";//$NON-NLS-1$
	String LOCAL = "local";//$NON-NLS-1$
	String JOB_ID_TAG = "@jobId";//$NON-NLS-1$
	String NAME_TAG = AT + NAME;
	String VALUE_TAG = AT + VALUE;

	/* TYPE MATCHING */
	String NT = "nt";//$NON-NLS-1$
	String BOOL = "bool";//$NON-NLS-1$
	String ET = "et";//$NON-NLS-1$
	String IST = "ist";//$NON-NLS-1$
	String ECTOR = "ector";//$NON-NLS-1$

	/* STANDARD PROPERTIES */
	String ID = "id";//$NON-NLS-1$
	String JAVA_USER_HOME = "user.home";//$NON-NLS-1$
	String JAVA_TMP_DIR = "java.io.tmpdir";//$NON-NLS-1$
	String FILE_SCHEME = "file";//$NON-NLS-1$
	String XMLSchema = "http://www.w3.org/2001/XMLSchema"; //$NON-NLS-1$
	String DATA = "data/"; //$NON-NLS-1$
	String RM_XSD = DATA + "resource_manager_type.xsd";//$NON-NLS-1$
	String JAXB = "JAXB";//$NON-NLS-1$
	String JAXB_CONTEXT = "org.eclipse.ptp.rm.jaxb.core.data";//$NON-NLS-1$
	String RM_CONFIG_PROPS = "rm_configurations.properties";//$NON-NLS-1$
	String RM_XSD_PATH = "rm_schema_path";//$NON-NLS-1$
	String RM_XSD_URL = "rm_schema_url";//$NON-NLS-1$
	String PREV_RM_XSD_PATH = "prev_rm_schema_path";//$NON-NLS-1$
	String EXTERNAL_RM_XSD_PATHS = "external_rm_schema_paths";//$NON-NLS-1$
	String CHECKED_ATTRIBUTES = "checked_attributes";//$NON-NLS-1$
	String SHOW_ONLY_CHECKED = "show_only_checked";//$NON-NLS-1$
	String IS_PRESET = "is_preset";//$NON-NLS-1$
	String SCRIPT_PATH = "script_path";//$NON-NLS-1$
	String SCRIPT = "script";//$NON-NLS-1$
	String SCRIPT_FILE = "managed_file_for_script";//$NON-NLS-1$
	String CSH = "csh";//$NON-NLS-1$
	String SH = ".sh";//$NON-NLS-1$
	String SETENV = "setenv";//$NON-NLS-1$
	String EXPORT = "export";//$NON-NLS-1$
	String STDOUT = "stdout";//$NON-NLS-1$
	String STDERR = "stderr";//$NON-NLS-1$
	String DATE_FORMAT = "yyyy/MM/dd HH:mm:ss";//$NON-NLS-1$

	String RESOURCE_MANAGERS = "resourceManagers";//$NON-NLS-1$
	String CUSTOM = "custom";//$NON-NLS-1$
	String CONTROL_USER_NAME = "controlUserName";//$NON-NLS-1$
	String MONITOR_USER_NAME = "monitorUserName";//$NON-NLS-1$
	String CONTROL_CONNECTION_NAME = "controlConnectionName";//$NON-NLS-1$
	String MONITOR_CONNECTION_NAME = "monitorConnectionName";//$NON-NLS-1$
	String LOCALHOST = "localhost";//$NON-NLS-1$
	String CONTROL_PATH = "controlPath"; //$NON-NLS-1$
	String MONITOR_PATH = "monitorPath"; //$NON-NLS-1$
	String CONTROL_OPTIONS = "controlOptions"; //$NON-NLS-1$
	String MONITOR_OPTIONS = "monitorOptions"; //$NON-NLS-1$
	String CONTROL_INVOCATION_OPTIONS = "controlInvocationOptions"; //$NON-NLS-1$
	String MONITOR_INVOCATION_OPTIONS = "monitorInvocationOptions"; //$NON-NLS-1$
	String CONTROL_ADDRESS = "controlAddress"; //$NON-NLS-1$
	String LOCAL_ADDRESS = "localAddress"; //$NON-NLS-1$
	String MONITOR_ADDRESS = "monitorAddress"; //$NON-NLS-1$
	String CONTROL_USER_VAR = "control.user.name";//$NON-NLS-1$
	String CONTROL_ADDRESS_VAR = "control.address";//$NON-NLS-1$
	String MONITOR_USER_VAR = "monitor.user.name";//$NON-NLS-1$
	String MONITOR_ADDRESS_VAR = "monitor.address";//$NON-NLS-1$
	String ARPA = ".in-addr.arpa";//$NON-NLS-1$
	String ECLIPSESETTINGS = ".eclipsesettings";//$NON-NLS-1$
	String DEBUG_PACKAGE = "org.eclipse.debug";//$NON-NLS-1$
	String PTP_PACKAGE = "org.eclipse.ptp";//$NON-NLS-1$

	String STARTUP = "OnStartUp";//$NON-NLS-1$
	String SHUTDOWN = "OnShutDown";//$NON-NLS-1$
	String DISCATTR = "DiscoverAttributes";//$NON-NLS-1$
	String JOBSTATUS = "GetJobStatus";//$NON-NLS-1$
	String SUBMIT_INTERACTIVE = "submit-interactive";//$NON-NLS-1$
	String SUBMIT_BATCH = "submit-batch";//$NON-NLS-1$
	String SUBMIT_DEBUG_INTERACTIVE = "submit-debug-interactive";//$NON-NLS-1$
	String SUBMIT_DEBUG_BATCH = "submit-debug-batch";//$NON-NLS-1$
	String VALIDATE = "ValidateJob";//$NON-NLS-1$

	String JOB_ATTRIBUTE = "jobAttribute";//$NON-NLS-1$
	String ATTRIBUTE = "attribute";//$NON-NLS-1$
	String PROPERTY = "property";//$NON-NLS-1$
	String QUEUES = "available_queues";//$NON-NLS-1$
	String JOB_ID = "jobId";//$NON-NLS-1$
	String EXEC_PATH = "executablePath";//$NON-NLS-1$
	String PROG_ARGS = "progArgs";//$NON-NLS-1$
	String DIRECTORY = "directory";//$NON-NLS-1$
	String MPI_CMD = "mpiCommand";//$NON-NLS-1$
	String MPI_ARGS = "mpiArgs";//$NON-NLS-1$

	String CASE_INSENSITIVE = "CASE_INSENSITIVE";//$NON-NLS-1$
	String MULTILINE = "MULTILINE";//$NON-NLS-1$
	String DOTALL = "DOTALL";//$NON-NLS-1$
	String UNICODE_CASE = "UNICODE_CASE";//$NON-NLS-1$
	String CANON_EQ = "CANON_EQ";//$NON-NLS-1$
	String LITERAL = "LITERAL";//$NON-NLS-1$
	String COMMENTS = "COMMENTS";//$NON-NLS-1$

	String DOT_XML = ".xml";//$NON-NLS-1$
	String TOKENIZER_EXT_PT = "streamParserTokenizer";//$NON-NLS-1$
	String TAIL = "tail";//$NON-NLS-1$
	String MINUS_F = "-f";//$NON-NLS-1$
	String CONFIGURATION_FILE_ATTRIBUTE = "configurationFile"; //$NON-NLS-1$
	String RM_CONFIG_EXTENSION_POINT = "org.eclipse.ptp.rm.jaxb.core.JAXBResourceManagerConfigurations"; //$NON-NLS-1$
	String JAXB_SERVICE_PROVIDER_EXTPT = "org.eclipse.ptp.rm.jaxb.JAXBServiceProvider"; //$NON-NLS-1$
	/*
	 * EFS Attributes
	 */
	String ATTRIBUTE_READ_ONLY = "ATTRIBUTE_READ_ONLY";//$NON-NLS-1$
	String ATTRIBUTE_IMMUTABLE = "ATTRIBUTE_IMMUTABLE";//$NON-NLS-1$
	String ATTRIBUTE_OWNER_READ = "ATTRIBUTE_OWNER_READ";//$NON-NLS-1$
	String ATTRIBUTE_OWNER_WRITE = "ATTRIBUTE_OWNER_WRITE";//$NON-NLS-1$
	String ATTRIBUTE_OWNER_EXECUTE = "ATTRIBUTE_OWNER_EXECUTE";//$NON-NLS-1$
	String ATTRIBUTE_GROUP_READ = "ATTRIBUTE_GROUP_READ";//$NON-NLS-1$
	String ATTRIBUTE_GROUP_WRITE = "ATTRIBUTE_GROUP_WRITE";//$NON-NLS-1$
	String ATTRIBUTE_GROUP_EXECUTE = "ATTRIBUTE_GROUP_EXECUTE";//$NON-NLS-1$
	String ATTRIBUTE_OTHER_READ = "ATTRIBUTE_OTHER_READ";//$NON-NLS-1$
	String ATTRIBUTE_OTHER_WRITE = "ATTRIBUTE_OTHER_WRITE";//$NON-NLS-1$
	String ATTRIBUTE_OTHER_EXECUTE = "ATTRIBUTE_OTHER_EXECUTE";//$NON-NLS-1$
	String ATTRIBUTE_EXECUTABLE = "ATTRIBUTE_EXECUTABLE";//$NON-NLS-1$
	String ATTRIBUTE_ARCHIVE = "ATTRIBUTE_ARCHIVE";//$NON-NLS-1$
	String ATTRIBUTE_HIDDEN = "ATTRIBUTE_HIDDEN";//$NON-NLS-1$
	String ATTRIBUTE_SYMLINK = "ATTRIBUTE_SYMLINK";//$NON-NLS-1$
	String ATTRIBUTE_LINK_TARGET = "ATTRIBUTE_LINK_TARGET";//$NON-NLS-1$
}
