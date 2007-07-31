package org.eclipse.photran.internal.core.parser;

import org.eclipse.photran.internal.core.lexer.Token;
import org.eclipse.photran.internal.core.parser.Parser.*;
import java.util.List;

public class ASTKindSelectorNode extends InteriorNode
{
    ASTKindSelectorNode(Production production, List<CSTNode> childNodes, List<CSTNode> discardedSymbols)
    {
         super(production);
         
         for (Object o : childNodes)
             addChild((CSTNode)o);
         constructionFinished();
    }
    
    @Override protected void visitThisNodeUsing(ASTVisitor visitor)
    {
        visitor.visitASTKindSelectorNode(this);
    }

    public Token getTLparen()
    {
        if (treeHasBeenModified()) throw new IllegalStateException("Accessor methods cannot be called on the nodes of a CST after it has been modified");

        if (getProduction() == Production.KIND_SELECTOR_273)
            return (Token)getChild(0);
        else if (getProduction() == Production.KIND_SELECTOR_274)
            return (Token)getChild(0);
        else
            return null;
    }

    public Token getTKindeq()
    {
        if (treeHasBeenModified()) throw new IllegalStateException("Accessor methods cannot be called on the nodes of a CST after it has been modified");

        if (getProduction() == Production.KIND_SELECTOR_273)
            return (Token)getChild(1);
        else
            return null;
    }

    public ASTExprNode getExpr()
    {
        if (treeHasBeenModified()) throw new IllegalStateException("Accessor methods cannot be called on the nodes of a CST after it has been modified");

        if (getProduction() == Production.KIND_SELECTOR_273)
            return (ASTExprNode)getChild(2);
        else if (getProduction() == Production.KIND_SELECTOR_274)
            return (ASTExprNode)getChild(1);
        else
            return null;
    }

    public Token getTRparen()
    {
        if (treeHasBeenModified()) throw new IllegalStateException("Accessor methods cannot be called on the nodes of a CST after it has been modified");

        if (getProduction() == Production.KIND_SELECTOR_273)
            return (Token)getChild(3);
        else if (getProduction() == Production.KIND_SELECTOR_274)
            return (Token)getChild(2);
        else
            return null;
    }
}
