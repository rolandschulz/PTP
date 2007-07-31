package org.eclipse.photran.internal.core.parser;

import org.eclipse.photran.internal.core.lexer.Token;
import org.eclipse.photran.internal.core.parser.Parser.*;
import java.util.List;

public class ASTForallBodyNode extends InteriorNode
{
    ASTForallBodyNode(Production production, List<CSTNode> childNodes, List<CSTNode> discardedSymbols)
    {
         super(production);
         
         for (Object o : childNodes)
             addChild((CSTNode)o);
         constructionFinished();
    }
    
    @Override protected void visitThisNodeUsing(ASTVisitor visitor)
    {
        visitor.visitASTForallBodyNode(this);
    }

    public ASTForallBodyConstructNode getForallBodyConstruct()
    {
        if (treeHasBeenModified()) throw new IllegalStateException("Accessor methods cannot be called on the nodes of a CST after it has been modified");

        if (getProduction() == Production.FORALL_BODY_628)
            return (ASTForallBodyConstructNode)getChild(0);
        else if (getProduction() == Production.FORALL_BODY_629)
            return (ASTForallBodyConstructNode)getChild(1);
        else
            return null;
    }

    public ASTForallBodyNode getForallBody()
    {
        if (treeHasBeenModified()) throw new IllegalStateException("Accessor methods cannot be called on the nodes of a CST after it has been modified");

        if (getProduction() == Production.FORALL_BODY_629)
            return (ASTForallBodyNode)getChild(0);
        else
            return null;
    }
}
