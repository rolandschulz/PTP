/*******************************************************************************
 * Copyright (c) 2011 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Greg Watson - Initial API and implementation
 *******************************************************************************/
package org.eclipse.ptp.remote.ui.widgets;

import java.util.Arrays;
import java.util.Comparator;

import org.eclipse.core.runtime.ListenerList;
import org.eclipse.jface.operation.IRunnableContext;
import org.eclipse.ptp.remote.core.IRemoteConnection;
import org.eclipse.ptp.remote.core.IRemoteConnectionManager;
import org.eclipse.ptp.remote.core.IRemotePreferenceConstants;
import org.eclipse.ptp.remote.core.IRemoteServices;
import org.eclipse.ptp.remote.core.PTPRemoteCorePlugin;
import org.eclipse.ptp.remote.core.Preferences;
import org.eclipse.ptp.remote.ui.IRemoteUIConnectionManager;
import org.eclipse.ptp.remote.ui.PTPRemoteUIPlugin;
import org.eclipse.ptp.remote.ui.messages.Messages;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Label;

/**
 * Widget to allow the user to select a service provider and connection. Provides a "New" button to create a new connection.
 * 
 * If title is supplied then the widget will be placed in a group.
 * 
 * @since 5.0
 * 
 */
public class RemoteConnectionWidget extends Composite {
	protected class WidgetListener implements SelectionListener {
		/** State of the listener (enabled/disabled). */
		private boolean listenerEnabled = true;

		/**
		 * Disable listener, received events shall be ignored.
		 */
		public void disable() {
			setEnabled(false);
		}

		protected void doWidgetDefaultSelected(SelectionEvent e) {
			// Default empty implementation.
		}

		/**
		 * Enable the listener to handle events.
		 */
		public void enable() {
			setEnabled(true);
		}

		/**
		 * Test if the listener is enabled.
		 */
		public synchronized boolean isEnabled() {
			return listenerEnabled;
		}

		/**
		 * Set listener enabled state
		 * 
		 * @param enabled
		 */
		public synchronized void setEnabled(boolean enabled) {
			listenerEnabled = enabled;
		}

		public void widgetDefaultSelected(SelectionEvent e) {
			if (isEnabled()) {
				widgetSelected(e);
			}
		}

		public void widgetSelected(SelectionEvent e) {
			if (isEnabled()) {
				Object source = e.getSource();
				if (source == fServicesCombo) {
					handleRemoteServiceSelected(null);
				} else if (source == fConnectionCombo) {
					handleConnectionSelected();
				} else if (source == fNewConnectionButton) {
					handleNewRemoteConnectionSelected();
				}
			}
		}

	}

	private static final String EMPTY_STRING = ""; //$NON-NLS-1$

	private Combo fServicesCombo = null;
	private Button fLocalButton;
	private Button fRemoteButton;
	private final Combo fConnectionCombo;
	private final Button fNewConnectionButton;
	private final Group fConnectionGroup;

	private final IRemoteServices[] fRemoteServices;
	private IRemoteConnection fSelectedConnection;
	private IRemoteServices fDefaultServices;
	private boolean fSelectionListernersEnabled = true;
	private boolean fEnabled = true;

	private final IRunnableContext fContext;

	private String[] fAttrHints;
	private String[] fAttrHintValues;

	private final ListenerList fSelectionListeners = new ListenerList();
	private final WidgetListener fWidgetListener = new WidgetListener();

	/**
	 * Constructor
	 * 
	 * @param parent
	 *            parent composite
	 * @param style
	 *            style or SWT.NONE
	 * @param title
	 *            title is supplied then the widget will be placed in a group
	 * @param context
	 *            runnable context, or null
	 */
	public RemoteConnectionWidget(Composite parent, int style, String title, IRunnableContext context) {
		super(parent, style);
		fContext = context;

		GridLayout layout = new GridLayout(1, false);
		layout.marginHeight = 0;
		layout.marginWidth = 0;
		setLayout(layout);
		setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));

		Composite body = this;

		if (title != null) {
			Group group = new Group(this, SWT.NONE);
			group.setText(title);
			group.setLayout(new GridLayout(1, false));
			group.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));
			body = group;
		}

		/*
		 * Composite for remote information
		 */
		fConnectionGroup = new Group(body, SWT.NONE);
		fConnectionGroup.setText(Messages.RemoteConnectionWidget_Connection_Type);
		GridLayout remoteLayout = new GridLayout();
		remoteLayout.numColumns = 4;
		remoteLayout.marginWidth = 0;
		fConnectionGroup.setLayout(remoteLayout);
		GridData gd = new GridData(GridData.FILL_HORIZONTAL);
		fConnectionGroup.setLayoutData(gd);

		/*
		 * Check if we need a remote services combo, or we should just use the default provider
		 */
		String id = Preferences.getString(PTPRemoteCorePlugin.getUniqueIdentifier(),
				IRemotePreferenceConstants.PREF_REMOTE_SERVICES_ID);
		if (id != null) {
			fDefaultServices = getRemoteServices(id);
		}
		if (fDefaultServices == null) {
			/*
			 * Remote provider
			 */
			Label label = new Label(fConnectionGroup, SWT.NONE);
			label.setText(Messages.RemoteConnectionWidget_remoteServiceProvider);
			gd = new GridData();
			gd.horizontalSpan = 1;
			label.setLayoutData(gd);

			fServicesCombo = new Combo(fConnectionGroup, SWT.DROP_DOWN | SWT.READ_ONLY);
			gd = new GridData(GridData.FILL_HORIZONTAL);
			gd.horizontalSpan = 3;
			fServicesCombo.setLayoutData(gd);
			fServicesCombo.addSelectionListener(fWidgetListener);
			fServicesCombo.setFocus();

			Label remoteLabel = new Label(fConnectionGroup, SWT.NONE);
			remoteLabel.setText(Messages.RemoteConnectionWidget_connectionName);
			gd = new GridData();
			gd.horizontalSpan = 1;
			label.setLayoutData(gd);
		} else {
			fLocalButton = new Button(fConnectionGroup, SWT.RADIO);
			fLocalButton.setText(Messages.RemoteConnectionWidget_Local);
			GridData data = new GridData();
			data.horizontalSpan = 1;
			fLocalButton.setLayoutData(data);
			fLocalButton.addSelectionListener(new SelectionAdapter() {
				@Override
				public void widgetSelected(SelectionEvent e) {
					updateEnablement();
					handleConnectionSelected();
				}
			});
			fLocalButton.setSelection(false);

			fRemoteButton = new Button(fConnectionGroup, SWT.RADIO);
			fRemoteButton.setText(Messages.RemoteConnectionWidget_Remote);
			data = new GridData();
			data.horizontalSpan = 1;
			fRemoteButton.setLayoutData(data);
		}

		fConnectionCombo = new Combo(fConnectionGroup, SWT.DROP_DOWN | SWT.READ_ONLY);
		gd = new GridData(GridData.FILL_HORIZONTAL);
		gd.horizontalSpan = 1;
		fConnectionCombo.setLayoutData(gd);
		fConnectionCombo.addSelectionListener(fWidgetListener);
		if (id != null) {
			fConnectionCombo.setFocus();
		}
		fConnectionCombo.setEnabled(false);

		fNewConnectionButton = new Button(fConnectionGroup, SWT.PUSH);
		fNewConnectionButton.setText(Messages.RemoteConnectionWidget_new);
		gd = new GridData();
		fNewConnectionButton.setLayoutData(gd);
		fNewConnectionButton.addSelectionListener(fWidgetListener);

		fRemoteServices = PTPRemoteUIPlugin.getDefault().getRemoteServices(fContext);

		if (fServicesCombo != null) {
			initializeRemoteServicesCombo(null);
		}

		handleRemoteServiceSelected(null);
	}

	/**
	 * Adds the listener to the collection of listeners who will be notified when the user changes the receiver's selection, by
	 * sending it one of the messages defined in the <code>SelectionListener</code> interface.
	 * <p>
	 * <code>widgetSelected</code> is called when the user changes the service provider or connection.
	 * </p>
	 * 
	 * @param listener
	 *            the listener which should be notified
	 */
	public void addSelectionListener(SelectionListener listener) {
		fSelectionListeners.add(listener);
	}

	/**
	 * Get the connection that is currently selected in the widget, or null if there is no selected connection.
	 * 
	 * @return selected connection
	 */
	public IRemoteConnection getConnection() {
		return fSelectedConnection;
	}

	private IRemoteConnection getRemoteConnection(IRemoteServices services, String name) {
		IRemoteConnectionManager manager = getRemoteConnectionManager(services);
		if (manager != null) {
			return manager.getConnection(name);
		}
		return null;
	}

	private IRemoteConnection getRemoteConnection(String name) {
		IRemoteServices services = getSelectedServices();
		if (fDefaultServices != null && name.equals(IRemoteConnectionManager.DEFAULT_CONNECTION_NAME)) {
			services = PTPRemoteCorePlugin.getDefault().getDefaultServices();
		}
		return getRemoteConnection(services, name);
	}

	protected IRemoteConnectionManager getRemoteConnectionManager(IRemoteServices services) {
		if (services != null) {
			return services.getConnectionManager();
		}
		return null;
	}

	protected IRemoteServices getRemoteServices(String id) {
		if (id != null && !id.equals(EMPTY_STRING)) {
			return PTPRemoteUIPlugin.getDefault().getRemoteServices(id, fContext);
		}
		return null;
	}

	private IRemoteServices getSelectedServices() {
		if (fDefaultServices != null) {
			return fDefaultServices;
		}
		int selectionIndex = fServicesCombo.getSelectionIndex();
		if (fRemoteServices.length > 0 && selectionIndex > 0) {
			return fRemoteServices[selectionIndex - 1];
		}
		return null;
	}

	private IRemoteUIConnectionManager getUIConnectionManager() {
		if (fDefaultServices != null) {
			return PTPRemoteUIPlugin.getDefault().getRemoteUIServices(fDefaultServices).getUIConnectionManager();
		}
		return null;
	}

	/**
	 * Handle the section of a new connection. Update connection option buttons appropriately.
	 */
	protected void handleConnectionSelected() {
		final boolean enabled = fWidgetListener.isEnabled();
		fWidgetListener.disable();
		IRemoteConnection selectedConnection = null;
		if (fDefaultServices != null && fLocalButton.getSelection()) {
			selectedConnection = PTPRemoteCorePlugin.getDefault().getDefaultServices().getConnectionManager()
					.getConnection(IRemoteConnectionManager.DEFAULT_CONNECTION_NAME);
		} else {
			int currentSelection = fConnectionCombo.getSelectionIndex();
			if (currentSelection >= 0) {
				String connectionName = fConnectionCombo.getItem(currentSelection);
				selectedConnection = getRemoteConnection(connectionName);
			}
		}
		if (selectedConnection == null || fSelectedConnection == null
				|| !selectedConnection.getName().equals(fSelectedConnection.getName())) {
			fSelectedConnection = selectedConnection;
			Event evt = new Event();
			evt.widget = this;
			notifyListeners(new SelectionEvent(evt));
		}
		fWidgetListener.setEnabled(enabled);
	}

	/**
	 * Handle creation of a new connection by pressing the 'New...' button. Calls handleRemoteServicesSelected() to update the
	 * connection combo with the new connection.
	 * 
	 * TODO should probably select the new connection
	 */
	protected void handleNewRemoteConnectionSelected() {
		if (getUIConnectionManager() != null) {
			IRemoteConnection conn = getUIConnectionManager().newConnection(getShell(), fAttrHints, fAttrHintValues);
			if (conn != null) {
				handleRemoteServiceSelected(conn);
				handleConnectionSelected();
			}
		}
	}

	/**
	 * Handle selection of a new remote services provider from the remote services combo. Handles the special case where the
	 * services combo is null and a local connection is supplied. In this case, the selected services are not changed.
	 * 
	 * The assumption is that this will trigger a call to the selection handler for the connection combo.
	 * 
	 * @param conn
	 *            connection to select as current. If conn is null, select the first item in the list.
	 * @param notify
	 *            if true, notify handlers that the connection has changed. This should only happen if the user changes the
	 *            connection.
	 */
	protected void handleRemoteServiceSelected(IRemoteConnection conn) {
		final boolean enabled = fWidgetListener.isEnabled();
		fWidgetListener.disable();
		try {
			IRemoteServices selectedServices = getSelectedServices();
			if (conn != null) {
				selectedServices = conn.getRemoteServices();
			}

			if (fDefaultServices == null) {
				/*
				 * If a connection was supplied, set its remote service provider in the combo. Otherwise get the currently selected
				 * service.
				 */
				if (conn != null) {
					for (int index = 0; index < fRemoteServices.length; index++) {
						if (fRemoteServices[index].getId().equals(selectedServices.getId())) {
							fServicesCombo.select(index + 1);
							break;
						}
					}
				} else {
					int selectionIndex = fServicesCombo.getSelectionIndex();
					if (fRemoteServices.length > 0 && selectionIndex > 0) {
						selectedServices = fRemoteServices[selectionIndex - 1];
					}
				}
			}

			fConnectionCombo.removeAll();
			fConnectionCombo.add(Messages.RemoteConnectionWidget_selectConnection);

			if (selectedServices == null) {
				fConnectionCombo.select(0);
				fConnectionCombo.setEnabled(false);
				fNewConnectionButton.setEnabled(false);
			} else {
				fConnectionCombo.setEnabled(true);

				IRemoteConnectionManager connectionManager = selectedServices.getConnectionManager();

				/*
				 * Populate the connection combo and select the connection
				 */
				IRemoteConnection[] connections = connectionManager.getConnections();
				Arrays.sort(connections, new Comparator<IRemoteConnection>() {
					public int compare(IRemoteConnection c1, IRemoteConnection c2) {
						return c1.getName().compareToIgnoreCase(c2.getName());
					}
				});
				int selected = 0;
				int offset = 1;

				for (int i = 0; i < connections.length; i++) {
					fConnectionCombo.add(connections[i].getName());
					if (conn != null && connections[i].getName().equals(conn.getName())) {
						selected = i + offset;
					}
				}

				fConnectionCombo.select(selected);
				handleConnectionSelected();

				/*
				 * Enable 'new' button if new connections are supported
				 */
				fNewConnectionButton.setEnabled(selectedServices.canCreateConnections());
			}
		} finally {
			fWidgetListener.setEnabled(enabled);
		}
	}

	/**
	 * Initialize the contents of the remote services combo. Keeps an array of remote services that matches the combo elements.
	 * Returns the id of the selected element.
	 * 
	 * @since 6.0
	 */
	protected void initializeRemoteServicesCombo(String id) {
		final boolean enabled = fWidgetListener.isEnabled();
		fWidgetListener.disable();
		IRemoteServices defService = null;
		if (id != null) {
			defService = getRemoteServices(id);
		}
		Arrays.sort(fRemoteServices, new Comparator<IRemoteServices>() {
			public int compare(IRemoteServices c1, IRemoteServices c2) {
				return c1.getName().compareToIgnoreCase(c2.getName());
			}
		});
		fServicesCombo.removeAll();
		int offset = 1;
		int defIndex = 0;
		fServicesCombo.add(Messages.RemoteConnectionWidget_selectRemoteProvider);
		for (int i = 0; i < fRemoteServices.length; i++) {
			fServicesCombo.add(fRemoteServices[i].getName());
			if (defService != null && fRemoteServices[i].equals(defService)) {
				defIndex = i + offset;
			}
		}
		if (fRemoteServices.length > 0) {
			fServicesCombo.select(defIndex);
		}
		fWidgetListener.setEnabled(enabled);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.swt.widgets.Control#isEnabled()
	 */
	@Override
	public boolean isEnabled() {
		return fEnabled;
	}

	private void notifyListeners(SelectionEvent e) {
		if (fSelectionListernersEnabled) {
			for (Object listener : fSelectionListeners.getListeners()) {
				((SelectionListener) listener).widgetSelected(e);
			}
		}
	}

	/**
	 * Remove a listener that will be notified when one of the widget's controls are selected
	 * 
	 * @param listener
	 *            listener to remove
	 */
	public void removeSelectionListener(SelectionListener listener) {
		fSelectionListeners.remove(listener);
	}

	/**
	 * Set the connection that should be selected in the widget.
	 * 
	 * @param connection
	 *            connection to select
	 */
	public void setConnection(IRemoteConnection connection) {
		fSelectionListernersEnabled = false;
		if (fDefaultServices != null && connection != null
				&& connection.getRemoteServices() == PTPRemoteCorePlugin.getDefault().getDefaultServices()) {
			fLocalButton.setSelection(true);
		} else {
			handleRemoteServiceSelected(connection);
		}
		handleConnectionSelected();
		updateEnablement();
		fSelectionListernersEnabled = true;
	}

	/**
	 * Set the connection that should be selected in the widget.
	 * 
	 * @param id
	 *            remote services id
	 * @param name
	 *            connection name
	 * @since 6.0
	 */
	public void setConnection(String id, String name) {
		IRemoteServices services = getRemoteServices(id);
		if (services != null) {
			IRemoteConnection connection = getRemoteConnection(services, name);
			if (connection != null) {
				setConnection(connection);
			}
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.swt.widgets.Control#setEnabled(boolean)
	 */
	@Override
	public void setEnabled(boolean enabled) {
		fEnabled = enabled;
		updateEnablement();
	}

	/**
	 * Set hints to use when creating a new connection.
	 * 
	 * @param attrHints
	 * @param attrHintValues
	 */
	public void setHints(String[] attrHints, String[] attrHintValues) {
		fAttrHints = attrHints;
		fAttrHintValues = attrHintValues;
	}

	/**
	 * Set the receivers text. Used to change the label for the connection type group.
	 * 
	 * @param string
	 *            the new text
	 * @since 7.0
	 */
	public void setText(String string) {
		fConnectionGroup.setText(string);
	}

	private void updateEnablement() {
		if (fDefaultServices != null) {
			fConnectionCombo.setEnabled(fEnabled && !fLocalButton.getSelection());
			fNewConnectionButton.setEnabled(fEnabled && !fLocalButton.getSelection() && fDefaultServices.canCreateConnections());
		} else {
			IRemoteServices services = getSelectedServices();
			fConnectionCombo.setEnabled(fEnabled && services != null);
			fNewConnectionButton.setEnabled(fEnabled && services != null && services.canCreateConnections());
			fServicesCombo.setEnabled(fEnabled);
		}
	}
}
