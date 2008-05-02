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
package org.eclipse.ptp.launch.ui;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.debug.core.ILaunchConfiguration;
import org.eclipse.debug.core.ILaunchConfigurationWorkingCopy;
import org.eclipse.debug.ui.ILaunchConfigurationDialog;
import org.eclipse.debug.ui.ILaunchConfigurationTab;
import org.eclipse.ptp.core.IPTPLaunchConfigurationConstants;
import org.eclipse.ptp.launch.internal.ui.LaunchImages;
import org.eclipse.ptp.launch.internal.ui.LaunchMessages;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Text;

/**
 *
 */
public class ArgumentsTab extends LaunchConfigurationTab {
    protected Text argumentText = null;
    
    protected WorkingDirectoryBlock workingDirectoryBlock = new WorkingDirectoryBlock(); 
    
    protected class WidgetListener implements ModifyListener {
		public void modifyText(ModifyEvent evt) {
			updateLaunchConfigurationDialog();
		}        
    }
    
    protected WidgetListener listener = new WidgetListener();

    /**
     * @see ILaunchConfigurationTab#createControl(Composite)
     */
    public void createControl(Composite parent) {
        Composite comp = new Composite(parent, SWT.NONE);
        setControl(comp);
        //WorkbenchHelp.setHelp(getControl(), ICDTLaunchHelpContextIds.LAUNCH_CONFIGURATION_DIALOG_ARGUMNETS_TAB);
        
		GridLayout topLayout = new GridLayout();
		comp.setLayout(topLayout);

		Composite parallelComp = new Composite(comp, SWT.NONE);		
		parallelComp.setLayout(createGridLayout(2, false, 0, 0));
		parallelComp.setLayoutData(spanGridData(GridData.FILL_HORIZONTAL, 5));        
                
		Label programArgumentLabel = new Label(parallelComp, SWT.NONE);
		programArgumentLabel.setLayoutData(spanGridData(-1, 2));
		programArgumentLabel.setText(LaunchMessages.getResourceString("ArgumentsTab.Parallel_Program_Arguments"));
		argumentText = new Text(parallelComp, SWT.MULTI | SWT.WRAP | SWT.BORDER | SWT.V_SCROLL);
		GridData gd = spanGridData(GridData.FILL_HORIZONTAL, 2);
		gd.heightHint = 40;
		argumentText.setLayoutData(gd);
		argumentText.addModifyListener(listener);
		
        createVerticalSpacer(parallelComp, 2);
		
        workingDirectoryBlock.createControl(parallelComp);
    }
    
    /**
     * Defaults are empty.
     * 
     * @see ILaunchConfigurationTab#setDefaults(ILaunchConfigurationWorkingCopy)
     */
    public void setDefaults(ILaunchConfigurationWorkingCopy configuration) {
        configuration.setAttribute(IPTPLaunchConfigurationConstants.ATTR_ARGUMENTS, (String) null);
        workingDirectoryBlock.setDefaults(configuration);
    }

    /**
     * @see ILaunchConfigurationTab#initializeFrom(ILaunchConfiguration)
     */
    public void initializeFrom(ILaunchConfiguration configuration) {
        try {
            argumentText.setText(configuration.getAttribute(IPTPLaunchConfigurationConstants.ATTR_ARGUMENTS, EMPTY_STRING));            
            workingDirectoryBlock.initializeFrom(configuration);
        } catch (CoreException e) {
            setErrorMessage(LaunchMessages.getFormattedResourceString("CommonTab.common.Exception_occurred_reading_configuration_EXCEPTION", e.getStatus().getMessage()));
        }
    }

    /**
     * @see ILaunchConfigurationTab#performApply(ILaunchConfigurationWorkingCopy)
     */
    public void performApply(ILaunchConfigurationWorkingCopy configuration) {
        configuration.setAttribute(IPTPLaunchConfigurationConstants.ATTR_ARGUMENTS, argumentText.getText());
        workingDirectoryBlock.performApply(configuration);
    }    
    
	/* (non-Javadoc)
	 * @see org.eclipse.debug.ui.ILaunchConfigurationTab#isValid(org.eclipse.debug.core.ILaunchConfiguration)
	 */
	public boolean isValid(ILaunchConfiguration configuration) {
		setErrorMessage(null);
		setMessage(null);
		
		return workingDirectoryBlock.isValid(configuration);
	}
	
    /**
     * @see ILaunchConfigurationTab#getName()
     */
    public String getName() {
        return LaunchMessages.getResourceString("ArgumentsTab.Arguments");
    }

    /**
     * @see ILaunchConfigurationTab#setLaunchConfigurationDialog(ILaunchConfigurationDialog)
     */
    public void setLaunchConfigurationDialog(ILaunchConfigurationDialog dialog) {
        super.setLaunchConfigurationDialog(dialog);
		workingDirectoryBlock.setLaunchConfigurationDialog(dialog);
    }
    
    /**
     * @see ILaunchConfigurationTab#getImage()
     */
    public Image getImage() {
        return LaunchImages.getImage(LaunchImages.IMG_ARGUMENTS_TAB);
    }    
    
    /**
	 * @see org.eclipse.debug.ui.ILaunchConfigurationTab#getErrorMessage()
	 */
	public String getErrorMessage() {
		String msg = super.getErrorMessage();
		if (msg == null)
			return workingDirectoryBlock.getErrorMessage();

		return msg;
	}	

	/**
	 * @see org.eclipse.debug.ui.ILaunchConfigurationTab#getMessage()
	 */
	public String getMessage() {
		String msg = super.getMessage();
		if (msg == null)
			return workingDirectoryBlock.getMessage();

		return msg;
	}		
}
