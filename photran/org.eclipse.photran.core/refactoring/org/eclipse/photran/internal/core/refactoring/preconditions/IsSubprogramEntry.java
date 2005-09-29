package org.eclipse.photran.internal.core.refactoring.preconditions;

import org.eclipse.photran.core.programrepresentation.Program;
import org.eclipse.photran.internal.core.f95parser.symboltable.SymbolTableEntry;
import org.eclipse.photran.internal.core.f95parser.symboltable.entries.AbstractSubprogramEntry;

/**
 * Precondition specifying that the <code>SymbolTableEntry</code> returned by a
 * <code>TokenIsInSymbolTable</code> precondition must be a subprogram.
 * 
 * 
 * Since this involves searching the program's symbol table for a <code>SymbolTableEntry</code>
 * corresponding to this token, the entry (if found) is stored and made accessible via
 * <code>getSymTblEntry</code> so that the search does not have to be done again.
 * 
 * @author joverbey
 */
public class IsSubprogramEntry extends AbstractPrecondition
{
    private TokenIsInSymbolTable tokenIsInSymTblPrecondition;

    public IsSubprogramEntry(Program program, TokenIsInSymbolTable tokenIsInSymTblPrecondition)
    {
        super(program);
        this.tokenIsInSymTblPrecondition = tokenIsInSymTblPrecondition;

        prereqPreconditions.add(tokenIsInSymTblPrecondition);
    }

    protected boolean checkThisPrecondition()
    {
        SymbolTableEntry symTblEntry = tokenIsInSymTblPrecondition.getSymTblEntryForToken();

        if (!(symTblEntry instanceof AbstractSubprogramEntry))
        {
            error = symTblEntry.getIdentifier().getText()
                + " is not a subprogram.\n\nError: Symbol table entry is a "
                + symTblEntry.getClass().getName() + ", not a SubprogramEntry";
            return false;
        }

        return true;
    }
}
