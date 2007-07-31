package org.eclipse.photran.internal.core.parser;

import org.eclipse.photran.internal.core.lexer.Token;
import org.eclipse.photran.internal.core.parser.Parser.*;
import java.util.List;

public class ASTCommaLoopControlNode extends InteriorNode
{
    ASTCommaLoopControlNode(Production production, List<CSTNode> childNodes, List<CSTNode> discardedSymbols)
    {
         super(production);
         
         for (Object o : childNodes)
             addChild((CSTNode)o);
         constructionFinished();
    }
    
    @Override protected void visitThisNodeUsing(ASTVisitor visitor)
    {
        visitor.visitASTCommaLoopControlNode(this);
    }

    public Token getTComma()
    {
        if (treeHasBeenModified()) throw new IllegalStateException("Accessor methods cannot be called on the nodes of a CST after it has been modified");

        if (getProduction() == Production.COMMA_LOOP_CONTROL_710)
            return (Token)getChild(0);
        else
            return null;
    }

    public ASTLoopControlNode getLoopControl()
    {
        if (treeHasBeenModified()) throw new IllegalStateException("Accessor methods cannot be called on the nodes of a CST after it has been modified");

        if (getProduction() == Production.COMMA_LOOP_CONTROL_710)
            return (ASTLoopControlNode)getChild(1);
        else if (getProduction() == Production.COMMA_LOOP_CONTROL_711)
            return (ASTLoopControlNode)getChild(0);
        else
            return null;
    }
}
