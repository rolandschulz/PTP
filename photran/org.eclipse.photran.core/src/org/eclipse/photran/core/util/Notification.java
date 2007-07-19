package org.eclipse.photran.core.util;

/**
 * Visitors can throw a <code>Notification</code> exception to abort a traversal while "remembering"
 * a specific object (e.g., <code>Token</code> or <code>ParseTreeNode</code>) that was located during the traversal
 * (called the <b>stored object</b>).  This object can be retrieved by the method requesting the traversal
 * by catching the <code>Notification</code> and calling its <code>getResult</code> method.
 *  
 * @author Jeff Overbey
 * 
 * LATER: Type-parameterize upon Core move to Java 5
 */
public class Notification extends Error
{
    private static final long serialVersionUID = 1L;

    private Object t;
    
    public Notification(Object t) { this.t = t; }
    
    /** @return the stored object */
    public Object getResult() { return t; }
}
