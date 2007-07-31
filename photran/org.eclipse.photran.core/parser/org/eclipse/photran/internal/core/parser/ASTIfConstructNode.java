package org.eclipse.photran.internal.core.parser;

import org.eclipse.photran.internal.core.lexer.Token;
import org.eclipse.photran.internal.core.parser.Parser.*;
import java.util.List;

public class ASTIfConstructNode extends InteriorNode
{
    ASTIfConstructNode(Production production, List<CSTNode> childNodes, List<CSTNode> discardedSymbols)
    {
         super(production);
         
         for (Object o : childNodes)
             addChild((CSTNode)o);
         constructionFinished();
    }
    
    @Override protected void visitThisNodeUsing(ASTVisitor visitor)
    {
        visitor.visitASTIfConstructNode(this);
    }

    public ASTIfThenStmtNode getIfThenStmt()
    {
        if (treeHasBeenModified()) throw new IllegalStateException("Accessor methods cannot be called on the nodes of a CST after it has been modified");

        if (getProduction() == Production.IF_CONSTRUCT_649)
            return (ASTIfThenStmtNode)getChild(0);
        else
            return null;
    }

    public ASTThenPartNode getThenPart()
    {
        if (treeHasBeenModified()) throw new IllegalStateException("Accessor methods cannot be called on the nodes of a CST after it has been modified");

        if (getProduction() == Production.IF_CONSTRUCT_649)
            return (ASTThenPartNode)getChild(1);
        else
            return null;
    }
}
