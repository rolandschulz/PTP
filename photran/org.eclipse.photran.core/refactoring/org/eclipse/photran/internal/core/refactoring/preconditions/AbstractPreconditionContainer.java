package org.eclipse.photran.internal.core.refactoring.preconditions;

import java.util.Iterator;
import java.util.List;

/**
 * Superclass for <code>Refactoring</code> and <code>Precondition</code>. Refactorings have
 * preconditions which must be met, and preconditions can have other prerequisite preconditions
 * which must first be met.
 * 
 * @author joverbey
 */
public abstract class AbstractPreconditionContainer
{
    protected String error = null;

    protected boolean checkPreconditions(List/* <Precondition> */preconditions)
    {
        Iterator/* <Precondition> */it = preconditions.iterator();
        while (it.hasNext())
        {
            AbstractPrecondition p = (AbstractPrecondition)it.next();
            if (!p.check())
            {
                error = p.getErrorMessage();
                return false;
            }
        }
        return true;
    }

    /**
     * Returns a user-comprehensible error message describing why the refactoring failed, or
     * <code>null</code> if it didn't.
     * 
     * Subclasses are not allowed to override this; they should set the <code>error</code> field (<code>String</code>)
     * instead.
     * 
     * @return error message
     */
    public final String getErrorMessage()
    {
        return error;
    }
}
