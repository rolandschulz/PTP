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
package org.eclipse.rephraserengine.core.refactorings;

import java.util.List;

import org.eclipse.core.resources.IFile;

/**
 * A refactoring that can be applied to one or more resources (i.e., files, folders, or projects).
 * <p>
 * These refactorings will be available when the user has selected one or more resources in the
 * Navigator view, as well as when a resource is open in an editor.  Since these refactorings may be
 * applied to several resources at once, and the refactoring is not necessarily initiated from an
 * editor, the current selection in the editor is not provided; an <code>IResourceRefactoring</code>
 * is expected to operate on entire files, oblivious to any current selection in the editor.
 * <p>
 * Contrast this with an {@link IEditorRefactoring}, which only applies to a single file, is only
 * available when the user has that file open in an editor, and is also provided the current
 * selection in the editor.
 *
 * @author Jeff Overbey
 *
 * @see IEditorRefactoring
 * @see IRefactoring
 */
public interface IResourceRefactoring extends IRefactoring
{
    void initialize(List<IFile> files);
}
