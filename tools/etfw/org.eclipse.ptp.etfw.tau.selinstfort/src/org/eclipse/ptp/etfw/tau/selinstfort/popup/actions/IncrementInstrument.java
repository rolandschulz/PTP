/****************************************************************************
 *			Tuning and Analysis Utilities
 *			http://www.cs.uoregon.edu/research/paracomp/tau
 ****************************************************************************
 * Copyright (c) 1997-2006
 *    Department of Computer and Information Science, University of Oregon
 *    Advanced Computing Laboratory, Los Alamos National Laboratory
 *    Research Center Juelich, ZAM Germany	
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Wyatt Spear - initial API and implementation
 ****************************************************************************/
package org.eclipse.ptp.etfw.tau.selinstfort.popup.actions;

import java.util.LinkedHashSet;

import org.eclipse.jface.action.IAction;
import org.eclipse.jface.text.ITextSelection;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.ptp.etfw.tau.selinst.Selector;
import org.eclipse.ptp.etfw.tau.selinstfort.messages.Messages;
import org.eclipse.ui.IEditorActionDelegate;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.IFileEditorInput;
import org.eclipse.ui.editors.text.TextEditor;

/**
 * Action for adding selective instrumentation of monotonically increasing events to a Photran source file via selection of source
 * code in the editor
 */
public class IncrementInstrument implements IEditorActionDelegate {

	TextEditor textEditor;

	/**
	 * Takes the position of the selected text and creates a user-defined selective instrumentation entry
	 * 
	 * @see org.eclipse.ui.IActionDelegate#run(IAction)
	 */
	public void run(IAction action) {
		final IFileEditorInput input = (IFileEditorInput) textEditor.getEditorInput();
		if (input != null) {
			input.getFile().getProject().getLocation().toOSString();
			final String location = input.getFile().getProject().getLocation().toOSString();

			int insertregs = 0;
			int insertstops = 0;
			final ITextSelection ts = (ITextSelection) textEditor.getSelectionProvider().getSelection();
			if (ts.getLength() <= 0)
			{
				System.out.println(Messages.IncrementInstrument_SelectAreaToInstrument);
			}
			insertregs = ts.getStartLine() + 1;
			insertstops = ts.getEndLine() + 1;

			final LinkedHashSet<String> instlines = new LinkedHashSet<String>();

			final String mainLine = org.eclipse.ptp.etfw.tau.selinst.popup.actions.IncrementInstrument.getPhaseTimeLine(input
					.getFile().getName(), insertregs, insertstops);

			if (mainLine == null)
			{
				return;
			}

			instlines.add(mainLine);
			final Selector selectinst = new Selector(location);
			selectinst.addInst(instlines);
		}
	}

	/**
	 * Enables the action if text has been selected, otherwise, the action is
	 * disabled.
	 * 
	 * @see org.eclipse.ui.IActionDelegate#selectionChanged(IAction, ISelection)
	 */
	public void selectionChanged(IAction action, ISelection selection) {

		action.setEnabled(true);
	}

	/**
	 * Saves a reference to the current active editor
	 * 
	 * @see org.eclipse.ui.IEditorActionDelegate#setActiveEditor(IAction, IEditorPart)
	 */
	public void setActiveEditor(IAction action, IEditorPart targetEditor) {
		textEditor = (TextEditor) targetEditor;
	}
}
