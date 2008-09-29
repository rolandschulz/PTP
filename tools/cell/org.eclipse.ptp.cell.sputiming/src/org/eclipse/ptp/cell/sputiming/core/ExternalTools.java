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

import java.text.MessageFormat;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.ISafeRunnable;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.SafeRunner;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.ptp.cell.sputiming.debug.Debug;
import org.eclipse.ptp.cell.sputiming.extension.ISPUTimingObserver;
import org.eclipse.ptp.utils.core.extensionpoints.ExtensionPointEnumeration;

/**
 * @author Richard Maciel
 * 
 */
public class ExternalTools {
	/**
	 * Fetch all classes from plugins that extend the observer extension point,
	 * calling their {@link ISPUTimingObserver#afterFileGeneration(IPath)}
	 * method, passing the file as parameter.
	 * 
	 * @param file
	 */
	static public void callExtensions(final IPath file) {
		ExtensionPointEnumeration epe = new ExtensionPointEnumeration(
				"org.eclipse.ptp.cell.sputiming.observerpoint"); //$NON-NLS-1$

		Debug.read();
		Debug.POLICY.trace(Debug.DEBUG_VISUALIZATION, "Start notifying visualization extension points."); //$NON-NLS-1$
			
		while (epe.hasMoreElements()) {
			final IConfigurationElement ice = (IConfigurationElement) epe.nextElement();

			Debug.POLICY.trace(Debug.DEBUG_VISUALIZATION, "name:{0} class:{1} valid:{2}", ice.getName(), ice.getAttribute("class"), ice.isValid()); //$NON-NLS-1$ //$NON-NLS-2$

			Job job = new Job(MessageFormat.format("Sputiming extension: {0}", ice.getName())) { //$NON-NLS-1$
				@Override
				protected IStatus run(IProgressMonitor monitor) {
			Object beholderWOType = null;
			try {
				beholderWOType = ice.createExecutableExtension("class"); //$NON-NLS-1$
			} catch (CoreException e) {
						Debug.POLICY.error(Debug.DEBUG_VISUALIZATION, "Failed to load class for extension point: {0}; reason: {1}", ice.getName(), e.getMessage()); //$NON-NLS-1$
						return e.getStatus();
			}
			
				Debug.POLICY.trace(Debug.DEBUG_VISUALIZATION, "Start notifying {0}.", ice.getName()); //$NON-NLS-1$
					final ISPUTimingObserver spubeholder = (ISPUTimingObserver) beholderWOType;
					SafeRunner.run(new ISafeRunnable() {
						public void handleException(Throwable exception) {
							Debug.POLICY.error(Debug.DEBUG_VISUALIZATION, "Failed to execute extension point: {0}; reason: {1}",ice.getName(),  exception.getMessage()); //$NON-NLS-1$
						}
						public void run() throws Exception {
				spubeholder.afterFileGeneration(file);
			}
					});
			Debug.POLICY.trace(Debug.DEBUG_VISUALIZATION, "Finished notifying {0}.", ice.getName()); //$NON-NLS-1$
// try {
// Debug.POLICY.trace(Debug.DEBUG_VISUALIZATION, "Start notifying {0}.",
// ice.getName()); //$NON-NLS-1$
// ISPUTimingObserver spubeholder = (ISPUTimingObserver) beholderWOType;
// spubeholder.afterFileGeneration(file);
// } catch (Throwable e) {
// Debug.POLICY.error(Debug.DEBUG_VISUALIZATION, "Failed to execute extension
// point: {0}; reason: {1}",ice.getName(), e.getMessage()); //$NON-NLS-1$
// return new Status(IStatus.ERROR,
// Activator.getDefault().getBundle().getSymbolicName(),
// Messages.ExternalTools_FailedExecuteExtensionPoint, e);
// }
// Debug.POLICY.trace(Debug.DEBUG_VISUALIZATION, "Finished notifying {0}.",
// ice.getName()); //$NON-NLS-1$
					
					return Status.OK_STATUS;
				}				
			};
			job.setSystem(true);
			job.setPriority(Job.LONG);
			job.schedule();
			try {
				job.join();
			} catch (InterruptedException e) {
				// Nothing to do.
			}
		}

		Debug.POLICY.trace(Debug.DEBUG_VISUALIZATION, "Finished notifying visualization extension points."); //$NON-NLS-1$
	}
}
