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

import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URI;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.eclipse.core.runtime.ListenerList;
import org.eclipse.ptp.rm.lml.core.events.IJobListSortedEvent;
import org.eclipse.ptp.rm.lml.core.events.ILguiAddedEvent;
import org.eclipse.ptp.rm.lml.core.events.ILguiRemovedEvent;
import org.eclipse.ptp.rm.lml.core.events.ILguiSelectedEvent;
import org.eclipse.ptp.rm.lml.core.events.IMarkObjectEvent;
import org.eclipse.ptp.rm.lml.core.events.ISelectedObjectChangeEvent;
import org.eclipse.ptp.rm.lml.core.events.ITableColumnChangeEvent;
import org.eclipse.ptp.rm.lml.core.events.IUnmarkObjectEvent;
import org.eclipse.ptp.rm.lml.core.events.IUnselectedObjectEvent;
import org.eclipse.ptp.rm.lml.core.events.IViewAddedEvent;
import org.eclipse.ptp.rm.lml.core.events.IViewDisposedEvent;
import org.eclipse.ptp.rm.lml.core.events.IViewUpdateEvent;
import org.eclipse.ptp.rm.lml.core.listeners.ILMLListener;
import org.eclipse.ptp.rm.lml.core.listeners.IListener;
import org.eclipse.ptp.rm.lml.core.listeners.IViewListener;
import org.eclipse.ptp.rm.lml.core.model.ILguiItem;
import org.eclipse.ptp.rm.lml.internal.core.elements.InfoType;
import org.eclipse.ptp.rm.lml.internal.core.events.JobListSortedEvent;
import org.eclipse.ptp.rm.lml.internal.core.events.LguiAddedEvent;
import org.eclipse.ptp.rm.lml.internal.core.events.LguiRemovedEvent;
import org.eclipse.ptp.rm.lml.internal.core.events.LguiSelectedEvent;
import org.eclipse.ptp.rm.lml.internal.core.events.MarkObjectEvent;
import org.eclipse.ptp.rm.lml.internal.core.events.SelectedObjectChangeEvent;
import org.eclipse.ptp.rm.lml.internal.core.events.TableColumnChangeEvent;
import org.eclipse.ptp.rm.lml.internal.core.events.UnmarkObjectEvent;
import org.eclipse.ptp.rm.lml.internal.core.events.UnselectObjectEvent;
import org.eclipse.ptp.rm.lml.internal.core.events.ViewAddedEvent;
import org.eclipse.ptp.rm.lml.internal.core.events.ViewDisposedEvent;
import org.eclipse.ptp.rm.lml.internal.core.events.ViewUpdateEvent;
import org.eclipse.ptp.rm.lml.internal.core.model.LguiItem;
import org.eclipse.ptp.rm.lml.internal.core.model.jobs.JobStatusData;
import org.eclipse.ptp.rmsystem.IJobStatus;
import org.eclipse.ui.IMemento;

/**
 * Class of the interface ILMLManager
 */
public class LMLManager {

	public static LMLManager getInstance() {
		if (manager == null) {
			manager = new LMLManager();
		}
		return manager;
	}

	/*
	 * List of the currently running Resource Managers
	 */
	// private final List<String> openLguis = new LinkedList<String>();

	/*
	 * Map of all ILguiItems
	 * 
	 * For every created Resource Manager instance there is an entry in this
	 * map; as long as the Resource Manager instance is not removed an
	 * associates entry keeps in this map
	 */
	protected final Map<String, ILguiItem> LGUIS = new HashMap<String, ILguiItem>();

	/*
	 * The current considered ILguiItem
	 */
	private ILguiItem fLguiItem = null;

	/*
	 * A list of all listeners on the ILguiItem
	 */
	private final ListenerList lmlListeners = new ListenerList();

	/*
	 * A list of all listeners on the views
	 */
	private final ListenerList viewListeners = new ListenerList();

	/*
	 * A list of all listeners.
	 */
	private final Map<String, IListener> listeners = new HashMap<String, IListener>();

	/*
	 * A list of jobs started by the user.
	 */
	private final Map<String, Map<String, JobStatusData>> userJobList = new HashMap<String, Map<String, JobStatusData>>();

	/*
	 * An instance of this class.
	 */
	private static LMLManager manager;
	/*
	 * 
	 */
	public static final String SELECT = "selected";

	/*
	 * 
	 */
	public static final String LGUIITEM = "lguiitem";

	private boolean isDisplayed = false;

	private LMLManager() {
		manager = this;
	}

	public void addComponent(String gid) {
		final String type = fLguiItem.getLayoutAccess().setComponentActive(gid, true);
		fireAddView(gid, type);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ptp.rm.lml.core.ILMLManager#addLgui(URL xmlFile)
	 */
	public boolean addLgui(URI xmlFile) {
		if (!LGUIS.containsKey(xmlFile.getPath())) {
			fLguiItem = new LguiItem(xmlFile);
			synchronized (LGUIS) {
				LGUIS.put(xmlFile.getPath(), fLguiItem);
			}
			fireNewLgui();
			return false;
		} else {
			fLguiItem = LGUIS.get(xmlFile);
			return true;
		}
	}

	public void addListener(ILMLListener listener, String view) {
		lmlListeners.add(listener);
		// listeners.put(view, (IListener) listener);
	}

	public void addListener(IViewListener listener) {
		viewListeners.add(listener);
		listeners.put("ViewManager", listener);
	}

	public void addListener(IViewListener listener, String view) {
		viewListeners.add(listener);
		listeners.put(view, listener);
	}

	public void addUserJob(String name, String jobId, IJobStatus status) {
		final JobStatusData statusData = new JobStatusData(status);
		userJobList.get(name).put(jobId, statusData);
		final ILguiItem item = LGUIS.get(name);
		if (item != null) {
			item.addUserJob(jobId, statusData);
		}
		if (item == fLguiItem) {
			fireUpdatedLgui();
		}
	}

	public void closeLgui(String name, IMemento memento) {
		ILguiItem item = null;
		synchronized (LGUIS) {
			item = LGUIS.get(name);
			if (item != null) {
				LGUIS.remove(name);
			}
		}
		if (fLguiItem != null && fLguiItem == item) {
			fireRemovedLgui(item);
		}

		/*
		 * takes care of persisting user job state info
		 */
		saveJobStatusData(userJobList.remove(name), memento);

	}

	private void fireAddView(String gid, String type) {
		final IViewAddedEvent event = new ViewAddedEvent(gid, type);
		for (final Object listener : viewListeners.getListeners()) {
			((IViewListener) listener).handleEvent(event);
		}
	}

	private void fireChangeSelectedObject(String oid) {
		final ISelectedObjectChangeEvent event = new SelectedObjectChangeEvent(oid);
		for (final Object listener : lmlListeners.getListeners()) {
			((ILMLListener) listener).handleEvent(event);
		}
	}

	private void fireChangeTableColumn() {
		final ITableColumnChangeEvent event = new TableColumnChangeEvent(this, fLguiItem);
		for (final Object listener : lmlListeners.getListeners()) {
			((ILMLListener) listener).handleEvent(event);
		}

	}

	private void fireMarkObject(String oid) {
		final IMarkObjectEvent event = new MarkObjectEvent(oid);
		for (final Object listener : lmlListeners.getListeners()) {
			((ILMLListener) listener).handleEvent(event);
		}
	}

	/**
	 * Method is called when a new ILguiItem was generated.
	 */
	private void fireNewLgui() {
		final ILguiAddedEvent event = new LguiAddedEvent();
		for (final Object listener : lmlListeners.getListeners()) {
			((ILMLListener) listener).handleEvent(event);
		}
		isDisplayed = true;
		// for (Object listener : viewListeners.getListeners()) {
		// ((IViewListener) listener).handleEvent(event);
		// }
	}

	private void fireRemovedLgui(ILguiItem title) {
		final ILguiRemovedEvent event = new LguiRemovedEvent();
		for (final Object listener : lmlListeners.getListeners()) {
			((ILMLListener) listener).handleEvent(event);
		}
		//
		// for (Object listener : viewListeners.getListeners()) {
		// ((IViewListener) listener).handleEvent(event);
		// }
		isDisplayed = false;
	}

	private void fireRemoveView(String gid) {
		final IViewDisposedEvent event = new ViewDisposedEvent();
		for (final Object listener : viewListeners.getListeners()) {
			((IViewListener) listener).handleEvent(event);
		}
	}

	private void fireSelectedLgui() {
		final ILguiSelectedEvent event = new LguiSelectedEvent(this, fLguiItem);
		for (final Object listener : viewListeners.getListeners()) {
			((IViewListener) listener).handleEvent(event);
		}
	}

	/**
	 * Method is called when an ILguiItem was sorted.
	 */
	private void fireSortedLgui() {
		final IJobListSortedEvent event = new JobListSortedEvent(this, fLguiItem);
		for (final Object listener : lmlListeners.getListeners()) {
			((ILMLListener) listener).handleEvent(event);
		}
	}

	private void fireUnmarkObject(String oid) {
		final IUnmarkObjectEvent event = new UnmarkObjectEvent(oid);
		for (final Object listener : lmlListeners.getListeners()) {
			((ILMLListener) listener).handleEvent(event);
		}
	}

	private void fireUnselectObject(String oid) {
		final IUnselectedObjectEvent event = new UnselectObjectEvent(oid);
		for (final Object listener : lmlListeners.getListeners()) {
			((ILMLListener) listener).handleEvent(event);
		}
	}

	private void fireUpdatedLgui() {
		final IViewUpdateEvent event = new ViewUpdateEvent();
		for (final Object listener : lmlListeners.getListeners()) {
			((ILMLListener) listener).handleEvent(event);
		}
	}

	public JobStatusData getJobStatusData(String rmName, String jobId) {
		return userJobList.get(rmName).get(jobId);
	}

	public InfoType getJobStatusDataInfo(String rmName, String jobId) {
		return userJobList.get(rmName).get(jobId).getJobInfo();
	}

	public ILguiItem[] getLguiItems() {
		synchronized (LGUIS) {
			final Collection<ILguiItem> lguis = LGUIS.values();
			return lguis.toArray(new ILguiItem[lguis.size()]);
		}
	}

	public String[] getLguis() {
		synchronized (LGUIS) {
			final Set<String> lguis = LGUIS.keySet();
			return lguis.toArray(new String[lguis.size()]);
		}
	}

	public IListener getListener(String view) {
		return listeners.get(view);
	}

	public void getRequestXml() {
		FileOutputStream os = null;
		try {
			os = new FileOutputStream("request.xml");
		} catch (final FileNotFoundException e) {
			e.printStackTrace();
		}
		fLguiItem.getRequestXml(os);
	}

	public int getSelectedLguiIndex(String title) {
		int index = 0;
		int i = 0;
		for (final String key : LGUIS.keySet()) {
			if (title.equals(key)) {
				index = i;
			}
			i++;
		}
		return index;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ptp.rm.lml.core.ILMLManager#getSelectedLguiItem()
	 */
	public ILguiItem getSelectedLguiItem() {
		return fLguiItem;
	}

	public void markObject(String oid) {
		fireMarkObject(oid);
	}

	public void openLgui(String name, IMemento memento) {
		synchronized (LGUIS) {
			ILguiItem item = LGUIS.get(name);
			if (item == null) {
				item = new LguiItem(name);
				LGUIS.put(name, item);
			}
			fLguiItem = item;
		}

		final Map<String, JobStatusData> map = new HashMap<String, JobStatusData>();

		restoreJobStatusData(map, memento);
		userJobList.put(name, map);
		fLguiItem.restoreUserJobs(map);

		if (!fLguiItem.isEmpty()) {
			fireNewLgui();
		}
	}

	public void register(String name, InputStream input, OutputStream output) {
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

		// TODO Checking for jobs, updating the InfoTyp in JobStatusData
	}

	public void removeComponent(String gid) {
		fLguiItem.getLayoutAccess().setComponentActive(gid, false);
		fireRemoveView(gid);
	}

	public void removeLgui(String title) {
		final ILguiItem item = LGUIS.get(title);
		LGUIS.remove(title);
		if (LGUIS.isEmpty()) {
			fLguiItem = null;
		} else {
			fLguiItem = LGUIS.get(getLguis()[0]);
		}
		fireRemovedLgui(item);
	}

	public void removeListener(ILMLListener listener) {
		lmlListeners.remove(listener);
	}

	public void removeListener(IViewListener listener) {
		viewListeners.remove(listener);
		listeners.remove("ViewManager");
	}

	public void removeUserJob(String name, String jobId) {
		userJobList.get(name).remove(jobId);
		final ILguiItem lguiItem = LGUIS.get(name);
		if (lguiItem != null) {
			lguiItem.removeUserJob(jobId);
		}
		if (lguiItem == fLguiItem) {
			fireUpdatedLgui();
		}
	}

	/**
	 * 
	 * @param map
	 * @param memento
	 *            may be <code>null</code>
	 */
	private void restoreJobStatusData(Map<String, JobStatusData> map, IMemento memento) {
		if (memento != null) {
			final List<JobStatusData> dataList = JobStatusData.reload(memento);
			/*
			 * NB: This may not be what you need to do; the work done here is
			 * just a placeholder -- Al
			 */
			for (final JobStatusData jobStatusData : dataList) {
				map.put(jobStatusData.getJobId(), jobStatusData);
			}
			for (final JobStatusData data : dataList) {

			}
		}
	}

	/**
	 * @param map
	 * @param memento
	 *            guaranteed by caller to be non-<code>null</code>
	 */
	private void saveJobStatusData(Map<String, JobStatusData> map, IMemento memento) {
		for (final JobStatusData jobStatusData : map.values()) {
			/*
			 * NB: This may not be what you need to do; the work done here is
			 * just a placeholder -- Al
			 */
			jobStatusData.save(memento);
		}
	}

	public void selectLgui(String name) {
		fireRemovedLgui(null);
		if (name != null) {
			final ILguiItem item = LGUIS.get(name);
			if (item != null) {
				fLguiItem = item;
				fireNewLgui();
				return;
			}

		}
		fLguiItem = null;
	}

	public void selectLgui(URI xmlFile) {
		fLguiItem = LGUIS.get(xmlFile.getPath());
		fireSelectedLgui();
	}

	public void selectObject(String oid) {
		fireChangeSelectedObject(oid);
	}

	public void setTableColumnActive(String gid, String title) {
		fLguiItem.getTableHandler().setTableColumnActive(gid, title, true);
		fireChangeTableColumn();
	}

	public void setTableColumnNonActive(String gid, String title) {
		fLguiItem.getTableHandler().setTableColumnActive(gid, title, false);
		fireChangeTableColumn();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ptp.rm.lml.core.ILMLManager#sortLgui
	 */
	public void sortLgui() {
		fireSortedLgui();
	}

	public void unmarkObject(String oid) {
		fireUnmarkObject(oid);
	}

	public void unselectObject(String oid) {
		fireUnselectObject(oid);
	}

	public void update() {
		// fLguiItem.updateXML();
		fireNewLgui();
	}

	public void update(InputStream stream) {
		fLguiItem.update(stream);
		fireNewLgui();
	}

	public void updateJobData(String rmName, String jobName, InfoType jobInfo) {
		userJobList.get(rmName).get(jobName).setJobInfo(jobInfo);
	}

	public void updateUserJob(String name, String jobId, IJobStatus status) {
		final JobStatusData statusData = userJobList.get(name).get(jobId);
		statusData.updateState(status);
		final ILguiItem lguiItem = LGUIS.get(name);
		if (lguiItem != null) {
			lguiItem.update(name, statusData);
		}
		if (lguiItem == fLguiItem) {
			fireUpdatedLgui();
		}
	}
}
