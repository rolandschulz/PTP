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

package org.eclipse.ptp.rtsystem.proxy.event;

import org.eclipse.ptp.core.proxy.event.IProxyExtendedEvent;

public interface IProxyRuntimeEvent extends IProxyExtendedEvent {
	public static final int PROXY_RUNTIME_EVENT_OFFSET = 200;
	
	public static final int PROXY_RUNTIME_MESSAGE_EVENT = PROXY_RUNTIME_EVENT_OFFSET + 1;
	public static final int PROXY_RUNTIME_ATTR_DEF_EVENT = PROXY_RUNTIME_EVENT_OFFSET + 2;
	public static final int PROXY_RUNTIME_NEW_JOB_EVENT = PROXY_RUNTIME_EVENT_OFFSET + 3;
	public static final int PROXY_RUNTIME_NEW_MACHINE_EVENT = PROXY_RUNTIME_EVENT_OFFSET + 4;
	public static final int PROXY_RUNTIME_NEW_NODE_EVENT = PROXY_RUNTIME_EVENT_OFFSET + 5;
	public static final int PROXY_RUNTIME_NEW_PROCESS_EVENT = PROXY_RUNTIME_EVENT_OFFSET + 6;
	public static final int PROXY_RUNTIME_NEW_QUEUE_EVENT = PROXY_RUNTIME_EVENT_OFFSET + 7;
	public static final int PROXY_RUNTIME_JOB_CHANGE_EVENT = PROXY_RUNTIME_EVENT_OFFSET + 8;
	public static final int PROXY_RUNTIME_MACHINE_CHANGE_EVENT = PROXY_RUNTIME_EVENT_OFFSET + 9;
	public static final int PROXY_RUNTIME_NODE_CHANGE_EVENT = PROXY_RUNTIME_EVENT_OFFSET + 10;
	public static final int PROXY_RUNTIME_PROCESS_CHANGE_EVENT = PROXY_RUNTIME_EVENT_OFFSET + 11;
	public static final int PROXY_RUNTIME_QUEUE_CHANGE_EVENT = PROXY_RUNTIME_EVENT_OFFSET + 12;
	public static final int PROXY_RUNTIME_STARTUP_EVENT = PROXY_RUNTIME_EVENT_OFFSET + 13;
	public static final int PROXY_RUNTIME_RUNNING_EVENT = PROXY_RUNTIME_EVENT_OFFSET + 14;
	public static final int PROXY_RUNTIME_SHUTDOWN_EVENT = PROXY_RUNTIME_EVENT_OFFSET + 15;
	public static final int PROXY_RUNTIME_REMOVE_ALL_EVENT = PROXY_RUNTIME_EVENT_OFFSET + 16;
	public static final int PROXY_RUNTIME_REMOVE_JOB_EVENT = PROXY_RUNTIME_EVENT_OFFSET + 17;
	public static final int PROXY_RUNTIME_REMOVE_MACHINE_EVENT = PROXY_RUNTIME_EVENT_OFFSET + 18;
	public static final int PROXY_RUNTIME_REMOVE_NODE_EVENT = PROXY_RUNTIME_EVENT_OFFSET + 19;
	public static final int PROXY_RUNTIME_REMOVE_PROCESS_EVENT = PROXY_RUNTIME_EVENT_OFFSET + 20;
	public static final int PROXY_RUNTIME_REMOVE_QUEUE_EVENT = PROXY_RUNTIME_EVENT_OFFSET + 21;
	public static final int PROXY_RUNTIME_STARTUP_ERROR_EVENT = PROXY_RUNTIME_EVENT_OFFSET + 22;
	public static final int PROXY_RUNTIME_SUBMITJOB_ERROR_EVENT = PROXY_RUNTIME_EVENT_OFFSET + 23;
	public static final int PROXY_RUNTIME_TERMINATEJOB_ERROR_EVENT = PROXY_RUNTIME_EVENT_OFFSET + 24;
}
