package org.eclipse.photran.internal.core.refactoring.preconditions;

import java.util.regex.Pattern;

import org.eclipse.photran.core.programrepresentation.Program;

public class IsValidIdentifier extends AbstractPrecondition
{
    protected String name;

    public IsValidIdentifier(Program program, String name)
    {
        super(program);

        this.name = name;
    }

    public boolean checkThisPrecondition()
    {
        return Pattern.matches("[A-Za-z][A-Za-z0-9_]*", name);
    }
}
