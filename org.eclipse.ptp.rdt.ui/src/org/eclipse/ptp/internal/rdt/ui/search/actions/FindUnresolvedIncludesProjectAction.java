/*******************************************************************************
 * Copyright (c) 2008, 2009 Wind River Systems, Inc. and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Markus Schorn - initial API and implementation
 *    IBM Corporation
 *******************************************************************************/ 
package org.eclipse.ptp.internal.rdt.ui.search.actions;

import org.eclipse.jface.action.IAction;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.ui.IObjectActionDelegate;
import org.eclipse.ui.IWorkbenchPart;

/**
 * Searches projects for unresolved includes.
 * Could be extended to work on resource selections.
 */
public class FindUnresolvedIncludesProjectAction implements IObjectActionDelegate {

	public FindUnresolvedIncludesProjectAction() {
	}

	public void run(IAction action) {
	}

	public void setActivePart(IAction action, IWorkbenchPart targetPart) {
		action.setEnabled(false);
	}

	public void selectionChanged(IAction action, ISelection selection) {
		action.setEnabled(false);
	}
}
