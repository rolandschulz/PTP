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
package org.eclipse.ptp.internal.debug.ui.events;

/**
 * @author Clement chu
 * @deprecated
 */
public abstract class DebugActionEvent implements IDebugActionEvent {
	private Object source = null;
	private String job_id = ""; //$NON-NLS-1$
	private Object target = null;
	
	/** Constructor
	 * @param job_id
	 * @param source
	 * @param target
	 */
	public DebugActionEvent(String job_id, Object source, Object target) {
		this.job_id = job_id;
		this.source = source;
		this.target = target;
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.ptp.debug.ui.events.IDebugActionEvent#getJobId()
	 */
	public String getJobId() {
		return job_id;
	}
		
	/* (non-Javadoc)
	 * @see org.eclipse.ptp.debug.ui.events.IDebugActionEvent#getSource()
	 */
	public Object getSource() {
		return source;
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.ptp.debug.ui.events.IDebugActionEvent#getTarget()
	 */
	public Object getTarget() {
		return target;
	}
}
