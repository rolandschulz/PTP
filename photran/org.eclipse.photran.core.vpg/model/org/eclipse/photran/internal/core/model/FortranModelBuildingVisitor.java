package org.eclipse.photran.internal.core.model;

import java.util.LinkedList;

import org.eclipse.cdt.core.model.CModelException;
import org.eclipse.cdt.internal.core.model.Parent;
import org.eclipse.photran.internal.core.lexer.Token;
import org.eclipse.photran.internal.core.parser.ASTBlockDataSubprogramNode;
import org.eclipse.photran.internal.core.parser.ASTComponentDeclListNode;
import org.eclipse.photran.internal.core.parser.ASTComponentDefStmtNode;
import org.eclipse.photran.internal.core.parser.ASTDerivedTypeDefNode;
import org.eclipse.photran.internal.core.parser.ASTExternalNameListNode;
import org.eclipse.photran.internal.core.parser.ASTExternalStmtNode;
import org.eclipse.photran.internal.core.parser.ASTFunctionSubprogramNode;
import org.eclipse.photran.internal.core.parser.ASTInterfaceBlockNode;
import org.eclipse.photran.internal.core.parser.ASTIntrinsicListNode;
import org.eclipse.photran.internal.core.parser.ASTIntrinsicStmtNode;
import org.eclipse.photran.internal.core.parser.ASTMainProgramNode;
import org.eclipse.photran.internal.core.parser.ASTModuleNode;
import org.eclipse.photran.internal.core.parser.ASTStmtFunctionStmtNode;
import org.eclipse.photran.internal.core.parser.ASTSubroutineSubprogramNode;
import org.eclipse.photran.internal.core.parser.ASTVisitor;
import org.eclipse.photran.internal.core.parser.GenericParseTreeVisitor;
import org.eclipse.photran.internal.core.parser.Parser.InteriorNode;
import org.eclipse.photran.internal.core.refactoring.infrastructure.FortranRefactoring;

/**
 * This parse tree visitor is used by the <code>FortranModelBuilder</code> to create the model you
 * see in the (normal) Outline view. When it encounters a program, module, function, or other entity
 * that should be displayed in the model, it calls the <code>FortranModelBuilder</code> back and
 * asks it to add the appropriate node.
 * 
 * @author joverbey
 */
public final class FortranModelBuildingVisitor extends GenericParseTreeVisitor
{
    // --INFRASTRUCTURE--------------------------------------------------

    private org.eclipse.cdt.internal.core.model.TranslationUnit translationUnit;

    private FortranModelBuilder modelBuilder;

    private ElementMappingVisitor elementMappingVisitor;

    public FortranModelBuildingVisitor(
        org.eclipse.cdt.internal.core.model.TranslationUnit translationUnit,
        FortranModelBuilder modelBuilder)
    {
        this.translationUnit = translationUnit;
        this.modelBuilder = modelBuilder;
        this.elementMappingVisitor = new ElementMappingVisitor();
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

    private boolean isCurrentParent(InteriorNode node)
    {
        if (parentParseTreeNodeStack.isEmpty())
            return false;
        else
            return node == (InteriorNode)parentParseTreeNodeStack.getLast();
    }

    private void addToModel(InteriorNode parseTreeNode, FortranElement element)
    {
        try
        {
            modelBuilder.addF90Element(element);
            beginAddingChildrenFor(parseTreeNode, element);
        }
        catch (CModelException e)
        {
            ;
        }
    }

    private void addToModelNoChildren(FortranElement element)
    {
        try
        {
            modelBuilder.addF90Element(element);
        }
        catch (CModelException e)
        {
            ;
        }
    }

    private void beginAddingChildrenFor(InteriorNode parseTreeNode, FortranElement element)
    {
        parentParseTreeNodeStack.addLast(parseTreeNode);
        parentElementStack.addLast(element);
    }

    private void doneAddingChildrenFor(InteriorNode node)
    {
        if (isCurrentParent(node))
        {
            parentParseTreeNodeStack.removeLast();
            parentElementStack.removeLast();
        }
    }

    // --VISITOR METHODS-------------------------------------------------
    
    public void preparingToVisitChildrenOf(InteriorNode node)
    {
        // beginAddingChildrenFor is called in addToModel
        node.visitOnlyThisNodeUsing(elementMappingVisitor);
    }

    public void doneVisitingChildrenOf(InteriorNode node)
    {
        doneAddingChildrenFor(node);
    }

    private class ElementMappingVisitor extends ASTVisitor
    {
        private <T extends FortranElement> T setPos(T element, InteriorNode astNode)
        {
            Token first = FortranRefactoring.findFirstTokenIn(astNode);
            Token last = FortranRefactoring.findLastTokenIn(astNode);
            if (first != null && last != null)
            {
                element.setPos(first.getFileOffset(), last.getFileOffset()+last.getLength()-first.getFileOffset());
                element.setLines(first.getLine(), last.getLine());
            }
            return element;
        }
        
        public void visitASTMainProgramNode(ASTMainProgramNode node)
        {
            Token token = node.getProgramStmt() == null
                ? null
                : node.getProgramStmt().getProgramName().getProgramName();
            addToModel(node, setPos(new FortranElement.MainProgram(getCurrentParent(), token), node));
        }
    
        public void visitASTModuleNode(ASTModuleNode node)
        {
            Token token = node.getModuleStmt().getModuleName().getModuleName();
            addToModel(node, setPos(new FortranElement.Module(getCurrentParent(), token), node));
        }
    
        public void visitASTFunctionSubprogramNode(ASTFunctionSubprogramNode node)
        {
            Token token = node.getFunctionStmt().getFunctionName().getFunctionName();
            addToModel(node, setPos(new FortranElement.Function(getCurrentParent(), token), node));
        }
    
        public void visitASTSubroutineSubprogramNode(ASTSubroutineSubprogramNode node)
        {
            Token token = node.getSubroutineStmt().getSubroutineName().getSubroutineName();
            addToModel(node, setPos(new FortranElement.Subroutine(getCurrentParent(), token), node));
        }
    
        public void visitASTBlockDataSubprogramNode(ASTBlockDataSubprogramNode node)
        {
            Token token = node.getBlockDataStmt().getBlockDataName() == null
                ? null
                : node.getBlockDataStmt().getBlockDataName().getBlockDataName();
            addToModel(node, setPos(new FortranElement.BlockData(getCurrentParent(), token), node));
        }
    
        public void visitASTDerivedTypeDefNode(ASTDerivedTypeDefNode node)
        {
            Token token = node.getDerivedTypeStmt().getTypeName();
            addToModel(node, setPos(new FortranElement.DerivedType(getCurrentParent(), token), node));
        }
    
        public void visitASTComponentDefStmtNode(ASTComponentDefStmtNode node)
        {
            ASTComponentDeclListNode list = node.getComponentDeclList();
            for (int i = 0; i < list.size(); i++)
				addToModelNoChildren(setPos(new FortranElement.Variable(getCurrentParent(), list.getComponentDecl(i).getComponentName().getComponentName()), node));
        }
    
        public void visitASTExternalStmtNode(ASTExternalStmtNode node)
        {
            ASTExternalNameListNode list = node.getExternalNameList();
            for (int i = 0; i < list.size(); i++)
                addToModel(node, setPos(new FortranElement.Variable(getCurrentParent(), list.getExternalName(i)), node));
        }
    
        public void visitASTInterfaceBlockNode(ASTInterfaceBlockNode node)
        {
            Token token = node.getInterfaceStmt().getGenericName() == null
                ? null
                : node.getInterfaceStmt().getGenericName().getGenericName();
            addToModel(node, setPos(new FortranElement.Variable(getCurrentParent(), token), node));
        }
    
        public void visitASTIntrinsicStmtNode(ASTIntrinsicStmtNode node)
        {
            ASTIntrinsicListNode list = node.getIntrinsicList();
            for (int i = 0; i < list.size(); i++)
                addToModel(node, setPos(new FortranElement.Variable(getCurrentParent(), list.getIntrinsicProcedureName(i)), node));
        }
        
        public void visitASTStmtFunctionStmtNode(ASTStmtFunctionStmtNode node)
        {
            addToModel(node, setPos(new FortranElement.Variable(getCurrentParent(), node.getName().getName()), node));
        }
    }
}
