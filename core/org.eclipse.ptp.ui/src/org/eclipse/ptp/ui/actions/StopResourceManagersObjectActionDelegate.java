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
import org.eclipse.osgi.util.NLS;
import org.eclipse.ptp.core.elementcontrols.IResourceManagerControl;
import org.eclipse.ptp.core.elements.attributes.ResourceManagerAttributes;
import org.eclipse.ptp.rmsystem.IResourceManagerMenuContribution;
import org.eclipse.ptp.ui.PTPUIPlugin;
import org.eclipse.ptp.ui.messages.Messages;

public class StopResourceManagersObjectActionDelegate extends AbstractResourceManagerSelectionActionDelegate {

	public void run(IAction action) {

		for (IResourceManagerMenuContribution menuContrib : getMenuContribs()) {
			IResourceManagerControl rmManager = (IResourceManagerControl) menuContrib.getAdapter(IResourceManagerControl.class);

			if (!isEnabledFor(rmManager)) {
				continue;
			}

			/*
			 * Only ask if we are really shutting down the RM
			 */
			ResourceManagerAttributes.State state = rmManager.getState();
			if (state == ResourceManagerAttributes.State.STARTED) {
				boolean shutdown = MessageDialog.openConfirm(getTargetShell(), Messages.StopResourceManagersObjectActionDelegate_0,
						NLS.bind(Messages.StopResourceManagersObjectActionDelegate_1, rmManager.getName()));
				if (!shutdown) {
					return;
				}
			}

			try {
				rmManager.stop();
			} catch (CoreException e) {
				final String message = NLS.bind(Messages.StopResourceManagersObjectActionDelegate_2, rmManager.getName());
				Status status = new Status(Status.ERROR, PTPUIPlugin.PLUGIN_ID, 1, message, e);
				ErrorDialog dlg = new ErrorDialog(getTargetShell(), Messages.StopResourceManagersObjectActionDelegate_3, message,
						status, IStatus.ERROR);
				dlg.open();
				PTPUIPlugin.log(status);
			}
		}
	}

	@Override
	protected boolean isEnabledFor(IResourceManagerControl rmManager) {
		ResourceManagerAttributes.State state = rmManager.getState();
		if (state == ResourceManagerAttributes.State.STARTING || state == ResourceManagerAttributes.State.STARTED
				|| state == ResourceManagerAttributes.State.ERROR) {
			return true;
		}
		return false;
	}

}
