package org.eclipse.photran.core.util;

/**
 * Visitors can throw a <code>Notification</code> exception to abort a traversal while "remembering"
 * a specific object (e.g., <code>Token</code> or <code>ParseTreeNode</code>) that was located during the traversal.
 *  
 * @author Jeff Overbey
 */
public class Notification extends Error
{
    private static final long serialVersionUID = 1L;

    private Object t;
    
    public Notification(Object t) { this.t = t; }
    public Object getResult() { return t; }
}
