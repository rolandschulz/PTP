/*******************************************************************************
 * Copyright (c) 2008, 2010 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 * IBM - Initial API and implementation
 *******************************************************************************/
package org.eclipse.ptp.internal.rdt.core.miners;

import org.eclipse.core.runtime.IProgressMonitor;

/**
 * @author crecoskie
 *
 */
public class StdoutProgressMonitor implements IProgressMonitor {

	private String fTaskName, fSubTaskName;
	private int fTotalWork;
	private int fCurrentWorked;
	private boolean fIsCanceled;
	
	/* (non-Javadoc)
	 * @see org.eclipse.core.runtime.IProgressMonitor#beginTask(java.lang.String, int)
	 */
	public void beginTask(String name, int totalWork) {
		fTaskName = name;
		fTotalWork = totalWork;
		System.out.println("Starting task " + name); //$NON-NLS-1$

	}

	/* (non-Javadoc)
	 * @see org.eclipse.core.runtime.IProgressMonitor#done()
	 */
	public void done() {
		System.out.println("Done task " + fTaskName); //$NON-NLS-1$

	}

	/* (non-Javadoc)
	 * @see org.eclipse.core.runtime.IProgressMonitor#internalWorked(double)
	 */
	public void internalWorked(double work) {
		fCurrentWorked += work;
		printProgress();

	}

	/* (non-Javadoc)
	 * @see org.eclipse.core.runtime.IProgressMonitor#isCanceled()
	 */
	public boolean isCanceled() {
		return fIsCanceled;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.core.runtime.IProgressMonitor#setCanceled(boolean)
	 */
	public void setCanceled(boolean value) {
		fIsCanceled = value;
		if (value == true) {
			System.out.println("Task " + fTaskName + " has been cancelled"); //$NON-NLS-1$ //$NON-NLS-2$
			System.out.flush();
		}

	}

	/* (non-Javadoc)
	 * @see org.eclipse.core.runtime.IProgressMonitor#setTaskName(java.lang.String)
	 */
	public void setTaskName(String name) {
		fTaskName = name;

	}

	/* (non-Javadoc)
	 * @see org.eclipse.core.runtime.IProgressMonitor#subTask(java.lang.String)
	 */
	public void subTask(String name) {
		fSubTaskName = name;
		printProgress();

	}

	/* (non-Javadoc)
	 * @see org.eclipse.core.runtime.IProgressMonitor#worked(int)
	 */
	public void worked(int work) {
		internalWorked(work);

	}
	
	private void printProgress() {
		System.out.println("Working... Task: " + fTaskName + " SubTask: "  + fSubTaskName + " Progress: " + fCurrentWorked + " of " + fTotalWork); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$
		System.out.flush();
	}

}
