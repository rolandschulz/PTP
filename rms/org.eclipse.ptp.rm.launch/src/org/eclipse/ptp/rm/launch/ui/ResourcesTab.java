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
package org.eclipse.ptp.rm.launch.ui;

import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.debug.core.ILaunchConfiguration;
import org.eclipse.debug.core.ILaunchConfigurationWorkingCopy;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.operation.IRunnableWithProgress;
import org.eclipse.osgi.util.NLS;
import org.eclipse.ptp.core.jobs.IJobControl;
import org.eclipse.ptp.core.util.LaunchUtils;
import org.eclipse.ptp.launch.ui.LaunchConfigurationTab;
import org.eclipse.ptp.launch.ui.LaunchImages;
import org.eclipse.ptp.launch.ui.extensions.IRMLaunchConfigurationContentsChangedListener;
import org.eclipse.ptp.launch.ui.extensions.IRMLaunchConfigurationDynamicTab;
import org.eclipse.ptp.launch.ui.extensions.RMLaunchValidation;
import org.eclipse.ptp.remote.core.IRemoteConnection;
import org.eclipse.ptp.remote.ui.widgets.RemoteConnectionWidget;
import org.eclipse.ptp.rm.jaxb.control.IJobController;
import org.eclipse.ptp.rm.jaxb.control.ILaunchController;
import org.eclipse.ptp.rm.jaxb.control.ui.launch.JAXBControllerLaunchConfigurationTab;
import org.eclipse.ptp.rm.jaxb.core.data.MonitorType;
import org.eclipse.ptp.rm.launch.RMLaunchPlugin;
import org.eclipse.ptp.rm.launch.RMLaunchUtils;
import org.eclipse.ptp.rm.launch.internal.ProviderInfo;
import org.eclipse.ptp.rm.launch.internal.messages.Messages;
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
import org.eclipse.swt.widgets.Label;

/**
 * The Resources Tab is used to specify the resources required for a successful job launch. It is populated by the selected resource
 * manager type
 * 
 * @since 6.0
 */
public class ResourcesTab extends LaunchConfigurationTab {
	private final class ContentsChangedListener implements IRMLaunchConfigurationContentsChangedListener {

		@Override
		public void handleContentsChanged(IRMLaunchConfigurationDynamicTab rmDynamicTab) {
			// The buttons and messages have to be updated based on anything
			// that has changed in the dynamic portion of the launch tab.
			updateLaunchConfigurationDialog();
		}
	}

	/**
	 * @since 4.0
	 */
	public static final String TAB_ID = "org.eclipse.ptp.rm.launch.applicationLaunch.resourcesTab"; //$NON-NLS-1$

	private Combo fSystemTypeCombo;
	private final List<String> fProviders = new ArrayList<String>();
	private boolean fDefaultConnection;

	/*
	 * Job controller created when type is selected from the combo.
	 */
	private ILaunchController fSelectedLaunchControl;
	/*
	 * Job controller with all necessary configuration information.
	 */
	private ILaunchController fLaunchControl;
	private RemoteConnectionWidget fRemoteConnectionWidget;
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
		if (fLaunchControl == null) {
			setErrorMessage(Messages.ResourcesTab_No_Resource_Manager);
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
	@Override
	public void createControl(Composite parent) {
		final int numColumns = 2;
		final Composite comp = new Composite(parent, SWT.NONE);
		setControl(comp);

		GridLayout layout = new GridLayout(2, false);
		comp.setLayout(layout);
		GridData gd = new GridData(GridData.FILL_HORIZONTAL);
		comp.setLayoutData(gd);

		new Label(comp, SWT.NONE).setText("Target System Template:");

		fSystemTypeCombo = new Combo(comp, SWT.READ_ONLY);
		gd = new GridData(GridData.FILL_HORIZONTAL);
		fSystemTypeCombo.setLayoutData(gd);
		fSystemTypeCombo.add("Please select a target system template");
		for (ProviderInfo provider : ProviderInfo.getProviders()) {
			fSystemTypeCombo.add(provider.getName());
			fProviders.add(provider.getName());
		}
		fSystemTypeCombo.addSelectionListener(new SelectionListener() {
			@Override
			public void widgetDefaultSelected(SelectionEvent e) {
			}

			@Override
			public void widgetSelected(SelectionEvent e) {
				rmTypeSelectionChanged();
				updateEnablement();
				updateLaunchAttributeControls(null, getLaunchConfiguration());
				updateLaunchConfigurationDialog();
			}
		});
		fSystemTypeCombo.deselectAll();

		fRemoteConnectionWidget = new RemoteConnectionWidget(comp, SWT.NONE, null, getLaunchConfigurationDialog());
		fRemoteConnectionWidget.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, false, 2, 1));
		fRemoteConnectionWidget.addSelectionListener(new SelectionListener() {
			private boolean enabled = true;

			@Override
			public void widgetDefaultSelected(SelectionEvent e) {
			}

			@Override
			public void widgetSelected(SelectionEvent e) {
				if (enabled) {
					IRemoteConnection conn = fRemoteConnectionWidget.getConnection();
					if (conn == null) {
						fLaunchControl = null;
						fRemoteConnection = conn;
						updateLaunchAttributeControls(null, getLaunchConfiguration());
						updateLaunchConfigurationDialog();
					} else if (connectionChanged(fSelectedLaunchControl)) {
						fLaunchControl = fSelectedLaunchControl;
						fRemoteConnection = conn;
						updateLaunchAttributeControls(fLaunchControl, getLaunchConfiguration());
						updateLaunchConfigurationDialog();
					} else {
						/*
						 * Failed to change connection, reset back to the previous one
						 */
						enabled = false;
						fRemoteConnectionWidget.setConnection(fRemoteConnection);
						enabled = true;
					}
				}
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
	@Override
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

		String rmType = LaunchUtils.getTemplateName(configuration);
		String controlId = LaunchUtils.getResourceManagerUniqueName(configuration);
		ProviderInfo provider = ProviderInfo.getProvider(rmType);
		if (provider != null) {
			if (fSelectedLaunchControl == null || !fSelectedLaunchControl.getControlId().equals(controlId)) {
				fSelectedLaunchControl = RMLaunchUtils.getLaunchControl(provider.getName(), controlId);
				fSystemTypeCombo.select(fProviders.lastIndexOf(provider.getName()) + 1);
				updateEnablement();

				/*
				 * Initialize remote connection widget
				 */
				String remId = LaunchUtils.getRemoteServicesId(configuration);
				String remName = LaunchUtils.getConnectionName(configuration);
				if (remId != null && remName != null) {
					fRemoteConnectionWidget.setConnection(remId, remName);
					fDefaultConnection = false;
				}
			}
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
		if (fLaunchControl == null) {
			setErrorMessage(Messages.ResourcesTab_No_Resource_Manager);
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
	@Override
	public void performApply(ILaunchConfigurationWorkingCopy configuration) {
		int index = fSystemTypeCombo.getSelectionIndex();
		if (fLaunchControl != null && index > 0) {
			ProviderInfo provider = ProviderInfo.getProviders().get(index - 1);
			LaunchUtils.setTemplateName(configuration, provider.getName());
			LaunchUtils.setResourceManagerUniqueName(configuration, fLaunchControl.getControlId());
			LaunchUtils.setConnectionName(configuration, fLaunchControl.getConnectionName());
			LaunchUtils.setRemoteServicesId(configuration, fLaunchControl.getRemoteServicesId());
			MonitorType monitorData = fLaunchControl.getConfiguration().getMonitorData();
			if (monitorData != null) {
				LaunchUtils.setSystemType(configuration, monitorData.getSchedulerType());
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
	@Override
	public void setDefaults(ILaunchConfigurationWorkingCopy configuration) {
		fDefaultConnection = true;
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
	 * Handle selection of a resource manager type
	 */
	private void rmTypeSelectionChanged() {
		int i = fSystemTypeCombo.getSelectionIndex();
		if (i > 0) {
			String controlId = LaunchUtils.getResourceManagerUniqueName(getLaunchConfiguration());
			if (fSelectedLaunchControl == null || !fSelectedLaunchControl.getControlId().equals(controlId)) {
				ProviderInfo provider = ProviderInfo.getProviders().get(i - 1);
				final ILaunchController control = RMLaunchUtils.getLaunchControl(provider.getName(), controlId);
				if (control != null) {
					if (fDefaultConnection) {
						fRemoteConnectionWidget.setConnection(control.getRemoteServicesId(), control.getConnectionName());
					}
					if (fSelectedLaunchControl != null) {
						try {
							fSelectedLaunchControl.stop();
						} catch (CoreException e) {
						}
					}
					fSelectedLaunchControl = control;
				}
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

	private boolean connectionChanged(final ILaunchController control) {
		IRemoteConnection conn = fRemoteConnectionWidget.getConnection();
		if (conn != null) {
			try {
				control.stop();
			} catch (CoreException e) {
			}
			control.setConnectionName(conn.getName());
			control.setRemoteServicesId(conn.getRemoteServices().getId());

			if (!fRemoteConnectionWidget.getConnection().isOpen()) {
				boolean result = MessageDialog
						.openQuestion(
								getShell(),
								"Open Connection",
								"No information about the target system '"
										+ fRemoteConnectionWidget.getConnection().getName()
										+ "' can be obtained because the connection is not open. Would you like to open the connection now?");
				if (!result) {
					return false;
				}
			}
			try {
				getLaunchConfigurationDialog().run(false, true, new IRunnableWithProgress() {
					@Override
					public void run(IProgressMonitor monitor) throws InvocationTargetException, InterruptedException {
						try {
							System.out.println("before start");
							control.start(monitor);
							System.out.println("after start");
						} catch (CoreException e) {
							throw new InvocationTargetException(e);
						}
					}
				});
				return true;
			} catch (InvocationTargetException e) {
				RMLaunchPlugin.errorDialog(e.getCause().getLocalizedMessage(), e.getCause());
			} catch (InterruptedException e) {
				RMLaunchPlugin.errorDialog(e.getCause().getLocalizedMessage(), e.getCause());
			}
		}
		return false;
	}

	/**
	 * @param comp
	 */
	private void setLaunchAttrsScrollComposite(ScrolledComposite comp) {
		this.launchAttrsScrollComposite = comp;
	}

	/**
	 * Returns a cached launch configuration dynamic tab. If it isn't in the cache then it creates a new one, and puts it in the
	 * cache.
	 * 
	 * @param rm
	 * @return
	 */
	private IRMLaunchConfigurationDynamicTab getLaunchConfigurationDynamicTab(final IJobController control) {
		if (!fDynamicTabs.containsKey(control)) {
			try {
				IRMLaunchConfigurationDynamicTab dynamicTab = new JAXBControllerLaunchConfigurationTab(control,
						getLaunchConfigurationDialog());
				dynamicTab.addContentsChangedListener(launchContentsChangedListener);
				fDynamicTabs.put(control, dynamicTab);
				return dynamicTab;
			} catch (Throwable e) {
				setErrorMessage(e.getMessage());
				RMLaunchPlugin.errorDialog(e.getMessage(), e);
				return null;
			}
		}
		return fDynamicTabs.get(control);
	}

	/**
	 * This routine is called when the resource manager has been changed via the combo boxes. It's job is to regenerate the dynamic
	 * ui components, dependent on the resource manager choice.
	 * 
	 * @param rm
	 * @param queue
	 * @param launchConfiguration
	 */
	private void updateLaunchAttributeControls(IJobController control, ILaunchConfiguration launchConfiguration) {
		final ScrolledComposite launchAttrsScrollComp = getLaunchAttrsScrollComposite();
		launchAttrsScrollComp.setContent(null);
		for (Control child : launchAttrsScrollComp.getChildren()) {
			child.dispose();
		}
		if (control != null) {
			IRMLaunchConfigurationDynamicTab dynamicTab = getLaunchConfigurationDynamicTab(control);
			if (dynamicTab != null) {
				try {
					dynamicTab.createControl(launchAttrsScrollComp, control.getControlId());
					final Control dynControl = dynamicTab.getControl();
					launchAttrsScrollComp.setContent(dynControl);
					Point size = dynControl.computeSize(SWT.DEFAULT, SWT.DEFAULT);
					launchAttrsScrollComp.setMinSize(size);
					dynamicTab.initializeFrom(launchConfiguration);
				} catch (CoreException e) {
					setErrorMessage(e.getMessage());
					Throwable t = e.getCause();
					if (t == null) {
						t = e;
					}
					RMLaunchPlugin.errorDialog(e.getMessage(), t);
				}
			}
		}
		launchAttrsScrollComp.layout(true);
	}
}