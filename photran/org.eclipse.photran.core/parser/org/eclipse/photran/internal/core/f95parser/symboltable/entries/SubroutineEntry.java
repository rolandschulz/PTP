package org.eclipse.photran.internal.core.f95parser.symboltable.entries;

import org.eclipse.photran.internal.core.f95parser.Nonterminal;
import org.eclipse.photran.internal.core.f95parser.ParseTreeNode;
import org.eclipse.photran.internal.core.f95parser.Token;
import org.eclipse.photran.internal.core.f95parser.symboltable.SymbolTable;
import org.eclipse.photran.internal.core.f95parser.symboltable.SymbolTableError;
import org.eclipse.photran.internal.core.f95parser.symboltable.SymbolTableVisitor;

/**
 * Symbol table entry for a subroutine
 * 
 * The parameters are stored in the subroutine's child symbol table
 * alongside its local variables.
 * 
 * @author joverbey
 */
public class SubroutineEntry extends AbstractSubprogramEntry
{
    public SubroutineEntry(SymbolTable parentTable, Token identifier, ParseTreeNode correspondingParseTreeNode)
    {
        super(parentTable, identifier, correspondingParseTreeNode);
        
        if (correspondingParseTreeNode.getRootNonterminal() != Nonterminal.XSUBROUTINESUBPROGRAM)
            throw new SymbolTableError("The ParseTreeNode passed to the SubroutineEntry constructor should be an xSubroutineSubprogram");
    }

    public String getTypeDescription()
    {
        return "Subroutine" + describeInheritedFields();
    }

    public void visitEntryUsing(SymbolTableVisitor visitor)
    {
        visitor.visit(this);
    }
}
