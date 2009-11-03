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
package org.eclipse.rephraserengine.core;

import org.eclipse.core.resources.IResource;

/**
 * A <i>resource filter</i> is given an {@link IResource} and determines whether or not it should be
 * processed.
 * <p>
 * Resource filters are used by the refactoring components to determine what types of files and
 * projects a set of refactorings can be applied to; this is used to determine, based on what
 * file(s) the user has selected, which refactoring(s) will be shown in the Refactor menu.
 * <p>
 * Resource filters are used by the VPG to determine what projects and what types of files in those
 * projects will be indexed, i.e., what projects and files will have data stored in the VPG
 * database.
 *
 * @author Jeff Overbey
 * 
 * @since 1.0
 */
public interface IResourceFilter
{
    /** @return true iff this resource can and should be processed */
    boolean shouldProcess(IResource resource);

    /**
     * As a precondition, this method will only be invoked if {@link #shouldProcess(IResource)}
     * recently returned <code>false</code> for the given resource.
     * 
     * @return an error message, if the given resource has been filtered out due to an error
     * condition, or <code>null</code> otherwise.  The error message will be displayed to the user,
     * so it may also be useful to describe what the user can do to fix the error.
     */
    String getError(IResource resource);
}
