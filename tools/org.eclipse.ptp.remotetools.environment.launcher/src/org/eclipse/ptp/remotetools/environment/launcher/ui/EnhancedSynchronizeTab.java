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

import java.net.URL;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;

import org.eclipse.cdt.launch.internal.ui.LaunchMessages;
import org.eclipse.cdt.launch.internal.ui.LaunchUIPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.debug.core.ILaunchConfiguration;
import org.eclipse.debug.core.ILaunchConfigurationWorkingCopy;
import org.eclipse.debug.ui.AbstractLaunchConfigurationTab;
import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.viewers.IStructuredContentProvider;
import org.eclipse.jface.viewers.ListViewer;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.ptp.remotetools.environment.launcher.RemoteLauncherPlugin;
import org.eclipse.ptp.remotetools.environment.launcher.core.IRemoteLaunchAttributes;
import org.eclipse.ptp.remotetools.environment.launcher.data.DownloadRule;
import org.eclipse.ptp.remotetools.environment.launcher.data.ISynchronizationRule;
import org.eclipse.ptp.remotetools.environment.launcher.data.OverwritePolicies;
import org.eclipse.ptp.remotetools.environment.launcher.data.RuleFactory;
import org.eclipse.ptp.remotetools.environment.launcher.data.UploadRule;
import org.eclipse.ptp.remotetools.environment.launcher.preferences.LaunchPreferences;
import org.eclipse.ptp.remotetools.utils.ui.swt.Frame;
import org.eclipse.ptp.remotetools.utils.ui.swt.FrameMold;
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
 * 
 * @author Daniel Felix Ferber
 * @since 1.1
 */
public class EnhancedSynchronizeTab extends AbstractLaunchConfigurationTab {

	protected Button addUploadRuleButton;
	protected Button addDownloadRuleButton;
	protected Button editRuleButton;
	protected Button removeRuleButton;
	protected List ruleList;
	protected ListViewer ruleViewer;
	
	protected Button syncBeforeButton;
	protected Button syncAfterButton;
	
	protected ArrayList rules = new ArrayList();
	protected SynchronizationRuleLabelProvider ruleLabelProvider;

	boolean dataChanged = false;
	int eventHandlerEnabled = 0;
	
	Image tabImage;
	
	public EnhancedSynchronizeTab() {
		super();
		dataChanged = false;
		URL url = RemoteLauncherPlugin.getDefault().getBundle().getEntry("/icons/sync.png"); //$NON-NLS-1$
		if (url != null) {
		    ImageDescriptor imageDescriptor = ImageDescriptor.createFromURL(url);
		    tabImage = imageDescriptor.createImage();
		}
	}

	protected void enableEventHandlers() {
		eventHandlerEnabled++;
	}
	
	protected void disableEventHandlers() {
		eventHandlerEnabled--;
	}
	
	protected boolean isEventHandlerEnabled() {
		return eventHandlerEnabled > 0;
	}

	protected class TabModifyListener implements ModifyListener, SelectionListener {
		public void modifyText(ModifyEvent e) {
			if (! isEventHandlerEnabled()) return;
			dataChanged = true;
			updateLaunchConfigurationDialog();
		}

		public void widgetDefaultSelected(SelectionEvent e) {
			if (! isEventHandlerEnabled()) return;
			dataChanged = true;
			updateLaunchConfigurationDialog();
		}

		public void widgetSelected(SelectionEvent e) {
			if (! isEventHandlerEnabled()) return;
			dataChanged = true;
			updateLaunchConfigurationDialog();
		}
	}

	protected TabModifyListener modifyListener;

	public void createControl(Composite parent) {

		this.setErrorMessage(null);
		this.setMessage(Messages.EnhancedSynchronizeTab_Tab_Message);

		Composite topControl = new Composite(parent, SWT.NONE);
		GridLayout topLayout = new GridLayout();
		topControl.setLayout(topLayout);

		setControl(topControl);
		
		Frame frame = new Frame(topControl, Messages.EnhancedSynchronizeTab_RulesFrame_Title);
		frame.setLayoutData(new GridData(GridData.FILL_HORIZONTAL | GridData.GRAB_HORIZONTAL | GridData.FILL_VERTICAL | GridData.GRAB_VERTICAL));
		Composite frameComposite = frame.getComposite();

		modifyListener = new TabModifyListener();

		createRuleListControl(frameComposite);
		createOptionsControl(frameComposite);
		createButtonControl(frameComposite);
		
		enableEventHandlers();
		
	}

	protected Composite createRuleListControl(Composite parent) {
		Frame frame = new Frame(parent, 1);
		frame.setLayoutData(new GridData(GridData.VERTICAL_ALIGN_FILL | GridData.HORIZONTAL_ALIGN_FILL | GridData.GRAB_HORIZONTAL | GridData.GRAB_VERTICAL));
		Composite frameComposite = frame.getComposite();
		
		Label label = new Label(frameComposite, SWT.WRAP);
		label.setText(Messages.EnhancedSynchronizeTab_RulesFrame_Description);
		label.setLayoutData(new GridData(GridData.FILL_HORIZONTAL | GridData.GRAB_HORIZONTAL));
		ruleList = new List(frameComposite, SWT.MULTI | SWT.V_SCROLL | SWT.H_SCROLL);
		GridData gridData = new GridData(GridData.VERTICAL_ALIGN_FILL | GridData.HORIZONTAL_ALIGN_FILL | GridData.GRAB_HORIZONTAL | GridData.GRAB_VERTICAL);
		gridData.heightHint=100;
		ruleList.setLayoutData(gridData);
		ruleList.addSelectionListener(new SelectionListener() {
			public void widgetDefaultSelected(SelectionEvent e) {
			}
			public void widgetSelected(SelectionEvent e) {
				if (! isEventHandlerEnabled()) return;
				removeRuleButton.setEnabled(ruleList.getSelectionCount() != 0);
				editRuleButton.setEnabled(ruleList.getSelectionCount() == 1);
			}
		});
		ruleList.deselectAll();
		ruleViewer = new ListViewer(ruleList);
		ruleViewer.setContentProvider(new IStructuredContentProvider() {
			public Object[] getElements(Object inputElement) {
				return rules.toArray();
			}

			public void dispose() {
			}
			public void inputChanged(Viewer viewer, Object oldInput, Object newInput) {
//				System.out.println(oldInput.toString());
			}
		});
		
		ruleLabelProvider = new SynchronizationRuleLabelProvider();
		ruleViewer.setLabelProvider(ruleLabelProvider);
		ruleViewer.setInput(rules);
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
				if (! isEventHandlerEnabled()) return;
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
				if (! isEventHandlerEnabled()) return;
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
				if (! isEventHandlerEnabled()) return;
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
				if (! isEventHandlerEnabled()) return;
				handleRemoveRuleButtonPressed();
			}
		});
		removeRuleButton.setEnabled(false);

		return frame;
	}
	

	public boolean isValid(ILaunchConfiguration config) {
		return super.isValid(config);
	}

	/*
	 * (non-Javadoc)
	 * @see org.eclipse.debug.ui.AbstractLaunchConfigurationTab#canSave()
	 */
	public boolean canSave() {
		return super.canSave() || dataChanged;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.debug.ui.ILaunchConfigurationTab#setDefaults(org.eclipse.debug.core.ILaunchConfigurationWorkingCopy)
	 */
	public void setDefaults(ILaunchConfigurationWorkingCopy configuration) {
		configuration.setAttribute(IRemoteLaunchAttributes.ATTR_SYNC_AFTER,
				IRemoteLaunchAttributes.DEFAULT_SYNC_AFTER);
		configuration.setAttribute(IRemoteLaunchAttributes.ATTR_SYNC_BEFORE,
				IRemoteLaunchAttributes.DEFAULT_SYNC_BEFORE);
		configuration.setAttribute(IRemoteLaunchAttributes.ATTR_SYNC_RULES, new ArrayList());
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.debug.ui.ILaunchConfigurationTab#initializeFrom(org.eclipse.debug.core.ILaunchConfiguration)
	 */
	public void initializeFrom(ILaunchConfiguration configuration) {
		dataChanged = false;
		try {
			syncAfterButton.setSelection(configuration.getAttribute(
					IRemoteLaunchAttributes.ATTR_SYNC_AFTER,
					IRemoteLaunchAttributes.DEFAULT_SYNC_AFTER));
			syncBeforeButton.setSelection(configuration.getAttribute(
					IRemoteLaunchAttributes.ATTR_SYNC_BEFORE,
					IRemoteLaunchAttributes.DEFAULT_SYNC_BEFORE));
			java.util.List list = configuration.getAttribute(
					IRemoteLaunchAttributes.ATTR_SYNC_RULES,
					new ArrayList());
			rules.clear();
			for (Iterator iter = list.iterator(); iter.hasNext();) {
				String string = (String) iter.next();
				ISynchronizationRule rule = RuleFactory.createRuleFromString(string);
				rules.add(rule);
			}

//			RemoteLaunchDelegate delegate = new RemoteLaunchDelegate();
//			ruleLabelProvider.setRemoteWorkingDir(LinuxPath.toString(delegate.getValidatedRemoteDirectory(configuration)));
//			ruleLabelProvider.setRemoteWorkingDir(LinuxPath.toString(delegate.getRemoteDirectory(configuration)));
			
			String defaultRemoteWorkingDirectory = LaunchPreferences.getPreferenceStore().getString(
					LaunchPreferences.ATTR_WORKING_DIRECTORY);
			
			if( configuration.getAttribute(IRemoteLaunchAttributes.ATTR_AUTOMATIC_WORKING_DIRECTORY, IRemoteLaunchAttributes.DEFAULT_AUTOMATIC_WORKING_DIRECTORY)){
				ruleLabelProvider.setRemoteWorkingDir( defaultRemoteWorkingDirectory );
			} else {
				ruleLabelProvider.setRemoteWorkingDir( configuration.getAttribute(IRemoteLaunchAttributes.ATTR_REMOTE_DIRECTORY, defaultRemoteWorkingDirectory) );
			}
			
			refreshRuleList();
		} catch (CoreException e) {
			setErrorMessage(LaunchMessages
					.getFormattedString(
							"Launch.common.Exception_occurred_reading_configuration_EXCEPTION", e.getStatus().getMessage())); //$NON-NLS-1$
			LaunchUIPlugin.log(e);
		}
	}

	private void refreshRuleList() {
//		ruleList.removeAll();
//		for (Iterator iter = rules.iterator(); iter.hasNext();) {
//			ISynchronizationRule rule = (ISynchronizationRule) iter.next();
//			ruleList.add(rule.toLabel());
//		}
//		IStructuredSelection selection = (IStructuredSelection) ruleViewer.getSelection();
//		Object firstElement = selection.getFirstElement();
		ruleViewer.refresh();
//		if (firstElement != null) {
//			selection = new StructuredSelection(new Object[] {firstElement});
//			ruleViewer.setSelection(selection, true);
//		}
	}
	
	public void performApply(ILaunchConfigurationWorkingCopy configuration) {
		configuration.setAttribute(IRemoteLaunchAttributes.ATTR_SYNC_AFTER, syncAfterButton.getSelection());
		configuration.setAttribute(IRemoteLaunchAttributes.ATTR_SYNC_BEFORE, syncBeforeButton.getSelection());
		ArrayList list = new ArrayList();
		for (Iterator iter = rules.iterator(); iter.hasNext();) {
			ISynchronizationRule rule = (ISynchronizationRule) iter.next();
			list.add(rule.toString());
		}
		configuration.setAttribute(IRemoteLaunchAttributes.ATTR_SYNC_RULES, list);
		dataChanged = false;
	}

	public String getName() {
		return Messages.EnhancedSynchronizeTab_Tab_Title;
	}

	public Image getImage() {
		return tabImage;
	}

	protected void handleAddUploadRuleButtonPressed() {
		UploadRule uploadRule = new UploadRule();
		uploadRule.setDefaultRemoteDirectory(true);
		uploadRule.setRemoteDirectory(null);
		uploadRule.setOverwritePolicy(OverwritePolicies.ALWAYS);
		newRuleDialog(uploadRule);
	}
	
	protected void handleAddDownloadRuleButtonPressed() {
		DownloadRule downloadRule = new DownloadRule();
		downloadRule.setOverwritePolicy(OverwritePolicies.ALWAYS);
		newRuleDialog(downloadRule);
	}
	
	protected void newRuleDialog(ISynchronizationRule rule) {
		Dialog dialog = RuleDialogFactory.createDialogForRule(getShell(), rule);
		if (dialog != null) {
			if (dialog.open() == Dialog.OK) {
				IRuleDialog ruleDialog = (IRuleDialog) dialog;
				ISynchronizationRule newRule = RuleFactory.duplicateRule(ruleDialog.getRuleWorkingCopy());
				rules.add(newRule);
			}
		} else {
			MessageDialog.openError(getShell(), Messages.EnhancedSynchronizeTab_ErrorMessage_NewRule_Title,	Messages.EnhancedSynchronizeTab_ErrorMessage_NewRule_DontKnowRuleType);
		}
		refreshRuleList();
		dataChanged = true;
		updateLaunchConfigurationDialog();
	}

	protected void handleEditRuleButtonPressed() {
		if (ruleList.getSelectionCount() != 1) return;
		
		int index = ruleList.getSelectionIndex();
		ISynchronizationRule rule = (ISynchronizationRule) rules.get(index);
		Dialog dialog = RuleDialogFactory.createDialogForRule(getShell(), rule);
		if (dialog != null) {
			if (dialog.open() == Dialog.OK) {
				IRuleDialog ruleDialog = (IRuleDialog) dialog;
				ISynchronizationRule newRule = RuleFactory.duplicateRule(ruleDialog.getRuleWorkingCopy());
				rules.set(index, newRule);
			}
		} else {
			MessageDialog.openError(getShell(), Messages.EnhancedSynchronizeTab_ErrorMessage_NewRule_Title,	Messages.EnhancedSynchronizeTab_ErrorMessage_NewRule_DontKnowRuleType);
		}
		refreshRuleList();
		dataChanged = true;
		updateLaunchConfigurationDialog();
	}
	
	protected void handleRemoveRuleButtonPressed() {		
		if (ruleList.getSelectionCount() == 0) return;

		int indexes[] = ruleList.getSelectionIndices();
		HashSet set = new HashSet();
		for (int i = 0; i < indexes.length; i++) {
			int index = indexes[i];
			set.add(rules.get(index));
		}
		rules.removeAll(set);
		refreshRuleList();
		dataChanged = true;
		updateLaunchConfigurationDialog();
	}
	
	public void dispose() {
		super.dispose();

		if (tabImage != null) tabImage.dispose();
		tabImage = null;
	}
}
