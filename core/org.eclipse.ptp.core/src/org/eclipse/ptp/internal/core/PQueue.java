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
package org.eclipse.ptp.internal.core;

import org.eclipse.ptp.core.attributes.IAttribute;
import org.eclipse.ptp.core.elementcontrols.IPJobControl;
import org.eclipse.ptp.core.elementcontrols.IPQueueControl;
import org.eclipse.ptp.core.elementcontrols.IResourceManagerControl;
import org.eclipse.ptp.core.elements.IPJob;
import org.eclipse.ptp.rmsystem.IResourceManager;

public class PQueue extends Parent implements IPQueueControl {
	protected String NAME_TAG = "queue ";
	protected String arch = "undefined";

	public PQueue(int id, IResourceManagerControl rm, IAttribute[] attrs) {
		super(id, rm, P_QUEUE, attrs);
	}

	/* (non-Javadoc)
	 * @see org.eclipse.ptp.core.IPMachine#getResourceManager()
	 */
	public IResourceManager getResourceManager() {
		return (IResourceManager) getParent();
	}
	
	public void addJob(IPJobControl job) {
		// TODO Auto-generated method stub
		
	}

	public IPJobControl findJobById(String job_id) {
		// TODO Auto-generated method stub
		return null;
	}

	public IPJobControl getJobControl(int jobId) {
		// TODO Auto-generated method stub
		return null;
	}

	public IPJobControl[] getJobControls() {
		// TODO Auto-generated method stub
		return null;
	}

	public void removeJob(IPJobControl job) {
		// TODO Auto-generated method stub
		
	}

	public IPJob getJob(int job_id) {
		// TODO Auto-generated method stub
		return null;
	}

	public IPJob[] getJobs() {
		// TODO Auto-generated method stub
		return null;
	}
}
