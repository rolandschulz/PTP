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

import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.core.commands.HandlerEvent;
import org.eclipse.core.commands.IHandler;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.SubMonitor;
import org.eclipse.jface.dialogs.ErrorDialog;
import org.eclipse.jface.operation.IRunnableWithProgress;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.ptp.internal.rm.lml.monitor.ui.LMLMonitorUIPlugin;
import org.eclipse.ptp.internal.rm.lml.monitor.ui.messages.Messages;
import org.eclipse.ptp.rm.lml.monitor.core.IMonitorControl;
import org.eclipse.ptp.rm.lml.monitor.core.MonitorControlManager;
import org.eclipse.ui.handlers.HandlerUtil;

public class StartMonitorHandler extends AbstractHandler implements IHandler, ISelectionChangedListener {
	private IMonitorControl fMonitor;

	public StartMonitorHandler() {
		MonitorControlManager.getInstance().addSelectionChangedListener(this);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.jface.viewers.ISelectionChangedListener#selectionChanged(org.eclipse.jface.viewers.SelectionChangedEvent)
	 */
	@Override
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
		fireHandlerChanged(new HandlerEvent(StartMonitorHandler.this, true, false));
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

	@Override
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
			try {
				HandlerUtil.getActiveWorkbenchWindow(event).run(true, true, new IRunnableWithProgress() {

					@Override
					public void run(IProgressMonitor progress) throws InvocationTargetException, InterruptedException {
						SubMonitor subMon = SubMonitor.convert(progress, monitors.size());
						try {
							for (IMonitorControl monitor : monitors) {
								try {
									if (!monitor.isActive()) {
										monitor.start(subMon.newChild(1));
									}
								} catch (CoreException e) {
									throw new InvocationTargetException(e);
								}
							}
						} finally {
							if (progress != null) {
								progress.done();
							}
						}
					}
				});
			} catch (InvocationTargetException e) {
				ErrorDialog.openError(HandlerUtil.getActiveShell(event), Messages.StartMonitorHandler_Start_Monitor,
						Messages.StartMonitorHandler_Unable_to_start_monitor,
						new Status(IStatus.WARNING, LMLMonitorUIPlugin.getUniqueIdentifier(), e.getTargetException()
								.getLocalizedMessage(), e.getTargetException()));
			} catch (InterruptedException e) {
				// Ignore
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
		return (fMonitor != null) ? !fMonitor.isActive() : false;
	}
}
