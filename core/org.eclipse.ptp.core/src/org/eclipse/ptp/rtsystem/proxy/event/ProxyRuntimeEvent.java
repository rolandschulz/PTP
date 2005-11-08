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
import org.eclipse.ptp.core.proxy.event.ProxyErrorEvent;
import org.eclipse.ptp.core.proxy.event.ProxyEvent;

public class ProxyRuntimeEvent extends ProxyEvent {
	public static IProxyEvent toEvent(String str) {
		IProxyRuntimeEvent evt = null;
		String[] args = str.split(" ");
		
		int type = Integer.parseInt(args[0]);
		
		switch (type) {
		case IProxyRuntimeEvent.EVENT_RUNTIME_OK:
			evt = new ProxyRuntimeOKEvent();
			break;
			
		case IProxyRuntimeEvent.EVENT_RUNTIME_ERROR:
			int errCode = Integer.parseInt(args[1]);
			evt = new ProxyRuntimeErrorEvent(errCode, decodeString(args[2]));
			break;

		case IProxyRuntimeEvent.EVENT_RUNTIME_JOBSTATE:
			int jobid = Integer.parseInt(args[1]);
			int state = Integer.parseInt(args[2]);
			evt = new ProxyRuntimeJobStateEvent(jobid, state);
			break;

		case IProxyRuntimeEvent.EVENT_RUNTIME_NEWJOB:
			int njobid = Integer.parseInt(args[1]);
			evt = new ProxyRuntimeNewJobEvent(njobid);
			break;
		
		case IProxyRuntimeEvent.EVENT_RUNTIME_PROCS:
			int nprocs = Integer.parseInt(args[1]);
			evt = new ProxyRuntimeProcessesEvent(nprocs);
			break;
		
		case IProxyRuntimeEvent.EVENT_RUNTIME_PROCATTR:
			evt = new ProxyRuntimeProcessAttributeEvent(args);
			break;

		default:
			evt = new ProxyRuntimeErrorEvent(ProxyErrorEvent.EVENT_ERR_EVENT, "Invalid event type");
			break;
		}
		
		return evt;
	}
}