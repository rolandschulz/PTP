/*******************************************************************************
 * Copyright (c) 2007,2013 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 * IBM Corporation - Initial API and implementation
 *******************************************************************************/
package org.eclipse.ptp.launch.ui.tabs;

import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.Platform;
import org.eclipse.core.runtime.SubMonitor;
import org.eclipse.debug.core.ILaunchConfiguration;
import org.eclipse.debug.core.ILaunchConfigurationWorkingCopy;
import org.eclipse.jface.bindings.keys.KeyStroke;
import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.dialogs.MessageDialogWithToggle;
import org.eclipse.jface.fieldassist.ComboContentAdapter;
import org.eclipse.jface.fieldassist.ContentProposalAdapter;
import org.eclipse.jface.fieldassist.IContentProposal;
import org.eclipse.jface.fieldassist.IContentProposalListener;
import org.eclipse.jface.fieldassist.SimpleContentProposalProvider;
import org.eclipse.jface.operation.IRunnableWithProgress;
import org.eclipse.osgi.util.NLS;
import org.eclipse.ptp.core.IPTPLaunchConfigurationConstants;
import org.eclipse.ptp.core.jobs.IJobControl;
import org.eclipse.ptp.core.util.LaunchUtils;
import org.eclipse.ptp.internal.rm.jaxb.core.JAXBExtensionUtils;
import org.eclipse.ptp.launch.PTPLaunchPlugin;
import org.eclipse.ptp.launch.internal.messages.Messages;
import org.eclipse.ptp.launch.ui.LaunchImages;
import org.eclipse.ptp.launch.ui.extensions.IRMLaunchConfigurationContentsChangedListener;
import org.eclipse.ptp.launch.ui.extensions.IRMLaunchConfigurationDynamicTab;
import org.eclipse.ptp.launch.ui.extensions.JAXBControllerLaunchConfigurationTab;
import org.eclipse.ptp.launch.ui.extensions.RMLaunchValidation;
import org.eclipse.ptp.remote.core.IRemoteConnection;
import org.eclipse.ptp.remote.ui.widgets.RemoteConnectionWidget;
import org.eclipse.ptp.rm.jaxb.control.core.ILaunchController;
import org.eclipse.ptp.rm.jaxb.control.core.LaunchControllerManager;
import org.eclipse.ptp.rm.jaxb.core.data.MonitorType;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.ScrolledComposite;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Listener;

/**
 * The Resources Tab is used to specify the resources required for a successful job launch. It is populated by the selected resource
 * manager type
 * 
 * @since 6.0
 */
public class ResourcesTab extends LaunchConfigurationTab {
	private final class ContentsChangedListener implements IRMLaunchConfigurationContentsChangedListener {

		public void handleContentsChanged(IRMLaunchConfigurationDynamicTab rmDynamicTab) {
			// The buttons and messages have to be updated based on anything
			// that has changed in the dynamic portion of the launch tab.
			updateLaunchConfigurationDialog();
		}
	}

	/**
	 * @since 4.0
	 */
	public static final String TAB_ID = "org.eclipse.ptp.launch.applicationLaunch.resourcesTab"; //$NON-NLS-1$

	private Combo fSystemTypeCombo;
	private final List<String> fProviders = new ArrayList<String>();

	/*
	 * Job controller with all necessary configuration information.
	 */
	private ILaunchController fLaunchControl;
	private RemoteConnectionWidget fRemoteConnectionWidget;
	/*
	 * Keep current remote connection so we can revert back to it
	 */
	private IRemoteConnection fRemoteConnection;

	/*
	 * The composite that holds the RM's attributes for the launch configuration
	 */
	private ScrolledComposite launchAttrsScrollComposite;

	private final Map<IJobControl, IRMLaunchConfigurationDynamicTab> fDynamicTabs = new HashMap<IJobControl, IRMLaunchConfigurationDynamicTab>();
	private final ContentsChangedListener launchContentsChangedListener = new ContentsChangedListener();

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.debug.ui.AbstractLaunchConfigurationTab#canSave()
	 */

	@Override
	public boolean canSave() {
		setErrorMessage(null);
		if (fSystemTypeCombo.getSelectionIndex() == 0) {
			setErrorMessage(Messages.ResourcesTab_No_Target_Configuration);
			return false;
		}
		if (fLaunchControl == null) {
			setErrorMessage(Messages.ResourcesTab_No_Connection_name);
			return false;
		}
		IRMLaunchConfigurationDynamicTab dynamicTab = getLaunchConfigurationDynamicTab(fLaunchControl);
		final Composite launchComp = getLaunchAttrsScrollComposite();
		if (dynamicTab == null || launchComp == null) {
			setErrorMessage(NLS.bind(Messages.ResourcesTab_No_Launch_Configuration, new Object[] { fLaunchControl
					.getConfiguration().getName() }));
			return false;
		}
		RMLaunchValidation validation = dynamicTab.canSave(launchComp);
		if (!validation.isSuccess()) {
			setErrorMessage(validation.getMessage());
			return false;
		}
		return true;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.debug.ui.ILaunchConfigurationTab#createControl(org.eclipse .swt.widgets.Composite)
	 */

	private boolean changeConnection(IRemoteConnection conn, ILaunchController controller) {
		boolean autoRun = false;
		try {
			autoRun = getLaunchConfiguration().getAttribute(IPTPLaunchConfigurationConstants.ATTR_AUTO_RUN_COMMAND, false);
		} catch (CoreException e) {
			// Ignore
		}
		if (!autoRun && controller.getConfiguration().getControlData().getStartUpCommand() != null) {
			MessageDialogWithToggle dialog = MessageDialogWithToggle.openYesNoQuestion(getShell(),
					Messages.ResourcesTab_openConnection, NLS.bind(Messages.ResourcesTab_noInformation, conn.getName()),
					Messages.ResourcesTab_Dont_ask_to_run_command, false, null, null);
			if (dialog.getReturnCode() == IDialogConstants.NO_ID) {
				return false;
			}
			setAutoRun(dialog.getToggleState());
		}
		return true;
	}

	public void createControl(Composite parent) {
		final int numColumns = 2;
		final Composite comp = new Composite(parent, SWT.NONE);
		setControl(comp);

		GridLayout layout = new GridLayout(2, false);
		comp.setLayout(layout);
		GridData gd = new GridData(GridData.FILL_HORIZONTAL);
		comp.setLayoutData(gd);

		new Label(comp, SWT.NONE).setText(Messages.ResourcesTab_targetSystemConfiguration);

		fSystemTypeCombo = new Combo(comp, SWT.NONE);
		gd = new GridData(GridData.FILL_HORIZONTAL);
		fSystemTypeCombo.setLayoutData(gd);
		fSystemTypeCombo.add(Messages.ResourcesTab_pleaseSelectTargetSystem);
		String[] configNames = JAXBExtensionUtils.getConfiguationNames();
		if (JAXBExtensionUtils.getInvalid() != null) {
			MessageDialog.openError(Display.getCurrent().getActiveShell(), Messages.ResourcesTab_InvalidConfig_title,
					Messages.ResourcesTab_InvalidConfig_message + JAXBExtensionUtils.getInvalid());
		}
		for (String name : configNames) {
			fSystemTypeCombo.add(name);
			fProviders.add(name);
		}
		fSystemTypeCombo.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetDefaultSelected(SelectionEvent e) {
				// this is called when 'enter' is pressed as opposed to mouse-click
				widgetSelected(e);
			}

			@Override
			public void widgetSelected(SelectionEvent e) {
				rmTypeSelectionChanged();
				updateEnablement();
				handleConnectionChanged();
			}
		});
		enableContentProposal(fSystemTypeCombo);
		fSystemTypeCombo.select(0);

		// select the default message; thus if user types a filter string immediately, it will replace it
		fSystemTypeCombo.setSelection(new Point(0, Messages.ResourcesTab_pleaseSelectTargetSystem.length()));

		// adjust selection events per Bug 403704 - for Linux/GTK only
		if (Platform.getOS().equals(Platform.OS_LINUX) && Platform.getWS().equals(Platform.WS_GTK)) {
			fSystemTypeCombo.addListener(SWT.Traverse, new Listener() {
				public void handleEvent(Event event) {
					if (event.detail == SWT.TRAVERSE_RETURN) {
						event.doit = false;
					}
				}
			});
		}

		fRemoteConnectionWidget = new RemoteConnectionWidget(comp, SWT.NONE, Messages.ResourcesTab_Connection_Type, 0,
				getLaunchConfigurationDialog());
		fRemoteConnectionWidget.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, false, 2, 1));
		fRemoteConnectionWidget.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				handleConnectionChanged();
			}
		});
		fRemoteConnectionWidget.setEnabled(false);

		createVerticalSpacer(comp, 2);

		final ScrolledComposite scrollComp = createLaunchAttributeControlComposite(comp, numColumns);
		setLaunchAttrsScrollComposite(scrollComp);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.debug.ui.AbstractLaunchConfigurationTab#getId()
	 */

	/**
	 * @param parent
	 * @param colspan
	 * @return
	 */
	private ScrolledComposite createLaunchAttributeControlComposite(Composite parent, int colspan) {
		ScrolledComposite attrComp = new ScrolledComposite(parent, SWT.H_SCROLL | SWT.V_SCROLL);
		GridData gridData = new GridData(SWT.FILL, SWT.FILL, true, true);
		gridData.horizontalSpan = colspan;
		attrComp.setLayoutData(gridData);
		attrComp.setExpandHorizontal(true);
		attrComp.setExpandVertical(true);
		return attrComp;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.debug.ui.AbstractLaunchConfigurationTab#getImage()
	 */

	/**
	 * Enable content-assist-like completion/filtering of TSC type by typing
	 * 
	 * @param combo
	 *            the combo box itself (could be extended to other controls)
	 */
	private void enableContentProposal(Combo combo) {
		final String LOWER_ALPHA = "abcdefghijklmnopqrstuvwxyz"; //$NON-NLS-1$
		final String UPPER_ALPHA = LOWER_ALPHA.toUpperCase();
		final String NUMBERS = "0123456789"; //$NON-NLS-1$

		final int keynoCTRL = new Integer(SWT.CTRL).intValue();
		final int keynoSPACE = new Integer(' ').intValue();
		// keystroke that activates this content proposal
		final KeyStroke ctrlSpace = KeyStroke.getInstance(keynoCTRL, keynoSPACE);

		final String delete = new String(new char[] { 8 });
		final String allChars = LOWER_ALPHA + UPPER_ALPHA + NUMBERS + delete;
		final char[] autoActivationChars = allChars.toCharArray();

		SimpleContentProposalProvider propProv = new SimpleContentProposalProvider(combo.getItems());
		ContentProposalAdapter propAdapter = new ContentProposalAdapter(combo, new ComboContentAdapter(), propProv, ctrlSpace,
				autoActivationChars);
		propProv.setFiltering(true);
		propAdapter.setPropagateKeys(true);
		propAdapter.setProposalAcceptanceStyle(ContentProposalAdapter.PROPOSAL_REPLACE);
		// avoid double-enter required
		propAdapter.addContentProposalListener(new IContentProposalListener() {
			public void proposalAccepted(IContentProposal proposal) {
				rmTypeSelectionChanged();
				updateEnablement();
				handleConnectionChanged();
			}
		});
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.debug.ui.ILaunchConfigurationTab#getName()
	 */

	@Override
	public String getId() {
		return TAB_ID;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.debug.ui.ILaunchConfigurationTab#initializeFrom(org.eclipse .debug.core.ILaunchConfiguration)
	 */

	@Override
	public Image getImage() {
		return LaunchImages.getImage(LaunchImages.IMG_PARALLEL_TAB);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.debug.ui.AbstractLaunchConfigurationTab#isValid(org.eclipse .debug.core.ILaunchConfiguration)
	 */

	/**
	 * @return
	 */
	private ScrolledComposite getLaunchAttrsScrollComposite() {
		return launchAttrsScrollComposite;
	}

	/**
	 * Returns a cached launch configuration dynamic tab. If it isn't in the cache then it creates a new one, and puts it in the
	 * cache. Does not require a progress monitor.
	 * 
	 * @param controller
	 *            launch controller
	 * @return
	 */
	private IRMLaunchConfigurationDynamicTab getLaunchConfigurationDynamicTab(final ILaunchController controller) {
		if (!fDynamicTabs.containsKey(controller)) {
			final IRMLaunchConfigurationDynamicTab[] dynamicTab = new IRMLaunchConfigurationDynamicTab[1];
			try {
				getLaunchConfigurationDialog().run(false, true, new IRunnableWithProgress() {

					public void run(IProgressMonitor monitor) throws InvocationTargetException, InterruptedException {
						SubMonitor progress = SubMonitor.convert(monitor, 1);
						dynamicTab[0] = getLaunchConfigurationDynamicTab(controller, progress.newChild(1));
					}
				});
			} catch (InvocationTargetException e) {
				// Ignore
			} catch (InterruptedException e) {
				// Ignore
			}
			return dynamicTab[0];
		}
		return fDynamicTabs.get(controller);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.debug.ui.ILaunchConfigurationTab#setDefaults(org.eclipse. debug.core.ILaunchConfigurationWorkingCopy)
	 */

	/**
	 * Returns a cached launch configuration dynamic tab. If it isn't in the cache then it creates a new one, and puts it in the
	 * cache.
	 * 
	 * @param controller
	 *            launch controller
	 * @param monitor
	 *            progress monitor
	 * @return
	 */
	private IRMLaunchConfigurationDynamicTab getLaunchConfigurationDynamicTab(final ILaunchController controller,
			IProgressMonitor monitor) {
		if (!fDynamicTabs.containsKey(controller)) {
			try {
				IRMLaunchConfigurationDynamicTab dynamicTab = new JAXBControllerLaunchConfigurationTab(controller, monitor);
				dynamicTab.addContentsChangedListener(launchContentsChangedListener);
				fDynamicTabs.put(controller, dynamicTab);
				return dynamicTab;
			} catch (Throwable e) {
				setErrorMessage(e.getMessage());
				PTPLaunchPlugin.errorDialog(e.getMessage(), e.getCause());
				return null;
			}
		}
		return fDynamicTabs.get(controller);
	}

	public String getName() {
		return Messages.ResourcesTab_Resources;
	}

	private ILaunchController getNewController(final String remId, final String connName, final String type) {
		try {
			return LaunchControllerManager.getInstance().getLaunchController(remId, connName, type);
		} catch (CoreException e) {
			PTPLaunchPlugin.errorDialog(e.getMessage(), e.getCause());
			return null;
		}
	}

	private void handleConnectionChanged() {
		/*
		 * LaunchConfigurationsDialog#run() tries to preserve the focus control. However, updateLaunchAttributeControls() will
		 * dispose of all the controls on the dynamic tab, leading to a widget disposed exception. The easy solution is to remove
		 * focus from the dynamic controls first.
		 */
		fRemoteConnectionWidget.setFocus();

		IRemoteConnection conn = null;
		if (fRemoteConnectionWidget.isEnabled()) {
			conn = fRemoteConnectionWidget.getConnection();
		}
		if (conn == null) {
			stopController(fLaunchControl);
			fLaunchControl = null;
			fRemoteConnection = null;
			updateLaunchAttributeControls(null, getLaunchConfiguration(), false);
			updateLaunchConfigurationDialog();
		} else {
			// We assume fSystemTypeCombo selection is valid based on previous tests
			String type = fSystemTypeCombo.getText();
			ILaunchController controller = getNewController(conn.getRemoteServices().getId(), conn.getName(), type);
			if (controller != null && changeConnection(conn, controller)) {
				stopController(fLaunchControl);
				fLaunchControl = controller;
				fRemoteConnection = conn;
				updateLaunchAttributeControls(fLaunchControl, getLaunchConfiguration(), true);
				updateLaunchConfigurationDialog();
			} else {
				/*
				 * Failed to change connection, reset back to the previous one
				 */
				fRemoteConnectionWidget.setConnection(fRemoteConnection);
			}
		}
	}

	@Override
	public void initializeFrom(ILaunchConfiguration configuration) {
		super.initializeFrom(configuration);

		final String rmType = LaunchUtils.getTemplateName(configuration);
		final String remId = LaunchUtils.getRemoteServicesId(configuration);
		final String remName = LaunchUtils.getConnectionName(configuration);
		if (rmType != null && remId != null && remName != null) {
			fSystemTypeCombo.select(fProviders.lastIndexOf(rmType) + 1);
			updateEnablement();
			/*
			 * Only stop the controller if something has changed.
			 */
			if (fLaunchControl != null
					&& (!fLaunchControl.getConfiguration().getName().equals(rmType)
							|| !fLaunchControl.getRemoteServicesId().equals(remId) || !fLaunchControl.getConnectionName().equals(
							remName))) {
				stopController(fLaunchControl);
				fLaunchControl = null;
			}
			/*
			 * Set the connection and see if the user wants to open it. If yes, create a new controller if one doesn't already
			 * exist. If no, revert to no connection selected.
			 */
			fRemoteConnectionWidget.setConnection(remId, remName);
			IRemoteConnection conn = fRemoteConnectionWidget.getConnection();
			if (conn != null) {
				ILaunchController control = getNewController(remId, remName, rmType);
				if (changeConnection(conn, control)) {
					fRemoteConnection = conn;
					fLaunchControl = control;
				} else {
					fRemoteConnectionWidget.setConnection(null);
				}
			}
			updateLaunchAttributeControls(fLaunchControl, getLaunchConfiguration(), true);
			updateLaunchConfigurationDialog();
		} else {
			stopController(fLaunchControl);
			fLaunchControl = null;
			fRemoteConnection = null;
			updateEnablement();
		}
	}

	/**
	 * Determine if target system configuration selection in the combo box is a valid one -
	 * one in the existing list has been selected:
	 * not an invalid value typed, and not the "Please Select..." message at the top.
	 */
	private boolean isTSCselectionValid() {
		String selected = fSystemTypeCombo.getText();
		boolean result = false;
		if (!fSystemTypeCombo.getText().equals(Messages.ResourcesTab_pleaseSelectTargetSystem)) {
			for (String s : fSystemTypeCombo.getItems()) {
				if (selected.equals(s)) {
					result = true;
				}
			}
		}
		return result;
	}

	@Override
	public boolean isValid(ILaunchConfiguration configuration) {
		setErrorMessage(null);
		setMessage(null);
		if (fSystemTypeCombo.getSelectionIndex() == 0) {
			setErrorMessage(Messages.ResourcesTab_No_Target_Configuration);
			return false;
		}
		if (fLaunchControl == null) {
			setErrorMessage(Messages.ResourcesTab_No_Connection_name);
			return false;
		}
		IRMLaunchConfigurationDynamicTab rmDynamicTab = getLaunchConfigurationDynamicTab(fLaunchControl);
		if (rmDynamicTab == null) {
			setErrorMessage(NLS.bind(Messages.ResourcesTab_No_Launch_Configuration, new Object[] { fLaunchControl
					.getConfiguration().getName() }));
			return false;
		}
		RMLaunchValidation validation = rmDynamicTab.isValid(configuration);
		if (!validation.isSuccess()) {
			setErrorMessage(validation.getMessage());
			return false;
		}

		return true;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.debug.ui.ILaunchConfigurationTab#performApply(org.eclipse .debug.core.ILaunchConfigurationWorkingCopy)
	 */
	public void performApply(ILaunchConfigurationWorkingCopy configuration) {
		if (fLaunchControl != null && isTSCselectionValid()) {
			LaunchUtils.setConfigurationName(configuration, fSystemTypeCombo.getText());
			LaunchUtils.setResourceManagerUniqueName(configuration, fLaunchControl.getControlId());
			LaunchUtils.setConnectionName(configuration, fLaunchControl.getConnectionName());
			LaunchUtils.setRemoteServicesId(configuration, fLaunchControl.getRemoteServicesId());
			String type = null;
			if (fLaunchControl.getConfiguration() != null) {
				MonitorType monitorData = fLaunchControl.getConfiguration().getMonitorData();
				if (monitorData != null) {
					type = monitorData.getSchedulerType();
				}
			}
			LaunchUtils.setSystemType(configuration, type);
			IRMLaunchConfigurationDynamicTab dynamicTab = getLaunchConfigurationDynamicTab(fLaunchControl);
			if (dynamicTab == null) {
				setErrorMessage(NLS.bind(Messages.ResourcesTab_No_Launch_Configuration, new Object[] { fLaunchControl
						.getConfiguration().getName() }));
				return;
			}
			RMLaunchValidation validation = dynamicTab.performApply(configuration);
			if (!validation.isSuccess()) {
				setErrorMessage(validation.getMessage());
				return;
			}
		}

	}

	/**
	 * Handle selection of a resource manager type
	 */
	private void rmTypeSelectionChanged() {
		stopController(fLaunchControl);
		fLaunchControl = null;
		setAutoRun(false);
	}

	private void setAutoRun(boolean auto) {
		try {
			ILaunchConfigurationWorkingCopy copy = getLaunchConfiguration().getWorkingCopy();
			copy.setAttribute(IPTPLaunchConfigurationConstants.ATTR_AUTO_RUN_COMMAND, auto);
			copy.doSave();
		} catch (CoreException e) {
			// Ignore
		}
	}

	public void setDefaults(ILaunchConfigurationWorkingCopy configuration) {
		// Do nothing
	}

	/**
	 * @param comp
	 */
	private void setLaunchAttrsScrollComposite(ScrolledComposite comp) {
		this.launchAttrsScrollComposite = comp;
	}

	private void stopController(ILaunchController controller) {
		if (controller != null && !controller.hasRunningJobs()) {
			try {
				controller.stop();
			} catch (CoreException e) {
				// Ignore
			}
		}

	}

	/**
	 * Enable/disable connection selection based on if a valid TSC is selected in combo box
	 */
	private void updateEnablement() {
		if (isTSCselectionValid()) {
			fRemoteConnectionWidget.setEnabled(true);
			fRemoteConnectionWidget.setConnection(null);
		} else {
			// First "Please select" message, or an invalid (probably user-typed) selection has been made.
			fRemoteConnectionWidget.setEnabled(false);
			// if first one or an invalid one is selected, select its text to ease typing a filter string to replace it
			fSystemTypeCombo.setSelection(new Point(0, Messages.ResourcesTab_pleaseSelectTargetSystem.length()));
		}
	}

	/**
	 * This routine is called when the configuration has been changed via the combo boxes. Its job is to regenerate the dynamic UI
	 * components, dependent on the configuration choice. The controller will be started if startController is true. This is only
	 * required if the connection information has been specified, in which case we want to run the start up commands
	 * so that the UI elements will be loaded correctly.
	 * 
	 * @param controller
	 *            current controller
	 * @param launchConfiguration
	 *            current launch configuration
	 * @param startController
	 *            if true, start the controller
	 */
	private void updateLaunchAttributeControls(final ILaunchController controller, ILaunchConfiguration launchConfiguration,
			final boolean startController) {
		final ScrolledComposite launchAttrsScrollComp = getLaunchAttrsScrollComposite();
		launchAttrsScrollComp.setContent(null);
		for (Control child : launchAttrsScrollComp.getChildren()) {
			child.dispose();
		}
		if (controller != null) {
			final IRMLaunchConfigurationDynamicTab[] dynamicTab = new IRMLaunchConfigurationDynamicTab[1];
			if (startController) {
				try {
					getLaunchConfigurationDialog().run(false, true, new IRunnableWithProgress() {

						public void run(IProgressMonitor monitor) throws InvocationTargetException, InterruptedException {
							SubMonitor progress = SubMonitor.convert(monitor, 20);
							try {
								controller.start(progress.newChild(10));
								dynamicTab[0] = getLaunchConfigurationDynamicTab(controller, progress.newChild(10));
							} catch (CoreException e) {
								PTPLaunchPlugin.errorDialog(e.getMessage(), e.getCause());

							}
						}
					});
				} catch (InvocationTargetException e) {
					// Ignore
				} catch (InterruptedException e) {
					// Ignore
				}
			}
			if (dynamicTab[0] != null) {
				try {
					dynamicTab[0].createControl(launchAttrsScrollComp, controller.getControlId());
					final Control dynControl = dynamicTab[0].getControl();
					launchAttrsScrollComp.setContent(dynControl);
					Point size = dynControl.computeSize(SWT.DEFAULT, SWT.DEFAULT);
					launchAttrsScrollComp.setMinSize(size);
					dynamicTab[0].initializeFrom(launchConfiguration);
				} catch (CoreException e) {
					setErrorMessage(e.getMessage());
					PTPLaunchPlugin.errorDialog(e.getMessage(), e.getCause());
				}
			}
		}
		launchAttrsScrollComp.layout(true);
	}
}
