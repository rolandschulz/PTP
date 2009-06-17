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

import org.eclipse.core.resources.IFile;
import org.eclipse.jface.text.ITextSelection;
import org.eclipse.ltk.core.refactoring.RefactoringStatus;
import org.eclipse.photran.core.IFortranAST;
import org.eclipse.photran.core.vpg.PhotranVPG;
import org.eclipse.photran.internal.core.properties.SearchPathProperties;

/**
 * This is a base class for all Photran refactorings that apply to a single file 
 * and possibly require user input/selection
 * @author Jeff Overbey, Timofey Yuvashev
 */
public abstract class SingleFileFortranRefactoring extends AbstractFortranRefactoring
{
    protected IFile fileInEditor = null;
    protected ITextSelection selectedRegionInEditor = null;
    protected IFortranAST astOfFileInEditor = null;
    protected final boolean inFixedFormEditor = false;
    
    
    public SingleFileFortranRefactoring(IFile file, ITextSelection selection)
    {
        assert file != null && file.isAccessible();
        assert selection != null;

        this.vpg = PhotranVPG.getInstance();
        
        this.fileInEditor = file;
        this.selectedRegionInEditor = selection;
    }

    public SingleFileFortranRefactoring(IFile file)
    {
        assert file != null && file.isAccessible();

        this.vpg = PhotranVPG.getInstance();
        
        this.fileInEditor = file;
    }
    
    @Override
    protected RefactoringStatus getAbstractSyntaxTree(RefactoringStatus status)
    {
        if(fileInEditor != null)
        {
            this.astOfFileInEditor = vpg.acquireTransientAST(fileInEditor);
            logVPGErrors(status);
            if (astOfFileInEditor == null)
            {
                status.addFatalError("The file in the editor cannot be parsed.");
                return status;
            }
        }
        return status;
    }
    
    @Override
    protected void ensureProjectHasRefactoringEnabled(RefactoringStatus status) throws PreconditionFailure
    {
        if (PhotranVPG.inTestingMode()) return;
        
        String vpgEnabledProperty = SearchPathProperties.getProperty(
            fileInEditor,
            SearchPathProperties.ENABLE_VPG_PROPERTY_NAME);
        if (vpgEnabledProperty == null || !vpgEnabledProperty.equals("true"))
            fail("Please enable analysis and refactoring in the project properties.");
    }

}
