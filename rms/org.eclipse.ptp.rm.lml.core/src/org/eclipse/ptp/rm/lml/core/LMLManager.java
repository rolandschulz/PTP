/**
 * Copyright (c) 2011 Forschungszentrum Juelich GmbH
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 * 		Claudia Knobloch, FZ Juelich
 */
package org.eclipse.ptp.rm.lml.core;

import java.io.InputStream;
import java.io.OutputStream;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.ListenerList;
import org.eclipse.ptp.rm.lml.core.events.ILguiAddedEvent;
import org.eclipse.ptp.rm.lml.core.events.ILguiRemovedEvent;
import org.eclipse.ptp.rm.lml.core.events.IMarkObjectEvent;
import org.eclipse.ptp.rm.lml.core.events.ISelectObjectEvent;
import org.eclipse.ptp.rm.lml.core.events.ITableFilterEvent;
import org.eclipse.ptp.rm.lml.core.events.ITableSortedEvent;
import org.eclipse.ptp.rm.lml.core.events.IUnmarkObjectEvent;
import org.eclipse.ptp.rm.lml.core.events.IUnselectedObjectEvent;
import org.eclipse.ptp.rm.lml.core.events.IViewUpdateEvent;
import org.eclipse.ptp.rm.lml.core.listeners.ILMLListener;
import org.eclipse.ptp.rm.lml.core.model.ILguiItem;
import org.eclipse.ptp.rm.lml.core.model.IPattern;
import org.eclipse.ptp.rm.lml.internal.core.elements.RequestType;
import org.eclipse.ptp.rm.lml.internal.core.events.LguiAddedEvent;
import org.eclipse.ptp.rm.lml.internal.core.events.LguiRemovedEvent;
import org.eclipse.ptp.rm.lml.internal.core.events.MarkObjectEvent;
import org.eclipse.ptp.rm.lml.internal.core.events.SelectObjectEvent;
import org.eclipse.ptp.rm.lml.internal.core.events.TableFilterEvent;
import org.eclipse.ptp.rm.lml.internal.core.events.TableSortedEvent;
import org.eclipse.ptp.rm.lml.internal.core.events.UnmarkObjectEvent;
import org.eclipse.ptp.rm.lml.internal.core.events.UnselectObjectEvent;
import org.eclipse.ptp.rm.lml.internal.core.events.ViewUpdateEvent;
import org.eclipse.ptp.rm.lml.internal.core.model.LguiItem;

/**
 * 
 */
public class LMLManager {

	public static LMLManager getInstance() {
		if (manager == null) {
			manager = new LMLManager();
		}
		return manager;
	}

	/*
	 * Map of all ILguiItems
	 * 
	 * For every created Resource Manager instance there is an entry in this map; as long as the Resource Manager instance is not
	 * removed an associates entry keeps in this map
	 */
	protected final Map<String, ILguiItem> LGUIS = new HashMap<String, ILguiItem>();

	/*
	 * The current considered ILguiItem
	 */
	private ILguiItem fLguiItem = null;

	/*
	 * A list of all listeners on the ILguiItem
	 */
	private final ListenerList viewListeners = new ListenerList();

	/*
	 * An instance of this class.
	 */
	private static LMLManager manager;

	private boolean isDisplayed = false;

	private LMLManager() {
		manager = this;
	}

	public void addListener(ILMLListener listener, String view) {
		viewListeners.add(listener);
	}

	public void addUserJob(String name, String jobId, JobStatusData status) {
		final ILguiItem item = LGUIS.get(name);
		if (item != null) {
			item.addUserJob(jobId, status, true);
		}
		if (item == fLguiItem) {
			fireUpdatedLgui();
		}
	}

	public void closeLgui(String name) {
		ILguiItem item = null;
		synchronized (LGUIS) {
			item = LGUIS.get(name);
			if (item != null) {
				if (fLguiItem == item) {
					selectLgui(null);
				}
				LGUIS.remove(name);
			}
		}

	}

	public void filterLgui(String gid, List<IPattern> filterValues) {
		fireFilterLgui(gid, filterValues);
	}

	public String getCurrentLayout(String name) {
		ILguiItem item = null;
		synchronized (LGUIS) {
			item = LGUIS.get(name);
		}
		if (item != null) {
			if (fLguiItem != null && fLguiItem == item) {
				fireRemovedLgui(item);
				fLguiItem = null;
			}
			final String string = item.saveCurrentLayout();
			return string;
		}
		return null;
	}

	public ILguiItem getSelectedLguiItem() {
		return fLguiItem;
	}

	public JobStatusData getUserJob(String name, String jobId) {
		final ILguiItem item = LGUIS.get(name);
		if (item != null) {
			return item.getUserJob(jobId);
		}
		return null;
	}

	public JobStatusData[] getUserJobs(String name) {
		ILguiItem item = null;
		synchronized (LGUIS) {
			item = LGUIS.get(name);
			if (item != null) {
				return item.getUserJobs();
			}
		}
		return null;
	}

	public void markObject(String oid) {
		fireMarkObject(oid);
	}

	/**
	 * This method is called by LMLResourceManagerMonitor. A ResourceManager was started.
	 * 
	 * @param name
	 *            Name of the ResourceManager
	 * @param configuration
	 *            Configuration information for session
	 * @param layout
	 *            Layout from an earlier Eclipse session
	 * @param jobs
	 *            Array of earlier started jobs
	 */
	public void openLgui(String name, String username, RequestType request, String layout, JobStatusData[] jobs) {
		synchronized (LGUIS) {
			ILguiItem item = LGUIS.get(name);
			if (item == null) {
				item = new LguiItem(name, username);
				LGUIS.put(name, item);
			}
			fLguiItem = item;
		}

		if (layout != null) {
			fLguiItem.reloadLastLayout(layout);
		}
		fLguiItem.setRequest(request);
		restoreJobStatusData(fLguiItem, jobs);

		if (!fLguiItem.isEmpty()) {
			fireNewLgui();
		}
	}

	public void removeListener(ILMLListener listener) {
		viewListeners.remove(listener);
	}

	public void removeUserJob(String name, String jobId) {
		final ILguiItem lguiItem = LGUIS.get(name);
		if (lguiItem != null) {
			lguiItem.removeUserJob(jobId);
		}
		if (lguiItem == fLguiItem) {
			fireUpdatedLgui();
		}
	}

	public void selectLgui(String name) {
		if (name != null && fLguiItem != null && fLguiItem.getName().equals(name)) {
			return;
		}
		if (fLguiItem != null) {
			fLguiItem = null;
			fireRemovedLgui(null);
		}
		if (name != null) {
			final ILguiItem item = LGUIS.get(name);
			if (item != null) {
				fLguiItem = item;
				fireNewLgui();
				return;
			}

		}

	}

	public void selectObject(String oid) {
		fireChangeSelectedObject(oid);
	}

	public void sortLgui() {
		fireSortedLgui();
	}

	public void unmarkObject(String oid) {
		fireUnmarkObject(oid);
	}

	public void unselectObject(String oid) {
		fireUnselectObject(oid);
	}

	public void update(String name, InputStream input, OutputStream output) throws CoreException {
		ILguiItem lguiItem = null;
		synchronized (LGUIS) {
			lguiItem = LGUIS.get(name);
		}
		if (lguiItem != null) {
			lguiItem.getCurrentLayout(output);
			lguiItem.update(input);

			if (fLguiItem == lguiItem) {
				if (!isDisplayed) {
					fireNewLgui();
				} else {
					fireUpdatedLgui();
				}
			}
		}
	}

	public void updateUserJob(String name, String jobId, String status, String detail) {
		final ILguiItem lguiItem = LGUIS.get(name);
		if (lguiItem != null) {
			lguiItem.updateUserJob(jobId, status, detail);
		}
		if (lguiItem == fLguiItem) {
			fireUpdatedLgui();
		}
	}

	private void fireChangeSelectedObject(String oid) {
		final ISelectObjectEvent event = new SelectObjectEvent(oid);
		for (final Object listener : viewListeners.getListeners()) {
			((ILMLListener) listener).handleEvent(event);
		}
	}

	private void fireFilterLgui(String gid, List<IPattern> filterValues) {
		final ITableFilterEvent event = new TableFilterEvent(gid, filterValues);
		for (final Object listener : viewListeners.getListeners()) {
			((ILMLListener) listener).handleEvent(event);
		}
	}

	private void fireMarkObject(String oid) {
		final IMarkObjectEvent event = new MarkObjectEvent(oid);
		for (final Object listener : viewListeners.getListeners()) {
			((ILMLListener) listener).handleEvent(event);
		}
	}

	/**
	 * Method is called when a new ILguiItem was generated.
	 */
	private void fireNewLgui() {
		final ILguiAddedEvent event = new LguiAddedEvent();
		for (final Object listener : viewListeners.getListeners()) {
			((ILMLListener) listener).handleEvent(event);
		}
		isDisplayed = true;
	}

	private void fireRemovedLgui(ILguiItem title) {
		final ILguiRemovedEvent event = new LguiRemovedEvent();
		for (final Object listener : viewListeners.getListeners()) {
			((ILMLListener) listener).handleEvent(event);
		}
		isDisplayed = false;
	}

	/**
	 * Method is called when an ILguiItem was sorted.
	 */
	private void fireSortedLgui() {
		final ITableSortedEvent event = new TableSortedEvent(this, fLguiItem);
		for (final Object listener : viewListeners.getListeners()) {
			((ILMLListener) listener).handleEvent(event);
		}
	}

	private void fireUnmarkObject(String oid) {
		final IUnmarkObjectEvent event = new UnmarkObjectEvent(oid);
		for (final Object listener : viewListeners.getListeners()) {
			((ILMLListener) listener).handleEvent(event);
		}
	}

	private void fireUnselectObject(String oid) {
		final IUnselectedObjectEvent event = new UnselectObjectEvent(oid);
		for (final Object listener : viewListeners.getListeners()) {
			((ILMLListener) listener).handleEvent(event);
		}
	}

	private void fireUpdatedLgui() {
		final IViewUpdateEvent event = new ViewUpdateEvent();
		for (final Object listener : viewListeners.getListeners()) {
			((ILMLListener) listener).handleEvent(event);
		}
	}

	/**
	 * 
	 * @param map
	 * @param memento
	 *            may be <code>null</code>
	 */
	private void restoreJobStatusData(ILguiItem item, JobStatusData[] jobs) {
		if (jobs != null && jobs.length > 0) {
			for (final JobStatusData status : jobs) {
				item.addUserJob(status.getJobId(), status, false);
			}
		}
	}
}
