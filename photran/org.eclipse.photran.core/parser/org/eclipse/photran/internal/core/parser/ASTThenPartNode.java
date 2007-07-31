package org.eclipse.photran.internal.core.parser;

import org.eclipse.photran.internal.core.lexer.Token;
import org.eclipse.photran.internal.core.parser.Parser.*;
import java.util.List;

public class ASTThenPartNode extends InteriorNode
{
    ASTThenPartNode(Production production, List<CSTNode> childNodes, List<CSTNode> discardedSymbols)
    {
         super(production);
         
         for (Object o : childNodes)
             addChild((CSTNode)o);
         constructionFinished();
    }
    
    @Override protected void visitThisNodeUsing(ASTVisitor visitor)
    {
        visitor.visitASTThenPartNode(this);
    }

    public ASTEndIfStmtNode getEndIfStmt()
    {
        if (treeHasBeenModified()) throw new IllegalStateException("Accessor methods cannot be called on the nodes of a CST after it has been modified");

        if (getProduction() == Production.THEN_PART_650)
            return (ASTEndIfStmtNode)getChild(0);
        else if (getProduction() == Production.THEN_PART_651)
            return (ASTEndIfStmtNode)getChild(1);
        else
            return null;
    }

    public ASTConditionalBodyNode getConditionalBody()
    {
        if (treeHasBeenModified()) throw new IllegalStateException("Accessor methods cannot be called on the nodes of a CST after it has been modified");

        if (getProduction() == Production.THEN_PART_651)
            return (ASTConditionalBodyNode)getChild(0);
        else if (getProduction() == Production.THEN_PART_653)
            return (ASTConditionalBodyNode)getChild(0);
        else if (getProduction() == Production.THEN_PART_655)
            return (ASTConditionalBodyNode)getChild(0);
        else
            return null;
    }

    public ASTElseIfConstructNode getElseIfConstruct()
    {
        if (treeHasBeenModified()) throw new IllegalStateException("Accessor methods cannot be called on the nodes of a CST after it has been modified");

        if (getProduction() == Production.THEN_PART_652)
            return (ASTElseIfConstructNode)getChild(0);
        else if (getProduction() == Production.THEN_PART_653)
            return (ASTElseIfConstructNode)getChild(1);
        else
            return null;
    }

    public ASTElseConstructNode getElseConstruct()
    {
        if (treeHasBeenModified()) throw new IllegalStateException("Accessor methods cannot be called on the nodes of a CST after it has been modified");

        if (getProduction() == Production.THEN_PART_654)
            return (ASTElseConstructNode)getChild(0);
        else if (getProduction() == Production.THEN_PART_655)
            return (ASTElseConstructNode)getChild(1);
        else
            return null;
    }
}
