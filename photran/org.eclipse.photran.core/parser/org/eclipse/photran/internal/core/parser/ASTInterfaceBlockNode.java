package org.eclipse.photran.internal.core.parser;

import org.eclipse.photran.internal.core.lexer.Token;
import org.eclipse.photran.internal.core.parser.Parser.*;
import java.util.List;

public class ASTInterfaceBlockNode extends InteriorNode
{
    ASTInterfaceBlockNode(Production production, List<CSTNode> childNodes, List<CSTNode> discardedSymbols)
    {
         super(production);
         
         for (Object o : childNodes)
             addChild((CSTNode)o);
         constructionFinished();
    }
    
    @Override protected void visitThisNodeUsing(ASTVisitor visitor)
    {
        visitor.visitASTInterfaceBlockNode(this);
    }

    public ASTInterfaceStmtNode getInterfaceStmt()
    {
        if (treeHasBeenModified()) throw new IllegalStateException("Accessor methods cannot be called on the nodes of a CST after it has been modified");

        if (getProduction() == Production.INTERFACE_BLOCK_923)
            return (ASTInterfaceStmtNode)getChild(0);
        else
            return null;
    }

    public ASTInterfaceRangeNode getInterfaceRange()
    {
        if (treeHasBeenModified()) throw new IllegalStateException("Accessor methods cannot be called on the nodes of a CST after it has been modified");

        if (getProduction() == Production.INTERFACE_BLOCK_923)
            return (ASTInterfaceRangeNode)getChild(1);
        else
            return null;
    }
}
