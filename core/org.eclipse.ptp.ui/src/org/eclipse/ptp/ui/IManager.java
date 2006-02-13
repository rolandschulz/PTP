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
	public int size();
	public void clear();
	public String initial();
	public IElementHandler getElementHandler(String id);
	public String getName(String id);
	
	public String getCurrentSetId();
	public void setCurrentSetId(String set_id);
	
	//set listener
	public void addSetListener(ISetListener setListener);
	public void removeSetListener(ISetListener setListener);
	public void fireEvent(int eventType, IElement[] elements, IElementSet cur_set, IElementSet pre_set);
	public void addToSet(IElement[] elements, IElementSet set);
	public void addToSet(IElement[] elements, String setID, IElementHandler elementHandler);
	public String createSet(IElement[] elements, String setID, String setName, IElementHandler elementHandler);
	public void removeSet(String setID, IElementHandler elementHandler);
	public void removeFromSet(IElement[] elements, String setID, IElementHandler elementHandler);
	public void updateMatchElementSets(IElementSet targetSet, IElementHandler elementHandler);
	
	//paint listener
	public void addPaintListener(IPaintListener pListener);
	public void removePaintListener(IPaintListener pListener);
	public void firePaintListener(Object condition);
	//job
	public boolean isNoJob(String jid);
	public boolean isJobStop(String job_id);
	public IPJob findJob(String job_name);
	public IPJob findJobById(String job_id);
	public void removeJob(IPJob job);
	public void removeAllStoppedJobs();
	public boolean hasStoppedJob();	
	
	//image
	public int getStatus(IElement element);
	public int getStatus(String id);
}
