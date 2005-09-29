package org.eclipse.photran.internal.core.refactoring.preconditions;

import org.eclipse.photran.core.programrepresentation.Program;

public class NamesMustDiffer extends AbstractPrecondition
{
    protected String name1, name2;

    public NamesMustDiffer(Program program, String name1, String name2)
    {
        super(program);

        this.name1 = name1;
        this.name2 = name2;
    }

    public boolean checkThisPrecondition()
    {
        if (name1.toUpperCase().equals(name2.toUpperCase()))
        {
            error = "Names must differ";
            return false;
        }
        else
            return true;
    }
}
