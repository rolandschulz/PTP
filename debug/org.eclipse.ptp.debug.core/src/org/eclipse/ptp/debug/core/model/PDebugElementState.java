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

import org.eclipse.ptp.internal.debug.core.messages.Messages;

/**
 * Debug element states
 * 
 * @author clement
 * 
 */
public class PDebugElementState {
	public static final PDebugElementState DISCONNECTED = new PDebugElementState(Messages.PDebugElementState_4);
	public static final PDebugElementState DISCONNECTING = new PDebugElementState(Messages.PDebugElementState_3);
	public static final PDebugElementState EXITED = new PDebugElementState(Messages.PDebugElementState_12);
	public static final PDebugElementState RESTARTING = new PDebugElementState(Messages.PDebugElementState_11);
	public static final PDebugElementState RESUMED = new PDebugElementState(Messages.PDebugElementState_6);
	public static final PDebugElementState RESUMING = new PDebugElementState(Messages.PDebugElementState_5);
	public static final PDebugElementState STEPPED = new PDebugElementState(Messages.PDebugElementState_8);
	public static final PDebugElementState STEPPING = new PDebugElementState(Messages.PDebugElementState_7);
	public static final PDebugElementState SUSPENDED = new PDebugElementState(Messages.PDebugElementState_10);
	public static final PDebugElementState SUSPENDING = new PDebugElementState(Messages.PDebugElementState_9);
	public static final PDebugElementState TERMINATED = new PDebugElementState(Messages.PDebugElementState_2);
	public static final PDebugElementState TERMINATING = new PDebugElementState(Messages.PDebugElementState_1);
	public static final PDebugElementState UNDEFINED = new PDebugElementState(Messages.PDebugElementState_0);

	private final String fName;

	private PDebugElementState(String name) {
		fName = name;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {
		return fName;
	}
}
