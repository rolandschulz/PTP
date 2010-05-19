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
package org.eclipse.ptp.etfw.tau;

import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.Preferences;
import org.eclipse.jface.dialogs.Dialog;
//import org.eclipse.ptp.etfw.tau.options.TAUOptionsPlugin;
//import org.eclipse.ptp.etfw.tau.Activator;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ptp.etfw.Activator;

/**
 * Launches a splash screen to prompt for global and tau-specific options for building/launching applications
 * @author wspear
 *
 */
public class OptionSplash extends Dialog{

	Button autoref;
	Button autobuild;
	Button fixAix;
	Button doagain;
	
	
	protected OptionSplash(Shell parentShell) {
		super(parentShell);
		
	}

	/**
	 * Defines the UI of the dialog, including options for enabling autorefresh, disabling autobuild and using the internal builder on AIX
	 */
	protected Control createDialogArea(Composite parent) {
		Composite composite = (Composite) super.createDialogArea(parent);
		
		Label introlabel = new Label(composite, SWT.NONE);
		introlabel.setText(Messages.OptionSplash_AdjFollowingOpts +
				Messages.OptionSplash_ImproveTauUsageExperience);
		autoref= new Button(composite, SWT.CHECK);
		autoref.setText(Messages.OptionSplash_AutoRefresh);
		autoref.setToolTipText(Messages.OptionSplash_TauSugOn);
		autoref.setSelection(ResourcesPlugin.getPlugin().getPluginPreferences().getBoolean(ResourcesPlugin.PREF_AUTO_REFRESH));
		
		autobuild= new Button(composite, SWT.CHECK);
		autobuild.setText(Messages.OptionSplash_AutoBuild);
		autobuild.setToolTipText(Messages.OptionSplash_TauSugOff);
		autobuild.setSelection(ResourcesPlugin.getPlugin().getPluginPreferences().getBoolean(ResourcesPlugin.PREF_AUTO_BUILDING));
		
		boolean isAIX=org.eclipse.cdt.utils.Platform.getOS().toLowerCase().trim().indexOf("aix")>=0; //$NON-NLS-1$
		if(isAIX)
		{
			fixAix= new Button(composite, SWT.CHECK);
			fixAix.setText(
			Messages.OptionSplash_AutoEclipseInternal
			);
			fixAix.setToolTipText(Messages.OptionSplash_TauSuggestsOnDesc);
			Preferences preferences = Activator.getDefault().getPluginPreferences();
			fixAix.setSelection(preferences.getBoolean("TAUCheckForAIXOptions")); //$NON-NLS-1$
			
		}
		
		doagain= new Button(composite, SWT.CHECK);
		doagain.setText(Messages.OptionSplash_ShowScreenWhenProf);
		doagain.setToolTipText(Messages.OptionSplash_EnDisAbleTauSplash);
		doagain.setSelection(Activator.getDefault().getPluginPreferences().getBoolean("TAUCheckForAutoOptions")); //$NON-NLS-1$
		
		return composite;
	}
	
	/**
	 * Sets the selected options upon user confirmation
	 */
	protected void okPressed() {
		
		if(autoref!=null)
			ResourcesPlugin.getPlugin().getPluginPreferences().setValue(ResourcesPlugin.PREF_AUTO_REFRESH, autoref.getSelection());
		if(autobuild!=null)
			ResourcesPlugin.getPlugin().getPluginPreferences().setValue(ResourcesPlugin.PREF_AUTO_BUILDING, autobuild.getSelection());
		
		if(fixAix!=null)
			Activator.getDefault().getPluginPreferences().setValue("TAUCheckForAIXOptions", fixAix.getSelection()); //$NON-NLS-1$
		
		if(doagain!=null)
			Activator.getDefault().getPluginPreferences().setValue("TAUCheckForAutoOptions", doagain.getSelection()); //$NON-NLS-1$
		
		super.okPressed();
	}
	
}
