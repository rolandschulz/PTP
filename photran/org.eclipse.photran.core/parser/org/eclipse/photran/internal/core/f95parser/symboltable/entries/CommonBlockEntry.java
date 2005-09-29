package org.eclipse.photran.internal.core.f95parser.symboltable.entries;

import org.eclipse.photran.internal.core.f95parser.Nonterminal;
import org.eclipse.photran.internal.core.f95parser.ParseTreeNode;
import org.eclipse.photran.internal.core.f95parser.Token;
import org.eclipse.photran.internal.core.f95parser.symboltable.SymbolTable;
import org.eclipse.photran.internal.core.f95parser.symboltable.SymbolTableEntry;
import org.eclipse.photran.internal.core.f95parser.symboltable.SymbolTableError;
import org.eclipse.photran.internal.core.f95parser.symboltable.SymbolTableVisitor;

/**
 * Symbol table entry for COMMON blocks
 * 
 * NOTE: The identifier token may not actually exist in the program, since
 * common blocks do not have to be named
 * 
 * @author joverbey
 */
public class CommonBlockEntry extends SymbolTableEntry
{
    public CommonBlockEntry(SymbolTable parentTable, Token identifier, ParseTreeNode correspondingParseTreeNode)
    {
        super(parentTable, identifier, correspondingParseTreeNode);
        
        if (correspondingParseTreeNode.getRootNonterminal() != Nonterminal.XCOMBLOCK)
            throw new SymbolTableError("The ParseTreeNode passed to the CommonBlockEntry constructor should be an xComBlock");
    }

    public String getTypeDescription()
    {
        return "Common Block";
    }

    public void visitEntryUsing(SymbolTableVisitor visitor)
    {
        visitor.visit(this);
    }
}
