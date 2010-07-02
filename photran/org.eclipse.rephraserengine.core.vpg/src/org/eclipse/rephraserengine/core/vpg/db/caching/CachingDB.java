/*******************************************************************************
 * Copyright (c) 2009 University of Illinois at Urbana-Champaign and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     UIUC - Initial API and implementation
 *******************************************************************************/
package org.eclipse.rephraserengine.core.vpg.db.caching;

import java.io.IOException;
import java.io.PrintStream;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.NoSuchElementException;

import org.eclipse.core.runtime.Assert;
import org.eclipse.rephraserengine.core.util.Pair;
import org.eclipse.rephraserengine.core.vpg.TokenRef;
import org.eclipse.rephraserengine.core.vpg.VPG;
import org.eclipse.rephraserengine.core.vpg.VPGDB;
import org.eclipse.rephraserengine.core.vpg.VPGDependency;
import org.eclipse.rephraserengine.core.vpg.VPGEdge;
import org.eclipse.rephraserengine.core.vpg.VPGLog;


/**
 * Base class for a typical Virtual Program Graph database the caches the result of another
 * (on-disk) database.
 * <p>
 * This class is intended to be subclassed directly, although Eclipse-based VPGs will subclass
 * <code>EclipseVPG</code> instead.
 * Subclasses must implement the language-specific (parser/AST) methods.
 * <p>
 * N.B. See several important notes in the JavaDoc for {@link VPG}.
 *
 * @author Jeff Overbey
 *
 * @param <A> AST type
 * @param <T> token type
 * @param <R> TokenRef type
 * @param <D> database type
 * 
 * @since 1.0
 */
public class CachingDB<A, T, R extends TokenRef<T>, D extends VPGDB<A, T, R, L>, L extends VPGLog<T, R>>
     extends VPGDB<A, T, R, L>
{
    private final class CacheKey
    {
        public final R tokenRef;
        public final int id;

        public CacheKey(R tokenRef, int edgeType)
        {
            this.tokenRef = tokenRef;
            this.id = edgeType;
        }

        @SuppressWarnings("unchecked")
        @Override public boolean equals(Object o)
        {
            try
            {
                if (o == null) return false;
                CacheKey other = (CacheKey)o;
                return this.tokenRef.equals(other.tokenRef)
                    && this.id == other.id;
            }
            catch (ClassCastException e)
            {
                return false;
            }
        }

        @Override public int hashCode()
        {
            return tokenRef.hashCode() * 17 + id;
        }
    }

    public D db;

    private int maxEdgeCacheEntries;
    private int maxAnnotationCacheEntries;

    private HashMap<CacheKey, Iterable<? extends VPGEdge<A, T, R>>> incomingEdgeCache;
    private HashMap<CacheKey, Iterable<? extends VPGEdge<A, T, R>>> outgoingEdgeCache;
    private HashMap<CacheKey, Serializable> annotationCache;

    private long edgeHits = 0, edgeMisses = 0, totalEdgeListBuildTime = 0;
    private long annotationHits = 0, annotationMisses = 0, totalDeserializationTime = 0;

    public CachingDB(D diskDatabase)
    {
        this(diskDatabase, Integer.MAX_VALUE, Integer.MAX_VALUE);
    }

    public CachingDB(D diskDatabase, int maxEdgeCacheEntries, int maxAnnotationCacheEntries)
    {
        super();

        Assert.isNotNull(diskDatabase);
        Assert.isTrue(diskDatabase != this);
        Assert.isTrue(maxEdgeCacheEntries > 0, "maxEdgeCacheEntries must be a positive integer"); //$NON-NLS-1$
        Assert.isTrue(maxAnnotationCacheEntries > 0, "maxAnnotationCacheEntries must be a positive integer"); //$NON-NLS-1$

        this.db = diskDatabase;
        this.maxEdgeCacheEntries = maxEdgeCacheEntries;
        this.maxAnnotationCacheEntries = maxAnnotationCacheEntries;
        this.incomingEdgeCache = new HashMap<CacheKey, Iterable<? extends VPGEdge<A, T, R>>>();
        this.outgoingEdgeCache = new HashMap<CacheKey, Iterable<? extends VPGEdge<A, T, R>>>();
        this.annotationCache = new HashMap<CacheKey, Serializable>();
    }

    @Override public void setVPG(VPG<A, T, R, ? extends VPGDB<A, T, R, L>, L> vpg)
    {
        super.setVPG(vpg);
        db.setVPG(vpg);
    }

    ////////////////////////////////////////////////////////////////////////////
    // VPG DATABASE METHODS
    ////////////////////////////////////////////////////////////////////////////

    @Override public void flush()
    {
        db.flush();
    }

    @Override public void close()
    {
        db.close();
    }

    private void clearCache()
    {
        incomingEdgeCache.clear();
        outgoingEdgeCache.clear();
        annotationCache.clear();
    }

    @Override public void clearDatabase()
    {
        clearCache();
        db.clearDatabase();
    }

    // HYPOTHETICAL UPDATING ///////////////////////////////////////////////////

    @Override public void enterHypotheticalMode() throws IOException
    {
        clearCache();
        db.enterHypotheticalMode();
    }

    @Override public void leaveHypotheticalMode() throws IOException
    {
        clearCache();
        db.leaveHypotheticalMode();
    }

    @Override public boolean isInHypotheticalMode()
    {
        return db.isInHypotheticalMode();
    }

    // FILES ///////////////////////////////////////////////////////////////////

    @Override public void updateModificationStamp(String filename)
    {
        db.updateModificationStamp(filename);
    }

    @Override public boolean isOutOfDate(String filename)
    {
        return db.isOutOfDate(filename);
    }

    @Override public void deleteAllEntriesFor(String filename)
    {
        clearCache();
        db.deleteAllEntriesFor(filename);
    }

    @Override public void deleteAllEdgesAndAnnotationsFor(String filename)
    {
        clearCache();
        db.deleteAllEdgesAndAnnotationsFor(filename);
    }

    @Override public void deleteAllIncomingDependenciesFor(String filename)
    {
        db.deleteAllIncomingDependenciesFor(filename);
    }

    @Override public void deleteAllOutgoingDependenciesFor(String filename)
    {
        db.deleteAllOutgoingDependenciesFor(filename);
    }

    @Override public Iterable<String> listAllFilenames()
    {
    	return db.listAllFilenames();
    }

    @Override public Iterable<String> listAllFilenamesWithDependents()
    {
    	return db.listAllFilenamesWithDependents();
    }

    @Override public Iterable<String> listAllDependentFilenames()
    {
    	return db.listAllDependentFilenames();
    }

    // DEPENDENCIES ////////////////////////////////////////////////////////////

    @Override public void ensure(VPGDependency<A, T, R> dependency)
    {
        db.ensure(dependency);
    }

    @Override public void delete(VPGDependency<A, T, R> dependency)
    {
        db.delete(dependency);
    }

    @Override public Iterable<String> getOutgoingDependenciesFrom(String filename)
    {
        return db.getOutgoingDependenciesFrom(filename);
    }

    @Override public Iterable<String> getIncomingDependenciesTo(String filename)
    {
        return db.getIncomingDependenciesTo(filename);
    }

    // EDGES ///////////////////////////////////////////////////////////////////

    @Override public void ensure(VPGEdge<A, T, R> edge)
    {
        removeFromCache(edge);
        db.ensure(edge);
    }

    @Override public void delete(VPGEdge<A, T, R> edge)
    {
        removeFromCache(edge);
        db.delete(edge);
    }

    private void removeFromCache(VPGEdge<A, T, R> edge)
    {
        incomingEdgeCache.remove(new CacheKey(edge.getSink(), edge.getType()));
        outgoingEdgeCache.remove(new CacheKey(edge.getSource(), edge.getType()));
    }

    @Override public Iterable<? extends VPGEdge<A, T, R>> getAllEdgesFor(String filename)
    {
        return db.getAllEdgesFor(filename);
    }

    @Override public Iterable<? extends VPGEdge<A, T, R>> getOutgoingEdgesFrom(R tokenRef, int edgeType)
    {
        CacheKey key = new CacheKey(tokenRef, edgeType);
        if (outgoingEdgeCache.containsKey(key))
        {
            edgeHits++;
            //System.out.println("Edge cache hit");
            return outgoingEdgeCache.get(key);
        }
        else
        {
            edgeMisses++;
            //System.out.println("Edge cache miss");
            return buildEdgeCache(outgoingEdgeCache, key, db.getOutgoingEdgesFrom(tokenRef, edgeType));
        }
    }

    @Override public Iterable<? extends VPGEdge<A, T, R>> getIncomingEdgesTo(R tokenRef, int edgeType)
    {
        CacheKey key = new CacheKey(tokenRef, edgeType);
        if (incomingEdgeCache.containsKey(key))
        {
            edgeHits++;
            //System.out.println("Edge cache hit");
            return incomingEdgeCache.get(key);
        }
        else
        {
            edgeMisses++;
            //System.out.println("Edge cache miss");
            return buildEdgeCache(incomingEdgeCache, key, db.getIncomingEdgesTo(tokenRef, edgeType));
        }
    }

    private Iterable<? extends VPGEdge<A, T, R>> buildEdgeCache(
        HashMap<CacheKey, Iterable<? extends VPGEdge<A, T, R>>> cache,
        CacheKey key,
        Iterable<? extends VPGEdge<A, T, R>> iterable)
    {
        try
        {
            if (cache.size() > maxEdgeCacheEntries)
            {
//                Iterator<CacheKey> it = cache.keySet().iterator();
//                if (it.next() != null)
//                    it.remove();
            	try
            	{
            		cache.remove(cache.keySet().iterator().next());
            	}
            	catch (NoSuchElementException e)
            	{
            		// This should not happen since cache.size() > 1,
            		// but it's been reported (S Kalogerakos on Photran
            		// mailing list), so we should handle it
            		cache.clear();
            	}
            }

            if (maxEdgeCacheEntries > 0)
            {
                long start = System.currentTimeMillis();

                ArrayList<VPGEdge<A, T, R>> list = new ArrayList<VPGEdge<A, T, R>>();
                for (VPGEdge<A, T, R> edge : iterable)
                    list.add(edge);

                long buildTime = System.currentTimeMillis() - start;
                totalEdgeListBuildTime += buildTime;
                //System.out.println("Edge list build time: " + buildTime + " ms");

                cache.put(key, list);
                return list;
            }
            else
            {
                return iterable;
            }
        }
        catch (OutOfMemoryError e)
        {
            maxEdgeCacheEntries = cache.size()-1;
            return iterable;
        }
    }

    // ANNOTATIONS /////////////////////////////////////////////////////////////

    @Override public void setAnnotation(R token, int annotationID, Serializable annotation)
    {
        removeFromCache(token, annotationID);
        db.setAnnotation(token, annotationID, annotation);
    }

    @Override public void deleteAnnotation(R token, int annotationID)
    {
        removeFromCache(token, annotationID);
        db.deleteAnnotation(token, annotationID);
    }

    private void removeFromCache(R token, int annotationID)
    {
        annotationCache.remove(new CacheKey(token, annotationID));
    }

    @Override public Serializable getAnnotation(R tokenRef, int annotationID)
    {
        CacheKey key = new CacheKey(tokenRef, annotationID);
        if (annotationCache.containsKey(key))
        {
            annotationHits++;
            //System.out.println("Annotation cache hit");
            return annotationCache.get(key);
        }
        else
        {
            annotationMisses++;
            //System.out.println("Annotation cache miss");
            if (annotationCache.size() > maxAnnotationCacheEntries)
            {
//                Iterator<CacheKey> it = annotationCache.keySet().iterator();
//                if (it.next() != null)
//                    it.remove();
                annotationCache.remove(annotationCache.keySet().iterator().next());
            }

            long start = System.currentTimeMillis();
            Serializable ann = db.getAnnotation(tokenRef, annotationID);
            long deserTime = System.currentTimeMillis() - start;
            totalDeserializationTime += deserTime;
            //System.out.println("Annotation deserialization time: " + deserTime + " ms");

            annotationCache.put(key, ann);
            return ann;
        }
    }

    @Override public Iterable<Pair<R, Integer>> getAllAnnotationsFor(String filename)
    {
        return db.getAllAnnotationsFor(filename);
    }

    // UTILITY METHODS /////////////////////////////////////////////////////////

    @Override public void printOn(PrintStream out)
    {
        printStatisticsOn(out);
        out.println();
        db.printOn(out);
    }

    @Override public void printStatisticsOn(PrintStream out)
    {
        out.println("Database Cache Statistics:"); //$NON-NLS-1$

        long edgeTotal = edgeHits + edgeMisses;
        float edgeHitRatio = edgeTotal == 0 ? 0 : ((float)edgeHits) / edgeTotal * 100;
        out.println("    Edge Cache Hit Ratio:        " + edgeHits + "/" + edgeTotal + " (" + (long)Math.round(edgeHitRatio) + "%)"); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$

        long annotationTotal = annotationHits + annotationMisses;
        float annotationHitRatio = annotationTotal == 0 ? 0 : ((float)annotationHits) / annotationTotal * 100;
        out.println("    Annotation Cache Hit Ratio: " + annotationHits + "/" + annotationTotal + " (" + (long)Math.round(annotationHitRatio) + "%)"); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$

        if (edgeMisses > 0)
            out.println("    Average edge list build time: " + (totalEdgeListBuildTime/edgeMisses) + " ms"); //$NON-NLS-1$ //$NON-NLS-2$

        if (annotationMisses > 0)
            out.println("    Average annotation deserialization time: " + (totalDeserializationTime/annotationMisses) + " ms"); //$NON-NLS-1$ //$NON-NLS-2$

//      out.println();
//      out.println("Cache Sizes:");
//      out.println("    Incoming Edge Cache: " + incomingEdgeCache.size() + " entries (max " + maxEdgeCacheEntries + ")");
//      out.println("    Incoming Edge Cache: " + outgoingEdgeCache.size() + " entries (max " + maxEdgeCacheEntries + ")");
//      out.println("    Annotation Cache: " + annotationCache.size() + " entries (max " + maxAnnotationCacheEntries + ")");
        
        db.printStatisticsOn(out);
    }

    @Override public void resetStatistics()
    {
        edgeHits = edgeMisses = annotationHits = annotationMisses = totalEdgeListBuildTime = totalDeserializationTime = 0;
    }
}