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
package org.eclipse.photran.internal.ui;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.photran.core.vpg.PhotranVPG;
import org.eclipse.rephraserengine.core.IResourceFilter;

/**
 *
 * @author joverbey
 */
public class PhotranResourceFilter implements IResourceFilter
{
    public boolean shouldProcess(IResource resource)
    {
        if (resource instanceof IProject)
            return PhotranVPG.getInstance().shouldProcessProject((IProject)resource);
        else if (resource instanceof IFile)
            return PhotranVPG.getInstance().shouldProcessFile((IFile)resource);
        else
            return true;
    }

}
