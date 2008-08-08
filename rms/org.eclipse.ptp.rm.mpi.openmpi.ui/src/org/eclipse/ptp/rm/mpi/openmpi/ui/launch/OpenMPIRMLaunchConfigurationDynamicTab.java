/*******************************************************************************
 * Copyright (c) 2008 IBM Corporation.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 ******************************************************************************/
package org.eclipse.ptp.rm.mpi.openmpi.ui.launch;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.core.runtime.Assert;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.debug.core.ILaunchConfiguration;
import org.eclipse.debug.core.ILaunchConfigurationWorkingCopy;
import org.eclipse.ptp.core.attributes.IAttribute;
import org.eclipse.ptp.core.attributes.IllegalValueException;
import org.eclipse.ptp.core.elements.IPQueue;
import org.eclipse.ptp.core.elements.IResourceManager;
import org.eclipse.ptp.core.elements.attributes.JobAttributes;
import org.eclipse.ptp.launch.ui.extensions.AbstractRMLaunchConfigurationDynamicTab;
import org.eclipse.ptp.launch.ui.extensions.RMLaunchValidation;
import org.eclipse.ptp.rm.mpi.openmpi.core.OpenMPILaunchAttributes;
import org.eclipse.ptp.rm.mpi.openmpi.ui.OpenMPIUIPlugin;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Spinner;
import org.eclipse.swt.widgets.TabFolder;
import org.eclipse.swt.widgets.TabItem;
import org.eclipse.swt.widgets.Text;

public class OpenMPIRMLaunchConfigurationDynamicTab extends
		AbstractRMLaunchConfigurationDynamicTab {
	
	private static final String ATTR_BASE = OpenMPIUIPlugin.PLUGIN_ID + ".launchAttributes";
	private static final String ATTR_NUMPROCS = ATTR_BASE + ".numProcs";
	private static final String ATTR_BYSLOT = ATTR_BASE + ".bySlot";
	private static final String ATTR_NOOVERSUBSCRIBE = ATTR_BASE + ".noOversubscribe";
	private static final String ATTR_NOLOCAL = ATTR_BASE + ".noLocal";
	private static final String ATTR_PREFIX = ATTR_BASE + ".prefix";
	private static final String ATTR_USEPREFIX = ATTR_BASE + ".usePrefix";
	
	private Composite control;
	private Spinner numProcsSpinner;
	private Text prefixText;
	private Button bySlotButton;
	private Button noOversubscribeButton;
	private Button noLocalButton;
	private Button usePrefixButton;

//	private String hostFile = EMPTY_STRING;
//	private String hostList = EMPTY_STRING;
	private class WidgetListener implements ModifyListener, SelectionListener {
		private boolean listenerEnabled = true;
		
		public void enable() { listenerEnabled = true; }
		public void disable() { listenerEnabled = false; }

		public void modifyText(ModifyEvent evt) {
			if (! listenerEnabled) return;
			Object source = evt.getSource();
			if (source == prefixText || source == numProcsSpinner) {
				fireContentsChanged();
			} else {
				assert false;
			}
		}
		
		public void widgetDefaultSelected(SelectionEvent e) {
			// Empty.
		}
		public void widgetSelected(SelectionEvent e) {
			if (! listenerEnabled) return;
			Object source = e.getSource();
			if (source == bySlotButton || source == noOversubscribeButton || source == noLocalButton || source == usePrefixButton) {
				fireContentsChanged();
				updateControls();
			} else {
				assert false;
			}
		}
	}

	WidgetListener widgetListener = new WidgetListener();
	
	class DataSource {
		int numProcs;
		boolean bySlot;
		boolean noOversubscribe;
		boolean noLocal;
		boolean usePrefix;
		String prefix;
		RMLaunchValidation validation;

		final int numProcsDefault = 1;
		final boolean bySlotDefault = false;
		final boolean noOversubscribeDefault = false;
		final boolean noLocalDefault = false;
		final boolean usePrefixDefault = false;
		final String prefixDefault = null;

		String extractText(Text text) {
			String s = text.getText().trim();
			return (s.length() == 0 ? null : s);
		}
		
		void applyText(Text t, String s) {
			if (s == null) t.setText(EMPTY_STRING);
			else t.setText(s);
		}
		
		void resetValidation() {
			validation = new RMLaunchValidation(true, "");
		}

		void copyFromFields() {
			numProcs = numProcsSpinner.getSelection(); 
			bySlot = bySlotButton.getSelection();
			noOversubscribe = noOversubscribeButton.getSelection();
			noLocal = noLocalButton.getSelection();
			usePrefix = usePrefixButton.getSelection();
			prefix = extractText(prefixText);
		}

		void copyToFields() {
			numProcsSpinner.setSelection(numProcs);
			bySlotButton.setSelection(bySlot);
			noOversubscribeButton.setSelection(noOversubscribe);
			noLocalButton.setSelection(noLocal);
			usePrefixButton.setSelection(usePrefix);
			applyText(prefixText, prefix);
		}

		void validateLocal()  {
			if (numProcs < 1) {
				validation = new RMLaunchValidation(false, "Must specify at least one process");
			}
			if (usePrefix && prefix == null) {
				validation = new RMLaunchValidation(false, "Prefix cannot be empty");
			}
		}
		
		void storeConfig(ILaunchConfigurationWorkingCopy configuration) {
			configuration.setAttribute(ATTR_NUMPROCS, numProcs);
			configuration.setAttribute(ATTR_BYSLOT, bySlot);
			configuration.setAttribute(ATTR_NOOVERSUBSCRIBE, noOversubscribe);
			configuration.setAttribute(ATTR_NOLOCAL, noLocal);
			configuration.setAttribute(ATTR_USEPREFIX, usePrefix);
			configuration.setAttribute(ATTR_PREFIX, prefix);
		}

		void loadConfig(ILaunchConfiguration configuration) {
			try {
				numProcs = configuration.getAttribute(ATTR_NUMPROCS, numProcsDefault);
				bySlot = configuration.getAttribute(ATTR_BYSLOT, bySlotDefault);
				noOversubscribe = configuration.getAttribute(ATTR_NOOVERSUBSCRIBE, noOversubscribeDefault);
				noLocal = configuration.getAttribute(ATTR_NOLOCAL, noLocalDefault);
				usePrefix = configuration.getAttribute(ATTR_USEPREFIX, usePrefixDefault);
				prefix = configuration.getAttribute(ATTR_PREFIX, prefixDefault);
			} catch (CoreException e) {
				validation = new RMLaunchValidation(false, e.getMessage());
			}
		}

		private void loadDefaultConfig() {
			numProcs = numProcsDefault;
			bySlot = bySlotDefault;
			noOversubscribe = noOversubscribeDefault;
			noLocal = noLocalDefault;
			usePrefix = usePrefixDefault;
			prefix = prefixDefault;
		}

		void validateGlobal() {
			// Nothing yet.
		}
		public RMLaunchValidation validateFields() {
			resetValidation();
			copyFromFields();
			validateLocal();
			validateGlobal();
			return validation;
		}

		public RMLaunchValidation initializeFrom(Control control, IResourceManager rm, IPQueue queue, ILaunchConfiguration configuration) {
			resetValidation();
			loadConfig(configuration);
			validateLocal();
			validateGlobal();
			copyToFields();
			return validation;
		}

		public RMLaunchValidation performApply(ILaunchConfigurationWorkingCopy configuration, IResourceManager rm, IPQueue queue) {
			resetValidation();
			copyFromFields();
			validateLocal();
			validateGlobal();
			storeConfig(configuration);
			return validation;
		}
	}
		
	DataSource dataSource = new DataSource();

	public OpenMPIRMLaunchConfigurationDynamicTab(IResourceManager rm) {
	}

	/* (non-Javadoc)
	 * @see org.eclipse.ptp.launch.ui.extensions.IRMLaunchConfigurationDynamicTab#canSave(org.eclipse.swt.widgets.Control, org.eclipse.ptp.rmsystem.IResourceManager, org.eclipse.ptp.core.IPQueue)
	 */
	public RMLaunchValidation canSave(Control control, IResourceManager rm, IPQueue queue) {
		return dataSource.validateFields();
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.ptp.launch.ui.extensions.IRMLaunchConfigurationDynamicTab#createControl(org.eclipse.swt.widgets.Composite, org.eclipse.ptp.rmsystem.IResourceManager, org.eclipse.ptp.core.IPQueue)
	 */
	public void createControl(Composite parent,	IResourceManager rm, IPQueue queue) {
		control = new Composite(parent, SWT.NONE);
		GridLayout layout = new GridLayout();
		control.setLayout(layout);

		final TabFolder tabFolder = new TabFolder(control, SWT.NONE);
		tabFolder.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));

		final TabItem simpleTabItem = new TabItem(tabFolder, SWT.NONE);
		simpleTabItem.setText("Simple");

		final Composite simpleComposite = new Composite(tabFolder, SWT.NONE);
		layout = new GridLayout();
		layout.numColumns = 3;
		simpleComposite.setLayout(layout);
		simpleTabItem.setControl(simpleComposite);

		Label label  = new Label(simpleComposite, SWT.NONE);
		label.setText("Number of processes:");

		numProcsSpinner = new Spinner(simpleComposite, SWT.BORDER);
		numProcsSpinner.addModifyListener(widgetListener);
		numProcsSpinner.setLayoutData(new GridData(SWT.LEFT, SWT.CENTER, false, false, 2, 1));

		final Group optionsGroup = new Group(simpleComposite, SWT.NONE);
		optionsGroup.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 3, 1));
		optionsGroup.setText("Options");
		layout = new GridLayout();
		layout.numColumns = 3;
		optionsGroup.setLayout(layout);

		bySlotButton = new Button(optionsGroup, SWT.CHECK);
		bySlotButton.addSelectionListener(widgetListener);
		bySlotButton.setText("By slot");

		noOversubscribeButton = new Button(optionsGroup, SWT.CHECK);
		noOversubscribeButton.addSelectionListener(widgetListener);
		noOversubscribeButton.setText("No oversubscribe");

		noLocalButton = new Button(optionsGroup, SWT.CHECK);
		noLocalButton.addSelectionListener(widgetListener);
		noLocalButton.setText("No local");

		usePrefixButton = new Button(optionsGroup, SWT.CHECK);
		usePrefixButton.addSelectionListener(widgetListener);
		usePrefixButton.setText("Prefix:");

		prefixText = new Text(optionsGroup, SWT.BORDER);
		prefixText.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 2, 1));
		prefixText.addModifyListener(widgetListener);
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.ptp.launch.ui.extensions.IRMLaunchConfigurationDynamicTab#getAttributes(org.eclipse.ptp.rmsystem.IResourceManager, org.eclipse.ptp.core.IPQueue, org.eclipse.debug.core.ILaunchConfiguration)
	 */
	public IAttribute<?,?,?>[] getAttributes(IResourceManager rm, IPQueue queue,
			ILaunchConfiguration configuration) throws CoreException {
		dataSource.loadConfig(configuration);

		String launchArgs = "-np " + dataSource.numProcs;
		if (dataSource.bySlot) {
			launchArgs += " -byslot";
		}
		if (dataSource.noOversubscribe) {
			launchArgs += " -nooversubscribe";
		}
		if (dataSource.noLocal) {
			launchArgs += " -nolocal";
		}
		if (dataSource.usePrefix && dataSource.prefix != null) {
			launchArgs += " --prefix " + dataSource.prefix;
		}
		
		List<IAttribute<?,?,?>> attrs = new ArrayList<IAttribute<?,?,?>>();
		try {
			attrs.add(JobAttributes.getNumberOfProcessesAttributeDefinition().create(dataSource.numProcs));
		} catch (IllegalValueException e) {
			// TODO: Handle this exception?
			Assert.isTrue(false);
		} 
		attrs.add(OpenMPILaunchAttributes.getLaunchArgumentsAttributeDefinition().create(launchArgs));
		
		return attrs.toArray(new IAttribute<?,?,?>[attrs.size()]);
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
	public RMLaunchValidation initializeFrom(Control control, IResourceManager rm, IPQueue queue, ILaunchConfiguration configuration) {
		widgetListener.disable();
		RMLaunchValidation validation = dataSource.initializeFrom(control, rm, queue, configuration);
		updateControls();
		widgetListener.enable();
		return validation;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.ptp.launch.ui.extensions.IRMLaunchConfigurationDynamicTab#isValid(org.eclipse.debug.core.ILaunchConfiguration, org.eclipse.ptp.rmsystem.IResourceManager, org.eclipse.ptp.core.IPQueue)
	 */
	public RMLaunchValidation isValid(ILaunchConfiguration configuration,
			IResourceManager rm, IPQueue queue) {
		return dataSource.validateFields();
	}

	/* (non-Javadoc)
	 * @see org.eclipse.ptp.launch.ui.extensions.IRMLaunchConfigurationDynamicTab#performApply(org.eclipse.debug.core.ILaunchConfigurationWorkingCopy, org.eclipse.ptp.rmsystem.IResourceManager, org.eclipse.ptp.core.IPQueue)
	 */
	public RMLaunchValidation performApply(ILaunchConfigurationWorkingCopy configuration, IResourceManager rm, IPQueue queue) {
		return dataSource.performApply(configuration, rm, queue);
	}

	/* (non-Javadoc)
	 * @see org.eclipse.ptp.launch.ui.extensions.IRMLaunchConfigurationDynamicTab#setDefaults(org.eclipse.debug.core.ILaunchConfigurationWorkingCopy, org.eclipse.ptp.rmsystem.IResourceManager, org.eclipse.ptp.core.IPQueue)
	 */
	public RMLaunchValidation setDefaults(ILaunchConfigurationWorkingCopy configuration,
			IResourceManager rm, IPQueue queue) {
		// TODO: Set default to number of processes equal to number of hosts in the RM.
		configuration.setAttribute(ATTR_NUMPROCS, dataSource.numProcsDefault);
		configuration.setAttribute(ATTR_BYSLOT, dataSource.bySlotDefault);
		configuration.setAttribute(ATTR_NOOVERSUBSCRIBE, dataSource.noOversubscribeDefault);
		configuration.setAttribute(ATTR_NOLOCAL, dataSource.noLocalDefault);
		configuration.setAttribute(ATTR_USEPREFIX, dataSource.usePrefixDefault);
		configuration.setAttribute(ATTR_PREFIX, dataSource.prefixDefault);
		return new RMLaunchValidation(true, "");
	}

   /**
	 * Update state of controls based on current selections
	 */
	private void updateControls() {
		prefixText.setEnabled(usePrefixButton.getSelection());
	}
}
