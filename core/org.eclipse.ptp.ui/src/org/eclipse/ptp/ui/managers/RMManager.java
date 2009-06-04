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
import org.eclipse.ptp.core.PTPCorePlugin;
import org.eclipse.ptp.core.elements.IPUniverse;
import org.eclipse.ptp.core.elements.IResourceManager;
import org.eclipse.ptp.ui.IRMSelectionListener;

public class RMManager {
	protected IResourceManager selectedRM = null;
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
	 * Fire a RM selection event
	 * 
	 * @param rm selected resource manager
	 */
	public void fireRMSelectedEvent(final IResourceManager rm) {
		selectedRM = rm;
		for (Object listener: rmSelectionListeners.getListeners()) {
			final IRMSelectionListener rmListener = (IRMSelectionListener)listener;
			SafeRunner.run(new SafeRunnable() {
				public void run() {
					rmListener.resourceManagerSelected(rm);
				}
			});
		}
	}

	/** 
	 * Get all resource managers
	 * 
	 * @return an array containing the resource managers
	 */
	public IResourceManager[] getResourceManagers() {
		IPUniverse universe = PTPCorePlugin.getDefault().getUniverse();
		if (universe == null) {
			return new IResourceManager[0];
		}
		return universe.getResourceManagers();
	}
	
	/**
	 * Get the default resource manager.
	 * 
	 * @return default resource manager, or null if none are selected
	 */
	public IResourceManager getSelected() {
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
		fireRMSelectedEvent(null);
		rmSelectionListeners.clear();
	}
	
	/**
	 * Persist selected RM
	 * 
	 * TODO: implement
	 */
	private void saveSelectedRM() {
	}
	
	/**
	 * Restore persisted state
	 * 
	 * TODO: implement
	 */
	private void restoreSelectedRM() {
		if (!loaded) {
			loaded = true;
		}
	}
}
