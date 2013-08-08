/*******************************************************************************
 * Copyright (c) 2012 University of Illinois and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Jeff Overbey (Illinois) - initial API and implementation
 *******************************************************************************/
package org.eclipse.ptp.internal.ems.ui;

import java.net.URI;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.osgi.util.NLS;
import org.eclipse.ptp.ems.core.EnvManagerRegistry;
import org.eclipse.ptp.ems.core.IEnvManager;
import org.eclipse.ptp.ems.ui.EnvManagerConfigWidget;
import org.eclipse.ptp.ems.ui.IErrorListener;
import org.eclipse.ptp.internal.ems.ui.messages.Messages;
import org.eclipse.ptp.remote.core.IRemoteConnection;
import org.eclipse.ptp.remote.ui.RemoteUIServicesUtils;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.StackLayout;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;

/**
 * Instances of this class represent a user interface element which is used to set an environment configuration using a checklist;
 * this is intended to be used solely as part of a {@link EnvManagerConfigWidget}.
 * <p>
 * This element appears as one of the following.
 * <ul>
 * <li>If a connection to the remote machine is available but not yet opened, an informational message is displayed with a button
 * allowing the user to establish the connection.
 * <li>If a connection to the remote machine is open, a {@link SearchableChecklist} is displayed with a list of available
 * environment modules/macros.
 * <li>If there is no remote connection configured, or if the remote system does not have a supported environment configuration
 * system installed, then an informational message is displayed to the user.
 * </ul>
 * 
 * @author Jeff Overbey
 * 
 * @see Composite
 */
public final class EnvManagerChecklist extends Composite {
	private IRemoteConnection remoteConnection;
	private IEnvManager envManager;
	private IErrorListener errorListener;
	private Job repopulateJob;
	private boolean isValid;

	private URI lastSyncURI;
	private List<String> lastSelectedItems;

	private Composite stack;
	private StackLayout stackLayout;

	private Composite messageComposite;
	private Label messageLabel;
	private Button connectButton;

	private SearchableSelectionList checklist;

	/**
	 * Constructor.
	 * 
	 * @param parent
	 *            parent {@link Composite} (non-<code>null</code>)
	 * @param style
	 *            the style of widget to construct
	 */
	public EnvManagerChecklist(Composite parent, int style) {
		super(parent, style);
		this.envManager = EnvManagerRegistry.getNullEnvManager();
		this.lastSyncURI = null;
		this.lastSelectedItems = Collections.<String> emptyList();

		this.errorListener = new NullErrorListener();

		GridLayout g = new GridLayout(1, false);
		g.marginHeight = 0;
		g.marginWidth = 0;
		setLayout(g);

		createStack(this);
		createMsgComposite();
		createChecklist();

		checklist.setEnabledAndVisible(isEnabled());

		stackLayout.topControl = messageComposite;
		stack.layout(true, true);
	}

	private void createStack(Composite composite) {
		stack = new Composite(composite, SWT.NONE);
		stack.setLayoutData(new GridData(GridData.FILL_BOTH));
		stackLayout = new StackLayout();
		stack.setLayout(stackLayout);
	}

	private void createMsgComposite() {
		messageComposite = new Composite(stack, SWT.NONE);
		final GridLayout layout = new GridLayout(2, false);
		layout.horizontalSpacing = 10;
		layout.verticalSpacing = 10;
		messageComposite.setLayout(layout);

		messageLabel = new Label(messageComposite, SWT.WRAP);
		final GridData gridData = new GridData(SWT.FILL, SWT.CENTER, false, false);
		gridData.widthHint = 450;
		messageLabel.setLayoutData(gridData);

		connectButton = new Button(messageComposite, SWT.PUSH);
		connectButton.setLayoutData(new GridData(SWT.LEFT, SWT.CENTER, false, false));
		connectButton.setText(Messages.EnvManagerChecklist_ConnectButtonLabel);
		connectButton.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				if (connect()) {
					reset(null, remoteConnection, lastSelectedItems); // force reset() to think that the sync URI has changed
				}
			}
		});

		setNotConnectedMessage();
	}

	private void setNotConnectedMessage() {
		final String connectionName = getConnectionName();
		if (connectionName != null) {
			messageLabel.setText(NLS.bind(Messages.EnvManagerChecklist_RemoteEnvironmentIsNotConnected, connectionName));
		} else {
			messageLabel.setText(Messages.EnvManagerChecklist_NotRemoteSync);
		}
		connectButton.setEnabled(connectionName != null);
		connectButton.setVisible(connectionName != null);
		messageComposite.pack();

		stackLayout.topControl = messageComposite;
		stack.layout(true, true);
	}

	/**
	 * Sets the (unique) {@link IErrorListener} which will be used to display error messages to the user.
	 * 
	 * @param listener
	 *            {@link IErrorListener} used to display error messages to the user, or <code>null</code>
	 * 
	 * @see EnvManagerConfigWidget#setErrorListener(IErrorListener)
	 */
	public void setErrorListener(IErrorListener listener) {
		if (listener == null) {
			this.errorListener = new NullErrorListener();
		} else {
			this.errorListener = listener;
		}
	}

	/**
	 * @return the name of the remote environment (possibly <code>null</code>). The remote environment is determined by the
	 *         {@link IRemoteConnection} provided to the constructor or
	 *         {@link EnvManagerConfigWidget#configurationChanged(URI, IRemoteConnection, List)},
	 *         whichever was
	 *         invoked most recently.
	 * 
	 * @see EnvManagerConfigWidget#getConnectionName()
	 */
	public String getConnectionName() {
		return remoteConnection == null ? null : remoteConnection.getName();
	}

	private boolean connect() {
		if (remoteConnection == null) {
			return false;
		}
		if (!remoteConnection.isOpen()) {
			RemoteUIServicesUtils.openConnectionWithProgress(getShell(), null, remoteConnection);
		}
		return remoteConnection.isOpen();
	}

	/**
	 * Set the remote connection to use when querying for the remote environment
	 * 
	 * @param connection
	 *            {@link IRemoteConnection} used to access files and execute shell commands on the remote machine (non-
	 *            <code>null</code>)
	 */
	public void setConnection(IRemoteConnection connection) {
		remoteConnection = connection;
	}

	private void createChecklist() {
		checklist = new SearchableSelectionList(stack);
		checklist.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));
		checklist.setEnabledAndVisible(false);
		checklist.setColumnHeaders(Messages.EnvManagerChecklist_Available_Modules, Messages.EnvManagerChecklist_Selected_Modules);
		checklist.addDefaultButtonSelectonListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				// No need to recompute the list of available modules; store the existing list and use it
				final List<String> allModules = new ArrayList<String>(checklist.getAllItems());
				checklist.asyncRepopulate(new AsyncRepopulationStrategy() {
					@Override
					public String getMessage() {
						return Messages.EnvManagerChecklist_PleaseWaitRetrievingModuleList;
					}

					@Override
					public List<String> computeItems(IProgressMonitor monitor) {
						return allModules;
					}

					@Override
					public List<String> computeSelectedItems(IProgressMonitor monitor) throws Exception {
						// Recompute the list of modules loaded by default
						return envManager.determineDefaultElements(monitor);
					}

					@Override
					public void afterRepopulation() {
						checklist.setEnabledAndVisible(EnvManagerChecklist.this.isEnabled());
					}
				});
			}
		});
		checklist.addReloadButtonSelectonListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				populateModuleList(checklist.getSelectedItems());
			}
		});
	}

	/**
	 * Reconfigures this checklist, changing its sync URI, remote connection, and set of selected items.
	 * 
	 * @param newURI
	 *            synchronization URI, or <code>null</code>
	 * @param remoteConnection
	 *            {@link IRemoteConnection}, or <code>null</code>
	 * @param selectedItems
	 *            items to check, or <code>null</code>
	 * 
	 * @see EnvManagerConfigWidget#configurationChanged(URI, IRemoteConnection, List)
	 */
	public void reset(URI newURI, final IRemoteConnection remoteConnection, final List<String> selectedItems) {
		this.remoteConnection = remoteConnection;

		final boolean syncURIHasChanged = newURI == null || !newURI.equals(lastSyncURI);
		final boolean selectedItemsHaveChanged = selectedItems == null || !selectedItems.equals(lastSelectedItems);
		if (newURI != null) {
			lastSyncURI = newURI;
		}
		if (selectedItems != null) {
			lastSelectedItems = selectedItems;
		}
		if (messageComposite != null && checklist != null) {
			if (syncURIHasChanged || selectedItemsHaveChanged) {
				if (connectionIsOpen()) {
					inBackgroundThreadDetectEnvManager(remoteConnection, selectedItems);
				} else {
					setNotConnectedMessage();
				}
			}
		}
	}

	private void inBackgroundThreadDetectEnvManager(final IRemoteConnection remoteConnection, final List<String> selectedItems) {
		if (repopulateJob == null) {
			repopulateJob = new Job(Messages.EnvManagerChecklist_DetectingRemoteEMS) {
				@Override
				protected IStatus run(IProgressMonitor monitor) {
					try {
						if (!isDisposed()) {
							getDisplay().syncExec(new Runnable() {
								@Override
								public void run() {
									setDetectingMessage();
								}
							});
						}
						envManager = EnvManagerRegistry.getEnvManager(monitor, remoteConnection);
						final String description = envManager.getDescription(monitor);
						inUIThreadDisplayChecklist(selectedItems, description);
						return Status.OK_STATUS;
					} catch (final Exception e) {
						EMSUIPlugin.log(e);
						return new Status(IStatus.ERROR, EMSUIPlugin.PLUGIN_ID, e.getLocalizedMessage(), e);
					}
				}
			};
			repopulateJob.setPriority(Job.INTERACTIVE);
		}
		repopulateJob.schedule();
	}

	private void setDetectingMessage() {
		messageLabel.setText(NLS.bind(Messages.EnvManagerChecklist_DetectingEMSPleaseWait, getConnectionName()));
		connectButton.setEnabled(false);
		connectButton.setVisible(false);
		messageComposite.pack();

		stackLayout.topControl = messageComposite;
		stack.layout(true, true);
	}

	private void inUIThreadDisplayChecklist(final List<String> selectedItems, final String description) {
		if (!isDisposed()) {
			getDisplay().asyncExec(new Runnable() {
				@Override
				public void run() {
					String instructions = envManager.getInstructions();
					if (instructions.length() > 0) {
						instructions += Messages.EnvManagerChecklist_SettingsOnEnvironmentsPageAreAppliedBeforehand;
					}
					checklist.setInstructions(instructions, 250);
					checklist.setComparator(envManager.getComparator());

					if (envManager.equals(EnvManagerRegistry.getNullEnvManager())) {
						setIncompatibleInstallationMessage();
					} else {
						checklist.setTitle(
								NLS.bind(
										Messages.EnvManagerChecklist_EnvManagerInfo,
										description,
										getConnectionName()));

						checklist.setEnabledAndVisible(false);
						stackLayout.topControl = checklist;
						stack.layout(true, true);
						errorListener.errorCleared();

						populateModuleList(selectedItems);
					}
				}
			});
		}
	}

	private boolean connectionIsOpen() {
		if (remoteConnection == null) {
			return false;
		} else {
			return remoteConnection.isOpen();
		}
	}

	private void setIncompatibleInstallationMessage() {
		messageLabel.setText(NLS.bind(Messages.EnvManagerChecklist_NoSupportedEMSInstalled, getConnectionName()));
		connectButton.setEnabled(false);
		connectButton.setVisible(false);
		messageComposite.pack();

		stackLayout.topControl = messageComposite;
		stack.layout(true, true);
	}

	private void populateModuleList(final List<String> selectedItems) {
		isValid = false;
		checklist.asyncRepopulate(new AsyncRepopulationStrategy() {
			@Override
			public String getMessage() {
				return Messages.EnvManagerChecklist_PleaseWaitRetrievingModuleList;
			}

			@Override
			public List<String> computeItems(IProgressMonitor monitor) throws Exception {
				return envManager.determineAvailableElements(monitor);
			}

			@Override
			public List<String> computeSelectedItems(IProgressMonitor monitor) throws Exception {
				if (selectedItems != null) {
					return selectedItems;
				} else {
					return envManager.determineDefaultElements(new NullProgressMonitor());
				}
			}

			@Override
			public void afterRepopulation() {
				checklist.setEnabledAndVisible(EnvManagerChecklist.this.isEnabled());
				isValid = true;
			}
		});
	}

	/**
	 * Get the text of the elements which are checked in the checklist. This list may be empty but is never <code>null</code>.
	 * It is, in theory, a subset of the strings returned by {@link IEnvManager#determineAvailableElements(IProgressMonitor)}.
	 * 
	 * This list is only valid if {@link #isValid() is true}
	 * 
	 * @return list of checked elements
	 * 
	 * @see EnvManagerConfigWidget#getSelectedElements()
	 */
	public List<String> getSelectedElements() {
		return checklist.getSelectedItems();
	}

	/**
	 * @return true iff the checklist is visible and enabled for modification by the user
	 */
	/*
	 * @see EnvManagerConfigWidget#isChecklistEnabled()
	 */
	public boolean isChecklistEnabled() {
		return checklist.isEnabled();
	}

	/**
	 * Test if the checklist is properly configured. The contents of the checklist is only valid when this is true.
	 * 
	 * @return true if checklist is valid
	 */
	public boolean isValid() {
		return isValid;
	}

	/** @return the {@link IEnvManager} being used to populate this checklist */
	public IEnvManager getEnvManager() {
		return envManager;
	}
}
