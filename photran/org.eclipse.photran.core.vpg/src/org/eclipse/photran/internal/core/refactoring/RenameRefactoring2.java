/*******************************************************************************
 * Copyright (c) 2009 University of Illinois at Urbana-Champaign and others.
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
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.ltk.core.refactoring.RefactoringStatus;
import org.eclipse.photran.internal.core.analysis.binding.Definition;
import org.eclipse.photran.internal.core.lexer.Token;
import org.eclipse.photran.internal.core.refactoring.infrastructure.PreservationBasedSingleFileFortranRefactoring;
import org.eclipse.photran.internal.core.vpg.PhotranTokenRef;
import org.eclipse.photran.internal.core.vpg.PhotranVPG;
import org.eclipse.rephraserengine.core.preservation.PreservationRule;
import org.eclipse.rephraserengine.core.refactorings.UserInputBoolean;
import org.eclipse.rephraserengine.core.refactorings.UserInputString;

/**
 * Preservation-based Rename refactoring for Fortran programs.
 *
 * @author Jeff Overbey
 */
public class RenameRefactoring2 extends PreservationBasedSingleFileFortranRefactoring
{
    ///////////////////////////////////////////////////////////////////////////
    // Fields
    ///////////////////////////////////////////////////////////////////////////

    private Definition definitionToRename = null;
    private Collection<PhotranTokenRef> allReferences = null;
    private String oldName = null, newName = null;
    private boolean shouldBindInterfacesAndExternals = true;

    @Override
    public String getName()
    {
        return "Rename (Alternate Implementation)";
    }

    ///////////////////////////////////////////////////////////////////////////
    // User-Accessible Parameters
    ///////////////////////////////////////////////////////////////////////////

    public String getOldNameOfIdentifier()
    {
        assert oldName != null;

        return oldName;
    }

    @UserInputString(label="Rename to", defaultValueMethod="getOldNameOfIdentifier")
    public void setNewNameForIdentifier(String newName)
    {
        assert newName != null;

        this.newName = newName;
    }

    @UserInputBoolean(label="Match external subprograms with interfaces and external declarations",
                      defaultValue=true)
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
            fail("Please select an identifier to rename.");
        return selectedToken;
    }

    private Definition findDeclarationToRename() throws PreconditionFailure
    {
        List<Definition> declarations = findEnclosingToken().resolveBinding();

        if (declarations.size() == 0)
            fail("No declaration was found for " + oldName);
        else if (declarations.size() > 1)
            fail("Multiple declarations were found for " + oldName);

        return declarations.get(0);
    }

    private void checkIfDefinitionCanBeRenamed() throws PreconditionFailure
    {
        if (definitionToRename.isSubprogramArgument())
            fail("Subprogram arguments cannot be renamed.");

        // F03
        if (definitionToRename.isTypeBoundProcedure() && !definitionToRename.isRenamedTypeBoundProcedure())
            fail("Type-bound procedures cannot be renamed.");

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
               fail("The " + definitionToRename.describeClassification() + " " + oldName + " cannot be renamed.  "
                    + "Only local variables, subprograms and interfaces, derived types, main programs, namelists, "
                    + "common blocks, and block data subprograms can be renamed.  Derived type components and subprogram "
                    + "arguments cannot be renamed.");
        }

        if (definitionToRename.isIntrinsic())
               fail(oldName + " cannot be renamed: It is an intrinsic procedure.");

        if (isPreprocessed(definitionToRename.getTokenRef().findToken()))
               fail(oldName + " cannot be renamed: It is declared in an INCLUDE file.");
    }

    ///////////////////////////////////////////////////////////////////////////
    // Final Preconditions
    ///////////////////////////////////////////////////////////////////////////

    @Override
    protected void doValidateUserInput(RefactoringStatus status, IProgressMonitor pm) throws PreconditionFailure
    {
        if (newName.equals(oldName)) fail("The new name (" + newName + ") is exactly the same as the old name!");
        // OK if capitalization is different

        if (!isValidIdentifier(newName)) fail(newName + " is not a valid identifier");

        allReferences = definitionToRename.findAllReferences(shouldBindInterfacesAndExternals);
        removeFixedFormReferences(status);
        checkIfReferencesCanBeRenamed(pm);
    }

    @Override
    protected PreservationRule getEdgesToPreserve()
    {
        return PreservationRule.preserveIncoming(PhotranVPG.BINDING_EDGE_TYPE);
    }

    @Override
    protected void doTransform(RefactoringStatus status, IProgressMonitor pm) throws PreconditionFailure
    {
        Set<IFile> filesToChange = determineFilesToChange();
        pm.beginTask("Renaming", filesToChange.size());

        try
        {
            for (IFile file : filesToChange)
                makeChangesTo(file, pm);
        }
        finally
        {
            //vpg.releaseAllASTs();
            pm.done();
        }
    }

    private void removeFixedFormReferences(RefactoringStatus status)
    {
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
            else if (PhotranVPG.hasFixedFormContentType(file))
            {
                fixedFormFiles.add(file);
                status.addError("The fixed form file " + file.getName() + " will not be refactored.");
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
            pm.subTask("Checking if references in " + ref.getFilename() + " can be renamed");

            Token reference = ref.findToken();

            if (reference.resolveBinding().size() > 1)
                fail(oldName + " cannot be renamed: " + describeToken(reference) + " is an ambiguous reference "
                     + " (it refers to " + oldName + " but may refer to another entity as well).");

            if (isPreprocessed(reference))
                fail(oldName + " cannot be renamed: It would require modifying an INCLUDE file "
                     + " (" + describeToken(reference) + ").");
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
            pm.subTask("Modifying " + file.getName());
            pm.worked(1);

            //vpg.acquirePermanentAST(file);
            vpg.acquireTransientAST(file);

            if (definitionToRename.getTokenRef().getFile().equals(file))
            {
                Token token = definitionToRename.getTokenRef().findToken();
                int prefixLength = token.getWhiteBefore().length();
                int suffixLength = token.getWhiteAfter().length();
                int oldLength = prefixLength + token.getText().length() + suffixLength;
                int newLength = prefixLength + newName.length() + suffixLength;

                token.setText(newName);
                preservation.markRho(file, token, oldLength, newLength);
            }

            for (PhotranTokenRef ref : allReferences)
            {
                if (ref.getFile().equals(file))
                {
                    Token token = ref.findToken();
                    int prefixLength = token.getWhiteBefore().length();
                    int suffixLength = token.getWhiteAfter().length();
                    int oldLength = prefixLength + token.getText().length() + suffixLength;
                    int newLength = prefixLength + newName.length() + suffixLength;

                    token.setText(newName);
                    preservation.markRho(file, token, oldLength, newLength);
                }
            }

            //addChangeFromModifiedAST(file, pm);
            //vpg.commitChangesFromInMemoryASTs(pm, 0, file);

            //vpg.releaseAST(file);
        }
        catch (Exception e)
        {
            throw new Error(e);
        }
    }
}
