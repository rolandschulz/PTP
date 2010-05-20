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
package org.eclipse.photran.internal.ui.vpg;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.photran.internal.core.refactoring.infrastructure.FortranResourceRefactoring;
import org.eclipse.photran.internal.core.sourceform.SourceForm;
import org.eclipse.photran.internal.core.vpg.PhotranVPG;
import org.eclipse.rephraserengine.core.resources.IResourceFilter;

/**
 * A resource filter that only matches accessible Fortran source files and Fortran/C/C++ projects
 * that have refactoring enabled in their project properties. See {@link IResourceFilter}.
 * <p>
 * Currently, this filter excludes fixed form source files, since Photran does not (yet) support
 * refactoring fixed form code.
 * 
 * @author Jeff Overbey
 * 
 * @see IResourceFilter
 */
public class PhotranResourceFilter implements IResourceFilter
{
    public boolean shouldProcess(IResource resource)
    {
        if (resource instanceof IProject)
            return PhotranVPG.getInstance().shouldProcessProject((IProject)resource);
        else if (resource instanceof IFile)
            return ((IFile)resource).getProject() != null
                && PhotranVPG.getInstance().shouldProcessProject(((IFile)resource).getProject())
                && PhotranVPG.getInstance().shouldProcessFile((IFile)resource)
                && ((IFile)resource).isAccessible()
                && (!SourceForm.isFixedForm((IFile)resource) || FortranResourceRefactoring.FIXED_FORM_REFACTORING_ENABLED)
                && !SourceForm.isCPreprocessed((IFile)resource);
        else
            return true;
    }

    public String getError(IResource resource)
    {
        if (resource instanceof IProject)
            return PhotranVPG.getInstance().describeWhyCannotProcessProject((IProject)resource);
        else if (resource instanceof IFile)
            return PhotranVPG.getInstance().describeWhyCannotProcessFile((IFile)resource);
        else
            return null;
    }
}
