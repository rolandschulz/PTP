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

import org.eclipse.cdt.ui.CUIPlugin;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.dialogs.IInputValidator;
import org.eclipse.jface.dialogs.InputDialog;
import org.eclipse.jface.text.ITextSelection;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.window.Window;
import org.eclipse.ptp.etfw.tau.selinst.Selector;
import org.eclipse.ptp.etfw.tau.selinstfort.messages.Messages;
import org.eclipse.ui.IEditorActionDelegate;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.IFileEditorInput;
import org.eclipse.ui.editors.text.TextEditor;

/**
 * Action for adding selective instrumentation of user defined events to a Photran source file via selection of source code in the editor 
 * @author wspear
 *
 */
public class Instrument implements IEditorActionDelegate {
	TextEditor textEditor;

	/**
	 * Saves a reference to the current active editor
	 * 
	 * @see org.eclipse.ui.IEditorActionDelegate#setActiveEditor(IAction,
	 *      IEditorPart)
	 */
	public void setActiveEditor(IAction action, IEditorPart targetEditor) {
		textEditor = (TextEditor) targetEditor;
	}

	/**
	 * Takes the position of the selected text and creates a user-defined selective instrumentation entry
	 * 
	 * @see org.eclipse.ui.IActionDelegate#run(IAction)
	 */
	public void run(IAction action) {
		IFileEditorInput input = (IFileEditorInput)textEditor.getEditorInput();
		if (input != null) {
			input.getFile().getProject().getLocation().toOSString();
			String location = input.getFile().getProject().getLocation().toOSString();
	
			int insertregs=0;
			
			ITextSelection ts = (ITextSelection) textEditor.getSelectionProvider().getSelection();
	
			insertregs = ts.getStartLine()+1;
	
			class validateName implements IInputValidator{
	
				public String isValid(String newText) {
					if(newText.equals("")) //$NON-NLS-1$
						return Messages.Instrument_EnterValidText;
					return null;
				}
			}
			
			class validateValue implements IInputValidator{
	
				public String isValid(String newText) {
					String err=Messages.Instrument_EnterValidDoubleOrVar;
					if(newText.equals("")) //$NON-NLS-1$
						return err;
					
					String fixed = newText.replaceAll("\\W", ""); //$NON-NLS-1$ //$NON-NLS-2$
					if(!newText.equals(fixed))
					{
						if(newText.length()-fixed.length()==1)
							if(newText.indexOf(".")>=0) //$NON-NLS-1$
								if(fixed.replaceAll("\\d", "").equals("")) //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
									return null;
						return err;
					}
					return null;
				}
			}
			InputDialog namedialog = new InputDialog(CUIPlugin.getActiveWorkbenchShell(), Messages.Instrument_UserDefinedEventName, Messages.Instrument_EnterUniqueName, "", new validateName()); //$NON-NLS-3$
	
			if(namedialog.open() == Window.CANCEL)return;
			String testline = namedialog.getValue();
			
			
			InputDialog incdialog = new InputDialog(CUIPlugin.getActiveWorkbenchShell(), Messages.Instrument_UserDefinedEventValue, Messages.Instrument_EnterStaticValueOrVariable, "", new validateValue()); //$NON-NLS-3$
			if(incdialog.open() == Window.CANCEL)return;
			String testinc = incdialog.getValue();
			
			String fixline = "TAU_"+testline.replaceAll("\\W", ""); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
			LinkedHashSet<String> instlines = new LinkedHashSet<String>();
			String initline1= "      integer "+fixline+"(2) / 0, 0 /"; //$NON-NLS-1$ //$NON-NLS-2$
			String initline2= "      save "+fixline; //$NON-NLS-1$
			String regline =  "      call TAU_REGISTER_EVENT("+fixline+", \'"+testline+"\')"; //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
			String evtline =  "      call TAU_EVENT("+fixline+", "+testinc+");"; //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
			
	
			String nosselinitline1 = "entry file=\""+input.getFile().getName()+"\" routine=\"#\" code=\""+initline1+"\""; //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
			String nosselinitline2 = "entry file=\""+input.getFile().getName()+"\" routine=\"#\" code=\""+initline2+"\""; //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
			String nosselregline = "file=\""+input.getFile().getName()+"\" line="+insertregs+" code=\""+regline+"\""; //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$
			String nosselinstline = "file=\""+input.getFile().getName()+"\" line="+insertregs+" code=\""+evtline+"\""; //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$
			instlines.add(nosselinitline1);
			instlines.add(nosselinitline2);
			instlines.add(nosselregline);
			instlines.add(nosselinstline);
			
			Selector selectinst = new Selector(location);
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
}
