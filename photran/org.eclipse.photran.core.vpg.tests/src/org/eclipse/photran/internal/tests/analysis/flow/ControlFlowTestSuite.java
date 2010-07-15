/*******************************************************************************
 * Copyright (c) 2010 University of Illinois at Urbana-Champaign and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Matthew Fotzler, UIUC - Initial API and implementation
 *******************************************************************************/
package org.eclipse.photran.internal.tests.analysis.flow;

import java.io.File;

import junit.framework.Test;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.photran.core.IFortranAST;
import org.eclipse.photran.internal.core.lexer.Token;
import org.eclipse.photran.internal.core.parser.IActionStmt;
import org.eclipse.photran.internal.core.parser.IExecutableConstruct;
import org.eclipse.photran.internal.core.vpg.PhotranTokenRef;
import org.eclipse.photran.internal.core.vpg.PhotranVPG;
import org.eclipse.photran.internal.tests.Activator;
import org.eclipse.photran.internal.tests.PhotranWorkspaceTestCase;
import org.eclipse.rephraserengine.core.analysis.flow.FlowGraphNode;
import org.eclipse.rephraserengine.core.analysis.flow.VPGFlowGraph;
import org.eclipse.rephraserengine.testing.junit3.GeneralTestSuiteFromMarkers;

/**
 * Unit tests for the control flow graph constructor.
 * 
 * @author Matthew Fotzler
 */
public class ControlFlowTestSuite extends GeneralTestSuiteFromMarkers
{
    private static final String DIR = "control-flow-test-code";
 
    public static Test suite() throws Exception
    {
        return new ControlFlowTestSuite();
    }
    
    public ControlFlowTestSuite() throws Exception
    {
        super("Constructing control flow graph for",
            PhotranWorkspaceTestCase.MARKER,
            new File(DIR),
            PhotranWorkspaceTestCase.FORTRAN_FILE_FILTER);
    }

    @Override
    protected Test createTestFor(File fileContainingMarker, int markerOffset, String markerText)
        throws Exception
    {
        return new ControlFlowTestCase(fileContainingMarker, markerText) {};
    }
    
    public static abstract class ControlFlowTestCase extends PhotranWorkspaceTestCase
    {
        private File javaFile;
        private IFile file;
        private String markerText;

        public ControlFlowTestCase(File file, String markerText) throws Exception
        {
            super("test");
            this.javaFile = file;
            this.markerText = markerText;
        }
        
        @Override public void setUp() throws Exception
        {
            super.setUp();

            this.file = importFile(Activator.getDefault(), javaFileDirectory(), javaFile.getName());
            project.refreshLocal(IResource.DEPTH_INFINITE, new NullProgressMonitor());
        }

        protected String javaFileDirectory()
        {
            return DIR+File.separator+javaFile.getParentFile().getName();
        }

        public void test() throws Exception
        {
            PhotranVPG vpg = PhotranVPG.getInstance();
            vpg.ensureVPGIsUpToDate(new NullProgressMonitor());
            
            IFortranAST ast = vpg.acquireTransientAST(file);
            assertNotNull(ast);
            
            Token programToken = ast.findFirstTokenOnLine(1);
            Token endToken = ast.findFirstTokenOnLine(Integer.parseInt(markerText));
            assertNotNull(programToken);
            assertNotNull(endToken);
            
            PhotranTokenRef ctlFlowEntryNode = programToken.getTokenRef();
            PhotranTokenRef ctlFlowExitNode = endToken.getTokenRef();
            PhotranVPGFlowGraph flowGraph = new PhotranVPGFlowGraph(ctlFlowEntryNode, ctlFlowExitNode);

            String actual = flowGraph.toString();
            String expected = this.readTestFile(Activator.getDefault(), javaFileDirectory(), file.getName() + ".result").replace("\r", "");
            assertEquals(expected, actual);
        }
        
        private static final class PhotranVPGFlowGraph extends VPGFlowGraph<PhotranTokenRef, IExecutableConstruct>
        {
            private PhotranVPGFlowGraph(PhotranTokenRef entryNodeRef, PhotranTokenRef exitNodeRef)
            {
                super(PhotranVPG.getInstance(), entryNodeRef, exitNodeRef, PhotranVPG.CONTROL_FLOW_EDGE_TYPE);
            }

            @Override protected IExecutableConstruct map(PhotranTokenRef tokenRef)
            {
                Token token = tokenRef.findToken();
                IActionStmt actionStmt = token.findNearestAncestor(IActionStmt.class);
                if (actionStmt != null)
                    return actionStmt;
                else
                    return token.findNearestAncestor(IExecutableConstruct.class);
            }

            @Override protected String nodeDataAsString(FlowGraphNode<IExecutableConstruct> node)
            {
                return String.valueOf(node.getData()).trim();
            }
        }
    }
}