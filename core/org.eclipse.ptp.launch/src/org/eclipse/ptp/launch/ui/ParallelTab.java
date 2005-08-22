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
import org.eclipse.jface.preference.IntegerFieldEditor;
import org.eclipse.jface.util.IPropertyChangeListener;
import org.eclipse.jface.util.PropertyChangeEvent;
import org.eclipse.ptp.core.IPTPLaunchConfigurationConstants;
import org.eclipse.ptp.internal.core.CoreMessages;
import org.eclipse.ptp.launch.internal.ui.LaunchImages;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Label;

/**
 * 
 */
public class ParallelTab extends PLaunchConfigurationTab {
    // Program arguments UI widgets
    //protected Combo startupCombo = null;
    protected Composite dynamicComp = null;
    //protected Combo networkTypeCombo = null;
    
    protected IntegerFieldEditor numberOfProcessField = null;
    /*
    protected IntegerFieldEditor numberOfProcessStartField = null;
    protected IntegerFieldEditor firstNodeNumberField = null;
    */

    protected class WidgetListener extends SelectionAdapter implements IPropertyChangeListener {
	    public void widgetSelected(SelectionEvent e) {
	    	/*
	        if (e.getSource() == startupCombo)
	            updateComboFromSelection();
	        else
	        */
	            updateLaunchConfigurationDialog();
	    }
        public void propertyChange(PropertyChangeEvent event) {
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
                
		new Label(parallelComp, SWT.NONE).setText(CoreMessages.getResourceString("ParallelTab.StartupLabel"));
		/*
		startupCombo = new Combo(parallelComp, SWT.DROP_DOWN | SWT.READ_ONLY);
		startupCombo.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
		startupCombo.setItems(new String[] {"BProc"});
		startupCombo.select(0);
		startupCombo.addSelectionListener(listener);
		*/
		
		createVerticalSpacer(parallelComp, 2);
		
		Group debuggerGroup = new Group(parallelComp, SWT.SHADOW_ETCHED_IN);
		debuggerGroup.setLayout(createGridLayout(2, false, 10, 10));
		debuggerGroup.setLayoutData(spanGridData(GridData.FILL_BOTH, 2));
		debuggerGroup.setText(CoreMessages.getResourceString("ParallelTab.Parallel_Options"));			

		dynamicComp = new Composite(debuggerGroup, SWT.NONE);
		dynamicComp.setLayout(createGridLayout(2, false, 10, 10));
		dynamicComp.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
		
        numberOfProcessField = new IntegerFieldEditor("numberOfProcess", CoreMessages.getResourceString("ParallelTab.Number_Of_Processes"), dynamicComp);
        numberOfProcessField.setValidRange(0, 100000);
        numberOfProcessField.setPropertyChangeListener(listener);

        //pRadio = new Button(parallelComp, SWT.RADIO);
        //pRadio.setText(CoreMessages.getResourceString("ParallelTab.Ethernet_For_Communication"));
        //gRadio = new Button(parallelComp, SWT.RADIO);
        //gRadio.setText(CoreMessages.getResourceString("ParallelTab.Myrinet_For_Communication"));
        
        //pRadio = createRadioButton(parallelComp, CoreMessages.getResourceString("ParallelTab.Ethernet_For_Communication"));
        //pRadio.addSelectionListener(listener);
        //gRadio = createRadioButton(parallelComp, CoreMessages.getResourceString("ParallelTab.Myrinet_For_Communication"));
        //gRadio.addSelectionListener(listener);
        
        /*
		new Label(dynamicComp, SWT.NONE).setText(CoreMessages.getResourceString("ParallelTab.NetworkTypeLabel"));
		networkTypeCombo = new Combo(dynamicComp, SWT.DROP_DOWN | SWT.READ_ONLY);
		networkTypeCombo.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
		networkTypeCombo.setItems(new String[] {"P4", "GM"});
		networkTypeCombo.addSelectionListener(listener);
		*/
        
        /* disabled temporarily
        numberOfProcessStartField = new IntegerFieldEditor("numberOfProcessStar", CoreMessages.getResourceString("ParallelTab.Processes_Per_Node"), dynamicComp);
        numberOfProcessStartField.setValidRange(0, 5000);
        numberOfProcessStartField.setPropertyChangeListener(listener);
        
        firstNodeNumberField = new IntegerFieldEditor("firstNodeNumber", CoreMessages.getResourceString("ParallelTab.First_Node_Number"), dynamicComp);
        firstNodeNumberField.setValidRange(0, 5000);
        firstNodeNumberField.setPropertyChangeListener(listener);
        */
        
        createVerticalSpacer(parallelComp, 2);
    }
    
    public void updateComboFromSelection() {
        System.out.println("change startup");        
    }
    /**
     * Defaults are empty.
     * 
     * @see ILaunchConfigurationTab#setDefaults(ILaunchConfigurationWorkingCopy)
     */
    public void setDefaults(ILaunchConfigurationWorkingCopy configuration) {
        configuration.setAttribute(IPTPLaunchConfigurationConstants.NUMBER_OF_PROCESSES, IPTPLaunchConfigurationConstants.DEF_NUMBER_OF_PROCESSES);
        //configuration.setAttribute(IPTPLaunchConfigurationConstants.NETWORK_TYPE, IPTPLaunchConfigurationConstants.DEF_NETWORK_TYPE);
        configuration.setAttribute(IPTPLaunchConfigurationConstants.PROCESSES_PER_NODE, IPTPLaunchConfigurationConstants.DEF_PROCESSES_PER_NODE);
        configuration.setAttribute(IPTPLaunchConfigurationConstants.FIRST_NODE_NUMBER, IPTPLaunchConfigurationConstants.DEF_FIRST_NODE_NUMBER);
    }

    /**
     * @see ILaunchConfigurationTab#initializeFrom(ILaunchConfiguration)
     */
    public void initializeFrom(ILaunchConfiguration configuration) {
        try {
            numberOfProcessField.setStringValue(configuration.getAttribute(IPTPLaunchConfigurationConstants.NUMBER_OF_PROCESSES, EMPTY_STRING));
            //String type = configuration.getAttribute(IPTPLaunchConfigurationConstants.NETWORK_TYPE, EMPTY_STRING);
            //networkTypeCombo.select(type.equals(IPTPLaunchConfigurationConstants.P_TYPE)?0:1);            
            //numberOfProcessStartField.setStringValue(configuration.getAttribute(IPTPLaunchConfigurationConstants.PROCESSES_PER_NODE, EMPTY_STRING));
            //firstNodeNumberField.setStringValue(configuration.getAttribute(IPTPLaunchConfigurationConstants.FIRST_NODE_NUMBER, EMPTY_STRING));
        } catch (CoreException e) {
            setErrorMessage(CoreMessages.getFormattedResourceString("CommonTab.common.Exception_occurred_reading_configuration_EXCEPTION", e.getStatus().getMessage()));
        }
    }

    /**
     * @see ILaunchConfigurationTab#performApply(ILaunchConfigurationWorkingCopy)
     */
    public void performApply(ILaunchConfigurationWorkingCopy configuration) {
        configuration.setAttribute(IPTPLaunchConfigurationConstants.NUMBER_OF_PROCESSES, getFieldContent(numberOfProcessField));
        //configuration.setAttribute(IPTPLaunchConfigurationConstants.NETWORK_TYPE, networkTypeCombo.getSelectionIndex()==0?IPTPLaunchConfigurationConstants.P_TYPE:IPTPLaunchConfigurationConstants.G_TYPE);
        //configuration.setAttribute(IPTPLaunchConfigurationConstants.PROCESSES_PER_NODE, getFieldContent(numberOfProcessStartField));
        //configuration.setAttribute(IPTPLaunchConfigurationConstants.FIRST_NODE_NUMBER, getFieldContent(firstNodeNumberField));
    }
    
    protected String getFieldContent(IntegerFieldEditor editorField) {
        return getFieldContent(editorField.getStringValue());
    }
    
	/* (non-Javadoc)
	 * @see org.eclipse.debug.ui.ILaunchConfigurationTab#isValid(org.eclipse.debug.core.ILaunchConfiguration)
	 */
	public boolean isValid(ILaunchConfiguration configuration) {
		setErrorMessage(null);
		setMessage(null);
		
        if (!numberOfProcessField.isValid()) {
            setErrorMessage(numberOfProcessField.getErrorMessage());
            return false;
        }

        /*
        if (getFieldContent(numberOfProcessStartField) != null && !numberOfProcessStartField.isValid()) {
            setErrorMessage(numberOfProcessStartField.getErrorMessage());
            return false;
        }

        if (getFieldContent(firstNodeNumberField) != null && !firstNodeNumberField.isValid()) {
            setErrorMessage(firstNodeNumberField.getErrorMessage());
            return false;
        }
        */
        
		return true;
	}    

    /**
     * @see ILaunchConfigurationTab#getName()
     */
    public String getName() {
        return CoreMessages.getResourceString("ParallelTab.Parallel");
    }

    /**
     * @see ILaunchConfigurationTab#setLaunchConfigurationDialog(ILaunchConfigurationDialog)
     */
    public void setLaunchConfigurationDialog(ILaunchConfigurationDialog dialog) {
        super.setLaunchConfigurationDialog(dialog);
    }	

    /**
     * @see ILaunchConfigurationTab#getImage()
     */
    public Image getImage() {
        return LaunchImages.getImage(LaunchImages.IMG_PARALLEL_TAB);
    }    
}