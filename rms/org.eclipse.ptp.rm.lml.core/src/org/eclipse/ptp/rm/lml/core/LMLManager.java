/**
 * Copyright (c) 2011-2014 Forschungszentrum Juelich GmbH
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 * 		Claudia Knobloch, FZ Juelich
 * 		Carsten Karbach, FZ Juelich
 */
package org.eclipse.ptp.rm.lml.core;

import java.io.InputStream;
import java.io.OutputStream;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.ListenerList;
import org.eclipse.ptp.internal.rm.lml.core.events.LguiAddedEvent;
import org.eclipse.ptp.internal.rm.lml.core.events.LguiRemovedEvent;
import org.eclipse.ptp.internal.rm.lml.core.events.MarkObjectEvent;
import org.eclipse.ptp.internal.rm.lml.core.events.SelectObjectEvent;
import org.eclipse.ptp.internal.rm.lml.core.events.TableFilterEvent;
import org.eclipse.ptp.internal.rm.lml.core.events.TableSortedEvent;
import org.eclipse.ptp.internal.rm.lml.core.events.UnmarkObjectEvent;
import org.eclipse.ptp.internal.rm.lml.core.events.UnselectObjectEvent;
import org.eclipse.ptp.internal.rm.lml.core.events.ViewUpdateEvent;
import org.eclipse.ptp.internal.rm.lml.core.model.LguiItem;
import org.eclipse.ptp.rm.lml.core.elements.RequestType;
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

/**
 * 
 * This is the input point for LML data to the system monitoring perspective.
 * It manages a list of ILguiItem instances (in short Lgui in the following),
 * one for each monitoring connection.
 * With this manager, updates on LML data can be triggered. Moreover, jobs
 * started via Eclipse can be added into the job lists. This API is used by
 * the lml.monitor.core.MonitorControl to fill LML data from LML_da into the
 * main LML handling plug-ins. It manages the start-up and clean-up for a
 * monitoring connection and converts the LML data into JAXB-classes and
 * forwards them to the LML displays in the rm.lml.ui plug-in.
 * 
 */
public class LMLManager {

	/**
	 * @return the singleton instance of the LMLManager
	 */
	public static LMLManager getInstance() {
		if (manager == null) {
			manager = new LMLManager();
		}
		return manager;
	}

	/**
	 * Map of all ILguiItems
	 * 
	 * For every created monitoring connection there is an entry in this map.
	 * As long as the monitoring connection is not
	 * removed an associated entry is keept in this map.
	 * 
	 * It maps a unique ID to the ILguiItem instance.
	 */
	protected final Map<String, ILguiItem> LGUIS = new HashMap<String, ILguiItem>();

	/**
	 * The active ILguiItem, which is currently selected by the user.
	 */
	private ILguiItem fLguiItem = null;

	/**
	 * A list of all listeners on the ILguiItem, they can listen for job highlighting, a data update
	 * opening and closing a connection.
	 */
	private final ListenerList viewListeners = new ListenerList();

	/**
	 * An instance of this class. Needed for implementing the singleton pattern.
	 */
	private static LMLManager manager;

	/**
	 * True, if there is an active connection at all, false otherwise.
	 */
	private boolean isDisplayed = false;

	/**
	 * Private constructor causes this class to be only used as singleton.
	 */
	private LMLManager() {
		manager = this;
	}

	/**
	 * The view with the name <code>view</code> registers itself
	 * as listener for LML data updates. It is then notified on
	 * data updates, object marking and table filtering.
	 * 
	 * @param listener
	 *            the listening object
	 * @param view
	 *            the name of the view, which is registering a listener here
	 */
	public void addListener(ILMLListener listener, String view) {
		viewListeners.add(listener);
	}

	/**
	 * This method allows to add a special job to the table list.
	 * A job submitted via PTP can be added into the job lists here.
	 * By using this method changes on the job status can be updated more
	 * quickly instead of waiting for the entire monitoring workflow to
	 * have updated the status of the whole system.
	 * This method must not be called before the corresponding Lgui is opened.
	 * Otherwise, it will not have any effect.
	 * 
	 * @param name
	 *            the hash name identifying an Lgui
	 * @param jobId
	 *            ID for the new added job
	 * @param status
	 *            status data object for the added job, transfers data from the job submission to the monitoring
	 */
	public void addUserJob(String name, String jobId, JobStatusData status) {
		final ILguiItem item = LGUIS.get(name);
		if (item != null) {
			item.addUserJob(jobId, status, true);
		}
		if (item == fLguiItem) {
			fireUpdatedLgui();
		}
	}

	/**
	 * Remove an Lgui from the list of active Lguis.
	 * This method is called, when the user stops the corresponding monitoring connection.
	 * 
	 * @param name
	 *            the hash name identifying the closed Lgui
	 */
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

	/**
	 * This can be used to request a filtering of the table identified
	 * with <code>gid</code>. The table is filtered with the patterns
	 * given in the filterValues list. Each pattern defines a column
	 * and a corresponding range or relation, on which to filter.
	 * 
	 * @param gid
	 *            identifies the table, which needs to be filtered
	 * @param filterValues
	 *            patterns describing how to filter the table
	 */
	public void filterLgui(String gid, List<IPattern> filterValues) {
		fireFilterLgui(gid, filterValues);
	}

	/**
	 * Retrieve the current LML-Layout and simultaneously send event
	 * that the resource manager is closed. This method is called
	 * right before a connection is closed. It returns the current layout
	 * for successive sessions.
	 * 
	 * @param name
	 *            the hash name identifying the Lgui
	 * @return LML-Layout as string containing the current LML-Layout configuration
	 */
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

	/**
	 * @return the currently active monitoring
	 */
	public ILguiItem getSelectedLguiItem() {
		return fLguiItem;
	}

	/**
	 * Retrieve status data stored for a special user job, which was added due to
	 * a job submission via PTP. If no job is found with the given jobId, null
	 * is returned.
	 * 
	 * @param name
	 *            the hash name identifying the Lgui
	 * @param jobId
	 *            ID of the user job
	 * @return status data stored for this job or null, if no job is found
	 */
	public JobStatusData getUserJob(String name, String jobId) {
		final ILguiItem item = LGUIS.get(name);
		if (item != null) {
			return item.getUserJob(jobId);
		}
		return null;
	}

	/**
	 * Retrieve all jobs stored in the Lgui specified by <code>name</code>.
	 * 
	 * @param name
	 *            the hash name identifying the Lgui
	 * @return array of status data objects
	 */
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

	/**
	 * Fire a mark object event for the LML object identified by <code>oid</code>.
	 * Marking in object is usually done by clicking on the object, while selecting
	 * an object means hovering over the object.
	 * 
	 * @param oid
	 *            the ID of an object from an LML file
	 */
	public void markObject(String oid) {
		fireMarkObject(oid);
	}

	/**
	 * This method is called by the monitoring core. A new connection is started.
	 * A new Lgui is added to the list of managed Lguis. Stored user jobs are
	 * placed into the data model. The last layout request is restored.
	 * 
	 * @param name
	 *            the hash name identifying the opened Lgui
	 * @param username
	 *            the username of the current user, who might submit jobs using PTP
	 * @param request
	 *            request typ, which is used to forward options to LML_DA
	 * @param layout
	 *            layout from a previous Eclipse session
	 * @param jobs
	 *            array of earlier started jobs
	 * 
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

	/**
	 * Remove a listener from the list of notified objects.
	 * 
	 * @param listener
	 *            the listener object, which should be removed.
	 */
	public void removeListener(ILMLListener listener) {
		viewListeners.remove(listener);
	}

	/**
	 * Remove a job from the job lists.
	 * 
	 * @param name
	 *            the hash name identifying the Lgui
	 * @param jobId
	 *            job ID for the job, which has to be removed
	 */
	public void removeUserJob(String name, String jobId) {
		final ILguiItem lguiItem = LGUIS.get(name);
		if (lguiItem != null) {
			lguiItem.removeUserJob(jobId);
		}
		if (lguiItem == fLguiItem) {
			fireUpdatedLgui();
		}
	}

	/**
	 * Activate the Lgui with the specified name.
	 * This updates all monitoring views with the data of the new Lgui.
	 * 
	 * @param name
	 *            the hash name identifying the new selected Lgui
	 */
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

	/**
	 * Select the object identified by <code>oid</code>.
	 * This method is called, if an object is hovered over
	 * with the cursor.
	 * 
	 * @param oid
	 *            the LML object ID for the selected object
	 */
	public void selectObject(String oid) {
		fireChangeSelectedObject(oid);
	}

	/**
	 * A table sorting event is fired to all listeners.
	 */
	public void sortLgui() {
		fireSortedLgui();
	}

	/**
	 * Undo the marking of an object.
	 * 
	 * @param oid
	 *            the LML object ID for the previously marked object
	 */
	public void unmarkObject(String oid) {
		fireUnmarkObject(oid);
	}

	/**
	 * Undo the selection of an object. E.g. the mouse has left the
	 * area display the object.
	 * 
	 * @param oid
	 *            the LML object ID for the previously selected object
	 */
	public void unselectObject(String oid) {
		fireUnselectObject(oid);
	}

	/**
	 * Activate/Deactivate caching. Per default the caching mode is
	 * parsed from the target system configuration. With this function
	 * the caching mode can be set easier. When caching is activated
	 * raw LML data can be cached among multiple PTP clients.
	 * If caching is off, every client runs its LML_DA instance
	 * entirely separately. This function must be called before
	 * running the corresponding update.
	 * 
	 * @param name
	 *            hash identifiying the Lgui, whose cache mode needs to be updated
	 * @param activeCaching
	 *            if true, caching is activated, otherwise it is deactivated
	 */
	public void setCachingMode(String name, boolean activeCaching) {
		ILguiItem lguiItem = null;
		synchronized (LGUIS) {
			lguiItem = LGUIS.get(name);
		}
		if (lguiItem != null) {
			lguiItem.setForceUpdate(!activeCaching);
		}
	}

	/**
	 * Run a refresh for one connection identified by <code>name</code>.
	 * Sends the current LML layout to the passed output stream.
	 * Expects an LML file as response from the input stream.
	 * 
	 * @param name
	 *            the hash name identifying the new selected Lgui
	 * @param input
	 *            input stream attached to LML_DA for retrieving the updated LML file
	 * @param output
	 *            output stream attached to LML_DA for transferring the LML request to the remote system
	 * @throws CoreException
	 */
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

	/**
	 * Only read the current LML layout for the given resource manager.
	 * If there is no resource manager with that name, nothing is written
	 * to the output stream. The output can be used as request for the LML_DA
	 * server scripts.
	 * 
	 * @param name
	 *            the hash name identifying the new selected Lgui
	 * @param output
	 *            stream, into which the current LML layout of the corresponding resource manager is written
	 */
	public void readCurrentLayout(String name, OutputStream output) {
		ILguiItem lguiItem = null;
		synchronized (LGUIS) {
			lguiItem = LGUIS.get(name);
		}
		if (lguiItem != null) {
			lguiItem.getCurrentLayout(output);
		}
	}

	/**
	 * Update the status of a user job. Besides the status attribute, the status detail
	 * string can also be passed and set to the jobstatus.
	 * 
	 * @param name
	 *            the hash name identifying the new selected Lgui
	 * @param jobId
	 *            the job ID, for which is status update is triggered
	 * @param status
	 *            the new status value, e.g. RUNNING, SUBMITTED, COMPLETED
	 * @param detail
	 *            a more detailed description of the job's status
	 */
	public void updateUserJob(String name, String jobId, String status, String detail) {
		final ILguiItem lguiItem = LGUIS.get(name);
		if (lguiItem != null) {
			lguiItem.updateUserJob(jobId, status, detail);
		}
		if (lguiItem == fLguiItem) {
			fireUpdatedLgui();
		}
	}

	/**
	 * Tells the listeners, that the object identified by oid is selected.
	 * 
	 * @param oid
	 *            the LML object ID for the selected object
	 */
	private void fireChangeSelectedObject(String oid) {
		final ISelectObjectEvent event = new SelectObjectEvent(oid);
		for (final Object listener : viewListeners.getListeners()) {
			((ILMLListener) listener).handleEvent(event);
		}
	}

	/**
	 * Tell the listeners that the table identified by gid has to be filtered
	 * by the patterns.
	 * 
	 * @param gid
	 *            table ID, which should be filtered
	 * @param filterValues
	 *            patterns for filtering the table
	 */
	private void fireFilterLgui(String gid, List<IPattern> filterValues) {
		final ITableFilterEvent event = new TableFilterEvent(gid, filterValues);
		for (final Object listener : viewListeners.getListeners()) {
			((ILMLListener) listener).handleEvent(event);
		}
	}

	/**
	 * Tell the listeners, that the object identified by oid is to be marked.
	 * 
	 * @param oid
	 *            the LML object ID for the marked object
	 */
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

	/**
	 * Tell listeners, that an Lgui is removed.
	 * 
	 * @param title
	 *            the removed Lgui
	 */
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

	/**
	 * Tell listeners, that an object's marking is undone.
	 * 
	 * @param oid
	 *            the LML object ID for the previously marked object
	 */
	private void fireUnmarkObject(String oid) {
		final IUnmarkObjectEvent event = new UnmarkObjectEvent(oid);
		for (final Object listener : viewListeners.getListeners()) {
			((ILMLListener) listener).handleEvent(event);
		}
	}

	/**
	 * Tell listeners, that an object's selection is undone.
	 * 
	 * @param oid
	 *            the LML object ID for the previously selected object
	 */
	private void fireUnselectObject(String oid) {
		final IUnselectedObjectEvent event = new UnselectObjectEvent(oid);
		for (final Object listener : viewListeners.getListeners()) {
			((ILMLListener) listener).handleEvent(event);
		}
	}

	/**
	 * Tell listeners, that a new LML file is received from LML_da.
	 */
	private void fireUpdatedLgui() {
		final IViewUpdateEvent event = new ViewUpdateEvent();
		for (final Object listener : viewListeners.getListeners()) {
			((ILMLListener) listener).handleEvent(event);
		}
	}

	/**
	 * Forward stored jobs to the Lgui passed to this function.
	 * This method is needed, when starting an Lgui in a subsequent
	 * session.
	 * 
	 * @param item
	 *            the Lgui, which should display the jobs
	 * @param jobs
	 *            list of stored jobs
	 */
	private void restoreJobStatusData(ILguiItem item, JobStatusData[] jobs) {
		if (jobs != null && jobs.length > 0) {
			for (final JobStatusData status : jobs) {
				item.addUserJob(status.getJobId(), status, false);
			}
		}
	}
}
