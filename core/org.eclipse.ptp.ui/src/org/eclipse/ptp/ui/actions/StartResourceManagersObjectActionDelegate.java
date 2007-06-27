/*******************************************************************************
 * Copyright (c) 2006 The Regents of the University of California. 
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
package org.eclipse.ptp.ui.actions;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.jface.action.IAction;
import org.eclipse.ptp.core.elements.IResourceManager;
import org.eclipse.ptp.core.elements.attributes.ResourceManagerAttributes;
import org.eclipse.ptp.rmsystem.IResourceManagerMenuContribution;

public class StartResourceManagersObjectActionDelegate
extends AbstractResourceManagerSelectionActionDelegate {

	public void run(IAction action) {
		
		for (IResourceManagerMenuContribution menuContrib : getMenuContribs()) {
			final IResourceManager rmManager = 
				(IResourceManager) menuContrib.getAdapter(IResourceManager.class);

			if (!isEnabledFor(rmManager)) {
				continue;
			}
			
			new Job("Starting Resource Manager"){

				protected IStatus run(IProgressMonitor monitor) {
					try {
						rmManager.startUp(monitor);
					} catch (CoreException e) {
						return e.getStatus();
					}
					if (monitor.isCanceled()) {
						return Status.CANCEL_STATUS;
					}
					return Status.OK_STATUS;
				}
			}.schedule();
		}
	}

	@Override
	protected boolean isEnabledFor(IResourceManager rmManager) {
		if (rmManager.getState() == ResourceManagerAttributes.State.STOPPED) {
			return true;		
		}
		return false;
	}
}
