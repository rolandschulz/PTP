package org.eclipse.photran.internal.core.parser;

import org.eclipse.photran.internal.core.lexer.Token;
import org.eclipse.photran.internal.core.parser.Parser.*;
import java.util.List;

public class ASTDerivedTypeBodyConstructNode extends InteriorNode
{
    ASTDerivedTypeBodyConstructNode(Production production, List<CSTNode> childNodes, List<CSTNode> discardedSymbols)
    {
         super(production);
         
         for (Object o : childNodes)
             addChild((CSTNode)o);
         constructionFinished();
    }
    
    @Override protected void visitThisNodeUsing(ASTVisitor visitor)
    {
        visitor.visitASTDerivedTypeBodyConstructNode(this);
    }

    public ASTPrivateSequenceStmtNode getPrivateSequenceStmt()
    {
        if (treeHasBeenModified()) throw new IllegalStateException("Accessor methods cannot be called on the nodes of a CST after it has been modified");

        if (getProduction() == Production.DERIVED_TYPE_BODY_CONSTRUCT_182)
            return (ASTPrivateSequenceStmtNode)getChild(0);
        else
            return null;
    }

    public ASTComponentDefStmtNode getComponentDefStmt()
    {
        if (treeHasBeenModified()) throw new IllegalStateException("Accessor methods cannot be called on the nodes of a CST after it has been modified");

        if (getProduction() == Production.DERIVED_TYPE_BODY_CONSTRUCT_183)
            return (ASTComponentDefStmtNode)getChild(0);
        else
            return null;
    }
}
