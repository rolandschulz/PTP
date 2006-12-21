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

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.debug.core.ILaunchConfiguration;
import org.eclipse.debug.core.ILaunchConfigurationWorkingCopy;
import org.eclipse.debug.ui.ILaunchConfigurationDialog;
import org.eclipse.debug.ui.ILaunchConfigurationTab;
import org.eclipse.jface.preference.IntegerFieldEditor;
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
import org.eclipse.ptp.internal.core.CoreMessages;
import org.eclipse.ptp.launch.internal.ui.LaunchImages;
import org.eclipse.ptp.rmsystem.IResourceManager;
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
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Label;

/**
 * 
 */
public class ParallelTab extends PLaunchConfigurationTab {
	// Program arguments UI widgets
	// protected Combo startupCombo = null;
	// protected Composite dynamicComp = null;

	protected class WidgetListener extends SelectionAdapter implements
			IPropertyChangeListener {
		public void propertyChange(PropertyChangeEvent event) {
			updateLaunchConfigurationDialog();
		}

		public void widgetSelected(SelectionEvent e) {

			if (e.getSource() == resourceManagerCombo) {
				loadMachineAndQueueCombos();
				updateLaunchAttributeControls();
			}
			else if (e.getSource() == machineCombo) {
				updateLaunchAttributeControls();
			}
			else if (e.getSource() == queueCombo) {
				updateLaunchAttributeControls();
			}

			updateLaunchConfigurationDialog();
		}
	}

	protected Combo resourceManagerCombo;

	// protected Combo networkTypeCombo = null;

	protected Combo machineCombo;
	
	/*
	 * protected IntegerFieldEditor numberOfProcessStartField = null; protected
	 * IntegerFieldEditor firstNodeNumberField = null;
	 */

	protected Combo queueCombo;

	protected WidgetListener listener = new WidgetListener();

	private Composite attrComp;

	private IAttributeControl[] attrControls = new IAttributeControl[0];

	/**
	 * @see ILaunchConfigurationTab#createControl(Composite)
	 */
	public void createControl(Composite parent) {
		Composite comp = new Composite(parent, SWT.NONE);
		setControl(comp);
		// WorkbenchHelp.setHelp(getControl(),
		// ICDTLaunchHelpContextIds.LAUNCH_CONFIGURATION_DIALOG_ARGUMNETS_TAB);

		GridLayout topLayout = new GridLayout();
		comp.setLayout(topLayout);
		// createVerticalSpacer(comp, 1);

		Composite parallelComp = new Composite(comp, SWT.NONE);
		parallelComp.setLayout(createGridLayout(2, false, 0, 0));
		parallelComp.setLayoutData(spanGridData(GridData.FILL_HORIZONTAL, 5));

		// new Label(parallelComp,
		// SWT.NONE).setText(CoreMessages.getResourceString("ParallelTab.StartupLabel"));
		/*
		 * startupCombo = new Combo(parallelComp, SWT.DROP_DOWN |
		 * SWT.READ_ONLY); startupCombo.setLayoutData(new
		 * GridData(GridData.FILL_HORIZONTAL)); startupCombo.setItems(new
		 * String[] {"BProc"}); startupCombo.select(0);
		 * startupCombo.addSelectionListener(listener);
		 */

		createVerticalSpacer(parallelComp, 2);

		// Group debuggerGroup = new Group(parallelComp, SWT.SHADOW_ETCHED_IN);
		// debuggerGroup.setLayout(createGridLayout(2, false, 10, 10));
		// debuggerGroup.setLayoutData(spanGridData(GridData.FILL_BOTH, 2));
		// debuggerGroup.setText(CoreMessages.getResourceString("ParallelTab.Parallel_Options"));

		// dynamicComp = new Composite(parallelComp, SWT.NONE);
		// dynamicComp.setLayout(createGridLayout(2, false, 10, 10));
		// dynamicComp.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));

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

		attrComp = new Composite(parallelComp, SWT.NONE);
		attrComp.setLayout(createGridLayout(2, false, 0, 0));
		attrComp.setLayoutData(spanGridData(GridData.FILL_HORIZONTAL, 5));

		updateLaunchAttributeControls();
		
		createVerticalSpacer(parallelComp, 2);
	}

	private void updateLaunchAttributeControls() {
		for (IAttributeControl control : attrControls) {
			control.dispose();
		}
		for (Control control : attrComp.getChildren()) {
			control.dispose();
		}
		
		int nChildren = attrComp.getChildren().length;
		assert(nChildren == 0);
		
		IResourceManager rm = getResourceManager();
		IAttribute[] attrs = rm.getLaunchAttributes(getMachine(), getQueue());
		attrControls = new IAttributeControl[attrs.length];
		for (int i = 0; i < attrs.length; ++i) {
			IAttributeDescription description = attrs[i].getDescription();
			new Label(attrComp, SWT.NONE).setText(description.getDescription() + ": ");
			attrControls[i] = AttributeControlFactory.create(attrComp,
					SWT.DROP_DOWN , attrs[i]);
			attrControls[i].addPropertyChangeListener(listener);
		}
		nChildren = attrComp.getChildren().length;
		assert(nChildren == 2*attrs.length);
		attrComp.pack(true);
		getControl().redraw();
		getControl().update();
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
		return CoreMessages.getResourceString("ParallelTab.Parallel");
	}

	/**
	 * @see ILaunchConfigurationTab#initializeFrom(ILaunchConfiguration)
	 */
	public void initializeFrom(ILaunchConfiguration configuration) {
		try {
			Map<String, String> attrValues;
			attrValues = configuration.getAttribute(IPTPLaunchConfigurationConstants.LAUNCH_ATTR_MAP,
					Collections.EMPTY_MAP);
			int idx = getResourceManagerNameIndex(configuration.getAttribute(
					IPTPLaunchConfigurationConstants.RESOURCE_MANAGER_NAME, EMPTY_STRING));
			if (idx < 0)
				idx = 0;
			resourceManagerCombo.select(idx);
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

			Map<String, IAttributeControl> attributeMap = 
				new HashMap<String, IAttributeControl>();
			
			for (IAttributeControl attrControl : attrControls) {
				IAttribute attr = attrControl.getAttribute();
				attributeMap.put(attr.getDescription().getName(), attrControl);
			}
			
			for (String name : attrValues.keySet()) {
				String value = attrValues.get(name);
				IAttributeControl attrControl = attributeMap.get(name);
				attrControl.setValue(value);
			}
			
		} catch (CoreException e) {
			setErrorMessage(CoreMessages
					.getFormattedResourceString(
							"CommonTab.common.Exception_occurred_reading_configuration_EXCEPTION",
							e.getStatus().getMessage()));
		} catch (IllegalValue e) {
			setErrorMessage(CoreMessages
					.getFormattedResourceString(
							"CommonTab.common.Exception_occurred_reading_configuration_EXCEPTION",
							e.getMessage()));
		}
		
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.debug.ui.ILaunchConfigurationTab#isValid(org.eclipse.debug.core.ILaunchConfiguration)
	 */
	public boolean isValid(ILaunchConfiguration configuration) {
		setErrorMessage(null);
		setMessage(null);
		try {
			Map<String, String> attrValues;
			attrValues = configuration.getAttribute(IPTPLaunchConfigurationConstants.LAUNCH_ATTR_MAP,
					Collections.EMPTY_MAP);
			String rmName = configuration.getAttribute(
					IPTPLaunchConfigurationConstants.RESOURCE_MANAGER_NAME, EMPTY_STRING);
			int idx = getResourceManagerNameIndex(rmName);
			if (idx < 0) {
				setErrorMessage("Invalid resource manager name: + " + rmName);
				return false;
			}
			String machName = configuration.getAttribute(
								IPTPLaunchConfigurationConstants.MACHINE_NAME, EMPTY_STRING);
			idx = getMachineNameIndex(machName);
			if (idx < 0) {
				setErrorMessage("Invalid machine name: + " + machName);
				return false;
			}

			String queueName = configuration.getAttribute(
								IPTPLaunchConfigurationConstants.QUEUE_NAME, EMPTY_STRING);
			idx = getQueueNameIndex(queueName);
			if (idx < 0) {
				setErrorMessage("Invalid queue name: + " + queueName);
				return false;
			}
			for (IAttributeControl attrControl : attrControls) {
				IAttribute attr = attrControl.getAttribute();
				IAttributeDescription desc = attr.getDescription();
				if (attrValues.containsKey(desc.getName())) {
					String newValue = attrValues.get(desc.getName());
					boolean valid = attr.isValid(newValue);
					if (!valid) {
						// try to set an invalid value on purpose to get
						// the error message from it.
						try {
							attr.setValue(newValue);
						} catch (IllegalValue e) {
							setErrorMessage(e.getMessage());
							return false;
						}
					}
				}
			}
		} catch (CoreException e) {
			setErrorMessage(CoreMessages
				.getFormattedResourceString(
						"CommonTab.common.Exception_occurred_reading_configuration_EXCEPTION",
						e.getStatus().getMessage()));
			return false;
		}	
		return true;
	}

	/**
	 * @see ILaunchConfigurationTab#performApply(ILaunchConfigurationWorkingCopy)
	 */
	public void performApply(ILaunchConfigurationWorkingCopy configuration) {
		configuration.setAttribute(IPTPLaunchConfigurationConstants.RESOURCE_MANAGER_NAME,
				getResourceManagerName());
		configuration.setAttribute(IPTPLaunchConfigurationConstants.MACHINE_NAME,
				getMachineName());
		configuration.setAttribute(IPTPLaunchConfigurationConstants.QUEUE_NAME,
				getQueueName());
		Map<String, String> valueMap = new HashMap<String, String>();
		for (IAttributeControl control : attrControls) {
			IAttribute attribute;
			attribute = control.getAttribute();
			valueMap.put(attribute.getDescription().getName(), control.getControlText());
		}
		configuration.setAttribute(
				IPTPLaunchConfigurationConstants.LAUNCH_ATTR_MAP,
				valueMap);
	}

	/**
	 * Defaults are empty.
	 * 
	 * @see ILaunchConfigurationTab#setDefaults(ILaunchConfigurationWorkingCopy)
	 */
	public void setDefaults(ILaunchConfigurationWorkingCopy configuration) {
		configuration.setAttribute(IPTPLaunchConfigurationConstants.RESOURCE_MANAGER_NAME,
				getResourceManagerName());
		configuration.setAttribute(IPTPLaunchConfigurationConstants.MACHINE_NAME,
				getMachineName());
		configuration.setAttribute(IPTPLaunchConfigurationConstants.QUEUE_NAME,
				getQueueName());
		Map<String, String> valueMap = new HashMap<String, String>();
		IResourceManager rm = getResourceManager();
		IAttribute[] attrs = rm.getLaunchAttributes(getMachine(), getQueue());
		for (IAttribute attr : attrs) {
			valueMap.put(attr.getDescription().getName(), attr.getStringRep());
		}
		configuration.setAttribute(
				IPTPLaunchConfigurationConstants.LAUNCH_ATTR_MAP,
				valueMap);
	}

	/**
	 * @see ILaunchConfigurationTab#setLaunchConfigurationDialog(ILaunchConfigurationDialog)
	 */
	public void setLaunchConfigurationDialog(ILaunchConfigurationDialog dialog) {
		super.setLaunchConfigurationDialog(dialog);
	}

	private void loadMachineAndQueueCombos() {
		machineCombo.removeAll();
		IResourceManager rm = getResourceManager();
		if (rm == null) {
			return;
		}

		final IPMachine[] macs = rm.getMachines();
		for (int i = 0; i < macs.length; i++) {
			machineCombo.add(macs[i].getElementName());
		}
		machineCombo.select(0);
		
		queueCombo.removeAll();

		final IPQueue[] queues = rm.getQueues();
		for (int i = 0; i < queues.length; i++) {
			queueCombo.add(queues[i].getElementName());
		}
		queueCombo.select(0);
	}

	protected String getFieldContent(IntegerFieldEditor editorField) {
		return getFieldContent(editorField.getStringValue());
	}

	protected IPMachineControl getMachine() {
		IResourceManager rm = getResourceManager();
		if (rm == null) {
			return null;
		}
		IPMachineControl[] macs = rm.getMachineControls();
		int i = 0;
		if (machineCombo != null)
			i = machineCombo.getSelectionIndex();
		return macs[i];
	}
	
	protected String getMachineName() {
		IResourceManager rm = getResourceManager();
		if (rm == null) {
			return "";
		}
		IPMachine[] macs = rm.getMachines();
		int i = 0;
		if (machineCombo != null)
			i = machineCombo.getSelectionIndex();
		return macs[i].getElementName();
	}

	protected int getMachineNameIndex(String machineName) {
		IResourceManager rm = getResourceManager();
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

	protected IPQueueControl getQueue() {
		IResourceManager rm = getResourceManager();
		if (rm == null) {
			return null;
		}
		IPQueueControl[] queues = rm.getQueueControls();
		int i = 0;
		if (queueCombo != null)
			i = queueCombo.getSelectionIndex();
		return queues[i];
	}

	protected String getQueueName() {
		IResourceManager rm = getResourceManager();
		if (rm == null) {
			return "";
		}
		IPQueue[] queues = rm.getQueues();
		int i = 0;
		if (queueCombo != null)
			i = queueCombo.getSelectionIndex();
		return queues[i].getElementName();
	}

	protected int getQueueNameIndex(String queueName) {
		IResourceManager rm = getResourceManager();
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

	protected IResourceManager getResourceManager() {
		IPUniverse universe = PTPCorePlugin.getDefault().getModelManager().getUniverse();
		if (universe == null) {
			return null;
		}
		IResourceManager[] rms = universe.getResourceManagers();
		int i = 0;
		if (resourceManagerCombo != null)
			i = resourceManagerCombo.getSelectionIndex();
		return rms[i];
	}

	protected String getResourceManagerName() {
		IPUniverse universe = PTPCorePlugin.getDefault().getModelManager().getUniverse();
		if (universe == null) {
			return "";
		}
		IResourceManager[] rms = universe.getResourceManagers();
		int i = 0;
		if (resourceManagerCombo != null)
			i = resourceManagerCombo.getSelectionIndex();
		return rms[i].getElementName();
	}

	protected int getResourceManagerNameIndex(String rmName) {
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
}