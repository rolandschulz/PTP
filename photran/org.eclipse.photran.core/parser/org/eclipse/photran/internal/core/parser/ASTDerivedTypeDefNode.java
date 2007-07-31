package org.eclipse.photran.internal.core.parser;

import org.eclipse.photran.internal.core.lexer.Token;
import org.eclipse.photran.internal.core.parser.Parser.*;
import java.util.List;

public class ASTDerivedTypeDefNode extends InteriorNode
{
    ASTDerivedTypeDefNode(Production production, List<CSTNode> childNodes, List<CSTNode> discardedSymbols)
    {
         super(production);
         
         for (Object o : childNodes)
             addChild((CSTNode)o);
         constructionFinished();
    }
    
    @Override protected void visitThisNodeUsing(ASTVisitor visitor)
    {
        visitor.visitASTDerivedTypeDefNode(this);
    }

    public ASTDerivedTypeStmtNode getDerivedTypeStmt()
    {
        if (treeHasBeenModified()) throw new IllegalStateException("Accessor methods cannot be called on the nodes of a CST after it has been modified");

        if (getProduction() == Production.DERIVED_TYPE_DEF_179)
            return (ASTDerivedTypeStmtNode)getChild(0);
        else
            return null;
    }

    public ASTDerivedTypeBodyNode getDerivedTypeBody()
    {
        if (treeHasBeenModified()) throw new IllegalStateException("Accessor methods cannot be called on the nodes of a CST after it has been modified");

        if (getProduction() == Production.DERIVED_TYPE_DEF_179)
            return (ASTDerivedTypeBodyNode)getChild(1);
        else
            return null;
    }

    public ASTEndTypeStmtNode getEndTypeStmt()
    {
        if (treeHasBeenModified()) throw new IllegalStateException("Accessor methods cannot be called on the nodes of a CST after it has been modified");

        if (getProduction() == Production.DERIVED_TYPE_DEF_179)
            return (ASTEndTypeStmtNode)getChild(2);
        else
            return null;
    }
}
