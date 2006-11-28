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

package org.eclipse.ptp.debug.external.core.proxy.event;

import org.eclipse.ptp.core.proxy.event.IProxyEvent;
import org.eclipse.ptp.core.util.BitList;


public interface IProxyDebugEvent extends IProxyEvent {
	public static final int DBG_EVENT_OFFSET = 100;
	public static final int EVENT_DBG_EXIT = DBG_EVENT_OFFSET + 2;
	public static final int 	EVENT_DBG_EXIT_NORMAL = 0;
	public static final int 	EVENT_DBG_EXIT_SIGNAL = 1;
	public static final int EVENT_DBG_BPSET = DBG_EVENT_OFFSET + 4;
	public static final int EVENT_DBG_FRAMES = DBG_EVENT_OFFSET + 5;
	public static final int EVENT_DBG_DATA = DBG_EVENT_OFFSET + 6;
	public static final int EVENT_DBG_TYPE = DBG_EVENT_OFFSET + 7;
	public static final int EVENT_DBG_VARS = DBG_EVENT_OFFSET + 8;
	public static final int EVENT_DBG_ARGS = DBG_EVENT_OFFSET + 9;
	public static final int EVENT_DBG_INIT = DBG_EVENT_OFFSET + 10;
	public static final int EVENT_DBG_OK = DBG_EVENT_OFFSET + 11;
	public static final int EVENT_DBG_ERROR = DBG_EVENT_OFFSET + 12;
	public static final int EVENT_DBG_SUSPEND = DBG_EVENT_OFFSET + 13;
	public static final int 	EVENT_DBG_SUSPEND_BPHIT = 0;
	public static final int 	EVENT_DBG_SUSPEND_SIGNAL = 1;
	public static final int 	EVENT_DBG_SUSPEND_STEP = 2;
	public static final int 	EVENT_DBG_SUSPEND_INT = 3;
	public static final int EVENT_DBG_THREADS	= DBG_EVENT_OFFSET + 14;
	public static final int EVENT_DBG_THREAD_SELECT = DBG_EVENT_OFFSET + 15;
	public static final int EVENT_DBG_STACK_INFO_DEPTH = DBG_EVENT_OFFSET + 16;
	public static final int EVENT_DBG_DATA_READ_MEMORY = DBG_EVENT_OFFSET + 17;
	public static final int EVENT_DBG_DATA_WRITE_MEMORY = DBG_EVENT_OFFSET + 18;
	public static final int EVENT_DBG_SIGNALS = DBG_EVENT_OFFSET + 19;

	public static final int EVENT_DBG_DATA_EVA_EX = DBG_EVENT_OFFSET + 20;
	public static final int EVENT_DBG_PARTIAL_AIF = DBG_EVENT_OFFSET + 21;

	public BitList getBitSet();
}
