package org.eclipse.ptp.core;

import java.util.Collection;
import org.eclipse.search.ui.ISearchPageScoreComputer;

/**
 * @author Clement
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
