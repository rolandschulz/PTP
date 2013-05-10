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
package org.eclipse.ptp.etfw.tau.selinst.popup.actions;

import java.util.LinkedHashSet;

import org.eclipse.cdt.core.model.ICElement;
import org.eclipse.cdt.internal.ui.editor.CEditor;
import org.eclipse.cdt.ui.CUIPlugin;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.dialogs.IInputValidator;
import org.eclipse.jface.dialogs.InputDialog;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.text.ITextSelection;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.window.Window;
import org.eclipse.ptp.etfw.tau.selinst.Selector;
import org.eclipse.ptp.etfw.tau.selinst.messages.Messages;
import org.eclipse.ui.IEditorActionDelegate;
import org.eclipse.ui.IEditorPart;

/*
 * Thanks to "The Java Developer's Guide to Eclipse"
 *   by D'Anjou, Fairbrother, Kehn, Kellerman, McCarthy
 */

/**
 * Action for adding selective instrumentation of monotonically increasing events to a CDT source file via selection of source code
 * in the editor
 */
@SuppressWarnings("restriction")
public class IncrementInstrument implements IEditorActionDelegate {

	public static String getPhaseTimeLine(String file, int start, int stop)
	{
		class ValidateName implements IInputValidator {

			public String isValid(String newText) {
				if (newText.equals("")) {
					return Messages.IncrementInstrument_EnterValidText;
				}
				return null;
			}
		}

		final String[] opts = {
				"static timer", "dynamic timer", "static phase", "dynamic phase", Messages.IncrementInstrument_Cancel }; //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$
		final MessageDialog timephase = new MessageDialog(CUIPlugin.getActiveWorkbenchShell(),
				Messages.IncrementInstrument_InstTypeSelect, null, Messages.IncrementInstrument_SelectOneOfFollowing,
				MessageDialog.QUESTION, opts, 0);

		if (timephase.open() == 4) {
			return null;
		}

		final int optline = timephase.getReturnCode();

		final InputDialog namedialog = new InputDialog(CUIPlugin.getActiveWorkbenchShell(),
				Messages.IncrementInstrument_UserDefEventName, Messages.IncrementInstrument_EnterUniqueName, "", new ValidateName());

		if (namedialog.open() == Window.CANCEL) {
			return null;
		}
		final String testline = namedialog.getValue();

		return opts[optline]
				+ " name=\"TAU__" + testline.replaceAll("\\W", "") + "\" file=\"" + file + "\" line=" + start + " to line=" + stop; //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$ //$NON-NLS-5$ //$NON-NLS-6$

	}

	CEditor textEditor;

	/**
	 * Takes the position of the selected text and creates a user-defined selective instrumentation entry
	 * 
	 * @see org.eclipse.ui.IActionDelegate#run(IAction)
	 */
	public void run(IAction action) {
		final ICElement cele = textEditor.getInputCElement();

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
		final Selector selectinst = new Selector(cele.getCProject().getResource().getLocation().toOSString());
		final String mainLine = getPhaseTimeLine(cele.getElementName(), insertregs, insertstops);

		if (mainLine == null)
		{
			return;
		}

		instlines.add(mainLine);
		selectinst.addInst(instlines);
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
		textEditor = (CEditor) targetEditor;
	}
}
