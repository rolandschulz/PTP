package org.eclipse.photran.internal.core.model;

import java.util.LinkedList;

import org.eclipse.cdt.core.model.CModelException;
import org.eclipse.cdt.internal.core.model.Parent;
import org.eclipse.photran.internal.core.f95parser.ParseTreeNode;
import org.eclipse.photran.internal.core.f95parser.ParseTreeVisitor;

/**
 * This parse tree visitor is used by the <code>FortranModelBuilder</code> to create the model you
 * see in the (normal) Outline view. When it encounters a program, module, function, or other entity
 * that should be displayed in the model, it calls the <code>FortranModelBuilder</code> back and
 * asks it to add the appropriate node.
 * 
 * @author joverbey
 */
final class FortranModelBuildingVisitor extends ParseTreeVisitor
{
    // --INFRASTRUCTURE--------------------------------------------------

    private org.eclipse.cdt.internal.core.model.TranslationUnit translationUnit;

    private FortranModelBuilder modelBuilder;

    FortranModelBuildingVisitor(
        org.eclipse.cdt.internal.core.model.TranslationUnit translationUnit,
        FortranModelBuilder modelBuilder)
    {
        this.translationUnit = translationUnit;
        this.modelBuilder = modelBuilder;
    }

    private LinkedList/* <ParseTreeNode> */parentParseTreeNodeStack = new LinkedList();

    private LinkedList/* <F90Elements.F90Element> */parentElementStack = new LinkedList();

    private Parent getCurrentParent()
    {
        if (parentElementStack.isEmpty())
            return translationUnit;
        else
            return (Parent)parentElementStack.getLast();
    }

    private boolean isCurrentParent(ParseTreeNode node)
    {
        if (parentParseTreeNodeStack.isEmpty())
            return false;
        else
            return node == (ParseTreeNode)parentParseTreeNodeStack.getLast();
    }

    private void addToModel(FortranElement element)
    {
        try
        {
            modelBuilder.addF90Element(element);
            beginAddingChildrenFor(element);
        }
        catch (CModelException e)
        {
            ;
        }
    }

    private void beginAddingChildrenFor(FortranElement element)
    {
        parentParseTreeNodeStack.addLast(element.getParseTreeNode());
        parentElementStack.addLast(element);
    }

    private void doneAddingChildrenFor(ParseTreeNode node)
    {
        if (isCurrentParent(node))
        {
            parentParseTreeNodeStack.removeLast();
            parentElementStack.removeLast();
        }
    }

    // --VISITOR METHODS-------------------------------------------------

    public void preparingToVisitChildrenOf(ParseTreeNode node)
    {
        // beginAddingChildrenFor is called in addToModel
    }

    public void doneVisitingChildrenOf(ParseTreeNode node)
    {
        doneAddingChildrenFor(node);
    }

    public void visitXmainprogram(ParseTreeNode node)
    {
        addToModel(new FortranElement.MainProgram(getCurrentParent(), node));
    }

    public void visitXmodule(ParseTreeNode node)
    {
        addToModel(new FortranElement.Module(getCurrentParent(), node));
    }

    public void visitXfunctionsubprogram(ParseTreeNode node)
    {
        addToModel(new FortranElement.Function(getCurrentParent(), node));
    }

    public void visitXsubroutinesubprogram(ParseTreeNode node)
    {
        addToModel(new FortranElement.Subroutine(getCurrentParent(), node));
    }

    public void visitXblockdatasubprogram(ParseTreeNode node)
    {
        addToModel(new FortranElement.BlockData(getCurrentParent(), node));
    }

    public void visitXderivedtypedef(ParseTreeNode node)
    {
        addToModel(new FortranElement.DerivedType(getCurrentParent(), node));
    }

    public void visitXobjectname(ParseTreeNode node)
    {
        // addToModel(new FortranElements.Variable(getCurrentParent(), node));
    }

    public void visitXpointername(ParseTreeNode node)
    {
        // addToModel(new FortranElements.Variable(getCurrentParent(), node));
    }

    public void visitXtargetname(ParseTreeNode node)
    {
        // addToModel(new FortranElements.Variable(getCurrentParent(), node));
    }

    public void visitXcomponentname(ParseTreeNode node)
    {
        // Inside derived type
        addToModel(new FortranElement.Variable(getCurrentParent(), node));
    }
}
