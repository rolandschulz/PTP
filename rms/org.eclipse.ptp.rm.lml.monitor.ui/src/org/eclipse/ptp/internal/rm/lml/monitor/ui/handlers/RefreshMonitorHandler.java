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
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.core.commands.HandlerEvent;
import org.eclipse.core.commands.IHandler;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.ptp.rm.lml.monitor.core.IMonitorControl;
import org.eclipse.ptp.rm.lml.monitor.core.MonitorControlManager;
import org.eclipse.ui.handlers.HandlerUtil;

public class RefreshMonitorHandler extends AbstractHandler implements IHandler, ISelectionChangedListener {
	private IMonitorControl fMonitor;

	public RefreshMonitorHandler() {
		MonitorControlManager.getInstance().addSelectionChangedListener(this);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.jface.viewers.ISelectionChangedListener#selectionChanged(org.eclipse.jface.viewers.SelectionChangedEvent)
	 */
	public void selectionChanged(SelectionChangedEvent event) {
		ISelection sel = event.getSelection();
		if (!sel.isEmpty() && sel instanceof IStructuredSelection) {
			IStructuredSelection ss = (IStructuredSelection) sel;
			if (ss.size() == 1) {
				fMonitor = (IMonitorControl) ss.getFirstElement();
			}
		} else {
			fMonitor = null;
		}
		fireHandlerChanged(new HandlerEvent(RefreshMonitorHandler.this, true, false));
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.core.commands.AbstractHandler#dispose()
	 */
	@Override
	public void dispose() {
		MonitorControlManager.getInstance().removeSelectionChangedListener(this);
		super.dispose();
	}

	public Object execute(ExecutionEvent event) throws ExecutionException {
		ISelection selection = HandlerUtil.getCurrentSelection(event);
		if (!selection.isEmpty() && selection instanceof IStructuredSelection) {
			final List<IMonitorControl> monitors = new ArrayList<IMonitorControl>();
			for (Iterator<?> itr = ((IStructuredSelection) selection).iterator(); itr.hasNext();) {
				Object sel = itr.next();
				if (sel instanceof IMonitorControl) {
					monitors.add((IMonitorControl) sel);
				}
			}
			for (IMonitorControl monitor : monitors) {
				if (monitor.isActive()) {
					monitor.refresh();
				}
			}
		}
		return null;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.core.commands.AbstractHandler#isEnabled()
	 */
	@Override
	public boolean isEnabled() {
		return (fMonitor != null) ? fMonitor.isActive() : false;
	}

}
