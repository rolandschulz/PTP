package org.eclipse.photran.internal.core.refactoring.preconditions;

import org.eclipse.photran.core.programrepresentation.Program;
import org.eclipse.photran.internal.core.f95parser.symboltable.SymbolTableEntry;
import org.eclipse.photran.internal.core.f95parser.symboltable.entries.AbstractSubprogramEntry;
import org.eclipse.photran.internal.core.f95parser.symboltable.entries.MainProgramEntry;
import org.eclipse.photran.internal.core.f95parser.symboltable.entries.VariableEntry;

/**
 * Precondition specifying that the <code>SymbolTableEntry</code> returned by a
 * <code>TokenIsInSymbolTable</code> precondition must be a local variable.
 * 
 * Since this involves searching the program's symbol table for a <code>SymbolTableEntry</code>
 * corresponding to this token, the entry (if found) is stored and made accessible via
 * <code>getSymTblEntry</code> so that the search does not have to be done again.
 * 
 * @author joverbey
 */
public class IsLocalVariableEntry extends AbstractPrecondition
{
    private TokenIsInSymbolTable tokenIsInSymTblPrecondition;

    public IsLocalVariableEntry(Program program, TokenIsInSymbolTable tokenIsInSymTblPrecondition)
    {
        super(program);
        this.tokenIsInSymTblPrecondition = tokenIsInSymTblPrecondition;

        prereqPreconditions.add(tokenIsInSymTblPrecondition);
    }

    protected boolean checkThisPrecondition()
    {
        SymbolTableEntry symTblEntry = tokenIsInSymTblPrecondition.getSymTblEntryForToken();

        if (!(symTblEntry instanceof VariableEntry))
        {
            error = symTblEntry.getIdentifier().getText()
                + " is not a local variable.\n\nError: Symbol table entry is a "
                + symTblEntry.getClass().getName() + ", not a VariableEntry";
            return false;
        }

        if (((VariableEntry)symTblEntry).isFunctionOrSubroutineParameter())
        {
            error = symTblEntry.getIdentifier().getText()
                + " is not a local variable.\n\nError: Variable is a subprogram parameter, not a local variable";
            return false;
        }

        SymbolTableEntry symTblParentEntry = tokenIsInSymTblPrecondition
            .getSymTblEntryForParentOfToken();
        if (symTblParentEntry == null)
        {
            error = symTblEntry.getIdentifier().getText()
                + " is not a local variable.\n\nError: Symbol table entry does not have a parent or parent entry";
            return false;
        }

        if (!(symTblParentEntry instanceof MainProgramEntry)
            && !(symTblParentEntry instanceof AbstractSubprogramEntry))
        {
            error = symTblEntry.getIdentifier().getText()
                + " is not a local variable.\n\nError: Symbol table entry is not contained in a main program, function, or subroutine";
            return false;
        }

        return true;
    }
}
