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

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

import org.eclipse.core.resources.IFile;
import org.eclipse.ltk.core.refactoring.RefactoringStatus;
import org.eclipse.photran.core.vpg.PhotranVPG;
import org.eclipse.photran.internal.core.properties.SearchPathProperties;

/**
 * This is a base class for all Photran refactorings that apply to multiple files
 * @author Jeff Overbey, Timofey Yuvashev
 */
public abstract class MultipleFileFortranRefactoring extends AbstractFortranRefactoring
{
    protected ArrayList<IFile> selectedFiles = null;
    
    public MultipleFileFortranRefactoring(ArrayList<IFile> files)
    {
        assert files != null && files.size() > 0;
        
        this.vpg= PhotranVPG.getInstance();
        this.selectedFiles = files;
    }
    
    @Override
    protected RefactoringStatus getAbstractSyntaxTree(RefactoringStatus status)
    {
        return status;
    }

    @Override
    protected void ensureProjectHasRefactoringEnabled(RefactoringStatus status) throws PreconditionFailure
    {
        if (PhotranVPG.inTestingMode()) return;
        
        HashSet<IFile> filesToBeRemoved = new HashSet<IFile>();
        
        for(IFile f : this.selectedFiles)
        {
            String vpgEnabledProperty = SearchPathProperties.getProperty(
                f,
                SearchPathProperties.ENABLE_VPG_PROPERTY_NAME);
            if (vpgEnabledProperty == null || !vpgEnabledProperty.equals("true"))
            {
                //FIXME: If a file is not associated with a project, this will return null 
                // and needs to be fixed
                status.addWarning("Please enable analysis and refactoring in the project "+ 
                    f.getProject().getName() + " properties.");
                filesToBeRemoved.add(f);
            }
        }
        //Remove files that didn't have Refactoring enabled in their projects
        this.selectedFiles.removeAll(filesToBeRemoved);
    }
    
    protected void removeFixedFormFilesFrom(Collection<IFile> files, RefactoringStatus status)
    {
        Set<IFile> filesToRemove = new HashSet<IFile>();
        
        for (IFile file : files)
        {
            if (!filesToRemove.contains(file) && PhotranVPG.hasFixedFormContentType(file))
            {
                status.addError("The fixed form file " + file.getName() + " will not be refactored.");
                filesToRemove.add(file);
            }
        }
        
        files.removeAll(filesToRemove);
    }

}
