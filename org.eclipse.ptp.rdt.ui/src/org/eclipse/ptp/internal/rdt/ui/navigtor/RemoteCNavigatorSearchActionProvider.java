/*******************************************************************************
 * Copyright (c) 2006, 2009 Wind River Systems, Inc. and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Anton Leherbauer (Wind River Systems) - initial API and implementation
 *     IBM Corporation
 *******************************************************************************/
package org.eclipse.ptp.internal.rdt.ui.navigtor;

import org.eclipse.cdt.core.model.ICElement;
import org.eclipse.core.resources.IProject;
import org.eclipse.jface.action.IMenuManager;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.ptp.internal.rdt.ui.search.actions.SelectionSearchGroup;
import org.eclipse.ptp.rdt.core.resources.RemoteNature;
import org.eclipse.ui.IActionBars;
import org.eclipse.ui.IViewPart;
import org.eclipse.ui.actions.ActionContext;
import org.eclipse.ui.navigator.CommonActionProvider;
import org.eclipse.ui.navigator.ICommonActionExtensionSite;
import org.eclipse.ui.navigator.ICommonViewerWorkbenchSite;

/**
 * Common Navigator action provider for the C-search sub menus.
 * 
 * @see org.eclipse.cdt.internal.ui.search.actions.SelectionSearchGroup
 */
public class RemoteCNavigatorSearchActionProvider extends CommonActionProvider {

	private SelectionSearchGroup fSearchGroup;
	private org.eclipse.cdt.internal.ui.search.actions.SelectionSearchGroup fCDTSearchGroup;
	
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
				fSearchGroup= new SelectionSearchGroup(workbenchSite.getSite());
				fCDTSearchGroup = new org.eclipse.cdt.internal.ui.search.actions.SelectionSearchGroup(workbenchSite.getSite());
			}
		}
	}

	/*
	 * @see org.eclipse.ui.actions.ActionGroup#dispose()
	 */
	@Override
	public void dispose() {
		if (fSearchGroup != null) {
			fSearchGroup.dispose();
			fSearchGroup = null;
		}
		if (fCDTSearchGroup != null) {
			fCDTSearchGroup.dispose();
			fCDTSearchGroup = null;
		}
		super.dispose();
	}

	/*
	 * @see org.eclipse.ui.actions.ActionGroup#fillActionBars(org.eclipse.ui.IActionBars)
	 */
	@Override
	public void fillActionBars(IActionBars actionBars) {
		if (fSearchGroup != null) {
			fSearchGroup.fillActionBars(actionBars);
		}
		if (fCDTSearchGroup != null) {
			fCDTSearchGroup.fillActionBars(actionBars);
		}
	}

	/*
	 * @see org.eclipse.ui.actions.ActionGroup#fillContextMenu(org.eclipse.jface.action.IMenuManager)
	 */
	@Override
	public void fillContextMenu(IMenuManager menu) {
		if (fSearchGroup != null && fCDTSearchGroup != null) {
			ISelection selection = getContext().getSelection();
			if (selection != null && !selection.isEmpty() && selection instanceof StructuredSelection) {
				Object sel = ((StructuredSelection)selection).getFirstElement();
				if (sel instanceof ICElement) {
					IProject project = ((ICElement)sel).getCProject().getProject();
					if (!RemoteNature.hasRemoteNature(project)){
						if (org.eclipse.cdt.internal.ui.search.actions.SelectionSearchGroup.canActionBeAdded(selection)){
							fCDTSearchGroup.fillContextMenu(menu);
						}
					} else {
						if (SelectionSearchGroup.canActionBeAdded(selection)){
							fSearchGroup.fillContextMenu(menu);
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
		if (fSearchGroup != null) {
			fSearchGroup.setContext(context);
		}
		if (fCDTSearchGroup != null) {
			fCDTSearchGroup.setContext(context);
		}
	}

	/*
	 * @see org.eclipse.ui.actions.ActionGroup#updateActionBars()
	 */
	@Override
	public void updateActionBars() {
		if (fSearchGroup != null) {
			fSearchGroup.updateActionBars();
		}
		if (fCDTSearchGroup != null) {
			fCDTSearchGroup.updateActionBars();
		}
	}
	
}
