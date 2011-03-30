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
import org.eclipse.ptp.remote.core.IRemoteServices;
import org.eclipse.ptp.remote.core.PTPRemoteCorePlugin;
import org.eclipse.ptp.remote.core.PreferenceConstants;
import org.eclipse.ptp.remote.core.Preferences;
import org.eclipse.ptp.remote.ui.IRemoteUIConnectionManager;
import org.eclipse.ptp.remote.ui.PTPRemoteUIPlugin;
import org.eclipse.ptp.remote.ui.messages.Messages;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
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
 * Widget to allow the user to select a service provider and connection.
 * Provides a "New" button to create a new connection.
 * 
 * If title is supplied then the widget will be placed in a group.
 * 
 * @since 5.0
 * 
 */
public class RemoteConnectionWidget extends Composite {
	protected class WidgetListener implements SelectionListener, ModifyListener {
		/** State of the listener (enabled/disabled). */
		private boolean listenerEnabled = true;

		/**
		 * Disable listener, received events shall be ignored.
		 */
		public void disable() {
			setEnabled(false);
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

		public void modifyText(ModifyEvent e) {
			if (isEnabled()) {
				Object source = e.getSource();
				if (source == remoteCombo) {
					handleRemoteServiceSelected(null);
				} else if (source == connectionCombo) {
					handleConnectionSelected();
				}
			}
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
				if (source == newConnectionButton) {
					handleNewRemoteConnectionSelected();
				}
			}
		}

		protected void doWidgetDefaultSelected(SelectionEvent e) {
			// Default empty implementation.
		}

	}

	private static final String EMPTY_STRING = ""; //$NON-NLS-1$

	private Combo remoteCombo = null;
	private final Combo connectionCombo;
	private final Button newConnectionButton;

	private IRemoteServices[] fRemoteServices;
	private IRemoteConnection fSelectedConnection;
	private IRemoteServices fSelectedServices;
	private IRunnableContext fContext;
	private final ListenerList fSelectionListeners = new ListenerList();

	private final WidgetListener fWidgetListener = new WidgetListener();

	public RemoteConnectionWidget(Composite parent, int style, String title) {
		super(parent, style);

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
		Composite remoteComp = new Composite(body, SWT.NONE);
		GridLayout remoteLayout = new GridLayout();
		remoteLayout.numColumns = 4;
		remoteLayout.marginWidth = 0;
		remoteComp.setLayout(remoteLayout);
		GridData gd = new GridData(GridData.FILL_HORIZONTAL);
		gd.horizontalSpan = 4;
		remoteComp.setLayoutData(gd);

		/*
		 * Check if we need a remote services combo, or we should just use the
		 * default provider
		 */
		String id = Preferences
				.getString(PTPRemoteCorePlugin.getUniqueIdentifier(), PreferenceConstants.DEFAULT_REMOTE_SERVICES_ID);
		if (id != null) {
			fSelectedServices = getRemoteServices(id);
		}
		if (fSelectedServices == null) {
			fSelectedServices = PTPRemoteCorePlugin.getDefault().getDefaultServices();
		}
		if (id == null) {
			/*
			 * Remote provider
			 */
			Label label = new Label(remoteComp, SWT.NONE);
			label.setText(Messages.RemoteConnectionWidget_remoteServiceProvider);
			gd = new GridData();
			gd.horizontalSpan = 1;
			label.setLayoutData(gd);

			remoteCombo = new Combo(remoteComp, SWT.DROP_DOWN | SWT.READ_ONLY);
			gd = new GridData(GridData.FILL_HORIZONTAL);
			gd.horizontalSpan = 3;
			remoteCombo.setLayoutData(gd);
			remoteCombo.addModifyListener(fWidgetListener);
			remoteCombo.setFocus();
		}

		/*
		 * Remote connection
		 */
		Label label = new Label(remoteComp, SWT.NONE);
		label.setText(Messages.RemoteConnectionWidget_connectionName);
		gd = new GridData();
		gd.horizontalSpan = 1;
		label.setLayoutData(gd);

		connectionCombo = new Combo(remoteComp, SWT.DROP_DOWN | SWT.READ_ONLY);
		gd = new GridData(GridData.FILL_HORIZONTAL);
		gd.horizontalSpan = 2;
		connectionCombo.setLayoutData(gd);
		connectionCombo.addModifyListener(fWidgetListener);
		if (id != null) {
			connectionCombo.setFocus();
		}

		newConnectionButton = new Button(remoteComp, SWT.PUSH);
		newConnectionButton.setText(Messages.RemoteConnectionWidget_new);
		GridData data = new GridData();
		newConnectionButton.setLayoutData(data);
		newConnectionButton.addSelectionListener(fWidgetListener);

		if (remoteCombo != null) {
			initializeRemoteServicesCombo(null);
		}
		handleRemoteServiceSelected(null);
	}

	/**
	 * Adds the listener to the collection of listeners who will be notified
	 * when the user changes the receiver's selection, by sending it one of the
	 * messages defined in the <code>SelectionListener</code> interface.
	 * <p>
	 * <code>widgetSelected</code> is called when the user changes the service
	 * provider or connection.
	 * </p>
	 * 
	 * @param listener
	 *            the listener which should be notified
	 */
	public void addSelectionListener(SelectionListener listener) {
		fSelectionListeners.add(listener);
	}

	/**
	 * Get the connection that is currently selected in the widget, or null if
	 * there is no selected connection.
	 * 
	 * @return selected connection
	 */
	public IRemoteConnection getConnection() {
		return fSelectedConnection;
	}

	/**
	 * Remove a listener that will be notified when one of the widget's controls
	 * are selected
	 * 
	 * @param listener
	 *            listener to remove
	 */
	public void removeSelectionListener(SelectionListener listener) {
		fSelectionListeners.remove(listener);
	}

	/**
	 * Set the selected connection.
	 * 
	 * @param connection
	 *            connection to select
	 */
	public void setConnection(IRemoteConnection connection) {
		handleRemoteServiceSelected(connection);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.swt.widgets.Control#setEnabled(boolean)
	 */
	@Override
	public void setEnabled(boolean enabled) {
		if (remoteCombo != null) {
			remoteCombo.setEnabled(enabled);
		}
		connectionCombo.setEnabled(enabled);
		newConnectionButton.setEnabled(enabled);
	}

	private IRemoteUIConnectionManager getUIConnectionManager() {
		if (fSelectedServices != null) {
			return PTPRemoteUIPlugin.getDefault().getRemoteUIServices(fSelectedServices).getUIConnectionManager();
		}
		return null;
	}

	private void notifyListeners(SelectionEvent e) {
		for (Object listener : fSelectionListeners.getListeners()) {
			((SelectionListener) listener).widgetSelected(e);
		}
	}

	protected IRemoteConnection getRemoteConnection(IRemoteServices services, String name) {
		IRemoteConnectionManager manager = getRemoteConnectionManager(services);
		if (manager != null) {
			return manager.getConnection(name);
		}
		return null;
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

	/**
	 * Handle the section of a new connection. Update connection option buttons
	 * appropriately.
	 */
	protected void handleConnectionSelected() {
		final boolean enabled = fWidgetListener.isEnabled();
		fWidgetListener.disable();
		fSelectedConnection = null;
		int currentSelection = connectionCombo.getSelectionIndex();
		if (currentSelection >= 0 && fSelectedServices != null) {
			String connectionName = connectionCombo.getItem(currentSelection);
			fSelectedConnection = getRemoteConnection(fSelectedServices, connectionName);
		}
		Event evt = new Event();
		evt.widget = this;
		notifyListeners(new SelectionEvent(evt));
		fWidgetListener.setEnabled(enabled);
	}

	/**
	 * Handle creation of a new connection by pressing the 'New...' button.
	 * Calls handleRemoteServicesSelected() to update the connection combo with
	 * the new connection.
	 * 
	 * TODO should probably select the new connection
	 */
	protected void handleNewRemoteConnectionSelected() {
		if (getUIConnectionManager() != null) {
			handleRemoteServiceSelected(getUIConnectionManager().newConnection(getShell()));
		}
	}

	/**
	 * Handle selection of a new remote services provider from the remote
	 * services combo.
	 * 
	 * The assumption is that this will trigger a call to the selection handler
	 * for the connection combo.
	 * 
	 * @param conn
	 *            connection to select as current. If conn is null, select the
	 *            first item in the list.
	 */
	protected void handleRemoteServiceSelected(IRemoteConnection conn) {
		final boolean enabled = fWidgetListener.isEnabled();
		fWidgetListener.disable();

		if (remoteCombo != null) {
			/*
			 * If a connection was supplied, set its remote service provider in
			 * the combo
			 */
			if (conn != null) {
				IRemoteServices services = conn.getRemoteServices();
				if (fRemoteServices != null) {
					for (int index = 0; index < fRemoteServices.length; index++) {
						if (fRemoteServices[index].getId().equals(services.getId())) {
							remoteCombo.select(index);
							break;
						}
					}
				}
			}

			/*
			 * Get the currently selected services
			 */
			int selectionIndex = remoteCombo.getSelectionIndex();
			if (fRemoteServices != null && fRemoteServices.length > 0 && selectionIndex >= 0) {
				fSelectedServices = fRemoteServices[selectionIndex];
			}
		}

		/*
		 * Populate the connection combo and select the connection
		 */
		IRemoteConnectionManager connectionManager = fSelectedServices.getConnectionManager();
		IRemoteConnection[] connections = connectionManager.getConnections();
		Arrays.sort(connections, new Comparator<IRemoteConnection>() {
			public int compare(IRemoteConnection c1, IRemoteConnection c2) {
				return c1.getName().compareToIgnoreCase(c2.getName());
			}
		});
		connectionCombo.removeAll();
		int selected = 0;
		for (int i = 0; i < connections.length; i++) {
			connectionCombo.add(connections[i].getName());
			if (conn != null && connections[i].equals(conn)) {
				selected = i;
			}
		}

		if (connections.length > 0) {
			connectionCombo.select(selected);
		}

		/*
		 * A connection is always going to be selected when a remote service
		 * provider is selected, so make sure the handlers get notified
		 */
		handleConnectionSelected();

		/*
		 * Enable 'new' button if new connections are supported
		 */
		newConnectionButton.setEnabled(fSelectedServices.canCreateConnections());

		fWidgetListener.setEnabled(enabled);
	}

	/**
	 * Initialize the contents of the remote services combo. Keeps an array of
	 * remote services that matches the combo elements. Returns the id of the
	 * selected element.
	 */
	protected String initializeRemoteServicesCombo(String id) {
		final boolean enabled = fWidgetListener.isEnabled();
		fWidgetListener.disable();
		fRemoteServices = PTPRemoteUIPlugin.getDefault().getRemoteServices(fContext);
		IRemoteServices defServices;
		if (id != null) {
			defServices = getRemoteServices(id);
		} else {
			defServices = PTPRemoteCorePlugin.getDefault().getDefaultServices();
		}
		int defIndex = 0;
		Arrays.sort(fRemoteServices, new Comparator<IRemoteServices>() {
			public int compare(IRemoteServices c1, IRemoteServices c2) {
				return c1.getName().compareToIgnoreCase(c2.getName());
			}
		});
		remoteCombo.removeAll();
		for (int i = 0; i < fRemoteServices.length; i++) {
			remoteCombo.add(fRemoteServices[i].getName());
			if (fRemoteServices[i].equals(defServices)) {
				defIndex = i;
			}
		}
		if (fRemoteServices.length > 0) {
			remoteCombo.select(defIndex);
		}
		fWidgetListener.setEnabled(enabled);
		return defServices.getId();
	}

	protected void setRunnableContext(IRunnableContext context) {
		fContext = context;
	}

}
