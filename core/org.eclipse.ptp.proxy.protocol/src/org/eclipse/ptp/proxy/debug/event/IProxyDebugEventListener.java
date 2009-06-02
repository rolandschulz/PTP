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

package org.eclipse.ptp.proxy.debug.event;

public interface IProxyDebugEventListener {
	public void handleProxyDebugArgsEvent(IProxyDebugArgsEvent e);
	public void handleProxyDebugBreakpointHitEvent(IProxyDebugBreakpointHitEvent e);
	public void handleProxyDebugBreakpointSetEvent(IProxyDebugBreakpointSetEvent e);
	public void handleProxyDebugDataEvent(IProxyDebugDataEvent e);
	public void handleProxyDebugExitEvent(IProxyDebugExitEvent e);
	public void handleProxyDebugErrorEvent(IProxyDebugErrorEvent e);
	public void handleProxyDebugInfoThreadsEvent(IProxyDebugInfoThreadsEvent e);
	public void handleProxyDebugInitEvent(IProxyDebugInitEvent e);
	public void handleProxyDebugMemoryInfoEvent(IProxyDebugMemoryInfoEvent e);
	public void handleProxyDebugOKEvent(IProxyDebugOKEvent e);
	public void handleProxyDebugSetThreadSelectEvent(IProxyDebugSetThreadSelectEvent e);
	public void handleProxyDebugSignalEvent(IProxyDebugSignalEvent e);
	public void handleProxyDebugSignalExitEvent(IProxyDebugSignalExitEvent e);
	public void handleProxyDebugSignalsEvent(IProxyDebugSignalsEvent e);
	public void handleProxyDebugStackframeEvent(IProxyDebugStackframeEvent e);
	public void handleProxyDebugStackInfoDepthEvent(IProxyDebugStackInfoDepthEvent e);
	public void handleProxyDebugStepEvent(IProxyDebugStepEvent e);
	public void handleProxyDebugSuspendEvent(IProxyDebugSuspendEvent e);
	public void handleProxyDebugTypeEvent(IProxyDebugTypeEvent e);
	public void handleProxyDebugVarsEvent(IProxyDebugVarsEvent e);
}
