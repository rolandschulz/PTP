/*******************************************************************************
 * Copyright (c) 2010 Zeeshan Ansari, Mark Chen, Burim Isai, Waseem Sheikh, Mumtaz Vauhkonen. 
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Zeeshan Ansari
 *    Mark Chen
 *    Mumtaz Vauhkonen
 *    Burim Isai
 *    Waseem Sheikh
 *******************************************************************************/
package org.eclipse.photran.internal.core.refactoring;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.OperationCanceledException;
import org.eclipse.ltk.core.refactoring.RefactoringStatus;
import org.eclipse.photran.internal.core.parser.ASTIfConstructNode;
import org.eclipse.photran.internal.core.parser.ASTIfStmtNode;
import org.eclipse.photran.internal.core.parser.ASTNode;
import org.eclipse.photran.internal.core.parser.IExecutionPartConstruct;
import org.eclipse.photran.internal.core.refactoring.infrastructure.FortranEditorRefactoring;
import org.eclipse.photran.internal.core.reindenter.Reindenter;
import org.eclipse.photran.internal.core.reindenter.Reindenter.Strategy;

/**
 * Converts an IF construct to an IF statement and vice versa. User must select the entire IF
 * statement or IF construct block, and select the refactoring option in the menu.
 * 
 * @author Zeeshan Ansari
 * @author Mark Chen
 * @author Mumtaz Vauhkonrn
 * @author Burim Isai
 * @author Waseem Sheikh
 */
public class IfConstructStatementConversionRefactoring extends FortranEditorRefactoring
{
    private ASTNode selectedNode = null;
    private boolean shouldAddEmptyElseBlock = false;

    /**
     * Beyond the standard condition checks, this checks to ensure that a valid IF statement or IF
     * construct is selected and is refactorable.
     * 
     * @param ifConstructNode
     * @throws PreconditionFailure
     */
    @Override
    protected void doCheckInitialConditions(RefactoringStatus status, IProgressMonitor pm)
        throws PreconditionFailure
    {
        ensureProjectHasRefactoringEnabled(status);

        if (!fileInEditor.exists())
            fail(Messages.FortranEditorRefactoring_CantPerformRefactoringOnFileThatDoesNotExist);

        if (fileInEditor.isReadOnly())
            fail(Messages.FortranEditorRefactoring_CantPerformRefactoringOnReadOnlyFile);

        ASTIfStmtNode ifStmtNode = getNode(this.astOfFileInEditor, this.selectedRegionInEditor, ASTIfStmtNode.class);
        ASTIfConstructNode ifConstructNode = getNode(this.astOfFileInEditor, this.selectedRegionInEditor, ASTIfConstructNode.class);

        if (ifStmtNode != null)
            selectedNode = ifStmtNode;
        else if (ifConstructNode != null)
        {
            checkRefactorableConstruct(ifConstructNode);
            selectedNode = ifConstructNode;
        }
        else
            fail(Messages.IfConstructStatementConversionRefactoring_SelectAValidIfStatement);
    }

    /**
     * Checks various conditions to see if the user-selected IF construct is refactorable to an IF
     * statement. This includes making sure there is only one valid statement line in the construct
     * and that the construct is not named.
     * 
     * @param ifConstructNode
     * @throws PreconditionFailure
     */
    private void checkRefactorableConstruct(ASTIfConstructNode ifConstructNode) throws PreconditionFailure
    {
        // Checks for named construct
        if (ifConstructNode.getIfThenStmt().getName() != null)
            fail(Messages.IfConstructStatementConversionRefactoring_InvalidNamedConstruct);
        
        // Check for multiple statements within construct
        if (ifConstructNode.getConditionalBody().size() > 1
                || ifConstructNode.getElseIfConstruct() != null
                || ifConstructNode.getElseConstruct() != null)
            fail(Messages.IfConstructStatementConversionRefactoring_TooManyStatements);
    }

    public boolean isStmtNode()
    {
        return selectedNode != null && selectedNode instanceof ASTIfStmtNode;
    }

    public void setAddEmptyElseBlock()
    {
        shouldAddEmptyElseBlock = true;
    }

    @Override
    protected void doCheckFinalConditions(RefactoringStatus status, IProgressMonitor pm)
        throws PreconditionFailure
    {
        // No final preconditions
    }

    /**
     * Determines whether an IF statement is selected or an IF construct is selected (done in
     * pre-condition). Depending on which, it will execute the appropriate refactoring (statement to
     * construct or vise versa). It will then reindent the entire section of refactored code based
     * on the formating context of the code around it.
     * 
     * @param pm
     * @throws CoreException, OperationCanceledException
     */
    @Override
    protected void doCreateChange(IProgressMonitor pm) throws CoreException,
        OperationCanceledException
    {
        if (selectedNode instanceof ASTIfStmtNode)
            refactorIfStmt();
        else if (selectedNode instanceof ASTIfConstructNode)
            refactorIfConstruct();
        else
            throw new IllegalStateException();

        Reindenter.reindent(selectedNode, this.astOfFileInEditor, Strategy.REINDENT_EACH_LINE);
        this.addChangeFromModifiedAST(this.fileInEditor, pm);

    }

    protected void refactorIfStmt()
    {
        ASTIfStmtNode ifStmtNode = (ASTIfStmtNode)selectedNode;
        ifStmtNode.replaceWith(createNewIfConstruct(ifStmtNode));
    }

    protected void refactorIfConstruct()
    {
        ASTIfConstructNode ifConstructNode = (ASTIfConstructNode)selectedNode;
        ifConstructNode.replaceWith(createNewIfStmt(ifConstructNode));
    }

    /**
     * Creates a new IF statement from the selected IF construct
     * 
     * @param ifConstructNode
     */
    private ASTIfStmtNode createNewIfStmt(ASTIfConstructNode ifConstructNode)
    {
        StringBuilder sb = new StringBuilder();

        sb.append("    if ("); //$NON-NLS-1$
        sb.append(ifConstructNode.getIfThenStmt().getGuardingExpression().toString().trim());
        sb.append(") "); //$NON-NLS-1$
        
        if (!ifConstructNode.getConditionalBody().isEmpty())
        {
            IExecutionPartConstruct stmt = ifConstructNode.getConditionalBody().get(0);
            
            String leadingComments = stmt.findFirstToken().getWhiteBefore().trim();
            if (!leadingComments.equals("")) //$NON-NLS-1$
            {
                sb.append("&\n"); //$NON-NLS-1$
                sb.append(stmt.toString());
            }
            else
            {
                sb.append(stmt.toString().trim() + "\n"); //$NON-NLS-1$
            }
        }
        
        ASTIfStmtNode result = (ASTIfStmtNode)parseLiteralStatement(sb.toString());
        
        String trailingComments = ifConstructNode.getEndIfStmt().findFirstToken().getWhiteBefore();
        if (!trailingComments.trim().equals("")) //$NON-NLS-1$
            result.findLastToken().setWhiteAfter(trailingComments + "\n"); //$NON-NLS-1$

        return result;
    }

    /**
     * Creates a new IF construct from the selected IF statement, with an option to add an empty
     * ELSE construct
     * 
     * @param ifConstructNode
     */
    private ASTIfConstructNode createNewIfConstruct(ASTIfStmtNode ifStmtNode)
    {
        StringBuilder sb = new StringBuilder();

        sb.append("    if ("); //$NON-NLS-1$
        sb.append(ifStmtNode.getGuardingExpression().toString());
        sb.append(") then"); //$NON-NLS-1$
        sb.append("\n        "); //$NON-NLS-1$
        sb.append(ifStmtNode.getActionStmt().toString().trim());
        sb.append("\n        !can add more statements here"); //$NON-NLS-1$
        if (shouldAddEmptyElseBlock)
        {
            sb.append("\n    else"); //$NON-NLS-1$
            sb.append("\n        !can add more statements here"); //$NON-NLS-1$
        }
        sb.append("\n    end if"); //$NON-NLS-1$

        ASTIfConstructNode newIfConstructNode = (ASTIfConstructNode)parseLiteralStatement(sb
            .toString());

        return newIfConstructNode;
    }

    @Override
    public String getName()
    {
        return Messages.IfConstructStatementConversionRefactoring_Name;
    }
}
