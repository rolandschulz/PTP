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
package org.eclipse.ptp.debug.core.model;

/**
 * @author Clement chu
 * 
 */
public class PDebugElementState {
	private final String fName;

	private PDebugElementState(String name) {
		this.fName = name;
	}
	public String toString() {
		return this.fName;
	}

	public static final PDebugElementState UNDEFINED = new PDebugElementState("undefined");
	public static final PDebugElementState TERMINATING = new PDebugElementState("terminating");
	public static final PDebugElementState TERMINATED = new PDebugElementState("terminated");
	public static final PDebugElementState DISCONNECTING = new PDebugElementState("disconnecting");
	public static final PDebugElementState DISCONNECTED = new PDebugElementState("disconnected");
	public static final PDebugElementState RESUMING = new PDebugElementState("resuming");
	public static final PDebugElementState RESUMED = new PDebugElementState("resumed");
	public static final PDebugElementState STEPPING = new PDebugElementState("stepping");
	public static final PDebugElementState STEPPED = new PDebugElementState("stepped");
	public static final PDebugElementState SUSPENDING = new PDebugElementState("suspending");
	public static final PDebugElementState SUSPENDED = new PDebugElementState("suspended");
	public static final PDebugElementState RESTARTING = new PDebugElementState("restarting");
	public static final PDebugElementState EXITED = new PDebugElementState("exited");
}
