/**
 * Copyright (c) 2009 IBM Corporation.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - Initial Implementation
 *
 */
package org.eclipse.ptp.ui.managers;

import org.eclipse.core.runtime.ListenerList;
import org.eclipse.core.runtime.SafeRunner;
import org.eclipse.jface.util.SafeRunnable;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.ptp.internal.ui.RMSelectionPersistence;
import org.eclipse.ptp.ui.IRMSelectionListener;

public class RMManager {
	private static RMManager fInstance;

	protected String selectedRM = null;
	protected ListenerList rmSelectionListeners = new ListenerList();
	protected boolean loaded = false;

	private RMManager() {
		fInstance = this;
	}

	/**
	 * @since 5.0
	 */
	public static RMManager getInstance() {
		if (fInstance == null) {
			fInstance = new RMManager();
		}
		return fInstance;
	}

	/**
	 * Add listener
	 * 
	 * @param listener
	 */
	public void addRMSelectionListener(IRMSelectionListener listener) {
		rmSelectionListeners.add(listener);
	}

	/**
	 * Fire an event when the default resource manager is set.
	 * 
	 * @param rmId
	 *            selected resource manager ID
	 * @since 5.0
	 */
	public void fireSetDefaultRMEvent(final String rmId) {
		selectedRM = rmId;
		for (Object listener : rmSelectionListeners.getListeners()) {
			final IRMSelectionListener rmListener = (IRMSelectionListener) listener;
			SafeRunner.run(new SafeRunnable() {
				public void run() {
					rmListener.setDefault(rmId);
				}
			});
		}
	}

	/**
	 * Fire an event when the selection in the resource manager view changes
	 * 
	 * @param selection
	 *            new selection
	 */
	public void fireSelectedEvent(final ISelection selection) {
		for (Object listener : rmSelectionListeners.getListeners()) {
			final IRMSelectionListener rmListener = (IRMSelectionListener) listener;
			SafeRunner.run(new SafeRunnable() {
				public void run() {
					rmListener.selectionChanged(selection);
				}
			});
		}
	}

	/**
	 * Get the default resource manager ID.
	 * 
	 * @return default resource manager ID, or null if none are selected
	 * @since 5.0
	 */
	public String getSelected() {
		restoreSelectedRM();
		return selectedRM;
	}

	/**
	 * Remove listener
	 * 
	 * @param listener
	 */
	public void removeRMSelectionListener(IRMSelectionListener listener) {
		rmSelectionListeners.remove(listener);
	}

	/**
	 * Shut down the RM manager
	 */
	public void shutdown() {
		saveSelectedRM();
		fireSetDefaultRMEvent(null);
		rmSelectionListeners.clear();
	}

	/**
	 * Persist selected RM
	 */
	private void saveSelectedRM() {
		RMSelectionPersistence store = new RMSelectionPersistence();
		store.saveDefaultRMID(selectedRM);
	}

	/**
	 * Restore persisted state
	 */
	private void restoreSelectedRM() {
		if ((!loaded) && (selectedRM == null)) {
			RMSelectionPersistence store = new RMSelectionPersistence();
			selectedRM = store.getDefaultRMID();
			if (selectedRM != null) {
				fireSetDefaultRMEvent(selectedRM);
			}
			loaded = true;
		}
	}
}
