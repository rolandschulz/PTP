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
package org.eclipse.ptp.debug.core;

import java.util.ArrayList;
import java.util.List;

/**
 * @author clement
 *
 */
public class DebugManager {
	private static DebugManager instance = null;
	protected List processes = new ArrayList();
	
	public DebugManager() {
		int total = 11500;
		for (int i=0; i<total; i++) {
			PProcess p = new PProcess(i);
			p.setStatus(randomStatus());
			processes.add(p);
		}
	}
	
	public PProcess getProcess(int index) {
		return (PProcess)processes.get(index);
	}
	
	public PProcess[] getProcesses() {
		return (PProcess[])processes.toArray(new PProcess[processes.size()]);
	}
	
	private int randomStatus() {
		return (int)Math.round(Math.random() * 4);
	}
	
	public static DebugManager getInstance() {
		if (instance == null)
			instance = new DebugManager();
		return instance;
	}	
}
