/*******************************************************************************
 * Copyright (c) 2000, 2004 QNX Software Systems and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     QNX Software Systems - Initial API and implementation
 *******************************************************************************/
package org.eclipse.ptp.debug.external.internal.ui;

import java.util.Observable;
import java.util.Observer;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.debug.core.ILaunchConfiguration;
import org.eclipse.debug.core.ILaunchConfigurationWorkingCopy;
import org.eclipse.debug.ui.AbstractLaunchConfigurationTab;
import org.eclipse.ptp.core.IPTPLaunchConfigurationConstants;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;

/**
 * The dynamic tab for gdb-based debugger implementations.
 */
public class SDMPage extends AbstractLaunchConfigurationTab implements Observer {

	protected Text fExePathText;
	protected Button fExePathButton = null;
	protected Text fCWDText;
	protected Button fCWDButton = null;
	
	protected static final String EMPTY_STRING = ""; //$NON-NLS-1$

	private boolean fIsInitializing = false;
	
	public void createControl(Composite parent) {
		Composite comp = new Composite(parent, SWT.NONE);
		comp.setLayout(new GridLayout());
		comp.setLayoutData(new GridData(GridData.FILL_BOTH));
		
		Composite subComp = ControlFactory.createCompositeEx(comp, 2, GridData.FILL_HORIZONTAL);
		((GridLayout)subComp.getLayout()).makeColumnsEqualWidth = false;

		fExePathButton = createCheckButton(subComp, ExternalDebugUIMessages.getString("SDMDebuggerPage.2"));
		fExePathButton.addSelectionListener(new SelectionListener() {
		    public void widgetDefaultSelected(SelectionEvent e) {
	            handleExePathButtonSelected();
	    		if (!isInitializing())
	    			updateLaunchConfigurationDialog();
		    }
		    public void widgetSelected(SelectionEvent e) {
	            handleExePathButtonSelected();
	    		if (!isInitializing())
	    			updateLaunchConfigurationDialog();
		    }
		});
		fExePathText = ControlFactory.createTextField(subComp, SWT.SINGLE | SWT.BORDER);
		fExePathText.addModifyListener(new ModifyListener() {
			public void modifyText(ModifyEvent evt) {
				if (!isInitializing())
					updateLaunchConfigurationDialog();
			}
		});
		
		fCWDButton = createCheckButton(subComp, ExternalDebugUIMessages.getString("SDMDebuggerPage.3"));
		fCWDButton.addSelectionListener(new SelectionListener() {
		    public void widgetDefaultSelected(SelectionEvent e) {
	            handleCWDButtonSelected();
	    		if (!isInitializing())
	    			updateLaunchConfigurationDialog();
		    }
		    public void widgetSelected(SelectionEvent e) {
	            handleCWDButtonSelected();
	    		if (!isInitializing())
	    			updateLaunchConfigurationDialog();
		    }
		});
		fCWDText = ControlFactory.createTextField(subComp, SWT.SINGLE | SWT.BORDER);
		fCWDText.addModifyListener(new ModifyListener() {
			public void modifyText(ModifyEvent evt) {
				if (!isInitializing())
					updateLaunchConfigurationDialog();
			}
		});
		
		setControl(parent);
	}

	public void setDefaults(ILaunchConfigurationWorkingCopy configuration) {
		configuration.setAttribute(IPTPLaunchConfigurationConstants.ATTR_DEBUGGER_EXECUTABLE_PATH, (String)null);
		configuration.setAttribute(IPTPLaunchConfigurationConstants.ATTR_DEBUGGER_WORKING_DIR, (String)null);
	}

	public boolean isValid(ILaunchConfiguration launchConfig) {
		setErrorMessage(null);
		setMessage(null);
		return true;
	}

	public void initializeFrom(ILaunchConfiguration configuration) {
		setInitializing(true);
		try {
			String exe = configuration.getAttribute(IPTPLaunchConfigurationConstants.ATTR_DEBUGGER_EXECUTABLE_PATH, (String)null);
			if (exe == null) {
				fExePathText.setText(EMPTY_STRING);
				fExePathButton.setSelection(false);
			} else {
				fExePathText.setText(exe);
				fExePathButton.setSelection(true);
			}
			handleExePathButtonSelected();	
			
			String cwd = configuration.getAttribute(IPTPLaunchConfigurationConstants.ATTR_DEBUGGER_WORKING_DIR, (String)null);
			if (cwd == null) {
				fCWDText.setText(EMPTY_STRING);
				fCWDButton.setSelection(false);
			} else {
				fCWDText.setText(cwd);
				fCWDButton.setSelection(true);
			}
			handleCWDButtonSelected();	
		}
		catch(CoreException e) {
	           setErrorMessage("Exception occurred reading configuration");
		}
		setInitializing(false); 
	}

	public void performApply(ILaunchConfigurationWorkingCopy configuration) {
		String exe = null;
		if (fExePathButton.getSelection()) {
			exe = getFieldContent(fExePathText.getText());
		}
		configuration.setAttribute(IPTPLaunchConfigurationConstants.ATTR_DEBUGGER_EXECUTABLE_PATH, exe);
		
		String cwd = null;
		if (fCWDButton.getSelection()) {
			cwd = getFieldContent(fCWDText.getText());
		}
		configuration.setAttribute(IPTPLaunchConfigurationConstants.ATTR_DEBUGGER_WORKING_DIR, cwd);
	}

	public String getName() {
		return ExternalDebugUIMessages.getString("SDMDebuggerPage.1");
	}

	/**
	 * @see org.eclipse.debug.ui.AbstractLaunchConfigurationTab#getShell()
	 */
	protected Shell getShell() {
		return super.getShell();
	}

	/**
	 * @see org.eclipse.debug.ui.AbstractLaunchConfigurationTab#updateLaunchConfigurationDialog()
	 */
	protected void updateLaunchConfigurationDialog() {
		super.updateLaunchConfigurationDialog();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.util.Observer#update(java.util.Observable, java.lang.Object)
	 */
	public void update(Observable o, Object arg) {
		if (!isInitializing())
			updateLaunchConfigurationDialog();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.debug.ui.ILaunchConfigurationTab#dispose()
	 */
	public void dispose() {
		super.dispose();
	}

	/* (non-Javadoc)
	 * @see org.eclipse.debug.ui.ILaunchConfigurationTab#activated(org.eclipse.debug.core.ILaunchConfigurationWorkingCopy)
	 */
	public void activated(ILaunchConfigurationWorkingCopy workingCopy) {
		// Override the default behavior
	}

	protected boolean isInitializing() {
		return fIsInitializing;
	}

	private void setInitializing(boolean isInitializing) {
		fIsInitializing = isInitializing;
	}

	/**
	 * The default check box has been toggled.
	 */
	protected void handleExePathButtonSelected() {
		if (fExePathButton.getSelection()) {
			fExePathText.setEnabled(true);
		} else {
			fExePathText.setEnabled(false);
		}
	}

	/**
	 * The default check box has been toggled.
	 */
	protected void handleCWDButtonSelected() {
		if (fCWDButton.getSelection()) {
			fCWDText.setEnabled(true);
		} else {
			fCWDText.setEnabled(false);
		}
	}
	
    protected String getFieldContent(String text) {
        if (text.trim().length() == 0 || text.equals(EMPTY_STRING))
            return null;
        
        return text;
    }
}
