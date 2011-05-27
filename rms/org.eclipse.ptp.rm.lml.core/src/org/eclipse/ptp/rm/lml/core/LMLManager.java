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

	/*
	 * Map of all ILguiItems
	 * 
	 * For every created Resource Manager instance there is an entry in this
	 * map; as long as the Resource Manager instance is not removed an
	 * associates entry keeps in this map
	 */
	protected final Map<String, ILguiItem> LGUIS = new HashMap<String, ILguiItem>();

	/*
	 * List of the currently running Resource Managers
	 */
	// private final List<String> openLguis = new LinkedList<String>();

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

	/**************************************************************************************************************
	 * Constructors
	 **************************************************************************************************************/

	private LMLManager() {
		manager = this;
	}

	public void addComponent(String gid) {
		String type = fLguiItem.getLayoutAccess().setComponentActive(gid, true);
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

	/**************************************************************************************************************
	 * Listener methods
	 **************************************************************************************************************/

	public void addListener(IViewListener listener) {
		viewListeners.add(listener);
		listeners.put("ViewManager", listener);
	}

	public void addListener(IViewListener listener, String view) {
		viewListeners.add(listener);
		listeners.put(view, listener);
	}

	/**************************************************************************************************************
	 * Job related methods
	 **************************************************************************************************************/

	public void addUserJob(String name, String jobId, IJobStatus status) {
		JobStatusData statusData = new JobStatusData(status);
		userJobList.get(name).put(jobId, statusData);

		// TODO Showing it at one of the views
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

	public ILguiItem[] getLguiItems() {
		synchronized (LGUIS) {
			Collection<ILguiItem> lguis = LGUIS.values();
			return lguis.toArray(new ILguiItem[lguis.size()]);
		}
	}

	public String[] getLguis() {
		synchronized (LGUIS) {
			Set<String> lguis = LGUIS.keySet();
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
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		}
		fLguiItem.getRequestXml(os);
	}

	public int getSelectedLguiIndex(String title) {
		int index = 0;
		int i = 0;
		for (String key : LGUIS.keySet()) {
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

	/**************************************************************************************************************
	 * Lgui handling methods
	 **************************************************************************************************************/

	public void openLgui(String name, IMemento memento) {
		synchronized (LGUIS) {
			ILguiItem item = LGUIS.get(name);
			if (item == null) {
				item = new LguiItem(name);
				LGUIS.put(name, item);
			}
			fLguiItem = item;
		}
		if (!fLguiItem.isEmpty()) {
			fireNewLgui();
		}

		Map<String, JobStatusData> map = new HashMap<String, JobStatusData>();

		restoreJobStatusData(map, memento);
		userJobList.put(name, map);
	}

	/**************************************************************************************************************
	 * Communication methods
	 **************************************************************************************************************/

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
		ILguiItem item = LGUIS.get(title);
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

		// TODO Changing one of the views
	}

	// public ILguiItem restoreState(IMemento memento) {
	// if (memento == null) {
	// return null;
	// }
	// IMemento[] mementoChilds = memento.getChildren(LGUIITEM);
	// for (IMemento mementoChild : mementoChilds) {
	// ILguiItem lguiItem = new LguiItem(mementoChild.getID());
	// LGUIS.put(mementoChild.getID(), lguiItem);
	// // lguiItem.restore(mementoChild);
	// }
	// String nameSelected = memento.getString(SELECT);
	// if (!LGUIS.containsKey(nameSelected)) {
	// ILguiItem lguiItem = new LguiItem(nameSelected);
	// LGUIS.put(nameSelected, lguiItem);
	// }
	// fLguiItem = LGUIS.get(nameSelected);
	// return fLguiItem;
	// }

	/**************************************************************************************************************
	 * Saving and restoring
	 **************************************************************************************************************/
	// public void saveState(IMemento memento) {
	// for (Entry<String, ILguiItem> entry : LGUIS.entrySet()) {
	// memento.createChild(LGUIITEM, entry.getKey());
	// // entry.getValue().save(memento);
	// }
	// memento.putString(SELECT, fLguiItem.toString());
	// }

	public void selectLgui(String name) {
		fireRemovedLgui(null);
		if (name != null) {
			ILguiItem item = LGUIS.get(name);
			if (fLguiItem != item) {
				fLguiItem = item;
			}
			fireNewLgui();
			return;
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

	/**************************************************************************************************************
	 * View methods
	 **************************************************************************************************************/

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

	public void updateUserJob(String name, String jobId, IJobStatus status) {
		JobStatusData statusData = userJobList.get(name).get(jobId);
		statusData.updateState(status);

		// TODO Checking if showing in another view and than showing it
	}

	private void fireAddView(String gid, String type) {
		IViewAddedEvent event = new ViewAddedEvent(gid, type);
		for (Object listener : viewListeners.getListeners()) {
			((IViewListener) listener).handleEvent(event);
		}
	}

	private void fireChangeSelectedObject(String oid) {
		ISelectedObjectChangeEvent event = new SelectedObjectChangeEvent(oid);
		for (Object listener : lmlListeners.getListeners()) {
			((ILMLListener) listener).handleEvent(event);
		}
	}

	/**************************************************************************************************************
	 * Fire events method
	 **************************************************************************************************************/

	private void fireChangeTableColumn() {
		ITableColumnChangeEvent event = new TableColumnChangeEvent(this, fLguiItem);
		for (Object listener : lmlListeners.getListeners()) {
			((ILMLListener) listener).handleEvent(event);
		}

	}

	private void fireMarkObject(String oid) {
		IMarkObjectEvent event = new MarkObjectEvent(oid);
		for (Object listener : lmlListeners.getListeners()) {
			((ILMLListener) listener).handleEvent(event);
		}
	}

	/**
	 * Method is called when a new ILguiItem was generated.
	 */
	private void fireNewLgui() {
		ILguiAddedEvent event = new LguiAddedEvent();
		for (Object listener : lmlListeners.getListeners()) {
			((ILMLListener) listener).handleEvent(event);
		}
		isDisplayed = true;
		// for (Object listener : viewListeners.getListeners()) {
		// ((IViewListener) listener).handleEvent(event);
		// }
	}

	private void fireRemovedLgui(ILguiItem title) {
		ILguiRemovedEvent event = new LguiRemovedEvent();
		for (Object listener : lmlListeners.getListeners()) {
			((ILMLListener) listener).handleEvent(event);
		}
		//
		// for (Object listener : viewListeners.getListeners()) {
		// ((IViewListener) listener).handleEvent(event);
		// }
		isDisplayed = false;
	}

	private void fireRemoveView(String gid) {
		IViewDisposedEvent event = new ViewDisposedEvent();
		for (Object listener : viewListeners.getListeners()) {
			((IViewListener) listener).handleEvent(event);
		}
	}

	private void fireSelectedLgui() {
		ILguiSelectedEvent event = new LguiSelectedEvent(this, fLguiItem);
		for (Object listener : viewListeners.getListeners()) {
			((IViewListener) listener).handleEvent(event);
		}
	}

	/**
	 * Method is called when an ILguiItem was sorted.
	 */
	private void fireSortedLgui() {
		IJobListSortedEvent event = new JobListSortedEvent(this, fLguiItem);
		for (Object listener : lmlListeners.getListeners()) {
			((ILMLListener) listener).handleEvent(event);
		}
	}

	private void fireUnmarkObject(String oid) {
		IUnmarkObjectEvent event = new UnmarkObjectEvent(oid);
		for (Object listener : lmlListeners.getListeners()) {
			((ILMLListener) listener).handleEvent(event);
		}
	}

	private void fireUnselectObject(String oid) {
		IUnselectedObjectEvent event = new UnselectObjectEvent(oid);
		for (Object listener : lmlListeners.getListeners()) {
			((ILMLListener) listener).handleEvent(event);
		}
	}

	private void fireUpdatedLgui() {
		IViewUpdateEvent event = new ViewUpdateEvent();
		for (Object listener : lmlListeners.getListeners()) {
			((ILMLListener) listener).handleEvent(event);
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
			List<JobStatusData> dataList = JobStatusData.reload(memento);
			/*
			 * NB: This may not be what you need to do; the work done here is
			 * just a placeholder -- Al
			 */
			for (JobStatusData jobStatusData : dataList) {
				map.put(jobStatusData.getJobId(), jobStatusData);
			}
		}
	}

	/**
	 * @param map
	 * @param memento
	 *            guaranteed by caller to be non-<code>null</code>
	 */
	private void saveJobStatusData(Map<String, JobStatusData> map, IMemento memento) {
		for (JobStatusData jobStatusData : map.values()) {
			/*
			 * NB: This may not be what you need to do; the work done here is
			 * just a placeholder -- Al
			 */
			jobStatusData.save(memento);
		}
	}

	/**************************************************************************************************************
	 * Getting methods
	 **************************************************************************************************************/

	public static LMLManager getInstance() {
		if (manager == null) {
			manager = new LMLManager();
		}
		return manager;
	}
}
