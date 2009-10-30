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
import java.io.IOException;
import java.io.InputStream;
import java.io.Serializable;

import org.eclipse.rephraserengine.core.vpg.TokenRef;
import org.eclipse.rephraserengine.core.vpg.VPG;
import org.eclipse.rephraserengine.core.vpg.VPGDB;
import org.eclipse.rephraserengine.core.vpg.VPGEdge;
import org.eclipse.rephraserengine.core.vpg.VPGLog;
import org.eclipse.rephraserengine.core.vpg.db.cdt.CDTDB;

import junit.framework.TestCase;

public class VPGTests extends TestCase
{
    private static class MyVPGDB extends CDTDB<Boolean, Integer, TokenRef<Integer>, VPGLog<Integer, TokenRef<Integer>>>
    {
        public MyVPGDB(File file)
        {
            super(file);
        }

        @Override
        protected Serializable deserialize(InputStream binaryStream) throws IOException, ClassNotFoundException
        {
            return null;
        }

        @Override
        protected long getModificationStamp(String filename)
        {
            return 0;
        }

        @Override
        protected byte[] serialize(Serializable annotation) throws IOException
        {
            return new byte[] {};
        }
    }

    private static class MyVPG extends VPG<Boolean, Integer, TokenRef<Integer>, MyVPGDB, VPGLog<Integer, TokenRef<Integer>>>
    {
        public MyVPG(File file)
        {
            super(new VPGLog<Integer, TokenRef<Integer>>(), new MyVPGDB(file));
        }

        @Override
        protected void calculateDependencies(String filename)
        {
        }

        @Override
        protected boolean shouldProcessFile(String filename)
        {
            return true;
        }

        @Override
        public TokenRef<Integer> createTokenRef(String filename, int offset, int length)
        {
            return new TokenRef<Integer>(filename, offset, length);
        }

        @Override
        public Integer findToken(TokenRef<Integer> tokenRef)
        {
            return tokenRef.getOffset();
        }

        @Override
        protected TokenRef<Integer> getTokenRef(Integer forToken)
        {
            return new TokenRef<Integer>("Hello", forToken, 0);
        }

        @Override
        protected Boolean parse(String filename)
        {
            return Boolean.TRUE;
        }

        @Override
        protected void populateVPG(String filename, Boolean ast)
        {
            db.ensure(new VPGEdge<Boolean, Integer, TokenRef<Integer>>(this, getTokenRef(1), getTokenRef(2), 3));
            db.ensure(new VPGEdge<Boolean, Integer, TokenRef<Integer>>(this, getTokenRef(1), getTokenRef(2), 3));
            db.ensure(new VPGEdge<Boolean, Integer, TokenRef<Integer>>(this, getTokenRef(1), getTokenRef(2), 4));
            db.ensure(new VPGEdge<Boolean, Integer, TokenRef<Integer>>(this, getTokenRef(5), getTokenRef(6), 7));
        }
    }

    private MyVPG vpg = null;

    @Override
    protected void setUp() throws Exception
    {
        File dbFile = File.createTempFile("vpg", null);
        dbFile.deleteOnExit();

        vpg = new MyVPG(dbFile);
    }

    @SuppressWarnings("unused")
    public void testEdges()
    {
        vpg.acquireTransientAST("Hello");

        int count = 0;
        for (TokenRef<Integer> t : vpg.db.getIncomingEdgeSources(vpg.getTokenRef(2), VPGDB.ALL_EDGES))
            count++;
        assertEquals(2, count);

        count = 0;
        for (TokenRef<Integer> t : vpg.db.getOutgoingEdgeTargets(vpg.getTokenRef(1), VPGDB.ALL_EDGES))
            count++;
        assertEquals(2, count);

        count = 0;
        for (TokenRef<Integer> t : vpg.db.getIncomingEdgeSources(vpg.getTokenRef(2), 3))
            count++;
        assertEquals(1, count);

        count = 0;
        for (TokenRef<Integer> t : vpg.db.getOutgoingEdgeTargets(vpg.getTokenRef(1), 3))
            count++;
        assertEquals(1, count);
    }
}
