/**
 * Copyright (c) 2009 IBM Corporation.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - Initial Implementation
 *
 */
package org.eclipse.ptp.ui.actions;

import org.eclipse.jface.action.Action;
import org.eclipse.ptp.ui.PTPUIPlugin;
import org.eclipse.ptp.ui.managers.RMManager;
import org.eclipse.ptp.ui.messages.Messages;
import org.eclipse.ptp.ui.views.ResourceManagerView;

public class SelectDefaultResourceManagerAction extends Action {

	private String fRmId;
	private final ResourceManagerView fView;

	public SelectDefaultResourceManagerAction(ResourceManagerView view) {
		super(Messages.SelectDefaultResourceManagerAction_0);
		fView = view;
	}

	@Override
	public void run() {
		RMManager rm = PTPUIPlugin.getDefault().getRMManager();
		if (rm != null) {
			rm.fireSetDefaultRMEvent(fRmId);
		}
		fView.refreshViewer();
	}

	/**
	 * @since 5.0
	 */
	public void setResourceManager(String rmId) {
		fRmId = rmId;
	}
}
