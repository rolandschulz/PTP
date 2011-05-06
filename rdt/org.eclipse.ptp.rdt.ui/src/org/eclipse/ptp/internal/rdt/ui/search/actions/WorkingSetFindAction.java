/*******************************************************************************
 * Copyright (c) 2005, 2011 QNX Software Systems and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     QNX Software Systems - initial API and implementation
 *     IBM Corporation
 *******************************************************************************/

/* -- ST-Origin --
 * Source folder: org.eclipse.cdt.ui/src
 * Class: org.eclipse.cdt.internal.ui.search.actions.WorkingSetFindAction
 * Version: 1.10
 */

package org.eclipse.ptp.internal.rdt.ui.search.actions;

import org.eclipse.cdt.core.model.ICElement;
import org.eclipse.cdt.internal.ui.editor.CEditor;
import org.eclipse.ui.IWorkbenchSite;

public class WorkingSetFindAction extends FindAction {

	private FindAction findAction;
	
	public WorkingSetFindAction(CEditor editor, FindAction action, String string) {
		super ( editor );
		this.findAction = action;
		setText(string); 
	}

	public WorkingSetFindAction(IWorkbenchSite site,FindAction action, String string) {
		super(site);
		this.findAction = action;
		setText(string); 
	}

	@Override
	protected String getScopeDescription() {
		return findAction.getScopeDescription();
	}

	@Override
	protected ICElement[] getScope() {
		return findAction.getScope();
	}

	@Override
	protected int getLimitTo() {
		return findAction.getLimitTo();
	}

	@Override
	public void run() {
		findAction.run();
	}

}
