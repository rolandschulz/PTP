package org.eclipse.ptp.launch.ui;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.debug.core.ILaunchConfiguration;
import org.eclipse.debug.core.ILaunchConfigurationWorkingCopy;
import org.eclipse.debug.ui.ILaunchConfigurationDialog;
import org.eclipse.debug.ui.ILaunchConfigurationTab;
import org.eclipse.ptp.core.IPDTLaunchConfigurationConstants;
import org.eclipse.ptp.ui.ParallelImages;
import org.eclipse.ptp.ui.UIMessage;
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
 * @author Clement
 *
 */
public class PArgumentTab extends PLaunchConfigurationTab {
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
		createVerticalSpacer(comp, 1);

		Composite parallelComp = new Composite(comp, SWT.NONE);		
		parallelComp.setLayout(createGridLayout(2, false, 0, 0));
		parallelComp.setLayoutData(spanGridData(GridData.FILL_HORIZONTAL, 5));        
                
		Label programArgumentLabel = new Label(parallelComp, SWT.NONE);
		programArgumentLabel.setLayoutData(spanGridData(-1, 2));
		programArgumentLabel.setText(UIMessage.getResourceString("ArgumentTab.Parallel_Program_Arguments"));
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
        configuration.setAttribute(IPDTLaunchConfigurationConstants.ATTR_ARGUMENT, (String) null);
        workingDirectoryBlock.setDefaults(configuration);
    }

    /**
     * @see ILaunchConfigurationTab#initializeFrom(ILaunchConfiguration)
     */
    public void initializeFrom(ILaunchConfiguration configuration) {
        try {
            argumentText.setText(configuration.getAttribute(IPDTLaunchConfigurationConstants.ATTR_ARGUMENT, EMPTY_STRING));            
            workingDirectoryBlock.initializeFrom(configuration);
        } catch (CoreException e) {
            setErrorMessage(UIMessage.getFormattedResourceString("CommonTab.common.Exception_occurred_reading_configuration_EXCEPTION", e.getStatus().getMessage()));
        }
    }

    /**
     * @see ILaunchConfigurationTab#performApply(ILaunchConfigurationWorkingCopy)
     */
    public void performApply(ILaunchConfigurationWorkingCopy configuration) {
        configuration.setAttribute(IPDTLaunchConfigurationConstants.ATTR_ARGUMENT, argumentText.getText());
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
        return UIMessage.getResourceString("ArgumentTab.Argument");
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
        return ParallelImages.getImage(ParallelImages.IMG_ARGUMENT_TAB);
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
