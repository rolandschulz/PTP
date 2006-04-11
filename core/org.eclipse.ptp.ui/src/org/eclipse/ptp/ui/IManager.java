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

import org.eclipse.ptp.core.IPJob;
import org.eclipse.ptp.ui.listeners.IPaintListener;
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
	public String initial();
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
	public void fireEvent(int eventType, IElement[] elements, IElementSet cur_set, IElementSet pre_set);
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
	
	//paint listener
	/** Add Paint listener
	 * @param pListener IPaintListener
	 */
	public void addPaintListener(IPaintListener pListener);
	/** Remove Paint listener
	 * @param pListener IPaintListener
	 */
	public void removePaintListener(IPaintListener pListener);
	/** Fire Paint listener
	 * @param condition paint or not
	 */
	public void firePaintListener(Object condition);
	//job
	/** Check is job existed
	 * @param jid Job ID
	 * @return true job ID is existed otherwise not existed
	 */
	public boolean isNoJob(String jid);
	/** Check the job is stopped
	 * @param job_id Job ID
	 * @return true job is stopped
	 */
	public boolean isJobStop(String job_id);
	/** Search a job by job name
	 * @param job_name Job name
	 * @return null if no job found
	 */
	public IPJob findJob(String job_name);
	/** Search a job by job ID
	 * @param job_id Job ID
	 * @return null if no job found
	 */
	public IPJob findJobById(String job_id);
	/** Remove Job
	 * @param job Job
	 */
	public void removeJob(IPJob job);
	/** Remove all stopped jobs
	 * 
	 */
	public void removeAllStoppedJobs();
	/** Check whether there is a job stopped
	 * @return true if there is a job stopped
	 */
	public boolean hasStoppedJob();	
	
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
