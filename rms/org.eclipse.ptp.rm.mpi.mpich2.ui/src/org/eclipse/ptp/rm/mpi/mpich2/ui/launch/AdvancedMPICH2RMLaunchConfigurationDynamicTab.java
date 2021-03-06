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
import org.eclipse.jface.viewers.CheckStateChangedEvent;
import org.eclipse.jface.viewers.ICheckStateListener;
import org.eclipse.ptp.core.elements.IPQueue;
import org.eclipse.ptp.launch.ui.extensions.RMLaunchValidation;
import org.eclipse.ptp.rm.mpi.mpich2.core.launch.MPICH2LaunchConfiguration;
import org.eclipse.ptp.rm.mpi.mpich2.core.launch.MPICH2LaunchConfigurationDefaults;
import org.eclipse.ptp.rm.mpi.mpich2.ui.MPICH2UIPlugin;
import org.eclipse.ptp.rm.mpi.mpich2.ui.messages.Messages;
import org.eclipse.ptp.rm.ui.launch.BaseRMLaunchConfigurationDynamicTab;
import org.eclipse.ptp.rm.ui.launch.RMLaunchConfigurationDynamicTabDataSource;
import org.eclipse.ptp.rm.ui.launch.RMLaunchConfigurationDynamicTabWidgetListener;
import org.eclipse.ptp.rmsystem.IResourceManager;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Text;

/**
 * 
 * @author Daniel Felix Ferber
 * 
 */
public class AdvancedMPICH2RMLaunchConfigurationDynamicTab extends BaseRMLaunchConfigurationDynamicTab {

	private class DataSource extends RMLaunchConfigurationDynamicTabDataSource {
		private boolean useDefArgs;
		private String args;

		protected DataSource(BaseRMLaunchConfigurationDynamicTab page) {
			super(page);
		}

		@Override
		protected void copyFromFields() throws ValidationException {
			useDefArgs = useArgsDefaultsButton.getSelection();
			args = extractText(argsText);
		}

		@Override
		protected void copyToFields() {
			applyText(argsText, args);
			useArgsDefaultsButton.setSelection(useDefArgs);
		}

		@Override
		protected void copyToStorage() {
			getConfigurationWorkingCopy().setAttribute(MPICH2LaunchConfiguration.ATTR_USEDEFAULTARGUMENTS, useDefArgs);
			getConfigurationWorkingCopy().setAttribute(MPICH2LaunchConfiguration.ATTR_ARGUMENTS, args);
		}

		@Override
		protected void loadDefault() {
			args = MPICH2LaunchConfigurationDefaults.ATTR_ARGUMENTS;
			useDefArgs = MPICH2LaunchConfigurationDefaults.ATTR_USEDEFAULTARGUMENTS;

		}

		@Override
		protected void loadFromStorage() {
			try {
				args = getConfiguration().getAttribute(MPICH2LaunchConfiguration.ATTR_ARGUMENTS,
						MPICH2LaunchConfigurationDefaults.ATTR_ARGUMENTS);
				useDefArgs = getConfiguration().getAttribute(MPICH2LaunchConfiguration.ATTR_USEDEFAULTARGUMENTS,
						MPICH2LaunchConfigurationDefaults.ATTR_USEDEFAULTARGUMENTS);
			} catch (CoreException e) {
				// TODO handle exception?
				MPICH2UIPlugin.log(e);
			}
		}

		@Override
		protected void validateLocal() throws ValidationException {
			if (!useDefArgs && args == null) {
				throw new ValidationException(Messages.AdvancedMPICH2RMLaunchConfigurationDynamicTab_Validation_EmptyArguments);
			}
		}

		protected boolean getUseDefArgs() {
			return useDefArgs;
		}
	}

	private class WidgetListener extends RMLaunchConfigurationDynamicTabWidgetListener implements ICheckStateListener {
		public WidgetListener(BaseRMLaunchConfigurationDynamicTab dynamicTab) {
			super(dynamicTab);
		}

		public void checkStateChanged(CheckStateChangedEvent event) {
			// do nothing
		}

		@Override
		protected void doWidgetSelected(SelectionEvent e) {
			if (e.getSource() == useArgsDefaultsButton) {
				updateControls();
			}
			super.doWidgetSelected(e);
		}
	}

	/**
	 * @since 2.0
	 */
	protected Composite control;
	/**
	 * @since 2.0
	 */
	protected Button useArgsDefaultsButton;
	/**
	 * @since 2.0
	 */
	protected Text argsText;

	/**
	 * @since 2.0
	 */
	public AdvancedMPICH2RMLaunchConfigurationDynamicTab(ILaunchConfigurationDialog dialog) {
		super(dialog);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.eclipse.ptp.launch.ui.extensions.IRMLaunchConfigurationDynamicTab
	 * #createControl(org.eclipse.swt.widgets.Composite,
	 * org.eclipse.ptp.rmsystem.IResourceManagerControl,
	 * org.eclipse.ptp.core.elements.IPQueue)
	 */
	/**
	 * @since 2.0
	 */
	public void createControl(Composite parent, IResourceManager rm, IPQueue queue) throws CoreException {
		control = new Composite(parent, SWT.NONE);
		control.setLayout(new GridLayout());

		final Group argumentsGroup = new Group(control, SWT.NONE);
		argumentsGroup.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false));
		GridLayout layout = new GridLayout();
		layout.numColumns = 2;
		argumentsGroup.setLayout(layout);
		argumentsGroup.setText(Messages.AdvancedMPICH2RMLaunchConfigurationDynamicTab_Label_LaunchArguments);

		useArgsDefaultsButton = new Button(argumentsGroup, SWT.CHECK);
		useArgsDefaultsButton.setText(Messages.AdvancedMPICH2RMLaunchConfigurationDynamicTab_Label_DefaultArguments);
		// useArgsDefaultsButton.setSelection(true);
		GridData gd = new GridData(SWT.FILL, SWT.CENTER, true, false, 2, 1);
		useArgsDefaultsButton.setLayoutData(gd);
		useArgsDefaultsButton.addSelectionListener(getListener());

		Label label = new Label(argumentsGroup, SWT.NONE);
		label.setLayoutData(new GridData());
		label.setText(Messages.AdvancedMPICH2RMLaunchConfigurationDynamicTab_Label_Arguments);

		argsText = new Text(argumentsGroup, SWT.BORDER);
		argsText.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false));
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.eclipse.ptp.launch.ui.extensions.IRMLaunchConfigurationDynamicTab
	 * #getControl()
	 */
	public Control getControl() {
		return control;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.eclipse.ptp.rm.ui.launch.BaseRMLaunchConfigurationDynamicTab#getImage
	 * ()
	 */
	@Override
	public Image getImage() {
		return null;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.eclipse.ptp.rm.ui.launch.BaseRMLaunchConfigurationDynamicTab#getText
	 * ()
	 */
	/**
	 * @since 2.0
	 */
	@Override
	public String getText() {
		return Messages.AdvancedMPICH2RMLaunchConfigurationDynamicTab_Title;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.eclipse.ptp.launch.ui.extensions.IRMLaunchConfigurationDynamicTab
	 * #setDefaults(org.eclipse.debug.core.ILaunchConfigurationWorkingCopy,
	 * org.eclipse.ptp.rmsystem.IResourceManager,
	 * org.eclipse.ptp.core.elements.IPQueue)
	 */
	/**
	 * @since 2.0
	 */
	public RMLaunchValidation setDefaults(ILaunchConfigurationWorkingCopy configuration, IResourceManager rm, IPQueue queue) {
		configuration.setAttribute(MPICH2LaunchConfiguration.ATTR_USEDEFAULTARGUMENTS,
				MPICH2LaunchConfigurationDefaults.ATTR_USEDEFAULTARGUMENTS);
		configuration.setAttribute(MPICH2LaunchConfiguration.ATTR_ARGUMENTS, MPICH2LaunchConfigurationDefaults.ATTR_ARGUMENTS);
		return new RMLaunchValidation(true, null);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ptp.rm.ui.launch.BaseRMLaunchConfigurationDynamicTab#
	 * updateControls()
	 */
	@Override
	public void updateControls() {
		argsText.setEnabled(!useArgsDefaultsButton.getSelection());
		if (getLocalDataSource().getUseDefArgs()) {
			String launchArgs = ""; //$NON-NLS-1$
			try {
				launchArgs = MPICH2LaunchConfiguration.calculateArguments(getLocalDataSource().getConfiguration());
			} catch (CoreException e) {
				// ignore
			}
			argsText.setText(launchArgs);
		}
	}

	private DataSource getLocalDataSource() {
		return (DataSource) super.getDataSource();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ptp.rm.ui.launch.BaseRMLaunchConfigurationDynamicTab#
	 * createDataSource()
	 */
	@Override
	protected RMLaunchConfigurationDynamicTabDataSource createDataSource() {
		return new DataSource(this);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ptp.rm.ui.launch.BaseRMLaunchConfigurationDynamicTab#
	 * createListener()
	 */
	@Override
	protected RMLaunchConfigurationDynamicTabWidgetListener createListener() {
		return new WidgetListener(this);
	}
}
