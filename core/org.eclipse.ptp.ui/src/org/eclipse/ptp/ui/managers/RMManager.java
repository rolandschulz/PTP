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
import org.eclipse.ptp.core.PTPCorePlugin;
import org.eclipse.ptp.core.elements.IPUniverse;
import org.eclipse.ptp.core.elements.IPResourceManager;
import org.eclipse.ptp.internal.ui.RMSelectionPersistence;
import org.eclipse.ptp.ui.IRMSelectionListener;

public class RMManager {
	protected IPResourceManager selectedRM = null;
	protected ListenerList rmSelectionListeners = new ListenerList();
	protected boolean loaded = false;

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
	 * @param rm selected resource manager
	 */
	public void fireSetDefaultRMEvent(final IPResourceManager rm) {
		selectedRM = rm;
		for (Object listener: rmSelectionListeners.getListeners()) {
			final IRMSelectionListener rmListener = (IRMSelectionListener)listener;
			SafeRunner.run(new SafeRunnable() {
				public void run() {
					rmListener.setDefault(rm);
				}
			});
		}
	}
	
	/**
	 * Fire an event when the selection in the resource manager view changes
	 * 
	 * @param selection new selection
	 */
	public void fireSelectedEvent(final ISelection selection) {
		for (Object listener: rmSelectionListeners.getListeners()) {
			final IRMSelectionListener rmListener = (IRMSelectionListener)listener;
			SafeRunner.run(new SafeRunnable() {
				public void run() {
					rmListener.selectionChanged(selection);
				}
			});
		}
	}

	/** 
	 * Get all resource managers
	 * 
	 * @return an array containing the resource managers
	 */
	public IPResourceManager[] getResourceManagers() {
		IPUniverse universe = PTPCorePlugin.getDefault().getUniverse();
		if (universe == null) {
			return new IPResourceManager[0];
		}
		return universe.getResourceManagers();
	}
	
	/**
	 * Get the default resource manager.
	 * 
	 * @return default resource manager, or null if none are selected
	 */
	public IPResourceManager getSelected() {
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
		store.saveDefaultRM(selectedRM);
	}
	
	/**
	 * Restore persisted state
	 */
	private void restoreSelectedRM() {
		if ((!loaded) && (selectedRM == null)) {
			RMSelectionPersistence store = new RMSelectionPersistence();
			selectedRM = store.getDefaultRM();
			if (selectedRM != null) {
				fireSetDefaultRMEvent(selectedRM);
			}
			loaded = true;
		}
	}
}
