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
package org.eclipse.ptp.rm.launch.ui;

import java.lang.reflect.InvocationTargetException;
import java.util.HashMap;
import java.util.Map;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.debug.core.ILaunchConfiguration;
import org.eclipse.debug.core.ILaunchConfigurationWorkingCopy;
import org.eclipse.debug.ui.ILaunchConfigurationDialog;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.operation.IRunnableWithProgress;
import org.eclipse.osgi.util.NLS;
import org.eclipse.ptp.core.IServiceConstants;
import org.eclipse.ptp.core.ModelManager;
import org.eclipse.ptp.launch.ui.LaunchConfigurationTab;
import org.eclipse.ptp.launch.ui.LaunchImages;
import org.eclipse.ptp.launch.ui.extensions.IRMLaunchConfigurationContentsChangedListener;
import org.eclipse.ptp.launch.ui.extensions.IRMLaunchConfigurationDynamicTab;
import org.eclipse.ptp.launch.ui.extensions.RMLaunchValidation;
import org.eclipse.ptp.remote.core.IRemoteConnection;
import org.eclipse.ptp.remote.ui.widgets.RemoteConnectionWidget;
import org.eclipse.ptp.rm.jaxb.control.ui.launch.JAXBControllerLaunchConfigurationTab;
import org.eclipse.ptp.rm.jaxb.core.IJAXBResourceManager;
import org.eclipse.ptp.rm.jaxb.core.IJAXBResourceManagerControl;
import org.eclipse.ptp.rm.launch.RMLaunchPlugin;
import org.eclipse.ptp.rm.launch.internal.ProviderInfo;
import org.eclipse.ptp.rm.launch.internal.messages.Messages;
import org.eclipse.ptp.rmsystem.IResourceManager;
import org.eclipse.ptp.rmsystem.IResourceManagerConfiguration;
import org.eclipse.ptp.rmsystem.ResourceManagerServiceProvider;
import org.eclipse.ptp.services.core.IService;
import org.eclipse.ptp.services.core.IServiceConfiguration;
import org.eclipse.ptp.services.core.IServiceProvider;
import org.eclipse.ptp.services.core.ServiceModelManager;
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

	private Combo fResourceManagerTypeCombo = null;
	private boolean fDefaultConnection;

	/**
	 * Resource manager created when type is selected from the combo.
	 */
	private IJAXBResourceManager fSelectedResourceManagerType = null;
	/**
	 * Resource manager with all necessary configuration information.
	 */
	private IJAXBResourceManager fResourceManager = null;
	private RemoteConnectionWidget fRemoteConnectionWidget;
	private IRemoteConnection fRemoteConnection = null;

	// The composite that holds the RM's attributes for the launch configuration
	private ScrolledComposite launchAttrsScrollComposite;

	private final IService fLaunchService = ServiceModelManager.getInstance().getService(IServiceConstants.LAUNCH_SERVICE);
	private final Map<IResourceManager, JAXBControllerLaunchConfigurationTab> rmDynamicTabs = new HashMap<IResourceManager, JAXBControllerLaunchConfigurationTab>();
	private final ContentsChangedListener launchContentsChangedListener = new ContentsChangedListener();

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.debug.ui.AbstractLaunchConfigurationTab#canSave()
	 */
	@Override
	public boolean canSave() {
		setErrorMessage(null);
		if (fResourceManager == null) {
			setErrorMessage(Messages.ResourcesTab_No_Resource_Manager);
			return false;
		}
		IRMLaunchConfigurationDynamicTab rmDynamicTab = getRMLaunchConfigurationDynamicTab(fResourceManager);
		final Composite launchComp = getLaunchAttrsScrollComposite();
		if (rmDynamicTab == null || launchComp == null) {
			setErrorMessage(NLS.bind(Messages.ResourcesTab_No_Launch_Configuration, new Object[] { fResourceManager.getName() }));
			return false;
		}
		RMLaunchValidation validation = rmDynamicTab.canSave(launchComp, fResourceManager);
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

		new Label(comp, SWT.NONE).setText("Target System Type:");

		fResourceManagerTypeCombo = new Combo(comp, SWT.READ_ONLY);
		gd = new GridData(GridData.FILL_HORIZONTAL);
		fResourceManagerTypeCombo.setLayoutData(gd);
		fResourceManagerTypeCombo.add("Please select a target system type");
		for (ProviderInfo provider : ProviderInfo.getProviders()) {
			fResourceManagerTypeCombo.add(provider.getName());
		}
		fResourceManagerTypeCombo.addSelectionListener(new SelectionListener() {
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
		fResourceManagerTypeCombo.deselectAll();

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
						fResourceManager = null;
						fRemoteConnection = conn;
						updateLaunchAttributeControls(null, getLaunchConfiguration());
						updateLaunchConfigurationDialog();
					} else if (connectionChanged(fSelectedResourceManagerType)) {
						fResourceManager = fSelectedResourceManagerType;
						fRemoteConnection = conn;
						updateLaunchAttributeControls(fResourceManager, getLaunchConfiguration());
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

		String rmType = getResourceManagerType(configuration);
		ProviderInfo provider = ProviderInfo.getProvider(rmType);
		if (provider != null) {
			fSelectedResourceManagerType = createResourceManager(provider);
			fSelectedResourceManagerType.getConfiguration().setUniqueName(getResourceManagerUniqueName(configuration));
			fResourceManagerTypeCombo.select(ProviderInfo.getProviders().lastIndexOf(provider) + 1);
			updateEnablement();
		}

		/*
		 * Initialize remote connection widget
		 */
		String remId = getRemoteServicesId(configuration);
		String remName = getConnectionName(configuration);
		if (remId != null && remName != null) {
			fRemoteConnectionWidget.setConnection(remId, remName);
			fDefaultConnection = false;
			if (connectionChanged(fSelectedResourceManagerType)) {
				/*
				 * Update the dynamic portions of the launch configuration tab.
				 */
				fResourceManager = fSelectedResourceManagerType;
				updateLaunchAttributeControls(fResourceManager, configuration);
			}
		}

		if (fResourceManager == null) {
			setErrorMessage(Messages.ResourcesTab_No_Resource_Manager_Available);
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
		if (fResourceManager == null) {
			setErrorMessage(Messages.ResourcesTab_No_Resource_Manager);
			return false;
		}
		IRMLaunchConfigurationDynamicTab rmDynamicTab = getRMLaunchConfigurationDynamicTab(fResourceManager);
		if (rmDynamicTab == null) {
			setErrorMessage(NLS.bind(Messages.ResourcesTab_No_Launch_Configuration, new Object[] { fResourceManager.getName() }));
			return false;
		}
		RMLaunchValidation validation = rmDynamicTab.isValid(configuration, fResourceManager);
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
		IRemoteConnection conn = fRemoteConnectionWidget.getConnection();
		if (conn != null) {
			setConnectionName(configuration, conn.getName());
			setRemoteServicesId(configuration, conn.getRemoteServices().getId());
		}

		int index = fResourceManagerTypeCombo.getSelectionIndex();
		if (fResourceManager != null && index > 0) {
			ProviderInfo provider = ProviderInfo.getProviders().get(index - 1);
			setResourceManagerType(configuration, provider.getName());
			setResourceManagerUniqueName(configuration, fResourceManager.getUniqueName());
			IRMLaunchConfigurationDynamicTab rmDynamicTab = getRMLaunchConfigurationDynamicTab(fResourceManager);
			if (rmDynamicTab == null) {
				setErrorMessage(NLS
						.bind(Messages.ResourcesTab_No_Launch_Configuration, new Object[] { fResourceManager.getName() }));
				return;
			}
			RMLaunchValidation validation = rmDynamicTab.performApply(configuration, fResourceManager);
			if (!validation.isSuccess()) {
				setErrorMessage(validation.getMessage());
				return;
			}
		}

	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.debug.ui.AbstractLaunchConfigurationTab#dispose()
	 */
	@Override
	public void dispose() {
		removeCurrentResourceManager();
		super.dispose();
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

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.debug.ui.AbstractLaunchConfigurationTab# setLaunchConfigurationDialog
	 * (org.eclipse.debug.ui.ILaunchConfigurationDialog)
	 */
	@Override
	public void setLaunchConfigurationDialog(ILaunchConfigurationDialog dialog) {
		super.setLaunchConfigurationDialog(dialog);
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
		int i = fResourceManagerTypeCombo.getSelectionIndex();
		if (i > 0) {
			ProviderInfo providerInfo = ProviderInfo.getProviders().get(i - 1);
			if (fSelectedResourceManagerType == null
					|| !fSelectedResourceManagerType.getJAXBConfiguration().getName().equals(providerInfo.getName())) {
				final IJAXBResourceManager rm = createResourceManager(providerInfo);
				if (rm != null) {
					if (fDefaultConnection) {
						fRemoteConnectionWidget.setConnection(rm.getControlConfiguration().getRemoteServicesId(), rm
								.getControlConfiguration().getConnectionName());
					}
					removeCurrentResourceManager();
					fSelectedResourceManagerType = rm;
				}
			}
		} else {
			removeCurrentResourceManager();
		}
	}

	private void removeCurrentResourceManager() {
		if (fResourceManager != null) {
			try {
				fResourceManager.getControl().stop();
				ModelManager.getInstance().removeResourceManager(fResourceManager);
			} catch (CoreException e) {
			}
			fResourceManager = null;
		}
	}

	private void updateEnablement() {
		if (fResourceManagerTypeCombo.getSelectionIndex() > 0) {
			fRemoteConnectionWidget.setEnabled(true);
			fRemoteConnectionWidget.setConnection(null);
		} else {
			fRemoteConnectionWidget.setEnabled(false);
		}
	}

	private boolean connectionChanged(final IJAXBResourceManager rm) {
		IRemoteConnection conn = fRemoteConnectionWidget.getConnection();
		if (conn != null) {
			rm.getControlConfiguration().setConnectionName(conn.getName());
			rm.getControlConfiguration().setRemoteServicesId(conn.getRemoteServices().getId());

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
							rm.getControl().start(monitor);
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
	 * @param info
	 *            provider information
	 * @param defaultConnection
	 *            true if the connection information should default to that specified in the configuration
	 * @return
	 */
	private IJAXBResourceManager createResourceManager(ProviderInfo info) {
		IServiceProvider provider = ServiceModelManager.getInstance().getServiceProvider(info.getDescriptor());
		if (provider != null) {
			IResourceManagerConfiguration baseConfig = ModelManager.getInstance().createBaseConfiguration(provider);
			info.getFactory().setConfigurationName(info.getName(), baseConfig);
			IServiceConfiguration config = ServiceModelManager.getInstance().newServiceConfiguration(info.getName());
			config.setServiceProvider(fLaunchService, provider);
			ServiceModelManager.getInstance().addConfiguration(config);
			final IResourceManager rm = ModelManager.getInstance().getResourceManagerFromUniqueName(
					((ResourceManagerServiceProvider) provider).getUniqueName());
			if (rm != null) {
				try {
					getLaunchConfigurationDialog().run(false, true, new IRunnableWithProgress() {
						@Override
						public void run(IProgressMonitor monitor) throws InvocationTargetException, InterruptedException {
							try {
								((IJAXBResourceManagerControl) rm.getControl()).initialize(monitor);
							} catch (Throwable e) {
								throw new InvocationTargetException(e);
							}
						}
					});
					return (IJAXBResourceManager) rm;
				} catch (InvocationTargetException e) {
					RMLaunchPlugin.errorDialog(e.getCause().getLocalizedMessage(), e.getCause());
				} catch (InterruptedException e) {
					RMLaunchPlugin.errorDialog(e.getCause().getLocalizedMessage(), e.getCause());
				}
			}
		}
		return null;
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
	private JAXBControllerLaunchConfigurationTab getRMLaunchConfigurationDynamicTab(final IJAXBResourceManager rm) {
		if (!rmDynamicTabs.containsKey(rm)) {
			try {
				JAXBControllerLaunchConfigurationTab rmDynamicTab = new JAXBControllerLaunchConfigurationTab(rm,
						getLaunchConfigurationDialog());
				rmDynamicTab.addContentsChangedListener(launchContentsChangedListener);
				rmDynamicTabs.put(rm, rmDynamicTab);
				return rmDynamicTab;
			} catch (Throwable e) {
				setErrorMessage(e.getMessage());
				RMLaunchPlugin.errorDialog(e.getMessage(), e);
				return null;
			}
		}
		return rmDynamicTabs.get(rm);
	}

	/**
	 * This routine is called when the resource manager has been changed via the combo boxes. It's job is to regenerate the dynamic
	 * ui components, dependent on the resource manager choice.
	 * 
	 * @param rm
	 * @param queue
	 * @param launchConfiguration
	 */
	private void updateLaunchAttributeControls(IJAXBResourceManager rm, ILaunchConfiguration launchConfiguration) {
		final ScrolledComposite launchAttrsScrollComp = getLaunchAttrsScrollComposite();
		launchAttrsScrollComp.setContent(null);
		for (Control child : launchAttrsScrollComp.getChildren()) {
			child.dispose();
		}
		if (rm != null) {
			JAXBControllerLaunchConfigurationTab rmDynamicTab = getRMLaunchConfigurationDynamicTab(rm);
			if (rmDynamicTab != null) {
				try {
					rmDynamicTab.createControl(launchAttrsScrollComp, rm);
					final Control dynControl = rmDynamicTab.getControl();
					launchAttrsScrollComp.setContent(dynControl);
					Point size = dynControl.computeSize(SWT.DEFAULT, SWT.DEFAULT);
					launchAttrsScrollComp.setMinSize(size);
					rmDynamicTab.initializeFrom(launchAttrsScrollComp, rm, launchConfiguration);
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