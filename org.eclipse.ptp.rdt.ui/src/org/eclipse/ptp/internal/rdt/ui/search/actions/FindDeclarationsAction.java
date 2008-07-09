/*******************************************************************************
 * Copyright (c) 2004, 2008 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corp. - Rational Software - initial implementation
 *******************************************************************************/

/* -- ST-Origin --
 * Source folder: org.eclipse.cdt.ui/src
 * Class: org.eclipse.cdt.internal.ui.search.actions.FindDeclarationsAction
 * Version: 1.12
 */

package org.eclipse.ptp.internal.rdt.ui.search.actions;

import org.eclipse.cdt.core.model.ICElement;
import org.eclipse.cdt.internal.ui.search.CSearchMessages;
import org.eclipse.cdt.internal.ui.search.PDOMSearchQuery;
import org.eclipse.ptp.internal.rdt.ui.editor.CEditor;
import org.eclipse.ui.IWorkbenchSite;


public class FindDeclarationsAction extends FindAction {
	
	public FindDeclarationsAction(CEditor editor, String label, String tooltip){
		super(editor);
		setText(label); 
		setToolTipText(tooltip); 
	}
	
	public FindDeclarationsAction(CEditor editor){
		this(editor,
			CSearchMessages.CSearch_FindDeclarationAction_label, 
			CSearchMessages.CSearch_FindDeclarationAction_tooltip); 
	}
	
	public FindDeclarationsAction(IWorkbenchSite site){
		this(site,
			CSearchMessages.CSearch_FindDeclarationAction_label, 
			CSearchMessages.CSearch_FindDeclarationAction_tooltip); 
	}

	public FindDeclarationsAction(IWorkbenchSite site, String label, String tooltip) {
		super(site);
		setText(label);
		setToolTipText(tooltip);
	}

	protected ICElement[] getScope() {
		return null;
	}
	
	protected String getScopeDescription() {
		return CSearchMessages.WorkspaceScope; 
	}
	
	protected int getLimitTo() {
		return PDOMSearchQuery.FIND_DECLARATIONS_DEFINITIONS;
	}
}
