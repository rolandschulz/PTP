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
package org.eclipse.ptp.launch.ui.tabs;

import java.net.URL;
import java.util.ArrayList;
import java.util.HashSet;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.Path;
import org.eclipse.core.runtime.Status;
import org.eclipse.debug.core.ILaunchConfiguration;
import org.eclipse.debug.core.ILaunchConfigurationWorkingCopy;
import org.eclipse.debug.ui.AbstractLaunchConfigurationTab;
import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.viewers.IStructuredContentProvider;
import org.eclipse.jface.viewers.ListViewer;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.ptp.core.IPTPLaunchConfigurationConstants;
import org.eclipse.ptp.launch.PTPLaunchPlugin;
import org.eclipse.ptp.launch.internal.messages.Messages;
import org.eclipse.ptp.launch.internal.rulesengine.DownloadRule;
import org.eclipse.ptp.launch.internal.rulesengine.UploadRule;
import org.eclipse.ptp.launch.rulesengine.ISynchronizationRule;
import org.eclipse.ptp.launch.rulesengine.OverwritePolicies;
import org.eclipse.ptp.launch.rulesengine.RuleFactory;
import org.eclipse.ptp.launch.ui.IRuleDialog;
import org.eclipse.ptp.launch.ui.RuleDialogFactory;
import org.eclipse.ptp.launch.ui.SynchronizationRuleLabelProvider;
import org.eclipse.ptp.utils.ui.swt.Frame;
import org.eclipse.ptp.utils.ui.swt.FrameMold;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.List;

/**
 * TODO: NEEDS TO BE DOCUMENTED
 * 
 * @author Daniel Felix Ferber
 * @since 1.1
 */
public class EnhancedSynchronizeTab extends AbstractLaunchConfigurationTab {
	protected class TabModifyListener implements ModifyListener, SelectionListener {
		public void modifyText(ModifyEvent e) {
			if (!isEventHandlerEnabled())
				return;
			dataChanged = true;
			updateLaunchConfigurationDialog();
		}

		public void widgetDefaultSelected(SelectionEvent e) {
			if (!isEventHandlerEnabled())
				return;
			dataChanged = true;
			updateLaunchConfigurationDialog();
		}

		public void widgetSelected(SelectionEvent e) {
			if (!isEventHandlerEnabled())
				return;
			dataChanged = true;
			updateLaunchConfigurationDialog();
		}
	}

	/**
	 * @since 4.0
	 */
	public static final String TAB_ID = "org.eclipse.ptp.launch.applicationLaunch.synchronizeTab"; //$NON-NLS-1$

	protected Button addUploadRuleButton;
	protected Button addDownloadRuleButton;
	protected Button editRuleButton;
	protected Button removeRuleButton;
	protected List ruleList;
	protected ListViewer ruleViewer;

	protected Button syncBeforeButton;
	protected Button syncAfterButton;

	protected ArrayList<ISynchronizationRule> rules = new ArrayList<ISynchronizationRule>();
	/**
	 * @since 4.0
	 */
	protected SynchronizationRuleLabelProvider ruleLabelProvider;

	/**
	 * @since 4.0
	 */
	protected boolean dataChanged = false;
	/**
	 * @since 4.0
	 */
	protected int eventHandlerEnabled = 0;
	/**
	 * @since 4.0
	 */
	protected Image tabImage;

	protected TabModifyListener modifyListener;

	public EnhancedSynchronizeTab() {
		super();
		dataChanged = false;
		URL url = PTPLaunchPlugin.getDefault().getBundle().getEntry("/icons/sync.png"); //$NON-NLS-1$
		if (url != null) {
			ImageDescriptor imageDescriptor = ImageDescriptor.createFromURL(url);
			tabImage = imageDescriptor.createImage();
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.debug.ui.AbstractLaunchConfigurationTab#canSave()
	 */
	@Override
	public boolean canSave() {
		return super.canSave() || dataChanged;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.eclipse.debug.ui.ILaunchConfigurationTab#createControl(org.eclipse
	 * .swt.widgets.Composite)
	 */
	public void createControl(Composite parent) {

		this.setErrorMessage(null);
		this.setMessage(Messages.EnhancedSynchronizeTab_Tab_Message);

		Composite topControl = new Composite(parent, SWT.NONE);
		GridLayout topLayout = new GridLayout();
		topControl.setLayout(topLayout);

		setControl(topControl);

		Frame frame = new Frame(topControl, Messages.EnhancedSynchronizeTab_RulesFrame_Title);
		frame.setLayoutData(new GridData(GridData.FILL_HORIZONTAL | GridData.GRAB_HORIZONTAL | GridData.FILL_VERTICAL
				| GridData.GRAB_VERTICAL));
		Composite frameComposite = frame.getComposite();

		modifyListener = new TabModifyListener();

		createRuleListControl(frameComposite);
		createOptionsControl(frameComposite);
		createButtonControl(frameComposite);

		enableEventHandlers();

	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.debug.ui.AbstractLaunchConfigurationTab#dispose()
	 */
	@Override
	public void dispose() {
		super.dispose();

		if (tabImage != null)
			tabImage.dispose();
		tabImage = null;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.debug.ui.AbstractLaunchConfigurationTab#getId()
	 */
	@Override
	public String getId() {
		return TAB_ID;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.debug.ui.AbstractLaunchConfigurationTab#getImage()
	 */
	@Override
	public Image getImage() {
		return tabImage;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.debug.ui.ILaunchConfigurationTab#getName()
	 */
	public String getName() {
		return Messages.EnhancedSynchronizeTab_Tab_Title;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.eclipse.debug.ui.ILaunchConfigurationTab#initializeFrom(org.eclipse
	 * .debug.core.ILaunchConfiguration)
	 */
	// FIXME Set data initialization
	@SuppressWarnings("unchecked")
	public void initializeFrom(ILaunchConfiguration configuration) {
		dataChanged = false;
		try {
			syncAfterButton.setSelection(configuration.getAttribute(IPTPLaunchConfigurationConstants.ATTR_SYNC_AFTER, false));
			syncBeforeButton.setSelection(configuration.getAttribute(IPTPLaunchConfigurationConstants.ATTR_SYNC_BEFORE, false));
			java.util.List<String> ruleList = configuration.getAttribute(IPTPLaunchConfigurationConstants.ATTR_SYNC_RULES,
					new ArrayList<String>());
			rules.clear();
			for (String ruleStr : ruleList) {
				ISynchronizationRule rule = RuleFactory.createRuleFromString(ruleStr);
				rules.add(rule);
			}

			// RemoteLaunchDelegate delegate = new RemoteLaunchDelegate();
			// ruleLabelProvider.setRemoteWorkingDir(LinuxPath.toString(delegate.getValidatedRemoteDirectory(configuration)));
			// ruleLabelProvider.setRemoteWorkingDir(LinuxPath.toString(delegate.getRemoteDirectory(configuration)));

			// For now the working directory will be the directory where the
			// executable
			// is
			// TODO: Add another field to the interface to represent the working
			// dir?
			String execFile = configuration.getAttribute(IPTPLaunchConfigurationConstants.ATTR_EXECUTABLE_PATH, (String) null);
			if (execFile == null) {
				String plugid = PTPLaunchPlugin.getUniqueIdentifier();
				String message = Messages.EnhancedSynchronizeTab_0;
				throw new CoreException(new Status(Status.ERROR, plugid, message));
			}
			/*
			 * String defaultRemoteWorkingDirectory =
			 * "";LaunchPreferences.getPreferenceStore().getString(
			 * LaunchPreferences.ATTR_WORKING_DIRECTORY);
			 */

			Path filePath = new Path(execFile);
			ruleLabelProvider.setRemoteWorkingDir(filePath.removeLastSegments(1).toOSString());

			/*
			 * if( configuration.getAttribute(IRemoteLaunchAttributes.
			 * ATTR_AUTOMATIC_WORKING_DIRECTORY,
			 * IRemoteLaunchAttributes.DEFAULT_AUTOMATIC_WORKING_DIRECTORY)){
			 * ruleLabelProvider.setRemoteWorkingDir(
			 * defaultRemoteWorkingDirectory ); } else {
			 * ruleLabelProvider.setRemoteWorkingDir(
			 * configuration.getAttribute(
			 * IRemoteLaunchAttributes.ATTR_REMOTE_DIRECTORY,
			 * defaultRemoteWorkingDirectory) ); }
			 */

			refreshRuleList();
			// FIXME: Set error messages
		} catch (CoreException e) {
			/*
			 * setErrorMessage(LaunchMessages .getFormattedString(
			 * "Launch.common.Exception_occurred_reading_configuration_EXCEPTION"
			 * , e.getStatus().getMessage())); //$NON-NLS-1$
			 * LaunchUIPlugin.log(e);
			 */
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.eclipse.debug.ui.AbstractLaunchConfigurationTab#isValid(org.eclipse
	 * .debug.core.ILaunchConfiguration)
	 */
	@Override
	public boolean isValid(ILaunchConfiguration config) {
		return super.isValid(config);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.eclipse.debug.ui.ILaunchConfigurationTab#performApply(org.eclipse
	 * .debug.core.ILaunchConfigurationWorkingCopy)
	 */
	public void performApply(ILaunchConfigurationWorkingCopy configuration) {
		configuration.setAttribute(IPTPLaunchConfigurationConstants.ATTR_SYNC_AFTER, syncAfterButton.getSelection());
		configuration.setAttribute(IPTPLaunchConfigurationConstants.ATTR_SYNC_BEFORE, syncBeforeButton.getSelection());
		ArrayList<String> list = new ArrayList<String>();
		for (ISynchronizationRule rule : rules) {
			list.add(rule.toString());
		}
		configuration.setAttribute(IPTPLaunchConfigurationConstants.ATTR_SYNC_RULES, list);
		dataChanged = false;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.eclipse.debug.ui.ILaunchConfigurationTab#setDefaults(org.eclipse.
	 * debug.core.ILaunchConfigurationWorkingCopy)
	 */
	public void setDefaults(ILaunchConfigurationWorkingCopy configuration) {
		configuration.setAttribute(IPTPLaunchConfigurationConstants.ATTR_SYNC_AFTER, false);
		configuration.setAttribute(IPTPLaunchConfigurationConstants.ATTR_SYNC_BEFORE, false);
		configuration.setAttribute(IPTPLaunchConfigurationConstants.ATTR_SYNC_RULES, new ArrayList<String>());
	}

	private void refreshRuleList() {
		ruleViewer.refresh();
	}

	protected Composite createButtonControl(Composite parent) {
		Frame frame = new Frame(parent, FrameMold.COLUMNS_EQUAL_WIDTH, 4);
		Composite frameComposite = frame.getComposite();

		addUploadRuleButton = new Button(frameComposite, SWT.PUSH);
		addUploadRuleButton.setText(Messages.EnhancedSynchronizeTab_RulesFrame_Actions_NewUploadRule);
		addUploadRuleButton.setLayoutData(new GridData(GridData.FILL_HORIZONTAL | GridData.GRAB_HORIZONTAL));
		addUploadRuleButton.addSelectionListener(new SelectionListener() {
			public void widgetDefaultSelected(SelectionEvent e) {
			}

			public void widgetSelected(SelectionEvent e) {
				if (!isEventHandlerEnabled())
					return;
				handleAddUploadRuleButtonPressed();
			}
		});

		addDownloadRuleButton = new Button(frameComposite, SWT.PUSH);
		addDownloadRuleButton.setText(Messages.EnhancedSynchronizeTab_RulesFrame_Actions_DownloadRule);
		addDownloadRuleButton.setLayoutData(new GridData(GridData.FILL_HORIZONTAL | GridData.GRAB_HORIZONTAL));
		addDownloadRuleButton.addSelectionListener(new SelectionListener() {
			public void widgetDefaultSelected(SelectionEvent e) {
			}

			public void widgetSelected(SelectionEvent e) {
				if (!isEventHandlerEnabled())
					return;
				handleAddDownloadRuleButtonPressed();
			}
		});

		editRuleButton = new Button(frameComposite, SWT.PUSH);
		editRuleButton.setText(Messages.EnhancedSynchronizeTab_RulesFrame_Actions_EditSelectedRule);
		editRuleButton.setLayoutData(new GridData(GridData.FILL_HORIZONTAL | GridData.GRAB_HORIZONTAL));
		editRuleButton.addSelectionListener(new SelectionListener() {
			public void widgetDefaultSelected(SelectionEvent e) {
			}

			public void widgetSelected(SelectionEvent e) {
				if (!isEventHandlerEnabled())
					return;
				handleEditRuleButtonPressed();
			}
		});
		editRuleButton.setEnabled(false);

		removeRuleButton = new Button(frameComposite, SWT.PUSH);
		removeRuleButton.setText(Messages.EnhancedSynchronizeTab_RulesFrame_Actions_RemoveSelectedRules);
		removeRuleButton.setLayoutData(new GridData(GridData.FILL_HORIZONTAL | GridData.GRAB_HORIZONTAL));
		removeRuleButton.addSelectionListener(new SelectionListener() {
			public void widgetDefaultSelected(SelectionEvent e) {
			}

			public void widgetSelected(SelectionEvent e) {
				if (!isEventHandlerEnabled())
					return;
				handleRemoveRuleButtonPressed();
			}
		});
		removeRuleButton.setEnabled(false);

		return frame;
	}

	protected Composite createOptionsControl(Composite parent) {
		Frame frame = new Frame(parent, FrameMold.COLUMNS_EQUAL_WIDTH, 2);
		Composite frameComposite = frame.getComposite();

		syncBeforeButton = createCheckButton(frameComposite, Messages.EnhancedSynchronizeTab_RulesFrame_Options_UploadEnabled);
		syncBeforeButton.setLayoutData(new GridData(GridData.FILL_HORIZONTAL | GridData.GRAB_HORIZONTAL));
		syncBeforeButton.addSelectionListener(modifyListener);
		syncAfterButton = createCheckButton(frameComposite, Messages.EnhancedSynchronizeTab_RulesFrame_Options_DownloadEnabled);
		syncAfterButton.addSelectionListener(modifyListener);
		syncAfterButton.setLayoutData(new GridData(GridData.FILL_HORIZONTAL | GridData.GRAB_HORIZONTAL));

		return frame;
	}

	protected Composite createRuleListControl(Composite parent) {
		Frame frame = new Frame(parent, 1);
		frame.setLayoutData(new GridData(GridData.VERTICAL_ALIGN_FILL | GridData.HORIZONTAL_ALIGN_FILL | GridData.GRAB_HORIZONTAL
				| GridData.GRAB_VERTICAL));
		Composite frameComposite = frame.getComposite();

		Label label = new Label(frameComposite, SWT.WRAP);
		label.setText(Messages.EnhancedSynchronizeTab_RulesFrame_Description);
		label.setLayoutData(new GridData(GridData.FILL_HORIZONTAL | GridData.GRAB_HORIZONTAL));
		ruleList = new List(frameComposite, SWT.MULTI | SWT.V_SCROLL | SWT.H_SCROLL);
		GridData gridData = new GridData(GridData.VERTICAL_ALIGN_FILL | GridData.HORIZONTAL_ALIGN_FILL | GridData.GRAB_HORIZONTAL
				| GridData.GRAB_VERTICAL);
		gridData.heightHint = 100;
		ruleList.setLayoutData(gridData);
		ruleList.addSelectionListener(new SelectionListener() {
			public void widgetDefaultSelected(SelectionEvent e) {
			}

			public void widgetSelected(SelectionEvent e) {
				if (!isEventHandlerEnabled())
					return;
				removeRuleButton.setEnabled(ruleList.getSelectionCount() != 0);
				editRuleButton.setEnabled(ruleList.getSelectionCount() == 1);
			}
		});
		ruleList.deselectAll();
		ruleViewer = new ListViewer(ruleList);
		ruleViewer.setContentProvider(new IStructuredContentProvider() {
			public void dispose() {
			}

			public Object[] getElements(Object inputElement) {
				return rules.toArray();
			}

			public void inputChanged(Viewer viewer, Object oldInput, Object newInput) {
				// System.out.println(oldInput.toString());
			}
		});

		ruleLabelProvider = new SynchronizationRuleLabelProvider();
		ruleViewer.setLabelProvider(ruleLabelProvider);
		ruleViewer.setInput(rules);
		return frame;
	}

	protected void disableEventHandlers() {
		eventHandlerEnabled--;
	}

	protected void enableEventHandlers() {
		eventHandlerEnabled++;
	}

	protected void handleAddDownloadRuleButtonPressed() {
		DownloadRule downloadRule = new DownloadRule();
		downloadRule.setOverwritePolicy(OverwritePolicies.ALWAYS);
		newRuleDialog(downloadRule);
	}

	protected void handleAddUploadRuleButtonPressed() {
		UploadRule uploadRule = new UploadRule();
		uploadRule.setDefaultRemoteDirectory(true);
		uploadRule.setRemoteDirectory(null);
		uploadRule.setOverwritePolicy(OverwritePolicies.ALWAYS);
		newRuleDialog(uploadRule);
	}

	protected void handleEditRuleButtonPressed() {
		if (ruleList.getSelectionCount() != 1)
			return;

		int index = ruleList.getSelectionIndex();
		ISynchronizationRule rule = rules.get(index);
		Dialog dialog = RuleDialogFactory.createDialogForRule(getShell(), rule);
		if (dialog != null) {
			if (dialog.open() == Dialog.OK) {
				IRuleDialog ruleDialog = (IRuleDialog) dialog;
				ISynchronizationRule newRule = RuleFactory.duplicateRule(ruleDialog.getRuleWorkingCopy());
				rules.set(index, newRule);
			}
		} else {
			MessageDialog.openError(getShell(), Messages.EnhancedSynchronizeTab_ErrorMessage_NewRule_Title,
					Messages.EnhancedSynchronizeTab_ErrorMessage_NewRule_DontKnowRuleType);
		}
		refreshRuleList();
		dataChanged = true;
		updateLaunchConfigurationDialog();
	}

	protected void handleRemoveRuleButtonPressed() {
		if (ruleList.getSelectionCount() == 0)
			return;

		int indexes[] = ruleList.getSelectionIndices();
		HashSet<ISynchronizationRule> set = new HashSet<ISynchronizationRule>();
		for (int i = 0; i < indexes.length; i++) {
			int index = indexes[i];
			set.add(rules.get(index));
		}
		rules.removeAll(set);
		refreshRuleList();
		dataChanged = true;
		updateLaunchConfigurationDialog();
	}

	protected boolean isEventHandlerEnabled() {
		return eventHandlerEnabled > 0;
	}

	/**
	 * @since 5.0
	 */
	protected void newRuleDialog(ISynchronizationRule rule) {
		Dialog dialog = RuleDialogFactory.createDialogForRule(getShell(), rule);
		if (dialog != null) {
			if (dialog.open() == Dialog.OK) {
				IRuleDialog ruleDialog = (IRuleDialog) dialog;
				ISynchronizationRule newRule = RuleFactory.duplicateRule(ruleDialog.getRuleWorkingCopy());
				rules.add(newRule);
			}
		} else {
			MessageDialog.openError(getShell(), Messages.EnhancedSynchronizeTab_ErrorMessage_NewRule_Title,
					Messages.EnhancedSynchronizeTab_ErrorMessage_NewRule_DontKnowRuleType);
		}
		refreshRuleList();
		dataChanged = true;
		updateLaunchConfigurationDialog();
	}
}
