package org.eclipse.photran.internal.core.parser;

import org.eclipse.photran.internal.core.lexer.Token;
import org.eclipse.photran.internal.core.parser.Parser.*;
import java.util.List;

public class ASTConditionalBodyNode extends InteriorNode
{
    ASTConditionalBodyNode(Production production, List<CSTNode> childNodes, List<CSTNode> discardedSymbols)
    {
         super(production);
         
         for (Object o : childNodes)
             addChild((CSTNode)o);
         constructionFinished();
    }
    
    @Override protected void visitThisNodeUsing(ASTVisitor visitor)
    {
        visitor.visitASTConditionalBodyNode(this);
    }

    public ASTExecutionPartConstructNode getExecutionPartConstruct()
    {
        if (treeHasBeenModified()) throw new IllegalStateException("Accessor methods cannot be called on the nodes of a CST after it has been modified");

        if (getProduction() == Production.CONDITIONAL_BODY_660)
            return (ASTExecutionPartConstructNode)getChild(0);
        else if (getProduction() == Production.CONDITIONAL_BODY_661)
            return (ASTExecutionPartConstructNode)getChild(1);
        else
            return null;
    }

    public ASTConditionalBodyNode getConditionalBody()
    {
        if (treeHasBeenModified()) throw new IllegalStateException("Accessor methods cannot be called on the nodes of a CST after it has been modified");

        if (getProduction() == Production.CONDITIONAL_BODY_661)
            return (ASTConditionalBodyNode)getChild(0);
        else
            return null;
    }
}
