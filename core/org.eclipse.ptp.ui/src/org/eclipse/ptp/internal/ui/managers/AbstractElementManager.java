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
package org.eclipse.ptp.internal.ui.managers;

import java.util.BitSet;
import java.util.HashMap;
import java.util.Map;

import org.eclipse.core.runtime.ListenerList;
import org.eclipse.core.runtime.SafeRunner;
import org.eclipse.jface.util.SafeRunnable;
import org.eclipse.ptp.internal.ui.IElementManager;
import org.eclipse.ptp.internal.ui.listeners.ISetListener;
import org.eclipse.ptp.internal.ui.model.IElementHandler;
import org.eclipse.ptp.internal.ui.model.IElementSet;
import org.eclipse.swt.graphics.Image;

/**
 * @author Clement chu
 * 
 */
public abstract class AbstractElementManager implements IElementManager {
	protected String cur_set_id = EMPTY_ID;
	protected Map<String, IElementHandler> elementHandlers = new HashMap<String, IElementHandler>();
	protected ListenerList setListeners = new ListenerList();

	/**
	 * Constructor
	 */
	public AbstractElementManager() {
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ptp.ui.IElementManager#addToSet(java.lang.String, org.eclipse.ptp.ui.model.IElementHandler,
	 * java.util.BitSet)
	 */
	/**
	 * @since 7.0
	 */
	@Override
	public void addToSet(String setID, IElementHandler elementHandler, BitSet elements) {
		IElementSet set = elementHandler.getSet(setID);
		set.addElements(elements);
		fireSetEvent(ADD_ELEMENT_TYPE, elements, set, null);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ptp.ui.IElementManager#clear()
	 */
	@Override
	public void clear() {
		elementHandlers.clear();
	}

	/**
	 * @since 7.0
	 */
	@Override
	public String createSet(String setID, String setName, IElementHandler elementHandler, BitSet elements) {
		IElementSet set = elementHandler.createSet(setID, setName, elements);
		fireSetEvent(CREATE_SET_TYPE, elements, set, null);
		return set.getID();
	}

	/**
	 * @since 7.0
	 */
	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ptp.ui.IElementManager#fireSetEvent(int, java.util.BitSet, org.eclipse.ptp.ui.model.IElementSet,
	 * org.eclipse.ptp.ui.model.IElementSet)
	 */
	@Override
	public void fireSetEvent(final int eventType, final BitSet elements, final IElementSet cur_set, final IElementSet pre_set) {
		for (Object listener : setListeners.getListeners()) {
			final ISetListener setListener = (ISetListener) listener;
			SafeRunner.run(new SafeRunnable() {
				@Override
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
	@Override
	public String getCurrentSetId() {
		return cur_set_id;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ptp.ui.IElementManager#getElementHandler(java.lang.String)
	 */
	@Override
	public IElementHandler getElementHandler(String id) {
		return elementHandlers.get(id);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ptp.ui.IElementManager#getImage(int, boolean)
	 */
	/**
	 * @since 7.0
	 */
	@Override
	public abstract Image getImage(int index, boolean isSelected);

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ptp.ui.IElementManager#removeElementHandler(java.lang.String)
	 */
	@Override
	public void removeElementHandler(String id) {
		elementHandlers.remove(id);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ptp.ui.IElementManager#removeFromSet(org.eclipse.ptp.ui.model .IElement[], java.lang.String,
	 * org.eclipse.ptp.ui.model.IElementHandler)
	 */
	/**
	 * @since 7.0
	 */
	@Override
	public void removeFromSet(BitSet elements, String setID, IElementHandler elementHandler) {
		IElementSet set = elementHandler.getSet(setID);
		set.removeElements(elements);
		fireSetEvent(REMOVE_ELEMENT_TYPE, elements, set, null);
		if (set.size() == 0) {
			removeSet(setID, elementHandler);
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ptp.ui.IElementManager#removeSet(java.lang.String, org.eclipse.ptp.ui.model.IElementHandler)
	 */
	@Override
	public void removeSet(String setID, IElementHandler elementHandler) {
		IElementSet set = elementHandler.removeSet(setID);
		fireSetEvent(DELETE_SET_TYPE, null, set, null);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ptp.ui.IElementManager#setElementHandler(java.lang.String, org.eclipse.ptp.ui.model.IElementHandler)
	 */
	@Override
	public void setElementHandler(String id, IElementHandler handler) {
		elementHandlers.put(id, handler);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ptp.ui.IElementManager#shutdown()
	 */
	@Override
	public void shutdown() {
	}
}
