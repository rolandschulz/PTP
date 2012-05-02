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
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.Path;
import org.eclipse.debug.core.ILaunchConfiguration;
import org.eclipse.debug.core.ILaunchConfigurationWorkingCopy;
import org.eclipse.debug.ui.ILaunchConfigurationDialog;
import org.eclipse.debug.ui.ILaunchConfigurationTab;
import org.eclipse.ptp.core.IPTPLaunchConfigurationConstants;
import org.eclipse.ptp.core.util.LaunchUtils;
import org.eclipse.ptp.launch.messages.Messages;
import org.eclipse.ptp.remote.core.IRemoteConnection;
import org.eclipse.ptp.remote.core.IRemoteConnectionManager;
import org.eclipse.ptp.remote.core.IRemoteServices;
import org.eclipse.ptp.remote.ui.IRemoteUIFileManager;
import org.eclipse.ptp.remote.ui.IRemoteUIServices;
import org.eclipse.ptp.remote.ui.PTPRemoteUIPlugin;
import org.eclipse.ptp.rmsystem.IResourceManager;
import org.eclipse.ptp.rmsystem.IResourceManagerComponentConfiguration;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Text;

/**
 * TODO: NEEDS TO BE DOCUMENTED
 */
public class WorkingDirectoryBlock extends LaunchConfigurationTab {
	protected Button useDefaultWorkingDirButton = null;
	protected Text workingDirText = null;
	protected Button workingDirBrowseButton = null;
	protected ILaunchConfiguration launchConfiguration;

	protected class WidgetListener extends SelectionAdapter implements ModifyListener {
		@Override
		public void widgetSelected(SelectionEvent e) {
			Object source = e.getSource();
			if (source == useDefaultWorkingDirButton) {
				handleUseDefaultWorkingDirButtonSelected();
			} else if (source == workingDirBrowseButton) {
				handleWorkingDirBrowseButtonSelected();
			} else {
				updateLaunchConfigurationDialog();
			}
		}

		public void modifyText(ModifyEvent evt) {
			updateLaunchConfigurationDialog();
		}
	}

	protected WidgetListener listener = new WidgetListener();

	/**
	 * @see ILaunchConfigurationTab#createControl(Composite)
	 */
	public void createControl(Composite parent) {
		Composite workingDirComp = new Composite(parent, SWT.NONE);
		workingDirComp.setLayout(createGridLayout(3, false, 0, 0));
		workingDirComp.setLayoutData(spanGridData(GridData.FILL_HORIZONTAL, 5));
		setControl(workingDirComp);

		Label workingDirLabel = new Label(workingDirComp, SWT.NONE);
		workingDirLabel.setText(Messages.WorkingDirectoryBlock_Working_directory_colon);
		workingDirLabel.setLayoutData(spanGridData(-1, 3));

		useDefaultWorkingDirButton = new Button(workingDirComp, SWT.CHECK);
		useDefaultWorkingDirButton.setText(Messages.WorkingDirectoryBlock_Use_default_working_directory);
		useDefaultWorkingDirButton.setLayoutData(spanGridData(-1, 3));
		useDefaultWorkingDirButton.addSelectionListener(listener);

		Label dirLabel = new Label(workingDirComp, SWT.NONE);
		dirLabel.setText(Messages.WorkingDirectoryBlock_Local_directory);

		workingDirText = new Text(workingDirComp, SWT.SINGLE | SWT.BORDER);
		workingDirText.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
		workingDirText.addModifyListener(listener);

		workingDirBrowseButton = createPushButton(workingDirComp, Messages.Tab_common_Browse_1, null);
		workingDirBrowseButton.addSelectionListener(listener);
	}

	/**
	 * Defaults are empty.
	 * 
	 * @see ILaunchConfigurationTab#setDefaults(ILaunchConfigurationWorkingCopy)
	 */
	public void setDefaults(ILaunchConfigurationWorkingCopy configuration) {
		configuration.setAttribute(IPTPLaunchConfigurationConstants.ATTR_WORKING_DIR, (String) null);
	}

	/**
	 * @see ILaunchConfigurationTab#initializeFrom(ILaunchConfiguration)
	 */
	@Override
	public void initializeFrom(ILaunchConfiguration configuration) {
		setLaunchConfiguration(configuration);
		try {
			String wd = configuration.getAttribute(IPTPLaunchConfigurationConstants.ATTR_WORKING_DIR, (String) null);
			workingDirText.setText(EMPTY_STRING);
			if (wd == null) {
				useDefaultWorkingDirButton.setSelection(true);
			} else {
				IPath path = new Path(wd);
				if (path.isAbsolute()) {
					workingDirText.setText(wd);
				}
				useDefaultWorkingDirButton.setSelection(false);
			}
			handleUseDefaultWorkingDirButtonSelected();
		} catch (CoreException e) {
			setErrorMessage(Messages.CommonTab_common_Exception_occurred_reading_configuration_EXCEPTION);
		}
	}

	/**
	 * @see ILaunchConfigurationTab#performApply(ILaunchConfigurationWorkingCopy)
	 */
	public void performApply(ILaunchConfigurationWorkingCopy configuration) {
		String wd = null;
		if (!isDefaultWorkingDirectory()) {
			wd = getFieldContent(workingDirText.getText());
		}
		configuration.setAttribute(IPTPLaunchConfigurationConstants.ATTR_WORKING_DIR, wd);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.debug.ui.ILaunchConfigurationTab#isValid(org.eclipse.debug .core.ILaunchConfiguration)
	 */
	@Override
	public boolean isValid(ILaunchConfiguration configuration) {
		setErrorMessage(null);
		setMessage(null);

		if (!isDefaultWorkingDirectory()) {
			String workingDirPath = getFieldContent(workingDirText.getText());
			if (workingDirPath == null) {
				setErrorMessage(Messages.WorkingDirectoryBlock_7);
				return false;
			}
		}
		return true;
	}

	/**
	 * @see ILaunchConfigurationTab#getName()
	 */
	public String getName() {
		return Messages.WorkingDirectoryBlock_Working_directory;
	}

	/**
	 * @see ILaunchConfigurationTab#setLaunchConfigurationDialog(ILaunchConfigurationDialog)
	 */
	@Override
	public void setLaunchConfigurationDialog(ILaunchConfigurationDialog dialog) {
		super.setLaunchConfigurationDialog(dialog);
	}

	/**
	 * @see org.eclipse.debug.ui.AbstractLaunchConfigurationTab#updateLaunchConfigurationDialog()
	 */
	@Override
	protected void updateLaunchConfigurationDialog() {
		super.updateLaunchConfigurationDialog();
	}

	/**
	 * Returns whether the default working directory is to be used
	 */
	protected boolean isDefaultWorkingDirectory() {
		return useDefaultWorkingDirButton.getSelection();
	}

	/**
	 * Sets the default working directory
	 */
	protected void setDefaultWorkingDir(ILaunchConfiguration configuration) {
		if (configuration == null) {
			workingDirText.setText(System.getProperty("user.dir")); //$NON-NLS-1$
			return;
		}
	}

	/**
	 * The default working dir check box has been toggled.
	 */
	protected void handleUseDefaultWorkingDirButtonSelected() {
		if (isDefaultWorkingDirectory()) {
			setDefaultWorkingDir(getLaunchConfiguration());
			workingDirText.setEnabled(false);
			workingDirBrowseButton.setEnabled(false);
		} else {
			workingDirText.setEnabled(true);
			workingDirBrowseButton.setEnabled(true);
		}
		updateLaunchConfigurationDialog();
	}

	/**
	 * Show a dialog that lets the user select a working directory
	 */
	protected void handleWorkingDirBrowseButtonSelected() {
		IResourceManager rm = LaunchUtils.getResourceManager(getLaunchConfiguration());
		if (rm != null) {
			IResourceManagerComponentConfiguration conf = rm.getControlConfiguration();
			IRemoteServices remoteServices = PTPRemoteUIPlugin.getDefault().getRemoteServices(conf.getRemoteServicesId(),
					getLaunchConfigurationDialog());
			if (remoteServices != null) {
				IRemoteUIServices remoteUIServices = PTPRemoteUIPlugin.getDefault().getRemoteUIServices(remoteServices);
				if (remoteUIServices != null) {
					IRemoteConnectionManager connMgr = remoteServices.getConnectionManager();
					if (connMgr != null) {
						IRemoteConnection conn = connMgr.getConnection(conf.getConnectionName());
						if (conn != null) {
							IRemoteUIFileManager fileManager = remoteUIServices.getUIFileManager();
							if (fileManager != null) {
								fileManager.setConnection(conn);
								fileManager.showConnections(false);
								String path = fileManager.browseDirectory(getShell(), Messages.WorkingDirectoryBlock_0,
										getFieldContent(workingDirText.getText()), 0);
								if (path != null) {
									workingDirText.setText(path.toString());
								}
							} else {
								setErrorMessage(Messages.WorkingDirectoryBlock_1);
							}
						} else {
							setErrorMessage(Messages.WorkingDirectoryBlock_2);
						}
					} else {
						setErrorMessage(Messages.WorkingDirectoryBlock_3);
					}
				} else {
					setErrorMessage(Messages.WorkingDirectoryBlock_4);
				}
			} else {
				setErrorMessage(Messages.WorkingDirectoryBlock_5);
			}
		} else {
			setErrorMessage(Messages.WorkingDirectoryBlock_6);
		}
	}

	/**
	 * @return Returns the launchConfiguration.
	 */
	@Override
	public ILaunchConfiguration getLaunchConfiguration() {
		return launchConfiguration;
	}

	/**
	 * @param launchConfiguration
	 *            The launchConfiguration to set.
	 */
	@Override
	public void setLaunchConfiguration(ILaunchConfiguration launchConfiguration) {
		this.launchConfiguration = launchConfiguration;
	}
}
