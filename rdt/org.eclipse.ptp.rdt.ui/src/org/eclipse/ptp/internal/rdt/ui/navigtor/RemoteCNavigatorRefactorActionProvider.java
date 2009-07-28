/*******************************************************************************
 * Copyright (c) 2006, 2009 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/

package org.eclipse.ptp.internal.rdt.ui.navigtor;

import org.eclipse.cdt.core.model.ICElement;
import org.eclipse.cdt.internal.ui.navigator.CNavigatorRefactorActionProvider;
import org.eclipse.core.resources.IProject;
import org.eclipse.jface.action.IMenuManager;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.ptp.rdt.core.resources.RemoteNature;
import org.eclipse.ui.IActionBars;
import org.eclipse.ui.actions.ActionContext;
import org.eclipse.ui.navigator.CommonActionProvider;
import org.eclipse.ui.navigator.ICommonActionExtensionSite;

/**
 * A clone of org.eclipse.ui.internal.navigator.resources.actions.RefactorActionProvider.
 */
public class RemoteCNavigatorRefactorActionProvider extends CommonActionProvider {

	private CNavigatorRefactorActionProvider fCDTRefactorActionProvider;
	
	public RemoteCNavigatorRefactorActionProvider() {
		super();
		fCDTRefactorActionProvider = new CNavigatorRefactorActionProvider();
	}
	/*
	 * @see org.eclipse.ui.navigator.CommonActionProvider#init(org.eclipse.ui.navigator.ICommonActionExtensionSite)
	 */
	@Override
	public void init(ICommonActionExtensionSite actionSite) {
		super.init(actionSite);
		fCDTRefactorActionProvider.init(actionSite);
	}

	@Override
	public void dispose() {
		fCDTRefactorActionProvider.dispose();
	}

	@Override
	public void fillActionBars(IActionBars actionBars) {
		fCDTRefactorActionProvider.fillActionBars(actionBars);
	}

	@Override
	public void fillContextMenu(IMenuManager menu) {
		ActionContext context = getContext();
		ISelection selection = context.getSelection();
		if (selection != null && !selection.isEmpty() && selection instanceof StructuredSelection) {
			Object sel = ((StructuredSelection)selection).getFirstElement();
			if (sel instanceof ICElement) {
				IProject project = ((ICElement)sel).getCProject().getProject();
				if (!RemoteNature.hasRemoteNature(project)){
					fCDTRefactorActionProvider.fillContextMenu(menu);
				}
			}
		}
	}

	@Override
	public void setContext(ActionContext context) {
		super.setContext(context);
		fCDTRefactorActionProvider.setContext(context);
	}

	@Override
	public void updateActionBars() {
		fCDTRefactorActionProvider.updateActionBars();
	}

}
