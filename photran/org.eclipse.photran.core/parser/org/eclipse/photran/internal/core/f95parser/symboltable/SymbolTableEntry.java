package org.eclipse.photran.internal.core.f95parser.symboltable;

import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

import org.eclipse.photran.internal.core.f95parser.ParseTreeNode;
import org.eclipse.photran.internal.core.f95parser.Token;

/**
 * Superclass for all entries in a Fortran symbol table
 * 
 * @author joverbey
 * @see SymbolTable
 */
public abstract class SymbolTableEntry implements Cloneable
{
    /**
     * Creates an entry 
     * @param parentTable parent symbol table, not null
     * @param identifier which describes this entry, not null
     * @param correspondingParseTreeNode, possibly null
     */
    public SymbolTableEntry(SymbolTable parentTable, Token identifier, ParseTreeNode correspondingParseTreeNode)
    {
        this.parentTable = parentTable;
        this.identifier = identifier;
        this.correspondingParseTreeNode = correspondingParseTreeNode;
        this.childTable = new SymbolTable(parentTable, this);
    }

    protected Token identifier;

    public Token getIdentifier()
    {
        return identifier;
    }

    protected ParseTreeNode correspondingParseTreeNode;

    public ParseTreeNode getCorrespondingParseTreeNode()
    {
        return correspondingParseTreeNode;
    }

    protected Set/* <Token> */references = new HashSet();

    void addReference(Token token)
    {
        if (token != identifier)
            references.add(token);
    }

    public Collection/* <Token> */getReferences()
    {
        return references;
    }

    protected SymbolTable parentTable;
    
    /**
     * @return the <code>SymbolTable</code> containing this entry
     */
    public SymbolTable getTableContainingEntry()
    {
        return parentTable;
    }
    
    protected SymbolTable childTable;

    /**
     * @return the child symbol table for this table (possibly empty,
     * never <code>null</code>)
     */
    public SymbolTable getChildTable()
    {
        return childTable;
    }
    
    public final void visitUsing(SymbolTableVisitor visitor)
    {
        visitEntryUsing(visitor);
        childTable.visitUsing(visitor);
    }
    
    public abstract void visitEntryUsing(SymbolTableVisitor visitor);

    public int hashCode()
    {
        return identifier.getText().hashCode();
    }

    public abstract String getTypeDescription();

    public String toString()
    {
        return toString(0);
    }

    public String toString(int indent)
    {
        StringBuffer sb = new StringBuffer();
        for (int i = 0; i < indent; i++)
            sb.append(' ');
        sb.append(identifier.getText());
        sb.append(" : ");
        sb.append(getTypeDescription());
        sb.append('\n');
        if (!childTable.isEmpty())
        ;
        sb.append(childTable.toString(indent + 4));
        return sb.toString();
    }
    
    protected Object clone()
    {
        try
        {
            return super.clone();
        }
        catch (CloneNotSupportedException e)
        {
            throw new Error("SymbolTableEntry cloning error: " + e.getMessage());
        }
    }
}
