/*******************************************************************************
 * Copyright (c) 2010 Dieter Krachtus and The University of Tennessee,
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Dieter Krachtus (dieter.krachtus@gmail.com) and Roland Schulz - initial API and implementation

 *******************************************************************************/
package org.eclipse.ptp.rm.pbs.jproxy.attributes;

import java.util.HashMap;
import java.util.Map;

import org.eclipse.ptp.rm.proxy.core.attributes.IElementAttributes;

/**
 * The Class defining the map that links the xml tag names to the attributes-ids
 * needs to be in the classpath of the jproxy. The proxy client defines the same
 * attribute-id constants. Obviously one shouldn't define those at two places.
 * This is only a temporary solution.
 */

/*
 * Should define attributes for all those in
 * org.eclipse.ptp.core.elements.attributes.JobAttributes
 * 
 * are all required? Or only some?
 * 
 * public enum State { STARTING, RUNNING, SUSPENDED, COMPLETED }
 * 
 * public static final int IO_FORWARDING_NONE = 0x0; public static final int
 * IO_FORWARDING_STDIN = 0x01; public static final int IO_FORWARDING_STDOUT =
 * 0x02; public static final int IO_FORWARDING_STDERR = 0x04;
 * 
 * private static final String DEBUG_ARGS_ATTR_ID = "debugArgs"; //$NON-NLS-1$
 * private static final String DEBUG_EXEC_NAME_ATTR_ID = "debugExecName";
 * //$NON-NLS-1$ private static final String DEBUG_EXEC_PATH_ATTR_ID =
 * "debugExecPath"; //$NON-NLS-1$ private static final String DEBUG_FLAG_ATTR_ID
 * = "debug"; //$NON-NLS-1$ private static final String
 * DEBUG_STOP_IN_MAIN_ATTR_ID = "debugStopInMain"; //$NON-NLS-1$ private static
 * final String DEBUGGER_ID_ATTR_ID = "debugerId"; //$NON-NLS-1$ private static
 * final String ENV_ATTR_ID = "env"; //$NON-NLS-1$ private static final String
 * EXEC_NAME_ATTR_ID = "execName"; //$NON-NLS-1$ private static final String
 * EXEC_PATH_ATTR_ID = "execPath"; //$NON-NLS-1$ private static final String
 * IO_FORWARDING_ATTR_ID = "ioForwarding"; //$NON-NLS-1$ private static final
 * String LAUNCHED_BY_PTP_FLAG_ATTR_ID = "launchedByPTP"; //$NON-NLS-1$ private
 * static final String JOB_ID_ATTR_ID = "jobId"; //$NON-NLS-1$ private static
 * final String NUM_PROCS_ATTR_ID = "jobNumProcs"; //$NON-NLS-1$ private static
 * final String PROG_ARGS_ATTR_ID = "progArgs"; //$NON-NLS-1$ private static
 * final String QUEUEID_ATTR_ID = "queueId"; //$NON-NLS-1$ private static final
 * String STATE_ATTR_ID = "jobState"; //$NON-NLS-1$ private static final String
 * STATUS_ATTR_ID = "jobStatus"; //$NON-NLS-1$ private static final String
 * STATUS_MESSAGE_ATTR_ID = "jobStatusMessage"; //$NON-NLS-1$ private static
 * final String SUBID_ATTR_ID = "jobSubId"; //$NON-NLS-1$ private static final
 * String USERID_ATTR_ID = "userId"; //$NON-NLS-1$ private static final String
 * WORKING_DIR_ATTR_ID = "workingDir"; //$NON-NLS-1$
 */

public class PBSJobClientAttributes extends PBSJobProtocolAttributes implements
		IElementAttributes {

	private static final Map<String, String> xmlTag_AttributeID_Map = new HashMap<String, String>();

	private static final String key = "job_id"; //$NON-NLS-1$
	private static final String parent_key = "queue"; //$NON-NLS-1$

	// TODO: if key, parentKey or filterKey are not in here - gives NullPointers
	// - should be checked
	// compare those three against the reqAttributes and give good error message
	// if there are not in
	static {
		xmlTag_AttributeID_Map.put("job_id", JOB_NAME_ATTR_ID);
		// xmlTag_AttributeID_Map.put("job_name" , JOB_NAME_ATTR_ID );
		xmlTag_AttributeID_Map.put("job_owner", "job_owner");
		// xmlTag_AttributeID_Map.put("job_state" , );
		xmlTag_AttributeID_Map.put("queue", "queue");
		// xmlTag_AttributeID_Map.put("server" , );
		// xmlTag_AttributeID_Map.put("account_name" , ACCOUNT_NAME_ATTR_ID );
		// xmlTag_AttributeID_Map.put("checkpoint" , CHECKPOINT_ATTR_ID );
		// xmlTag_AttributeID_Map.put("error_path" , ERROR_PATH_ATTR_ID );
		// xmlTag_AttributeID_Map.put("hold_types" , HOLD_TYPES_ATTR_ID );
		// xmlTag_AttributeID_Map.put("join_path" , JOIN_PATH_ATTR_ID );
		// xmlTag_AttributeID_Map.put("keep_files" , KEEP_FILES_ATTR_ID );
		// xmlTag_AttributeID_Map.put("mail_points" , MAIL_POINTS_ATTR_ID );
		// xmlTag_AttributeID_Map.put("output_path" , OUTPUT_PATH_ATTR_ID );
		xmlTag_AttributeID_Map.put("rerunable", RERUNNABLE_ATTR_ID);
		// xmlTag_AttributeID_Map.put("submit_args" , );

		xmlTag_AttributeID_Map.put("priority", PRIORITY_ATTR_ID);
		// xmlTag_AttributeID_Map.put("ctime" , );
		// xmlTag_AttributeID_Map.put("mtime" , );
		// xmlTag_AttributeID_Map.put("qtime" , );
		// xmlTag_AttributeID_Map.put("fault_tolerant" , );

		// xmlTag_AttributeID_Map.put("nodect" , RES_NODECT_ATTR_ID );
		// xmlTag_AttributeID_Map.put("nodes" , RES_NODES_ATTR_ID );
		// xmlTag_AttributeID_Map.put("size" , );
		xmlTag_AttributeID_Map.put("walltime", RES_WALLTIME_ATTR_ID);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.eclipse.ptp.rm.pbs.jproxy.pbs.attributes.IElementAttributes#getKey()
	 */
	public String getKey() {
		return key;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.eclipse.ptp.rm.pbs.jproxy.pbs.attributes.IElementAttributes#getParentKey
	 * ()
	 */
	public String getParentKey() {
		return parent_key;
	}

	public Map<String, DefaultValueMap<String, String>> getValueMap() {
		return null;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ptp.rm.pbs.jproxy.pbs.attributes.IElementAttributes#
	 * getXmltagAttributeID_Map()
	 */
	public Map<String, String> getXmltagAttributeID_Map() {
		return xmlTag_AttributeID_Map;
	}

}
