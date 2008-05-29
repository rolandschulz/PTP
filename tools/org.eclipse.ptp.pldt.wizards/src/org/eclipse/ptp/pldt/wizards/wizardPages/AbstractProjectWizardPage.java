/*******************************************************************************
 * Copyright (c) 2006, 2008 IBM Corp. and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     IBM Corp. - initial API and implementation
 *******************************************************************************/

package org.eclipse.ptp.pldt.wizards.wizardPages;

import org.eclipse.cdt.ui.templateengine.AbstractWizardDataPage;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.preference.IPreferenceNode;
import org.eclipse.jface.preference.IPreferencePage;
import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.jface.preference.PreferenceDialog;
import org.eclipse.jface.preference.PreferenceManager;
import org.eclipse.jface.preference.PreferenceNode;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.PlatformUI;

/**
 * Common behavior between the different PLDT new project wizard pages
 * @author beth
 *
 */
public abstract class AbstractProjectWizardPage extends AbstractWizardDataPage{

	protected IPreferenceStore preferenceStore;
	protected IPreferencePage preferencePage;
	protected String prefIDincludes;

	public AbstractProjectWizardPage(String string) {
		super(string);
	}
	/**
	 * Inform the user that PLDT preferences have not been set, and offer to set them now,
	 * and return the include path.
	 * @param type - e.g. "MPI" or "OpenMP" the type of preferences that aren't set
	 * @return
	 */
	protected  String showNoPrefs(String type, String prefID) {
		Shell shell = PlatformUI.getWorkbench().getActiveWorkbenchWindow().getShell();
		
		//IPreferencePage pp = new MPIPreferencePage();
		//MessageDialogWithLink md = new MessageDialogWithLink(shell,"MPI", "MPI preferences have not been set. Click below to do it now.","Click here to add MPI preferencess now",pp);
		//int result = md.open();
		
		String msg = type+" Preferences have not been set.  Do you want to set them now?";
		boolean doit = MessageDialog.openQuestion(shell, "No "+type+" Preferences", msg);
		if(doit) {
			showPreferenceDialog(type);
		}
		String mip = preferenceStore.getString(prefID);
		return mip;
	}
	private void showPreferenceDialog(String type) {
		PreferenceManager mgr = new PreferenceManager();
		IPreferencePage preferencePage = getPreferencePage();
		preferencePage.setTitle(type);
		IPreferenceNode node = new PreferenceNode("1", preferencePage);
		mgr.addToRoot(node);
		Shell shell = Display.getCurrent().getActiveShell();
		PreferenceDialog dialog = new PreferenceDialog(shell, mgr);
		dialog.create();
		// must do dialog.create() before setting message
		dialog.setMessage(preferencePage.getTitle()); 
		dialog.open();
	}
	abstract IPreferencePage getPreferencePage();

}
