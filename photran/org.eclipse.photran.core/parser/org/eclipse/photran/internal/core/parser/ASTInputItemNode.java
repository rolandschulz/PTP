package org.eclipse.photran.internal.core.parser;

import org.eclipse.photran.internal.core.lexer.Token;
import org.eclipse.photran.internal.core.parser.Parser.*;
import java.util.List;

public class ASTInputItemNode extends InteriorNode
{
    ASTInputItemNode(Production production, List<CSTNode> childNodes, List<CSTNode> discardedSymbols)
    {
         super(production);
         
         for (Object o : childNodes)
             addChild((CSTNode)o);
         constructionFinished();
    }
    
    @Override protected void visitThisNodeUsing(ASTVisitor visitor)
    {
        visitor.visitASTInputItemNode(this);
    }

    public ASTVariableNode getVariable()
    {
        if (treeHasBeenModified()) throw new IllegalStateException("Accessor methods cannot be called on the nodes of a CST after it has been modified");

        if (getProduction() == Production.INPUT_ITEM_806)
            return (ASTVariableNode)getChild(0);
        else
            return null;
    }

    public ASTInputImpliedDoNode getInputImpliedDo()
    {
        if (treeHasBeenModified()) throw new IllegalStateException("Accessor methods cannot be called on the nodes of a CST after it has been modified");

        if (getProduction() == Production.INPUT_ITEM_807)
            return (ASTInputImpliedDoNode)getChild(0);
        else
            return null;
    }
}
