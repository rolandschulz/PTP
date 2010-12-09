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
package org.eclipse.ptp.remotetools.environment.launcher.ui;

import java.util.ArrayList;

import org.eclipse.cdt.debug.core.ICDTLaunchConfigurationConstants;
import org.eclipse.cdt.launch.ui.CLaunchConfigurationTab;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.debug.core.DebugPlugin;
import org.eclipse.debug.core.ILaunchConfiguration;
import org.eclipse.debug.core.ILaunchConfigurationWorkingCopy;
import org.eclipse.osgi.util.NLS;
import org.eclipse.ptp.remotetools.environment.launcher.RemoteLauncherPlugin;
import org.eclipse.ptp.remotetools.environment.launcher.core.IRemoteLaunchAttributes;
import org.eclipse.ptp.remotetools.environment.launcher.internal.LaunchObserverIterator;
import org.eclipse.ptp.remotetools.environment.launcher.messages.Messages;
import org.eclipse.ptp.utils.ui.swt.ToolKit;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Text;

public class LauncherExecutionTab extends CLaunchConfigurationTab {

	protected Text programArgumentsText;

	private Combo observerList;
	private ArrayList observerIDs;
	private Text beforeCommandText;
	private Text afterCommandText;

	private boolean isValid;

	private class TabModifyListener implements ModifyListener, SelectionListener {
		public void modifyText(ModifyEvent e) {
			verifyContent();
			updateLaunchConfigurationDialog();
		}

		public void widgetDefaultSelected(SelectionEvent e) {
			verifyContent();
			updateLaunchConfigurationDialog();
		}

		public void widgetSelected(SelectionEvent e) {
			verifyContent();
			updateLaunchConfigurationDialog();
		}
	}

	private TabModifyListener modifyListener;

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.eclipse.debug.ui.ILaunchConfigurationTab#createControl(org.eclipse
	 * .swt.widgets.Composite)
	 */
	public void createControl(Composite parent) {

		this.setErrorMessage(null);

		GridLayout topLayout = new GridLayout();
		Composite topControl = new Composite(parent, SWT.NONE);
		topControl.setLayout(topLayout);

		setControl(topControl);

		// LaunchUIPlugin.getDefault().getWorkbench().getHelpSystem().setHelp(getControl(),
		// ICDTLaunchHelpContextIds.LAUNCH_CONFIGURATION_DIALOG_ARGUMNETS_TAB);

		modifyListener = new TabModifyListener();

		createArgumentGroup(topControl);
		createTargetObserverGroup(topControl);

		verifyContent();
	}

	protected void createArgumentGroup(Composite comp) {
		Group group = ToolKit.createGroup(comp, Messages.LauncherExecutionTab_ArgumentsFrame_Title);

		programArgumentsText = ToolKit.createTextRow(group, Messages.LauncherExecutionTab_ArgumentsFrame_CommandLineLabel, null);
		programArgumentsText.addModifyListener(modifyListener);
	}

	protected void createTargetObserverGroup(Composite topControl) {
		Group group = ToolKit.createGroup(topControl, Messages.LauncherExecutionTab_ObserverFrame_Title);

		observerList = ToolKit.createShortDropDownRow(group, Messages.LauncherExecutionTab_ObserverFrame_ParserLabel);
		observerList.addSelectionListener(modifyListener);

		beforeCommandText = ToolKit.createTextRow(group, Messages.LauncherExecutionTab_ObserverFrame_BashCommandsBeforeLAbel, null,
				4);
		beforeCommandText.addModifyListener(modifyListener);

		afterCommandText = ToolKit
				.createTextRow(group, Messages.LauncherExecutionTab_ObserverFrame_BashCommandsAfterLabel, null, 4);
		afterCommandText.addModifyListener(modifyListener);
	}

	protected void verifyContent() {
		isValid = true;
		setErrorMessage(null);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.eclipse.debug.ui.ILaunchConfigurationTab#isValid(org.eclipse.debug
	 * .core.ILaunchConfiguration)
	 */
	@Override
	public boolean isValid(ILaunchConfiguration config) {
		/*
		 * If a observer is selected, then the DebugPlugin.ATTR_CAPTURE_OUTPUT
		 * attribute must be true.
		 */
		int selection = observerList.getSelectionIndex();
		if (selection == -1) {
			selection = 0;
		}
		String selectedObserverID = (String) observerIDs.get(selection);
		if (selectedObserverID != null) {
			try {
				if (!config.getAttribute(DebugPlugin.ATTR_CAPTURE_OUTPUT, false)) {
					isValid = false;
					setMessage(Messages.LauncherExecutionTab_Validation_ObserverAndNoConsole);
				}
			} catch (CoreException e) {
				// Nothing
			}
		}

		return super.isValid(config) && isValid;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see ILaunchConfigurationTab#canSave()
	 */
	@Override
	public boolean canSave() {
		return true;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.eclipse.debug.ui.ILaunchConfigurationTab#setDefaults(org.eclipse.
	 * debug.core.ILaunchConfigurationWorkingCopy)
	 */
	public void setDefaults(ILaunchConfigurationWorkingCopy configuration) {
		configuration.setAttribute(ICDTLaunchConfigurationConstants.ATTR_PROGRAM_ARGUMENTS, (String) null);

		configuration.setAttribute(IRemoteLaunchAttributes.ATTR_AFTER_COMMAND, IRemoteLaunchAttributes.DEFAULT_AFTER_COMMAND);
		configuration.setAttribute(IRemoteLaunchAttributes.ATTR_BEFORE_COMMAND, IRemoteLaunchAttributes.DEFAULT_BEFORE_COMMAND);
		configuration.setAttribute(IRemoteLaunchAttributes.ATTR_OUTPUT_OBSERVER, IRemoteLaunchAttributes.DEFAULT_OUTPUT_OBSERVER);
		configuration.setAttribute(IRemoteLaunchAttributes.ATTR_USE_FORWARDED_X11,
				IRemoteLaunchAttributes.DEFAULT_USE_FORWARDED_X11);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.eclipse.debug.ui.ILaunchConfigurationTab#initializeFrom(org.eclipse
	 * .debug.core.ILaunchConfiguration)
	 */
	public void initializeFrom(ILaunchConfiguration configuration) {
		try {
			programArgumentsText.setText(configuration.getAttribute(ICDTLaunchConfigurationConstants.ATTR_PROGRAM_ARGUMENTS, "")); //$NON-NLS-1$

			/*
			 * Generate list of all observer plug-ins
			 */
			observerList.removeAll();
			String current;
			int selected;
			try {
				current = configuration.getAttribute(IRemoteLaunchAttributes.ATTR_OUTPUT_OBSERVER, ""); //$NON-NLS-1$
			} catch (CoreException e) {
				current = ""; //$NON-NLS-1$
			}
			selected = 0;
			observerList.add(Messages.LauncherExecutionTab_ObserverFrame_ObserverCombo_DefaultNoneOption);
			observerIDs = new ArrayList();
			observerIDs.add(null);
			LaunchObserverIterator iterator = RemoteLauncherPlugin.getLaunchObserverIterator();
			int index = 0;
			while (iterator.hasMoreElements()) {
				iterator.nextElement();
				String name = iterator.getName();
				String id = iterator.getID();
				if (name == null)
					continue;
				if (id == null)
					continue;
				observerList.add(name);
				if (current.equals(id)) {
					selected = index;
				}
				observerIDs.add(id);
				index++;
			}
			observerList.select(selected);

			/*
			 * Fill bash command fields
			 */
			try {
				beforeCommandText.setText(configuration.getAttribute(IRemoteLaunchAttributes.ATTR_BEFORE_COMMAND, "")); //$NON-NLS-1$
			} catch (CoreException e) {
				beforeCommandText.setText(null);
			}
			try {
				afterCommandText.setText(configuration.getAttribute(IRemoteLaunchAttributes.ATTR_AFTER_COMMAND, "")); //$NON-NLS-1$
			} catch (CoreException e) {
				afterCommandText.setText(null);
			}

			verifyContent();
		} catch (CoreException e) {
			setErrorMessage(NLS.bind(Messages.LauncherExecutionTab_0, e.getStatus().getMessage()));
			RemoteLauncherPlugin.log(e);
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.eclipse.debug.ui.ILaunchConfigurationTab#performApply(org.eclipse
	 * .debug.core.ILaunchConfigurationWorkingCopy)
	 */
	public void performApply(ILaunchConfigurationWorkingCopy configuration) {
		configuration.setAttribute(ICDTLaunchConfigurationConstants.ATTR_PROGRAM_ARGUMENTS,
				getAttributeValueFrom(programArgumentsText));

		/*
		 * Observer
		 */
		int selection = observerList.getSelectionIndex();
		if (selection == -1) {
			selection = 0;
		}
		configuration.setAttribute(IRemoteLaunchAttributes.ATTR_OUTPUT_OBSERVER, (String) observerIDs.get(selection));

		/*
		 * Bash command fields
		 */
		configuration.setAttribute(IRemoteLaunchAttributes.ATTR_BEFORE_COMMAND, beforeCommandText.getText());
		configuration.setAttribute(IRemoteLaunchAttributes.ATTR_AFTER_COMMAND, afterCommandText.getText());
	}

	/**
	 * Returns the string in the text widget, or <code>null</code> if empty.
	 * 
	 * @return text or <code>null</code>
	 */
	protected String getAttributeValueFrom(Text text) {
		String content = text.getText().trim();
		if (content.length() > 0) {
			return content;
		}
		return null;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.debug.ui.ILaunchConfigurationTab#getName()
	 */
	public String getName() {
		return Messages.LauncherExecutionTab_Tab_Title;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.debug.ui.ILaunchConfigurationTab#getImage()
	 */
	@Override
	public Image getImage() {
		return null;
	}
}
