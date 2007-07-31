package org.eclipse.photran.internal.core.parser;

import org.eclipse.photran.internal.core.lexer.Token;
import org.eclipse.photran.internal.core.parser.Parser.*;
import java.util.List;

public class ASTCharSelectorNode extends InteriorNode
{
    ASTCharSelectorNode(Production production, List<CSTNode> childNodes, List<CSTNode> discardedSymbols)
    {
         super(production);
         
         for (Object o : childNodes)
             addChild((CSTNode)o);
         constructionFinished();
    }
    
    @Override protected void visitThisNodeUsing(ASTVisitor visitor)
    {
        visitor.visitASTCharSelectorNode(this);
    }

    public ASTLengthSelectorNode getLengthSelector()
    {
        if (treeHasBeenModified()) throw new IllegalStateException("Accessor methods cannot be called on the nodes of a CST after it has been modified");

        if (getProduction() == Production.CHAR_SELECTOR_275)
            return (ASTLengthSelectorNode)getChild(0);
        else
            return null;
    }

    public Token getTLparen()
    {
        if (treeHasBeenModified()) throw new IllegalStateException("Accessor methods cannot be called on the nodes of a CST after it has been modified");

        if (getProduction() == Production.CHAR_SELECTOR_276)
            return (Token)getChild(0);
        else if (getProduction() == Production.CHAR_SELECTOR_277)
            return (Token)getChild(0);
        else if (getProduction() == Production.CHAR_SELECTOR_278)
            return (Token)getChild(0);
        else if (getProduction() == Production.CHAR_SELECTOR_279)
            return (Token)getChild(0);
        else if (getProduction() == Production.CHAR_SELECTOR_280)
            return (Token)getChild(0);
        else
            return null;
    }

    public Token getTLeneq()
    {
        if (treeHasBeenModified()) throw new IllegalStateException("Accessor methods cannot be called on the nodes of a CST after it has been modified");

        if (getProduction() == Production.CHAR_SELECTOR_276)
            return (Token)getChild(1);
        else if (getProduction() == Production.CHAR_SELECTOR_277)
            return (Token)getChild(1);
        else if (getProduction() == Production.CHAR_SELECTOR_279)
            return (Token)getChild(1);
        else
            return null;
    }

    public ASTCharLenParamValueNode getCharLenParamValue()
    {
        if (treeHasBeenModified()) throw new IllegalStateException("Accessor methods cannot be called on the nodes of a CST after it has been modified");

        if (getProduction() == Production.CHAR_SELECTOR_276)
            return (ASTCharLenParamValueNode)getChild(2);
        else if (getProduction() == Production.CHAR_SELECTOR_277)
            return (ASTCharLenParamValueNode)getChild(2);
        else if (getProduction() == Production.CHAR_SELECTOR_279)
            return (ASTCharLenParamValueNode)getChild(2);
        else if (getProduction() == Production.CHAR_SELECTOR_280)
            return (ASTCharLenParamValueNode)getChild(1);
        else
            return null;
    }

    public Token getTComma()
    {
        if (treeHasBeenModified()) throw new IllegalStateException("Accessor methods cannot be called on the nodes of a CST after it has been modified");

        if (getProduction() == Production.CHAR_SELECTOR_276)
            return (Token)getChild(3);
        else if (getProduction() == Production.CHAR_SELECTOR_277)
            return (Token)getChild(3);
        else
            return null;
    }

    public Token getTKindeq()
    {
        if (treeHasBeenModified()) throw new IllegalStateException("Accessor methods cannot be called on the nodes of a CST after it has been modified");

        if (getProduction() == Production.CHAR_SELECTOR_276)
            return (Token)getChild(4);
        else if (getProduction() == Production.CHAR_SELECTOR_278)
            return (Token)getChild(1);
        else
            return null;
    }

    public ASTExprNode getExpr()
    {
        if (treeHasBeenModified()) throw new IllegalStateException("Accessor methods cannot be called on the nodes of a CST after it has been modified");

        if (getProduction() == Production.CHAR_SELECTOR_276)
            return (ASTExprNode)getChild(5);
        else if (getProduction() == Production.CHAR_SELECTOR_277)
            return (ASTExprNode)getChild(4);
        else if (getProduction() == Production.CHAR_SELECTOR_278)
            return (ASTExprNode)getChild(2);
        else
            return null;
    }

    public Token getTRparen()
    {
        if (treeHasBeenModified()) throw new IllegalStateException("Accessor methods cannot be called on the nodes of a CST after it has been modified");

        if (getProduction() == Production.CHAR_SELECTOR_276)
            return (Token)getChild(6);
        else if (getProduction() == Production.CHAR_SELECTOR_277)
            return (Token)getChild(5);
        else if (getProduction() == Production.CHAR_SELECTOR_278)
            return (Token)getChild(3);
        else if (getProduction() == Production.CHAR_SELECTOR_279)
            return (Token)getChild(3);
        else if (getProduction() == Production.CHAR_SELECTOR_280)
            return (Token)getChild(2);
        else
            return null;
    }
}
