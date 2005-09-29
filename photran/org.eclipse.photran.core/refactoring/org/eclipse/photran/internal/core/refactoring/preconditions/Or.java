package org.eclipse.photran.internal.core.refactoring.preconditions;

import java.util.ArrayList;
import java.util.Iterator;

public class Or extends AbstractPrecondition
{
    protected ArrayList/* <AbstractPrecondition> */preconditions = new ArrayList();

    protected String errorMessage;

    public Or(AbstractPrecondition p1, AbstractPrecondition p2, String errorMessage)
    {
        super(null);

        preconditions.add(p1);
        preconditions.add(p2);

        this.errorMessage = errorMessage;
    }

    public Or(AbstractPrecondition p1, AbstractPrecondition p2, AbstractPrecondition p3,
        String errorMessage)
    {
        super(null);

        preconditions.add(p1);
        preconditions.add(p2);
        preconditions.add(p3);

        this.errorMessage = errorMessage;
    }

    public boolean checkThisPrecondition()
    {
        Iterator/* <Precondition> */it = preconditions.iterator();
        while (it.hasNext())
        {
            AbstractPrecondition p = (AbstractPrecondition)it.next();
            if (p.check()) return true;
        }

        error = errorMessage;
        return false;
    }
}
