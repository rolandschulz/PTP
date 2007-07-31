package org.eclipse.photran.internal.core.parser;

import org.eclipse.photran.internal.core.lexer.Token;
import org.eclipse.photran.internal.core.parser.Parser.*;
import java.util.List;

public class ASTMaskedElsewhereStmtNode extends InteriorNode
{
    ASTMaskedElsewhereStmtNode(Production production, List<CSTNode> childNodes, List<CSTNode> discardedSymbols)
    {
         super(production);
         
         for (Object o : childNodes)
             addChild((CSTNode)o);
         constructionFinished();
    }
    
    @Override protected void visitThisNodeUsing(ASTVisitor visitor)
    {
        visitor.visitASTMaskedElsewhereStmtNode(this);
    }

    public ASTLblDefNode getLblDef()
    {
        if (treeHasBeenModified()) throw new IllegalStateException("Accessor methods cannot be called on the nodes of a CST after it has been modified");

        if (getProduction() == Production.MASKED_ELSEWHERE_STMT_618)
            return (ASTLblDefNode)getChild(0);
        else if (getProduction() == Production.MASKED_ELSEWHERE_STMT_619)
            return (ASTLblDefNode)getChild(0);
        else
            return null;
    }

    public Token getTElsewhere()
    {
        if (treeHasBeenModified()) throw new IllegalStateException("Accessor methods cannot be called on the nodes of a CST after it has been modified");

        if (getProduction() == Production.MASKED_ELSEWHERE_STMT_618)
            return (Token)getChild(1);
        else if (getProduction() == Production.MASKED_ELSEWHERE_STMT_619)
            return (Token)getChild(1);
        else
            return null;
    }

    public Token getTLparen()
    {
        if (treeHasBeenModified()) throw new IllegalStateException("Accessor methods cannot be called on the nodes of a CST after it has been modified");

        if (getProduction() == Production.MASKED_ELSEWHERE_STMT_618)
            return (Token)getChild(2);
        else if (getProduction() == Production.MASKED_ELSEWHERE_STMT_619)
            return (Token)getChild(2);
        else
            return null;
    }

    public ASTMaskExprNode getMaskExpr()
    {
        if (treeHasBeenModified()) throw new IllegalStateException("Accessor methods cannot be called on the nodes of a CST after it has been modified");

        if (getProduction() == Production.MASKED_ELSEWHERE_STMT_618)
            return (ASTMaskExprNode)getChild(3);
        else if (getProduction() == Production.MASKED_ELSEWHERE_STMT_619)
            return (ASTMaskExprNode)getChild(3);
        else
            return null;
    }

    public Token getTRparen()
    {
        if (treeHasBeenModified()) throw new IllegalStateException("Accessor methods cannot be called on the nodes of a CST after it has been modified");

        if (getProduction() == Production.MASKED_ELSEWHERE_STMT_618)
            return (Token)getChild(4);
        else if (getProduction() == Production.MASKED_ELSEWHERE_STMT_619)
            return (Token)getChild(4);
        else
            return null;
    }

    public Token getTEos()
    {
        if (treeHasBeenModified()) throw new IllegalStateException("Accessor methods cannot be called on the nodes of a CST after it has been modified");

        if (getProduction() == Production.MASKED_ELSEWHERE_STMT_618)
            return (Token)getChild(5);
        else if (getProduction() == Production.MASKED_ELSEWHERE_STMT_619)
            return (Token)getChild(6);
        else
            return null;
    }

    public ASTEndNameNode getEndName()
    {
        if (treeHasBeenModified()) throw new IllegalStateException("Accessor methods cannot be called on the nodes of a CST after it has been modified");

        if (getProduction() == Production.MASKED_ELSEWHERE_STMT_619)
            return (ASTEndNameNode)getChild(5);
        else
            return null;
    }
}
