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

import java.lang.reflect.InvocationTargetException;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.operation.IRunnableWithProgress;
import org.eclipse.ptp.core.elements.IPResourceManager;
import org.eclipse.ptp.core.elements.attributes.ResourceManagerAttributes;
import org.eclipse.ptp.rmsystem.IResourceManagerMenuContribution;
import org.eclipse.ptp.ui.UIUtils;
import org.eclipse.ptp.ui.messages.Messages;
import org.eclipse.ui.PlatformUI;

public class StartResourceManagersObjectActionDelegate extends AbstractResourceManagerSelectionActionDelegate {

	public void run(IAction action) {

		for (IResourceManagerMenuContribution menuContrib : getMenuContribs()) {
			final IPResourceManager rmManager = (IPResourceManager) menuContrib.getAdapter(IPResourceManager.class);

			if (!isEnabledFor(rmManager)) {
				continue;
			}
			IRunnableWithProgress runnable = new IRunnableWithProgress() {
				public void run(IProgressMonitor monitor) throws InvocationTargetException, InterruptedException {
					try {
						rmManager.getResourceManager().start(monitor);
					} catch (CoreException e) {
						throw new InvocationTargetException(e);
					}
					if (monitor.isCanceled()) {
						throw new InterruptedException();
					}
				}
			};
			try {
				PlatformUI.getWorkbench().getProgressService().run(true, true, runnable);
			} catch (InvocationTargetException e) {
				Throwable t = e.getCause();
				IStatus status = null;
				if (t != null && t instanceof CoreException) {
					status = ((CoreException) t).getStatus();
				}
				UIUtils.showErrorDialog(Messages.StartResourceManagersObjectActionDelegate_0,
						Messages.StartResourceManagersObjectActionDelegate_1, status);
			} catch (InterruptedException e) {
				// Do nothing. Operation has been canceled.
			}
		}
	}

	/**
	 * @since 5.0
	 */
	@Override
	protected boolean isEnabledFor(IPResourceManager rmManager) {
		if (rmManager.getState() == ResourceManagerAttributes.State.STOPPED) {
			return true;
		}
		return false;
	}
}
