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
package org.eclipse.ptp.perf.preferences;

import java.io.File;

import org.eclipse.core.runtime.Preferences;
import org.eclipse.jface.preference.FieldEditor;
import org.eclipse.jface.preference.PreferencePage;
import org.eclipse.jface.util.IPropertyChangeListener;
import org.eclipse.jface.util.PropertyChangeEvent;
import org.eclipse.ptp.perf.Activator;
import org.eclipse.ptp.perf.IPerformanceLaunchConfigurationConstants;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.FileDialog;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Text;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchPreferencePage;

/**
 * Provides a user-interface for and managed workspace-wide performance tool settings.
 * The location of the local tool xml definition file is the most critical of these
 * @author wspear
 *
 */
public class PerformancePreferencePage extends PreferencePage implements IWorkbenchPreferencePage, IPerformanceLaunchConfigurationConstants
{
	protected Text XMLLoc = null;
	protected Button browseXMLButton = null;
	//protected Button checkAutoOpts=null;
	//protected Button checkAixOpts=null;

	public PerformancePreferencePage() {
		setPreferenceStore(Activator.getDefault().getPreferenceStore());
	}

	protected class WidgetListener extends SelectionAdapter implements ModifyListener, IPropertyChangeListener
	{
		public void widgetSelected(SelectionEvent e) {
			Object source = e.getSource();
			if(source == browseXMLButton) {
				handleXMLBrowseButtonSelected();
			}
			updatePreferencePage();
		}

		public void modifyText(ModifyEvent evt) {
			Object source = evt.getSource();
			if(source==XMLLoc){
			}

			updatePreferencePage();
		}

		public void propertyChange(PropertyChangeEvent event) {
			if (event.getProperty().equals(FieldEditor.IS_VALID))
				updatePreferencePage();
		}
	}

	protected WidgetListener listener = new WidgetListener();

	protected Control createContents(Composite parent) 
	{
		Composite composite = new Composite(parent, SWT.NONE);
		composite.setLayout(createGridLayout(1, true, 0, 0));
		composite.setLayoutData(spanGridData(GridData.FILL_HORIZONTAL, 2));


		createTauConf(composite);
		loadSaved();
		defaultSetting();
		return composite;
	}

	/**
	 * Create the TAU options UI
	 * @param parent
	 */
	private void createTauConf(Composite parent)
	{
		Group aGroup = new Group(parent, SWT.SHADOW_ETCHED_IN);
		aGroup.setLayout(createGridLayout(1, true, 10, 10));
		aGroup.setLayoutData(spanGridData(GridData.FILL_HORIZONTAL, 2));
		aGroup.setText("Performance Tool Configuration");

		Composite xmlcom = new Composite(aGroup, SWT.NONE);
		xmlcom.setLayout(createGridLayout(3, false, 0, 0));
		xmlcom.setLayoutData(spanGridData(GridData.FILL_HORIZONTAL, 5));

		Label tauarchComment = new Label(xmlcom, SWT.WRAP);
		tauarchComment.setText("Tool Definition File:");
		XMLLoc = new Text(xmlcom, SWT.BORDER | SWT.SINGLE);
		XMLLoc.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
		XMLLoc.addModifyListener(listener);

		browseXMLButton = new Button(xmlcom,SWT.PUSH);
		browseXMLButton.setText("Browse");
		browseXMLButton.addSelectionListener(listener);
		//TODO: Implement tau-option checking
//		GridData gridData = new GridData(GridData.VERTICAL_ALIGN_END);
//		gridData.horizontalSpan = 3;
//		gridData.horizontalAlignment = GridData.FILL;
//
//		if(org.eclipse.cdt.utils.Platform.getOS().toLowerCase().trim().indexOf("aix")>=0)
//		{
//			checkAixOpts=createCheckButton(tauarch,"Automatically use Eclipse internal builder (May be needed for AIX compatibility)");
//			checkAixOpts.setLayoutData(gridData);
//			checkAixOpts.addSelectionListener(listener);
//		}
//
//		checkAutoOpts=createCheckButton(tauarch, "Check for TAU System options");
//		checkAutoOpts.setLayoutData(gridData);
//		checkAutoOpts.addSelectionListener(listener);
	}

	/**
	 * Allow user to specify a TAU arch directory.  The specified directory must contain at least one 
	 * recognizable TAU makefile in its lib sub-directory to be accepted.
	 *
	 */
	protected void handleXMLBrowseButtonSelected() 
	{
		FileDialog dialog = new FileDialog(getShell());
		File path=null;
		String correctPath = getFieldContent(XMLLoc.getText());
		if (correctPath != null) {
			path = new File(correctPath);
			if (path.exists())
				dialog.setFilterPath(path.isFile() ? correctPath : path.getParent());
		}

//		String tlpath = correctPath+File.separator+"lib";
//
//		class makefilter implements FilenameFilter{
//			public boolean accept(File dir, String name) {
//				if(name.indexOf("Makefile.tau")!=0 || name.indexOf("-pdt")<=0)
//					return false;
//				return true;
//			}
//		}
//		File[] mfiles=null;
//		makefilter mfilter = new makefilter();
//		File test = new File(tlpath);

		dialog.setText("Select tool definition xml file");
		XMLLoc.setText(dialog.open());
		//dialog.setMessage("You must select a valid TAU architecture directory.  Such a directory should be created when you configure and install TAU.  It must contain least one valid stub makefile configured with the Program Database Toolkit (pdt)");

//		String selectedPath=null;
//		while(true)
//		{
//			selectedPath = dialog.open();
//			if(selectedPath==null)
//				break;
//
//			tlpath=selectedPath+File.separator+"lib";
//			test = new File(tlpath);
//			if(test.exists()){
//				mfiles = test.listFiles(mfilter);
//			}
//			if (mfiles!=null&&mfiles.length>0)
//			{
//				if (selectedPath != null)
//					XMLLoc.setText(selectedPath);
//				break;
//			}
//		}

	}


	private void loadSaved()
	{
		Preferences preferences = Activator.getDefault().getPluginPreferences();

		XMLLoc.setText(preferences.getString(XMLLOCID));
		//TODO: Add checks
//		checkAutoOpts.setSelection(preferences.getBoolean("TAUCheckForAutoOptions"));
//		if(checkAixOpts!=null)
//			checkAixOpts.setSelection(preferences.getBoolean("TAUCheckForAIXOptions"));
	}

	public boolean performOk() 
	{
		Preferences preferences = Activator.getDefault().getPluginPreferences();

		preferences.setValue(XMLLOCID,XMLLoc.getText());
		Activator.getDefault().refreshTools();
		//TODO: Add checks
//		preferences.setValue("TAUCheckForAutoOptions", checkAutoOpts.getSelection());
//		if(checkAixOpts!=null)
//			preferences.setValue("TAUCheckForAIXOptions", checkAixOpts.getSelection());
//
//		Activator.getDefault().savePluginPreferences();
		return true;
	}

	protected Button createCheckButton(Composite parent, String label) {
		return createButton(parent, label, SWT.CHECK | SWT.LEFT);
	}
	protected Button createButton(Composite parent, String label, int type) {
		Button button = new Button(parent, type);
		button.setText(label);
		GridData data = new GridData();
		button.setLayoutData(data);
		return button;
	}

	public void init(IWorkbench workbench) 
	{
	}

	protected void defaultSetting() 
	{
	}

	public void dispose() 
	{
		super.dispose();
	}

	public void performDefaults() 
	{
		defaultSetting();
		updateApplyButton();
	}

	protected void updatePreferencePage() 
	{
		setErrorMessage(null);
		setMessage(null);

		setValid(true);
	}

	protected String getFieldContent(String text) 
	{
		if (text.trim().length() == 0 || text.equals(EMPTY_STRING))
			return null;

		return text;
	}

	protected GridLayout createGridLayout(int columns, boolean isEqual, int mh, int mw) 
	{
		GridLayout gridLayout = new GridLayout();
		gridLayout.numColumns = columns;
		gridLayout.makeColumnsEqualWidth = isEqual;
		gridLayout.marginHeight = mh;
		gridLayout.marginWidth = mw;
		return gridLayout;
	}

	protected GridData spanGridData(int style, int space) 
	{
		GridData gd = null;
		if (style == -1)
			gd = new GridData();
		else
			gd = new GridData(style);
		gd.horizontalSpan = space;
		return gd;
	}
}