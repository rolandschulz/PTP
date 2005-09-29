package org.eclipse.photran.internal.core.refactoring;

import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

import org.eclipse.photran.core.programrepresentation.Program;
import org.eclipse.photran.internal.core.f95parser.Nonterminal;
import org.eclipse.photran.internal.core.f95parser.ParseTreeNode;
import org.eclipse.photran.internal.core.f95parser.ParseTreeSearcher;
import org.eclipse.photran.internal.core.f95parser.Token;
import org.eclipse.photran.internal.core.f95parser.symboltable.SymbolTable;
import org.eclipse.photran.internal.core.f95parser.symboltable.SymbolTableEntry;
import org.eclipse.photran.internal.core.programeditor.ProgramEditor;
import org.eclipse.photran.internal.core.refactoring.preconditions.IsSubprogramEntry;
import org.eclipse.photran.internal.core.refactoring.preconditions.IsValidIdentifier;
import org.eclipse.photran.internal.core.refactoring.preconditions.NameDoesNotExistInScopeOf;
import org.eclipse.photran.internal.core.refactoring.preconditions.NamesMustDiffer;
import org.eclipse.photran.internal.core.refactoring.preconditions.ParseTreeNodeHasType;
import org.eclipse.photran.internal.core.refactoring.preconditions.TokenIsInSymbolTable;

/**
 * Refactoring to replace implicit variables with explicitly declared variables.
 * 
 * TODO-Jeff: Only works on local variables and main programs. Subprograms need to have references
 * detected in files other than the original source file. Also, derived type component names are not
 * handled correctly in the symbol table.
 * 
 * @author spiros
 * @author joverbey
 */
public class IntroduceImplicitNoneRefactoring extends FortranRefactoring
{
    protected TokenIsInSymbolTable tokenIsInSymTblPrecondition;
    
    public IntroduceImplicitNoneRefactoring(Program program, ParseTreeNode functionSubprogramNode)
    {
        super(program);
        
        initialPreconditions.add(new ParseTreeNodeHasType(functionSubprogramNode, Nonterminal.XFUNCTIONSUBPROGRAM));
        
        Token functionNameToken = ParseTreeSearcher.findFirstIdentifierIn(functionSubprogramNode);
        initialPreconditions.add(tokenIsInSymTblPrecondition = new TokenIsInSymbolTable(program, functionNameToken));
        initialPreconditions.add(new IsSubprogramEntry(program, tokenIsInSymTblPrecondition));
    }

    public boolean perform()
    {
        if (initialPreconditionsCheckedAndPassed)
            throw new Error("Must check and pass initial preconditions before performing refactoring");

        SymbolTableEntry symTblEntry = tokenIsInSymTblPrecondition.getSymTblEntryForToken();

        //TODO

        return rebuildSymbolTable();
    }
}
