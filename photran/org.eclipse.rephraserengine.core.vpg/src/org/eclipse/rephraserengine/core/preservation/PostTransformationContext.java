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
package org.eclipse.rephraserengine.core.preservation;

import org.eclipse.core.resources.IFile;
import org.eclipse.jface.text.IRegion;
import org.eclipse.ltk.core.refactoring.FileStatusContext;
import org.eclipse.ltk.core.refactoring.RefactoringStatusContext;

/**
 * A {@link RefactoringStatusContext} that is similar to a {@link FileStatusContext} except it
 * represents a position in a file <i>after</i> it has been modified by a refactoring.
 * <p>
 * The org.eclipse.rephraserengine.ui.vpg plug-in registers a viewer for this type of object; it
 * displays the modified contents of the file and highlights the indicated region.
 *
 * @author Jeff Overbey
 *
 * @since 1.0
 */
public class PostTransformationContext extends RefactoringStatusContext
{
    protected IFile file;
    protected String newContents;
    protected IRegion region;

    public PostTransformationContext(IFile file, String newContents, IRegion region)
    {
        this.file = file;
        this.newContents = newContents;
        this.region = region;
    }

    public IFile getFile()
    {
        return file;
    }

    public String getFileContents()
    {
        return newContents == null ? "" : newContents;
    }

    public IRegion getTextRegion()
    {
        return region;
    }

    @Override
    public Object getCorrespondingElement()
    {
        return null;
    }
}
