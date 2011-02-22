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
package org.eclipse.rephraserengine.core.vpg;

/**
 * Adds edges and annotations to an in-memory AST on demand, when they are requested,
 * rather than computing them immediately when the file is indexed.
 * 
 * @author Jeff Overbey
 * 
 * @since 3.0
 */
public interface ILazyVPGPopulator
{
    /** @return true if, to populate lazy edges in a file correctly, lazy edges must be populated in all of its dependent files */
    public abstract boolean dependentFilesMustBePopulated();
    
    /** @return the edge types populated by this {@link ILazyVPGPopulator} */
    public abstract int[] edgeTypesPopulated();
    
    /** @return the annotation types populated by this {@link ILazyVPGPopulator} */
    public abstract int[] annotationTypesPopulated();
    
    /** Populates lazy edges in the given file */
    public abstract void populateVPG(String filename);
}
