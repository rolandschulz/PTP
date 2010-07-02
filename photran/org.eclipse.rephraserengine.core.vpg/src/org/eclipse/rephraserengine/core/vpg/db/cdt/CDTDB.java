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
package org.eclipse.rephraserengine.core.vpg.db.cdt;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.PrintStream;
import java.io.Serializable;
import java.nio.channels.FileChannel;
import java.util.Collections;
import java.util.Iterator;
import java.util.LinkedList;

import org.eclipse.core.runtime.Assert;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.rephraserengine.core.vpg.TokenRef;
import org.eclipse.rephraserengine.core.vpg.VPG;
import org.eclipse.rephraserengine.core.vpg.VPGDB;
import org.eclipse.rephraserengine.core.vpg.VPGDependency;
import org.eclipse.rephraserengine.core.vpg.VPGEdge;
import org.eclipse.rephraserengine.core.vpg.VPGLog;
import org.eclipse.rephraserengine.internal.core.vpg.db.cdt.InternalCDTDB;
import org.eclipse.rephraserengine.internal.core.vpg.db.cdt.InternalCDTDB.IntVector;


/**
 * Base class for a typical Virtual Program Graph with a database maintained on disk using the
 * Eclipse C/C++ Development Tool's internal indexer database.
 * <p>
 * This class is intended to be subclassed directly, although Eclipse-based VPGs will subclass
 * <code>EclipseVPG</code> instead.
 * Subclasses must implement the language-specific (parser/AST) methods.
 * <p>
 * N.B. You <i>must</i> call {@link #close()} to flush the database to disk.
 * <p>
 * N.B. See several important notes in the JavaDoc for {@link VPG}.
 *
 * @author Jeff Overbey
 *
 * @param <A> AST type
 * @param <T> token type
 * @param <R> TokenRef type
 * @param <L> log type
 * 
 * @since 1.0
 */
public abstract class CDTDB<A, T, R extends TokenRef<T>, L extends VPGLog<T, R>>
              extends VPGDB<A, T, R, L>
{
    private InternalCDTDB db;
    private File lock;

    public CDTDB(String filename)
    {
        this(new File(filename));
    }

    public CDTDB(File file)
    {
        super();

        Assert.isNotNull(file);

        try
        {
            lock = new File(file.getPath() + ".lock"); //$NON-NLS-1$
            db = new InternalCDTDB(file);
        }
        catch (CoreException e)
        {
            throw new Error("Unable to create VPG database " + file.getName(), e); //$NON-NLS-1$
        }
    }

    ////////////////////////////////////////////////////////////////////////////
    // VPG DATABASE METHODS
    ////////////////////////////////////////////////////////////////////////////

    @Override public void setVPG(VPG<A, T, R, ? extends VPGDB<A, T, R, L>, L> vpg)
    {
        super.setVPG(vpg);
        clearDBIfPossiblyCorrupted();
    }

    /**
     * Clears the database if a lock file exists, indicating that it might be corrupted.
     * <p>
     * A lock file is created when the database is started.  If Eclipse is shut down cleanly, the
     * lock file is deleted.  However, if it is terminated abnormally, the lock file will still
     * exist.  This checks if the lock file exists, and, if so, clears the database, which will
     * force the VPG to re-index all the source files in the workspace.
     * <p>
     * Note that only one instance of Eclipse can be open on a particular workspace, so it is not
     * possible that the lock exists because the database is open in a different instance of Eclipse.
     */
    private void clearDBIfPossiblyCorrupted()
    {
        if (lock.exists())
        {
            this.clearDatabase();
        }
        else
        {
            try
            {
                lock.createNewFile();
            }
            catch (IOException e)
            {
                //ignore
            }
        }
    }

    @Override public void flush()
    {
        try
        {
            db.flush();
        }
        catch (CoreException e)
        {
            throw new Error("Unable to flush VPG database to disk", e); //$NON-NLS-1$
        }
    }

    @Override public void close()
    {
        try
        {
            db.close();
            lock.delete();
        }
        catch (CoreException e)
        {
            throw new Error("Unable to close VPG database", e); //$NON-NLS-1$
        }
    }

    @Override public void clearDatabase()
    {
        getVPG().log.clear();

        try
        {
            db.clear();
        }
        catch (CoreException e)
        {
            getVPG().log.logError(e);
        }
    }

    // HYPOTHETICAL UPDATING ///////////////////////////////////////////////////
    
    private InternalCDTDB origDB = null;
    
    @Override public void enterHypotheticalMode() throws IOException
    {
        assert !isInHypotheticalMode();
        
        try
        {
            db.flush();
            File copy = copyFile(db.getFile());
            
            origDB = db;
            db = new InternalCDTDB(copy);
        }
        catch (CoreException e)
        {
            throw new Error(e);
        }
    }
    
    private static File copyFile(File orig) throws IOException
    {
        File tempFile = File.createTempFile("rephraser-tmp", "db"); //$NON-NLS-1$ //$NON-NLS-2$
        FileChannel from = new FileInputStream(orig).getChannel();
        FileChannel to = new FileOutputStream(tempFile).getChannel();
        to.transferFrom(from, 0, from.size());
        from.close();
        to.close();
        return tempFile;
    }
    
    @Override public void leaveHypotheticalMode()
    {
        assert isInHypotheticalMode();
        
        db = origDB;
        origDB = null;
    }
    
    @Override public boolean isInHypotheticalMode()
    {
        return origDB != null;
    }

    // FILES ///////////////////////////////////////////////////////////////////

    private int ensureEntryForFile(String filename) throws CoreException
    {
        return db.files.ensure(filename);
    }

    @Override public void updateModificationStamp(String filename)
    {
        try
        {
            db.files.setModificationStamp(ensureEntryForFile(filename), getModificationStamp(filename));
        }
        catch (CoreException e)
        {
            getVPG().log.logError(e);
        }
    }

    @Override public boolean isOutOfDate(String filename)
    {
        try
        {
            long storedModificationStamp = db.files.getModificationStamp(ensureEntryForFile(filename));
            //System.out.println((storedModificationStamp < getModificationStamp(filename)) + " - " + storedModificationStamp + " " + getModificationStamp(filename) + " - " + filename);
            return storedModificationStamp < getModificationStamp(filename);
        }
        catch (CoreException e)
        {
            return true;
        }
    }

    protected abstract long getModificationStamp(String filename);

    /**
     * Deletes all dependencies, edges, and token annotations that reference
     * a token in the given file
     *
     * @param filename (non-null)
     */
    @Override public void deleteAllEntriesFor(String filename)
    {
        try
        {
            db.dependencies.deleteAllIncomingDependenciesTo(filename);
            db.dependencies.deleteAllOutgoingDependenciesFrom(filename);
            db.edges.deleteAllIncomingEdgesTo(filename);
            db.edges.deleteAllOutgoingEdgesFrom(filename);
            db.annotations.deleteAllAnnotationsFor(filename);
            db.files.delete(filename);
        }
        catch (CoreException e)
        {
            getVPG().log.logError(e);
        }
    }

    @Override public void deleteAllEdgesAndAnnotationsFor(String filename)
    {
        try
        {
            db.edges.deleteAllIncomingEdgesTo(filename);
            db.edges.deleteAllOutgoingEdgesFrom(filename);
            db.annotations.deleteAllAnnotationsFor(filename);
        }
        catch (CoreException e)
        {
            getVPG().log.logError(e);
        }
    }

    @Override public void deleteAllIncomingDependenciesFor(String filename)
    {
        try
        {
            db.dependencies.deleteAllIncomingDependenciesTo(filename);
        }
        catch (CoreException e)
        {
            getVPG().log.logError(e);
        }
    }

    @Override public void deleteAllOutgoingDependenciesFor(String filename)
    {
        try
        {
            db.dependencies.deleteAllOutgoingDependenciesFrom(filename);
        }
        catch (CoreException e)
        {
            getVPG().log.logError(e);
        }
    }

    private void cleanUp(String filename)
    {
        try
        {
            if (db.dependencies.hasIncomingDependencyRecords(filename)) return;
            if (db.dependencies.hasOutgoingDependencyRecords(filename)) return;
            if (db.edges.hasIncomingEdges(filename)) return;
            if (db.edges.hasOutgoingEdges(filename)) return;
            if (db.annotations.hasAnnotations(filename)) return;

            db.files.delete(filename);
        }
        catch (CoreException e)
        {
            getVPG().log.logError(e);
        }
    }

    @Override public Iterable<String> listAllFilenames()
    {
        try
        {
            return db.files.getAllFilenames();
        }
        catch (CoreException e)
        {
            getVPG().log.logError(e);
            return Collections.<String>emptyList();
        }
    }

    @Override public Iterable<String> listAllFilenamesWithDependents()
    {
        try
        {
            return db.dependencies.listAllFilenamesWithDependents();
        }
        catch (CoreException e)
        {
            getVPG().log.logError(e);
            return Collections.<String>emptyList();
        }
    }

    @Override public Iterable<String> listAllDependentFilenames()
    {
        try
        {
            return db.dependencies.listAllDependentFilenames();
        }
        catch (CoreException e)
        {
            getVPG().log.logError(e);
            return Collections.<String>emptyList();
        }
    }

    // DEPENDENCIES ////////////////////////////////////////////////////////////

    @Override public void ensure(VPGDependency<A, T, R> dependency)
    {
        try
        {
            db.dependencies.ensure(dependency.getDependentFile(), dependency.getDependsOnFile());
        }
        catch (CoreException e)
        {
            getVPG().log.logError(e);
        }
    }

    @Override public void delete(VPGDependency<A, T, R> dependency)
    {
        try
        {
            String dependentFile = dependency.getDependentFile();
            String dependsOnFile = dependency.getDependsOnFile();
            db.dependencies.delete(dependentFile, dependsOnFile);
            cleanUp(dependentFile);
            cleanUp(dependsOnFile);
        }
        catch (CoreException e)
        {
            getVPG().log.logError(e);
        }
    }

    @Override public Iterable<String> getOutgoingDependenciesFrom(String filename)
    {
        try
        {
            final IntVector records = db.dependencies.findAllOutgoingDependencyRecordsFrom(filename);

            return new Iterable<String>()
            {
                public Iterator<String> iterator()
                {

                    return new Iterator<String>()
                    {
                        private int nextRecord = 0;
                        private int numRecords = records.size();

                        public boolean hasNext()
                        {
                            return nextRecord < numRecords;
                        }

                        public String next()
                        {
                            try
                            {
                                return db.files.getFilename(db.dependencies.getDependsOnFileRecordPtr(records.get(nextRecord++))).getString();
                            }
                            catch (CoreException e)
                            {
                                getVPG().log.logError(e);
                                throw new Error(e);
                            }
                        }

                        public void remove()
                        {
                            throw new UnsupportedOperationException();
                        }
                    };
                }
            };
        }
        catch (CoreException e)
        {
            getVPG().log.logError(e);
            return new LinkedList<String>();
        }
    }

    @Override public Iterable<String> getIncomingDependenciesTo(String filename)
    {
        try
        {
            final IntVector records = db.dependencies.findAllIncomingDependencyRecordsTo(filename);

            return new Iterable<String>()
            {
                public Iterator<String> iterator()
                {
                    return new Iterator<String>()
                    {
                        private int nextRecord = 0;
                        private int numRecords = records.size();

                        public boolean hasNext()
                        {
                            return nextRecord < numRecords;
                        }

                        public String next()
                        {
                            try
                            {
                                return db.files.getFilename(db.dependencies.getDependentFileRecordPtr(records.get(nextRecord++))).getString();
                            }
                            catch (CoreException e)
                            {
                                getVPG().log.logError(e);
                                throw new Error(e);
                            }
                        }

                        public void remove()
                        {
                            throw new UnsupportedOperationException();
                        }

                    };
                }
            };
        }
        catch (CoreException e)
        {
            getVPG().log.logError(e);
            return new LinkedList<String>();
        }
    }

    // EDGES ///////////////////////////////////////////////////////////////////

    @Override public void ensure(VPGEdge<A, T, R> edge)
    {
        try
        {
            R source = edge.getSource();
            R sink = edge.getSink();
            db.edges.ensure(source.getFilename(), source.getOffset(), source.getLength(), sink.getFilename(), sink.getOffset(), sink.getLength(), edge.getType());
        }
        catch (CoreException e)
        {
            getVPG().log.logError(e);
        }
    }

    @Override public void delete(VPGEdge<A, T, R> edge)
    {
        try
        {
            R source = edge.getSource();
            R sink = edge.getSink();
            db.edges.delete(source.getFilename(), source.getOffset(), source.getLength(), sink.getFilename(), sink.getOffset(), sink.getLength(), edge.getType());
            cleanUp(source.getFilename());
            cleanUp(sink.getFilename());
        }
        catch (CoreException e)
        {
            getVPG().log.logError(e);
        }
    }

    @Override public Iterable<? extends VPGEdge<A, T, R>> getAllEdgesFor(String filename)
    {
        try
        {
            final IntVector inRecords = db.edges.findAllIncomingEdgeRecordsTo(filename);
            final IntVector outRecords = db.edges.findAllOutgoingEdgeRecordsFrom(filename);

            return new Iterable<VPGEdge<A, T, R>>()
            {
                public Iterator<VPGEdge<A, T, R>> iterator()
                {
                    return new Iterator<VPGEdge<A, T, R>>()
                    {
                        private int nextRecord = 0;
                        private int numRecords = inRecords.size() + outRecords.size();

                        public boolean hasNext()
                        {
                            return nextRecord < numRecords;
                        }

                        public VPGEdge<A, T, R> next()
                        {
                            try
                            {
                                IntVector records = (nextRecord < inRecords.size() ? inRecords : outRecords);
                                int recordNum     = (nextRecord < inRecords.size() ? nextRecord : nextRecord-inRecords.size());

                                R fromRef = getVPG().createTokenRef(db.files.getFilename(db.edges.getFromFileRecordPtr(records.get(recordNum))).getString(), db.edges.getFromOffset(records.get(recordNum)), db.edges.getFromLength(records.get(recordNum)));
                                R toRef = getVPG().createTokenRef(db.files.getFilename(db.edges.getToFileRecordPtr(records.get(recordNum))).getString(), db.edges.getToOffset(records.get(recordNum)), db.edges.getToLength(records.get(recordNum)));
                                VPGEdge<A, T, R> result = getVPG().createEdge(fromRef, toRef, db.edges.getEdgeType(records.get(recordNum)));
                                nextRecord++;
                                return result;
                            }
                            catch (CoreException e)
                            {
                                getVPG().log.logError(e);
                                throw new Error(e);
                            }
                        }

                        public void remove()
                        {
                            throw new UnsupportedOperationException();
                        }
                    };
                }
            };
        }
        catch (CoreException e)
        {
            getVPG().log.logError(e);
            return new LinkedList<VPGEdge<A, T, R>>();
        }
    }

    @Override public Iterable<VPGEdge<A, T, R>> getOutgoingEdgesFrom(R tokenRef, int edgeType)
    {
        try
        {
            final IntVector records =
                edgeType == ALL_EDGES
                    ? db.edges.findAllOutgoingEdgeRecordsFrom(tokenRef.getFilename(), tokenRef.getOffset(), tokenRef.getLength())
                    : db.edges.findAllOutgoingEdgeRecordsFrom(tokenRef.getFilename(), tokenRef.getOffset(), tokenRef.getLength(), edgeType);

            return new Iterable<VPGEdge<A, T, R>>()
            {
                public Iterator<VPGEdge<A, T, R>> iterator()
                {
                    return new Iterator<VPGEdge<A, T, R>>()
                    {
                        private int nextRecord = 0;
                        private int numRecords = records.size();

                        public boolean hasNext()
                        {
                            return nextRecord < numRecords;
                        }

                        public VPGEdge<A, T, R> next()
                        {
                            try
                            {
                                R fromRef = getVPG().createTokenRef(db.files.getFilename(db.edges.getFromFileRecordPtr(records.get(nextRecord))).getString(), db.edges.getFromOffset(records.get(nextRecord)), db.edges.getFromLength(records.get(nextRecord)));
                                R toRef = getVPG().createTokenRef(db.files.getFilename(db.edges.getToFileRecordPtr(records.get(nextRecord))).getString(), db.edges.getToOffset(records.get(nextRecord)), db.edges.getToLength(records.get(nextRecord)));
                                return getVPG().createEdge(fromRef, toRef, db.edges.getEdgeType(records.get(nextRecord++)));
                            }
                            catch (CoreException e)
                            {
                                getVPG().log.logError(e);
                                throw new Error(e);
                            }
                        }

                        public void remove()
                        {
                            throw new UnsupportedOperationException();
                        }
                    };
                }
            };
        }
        catch (CoreException e)
        {
            getVPG().log.logError(e);
            return new LinkedList<VPGEdge<A, T, R>>();
        }
    }

    @Override public Iterable<VPGEdge<A, T, R>> getIncomingEdgesTo(R tokenRef, int edgeType)
    {
        try
        {
            final IntVector records =
                edgeType == ALL_EDGES
                    ? db.edges.findAllIncomingEdgeRecordsTo(tokenRef.getFilename(), tokenRef.getOffset(), tokenRef.getLength())
                    : db.edges.findAllIncomingEdgeRecordsTo(tokenRef.getFilename(), tokenRef.getOffset(), tokenRef.getLength(), edgeType);

            return new Iterable<VPGEdge<A, T, R>>()
            {
                public Iterator<VPGEdge<A, T, R>> iterator()
                {
                    return new Iterator<VPGEdge<A, T, R>>()
                    {
                        private int nextRecord = 0;
                        private int numRecords = records.size();

                        public boolean hasNext()
                        {
                            return nextRecord < numRecords;
                        }

                        public VPGEdge<A, T, R> next()
                        {
                            try
                            {
                                R fromRef = getVPG().createTokenRef(db.files.getFilename(db.edges.getFromFileRecordPtr(records.get(nextRecord))).getString(), db.edges.getFromOffset(records.get(nextRecord)), db.edges.getFromLength(records.get(nextRecord)));
                                R toRef = getVPG().createTokenRef(db.files.getFilename(db.edges.getToFileRecordPtr(records.get(nextRecord))).getString(), db.edges.getToOffset(records.get(nextRecord)), db.edges.getToLength(records.get(nextRecord)));
                                return getVPG().createEdge(fromRef, toRef, db.edges.getEdgeType(records.get(nextRecord++)));
                            }
                            catch (CoreException e)
                            {
                                getVPG().log.logError(e);
                                throw new Error(e);
                            }
                        }

                        public void remove()
                        {
                            throw new UnsupportedOperationException();
                        }

                    };
                }
            };
        }
        catch (CoreException e)
        {
            getVPG().log.logError(e);
            return new LinkedList<VPGEdge<A, T, R>>();
        }
    }

    // ANNOTATIONS /////////////////////////////////////////////////////////////

    @Override public void setAnnotation(R token, int annotationID, Serializable annotation)
    {
        try
        {
            db.annotations.set(token.getFilename(), token.getOffset(), token.getLength(), annotationID, serialize(annotation));
        }
        catch (Exception e)
        {
            getVPG().log.logError(e);
        }
    }

    @Override public void deleteAnnotation(R token, int annotationID)
    {
        try
        {
            db.annotations.delete(token.getFilename(), token.getOffset(), token.getLength(), annotationID);
            cleanUp(token.getFilename());
        }
        catch (CoreException e)
        {
            getVPG().log.logError(e);
        }
    }

    @Override public Serializable getAnnotation(R token, int annotationID)
    {
        try
        {
            int record = db.annotations.findRecordFor(token.getFilename(), token.getOffset(), token.getLength(), annotationID);
            if (record < 0)
                return null;
            else
                return deserialize(db.annotations.getAnnotation(record));
        }
        catch (Exception e)
        {
            getVPG().log.logError(e);
            return null;
        }
    }

    /**
     * Subclasses should implement this as
     * <pre>
     * ByteArrayOutputStream out = new ByteArrayOutputStream();
     * new ObjectOutputStream(out).writeObject(annotation);
     * return out.toByteArray();
     * </pre>
     * or a more efficient variant.
     * <p>
     * This is delegated to subclasses to complement <code>deserialize</code>.
     */
    protected abstract byte[] serialize(Serializable annotation) throws IOException;

    /**
     * Subclasses should implement this as
     * <pre>
     * return (Serializable)new ObjectInputStream(binaryStream).readObject();
     * </pre>
     * <p>
     * This must be implemented in the subclass, since the serialized (annotation) classes may not be in the
     * classpath of the VPG plug-in, causing <code>ClassNotFoundException</code>s.
     */
    protected abstract Serializable deserialize(InputStream binaryStream) throws IOException, ClassNotFoundException;

    ////////////////////////////////////////////////////////////////////////////

    @Override public void printOn(PrintStream out)
    {
        try
        {
            out.println("MODIFICATION STAMPS:"); //$NON-NLS-1$
            out.println();
            out.println(db.files.toString());

            out.println();
            out.println();
            out.println("DEPENDENCIES:"); //$NON-NLS-1$
            out.println();
            out.println(db.dependencies.toString());

            out.println();
            out.println();
            out.println("EDGES:"); //$NON-NLS-1$
            out.println();
            out.println(db.edges.toString());

            out.println();
            out.println();
            out.println("ANNOTATIONS:"); //$NON-NLS-1$
            out.println();
            try
            {
                out.println(db.annotations.toString());
            }
            catch (Exception e)
            {
                getVPG().log.logError(e);
            }
        }
        catch (Exception e)
        {
            out.print(e.getMessage());
            e.printStackTrace(out);
        }
    }

    @Override public void printStatisticsOn(PrintStream out)
    {
        out.println("(No statistics available)"); //$NON-NLS-1$
    }

    @Override public void resetStatistics()
    {
        // Nothing to do
    }
}
