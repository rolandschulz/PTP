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
package org.eclipse.rephraserengine.internal.examples.text;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.Platform;
import org.eclipse.core.runtime.content.IContentType;
import org.eclipse.core.runtime.content.IContentTypeManager;
import org.eclipse.rephraserengine.core.IResourceFilter;

/**
 * Resource filter that accepts any kind of text file (but not binaries) in any project or folder,
 * excluding files named *makefile or .*
 * <p>
 * Only resources that are accessible and not "derived resources" (according to the Eclipse
 * Platform) are considered.
 *
 * @author Jeff Overbey
 */
public class TextFileResourceFilter implements IResourceFilter
{
    private static final IContentType TEXT_CONTENT_TYPE =
        Platform.getContentTypeManager().getContentType(IContentTypeManager.CT_TEXT);

    public boolean shouldProcess(IResource resource)
    {
        if (!(resource instanceof IFile)) return true; // Process all projects, all folders

        String filename = resource.getName();
        if (filename.toLowerCase().endsWith("makefile") || filename.startsWith(".")) return false;

        IContentType contentType = Platform.getContentTypeManager().findContentTypeFor(filename);
        return contentType != null
            && contentType.isKindOf(TEXT_CONTENT_TYPE)
            && resource.isAccessible()
            && !resource.isDerived();
    }

    public String getError(IResource resource)
    {
        return "The file " + resource.getName() + " is not a text file, or it is not accessible.";
    }
}
