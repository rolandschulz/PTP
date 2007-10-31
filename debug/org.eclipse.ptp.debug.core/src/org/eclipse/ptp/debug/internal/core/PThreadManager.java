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
package org.eclipse.ptp.debug.internal.core;

import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.ptp.debug.core.IPSession;
import org.eclipse.ptp.debug.core.pdi.IPDISession;

/**
 * @author clement
 *
 */
public class PThreadManager implements IAdaptable {
	private PSession session;
	public PThreadManager(PSession session) {
		this.session = session;
	}
	public void initialize(IProgressMonitor monitor) {
		
	}
	public IPSession getSession() {
		return session;
	}
	public Object getAdapter(Class adapter) {
		if (adapter.equals(IPDISession.class))
			return getSession();
		if (adapter.equals(PThreadManager.class))
			return this;
		return null;
	}
	public void dispose(IProgressMonitor monitor) {
	}
}
