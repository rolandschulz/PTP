/*******************************************************************************
 * Copyright (c) 2005 The Regents of the University of California. 
 * This material was produced under U.S. Government contract W-7405-ENG-36 
 * for Los Alamos National Laboratory, which is operated by the University 
 * of California for the U.S. Department of Energy. The U.S. Government has 
 * rights to use, reproduce, and distribute this software. NEITHER THE 
 * GOVERNMENT NOR THE UNIVERSITY MAKES ANY WARRANTY, EXPRESS OR IMPLIED, OR 
 * ASSUMES ANY LIABILITY FOR THE USE OF THIS SOFTWARE. If software is modified 
 * to produce derivative works, such modified software should be clearly marked, 
 * so as not to confuse it with the version available from LANL.
 * 
 * Additionally, this program and the accompanying materials 
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * LA-CC 04-115
 *******************************************************************************/
package org.eclipse.ptp.ui.managers;

import java.util.HashMap;
import java.util.Map;

import org.eclipse.core.runtime.ListenerList;
import org.eclipse.core.runtime.SafeRunner;
import org.eclipse.jface.util.SafeRunnable;
import org.eclipse.ptp.ui.IElementManager;
import org.eclipse.ptp.ui.listeners.IJobChangedListener;
import org.eclipse.ptp.ui.listeners.ISetListener;
import org.eclipse.ptp.ui.model.ElementSet;
import org.eclipse.ptp.ui.model.IElement;
import org.eclipse.ptp.ui.model.IElementHandler;
import org.eclipse.ptp.ui.model.IElementSet;
import org.eclipse.swt.graphics.Image;

/**
 * @author Clement chu
 * 
 */
public abstract class AbstractElementManager implements IElementManager {
	protected String cur_set_id = EMPTY_ID;
	protected ListenerList setListeners = new ListenerList();
	protected ListenerList jListeners = new ListenerList();
	protected Map<String, IElementHandler> elementHandlers = new HashMap<String, IElementHandler>();

	/**
	 * Constructor
	 */
	public AbstractElementManager() {
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ptp.ui.IElementManager#addJobListener(org.eclipse.ptp.ui. listeners.IJobListener)
	 */
	public void addJobChangedListener(IJobChangedListener jobListener) {
		jListeners.add(jobListener);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ptp.ui.IElementManager#addSetListener(org.eclipse.ptp.ui. listeners.ISetListener)
	 */
	public void addSetListener(ISetListener setListener) {
		setListeners.add(setListener);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ptp.ui.IElementManager#addToSet(org.eclipse.ptp.ui.model. IElement[], org.eclipse.ptp.ui.model.IElementSet)
	 */
	public void addToSet(IElement[] elements, IElementSet set) {
		set.addElements(elements);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ptp.ui.IElementManager#addToSet(org.eclipse.ptp.ui.model. IElement[], java.lang.String,
	 * org.eclipse.ptp.ui.model.IElementHandler)
	 */
	public void addToSet(IElement[] elements, String setID, IElementHandler elementHandler) {
		IElementSet set = (IElementSet) elementHandler.getElementByID(setID);
		addToSet(elements, set);
		fireSetEvent(ADD_ELEMENT_TYPE, elements, set, null);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ptp.ui.IElementManager#clear()
	 */
	public void clear() {
		elementHandlers.clear();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ptp.ui.IElementManager#createSet(org.eclipse.ptp.ui.model .IElement[], java.lang.String, java.lang.String,
	 * org.eclipse.ptp.ui.model.IElementHandler)
	 */
	public String createSet(IElement[] elements, String setID, String setName, IElementHandler elementHandler) {
		IElementSet set = new ElementSet(elementHandler, setID, setName);
		addToSet(elements, set);
		elementHandler.addElements(new IElement[] { set });
		fireSetEvent(CREATE_SET_TYPE, elements, set, null);
		return set.getID();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ptp.ui.IElementManager#fireJobListener(int, java.lang.String, java.lang.String)
	 */
	public void fireJobChangedEvent(final int type, final String cur_job_id, final String pre_job_id) {
		Object[] array = jListeners.getListeners();
		for (Object element : array) {
			final IJobChangedListener listener = (IJobChangedListener) element;
			SafeRunner.run(new SafeRunnable() {
				public void run() {
					listener.jobChangedEvent(type, cur_job_id, pre_job_id);
				}
			});
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ptp.ui.IElementManager#fireEvent(int, org.eclipse.ptp.ui.model.IElement[],
	 * org.eclipse.ptp.ui.model.IElementSet, org.eclipse.ptp.ui.model.IElementSet)
	 */
	public void fireSetEvent(final int eventType, final IElement[] elements, final IElementSet cur_set, final IElementSet pre_set) {
		Object[] array = setListeners.getListeners();
		for (Object element : array) {
			final ISetListener setListener = (ISetListener) element;
			SafeRunner.run(new SafeRunnable() {
				public void run() {
					switch (eventType) {
					case CREATE_SET_TYPE:
						setListener.createSetEvent(cur_set, elements);
						break;
					case DELETE_SET_TYPE:
						setListener.deleteSetEvent(cur_set);
						break;
					case CHANGE_SET_TYPE:
						setListener.changeSetEvent(cur_set, pre_set);
						break;
					case ADD_ELEMENT_TYPE:
						setListener.addElementsEvent(cur_set, elements);
						break;
					case REMOVE_ELEMENT_TYPE:
						setListener.removeElementsEvent(cur_set, elements);
						break;
					}
				}
			});
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ptp.ui.IElementManager#getCurrentSetId()
	 */
	public String getCurrentSetId() {
		return cur_set_id;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ptp.ui.IElementManager#getElementHandler(java.lang.String)
	 */
	public IElementHandler getElementHandler(String id) {
		return elementHandlers.get(id);
	}

	public void removeElementHandler(String id) {
		elementHandlers.remove(id);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ptp.ui.IElementManager#getImage(org.eclipse.ptp.ui.model. IElement)
	 */
	public abstract Image getImage(IElement element);

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ptp.ui.IElementManager#removeFromSet(org.eclipse.ptp.ui.model .IElement[], java.lang.String,
	 * org.eclipse.ptp.ui.model.IElementHandler)
	 */
	public void removeFromSet(IElement[] elements, String setID, IElementHandler elementHandler) {
		IElementSet set = (IElementSet) elementHandler.getElementByID(setID);
		set.removeElements(elements);
		fireSetEvent(REMOVE_ELEMENT_TYPE, elements, set, null);
		if (set.size() == 0) {
			removeSet(setID, elementHandler);
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ptp.ui.IElementManager#removeJobListener(org.eclipse.ptp. ui.listeners.IJobListener)
	 */
	public void removeJobChangedListener(IJobChangedListener jobListener) {
		jListeners.remove(jobListener);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ptp.ui.IElementManager#removeSet(java.lang.String, org.eclipse.ptp.ui.model.IElementHandler)
	 */
	public void removeSet(String setID, IElementHandler elementHandler) {
		IElementSet set = (IElementSet) elementHandler.getElementByID(setID);
		elementHandler.removeElements(new IElement[] { set });
		fireSetEvent(DELETE_SET_TYPE, null, set, null);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ptp.ui.IElementManager#removeSetListener(org.eclipse.ptp. ui.listeners.ISetListener)
	 */
	public void removeSetListener(ISetListener setListener) {
		setListeners.remove(setListener);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ptp.ui.IElementManager#setElementHandler(java.lang.String, org.eclipse.ptp.ui.model.IElementHandler)
	 */
	public void setElementHandler(String id, IElementHandler handler) {
		elementHandlers.put(id, handler);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ptp.ui.IElementManager#shutdown()
	 */
	public void shutdown() {
		setListeners.clear();
		jListeners.clear();
	}
}
