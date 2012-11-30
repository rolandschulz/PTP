/*******************************************************************************
 * Copyright (c) 2007 IBM Corporation and others.
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
import org.eclipse.core.runtime.SubMonitor;
import org.eclipse.debug.core.ILaunchConfiguration;
import org.eclipse.debug.core.ILaunchConfigurationWorkingCopy;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.operation.IRunnableWithProgress;
import org.eclipse.osgi.util.NLS;
import org.eclipse.ptp.core.jobs.IJobControl;
import org.eclipse.ptp.core.util.LaunchUtils;
import org.eclipse.ptp.launch.PTPLaunchPlugin;
import org.eclipse.ptp.launch.internal.messages.Messages;
import org.eclipse.ptp.launch.ui.LaunchImages;
import org.eclipse.ptp.launch.ui.extensions.IRMLaunchConfigurationContentsChangedListener;
import org.eclipse.ptp.launch.ui.extensions.IRMLaunchConfigurationDynamicTab;
import org.eclipse.ptp.launch.ui.extensions.JAXBControllerLaunchConfigurationTab;
import org.eclipse.ptp.launch.ui.extensions.RMLaunchValidation;
import org.eclipse.ptp.remote.core.IRemoteConnection;
import org.eclipse.ptp.remote.ui.widgets.RemoteConnectionWidget;
import org.eclipse.ptp.rm.jaxb.control.IJobController;
import org.eclipse.ptp.rm.jaxb.control.ILaunchController;
import org.eclipse.ptp.rm.jaxb.control.LaunchControllerManager;
import org.eclipse.ptp.rm.jaxb.core.JAXBExtensionUtils;
import org.eclipse.ptp.rm.jaxb.core.data.MonitorType;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.ScrolledComposite;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Label;

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

	public void createControl(Composite parent) {
		final int numColumns = 2;
		final Composite comp = new Composite(parent, SWT.NONE);
		setControl(comp);

		GridLayout layout = new GridLayout(2, false);
		comp.setLayout(layout);
		GridData gd = new GridData(GridData.FILL_HORIZONTAL);
		comp.setLayoutData(gd);

		new Label(comp, SWT.NONE).setText(Messages.ResourcesTab_targetSystemConfiguration);

		fSystemTypeCombo = new Combo(comp, SWT.READ_ONLY);
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
		fSystemTypeCombo.addSelectionListener(new SelectionListener() {

			public void widgetDefaultSelected(SelectionEvent e) {
			}

			public void widgetSelected(SelectionEvent e) {
				rmTypeSelectionChanged();
				updateEnablement();
				handleConnectionChanged();
			}
		});
		fSystemTypeCombo.select(0);

		fRemoteConnectionWidget = new RemoteConnectionWidget(comp, SWT.NONE, null, getLaunchConfigurationDialog());
		fRemoteConnectionWidget.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, false, 2, 1));
		fRemoteConnectionWidget.addSelectionListener(new SelectionListener() {

			public void widgetDefaultSelected(SelectionEvent e) {
			}

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
		return LaunchImages.getImage(LaunchImages.IMG_PARALLEL_TAB);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.debug.ui.ILaunchConfigurationTab#getName()
	 */

	public String getName() {
		return Messages.ResourcesTab_Resources;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.debug.ui.ILaunchConfigurationTab#initializeFrom(org.eclipse .debug.core.ILaunchConfiguration)
	 */

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
			if (openConnection()) {
				fRemoteConnection = fRemoteConnectionWidget.getConnection();
				if (fLaunchControl == null) {
					fLaunchControl = getNewController(remId, remName, rmType);
				}
			} else {
				fRemoteConnectionWidget.setConnection(null);
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

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.debug.ui.AbstractLaunchConfigurationTab#isValid(org.eclipse .debug.core.ILaunchConfiguration)
	 */

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
		int index = fSystemTypeCombo.getSelectionIndex();
		if (fLaunchControl != null && index > 0) {
			LaunchUtils.setConfigurationName(configuration, fSystemTypeCombo.getItem(index));
			LaunchUtils.setResourceManagerUniqueName(configuration, fLaunchControl.getControlId());
			LaunchUtils.setConnectionName(configuration, fLaunchControl.getConnectionName());
			LaunchUtils.setRemoteServicesId(configuration, fLaunchControl.getRemoteServicesId());
			if (fLaunchControl.getConfiguration() != null) {
				MonitorType monitorData = fLaunchControl.getConfiguration().getMonitorData();
				if (monitorData != null) {
					LaunchUtils.setSystemType(configuration, monitorData.getSchedulerType());
				}
			}
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

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.debug.ui.ILaunchConfigurationTab#setDefaults(org.eclipse. debug.core.ILaunchConfigurationWorkingCopy)
	 */

	public void setDefaults(ILaunchConfigurationWorkingCopy configuration) {
	}

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
	private IRMLaunchConfigurationDynamicTab getLaunchConfigurationDynamicTab(final IJobController controller) {
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
			} catch (InterruptedException e) {
			}
			return dynamicTab[0];
		}
		return fDynamicTabs.get(controller);
	}

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
	private IRMLaunchConfigurationDynamicTab getLaunchConfigurationDynamicTab(final IJobController controller,
			IProgressMonitor monitor) {
		if (!fDynamicTabs.containsKey(controller)) {
			try {
				IRMLaunchConfigurationDynamicTab dynamicTab = new JAXBControllerLaunchConfigurationTab(controller, monitor);
				dynamicTab.addContentsChangedListener(launchContentsChangedListener);
				fDynamicTabs.put(controller, dynamicTab);
				return dynamicTab;
			} catch (Throwable e) {
				setErrorMessage(e.getMessage());
				PTPLaunchPlugin.errorDialog(e.getMessage(), e);
				return null;
			}
		}
		return fDynamicTabs.get(controller);
	}

	private ILaunchController getNewController(final String remId, final String connName, final String type) {
		try {
			return LaunchControllerManager.getInstance().getLaunchController(remId, connName, type);
		} catch (CoreException e) {
			PTPLaunchPlugin.errorDialog(e.getMessage(), e);
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
		} else if (openConnection()) {
			String type = fSystemTypeCombo.getItem(fSystemTypeCombo.getSelectionIndex());
			ILaunchController controller = getNewController(conn.getRemoteServices().getId(), conn.getName(), type);
			if (controller != null) {
				stopController(fLaunchControl);
				fLaunchControl = controller;
				fRemoteConnection = conn;
				updateLaunchAttributeControls(fLaunchControl, getLaunchConfiguration(), true);
				updateLaunchConfigurationDialog();
			}
		} else {
			/*
			 * Failed to change connection, reset back to the previous one
			 */
			fRemoteConnectionWidget.setConnection(fRemoteConnection);
		}
	}

	private boolean openConnection() {
		IRemoteConnection conn = fRemoteConnectionWidget.getConnection();
		if (conn != null) {
			if (!conn.isOpen()) {
				boolean result = MessageDialog.openQuestion(getShell(), Messages.ResourcesTab_openConnection,
						NLS.bind(Messages.ResourcesTab_noInformation, conn.getName()));
				if (!result) {
					return false;
				}
			}
			return true;
		}
		return false;
	}

	/**
	 * Handle selection of a resource manager type
	 */
	private void rmTypeSelectionChanged() {
		stopController(fLaunchControl);
		fLaunchControl = null;
	}

	/**
	 * @param comp
	 */
	private void setLaunchAttrsScrollComposite(ScrolledComposite comp) {
		this.launchAttrsScrollComposite = comp;
	}

	private void stopController(ILaunchController controller) {
		if (controller != null) {
			try {
				controller.stop();
			} catch (CoreException e) {
			}
		}

	}

	private void updateEnablement() {
		if (fSystemTypeCombo.getSelectionIndex() > 0) {
			fRemoteConnectionWidget.setEnabled(true);
			fRemoteConnectionWidget.setConnection(null);
		} else {
			fRemoteConnectionWidget.setEnabled(false);
		}
	}

	/**
	 * This routine is called when the configuration has been changed via the combo boxes. It's job is to regenerate the dynamic ui
	 * components, dependent on the configuration choice.
	 * 
	 * @param rm
	 * @param queue
	 * @param launchConfiguration
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
			try {
				getLaunchConfigurationDialog().run(false, true, new IRunnableWithProgress() {

					public void run(IProgressMonitor monitor) throws InvocationTargetException, InterruptedException {
						SubMonitor progress = SubMonitor.convert(monitor, 20);
						if (startController) {
							try {
								controller.start(progress.newChild(10));
								dynamicTab[0] = getLaunchConfigurationDynamicTab(controller, progress.newChild(10));
							} catch (CoreException e) {
								PTPLaunchPlugin.errorDialog(e.getMessage(), e);
							}
						}
					}
				});
			} catch (InvocationTargetException e) {
			} catch (InterruptedException e) {
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
					Throwable t = e.getCause();
					if (t == null) {
						t = e;
					}
					PTPLaunchPlugin.errorDialog(e.getMessage(), t);
				}
			}
		}
		launchAttrsScrollComp.layout(true);
	}
}