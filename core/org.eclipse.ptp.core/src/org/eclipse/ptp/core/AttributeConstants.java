/*******************************************************************************
 * Copyright (c) 2005 The Regents of the University of California. 
 * This material was produced under U.S. Government contract W-7405-ENG-36 
 * for Los Alamos National Laboratory, which is operated by the University 
 * of California for the U.S. Department of Energy. The U.S. Government has 
 * rights to use, reproduce, and distribute this software. NEITHER THE 
 * GOVERNMENT NOR THE UNIVERSITY MAKES ANY WARRANTY, EXPRESS OR IMPLIED, OR 
 * ASSUMES ANY LIABILITY FOR THE USE OF THIS SOFTWARE. If software is modified 
 * to produce derivative works, such modified software should be clearly marked, 
 * so as not to confuse it with the version available from LANL.
 * 
 * Additionally, this program and the accompanying materials 
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * LA-CC 04-115
 *******************************************************************************/
package org.eclipse.ptp.core;

public interface AttributeConstants {

	public static final String ATTRIB_MACHINEID = "ATTRIB_MACHINEID";
	
	public static final String ATTRIB_JOBID = "ATTRIB_JOBID";
	
	public static final String ATTRIB_PARENT = "ATTRIB_PARENT";
	public static final String ATTRIB_NAME = "ATTRIB_NAME";
	public static final String ATTRIB_TYPE = "ATTRIB_TYPE";
	public static final String ATTRIB_TASKID = "ATTRIB_TASKID";
	public static final String ATTRIB_ISREGISTERED = "ATTRIB_ISREGISTERED";
	
	/* node attributes */
	public static final String ATTRIB_NODE_NAME = "ATTRIB_NODE_NAME";
	public static final String ATTRIB_NODE_NUMBER = "ATTRIB_NODE_NUMBER";
	public static final String ATTRIB_NODE_STATE = "ATTRIB_NODE_STATE";
	public static final String ATTRIB_NODE_GROUP = "ATTRIB_NODE_GROUP";
	public static final String ATTRIB_NODE_USER = "ATTRIB_NODE_USER";
	public static final String ATTRIB_NODE_MODE = "ATTRIB_NODE_MODE";

	/* process attributes */
	public static final String ATTRIB_PROCESS_PID = "ATTRIB_PROCESS_PID";
	public static final String ATTRIB_PROCESS_EXIT_CODE = "ATTRIB_PROCESS_EXIT_CODE";
	public static final String ATTRIB_PROCESS_STATUS = "ATTRIB_PROCESS_STATUS";
	public static final String ATTRIB_PROCESS_SIGNAL = "ATTRIB_PROCESS_SIGNAL";
	public static final String ATTRIB_PROCESS_NODE_NAME = "ATTRIB_PROCESS_NODE_NAME";
}
