package org.eclipse.photran.internal.core.model;

import java.util.LinkedList;

import org.eclipse.cdt.core.model.CModelException;
import org.eclipse.cdt.internal.core.model.Parent;
import org.eclipse.photran.internal.core.lexer.Token;
import org.eclipse.photran.internal.core.parser.ASTBlockDataSubprogramNode;
import org.eclipse.photran.internal.core.parser.ASTComponentDeclNode;
import org.eclipse.photran.internal.core.parser.ASTDataComponentDefStmtNode;
import org.eclipse.photran.internal.core.parser.ASTDerivedTypeDefNode;
import org.eclipse.photran.internal.core.parser.ASTExternalNameListNode;
import org.eclipse.photran.internal.core.parser.ASTExternalStmtNode;
import org.eclipse.photran.internal.core.parser.ASTFunctionSubprogramNode;
import org.eclipse.photran.internal.core.parser.ASTGenericBindingNode;
import org.eclipse.photran.internal.core.parser.ASTGenericSpecNode;
import org.eclipse.photran.internal.core.parser.ASTInterfaceBlockNode;
import org.eclipse.photran.internal.core.parser.ASTIntrinsicListNode;
import org.eclipse.photran.internal.core.parser.ASTIntrinsicStmtNode;
import org.eclipse.photran.internal.core.parser.ASTMainProgramNode;
import org.eclipse.photran.internal.core.parser.ASTModuleNode;
import org.eclipse.photran.internal.core.parser.ASTSpecificBindingNode;
import org.eclipse.photran.internal.core.parser.ASTStmtFunctionStmtNode;
import org.eclipse.photran.internal.core.parser.ASTSubroutineSubprogramNode;
import org.eclipse.photran.internal.core.parser.Parser.GenericASTVisitor;
import org.eclipse.photran.internal.core.parser.Parser.IASTListNode;
import org.eclipse.photran.internal.core.parser.Parser.IASTNode;

/**
 * This parse tree visitor is used by the <code>FortranModelBuilder</code> to create the model you
 * see in the (normal) Outline view. When it encounters a program, module, function, or other entity
 * that should be displayed in the model, it calls the <code>FortranModelBuilder</code> back and
 * asks it to add the appropriate node.
 * 
 * @author joverbey
 */
@SuppressWarnings("restriction")
public final class FortranModelBuildingVisitor extends GenericASTVisitor
{
    // --INFRASTRUCTURE--------------------------------------------------

    private org.eclipse.cdt.internal.core.model.TranslationUnit translationUnit;

    private FortranModelBuilder modelBuilder;

    public FortranModelBuildingVisitor(
        org.eclipse.cdt.internal.core.model.TranslationUnit translationUnit,
        FortranModelBuilder modelBuilder)
    {
        this.translationUnit = translationUnit;
        this.modelBuilder = modelBuilder;
    }

    private LinkedList<IASTNode> parentParseTreeNodeStack = new LinkedList<IASTNode>();

    private LinkedList<FortranElement> parentElementStack = new LinkedList<FortranElement>();

    private Parent getCurrentParent()
    {
        if (parentElementStack.isEmpty())
            return translationUnit;
        else
            return (Parent)parentElementStack.getLast();
    }

    private boolean isCurrentParent(IASTNode node)
    {
        if (parentParseTreeNodeStack.isEmpty())
            return false;
        else
            return node == (IASTNode)parentParseTreeNodeStack.getLast();
    }

    private void addToModel(IASTNode parseTreeNode, FortranElement element)
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

    private void beginAddingChildrenFor(IASTNode parseTreeNode, FortranElement element)
    {
        parentParseTreeNodeStack.addLast(parseTreeNode);
        parentElementStack.addLast(element);
    }

    private void doneAddingChildrenFor(IASTNode node)
    {
        if (isCurrentParent(node))
        {
            parentParseTreeNodeStack.removeLast();
            parentElementStack.removeLast();
        }
    }

    // --VISITOR METHODS-------------------------------------------------
    
    @Override public void visitASTNode(IASTNode node)
    {
        // beginAddingChildrenFor is called in addToModel
        traverseChildren(node);
        doneAddingChildrenFor(node);
    }

    private <T extends FortranElement> T setPos(T element, IASTNode astNode)
    {
        Token first = astNode.findFirstToken();
        Token last = astNode.findLastToken();
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

    public void visitASTSpecificBindingNode(ASTSpecificBindingNode node)
    {
        Token token = node.getBindingName();
        addToModel(node, setPos(new FortranElement.Subroutine(getCurrentParent(), token), node));
    }

    public void visitASTGenericBindingNode(ASTGenericBindingNode node)
    {
        Token token;
        if (node.getGenericName() != null)
        {
            token =  node.getGenericName().getGenericName();
        }
        else
        {
            ASTGenericSpecNode spec = node.getGenericSpec();
            if (spec.isAssignmentOperator())
                token = spec.getEqualsToken();
            else if (spec.isDefinedOperator())
                token = spec.getDefinedOperator().findFirstToken();
            else
                token = spec.findFirstToken();
        }
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

    public void visitASTDataComponentDefStmtNode(ASTDataComponentDefStmtNode node)
    {
        for (ASTComponentDeclNode decl : node.getComponentDeclList())
            addToModelNoChildren(setPos(new FortranElement.Variable(getCurrentParent(), decl.getComponentName().getComponentName()), node));
    }

    public void visitASTExternalStmtNode(ASTExternalStmtNode node)
    {
        IASTListNode<ASTExternalNameListNode> list = node.getExternalNameList();
        for (int i = 0; i < list.size(); i++)
            addToModel(node, setPos(new FortranElement.Variable(getCurrentParent(), list.get(i).getExternalName()), node));
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
        IASTListNode<ASTIntrinsicListNode> list = node.getIntrinsicList();
        for (int i = 0; i < list.size(); i++)
            addToModel(node, setPos(new FortranElement.Variable(getCurrentParent(), list.get(i).getIntrinsicProcedureName()), node));
    }
    
    public void visitASTStmtFunctionStmtNode(ASTStmtFunctionStmtNode node)
    {
        addToModel(node, setPos(new FortranElement.Variable(getCurrentParent(), node.getName().getName()), node));
    }
}
