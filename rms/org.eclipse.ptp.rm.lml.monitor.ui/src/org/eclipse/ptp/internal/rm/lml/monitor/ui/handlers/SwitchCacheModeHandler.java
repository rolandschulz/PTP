/*******************************************************************************
 * Copyright (c) 2012 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 * IBM Corporation - Initial API and implementation
 *******************************************************************************/
package org.eclipse.ptp.internal.rm.lml.monitor.ui.handlers;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.Command;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.core.commands.IHandler;
import org.eclipse.core.commands.State;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.ptp.internal.rm.lml.monitor.ui.IMonitorUIConstants;
import org.eclipse.ptp.rm.lml.monitor.core.IMonitorControl;
import org.eclipse.ptp.rm.lml.monitor.core.MonitorControlManager;
import org.eclipse.ptp.rm.lml.monitor.core.listeners.IMonitorChangedListener;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.commands.ICommandService;
import org.eclipse.ui.handlers.HandlerUtil;

/**
 * Allows to switch the cache mode flag for the monitor refresh command.
 * Keeps the monitors' cache states in synch with state of the command.
 * 
 */
public class SwitchCacheModeHandler extends AbstractHandler implements IHandler {

	/**
	 * ID of this handler's command for getting access to its instance
	 */
	private static final String COMMAND_ID = "org.eclipse.ptp.rm.lml.monitor.ui.switchCacheMode"; //$NON-NLS-1$

	private static final String STATE_ID = "org.eclipse.ui.commands.toggleState"; //$NON-NLS-1$

	/**
	 * Stores the monitor, which was the last selected one
	 */
	private IMonitorControl lastSelectedMonitor;

	/**
	 * Tries to retrieve the currently selected monitor from the monitor view.
	 * If this is not possible, null is returned.
	 * 
	 * @return the currently selected monitor or null, if there is none
	 */
	private IMonitorControl getSelectedMonitorFromMonitorView() {
		// Update the state with the current selection on startup
		if (PlatformUI.getWorkbench() != null && PlatformUI.getWorkbench().getActiveWorkbenchWindow() != null &&
				PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage() != null) {
			// Try to get a selection directly from the view
			IWorkbenchPage page = PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage();
			ISelection selection = page.getSelection(IMonitorUIConstants.ID_SYSTEM_MONITOR_VIEW);
			if (selection != null && selection instanceof IStructuredSelection) {
				IStructuredSelection sel = (IStructuredSelection) selection;
				if (!sel.isEmpty()) {
					Object selected = sel.getFirstElement();
					if (selected instanceof IMonitorControl) {
						return (IMonitorControl) selected;
					}
				}
			}
		}

		return null;
	}

	/**
	 * Update the state of the associated command according to the
	 * monitor's cache state.
	 * 
	 * @param monitor
	 *            the monitor from which the state is retrieved.
	 */
	private void updateState(IMonitorControl monitor) {

		if (monitor == null) {
			return;
		}

		// Get access to the command
		ICommandService commandService = (ICommandService) PlatformUI.getWorkbench().getActiveWorkbenchWindow()
				.getService(ICommandService.class);
		Command command = commandService.getCommand(COMMAND_ID);
		State state = command.getState(STATE_ID);
		boolean isToggled = (Boolean) state.getValue();
		boolean forceUpdate = !monitor.isCacheActive();

		// Toggle if necessary
		if (isToggled != forceUpdate) {
			try {
				HandlerUtil.toggleCommandState(command);
			} catch (ExecutionException e) {
			}
		}

		state.setValue(forceUpdate);
	}

	/**
	 * Add listeners to changes in the monitoring configurations.
	 * Make an initial state update.
	 */
	public SwitchCacheModeHandler() {
		MonitorControlManager.getInstance().addSelectionChangedListener(new ISelectionChangedListener() {

			@Override
			public void selectionChanged(SelectionChangedEvent event) {
				IMonitorControl monitor = null;

				// Get Access to the monitor
				if (event.getSelection() instanceof IStructuredSelection) {
					IStructuredSelection sel = (IStructuredSelection) event.getSelection();
					if (!sel.isEmpty()) {
						monitor = (IMonitorControl) sel.getFirstElement();
					}
				}

				lastSelectedMonitor = monitor;

				updateState(monitor);
			}
		});

		MonitorControlManager.getInstance().addMonitorChangedListener(new IMonitorChangedListener() {

			@Override
			public void monitorUpdated(IMonitorControl[] monitors) {
				for (IMonitorControl monitor : monitors) {
					if (monitor == lastSelectedMonitor) {
						updateState(monitor);
					}
				}
			}

			@Override
			public void monitorRemoved(IMonitorControl[] monitors) {

			}

			@Override
			public void monitorAdded(IMonitorControl[] monitors) {

			}
		});

		lastSelectedMonitor = getSelectedMonitorFromMonitorView();
		updateState(lastSelectedMonitor);
	}

	@Override
	public Object execute(ExecutionEvent event) throws ExecutionException {
		// First try to get selection from monitor view:
		IMonitorControl monitor = getSelectedMonitorFromMonitorView();
		if (monitor != null) {
			boolean isCacheActive = monitor.isCacheActive();
			monitor.setCacheActive(!isCacheActive);
			return null;
		}
		// Otherwise use the selection from the event
		ISelection selection = HandlerUtil.getCurrentSelection(event);
		final List<IMonitorControl> monitors = new ArrayList<IMonitorControl>();
		if (!selection.isEmpty() && selection instanceof IStructuredSelection) {
			for (Iterator<?> itr = ((IStructuredSelection) selection).iterator(); itr.hasNext();) {
				Object sel = itr.next();
				if (sel instanceof IMonitorControl) {
					monitors.add((IMonitorControl) sel);
				}
			}
			for (IMonitorControl cmonitor : monitors) {
				boolean isCacheActive = cmonitor.isCacheActive();
				cmonitor.setCacheActive(!isCacheActive);
			}
		}
		return null;
	}
}
