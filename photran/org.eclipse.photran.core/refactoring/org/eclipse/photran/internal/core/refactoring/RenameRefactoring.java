package org.eclipse.photran.internal.core.refactoring;

import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

import org.eclipse.photran.core.programrepresentation.Program;
import org.eclipse.photran.internal.core.f95parser.Token;
import org.eclipse.photran.internal.core.f95parser.symboltable.SymbolTable;
import org.eclipse.photran.internal.core.f95parser.symboltable.SymbolTableEntry;
import org.eclipse.photran.internal.core.programeditor.ProgramEditor;
import org.eclipse.photran.internal.core.refactoring.preconditions.IsLocalVariableEntry;
import org.eclipse.photran.internal.core.refactoring.preconditions.IsMainProgramEntry;
import org.eclipse.photran.internal.core.refactoring.preconditions.IsValidIdentifier;
import org.eclipse.photran.internal.core.refactoring.preconditions.NameDoesNotExistInScopeOf;
import org.eclipse.photran.internal.core.refactoring.preconditions.NamesMustDiffer;
import org.eclipse.photran.internal.core.refactoring.preconditions.Or;
import org.eclipse.photran.internal.core.refactoring.preconditions.TokenIsInSymbolTable;

/**
 * A rename refactoring for Fortran.
 * 
 * TODO-Jeff: Only works on local variables and main programs. Subprograms need to have references
 * detected in files other than the original source file. Also, derived type component names are not
 * handled correctly in the symbol table.
 * 
 * @author joverbey
 */
public class RenameRefactoring extends FortranRefactoring
{
    protected TokenIsInSymbolTable tokenIsInSymTblPrecondition;

    protected Token tokenToRename;

    protected String newName;

    public RenameRefactoring(Program program, Token tokenToRename)
    {
        super(program);

        final String notRenameableErrorMsg = tokenToRename.getText() + " cannot be renamed: "
            + "It is not a local variable, subprogram, or main program.";

        this.tokenToRename = tokenToRename;

        initialPreconditions.add(tokenIsInSymTblPrecondition = new TokenIsInSymbolTable(program,
            tokenToRename));
        initialPreconditions.add(
            new Or(
                // Symbol table entries will be retrieved from tokenIsInSymTblPrecondition
                new IsLocalVariableEntry(program, tokenIsInSymTblPrecondition),
                //new IsSubprogramEntry(program, tokenIsInSymTblPrecondition),
                new IsMainProgramEntry(program, tokenIsInSymTblPrecondition),

                notRenameableErrorMsg));
    }

    public void setNewName(String nm)
    {
        if (!initialPreconditionsCheckedAndPassed)
            throw new Error("Must check and pass initial preconditions before setting new name");

        SymbolTable symTblOfToken = tokenIsInSymTblPrecondition.getSymTblEntryForToken()
            .getTableContainingEntry();

        newName = nm;
        
        // TODO: Warn if keyword

        finalPreconditions.add(new IsValidIdentifier(program, newName));
        finalPreconditions.add(new NamesMustDiffer(program, tokenToRename.getText(), newName));
        finalPreconditions.add(new NameDoesNotExistInScopeOf(program, newName, symTblOfToken));
    }

    public boolean perform()
    {
        if (newName == null) throw new Error("Must set new name before performing refactoring");
        if (!finalPreconditionsCheckedAndPassed)
            throw new Error("Must check and pass final preconditions before performing refactoring");

        SymbolTableEntry symTblEntry = tokenIsInSymTblPrecondition.getSymTblEntryForToken();

        List/* <Token> */tokensToChange = new LinkedList();
        tokensToChange.add(symTblEntry.getIdentifier());
        tokensToChange.addAll(symTblEntry.getReferences());

        Iterator/* <Token> */it = tokensToChange.iterator();
        while (it.hasNext())
            ProgramEditor.changeTokenText(program, (Token)it.next(), newName);

        return rebuildSymbolTable();
    }
}
