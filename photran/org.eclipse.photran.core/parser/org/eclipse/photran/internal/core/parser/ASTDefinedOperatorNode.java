package org.eclipse.photran.internal.core.parser;

import org.eclipse.photran.internal.core.lexer.Token;
import org.eclipse.photran.internal.core.parser.Parser.*;
import java.util.List;

public class ASTDefinedOperatorNode extends InteriorNode
{
    ASTDefinedOperatorNode(Production production, List<CSTNode> childNodes, List<CSTNode> discardedSymbols)
    {
         super(production);
         
         for (Object o : childNodes)
             addChild((CSTNode)o);
         constructionFinished();
    }
    
    @Override protected void visitThisNodeUsing(ASTVisitor visitor)
    {
        visitor.visitASTDefinedOperatorNode(this);
    }

    public Token getTXdop()
    {
        if (treeHasBeenModified()) throw new IllegalStateException("Accessor methods cannot be called on the nodes of a CST after it has been modified");

        if (getProduction() == Production.DEFINED_OPERATOR_149)
            return (Token)getChild(0);
        else
            return null;
    }

    public ASTConcatOpNode getConcatOp()
    {
        if (treeHasBeenModified()) throw new IllegalStateException("Accessor methods cannot be called on the nodes of a CST after it has been modified");

        if (getProduction() == Production.DEFINED_OPERATOR_150)
            return (ASTConcatOpNode)getChild(0);
        else
            return null;
    }

    public ASTPowerOpNode getPowerOp()
    {
        if (treeHasBeenModified()) throw new IllegalStateException("Accessor methods cannot be called on the nodes of a CST after it has been modified");

        if (getProduction() == Production.DEFINED_OPERATOR_151)
            return (ASTPowerOpNode)getChild(0);
        else
            return null;
    }

    public ASTMultOpNode getMultOp()
    {
        if (treeHasBeenModified()) throw new IllegalStateException("Accessor methods cannot be called on the nodes of a CST after it has been modified");

        if (getProduction() == Production.DEFINED_OPERATOR_152)
            return (ASTMultOpNode)getChild(0);
        else
            return null;
    }

    public ASTAddOpNode getAddOp()
    {
        if (treeHasBeenModified()) throw new IllegalStateException("Accessor methods cannot be called on the nodes of a CST after it has been modified");

        if (getProduction() == Production.DEFINED_OPERATOR_153)
            return (ASTAddOpNode)getChild(0);
        else
            return null;
    }

    public ASTRelOpNode getRelOp()
    {
        if (treeHasBeenModified()) throw new IllegalStateException("Accessor methods cannot be called on the nodes of a CST after it has been modified");

        if (getProduction() == Production.DEFINED_OPERATOR_154)
            return (ASTRelOpNode)getChild(0);
        else
            return null;
    }

    public ASTNotOpNode getNotOp()
    {
        if (treeHasBeenModified()) throw new IllegalStateException("Accessor methods cannot be called on the nodes of a CST after it has been modified");

        if (getProduction() == Production.DEFINED_OPERATOR_155)
            return (ASTNotOpNode)getChild(0);
        else
            return null;
    }

    public ASTAndOpNode getAndOp()
    {
        if (treeHasBeenModified()) throw new IllegalStateException("Accessor methods cannot be called on the nodes of a CST after it has been modified");

        if (getProduction() == Production.DEFINED_OPERATOR_156)
            return (ASTAndOpNode)getChild(0);
        else
            return null;
    }

    public ASTOrOpNode getOrOp()
    {
        if (treeHasBeenModified()) throw new IllegalStateException("Accessor methods cannot be called on the nodes of a CST after it has been modified");

        if (getProduction() == Production.DEFINED_OPERATOR_157)
            return (ASTOrOpNode)getChild(0);
        else
            return null;
    }

    public ASTEquivOpNode getEquivOp()
    {
        if (treeHasBeenModified()) throw new IllegalStateException("Accessor methods cannot be called on the nodes of a CST after it has been modified");

        if (getProduction() == Production.DEFINED_OPERATOR_158)
            return (ASTEquivOpNode)getChild(0);
        else
            return null;
    }
}
