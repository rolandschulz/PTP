/*******************************************************************************
 * Copyright (c) 2007, 2009 Wind River Systems, Inc. and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Markus Schorn - initial API and implementation
 *    IBM Corporation
 *******************************************************************************/ 

package org.eclipse.ptp.internal.rdt.ui.navigtor;

import org.eclipse.cdt.core.model.ICElement;
import org.eclipse.core.resources.IProject;
import org.eclipse.jface.action.IMenuManager;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.ptp.internal.rdt.ui.actions.OpenViewActionGroup;
import org.eclipse.ptp.rdt.core.resources.RemoteNature;
import org.eclipse.ui.IActionBars;
import org.eclipse.ui.IViewPart;
import org.eclipse.ui.actions.ActionContext;
import org.eclipse.ui.navigator.CommonActionProvider;
import org.eclipse.ui.navigator.ICommonActionExtensionSite;
import org.eclipse.ui.navigator.ICommonViewerWorkbenchSite;


public class RemoteCNavigatorOpenViewActionProvider extends CommonActionProvider {

	private OpenViewActionGroup fOpenViewActionGroup;
	private org.eclipse.cdt.ui.actions.OpenViewActionGroup fCDTOpenViewActionGroup;

	/*
	 * @see org.eclipse.ui.navigator.CommonActionProvider#init(org.eclipse.ui.navigator.ICommonActionExtensionSite)
	 */
	@Override
	public void init(ICommonActionExtensionSite site) {
		ICommonViewerWorkbenchSite workbenchSite= null;
		if (site.getViewSite() instanceof ICommonViewerWorkbenchSite) {
			workbenchSite= (ICommonViewerWorkbenchSite) site.getViewSite();
		}
		if (workbenchSite != null) {
			if (workbenchSite.getPart() != null && workbenchSite.getPart() instanceof IViewPart) {
				fOpenViewActionGroup= new OpenViewActionGroup(workbenchSite.getPart());
				// properties action is already provided by resource extensions
				fOpenViewActionGroup.setSuppressProperties(true);
				
				fCDTOpenViewActionGroup= new org.eclipse.cdt.ui.actions.OpenViewActionGroup(workbenchSite.getPart());
				// properties action is already provided by resource extensions
				fCDTOpenViewActionGroup.setSuppressProperties(true);
			}
		}
	}

	/*
	 * @see org.eclipse.ui.actions.ActionGroup#dispose()
	 */
	@Override
	public void dispose() {
		if (fOpenViewActionGroup != null) {
			fOpenViewActionGroup.dispose();
			fOpenViewActionGroup = null;
		}
		if (fCDTOpenViewActionGroup != null) {
			fCDTOpenViewActionGroup.dispose();
			fCDTOpenViewActionGroup = null;
		}
		super.dispose();
	}

	/*
	 * @see org.eclipse.ui.actions.ActionGroup#fillActionBars(org.eclipse.ui.IActionBars)
	 */
	@Override
	public void fillActionBars(IActionBars actionBars) {
		if (fOpenViewActionGroup != null) {
			fOpenViewActionGroup.fillActionBars(actionBars);
		}
		if (fCDTOpenViewActionGroup != null) {
			fCDTOpenViewActionGroup.fillActionBars(actionBars);
		}
	}

	/*
	 * @see org.eclipse.ui.actions.ActionGroup#fillContextMenu(org.eclipse.jface.action.IMenuManager)
	 */
	@Override
	public void fillContextMenu(IMenuManager menu) {
		if (fOpenViewActionGroup != null && fCDTOpenViewActionGroup != null) {
			ISelection selection = getContext().getSelection();
			if (selection != null && !selection.isEmpty() && selection instanceof StructuredSelection) {
				Object sel = ((StructuredSelection)selection).getFirstElement();
				if (sel instanceof ICElement) {
					IProject project = ((ICElement)sel).getCProject().getProject();
					if (!RemoteNature.hasRemoteNature(project)){
						if (org.eclipse.cdt.ui.actions.OpenViewActionGroup.canActionBeAdded(selection)){
							fCDTOpenViewActionGroup.fillContextMenu(menu);
						}
					} else {
						if (OpenViewActionGroup.canActionBeAdded(selection)){
							fOpenViewActionGroup.fillContextMenu(menu);
						}
					}
				}
			}			
		}
	}

	/*
	 * @see org.eclipse.ui.actions.ActionGroup#setContext(org.eclipse.ui.actions.ActionContext)
	 */
	@Override
	public void setContext(ActionContext context) {
		super.setContext(context);
		if (fOpenViewActionGroup != null) {
			fOpenViewActionGroup.setContext(context);
		}
		if (fCDTOpenViewActionGroup != null) {
			fCDTOpenViewActionGroup.setContext(context);
		}
	}

	/*
	 * @see org.eclipse.ui.actions.ActionGroup#updateActionBars()
	 */
	@Override
	public void updateActionBars() {
		if (fOpenViewActionGroup != null) {
			fOpenViewActionGroup.updateActionBars();
		}
		if (fCDTOpenViewActionGroup != null) {
			fCDTOpenViewActionGroup.updateActionBars();
		}
	}
}
