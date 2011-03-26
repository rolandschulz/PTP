/*******************************************************************************
 * Copyright (c) 2011 University of Illinois All rights reserved. This program
 * and the accompanying materials are made available under the terms of the
 * Eclipse Public License v1.0 which accompanies this distribution, and is
 * available at http://www.eclipse.org/legal/epl-v10.html 
 * 	
 * Contributors: 
 * 	Albert L. Rossi - design and implementation
 ******************************************************************************/

package org.eclipse.ptp.rm.jaxb.ui.actions;

import org.eclipse.jface.action.IAction;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.ptp.rm.jaxb.ui.IJAXBUINonNLSConstants;
import org.eclipse.ptp.rm.jaxb.ui.messages.Messages;
import org.eclipse.ptp.rm.jaxb.ui.util.WidgetActionUtils;
import org.eclipse.ui.IViewActionDelegate;
import org.eclipse.ui.IViewPart;
import org.eclipse.ui.IWorkbenchPage;

public class HideResourceManagerEditor implements IViewActionDelegate, IJAXBUINonNLSConstants {
	private IViewPart part;

	public void init(IViewPart view) {
		this.part = view;
	}

	public void run(IAction action) {
		try {
			IWorkbenchPage page = part.getViewSite().getWorkbenchWindow().getActivePage();
			page.setEditorAreaVisible(false);
		} catch (Throwable e) {
			WidgetActionUtils.errorMessage(part.getSite().getShell(), e, Messages.HideResourceManagerEditorAction_error,
					Messages.HideResourceManagerEditorAction_title, false);
		}
	}

	public void selectionChanged(IAction action, ISelection selection) {
	}

}
