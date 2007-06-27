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
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.dialogs.ErrorDialog;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.ptp.core.elements.IResourceManager;
import org.eclipse.ptp.core.elements.attributes.ResourceManagerAttributes;
import org.eclipse.ptp.rmsystem.IResourceManagerMenuContribution;
import org.eclipse.ptp.ui.PTPUIPlugin;
import org.eclipse.ptp.ui.UIMessage;

public class StopResourceManagersObjectActionDelegate
extends AbstractResourceManagerSelectionActionDelegate {

	public void run(IAction action) {
		
		for (IResourceManagerMenuContribution menuContrib : getMenuContribs()) {
			IResourceManager rmManager = (IResourceManager) menuContrib.getAdapter(IResourceManager.class);

			if (!isEnabledFor(rmManager)) {
				continue;
			}
			
			/*
			 * Only ask if we are really shutting down the RM
			 */
			ResourceManagerAttributes.State state = rmManager.getState();
			if (state == ResourceManagerAttributes.State.STARTED) {
				boolean shutdown = MessageDialog.openConfirm(getTargetShell(),
						UIMessage.getResourceString("StopResourceManagersObjectActionDelegate.Title"), //$NON-NLS-1$
						UIMessage.getResourceString("StopResourceManagersObjectActionDelegate.Confirm") //$NON-NLS-1$
						+ rmManager.getName()
						+ UIMessage.getResourceString("StopResourceManagersObjectActionDelegate.ResourceManager")); //$NON-NLS-1$
				if (!shutdown) {
					return;
				}
			}

			try {
				rmManager.shutdown();
			} catch (CoreException e) {
				final String message = UIMessage.getResourceString("StopResourceManagersObjectActionDelegate.UnableStopResourceManager") //$NON-NLS-1$
						+ rmManager.getName() + "\""; //$NON-NLS-1$
				Status status = new Status(Status.ERROR, PTPUIPlugin.PLUGIN_ID,
						1, message, e);
				ErrorDialog dlg = new ErrorDialog(getTargetShell(),
						UIMessage.getResourceString("StopResourceManagersObjectActionDelegate.ErrorStopingResourceManager"), message, status, //$NON-NLS-1$
						IStatus.ERROR);
				dlg.open();
				PTPUIPlugin.log(status);
			}
		}
	}

	@Override
	protected boolean isEnabledFor(IResourceManager rmManager) {
		ResourceManagerAttributes.State state = rmManager.getState();
		if (state == ResourceManagerAttributes.State.STARTED ||
				state == ResourceManagerAttributes.State.ERROR) {
			return true;
		}
		return false;
	}

}
