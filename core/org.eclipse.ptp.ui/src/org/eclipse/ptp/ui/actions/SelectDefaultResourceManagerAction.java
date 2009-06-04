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
import org.eclipse.ptp.core.elementcontrols.IResourceManagerControl;
import org.eclipse.ptp.ui.PTPUIPlugin;
import org.eclipse.ptp.ui.messages.Messages;
import org.eclipse.ptp.ui.views.ResourceManagerView;

public class SelectDefaultResourceManagerAction extends Action {
	
	private IResourceManagerControl resourceManager;
	private ResourceManagerView view;

	public SelectDefaultResourceManagerAction(ResourceManagerView view) {
		super(Messages.SelectDefaultResourceManagerAction_0);
		this.view = view;
	}

	public void run() {
		PTPUIPlugin.getDefault().getRMManager().fireRMSelectedEvent(resourceManager);
		view.refreshViewer();
	}

	public void setResourceManager(IResourceManagerControl rm) {
		resourceManager = rm;
	}
}
