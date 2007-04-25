/*******************************************************************************
 * Copyright (c) 2005, 2006, 2007 Los Alamos National Security, LLC.
 * This material was produced under U.S. Government contract DE-AC52-06NA25396
 * for Los Alamos National Laboratory (LANL), which is operated by the Los Alamos
 * National Security, LLC (LANS) for the U.S. Department of Energy.  The U.S. Government has
 * rights to use, reproduce, and distribute this software. NEITHER THE
 * GOVERNMENT NOR LANS MAKES ANY WARRANTY, EXPRESS OR IMPLIED, OR
 * ASSUMES ANY LIABILITY FOR THE USE OF THIS SOFTWARE. If software is modified
 * to produce derivative works, such modified software should be clearly marked,
 * so as not to confuse it with the version available from LANL.
 *
 * Additionally, this program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *******************************************************************************/
package org.eclipse.ptp.lsf.ui.rmLaunchConfiguration;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.debug.core.ILaunchConfiguration;
import org.eclipse.debug.core.ILaunchConfigurationWorkingCopy;
import org.eclipse.ptp.core.attributes.IAttribute;
import org.eclipse.ptp.core.attributes.IllegalValueException;
import org.eclipse.ptp.core.attributes.IntegerAttribute;
import org.eclipse.ptp.core.attributes.IntegerAttributeDefinition;
import org.eclipse.ptp.core.elements.IPQueue;
import org.eclipse.ptp.launch.ui.extensions.AbstractRMLaunchConfigurationDynamicTab;
import org.eclipse.ptp.launch.ui.extensions.RMLaunchValidation;
import org.eclipse.ptp.lsf.core.rmsystem.LSFResourceManager;
import org.eclipse.ptp.lsf.ui.Activator;
import org.eclipse.ptp.rmsystem.IResourceManager;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Text;

public class LSFRMLaunchConfigurationDynamicTab extends
		AbstractRMLaunchConfigurationDynamicTab {
	
	private static final int MIN_TEXT_WIDTH = 50;
	private static final String NUMBER_OF_PROCESSES_LABEL = "Number of Processes: ";
	private static final String ATTR_PREFIX = Activator.PLUGIN_ID + ".launchAttributes";
	private static final String ATTR_NUMPROCS = ATTR_PREFIX + ".numProcs";
	private static final RMLaunchValidation success = new RMLaunchValidation(true, "");
	private Text numProcsText;
	private Composite control;

	public LSFRMLaunchConfigurationDynamicTab(IResourceManager rm) {
		// TODO Auto-generated constructor stub
	}

	/* (non-Javadoc)
	 * @see org.eclipse.ptp.launch.ui.extensions.IRMLaunchConfigurationDynamicTab#canSave(org.eclipse.swt.widgets.Control, org.eclipse.ptp.rmsystem.IResourceManager, org.eclipse.ptp.core.IPQueue)
	 */
	public RMLaunchValidation canSave(Control control, IResourceManager rm, IPQueue queue) {
		String numProcsString = numProcsText.getText();
		try {
			IntegerAttributeDefinition numProcsAttrDef = getNumProcsAttrDef(rm, queue);
			@SuppressWarnings("unused")
			IntegerAttribute iattr = new IntegerAttribute(numProcsAttrDef, numProcsString);
		} catch (IllegalValueException e) {
			return new RMLaunchValidation(false, NUMBER_OF_PROCESSES_LABEL + e.getMessage());
		}
		return new RMLaunchValidation(true, "");
	}

	/* (non-Javadoc)
	 * @see org.eclipse.ptp.launch.ui.extensions.IRMLaunchConfigurationDynamicTab#createControl(org.eclipse.swt.widgets.Composite, org.eclipse.ptp.rmsystem.IResourceManager, org.eclipse.ptp.core.IPQueue)
	 */
	public void createControl(Composite parent,	IResourceManager rm, IPQueue queue) {
		control = new Composite(parent, SWT.NONE);
		control.setLayout(new GridLayout(1, true));
		GridData gd = new GridData(GridData.FILL_BOTH);
		control.setLayoutData(gd);
		
		Label label = new Label(control, SWT.NONE);
		label.setText("LSF Specific info goes here");
		
		final Composite comp = new Composite(control, SWT.NONE);
		GridLayout launchConfigLayout = new GridLayout();
		launchConfigLayout.marginHeight = 0;
		launchConfigLayout.marginWidth = 0;
		launchConfigLayout.numColumns = 2;
		comp.setLayout(launchConfigLayout);
		gd = new GridData(GridData.FILL_BOTH);
		gd.horizontalSpan = 2;
		comp.setLayoutData(gd);
		label = new Label(comp, SWT.NONE);
		label.setText(NUMBER_OF_PROCESSES_LABEL);
		gd = new GridData(GridData.FILL_HORIZONTAL);
		gd.minimumWidth = MIN_TEXT_WIDTH;
		label.setLayoutData(gd);
		numProcsText = new Text(comp, SWT.NONE);
		numProcsText.setTextLimit(10);
		numProcsText.setLayoutData(gd);
		numProcsText.setText("1000");
		
		// Tell the client of this dynamic tab that the
		// contents of this tab are affected by the contents
		// of this widget.
		numProcsText.addModifyListener(new ModifyListener(){

			public void modifyText(ModifyEvent e) {
				fireContentsChanged();
			}});
	}

	/* (non-Javadoc)
	 * @see org.eclipse.ptp.launch.ui.extensions.IRMLaunchConfigurationDynamicTab#getAttributes(org.eclipse.ptp.rmsystem.IResourceManager, org.eclipse.ptp.core.IPQueue, org.eclipse.debug.core.ILaunchConfiguration)
	 */
	public IAttribute[] getAttributes(IResourceManager rm, IPQueue queue,
			ILaunchConfiguration configuration) throws CoreException {
		int numProcs = configuration.getAttribute(ATTR_NUMPROCS, -1);
		IntegerAttribute iattr = null;
		try {
			IntegerAttributeDefinition numProcsAttrDef = getNumProcsAttrDef(rm, queue);
			iattr = new IntegerAttribute(numProcsAttrDef, numProcs);
		} catch (IllegalValueException e) {
			return new IAttribute[0];
		}
		return new IAttribute[]{iattr};
	}

	/* (non-Javadoc)
	 * @see org.eclipse.ptp.launch.ui.extensions.IRMLaunchConfigurationDynamicTab#getControl()
	 */
	public Control getControl() {
		return control;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.ptp.launch.ui.extensions.IRMLaunchConfigurationDynamicTab#initializeFrom(org.eclipse.swt.widgets.Control, org.eclipse.ptp.rmsystem.IResourceManager, org.eclipse.ptp.core.IPQueue, org.eclipse.debug.core.ILaunchConfiguration)
	 */
	public RMLaunchValidation initializeFrom(Control control, IResourceManager rm,
			IPQueue queue, ILaunchConfiguration configuration) {
		int numProcs;
		try {
			numProcs = configuration.getAttribute(ATTR_NUMPROCS, 1);
		} catch (CoreException e) {
			try {
				IntegerAttributeDefinition numProcsAttrDef = getNumProcsAttrDef(rm, queue);
				numProcsText.setText(numProcsAttrDef.create().getValueAsString());
			} catch (IllegalValueException e1) {
				return new RMLaunchValidation(false, NUMBER_OF_PROCESSES_LABEL +
						e.getMessage() + " : " + e1.getMessage());
			}
			return new RMLaunchValidation(false, NUMBER_OF_PROCESSES_LABEL + e.getMessage());
		}
		numProcsText.setText(Integer.toString(numProcs));
		return success;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.ptp.launch.ui.extensions.IRMLaunchConfigurationDynamicTab#isValid(org.eclipse.debug.core.ILaunchConfiguration, org.eclipse.ptp.rmsystem.IResourceManager, org.eclipse.ptp.core.IPQueue)
	 */
	public RMLaunchValidation isValid(ILaunchConfiguration configuration,
			IResourceManager rm, IPQueue queue) {
		String numProcsString = numProcsText.getText();
		try {
			IntegerAttributeDefinition numProcsAttrDef = getNumProcsAttrDef(rm, queue);
			@SuppressWarnings("unused")
			IntegerAttribute iattr = new IntegerAttribute(numProcsAttrDef, numProcsString);
		} catch (IllegalValueException e) {
			return new RMLaunchValidation(false, NUMBER_OF_PROCESSES_LABEL + e.getMessage());
		}
		return new RMLaunchValidation(true, "");
	}

	/* (non-Javadoc)
	 * @see org.eclipse.ptp.launch.ui.extensions.IRMLaunchConfigurationDynamicTab#performApply(org.eclipse.debug.core.ILaunchConfigurationWorkingCopy, org.eclipse.ptp.rmsystem.IResourceManager, org.eclipse.ptp.core.IPQueue)
	 */
	public RMLaunchValidation performApply(ILaunchConfigurationWorkingCopy configuration,
			IResourceManager rm, IPQueue queue) {
		String numProcsString = numProcsText.getText();
		IntegerAttribute iattr = null;
		try {
			IntegerAttributeDefinition numProcsAttrDef = getNumProcsAttrDef(rm, queue);
			iattr = new IntegerAttribute(numProcsAttrDef, numProcsString);
		} catch (IllegalValueException e) {
			return new RMLaunchValidation(false, NUMBER_OF_PROCESSES_LABEL + e.getMessage());
		}
		configuration.setAttribute(ATTR_NUMPROCS, iattr.getValue());
		return new RMLaunchValidation(true, "");
	}

	/* (non-Javadoc)
	 * @see org.eclipse.ptp.launch.ui.extensions.IRMLaunchConfigurationDynamicTab#setDefaults(org.eclipse.debug.core.ILaunchConfigurationWorkingCopy, org.eclipse.ptp.rmsystem.IResourceManager, org.eclipse.ptp.core.IPQueue)
	 */
	public RMLaunchValidation setDefaults(ILaunchConfigurationWorkingCopy configuration,
			IResourceManager rm, IPQueue queue) {
		try {
			IntegerAttributeDefinition numProcsAttrDef = getNumProcsAttrDef(rm, queue);
			configuration.setAttribute(ATTR_NUMPROCS, numProcsAttrDef.create().getValue());
		} catch (IllegalValueException e) {
			return new RMLaunchValidation(false, NUMBER_OF_PROCESSES_LABEL + e.getMessage());
		}
		return success;
	}

	/**
	 * Get the attribute definition for the number of processes job launch attribute
	 * 
	 * @param rm
	 * @param queue
	 * @return
	 */
	private IntegerAttributeDefinition getNumProcsAttrDef(IResourceManager rm, IPQueue queue) {
		LSFResourceManager lrm = (LSFResourceManager) rm;
		IntegerAttributeDefinition numProcsAttrDef = lrm.getNumProcsAttrDef(queue);
		return numProcsAttrDef;
	}

}
