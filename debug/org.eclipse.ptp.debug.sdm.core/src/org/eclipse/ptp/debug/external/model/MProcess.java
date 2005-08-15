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
/*
 * Created on Feb 21, 2005
 *
 */
package org.eclipse.ptp.debug.external.model;

import org.eclipse.ptp.core.IPProcess;


/**
 * @author donny
 *
 */
public class MProcess {
	
	/* debugInfo holds an internal state of the debugger associated
	 * with this MProcess
	 * It is an Object so that it's generic and not tied to any
	 * particular debugger, (of course to be useful, it must casted
	 * accordingly).
	 */
	private Object debugInfo;
	private IPProcess pproc;
	
	int id;
	String name = "";

	public IPProcess getPProcess() {
		return pproc;
	}
	
	public void setPProcess(IPProcess p) {
		pproc = p;
	}
	
	public Object getDebugInfo() {
		return debugInfo;
	}
	
	public void setDebugInfo(Object info) {
		debugInfo = info;
	}
	
	public MProcess(int pId) {
		id = pId;
		name = "process" + Integer.toString(id);
	}
	
	public int getId() {
		return id;
	}

	public String getName() {
		return name;
	}
}
