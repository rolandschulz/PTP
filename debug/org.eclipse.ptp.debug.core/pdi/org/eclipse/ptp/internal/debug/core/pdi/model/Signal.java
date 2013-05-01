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
package org.eclipse.ptp.internal.debug.core.pdi.model;

import org.eclipse.ptp.debug.core.TaskSet;
import org.eclipse.ptp.debug.core.pdi.IPDISession;
import org.eclipse.ptp.debug.core.pdi.PDIException;
import org.eclipse.ptp.debug.core.pdi.model.IPDISignal;
import org.eclipse.ptp.debug.core.pdi.model.IPDISignalDescriptor;
import org.eclipse.ptp.internal.debug.core.pdi.SessionObject;

/**
 * @author Clement chu
 */
public class Signal extends SessionObject implements IPDISignal {
	private IPDISignalDescriptor desc;

	public Signal(IPDISession session, TaskSet tasks, IPDISignalDescriptor desc) {
		super(session, tasks);
		this.desc = desc;
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.ptp.debug.core.pdi.model.IPDISignal#getDescription()
	 */
	public String getDescription() {
		return desc.getDescription();
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.ptp.debug.core.pdi.model.IPDISignal#getName()
	 */
	public String getName() {
		return desc.getName();
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.ptp.debug.core.pdi.model.IPDISignal#handle(boolean, boolean)
	 */
	public void handle(boolean isIgnore, boolean isStop) throws PDIException {
		session.getSignalManager().handle(this, isIgnore, isStop);
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.ptp.debug.core.pdi.model.IPDISignal#isIgnore()
	 */
	public boolean isIgnore() {
		return !isPass();
	}
	
	/**
	 * @return
	 */
	public boolean isPass() {
		return desc.getPass();
	}
	
	/**
	 * @return
	 */
	public boolean isPrint() {
		return desc.getPrint();
	}
	
	/**
	 * @return
	 */
	public boolean isStop() {
		return desc.getStop();
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.ptp.debug.core.pdi.model.IPDISignal#isStopSet()
	 */
	public boolean isStopSet() {
		return isStop();
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.ptp.debug.core.pdi.model.IPDISignal#setHandle(boolean, boolean)
	 */
	public void setHandle(boolean isIgnore, boolean isStop) {
		desc.setPass(!isIgnore);
		desc.setStop(isStop);
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.ptp.debug.core.pdi.model.IPDISignal#setDescriptor(org.eclipse.ptp.debug.core.pdi.model.IPDISignalDescriptor)
	 */
	public void setDescriptor(IPDISignalDescriptor desc) {
		this.desc = desc;
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.ptp.debug.core.pdi.model.IPDISignal#signal()
	 */
	public void signal() throws PDIException {
		session.resume(getTasks(), this);
	}
	
	/* (non-Javadoc)
	 * @see java.lang.Object#toString()
	 */
	public String toString() {
		return desc.toString(); 
	}
}
