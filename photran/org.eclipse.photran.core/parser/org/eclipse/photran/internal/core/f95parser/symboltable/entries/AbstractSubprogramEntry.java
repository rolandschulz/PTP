package org.eclipse.photran.internal.core.f95parser.symboltable.entries;

import org.eclipse.photran.internal.core.f95parser.ParseTreeNode;
import org.eclipse.photran.internal.core.f95parser.Token;
import org.eclipse.photran.internal.core.f95parser.symboltable.SymbolTable;
import org.eclipse.photran.internal.core.f95parser.symboltable.SymbolTableEntry;
import org.eclipse.photran.internal.core.f95parser.symboltable.SymbolTableType;

/**
 * Superclass for <code>FunctionEntry</code> and <code>SubroutineEntry</code>
 * 
 * Yes, subroutines can't actually return values, but the grammar allows it, and the
 * <code>PrefixSpecVisitor</code> nested in <code>DeclarationCollection</code> expects the
 * corresponding <code>setReturnType</code> method to be in here in case a return type is
 * specified.
 * 
 * @author joverbey
 */
public abstract class AbstractSubprogramEntry extends SymbolTableEntry
{
    public AbstractSubprogramEntry(SymbolTable parentTable, Token identifier,
        ParseTreeNode correspondingParseTreeNode)
    {
        super(parentTable, identifier, correspondingParseTreeNode);
    }

    /**
     * @return a <code>String</code>, suitable for appending to the end of the
     *         <code>getTypeDescription</code> message, which describes the return type (if
     *         specified) and notes if this entry is marked as elemental, pure, or recursive.
     */
    protected String describeInheritedFields()
    {
        StringBuffer sb = new StringBuffer();
        if (returnType != null) sb.append(", returns " + returnType.getDescription());
        if (isElemental) sb.append(", elemental");
        if (isPure) sb.append(", pure");
        if (isRecursive) sb.append(", recursive");
        return sb.toString();
    }

    protected SymbolTableType returnType = null;

    public void setReturnType(SymbolTableType type)
    {
        this.returnType = type;
    }

    public SymbolTableType getReturnType()
    {
        return this.returnType;
    }

    protected boolean isRecursive = false;

    public boolean isRecursive()
    {
        return isRecursive;
    }

    public void setRecursive(boolean isRecursive)
    {
        this.isRecursive = isRecursive;
    }

    protected boolean isPure = false;

    public boolean isPure()
    {
        return isPure;
    }

    public void setPure(boolean isPure)
    {
        this.isPure = isPure;
    }

    protected boolean isElemental = false;

    public boolean isElemental()
    {
        return isElemental;
    }

    public void setElemental(boolean isElemental)
    {
        this.isElemental = isElemental;
    }
}
