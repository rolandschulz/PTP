package org.eclipse.photran.internal.core.f95parser.symboltable.entries;

import org.eclipse.photran.internal.core.f95parser.Nonterminal;
import org.eclipse.photran.internal.core.f95parser.ParseTreeNode;
import org.eclipse.photran.internal.core.f95parser.Token;
import org.eclipse.photran.internal.core.f95parser.symboltable.SymbolTable;
import org.eclipse.photran.internal.core.f95parser.symboltable.SymbolTableEntry;
import org.eclipse.photran.internal.core.f95parser.symboltable.SymbolTableError;
import org.eclipse.photran.internal.core.f95parser.symboltable.SymbolTableVisitor;

/**
 * Symbol table entry for names specified as EXTERNAL
 * 
 * @author joverbey
 */
public class ExternalEntry extends SymbolTableEntry
{
    public ExternalEntry(SymbolTable parentTable, Token identifier, ParseTreeNode correspondingParseTreeNode)
    {
        super(parentTable, identifier, correspondingParseTreeNode);
        
        if (correspondingParseTreeNode.getRootNonterminal() != Nonterminal.XEXTERNALNAME)
            throw new SymbolTableError("The ParseTreeNode passed to the ExternalEntry constructor should be an xExternalName");
    }

    public String getTypeDescription()
    {
        return "External";
    }

    public void visitEntryUsing(SymbolTableVisitor visitor)
    {
        visitor.visit(this);
    }
}
