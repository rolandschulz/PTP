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
package org.eclipse.ptp.core;

import java.util.Collection;
import org.eclipse.search.ui.ISearchPageScoreComputer;

/**
 *
 */
public interface IPElement extends ISearchPageScoreComputer {
    public String NAME_TAG = "";
    
    public static final int P_UNIVERSE = 10;
    public static final int P_MACHINE = 11;
    public static final int P_NODE = 12;
    public static final int P_JOB = 13;
    public static final int P_PROCESS = 14;
    
    public String getElementName();
    public int getElementType();
    public boolean exists();
    public IPElement[] getChildren();
    
    public IPElement getParent();
    /*
    public IPUniverse getPUniverse();
    public IPMachine getPMachine();
    public IPJob getPRoot();
    */
    
    public void addChild(IPElement member);
	public void removeChild(IPElement member);
	public void removeChildren();
	public IPElement findChild(String elementName);
	
	public int size();
	
    public boolean isAllStop();	
    
	public void setData(Object data);
	public Object getData();
	
	public IPElement[] getSortedChildren();
	
	public String getKey();
	public int getKeyNumber();	
	public Collection getCollection();
	public boolean hasChildren();
}
