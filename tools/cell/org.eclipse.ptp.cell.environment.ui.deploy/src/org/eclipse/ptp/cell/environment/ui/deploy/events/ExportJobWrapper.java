/**
 * Copyright (c) 2006 IBM Corporation.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - Initial Implementation
 *
 */
package org.eclipse.ptp.cell.environment.ui.deploy.events;

import java.lang.reflect.InvocationTargetException;
import java.util.List;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jface.operation.IRunnableWithProgress;
import org.eclipse.ptp.cell.environment.ui.deploy.debug.Debug;
import org.eclipse.ptp.cell.environment.ui.deploy.wizard.CellExportResourcesPage;
import org.eclipse.ptp.remotetools.environment.control.ITargetControl;


public class ExportJobWrapper implements IRunnableWithProgress {
	


	//Instance variables
	private ITargetControl cellControl;
	List resourcesToCopy;
	String remoteDir;
	CellExportResourcesPage exportPage;
	
	boolean autoOverwrite = false;
	boolean noToAllOverwrite = false;
	boolean createDirStructure = false;
	private int size;

	public void init(ITargetControl control, List resources, String dir, CellExportResourcesPage page, int s) {
		Debug.POLICY.pass(Debug.DEBUG_JOBS, control.toString(), resources.size(), dir, page.getName(), s);
		this.cellControl = control;
		this.resourcesToCopy = resources;
		this.remoteDir = dir;
		this.exportPage = page;
		this.size = s;		
	}
	
	public void run(final IProgressMonitor monitor) throws InvocationTargetException, InterruptedException {
		Debug.POLICY.enter(Debug.DEBUG_JOBS);
		
		monitor.beginTask(Messages.ExportJobWrapper_0, size);

		if(monitor.isCanceled())
			throw new InterruptedException(Messages.ExportJobWrapper_1);

		AbstractCellTargetJob copyJob = new ExportJob(this, monitor);
		
		try {
			cellControl.startJob(copyJob);
			copyJob.waitFor(monitor, Messages.ExportJobWrapper_39);
			if(copyJob.didHaveError()){
				Exception exception = copyJob.getException();
				if(exception instanceof InterruptedException){
					throw (InterruptedException)exception;
				} else if(exception instanceof InvocationTargetException){
					throw (InvocationTargetException)exception;
				} else {
					throw new InvocationTargetException(copyJob.getException(), copyJob.getErrorMessage());
				}
			}
		} catch (CoreException e) {
			Debug.POLICY.error(Debug.DEBUG_JOBS, e);
			throw new InvocationTargetException(e, 
					Messages.ExportJobWrapper_40);
		}
		finally{
			monitor.done();
		}		
		Debug.POLICY.exit(Debug.DEBUG_JOBS);
	}
	
	public void setAutoOverwrite(boolean val){
		Debug.POLICY.pass(Debug.DEBUG_JOBS, val);
		autoOverwrite = val;
	}
	
	public void setCreateDirStructure(boolean val){
		Debug.POLICY.pass(Debug.DEBUG_JOBS, val);
		createDirStructure = val;
	}
}
