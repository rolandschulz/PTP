package org.eclipse.ptp.core;

import java.util.Collection;
import org.eclipse.search.ui.ISearchPageScoreComputer;

/**
 * @author Clement
 *
 */
public interface IPElement extends ISearchPageScoreComputer {
    public String NAME_TAG = "";
    
    public static final int P_ROOT = 10;
    public static final int P_NODE = 11;
    public static final int P_PROCESS = 12;
    
    public String getElementName();
    public int getElementType();
    public boolean exists();
    public IPElement[] getChildren();
    
    public IPElement getParent();
    public IPRoot getPRoot();
    
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
