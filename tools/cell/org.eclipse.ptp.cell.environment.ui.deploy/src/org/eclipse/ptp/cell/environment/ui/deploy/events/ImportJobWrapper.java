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
import org.eclipse.ptp.cell.environment.ui.deploy.wizard.CellImportResourcesPage;
import org.eclipse.ptp.remotetools.core.IRemoteCopyTools;
import org.eclipse.ptp.remotetools.core.IRemoteFileTools;
import org.eclipse.ptp.remotetools.environment.control.ITargetControl;
import org.eclipse.ptp.remotetools.exception.CancelException;
import org.eclipse.ptp.remotetools.exception.RemoteConnectionException;
import org.eclipse.ptp.remotetools.exception.RemoteOperationException;


public class ImportJobWrapper implements IRunnableWithProgress {

	private ITargetControl cellControl;
	private String[][] resources;
	private String localDir;
	private CellImportResourcesPage importPage;
	private boolean copyEntireDir = false;
	
	public void init(ITargetControl control, String[][] resourcesToImport, String dir, CellImportResourcesPage page){
		Debug.POLICY.pass(Debug.DEBUG_JOBS, control.toString(), resourcesToImport.length, dir, page.getName());

		this.cellControl = control;
		this.resources = resourcesToImport;
		this.localDir = dir;
		this.importPage = page;
	}
	
	public void run(final IProgressMonitor monitor) throws InvocationTargetException, InterruptedException {
		Debug.POLICY.enter(Debug.DEBUG_JOBS);

		monitor.beginTask(Messages.ImportJobWrapper_0, resources.length);

		if(monitor.isCanceled())
			throw new InterruptedException(Messages.ImportJobWrapper_1);
		
		AbstractCellTargetJob importJob = new AbstractCellTargetJob(){
			public void run() {
				Debug.POLICY.enter(Debug.DEBUG_JOBS);
				try{
					if(monitor.isCanceled())
						throw new InterruptedException(Messages.ImportJobWrapper_2);
					
					String[] currentItem;
					IRemoteFileTools fileTools = executionManager.getRemoteFileTools();
					
					monitor.setTaskName(Messages.ImportJobWrapper_3);
					
					String localPath = localDir;
					
					for(int i = 0; i < resources.length; ++i){
						if(monitor.isCanceled())
							throw new InterruptedException(Messages.ImportJobWrapper_4);		
											
						currentItem = resources[i];
						localPath = localDir;
						IRemoteCopyTools copyTools = fileTools.getRemoteCopyTools();
						
						if(currentItem[1].equals("Directory")){ //$NON-NLS-1$
							Debug.POLICY.pass(Debug.DEBUG_JOBS, "Directory: {0}", currentItem[0].toString()); //$NON-NLS-1$
							monitor.subTask(Messages.ImportJobWrapper_5 + currentItem[0]);
							
							if(copyEntireDir){
								String temp = getDirectoryName(currentItem[0]);
								if(temp != null)
									localPath = localDir + '/' + temp;
							}
							
							copyTools.downloadDirToDir(currentItem[0], localPath, true);
						}
						else if(currentItem[1].equals("File")){ //$NON-NLS-1$
							Debug.POLICY.pass(Debug.DEBUG_JOBS, "File: {0}", currentItem[0].toString()); //$NON-NLS-1$
							monitor.subTask(Messages.ImportJobWrapper_6 + currentItem[0]);
							copyTools.downloadFileToDir(currentItem[0], localDir);
						}
						else {
							continue;
						}						
						monitor.worked(1);
					}
				} catch (RemoteConnectionException e) {
					Debug.POLICY.error(Debug.DEBUG_JOBS, e);
					exception = e;
					errorMessage = Messages.ImportJobWrapper_7;
					hadError = true;
//				} catch (RemoteExecutionException e) {
//					Debug.POLICY.error(Debug.DEBUG_JOBS, e);
//					exception = e;
//					errorMessage = Messages.ImportJobWrapper_8;
//					hadError = true;
				} catch (RemoteOperationException e) {
					Debug.POLICY.error(Debug.DEBUG_JOBS, e);
					exception = e;
					errorMessage = Messages.ImportJobWrapper_8;
					hadError = true;
				} catch (CancelException e) {
					Debug.POLICY.error(Debug.DEBUG_JOBS, e);
					exception = e;
					errorMessage = Messages.ImportJobWrapper_9;
					hadError = true;
				} catch (InterruptedException e) {
					Debug.POLICY.error(Debug.DEBUG_JOBS, e);
					exception = e;
					hadError = true;
				}
				Debug.POLICY.enter(Debug.DEBUG_JOBS);
			}
		};
		
		try {
			cellControl.startJob(importJob);
			importJob.waitFor(monitor, Messages.ImportJobWrapper_10);
			if(importJob.didHaveError()){
				Exception exception = importJob.getException();
				if(exception instanceof InterruptedException){
					throw (InterruptedException)exception;
				} else if(exception instanceof InvocationTargetException){
					throw (InvocationTargetException)exception;
				} else {
					throw new InvocationTargetException(importJob.getException(), importJob.getErrorMessage());
				}
			}
		} catch (CoreException e) {
			Debug.POLICY.error(Debug.DEBUG_JOBS, e);
			throw new InvocationTargetException(e, 
					Messages.ImportJobWrapper_11);
		}
		finally{
			monitor.done();
		}
		Debug.POLICY.enter(Debug.DEBUG_JOBS);
	}
	
	private String getDirectoryName(String path){
		for(int i = path.length() - 1; i >= 0; --i){
			if(path.charAt(i) == '/'){
				return path.substring(i + 1, path.length());
			}
		}
		return null;
	}
	
	public void setCopyEntireDir(boolean val) { 
		Debug.POLICY.pass(Debug.DEBUG_JOBS, val);
		copyEntireDir = val; 
	}
}
