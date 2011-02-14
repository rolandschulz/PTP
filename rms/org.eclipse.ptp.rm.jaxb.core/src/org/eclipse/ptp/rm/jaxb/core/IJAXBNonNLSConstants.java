package org.eclipse.ptp.rm.jaxb.core;

/**
 * Gathers all internal, unmodifiable string constants into a single place for
 * convenience and code clarity.
 * 
 * @since 5.0
 */
public interface IJAXBNonNLSConstants {

	/* CHARACTERS */
	String ZEROSTR = "";//$NON-NLS-1$
	String TAB = "\t"; //$NON-NLS-1$
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
	String AMP = "@";//$NON-NLS-1$
	String DOL = "$";//$NON-NLS-1$
	String OPENV = "${";//$NON-NLS-1$
	String CLOSV = "}";//$NON-NLS-1$
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
	String YES = "yes";//$NON-NLS-1$
	String NO = "no";//$NON-NLS-1$

	String XMLSchema = "http://www.w3.org/2001/XMLSchema"; //$NON-NLS-1$
	String DATA = "data/"; //$NON-NLS-1$
	String RM_XSD = DATA + "resource_manager_type.xsd";//$NON-NLS-1$
	String JAXB = "JAXB";//$NON-NLS-1$
	String JAXB_CONTEXT = "org.eclipse.ptp.rm.jaxb.core.data";//$NON-NLS-1$
	String RM_CONFIG_PROPS = "rm_configurations.properties";//$NON-NLS-1$
	String RM_XSD_PATH = "rm_schema_path";//$NON-NLS-1$
	String SELECTED_ATTRIBUTES = "selected_attributes";//$NON-NLS-1$
	String VALID_ATTRIBUTES = "valid_attributes"; //$NON-NLS-1$

	String ATTR_ID = "[id]";//$NON-NLS-1$
	String ATTR_TYPE = "[type]";//$NON-NLS-1$
	String ATTR_DEFAULT = "[default]";//$NON-NLS-1$
	String ATTR_BASIC = "[basic]";//$NON-NLS-1$
	String ATTR_MAX = "[max]";//$NON-NLS-1$
	String ATTR_MIN = "[min]";//$NON-NLS-1$
	String ATTR_CHOICE = "[choice]";//$NON-NLS-1$
	String ATTR_TOOLTIP = "[tooltip]";//$NON-NLS-1$
	String ATTR_DESCRIPTION = "[description]";//$NON-NLS-1$
	String ATTR_VALIDATOR = "[validator]";//$NON-NLS-1$
	String ATTR_VALUE = "[value]";//$NON-NLS-1$

	String LOCALHOST = "localhost";//$NON-NLS-1$
	String TAG_CONTROL_PATH = "controlPath"; //$NON-NLS-1$
	String TAG_MONITOR_PATH = "monitorPath"; //$NON-NLS-1$
	String TAG_OPTIONS = "options"; //$NON-NLS-1$
	String TAG_CONTROL_INVOCATION_OPTIONS = "controlInvocationOptions"; //$NON-NLS-1$
	String TAG_MONITOR_INVOCATION_OPTIONS = "monitorInvocationOptions"; //$NON-NLS-1$
	String TAG_LOCAL_ADDRESS = "localAddress"; //$NON-NLS-1$
}
