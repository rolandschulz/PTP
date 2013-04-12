/*******************************************************************************
 * Copyright (c) 2011 Oak Ridge National Laboratory and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    John Eblen - initial implementation
 *******************************************************************************/
package org.eclipse.ptp.rdt.sync.core;

import org.eclipse.core.resources.IWorkspace;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.ptp.internal.rdt.sync.core.RDTSyncCorePlugin;
import org.eclipse.ptp.internal.rdt.sync.core.messages.Messages;
import org.osgi.service.prefs.BackingStoreException;
import org.osgi.service.prefs.Preferences;

public class SyncUtils {

	/**
	 * The node flushing mechanism fails if the workspace is locked. So calling "Node.flush()" is not enough. Instead, spawn a
	 * thread that flushes once the workspace is unlocked.
	 * 
	 * @param prefNode
	 *            node to flush
	 */

	public static void flushNode(final Preferences prefNode) {
		Throwable firstException = null;
		final IWorkspace ws = ResourcesPlugin.getWorkspace();
		// Avoid creating a thread if possible.
		try {
			if (!ws.isTreeLocked()) {
				prefNode.flush();
				return;
			}
		} catch (BackingStoreException e) {
			// Proceed to create thread
			firstException = e;
		} catch (IllegalStateException e) {
			// Can occur if the project has been moved or deleted, so the preference node no longer exists.
			firstException = e;
			return;
		}

		final Throwable currentException = firstException;
		Thread flushThread = new Thread(new Runnable() {
			@Override
			public void run() {
				int sleepCount = 0;
				Throwable lastException = currentException;
				while (true) {
					try {
						Thread.sleep(1000);
						// Give up after 30 sleeps - this should never happen
						sleepCount++;
						if (sleepCount > 30) {
							if (lastException != null) {
								RDTSyncCorePlugin.log(Messages.BuildConfigurationManager_17, lastException);
							} else {
								RDTSyncCorePlugin.log(Messages.BuildConfigurationManager_17);
							}
							break;
						}
						if (!ws.isTreeLocked()) {
							prefNode.flush();
							break;
						}
					} catch (InterruptedException e) {
						lastException = e;
					} catch (BackingStoreException e) {
						// This can happen in the rare case that the lock is locked between the check and the flush.
						lastException = e;
					} catch (IllegalStateException e) {
						// Can occur if the project has been moved or deleted, so the preference node no longer exists.
						return;
					}
				}
			}
		}, "Flush project data thread"); //$NON-NLS-1$
		flushThread.start();
	}
}
