package org.eclipse.photran.internal.core.refactoring.preconditions;

import org.eclipse.photran.core.programrepresentation.Program;
import org.eclipse.photran.internal.core.f95parser.symboltable.SymbolTable;

/**
 * Precondition specifying that a given name is not defined in the same scope or a larger scope than
 * that of the given node.
 * 
 * @author joverbey
 */
public class NameDoesNotExistInScopeOf extends AbstractPrecondition
{
    protected String name;

    protected SymbolTable smallestScope;

    public NameDoesNotExistInScopeOf(Program program, String name, SymbolTable smallestScope)
    {
        super(program);
        this.name = name;
        this.smallestScope = smallestScope;
    }

    protected boolean checkThisPrecondition()
    {
        boolean nameIsDefined = smallestScope.hierarchyContainsOrImportsEntryFor(name);

        if (nameIsDefined)
        {
            error = name + " is already defined in an outer scope";
            return false;
        }
        else
            return true;
    }
}
