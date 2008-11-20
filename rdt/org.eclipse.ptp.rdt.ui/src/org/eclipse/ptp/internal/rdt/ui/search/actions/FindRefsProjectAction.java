/*******************************************************************************
 * Copyright (c) 2005, 2008 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/

/* -- ST-Origin --
 * Source folder: org.eclipse.cdt.ui/src
 * Class: org.eclipse.cdt.internal.ui.search.actions.FindRefsProjectAction
 * Version: 1.7
 */

package org.eclipse.ptp.internal.rdt.ui.search.actions;

import org.eclipse.cdt.core.model.CoreModel;
import org.eclipse.cdt.core.model.ICElement;
import org.eclipse.cdt.core.model.ICProject;
import org.eclipse.cdt.internal.ui.editor.CEditor;
import org.eclipse.cdt.internal.ui.search.CSearchMessages;
import org.eclipse.cdt.internal.ui.search.PDOMSearchQuery;
import org.eclipse.core.resources.IResource;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.ui.IWorkbenchSite;

public class FindRefsProjectAction extends FindAction {

	public FindRefsProjectAction(CEditor editor, String label, String tooltip){
		super(editor);
		setText(label); 
		setToolTipText(tooltip); 
	}
	
	public FindRefsProjectAction(CEditor editor){
		this(editor,
			CSearchMessages.CSearch_FindReferencesProjectAction_label, 
			CSearchMessages.CSearch_FindReferencesProjectAction_tooltip); 
	}
	
	public FindRefsProjectAction(IWorkbenchSite site){
		this(site,
			CSearchMessages.CSearch_FindReferencesProjectAction_label, 
			CSearchMessages.CSearch_FindReferencesProjectAction_tooltip); 
	}

	public FindRefsProjectAction(IWorkbenchSite site, String label, String tooltip) {
		super(site);
		setText(label);
		setToolTipText(tooltip);
	}

	protected ICElement[] getScope() {
		ICProject project = null;
		if (fEditor != null) {
			project = fEditor.getInputCElement().getCProject();			 
		} else if (fSite != null){
			ISelection selection = getSelection();
			if (selection instanceof IStructuredSelection) {
				Object element = ((IStructuredSelection)selection).getFirstElement();
				if (element instanceof IResource)
					project = CoreModel.getDefault().create(((IResource)element).getProject());
				else if (element instanceof ICElement)
					project = ((ICElement)element).getCProject();
			}
		}
		
		return project != null ? new ICElement[] { project } : null;
	}

	protected String getScopeDescription() {
		return CSearchMessages.ProjectScope; 
	}

	protected int getLimitTo() {
		return PDOMSearchQuery.FIND_REFERENCES;
	}

}
