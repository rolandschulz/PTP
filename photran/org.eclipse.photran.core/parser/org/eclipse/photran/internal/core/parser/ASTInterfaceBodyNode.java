package org.eclipse.photran.internal.core.parser;

import org.eclipse.photran.internal.core.lexer.Token;
import org.eclipse.photran.internal.core.parser.Parser.*;
import java.util.List;

public class ASTInterfaceBodyNode extends InteriorNode
{
    ASTInterfaceBodyNode(Production production, List<CSTNode> childNodes, List<CSTNode> discardedSymbols)
    {
         super(production);
         
         for (Object o : childNodes)
             addChild((CSTNode)o);
         constructionFinished();
    }
    
    @Override protected void visitThisNodeUsing(ASTVisitor visitor)
    {
        visitor.visitASTInterfaceBodyNode(this);
    }

    public ASTFunctionStmtNode getFunctionStmt()
    {
        if (treeHasBeenModified()) throw new IllegalStateException("Accessor methods cannot be called on the nodes of a CST after it has been modified");

        if (getProduction() == Production.INTERFACE_BODY_936)
            return (ASTFunctionStmtNode)getChild(0);
        else
            return null;
    }

    public ASTFunctionInterfaceRangeNode getFunctionInterfaceRange()
    {
        if (treeHasBeenModified()) throw new IllegalStateException("Accessor methods cannot be called on the nodes of a CST after it has been modified");

        if (getProduction() == Production.INTERFACE_BODY_936)
            return (ASTFunctionInterfaceRangeNode)getChild(1);
        else
            return null;
    }

    public ASTSubroutineStmtNode getSubroutineStmt()
    {
        if (treeHasBeenModified()) throw new IllegalStateException("Accessor methods cannot be called on the nodes of a CST after it has been modified");

        if (getProduction() == Production.INTERFACE_BODY_937)
            return (ASTSubroutineStmtNode)getChild(0);
        else
            return null;
    }

    public ASTSubroutineInterfaceRangeNode getSubroutineInterfaceRange()
    {
        if (treeHasBeenModified()) throw new IllegalStateException("Accessor methods cannot be called on the nodes of a CST after it has been modified");

        if (getProduction() == Production.INTERFACE_BODY_937)
            return (ASTSubroutineInterfaceRangeNode)getChild(1);
        else
            return null;
    }
}
