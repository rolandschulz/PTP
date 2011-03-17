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
 * Modified by:
 * 		Claudia Konbloch, Forschungszentrum Juelich GmbH
 */

package org.eclipse.ptp.rm.lml.ui.managers;

import org.eclipse.core.runtime.ListenerList;
import org.eclipse.core.runtime.SafeRunner;
import org.eclipse.jface.util.SafeRunnable;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.ptp.rm.lml.internal.ui.LguiSelectionPersistence;
import org.eclipse.ptp.rm.lml.ui.ILguiSelectionListener;

public class LguiManager {
	protected String selectedLgui = null;
	protected ListenerList lguiSelectionListeners = new ListenerList();
	protected boolean loaded = false;

	/**
	 * Add listener
	 * 
	 * @param listener
	 */
	public void addLguiSelectionListener(ILguiSelectionListener listener) {
		lguiSelectionListeners.add(listener);
	}

	/**
	 * Fire an event when the default resource manager is set.
	 * 
	 * @param rmId
	 *            selected resource manager ID
	 * @since 5.0
	 */
	public void fireSetDefaultLguiEvent(final String lguiId) {
		selectedLgui = lguiId;
		for (Object listener : lguiSelectionListeners.getListeners()) {
			final ILguiSelectionListener lguiListener = (ILguiSelectionListener) listener;
			SafeRunner.run(new SafeRunnable() {
				public void run() {
					lguiListener.setDefault(lguiId);
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
		for (Object listener : lguiSelectionListeners.getListeners()) {
			final ILguiSelectionListener lguiListener = (ILguiSelectionListener) listener;
			SafeRunner.run(new SafeRunnable() {
				public void run() {
					lguiListener.selectionChanged(selection);
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
		restoreSelectedLgui();
		return selectedLgui;
	}

	/**
	 * Remove listener
	 * 
	 * @param listener
	 */
	public void removeLguiSelectionListener(ILguiSelectionListener listener) {
		lguiSelectionListeners.remove(listener);
	}

	/**
	 * Shut down the RM manager
	 */
	public void shutdown() {
		saveSelectedLgui();
		fireSetDefaultLguiEvent(null);
		lguiSelectionListeners.clear();
	}

	/**
	 * Persist selected RM
	 */
	private void saveSelectedLgui() {
		LguiSelectionPersistence store = new LguiSelectionPersistence();
		store.saveDefaultLguiID(selectedLgui);
	}

	/**
	 * Restore persisted state
	 */
	private void restoreSelectedLgui() {
		if ((!loaded) && (selectedLgui == null)) {
			LguiSelectionPersistence store = new LguiSelectionPersistence();
			selectedLgui = store.getDefaultLguiID();
			if (selectedLgui != null) {
				fireSetDefaultLguiEvent(selectedLgui);
			}
			loaded = true;
		}
	}
}
