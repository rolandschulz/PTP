/*******************************************************************************
 * Copyright (c) 2005 The Regents of the University of California. 
 * This material was produced under U.S. Government contract W-7405-ENG-36 
 * for Los Alamos National Laboratory, which is operated by the University 
 * of California for the U.S. Department of Energy. The U.S. Government has 
 * rights to use, reproduce, and distribute this software. NEITHER THE 
 * GOVERNMENT NOR THE UNIVERSITY MAKES ANY WARRANTY, EXPRESS OR IMPLIED, OR 
 * ASSUMES ANY LIABILITY FOR THE USE OF THIS SOFTWARE. If software is modified 
 * to produce derivative works, such modified software should be clearly marked, 
 * so as not to confuse it with the version available from LANL.
 * 
 * Additionally, this program and the accompanying materials 
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * LA-CC 04-115
 *******************************************************************************/
package org.eclipse.ptp.preferences;

import java.io.File;

import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.Preferences;
import org.eclipse.debug.internal.ui.SWTUtil;
import org.eclipse.jface.preference.FieldEditor;
import org.eclipse.jface.preference.IntegerFieldEditor;
import org.eclipse.jface.preference.PreferencePage;
import org.eclipse.jface.util.IPropertyChangeListener;
import org.eclipse.jface.util.PropertyChangeEvent;
import org.eclipse.ptp.ParallelPlugin;
import org.eclipse.ptp.core.IOutputTextFileContants;
import org.eclipse.ptp.launch.core.ILaunchManager;
import org.eclipse.ptp.ui.UIMessage;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.DirectoryDialog;
import org.eclipse.swt.widgets.FileDialog;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Text;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchPreferencePage;

public class PDTPreferencesPage extends PreferencePage implements IWorkbenchPreferencePage, IOutputTextFileContants {
    public static final String EMPTY_STRING = "";
    
    protected Text mpiPathText = null;
    protected Button browseButton1 = null;
    protected IntegerFieldEditor requestTimeoutField = null;
    protected IntegerFieldEditor launchTimoutField = null;
    
    protected Text outputDirText = null;
    protected Button browseButton2 = null;
    protected IntegerFieldEditor storeLineField = null;

    private String mpiFile = EMPTY_STRING;
    /*
    private int requestTimeout = IMIConstants.DEF_REQUEST_TIMEOUT;
    private int launchTimeout = IMIConstants.DEF_REQUEST_LAUNCH_TIMEOUT;
    */

    private String outputDIR = EMPTY_STRING;
    private int storeLine = DEF_STORE_LINE;
    
    public PDTPreferencesPage() {
        setPreferenceStore(ParallelPlugin.getDefault().getPreferenceStore());
        //setDescription(UIMessage.getResourceString("PDTPreferencesPage.preferencesDescription"));
    }
       
    protected class WidgetListener extends SelectionAdapter implements ModifyListener, IPropertyChangeListener {
	    public void widgetSelected(SelectionEvent e) {
	        Object source = e.getSource();
	        if (source == browseButton1)
	            handleMPIPathBrowseButtonSelected();
	        else if (source == browseButton2)
	            handleOutputDirectoryBrowseButtonSelected();
	        else
	            updatePreferencePage();
	    }
		public void modifyText(ModifyEvent evt) {
		    updatePreferencePage();
		}        
        public void propertyChange(PropertyChangeEvent event) {
        	if (event.getProperty().equals(FieldEditor.IS_VALID))
        		updatePreferencePage();
        }		
    }
    
    protected WidgetListener listener = new WidgetListener();
    
    protected Control createContents(Composite parent) {
        Composite composite = new Composite(parent, SWT.NONE);
        composite.setLayout(createGridLayout(1, true, 0, 0));
        composite.setLayoutData(spanGridData(GridData.FILL_HORIZONTAL, 2));
        
        createMPICTRLContents(composite);
        createOutputContents(composite);
        
		defaultSetting();
		return composite;
    }
    
    private void createOutputContents(Composite parent) {
		Group aGroup = new Group(parent, SWT.SHADOW_ETCHED_IN);
		aGroup.setLayout(createGridLayout(1, true, 10, 10));
		aGroup.setLayoutData(spanGridData(GridData.FILL_HORIZONTAL, 2));
		aGroup.setText(UIMessage.getResourceString("PDTPreferencesPage.group_output"));			

		Composite outputComposite = new Composite(aGroup, SWT.NONE);
		outputComposite.setLayout(createGridLayout(3, false, 0, 0));
		outputComposite.setLayoutData(spanGridData(GridData.FILL_HORIZONTAL, 5));

		new Label(outputComposite, SWT.NONE).setText(UIMessage.getResourceString("PDTPreferencesPage.output_text"));
		outputDirText = new Text(outputComposite, SWT.SINGLE | SWT.BORDER);
		outputDirText.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
		outputDirText.addModifyListener(listener);
		browseButton2 = SWTUtil.createPushButton(outputComposite, UIMessage.getResourceString("PDTPreferencesPage.browseButton"), null);
		browseButton2.addSelectionListener(listener);

		Composite lineComposite = new Composite(aGroup, SWT.NONE);
		lineComposite.setLayout(new FillLayout());
		lineComposite.setLayoutData(spanGridData(GridData.FILL_HORIZONTAL, 5));

		storeLineField = new IntegerFieldEditor(STORE_LINE, UIMessage.getResourceString("PDTPreferencesPage.store_line_text"), lineComposite);
		storeLineField.setPropertyChangeListener(listener);
		storeLineField.setEmptyStringAllowed(false);		
    }
    
    private void createMPICTRLContents(Composite parent) {
		Group aGroup = new Group(parent, SWT.SHADOW_ETCHED_IN);
		aGroup.setLayout(createGridLayout(1, true, 10, 10));
		aGroup.setLayoutData(spanGridData(GridData.FILL_HORIZONTAL, 2));
		aGroup.setText(UIMessage.getResourceString("PDTPreferencesPage.group_mpictrl"));			
        
		Composite mpiFilecomposite = new Composite(aGroup, SWT.NONE);
		mpiFilecomposite.setLayout(createGridLayout(3, false, 0, 0));
		mpiFilecomposite.setLayoutData(spanGridData(GridData.FILL_HORIZONTAL, 5));
		
		new Label(mpiFilecomposite, SWT.NONE).setText(UIMessage.getResourceString("PDTPreferencesPage.mpiFile_text"));
		mpiPathText = new Text(mpiFilecomposite, SWT.SINGLE | SWT.BORDER);
		mpiPathText.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
		mpiPathText.addModifyListener(listener);
		browseButton1 = SWTUtil.createPushButton(mpiFilecomposite, UIMessage.getResourceString("PDTPreferencesPage.browseButton"), null);
		browseButton1.addSelectionListener(listener);

		Composite timeoutComposite = new Composite(aGroup, SWT.NONE);
		timeoutComposite.setLayout(new FillLayout());
		timeoutComposite.setLayoutData(spanGridData(GridData.FILL_HORIZONTAL, 5));

		/*
		requestTimeoutField = new IntegerFieldEditor(IMIConstants.PREF_REQUEST_TIMEOUT, UIMessage.getResourceString("PDTPreferencesPage.Request_timeout_text"), timeoutComposite);
		requestTimeoutField.setPropertyChangeListener(listener);
		requestTimeoutField.setEmptyStringAllowed(false);
		
		launchTimoutField = new IntegerFieldEditor(IMIConstants.PREF_REQUEST_LAUNCH_TIMEOUT, UIMessage.getResourceString("PDTPreferencesPage.Launch_timeout_text"), timeoutComposite);
		launchTimoutField.setPropertyChangeListener(listener);
		launchTimoutField.setEmptyStringAllowed(false);
		*/        
    }

    protected void defaultSetting() {
        mpiPathText.setText(mpiFile);
        /*
        requestTimeoutField.setStringValue(String.valueOf(requestTimeout));
        launchTimoutField.setStringValue(String.valueOf(launchTimeout));
        */
        
	    outputDirText.setText(outputDIR);
	    storeLineField.setStringValue(String.valueOf(storeLine));
    }
    
    public void init(IWorkbench workbench) {
        //IPreferenceStore store = getPreferenceStore();
    	
    	Preferences preferences = ParallelPlugin.getDefault().getPluginPreferences();
    	/*
        mpiFile = preferences.getString(IMIConstants.PREF_MPICTRL_LOCATION);
        
        requestTimeout = preferences.getInt(IMIConstants.PREF_REQUEST_TIMEOUT);
        if (requestTimeout == 0)
            requestTimeout = IMIConstants.DEF_REQUEST_TIMEOUT;

        launchTimeout = preferences.getInt(IMIConstants.PREF_REQUEST_LAUNCH_TIMEOUT);
        if (launchTimeout == 0)
            launchTimeout = IMIConstants.DEF_REQUEST_LAUNCH_TIMEOUT;

        outputDIR = getFieldContent(preferences.getString(OUTPUT_DIR));
        if (outputDIR == null)
            outputDIR = ResourcesPlugin.getWorkspace().getRoot().getLocation().append(DEF_OUTPUT_DIR_NAME).toOSString();
            
        storeLine = preferences.getInt(STORE_LINE);
        if (storeLine == 0)
            storeLine = DEF_STORE_LINE;
            */    
    }
    
    public void dispose() {
        super.dispose();
    }
    
	public void performDefaults() {
	    defaultSetting();
		updateApplyButton();
	}
	
	private void store() {
	    mpiFile = mpiPathText.getText();
	    /*
	    requestTimeout = requestTimeoutField.getIntValue();
	    launchTimeout = launchTimoutField.getIntValue();
	    */
	    
	    outputDIR = outputDirText.getText();
	    storeLine = storeLineField.getIntValue();
	}
	
    public boolean performOk() {
        store();
        Preferences preferences = ParallelPlugin.getDefault().getPluginPreferences();
        /*
        preferences.setValue(IMIConstants.PREF_MPICTRL_LOCATION, mpiFile);
        preferences.setValue(IMIConstants.PREF_REQUEST_TIMEOUT, requestTimeout);
        preferences.setValue(IMIConstants.PREF_REQUEST_LAUNCH_TIMEOUT, launchTimeout);
        */
        
        preferences.setValue(OUTPUT_DIR, outputDIR);
        preferences.setValue(STORE_LINE, storeLine);

        /*
        IPreferenceStore store = getPreferenceStore();
        store.setValue(MPI_PATH, mpiFile);
        store.setValue(REQUEST_TIMEOUT, requestTimeout);
        store.setValue(LAUNCH_TIMEOUT, launchTimeout);
        */
        
        ParallelPlugin.getDefault().savePluginPreferences();
        
        ILaunchManager manager = ParallelPlugin.getDefault().getLaunchManager();
        if (!manager.isMPIRuning() && manager.isParallelPerspectiveOpen()) {
        	try {
        		manager.createMPISession();
            } catch (CoreException e) {
                System.out.println("Cannot creation MPI session: " + e.getMessage());
            }
        }
        
        File outputDirPath = new File(outputDIR);
        if (!outputDirPath.exists())
            outputDirPath.mkdir();
                
        return true;
    }
        
	/**
	 * Show a dialog that lets the user select a file
	 */
	protected void handleMPIPathBrowseButtonSelected() {
		FileDialog dialog = new FileDialog(getShell());
		dialog.setText(UIMessage.getResourceString("PDTPreferencesPage.Select_MPI_FILE"));
		String currectMPIPath = getFieldContent(mpiPathText.getText());
		if (currectMPIPath != null) {
			File path = new File(currectMPIPath);
			if (path.exists())
				dialog.setFilterPath(path.isFile()?currectMPIPath:path.getParent());
		}
		
		String selectedMPIPath = dialog.open();
		if (selectedMPIPath != null)
		    mpiPathText.setText(selectedMPIPath);
	}

	protected void handleOutputDirectoryBrowseButtonSelected() {
	    DirectoryDialog dialog = new DirectoryDialog(getShell());
		dialog.setText(UIMessage.getResourceString("PDTPreferencesPage.Select_Output_Directory"));
		String currectDirPath = getFieldContent(outputDirText.getText());
		if (currectDirPath != null) {
			File path = new File(currectDirPath);
			if (path.exists())
				dialog.setFilterPath(currectDirPath);
		}
		
		String selectedDirPath = dialog.open();
		if (selectedDirPath != null)
		    outputDirText.setText(selectedDirPath);
	}
	
	protected boolean isValidMPISetting() {
		String name = getFieldContent(mpiPathText.getText());
		if (name == null) {
			setErrorMessage(UIMessage.getResourceString("PDTPreferencesPage.Incorrect_MPI_file"));
			setValid(false);
			return false;
		}
		
		File path = new File(name);
		if (!path.exists() || !path.isFile()) {
			setErrorMessage(UIMessage.getResourceString("PDTPreferencesPage.Incorrect_MPI_file"));
			setValid(false);
			return false;
		}
		
        if (!requestTimeoutField.isValid()) {
            setErrorMessage(requestTimeoutField.getErrorMessage());
			setValid(false);
			return false;
        }

        if (!launchTimoutField.isValid()) {
            setErrorMessage(launchTimoutField.getErrorMessage());
			setValid(false);
			return false;
        }
        
        return true;
	}

	protected boolean isValidOutputSetting() {
		String name = getFieldContent(outputDirText.getText());
		if (name == null) {
			setErrorMessage(UIMessage.getResourceString("PDTPreferencesPage.Incorrect_Output_directory"));
			setValid(false);
			return false;
		}
		
		File path = new File(name);
		if (!path.exists()) {
		    File parent = path.getParentFile();
		    if (parent == null || !parent.exists()) {
				setErrorMessage(UIMessage.getResourceString("PDTPreferencesPage.Incorrect_Output_directory"));
				setValid(false);
				return false;
			}
		}

	    if (!storeLineField.isValid()) {
            setErrorMessage(storeLineField.getErrorMessage());
			setValid(false);
			return false;
        }
        
	    return true;
	}
	
	protected void updatePreferencePage() {
		setErrorMessage(null);
		setMessage(null);

		if (!isValidMPISetting())
		    return;
		
		if (!isValidOutputSetting())
		    return;		
		
        setValid(true);
	}
	
    protected String getFieldContent(String text) {
        if (text.trim().length() == 0 || text.equals(EMPTY_STRING))
            return null;
        
        return text;
    }	
       
    protected GridLayout createGridLayout(int columns, boolean isEqual, int mh, int mw) {
        GridLayout gridLayout = new GridLayout();
        gridLayout.numColumns = columns;
        gridLayout.makeColumnsEqualWidth = isEqual;
        gridLayout.marginHeight = mh;
        gridLayout.marginWidth = mw;
        return gridLayout;
    }

    protected GridData spanGridData(int style, int space) {
        GridData gd = null;
        if (style == -1)
            gd = new GridData();
        else
            gd = new GridData(style);
        gd.horizontalSpan = space;
        return gd;
    }      
}
