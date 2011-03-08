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

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.debug.core.ILaunchConfiguration;
import org.eclipse.debug.core.ILaunchConfigurationWorkingCopy;
import org.eclipse.debug.ui.ILaunchConfigurationDialog;
import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.ptp.core.attributes.IAttribute;
import org.eclipse.ptp.core.attributes.IllegalValueException;
import org.eclipse.ptp.core.elements.IPQueue;
import org.eclipse.ptp.core.elements.attributes.JobAttributes;
import org.eclipse.ptp.launch.ui.extensions.RMLaunchValidation;
import org.eclipse.ptp.rm.mpi.openmpi.core.OpenMPILaunchAttributes;
import org.eclipse.ptp.rm.mpi.openmpi.core.launch.OpenMPILaunchConfiguration;
import org.eclipse.ptp.rm.mpi.openmpi.core.launch.OpenMPILaunchConfigurationDefaults;
import org.eclipse.ptp.rm.mpi.openmpi.ui.OpenMPIUIPlugin;
import org.eclipse.ptp.rm.mpi.openmpi.ui.messages.Messages;
import org.eclipse.ptp.rm.ui.launch.BaseRMLaunchConfigurationDynamicTab;
import org.eclipse.ptp.rm.ui.launch.RMLaunchConfigurationDynamicTabDataSource;
import org.eclipse.ptp.rm.ui.launch.RMLaunchConfigurationDynamicTabWidgetListener;
import org.eclipse.ptp.rmsystem.IResourceManager;
import org.eclipse.ptp.utils.ui.PixelConverter;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Spinner;
import org.eclipse.swt.widgets.Text;

/**
 * 
 * @author Daniel Felix Ferber
 * @since 2.0
 * 
 */
public class BasicOpenMpiRMLaunchConfigurationDynamicTab extends BaseRMLaunchConfigurationDynamicTab {

	/**
	 * @since 2.0
	 */
	public BasicOpenMpiRMLaunchConfigurationDynamicTab(ILaunchConfigurationDialog dialog) {
		super(dialog);
	}

	/**
	 * @since 2.0
	 */
	protected Composite control;
	/**
	 * @since 2.0
	 */
	protected Spinner numProcsSpinner;
	/**
	 * @since 2.0
	 */
	protected Button byNodeButton;
	/**
	 * @since 2.0
	 */
	protected Button bySlotButton;
	/**
	 * @since 2.0
	 */
	protected Button noOversubscribeButton;
	/**
	 * @since 2.0
	 */
	protected Button noLocalButton;
	/**
	 * @since 2.0
	 */
	protected Button usePrefixButton;
	/**
	 * @since 2.0
	 */
	protected Text prefixText;
	/**
	 * @since 2.0
	 */
	protected Text hostFileText;
	/**
	 * @since 2.0
	 */
	protected Button hostFileButton;
	/**
	 * @since 2.0
	 */
	protected Text hostListText;
	/**
	 * @since 2.0
	 */
	protected Button hostListButton;
	/**
	 * @since 2.0
	 */
	protected Button browseButton;

	private class WidgetListener extends RMLaunchConfigurationDynamicTabWidgetListener {
		public WidgetListener(BaseRMLaunchConfigurationDynamicTab dynamicTab) {
			super(dynamicTab);
		}

		@Override
		protected void doModifyText(ModifyEvent e) {
			if (e.getSource() == numProcsSpinner || e.getSource() == prefixText || e.getSource() == hostFileText
					|| e.getSource() == hostListText) {
				// getDataSource().justValidate();
			} else {
				super.doModifyText(e);
			}
		}

		@Override
		protected void doWidgetSelected(SelectionEvent e) {
			if (e.getSource() == byNodeButton || e.getSource() == bySlotButton || e.getSource() == noOversubscribeButton
					|| e.getSource() == noLocalButton || e.getSource() == usePrefixButton) {
				// getDataSource().justValidate();
			} else if (e.getSource() == usePrefixButton || e.getSource() == hostFileButton || e.getSource() == hostListButton) {
				// getDataSource().justValidate();
				updateControls();
			} else {
				super.doWidgetSelected(e);
			}
		}
	}

	private class DataSource extends RMLaunchConfigurationDynamicTabDataSource {

		private int numProcs;
		private boolean byNode;
		private boolean bySlot;
		private boolean noOversubscribe;
		private boolean noLocal;
		private boolean usePrefix;
		private String prefix;
		private boolean useHostFile;
		private String hostFile;
		private boolean useHostList;
		private String hostList;

		protected DataSource(BaseRMLaunchConfigurationDynamicTab page) {
			super(page);
		}

		@Override
		protected void copyFromFields() throws ValidationException {
			numProcs = numProcsSpinner.getSelection();
			byNode = byNodeButton.getSelection();
			bySlot = bySlotButton.getSelection();
			noOversubscribe = noOversubscribeButton.getSelection();
			noLocal = noLocalButton.getSelection();
			usePrefix = usePrefixButton.getSelection();
			prefix = extractText(prefixText);
			useHostFile = hostFileButton.getSelection();
			hostFile = extractText(hostFileText);
			useHostList = hostListButton.getSelection();
			hostList = extractText(hostListText);
		}

		@Override
		protected void copyToFields() {
			numProcsSpinner.setSelection(numProcs);
			byNodeButton.setSelection(byNode);
			bySlotButton.setSelection(bySlot);
			noOversubscribeButton.setSelection(noOversubscribe);
			noLocalButton.setSelection(noLocal);
			usePrefixButton.setSelection(usePrefix);
			applyText(prefixText, prefix);
			applyText(hostFileText, hostFile);
			hostFileButton.setSelection(useHostFile);
			applyText(hostListText, hostListToText(hostList));
			hostListButton.setSelection(useHostList);
		}

		@Override
		protected void copyToStorage() {
			getConfigurationWorkingCopy().setAttribute(OpenMPILaunchConfiguration.ATTR_NUMPROCS, numProcs);
			getConfigurationWorkingCopy().setAttribute(OpenMPILaunchConfiguration.ATTR_BYNODE, byNode);
			getConfigurationWorkingCopy().setAttribute(OpenMPILaunchConfiguration.ATTR_BYSLOT, bySlot);
			getConfigurationWorkingCopy().setAttribute(OpenMPILaunchConfiguration.ATTR_NOOVERSUBSCRIBE, noOversubscribe);
			getConfigurationWorkingCopy().setAttribute(OpenMPILaunchConfiguration.ATTR_NOLOCAL, noLocal);
			getConfigurationWorkingCopy().setAttribute(OpenMPILaunchConfiguration.ATTR_USEPREFIX, usePrefix);
			getConfigurationWorkingCopy().setAttribute(OpenMPILaunchConfiguration.ATTR_PREFIX, prefix);
			getConfigurationWorkingCopy().setAttribute(OpenMPILaunchConfiguration.ATTR_USEHOSTFILE, useHostFile);
			getConfigurationWorkingCopy().setAttribute(OpenMPILaunchConfiguration.ATTR_HOSTFILE, hostFile);
			getConfigurationWorkingCopy().setAttribute(OpenMPILaunchConfiguration.ATTR_USEHOSTLIST, useHostList);
			getConfigurationWorkingCopy().setAttribute(OpenMPILaunchConfiguration.ATTR_HOSTLIST, hostList);
		}

		@Override
		protected void loadDefault() {
			numProcs = OpenMPILaunchConfigurationDefaults.ATTR_NUMPROCS;
			byNode = OpenMPILaunchConfigurationDefaults.ATTR_BYNODE;
			bySlot = OpenMPILaunchConfigurationDefaults.ATTR_BYSLOT;
			noOversubscribe = OpenMPILaunchConfigurationDefaults.ATTR_NOOVERSUBSCRIBE;
			noLocal = OpenMPILaunchConfigurationDefaults.ATTR_NOLOCAL;
			usePrefix = OpenMPILaunchConfigurationDefaults.ATTR_USEPREFIX;
			prefix = OpenMPILaunchConfigurationDefaults.ATTR_PREFIX;
			hostFile = OpenMPILaunchConfigurationDefaults.ATTR_HOSTFILE;
			useHostFile = OpenMPILaunchConfigurationDefaults.ATTR_USEHOSTFILE;
			hostList = OpenMPILaunchConfigurationDefaults.ATTR_HOSTLIST;
			useHostList = OpenMPILaunchConfigurationDefaults.ATTR_USEHOSTLIST;

		}

		@Override
		protected void loadFromStorage() {
			try {
				numProcs = getConfiguration().getAttribute(OpenMPILaunchConfiguration.ATTR_NUMPROCS,
						OpenMPILaunchConfigurationDefaults.ATTR_NUMPROCS);
				byNode = getConfiguration().getAttribute(OpenMPILaunchConfiguration.ATTR_BYNODE,
						OpenMPILaunchConfigurationDefaults.ATTR_BYNODE);
				bySlot = getConfiguration().getAttribute(OpenMPILaunchConfiguration.ATTR_BYSLOT,
						OpenMPILaunchConfigurationDefaults.ATTR_BYSLOT);
				noOversubscribe = getConfiguration().getAttribute(OpenMPILaunchConfiguration.ATTR_NOOVERSUBSCRIBE,
						OpenMPILaunchConfigurationDefaults.ATTR_NOOVERSUBSCRIBE);
				noLocal = getConfiguration().getAttribute(OpenMPILaunchConfiguration.ATTR_NOLOCAL,
						OpenMPILaunchConfigurationDefaults.ATTR_NOLOCAL);
				usePrefix = getConfiguration().getAttribute(OpenMPILaunchConfiguration.ATTR_USEPREFIX,
						OpenMPILaunchConfigurationDefaults.ATTR_USEPREFIX);
				prefix = getConfiguration().getAttribute(OpenMPILaunchConfiguration.ATTR_PREFIX,
						OpenMPILaunchConfigurationDefaults.ATTR_PREFIX);
				hostFile = getConfiguration().getAttribute(OpenMPILaunchConfiguration.ATTR_HOSTFILE,
						OpenMPILaunchConfigurationDefaults.ATTR_HOSTFILE);
				useHostFile = getConfiguration().getAttribute(OpenMPILaunchConfiguration.ATTR_USEHOSTFILE,
						OpenMPILaunchConfigurationDefaults.ATTR_USEHOSTFILE);
				hostList = getConfiguration().getAttribute(OpenMPILaunchConfiguration.ATTR_HOSTLIST,
						OpenMPILaunchConfigurationDefaults.ATTR_HOSTLIST);
				useHostList = getConfiguration().getAttribute(OpenMPILaunchConfiguration.ATTR_USEHOSTLIST,
						OpenMPILaunchConfigurationDefaults.ATTR_USEHOSTLIST);
			} catch (CoreException e) {
				// TODO handle exception?
				OpenMPIUIPlugin.log(e);
			}
		}

		@Override
		protected void validateLocal() throws ValidationException {
			if (numProcs < 1) {
				throw new ValidationException(Messages.BasicOpenMpiRMLaunchConfigurationDynamicTab_Validation_NoProcess);
			}
			if (usePrefix && prefix == null) {
				throw new ValidationException(Messages.BasicOpenMpiRMLaunchConfigurationDynamicTab_Validation_EmptyPrefix);
			}
			if (useHostFile && hostFile == null) {
				throw new ValidationException(Messages.BasicOpenMpiRMLaunchConfigurationDynamicTab_Validation_EmptyHostfile);
			}
			if (useHostList && hostList == null) {
				throw new ValidationException(Messages.BasicOpenMpiRMLaunchConfigurationDynamicTab_Validation_EmptyHostList);
			}
		}

		/**
		 * Convert a comma separated list into one host per line
		 * 
		 * @param list
		 * @return
		 */
		private String hostListToText(String list) {
			if (list == null) {
				return ""; //$NON-NLS-1$
			}
			String result = ""; //$NON-NLS-1$
			String[] values = list.split(","); //$NON-NLS-1$
			for (int i = 0; i < values.length; i++) {
				if (!values[i].equals("")) { //$NON-NLS-1$
					if (i > 0) {
						result += "\n"; //$NON-NLS-1$
					}
					result += values[i];
				}
			}
			return result;
		}
	}

	@Override
	protected RMLaunchConfigurationDynamicTabDataSource createDataSource() {
		return new DataSource(this);
	}

	@Override
	protected RMLaunchConfigurationDynamicTabWidgetListener createListener() {
		return new WidgetListener(this);
	}

	@Override
	public Image getImage() {
		return null;
	}

	@Override
	public String getText() {
		return Messages.BasicOpenMpiRMLaunchConfigurationDynamicTab_Title;
	}

	/**
	 * @since 2.0
	 */
	public void createControl(Composite parent, IResourceManager rm, IPQueue queue) throws CoreException {
		control = new Composite(parent, SWT.NONE);
		GridLayout layout = new GridLayout();
		layout.numColumns = 3;
		control.setLayout(layout);

		Label label = new Label(control, SWT.NONE);
		label.setText(Messages.BasicOpenMpiRMLaunchConfigurationDynamicTab_Label_NumberProcesses);

		numProcsSpinner = new Spinner(control, SWT.BORDER);
		numProcsSpinner.addModifyListener(getListener());
		numProcsSpinner.setLayoutData(new GridData(SWT.LEFT, SWT.CENTER, false, false, 2, 1));

		final Group optionsGroup = new Group(control, SWT.NONE);
		optionsGroup.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 3, 1));
		optionsGroup.setText(Messages.BasicOpenMpiRMLaunchConfigurationDynamicTab_Title_OptionsGroup);
		layout = new GridLayout();
		layout.numColumns = 4;
		optionsGroup.setLayout(layout);

		byNodeButton = new Button(optionsGroup, SWT.CHECK);
		byNodeButton.addSelectionListener(getListener());
		byNodeButton.setText(Messages.BasicOpenMpiRMLaunchConfigurationDynamicTab_Label_ByNode);

		bySlotButton = new Button(optionsGroup, SWT.CHECK);
		bySlotButton.addSelectionListener(getListener());
		bySlotButton.setText(Messages.BasicOpenMpiRMLaunchConfigurationDynamicTab_Label_BySlot);

		noOversubscribeButton = new Button(optionsGroup, SWT.CHECK);
		noOversubscribeButton.addSelectionListener(getListener());
		noOversubscribeButton.setText(Messages.BasicOpenMpiRMLaunchConfigurationDynamicTab_Label_NoOversubscribe);

		noLocalButton = new Button(optionsGroup, SWT.CHECK);
		noLocalButton.addSelectionListener(getListener());
		noLocalButton.setText(Messages.BasicOpenMpiRMLaunchConfigurationDynamicTab_Label_NoLocal);

		usePrefixButton = new Button(optionsGroup, SWT.CHECK);
		usePrefixButton.addSelectionListener(getListener());
		usePrefixButton.setText(Messages.BasicOpenMpiRMLaunchConfigurationDynamicTab_Label_Prefix);

		prefixText = new Text(optionsGroup, SWT.BORDER);
		prefixText.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 3, 1));
		prefixText.addModifyListener(getListener());

		final Group hostGroup = new Group(control, SWT.NONE);
		hostGroup.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true, 2, 1));
		hostGroup.setText(Messages.BasicOpenMpiRMLaunchConfigurationDynamicTab_Title_HostGroup);
		layout = new GridLayout();
		layout.numColumns = 3;
		hostGroup.setLayout(layout);

		hostFileButton = new Button(hostGroup, SWT.CHECK);
		hostFileButton.addSelectionListener(getListener());
		hostFileButton.setText(Messages.BasicOpenMpiRMLaunchConfigurationDynamicTab_Label_HostFile);

		hostFileText = new Text(hostGroup, SWT.BORDER);
		hostFileText.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false));
		hostFileText.addModifyListener(getListener());

		browseButton = new Button(hostGroup, SWT.NONE);
		browseButton.setLayoutData(new GridData(SWT.RIGHT, SWT.CENTER, false, false));
		browseButton.addSelectionListener(getListener());
		PixelConverter pixelconverter = new PixelConverter(control);
		GridData gd = new GridData(SWT.RIGHT, SWT.CENTER, false, false);
		gd.widthHint = pixelconverter.convertHorizontalDLUsToPixels(IDialogConstants.BUTTON_WIDTH);
		browseButton.setLayoutData(gd);
		browseButton.setText(Messages.BasicOpenMpiRMLaunchConfigurationDynamicTab_Label_Browse);

		hostListButton = new Button(hostGroup, SWT.CHECK);
		hostListButton.setLayoutData(new GridData(SWT.LEFT, SWT.TOP, false, false));
		hostListButton.addSelectionListener(getListener());
		hostListButton.setText(Messages.BasicOpenMpiRMLaunchConfigurationDynamicTab_Title_HostList);

		hostListText = new Text(hostGroup, SWT.V_SCROLL | SWT.BORDER);
		gd = new GridData(SWT.FILL, SWT.FILL, true, true, 2, 1);
		gd.heightHint = 20;
		hostListText.setLayoutData(gd);
		hostListText.addModifyListener(getListener());

	}

	/**
	 * @since 2.0
	 */
	public IAttribute<?, ?, ?>[] getAttributes(IResourceManager rm, IPQueue queue, ILaunchConfiguration configuration, String mode)
			throws CoreException {

		List<IAttribute<?, ?, ?>> attrs = new ArrayList<IAttribute<?, ?, ?>>();

		int numProcs = configuration.getAttribute(OpenMPILaunchConfiguration.ATTR_NUMPROCS,
				OpenMPILaunchConfigurationDefaults.ATTR_NUMPROCS);
		try {
			attrs.add(JobAttributes.getNumberOfProcessesAttributeDefinition().create(Integer.valueOf(numProcs)));
		} catch (IllegalValueException e) {
			throw new CoreException(new Status(IStatus.ERROR, OpenMPIUIPlugin.getDefault().getBundle().getSymbolicName(),
					Messages.BasicOpenMpiRMLaunchConfigurationDynamicTab_Exception_InvalidConfiguration, e));
		}

		attrs.add(OpenMPILaunchAttributes.getLaunchArgumentsAttributeDefinition().create(
				OpenMPILaunchConfiguration.calculateArguments(configuration)));

		return attrs.toArray(new IAttribute<?, ?, ?>[attrs.size()]);
	}

	public Control getControl() {
		return control;
	}

	/**
	 * @since 2.0
	 */
	public RMLaunchValidation setDefaults(ILaunchConfigurationWorkingCopy configuration, IResourceManager rm, IPQueue queue) {
		configuration.setAttribute(OpenMPILaunchConfiguration.ATTR_NUMPROCS, OpenMPILaunchConfigurationDefaults.ATTR_NUMPROCS);
		configuration.setAttribute(OpenMPILaunchConfiguration.ATTR_BYNODE, OpenMPILaunchConfigurationDefaults.ATTR_BYNODE);
		configuration.setAttribute(OpenMPILaunchConfiguration.ATTR_BYSLOT, OpenMPILaunchConfigurationDefaults.ATTR_BYSLOT);
		configuration.setAttribute(OpenMPILaunchConfiguration.ATTR_NOOVERSUBSCRIBE,
				OpenMPILaunchConfigurationDefaults.ATTR_NOOVERSUBSCRIBE);
		configuration.setAttribute(OpenMPILaunchConfiguration.ATTR_NOLOCAL, OpenMPILaunchConfigurationDefaults.ATTR_NOLOCAL);
		configuration.setAttribute(OpenMPILaunchConfiguration.ATTR_USEPREFIX, OpenMPILaunchConfigurationDefaults.ATTR_USEPREFIX);
		configuration.setAttribute(OpenMPILaunchConfiguration.ATTR_PREFIX, OpenMPILaunchConfigurationDefaults.ATTR_PREFIX);
		configuration
				.setAttribute(OpenMPILaunchConfiguration.ATTR_USEHOSTFILE, OpenMPILaunchConfigurationDefaults.ATTR_USEHOSTFILE);
		configuration.setAttribute(OpenMPILaunchConfiguration.ATTR_HOSTFILE, OpenMPILaunchConfigurationDefaults.ATTR_HOSTFILE);
		configuration
				.setAttribute(OpenMPILaunchConfiguration.ATTR_USEHOSTLIST, OpenMPILaunchConfigurationDefaults.ATTR_USEHOSTLIST);
		configuration.setAttribute(OpenMPILaunchConfiguration.ATTR_HOSTLIST, OpenMPILaunchConfigurationDefaults.ATTR_HOSTLIST);
		return new RMLaunchValidation(true, null);
	}

	@Override
	public void updateControls() {
		prefixText.setEnabled(usePrefixButton.getSelection());
		browseButton.setEnabled(hostFileButton.getSelection());
		hostFileText.setEnabled(hostFileButton.getSelection());
		hostListText.setEnabled(hostListButton.getSelection());
	}
}
