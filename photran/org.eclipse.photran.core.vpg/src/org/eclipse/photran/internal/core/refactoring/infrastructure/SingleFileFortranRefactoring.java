/*******************************************************************************
 * Copyright (c) 2009 University of Illinois at Urbana-Champaign and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    UIUC - Initial API and implementation
 *******************************************************************************/
package org.eclipse.photran.internal.core.refactoring.infrastructure;

import java.util.Collections;
import java.util.List;

import org.eclipse.core.resources.IFile;
import org.eclipse.jface.text.ITextSelection;
import org.eclipse.ltk.core.refactoring.RefactoringStatus;
import org.eclipse.photran.core.IFortranAST;
import org.eclipse.photran.internal.core.vpg.PhotranVPG;
import org.eclipse.rephraserengine.core.refactorings.IEditorRefactoring;

/**
 * This is a base class for all Photran refactorings that apply to a single file
 * and possibly require user input/selection
 * @author Jeff Overbey, Timofey Yuvashev
 */
public abstract class SingleFileFortranRefactoring
    extends MultipleFileFortranRefactoring
    implements IEditorRefactoring
{
    /*
     * By the time the Rephraser Engine was built (but after about 15 refactorings
     * were built), we had established the precedent that all preconditions for
     * Fortran refactorings were protected methods inherited from a superclass.
     * In order to keep this (and avoid upsetting all the refactoring classes),
     * these methods were placed into the MultipleFileFortranRefactoring class, and
     * this class inherits from that.  Unfortunately, that means this class cannot
     * inherit from VPGEditorRefactoring, which would be more logical.  So,
     * VPGEditorRefactoring is copied into this class... which is probably OK since
     * that class will rarely, if ever, change.  A more "correct" strategy would be
     * to move all of the precondition checks into a Check class and have this
     * inherit from VPGEditorRefactoring.  We should do that eventually, but it will
     * be a fairly disruptive change...  TODO: Correct inheritance, move prec checks
     */
    
    // v v v v v v v v v v v v v v v v v v v v v v v v v v v v v v v v v v v v v v v v v v v v v v v
    // Copy of VPGEditorRefactoring
    // v v v v v v v v v v v v v v v v v v v v v v v v v v v v v v v v v v v v v v v v v v v v v v v

    protected IFile fileInEditor;
    protected ITextSelection selectedRegionInEditor;
    protected IFortranAST astOfFileInEditor;
    
    @Override
    public final void initialize(List<IFile> files)
    {
        throw new IllegalArgumentException("#initialize(List) cannot be invoked on VPGEditorRefactoring");
    }

    public void initialize(IFile file, ITextSelection selection)
    {
        if (file == null) throw new IllegalArgumentException("file argument cannot be null");

        super.initialize(Collections.<IFile>singletonList(file));
        this.fileInEditor = file;
        this.selectedRegionInEditor = selection;
        this.astOfFileInEditor = null; // until #checkInitialConditions invoked
    }

    public void initialize(IFile file)
    {
        initialize(file, null);
    }

    @Override
    protected void checkFiles(RefactoringStatus status) throws PreconditionFailure
    {
        assert fileInEditor != null;

        checkIfFileIsAccessibleAndWritable(fileInEditor);

        this.astOfFileInEditor = vpg.acquireTransientAST(fileInEditor);
        logVPGErrors(status);
        if (astOfFileInEditor == null)
            fail("The file in the editor cannot be parsed.");
    }
    
    // ^ ^ ^ ^ ^ ^ ^ ^ ^ ^ ^ ^ ^ ^ ^ ^ ^ ^ ^ ^ ^ ^ ^ ^ ^ ^ ^ ^ ^ ^ ^ ^ ^ ^ ^ ^ ^ ^ ^ ^ ^ ^ ^ ^ ^ ^ ^

    @Override
    protected void ensureProjectHasRefactoringEnabled(RefactoringStatus status) throws PreconditionFailure
    {
        if (!PhotranVPG.getInstance().doesProjectHaveRefactoringEnabled(fileInEditor))
            fail("Please enable analysis and refactoring in the project properties.");
    }
}
