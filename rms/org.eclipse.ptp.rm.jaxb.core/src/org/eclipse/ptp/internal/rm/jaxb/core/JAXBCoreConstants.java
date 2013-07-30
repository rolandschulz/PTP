/*******************************************************************************
 * Copyright (c) 2011 University of Illinois All rights reserved. This program
 * and the accompanying materials are made available under the terms of the
 * Eclipse Public License v1.0 which accompanies this distribution, and is
 * available at http://www.eclipse.org/legal/epl-v10.html 
 * 	
 * Contributors: 
 * 	Albert L. Rossi - design and implementation
 ******************************************************************************/
package org.eclipse.ptp.internal.rm.jaxb.core;

/**
 * Gathers all internal, unmodifiable string constants into a single place for convenience and in the interest of uncluttered code.
 * 
 * @since 1.2
 */
public class JAXBCoreConstants {
	public static final int UNDEFINED = -1;

	/* CHARACTERS */
	public static final String ZEROSTR = "";//$NON-NLS-1$
	public static final String SP = " ";//$NON-NLS-1$
	public static final String REGPIP = "[|]";//$NON-NLS-1$
	public static final String LINE_SEP = System.getProperty("line.separator"); //$NON-NLS-1$
	public static final String REMOTE_LINE_SEP = "\n"; //$NON-NLS-1$
	public static final String REMOTE_PATH_SEP = "/"; //$NON-NLS-1$
	public static final String PATH_SEP = System.getProperty("file.separator"); //$NON-NLS-1$
	public static final String LEN = "N";//$NON-NLS-1$
	public static final String TAB = "\t"; //$NON-NLS-1$
	public static final String EQ = "=";//$NON-NLS-1$
	public static final String QT = "\"";//$NON-NLS-1$
	public static final String QM = "?";//$NON-NLS-1$
	public static final String PD = "#";//$NON-NLS-1$
	public static final String PDRX = "[#]";//$NON-NLS-1$
	public static final String CM = ",";//$NON-NLS-1$
	public static final String CO = ":";//$NON-NLS-1$
	public static final String SC = ";";//$NON-NLS-1$
	public static final String LT = "<"; //$NON-NLS-1$
	public static final String LTS = "</";//$NON-NLS-1$
	public static final String GT = ">";//$NON-NLS-1$
	public static final String GTLT = "><";//$NON-NLS-1$
	public static final String HYPH = "-";//$NON-NLS-1$
	public static final String AT = "@";//$NON-NLS-1$
	public static final String DOL = "$";//$NON-NLS-1$
	public static final String PIP = "|";//$NON-NLS-1$
	public static final String DOT = ".";//$NON-NLS-1$
	public static final String Z3 = "000";//$NON-NLS-1$
	public static final String OPENP = "(";//$NON-NLS-1$
	public static final String OPENSQ = "[";//$NON-NLS-1$
	public static final String OPENV = "${";//$NON-NLS-1$
	public static final String OPENVRM = "${ptp_rm:";//$NON-NLS-1$
	public static final String VRM = "ptp_rm:";//$NON-NLS-1$
	public static final String VLC = "ptp_lc:";//$NON-NLS-1$
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
	public static final String LN = "\n";//$NON-NLS-1$
	public static final String RT = "\r";//$NON-NLS-1$

	/* JAXB */
	public static final String XMLSchema = "http://www.w3.org/2001/XMLSchema"; //$NON-NLS-1$
	public static final String SCHEMA = "schema/"; //$NON-NLS-1$
	public static final String RM_XSD = SCHEMA + "resource_manager_type.xsd";//$NON-NLS-1$
	public static final String JAXB = "JAXB";//$NON-NLS-1$
	public static final String JAXB_CONTEXT = "org.eclipse.ptp.rm.jaxb.core.data";//$NON-NLS-1$
	public static final String RM_XML = "rm_config_xml";//$NON-NLS-1$
	public static final String RM_URL = "rm_config_url";//$NON-NLS-1$
	public static final String TARGET_CONFIGURATIONS = "targetConfigurations";//$NON-NLS-1$
	public static final String DOT_XML = ".xml";//$NON-NLS-1$
	public static final String CONFIGURATION_FILE_ATTRIBUTE = "configurationFile"; //$NON-NLS-1$
	public static final String RM_CONFIG_EXTENSION_POINT = "org.eclipse.ptp.rm.jaxb.core.JAXBResourceManagerConfigurations"; //$NON-NLS-1$
	public static final String IMPORTED_JAXB_CONFIG = "org.eclipse.ptp.rm.jaxb.ImportedConfigurations"; //$NON-NLS-1$
	public static final String PTP_PACKAGE = "org.eclipse.ptp";//$NON-NLS-1$

	/* KEY WORDS */
	public static final String ID = "id";//$NON-NLS-1$
	public static final String TRUE = "true";//$NON-NLS-1$
	public static final String FALSE = "false";//$NON-NLS-1$
	public static final String YES = "yes";//$NON-NLS-1$
	public static final String NO = "no";//$NON-NLS-1$
	public static final String NOT = "not";//$NON-NLS-1$
	public static final String OR = "or";//$NON-NLS-1$
	public static final String AND = "and";//$NON-NLS-1$
	public static final String xEQ = "EQ";//$NON-NLS-1$
	public static final String xLT = "LT";//$NON-NLS-1$
	public static final String xGT = "GT";//$NON-NLS-1$
	public static final String xLE = "LE";//$NON-NLS-1$
	public static final String xGE = "GE";//$NON-NLS-1$
	public static final String GET = "get";//$NON-NLS-1$
	public static final String SET = "set";//$NON-NLS-1$
	public static final String IS = "is";//$NON-NLS-1$
	public static final String CLASS = "class";//$NON-NLS-1$
	public static final String STRING = "string";//$NON-NLS-1$
	public static final String NAME = "name";//$NON-NLS-1$
	public static final String VALUE = "value";//$NON-NLS-1$
	public static final String sDEFAULT = "default";//$NON-NLS-1$
	public static final String LOCAL = "local";//$NON-NLS-1$
	public static final String VISIBLE = "visible_";//$NON-NLS-1$
	public static final String ENABLED = "enabled_";//$NON-NLS-1$
	public static final String VALID = "valid_";//$NON-NLS-1$
	public static final String INVALID = "invalid_";//$NON-NLS-1$
	public static final String TEMP = "temp";//$NON-NLS-1$
	public static final String ACTIVE = "active";//$NON-NLS-1$
	public static final String INITIALIZED = "initialized";//$NON-NLS-1$

	/* TYPE MATCHING */
	public static final String NT = "nt";//$NON-NLS-1$
	public static final String BOOL = "bool";//$NON-NLS-1$

	/* STANDARD PROPERTIES */
	public static final String JAVA_USER_HOME = "user.home";//$NON-NLS-1$
	public static final String JAVA_TMP_DIR = "java.io.tmpdir";//$NON-NLS-1$
	public static final String FILE_SCHEME = "file";//$NON-NLS-1$
	public static final String STDOUT_REMOTE_FILE = "stdout_remote_path";//$NON-NLS-1$
	public static final String STDERR_REMOTE_FILE = "stderr_remote_path";//$NON-NLS-1$
	public static final String SCRIPT_PATH = "script_path";//$NON-NLS-1$
	public static final String SCRIPT = "script";//$NON-NLS-1$
	public static final String SCRIPT_FILE = "managed_file_for_script";//$NON-NLS-1$
	public static final String CURRENT_CONTROLLER = "current_controller";//$NON-NLS-1$

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
	 * Format
	 */
	public static final String DATE_FORMAT = "yyyy/MM/dd HH:mm:ss";//$NON-NLS-1$

}
