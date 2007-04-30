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
import org.eclipse.ptp.ui.listeners.IJobChangedListener;
import org.eclipse.ptp.ui.listeners.ISetListener;
import org.eclipse.ptp.ui.model.IElement;
import org.eclipse.ptp.ui.model.IElementHandler;
import org.eclipse.ptp.ui.model.IElementSet;

/**
 * @author clement chu
 *
 */
public interface IManager {
	public static final String EMPTY_ID = "";
	public static final int CREATE_SET_TYPE = 0;
	public static final int DELETE_SET_TYPE = 1;
	public static final int CHANGE_SET_TYPE = 2;
	public static final int ADD_ELEMENT_TYPE = 3;
	public static final int REMOVE_ELEMENT_TYPE = 4;	
	
	public void shutdown();
	/** Get element size
	 * @return size of element
	 */
	public int size();
	/**
	 * clean all setting
	 */
	public void clear();
	/** initial elements
	 * @return first element name
	 */
	public IPElement initial();
	/** Get IElementHandler
	 * @param id element ID
	 * @return IElementHandler
	 */
	public IElementHandler getElementHandler(String id);
	/** Get name of element
	 * @param id element ID
	 * @return name of element
	 */
	public String getName(String id);
	
	/** Get fully qualified name of element
	 * @param id element ID
	 * @return fully quallified name of element
	 */
	public String getFullyQualifiedName(String id);

	/** Get current set ID
	 * @return current set ID
	 */
	public String getCurrentSetId();
	/** set current set ID
	 * @param set_id set ID
	 */
	public void setCurrentSetId(String set_id);
	
	//set listener
	/** Add Set Listener
	 * @param setListener ISetListener
	 */
	public void addSetListener(ISetListener setListener);
	/** Remove Set Listener
	 * @param setListener ISetListener
	 */
	public void removeSetListener(ISetListener setListener);
	/** Fire Event for set change
	 * @param eventType the type of event
	 * @param elements the selected elements
	 * @param cur_set the current set
	 * @param pre_set the previous set
	 */
	public void fireSetEvent(int eventType, IElement[] elements, IElementSet cur_set, IElementSet pre_set);
	/** Add elements to set
	 * @param elements selected elements
	 * @param set Set
	 */
	public void addToSet(IElement[] elements, IElementSet set);
	/** Add elements to set
	 * @param elements selected elements
	 * @param setID set ID
	 * @param elementHandler IElementHandler
	 */
	public void addToSet(IElement[] elements, String setID, IElementHandler elementHandler);
	/** Create a Set
	 * @param elements selected elements
	 * @param setID set ID
	 * @param setName set Name
	 * @param elementHandler IElementHandler
	 * @return set ID
	 */
	public String createSet(IElement[] elements, String setID, String setName, IElementHandler elementHandler);
	/** Remove Set
	 * @param setID set ID
	 * @param elementHandler IElementHandler
	 */
	public void removeSet(String setID, IElementHandler elementHandler);
	/** Remove elements in specific Set
	 * @param elements selected elements
	 * @param setID set ID
	 * @param elementHandler IElementHandler
	 */
	public void removeFromSet(IElement[] elements, String setID, IElementHandler elementHandler);
	/** Update elements  
	 * @param targetSet Target set
	 * @param elementHandler IElementHandler
	 */
	public void updateMatchElementSets(IElementSet targetSet, IElementHandler elementHandler);
	
	/** Add job listener
	 * @param jobListener
	 */
	public void addJobChangedListener(IJobChangedListener jobListener);
	/** Remove job listener
	 * @param jobListener
	 */
	public void removeJobChangedListener(IJobChangedListener jobListener);
	/** Fire job event when job is changed
	 * @param type job change type or remove type
	 * @param cur_jid
	 * @param pre_jid
	 */
	public void fireJobChangedEvent(int type, String cur_jid, String pre_jid);
	//job
	
	//image
	/** Get element status
	 * @param element IElement
	 * @return element status
	 */
	public int getStatus(IElement element);
	/** Get element status
	 * @param id Element ID
	 * @return element status
	 */
	public int getStatus(String id);
}
