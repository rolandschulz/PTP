package org.eclipse.photran.internal.core.f95parser.symboltable.entries;

import org.eclipse.photran.internal.core.f95parser.Nonterminal;
import org.eclipse.photran.internal.core.f95parser.ParseTreeNode;
import org.eclipse.photran.internal.core.f95parser.Token;
import org.eclipse.photran.internal.core.f95parser.symboltable.SymbolTable;
import org.eclipse.photran.internal.core.f95parser.symboltable.SymbolTableEntry;
import org.eclipse.photran.internal.core.f95parser.symboltable.SymbolTableError;
import org.eclipse.photran.internal.core.f95parser.symboltable.SymbolTableVisitor;

/**
 * Symbol table entry for names specified as INTRINSIC
 * 
 * Parse tree node will be <code>null</code> for built-in intrinsics.
 * 
 * @author joverbey
 */
public class IntrinsicEntry extends SymbolTableEntry
{
    public IntrinsicEntry(SymbolTable parentTable, Token identifier, ParseTreeNode correspondingParseTreeNode)
    {
        super(parentTable, identifier, correspondingParseTreeNode);
        
        if (correspondingParseTreeNode != null && correspondingParseTreeNode.getRootNonterminal() != Nonterminal.XINTRINSICPROCEDURENAME)
            throw new SymbolTableError("The ParseTreeNode passed to the IntrinsicEntry constructor should be an xIntrinsicProcedureName");
    }

    public String getTypeDescription()
    {
        return "Intrinsic";
    }

    public void visitEntryUsing(SymbolTableVisitor visitor)
    {
        visitor.visit(this);
    }
}
