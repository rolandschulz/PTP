/*******************************************************************************
 * Copyright (c) 2010 University of Illinois at Urbana-Champaign and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    UIUC - Initial API and implementation
 *******************************************************************************/
package org.eclipse.rephraserengine.core.vpg.eclipse;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IResource;
import org.eclipse.rephraserengine.core.vpg.NodeRef;
import org.eclipse.rephraserengine.core.vpg.VPGWriter;
import org.eclipse.rephraserengine.core.vpg.IVPGNode;
import org.eclipse.rephraserengine.core.vpg.VPGDB;
import org.eclipse.rephraserengine.core.vpg.VPGLog;

/**
 * A {@link VPGWriter} corresponding to an {@link EclipseVPG}.
 * 
 * @author Jeff Overbey
 * 
 * @param <A> AST type
 * @param <T> token type
 * @param <R> {@link IVPGNode}/{@link NodeRef} type
 * 
 * @since 3.0
 */
public abstract class EclipseVPGWriter<A, T, R extends IVPGNode<T>>
              extends VPGWriter<A, T, R>
{
    protected EclipseVPGWriter(VPGDB<A, T, R> db, VPGLog<T,R> log)
    {
        super(db, log);
    }

    ///////////////////////////////////////////////////////////////////////////
    // Utility Methods (IFile<->Filename Mapping)
    ///////////////////////////////////////////////////////////////////////////

    public static IFile getIFileForFilename(String filename)
    {
        return ResourceUtil.getIFileForFilename(filename);
    }

    public static String getFilenameForIFile(IFile file)
    {
        return ResourceUtil.getFilenameForIFile(file);
    }

    public static IResource getIResourceForFilename(String filename)
    {
        return ResourceUtil.getIResourceForFilename(filename);
    }

    public static String getFilenameForIResource(IResource resource)
    {
        return ResourceUtil.getFilenameForIResource(resource);
    }
}
