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
import org.eclipse.photran.internal.core.parser.ParseTreeNode;

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

    private boolean isCurrentParent(ParseTreeNode node)
    {
        if (parentParseTreeNodeStack.isEmpty())
            return false;
        else
            return node == (ParseTreeNode)parentParseTreeNodeStack.getLast();
    }

    private void addToModel(ParseTreeNode parseTreeNode, FortranElement element)
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

    private void beginAddingChildrenFor(ParseTreeNode parseTreeNode, FortranElement element)
    {
        parentParseTreeNodeStack.addLast(parseTreeNode);
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
        node.visitOnlyThisNodeUsing(elementMappingVisitor);
    }

    public void doneVisitingChildrenOf(ParseTreeNode node)
    {
        doneAddingChildrenFor(node);
    }

    private class ElementMappingVisitor extends ASTVisitor
    {
        public void visitASTMainProgramNode(ASTMainProgramNode node)
        {
            Token token = node.getASTProgramStmt() == null
                ? null
                : node.getASTProgramStmt().getASTProgramName().getASTTident();
            addToModel(node, new FortranElement.MainProgram(getCurrentParent(), token));
        }
    
        public void visitASTModuleNode(ASTModuleNode node)
        {
            Token token = node.getASTModuleStmt().getASTModuleName().getASTTident();
            addToModel(node, new FortranElement.Module(getCurrentParent(), token));
        }
    
        public void visitASTFunctionSubprogramNode(ASTFunctionSubprogramNode node)
        {
            Token token = node.getASTFunctionStmt().getASTFunctionName().getASTTident();
            addToModel(node, new FortranElement.Function(getCurrentParent(), token));
        }
    
        public void visitASTSubroutineSubprogramNode(ASTSubroutineSubprogramNode node)
        {
            Token token = node.getASTSubroutineStmt().getASTSubroutineName().getASTTident();
            addToModel(node, new FortranElement.Subroutine(getCurrentParent(), token));
        }
    
        public void visitASTBlockDataSubprogramNode(ASTBlockDataSubprogramNode node)
        {
            Token token = node.getASTBlockDataStmt().getASTBlockDataName() == null
                ? null
                : node.getASTBlockDataStmt().getASTBlockDataName().getASTTident();
            addToModel(node, new FortranElement.BlockData(getCurrentParent(), token));
        }
    
        public void visitASTDerivedTypeDefNode(ASTDerivedTypeDefNode node)
        {
            Token token = node.getASTDerivedTypeStmt().getASTTypeName().getASTTident();
            addToModel(node, new FortranElement.DerivedType(getCurrentParent(), token));
        }
    
        public void visitASTComponentDefStmtNode(ASTComponentDefStmtNode node)
        {
            ASTComponentDeclListNode list = node.getASTComponentDeclList();
            for (int i = 0; i < list.count(); i++)
                addToModel(node, new FortranElement.Variable(getCurrentParent(), list.getASTComponentDecl(i).getASTComponentName().getASTTident()));
        }
    
        public void visitASTExternalStmtNode(ASTExternalStmtNode node)
        {
            ASTExternalNameListNode list = node.getASTExternalNameList();
            for (int i = 0; i < list.count(); i++)
                addToModel(node, new FortranElement.Variable(getCurrentParent(), list.getASTExternalName(i).getASTTident()));
        }
    
        public void visitASTInterfaceBlockNode(ASTInterfaceBlockNode node)
        {
            Token token = node.getASTInterfaceStmt().getASTGenericName() == null
                ? null
                : node.getASTInterfaceStmt().getASTGenericName().getASTTident();
            addToModel(node, new FortranElement.Variable(getCurrentParent(), token));
        }
    
        public void visitASTIntrinsicStmtNode(ASTIntrinsicStmtNode node)
        {
            ASTIntrinsicListNode list = node.getASTIntrinsicList();
            for (int i = 0; i < list.count(); i++)
                addToModel(node, new FortranElement.Variable(getCurrentParent(), list.getASTIntrinsicProcedureName(i).getASTTident()));
        }
        
        public void visitASTStmtFunctionStmtNode(ASTStmtFunctionStmtNode node)
        {
            addToModel(node, new FortranElement.Variable(getCurrentParent(), node.getASTName().getASTTident()));
        }
    }
}
