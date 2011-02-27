package org.eclipse.ptp.rm.jaxb.core;

/**
 * Gathers all internal, unmodifiable string constants into a single place for
 * convenience and code clarity.
 * 
 * @since 5.0
 */
public interface IJAXBNonNLSConstants {

	int UNDEFINED = -1;
	int COPY_BUFFER_SIZE = 64 * 1024;
	int STREAM_BUFFER_SIZE = 1024;
	int EOF = -1;

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
	String AMP = "@";//$NON-NLS-1$
	String DOL = "$";//$NON-NLS-1$
	String OPENV = "${";//$NON-NLS-1$
	String OPENVRM = "${rm:";//$NON-NLS-1$
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
	String LINE_SEP = System.getProperty("line.separator"); //$NON-NLS-1$
	String REMOTE_LINE_SEP = "\n"; //$NON-NLS-1$
	String REMOTE_PATH_SEP = "/"; //$NON-NLS-1$
	String PATH_SEP = System.getProperty("file.separator"); //$NON-NLS-1$

	/* KEY WORDS */
	String TRUE = "true";//$NON-NLS-1$
	String FALSE = "false";//$NON-NLS-1$
	String YES = "yes";//$NON-NLS-1$
	String NO = "no";//$NON-NLS-1$
	String GET = "get";//$NON-NLS-1$
	String SET = "set";//$NON-NLS-1$
	String VALUE = "value";//$NON-NLS-1$

	/* STANDARD PROPERTIES */
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
	String PREV_RM_XSD_PATH = "prev_rm_schema_path";//$NON-NLS-1$
	String EXTERNAL_RM_XSD_PATHS = "external_rm_schema_paths";//$NON-NLS-1$
	String SELECTED_ATTRIBUTES = "selected_attributes";//$NON-NLS-1$
	String VALID_ATTRIBUTES = "valid_attributes"; //$NON-NLS-1$
	String IS_PRESET = "is_preset";//$NON-NLS-1$
	String SCRIPT_PATH = "script_path";//$NON-NLS-1$
	String SCRIPT = "script";//$NON-NLS-1$
	String SH = ".sh";//$NON-NLS-1$

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

	String QUEUES = "available_queues";//$NON-NLS-1$
	String JOB_ID = "jobId";//$NON-NLS-1$
	String EXEC_PATH = "executablePath";//$NON-NLS-1$
	String PROG_ARGS = "progArgs";//$NON-NLS-1$
	String DIRECTORY = "directory";//$NON-NLS-1$
	String MPI_CMD = "mpiCommand";//$NON-NLS-1$
	String MPI_ARGS = "mpiArgs";//$NON-NLS-1$

}
