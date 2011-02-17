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
package org.eclipse.ptp.rm.mpi.mpich2.ui.launch;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.debug.core.ILaunchConfigurationWorkingCopy;
import org.eclipse.debug.ui.ILaunchConfigurationDialog;
import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.ptp.core.elements.IPQueue;
import org.eclipse.ptp.launch.ui.extensions.RMLaunchValidation;
import org.eclipse.ptp.rm.mpi.mpich2.core.launch.MPICH2LaunchConfiguration;
import org.eclipse.ptp.rm.mpi.mpich2.core.launch.MPICH2LaunchConfigurationDefaults;
import org.eclipse.ptp.rm.mpi.mpich2.ui.MPICH2UIPlugin;
import org.eclipse.ptp.rm.mpi.mpich2.ui.messages.Messages;
import org.eclipse.ptp.rm.ui.launch.BaseRMLaunchConfigurationDynamicTab;
import org.eclipse.ptp.rm.ui.launch.RMLaunchConfigurationDynamicTabDataSource;
import org.eclipse.ptp.rm.ui.launch.RMLaunchConfigurationDynamicTabWidgetListener;
import org.eclipse.ptp.rmsystem.IResourceManagerControl;
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
 * 
 */
public class BasicMPICH2RMLaunchConfigurationDynamicTab extends BaseRMLaunchConfigurationDynamicTab {

	private class DataSource extends RMLaunchConfigurationDynamicTabDataSource {

		private int numProcs;
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

		@Override
		protected void copyFromFields() throws ValidationException {
			numProcs = numProcsSpinner.getSelection();
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
			getConfigurationWorkingCopy().setAttribute(MPICH2LaunchConfiguration.ATTR_NUMPROCS, numProcs);
			getConfigurationWorkingCopy().setAttribute(MPICH2LaunchConfiguration.ATTR_NOLOCAL, noLocal);
			getConfigurationWorkingCopy().setAttribute(MPICH2LaunchConfiguration.ATTR_USEPREFIX, usePrefix);
			getConfigurationWorkingCopy().setAttribute(MPICH2LaunchConfiguration.ATTR_PREFIX, prefix);
			getConfigurationWorkingCopy().setAttribute(MPICH2LaunchConfiguration.ATTR_USEHOSTFILE, useHostFile);
			getConfigurationWorkingCopy().setAttribute(MPICH2LaunchConfiguration.ATTR_HOSTFILE, hostFile);
			getConfigurationWorkingCopy().setAttribute(MPICH2LaunchConfiguration.ATTR_USEHOSTLIST, useHostList);
			getConfigurationWorkingCopy().setAttribute(MPICH2LaunchConfiguration.ATTR_HOSTLIST, hostList);
		}

		@Override
		protected void loadDefault() {
			numProcs = MPICH2LaunchConfigurationDefaults.ATTR_NUMPROCS;
			noLocal = MPICH2LaunchConfigurationDefaults.ATTR_NOLOCAL;
			usePrefix = MPICH2LaunchConfigurationDefaults.ATTR_USEPREFIX;
			prefix = MPICH2LaunchConfigurationDefaults.ATTR_PREFIX;
			hostFile = MPICH2LaunchConfigurationDefaults.ATTR_HOSTFILE;
			useHostFile = MPICH2LaunchConfigurationDefaults.ATTR_USEHOSTFILE;
			hostList = MPICH2LaunchConfigurationDefaults.ATTR_HOSTLIST;
			useHostList = MPICH2LaunchConfigurationDefaults.ATTR_USEHOSTLIST;

		}

		@Override
		protected void loadFromStorage() {
			try {
				numProcs = getConfiguration().getAttribute(MPICH2LaunchConfiguration.ATTR_NUMPROCS,
						MPICH2LaunchConfigurationDefaults.ATTR_NUMPROCS);
				noLocal = getConfiguration().getAttribute(MPICH2LaunchConfiguration.ATTR_NOLOCAL,
						MPICH2LaunchConfigurationDefaults.ATTR_NOLOCAL);
				usePrefix = getConfiguration().getAttribute(MPICH2LaunchConfiguration.ATTR_USEPREFIX,
						MPICH2LaunchConfigurationDefaults.ATTR_USEPREFIX);
				prefix = getConfiguration().getAttribute(MPICH2LaunchConfiguration.ATTR_PREFIX,
						MPICH2LaunchConfigurationDefaults.ATTR_PREFIX);
				hostFile = getConfiguration().getAttribute(MPICH2LaunchConfiguration.ATTR_HOSTFILE,
						MPICH2LaunchConfigurationDefaults.ATTR_HOSTFILE);
				useHostFile = getConfiguration().getAttribute(MPICH2LaunchConfiguration.ATTR_USEHOSTFILE,
						MPICH2LaunchConfigurationDefaults.ATTR_USEHOSTFILE);
				hostList = getConfiguration().getAttribute(MPICH2LaunchConfiguration.ATTR_HOSTLIST,
						MPICH2LaunchConfigurationDefaults.ATTR_HOSTLIST);
				useHostList = getConfiguration().getAttribute(MPICH2LaunchConfiguration.ATTR_USEHOSTLIST,
						MPICH2LaunchConfigurationDefaults.ATTR_USEHOSTLIST);
			} catch (CoreException e) {
				// TODO handle exception?
				MPICH2UIPlugin.log(e);
			}
		}

		@Override
		protected void validateLocal() throws ValidationException {
			if (numProcs < 1) {
				throw new ValidationException(Messages.BasicMPICH2RMLaunchConfigurationDynamicTab_Validation_NoProcess);
			}
			if (usePrefix && prefix == null) {
				throw new ValidationException(Messages.BasicMPICH2RMLaunchConfigurationDynamicTab_Validation_EmptyPrefix);
			}
			if (useHostFile && hostFile == null) {
				throw new ValidationException(Messages.BasicMPICH2RMLaunchConfigurationDynamicTab_Validation_EmptyHostfile);
			}
			if (useHostList && hostList == null) {
				throw new ValidationException(Messages.BasicMPICH2RMLaunchConfigurationDynamicTab_Validation_EmptyHostList);
			}
		}
	}

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
			if (e.getSource() == noLocalButton || e.getSource() == usePrefixButton) {
				// getDataSource().justValidate();
			} else if (e.getSource() == usePrefixButton || e.getSource() == hostFileButton || e.getSource() == hostListButton) {
				// getDataSource().justValidate();
				updateControls();
			} else {
				super.doWidgetSelected(e);
			}
		}
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

	/**
	 * @since 2.0
	 */
	public BasicMPICH2RMLaunchConfigurationDynamicTab(ILaunchConfigurationDialog dialog) {
		super(dialog);
	}

	/**
	 * @since 2.0
	 */
	public void createControl(Composite parent, IResourceManagerControl rm, IPQueue queue) throws CoreException {
		control = new Composite(parent, SWT.NONE);
		GridLayout layout = new GridLayout();
		layout.numColumns = 3;
		control.setLayout(layout);

		Label label = new Label(control, SWT.NONE);
		label.setText(Messages.BasicMPICH2RMLaunchConfigurationDynamicTab_Label_NumberProcesses);

		numProcsSpinner = new Spinner(control, SWT.BORDER);
		numProcsSpinner.addModifyListener(getListener());
		numProcsSpinner.setLayoutData(new GridData(SWT.LEFT, SWT.CENTER, false, false, 2, 1));

		final Group optionsGroup = new Group(control, SWT.NONE);
		optionsGroup.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 3, 1));
		optionsGroup.setText(Messages.BasicMPICH2RMLaunchConfigurationDynamicTab_Title_OptionsGroup);
		layout = new GridLayout();
		layout.numColumns = 3;
		optionsGroup.setLayout(layout);

		noLocalButton = new Button(optionsGroup, SWT.CHECK);
		noLocalButton.addSelectionListener(getListener());
		noLocalButton.setText(Messages.BasicMPICH2RMLaunchConfigurationDynamicTab_Label_NoLocal);

		usePrefixButton = new Button(optionsGroup, SWT.CHECK);
		usePrefixButton.addSelectionListener(getListener());
		usePrefixButton.setText(Messages.BasicMPICH2RMLaunchConfigurationDynamicTab_Label_Prefix);

		prefixText = new Text(optionsGroup, SWT.BORDER);
		prefixText.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 2, 1));
		prefixText.addModifyListener(getListener());

		final Group hostGroup = new Group(control, SWT.NONE);
		hostGroup.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true, 2, 1));
		hostGroup.setText(Messages.BasicMPICH2RMLaunchConfigurationDynamicTab_Title_HostGroup);
		layout = new GridLayout();
		layout.numColumns = 3;
		hostGroup.setLayout(layout);

		hostFileButton = new Button(hostGroup, SWT.CHECK);
		hostFileButton.addSelectionListener(getListener());
		hostFileButton.setText(Messages.BasicMPICH2RMLaunchConfigurationDynamicTab_Label_HostFile);

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
		browseButton.setText(Messages.BasicMPICH2RMLaunchConfigurationDynamicTab_Label_Browse);

		hostListButton = new Button(hostGroup, SWT.CHECK);
		hostListButton.setLayoutData(new GridData(SWT.LEFT, SWT.TOP, false, false));
		hostListButton.addSelectionListener(getListener());
		hostListButton.setText(Messages.BasicMPICH2RMLaunchConfigurationDynamicTab_Title_HostList);

		hostListText = new Text(hostGroup, SWT.V_SCROLL | SWT.BORDER);
		gd = new GridData(SWT.FILL, SWT.FILL, true, true, 2, 1);
		gd.heightHint = 20;
		hostListText.setLayoutData(gd);
		hostListText.addModifyListener(getListener());
	}

	public Control getControl() {
		return control;
	}

	@Override
	public Image getImage() {
		return null;
	}

	@Override
	public String getText() {
		return Messages.BasicMPICH2RMLaunchConfigurationDynamicTab_Title;
	}

	/**
	 * @since 2.0
	 */
	public RMLaunchValidation setDefaults(ILaunchConfigurationWorkingCopy configuration, IResourceManagerControl rm, IPQueue queue) {
		configuration.setAttribute(MPICH2LaunchConfiguration.ATTR_NUMPROCS, MPICH2LaunchConfigurationDefaults.ATTR_NUMPROCS);
		configuration.setAttribute(MPICH2LaunchConfiguration.ATTR_NOLOCAL, MPICH2LaunchConfigurationDefaults.ATTR_NOLOCAL);
		configuration.setAttribute(MPICH2LaunchConfiguration.ATTR_USEPREFIX, MPICH2LaunchConfigurationDefaults.ATTR_USEPREFIX);
		configuration.setAttribute(MPICH2LaunchConfiguration.ATTR_PREFIX, MPICH2LaunchConfigurationDefaults.ATTR_PREFIX);
		configuration.setAttribute(MPICH2LaunchConfiguration.ATTR_USEHOSTFILE, MPICH2LaunchConfigurationDefaults.ATTR_USEHOSTFILE);
		configuration.setAttribute(MPICH2LaunchConfiguration.ATTR_HOSTFILE, MPICH2LaunchConfigurationDefaults.ATTR_HOSTFILE);
		configuration.setAttribute(MPICH2LaunchConfiguration.ATTR_USEHOSTLIST, MPICH2LaunchConfigurationDefaults.ATTR_USEHOSTLIST);
		configuration.setAttribute(MPICH2LaunchConfiguration.ATTR_HOSTLIST, MPICH2LaunchConfigurationDefaults.ATTR_HOSTLIST);
		return new RMLaunchValidation(true, null);
	}

	@Override
	public void updateControls() {
		prefixText.setEnabled(usePrefixButton.getSelection());
		browseButton.setEnabled(hostFileButton.getSelection());
		hostFileText.setEnabled(hostFileButton.getSelection());
		hostListText.setEnabled(hostListButton.getSelection());
	}

	@Override
	protected RMLaunchConfigurationDynamicTabDataSource createDataSource() {
		return new DataSource(this);
	}

	@Override
	protected RMLaunchConfigurationDynamicTabWidgetListener createListener() {
		return new WidgetListener(this);
	}
}
