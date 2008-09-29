/******************************************************************************
 * Copyright (c) 2007 IBM Corporation.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - Initial Implementation
 *

*****************************************************************************/
package org.eclipse.ptp.cell.environment.launcher.pdt.internal;

/**
 * Definition of key names (and their default values) used by the pdt launcher
 * 
 * @author Richard Maciel
 *
 */
public interface IPdtLaunchAttributes {
	public static final String ATTR_PREFIX = IPdtLaunchAttributes.class.getCanonicalName() + "."; //$NON-NLS-1$
	
	
	public static final String ATTR_TRACE_LIB_PATH = ATTR_PREFIX + "trace_lib_path";//$NON-NLS-1$
	public static final String ATTR_PDT_MODULE_PATH = ATTR_PREFIX + "pdt_module_path";//$NON-NLS-1$
	
	public static final String ATTR_LOCAL_XML_FILE = ATTR_PREFIX + "local_xml_path";//$NON-NLS-1$
	public static final String ATTR_REMOTE_XML_DIR = ATTR_PREFIX + "remote_xml_dir_path";//$NON-NLS-1$
	public static final String ATTR_COPY_XML_FILE = ATTR_PREFIX + "copy_xml_file";//$NON-NLS-1$
	public static final String ATTR_REMOTE_XML_FILE = ATTR_PREFIX + "remote_xml_file";//$NON-NLS-1$
	
	public static final String ATTR_REMOTE_TRACE_DIR = ATTR_PREFIX + "trace_path";//$NON-NLS-1$
	public static final String ATTR_TRACE_FILE_PREFIX = ATTR_PREFIX + "trace_file_prefix";//$NON-NLS-1$
	public static final String ATTR_LOCAL_TRACE_DIR = ATTR_PREFIX + "local_trace_dir";//$NON-NLS-1$
	
	// DEFAULT VALUES
	public static final String DEFAULT_TRACE_LIB_PATH = DefaultValues.IPdtLaunchAttributes_DEFAULT_TRACE_LIB_PATH;
	public static final String DEFAULT_PDT_MODULE_PATH = DefaultValues.IPdtLaunchAttributes_DEFAULT_PDT_MODULE_PATH;
	
	public static final String DEFAULT_LOCAL_XML_FILE = DefaultValues.IPdtLaunchAttributes_DEFAULT_LOCAL_XML_FILE;
	public static final String DEFAULT_REMOTE_XML_DIR = DefaultValues.IPdtLaunchAttributes_DEFAULT_REMOTE_XML_DIR;
	public static final Boolean DEFAULT_COPY_XML_FILE = Boolean.parseBoolean(DefaultValues.IPdtLaunchAttributes_DEFAULT_COPY_XML_FILE);
	public static final String DEFAULT_REMOTE_XML_FILE = DefaultValues.IPdtLaunchAttributes_DEFAULT_REMOTE_XML_FILE; 
	
	public static final String DEFAULT_REMOTE_TRACE_DIR = DefaultValues.IPdtLaunchAttributes_DEFAULT_REMOTE_TRACE_DIR;
	public static final String DEFAULT_TRACE_FILE_PREFIX = DefaultValues.IPdtLaunchAttributes_DEFAULT_TRACE_FILE_PREFIX;
	public static final String DEFAULT_LOCAL_TRACE_DIR = DefaultValues.IPdtLaunchAttributes_DEFAULT_LOCAL_TRACE_DIR;
}
