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

import java.lang.ref.WeakReference;
import java.util.HashMap;

/**
 * The VPG's cache of abstract syntax trees.
 * 
 * @param <A> AST type
 * 
 * @author Jeff Overbey
 */
final class ASTRepository<A>
{
    /** Cache of ASTs acquired using {@link #acquirePermanentAST(String)} or converted
     *  using {@link #makeTransientASTPermanent(String)}. */
    protected HashMap<String, A> permanentASTs;

    /** Cache of ASTs acquired using {@link #acquireTransientAST(String)}. */
    protected HashMap<String, WeakReference<A>> transientASTs;

    /** Small queue of <i>recent</i> ASTs acquired using {@link #acquireTransientAST(String)}. */
    protected Object[] transientASTCache;
    private int transientASTCacheIndex = 0;

    public ASTRepository(int transientASTCacheSize)
    {
        assert transientASTCacheSize > 0;

        this.transientASTs = new HashMap<String, WeakReference<A>>();
        this.permanentASTs = new HashMap<String, A>();
        this.transientASTCache = new Object[transientASTCacheSize];
    }

    ////////////////////////////////////////////////////////////////////////////
    // API: AST ACQUISITION/RELEASE
    ////////////////////////////////////////////////////////////////////////////

    /** @return an AST for the given file which will be garbage collected after
     *  no pointers to any of its nodes remain.
     */
    public <T, R extends IVPGNode<T>>
           A acquireTransientAST(String filename, boolean forceRecomputationOfEdgesAndAnnotations, VPG<A, T, R> vpg)
    {
        if (vpg.isVirtualFile(filename) || !vpg.shouldProcessFile(filename)) return null;

        A ast = null;

        if (!forceRecomputationOfEdgesAndAnnotations)
        {
            if (permanentASTs.containsKey(filename))
                ast = permanentASTs.get(filename);
            else if (transientASTs.containsKey(filename))
                ast = transientASTs.get(filename).get();

            if (ast != null) return ast;
        }

        boolean shouldComputeEdgesAndAnnotations =
            forceRecomputationOfEdgesAndAnnotations || vpg.isOutOfDate(filename);
        
        if (shouldComputeEdgesAndAnnotations)
            vpg.getLog().clearEntriesFor(filename);

        ast = vpg.parse(filename);
        if (ast != null)
        {
            WeakReference<A> astRef = new WeakReference<A>(ast);
            transientASTs.put(filename, astRef);
            //astFilenames.put(astRef, filename);

            transientASTCache[transientASTCacheIndex] = ast;
            transientASTCacheIndex = (transientASTCacheIndex+1) % transientASTCache.length;
        }

        if (shouldComputeEdgesAndAnnotations)
            vpg.getVPGWriter().computeEdgesAndAnnotations(filename, ast);

        return ast;
    }

    /** @return an AST for the given file.  The AST will remain in memory until it is
     *  explicitly released using {@link #releaseAST(String)} or {@link #releaseAllASTs()}.
     */
    public <T, R extends IVPGNode<T>>
           A acquirePermanentAST(String filename, VPG<A, T, R> vpg)
    {
        A ast = acquireTransientAST(filename, false, vpg);
        return makeTransientASTPermanent(filename, ast);
    }

    /**
     * Changes the AST for the given file from a transient AST to a permanent
     * AST.  The AST will remain in memory until it is explicitly released
     * using {@link #releaseAST(String)} or {@link #releaseAllASTs()}.
     * 
     * @since 2.0
     */
    public <T, R extends IVPGNode<T>>
           A makeTransientASTPermanent(String filename, VPG<A, T, R> vpg)
    {
        return makeTransientASTPermanent(filename, acquireTransientAST(filename, false, vpg));
    }

    /** Changes the AST for the given file from a transient AST to a permanent
     *  AST.  The AST will remain in memory until it is explicitly released
     *  using {@link #releaseAST(String)} or {@link #releaseAllASTs()}.
     */
    public A makeTransientASTPermanent(String filename, A ast)
    {
        transientASTs.remove(filename);
        permanentASTs.put(filename, ast);
        return ast;
    }

    /** Releases the AST for the given file, regardless of whether it was
     *  acquired as a permanent or transient AST. */
    public void releaseAST(String filename)
    {
        transientASTs.remove(filename);
        permanentASTs.remove(filename);
    }

    /**
     * Releases all ASTs, regardless of whether they were acquired as
     * transient and permanent ASTs.
     *
     * @see #acquireTransientAST(String)
     * @see #acquirePermanentAST(String)
     * @see #makeTransientASTPermanent(String)
     */
    public void releaseAllASTs()
    {
        transientASTs.clear();
        permanentASTs.clear();
    }

    /**
     * If the given AST was acquired using {@link #acquireTransientAST(String)} or
     * {@link #acquirePermanentAST(String)}, returns the filename to which it
     * corresponds.  If it is not the root of an AST acquired from this VPG, returns
     * <code>null</code>.
     * <p>
     * Note that this is not an O(1) operation: Internally, the filename is found
     * by comparing the given argument with every AST in memory.
     * 
     * @return filename or <code>null</code>
     * 
     * @since 2.0
     */
    public String getFilenameCorrespondingTo(A ast)
    {
        for (String filename : transientASTs.keySet())
            if (transientASTs.get(filename).get() == ast)
                return filename;
        
        for (String filename : permanentASTs.keySet())
            if (permanentASTs.get(filename) == ast)
                return filename;
        
        return null;
    }
}
