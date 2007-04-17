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
package org.eclipse.ptp.tau.options;

import java.io.File;
import java.io.FilenameFilter;

import org.eclipse.core.runtime.Preferences;
import org.eclipse.jface.preference.FieldEditor;
import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.jface.preference.PreferencePage;
import org.eclipse.jface.util.IPropertyChangeListener;
import org.eclipse.jface.util.PropertyChangeEvent;
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
import org.eclipse.ui.PlatformUI;

public class TAUPreferencePage extends PreferencePage implements IWorkbenchPreferencePage 
{
	public static final String EMPTY_STRING = "";
	protected Text tauArch = null;
	//Tau makefile location
	//protected Text tauMake = null;
	//Tau compiler options
	//protected Text tauOpt = null;
	protected Button browseArchButton = null;
	//protected Button browseMakeButton = null;
	//Tau makefile selection
	//protected Combo tcombo = null;
	
	
	//protected Button paraprofButton=null;
	//protected Button altOutButton=null;
	//protected Button browseOutButton=null;
	//Tau output directory
	//protected Text tauOut = null;

	public TAUPreferencePage() {
		setPreferenceStore(TAUOptionsPlugin.getDefault().getPreferenceStore());
	}

	protected class WidgetListener extends SelectionAdapter implements ModifyListener, IPropertyChangeListener
	{
		public void widgetSelected(SelectionEvent e) {
			Object source = e.getSource();
			if(source == browseArchButton) {
				handleArchBrowseButtonSelected();
			}
			/*else
			if(source == browseMakeButton) {
				handleMakeBrowseButtonSelected();
			}
			else
			if(source == browseOutButton) {
				handleOutBrowseButtonSelected();
			}*/
			updatePreferencePage();
		}

		public void modifyText(ModifyEvent evt) {
			Object source = evt.getSource();
			if(source==tauArch){
				//setupMakefiles();
				//tcombo.select(0);
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
		//createParaOpts(composite);
		//createPerfOutput(composite);
		
		loadSaved();
		defaultSetting();
		return composite;
	}
	
	private void createTauConf(Composite parent)
	{
		Group aGroup = new Group(parent, SWT.SHADOW_ETCHED_IN);
		aGroup.setLayout(createGridLayout(1, true, 10, 10));
		aGroup.setLayoutData(spanGridData(GridData.FILL_HORIZONTAL, 2));
		aGroup.setText("TAU Configuration");
		
		Composite tauarch = new Composite(aGroup, SWT.NONE);
		tauarch.setLayout(createGridLayout(3, false, 0, 0));
		tauarch.setLayoutData(spanGridData(GridData.FILL_HORIZONTAL, 5));
		
		Label tauarchComment = new Label(tauarch, SWT.WRAP);
		tauarchComment.setText("TAU Arch Directory:");
		tauArch = new Text(tauarch, SWT.BORDER | SWT.SINGLE);
		tauArch.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
		tauArch.addModifyListener(listener);
		
		browseArchButton = new Button(tauarch,SWT.PUSH);//SWTUtil.createPushButton(tauarch, "Browse", null);
		browseArchButton.setText("Browse");
		browseArchButton.addSelectionListener(listener);
	}
	
	protected void handleArchBrowseButtonSelected() 
	{
		
		
		
		DirectoryDialog dialog = new DirectoryDialog(getShell());
		//dialog.setText("Select TAU Arch Directory");
		File path=null;
		String correctPath = getFieldContent(tauArch.getText());
		if (correctPath != null) {
			path = new File(correctPath);
			if (path.exists())
				dialog.setFilterPath(path.isFile() ? correctPath : path.getParent());
		}

		
		
		//IPreferenceStore pstore = TAUOptionsPlugin.getDefault().getPreferenceStore();
		//String archpath=pstore.getString("TAUCDTArchPath");
		String tlpath = correctPath+File.separator+"lib";
		//File taulib = new File(tlpath);
		
		class makefilter implements FilenameFilter{
			public boolean accept(File dir, String name) {
				if(name.indexOf("Makefile.tau")!=0 || name.indexOf("-pdt")<=0)//|| name.indexOf("-pdt")<=0 || name.indexOf("-mpi")<=0 
					return false;
				return true;
			}
		}
		File[] mfiles=null;
		makefilter mfilter = new makefilter();
		//if(taulib.exists()){
		//	mfiles = taulib.listFiles(mfilter);
		//}
		//if(mfiles==null||mfiles.length==0)
		//{
//			/mnt/netapp/home/users/wspear/tau2/x86_64
			File test = new File(tlpath);
			//System.out.println(tlpath);
			
				//System.out.println("Error: Please specify a valid TAU arch directory");
				//DirectoryDialog dialog = new DirectoryDialog(PlatformUI.getWorkbench().getDisplay().getActiveShell());
				dialog.setText("Select TAU Arch Directory");
				dialog.setMessage("You must select a valid TAU architecture directory.  Such a directory should be created when you configure and install TAU.  It must contain least one valid stub makefile configured with the Program Database Toolkit (pdt)");
				//if (correctPath != null) 
				//{
					//File path = new File(archpath);
					//if (path.exists())
						//dialog.setFilterPath(path.isFile() ? correctPath : path.getParent());
				
					/*
					String correctPath = getFieldContent(tauArch.getText());
				
					}*/
					String selectedPath=null;
					while(true)
					{
						selectedPath = dialog.open();
						if(selectedPath==null)
							break;
							//throw new FileNotFoundException("Invalid TAU Arch Directory");
					
						tlpath=selectedPath+File.separator+"lib";
						test = new File(tlpath);
						if(test.exists()){
							mfiles = test.listFiles(mfilter);
						}
						if (mfiles!=null&&mfiles.length>0)
						{
							//selectedPath = dialog.open();
							if (selectedPath != null)
								tauArch.setText(selectedPath);
							//pstore.setValue("TAUCDTArchPath", selectedPath);
							break;
						}
					}
				//}
			//}
	}
	
	
	private void loadSaved()
	{
		Preferences preferences = TAUOptionsPlugin.getDefault().getPluginPreferences();
		
		tauArch.setText(preferences.getString("TAUCDTArchPath"));
	}

	public boolean performOk() 
	{
		Preferences preferences = TAUOptionsPlugin.getDefault().getPluginPreferences();

		preferences.setValue("TAUCDTArchPath",tauArch.getText());
		
		TAUOptionsPlugin.getDefault().savePluginPreferences();
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