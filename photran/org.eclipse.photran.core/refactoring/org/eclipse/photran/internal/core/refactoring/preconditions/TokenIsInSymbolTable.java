package org.eclipse.photran.internal.core.refactoring.preconditions;

import org.eclipse.photran.core.programrepresentation.Program;
import org.eclipse.photran.internal.core.f95parser.SymbolTableSearcher;
import org.eclipse.photran.internal.core.f95parser.Token;
import org.eclipse.photran.internal.core.f95parser.symboltable.SymbolTableEntry;

/**
 * Precondition specifying that a given <code>Token</code> must exist in the symbol table, either
 * as a definition or a reference.
 * 
 * Since this involves searching the program's symbol table for a <code>SymbolTableEntry</code>
 * corresponding to this token, the entry (if found) is stored and made accessible via
 * <code>getSymTblEntry</code> so that the search does not have to be done again.
 * 
 * @author joverbey
 */
public class TokenIsInSymbolTable extends AbstractPrecondition
{
    protected Token token;

    private SymbolTableEntry symTblEntry;

    private SymbolTableEntry symTblParentEntry = null;

    public TokenIsInSymbolTable(Program program, Token token)
    {
        super(program);
        this.token = token;

        prereqPreconditions.add(new TokenIsIdentifier(program, token));
    }

    protected boolean checkThisPrecondition()
    {
        // From prereqs, we know token is an identifier

        symTblEntry = SymbolTableSearcher.findEntryCorrespondingToIdentifier(symbolTable, token);
        if (symTblEntry == null)
        {
            error = token.getText() + " was not found in the symbol table";
            return false;
        }

        if (symTblEntry.getTableContainingEntry() != null)
            symTblParentEntry = symTblEntry.getTableContainingEntry().getParentEntry();

        return true;
    }

    public SymbolTableEntry getSymTblEntryForToken()
    {
        return (SymbolTableEntry)symTblEntry;
    }

    public SymbolTableEntry getSymTblEntryForParentOfToken()
    {
        return (SymbolTableEntry)symTblParentEntry;
    }
}
