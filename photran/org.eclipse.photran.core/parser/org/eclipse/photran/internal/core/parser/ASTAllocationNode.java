package org.eclipse.photran.internal.core.parser;

import org.eclipse.photran.internal.core.lexer.Token;
import org.eclipse.photran.internal.core.parser.Parser.*;
import java.util.List;

public class ASTAllocationNode extends InteriorNode
{
    ASTAllocationNode(Production production, List<CSTNode> childNodes, List<CSTNode> discardedSymbols)
    {
         super(production);
         
         for (Object o : childNodes)
             addChild((CSTNode)o);
         constructionFinished();
    }
    
    @Override protected void visitThisNodeUsing(ASTVisitor visitor)
    {
        visitor.visitASTAllocationNode(this);
    }

    public ASTAllocateObjectNode getAllocateObject()
    {
        if (treeHasBeenModified()) throw new IllegalStateException("Accessor methods cannot be called on the nodes of a CST after it has been modified");

        if (getProduction() == Production.ALLOCATION_458)
            return (ASTAllocateObjectNode)getChild(0);
        else if (getProduction() == Production.ALLOCATION_459)
            return (ASTAllocateObjectNode)getChild(0);
        else
            return null;
    }

    public ASTAllocatedShapeNode getAllocatedShape()
    {
        if (treeHasBeenModified()) throw new IllegalStateException("Accessor methods cannot be called on the nodes of a CST after it has been modified");

        if (getProduction() == Production.ALLOCATION_459)
            return (ASTAllocatedShapeNode)getChild(1);
        else
            return null;
    }
}
