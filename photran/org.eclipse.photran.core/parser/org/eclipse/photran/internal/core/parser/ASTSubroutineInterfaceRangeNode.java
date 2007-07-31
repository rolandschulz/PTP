package org.eclipse.photran.internal.core.parser;

import org.eclipse.photran.internal.core.lexer.Token;
import org.eclipse.photran.internal.core.parser.Parser.*;
import java.util.List;

public class ASTSubroutineInterfaceRangeNode extends InteriorNode
{
    ASTSubroutineInterfaceRangeNode(Production production, List<CSTNode> childNodes, List<CSTNode> discardedSymbols)
    {
         super(production);
         
         for (Object o : childNodes)
             addChild((CSTNode)o);
         constructionFinished();
    }
    
    @Override protected void visitThisNodeUsing(ASTVisitor visitor)
    {
        visitor.visitASTSubroutineInterfaceRangeNode(this);
    }

    public ASTSubprogramInterfaceBodyNode getSubprogramInterfaceBody()
    {
        if (treeHasBeenModified()) throw new IllegalStateException("Accessor methods cannot be called on the nodes of a CST after it has been modified");

        if (getProduction() == Production.SUBROUTINE_INTERFACE_RANGE_940)
            return (ASTSubprogramInterfaceBodyNode)getChild(0);
        else
            return null;
    }

    public ASTEndSubroutineStmtNode getEndSubroutineStmt()
    {
        if (treeHasBeenModified()) throw new IllegalStateException("Accessor methods cannot be called on the nodes of a CST after it has been modified");

        if (getProduction() == Production.SUBROUTINE_INTERFACE_RANGE_940)
            return (ASTEndSubroutineStmtNode)getChild(1);
        else if (getProduction() == Production.SUBROUTINE_INTERFACE_RANGE_941)
            return (ASTEndSubroutineStmtNode)getChild(0);
        else
            return null;
    }
}
