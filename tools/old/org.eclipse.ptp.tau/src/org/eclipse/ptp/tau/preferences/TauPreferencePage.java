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

package org.eclipse.ptp.tau.preferences;

import java.io.File;
import java.io.FilenameFilter;

//import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.Preferences;
import org.eclipse.debug.internal.ui.SWTUtil;
//import org.eclipse.jface.dialogs.ErrorDialog;
import org.eclipse.jface.preference.FieldEditor;
import org.eclipse.jface.preference.PreferencePage;
import org.eclipse.jface.util.IPropertyChangeListener;
import org.eclipse.jface.util.PropertyChangeEvent;
//import org.eclipse.ptp.core.IModelManager;
//import org.eclipse.ptp.core.PTPCorePlugin;
//import org.eclipse.ptp.core.PreferenceConstants;
//import org.eclipse.ptp.internal.core.CoreMessages;
//import org.eclipse.ptp.rtsystem.simulation.SimulationControlSystem;
//import org.eclipse.ptp.rtsystem.simulation.SimulationMonitoringSystem;

//import org.eclipse.ptp.internal.core.CoreMessages;
import org.eclipse.ptp.tau.TauPlugin;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
//import org.eclipse.swt.events.VerifyEvent;
//import org.eclipse.swt.events.VerifyListener;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.DirectoryDialog;
import org.eclipse.swt.widgets.FileDialog;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Spinner;
import org.eclipse.swt.widgets.Text;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchPreferencePage;

public class TauPreferencePage extends PreferencePage implements IWorkbenchPreferencePage 
{
	public static final String EMPTY_STRING = "";
	
	//Number of processes
	protected Spinner spin = null;
	
	//Tau arch directory
	protected Text tauArch = null;
	//Tau makefile location
	protected Text tauMake = null;
	//Tau compiler options
	protected Text tauOpt = null;
	protected Button browseArchButton = null;
	protected Button browseMakeButton = null;
	//Tau makefile selection
	protected Combo tcombo = null;
	
	
	protected Button paraprofButton=null;
	protected Button altOutButton=null;
	protected Button browseOutButton=null;
	//Tau output directory
	protected Text tauOut = null;

	public TauPreferencePage() {
		setPreferenceStore(TauPlugin.getDefault().getPreferenceStore());
	}

	protected class WidgetListener extends SelectionAdapter implements ModifyListener, IPropertyChangeListener
	{
		public void widgetSelected(SelectionEvent e) {
			Object source = e.getSource();
			if(source == browseArchButton) {
				handleArchBrowseButtonSelected();
			}
			else
			if(source == browseMakeButton) {
				handleMakeBrowseButtonSelected();
			}
			else
			if(source == browseOutButton) {
				handleOutBrowseButtonSelected();
			}
			updatePreferencePage();
		}

		public void modifyText(ModifyEvent evt) {
			Object source = evt.getSource();
			if(source==tauArch){
				//System.out.println("TauArchMod!");
				setupMakefiles();
				tcombo.select(0);
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
		createParaOpts(composite);
		createPerfOutput(composite);
		
		loadSaved();
		defaultSetting();
		return composite;
	}
	
	private void createPerfOutput(Composite parent) {
		Group aGroup = new Group(parent, SWT.SHADOW_ETCHED_IN);
		aGroup.setLayout(createGridLayout(3, false, 10, 10));
		aGroup.setLayoutData(spanGridData(GridData.FILL_HORIZONTAL, 2));
		aGroup.setText("Performance Data Output");//CoreMessages.getResourceString
		
		//new Label(aGroup, SWT.NONE).setText(("Select alternate output root:"));//CoreMessages.getResourceString
		
		Composite tauout = new Composite(aGroup, SWT.NONE);
		tauout.setLayout(createGridLayout(3, false, 0, 0));
		tauout.setLayoutData(spanGridData(GridData.FILL_HORIZONTAL, 5));
		
		altOutButton=createCheckButton(tauout, "Use alternate output directory:");
		//altOutButton.setEnabled(false);
		tauOut = new Text(tauout, SWT.BORDER | SWT.SINGLE);
		tauOut.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
		browseOutButton = SWTUtil.createPushButton(tauout, "Browse", null);
		browseOutButton.addSelectionListener(listener);
		paraprofButton=createCheckButton(aGroup, "Automatically run ParaProf on profile output");
		//createButton(parent, "", SWT.CHECK | SWT.LEFT);
	}

	private void createTauConf(Composite parent)
	{
		Group aGroup = new Group(parent, SWT.SHADOW_ETCHED_IN);
		aGroup.setLayout(createGridLayout(1, true, 10, 10));
		aGroup.setLayoutData(spanGridData(GridData.FILL_HORIZONTAL, 2));
		aGroup.setText("TAU Configuration");//CoreMessages.getResourceString
		
		Composite tauarch = new Composite(aGroup, SWT.NONE);
		tauarch.setLayout(createGridLayout(3, false, 0, 0));
		tauarch.setLayoutData(spanGridData(GridData.FILL_HORIZONTAL, 5));
		
		Label tauarchComment = new Label(tauarch, SWT.WRAP);
		tauarchComment.setText("TAU Arch Directory:");
		tauArch = new Text(tauarch, SWT.BORDER | SWT.SINGLE);
		tauArch.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));//   spanGridData(GridData.FILL_HORIZONTAL, 2)
		tauArch.addModifyListener(listener);
		
		browseArchButton = SWTUtil.createPushButton(tauarch, "Browse", null);
		browseArchButton.addSelectionListener(listener);
		
		Composite taucomcom = new Composite(aGroup, SWT.NONE);
		taucomcom.setLayout(createGridLayout(3, false, 0, 0));
		taucomcom.setLayoutData(spanGridData(GridData.FILL_HORIZONTAL, 5));		
		
		new Label(taucomcom, SWT.NONE).setText("Select a makefile:");
		tcombo = new Combo(taucomcom, SWT.READ_ONLY);
		tcombo.addSelectionListener(listener);
		setupMakefiles();
		tcombo.select(0);
		
		Composite taumake = new Composite(aGroup, SWT.NONE);
		taumake.setLayout(createGridLayout(3, false, 0, 0));
		taumake.setLayoutData(spanGridData(GridData.FILL_HORIZONTAL, 5));
		
		Label taumakeComment = new Label(taumake, SWT.WRAP);
		taumakeComment.setText("TAU Makefile:");
		tauMake = new Text(taumake, SWT.BORDER | SWT.SINGLE);
		tauMake.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));//   spanGridData(GridData.FILL_HORIZONTAL, 2)
		//tau.addModifyListener(listener);
		
		browseMakeButton = SWTUtil.createPushButton(taumake, "Browse", null);
		browseMakeButton.addSelectionListener(listener);
		
		Composite tauoptcom = new Composite(aGroup, SWT.NONE);
		tauoptcom.setLayout(createGridLayout(3, false, 0, 0));
		tauoptcom.setLayoutData(spanGridData(GridData.FILL_HORIZONTAL, 5));
		
		Label tauoptComment = new Label(tauoptcom, SWT.WRAP);
		tauoptComment.setText("TAU Options:");
		tauOpt=new Text(tauoptcom, SWT.BORDER | SWT.SINGLE);
		tauOpt.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
	}
	
	private void createParaOpts(Composite parent)
	{
		Group bGroup = new Group(parent, SWT.SHADOW_ETCHED_IN);
		bGroup.setLayout(createGridLayout(1, true, 10, 10));
		bGroup.setLayoutData(spanGridData(GridData.FILL_HORIZONTAL | GridData.GRAB_HORIZONTAL, 2));
		bGroup.setText("Parallell Settings");//CoreMessages.getResourceString
		
		new Label(bGroup, SWT.NONE).setText("Set number of nodes on TAU run:");
		
		spin = new Spinner(bGroup, SWT.READ_ONLY);
		spin.setMinimum(1);
		spin.addModifyListener(listener);
	}
	
	protected void setupMakefiles()
	{		
		tcombo.removeAll();
		tcombo.add("Specify Makefile Manually");
		
		class makefilter implements FilenameFilter{
			public boolean accept(File dir, String name) {
				if(name.indexOf("Makefile.tau")==0){return true;}
				else{return false;}
			}
		}

		int nummakes=0;
		File[] makefiles = null;
		File dir = new File(tauArch.getText()+File.separator+"lib");

		if(dir.exists()){
		makefiles = dir.listFiles(new makefilter());
		}
		if(makefiles==null){nummakes=0;}
		else{nummakes=makefiles.length;}
		
		for(int i=0;i<nummakes;i++)
		{
			tcombo.add(makefiles[i].getName());
			//System.out.println(makefiles[i].getName());
		}
		tcombo.pack();
	}

	protected void handleArchBrowseButtonSelected() 
	{
		DirectoryDialog dialog = new DirectoryDialog(getShell());
		dialog.setText("Select TAU Arch Directory");
		
		String correctPath = getFieldContent(tauArch.getText());
		if (correctPath != null) {
			File path = new File(correctPath);
			if (path.exists())
				dialog.setFilterPath(path.isFile() ? correctPath : path
						.getParent());
		}

		String selectedPath = dialog.open();
		if (selectedPath != null)
			tauArch.setText(selectedPath);
	}
	
	protected void handleOutBrowseButtonSelected() 
	{
		DirectoryDialog dialog = new DirectoryDialog(getShell());
		dialog.setText("Select TAU Output Directory");
		
		String correctPath = getFieldContent(tauOut.getText());
		if (correctPath != null) {
			File path = new File(correctPath);
			if (path.exists())
				dialog.setFilterPath(path.isFile() ? correctPath : path
						.getParent());
		}

		String selectedPath = dialog.open();
		if (selectedPath != null)
			tauOut.setText(selectedPath);
	}
	
	protected void handleMakeBrowseButtonSelected() 
	{
		FileDialog dialog = new FileDialog(getShell());
		dialog.setText("Select TAU Makefile");
		
		String correctPath = getFieldContent(tauMake.getText());
		if (correctPath != null) {
			File path = new File(correctPath);
			if (path.exists())
				dialog.setFilterPath(path.isFile() ? correctPath : path
						.getParent());
		}

		String selectedPath = dialog.open();
		if (selectedPath != null)
			tauMake.setText(selectedPath);
	}
	
	private void loadSaved()
	{
		Preferences preferences = TauPlugin.getDefault().getPluginPreferences();
		
		int numMachines = preferences.getInt("numProc");
		if(numMachines < 1) numMachines = 1;
		spin.setSelection(numMachines);
		
		tauArch.setText(preferences.getString("TAUCDTArchPath"));
		
		tauMake.setText(preferences.getString("TAUCDTMakefile"));
		
		tauOpt.setText(preferences.getString("TAUCDTOpts"));
		setupMakefiles();
		//Preferences preferences = TauPlugin.getDefault().getPluginPreferences();
		int tcdex=tcombo.indexOf(preferences.getString("makeCombo"));
		if(tcdex<0)tcdex=0;
		tcombo.select(tcdex);
		paraprofButton.setSelection(preferences.getBoolean("runParaProf"));
		altOutButton.setSelection(preferences.getBoolean("defaultOutputRoot"));
		tauOut.setText(preferences.getString("outputRoot"));
		/*if(makeDexHold>0&&makeDexHold<tcombo.getItemCount())
		{
			tcdex=makeDexHold;
		}*/
		//System.out.println(preferences.getString("makeCombo")+" got, using "+tcdex+" of "+tcombo.getItemCount());
		
		//makeDexHold=preferences.getInt("makeDex");
	}

	public boolean performOk() 
	{
		Preferences preferences = TauPlugin.getDefault().getPluginPreferences();
		
		preferences.setValue("numProc", spin.getSelection());

		preferences.setValue("TAUCDTArchPath",tauArch.getText());
		
		preferences.setValue("TAUCDTMakefile",tauMake.getText());
		
		preferences.setValue("makeCombo",tcombo.getText());
		//preferences.setValue("makeDex",tcombo.getSelectionIndex());
		//System.out.println("saving "+tcombo.getText()+" from "+tcombo.getSelectionIndex());
		preferences.setValue("TAUCDTOpts",tauOpt.getText());
		
		preferences.setValue("runParaProf",paraprofButton.getSelection());
		preferences.setValue("defaultOutputRoot",altOutButton.getSelection());
		preferences.setValue("outputRoot",tauOut.getText());
		
		TauPlugin.getDefault().savePluginPreferences();
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