/*******************************************************************************
 * Copyright (c) 2011 University of Illinois All rights reserved. This program
 * and the accompanying materials are made available under the terms of the
 * Eclipse Public License v1.0 which accompanies this distribution, and is
 * available at http://www.eclipse.org/legal/epl-v10.html 
 * 	
 * Contributors: 
 * 	Albert L. Rossi - design and implementation
 ******************************************************************************/
package org.eclipse.ptp.rm.lml.monitor.core;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;
import java.util.UUID;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.ListenerList;
import org.eclipse.core.runtime.Status;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.ptp.remote.core.IRemoteConnection;
import org.eclipse.ptp.rm.lml.core.LMLManager;
import org.eclipse.ptp.rm.lml.monitor.LMLMonitorCorePlugin;
import org.eclipse.ptp.rm.lml.monitor.core.listeners.IMonitorChangedListener;
import org.eclipse.ptp.rm.lml.monitor.core.listeners.IMonitorRefreshListener;
import org.eclipse.ptp.rm.lml.monitor.core.messages.Messages;
import org.eclipse.ui.IMemento;
import org.eclipse.ui.WorkbenchException;
import org.eclipse.ui.XMLMemento;
import org.eclipse.ui.progress.UIJob;

public class MonitorControlManager {
	private static final MonitorControlManager fInstance = new MonitorControlManager();
	private static final ListenerList fMonitorChangedListeners = new ListenerList();
	private static final ListenerList fSelectionChangedListeners = new ListenerList();
	private static final ListenerList fMonitorRefreshListeners = new ListenerList();
	private static final String MONITORS_SAVED_STATE = "monitors.xml";//$NON-NLS-1$
	private static final String MONITORS_ATTR = "monitors";//$NON-NLS-1$
	private static final String MONITOR_ID_ATTR = "monitor";//$NON-NLS-1$

	public static MonitorControlManager getInstance() {
		return fInstance;
	}

	public static String generateMonitorId(String remoteServicesId, String connectionName, String monitorType) {
		String bytes = remoteServicesId + "." + connectionName + "." + monitorType; //$NON-NLS-1$//$NON-NLS-2$
		return UUID.nameUUIDFromBytes(bytes.getBytes()).toString();
	}

	private final Map<String, IMonitorControl> fMonitorControls = Collections
			.synchronizedMap(new HashMap<String, IMonitorControl>());
	private final Set<IMonitorControl> fMonitorControlsToStart = new TreeSet<IMonitorControl>();

	private MonitorControlManager() {
	}

	public void addMonitorChangedListener(IMonitorChangedListener listener) {
		fMonitorChangedListeners.add(listener);
	}

	public void addMonitorRefreshListener(IMonitorChangedListener listener) {
		fMonitorRefreshListeners.add(listener);
	}

	public void addSelectionChangedListener(ISelectionChangedListener listener) {
		fSelectionChangedListeners.add(listener);
	}

	public IMonitorControl createMonitorControl(String type, String remoteServicesId, String connectionName) {
		IMonitorControl monitor = new MonitorControl(generateMonitorId(remoteServicesId, connectionName, type));
		monitor.setRemoteServicesId(remoteServicesId);
		monitor.setConnectionName(connectionName);
		monitor.setSystemType(type);
		addMonitorControl(monitor);
		return monitor;
	}

	public void fireMonitorAdded(final IMonitorControl[] monitors) {
		UIJob job = new UIJob(Messages.MonitorControlManager_monitorAddedJobName) {

			@Override
			public IStatus runInUIThread(IProgressMonitor monitor) {
				for (Object listener : fMonitorChangedListeners.getListeners()) {
					((IMonitorChangedListener) listener).monitorAdded(monitors);
				}
				return Status.OK_STATUS;
			}

		};
		job.schedule();
	}

	public void fireMonitorRefresh(final IMonitorControl[] monitors) {
		UIJob job = new UIJob(Messages.MonitorControlManager_monitoRefreshJobName) {

			@Override
			public IStatus runInUIThread(IProgressMonitor monitor) {
				for (Object listener : fMonitorRefreshListeners.getListeners()) {
					((IMonitorRefreshListener) listener).refresh(monitors);
				}
				return Status.OK_STATUS;
			}

		};
		job.schedule();
	}

	public void fireMonitorRemoved(final IMonitorControl[] monitors) {
		UIJob job = new UIJob(Messages.MonitorControlManager_monitorRemovedJobName) {

			@Override
			public IStatus runInUIThread(IProgressMonitor monitor) {
				for (Object listener : fMonitorChangedListeners.getListeners()) {
					((IMonitorChangedListener) listener).monitorRemoved(monitors);
				}
				return Status.OK_STATUS;
			}

		};
		job.schedule();
	}

	public void fireMonitorUpdated(final IMonitorControl[] monitors) {
		UIJob job = new UIJob(Messages.MonitorControlManager_monitorUpdatedJobName) {

			@Override
			public IStatus runInUIThread(IProgressMonitor progress) {
				for (Object listener : fMonitorChangedListeners.getListeners()) {
					((IMonitorChangedListener) listener).monitorUpdated(monitors);
				}
				return Status.OK_STATUS;
			}

		};
		job.schedule();
	}

	public void fireSelectionChanged(final SelectionChangedEvent event) {
		UIJob job = new UIJob(Messages.MonitorControlManager_monitorSelectionChangedJobName) {

			@Override
			public IStatus runInUIThread(IProgressMonitor monitor) {
				for (Object listener : fSelectionChangedListeners.getListeners()) {
					((ISelectionChangedListener) listener).selectionChanged(event);
				}
				String monitorId = null;
				if (event.getSelection() instanceof IStructuredSelection) {
					IStructuredSelection sel = (IStructuredSelection) event.getSelection();
					if (!sel.isEmpty()) {
						monitorId = ((IMonitorControl) sel.getFirstElement()).getMonitorId();
					}
				}
				LMLManager.getInstance().selectLgui(monitorId);
				return Status.OK_STATUS;
			}

		};
		job.schedule();
	}

	public IMonitorControl getMonitorControl(IRemoteConnection connection, String monitorType) {
		return fMonitorControls.get(generateMonitorId(connection.getRemoteServices().getId(), connection.getName(), monitorType));
	}

	public IMonitorControl getMonitorControl(String remoteServicesId, String connectionName, String monitorType) {
		return fMonitorControls.get(generateMonitorId(remoteServicesId, connectionName, monitorType));
	}

	public Collection<IMonitorControl> getMonitorControls() {
		return Collections.unmodifiableCollection(fMonitorControls.values());
	}

	public void removeMonitorChangedListener(IMonitorChangedListener listener) {
		fMonitorChangedListeners.remove(listener);
	}

	public void removeMonitorControls(IMonitorControl[] monitors) {
		for (IMonitorControl monitor : monitors) {
			fMonitorControls.remove(monitor.getMonitorId());
		}
		fireMonitorRemoved(monitors);
	}

	public void removeMonitorRefreshListener(IMonitorChangedListener listener) {
		fMonitorRefreshListeners.remove(listener);
	}

	public void removeSelectionChangedListener(ISelectionChangedListener listener) {
		fSelectionChangedListeners.remove(listener);
	}

	public void start() {
		fMonitorControls.clear();
		fMonitorControlsToStart.clear();
		loadMonitors();
		startMonitors();
	}

	public void stop() throws CoreException {
		stopMonitors();
		saveMonitors();
	}

	private void addMonitorControl(IMonitorControl monitor) {
		fMonitorControls.put(monitor.getMonitorId(), monitor);
		fireMonitorAdded(new IMonitorControl[] { monitor });
	}

	private File getSaveLocation() {
		return LMLMonitorCorePlugin.getDefault().getStateLocation().append(MONITORS_SAVED_STATE).toFile();
	}

	private void loadMonitors() {
		try {
			FileReader reader = new FileReader(getSaveLocation());
			IMemento memento = XMLMemento.createReadRoot(reader);
			IMemento[] monitorsMemento = memento.getChildren(MONITOR_ID_ATTR);
			for (IMemento monitorMemento : monitorsMemento) {
				IMonitorControl monitor = new MonitorControl(monitorMemento.getID());
				if (monitor.load(monitorMemento)) {
					fMonitorControlsToStart.add(monitor);
				}
				addMonitorControl(monitor);
			}
		} catch (FileNotFoundException e) {
			LMLMonitorCorePlugin.log(e.getLocalizedMessage());
		} catch (WorkbenchException e) {
			LMLMonitorCorePlugin.log(e.getLocalizedMessage());
		}
	}

	private void saveMonitors() {
		final XMLMemento memento = XMLMemento.createWriteRoot(MONITORS_ATTR);
		for (IMonitorControl monitor : fMonitorControls.values()) {
			final IMemento monitorMemento = memento.createChild(MONITOR_ID_ATTR, monitor.getMonitorId());
			monitor.save(monitorMemento);
		}

		try {
			FileWriter writer = new FileWriter(getSaveLocation());
			memento.save(writer);
		} catch (final IOException e) {
			LMLMonitorCorePlugin.log(e.getLocalizedMessage());
		}
	}

	private void startMonitors() {
		// Not implemented
	}

	private void stopMonitors() {
		for (IMonitorControl monitor : fMonitorControls.values()) {
			try {
				monitor.stop();
			} catch (CoreException e) {
				LMLMonitorCorePlugin.log(e.getLocalizedMessage());
			}
		}
	}
}