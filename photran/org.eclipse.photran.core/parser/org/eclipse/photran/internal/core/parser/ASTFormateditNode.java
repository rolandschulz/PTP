package org.eclipse.photran.internal.core.parser;

import org.eclipse.photran.internal.core.lexer.Token;
import org.eclipse.photran.internal.core.parser.Parser.*;
import java.util.List;

public class ASTFormateditNode extends InteriorNode
{
    ASTFormateditNode(Production production, List<CSTNode> childNodes, List<CSTNode> discardedSymbols)
    {
         super(production);
         
         for (Object o : childNodes)
             addChild((CSTNode)o);
         constructionFinished();
    }
    
    @Override protected void visitThisNodeUsing(ASTVisitor visitor)
    {
        visitor.visitASTFormateditNode(this);
    }

    public ASTEditElementNode getEditElement()
    {
        if (treeHasBeenModified()) throw new IllegalStateException("Accessor methods cannot be called on the nodes of a CST after it has been modified");

        if (getProduction() == Production.FORMATEDIT_875)
            return (ASTEditElementNode)getChild(0);
        else if (getProduction() == Production.FORMATEDIT_876)
            return (ASTEditElementNode)getChild(1);
        else if (getProduction() == Production.FORMATEDIT_879)
            return (ASTEditElementNode)getChild(1);
        else if (getProduction() == Production.FORMATEDIT_880)
            return (ASTEditElementNode)getChild(2);
        else
            return null;
    }

    public Token getTIcon()
    {
        if (treeHasBeenModified()) throw new IllegalStateException("Accessor methods cannot be called on the nodes of a CST after it has been modified");

        if (getProduction() == Production.FORMATEDIT_876)
            return (Token)getChild(0);
        else if (getProduction() == Production.FORMATEDIT_880)
            return (Token)getChild(1);
        else
            return null;
    }

    public Token getTXcon()
    {
        if (treeHasBeenModified()) throw new IllegalStateException("Accessor methods cannot be called on the nodes of a CST after it has been modified");

        if (getProduction() == Production.FORMATEDIT_877)
            return (Token)getChild(0);
        else
            return null;
    }

    public Token getTPcon()
    {
        if (treeHasBeenModified()) throw new IllegalStateException("Accessor methods cannot be called on the nodes of a CST after it has been modified");

        if (getProduction() == Production.FORMATEDIT_878)
            return (Token)getChild(0);
        else if (getProduction() == Production.FORMATEDIT_879)
            return (Token)getChild(0);
        else if (getProduction() == Production.FORMATEDIT_880)
            return (Token)getChild(0);
        else
            return null;
    }
}
