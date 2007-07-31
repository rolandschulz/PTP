package org.eclipse.photran.internal.core.parser;

import org.eclipse.photran.internal.core.lexer.Token;
import org.eclipse.photran.internal.core.parser.Parser.*;
import java.util.List;

public class ASTInterfaceSpecificationNode extends InteriorNode
{
    ASTInterfaceSpecificationNode(Production production, List<CSTNode> childNodes, List<CSTNode> discardedSymbols)
    {
         super(production);
         
         for (Object o : childNodes)
             addChild((CSTNode)o);
         constructionFinished();
    }
    
    @Override protected void visitThisNodeUsing(ASTVisitor visitor)
    {
        visitor.visitASTInterfaceSpecificationNode(this);
    }

    public ASTInterfaceBodyNode getInterfaceBody()
    {
        if (treeHasBeenModified()) throw new IllegalStateException("Accessor methods cannot be called on the nodes of a CST after it has been modified");

        if (getProduction() == Production.INTERFACE_SPECIFICATION_927)
            return (ASTInterfaceBodyNode)getChild(0);
        else
            return null;
    }

    public ASTModuleProcedureStmtNode getModuleProcedureStmt()
    {
        if (treeHasBeenModified()) throw new IllegalStateException("Accessor methods cannot be called on the nodes of a CST after it has been modified");

        if (getProduction() == Production.INTERFACE_SPECIFICATION_928)
            return (ASTModuleProcedureStmtNode)getChild(0);
        else
            return null;
    }
}
