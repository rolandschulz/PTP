/*******************************************************************************
 * Copyright (c) 2004, 2010 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corp. - Rational Software - initial implementation
 *     Markus Schorn (Wind River Systems)
 *******************************************************************************/

/* -- ST-Origin --
 * Source folder: org.eclipse.cdt.ui/src
 * Class: org.eclipse.cdt.internal.ui.search.actions.FindRefsInWorkingSetAction
 * Version: 1.15
 */

package org.eclipse.ptp.internal.rdt.ui.search.actions;

import org.eclipse.cdt.internal.ui.editor.CEditor;
import org.eclipse.cdt.internal.ui.search.CSearchMessages;
import org.eclipse.cdt.internal.ui.search.PDOMSearchQuery;
import org.eclipse.ui.IWorkbenchSite;
import org.eclipse.ui.IWorkingSet;

public class FindRefsInWorkingSetAction extends FindInWorkingSetAction {
	
	public FindRefsInWorkingSetAction(CEditor editor, IWorkingSet[] workingSets) {
		super(editor,
				CSearchMessages.CSearch_FindReferencesInWorkingSetAction_label, 
				CSearchMessages.CSearch_FindReferencesInWorkingSetAction_tooltip, 
				workingSets);
	}
	
	public FindRefsInWorkingSetAction(IWorkbenchSite site, IWorkingSet[] workingSets){
		super (site,
				CSearchMessages.CSearch_FindReferencesInWorkingSetAction_label, 
				CSearchMessages.CSearch_FindReferencesInWorkingSetAction_tooltip, 
				workingSets);
	}
	
	@Override
	protected int getLimitTo() {
		return PDOMSearchQuery.FIND_REFERENCES;
	}
}
