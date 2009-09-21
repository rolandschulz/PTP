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
package org.eclipse.ptp.ui;

import org.eclipse.ptp.core.elements.IPElement;
import org.eclipse.ptp.core.elements.IPUniverse;
import org.eclipse.ptp.ui.listeners.ISetListener;
import org.eclipse.ptp.ui.model.IElement;
import org.eclipse.ptp.ui.model.IElementHandler;
import org.eclipse.ptp.ui.model.IElementSet;
import org.eclipse.swt.graphics.Image;

/**
 * @author clement chu
 *
 */
public interface IElementManager {
	public static final String EMPTY_ID = ""; //$NON-NLS-1$
	public static final int CREATE_SET_TYPE = 0;
	public static final int DELETE_SET_TYPE = 1;
	public static final int CHANGE_SET_TYPE = 2;
	public static final int ADD_ELEMENT_TYPE = 3;
	public static final int REMOVE_ELEMENT_TYPE = 4;	
	
	/** 
	 * Add Set Listener
	 * 
	 * @param setListener ISetListener
	 */
	public void addSetListener(ISetListener setListener);
	
	/** 
	 * Add elements to set
	 * 
	 * @param elements selected elements
	 * @param set Set
	 */
	public void addToSet(IElement[] elements, IElementSet set);
	
	/** 
	 * Add elements to set
	 * 
	 * @param elements selected elements
	 * @param setID set ID
	 * @param elementHandler IElementHandler
	 */
	public void addToSet(IElement[] elements, String setID, IElementHandler elementHandler);
	
	/**
	 * clean all setting
	 */
	public void clear();
	
	/** 
	 * Create a Set
	 * 
	 * @param elements selected elements
	 * @param setID set ID
	 * @param setName set Name
	 * @param elementHandler IElementHandler
	 * @return set ID
	 */
	public String createSet(IElement[] elements, String setID, String setName, IElementHandler elementHandler);
	
	/** 
	 * Fire Event for set change
	 * 
	 * @param eventType the type of event
	 * @param elements the selected elements
	 * @param cur_set the current set
	 * @param pre_set the previous set
	 */
	public void fireSetEvent(int eventType, IElement[] elements, IElementSet cur_set, IElementSet pre_set);

	/** 
	 * Get current set ID
	 * 
	 * @return current set ID
	 */
	public String getCurrentSetId();
	
	/** 
	 * Get element handler
	 * 
	 * @param id element ID
	 * @return IElementHandler
	 */
	public IElementHandler getElementHandler(String id);
	
	/** 
	 * Get fully qualified name of element
	 * 
	 * @param id element ID
	 * @return fully quallified name of element
	 */
	public String getFullyQualifiedName(String id);

	/** 
	 * Get element image
	 * 
	 * @param element IElement
	 * @return element image
	 */
	public Image getImage(IElement element);
	
	/** 
	 * Get name of element
	 * 
	 * @param id element ID
	 * @return name of element
	 */
	public String getName(String id);
	
	/** 
	 * initial elements
	 * 
	 * @return first element name
	 */
	public IPElement initial(IPUniverse universe);
	
	/**
	 * Remove the element handler
	 * 
	 * @param id element handler to remove
	 */
	public void removeElementHandler(String id);
	
	/** 
	 * Remove elements in specific Set
	 * 
	 * @param elements selected elements
	 * @param setID set ID
	 * @param elementHandler IElementHandler
	 */
	public void removeFromSet(IElement[] elements, String setID, IElementHandler elementHandler);
	
	/** 
	 * Remove Set
	 * 
	 * @param setID set ID
	 * @param elementHandler IElementHandler
	 */
	public void removeSet(String setID, IElementHandler elementHandler);
	
	/** 
	 * Remove Set Listener
	 * 
	 * @param setListener ISetListener
	 */
	public void removeSetListener(ISetListener setListener);
	
	/** 
	 * set current set ID
	 * 
	 * @param set_id set ID
	 */
	public void setCurrentSetId(String set_id);
	
	/** 
	 * Set element handler
	 * 
	 * @param id element ID
	 * param IElementHandler
	 */
	public void setElementHandler(String id, IElementHandler handler);
	
	/**
	 * Shutdown the manager
	 */
	public void shutdown();
	
	/** 
	 * Get element size
	 * 
	 * @return size of element
	 */
	public int size();
}
