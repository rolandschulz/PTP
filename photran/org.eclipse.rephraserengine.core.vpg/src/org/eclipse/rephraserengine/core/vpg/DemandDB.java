/*******************************************************************************
 * Copyright (c) 2011 University of Illinois at Urbana-Champaign and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     UIUC - Initial API and implementation
 *******************************************************************************/
package org.eclipse.rephraserengine.core.vpg;

import java.io.IOException;
import java.io.PrintStream;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.eclipse.core.runtime.Assert;
import org.eclipse.rephraserengine.core.util.Pair;
import org.eclipse.rephraserengine.core.util.SetOfPairs;

/**
 * Decorator for a VPG database that handles lazy edge computations (i.e., edges computed on demand).
 *
 * @author Jeff Overbey
 *
 * @param <A> AST type
 * @param <T> token type
 * @param <R> TokenRef type
 * 
 * @since 3.0
 */
public final class DemandDB<A, T, R extends IVPGNode<T>>
           extends VPGDB<A, T, R>
{
    private final VPGDB<A, T, R> db;
    
    private SetOfPairs<String, ILazyVPGPopulator> oldFilesPopulated;
    private SetOfPairs<String, ILazyVPGPopulator> filesPopulated;
    
    private ILazyVPGPopulator[] lazyPopulators;
    private Map<Integer, ILazyVPGPopulator> edgeTypePopulators;
    private Map<Integer, ILazyVPGPopulator> annotationTypePopulators;

    public DemandDB(VPGDB<A, T, R> diskDatabase)
    {
        super(diskDatabase);

        Assert.isNotNull(diskDatabase);
        Assert.isTrue(diskDatabase != this);

        this.db = diskDatabase;
        this.oldFilesPopulated = null;
        this.filesPopulated = new SetOfPairs<String, ILazyVPGPopulator>();
        this.edgeTypePopulators = new HashMap<Integer, ILazyVPGPopulator>();
        this.annotationTypePopulators = new HashMap<Integer, ILazyVPGPopulator>();
    }
    
    void setContentProvider(VPGWriter<A, T, R> contentProvider)
    {
        this.lazyPopulators = contentProvider.getLazyEdgePopulators();
        
        for (ILazyVPGPopulator populator : lazyPopulators)
        {
            for (int edgeType : populator.edgeTypesPopulated())
                edgeTypePopulators.put(edgeType, populator);

            for (int annotationType : populator.annotationTypesPopulated())
                annotationTypePopulators.put(annotationType, populator);
        }
    }
    
    ////////////////////////////////////////////////////////////////////////////
    // NEW API
    ////////////////////////////////////////////////////////////////////////////

    public boolean lazyComputationEnabled = true;

    ////////////////////////////////////////////////////////////////////////////
    // UTILITY METHODS
    ////////////////////////////////////////////////////////////////////////////

    private void ensureLazyEdgesAndAnnotations(String targetFile)
    {
        if (!lazyComputationEnabled) return;
        
        runPopulators(getPopulatorsThatHaveNotBeenRunOn(targetFile), targetFile);
    }

    private List<ILazyVPGPopulator> getPopulatorsThatHaveNotBeenRunOn(String targetFile)
    {
        List<ILazyVPGPopulator> result = new ArrayList<ILazyVPGPopulator>(lazyPopulators.length);
        for (ILazyVPGPopulator populator : lazyPopulators)
            if (!filesPopulated.contains(targetFile, populator))
                result.add(populator);
        return result;
    }

    private void runPopulators(Collection<ILazyVPGPopulator> populators, String targetFile)
    {
        if (somePopulatorRequiresDependentFilesToBePopulated(populators))
        {
            ArrayList<String> files = new ArrayList<String>(Collections.singletonList(targetFile));
            for (String filename : sortFilesAccordingToDependencies(files))
            {
                for (ILazyVPGPopulator populator : populators)
                {
                    // Add the populator to the list BEFORE running it so that
                    // it can read edges that it has written without causing
                    // infinite recursion
                    filesPopulated.add(filename, populator);
                    populator.populateVPG(filename);
                }
            }
        }
        else
        {
            for (ILazyVPGPopulator populator : populators)
            {
                filesPopulated.add(targetFile, populator);
                populator.populateVPG(targetFile);
            }
        }
    }

    private boolean somePopulatorRequiresDependentFilesToBePopulated(Collection<ILazyVPGPopulator> populatorsToRun)
    {
        for (ILazyVPGPopulator populator : populatorsToRun)
            if (populator.dependentFilesMustBePopulated())
                return true;

        return false;
    }

    private void ensureLazyEdge(String targetFile, int edgeType)
    {
        if (!lazyComputationEnabled) return;
        
        if (edgeTypePopulators.containsKey(edgeType))
        {
            ILazyVPGPopulator populator = edgeTypePopulators.get(edgeType);
            if (!filesPopulated.contains(targetFile, populator))
                runPopulators(Collections.singleton(populator), targetFile);
        }
    }

    private void ensureLazyAnnotation(String targetFile, int annotationType)
    {
        if (!lazyComputationEnabled) return;
        
        if (annotationTypePopulators.containsKey(annotationType))
        {
            ILazyVPGPopulator populator = annotationTypePopulators.get(annotationType);
            if (!filesPopulated.contains(targetFile, populator))
                runPopulators(Collections.singleton(populator), targetFile);
        }
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

    @Override public void clearDatabase()
    {
        filesPopulated.clear();
        db.clearDatabase();
    }

    // HYPOTHETICAL UPDATING ///////////////////////////////////////////////////

    @Override public void enterHypotheticalMode() throws IOException
    {
        oldFilesPopulated = filesPopulated.clone();
        db.enterHypotheticalMode();
    }

    @Override public void leaveHypotheticalMode() throws IOException
    {
        db.leaveHypotheticalMode();
        filesPopulated = oldFilesPopulated;
        oldFilesPopulated = null;
    }

    @Override public boolean isInHypotheticalMode()
    {
        return oldFilesPopulated != null; // true iff db.isInHypotheticalMode();
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
        filesPopulated.remove(filename);
        db.deleteAllEntriesFor(filename);
    }

    @Override public void deleteAllEdgesAndAnnotationsFor(String filename)
    {
        filesPopulated.remove(filename);
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
        db.ensure(edge);
    }

    @Override public void delete(VPGEdge<A, T, R> edge)
    {
        db.delete(edge);
    }

    @Override public Iterable<? extends VPGEdge<A, T, R>> getAllEdgesFor(String filename)
    {
        ensureLazyEdgesAndAnnotations(filename);
        return db.getAllEdgesFor(filename);
    }

    @Override public Iterable<? extends VPGEdge<A, T, R>> getOutgoingEdgesFrom(R tokenRef, int edgeType)
    {
        ensureLazyEdge(tokenRef.getFilename(), edgeType);
        return db.getOutgoingEdgesFrom(tokenRef, edgeType);
    }

    @Override public Iterable<? extends VPGEdge<A, T, R>> getIncomingEdgesTo(R tokenRef, int edgeType)
    {
        ensureLazyEdge(tokenRef.getFilename(), edgeType);
        return db.getIncomingEdgesTo(tokenRef, edgeType);
    }

    // ANNOTATIONS /////////////////////////////////////////////////////////////

    @Override public void setAnnotation(R token, int annotationID, Serializable annotation)
    {
        db.setAnnotation(token, annotationID, annotation);
    }

    @Override public void deleteAnnotation(R token, int annotationID)
    {
        db.deleteAnnotation(token, annotationID);
    }

    @Override public Serializable getAnnotation(R tokenRef, int annotationID)
    {
        ensureLazyAnnotation(tokenRef.getFilename(), annotationID);
        return db.getAnnotation(tokenRef, annotationID);
    }

    @Override public Iterable<Pair<R, Integer>> getAllAnnotationsFor(String filename)
    {
        ensureLazyEdgesAndAnnotations(filename);
        return db.getAllAnnotationsFor(filename);
    }

    // UTILITY METHODS /////////////////////////////////////////////////////////

    @Override public void printOn(PrintStream out)
    {
        db.printOn(out);
    }

    @Override public void printStatisticsOn(PrintStream out)
    {
        db.printStatisticsOn(out);
    }

    @Override public void resetStatistics()
    {
        db.resetStatistics();
    }
}