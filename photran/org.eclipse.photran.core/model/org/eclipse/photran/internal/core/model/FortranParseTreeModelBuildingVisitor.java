package org.eclipse.photran.internal.core.model;

import java.util.LinkedList;

import org.eclipse.cdt.core.model.CModelException;
import org.eclipse.cdt.internal.core.model.Parent;
import org.eclipse.photran.internal.core.f95parser.GenericParseTreeVisitor;
import org.eclipse.photran.internal.core.f95parser.ParseTreeNode;
import org.eclipse.photran.internal.core.f95parser.ParseTreeSearcher;
import org.eclipse.photran.internal.core.f95parser.Token;

/**
 * This visitor is used to build the Outline view when the user chooses the (debugging) option to
 * have the <i>entire parse tree</i> displayed instead of the normal Outline view.
 * 
 * The normal Outline view is created by a <code>FortranModelBuildingVisitor</code> instead.
 * 
 * @author joverbey
 */
final class FortranParseTreeModelBuildingVisitor extends GenericParseTreeVisitor
{
//    // --INFRASTRUCTURE--------------------------------------------------
//
//    private org.eclipse.cdt.internal.core.model.TranslationUnit translationUnit;
//
//    private FortranModelBuilder modelBuilder;
//
//    FortranParseTreeModelBuildingVisitor(
//        org.eclipse.cdt.internal.core.model.TranslationUnit translationUnit,
//        FortranModelBuilder modelBuilder)
//    {
//        this.translationUnit = translationUnit;
//        this.modelBuilder = modelBuilder;
//    }
//
//    private LinkedList/* <ParseTreeNode> */parentParseTreeNodeStack = new LinkedList();
//
//    private LinkedList/* <F90Elements.F90Element> */parentElementStack = new LinkedList();
//
//    private Parent getCurrentParent()
//    {
//        if (parentElementStack.isEmpty())
//            return translationUnit;
//        else
//            return (Parent)parentElementStack.getLast();
//    }
//
//    private boolean isCurrentParent(ParseTreeNode node)
//    {
//        if (parentParseTreeNodeStack.isEmpty())
//            return false;
//        else
//            return node == (ParseTreeNode)parentParseTreeNodeStack.getLast();
//    }
//
//    private void beginAddingChildrenFor(FortranElement element)
//    {
//        parentParseTreeNodeStack.addLast(element.getParseTreeNode());
//        parentElementStack.addLast(element);
//    }
//
//    private void doneAddingChildrenFor(ParseTreeNode node)
//    {
//        if (isCurrentParent(node))
//        {
//            parentParseTreeNodeStack.removeLast();
//            parentElementStack.removeLast();
//        }
//    }
//
//    // --VISITOR METHODS-------------------------------------------------
//
//    public void preparingToVisitChildrenOf(ParseTreeNode node)
//    {
//        // beginAddingChildrenFor is called in addToModel
//    }
//
//    public void doneVisitingChildrenOf(ParseTreeNode node)
//    {
//        doneAddingChildrenFor(node);
//    }
//
//    public void visitParseTreeNode(ParseTreeNode node)
//    {
//        FortranElement element = new FortranElement.UnknownNode(getCurrentParent(), node
//            .getRootNonterminal().getDescription());
//
//        Token firstToken = ParseTreeSearcher.findFirstTokenIn(node);
//        Token lastToken = ParseTreeSearcher.findLastTokenIn(node);
//        if (firstToken != null && lastToken != null)
//        {
//            element.setPos(firstToken.getOffset(), (lastToken.getOffset() + lastToken.getLength())
//                - firstToken.getOffset() - 1);
//            element.setIdPos(firstToken.getOffset(), firstToken.getLength());
//            element.setLines(firstToken.getStartLine(), lastToken.getEndLine());
//        }
//
//        try
//        {
//            modelBuilder.addF90Element(element);
//        }
//        catch (CModelException e)
//        {
//            ;
//        }
//
//        element.setParseTreeNode(node);
//        beginAddingChildrenFor(element);
//    }
//
//    public void visitToken(Token token)
//    {
//        FortranElement element = new FortranElement.UnknownNode(getCurrentParent(), token
//            .getDescription());
//        try
//        {
//            modelBuilder.addF90Element(element);
//        }
//        catch (CModelException e)
//        {
//            ;
//        }
//    }
}
