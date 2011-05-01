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
package org.eclipse.rephraserengine.core.vpg.db.ram;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.EOFException;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.PrintStream;
import java.io.Serializable;
import java.nio.channels.FileChannel;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;
import java.util.TreeSet;
import java.util.zip.DeflaterOutputStream;
import java.util.zip.InflaterInputStream;

import org.eclipse.rephraserengine.core.util.Pair;
import org.eclipse.rephraserengine.core.util.TwoKeyHashMap;
import org.eclipse.rephraserengine.core.vpg.IVPGComponentFactory;
import org.eclipse.rephraserengine.core.vpg.IVPGNode;
import org.eclipse.rephraserengine.core.vpg.NodeRef;
import org.eclipse.rephraserengine.core.vpg.VPG;
import org.eclipse.rephraserengine.core.vpg.VPGDB;
import org.eclipse.rephraserengine.core.vpg.VPGDependency;
import org.eclipse.rephraserengine.core.vpg.VPGEdge;

/**
 * VPG database that maintains files, dependencies, edges, and annotations in memory, then flushes
 * the in-memory database to disk when the database is closed (i.e., the Eclipse workbench is
 * closed).
 * 
 * @author Esfar Huq
 * @author Rui Wang
 * @author Jeff Overbey
 * 
 * @param <A> AST type
 * @param <T> token type
 * @param <R> {@link IVPGNode}/{@link NodeRef} type
 * 
 * @since 3.0
 */
public abstract class RAMDB<A, T, R extends IVPGNode<T>>
              extends VPGDB<A, T, R>
{
    protected final File file; //file to which database information will be written to/read from
    protected HashMap<String, Long> files;
    protected HashSet<VPGDependency<A, T, R>> dependencies;
    protected HashMap<R, Set<VPGEdge<A, T, R>>> outgoingEdges;
    protected HashMap<R, Set<VPGEdge<A, T, R>>> incomingEdges;
    protected TwoKeyHashMap<R, Integer, Serializable> annotations;
    
    /** Constructor that initializes the private fields */
    public RAMDB(IVPGComponentFactory<A, T, R> locator, File file)
    {
        super(locator);
        
        this.file = file;
        
        if (file.exists() && file.canRead())
            readFrom(file);
        else
            createEmptyDatabase();
    }

    protected void createEmptyDatabase()
    {
        files = new HashMap<String, Long>();
        dependencies = new HashSet<VPGDependency<A, T, R>>();
        outgoingEdges = new HashMap<R, Set<VPGEdge<A, T, R>>>();
        incomingEdges = new HashMap<R, Set<VPGEdge<A, T, R>>>();
        annotations = new TwoKeyHashMap<R, Integer, Serializable>();
    }
    
    @SuppressWarnings("unchecked")
    protected void readFrom(File file)
    {
        //check if we can load in fields from disk
        try
        {
            ObjectInputStream in =
                new ObjectInputStream(
                    new InflaterInputStream(
                        new BufferedInputStream(
                            new FileInputStream(file))));
            
            files = (HashMap<String, Long>)readObject(in);
            dependencies = (HashSet<VPGDependency<A, T, R>>)readObject(in);
            outgoingEdges = (HashMap<R, Set<VPGEdge<A, T, R>>>)readObject(in);
            incomingEdges = (HashMap<R, Set<VPGEdge<A, T, R>>>)readObject(in);
            annotations = (TwoKeyHashMap<R, Integer, Serializable>)readObject(in);
            
            in.close();
        }
        catch (EOFException e)
        {
            // Database file is probably empty; don't worry
            createEmptyDatabase();
        }
        catch (Exception e)
        {
            //Activator.log(e);
            e.printStackTrace();
            createEmptyDatabase();
        }
    }
    
    protected abstract Object readObject(ObjectInputStream in) throws IOException, ClassNotFoundException;
    
    protected void writeTo(File file)
    {
        //serialize private fields and store them
        try
        {
            ObjectOutputStream out =
                new ObjectOutputStream(
                    new DeflaterOutputStream(
                        new BufferedOutputStream(
                            new FileOutputStream(file))));
            
            out.writeObject(files);
            out.writeObject(dependencies);
            out.writeObject(outgoingEdges);
            out.writeObject(incomingEdges);
            out.writeObject(annotations);
            
            out.close();
        }
        catch (IOException e)
        {
            //Activator.log(e);
            e.printStackTrace();
            file.delete();
        }
    }

    ///////////////////////////////////////////////////////////////////////////
    // API
    ///////////////////////////////////////////////////////////////////////////

    /** Forces any in-memory data to be flushed to disk */
    @Override
    public void flush()
    {
        writeTo(file);
    }

    /** Called when the database is no longer needed.  Typically ensures that
     * any data in memory is flushed to disk and any locks are released.
     */
    @Override
    public void close()
    {
        flush();
        files = null;
        dependencies = null;
        outgoingEdges = null;
        incomingEdges = null;
        annotations = null;
    }

    /** Removes ALL data from the database; also clears the error/warning log. */
    @Override
    public void clearDatabase()
    {
        files.clear();
        dependencies.clear();
        outgoingEdges.clear();
        incomingEdges.clear();
        annotations.clear();
    }
    
    
    // HYPOTHETICAL UPDATING ///////////////////////////////////////////////////
    
    private File originalContents = null;
    
    @Override
    public void enterHypotheticalMode() throws IOException
    {
        if (isInHypotheticalMode()) return;
        
        flush();
        originalContents = copyFile(file);
        clearDatabase();
        readFrom(file);
    }
    
    private static File copyFile(File orig) throws IOException
    {
        File tempFile = File.createTempFile("rephraser-tmp", "db"); //$NON-NLS-1$ //$NON-NLS-2$
        tempFile.deleteOnExit();
        FileChannel from = new FileInputStream(orig).getChannel();
        FileChannel to = new FileOutputStream(tempFile).getChannel();
        to.transferFrom(from, 0, from.size());
        from.close();
        to.close();
        return tempFile;
    }

    @Override
    public void leaveHypotheticalMode() throws IOException
    {
        if (!isInHypotheticalMode()) return;
        
        clearDatabase();
        readFrom(originalContents);
        originalContents.delete();
        originalContents = null;
    }
    
    @Override
    public boolean isInHypotheticalMode()
    {
        return originalContents != null;
    }
    
    
    // FILES ///////////////////////////////////////////////////////////////////

    /** Marks the VPG database entries for the given file as being up-to-date. */
    @Override
    public void updateModificationStamp(String filename)
    {
        files.put(filename, getModificationStamp(filename));
    }

    public abstract long getModificationStamp(String filename);
    
    /** @return true iff the VPG entries for the given file are not up-to-date */
    @Override
    public boolean isOutOfDate(String filename)
    {
        checkIfFileInDatabase(filename);
        
        long storedModificationStamp = files.get(filename);
        return storedModificationStamp < getModificationStamp(filename);
    }

    /** Removes all dependencies, edges, and annotations for the given file. */
    @Override
    public void deleteAllEntriesFor(String filename)
    {
        this.deleteAllIncomingDependenciesFor(filename);
        this.deleteAllOutgoingDependenciesFor(filename);
        
        this.deleteAllEdgesAndAnnotationsFor(filename);
    }

    /** Removes all edges and annotations (but not dependencies) for the given file. */
    @Override
    public void deleteAllEdgesAndAnnotationsFor(String filename)
    {
        //edges
        Iterator<R> it = outgoingEdges.keySet().iterator();
        while (it.hasNext())
            if (it.next().getFilename().equals(filename))
                it.remove();

        it = incomingEdges.keySet().iterator();
        while (it.hasNext())
            if (it.next().getFilename().equals(filename))
                it.remove();

        // annotations
        it = annotations.keySet().iterator();
        while (it.hasNext())
            if (it.next().getFilename().equals(filename))
                it.remove();
    }

    /** Removes all edges pointing inward to any token in the given file. */
    @Override
    public void deleteAllIncomingDependenciesFor(String filename)
    {
        Iterator<VPGDependency<A, T, R>> dItr = dependencies.iterator();
        while (dItr.hasNext())
            if (dItr.next().getDependsOnFile().equals(filename))
                dItr.remove();
    }

    /** Removes all edges pointing outward from any token in the given file. */
    @Override
    public void deleteAllOutgoingDependenciesFor(String filename)
    {
        Iterator<VPGDependency<A, T, R>> dItr = dependencies.iterator();
        while (dItr.hasNext())
            if (dItr.next().getDependentFile().equals(filename))
                dItr.remove();
    }

    /** Returns all filenames present in the VPG database. */
    @Override
    public Set<String> listAllFilenames()
    {
        return files.keySet();
    }

    /** Returns the name of every file on which at least one other file is
     *  dependent. */
    @Override
    public Set<String> listAllFilenamesWithDependents()
    {
        Set<String> toReturn = new HashSet<String>();
        
        for (VPGDependency<A, T, R> d : dependencies)
            toReturn.add(d.getDependsOnFile());
        
        return toReturn;
    }

    /** Returns the name of every file which depends on at least one other
     *  file. */
    @Override
    public Set<String> listAllDependentFilenames()
    {
        Set<String> toReturn = new HashSet<String>();
        
        for (VPGDependency<A, T, R> d : dependencies)
            toReturn.add(d.getDependentFile());
        
        return toReturn;
    }

    
    // DEPENDENCIES ////////////////////////////////////////////////////////////

    /** Adds the given dependency to the VPG database if a dependency between
     *  its files does not already exist. */
    @Override
    public void ensure(VPGDependency<A, T, R> dependency)
    {
        checkIfFileInDatabase(dependency.getDependentFile());
        checkIfFileInDatabase(dependency.getDependsOnFile());
        
        dependencies.add(dependency);
    }

    /** Deletes the given dependency from the VPG database. */
    @Override
    public void delete(VPGDependency<A, T, R> dependency)
    {
        dependencies.remove(dependency);
    }

    /** @return all of the files on which the given file depends */
    @Override
    public Iterable<String> getOutgoingDependenciesFrom(String filename)
    {
        checkIfFileInDatabase(filename);
        
        Set<String> toReturn = new TreeSet<String>();
        for (VPGDependency<A, T, R> d : dependencies)
            if (d.getDependentFile().equals(filename))
                toReturn.add(d.getDependsOnFile());
        
        return toReturn;
    }

    /** @return all of the files dependent on the given file */
    @Override
    public Iterable<String> getIncomingDependenciesTo(String filename)
    {
        checkIfFileInDatabase(filename);
        
        Set<String> toReturn = new TreeSet<String>();
        for (VPGDependency<A, T, R> d : dependencies)
            if (d.getDependsOnFile().equals(filename))
                toReturn.add(d.getDependentFile());
        
        return toReturn;
    }
    
    
    // EDGES ///////////////////////////////////////////////////////////////////

    /** Adds the given edge to the VPG database if an edge of the given type
     *  between its tokens does not already exist. */
    @Override
    public void ensure(VPGEdge<A, T, R> edge)
    {
        checkIfFileInDatabase(edge.getSource().getFilename());
        checkIfFileInDatabase(edge.getSink().getFilename());
        
        R source = edge.getSource();
        R sink = edge.getSink();
        
        Set<VPGEdge<A, T, R>> eSource = outgoingEdges.get(source);
        Set<VPGEdge<A, T, R>> eSink = incomingEdges.get(sink);
        
        if (eSource == null)
            outgoingEdges.put(source, new TreeSet<VPGEdge<A, T, R>>());
        outgoingEdges.get(source).add(edge);

        if (eSink == null) 
            incomingEdges.put(sink, new TreeSet<VPGEdge<A, T, R>>());
        incomingEdges.get(sink).add(edge);
    }

    /** Deletes the given edge from the VPG database. */
    @Override
    public void delete(VPGEdge<A, T, R> edge)
    {
        Set<VPGEdge<A, T, R>> e1 = outgoingEdges.get(edge.getSource());
        if (e1 != null && e1.contains(edge))
        {
            if (e1.size() == 1)
                outgoingEdges.remove(edge.getSource());
            else
                outgoingEdges.get(edge.getSource()).remove(edge);
        }
        
        Set<VPGEdge<A, T, R>> e2 = incomingEdges.get(edge.getSink());
        if (e2 != null && e2.contains(edge))
        {
            if (e2.size() == 1)
                incomingEdges.remove(edge.getSink());
            else
                incomingEdges.get(edge.getSink()).remove(edge);
        }
    }

    /**
     * Returns a list of all of the edges with at least one endpoint in the given file.
     * <p>
     * Due to implementation details, some edges may be listed more than once.
     *
     */
    @Override
    public Collection<? extends VPGEdge<A, T, R>> getAllEdgesFor(String filename)
    {
        checkIfFileInDatabase(filename);
        
        Set<VPGEdge<A, T, R>> toReturn = new TreeSet<VPGEdge<A, T, R>>(); 

        for (R r : outgoingEdges.keySet())
            if (r.getFilename().equals(filename))
                toReturn.addAll(this.getOutgoingEdgesFrom(r, ALL_EDGES));
   
        for (R r : incomingEdges.keySet())
            if (r.getFilename().equals(filename))
                toReturn.addAll(this.getIncomingEdgesTo(r, ALL_EDGES));
   
        return toReturn;
    }

    /**
     * Returns a list of the edges extending from the given token.
     * <p>
     * To only return edges of a particular type, set the <code>edgeType</code>
     * parameter to that type.
     *
     * @param edgeType the type of edge (an arbitrary non-negative integer), or
     *                 {@link VPG#ALL_EDGES_ALLOWED} to return all edges, regardless
     *                 of type THIS IS INDICATED BY Integer.MIN_VALUE
     */
    @Override
    public Collection<? extends VPGEdge<A, T, R>> getOutgoingEdgesFrom(R source, int edgeType)
    {
        if (outgoingEdges.get(source) == null) return Collections.emptySet();
        
        checkIfFileInDatabase(source.getFilename());
        
        if (edgeType == ALL_EDGES)
        {
            return outgoingEdges.get(source);
        }
        else
        {
            Set<VPGEdge<A, T, R>> toReturn = new TreeSet<VPGEdge<A, T, R>>();
            for (VPGEdge<A, T, R> e : outgoingEdges.get(source))
                if (e.getType() == edgeType)
                    toReturn.add(e);
            return toReturn;
        }
    }

    /**
     * Returns a list of the edges pointing at the given token.
     * <p>
     * To only return edges of a particular type, set the <code>edgeType</code>
     * parameter to that type.
     *
     * @param edgeType the type of edge (an arbitrary non-negative integer), or
     *                 {@link VPG#ALL_EDGES} to return all edges, regardless
     *                 of type THIS IS INDICATED BY Integer.MIN_VALUE
     */
    @Override
    public Collection<? extends VPGEdge<A, T, R>> getIncomingEdgesTo(R sink, int edgeType)
    {
        if (incomingEdges.get(sink) == null) return new TreeSet<VPGEdge<A, T, R>>();
        
        checkIfFileInDatabase(sink.getFilename());
        
        if (edgeType == ALL_EDGES)
        {
            return incomingEdges.get(sink);
        }
        else
        {
            Set<VPGEdge<A, T, R>> toReturn = new TreeSet<VPGEdge<A, T, R>>();
            for (VPGEdge<A, T, R> e : incomingEdges.get(sink))
                if (e.getType() == edgeType)
                    toReturn.add(e);
            return toReturn;
        }
    }
    
    
    // ANNOTATIONS /////////////////////////////////////////////////////////////

    /**
     * Annotates the given token with the given (serializable) object
     * (which may be <code>null</code>).
     * If an annotation for the given token with the given ID already exists,
     * it will be replaced.
     * <p>
     * A token can have several annotations, but each annotation must be given
     * a unique ID.  For example, annotation 0 might describe
     * the type of an identifier, while annotation 1 might hold documentation
     * for that identifier.
     */
    @Override
    public void setAnnotation(R token, int annotationID, Serializable annotation)
    {
        checkIfFileInDatabase(token.getFilename());
        
        annotations.put(token, annotationID, annotation);
    }

    /** Deletes the annotation with the given ID for the given token, if it exists. */
    @Override
    public void deleteAnnotation(R token, int annotationID)
    {
        annotations.remove(token, annotationID);
    }

    /** @return the annotation with the given ID for the given token, or <code>null</code>
     *  if it does not exist */
    @Override
    public Serializable getAnnotation(R token, int annotationID)
    {
        checkIfFileInDatabase(token.getFilename());
        
        return annotations.getEntry(token, annotationID);
    }

    /**
     * Returns a list of all of the annotations in the given file.
     * <p>
     * The first entry of each pair is a {@link IVPGNode}, and the second is an annotation type.
     * The annotation can be retrieved using {@link VPGDB#getAnnotation(IVPGNode, int)}.
     * <p>
     * Due to implementation details, some annotations may be listed more than once.
     * 
     * @since 3.0
     */
    @Override
    public Iterable<Pair<R, Integer>> getAllAnnotationsFor(String filename)
    {
        checkIfFileInDatabase(filename);
        
        Set<Pair<R, Integer>> toReturn = new HashSet<Pair<R, Integer>>();
        
        for (R r : annotations.keySet())
            if (r.getFilename().equals(filename))
                for (Integer i : annotations.getAllEntriesFor(r).keySet())
                    toReturn.add(new Pair<R, Integer>((R)r, i));
        
        return toReturn;
    }
    
    
    // UTILITY METHODS /////////////////////////////////////////////////////////

    @Override
    public void printOn(PrintStream out)
    {
        out.println("MODIFICATION STAMPS:"); //$NON-NLS-1$
        out.println();
        out.println(this.files.toString());

        out.println();
        out.println();
        out.println("DEPENDENCIES:"); //$NON-NLS-1$
        out.println();
        out.println(this.dependencies.toString());

        out.println();
        out.println();
        out.println("EDGES:"); //$NON-NLS-1$
        out.println();
        out.println(this.outgoingEdges.toString());

        out.println();
        out.println();
        out.println("ANNOTATIONS:"); //$NON-NLS-1$
        out.println();
        out.println(this.annotations.toString());
    }

    @Override
    public void printStatisticsOn(PrintStream out)
    {
        //BLANK
    }

    @Override
    public void resetStatistics()
    {
        //BLANK
    }
    
    /** Checks to see if a file is in the database, if not, bring the file in **/
    private void checkIfFileInDatabase(String filename)
    {
        if (!files.containsKey(filename))
            files.put(filename, Long.MIN_VALUE);
    }
}
