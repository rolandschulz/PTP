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
package org.eclipse.rephraserengine.internal.core.tests.vpg;

import java.io.File;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;

import junit.framework.TestCase;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.rephraserengine.core.util.Pair;
import org.eclipse.rephraserengine.core.util.TwoKeyHashMap;
import org.eclipse.rephraserengine.core.vpg.IVPGNode;
import org.eclipse.rephraserengine.core.vpg.VPGDependency;
import org.eclipse.rephraserengine.core.vpg.VPGEdge;
import org.eclipse.rephraserengine.core.vpg.db.ram.RAMDB;

/**
 * Class that checks the correctness of NewDB.java
 * 
 * @author Esfar Huq
 * @author Rui Wang
 * @author Jeff Overbey - replaced public fields with MyRAMDB
 */
@SuppressWarnings(value={"unchecked", "rawtypes"})
public class RAMDBTests extends TestCase
{
    private static class MyRAMDB<A, T, R extends IVPGNode<T>> extends RAMDB<A, T, R>
    {
        private static File tempFile = null;
        
        public MyRAMDB() throws IOException
        {
            super(null, createTempFile());
        }

        private static File createTempFile() throws IOException
        {
            if (tempFile == null)
            {
                tempFile = File.createTempFile("rephraser", "ramdb");
                tempFile.deleteOnExit();
            }
            return tempFile;
        }

        @Override
        public long getModificationStamp(String filename)
        {
            return 0L;
        }
        
        public HashMap<String, Long> files() { return files; }
        public HashSet<VPGDependency<A, T, R>> dependencies() { return dependencies; }
        public HashMap<R, Set<VPGEdge<A, T, R>>> outgoingEdges() { return outgoingEdges; }
        //public HashMap<R, Set<VPGEdge<A, T, R>>> incomingEdges() { return incomingEdges; }
        public TwoKeyHashMap<R, Integer, Serializable> annotations() { return annotations; }

        @Override protected Object readObject(ObjectInputStream in) throws IOException, ClassNotFoundException { return in.readObject(); }
    }
    
    private MyRAMDB db;
    
    @Override
    protected void setUp() throws Exception
    {
        super.setUp();
        
        this.db = new MyRAMDB();
    }

    public void testDependencies() throws CoreException
    {
        assertNotNull(db);
        assertNotNull(db.dependencies());
        
        VPGDependency d1 = new VPGDependency("A.txt", "B.txt");
        VPGDependency d2 = new VPGDependency("C.txt", "D.txt");
        VPGDependency d3 = new VPGDependency("C.txt", "E.txt");
        VPGDependency d4 = new VPGDependency("C.txt", "F.txt");
        
        //ENSURE
        db.ensure(d1);
        db.ensure(d2);
        db.ensure(d2);  //check if duplicates are handled
        db.ensure(d3);
        db.ensure(d4);
        assertEquals(4, db.dependencies().size());
        
        //DELETE
        db.delete(d1);
        db.delete(d1);
        assertEquals(3, db.dependencies().size());
        assertEquals(true, db.dependencies().contains(d2));
        
        //GET OUTGOING DEPENDENCIES FROM
        Iterable<String> result1 = db.getOutgoingDependenciesFrom("C.txt");
        assertEquals("[D.txt, E.txt, F.txt]", result1.toString());
        
        //GET INCOMING DEPENDENCIES TO
        Iterable<String> result2 = db.getIncomingDependenciesTo("E.txt");
        assertEquals("[C.txt]", result2.toString());
    }
    
    public void testEdges() throws CoreException
    {
        assertNotNull(db);
        assertNotNull(db.outgoingEdges());
        
        VPGEdge e1 = new VPGEdge(new TestTokenRef(db, "C.txt", 0, 0), new TestTokenRef(db, "D.txt", 0, 0), Integer.MIN_VALUE);
        VPGEdge e2 = new VPGEdge(new TestTokenRef(db, "A.txt", 0, 0), new TestTokenRef(db, "B.txt", 0, 0), Integer.MIN_VALUE);
        VPGEdge e3 = new VPGEdge(new TestTokenRef(db, "C.txt", 0, 0), new TestTokenRef(db, "A.txt", 0, 0), Integer.MIN_VALUE);
        
        //ENSURE
        db.ensure(e1);
        db.ensure(e2);
        db.ensure(e3);
        db.ensure(e1);
        assertEquals(2, db.outgoingEdges().keySet().size());
        
        //DELETE
        db.delete(e3);
        db.delete(e3);
        //System.out.println(db.outgoingEdges());
        assertEquals(2, db.outgoingEdges().keySet().size());
        
        //GET ALL EDGES FOR
        Iterable<VPGEdge> result1 = db.getAllEdgesFor("B.txt");
        List<VPGEdge> r1 = new ArrayList<VPGEdge>();
        r1.add(e2);
        
        assertEquals(result1.toString(),r1.toString());
    }
    
    public void testAnnotations() throws CoreException
    {
        assertNotNull(db);
        assertNotNull(db.annotations());
        
        //SET ANNOTATION
        int count = 0;
        
        TestTokenRef r1 = new TestTokenRef(db, "A.txt", 0, 0);
        TestTokenRef r2 = new TestTokenRef(db, "B.txt", 0, 0);
        TestTokenRef r3 = new TestTokenRef(db, "C.txt", 0, 0);
        
        byte[] b1 = new byte[] {1, 2, 3};
        byte[] b2 = new byte[] {1, 2, 3};
        byte[] b3 = new byte[] {1, 2, 3};
        
        db.setAnnotation(r1, 0, b1);
        db.setAnnotation(r2, 1, b2);
        db.setAnnotation(r3, 2, b3);
        db.setAnnotation(r1, 0, b1);
        
        for(TestTokenRef r : (Set<TestTokenRef>)db.annotations().keySet())
        {
            count += db.annotations().getAllEntriesFor(r).keySet().size();
        }
        
        assertEquals(3, count);
        
        //DELETE ANNOTATION
        count = 0;
        db.deleteAnnotation(r2, 1);
        db.deleteAnnotation(r3, 2);
        db.deleteAnnotation(r1, -1);
        
        for(TestTokenRef r : (Set<TestTokenRef>)db.annotations().keySet())
        {
            count += db.annotations().getAllEntriesFor(r).keySet().size();
        }
        
        assertEquals(1, count);
               
        //GET ANNOTATION
        assertEquals(b1, db.getAnnotation(r1, 0));
        assertEquals(null, db.getAnnotation(r3, 2));
        
        //GET ALL ANNOTATIONS FOR
        Iterable<Pair<TestTokenRef, Integer>> a = db.getAllAnnotationsFor("A.txt");
        assertEquals(a.iterator().next(), new Pair<TestTokenRef, Integer>(r1, 0));
    }
    
    public void testFiles()
    {
        //reset the database for convenience
        try
        {
            this.setUp();
        }catch(Exception e)
        {
            e.printStackTrace();
        }
       
        assertNotNull(db);
        assertNotNull(db.files());
       
        //MODIFICATION STAMP
        db.updateModificationStamp("A.txt");
        assertEquals(0L, db.getModificationStamp("A.txt"));
       
        //DELETE ALL ENTRIES FOR
        VPGDependency d1 = new VPGDependency("A.txt", "B.txt");
        VPGDependency d2 = new VPGDependency("C.txt", "D.txt");
        VPGDependency d3 = new VPGDependency("C.txt", "E.txt");
        VPGDependency d4 = new VPGDependency("C.txt", "F.txt");
        
        db.ensure(d1);
        db.ensure(d2);
        db.ensure(d3);
        db.ensure(d4);
        
        VPGEdge e1 = new VPGEdge(new TestTokenRef(db, "C.txt", 0, 0), new TestTokenRef(db, "D.txt", 0, 0), Integer.MIN_VALUE);
        VPGEdge e2 = new VPGEdge(new TestTokenRef(db, "A.txt", 0, 0), new TestTokenRef(db, "B.txt", 0, 0), Integer.MIN_VALUE);
        VPGEdge e3 = new VPGEdge(new TestTokenRef(db, "C.txt", 0, 0), new TestTokenRef(db, "A.txt", 0, 0), Integer.MIN_VALUE);
        
        db.ensure(e1);
        db.ensure(e2);
        db.ensure(e3);
        
        TestTokenRef r1 = new TestTokenRef(db, "A.txt", 0, 0);
        TestTokenRef r2 = new TestTokenRef(db, "B.txt", 0, 0);
        TestTokenRef r3 = new TestTokenRef(db, "C.txt", 0, 0);
        
        byte[] b1 = new byte[] {1, 2, 3};
        byte[] b2 = new byte[] {1, 2, 3};
        byte[] b3 = new byte[] {1, 2, 3};
        
        db.setAnnotation(r1, 0, b1);
        db.setAnnotation(r2, 1, b2);
        db.setAnnotation(r3, 2, b3);
        
        db.deleteAllEntriesFor("A.txt");
        assertEquals(3, db.dependencies().size());
        assertEquals(1, db.outgoingEdges().keySet().size());
        assertEquals(2, db.annotations().keySet().size());
        
        //LIST ALL FILENAMES
        TreeSet<String> filenames = new TreeSet<String>(db.listAllFilenames());
        assertEquals("[A.txt, B.txt, C.txt, D.txt, E.txt, F.txt]", filenames.toString());
        
        //LIST ALL FILENAMES WITH DEPENDENTS
        filenames = new TreeSet<String>(db.listAllFilenamesWithDependents());
        assertEquals("[D.txt, E.txt, F.txt]", filenames.toString());
        
        //LIST ALL DEPENDENT FILENAMES
        filenames = new TreeSet<String>(db.listAllDependentFilenames());
        assertEquals("[C.txt]", filenames.toString());
    }
    
    public void testFlush() throws Exception
    {
        //reset the database for convenience
        this.setUp();
       
        assertNotNull(db);
        assertNotNull(db.files());
        assertNotNull(db.dependencies());
        assertNotNull(db.outgoingEdges());
        assertNotNull(db.annotations());
        
        VPGDependency d1 = new VPGDependency("A.txt", "B.txt");
        VPGDependency d2 = new VPGDependency("C.txt", "D.txt");
        
        db.ensure(d1);
        db.ensure(d2);
        
        VPGEdge e2 = new VPGEdge(new TestTokenRef(db, "A.txt", 0, 0), new TestTokenRef(db, "B.txt", 0, 0), Integer.MIN_VALUE);
        db.ensure(e2);
        
        db.flush();
        this.setUp();
        
        assertEquals(2, db.dependencies().size());
        assertEquals(1, db.outgoingEdges().keySet().size());
        assertEquals(4, db.files().keySet().size());
    }
}
