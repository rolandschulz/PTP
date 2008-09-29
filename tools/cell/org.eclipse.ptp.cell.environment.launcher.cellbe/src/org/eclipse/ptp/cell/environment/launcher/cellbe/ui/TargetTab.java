/**
 * Copyright (c) 2006 IBM Corporation.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - Initial Implementation
 *
 */
package org.eclipse.ptp.cell.environment.launcher.cellbe.ui;

import org.eclipse.cdt.launch.internal.ui.LaunchMessages;
import org.eclipse.cdt.launch.internal.ui.LaunchUIPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.debug.core.ILaunchConfiguration;
import org.eclipse.debug.core.ILaunchConfigurationWorkingCopy;
import org.eclipse.debug.ui.AbstractLaunchConfigurationTab;
import org.eclipse.ptp.cell.environment.launcher.cellbe.debug.Debug;
import org.eclipse.ptp.cell.environment.launcher.cellbe.internal.ITargetLaunchAttributes;
import org.eclipse.ptp.cell.environment.launcher.cellbe.internal.TargetLaunchDelegate;
import org.eclipse.ptp.remotetools.environment.EnvironmentPlugin;
import org.eclipse.ptp.remotetools.environment.launcher.core.IRemoteLaunchAttributes;
import org.eclipse.ptp.remotetools.environment.launcher.core.LinuxPath;
import org.eclipse.ptp.remotetools.environment.launcher.preferences.LaunchPreferences;
import org.eclipse.ptp.utils.ui.swt.ControlsRelationshipHandler;
import org.eclipse.ptp.utils.ui.swt.Frame;
import org.eclipse.ptp.utils.ui.swt.TextGroup;
import org.eclipse.ptp.utils.ui.swt.TextMold;
import org.eclipse.ptp.utils.ui.swt.ToolKit;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Label;


/**
 * 
 * @author Daniel Felix Ferber
 * @since 1.1
 */
public class TargetTab extends AbstractLaunchConfigurationTab {

	protected Combo targetList;
	protected TextGroup remoteDirectoryGroup;
//	protected Label remoteDirectoryLabel;
	protected Button doExportX11;
	protected Button doCleanup;
	protected Button doAutomaticWorkingDir;

	private class TabModifyListener implements ModifyListener,
			SelectionListener {
		public void modifyText(ModifyEvent e) {
			try {
			updateLaunchConfigurationDialog();
			} catch (Exception ee) {
				Debug.POLICY.logError(ee);
		}
		}

		public void widgetDefaultSelected(SelectionEvent e) {
			try {
			updateLaunchConfigurationDialog();
			} catch (Exception ee) {
				Debug.POLICY.logError(ee);
		}
		}

		public void widgetSelected(SelectionEvent e) {
			try {
			updateLaunchConfigurationDialog();
			} catch (Exception ee) {
				Debug.POLICY.logError(ee);
			}
		}
	}

	private TabModifyListener modifyListener;
	private ControlsRelationshipHandler remoteDirectoryEnabler;

	public TargetTab() {
		super();
	}

	public void createControl(Composite parent) {

		this.setErrorMessage(null);
		this.setMessage(Messages.TargetTab_Tab_Message);

		Composite topControl = new Composite(parent, SWT.NONE);
		GridLayout topLayout = new GridLayout();
		topControl.setLayout(topLayout);

		setControl(topControl);

		modifyListener = new TabModifyListener();

		createTargetGroup(topControl);
		createWorkingDirectoryComponent(topControl);
		createOptionsGroup(topControl);
	}

	protected void createTargetGroup(Composite comp) {
		Group group = ToolKit.createGroup(comp, Messages.TargetTab_TargetFrame_Title);

		targetList = ToolKit.createShortDropDownRow(group, Messages.TargetTab_TargetFrame_TargetListLabel);
		String[] targets = EnvironmentPlugin.getDefault().getTargetsManager().getAllConfigNames();
		for (int i = 0; i < targets.length; i++) {
			targetList.add(targets[i]);
		}
		targetList.addSelectionListener(modifyListener);
	}

	protected void createOptionsGroup(Composite comp) {
		Frame frame = new Frame(comp, Messages.TargetTab_OptionsFrame_Title);
		Composite group = frame.getTopUserReservedComposite();

		doExportX11 = createCheckButton(group, Messages.TargetTab_OptionsFrame_ExportX11ButtonLabel);
		doExportX11.addSelectionListener(modifyListener);
		
		Label label = new Label(group, SWT.WRAP);
		label.setText(Messages.TargetTab_OptionsFrame_ExportX11Note);
	}

	protected void createWorkingDirectoryComponent(Composite comp) {
		Frame frame = new Frame(comp, Messages.TargetTab_WorkingDirectoryFrame_Title);
		Composite group = frame.getTopUserReservedComposite();

		doAutomaticWorkingDir = createCheckButton(group, Messages.TargetTab_WorkingDirectoryFrame_UseDefaultButtonLabel);
		doAutomaticWorkingDir.addSelectionListener(modifyListener);
		
		TextMold remoteDirectoryMold = new TextMold(TextMold.LABELABOVE | TextMold.GRID_DATA_ALIGNMENT_FILL | TextMold.GRID_DATA_GRAB_EXCESS_SPACE, Messages.TargetTab_WorkingDirectoryFrame_DirectoryFieldLabel);
		remoteDirectoryGroup = new TextGroup(group, remoteDirectoryMold);
		remoteDirectoryGroup.addModifyListener(modifyListener);
		
//		remoteDirectoryLabel = new Label(group, SWT.WRAP);
//		remoteDirectoryLabel.setText(NLS.bind("Will look like: {0}", ""));

		doCleanup = createCheckButton(group,
				Messages.TargetTab_WorkingDirectoryFrame_CleanUpButtonLabel);
		doCleanup.addSelectionListener(modifyListener);
		
		remoteDirectoryEnabler = new ControlsRelationshipHandler(doAutomaticWorkingDir, remoteDirectoryGroup, false);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.debug.ui.AbstractLaunchConfigurationTab#isValid(org.eclipse.debug.core.ILaunchConfiguration)
	 */
	public boolean isValid(ILaunchConfiguration config) {
		if (! super.isValid(config)) {
			return false;
		}
		
		TargetLaunchDelegate delegate = new TargetLaunchDelegate();
		String remoteDirectory = Messages.TargetTab_WorkingDirectoryFrame_PreviewNotAvailable;
		
		try {
			remoteDirectory = LinuxPath.toString(delegate.getValidatedRemoteDirectory(config));
			delegate.getValidatedTargetControl(config);
		} catch (CoreException e) {
			setErrorMessage(e.getMessage());
			return false;
		} finally {
//			remoteDirectoryLabel.setText(NLS.bind("Path will look like: {0}", remoteDirectory));
//			remoteDirectoryLabel.pack(true); // Force to resized according to new content.
		}
			
		setErrorMessage(null);
		return true;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see ILaunchConfigurationTab#canSave()
	 */
	public boolean canSave() {
		return true;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.debug.ui.ILaunchConfigurationTab#setDefaults(org.eclipse.debug.core.ILaunchConfigurationWorkingCopy)
	 */
	public void setDefaults(ILaunchConfigurationWorkingCopy configuration) {
		/*
		 * By default, if possible, select the first item.
		 */
		String[] targets = EnvironmentPlugin.getDefault().getTargetsManager()
				.getAllConfigNames();
		if (targets.length > 0) {
			configuration.setAttribute(ITargetLaunchAttributes.ATTR_TARGET_ID,
					targets[0]);
		}

		configuration.setAttribute(
				IRemoteLaunchAttributes.ATTR_AUTOMATIC_WORKING_DIRECTORY, 
				IRemoteLaunchAttributes.DEFAULT_AUTOMATIC_WORKING_DIRECTORY
			);
		configuration.setAttribute(
				IRemoteLaunchAttributes.ATTR_SYNC_CLEANUP,
				IRemoteLaunchAttributes.DEFAULT_SYNC_CLEANUP
			);
//		configuration.setAttribute(
//				ILaunchAttributes.ATTR_LOCAL_DIRECTORY,
//				ILaunchAttributes.DEFAULT_LOCAL_DIRECTORY
//			);
		configuration.setAttribute(
				IRemoteLaunchAttributes.ATTR_REMOTE_DIRECTORY,
				LaunchPreferences.getPreferenceStore().getString(LaunchPreferences.ATTR_WORKING_DIRECTORY)
			);
		configuration.setAttribute(
				IRemoteLaunchAttributes.ATTR_USE_FORWARDED_X11,
				IRemoteLaunchAttributes.DEFAULT_USE_FORWARDED_X11
			);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.debug.ui.ILaunchConfigurationTab#initializeFrom(org.eclipse.debug.core.ILaunchConfiguration)
	 */
	public void initializeFrom(ILaunchConfiguration configuration) {
		try {
			int selected = -1;
			String current = null;
			try {
				current = configuration.getAttribute(ITargetLaunchAttributes.ATTR_TARGET_ID, ""); //$NON-NLS-1$
			} catch (CoreException e) {
				current = ""; //$NON-NLS-1$
			}
			for (int i = 0; i < targetList.getItemCount(); i++) {
				if (current.equals(targetList.getItem(i))) {
					selected = i;
				}
			}
			targetList.select(selected);

			doAutomaticWorkingDir.setSelection(
						configuration.getAttribute(
							IRemoteLaunchAttributes.ATTR_AUTOMATIC_WORKING_DIRECTORY,
							IRemoteLaunchAttributes.DEFAULT_AUTOMATIC_WORKING_DIRECTORY
				));
			remoteDirectoryEnabler.manageDependentControls(doAutomaticWorkingDir);
			doCleanup.setSelection(
					configuration.getAttribute(
							IRemoteLaunchAttributes.ATTR_SYNC_CLEANUP, 
							IRemoteLaunchAttributes.DEFAULT_SYNC_CLEANUP
				));
			remoteDirectoryGroup.setString(
					configuration.getAttribute(
							IRemoteLaunchAttributes.ATTR_REMOTE_DIRECTORY,
							LaunchPreferences.getPreferenceStore().getString(LaunchPreferences.ATTR_WORKING_DIRECTORY)
				));
			doExportX11.setSelection(
					configuration.getAttribute(
							IRemoteLaunchAttributes.ATTR_USE_FORWARDED_X11,
							IRemoteLaunchAttributes.DEFAULT_USE_FORWARDED_X11
				));
		} catch (CoreException e) {
			setErrorMessage(LaunchMessages
					.getFormattedString(
							"Launch.common.Exception_occurred_reading_configuration_EXCEPTION", e.getStatus().getMessage())); //$NON-NLS-1$
			LaunchUIPlugin.log(e);
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.debug.ui.ILaunchConfigurationTab#performApply(org.eclipse.debug.core.ILaunchConfigurationWorkingCopy)
	 */
	public void performApply(ILaunchConfigurationWorkingCopy configuration) {
		/*
		 * Target
		 */
		int selection = targetList.getSelectionIndex();
		if (selection > -1) {
			configuration.setAttribute(ITargetLaunchAttributes.ATTR_TARGET_ID,
					targetList.getItem(selection));
		} else {
			configuration.setAttribute(ITargetLaunchAttributes.ATTR_TARGET_ID,
					(String) null);
		}

		configuration.setAttribute(
				IRemoteLaunchAttributes.ATTR_AUTOMATIC_WORKING_DIRECTORY,
				doAutomaticWorkingDir.getSelection()
			);
		configuration.setAttribute(
				IRemoteLaunchAttributes.ATTR_REMOTE_DIRECTORY,
				remoteDirectoryGroup.getString()
			);
		configuration.setAttribute(
				IRemoteLaunchAttributes.ATTR_USE_FORWARDED_X11,
				doExportX11.getSelection()
			);
		configuration.setAttribute(
				IRemoteLaunchAttributes.ATTR_SYNC_CLEANUP,
				doCleanup.getSelection()
			);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.debug.ui.ILaunchConfigurationTab#getName()
	 */
	public String getName() {
		return Messages.TargetTab_Tab_Title;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.debug.ui.AbstractLaunchConfigurationTab#getImage()
	 */
	public Image getImage() {
		return null;
	}
}
