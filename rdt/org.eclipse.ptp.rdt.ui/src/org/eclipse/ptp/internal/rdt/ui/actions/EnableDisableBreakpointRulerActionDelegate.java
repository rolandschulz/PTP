/*******************************************************************************
 * Copyright (c) 2009 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    IBM Corporation - Initial API and implementation
 *******************************************************************************/
package org.eclipse.ptp.internal.rdt.ui.actions;

import org.eclipse.jface.action.IAction;
import org.eclipse.jface.action.IMenuManager;
import org.eclipse.jface.text.source.IVerticalRulerInfo;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.ui.texteditor.AbstractRulerActionDelegate;
import org.eclipse.ui.texteditor.ITextEditor;

public class EnableDisableBreakpointRulerActionDelegate extends AbstractRulerActionDelegate {

	/*
	 * @see AbstractRulerActionDelegate#createAction(ITextEditor, IVerticalRulerInfo)
	 */
	protected IAction createAction( ITextEditor editor, IVerticalRulerInfo rulerInfo ) {
		return null;
	}
	
	@Override
	public void menuAboutToShow(IMenuManager manager) {

	}
	
	@Override
	public void selectionChanged(IAction action, ISelection selection) {
		action.setEnabled(false);
	}
}
