package org.eclipse.photran.internal.core.f95parser.symboltable.entries;

import org.eclipse.photran.internal.core.f95parser.Nonterminal;
import org.eclipse.photran.internal.core.f95parser.ParseTreeNode;
import org.eclipse.photran.internal.core.f95parser.Token;
import org.eclipse.photran.internal.core.f95parser.symboltable.SymbolTable;
import org.eclipse.photran.internal.core.f95parser.symboltable.SymbolTableEntry;
import org.eclipse.photran.internal.core.f95parser.symboltable.SymbolTableError;
import org.eclipse.photran.internal.core.f95parser.symboltable.SymbolTableVisitor;

/**
 * Symbol table entry for a derived type
 * 
 * @author joverbey
 */
public class DerivedTypeEntry extends SymbolTableEntry
{
    public DerivedTypeEntry(SymbolTable parentTable, Token identifier,
        ParseTreeNode correspondingParseTreeNode)
    {
        super(parentTable, identifier, correspondingParseTreeNode);
        
        if (correspondingParseTreeNode.getRootNonterminal() != Nonterminal.XDERIVEDTYPEDEF)
            throw new SymbolTableError("The ParseTreeNode passed to the DerivedTypeEntry constructor should be an xDerivedTypeDef");
    }

    public String getTypeDescription()
    {
        StringBuffer sb = new StringBuffer();
        sb.append("Derived Type");
        if (isPrivate)
            sb.append(", private");
        if (isSequence)
            sb.append(", sequence");
        return sb.toString();
    }

    public void visitEntryUsing(SymbolTableVisitor visitor)
    {
        visitor.visit(this);
    }

    protected boolean isPrivate = false;

    public boolean isPrivate()
    {
        return isPrivate;
    }

    public void setPrivate(boolean isPrivate)
    {
        this.isPrivate = isPrivate;
    }

    protected boolean isSequence = false;

    public boolean isSequence()
    {
        return isSequence;
    }

    public void setSequence(boolean isSequence)
    {
        this.isSequence = isSequence;
    }
}
