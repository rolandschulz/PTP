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

package org.eclipse.ptp.proxy.runtime.event;

import org.eclipse.ptp.proxy.event.IProxyExtendedEvent;

public interface IProxyRuntimeEvent extends IProxyExtendedEvent {
	public static final int EVENT_OFFSET = 200;
	
	/*
	 * Internal state events
	 */
	public static final int STARTUP_STATE = EVENT_OFFSET + 1;
	public static final int CONNECTED_STATE = EVENT_OFFSET + 2;
	public static final int RUNNING_STATE = EVENT_OFFSET + 3;
	public static final int SHUTDOWN_STATE = EVENT_OFFSET + 4;
	public static final int ERROR_STATE = EVENT_OFFSET + 5;
	
	/*
	 * Internal error events
	 */
	public static final int STARTUP_ERROR = EVENT_OFFSET + 10;
	public static final int SUBMITJOB_ERROR = EVENT_OFFSET + 11;
	public static final int TERMINATEJOB_ERROR = EVENT_OFFSET + 12;

	/*
	 * New model element events
	 */
	public static final int NEW_JOB = EVENT_OFFSET + 20;
	public static final int NEW_MACHINE = EVENT_OFFSET + 21;
	public static final int NEW_NODE = EVENT_OFFSET + 22;
	public static final int NEW_PROCESS = EVENT_OFFSET + 23;
	public static final int NEW_QUEUE = EVENT_OFFSET + 24;

	/*
	 * Change model element events
	 */
	public static final int JOB_CHANGE = EVENT_OFFSET + 30;
	public static final int MACHINE_CHANGE = EVENT_OFFSET + 31;
	public static final int NODE_CHANGE = EVENT_OFFSET + 32;
	public static final int PROCESS_CHANGE = EVENT_OFFSET + 33;
	public static final int QUEUE_CHANGE = EVENT_OFFSET + 34;

	/*
	 * Remove model element events
	 */
	public static final int REMOVE_ALL = EVENT_OFFSET + 40;
	public static final int REMOVE_JOB = EVENT_OFFSET + 41;
	public static final int REMOVE_MACHINE = EVENT_OFFSET + 42;
	public static final int REMOVE_NODE = EVENT_OFFSET + 43;
	public static final int REMOVE_PROCESS = EVENT_OFFSET + 44;
	public static final int REMOVE_QUEUE = EVENT_OFFSET + 45;

	/*
	 * Miscellaneous events
	 */
	public static final int MESSAGE = EVENT_OFFSET + 50;
	public static final int ATTR_DEF = EVENT_OFFSET + 51;
}
