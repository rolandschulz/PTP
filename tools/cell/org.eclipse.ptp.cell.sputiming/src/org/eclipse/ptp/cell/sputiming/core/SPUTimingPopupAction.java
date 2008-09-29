/*******************************************************************************
 * Copyright (c) 2007 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.ptp.cell.sputiming.core;

import org.eclipse.cdt.core.model.ICElement;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.ptp.cell.sputiming.debug.Debug;
import org.eclipse.ui.IActionDelegate;
import org.eclipse.ui.IObjectActionDelegate;
import org.eclipse.ui.IWorkbenchPart;


public class SPUTimingPopupAction implements IObjectActionDelegate {

	IFile selectedfile;

	/**
	 * Constructor for Action1.
	 */
	public SPUTimingPopupAction() {
		super();
	}

	/**
	 * @see IObjectActionDelegate#setActivePart(IAction, IWorkbenchPart)
	 */
	public void setActivePart(IAction action, IWorkbenchPart targetPart) {
	}

	/**
	 * @see IActionDelegate#run(IAction)
	 */
	public void run(IAction action) {
		Debug.read();
		Job job = new Job(Messages.SPUTimingPopupAction_Popup_Run_JobLabel) {

			protected IStatus run(IProgressMonitor monitor) {
				try {
					Debug.POLICY.trace(Debug.DEBUG_POPUP_ACTION, "Start popup action delegate."); //$NON-NLS-1$
					PopupActionDelegate exec = new PopupActionDelegate(selectedfile);
					exec.execute(monitor);
					return Status.OK_STATUS;
				} catch (CoreException e) {
					/*
					 * Return the status of the core exception. The Job Manager will log and display and error message.
					 * Core exceptions are expected conditions that to terminate sputiming.
					 */ 
					Debug.POLICY.error(Debug.DEBUG_POPUP_ACTION, "Failed to executed delegate: {0}", e.getMessage()); //$NON-NLS-1$
					return e.getStatus();
//				} catch (RuntimeException e) {
//					/*
//					 * Just trace the error, but let the Job Manager handle the exception, log and display.
//					 */
//					Debug.POLICY.error(Debug.DEBUG_POPUP_ACTION, "Internal failure on execution of delegate: {0}", e.getMessage()); //$NON-NLS-1$
//					throw e;
				} finally {
					Debug.POLICY.trace(Debug.DEBUG_POPUP_ACTION, "Finished popup action delegate."); //$NON-NLS-1$
				}
			}
		};
		job.setUser(true);
		job.setPriority(Job.LONG);
		job.schedule();
	}

	/**
	 * @see IActionDelegate#selectionChanged(IAction, ISelection)
	 */
	public void selectionChanged(IAction action, ISelection selection) {
		if (selection instanceof StructuredSelection) {
			selectedfile = (IFile) (((ICElement) ((StructuredSelection) selection)
					.getFirstElement()).getResource());
			Debug.POLICY.trace(Debug.DEBUG_POPUP_ACTION, "New resource selected: ''{0}''", selectedfile.toString()); //$NON-NLS-1$
		}		
	}

}
