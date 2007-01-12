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

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.debug.core.ILaunchConfiguration;
import org.eclipse.debug.core.ILaunchConfigurationWorkingCopy;
import org.eclipse.debug.ui.ILaunchConfigurationDialog;
import org.eclipse.debug.ui.ILaunchConfigurationTab;
import org.eclipse.jface.util.IPropertyChangeListener;
import org.eclipse.jface.util.PropertyChangeEvent;
import org.eclipse.ptp.core.IPMachine;
import org.eclipse.ptp.core.IPQueue;
import org.eclipse.ptp.core.IPTPLaunchConfigurationConstants;
import org.eclipse.ptp.core.IPUniverse;
import org.eclipse.ptp.core.PTPCorePlugin;
import org.eclipse.ptp.core.attributes.IAttribute;
import org.eclipse.ptp.core.attributes.IAttributeDescription;
import org.eclipse.ptp.core.attributes.IAttribute.IllegalValue;
import org.eclipse.ptp.core.elementcontrols.IPMachineControl;
import org.eclipse.ptp.core.elementcontrols.IPQueueControl;
import org.eclipse.ptp.launch.internal.ui.LaunchImages;
import org.eclipse.ptp.launch.internal.ui.LaunchMessages;
import org.eclipse.ptp.rmsystem.IResourceManager;
import org.eclipse.ptp.ui.attributes.AbstractAttributeControl;
import org.eclipse.ptp.ui.attributes.AttributeControlFactory;
import org.eclipse.ptp.ui.attributes.IAttributeControl;
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
	// private Combo startupCombo = null;
	// private Composite dynamicComp = null;

	private class WidgetListener extends SelectionAdapter implements
			IPropertyChangeListener {
		
		public void propertyChange(PropertyChangeEvent event) {
			
			if (event.getProperty() == AbstractAttributeControl.VALUE_CHANGED_PROPERTY) {
				// Update the value of the controls based on any changes
				// in the values
				updateLaunchAttributeControls();
			}
			
			/*
			 * Updates the buttons and message in this page's launch
			 * configuration dialog.
			 */
			updateLaunchConfigurationDialog();
		}

		public void widgetSelected(SelectionEvent e) {

			if (e.getSource() == resourceManagerCombo) {
				/*
				 * After a resource manager is chosen the machine and
				 * queues combo boxes must be re-loaded, and the attributes'
				 * controls must be updated.
				 */
				loadMachineAndQueueCombos();
				updateLaunchAttributeControls();
			}
			else if (e.getSource() == machineCombo) {
				// If a different machine is chosen the
				// attributes' controls must be updated.
				updateLaunchAttributeControls();
			}
			else if (e.getSource() == queueCombo) {
				// If a different queue is chosen the
				// attributes' controls must be updated.
				updateLaunchAttributeControls();
			}

			/*
			 * Updates the buttons and message in this page's launch
			 * configuration dialog.
			 */
			updateLaunchConfigurationDialog();
		}
	}

	private Combo resourceManagerCombo;

	// private Combo networkTypeCombo = null;

	private Combo machineCombo;
	
	/*
	 * private IntegerFieldEditor numberOfProcessStartField = null; private
	 * IntegerFieldEditor firstNodeNumberField = null;
	 */

	private Combo queueCombo;

	private WidgetListener listener = new WidgetListener();

	// The composite that holds the RM's attributes for the launch configuration 
	private Composite attrComp;

	private Map<String, IAttributeControl> attrControls = null;


	/**
	 * @see ILaunchConfigurationTab#createControl(Composite)
	 */
	public void createControl(Composite parent) {
		Composite comp = new Composite(parent, SWT.NONE);
		setControl(comp);

		GridLayout topLayout = new GridLayout();
		comp.setLayout(topLayout);
		// createVerticalSpacer(comp, 1);

		final Composite parallelComp = new Composite(comp, SWT.NONE);
		parallelComp.setLayout(createGridLayout(2, false, 0, 0));
		parallelComp.setLayoutData(spanGridData(GridData.FILL_HORIZONTAL, 5));

		createVerticalSpacer(parallelComp, 2);

		IPUniverse universe = PTPCorePlugin.getDefault().getModelManager().getUniverse();
		if (universe == null) {
			return;
		}
		
		IResourceManager[] rms = universe.getResourceManagers();
		new Label(parallelComp, SWT.NONE).setText("Select resource manager:");
		
		resourceManagerCombo = new Combo(parallelComp, SWT.READ_ONLY);
		for (int i = 0; i < rms.length; i++) {
			resourceManagerCombo.add(rms[i].getElementName());
		}
		resourceManagerCombo.select(0);
		resourceManagerCombo.addSelectionListener(listener);

		new Label(parallelComp, SWT.NONE).setText("Select machine:");
		machineCombo = new Combo(parallelComp, SWT.READ_ONLY);

		new Label(parallelComp, SWT.NONE).setText("Select queue:");
		queueCombo = new Combo(parallelComp, SWT.READ_ONLY);

		loadMachineAndQueueCombos();
		
		machineCombo.addSelectionListener(listener);
		queueCombo.addSelectionListener(listener);

		createVerticalSpacer(parallelComp, 2);

		// The composite that holds the RM's attributes for the launch configuration
		Group attrGroup = new Group(parallelComp, SWT.SHADOW_ETCHED_IN);
		attrComp = attrGroup;
		attrGroup.setText("Launch Attributes");
		attrComp.setLayout(createGridLayout(2, false, 0, 0));
		attrComp.setLayoutData(spanGridData(GridData.FILL_HORIZONTAL, 5));

		createLaunchAttributeControls();
		
		createVerticalSpacer(parallelComp, 2);
	}

	/**
	 * @see ILaunchConfigurationTab#getImage()
	 */
	public Image getImage() {
		return LaunchImages.getImage(LaunchImages.IMG_PARALLEL_TAB);
	}

	/**
	 * @see ILaunchConfigurationTab#getName()
	 */
	public String getName() {
		return LaunchMessages.getResourceString("ParallelTab.Parallel");
	}

	/**
	 * @see ILaunchConfigurationTab#initializeFrom(ILaunchConfiguration)
	 */
	public void initializeFrom(ILaunchConfiguration configuration) {
		try {
			int idx = getResourceManagerNameIndex(configuration.getAttribute(
					IPTPLaunchConfigurationConstants.RESOURCE_MANAGER_NAME, EMPTY_STRING));
			if (idx < 0)
				idx = 0;
			resourceManagerCombo.select(idx);

			// load up the combos given that the configuration has selected
			// a resource manager
			loadMachineAndQueueCombos();
			
			idx = getMachineNameIndex(configuration.getAttribute(
					IPTPLaunchConfigurationConstants.MACHINE_NAME, EMPTY_STRING));
			if (idx < 0)
				idx = 0;
			machineCombo.select(idx);

			idx = getQueueNameIndex(configuration.getAttribute(
					IPTPLaunchConfigurationConstants.QUEUE_NAME, EMPTY_STRING));
			if (idx < 0)
				idx = 0;
			queueCombo.select(idx);

			Map<String, String> configurationAttributeValueMap = 
				getConfigurationAttributeValueMap(configuration);

			for (String attrUniqueId : configurationAttributeValueMap.keySet()) {
				IAttributeControl attrControl = attrControls.get(attrUniqueId);
				if (attrControl != null) {
					String value = configurationAttributeValueMap.get(attrUniqueId);
					attrControl.setValue(value);
				}
			}
			
		} catch (CoreException e) {
			setErrorMessage(LaunchMessages
					.getFormattedResourceString(
							"CommonTab.common.Exception_occurred_reading_configuration_EXCEPTION",
							e.getStatus().getMessage()));
		} catch (IllegalValue e) {
			setErrorMessage(LaunchMessages
					.getFormattedResourceString(
							"CommonTab.common.Exception_occurred_reading_configuration_EXCEPTION",
							e.getMessage()));
		}
		
	}

	/* (non-Javadoc)
	 * @see org.eclipse.debug.ui.AbstractLaunchConfigurationTab#canSave()
	 */
	@Override
	public boolean canSave() {
		setErrorMessage(null);
		if (resourceManagerCombo.getSelectionIndex() < 0) {
			setErrorMessage("No Resource Manager has been selected");
			return false;
		}
		if (machineCombo.getSelectionIndex() < 0) {
			setErrorMessage("No Machine has been selected");
			return false;
		}
		if (queueCombo.getSelectionIndex() < 0) {
			setErrorMessage("No Queue has been selected");
			return false;
		}
		for (IAttributeControl attrControl : attrControls.values()) {
			if (attrControl.isEnabled()) {
				if (!attrControl.isValid()) {
					setErrorMessage(attrControl.getErrorMessage());
					return false;
				}
			}
		}	
		return true;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.debug.ui.ILaunchConfigurationTab#isValid(org.eclipse.debug.core.ILaunchConfiguration)
	 */
	public boolean isValid(ILaunchConfiguration configuration) {
		return canSave();
	}

	/**
	 * @see ILaunchConfigurationTab#performApply(ILaunchConfigurationWorkingCopy)
	 */
	public void performApply(ILaunchConfigurationWorkingCopy configuration) {
		configuration.setAttribute(IPTPLaunchConfigurationConstants.RESOURCE_MANAGER_NAME,
				getResourceManagerNameFromCombo());
		configuration.setAttribute(IPTPLaunchConfigurationConstants.MACHINE_NAME,
				getMachineNameFromCombo());
		configuration.setAttribute(IPTPLaunchConfigurationConstants.QUEUE_NAME,
				getQueueNameFromCombo());
		Map<String, String> valueMap = new HashMap<String, String>();
		for (IAttributeControl control : attrControls.values()) {
			IAttribute attribute;
			attribute = control.getAttribute();
			valueMap.put(attribute.getDescription().getUniqueId(), attribute.getStringRep());
		}
		setConfigurationAttributeValueMap(configuration, valueMap);
	}

	/* (non-Javadoc)
	 * @see org.eclipse.debug.ui.ILaunchConfigurationTab#setDefaults(org.eclipse.debug.core.ILaunchConfigurationWorkingCopy)
	 */
	public void setDefaults(ILaunchConfigurationWorkingCopy configuration) {
		configuration.setAttribute(IPTPLaunchConfigurationConstants.RESOURCE_MANAGER_NAME,
				getResourceManagerNameFromCombo());
		configuration.setAttribute(IPTPLaunchConfigurationConstants.MACHINE_NAME,
				getMachineNameFromCombo());
		configuration.setAttribute(IPTPLaunchConfigurationConstants.QUEUE_NAME,
				getQueueNameFromCombo());
		Map<String, String> configurationAttributeValueMap = new HashMap<String, String>();
		IResourceManager rm = getResourceManagerFromCombo();
		if (rm != null) {
			IAttribute[] attributes = null;
			attributes = rm.getLaunchAttributes(getMachineNameFromCombo(),
					getQueueNameFromCombo(), attributes);
			for (IAttribute attr : attributes) {
				final String attrValue = attr.getStringRep();
				final String uniqueId = attr.getDescription().getUniqueId();
				configurationAttributeValueMap.put(uniqueId, attrValue);
			}
			setConfigurationAttributeValueMap(configuration, configurationAttributeValueMap);
		}
	}

	/**
	 * @see ILaunchConfigurationTab#setLaunchConfigurationDialog(ILaunchConfigurationDialog)
	 */
	public void setLaunchConfigurationDialog(ILaunchConfigurationDialog dialog) {
		super.setLaunchConfigurationDialog(dialog);
	}

	private void createLaunchAttributeControls() {
		createVerticalSpacer(attrComp, 2);
	
		IResourceManager rm = getResourceManagerFromCombo();
		if (rm != null) {
			IAttribute[] attributes = rm.getLaunchAttributes(getMachineNameFromCombo(),
					getQueueNameFromCombo(), null);
			attrControls = new HashMap<String, IAttributeControl>();
			AttributeControlFactory factory = new AttributeControlFactory();
			for (int i = 0; i < attributes.length; ++i) {
				IAttributeDescription description = attributes[i].getDescription();
				new Label(attrComp, SWT.NONE).setText(description.getName() + ": ");
				String uniqeId = description.getUniqueId();
				final IAttributeControl attrControl = factory.create(attrComp,
						SWT.DROP_DOWN,	attributes[i]);
				attrControls.put(uniqeId, attrControl);
				// This listens for changes in the attrControl's value,
				// and to the valid/invalid state of the attrControl.
				attrControl.addPropertyChangeListener(listener);
			}
		}
		
		createVerticalSpacer(attrComp, 2);
	}

	private IPMachineControl getMachineFromCombo() {
		IResourceManager rm = getResourceManagerFromCombo();
		if (rm == null) {
			return null;
		}
		IPMachineControl[] macs = rm.getMachineControls();
		if (macs.length == 0) {
			return null;
		}
		int i = 0;
		if (machineCombo != null)
			i = machineCombo.getSelectionIndex();
		return macs[i];
	}

	private String getMachineNameFromCombo() {
		IPMachineControl machine = getMachineFromCombo();
		if (machine == null) {
			return "";
		}
		return machine.getElementName();
	}

	private int getMachineNameIndex(String machineName) {
		IResourceManager rm = getResourceManagerFromCombo();
		if (rm == null) {
			return -1;
		}
		IPMachine[] macs = rm.getMachines();
		int found = -1;
		for(int i=0; i<macs.length; i++) {
			if(macs[i].getElementName().equals(machineName)) {
				found = i;
				break;
			}
		}
		return found;
	}
	
	private IPQueueControl getQueueFromCombo() {
		IResourceManager rm = getResourceManagerFromCombo();
		if (rm == null) {
			return null;
		}
		IPQueueControl[] queues = rm.getQueueControls();
		int i = 0;
		if (queueCombo != null)
			i = queueCombo.getSelectionIndex();
		return queues[i];
	}

	private String getQueueNameFromCombo() {
		IPQueueControl queue = getQueueFromCombo();
		if (queue == null) {
			return "";
		}
		return queue.getElementName();
	}

	private int getQueueNameIndex(String queueName) {
		IResourceManager rm = getResourceManagerFromCombo();
		if (rm == null) {
			return -1;
		}
		IPQueue[] queues = rm.getQueues();
		int found = -1;
		for(int i=0; i<queues.length; i++) {
			if(queues[i].getElementName().equals(queueName)) {
				found = i;
				break;
			}
		}
		return found;
	}

	private IResourceManager getResourceManagerFromCombo() {
		IPUniverse universe = PTPCorePlugin.getDefault().getModelManager().getUniverse();
		if (universe == null) {
			return null;
		}
		IResourceManager[] rms = universe.getResourceManagers();
		int i = -1;
		if (resourceManagerCombo != null)
			i = resourceManagerCombo.getSelectionIndex();
		if (i >= 0 && i < rms.length)
			return rms[i];
		return null;
	}

	private String getResourceManagerNameFromCombo() {
		IResourceManager rm = getResourceManagerFromCombo();
		if (rm == null) {
			return "";
		}
		return rm.getElementName();
	}

	private int getResourceManagerNameIndex(String rmName) {
		IPUniverse universe = PTPCorePlugin.getDefault().getModelManager().getUniverse();
		if (universe == null) {
			return -1;
		}
		IResourceManager[] rms = universe.getResourceManagers();
		int found = -1;
		for(int i=0; i<rms.length; i++) {
			if(rms[i].getElementName().equals(rmName)) {
				found = i;
				break;
			}
		}
		return found;
	}

	/**
	 * After a resource manager is chosen the machine and
	 * queues combo boxes must be re-loaded.
	 * This method does just that.
	 */
	private void loadMachineAndQueueCombos() {
		machineCombo.removeAll();
		queueCombo.removeAll();

		IResourceManager rm = getResourceManagerFromCombo();
		if (rm == null) {
			return;
		}

		final IPMachine[] macs = rm.getMachines();
		for (int i = 0; i < macs.length; i++) {
			machineCombo.add(macs[i].getElementName());
		}
		machineCombo.select(0);
		
		final IPQueue[] queues = rm.getQueues();
		for (int i = 0; i < queues.length; i++) {
			queueCombo.add(queues[i].getElementName());
		}
		queueCombo.select(0);
	}

	private void updateLaunchAttributeControls() {
		
		IResourceManager rm = getResourceManagerFromCombo();
		
		if (rm != null) {
			
			ArrayList<IAttribute> currentAttrList = 
				new ArrayList<IAttribute>(attrControls.size());
			for (IAttributeControl attrControl : attrControls.values()) {
				currentAttrList.add(attrControl.getAttribute());
			}
			final IAttribute[] currentAttrs = currentAttrList.toArray(new IAttribute[0]);
			
			IAttribute[] newAttrs = rm.getLaunchAttributes(getMachineNameFromCombo(),
					getQueueNameFromCombo(), currentAttrs);
			
			for (IAttribute attr : newAttrs) {
				IAttributeDescription description = attr.getDescription();
				String uniqId = description.getUniqueId();
				IAttributeControl attrControl = attrControls.get(uniqId);
				if (attrControl != null) {
					if (attrControl.isEnabled() != attr.isEnabled())
						attrControl.setEnabled(attr.isEnabled());
				}
			}
		}
		
		attrComp.layout();
	}

	/**
	 * @param configuration
	 * @param valueMap
	 */
	static public void setConfigurationAttributeValueMap(ILaunchConfigurationWorkingCopy configuration,
			Map<String, String> valueMap) {
		configuration.setAttribute(IPTPLaunchConfigurationConstants.LAUNCH_ATTR_MAP,
				valueMap);
	}

	/**
	 * @param configuration
	 * @return
	 * @throws CoreException
	 */
	static public Map<String,String> getConfigurationAttributeValueMap(ILaunchConfiguration configuration)
	throws CoreException {
		return configuration.getAttribute(IPTPLaunchConfigurationConstants.LAUNCH_ATTR_MAP,
				Collections.EMPTY_MAP);
	}
}