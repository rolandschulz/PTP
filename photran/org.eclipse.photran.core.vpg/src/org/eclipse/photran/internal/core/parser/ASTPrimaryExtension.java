package org.eclipse.photran.internal.core.parser;

import java.util.List;

import org.eclipse.photran.internal.core.lexer.Token;
import org.eclipse.photran.internal.core.parser.Parser.CSTNode;
import org.eclipse.photran.internal.core.parser.Parser.Production;

/**
 * This class exists to rename and/or increase the visibility of the generated members of {@link ASTPrimaryBase}.
 * Type checking and the other &quot;interesting&quot; parts of {@link ASTPrimaryNode} are in that class.
 * <p>
 * This distinction is made solely to keep that class from getting cluttered with these trivial methods.
 * 
 * @author Jeff Overbey
 */
class ASTPrimaryExtension extends ASTPrimaryBase
{
    ASTPrimaryExtension(Production production,
                   List<CSTNode> childNodes,
                   List<CSTNode> discardedSymbols)
    {
        super(production, childNodes, discardedSymbols);
    }
    
    @Override
    public ASTArrayConstructorNode getArrayConstructor()
    {
        return super.getArrayConstructor();
    }

    @Override
    public ASTComplexConstNode getComplexConst()
    {
        return super.getComplexConst();
    }

    @Override
    public Token getDblConst()
    {
        return super.getDblConst();
    }

    @Override
    public ASTFunctionArgListNode getFunctionArgList()
    {
        return super.getFunctionArgList();
    }

    @Override
    public Token getIntConst()
    {
        return super.getIntConst();
    }

    @Override
    public ASTLogicalConstantNode getLogicalConst()
    {
        return super.getLogicalConst();
    }

    @Override
    public ASTNameNode getName()
    {
        return super.getName();
    }

    @Override
    public ASTExpressionNode getNestedExpression()
    {
        return super.getNestedExpression();
    }

    @Override
    public Token getRealConst()
    {
        return super.getRealConst();
    }

    @Override
    public Token getStringConst()
    {
        return super.getStringConst();
    }

    public boolean isComplexConst()
    {
        return super.hasComplexConst();
    }

    public boolean isDblConst()
    {
        return super.hasDblConst();
    }

    public boolean isIntConst()
    {
        return super.hasIntConst();
    }

    public boolean isLogicalConst()
    {
        return super.hasLogicalConst();
    }

    public boolean isNestedExpression()
    {
        return super.hasNestedExpression();
    }

    public boolean isRealConst()
    {
        return super.hasRealConst();
    }

    public boolean isArrayConstructor()
    {
        return super.hasArrayConstructor();
    }

    public boolean isStringConst()
    {
        return super.hasStringConst();
    }
}
