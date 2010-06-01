/*******************************************************************************
 * Copyright (c) 2007-2009 University of Illinois at Urbana-Champaign and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     UIUC - Initial API and implementation
 *******************************************************************************/
package org.eclipse.photran.internal.core.refactoring;

import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.OperationCanceledException;
import org.eclipse.ltk.core.refactoring.RefactoringStatus;
import org.eclipse.ltk.core.refactoring.RefactoringStatusContext;
import org.eclipse.photran.internal.core.analysis.binding.Definition;
import org.eclipse.photran.internal.core.lexer.Token;
import org.eclipse.photran.internal.core.refactoring.infrastructure.FortranEditorRefactoring;
import org.eclipse.photran.internal.core.refactoring.interfaces.IRenameRefactoring;
import org.eclipse.photran.internal.core.sourceform.SourceForm;
import org.eclipse.photran.internal.core.vpg.PhotranTokenRef;

/**
 * Refactoring to rename identifiers in Fortran programs.
 *
 * @author Jeff Overbey
 */
public class RenameRefactoring extends FortranEditorRefactoring implements IRenameRefactoring
{
    private Definition definitionToRename = null;
    private Collection<PhotranTokenRef> allReferences = null;
    private String oldName = null, newName = null;
    private boolean shouldBindInterfacesAndExternals = true;

    @Override
    public String getName()
    {
        return Messages.RenameRefactoring_Name;
    }

    ///////////////////////////////////////////////////////////////////////////
    // User-Accessible Parameters
    ///////////////////////////////////////////////////////////////////////////

    public String getOldNameOfIdentifier()
    {
    	assert oldName != null;

    	return oldName;
    }

    public void setNewNameForIdentifier(String newName)
    {
        assert newName != null;

        this.newName = newName;
    }

    public void setShouldBindInterfacesAndExternals(boolean value)
    {
        this.shouldBindInterfacesAndExternals = value;
    }

    ///////////////////////////////////////////////////////////////////////////
    // Initial Preconditions
    ///////////////////////////////////////////////////////////////////////////

    @Override
    protected void doCheckInitialConditions(RefactoringStatus status, IProgressMonitor pm) throws PreconditionFailure
    {
        ensureProjectHasRefactoringEnabled(status);

        oldName = findEnclosingToken().getText();
        definitionToRename = findDeclarationToRename();

        checkIfDefinitionCanBeRenamed();
    }

	private Token findEnclosingToken() throws PreconditionFailure
	{
		Token selectedToken = findEnclosingToken(this.astOfFileInEditor, this.selectedRegionInEditor);
        if (selectedToken == null || !isIdentifier(selectedToken))
            fail(Messages.RenameRefactoring_PleaseSelectAnIdentifier);
		return selectedToken;
	}

	private Definition findDeclarationToRename() throws PreconditionFailure
	{
		List<Definition> declarations = findEnclosingToken().resolveBinding();

        if (declarations.size() == 0)
        	fail(Messages.bind(Messages.RenameRefactoring_NoDeclarationFoundFor, oldName));
        else if (declarations.size() > 1)
        	fail(Messages.bind(Messages.RenameRefactoring_MultipleDeclarationsFoundFor, oldName));

        return declarations.get(0);
	}

	private void checkIfDefinitionCanBeRenamed() throws PreconditionFailure
	{
		if (definitionToRename.isSubprogramArgument())
        	fail(Messages.RenameRefactoring_CannotRenameSubprogramArgs);

		// F03
		if (definitionToRename.isTypeBoundProcedure() && !definitionToRename.isRenamedTypeBoundProcedure())
		    fail(Messages.RenameRefactoring_CannotRenameTypeBoundProcedures);

        if (!definitionToRename.isLocalVariable()
               && !definitionToRename.isSubprogram()
               && !definitionToRename.isExternal()
               && !definitionToRename.isInterface()
               && !definitionToRename.isDerivedType()
               && !definitionToRename.isModuleEntityBeforeRename()
               && !definitionToRename.isRenamedModuleEntity()
               && !definitionToRename.isMainProgram()
               && !definitionToRename.isNamelist()
               && !definitionToRename.isCommon()
               && !definitionToRename.isBlockData())
        {
               fail(
                   Messages.bind(
                       Messages.RenameRefactoring_CannotRename,
                       definitionToRename.describeClassification(),
                       oldName));
        }

        if (definitionToRename.isIntrinsic())
               fail(Messages.bind(Messages.RenameRefactoring_CannotRenameIntrinsicProcedure, oldName));

        if (isPreprocessed(definitionToRename.getTokenRef().findToken()))
               fail(Messages.bind(Messages.RenameRefactoring_CannotRenameInINCLUDEFile, oldName));
	}

    ///////////////////////////////////////////////////////////////////////////
    // Final Preconditions
    ///////////////////////////////////////////////////////////////////////////

    @Override
    protected void doCheckFinalConditions(final RefactoringStatus status, IProgressMonitor pm) throws PreconditionFailure
    {
        assert definitionToRename != null;
        assert newName != null;
        assert allReferences == null;

        if (newName.equals(oldName))
            fail(
                Messages.bind(
                    Messages.RenameRefactoring_NewNameIsExactlyTheSame,
                    newName));
        // OK if capitalization is different

        if (!isValidIdentifier(newName)) fail(Messages.bind(Messages.RenameRefactoring_InvalidIdentifier, newName));

        allReferences = definitionToRename.findAllReferences(shouldBindInterfacesAndExternals);
        removeFixedFormReferences(status);
        removeCPreprocessedReferences(status);
        checkIfReferencesCanBeRenamed(pm);

        checkForConflictingBindings(
            pm,
            new ConflictingBindingErrorHandler(status),
            definitionToRename,
            allReferences,
            newName);
    }

    private final class ConflictingBindingErrorHandler implements IConflictingBindingCallback
    {
        private final RefactoringStatus status;

        private ConflictingBindingErrorHandler(RefactoringStatus status) { this.status = status; }

        public void addConflictError(List<Conflict> conflictingDef)
        {
            Conflict conflict = conflictingDef.get(0);

            String msg =
                Messages.bind(
                    Messages.RenameRefactoring_NameConflicts,
                    conflict.name,
                    vpg.getDefinitionFor(conflict.tokenRef));
            RefactoringStatusContext context = createContext(conflict.tokenRef); // Highlights problematic definition
            status.addError(msg, context);
        }

        public void addConflictWarning(List<Conflict> conflictingDef)
        {
            Conflict conflict = conflictingDef.get(0);

            String msg =
                Messages.bind(
                    Messages.RenameRefactoring_NameMightConflict,
                    conflict.name);
            RefactoringStatusContext context = createContext(conflict.tokenRef); // Highlights problematic definition
            status.addWarning(msg, context);
        }

        public void addReferenceWillChangeError(String newName, Token reference)
        {
            // The entity with the new name will shadow the definition to which this binding resolves
            status.addError(
                Messages.bind(
                    Messages.RenameRefactoring_ChangingNameWouldChangeMeaning,
                    new Object[] {
                        newName,
                        reference.getText(),
                        reference.getLine(),
                        reference.getTokenRef().getFilename() }),
                createContext(reference)); // Highlight problematic reference
        }
    }

    private void removeFixedFormReferences(RefactoringStatus status)
    {
        if (FIXED_FORM_REFACTORING_ENABLED) return;
        
        HashSet<IFile> fixedFormFiles = new HashSet<IFile>();
        HashSet<IFile> freeFormFiles = new HashSet<IFile>();
        HashSet<PhotranTokenRef> referencesToRemove = new HashSet<PhotranTokenRef>();

        for (PhotranTokenRef reference : allReferences)
        {
            IFile file = reference.getFile();

            if (fixedFormFiles.contains(file))
            {
                referencesToRemove.add(reference);
            }
            else if (freeFormFiles.contains(file))
            {
                continue;
            }
            else if (SourceForm.isFixedForm(file))
            {
                fixedFormFiles.add(file);
                status.addError(Messages.bind(Messages.RenameRefactoring_FixedFormFileWillNotBeRefactored, file.getName()));
                referencesToRemove.add(reference);
            }
            else
            {
                freeFormFiles.add(file);
            }
        }

        allReferences.removeAll(referencesToRemove);
    }
    
    private void removeCPreprocessedReferences(RefactoringStatus status)
    {
        HashSet<IFile> cppFiles = new HashSet<IFile>();
        HashSet<IFile> freeFormFiles = new HashSet<IFile>();
        HashSet<PhotranTokenRef> referencesToRemove = new HashSet<PhotranTokenRef>();

        for (PhotranTokenRef reference : allReferences)
        {
            IFile file = reference.getFile();

            if (cppFiles.contains(file))
            {
                referencesToRemove.add(reference);
            }
            else if (freeFormFiles.contains(file))
            {
                continue;
            }
            else if (SourceForm.isCPreprocessed(file))
            {
                cppFiles.add(file);
                status.addError(
                    Messages.bind(
                        Messages.RenameRefactoring_CPreprocessedFileWillNotBeRefactored,
                        file.getName()));
                referencesToRemove.add(reference);
            }
            else
            {
                freeFormFiles.add(file);
            }
        }

        allReferences.removeAll(referencesToRemove);
    }

    private void checkIfReferencesCanBeRenamed(IProgressMonitor pm) throws PreconditionFailure
    {
        for (PhotranTokenRef ref : allReferences)
        {
            pm.subTask(Messages.bind(Messages.RenameRefactoring_StatusCheckingIfReferencesInFileCanBeRenamed, ref.getFilename()));

            Token reference = ref.findToken();

            if (reference.resolveBinding().size() > 1)
                fail(
                    Messages.bind(
                        Messages.RenameRefactoring_CannotRenameAmbiguous, new Object[] {
                        oldName,
                        describeToken(reference),
                        oldName }));

            if (isPreprocessed(reference))
                fail(
                    Messages.bind(
                        Messages.RenameRefactoring_CannotRenameUsedInINCLUDEFile,
                        oldName,
                        describeToken(reference)));
        }
    }





    ///////////////////////////////////////////////////////////////////////////
    // Change
    ///////////////////////////////////////////////////////////////////////////

    @Override
    protected void doCreateChange(IProgressMonitor pm) throws CoreException, OperationCanceledException
    {
        assert definitionToRename != null;
        assert allReferences != null;
        assert newName != null;

        Set<IFile> filesToChange = determineFilesToChange();
        pm.beginTask(Messages.RenameRefactoring_StatusRenaming, filesToChange.size());

        try
        {
            for (IFile file : filesToChange)
	            makeChangesTo(file, pm);
        }
        finally
        {
        	vpg.releaseAllASTs();
            pm.done();
        }
    }

    private Set<IFile> determineFilesToChange()
    {
        Set<IFile> files = new HashSet<IFile>(allReferences.size() + 2);

        files.add(fileInEditor); // File in the editor (containing the reference)
        files.add(definitionToRename.getTokenRef().getFile());

        for (PhotranTokenRef ref : allReferences)
            files.add(ref.getFile());

        return files;
    }

    private void makeChangesTo(IFile file, IProgressMonitor pm) throws Error
    {
        try
        {
            pm.subTask(Messages.bind(Messages.RenameRefactoring_StatusModifyingFile, file.getName()));
            pm.worked(1);

            vpg.acquirePermanentAST(file);

            if (definitionToRename.getTokenRef().getFile().equals(file))
                definitionToRename.getTokenRef().findToken().setText(newName);

            for (PhotranTokenRef ref : allReferences)
                if (ref.getFile().equals(file))
                    ref.findToken().setText(newName);

            addChangeFromModifiedAST(file, pm);

            vpg.releaseAST(file);
        }
        catch (Exception e)
        {
            throw new Error(e);
        }
    }
}
