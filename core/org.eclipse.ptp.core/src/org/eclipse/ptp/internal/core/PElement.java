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
package org.eclipse.ptp.internal.core;

import org.eclipse.core.runtime.PlatformObject;
import org.eclipse.ptp.core.IPElement;
import org.eclipse.ptp.core.IPUniverse;
import org.eclipse.ptp.core.IPJob;
import org.eclipse.ptp.core.IPMachine;
import org.eclipse.ptp.internal.core.CoreUtils;
import org.eclipse.search.ui.ISearchPageScoreComputer;

/**
 *
 */
public abstract class PElement extends PlatformObject implements IPElement, Comparable {
    private IPElement fParent = null;
    private String fName = null;
    private String fKey = null;
    private int fType;
    
    private PElementInfo elementInfo = null;

	protected PElement(IPElement parent, String name, String key, int type) {
		fParent = parent;
		fName = name;
		fKey = key;
		fType = type;
	}
    
	protected PElementInfo getElementInfo() {
	    if (elementInfo == null)
	        elementInfo = new PElementInfo(this);
		return elementInfo;
	}
	
	/*
	public String getKey() {
	    return fKey;
	}
	*/
	
    public String getElementName() {
        //return NAME_TAG + getKey();
    		return fName;
    }	
	
	public int getKeyNumber() {
	    try {
	        return Integer.parseInt(fKey);
	    } catch (NumberFormatException e) {
	        return -1;
	    }
	}
	
	public String getKeyString() {
		return fKey;
	}
	
    /**
     * @param name The fName to set.
     */
    public void setElementName(String name) {
        fName = name;
    }
    /**
     * @return Returns the Parent.
     */
    public IPElement getParent() {
        return fParent;
    }
    /**
     * @param parent The fParent to set.
     */
    public void setParent(IPElement parent) {
        fParent = parent;
    }
    /**
     * @return Returns the fType.
     */
    public int getElementType() {
        return fType;
    }
    /**
     * @param type The fType to set.
     */
    public void setElementType(int type) {
        fType = type;
    }
    
	public String toString() {
		return getElementName();
	}
	
	public int size() {
	    return getElementInfo().size();
	}
	
	public int compareTo(Object obj) {
		if(obj instanceof IPElement) {
			int my_rank = getKeyNumber();
			int his_rank = ((IPElement)obj).getKeyNumber();
			if(my_rank < his_rank) return -1;
			if(my_rank == his_rank) return 0;
			if(my_rank > his_rank) return 1;
		}
		return 0;
	}
	
	public int computeScore(String pageId, Object element) {
		if (!CoreUtils.PTP_SEARCHPAGE_ID.equals(pageId))
			return ISearchPageScoreComputer.UNKNOWN;
		
		if (element instanceof IPElement)
			return 90;
		
		return ISearchPageScoreComputer.LOWEST;
	}
}
