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
package org.eclipse.ptp.cell.environment.ui.deploy.events;

import java.lang.reflect.InvocationTargetException;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jface.operation.IRunnableWithProgress;
import org.eclipse.ptp.cell.environment.ui.deploy.debug.Debug;
import org.eclipse.ptp.remotetools.core.IRemoteFileTools;
import org.eclipse.ptp.remotetools.environment.control.ITargetControl;
import org.eclipse.ptp.remotetools.exception.CancelException;
import org.eclipse.ptp.remotetools.exception.RemoteConnectionException;
import org.eclipse.ptp.remotetools.exception.RemoteOperationException;



public class DeleteJobWrapper implements IRunnableWithProgress {

	private ITargetControl cellControl;
	private String path;
	
	public void init(ITargetControl control, String p) {
		Debug.POLICY.pass(Debug.DEBUG_JOBS, control.toString(), p);
		this.cellControl = control;
		this.path = p;
	}
	
	public void run(final IProgressMonitor monitor) throws InvocationTargetException, InterruptedException {
		Debug.POLICY.enter(Debug.DEBUG_JOBS);
		monitor.beginTask(Messages.DeleteJobWrapper_0, 1);
		monitor.subTask(Messages.DeleteJobWrapper_1 + path);
		
		if(monitor.isCanceled())
			throw new InterruptedException(Messages.DeleteJobWrapper_2);

		//create and define the job that will delete the cell resources
		AbstractCellTargetJob deleteJob = new AbstractCellTargetJob(){

			public void run() {
				Debug.POLICY.enter(Debug.DEBUG_JOBS);
				try {
					IRemoteFileTools fileTools = executionManager.getRemoteFileTools();
					
					if(monitor.isCanceled())
						throw new InterruptedException(Messages.DeleteJobWrapper_3);
					
					if((!fileTools.hasFile(path)) && (!fileTools.hasDirectory(path))){
						throw new InvocationTargetException(null, 
							Messages.DeleteJobWrapper_4);
					}
					
					fileTools.removeFile(path);
					monitor.worked(1);

				} catch (RemoteConnectionException e) {
					Debug.POLICY.error(Debug.DEBUG_JOBS, e);
					exception = e;
					errorMessage = Messages.DeleteJobWrapper_5;
					hadError = true;
				} catch (RemoteOperationException e) {
					Debug.POLICY.error(Debug.DEBUG_JOBS, e);
					exception = e;
					errorMessage = Messages.DeleteJobWrapper_6;
					hadError = true;
				} catch (CancelException e) {
					Debug.POLICY.error(Debug.DEBUG_JOBS, e);
					exception = e;
					errorMessage = Messages.DeleteJobWrapper_7;
					hadError = true;
				} catch (InvocationTargetException e) {
					Debug.POLICY.error(Debug.DEBUG_JOBS, e);
					exception = e;
					hadError = true;
				} catch (InterruptedException e) {
					Debug.POLICY.error(Debug.DEBUG_JOBS, e);
					exception = e;
					hadError = true;
				}		
				Debug.POLICY.exit(Debug.DEBUG_JOBS);
			}			
		};

		try {
			cellControl.startJob(deleteJob);
			deleteJob.waitFor(monitor, Messages.DeleteJobWrapper_8);
			if(deleteJob.didHaveError()){
				Exception exception = deleteJob.getException();
				if(exception instanceof InterruptedException){
					throw (InterruptedException)exception;
				} else if(exception instanceof InvocationTargetException){
					throw (InvocationTargetException)exception;
				} else {
					throw new InvocationTargetException(deleteJob.getException(), deleteJob.getErrorMessage());
				}
			}
		} catch (CoreException e) {
			Debug.POLICY.error(Debug.DEBUG_JOBS, e);
			throw new InvocationTargetException(e, 
					Messages.DeleteJobWrapper_9);
		}
		finally{
			monitor.done();
		}
		Debug.POLICY.exit(Debug.DEBUG_JOBS);
	}
}
