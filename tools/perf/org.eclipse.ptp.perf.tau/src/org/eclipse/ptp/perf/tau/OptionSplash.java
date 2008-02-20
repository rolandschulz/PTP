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
package org.eclipse.ptp.perf.tau;

import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.Preferences;
import org.eclipse.jface.dialogs.Dialog;
//import org.eclipse.ptp.perf.tau.options.TAUOptionsPlugin;
//import org.eclipse.ptp.perf.tau.Activator;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ptp.perf.Activator;

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
		introlabel.setText("Adjusting the following options as suggested in the tooltips may\n" +
				"improve your TAU usage experience.");
		autoref= new Button(composite, SWT.CHECK);
		autoref.setText("Auto-Refresh");
		autoref.setToolTipText("TAU Suggests: On");
		autoref.setSelection(ResourcesPlugin.getPlugin().getPluginPreferences().getBoolean(ResourcesPlugin.PREF_AUTO_REFRESH));
		
		autobuild= new Button(composite, SWT.CHECK);
		autobuild.setText("Auto-Build");
		autobuild.setToolTipText("Tau Suggests: Off");
		autobuild.setSelection(ResourcesPlugin.getPlugin().getPluginPreferences().getBoolean(ResourcesPlugin.PREF_AUTO_BUILDING));
		
		boolean isAIX=org.eclipse.cdt.utils.Platform.getOS().toLowerCase().trim().indexOf("aix")>=0;
		if(isAIX)
		{
			fixAix= new Button(composite, SWT.CHECK);
			fixAix.setText(
			"Automatically use Eclipse internal builder (May be needed for AIX compatibility)"
			);
			fixAix.setToolTipText("Tau Suggests: On if you have trouble compiling in AIX.  This option only effects build operations with TAU");
			Preferences preferences = Activator.getDefault().getPluginPreferences();
			fixAix.setSelection(preferences.getBoolean("TAUCheckForAIXOptions"));
			
		}
		
		doagain= new Button(composite, SWT.CHECK);
		doagain.setText("Show this screen when you launch a job for Profiling");
		doagain.setToolTipText("Enable/disable TAU setting splash screen");
		doagain.setSelection(Activator.getDefault().getPluginPreferences().getBoolean("TAUCheckForAutoOptions"));
		
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
			Activator.getDefault().getPluginPreferences().setValue("TAUCheckForAIXOptions", fixAix.getSelection());
		
		if(doagain!=null)
			Activator.getDefault().getPluginPreferences().setValue("TAUCheckForAutoOptions", doagain.getSelection());
		
		super.okPressed();
	}
	
}
