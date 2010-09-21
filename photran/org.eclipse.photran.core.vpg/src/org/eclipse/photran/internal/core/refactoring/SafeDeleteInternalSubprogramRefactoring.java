/*******************************************************************************
 * Copyright (c) 2010 University of Illinois at Urbana-Champaign and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    UIUC - Initial API and implementation
 *******************************************************************************/
package org.eclipse.photran.internal.core.refactoring;

import java.util.List;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.OperationCanceledException;
import org.eclipse.ltk.core.refactoring.RefactoringStatus;
import org.eclipse.photran.internal.core.analysis.binding.Definition;
import org.eclipse.photran.internal.core.analysis.binding.ScopingNode;
import org.eclipse.photran.internal.core.lexer.Token;
import org.eclipse.photran.internal.core.parser.ASTAccessStmtNode;
import org.eclipse.photran.internal.core.parser.ASTSubroutineNameNode;
import org.eclipse.photran.internal.core.parser.ASTSubroutineStmtNode;
import org.eclipse.photran.internal.core.parser.ASTSubroutineSubprogramNode;
import org.eclipse.photran.internal.core.parser.IASTListNode;
import org.eclipse.photran.internal.core.parser.IASTNode;
import org.eclipse.photran.internal.core.parser.IAccessId;
import org.eclipse.photran.internal.core.parser.IInternalSubprogram;
import org.eclipse.photran.internal.core.refactoring.infrastructure.FortranEditorRefactoring;
import org.eclipse.photran.internal.core.vpg.PhotranTokenRef;

/**
 * Refactoring to delete un-referenced internal subprograms.If the subprogram has only internal
 * references then delete the subprogram in all cases.If the particular subprogram happens to be the
 * only internal subprogram then remove the contains statement and any access statement.
 * 
 * @author Abhishek Sharma
 */
public class SafeDeleteInternalSubprogramRefactoring extends FortranEditorRefactoring
{

    private ASTSubroutineSubprogramNode subProgramNode;

    @Override
    protected void doCheckInitialConditions(RefactoringStatus status, IProgressMonitor pm)
        throws PreconditionFailure
    {

        ensureProjectHasRefactoringEnabled(status);

        IASTNode selectedNode = findEnclosingNode(astOfFileInEditor, selectedRegionInEditor);

        if (selectedNode == null) fail(Messages.SafeDeleteInternalSubprogramRefactoring_NoSubroutineSelected);

        if (selectedNode instanceof ASTSubroutineSubprogramNode)
            subProgramNode = ((ASTSubroutineSubprogramNode)selectedNode);

        else if (selectedNode instanceof ASTSubroutineStmtNode
            || selectedNode instanceof ASTSubroutineNameNode)
        {
            ASTSubroutineSubprogramNode NearestAncestor = selectedNode
                .findNearestAncestor(ASTSubroutineSubprogramNode.class);
            subProgramNode = NearestAncestor;
        }

        if (subProgramNode == null) fail(Messages.SafeDeleteInternalSubprogramRefactoring_NoSubroutineSelected);

        // list of definitions.Ideally there should be only 1 element in the list
        List<Definition> definitionsList = subProgramNode.getSubroutineStmt().getSubroutineName()
            .getSubroutineName().resolveBinding();

        if (definitionsList.size() == 0)
            throw new PreconditionFailure(Messages.SafeDeleteInternalSubprogramRefactoring_NoDefinition);
        else if (definitionsList.size() > 1)
            throw new PreconditionFailure(Messages.SafeDeleteInternalSubprogramRefactoring_MultipleDefinitions);
        else if (definitionsList.get(0).isInternalSubprogramDefinition() == false)
            throw new PreconditionFailure(Messages.SafeDeleteInternalSubprogramRefactoring_NotAnInternalSubprogram);

        for (PhotranTokenRef tokenRef : definitionsList.get(0).findAllReferences(true))
        {
            // token is neither in the access statement nor does it occur in itself(recursive)
            if (tokenRef.findToken().findNearestAncestor(ASTAccessStmtNode.class) == null
                    && tokenRef.findToken().findNearestAncestor(ASTSubroutineSubprogramNode.class) != subProgramNode)
                fail(Messages.SafeDeleteInternalSubprogramRefactoring_SubroutineMustHaveOnlyInternalReferences);
        }

    }

    @Override
    protected void doCheckFinalConditions(RefactoringStatus status, IProgressMonitor pm)
        throws org.eclipse.rephraserengine.core.vpg.refactoring.VPGRefactoring.PreconditionFailure
    {
    }

    @Override
    protected void doCreateChange(IProgressMonitor pm) throws CoreException,
        OperationCanceledException
    {
        removeReferenceInAccessStatement();
        removeSubProgram();
        this.addChangeFromModifiedAST(this.fileInEditor, pm);

        vpg.releaseAllASTs();
    }

    private void removeSubProgram()
    {
        ScopingNode enclosingScope = subProgramNode.findNearestAncestor(ScopingNode.class);

        if (enclosingScope != null)
        {
            IASTListNode<IInternalSubprogram> internalSubprogramList = enclosingScope.getInternalSubprograms();

            if (internalSubprogramList.size() == 1)
                enclosingScope.getContainsStmt().removeFromTree();

            subProgramNode.removeFromTree();
        }
    }

    private void removeReferenceInAccessStatement()
    {
        List<Definition> definitionsList = subProgramNode.getSubroutineStmt().getSubroutineName()
            .getSubroutineName().resolveBinding();

        for (PhotranTokenRef tokenRef : definitionsList.get(0).findAllReferences(true))
        {

            if (tokenRef.findToken().findNearestAncestor(ASTAccessStmtNode.class) != null)
            {
                IASTListNode<IAccessId> accessIdList = tokenRef.findToken()
                    .findNearestAncestor(ASTAccessStmtNode.class).getAccessIdList();

                if (accessIdList.size() == 1)
                    tokenRef.findToken().findNearestAncestor(ASTAccessStmtNode.class)
                        .removeFromTree();
                else
                {
                    Token token = tokenRef.findToken();
                    IAccessId accessID = token.findNearestAncestor(IAccessId.class);
                    accessIdList.remove(accessID);
                }
            }
        }

    }

    @Override
    public String getName()
    {
        return "Safe-Delete Non-Generic Internal Subprogram"; //$NON-NLS-1$
    }
}
