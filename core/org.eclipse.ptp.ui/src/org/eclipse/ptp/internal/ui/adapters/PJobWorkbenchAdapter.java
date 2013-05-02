/*******************************************************************************
Copyright (c) 2007 The Regents of the University of California. 
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
package org.eclipse.ptp.internal.ui.adapters;

import org.eclipse.debug.core.ILaunchManager;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.ptp.core.jobs.IJobStatus;
import org.eclipse.ptp.internal.ui.ImageImageDescriptor;
import org.eclipse.ptp.internal.ui.ParallelImages;
import org.eclipse.ui.model.WorkbenchAdapter;

public class PJobWorkbenchAdapter extends WorkbenchAdapter {
	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ui.model.WorkbenchAdapter#getImageDescriptor(java.lang.Object )
	 */
	@Override
	public ImageDescriptor getImageDescriptor(Object object) {
		IJobStatus job = (IJobStatus) object;
		int state = 0;
		if (job.getState() == IJobStatus.SUBMITTED) {
			state = 0;
		} else if (job.getState() == IJobStatus.RUNNING) {
			state = 1;
		} else if (job.getState() == IJobStatus.SUSPENDED) {
			state = 2;
		} else if (job.getState() == IJobStatus.COMPLETED) {
			state = 3;
		}
		return new ImageImageDescriptor(ParallelImages.jobImages[state][job.getLaunchMode().equals(ILaunchManager.DEBUG_MODE) ? 1
				: 0]);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ui.model.WorkbenchAdapter#getLabel(java.lang.Object)
	 */
	@Override
	public String getLabel(Object object) {
		IJobStatus job = (IJobStatus) object;
		return job.getJobId();
	}
}
