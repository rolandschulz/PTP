package org.eclipse.photran.internal.core.parser;

import org.eclipse.photran.internal.core.lexer.Token;
import org.eclipse.photran.internal.core.parser.Parser.*;
import java.util.List;

public class ASTModuleNode extends InteriorNode
{
    ASTModuleNode(Production production, List<CSTNode> childNodes, List<CSTNode> discardedSymbols)
    {
         super(production);
         
         for (Object o : childNodes)
             addChild((CSTNode)o);
         constructionFinished();
    }
    
    @Override protected void visitThisNodeUsing(ASTVisitor visitor)
    {
        visitor.visitASTModuleNode(this);
    }

    public ASTModuleStmtNode getModuleStmt()
    {
        if (treeHasBeenModified()) throw new IllegalStateException("Accessor methods cannot be called on the nodes of a CST after it has been modified");

        if (getProduction() == Production.MODULE_25)
            return (ASTModuleStmtNode)getChild(0);
        else
            return null;
    }

    public ASTModuleBodyNode getModuleBody()
    {
        if (treeHasBeenModified()) throw new IllegalStateException("Accessor methods cannot be called on the nodes of a CST after it has been modified");

        if (getProduction() == Production.MODULE_25)
            return (ASTModuleBodyNode)getChild(1, 0);
        else
            return null;
    }

    public ASTEndModuleStmtNode getEndModuleStmt()
    {
        if (treeHasBeenModified()) throw new IllegalStateException("Accessor methods cannot be called on the nodes of a CST after it has been modified");

        if (getProduction() == Production.MODULE_25)
            return (ASTEndModuleStmtNode)getChild(1, 1);
        else if (getProduction() == Production.MODULE_25)
            return (ASTEndModuleStmtNode)getChild(1, 0);
        else
            return null;
    }

    @Override protected boolean childIsPulledUp(int index)
    {
        if (getProduction() == Production.MODULE_25 && index == 1)
            return true;
        else
            return false;
    }
}
