/**********************************************************************
 * Copyright (c) 2002,2004 Rational Software Corporation and others.
 * All rights reserved.   This program and the accompanying materials
 * are made available under the terms of the Common Public License v0.5
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v05.html
 * 
 * Contributors: 
 * IBM Rational Software - Initial API and implementation
 * **********************************************************************/
package org.eclipse.cldt.managedbuilder.ui.properties;

import java.lang.reflect.InvocationTargetException;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.SortedMap;

import org.eclipse.cldt.managedbuilder.core.IConfiguration;
import org.eclipse.cldt.managedbuilder.core.IManagedBuildInfo;
import org.eclipse.cldt.managedbuilder.core.IProjectType;
import org.eclipse.cldt.managedbuilder.core.ManagedBuildManager;
import org.eclipse.cldt.managedbuilder.internal.ui.ManagedBuildOptionBlock;
import org.eclipse.cldt.managedbuilder.internal.ui.ManagedBuilderHelpContextIds;
import org.eclipse.cldt.managedbuilder.internal.ui.ManagedBuilderUIMessages;
import org.eclipse.cldt.managedbuilder.internal.ui.ManagedBuilderUIPlugin;
import org.eclipse.cldt.ui.dialogs.ICOptionContainer;
import org.eclipse.cldt.utils.ui.controls.ControlFactory;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.Preferences;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.dialogs.ProgressMonitorDialog;
import org.eclipse.jface.operation.IRunnableWithProgress;
import org.eclipse.jface.preference.IPreferencePageContainer;
import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.layout.FormAttachment;
import org.eclipse.swt.layout.FormData;
import org.eclipse.swt.layout.FormLayout;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.IWorkbenchPropertyPage;
import org.eclipse.ui.actions.WorkspaceModifyDelegatingOperation;
import org.eclipse.ui.dialogs.PropertyPage;
import org.eclipse.ui.help.WorkbenchHelp;

public class BuildPropertyPage extends PropertyPage implements IWorkbenchPropertyPage, 
					IPreferencePageContainer, ICOptionContainer {
	/*
	 * String constants
	 */
	private static final String PREFIX = "BuildPropertyPage";	//$NON-NLS-1$
	private static final String LABEL = PREFIX + ".label";	//$NON-NLS-1$
	private static final String PLATFORM_LABEL = LABEL + ".Platform";	//$NON-NLS-1$
	private static final String CONFIG_LABEL = LABEL + ".Configuration";	//$NON-NLS-1$
	private static final String ALL_CONFS = PREFIX + ".selection.configuration.all";	//$NON-NLS-1$
	private static final String ACTIVE_LABEL = LABEL + ".Active";	//$NON-NLS-1$
	private static final String SETTINGS_LABEL = LABEL + ".Settings";	//$NON-NLS-1$
	private static final String ADD_CONF = LABEL + ".AddConfButton";	//$NON-NLS-1$
	private static final String TIP = PREFIX + ".tip";	//$NON-NLS-1$
	private static final String PLAT_TIP = TIP + ".platform";	//$NON-NLS-1$
	private static final String CONF_TIP = TIP + ".config";	//$NON-NLS-1$
	private static final String ADD_TIP = TIP + ".addconf";	//$NON-NLS-1$
	private static final String MANAGE_TITLE = PREFIX + ".manage.title";	//$NON-NLS-1$
	private static final String ID_SEPARATOR = ".";	//$NON-NLS-1$
	private static final String MSG_CLOSEDPROJECT = "MngMakeProjectPropertyPage.closedproject"; //$NON-NLS-1$
	
	/*
	 * Dialog widgets
	 */
	private Combo projectTypeSelector;
	private Combo configSelector;
	private Button manageConfigs;
				 
	/*
	 * Bookeeping variables
	 */
	private IProjectType[] projectTypes;
	private IProjectType selectedProjectType;
	private IConfiguration[] configurations;
	private IConfiguration selectedConfiguration;
	private Point lastShellSize;
	protected ManagedBuildOptionBlock fOptionBlock;
	protected boolean displayedConfig = false;
		
	/**
	 * Default constructor
	 */
	public BuildPropertyPage() {
		super();
	}

	public void setContainer(IPreferencePageContainer preferencePageContainer) {
	    super.setContainer(preferencePageContainer);
	    if (fOptionBlock == null) {
	    	fOptionBlock = new ManagedBuildOptionBlock(this);
	    }
	}	

	protected Control createContents(Composite parent)  {
		// Create the container we return to the property page editor
		Composite composite = new Composite(parent, SWT.NULL);
		composite.setFont(parent.getFont());
		GridLayout compositeLayout = new GridLayout();
		compositeLayout.numColumns = 1;
		compositeLayout.marginHeight = 0;
		compositeLayout.marginWidth = 0;
		composite.setLayout( compositeLayout );

		IProject project = getProject();
		if (!project.isOpen()) {
			contentForClosedProject(composite);
		} else {
			contentForCProject(composite);
		}

		return composite;
	}

	private void contentForCProject(Composite parent) {
		GridData gd;

		// Initialize the key data
		IManagedBuildInfo info = ManagedBuildManager.getBuildInfo(getProject());
		if (info.getVersion() == null) {
			// Display a message page instead of the properties control
			final Label invalidInfo = new Label(parent, SWT.LEFT);
			invalidInfo.setFont(parent.getFont());
			invalidInfo.setText(ManagedBuilderUIMessages.getResourceString("BuildPropertyPage.error.version_low"));	//$NON-NLS-1$
			invalidInfo.setLayoutData(new GridData(GridData.HORIZONTAL_ALIGN_BEGINNING, GridData.VERTICAL_ALIGN_CENTER, true, true));
			noDefaultAndApplyButton();
			return;
		}
		projectTypes = ManagedBuildManager.getDefinedProjectTypes();
		IProjectType defaultProjectType = info.getManagedProject().getProjectType();

		// Add a config selection area
		Group configGroup = ControlFactory.createGroup(parent, ManagedBuilderUIMessages.getResourceString(ACTIVE_LABEL), 1);
		gd = new GridData(GridData.HORIZONTAL_ALIGN_FILL);
		gd.grabExcessHorizontalSpace = true;
		configGroup.setLayoutData(gd);
		// Use the form layout inside the group composite
		FormLayout form = new FormLayout();
		form.marginHeight = 5;
		form.marginWidth = 5;
		configGroup.setLayout(form);
		
		Label platformLabel = ControlFactory.createLabel(configGroup, ManagedBuilderUIMessages.getResourceString(PLATFORM_LABEL));
		projectTypeSelector = ControlFactory.createSelectCombo(configGroup, getPlatformNames(), defaultProjectType.getName()); 
		//  Note: Changing the project type is not currently handled, so this widget is disabled
		projectTypeSelector.setEnabled(false);
		projectTypeSelector.addListener(SWT.Selection, new Listener () {
			public void handleEvent(Event e) {
				handleProjectTypeSelection();
			}
		});
		projectTypeSelector.setToolTipText(ManagedBuilderUIMessages.getResourceString(PLAT_TIP));
		Label configLabel = ControlFactory.createLabel(configGroup, ManagedBuilderUIMessages.getResourceString(CONFIG_LABEL));
		configSelector = new Combo(configGroup, SWT.READ_ONLY|SWT.DROP_DOWN);
		configSelector.addListener(SWT.Selection, new Listener () {
			public void handleEvent(Event e) {
				handleConfigSelection();
			}
		});
		configSelector.setToolTipText(ManagedBuilderUIMessages.getResourceString(CONF_TIP));
		manageConfigs = ControlFactory.createPushButton(configGroup, ManagedBuilderUIMessages.getResourceString(ADD_CONF));
		manageConfigs.setToolTipText(ManagedBuilderUIMessages.getResourceString(ADD_TIP));
		manageConfigs.addSelectionListener(new SelectionAdapter() {
		public void widgetSelected(SelectionEvent e) {
				handleManageConfig();
			}
		});
		// Now do the form layout for the widgets
		FormData fd = new FormData();
		// Anchor the labels in the centre of their respective combos
		fd.top = new FormAttachment(projectTypeSelector, 0, SWT.CENTER);
		platformLabel.setLayoutData(fd);
		fd = new FormData();
		fd.top = new FormAttachment(configSelector, 0, SWT.CENTER);
		configLabel.setLayoutData(fd);
		// Anchor platform combo left to the config selector
		fd = new FormData();
		fd.left = new FormAttachment(configSelector, 0, SWT.LEFT);
		fd.right = new FormAttachment(100, 0);
		projectTypeSelector.setLayoutData(fd);
		// Anchor button right to combo and left to group
		fd = new FormData();
		fd.top = new FormAttachment(configSelector, 0, SWT.CENTER);
		fd.right = new FormAttachment(100,0);
		manageConfigs.setLayoutData(fd);
		// Anchor config combo left 5 pixels from longest label, top 5% below the centre, and right to the button
		Label longestLabel = (platformLabel.getText().length()>configLabel.getText().length()?platformLabel:configLabel);
		fd = new FormData();
		fd.left = new FormAttachment(longestLabel, 5); 
		fd.top = new FormAttachment(55,0);
		fd.right = new FormAttachment(manageConfigs, -5 , SWT.LEFT);
		configSelector.setLayoutData(fd);		
		
		// Create the Tools Settings, Build Settings, ... Tabbed pane
		Group tabGroup = ControlFactory.createGroup(parent, ManagedBuilderUIMessages.getResourceString(SETTINGS_LABEL), 1);
		gd = new GridData(GridData.FILL_BOTH);
		tabGroup.setLayoutData(gd);
		fOptionBlock.createContents(tabGroup, getElement());

		// Do not call this until the widgets are constructed		
		handleProjectTypeSelection();
		
		WorkbenchHelp.setHelp(parent, ManagedBuilderHelpContextIds.MAN_PROJ_BUILD_PROP);
	}

	private void contentForClosedProject(Composite parent) {
		Label label = new Label(parent, SWT.LEFT);
		label.setText(ManagedBuilderUIMessages.getResourceString(MSG_CLOSEDPROJECT));
		label.setFont(parent.getFont());

		noDefaultAndApplyButton();
	}

	/* 
	 *  (non-Javadoc)
	 * @see org.eclipse.jface.preference.IPreferencePage#performOk()
	 */
	public boolean performOk() {

		//  If the user did not visit this page, then there is nothing to do.
		if (!displayedConfig) return true;

		IRunnableWithProgress runnable = new IRunnableWithProgress() {
			public void run(IProgressMonitor monitor) {
				fOptionBlock.performApply(monitor);
			}
		};
		IRunnableWithProgress op = new WorkspaceModifyDelegatingOperation(runnable);
		try {
			new ProgressMonitorDialog(getShell()).run(false, true, op);
		} catch (InvocationTargetException e) {
			Throwable e1 = e.getTargetException();
			ManagedBuilderUIPlugin.errorDialog(getShell(), ManagedBuilderUIMessages.getResourceString("ManagedProjectPropertyPage.internalError"),e1.toString(), e1); //$NON-NLS-1$
			return false;
		} catch (InterruptedException e) {
			// cancelled
			return false;
		}

		// Write out the build model info
		ManagedBuildManager.setDefaultConfiguration(getProject(), getSelectedConfiguration());
		ManagedBuildManager.saveBuildInfo(getProject(), false);
		return true;
	}

	/*
	 *  (non-Javadoc)
	 * @see org.eclipse.jface.preference.PreferencePage#performDefaults()
	 */
	protected void performDefaults() {
		fOptionBlock.performDefaults();
		super.performDefaults();
	}
	
	private String [] getPlatformNames() {
		String [] names = new String[projectTypes.length];
		for (int index = 0; index < projectTypes.length; ++index) {
			names[index] = projectTypes[index].getName();
		}
		return names;
	}

	/* (non-Javadoc)
	 * @return
	 */
	public IProjectType getSelectedProjectType() {
		return selectedProjectType;
	}

	private void populateConfigurations() {
		// If the config select widget is not there yet, just stop		
		if (configSelector == null) return;
		
		// Find the configurations defined for the platform
		IManagedBuildInfo info = ManagedBuildManager.getBuildInfo(getProject());
		configurations = info.getManagedProject().getConfigurations();
		if (configurations.length == 0) return;
		
		// Clear and replace the contents of the selector widget
		configSelector.removeAll();
		configSelector.setItems(getConfigurationNames());
		
		// Make sure the active configuration is selected
		IConfiguration defaultConfig = info.getDefaultConfiguration();
		int index = configSelector.indexOf(defaultConfig.getName());
		configSelector.select(index == -1 ? 0 : index);
		handleConfigSelection();
	}

	/* (non-Javadoc)
	 * @return an array of names for the configurations defined for the chosen
	 */
	private String [] getConfigurationNames () {
		String [] names = new String[configurations.length /*+ 1*/];
		for (int index = 0; index < configurations.length; ++index) {
			names[index] = configurations[index].getName();
		}
//		names[names.length - 1] = ManagedBuilderUIPlugin.getResourceString(ALL_CONFS);
		return names;
	}

	public void enableConfigSelection (boolean enable) {
		configSelector.setEnabled(enable);
	}

	/* (non-Javadoc)
	 * @return
	 */
	public IConfiguration getSelectedConfiguration() {
		return selectedConfiguration;
	}
	
	/* (non-Javadoc)
	 * @return
	 */
	protected Point getLastShellSize() {
		if (lastShellSize == null) {
			Shell shell = getShell();
			if (shell != null)
				lastShellSize = shell.getSize();
		}
		return lastShellSize;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.jface.preference.IPreferencePageContainer#getPreferenceStore()
	 */
	public IPreferenceStore getPreferenceStore()
	{
		return fOptionBlock.getPreferenceStore();
	}

	/* (non-Javadoc)
	 * Return the IPreferenceStore of the Tool Settings block
	 */
	public IPreferenceStore getToolSettingsPreferenceStore()
	{
		return fOptionBlock.getToolSettingsPreferenceStore();
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.ui.dialog.ICOptionContainer.getPreferences()
	 */
	public Preferences getPreferences()
	{
		return null;
	}

	public IProject getProject() {
		Object element= getElement();
		if (element != null && element instanceof IProject) {
			return (IProject)element;
		}
		return null;
	}
	
	public void updateContainer() {
		fOptionBlock.update();
		setValid(fOptionBlock.isValid());
		setErrorMessage(fOptionBlock.getErrorMessage());
	}

	public boolean isValid() {
		updateContainer();
		return super.isValid();
	}

	/**
	 * @see DialogPage#setVisible(boolean)
	 */
	public void setVisible(boolean visible) {
		super.setVisible(visible);
		fOptionBlock.setVisible(visible);
		if (visible) {
			fOptionBlock.updateValues();
			displayedConfig = true;
		}
	}
	
	/**
	 * @see org.eclipse.jface.preference.IPreferencePageContainer#updateButtons()
	 */
	public void updateButtons() {
	}
	/**
	 * @see org.eclipse.jface.preference.IPreferencePageContainer#updateMessage()
	 */
	public void updateMessage() {
	}
	/**
	 * @see org.eclipse.jface.preference.IPreferencePageContainer#updateTitle()
	 */
	public void updateTitle() {
	}

	/*
	 * Event Handlers
	 */
	private void handleConfigSelection () {
		// If there is nothing in config selection widget just bail
		if (configSelector.getItemCount() == 0) return;
		
		// TODO: Check if the user has selected the "all" configuration
		int selectionIndex = configSelector.getSelectionIndex();
		if (selectionIndex == -1) return;
		String configName = configSelector.getItem(selectionIndex);
		if (configName.equals(ManagedBuilderUIMessages.getResourceString(ALL_CONFS))) {
			// This is the all config
			return;
		} else {
			// Cache the selected config 
			IConfiguration newConfig = configurations[selectionIndex];
			if (newConfig != selectedConfiguration) {
				// If the user has changed values, and is now switching configurations, prompt for saving
			    if (selectedConfiguration != null) {
			        if (fOptionBlock.isDirty()) {
						Shell shell = ManagedBuilderUIPlugin.getDefault().getShell();
						boolean shouldApply = MessageDialog.openQuestion(shell,
						        ManagedBuilderUIMessages.getResourceString("BuildPropertyPage.changes.save.title"), //$NON-NLS-1$
						        ManagedBuilderUIMessages.getFormattedString("BuildPropertyPage.changes.save.question",  //$NON-NLS-1$
						                new String[] {selectedConfiguration.getName(), newConfig.getName()}));
						if (shouldApply) {
						    if (performOk()) {
			        			fOptionBlock.setDirty(false);    
			        		} else {
						        MessageDialog.openWarning(shell,
								        ManagedBuilderUIMessages.getResourceString("BuildPropertyPage.changes.save.title"), //$NON-NLS-1$
								        ManagedBuilderUIMessages.getResourceString("BuildPropertyPage.changes.save.error")); //$NON-NLS-1$ 
						    }
						}
			        }
			    }
			    // Set the new selected configuration
				selectedConfiguration = newConfig;
				ManagedBuildManager.setSelectedConfiguration(getProject(), selectedConfiguration);
				// TODO: Set the appropriate error parsers...
				// TODO: Binary parsers too?
				fOptionBlock.updateValues();
			}
		}
	}

	// Event handler for the manage configuration button event
	private void handleManageConfig () {
		IManagedBuildInfo info = ManagedBuildManager.getBuildInfo(getProject());
		ManageConfigDialog manageDialog = new ManageConfigDialog(getShell(), ManagedBuilderUIMessages.getResourceString(MANAGE_TITLE), info.getManagedProject());
		if (manageDialog.open() == ManageConfigDialog.OK) {
			boolean updateConfigs = false;
			
			// Check to see if any configurations have to be deleted
			List deletedConfigs = manageDialog.getDeletedConfigIds();
			Iterator iter = deletedConfigs.listIterator();
			while (iter.hasNext()) {
				String id = (String)iter.next();
				
				// Remove the configurations from the project 
				info.getManagedProject().removeConfiguration(id);
				
				// Remove any settings stores
				fOptionBlock.removeValues(id);
				
				// Clean up the UI
				configurations = info.getManagedProject().getConfigurations();
				configSelector.removeAll();
				configSelector.setItems(getConfigurationNames());
				configSelector.select(0);
				updateConfigs = true;
			}
			
			// Check to see if any have to be added
			SortedMap newConfigs = manageDialog.getNewConfigs();
			Set keys = newConfigs.keySet();
			Iterator keyIter = keys.iterator();
			while (keyIter.hasNext()) {
				String name = (String) keyIter.next();
				IConfiguration parent = (IConfiguration) newConfigs.get(name);
				if (parent != null) {
					int id = ManagedBuildManager.getRandomNumber();
					
					// Create ID for the new component based on the parent ID and random component
					String newId = parent.getId();
					int index = newId.lastIndexOf(ID_SEPARATOR);
					if (index > 0) {
						String lastComponent = newId.substring(index + 1, newId.length());
						if (Character.isDigit(lastComponent.charAt(0))) {
							// Strip the last component
							newId = newId.substring(0, index);
						}
					}
					newId += ID_SEPARATOR + id;
					IConfiguration newConfig;
					if (parent.isExtensionElement()) {
						newConfig = info.getManagedProject().createConfiguration(parent, newId);
					} else {
						newConfig = info.getManagedProject().createConfigurationClone(parent, newId);
					}
					newConfig.setName(name);
					newConfig.setArtifactName(info.getManagedProject().getDefaultArtifactName());
					// Update the config lists
					configurations = info.getManagedProject().getConfigurations();
					configSelector.removeAll();
					configSelector.setItems(getConfigurationNames());
					configSelector.select(configSelector.indexOf(name));
					updateConfigs = true;
				}
			}
			if (updateConfigs){
				handleConfigSelection();
			}
		}
		return;
	}

	private void handleProjectTypeSelection() {
		// Is there anything in the selector widget
		if (projectTypeSelector.getItemCount() == 0) {
			manageConfigs.setEnabled(false);
			return;
		} 

		// Enable the manage button
		manageConfigs.setEnabled(true);

		// Cache the platform at the selection index
		selectedProjectType = projectTypes[projectTypeSelector.getSelectionIndex()];
		ManagedBuildManager.setSelectedConfiguration(getProject(), selectedConfiguration);
		
		// Update the contents of the configuration widget
		populateConfigurations();		
	}
}