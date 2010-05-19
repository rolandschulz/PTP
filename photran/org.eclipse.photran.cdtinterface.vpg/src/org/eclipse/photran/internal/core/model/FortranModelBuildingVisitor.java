package org.eclipse.photran.internal.core.model;

import java.util.LinkedList;

import org.eclipse.cdt.core.model.CModelException;
import org.eclipse.cdt.internal.core.model.Parent;
import org.eclipse.photran.internal.core.lexer.Token;
import org.eclipse.photran.internal.core.parser.ASTBlockDataSubprogramNode;
import org.eclipse.photran.internal.core.parser.ASTComponentDeclNode;
import org.eclipse.photran.internal.core.parser.ASTDataComponentDefStmtNode;
import org.eclipse.photran.internal.core.parser.ASTDerivedTypeDefNode;
import org.eclipse.photran.internal.core.parser.ASTErrorConstructNode;
import org.eclipse.photran.internal.core.parser.ASTErrorProgramUnitNode;
import org.eclipse.photran.internal.core.parser.ASTExternalNameListNode;
import org.eclipse.photran.internal.core.parser.ASTExternalStmtNode;
import org.eclipse.photran.internal.core.parser.ASTFunctionSubprogramNode;
import org.eclipse.photran.internal.core.parser.ASTGenericBindingNode;
import org.eclipse.photran.internal.core.parser.ASTGenericSpecNode;
import org.eclipse.photran.internal.core.parser.ASTInterfaceBlockNode;
import org.eclipse.photran.internal.core.parser.ASTInterfaceBodyNode;
import org.eclipse.photran.internal.core.parser.ASTIntrinsicListNode;
import org.eclipse.photran.internal.core.parser.ASTIntrinsicStmtNode;
import org.eclipse.photran.internal.core.parser.ASTMainProgramNode;
import org.eclipse.photran.internal.core.parser.ASTModuleNode;
import org.eclipse.photran.internal.core.parser.ASTModuleProcedureStmtNode;
import org.eclipse.photran.internal.core.parser.ASTNodeWithErrorRecoverySymbols;
import org.eclipse.photran.internal.core.parser.ASTProcedureNameListNode;
import org.eclipse.photran.internal.core.parser.ASTSpecificBindingNode;
import org.eclipse.photran.internal.core.parser.ASTStmtFunctionStmtNode;
import org.eclipse.photran.internal.core.parser.ASTSubmoduleNode;
import org.eclipse.photran.internal.core.parser.ASTSubroutineSubprogramNode;
import org.eclipse.photran.internal.core.parser.GenericASTVisitor;
import org.eclipse.photran.internal.core.parser.IASTListNode;
import org.eclipse.photran.internal.core.parser.IASTNode;

/**
 * THIS IS AN INTERNAL CLASS.
 * <p>
 * This AST visitor is used by the {@link FortranModelBuilder} to construct the C model of a
 * particular file.
 * <p>
 * Internally, when this visitor encounters a program, module, function, or other entity that should
 * be displayed in the model, it calls back to the {@link FortranModelBuilder} and asks it to add an
 * appropriate node to the model (via {@link FortranModelBuilder#addF90Element(FortranElement)}).
 *
 * @author Jeff Overbey
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
    
    private FortranElement errorElement = null;

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

    private FortranElement getErrorElement()
    {
        if (errorElement == null)
        {
            errorElement = new FortranElement.ErrorNode(translationUnit, "Syntax Errors"); //$NON-NLS-1$
            addToModelNoChildren(errorElement);
        }

        return errorElement;
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
        return setPos(element, astNode, false);
    }

    private <T extends FortranElement> T setPos(T element, IASTNode astNode, boolean setIdPos)
    {
        Token first = astNode.findFirstToken();
        Token last = astNode.findLastToken();
        if (first != null && last != null)
        {
            int offset = first.getFileOffset();
            int length = last.getFileOffset()+last.getLength()-offset;
            if (setIdPos)
                element.setIdPos(offset, length);
            element.setPos(offset, length);
            element.setLines(first.getLine(), last.getLine());
        }
        return element;
    }

    public void visitASTErrorProgramUnitNode(ASTErrorProgramUnitNode node)
    {
        addToModelNoChildren(setPos(configureElement(new FortranElement.ErrorNode(getCurrentParent(), "Erroneous program unit - " + describeError(node)), node.getErrorToken()), node, true)); //$NON-NLS-1$
        addToModelNoChildren(configureElement(new FortranElement.ErrorNode(getErrorElement(), describeError(node)), node.getErrorToken()));
    }

    public void visitASTErrorConstructNode(ASTErrorConstructNode node)
    {
        addToModelNoChildren(setPos(configureElement(new FortranElement.ErrorNode(getCurrentParent(), "Unrecognized statement or construct - " + describeError(node)), node.getErrorToken()), node, true)); //$NON-NLS-1$
        addToModelNoChildren(configureElement(new FortranElement.ErrorNode(getErrorElement(), describeError(node)), node.getErrorToken()));
    }
    
    private String describeError(ASTNodeWithErrorRecoverySymbols node)
    {
        StringBuilder sb = new StringBuilder();
        sb.append("Unexpected "); //$NON-NLS-1$
        sb.append(node.getErrorToken());
        sb.append(" (line "); //$NON-NLS-1$
        sb.append(node.getErrorToken().getLine());
        sb.append(", column "); //$NON-NLS-1$
        sb.append(node.getErrorToken().getCol());
        sb.append(").  Expected one of the following: "); //$NON-NLS-1$
        sb.append(node.describeTerminalsExpectedAtErrorPoint());
        return sb.toString();
    }

    public void visitASTMainProgramNode(ASTMainProgramNode node)
    {
        Token token = node.getProgramStmt() == null
            ? null
            : node.getProgramStmt().getProgramName().getProgramName();
        addToModel(node, setPos(configureElement(new FortranElement.MainProgram(getCurrentParent()), token), node));
    }

    public void visitASTModuleNode(ASTModuleNode node)
    {
        Token token = node.getModuleStmt().getModuleName().getModuleName();
        addToModel(node, setPos(configureElement(new FortranElement.Module(getCurrentParent()), token), node));
    }

    public void visitASTSubmoduleNode(ASTSubmoduleNode node)
    {
        Token token = node.getSubmoduleStmt().getSubmoduleName().getModuleName();
        addToModel(node, setPos(configureElement(new FortranElement.Submodule(getCurrentParent()), token), node));
    }

    public void visitASTFunctionSubprogramNode(ASTFunctionSubprogramNode node)
    {
        Token token = node.getFunctionStmt().getFunctionName().getFunctionName();
        addToModel(node, setPos(configureElement(new FortranElement.Function(getCurrentParent()), token), node));
    }

    public void visitASTSubroutineSubprogramNode(ASTSubroutineSubprogramNode node)
    {
        Token token = node.getSubroutineStmt().getSubroutineName().getSubroutineName();
        addToModel(node, setPos(configureElement(new FortranElement.Subroutine(getCurrentParent()), token), node));
    }

    public void visitASTSpecificBindingNode(ASTSpecificBindingNode node)
    {
        Token token = node.getBindingName();
        addToModel(node, setPos(configureElement(new FortranElement.Subroutine(getCurrentParent()), token), node));
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
        addToModel(node, setPos(configureElement(new FortranElement.Subroutine(getCurrentParent()), token), node));
    }

    public void visitASTBlockDataSubprogramNode(ASTBlockDataSubprogramNode node)
    {
        Token token = node.getBlockDataStmt().getBlockDataName() == null
            ? null
            : node.getBlockDataStmt().getBlockDataName().getBlockDataName();
        addToModel(node, setPos(configureElement(new FortranElement.BlockData(getCurrentParent()), token), node));
    }

    public void visitASTDerivedTypeDefNode(ASTDerivedTypeDefNode node)
    {
        Token token = node.getDerivedTypeStmt().getTypeName();
        addToModel(node, setPos(configureElement(new FortranElement.DerivedType(getCurrentParent()), token), node));
    }

    public void visitASTDataComponentDefStmtNode(ASTDataComponentDefStmtNode node)
    {
        for (ASTComponentDeclNode decl : node.getComponentDeclList())
            addToModelNoChildren(setPos(configureElement(new FortranElement.Variable(getCurrentParent()), decl.getComponentName().getComponentName()), node));
    }

    public void visitASTExternalStmtNode(ASTExternalStmtNode node)
    {
        IASTListNode<ASTExternalNameListNode> list = node.getExternalNameList();
        for (int i = 0; i < list.size(); i++)
            addToModel(node, setPos(configureElement(new FortranElement.Subprogram(getCurrentParent()), list.get(i).getExternalName()), node));
    }

    public void visitASTInterfaceBlockNode(ASTInterfaceBlockNode node)
    {
        Token token = node.getInterfaceStmt().getGenericName() == null
            ? node.getInterfaceStmt().getInterfaceToken()
            : node.getInterfaceStmt().getGenericName().getGenericName();
        addToModel(node, setPos(configureElement(new FortranElement.Subprogram(getCurrentParent()), token), node));
    }

    public void visitASTInterfaceBodyNode(ASTInterfaceBodyNode node)
    {
        if (node.getFunctionStmt() != null)
        {
            Token token = node.getFunctionStmt().getFunctionName().getFunctionName();
            addToModel(node, setPos(configureElement(new FortranElement.Function(getCurrentParent()), token), node));
        }
        else if (node.getSubroutineStmt() != null)
        {
            Token token = node.getSubroutineStmt().getSubroutineName().getSubroutineName();
            addToModel(node, setPos(configureElement(new FortranElement.Subroutine(getCurrentParent()), token), node));
        }
    }

    public void visitASTModuleProcedureStmtNode(ASTModuleProcedureStmtNode node)
    {
        IASTListNode<ASTProcedureNameListNode> list = node.getProcedureNameList();
        for (int i = 0; i < list.size(); i++)
            addToModel(node, setPos(configureElement(new FortranElement.Subprogram(getCurrentParent()), list.get(i).getProcedureName()), node));
    }

    public void visitASTIntrinsicStmtNode(ASTIntrinsicStmtNode node)
    {
        IASTListNode<ASTIntrinsicListNode> list = node.getIntrinsicList();
        for (int i = 0; i < list.size(); i++)
            addToModel(node, setPos(configureElement(new FortranElement.Variable(getCurrentParent()), list.get(i).getIntrinsicProcedureName()), node));
    }

    public void visitASTStmtFunctionStmtNode(ASTStmtFunctionStmtNode node)
    {
        addToModel(node, setPos(configureElement(new FortranElement.Variable(getCurrentParent()), node.getName().getName()), node));
    }

    private FortranElement configureElement(FortranElement elt, Token nameToken)
    {
        modelBuilder.configureElement(elt, nameToken);
        return elt;
    }
}
