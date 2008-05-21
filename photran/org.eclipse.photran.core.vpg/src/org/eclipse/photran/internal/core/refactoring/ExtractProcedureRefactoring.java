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

import java.util.List;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.OperationCanceledException;
import org.eclipse.jface.text.ITextSelection;
import org.eclipse.ltk.core.refactoring.RefactoringStatus;
import org.eclipse.photran.internal.core.parser.IBodyConstruct;
import org.eclipse.photran.internal.core.refactoring.infrastructure.FortranRefactoring;

/**
 * Refactoring to extract a sequence of statements into a new subprogram,
 * replacing the sequence with a call to that subprogram.
 * 
 * INCOMPLETE
 * 
 * @author Jeff Overbey
 */
public class ExtractProcedureRefactoring extends FortranRefactoring
{
	private List<IBodyConstruct> stmtSeq = null;
	private String name = null;
	
    public ExtractProcedureRefactoring(IFile file, ITextSelection selection)
    {
        super(file, selection);
    }
    
    @Override
    public String getName()
    {
        return "Extract Procedure";
    }

    ///////////////////////////////////////////////////////////////////////////
    // User-Specified Parameters
    ///////////////////////////////////////////////////////////////////////////
    
    public void setName(String name)
    {
        assert name != null;
        
        this.name = name;
    }

    ///////////////////////////////////////////////////////////////////////////
    // Initial Preconditions
    ///////////////////////////////////////////////////////////////////////////
    
    @Override
    protected void doCheckInitialConditions(RefactoringStatus status, IProgressMonitor pm) throws PreconditionFailure
    {
    	stmtSeq = this.findEnclosingStatementSequence(this.astOfFileInEditor, this.selectedRegionInEditor);
        
        if (stmtSeq == null) fail("Please select a sequence of statements to extract.");
    }

    ///////////////////////////////////////////////////////////////////////////
    // Final Preconditions
    ///////////////////////////////////////////////////////////////////////////
    
    @Override
    protected void doCheckFinalConditions(RefactoringStatus status, IProgressMonitor pm) throws PreconditionFailure
    {
        assert stmtSeq != null;
        assert name != null;
        
        // TODO
    }

    ///////////////////////////////////////////////////////////////////////////
    // Change
    ///////////////////////////////////////////////////////////////////////////
    
    @Override
    protected void doCreateChange(IProgressMonitor pm) throws CoreException, OperationCanceledException
    {
        assert stmtSeq != null;
        assert name != null;

        // TODO
        
        this.addChangeFromModifiedAST(this.fileInEditor, pm);
    }
}
