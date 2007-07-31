package org.eclipse.photran.internal.core.parser;

import org.eclipse.photran.internal.core.lexer.Token;
import org.eclipse.photran.internal.core.parser.Parser.*;
import java.util.List;

public class ASTPrivateSequenceStmtNode extends InteriorNode
{
    ASTPrivateSequenceStmtNode(Production production, List<CSTNode> childNodes, List<CSTNode> discardedSymbols)
    {
         super(production);
         
         for (Object o : childNodes)
             addChild((CSTNode)o);
         constructionFinished();
    }
    
    @Override protected void visitThisNodeUsing(ASTVisitor visitor)
    {
        visitor.visitASTPrivateSequenceStmtNode(this);
    }

    public ASTLblDefNode getLblDef()
    {
        if (treeHasBeenModified()) throw new IllegalStateException("Accessor methods cannot be called on the nodes of a CST after it has been modified");

        if (getProduction() == Production.PRIVATE_SEQUENCE_STMT_187)
            return (ASTLblDefNode)getChild(0);
        else if (getProduction() == Production.PRIVATE_SEQUENCE_STMT_188)
            return (ASTLblDefNode)getChild(0);
        else
            return null;
    }

    public Token getTPrivate()
    {
        if (treeHasBeenModified()) throw new IllegalStateException("Accessor methods cannot be called on the nodes of a CST after it has been modified");

        if (getProduction() == Production.PRIVATE_SEQUENCE_STMT_187)
            return (Token)getChild(1);
        else
            return null;
    }

    public Token getTEos()
    {
        if (treeHasBeenModified()) throw new IllegalStateException("Accessor methods cannot be called on the nodes of a CST after it has been modified");

        if (getProduction() == Production.PRIVATE_SEQUENCE_STMT_187)
            return (Token)getChild(2);
        else if (getProduction() == Production.PRIVATE_SEQUENCE_STMT_188)
            return (Token)getChild(2);
        else
            return null;
    }

    public Token getTSequence()
    {
        if (treeHasBeenModified()) throw new IllegalStateException("Accessor methods cannot be called on the nodes of a CST after it has been modified");

        if (getProduction() == Production.PRIVATE_SEQUENCE_STMT_188)
            return (Token)getChild(1);
        else
            return null;
    }
}
