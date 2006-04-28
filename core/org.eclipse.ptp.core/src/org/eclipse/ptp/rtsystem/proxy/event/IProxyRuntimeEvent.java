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

import org.eclipse.ptp.core.proxy.event.IProxyEvent;
import org.eclipse.ptp.core.util.BitList;

public interface IProxyRuntimeEvent extends IProxyEvent {
	public static final int RUNTIME_EVENT_OFFSET = 200;
	public static final int EVENT_RUNTIME_OK = RUNTIME_EVENT_OFFSET + 0;
	public static final int EVENT_RUNTIME_ERROR = RUNTIME_EVENT_OFFSET + 1;
	public static final int EVENT_RUNTIME_JOBSTATE = RUNTIME_EVENT_OFFSET + 2;
	public static final int EVENT_RUNTIME_JOBS = RUNTIME_EVENT_OFFSET + 3;
	public static final int EVENT_RUNTIME_PROCS = RUNTIME_EVENT_OFFSET + 4;
	public static final int EVENT_RUNTIME_PROCATTR = RUNTIME_EVENT_OFFSET + 5;
	public static final int EVENT_RUNTIME_MACHINES = RUNTIME_EVENT_OFFSET + 6;
	public static final int EVENT_RUNTIME_NODES = RUNTIME_EVENT_OFFSET + 7;
	public static final int EVENT_RUNTIME_NODEATTR = RUNTIME_EVENT_OFFSET + 8;
	public static final int EVENT_RUNTIME_MACHID = RUNTIME_EVENT_OFFSET + 9;
	public static final int EVENT_RUNTIME_CONNECTED = RUNTIME_EVENT_OFFSET + 10;
	public static final int EVENT_RUNTIME_DISCONNECTED = RUNTIME_EVENT_OFFSET + 11;
	public static final int EVENT_RUNTIME_NEWJOB = RUNTIME_EVENT_OFFSET + 12;
	public static final int EVENT_RUNTIME_PROCOUT = RUNTIME_EVENT_OFFSET + 13;
	public static final int EVENT_RUNTIME_NODECHANGE = RUNTIME_EVENT_OFFSET + 14;
	
	
	public BitList getBitSet();
}
