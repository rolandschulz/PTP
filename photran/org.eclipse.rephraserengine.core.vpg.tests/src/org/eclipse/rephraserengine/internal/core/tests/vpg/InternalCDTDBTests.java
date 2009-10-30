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
package org.eclipse.rephraserengine.internal.core.tests.vpg;

import java.io.File;

import junit.framework.TestCase;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.rephraserengine.internal.core.vpg.db.cdt.InternalCDTDB;
import org.eclipse.rephraserengine.internal.core.vpg.db.cdt.InternalCDTDB.Annotations;
import org.eclipse.rephraserengine.internal.core.vpg.db.cdt.InternalCDTDB.Dependencies;
import org.eclipse.rephraserengine.internal.core.vpg.db.cdt.InternalCDTDB.Edges;
import org.eclipse.rephraserengine.internal.core.vpg.db.cdt.InternalCDTDB.Files;

public class InternalCDTDBTests extends TestCase
{
    private InternalCDTDB db = null;

    @Override
    protected void setUp() throws Exception
    {
        File dbFile = File.createTempFile("vpg", null);
        dbFile.deleteOnExit();

        db = new InternalCDTDB(dbFile);
    }

    public void testFiles() throws CoreException
    {
        assertNotNull(db);
        Files f = db.files;
        assertNotNull(f);

        assertEquals(0, numFiles());
        assertTrue(f.findRecordFor("a") < 0);
        assertTrue(f.findRecordFor("a") < 0);

        for (String filename : new String[] { "a", "b", "c" })
        {
            int record = f.ensure(filename);
            assertTrue(record >= 0);
            assertEquals(record, f.ensure(filename));
            assertEquals(record, f.findRecordFor(filename));
            assertEquals(filename, f.getFilename(record).getString());
        }

        assertEquals(3, numFiles());

        f.delete("b");

        assertTrue(f.findRecordFor("a") >= 0);
        assertTrue(f.findRecordFor("b") < 0);
        assertTrue(f.findRecordFor("c") >= 0);
        assertEquals(2, numFiles());

        int record = f.findRecordFor("a");
        f.setModificationStamp(record, 12345);
        assertEquals(12345, f.getModificationStamp(record));
        f.setModificationStamp(record, 54321);
        assertEquals(54321, f.getModificationStamp(record));

        f.delete("a");
        f.delete("c");
        f.delete("c");
        assertEquals(0, numFiles());
    }

    private int numFiles() throws CoreException
    {
        return db.files.findAllFileRecords().size();
    }

    public void testDependencies() throws CoreException
    {
        assertNotNull(db);
        Dependencies d = db.dependencies;
        assertNotNull(d);

        assertEquals(0, numFiles());
        assertDependencies(0, 0, 0, 0, 0, 0);

        d.ensure("a", "b");
        assertDependencies(0, 1, 1, 0, 0, 0);
        d.ensure("a", "b");
        assertDependencies(0, 1, 1, 0, 0, 0);

        d.ensure("a", "c");
        assertDependencies(0, 2, 1, 0, 1, 0);

        d.ensure("c", "a");
        assertDependencies(1, 2, 1, 0, 1, 1);

        d.delete("a", "b");
        assertDependencies(1, 1, 0, 0, 1, 1);

        d.delete("c", "a");
        assertDependencies(0, 1, 0, 0, 1, 0);

        d.delete("a", "c");
        assertDependencies(0, 0, 0, 0, 0, 0);

        db.files.delete("a");
        db.files.delete("b");
        db.files.delete("c");
        assertEquals(0, numFiles());
    }

    private void assertDependencies(int inA, int outA, int inB, int outB, int inC, int outC) throws CoreException
    {
        assertEquals(inA, db.dependencies.findAllIncomingDependencyRecordsTo("a").size());
        assertEquals(outA, db.dependencies.findAllOutgoingDependencyRecordsFrom("a").size());

        assertEquals(inB, db.dependencies.findAllIncomingDependencyRecordsTo("b").size());
        assertEquals(outB, db.dependencies.findAllOutgoingDependencyRecordsFrom("b").size());

        assertEquals(inC, db.dependencies.findAllIncomingDependencyRecordsTo("c").size());
        assertEquals(outC, db.dependencies.findAllOutgoingDependencyRecordsFrom("c").size());

        assertEquals(inA > 0, db.dependencies.hasIncomingDependencyRecords("a"));
        assertEquals(outA > 0, db.dependencies.hasOutgoingDependencyRecords("a"));

        assertEquals(inB > 0, db.dependencies.hasIncomingDependencyRecords("b"));
        assertEquals(outB > 0, db.dependencies.hasOutgoingDependencyRecords("b"));

        assertEquals(inC > 0, db.dependencies.hasIncomingDependencyRecords("c"));
        assertEquals(outC > 0, db.dependencies.hasOutgoingDependencyRecords("c"));
    }

    public void testEdges() throws CoreException
    {
        assertNotNull(db);
        Edges e = db.edges;
        assertNotNull(e);

        assertEquals(0, numFiles());

        e.ensure("a", 1, 2, "b", 3, 4, 5);
        assertEdges(0, 1, 1, 0, 0, 0);
        e.ensure("a", 1, 2, "b", 3, 4, 5);
        assertEdges(0, 1, 1, 0, 0, 0);

        e.ensure("a", 1, 2, "b", 3, 4, 6);
        assertEdges(0, 2, 2, 0, 0, 0);

        int record = e.ensure("b", 3, 4, "a", 1, 2, 6);
        assertEdges(1, 2, 2, 1, 0, 0);

        assertEquals("b", db.files.getFilename(e.getFromFileRecordPtr(record)).getString());
        assertEquals(3, e.getFromOffset(record));
        assertEquals(4, e.getFromLength(record));
        assertEquals("a", db.files.getFilename(e.getToFileRecordPtr(record)).getString());
        assertEquals(1, e.getToOffset(record));
        assertEquals(2, e.getToLength(record));
        assertEquals(6, e.getEdgeType(record));

        assertEquals(2, e.findAllOutgoingEdgeRecordsFrom("a", 1, 2).size());
        assertEquals(2, e.findAllIncomingEdgeRecordsTo("b", 3, 4).size());

        assertEquals(1, e.findAllOutgoingEdgeRecordsFrom("a", 1, 2, 5).size());
        assertEquals(1, e.findAllIncomingEdgeRecordsTo("b", 3, 4, 5).size());
        assertEquals(1, e.findAllOutgoingEdgeRecordsFrom("a", 1, 2, 6).size());
        assertEquals(1, e.findAllIncomingEdgeRecordsTo("b", 3, 4, 6).size());
        assertEquals(0, e.findAllOutgoingEdgeRecordsFrom("a", 1, 2, 7).size());
        assertEquals(0, e.findAllIncomingEdgeRecordsTo("b", 3, 4, 7).size());

        db.files.delete("a");
        db.files.delete("b");
        db.files.delete("c");
        assertEquals(0, numFiles());
    }

    private void assertEdges(int inA, int outA, int inB, int outB, int inC, int outC) throws CoreException
    {
        assertEquals(inA, db.edges.findAllIncomingEdgeRecordsTo("a").size());
        assertEquals(outA, db.edges.findAllOutgoingEdgeRecordsFrom("a").size());

        assertEquals(inB, db.edges.findAllIncomingEdgeRecordsTo("b").size());
        assertEquals(outB, db.edges.findAllOutgoingEdgeRecordsFrom("b").size());

        assertEquals(inC, db.edges.findAllIncomingEdgeRecordsTo("c").size());
        assertEquals(outC, db.edges.findAllOutgoingEdgeRecordsFrom("c").size());

        assertEquals(inA > 0, db.edges.hasIncomingEdges("a"));
        assertEquals(outA > 0, db.edges.hasOutgoingEdges("a"));

        assertEquals(inB > 0, db.edges.hasIncomingEdges("b"));
        assertEquals(outB > 0, db.edges.hasOutgoingEdges("b"));

        assertEquals(inC > 0, db.edges.hasIncomingEdges("c"));
        assertEquals(outC > 0, db.edges.hasOutgoingEdges("c"));
    }

    public void testAnnotations() throws CoreException
    {
        assertNotNull(db);
        Annotations a = db.annotations;
        assertNotNull(a);

        assertEquals(0, numFiles());

        byte[] a1 = new byte[] { 1, 2, 3 };
        byte[] a2 = new byte[] { 4, 5, 6 };

        a.set("a", 1, 2, 0, a2);
        assertAnnotations(1, 0);

        a.set("a", 1, 2, 0, a1);
        assertAnnotations(1, 0);

        a.set("a", 1, 2, 1, a2);
        assertAnnotations(2, 0);

        System.out.println(a);

        a.set("a", 1, 3, 1, a2);
        System.out.println(a);
        assertAnnotations(3, 0);

        assertEquals(2, db.annotations.findAllAnnotationRecordsFor("a", 1, 2).size());
        assertEquals(1, db.annotations.findAllAnnotationRecordsFor("a", 1, 3).size());

        db.files.delete("a");
        db.files.delete("b");
        db.files.delete("c");
        assertEquals(0, numFiles());
    }

    private void assertAnnotations(int numA, int numB) throws CoreException
    {
        assertEquals(numA, db.annotations.findAllAnnotationRecordsFor("a").size());
        assertEquals(numB, db.annotations.findAllAnnotationRecordsFor("b").size());
    }
}
