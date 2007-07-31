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
        public void visitASTMainProgramNode(ASTMainProgramNode node)
        {
            Token token = node.getProgramStmt() == null
                ? null
                : node.getProgramStmt().getProgramName().getTIdent();
            addToModel(node, new FortranElement.MainProgram(getCurrentParent(), token));
        }
    
        public void visitASTModuleNode(ASTModuleNode node)
        {
            Token token = node.getModuleStmt().getModuleName().getTIdent();
            addToModel(node, new FortranElement.Module(getCurrentParent(), token));
        }
    
        public void visitASTFunctionSubprogramNode(ASTFunctionSubprogramNode node)
        {
            Token token = node.getFunctionStmt().getFunctionName().getTIdent();
            addToModel(node, new FortranElement.Function(getCurrentParent(), token));
        }
    
        public void visitASTSubroutineSubprogramNode(ASTSubroutineSubprogramNode node)
        {
            Token token = node.getSubroutineStmt().getSubroutineName().getTIdent();
            addToModel(node, new FortranElement.Subroutine(getCurrentParent(), token));
        }
    
        public void visitASTBlockDataSubprogramNode(ASTBlockDataSubprogramNode node)
        {
            Token token = node.getBlockDataStmt().getBlockDataName() == null
                ? null
                : node.getBlockDataStmt().getBlockDataName().getTIdent();
            addToModel(node, new FortranElement.BlockData(getCurrentParent(), token));
        }
    
        public void visitASTDerivedTypeDefNode(ASTDerivedTypeDefNode node)
        {
            Token token = node.getDerivedTypeStmt().getTypeName().getTIdent();
            addToModel(node, new FortranElement.DerivedType(getCurrentParent(), token));
        }
    
        public void visitASTComponentDefStmtNode(ASTComponentDefStmtNode node)
        {
            ASTComponentDeclListNode list = node.getComponentDeclList();
            for (int i = 0; i < list.size(); i++)
                addToModel(node, new FortranElement.Variable(getCurrentParent(), list.getComponentDecl(i).getComponentName().getTIdent()));
        }
    
        public void visitASTExternalStmtNode(ASTExternalStmtNode node)
        {
            ASTExternalNameListNode list = node.getExternalNameList();
            for (int i = 0; i < list.size(); i++)
                addToModel(node, new FortranElement.Variable(getCurrentParent(), list.getExternalName(i).getTIdent()));
        }
    
        public void visitASTInterfaceBlockNode(ASTInterfaceBlockNode node)
        {
            Token token = node.getInterfaceStmt().getGenericName() == null
                ? null
                : node.getInterfaceStmt().getGenericName().getTIdent();
            addToModel(node, new FortranElement.Variable(getCurrentParent(), token));
        }
    
        public void visitASTIntrinsicStmtNode(ASTIntrinsicStmtNode node)
        {
            ASTIntrinsicListNode list = node.getIntrinsicList();
            for (int i = 0; i < list.size(); i++)
                addToModel(node, new FortranElement.Variable(getCurrentParent(), list.getIntrinsicProcedureName(i).getTIdent()));
        }
        
        public void visitASTStmtFunctionStmtNode(ASTStmtFunctionStmtNode node)
        {
            addToModel(node, new FortranElement.Variable(getCurrentParent(), node.getName().getTIdent()));
        }
    }
}
