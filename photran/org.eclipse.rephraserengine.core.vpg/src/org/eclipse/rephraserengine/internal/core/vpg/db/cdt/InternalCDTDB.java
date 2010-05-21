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
package org.eclipse.rephraserengine.internal.core.vpg.db.cdt;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.TreeSet;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.rephraserengine.internal.db.org.eclipse.cdt.internal.core.pdom.db.BTree;
import org.eclipse.rephraserengine.internal.db.org.eclipse.cdt.internal.core.pdom.db.ChunkCache;
import org.eclipse.rephraserengine.internal.db.org.eclipse.cdt.internal.core.pdom.db.Database;
import org.eclipse.rephraserengine.internal.db.org.eclipse.cdt.internal.core.pdom.db.IBTreeComparator;
import org.eclipse.rephraserengine.internal.db.org.eclipse.cdt.internal.core.pdom.db.IBTreeVisitor;
import org.eclipse.rephraserengine.internal.db.org.eclipse.cdt.internal.core.pdom.db.IString;

/**
 * Encapsulation of a CDT BTree-based VPG database.
 * <p>
 * This is essentially a database with four tables.
 * <ul>
 * <li> files
 * <li> dependencies
 * <li> edges
 * <li> annotations
 * </ul>
 * This class contains a public field with each of these names
 * which provides access to the corresponding table.
 * 
 * @author Jeff Overbey
 * 
 * @since 1.0
 */
/*
 * In reality, there are no tables: A Database is simply a big
 * block of "memory" on disk, where blocks of any size can be
 * malloc()'d and free()'d.
 *
 * The first few blocks in the database hold the root nodes
 * of several BTrees.  The nodes of these BTrees point to
 * the records of each table.
 *
 * The JUnit tests in org.eclipse.cdt.internal.pdom.tests.DBTest
 * were particularly helpful in figuring out the CDT Database API.
 *
 * @author Jeff Overbey
 */
public class InternalCDTDB
{
    static final int FILENAME_BTREE_ROOT           = Database.DATA_AREA + 0;
    static final int FORWARD_DEPENDENCY_BTREE_ROOT = Database.DATA_AREA + 4;
    static final int REVERSE_DEPENDENCY_BTREE_ROOT = Database.DATA_AREA + 8;
    static final int FORWARD_EDGE_BTREE_ROOT       = Database.DATA_AREA + 12;
    static final int REVERSE_EDGE_BTREE_ROOT       = Database.DATA_AREA + 16;
    static final int ANNOTATION_BTREE_ROOT         = Database.DATA_AREA + 20;

    protected File file;
    protected Database db;

    public final Files files;
    public final Dependencies dependencies;
    public final Edges edges;
    public final Annotations annotations;

    public InternalCDTDB(File file) throws CoreException
    {
        this.file = file;

        db = new Database(file, new ChunkCache(), 0, false);
        db.setExclusiveLock();

        files = new Files();
        dependencies = new Dependencies();
        edges = new Edges();
        annotations = new Annotations();
    }

    public File getFile()
    {
        return file;
    }

    public void flush() throws CoreException
    {
        db.flush();
    }

    public void close() throws CoreException
    {
        db.close();
        db = null;
    }

    public void clear() throws CoreException
    {
        db.clear(0);
    }

    /**
     * Base class for BTree visitors that are searching for a single record
     * <p>
     * Based on org.eclipse.cdt.internal.pdom.tests.DBTest.FindVisitor
     *
     * @author Jeff Overbey
     */
    protected abstract class FindVisitor implements IBTreeVisitor
    {
        private int record = -1;

        public boolean visit(int record)
        {
            this.record = record;
            return false;
        }

        public boolean foundRecord()
        {
            return record >= 0;
        }

        public int getRecord()
        {
            return record;
        }
    }

    /**
     * Performs a lexicographic comparison of two tuples, i.e.,
     * <pre>
     * (0,0) < (0,1) < (0,2) < (1,0) < (1,1) < (1,2)
     * <pre>
     * If tuple1 and tuple2 are not of the same length,
     * only the first <i>n</i> components are compared,
     * where <i>n</i> is the length of the smaller tuple.
     * This is useful, for example, when comparing two edges:
     * the last component of the tuple contains the edge type,
     * so if that component is omitted, edges with any type
     * will be matched.
     * @return an integer which is
     * <ol>
     * <li> less than zero iff tuple1 &lt; tuple2,
     * <li> greater than zero iff tuple1 &gt; tuple2, or
     * <li> equal to zero iff tuple1 = tuple2.
     * </ol>
     */
    private static int lexicographicallyCompare(int[] tuple1, int[] tuple2)
    {
        int length = Math.min(tuple1.length, tuple2.length);

        for (int i = 0; i < length; i++)
        {
            if (tuple1[i] < tuple2[i])
                return -1;
            else if (tuple1[i] > tuple2[i])
                return 1;
        }

        return 0;
    }

    /**
     * The files table contains two fields:
     * <ol>
     * <li> Filename (String)
     * <li> Modification stamp (long)
     * </ol>
     *
     * @author Jeff Overbey
     */
    public class Files
    {
        // RECORD STRUCTURE

        protected static final int FILENAME_FIELD = 0;
        protected static final int MODIFICATION_STAMP_FIELD = 4;

        public static final int RECORD_SIZE = 8;

        public IString getFilename(int record) throws CoreException
        {
            return db.getString(db.getInt(record + FILENAME_FIELD));
        }

        public void setFilename(int record, String filename) throws CoreException
        {
            db.putInt(record + FILENAME_FIELD, db.newString(filename).getRecord());
        }

        public long getModificationStamp(int record) throws CoreException
        {
            return db.getLong(record + MODIFICATION_STAMP_FIELD);
        }

        public void setModificationStamp(int record, long timestamp) throws CoreException
        {
            db.putLong(record + MODIFICATION_STAMP_FIELD, timestamp);
        }

        protected int createNewRecord(String filename) throws CoreException
        {
            int record = db.malloc(RECORD_SIZE);
            setFilename(record, filename);
            setModificationStamp(record, Integer.MIN_VALUE);
            filenameBTree.insert(record);
            return record;
        }

        // INDEX

        protected BTree filenameBTree = new BTree(db, FILENAME_BTREE_ROOT, new IBTreeComparator()
        {
            public int compare(int record1, int record2) throws CoreException
            {
                return getFilename(record1).compare(getFilename(record2), true);
            }
        });

        public int findRecordFor(final String filename) throws CoreException
        {
            FindVisitor visitor = new FindVisitor()
            {
                public int compare(int record) throws CoreException
                {
                    return getFilename(record).compare(filename, true);
                }
            };
            filenameBTree.accept(visitor);
            return visitor.getRecord();
        }

        public IntVector findAllFileRecords() throws CoreException
        {
            final IntVector records = new IntVector();

            filenameBTree.accept(new IBTreeVisitor()
            {
                public int compare(int record) throws CoreException
                {
                    return 0;
                }

                public boolean visit(int record) throws CoreException
                {
                    records.add(record);
                    return true;
                }
            });

            return records;
        }

        public int ensure(String filename) throws CoreException
        {
            int record = findRecordFor(filename);
            return record < 0 ? createNewRecord(filename) : record;
        }

        public void delete(String filename) throws CoreException
        {
            int record = findRecordFor(filename);
            if (record < 0) return;

            filenameBTree.delete(record);
            db.free(record);
        }

        public Iterable<String> getAllFilenames() throws CoreException
        {
            final TreeSet<String> result = new TreeSet<String>();

            filenameBTree.accept(new IBTreeVisitor()
            {
                public int compare(int record) throws CoreException
                {
                    return 0;
                }

                public boolean visit(int record) throws CoreException
                {
                    result.add(getFilename(record).getString());
                    return true;
                }

            });

            return result;
        }

        @Override public String toString()
        {
            final StringBuilder sb = new StringBuilder();

            try
            {
                filenameBTree.accept(new IBTreeVisitor()
                {
                    public int compare(int record) throws CoreException
                    {
                        return 0;
                    }

                    public boolean visit(int record) throws CoreException
                    {
                        sb.append(getFilename(record).getChars());
                        sb.append(" (");
                        sb.append(record);
                        sb.append(")\n");
                        return true;
                    }

                });
            }
            catch (CoreException e)
            {
                sb.append(e);
            }

            return sb.toString();
        }
    }

    /**
     * The dependencies table contains two fields:
     * <ol>
     * <li> Dependent file (pointer to a record in the Files table)
     * <li> Depends on file (pointer to a record in the Files table)
     * </ol>
     *
     * @author Jeff Overbey
     */
    public class Dependencies
    {
        // RECORD STRUCTURE

        protected static final int DEPENDENT_FILE_FIELD = 0;
        protected static final int DEPENDS_ON_FILE_FIELD = 4;

        public static final int RECORD_SIZE = 8;

        public int getDependentFileRecordPtr(int record) throws CoreException
        {
            return db.getInt(record + DEPENDENT_FILE_FIELD);
        }

        public void setDependentFileRecordPtr(int dependencyRecord, int fileRecord) throws CoreException
        {
            db.putInt(dependencyRecord + DEPENDENT_FILE_FIELD, fileRecord);
        }

        public int getDependsOnFileRecordPtr(int record) throws CoreException
        {
            return db.getInt(record + DEPENDS_ON_FILE_FIELD);
        }

        public void setDependsOnFileRecordPtr(int dependencyRecord, int fileRecord) throws CoreException
        {
            db.putInt(dependencyRecord + DEPENDS_ON_FILE_FIELD, fileRecord);
        }

        protected int[] getRecordAsTuple(int record) throws CoreException
        {
            return new int[]
            {
                getDependentFileRecordPtr(record),
                getDependsOnFileRecordPtr(record),
            };
        }

        protected int[] getReverseRecordAsTuple(int record) throws CoreException
        {
            return new int[]
            {
                getDependsOnFileRecordPtr(record),
                getDependentFileRecordPtr(record),
            };
        }

        protected int createNewRecord(int dependentFile, int dependsOnFile) throws CoreException
        {
            int record = db.malloc(RECORD_SIZE);
            setDependentFileRecordPtr(record, dependentFile);
            setDependsOnFileRecordPtr(record, dependsOnFile);
            forwardDependencyBTree.insert(record);
            reverseDependencyBTree.insert(record);
            return record;
        }

        // INDICES

        protected BTree forwardDependencyBTree = new BTree(db, FORWARD_DEPENDENCY_BTREE_ROOT, new IBTreeComparator()
        {
            public int compare(int record1, int record2) throws CoreException
            {
                return lexicographicallyCompare(getRecordAsTuple(record1), getRecordAsTuple(record2));
            }
        });

        protected BTree reverseDependencyBTree = new BTree(db, REVERSE_DEPENDENCY_BTREE_ROOT, new IBTreeComparator()
        {
            public int compare(int record1, int record2) throws CoreException
            {
                return lexicographicallyCompare(getReverseRecordAsTuple(record1), getReverseRecordAsTuple(record2));
            }
        });

        private int findRecordFor(String filename1, String filename2) throws CoreException
        {
            int fromFileRecordPtr = files.findRecordFor(filename1);
            if (fromFileRecordPtr < 0) return -1;

            int toFileRecordPtr = files.findRecordFor(filename2);
            if (toFileRecordPtr < 0) return -1;

            return findRecordFor(fromFileRecordPtr, toFileRecordPtr);
        }

        private int findRecordFor(final int fromFileRecordPtr, final int toFileRecordPtr) throws CoreException
        {
            final int[] tuple2 = new int[] { fromFileRecordPtr, toFileRecordPtr };

            //System.out.println("Searching for (" + fromFileRecordPtr + "," + toFileRecordPtr + ")");
            FindVisitor visitor = new FindVisitor()
            {
                public int compare(int record1) throws CoreException
                {
                    int[] tuple1 = getRecordAsTuple(record1);

                    return lexicographicallyCompare(tuple1, tuple2);
                }
            };
            forwardDependencyBTree.accept(visitor);
            //System.out.println("Found record " + visitor.getRecord());
            return visitor.getRecord();
        }

        public int ensure(String filename1, String filename2) throws CoreException
        {
            int fromFileRecordPtr = files.ensure(filename1);
            int toFileRecordPtr = files.ensure(filename2);

            int result = findRecordFor(fromFileRecordPtr, toFileRecordPtr);

            return result < 0 ? createNewRecord(fromFileRecordPtr, toFileRecordPtr) : result;
        }

        public void delete(String filename1, String filename2) throws CoreException
        {
            int record = findRecordFor(filename1, filename2);
            if (record < 0) return;

            forwardDependencyBTree.delete(record);
            reverseDependencyBTree.delete(record);
            db.free(record);
        }

        public void deleteAllOutgoingDependenciesFrom(String fromFilename) throws CoreException
        {
            IntVector records = findAllOutgoingDependencyRecordsFrom(fromFilename);
            for (int i = 0, size = records.size(); i < size; i++)
            {
                int record = records.get(i);
                forwardDependencyBTree.delete(record);
                reverseDependencyBTree.delete(record);
                db.free(record);
            }
        }

        public void deleteAllIncomingDependenciesTo(String toFilename) throws CoreException
        {
            IntVector records = findAllIncomingDependencyRecordsTo(toFilename);
            for (int i = 0, size = records.size(); i < size; i++)
            {
                int record = records.get(i);
                forwardDependencyBTree.delete(record);
                reverseDependencyBTree.delete(record);
                db.free(record);
            }
        }

        public boolean hasOutgoingDependencyRecords(String fromFilename) throws CoreException
        {
            int fromFileRecordPtr = files.findRecordFor(fromFilename);
            if (fromFileRecordPtr == -1) return false;

            final int[] tuple2 = { fromFileRecordPtr };

            FindVisitor v = new FindVisitor()
            {
                public int compare(int record1) throws CoreException
                {
                    return lexicographicallyCompare(getRecordAsTuple(record1), tuple2);
                }
            };
            forwardDependencyBTree.accept(v);
            return v.foundRecord();
        }

        public IntVector findAllOutgoingDependencyRecordsFrom(String fromFilename) throws CoreException
        {
            final IntVector records = new IntVector();

            int fromFileRecordPtr = files.findRecordFor(fromFilename);
            if (fromFileRecordPtr == -1) return records;

            final int[] tuple2 = { fromFileRecordPtr };

            forwardDependencyBTree.accept(new IBTreeVisitor()
            {
                public int compare(int record1) throws CoreException
                {
                    return lexicographicallyCompare(getRecordAsTuple(record1), tuple2);
                }

                public boolean visit(int record) throws CoreException
                {
                    records.add(record);
                    return true;
                }
            });

            return records;
        }

        public boolean hasIncomingDependencyRecords(String toFilename) throws CoreException
        {
            int toFileRecordPtr = files.findRecordFor(toFilename);
            if (toFileRecordPtr == -1) return false;

            final int[] tuple2 = { toFileRecordPtr };

            FindVisitor v = new FindVisitor()
            {
                public int compare(int record1) throws CoreException
                {
                    return lexicographicallyCompare(getReverseRecordAsTuple(record1), tuple2);
                }
            };
            reverseDependencyBTree.accept(v);
            return v.foundRecord();
        }

        public IntVector findAllIncomingDependencyRecordsTo(String toFilename) throws CoreException
        {
            final IntVector records = new IntVector();

            int toFileRecordPtr = files.findRecordFor(toFilename);
            if (toFileRecordPtr == -1) return records;

            final int[] tuple2 = { toFileRecordPtr };

            reverseDependencyBTree.accept(new IBTreeVisitor()
            {
                public int compare(int record1) throws CoreException
                {
                    return lexicographicallyCompare(getReverseRecordAsTuple(record1), tuple2);
                }

                public boolean visit(int record) throws CoreException
                {
                    records.add(record);
                    return true;
                }
            });

            return records;
        }

        public Iterable<String> listAllFilenamesWithDependents() throws CoreException
        {
            final TreeSet<String> result = new TreeSet<String>();

            reverseDependencyBTree.accept(new IBTreeVisitor()
            {
                public int compare(int record) throws CoreException
                {
                    return 0;
                }

                public boolean visit(int record) throws CoreException
                {
                    result.add(files.getFilename(getDependsOnFileRecordPtr(record)).getString());
                    return true;
                }

            });

            return result;
        }

        public Iterable<String> listAllDependentFilenames() throws CoreException
        {
            final TreeSet<String> result = new TreeSet<String>();

            forwardDependencyBTree.accept(new IBTreeVisitor()
            {
                public int compare(int record) throws CoreException
                {
                    return 0;
                }

                public boolean visit(int record) throws CoreException
                {
                    result.add(files.getFilename(getDependentFileRecordPtr(record)).getString());
                    return true;
                }

            });

            return result;
        }

        @Override public String toString()
        {
            final StringBuilder sb = new StringBuilder();

            try
            {
                forwardDependencyBTree.accept(new IBTreeVisitor()
                {
                    public int compare(int record) throws CoreException
                    {
                        return 0;
                    }

                    public boolean visit(int record) throws CoreException
                    {
                        sb.append(files.getFilename(getDependentFileRecordPtr(record)).getChars());
                        sb.append(" depends on ");
                        sb.append(files.getFilename(getDependsOnFileRecordPtr(record)).getChars());
                        sb.append(" (");
                        sb.append(record);
                        sb.append(")\n");
                        return true;
                    }

                });
            }
            catch (CoreException e)
            {
                sb.append(e);
            }

            return sb.toString();
        }
    }

    /**
     * The edges table contains the following fields:
     * <ol>
     * <li> From file (pointer to a record in the Files table)
     * <li> From offset (int)
     * <li> From length (int)
     * <li> To file (pointer to a record in the Files table)
     * <li> To offset (int)
     * <li> To length (int)
     * <li> Edge type (int)
     * </ol>
     *
     * @author Jeff Overbey
     */
    public class Edges
    {
        // RECORD STRUCTURE

        protected static final int FROM_FILE_FIELD = 0;
        protected static final int FROM_OFFSET_FIELD = 4;
        protected static final int FROM_LENGTH_FIELD = 8;
        protected static final int TO_FILE_FIELD = 12;
        protected static final int TO_OFFSET_FIELD = 16;
        protected static final int TO_LENGTH_FIELD = 20;
        protected static final int EDGE_TYPE_FIELD = 24;

        public static final int RECORD_SIZE = 28;

        public int getFromFileRecordPtr(int record) throws CoreException
        {
            return db.getInt(record + FROM_FILE_FIELD);
        }

        public void setFromFileRecordPtr(int dependencyRecord, int fileRecord) throws CoreException
        {
            db.putInt(dependencyRecord + FROM_FILE_FIELD, fileRecord);
        }

        public int getFromOffset(int record) throws CoreException
        {
            return db.getInt(record + FROM_OFFSET_FIELD);
        }

        public void setFromOffset(int dependencyRecord, int value) throws CoreException
        {
            db.putInt(dependencyRecord + FROM_OFFSET_FIELD, value);
        }

        public int getFromLength(int record) throws CoreException
        {
            return db.getInt(record + FROM_LENGTH_FIELD);
        }

        public void setFromLength(int dependencyRecord, int value) throws CoreException
        {
            db.putInt(dependencyRecord + FROM_LENGTH_FIELD, value);
        }

        public int getToFileRecordPtr(int record) throws CoreException
        {
            return db.getInt(record + TO_FILE_FIELD);
        }

        public void setToFileRecordPtr(int dependencyRecord, int fileRecord) throws CoreException
        {
            db.putInt(dependencyRecord + TO_FILE_FIELD, fileRecord);
        }

        public int getToOffset(int record) throws CoreException
        {
            return db.getInt(record + TO_OFFSET_FIELD);
        }

        public void setToOffset(int record, int value) throws CoreException
        {
            db.putInt(record + TO_OFFSET_FIELD, value);
        }

        public int getToLength(int record) throws CoreException
        {
            return db.getInt(record + TO_LENGTH_FIELD);
        }

        public void setToLength(int record, int value) throws CoreException
        {
            db.putInt(record + TO_LENGTH_FIELD, value);
        }

        public int getEdgeType(int record) throws CoreException
        {
            return db.getInt(record + EDGE_TYPE_FIELD);
        }

        public void setEdgeType(int record, int value) throws CoreException
        {
            db.putInt(record + EDGE_TYPE_FIELD, value);
        }

        protected int[] getRecordAsTuple(int record) throws CoreException
        {
            return new int[]
            {
                getFromFileRecordPtr(record),
                getFromOffset(record),
                getFromLength(record),
                getEdgeType(record),
                getToFileRecordPtr(record),
                getToOffset(record),
                getToLength(record),
            };
        }

        protected int[] getReverseRecordAsTuple(int record) throws CoreException
        {
            return new int[]
            {
                getToFileRecordPtr(record),
                getToOffset(record),
                getToLength(record),
                getEdgeType(record),
                getFromFileRecordPtr(record),
                getFromOffset(record),
                getFromLength(record),
            };
        }

        protected int createNewRecord(int fromFile, int fromOffset, int fromLength, int toFile, int toOffset, int toLength, int edgeType) throws CoreException
        {
            int record = db.malloc(RECORD_SIZE);
            setFromFileRecordPtr(record, fromFile);
            setFromOffset(record, fromOffset);
            setFromLength(record, fromLength);
            setToFileRecordPtr(record, toFile);
            setToOffset(record, toOffset);
            setToLength(record, toLength);
            setEdgeType(record, edgeType);
            forwardEdgeBTree.insert(record);
            reverseEdgeBTree.insert(record);
            return record;
        }

        // INDICES

        protected BTree forwardEdgeBTree = new BTree(db, FORWARD_EDGE_BTREE_ROOT, new IBTreeComparator()
        {
            public int compare(int record1, int record2) throws CoreException
            {
                return lexicographicallyCompare(getRecordAsTuple(record1), getRecordAsTuple(record2));
            }
        });

        protected BTree reverseEdgeBTree = new BTree(db, REVERSE_EDGE_BTREE_ROOT, new IBTreeComparator()
        {
            public int compare(int record1, int record2) throws CoreException
            {
                return lexicographicallyCompare(getReverseRecordAsTuple(record1), getReverseRecordAsTuple(record2));
            }
        });

        private int findRecordFor(String fromFilename, int fromOffset, int fromLength, String toFilename, int toOffset, int toLength, int edgeType) throws CoreException
        {
            int fromFileRecordPtr = files.findRecordFor(fromFilename);
            if (fromFileRecordPtr < 0) return -1;

            int toFileRecordPtr = files.findRecordFor(toFilename);
            if (toFileRecordPtr < 0) return -1;

            return findRecordFor(fromFileRecordPtr, fromOffset, fromLength, toFileRecordPtr, toOffset, toLength, edgeType);
        }

        private int findRecordFor(final int fromFileRecordPtr, int fromOffset, int fromLength, final int toFileRecordPtr, int toOffset, int toLength, int edgeType) throws CoreException
        {
            final int[] tuple2 = new int[] { fromFileRecordPtr, fromOffset, fromLength, edgeType, toFileRecordPtr, toOffset, toLength };

            FindVisitor visitor = new FindVisitor()
            {
                public int compare(int record1) throws CoreException
                {
                    return lexicographicallyCompare(getRecordAsTuple(record1), tuple2);
                }
            };
            forwardEdgeBTree.accept(visitor);
            return visitor.getRecord();
        }

        public int ensure(String fromFilename, int fromOffset, int fromLength, String toFilename, int toOffset, int toLength, int edgeType) throws CoreException
        {
            int fromFileRecordPtr = files.ensure(fromFilename);
            int toFileRecordPtr = files.ensure(toFilename);

            int result = findRecordFor(fromFileRecordPtr, fromOffset, fromLength, toFileRecordPtr, toOffset, toLength, edgeType);

            return result < 0 ? createNewRecord(fromFileRecordPtr, fromOffset, fromLength, toFileRecordPtr, toOffset, toLength, edgeType) : result;
        }

        public void delete(String fromFilename, int fromOffset, int fromLength, String toFilename, int toOffset, int toLength, int edgeType) throws CoreException
        {
            int record = findRecordFor(fromFilename, fromOffset, fromLength, toFilename, toOffset, toLength, edgeType);
            if (record < 0) return;

            forwardEdgeBTree.delete(record);
            reverseEdgeBTree.delete(record);
            db.free(record);
        }

        public void deleteAllOutgoingEdgesFrom(String fromFilename) throws CoreException
        {
            IntVector records = findAllOutgoingEdgeRecordsFrom(fromFilename);
            for (int i = 0, size = records.size(); i < size; i++)
            {
                int record = records.get(i);
                forwardEdgeBTree.delete(record);
                reverseEdgeBTree.delete(record);
                db.free(record);
            }
        }

        public void deleteAllIncomingEdgesTo(String toFilename) throws CoreException
        {
            IntVector records = findAllIncomingEdgeRecordsTo(toFilename);
            for (int i = 0, size = records.size(); i < size; i++)
            {
                int record = records.get(i);
                forwardEdgeBTree.delete(record);
                reverseEdgeBTree.delete(record);
                db.free(record);
            }
        }

        public IntVector findAllOutgoingEdgeRecordsFrom(String fromFilename) throws CoreException
        {
            return findAllOutgoingEdgeRecords(new int[] { files.findRecordFor(fromFilename) });
        }

        public IntVector findAllOutgoingEdgeRecordsFrom(String fromFilename, int offset, int length) throws CoreException
        {
            return findAllOutgoingEdgeRecords(new int[] { files.findRecordFor(fromFilename), offset, length });
        }

        public IntVector findAllOutgoingEdgeRecordsFrom(String fromFilename, int offset, int length, int edgeType) throws CoreException
        {
            return findAllOutgoingEdgeRecords(new int[] { files.findRecordFor(fromFilename), offset, length, edgeType });
        }

        protected IntVector findAllOutgoingEdgeRecords(final int[] tuple2) throws CoreException
        {
            if (tuple2[0] < 0) return new IntVector(); // File not found

            final IntVector records = new IntVector();

            forwardEdgeBTree.accept(new IBTreeVisitor()
            {
                public int compare(int record1) throws CoreException
                {
                    return lexicographicallyCompare(getRecordAsTuple(record1), tuple2);
                }

                public boolean visit(int record) throws CoreException
                {
                    records.add(record);
                    return true;
                }
            });

            return records;
        }

        public boolean hasOutgoingEdges(String filename) throws CoreException
        {
            final int[] tuple2 = new int[] { files.findRecordFor(filename) };
            if (tuple2[0] < 0) return false; // File not found

            FindVisitor v = new FindVisitor()
            {
                public int compare(int record1) throws CoreException
                {
                    return lexicographicallyCompare(getRecordAsTuple(record1), tuple2);
                }
            };
            forwardEdgeBTree.accept(v);
            return v.foundRecord();
        }

        public IntVector findAllIncomingEdgeRecordsTo(String fromFilename) throws CoreException
        {
            return findAllIncomingEdgeRecords(new int[] { files.findRecordFor(fromFilename) });
        }

        public IntVector findAllIncomingEdgeRecordsTo(String fromFilename, int offset, int length) throws CoreException
        {
            return findAllIncomingEdgeRecords(new int[] { files.findRecordFor(fromFilename), offset, length });
        }

        public IntVector findAllIncomingEdgeRecordsTo(String fromFilename, int offset, int length, int edgeType) throws CoreException
        {
            return findAllIncomingEdgeRecords(new int[] { files.findRecordFor(fromFilename), offset, length, edgeType });
        }

        protected IntVector findAllIncomingEdgeRecords(final int[] tuple2) throws CoreException
        {
            if (tuple2[0] < 0) return new IntVector(); // File not found

            final IntVector records = new IntVector();

            reverseEdgeBTree.accept(new IBTreeVisitor()
            {
                public int compare(int record1) throws CoreException
                {
                    return lexicographicallyCompare(getReverseRecordAsTuple(record1), tuple2);
                }

                public boolean visit(int record) throws CoreException
                {
                    records.add(record);
                    return true;
                }
            });

            return records;
        }

        public boolean hasIncomingEdges(String filename) throws CoreException
        {
            final int[] tuple2 = new int[] { files.findRecordFor(filename) };
            if (tuple2[0] < 0) return false; // File not found

            FindVisitor v = new FindVisitor()
            {
                public int compare(int record1) throws CoreException
                {
                    return lexicographicallyCompare(getReverseRecordAsTuple(record1), tuple2);
                }
            };
            reverseEdgeBTree.accept(v);
            return v.foundRecord();
        }

        @Override public String toString()
        {
            final StringBuilder sb = new StringBuilder();

            try
            {
                forwardEdgeBTree.accept(new IBTreeVisitor()
                {
                    public int compare(int record) throws CoreException
                    {
                        return 0;
                    }

                    public boolean visit(int record) throws CoreException
                    {
                        sb.append("Edge of type ");
                        sb.append(getEdgeType(record));
                        sb.append(" from ");
                        sb.append(files.getFilename(getFromFileRecordPtr(record)).getChars());
                        sb.append(", offset ");
                        sb.append(getFromOffset(record));
                        sb.append(", length ");
                        sb.append(getFromLength(record));
                        sb.append(" to ");
                        sb.append(files.getFilename(getToFileRecordPtr(record)).getChars());
                        sb.append(", offset ");
                        sb.append(getToOffset(record));
                        sb.append(", length ");
                        sb.append(getToLength(record));
                        sb.append(" (");
                        sb.append(record);
                        sb.append(")\n");
                        return true;
                    }

                });
            }
            catch (CoreException e)
            {
                sb.append(e);
            }

            return sb.toString();
        }
    }

    /**
     * The annotations table contains the following fields:
     * <ol>
     * <li> File (pointer to a record in the Files table)
     * <li> Offset (int)
     * <li> Length (int)
     * <li> Annotation type (int)
     * <li> Annotation (serialized object)
     * </ol>
     *
     * @author Jeff Overbey
     */
    public class Annotations
    {
        // RECORD STRUCTURE

        protected static final int FILE_FIELD = 0;
        protected static final int OFFSET_FIELD = 4;
        protected static final int LENGTH_FIELD = 8;
        protected static final int ANNOTATION_TYPE_FIELD = 12;
        protected static final int ANNOTATION_PTR_FIELD = 16;
        protected static final int ANNOTATION_LENGTH_FIELD = 20;

        public static final int RECORD_SIZE = 24;

        public int getFileRecordPtr(int record) throws CoreException
        {
            return db.getInt(record + FILE_FIELD);
        }

        public void setFileRecordPtr(int dependencyRecord, int fileRecord) throws CoreException
        {
            db.putInt(dependencyRecord + FILE_FIELD, fileRecord);
        }

        public int getOffset(int record) throws CoreException
        {
            return db.getInt(record + OFFSET_FIELD);
        }

        public void setOffset(int dependencyRecord, int value) throws CoreException
        {
            db.putInt(dependencyRecord + OFFSET_FIELD, value);
        }

        public int getLength(int record) throws CoreException
        {
            return db.getInt(record + LENGTH_FIELD);
        }

        public void setLength(int dependencyRecord, int value) throws CoreException
        {
            db.putInt(dependencyRecord + LENGTH_FIELD, value);
        }

        public int getAnnotationType(int record) throws CoreException
        {
            return db.getInt(record + ANNOTATION_TYPE_FIELD);
        }

        public void setAnnotationType(int record, int value) throws CoreException
        {
            db.putInt(record + ANNOTATION_TYPE_FIELD, value);
        }

        public int getAnnotationPtr(int record) throws CoreException
        {
            return db.getInt(record + ANNOTATION_PTR_FIELD);
        }

        public int getAnnotationLength(int record) throws CoreException
        {
            return db.getInt(record + ANNOTATION_LENGTH_FIELD);
        }

        public InputStream getAnnotation(final int record) throws CoreException
        {
            return new InputStream()
            {
                private int annotationRecord = getAnnotationPtr(record);
                private int length = getAnnotationLength(record);

                private int i = 0;

                @Override
                public int read() throws IOException
                {
                    try
                    {
                        if (i < length)
                            return db.getByte(annotationRecord + (i++)) & 0xFF;
                        else
                            return -1;
                    }
                    catch (CoreException e)
                    {
                        throw new IOException("Internal error reading serialized object from database: " + e.getMessage());
                    }
                }
            };
        }

        public void setAnnotation(int record, byte[] annotation) throws CoreException
        {
            int annotationPtr = db.malloc(annotation.length);

            for (int i = 0; i < annotation.length; i++)
                db.putByte(annotationPtr + i, annotation[i]);

            db.putInt(record + ANNOTATION_PTR_FIELD, annotationPtr);
            db.putInt(record + ANNOTATION_LENGTH_FIELD, annotation.length);
        }

        protected int[] getRecordAsTuple(int record) throws CoreException
        {
            return new int[]
            {
                getFileRecordPtr(record),
                getOffset(record),
                getLength(record),
                getAnnotationType(record),
            };
        }

        protected int createNewRecord(int file, int offset, int length, int annotationType, byte[] annotation) throws CoreException
        {
            int record = db.malloc(RECORD_SIZE);
            setFileRecordPtr(record, file);
            setOffset(record, offset);
            setLength(record, length);
            setAnnotationType(record, annotationType);
            setAnnotation(record, annotation);
            annotationBTree.insert(record);
            return record;
        }

        // INDICES

        protected BTree annotationBTree = new BTree(db, ANNOTATION_BTREE_ROOT, new IBTreeComparator()
        {
            public int compare(int record1, int record2) throws CoreException
            {
                return lexicographicallyCompare(getRecordAsTuple(record1), getRecordAsTuple(record2));
            }
        });

        public int findRecordFor(String filename, int offset, int length, int annotationType) throws CoreException
        {
            int fromFileRecordPtr = files.findRecordFor(filename);
            if (fromFileRecordPtr < 0) return -1;

            return findRecordFor(fromFileRecordPtr, offset, length, annotationType);
        }

        private int findRecordFor(final int fileRecordPtr, int offset, int length, int annotationType) throws CoreException
        {
            final int[] tuple2 = new int[] { fileRecordPtr, offset, length, annotationType };

            FindVisitor visitor = new FindVisitor()
            {
                public int compare(int record1) throws CoreException
                {
                    return lexicographicallyCompare(getRecordAsTuple(record1), tuple2);
                }
            };
            annotationBTree.accept(visitor);
            return visitor.getRecord();
        }

        public int set(String fromFilename, int fromOffset, int fromLength, int edgeType, byte[] annotation) throws CoreException
        {
            delete(fromFilename, fromOffset, fromLength, edgeType);
            return createNewRecord(files.ensure(fromFilename), fromOffset, fromLength, edgeType, annotation);
        }

        public void delete(String fromFilename, int fromOffset, int fromLength, int edgeType) throws CoreException
        {
            int record = findRecordFor(fromFilename, fromOffset, fromLength, edgeType);
            if (record < 0) return;

            db.free(getAnnotationPtr(record));
            annotationBTree.delete(record);
            db.free(record);
        }

        public void deleteAllAnnotationsFor(String fromFilename) throws CoreException
        {
            IntVector records = findAllAnnotationRecordsFor(fromFilename);
            for (int i = 0, size = records.size(); i < size; i++)
            {
                int record = records.get(i);
                db.free(getAnnotationPtr(record));
                annotationBTree.delete(record);
                db.free(record);
            }
        }

        public IntVector findAllAnnotationRecordsFor(String fromFilename) throws CoreException
        {
            return findAllAnnotationRecords(new int[] { files.findRecordFor(fromFilename) });
        }

        public IntVector findAllAnnotationRecordsFor(String fromFilename, int offset, int length) throws CoreException
        {
            return findAllAnnotationRecords(new int[] { files.findRecordFor(fromFilename), offset, length });
        }

        protected IntVector findAllAnnotationRecords(final int[] tuple2) throws CoreException
        {
            if (tuple2[0] < 0) return new IntVector(); // File not found

            final IntVector records = new IntVector();

            annotationBTree.accept(new IBTreeVisitor()
            {
                public int compare(int record1) throws CoreException
                {
                    return lexicographicallyCompare(getRecordAsTuple(record1), tuple2);
                }

                public boolean visit(int record) throws CoreException
                {
                    records.add(record);
                    return true;
                }
            });

            return records;
        }

        public boolean hasAnnotations(String filename) throws CoreException
        {
            final int[] tuple2 = new int[] { files.findRecordFor(filename) };
            if (tuple2[0] < 0) return false; // File not found

            FindVisitor v = new FindVisitor()
            {
                public int compare(int record1) throws CoreException
                {
                    return lexicographicallyCompare(getRecordAsTuple(record1), tuple2);
                }
            };
            annotationBTree.accept(v);
            return v.foundRecord();
        }

        @Override public String toString()
        {
            final StringBuilder sb = new StringBuilder();

            try
            {
                annotationBTree.accept(new IBTreeVisitor()
                {
                    public int compare(int record) throws CoreException
                    {
                        return 0;
                    }

                    public boolean visit(int record) throws CoreException
                    {
                        sb.append("Annotation of type ");
                        sb.append(getAnnotationType(record));
                        sb.append(" on ");
                        sb.append(files.getFilename(getFileRecordPtr(record)).getChars());
                        sb.append(", offset ");
                        sb.append(getOffset(record));
                        sb.append(", length ");
                        sb.append(getLength(record));
                        sb.append(" (");
                        sb.append(record);
                        sb.append(")\n");
                        return true;
                    }

                });
            }
            catch (CoreException e)
            {
                sb.append(e);
            }

            return sb.toString();
        }
    }

    ///////////////////////////////////////////////////////////////////////////

    /**
     * An automatically-expanding vector of integers.
     *
     * Integers are stored as primitives rather than <code>Integer</code> in order to increase efficiency.
     */
    public static class IntVector
    {
        private int[] array;
        private int size;

        public IntVector()
        {
            this(64); // Heuristic
        }

        public IntVector(int initialCapacity)
        {
            if (initialCapacity < 0) throw new IllegalArgumentException("Initial capacity must be a positive integer (not " + initialCapacity + ")");

            this.array = new int[initialCapacity];
            this.size = 0;
        }

        /**
         * Increases the capacity of the stack, if necessary, to hold at least <code>minCapacity</code> elements.
         *
         * The resizing heuristic is from <code>java.util.ArrayList</code>.
         *
         * @param minCapacity
         */
        public void ensureCapacity(int minCapacity)
        {
            if (minCapacity <= this.array.length) return;

            int newCapacity = Math.max((this.array.length * 3) / 2 + 1, minCapacity);
            int[] newStack = new int[newCapacity];
            System.arraycopy(this.array, 0, newStack, 0, this.size);
            this.array = newStack;
        }

        public void add(int value)
        {
            ensureCapacity(this.size + 1);
            this.array[this.size++] = value;
        }

        public int get(int index)
        {
            if (index < 0 || index > this.size) throw new IndexOutOfBoundsException();

            return this.array[index];
        }

        public boolean isEmpty()
        {
            return this.size == 0;
        }

        public void clear()
        {
            this.size = 0;
        }

        public int size()
        {
            return this.size;
        }

        @Override public String toString()
        {
            StringBuilder sb = new StringBuilder();
            sb.append("[");
            for (int i = 0; i < this.size; i++)
            {
                if (i > 0) sb.append(", ");
                sb.append(this.array[i]);
            }
            sb.append("]");
            return sb.toString();
        }
    }

    ///////////////////////////////////////////////////////////////////////////

//    public static void main(String[] args) throws Exception
//    {
//        InternalCDTDB db = new InternalCDTDB(new File("/Users/joverbey/Desktop/db"));
//
//        db.files.ensure("Dog");
//        db.files.ensure("Cat");
//        db.files.ensure("Mouse");
//        System.out.println(db.files);
//
//        db.files.delete("Dog");
//        System.out.println(db.files);
//
//        db.files.delete("Cat");
//        System.out.println(db.files);
//
//        db.files.delete("Mouse");
//        System.out.println(db.files);
//
//        db.files.ensure("Mouse");
//        db.files.ensure("Mouse");
//        System.out.println(db.files);
//
//        db.files.ensure("Dog");
//        db.files.ensure("Cat");
//        db.files.ensure("Mouse");
//        System.out.println(db.files);
//
//        db.dependencies.ensure("Dog", "Cat");
//        db.dependencies.ensure("Dog", "Cat");
//        System.out.println(db.dependencies);
//
//        db.dependencies.ensure("Dog", "Tiger");
//        System.out.println(db.dependencies);
//
//        db.dependencies.delete("Dog", "Cat");
//        System.out.println(db.dependencies);
//
//        db.dependencies.delete("Dog", "Coolio");
//        System.out.println(db.dependencies);
//
//        db.dependencies.delete("Dog", "Tiger");
//        db.dependencies.delete("Dog", "Tiger");
//        System.out.println(db.dependencies);
//
//        System.out.println(db.files);
//
//        db.edges.ensure("Cat", 1, 2, "Dog", 3, 4, 0);
//        db.edges.ensure("Dog", 4, 3, "Cat", 2, 1, 1);
//        System.out.println(db.edges);
//
//        db.edges.ensure("Dog", 4, 3, "Beetle", 2, 1, 6);
//        db.edges.ensure("Dog", 4, 3, "Beetle", 2, 1, 6);
//        System.out.println(db.edges);
//
//        System.out.println(db.files);
//
//        db.edges.delete("Dog", 4, 3, "Beetle", 2, 1, 6);
//        db.edges.delete("Dog", 4, 3, "Beetle", 2, 1, 6);
//        System.out.println(db.edges);
//
//        System.out.println(db.files);
//    }
}
