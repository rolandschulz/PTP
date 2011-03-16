/*******************************************************************************
 * Copyright (c) 2010 Joe Handzik, Joe Gonzales, Marc Celani, and Jason Patel.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Joe Handzik, Joe Gonzales, Marc Celani, and Jason Patel - Initial API and implementation
 *******************************************************************************/
package org.eclipse.photran.internal.core.refactoring;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.OperationCanceledException;
import org.eclipse.ltk.core.refactoring.RefactoringStatus;
import org.eclipse.ltk.core.refactoring.RefactoringStatusContext;
import org.eclipse.photran.internal.core.analysis.binding.Definition;
import org.eclipse.photran.internal.core.analysis.binding.ScopingNode;
import org.eclipse.photran.internal.core.lexer.Terminal;
import org.eclipse.photran.internal.core.lexer.Token;
import org.eclipse.photran.internal.core.parser.ASTCallStmtNode;
import org.eclipse.photran.internal.core.parser.ASTEntityDeclNode;
import org.eclipse.photran.internal.core.parser.ASTIntConstNode;
import org.eclipse.photran.internal.core.parser.ASTListNode;
import org.eclipse.photran.internal.core.parser.ASTSeparatedListNode;
import org.eclipse.photran.internal.core.parser.ASTSubroutineArgNode;
import org.eclipse.photran.internal.core.parser.ASTSubroutineParNode;
import org.eclipse.photran.internal.core.parser.ASTSubroutineStmtNode;
import org.eclipse.photran.internal.core.parser.ASTSubroutineSubprogramNode;
import org.eclipse.photran.internal.core.parser.ASTTypeDeclarationStmtNode;
import org.eclipse.photran.internal.core.parser.IASTListNode;
import org.eclipse.photran.internal.core.parser.IASTNode;
import org.eclipse.photran.internal.core.parser.IBodyConstruct;
import org.eclipse.photran.internal.core.refactoring.infrastructure.FortranEditorRefactoring;
import org.eclipse.photran.internal.core.reindenter.Reindenter;
import org.eclipse.photran.internal.core.vpg.PhotranTokenRef;
import org.eclipse.photran.internal.core.vpg.PhotranVPG;

/**
 * 
 * 
 * This refactoring allows a user to select a subroutine and to add a new parameter to the list. The
 * refactoring will ask for a declaration line for the parameter, a default value with which to
 * update all callers of the subroutine, and a position in the list at which to add the new
 * parameter. The refactoring ensures that the declaration line is valid, contains some logic to
 * ensure that the default value matches the appropriate type, and ensures that the position is in
 * bounds. It then updates the subroutine signature and updates the callers of the subroutine. If
 * the callers specify the variable name in the call list, the refactoring will match this pattern.
 * 
 * @author Joe Handzik, Joe Gonzales, Marc Celani, Jason Patel
 */
public class AddSubroutineParameterRefactoring extends FortranEditorRefactoring
{
    private ASTSubroutineStmtNode selectedSubroutine;

    private List<ASTSubroutineParNode> oldParameterList;

    private List<ASTSubroutineParNode> newParameterList;

    private int position = 0;

    private String parameterName = null;

    private String declaration = "integer, intent(in) :: newName"; //$NON-NLS-1$

    private String defaultValue = "0"; //$NON-NLS-1$

    private ASTTypeDeclarationStmtNode declStmt = null;

    private String type = "integer"; //$NON-NLS-1$

    public List<ASTSubroutineParNode> getOldParameterList()
    {
        return oldParameterList;
    }

    public String getDeclaration()
    {
        assert declaration != null;
        return this.declaration;
    }

    public int getPosition()
    {
        return this.position;
    }

    public String getDefault()
    {
        assert defaultValue != null;
        return this.defaultValue;
    }

    public void setPosition(int position)
    {
        this.position = position;
    }

    /*
     * Sets the declaration member, but first checks that an appropriate type is at the beggining of
     * the declaration. If not, it assumes that "real" should be prepended. The type member is then
     * set to this type.
     * 
     * @param declaration The declaration to be set.
     */
    public void setDeclaration(String declaration)
    {
        // Add "real" to all declaration lines that do not specify a type to avoid parser errors.

        String[] declArgs = declaration.split(","); //$NON-NLS-1$
        String[] validTypes = { "integer", "real", "logical", "double", "character" }; //$NON-NLS-1$ //$NON-NLS-2$//$NON-NLS-3$ //$NON-NLS-4$ //$NON-NLS-5$
        boolean hasTypeDefined = false;
        for (int i = 0; i < validTypes.length; i++)
        {
            if (validTypes[i].equals(declArgs[0]))
            {
                hasTypeDefined = true;
                type = declArgs[0];
                break;
            }
        }

        if (!hasTypeDefined)
        {
            type = "real"; //$NON-NLS-1$
            if (declArgs.length == 1)
                declaration = "real, " + declaration; //$NON-NLS-1$
            else
                declaration = "real" + " :: " + declaration; //$NON-NLS-1$ //$NON-NLS-2$
        }

        this.declaration = declaration;
    }

    public void setDefaultValue(String defValue)
    {
        defaultValue = defValue;
    }

    /*
     * (non-Javadoc)
     * 
     * @see
     * org.eclipse.rephraserengine.core.vpg.refactoring.VPGRefactoring#doCheckInitialConditions(
     * org.eclipse.ltk.core.refactoring.RefactoringStatus,
     * org.eclipse.core.runtime.IProgressMonitor)
     */
    @Override
    protected void doCheckInitialConditions(RefactoringStatus status, IProgressMonitor pm)
        throws org.eclipse.rephraserengine.core.vpg.refactoring.VPGRefactoring.PreconditionFailure
    {
        ensureProjectHasRefactoringEnabled(status);

        ensureSubroutineIsSelected();

        if (!matchingDeclarationsInInterfacesUniquelyBind())
            status
                .addWarning(Messages.AddSubroutineParameterRefactoring_matchingDeclarationsDoNotUniquelyBind);

        oldParameterList = getSubroutineParameters();
    }

    /*
     * By looking at the AST tree, starting at the node supplied to the refactoring as the selected
     * node, this method determines if a subroutine node has been selected or not.
     */
    private void ensureSubroutineIsSelected()
        throws org.eclipse.rephraserengine.core.vpg.refactoring.VPGRefactoring.PreconditionFailure
    {
        IASTNode temporaryNode = findEnclosingNode(astOfFileInEditor, selectedRegionInEditor);

        if (temporaryNode == null)
            fail(Messages.AddSubroutineParameterRefactoring_selectSubroutineError);

        if (temporaryNode instanceof ASTSubroutineSubprogramNode)
            selectedSubroutine = ((ASTSubroutineSubprogramNode)temporaryNode).getSubroutineStmt();
        else if (temporaryNode instanceof ASTSubroutineStmtNode)
        {
            if (temporaryNode.findNearestAncestor(ASTSubroutineSubprogramNode.class) == null)
                fail(Messages.AddSubroutineParameterRefactoring_selectSubroutineError);
            selectedSubroutine = (ASTSubroutineStmtNode)temporaryNode;
        }
        else
            fail(Messages.AddSubroutineParameterRefactoring_selectSubroutineError);
    }

    /*
     * This method determines if a matching declaration already exists in scope, and if so, will
     * fail the refactoring.
     */
    private boolean matchingDeclarationsInInterfacesUniquelyBind()
    {
        for (Definition declaration : getInterfaceDeclarations())
            if (declaration.resolveInterfaceBinding().size() != 1) return false;

        return true;
    }

    /*
     * (non-Javadoc)
     * 
     * @see
     * org.eclipse.rephraserengine.core.vpg.refactoring.VPGRefactoring#doCheckFinalConditions(org
     * .eclipse.ltk.core.refactoring.RefactoringStatus, org.eclipse.core.runtime.IProgressMonitor)
     */
    @Override
    protected void doCheckFinalConditions(RefactoringStatus status, IProgressMonitor pm)
        throws org.eclipse.rephraserengine.core.vpg.refactoring.VPGRefactoring.PreconditionFailure
    {
        ensureDeclarationIsValid();

        parameterName = declStmt.getEntityDeclList().get(0).getObjectName().getObjectName()
            .getText();

        ensurePositionIsValid();

        ensureDefaultValueIsValid();

        checkForConflictingBindings(pm, status);
    }

    /*
     * This method ensures that the default value supplied is valid by applying logic that tests
     * whether or not the default type supplied matches the type supplied in the declaration line.
     * For example, .true. and .false. are reserved for logical types. This method also ensures that
     * the default value is not a variable name beginning with a number.
     */
    private void ensureDefaultValueIsValid()
        throws org.eclipse.rephraserengine.core.vpg.refactoring.VPGRefactoring.PreconditionFailure
    {
        if (defaultValue == null || defaultValue.equals("") || //$NON-NLS-1$
            isWhiteSpace(defaultValue) || isVariableNameBeginningWithNumber(defaultValue)
            || (isTrueOrFalse(defaultValue) && !type.equals("logical")) || //$NON-NLS-1$
            (isANumber(defaultValue) && (!type.equals("integer") && !type.equals("real"))) || //$NON-NLS-1$//$NON-NLS-2$
            (isRealAndNotInteger(defaultValue) && type.equals("integer")) || //$NON-NLS-1$
            (defaultValue.equals("null") && !declaration.contains("pointer"))) //$NON-NLS-1$ //$NON-NLS-2$
            fail(Messages.AddSubroutineParameterRefactoring_InvalidDefaultValue);
    }

    /*
     * @param str A string to be tested
     * 
     * @return <code> true </code> if the selected string is a real number, and <code> false </code>
     * if the selected string is ann integer or not a number.
     */
    private boolean isRealAndNotInteger(String str)
    {
        if (isANumber(str))
        {
            try
            {
                Integer.parseInt(str);
            }
            catch (NumberFormatException e)
            {
                return true;
            }
        }
        return false;
    }

    /*
     * @param str A string to be tested
     * 
     * @return <code> true </code> if the selected string is ".true." or ".false.", and <code> false
     * </code> if the selected string is anything else.
     */
    private boolean isTrueOrFalse(String str)
    {
        if (str == null) return false;
        return str.equals(".true.") || str.equals(".false."); //$NON-NLS-1$ //$NON-NLS-2$
    }

    /*
     * @param str A string to be tested
     * 
     * @return <code> true </code> if the string begins with a number but is not a number (hence, a
     * variable name beginning with a number) and <code> false </code> otherwise.
     */
    private boolean isVariableNameBeginningWithNumber(String str)
    {
        if (str != null)
        {
            if (str.length() != 0)
            {
                if (isANumber(str.substring(0, 1)))
                {
                    if (!isANumber(str)) return true;
                }
            }
        }

        return false;
    }

    /*
     * @param str A string
     * 
     * @return <code> true </code> if str is a number.
     */
    private boolean isANumber(String str)
    {
        try
        {
            Double.parseDouble(str);
        }
        catch (NumberFormatException e)
        {
            return false;
        }
        return true;
    }

    /*
     * This function ensures that the position given to the refactoring is in bounds for the current
     * size of the list, and if not, fails the refactoring.
     */
    private void ensurePositionIsValid()
        throws org.eclipse.rephraserengine.core.vpg.refactoring.VPGRefactoring.PreconditionFailure
    {
        if (position > oldParameterList.size() || position < 0)
            fail(Messages.AddSubroutineParameterRefactoring_InvalidParameterPosition);
    }

    /*
     * This function attempts to produce a declaration node by passing the declaration line on to a
     * parser. If this node is returned as an error node, the refactoring fails.
     */
    private void ensureDeclarationIsValid()
        throws org.eclipse.rephraserengine.core.vpg.refactoring.VPGRefactoring.PreconditionFailure
    {
        IBodyConstruct decl = parseLiteralStatementNoFail(declaration);
        if (decl == null || !(decl instanceof ASTTypeDeclarationStmtNode))
            fail(Messages.AddSubroutineParameterRefactoring_InvalidDeclaration);
        declStmt = (ASTTypeDeclarationStmtNode)decl;
        IASTListNode<ASTEntityDeclNode> entityDeclList = declStmt.getEntityDeclList();
        if (entityDeclList == null)
        {
            fail(Messages.AddSubroutineParameterRefactoring_InvalidDeclaration);
        }
    }

    /*
     * This function checks to see whether or not the variable name supplied to the refactoring is
     * already in scope in the subroutine.
     */
    private void checkForConflictingBindings(IProgressMonitor pm, RefactoringStatus status)
    {
        Definition def = arbitraryDefinitionInScope();
        if (def == null) return; // No declarations in scope, so the new one can't conflict

        checkForConflictingBindings(pm, new ConflictingBindingErrorHandler(status), def,
            Collections.<PhotranTokenRef> emptyList(), parameterName);
    }

    /*
     * This function returns an arbitrary definition line in scope of the current node. It is used
     * to iterate through the declarations to see if there are any conflicts.
     */
    private Definition arbitraryDefinitionInScope()
    {
        ScopingNode enclosingScope = selectedSubroutine.findNearestAncestor(ScopingNode.class);
        List<Definition> allDefs = enclosingScope.getAllDefinitions();
        if (allDefs.isEmpty())
            return null;
        else
            return allDefs.get(0);
    }

    /*
     * @param str A string
     * 
     * @return <code> true </code> if str is only white space. This is used to test if default
     * values are nothing but white space.
     */
    private boolean isWhiteSpace(String str)
    {
        return str.replace(" ", "").replace("\t", "").equals(""); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$ //$NON-NLS-5$
    }

    /*
     * (non-Javadoc)
     * 
     * @see
     * org.eclipse.rephraserengine.core.vpg.refactoring.VPGRefactoring#doCreateChange(org.eclipse
     * .core.runtime.IProgressMonitor)
     */
    @Override
    protected void doCreateChange(IProgressMonitor pm) throws CoreException,
        OperationCanceledException
    {

        buildNewParameterListWithNewParameter();

        // Change the arguments list to the new list
        permuteArgumentList(selectedSubroutine);

        addArgumentDeclaration(selectedSubroutine);

        permuteCallSites();

        addChangeFromModifiedAST(fileInEditor, pm);
        vpg.releaseAST(fileInEditor);

    }

    /*
     * This function adds the declaration line to the subroutine.
     */
    private void addArgumentDeclaration(ASTSubroutineStmtNode subroutineStatement)
    {
        ASTSubroutineSubprogramNode subroutine = (ASTSubroutineSubprogramNode)subroutineStatement
            .getParent();

        IASTListNode<IBodyConstruct> statements = subroutine.getBody();
        if (statements == null)
        {
            statements = new ASTListNode<IBodyConstruct>();
            subroutine.setBody(statements);
        }

        statements.add(0, declStmt);
        Reindenter.reindent(declStmt, astOfFileInEditor);

    }

    /*
     * (non-Javadoc)
     * 
     * @see org.eclipse.ltk.core.refactoring.Refactoring#getName()
     */
    @Override
    public String getName()
    {
        return Messages.AddSubroutineParameterRefactoring_Name;
    }

    /*
     * This function returns the list of subroutine parameters from the selected subroutine node.
     */
    public List<ASTSubroutineParNode> getSubroutineParameters()
    {
        if (selectedSubroutine.getSubroutinePars() != null)
            return selectedSubroutine.getSubroutinePars();

        return new ArrayList<ASTSubroutineParNode>();
    }

    /*
     * This function returns a collection of interface declarations.
     */
    private Collection<Definition> getInterfaceDeclarations()
    {
        List<Definition> subroutineDefinitions = selectedSubroutine.getSubroutineName()
            .getSubroutineName().resolveBinding();

        if (subroutineDefinitions.size() != 1) return new ArrayList<Definition>();

        return subroutineDefinitions.get(0).findMatchingDeclarationsInInterfaces();
    }

    /*
     * This function builds the new parameter list to be supplied to the subroutine node by adding
     * the new parameter to the list in the appropriate position.
     */
    public void buildNewParameterListWithNewParameter()
    {
        // Create new variable
        ASTSubroutineParNode newParameter = new ASTSubroutineParNode();
        Token variableName = generateVariableName();
        newParameter.setVariableName(variableName);

        // Create new list
        newParameterList = new ArrayList<ASTSubroutineParNode>(oldParameterList);
        newParameterList.add(position, newParameter);

    }

    /*
     * This function returns a token for a variable with the name of the new parameter name.
     */
    private Token generateVariableName()
    {
        Token variableName = new Token(Terminal.T_IDENT, parameterName);
        return variableName;
    }

    /*
     * This function changes the argument list of the subroutine statement node to the new list
     * generated in buildNewParameterListWithNewParameter()
     */
    protected void permuteArgumentList(ASTSubroutineStmtNode node)
    {
        ASTSeparatedListNode<ASTSubroutineParNode> newParameterList = new ASTSeparatedListNode<ASTSubroutineParNode>(
            new Token(Terminal.T_COMMA, ","), this.newParameterList); //$NON-NLS-1$
        node.setSubroutinePars(newParameterList);
    }

    /*
     * This function changes all call sites to be updated to have the new argument in place, and
     * will match any calling pattern currently used.
     */
    private void permuteCallSites()
    {
        for (ASTCallStmtNode callStmt : getCallSites())
        {
            int previousArgumentListSize = 0;

            if (callStmt.getArgList() != null)
            {
                previousArgumentListSize = callStmt.getArgList().size();
            }

            // Generate new IExpression Node for the default value
            ASTIntConstNode expr = new ASTIntConstNode();
            expr.setIntConst(new Token(Terminal.T_ICON, defaultValue));
            ASTSubroutineArgNode addedParArg = new ASTSubroutineArgNode();
            addedParArg.setExpr(expr);

            // Test to see if the call site is using the
            // "(variableName = value, variablename = value)" pattern, or simply the
            // "(value, value)" pattern
            // The new parameter should follow this pattern at the call site, and should assume the
            // (value) pattern if the list was previously empty.
            if (previousArgumentListSize > 0)
            {
                int positionToCompareTo = Math.min(position, previousArgumentListSize - 1);
                ASTSubroutineParNode firstParameter = oldParameterList.get(positionToCompareTo);
                ASTSubroutineArgNode firstParameterArgument = getActualArgFromCallStmt(callStmt,
                    firstParameter.getVariableName(), positionToCompareTo);
                if (firstParameterArgument.getName() != null)
                    addedParArg.setName(new Token(Terminal.T_IDENT, parameterName));
            }

            ArrayList<ASTSubroutineArgNode> newParameterListForCallSite = new ArrayList<ASTSubroutineArgNode>();

            for (int i = 0; i < previousArgumentListSize; i++)
            {
                ASTSubroutineParNode desiredPar = oldParameterList.get(i);
                ASTSubroutineArgNode desiredParArgument = getActualArgFromCallStmt(callStmt,
                    desiredPar.getVariableName(), i);
                newParameterListForCallSite.add(desiredParArgument);
            }

            newParameterListForCallSite.add(position, addedParArg);

            ASTSeparatedListNode<ASTSubroutineArgNode> newArgList = new ASTSeparatedListNode<ASTSubroutineArgNode>(
                new Token(Terminal.T_COMMA, ","), newParameterListForCallSite); //$NON-NLS-1$
            callStmt.setArgList(newArgList);
        }
    }

    /*
     * This function returns the set of call sites for the subroutine that was selected.
     */
    private Set<ASTCallStmtNode> getCallSites()
    {
        List<Definition> subroutineDefinitions = selectedSubroutine.getSubroutineName()
            .getSubroutineName().resolveBinding();
        HashSet<ASTCallStmtNode> result = new HashSet<ASTCallStmtNode>();

        if (subroutineDefinitions.size() != 1) return result;

        for (PhotranTokenRef tokenRef : subroutineDefinitions.get(0).findAllReferences(true))
        {
            Token token = tokenRef.findToken();

            ASTCallStmtNode callStmtNode = token.findNearestAncestor(ASTCallStmtNode.class);

            if (callStmtNode != null) result.add(callStmtNode);
        }

        return result;
    }

    /*
     * This function gets an argument from a call statement, in order to check if it follows the
     * pattern of "Variablename = Value".
     */
    private ASTSubroutineArgNode getActualArgFromCallStmt(ASTCallStmtNode callStmt,
        Token desiredParName, int desiredParIndex)
    {
        for (int i = 0; i < callStmt.getArgList().size(); i++)
        {
            ASTSubroutineArgNode argument = callStmt.getArgList().get(i);
            if (argument.getName() == null || desiredParName == null)
            {
                if (i == desiredParIndex) return argument;
            }
            else
            {
                String argumentName = PhotranVPG.canonicalizeIdentifier(argument.getName()
                    .getText());
                String parameterName = PhotranVPG.canonicalizeIdentifier(desiredParName.getText());
                if (argumentName.equals(parameterName)) return argument;
            }
        }
        return null;
    }

    /*
     * This class handles all error cases for conflicting variable names or bindings.
     */
    private final class ConflictingBindingErrorHandler implements IConflictingBindingCallback
    {
        private final RefactoringStatus status;

        private ConflictingBindingErrorHandler(RefactoringStatus status)
        {
            this.status = status;
        }

        public void addConflictError(List<Conflict> conflictingDef)
        {
            Conflict conflict = conflictingDef.get(0);

            String msg = Messages.bind(
                Messages.AddSubroutineParameterRefactoring_NameConflictsWith, conflict.name,
                vpg.getDefinitionFor(conflict.tokenRef));
            RefactoringStatusContext context = createContext(conflict.tokenRef); // Highlights
                                                                                 // problematic
                                                                                 // definition
            status.addError(msg, context);
        }

        public void addConflictWarning(List<Conflict> conflictingDef)
        {
            Conflict conflict = conflictingDef.get(0);

            String msg = Messages.bind(
                Messages.AddSubroutineParameterRefactoring_NameMightConflictWithSubprogram,
                conflict.name);
            RefactoringStatusContext context = createContext(conflict.tokenRef); // Highlights
                                                                                 // problematic
                                                                                 // definition
            status.addWarning(msg, context);
        }

        public void addReferenceWillChangeError(String newName, Token reference)
        {
            throw new IllegalStateException();
        }
    }

}
