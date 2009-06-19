/*******************************************************************************
 * Copyright (c) 2007 University of Illinois at Urbana-Champaign and others.
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
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.OperationCanceledException;
import org.eclipse.jface.text.ITextSelection;
import org.eclipse.ltk.core.refactoring.RefactoringStatus;
import org.eclipse.photran.core.vpg.PhotranTokenRef;
import org.eclipse.photran.internal.core.analysis.binding.Definition;
import org.eclipse.photran.internal.core.analysis.binding.ScopingNode;
import org.eclipse.photran.internal.core.lexer.Token;
import org.eclipse.photran.internal.core.lexer.Token.FakeToken;
import org.eclipse.photran.internal.core.refactoring.infrastructure.SingleFileFortranRefactoring;

/**
 * Refactoring to rename identifiers in Fortran programs.
 * 
 * @author Jeff Overbey
 */
public class RenameRefactoring extends SingleFileFortranRefactoring
{
    private Definition definitionToRename = null;
    private Collection<PhotranTokenRef> allReferences = null;
    private String oldName = null, newName = null;
    private boolean shouldBindInterfacesAndExternals = true;

    public RenameRefactoring(IFile file, ITextSelection selection)
    {
        super(file, selection);
    }
    
    @Override
    public String getName()
    {
        return "Rename";
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
               && !definitionToRename.isDerivedType()
               && !definitionToRename.isModuleEntityBeforeRename()
               && !definitionToRename.isRenamedModuleEntity()
               && !definitionToRename.isMainProgram()
               && !definitionToRename.isNamelist()
               && !definitionToRename.isCommon()
               && !definitionToRename.isBlockData())
        {
               fail("The " + definitionToRename.describeClassification() + " " + oldName + " cannot be renamed.  "
                    + "Only local variables, subprograms, derived types, main programs, namelists, common blocks, "
            		+ "and block data subprograms can be renamed.  Derived type components and subprogram "
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
    protected void doCheckFinalConditions(RefactoringStatus status, IProgressMonitor pm) throws PreconditionFailure
    {
        assert definitionToRename != null;
        assert allReferences != null;
        assert newName != null;
        
        if (newName.equals(oldName)) fail("The new name (" + newName + ") is exactly the same as the old name!");
        // OK if capitalization is different
        
        if (!isValidIdentifier(newName)) fail(newName + " is not a valid identifier");

        allReferences = definitionToRename.findAllReferences(shouldBindInterfacesAndExternals);
        checkIfReferencesCanBeRenamed();

        checkForConflictingDefinitionOrShadowing(status);
        
        for (PhotranTokenRef ref : findReferencesToShadowedDefinitions())
        	checkIfReferenceBindingWillChange(status, ref, false);
        
        for (PhotranTokenRef ref : allReferences)
            checkIfReferenceBindingWillChange(status, ref, true);
    }

    private void checkIfReferencesCanBeRenamed() throws PreconditionFailure
    {
        for (PhotranTokenRef ref : allReferences)
        {
            Token reference = ref.findToken();
            
            if (reference.resolveBinding().size() > 1)
                fail(oldName + " cannot be renamed: " + describeToken(reference) + " is an ambiguous reference "
                     + " (it refers to " + oldName + " but may refer to another entity as well).");
            
            if (isPreprocessed(reference))
                fail(oldName + " cannot be renamed: It would require modifying an INCLUDE file "
                     + " (" + describeToken(reference) + ").");
        }
    }

    /** Check whether the new definition will either conflict with or shadow an existing definition */
	private void checkForConflictingDefinitionOrShadowing(RefactoringStatus status)
	{
		ScopingNode scopeOfDefinitionToRename = definitionToRename.getTokenRef().findToken().getEnclosingScope();
		
		Token token = new FakeToken(definitionToRename.getTokenRef().findToken(), newName);
		List<PhotranTokenRef> conflictingDef = scopeOfDefinitionToRename.manuallyResolveInLocalScope(token);
		// TODO: Does not consider rename or only lists (need to tell if this SPECIFIC definition will be imported)
		for (ScopingNode importingScope : scopeOfDefinitionToRename.findImportingScopes())
			conflictingDef.addAll(importingScope.manuallyResolveInLocalScope(token));

		if (!conflictingDef.isEmpty())
        {
        	PhotranTokenRef defToken = conflictingDef.get(0);
        	Definition def = vpg.getDefinitionFor(defToken);
        	//                                                                    v-- Highlight problematic definition
        	status.addError("The name \"" + newName + "\" conflicts with " + def, createContext(defToken));
        }
	}

    /** Check whether the new definition will either conflict with or shadow an existing definition */
	private List<PhotranTokenRef> findReferencesToShadowedDefinitions()
	{
		ScopingNode scopeOfDefinitionToRename = definitionToRename.getTokenRef().findToken().getEnclosingScope();
		
		Token token = new FakeToken(definitionToRename.getTokenRef().findToken(), newName);
		
		List<PhotranTokenRef> shadowedDefinitions = scopeOfDefinitionToRename.manuallyResolve(token);
		// TODO: Does not consider rename or only lists (need to tell if this SPECIFIC definition will be imported)
		for (ScopingNode importingScope : scopeOfDefinitionToRename.findImportingScopes())
			shadowedDefinitions.addAll(importingScope.manuallyResolve(token));

		List<PhotranTokenRef> referencesToShadowedDefinitions = new LinkedList<PhotranTokenRef>();
		for (PhotranTokenRef def : shadowedDefinitions)
			referencesToShadowedDefinitions.addAll(vpg.getDefinitionFor(def).findAllReferences(false));
		return referencesToShadowedDefinitions;
	}

	private void checkIfReferenceBindingWillChange(RefactoringStatus status, PhotranTokenRef ref, boolean shouldReferenceRenamedDefinition)
	{
		Token reference = ref.findToken();
		
		ScopingNode scopeOfDefinitionToRename = reference.findScopeDeclaringOrImporting(definitionToRename);
		if (scopeOfDefinitionToRename == null) return;
		
		for (PhotranTokenRef existingBinding : new FakeToken(reference, newName).manuallyResolveBinding())
		{
			ScopingNode scopeOfExistingBinding = existingBinding.findToken().getEnclosingScope();
			
			boolean willReferenceRenamedDefinition = scopeOfExistingBinding.isParentScopeOf(scopeOfDefinitionToRename);
			if (shouldReferenceRenamedDefinition != willReferenceRenamedDefinition)
			{
				// The renamed entity will shadow the definition to which this binding resolves
				status.addError("Changing the name to \"" + newName + "\""
			                + " would change the meaning of \"" + reference.getText() + "\" on line " + reference.getLine()
			                + " in " + ref.getFilename(),
			                createContext(ref)); // Highlight problematic reference
			}
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

        try
        {
	        for (IFile file : determineFilesToChange())
	            makeChangesTo(file, pm);
        }
        finally
        {
        	vpg.releaseAllASTs();
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
