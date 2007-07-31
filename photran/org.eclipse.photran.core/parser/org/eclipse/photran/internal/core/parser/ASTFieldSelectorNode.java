package org.eclipse.photran.internal.core.parser;

import org.eclipse.photran.internal.core.lexer.Token;
import org.eclipse.photran.internal.core.parser.Parser.*;
import java.util.List;

public class ASTFieldSelectorNode extends InteriorNode
{
    ASTFieldSelectorNode(Production production, List<CSTNode> childNodes, List<CSTNode> discardedSymbols)
    {
         super(production);
         
         for (Object o : childNodes)
             addChild((CSTNode)o);
         constructionFinished();
    }
    
    @Override protected void visitThisNodeUsing(ASTVisitor visitor)
    {
        visitor.visitASTFieldSelectorNode(this);
    }

    public Token getTLparen()
    {
        if (treeHasBeenModified()) throw new IllegalStateException("Accessor methods cannot be called on the nodes of a CST after it has been modified");

        if (getProduction() == Production.FIELD_SELECTOR_437)
            return (Token)getChild(0);
        else
            return null;
    }

    public ASTSectionSubscriptListNode getSectionSubscriptList()
    {
        if (treeHasBeenModified()) throw new IllegalStateException("Accessor methods cannot be called on the nodes of a CST after it has been modified");

        if (getProduction() == Production.FIELD_SELECTOR_437)
            return (ASTSectionSubscriptListNode)getChild(1);
        else
            return null;
    }

    public Token getTRparen()
    {
        if (treeHasBeenModified()) throw new IllegalStateException("Accessor methods cannot be called on the nodes of a CST after it has been modified");

        if (getProduction() == Production.FIELD_SELECTOR_437)
            return (Token)getChild(2);
        else
            return null;
    }

    public Token getTPercent()
    {
        if (treeHasBeenModified()) throw new IllegalStateException("Accessor methods cannot be called on the nodes of a CST after it has been modified");

        if (getProduction() == Production.FIELD_SELECTOR_437)
            return (Token)getChild(3);
        else if (getProduction() == Production.FIELD_SELECTOR_438)
            return (Token)getChild(0);
        else
            return null;
    }

    public ASTNameNode getName()
    {
        if (treeHasBeenModified()) throw new IllegalStateException("Accessor methods cannot be called on the nodes of a CST after it has been modified");

        if (getProduction() == Production.FIELD_SELECTOR_437)
            return (ASTNameNode)getChild(4);
        else if (getProduction() == Production.FIELD_SELECTOR_438)
            return (ASTNameNode)getChild(1);
        else
            return null;
    }
}
