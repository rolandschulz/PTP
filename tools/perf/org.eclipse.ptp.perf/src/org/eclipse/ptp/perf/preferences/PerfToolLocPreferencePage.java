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
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;
import org.eclipse.core.runtime.Preferences;
import org.eclipse.jface.preference.FieldEditor;
import org.eclipse.jface.preference.PreferencePage;
import org.eclipse.jface.util.IPropertyChangeListener;
import org.eclipse.jface.util.PropertyChangeEvent;
import org.eclipse.ptp.perf.Activator;
import org.eclipse.ptp.perf.IPerformanceLaunchConfigurationConstants;
import org.eclipse.ptp.perf.toolopts.PerformanceTool;
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
import org.eclipse.swt.widgets.DirectoryDialog;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Text;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchPreferencePage;

/**
 * Provides a user-interface for and managed workspace-wide TAU settings.
 * The location of the local TAU installation is the most critical of these
 * @author wspear
 *
 */
public class PerfToolLocPreferencePage extends PreferencePage implements IWorkbenchPreferencePage 
{
	
	private class BinDirPanel
	{
		String group="";
		Button browseBinButton=null;
		Text binDir=null;
		BinListener binLis=new BinListener();
		
		
		
		protected class BinListener extends SelectionAdapter implements ModifyListener, IPropertyChangeListener
		{
			public void widgetSelected(SelectionEvent e) {
				Object source = e.getSource();
				if(source == browseBinButton) {
					handleBinBrowseButtonSelected(binDir,group);
				}
				updatePreferencePage();
			}

			public void modifyText(ModifyEvent evt) {
				Object source = evt.getSource();
				if(source==binDir){
				}

				updatePreferencePage();
			}

			public void propertyChange(PropertyChangeEvent event) {
				if (event.getProperty().equals(FieldEditor.IS_VALID))
					updatePreferencePage();
			}
		}
		
		private void makeToolBinPane(Composite parent)
		{
			Composite tauarch = new Composite(parent, SWT.NONE);
			tauarch.setLayout(createGridLayout(3, false, 0, 0));
			tauarch.setLayoutData(spanGridData(GridData.FILL_HORIZONTAL, 5));
			
			Label taubinComment = new Label(tauarch, SWT.WRAP);
			taubinComment.setText(group+" Bin Directory:");
			binDir = new Text(tauarch, SWT.BORDER | SWT.SINGLE);
			binDir.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
			binDir.addModifyListener(binLis);

			browseBinButton = new Button(tauarch,SWT.PUSH);
			browseBinButton.setText("Browse");
			browseBinButton.addSelectionListener(binLis);
		}

		public BinDirPanel(String group) {
			this.group = group;
		}
	}
	
	public static final String EMPTY_STRING = "";
	
	BinDirPanel[] toolGroups=null;
	
	//protected Text tauBin = null;
	//protected Button browseBinButton = null;
	

	public PerfToolLocPreferencePage() {
		setPreferenceStore(Activator.getDefault().getPreferenceStore());
		Iterator<Map.Entry<String,String>> eIt = null;
		String me = null;
		PerformanceTool[] tools=Activator.getTools();
		Set<String> groups = new LinkedHashSet<String>();
		for (int i = 0; i < tools.length; i++) 
		{
			eIt = tools[i].groupApp.entrySet().iterator();
			while (eIt.hasNext()) 
			{
				me = (eIt.next()).getKey().toString();
				if(!me.equals("internal"))
					groups.add(me);
			}
		}
		
		toolGroups=new BinDirPanel[groups.size()];
		Iterator<String> gIt=groups.iterator();
		int i=0;
		while(gIt.hasNext())
		{
			toolGroups[i]=new BinDirPanel(gIt.next());
			i++;
		}
	}

	protected class WidgetListener extends SelectionAdapter implements ModifyListener, IPropertyChangeListener
	{
		public void widgetSelected(SelectionEvent e) {
//			Object source = e.getSource();
//			if(source == browseBinButton) {
//				handleBinBrowseButtonSelected();
//			}
			updatePreferencePage();
		}

		public void modifyText(ModifyEvent evt) {
//			Object source = evt.getSource();
//			if(source==tauBin){
//			}

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
		aGroup.setText("Tool Configuration");

		if(toolGroups!=null)
		for(int i=0;i<toolGroups.length;i++)
			toolGroups[i].makeToolBinPane(aGroup);
		
	}
	


	/**
	 * Allow user to specify a TAU bin directory.  
	 *
	 */
	protected void handleBinBrowseButtonSelected(Text field, String group) 
	{
		DirectoryDialog dialog = new DirectoryDialog(getShell());
		File path=null;
		String correctPath = getFieldContent(field.getText());
		if (correctPath != null) {
			path = new File(correctPath);
			if (path.exists())
				dialog.setFilterPath(path.isFile() ? correctPath : path.getParent());
		}
//The specified directory previously had to contain at least one recognizable TAU makefile in its lib sub-directory to be accepted.
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

		dialog.setText("Select "+group+" Bin Directory");
		//dialog.setMessage("You must select a valid TAU bin directory.  Such a directory should be created when you configure and install TAU.  It should contain least one valid stub makefile configured with the Program Database Toolkit (pdt)");

		String selectedPath=dialog.open();//null;
		if(selectedPath!=null)
			field.setText(selectedPath);
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
//					tauBin.setText(selectedPath);
//				break;
//			}
//		}

	}


	private void loadSaved()
	{
		Preferences preferences = Activator.getDefault().getPluginPreferences();

		if(toolGroups!=null)
			for(int i=0;i<toolGroups.length;i++)
			{
				toolGroups[i].binDir.setText(preferences.getString(IPerformanceLaunchConfigurationConstants.TOOL_BIN_ID+"."+toolGroups[i].group));// ITAULaunchConfigurationConstants.TAU_BIN_PATH));
			}

	}

	public boolean performOk() 
	{
		Preferences preferences = Activator.getDefault().getPluginPreferences();

		if(toolGroups!=null)
			for(int i=0;i<toolGroups.length;i++)
			{
				preferences.setValue(IPerformanceLaunchConfigurationConstants.TOOL_BIN_ID+"."+toolGroups[i].group,toolGroups[i].binDir.getText());
			}

		Activator.getDefault().savePluginPreferences();
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