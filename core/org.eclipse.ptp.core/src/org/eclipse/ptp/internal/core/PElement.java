package org.eclipse.ptp.internal.core;

import org.eclipse.core.runtime.PlatformObject;
import org.eclipse.ptp.core.IPElement;
import org.eclipse.ptp.core.IPRoot;
import org.eclipse.ptp.ui.UIUtils;
import org.eclipse.search.ui.ISearchPageScoreComputer;

/**
 * @author Clement
 *
 */
public abstract class PElement extends PlatformObject implements IPElement, Comparable {
    private IPElement fParent = null;
    private String fName = null;
    private int fType;
    private Object data = null;
    
    private PElementInfo elementInfo = null;

	protected PElement(IPElement parent, String name, int type) {
		fParent = parent;
		fName = name;
		fType = type;
	}
    
	protected PElementInfo getElementInfo() {
	    if (elementInfo == null)
	        elementInfo = new PElementInfo(this);
		return elementInfo;
	}
	
	public String getKey() {
	    return fName;
	}
	
    public String getElementName() {
        return NAME_TAG + getKey();
    }	
	
	public int getKeyNumber() {
	    try {
	        return Integer.parseInt(getKey());
	    } catch (NumberFormatException e) {
	        return -1;
	    }
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
	
	public IPRoot getPRoot() {
		IPElement current = this;
		do {
			if (current instanceof IPRoot) return (IPRoot) current;
		} while ((current = current.getParent()) != null);
		return null;
	}
	
	public boolean exists() {
		return getElementInfo() != null;
	}
	
	public int size() {
	    return getElementInfo().size();
	}
	
	public void setData(Object data) {
	    this.data = data;
	}
	
	public Object getData() {
	    return data;
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
		if (!UIUtils.PDT_SEARCHPAGE_ID.equals(pageId))
			return ISearchPageScoreComputer.UNKNOWN;
		
		if (element instanceof IPElement)
			return 90;
		
		return ISearchPageScoreComputer.LOWEST;
	}
}
