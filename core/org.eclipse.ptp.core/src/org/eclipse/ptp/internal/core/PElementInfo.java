package org.eclipse.ptp.internal.core;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import org.eclipse.ptp.core.IPElement;

/**
 * @author Clement
 *
 */
public class PElementInfo {
    private Map fChildren = null;
    protected PElement element;
    
    public PElementInfo(PElement element) {
		this.element = element;
		// Array list starts with size = 0
		fChildren = new HashMap(0);        
    }
    
	public PElement getElement() {
		return element;
	}    
    
	public void addChild(IPElement member) {
	    fChildren.put(member.getKey(), member);
	}

	public void removeChild(IPElement member) {
	    fChildren.remove(member.getKey());
	}
	
	public IPElement findChild(String key) {
	    if(fChildren.containsKey(key))
	        return (IPElement)fChildren.get(key);
	    return null;
	}

	public IPElement[] getChildren() {
		synchronized (fChildren) {
			return (IPElement[]) fChildren.values().toArray( new IPElement[size()] );
		}
	}
	
	public Collection getCollection() {
	    synchronized (fChildren) {
	        return fChildren.values();
	    }
	}
		
	public boolean includesChild(IPElement child) {
		if(fChildren.containsKey(child.getKey()))
			return true;
		return false;
	}	
	
	public void removeChildren() {
	    fChildren.clear();
	}
	
	public void setChildren(Map children) {
		fChildren.putAll(children);
	}

	public boolean hasChildren() {
		return size() > 0;
	}
	
	public int size() {
	    return fChildren.size();
	}
	
	public Object clone() {
		try {
			return super.clone();
		} catch (CloneNotSupportedException e) {
			throw new Error();
		}
	}    
}
