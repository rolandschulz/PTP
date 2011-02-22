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
 * Strategy object which populates a VPG database ({@link VPGDB}).
 * 
 * @author Jeff Overbey
 * 
 * @since 3.0
 */
public abstract class VPGWriter<A, T, R extends IVPGNode<T>>
{
    /** The VPG database, which persists edges and annotations. */
    protected final VPGDB<A, T, R> db;

    /** The VPG error/warning log. */
    protected final VPGLog<T, R> log;

    ////////////////////////////////////////////////////////////////////////////
    // CONSTRUCTOR
    ////////////////////////////////////////////////////////////////////////////

    protected VPGWriter(VPGDB<A, T, R> db, VPGLog<T, R> log)
    {
        this.db = db;
        this.log = log;
    }

    ////////////////////////////////////////////////////////////////////////////
    // CALLBACKS: VPG POPULATION
    ////////////////////////////////////////////////////////////////////////////

    /**
     * Calculates dependencies for the given file.
     * @param filename (non-null)
     */
    public abstract void computeDependencies(String filename);

    /**
     * Computes dependencies, edges, and annotations for the given file, adding them to the VPG
     * database.
     * <p>
     * If the parser was unable to parse the given file, the AST will be <code>null</code>.
     * 
     * @param filename the name of the parsed file (not null)
     * @param ast the AST for the given file, as returned from the parser (possibly null)
     */
    protected abstract void populateVPG(String filename, A ast);

    public void computeEdgesAndAnnotations(String filename, A ast)
    {
        // Log cleared prior to parse -- log.clearEntriesFor(filename);
        
        if (db instanceof DemandDB<?,?,?>) ((DemandDB<A,T,R>)db).lazyComputationEnabled = false;
        db.deleteAllEdgesAndAnnotationsFor(filename);
        populateVPG(filename, ast);
        db.updateModificationStamp(filename);
        if (db instanceof DemandDB<?,?,?>) ((DemandDB<A,T,R>)db).lazyComputationEnabled = true;
    }

    public void computeEdgesAndAnnotationsFromModifiedAST(String filename, A ast)
    {
        computeEdgesAndAnnotations(filename, ast);
    }
    
    public ILazyVPGPopulator[] getLazyEdgePopulators()
    {
        return new ILazyVPGPopulator[0];
    }
}
