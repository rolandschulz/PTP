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
package org.eclipse.photran.internal.tests.vpg.failing;

import java.io.File;
import java.util.ArrayList;

import junit.framework.Test;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.photran.core.IFortranAST;
import org.eclipse.photran.internal.core.analysis.loops.ASTVisitorWithLoops;
import org.eclipse.photran.internal.core.lexer.Token;
import org.eclipse.photran.internal.core.vpg.PhotranVPG;
import org.eclipse.photran.internal.tests.failing.Activator;
import org.eclipse.photran.internal.tests.PhotranWorkspaceTestCase;
import org.eclipse.rephraserengine.testing.junit3.GeneralTestSuiteFromMarkers;

/**
 * Test Suite for variable access marking (performed by the <code>ReferenceCollector</code>).
 * 
 * @author Matthew Fotzler
 */
public class VariableAccessTestSuite extends GeneralTestSuiteFromMarkers
{
    private static final String DIR = "var-access-test-code";
    
    public static Test suite() throws Exception
    {
        return new VariableAccessTestSuite();
    }
    
    public VariableAccessTestSuite() throws Exception
    {
        super("Constructing variable access marking for",
            PhotranWorkspaceTestCase.MARKER,
            new File(DIR),
            PhotranWorkspaceTestCase.FORTRAN_FILE_FILTER);
    }
    
    @Override
    protected Test createTestFor(File fileContainingMarker, int markerOffset, String markerText)
        throws Exception
    {
        return new ReferenceCollectorTestCase(fileContainingMarker, markerText) {};
    }
    
    public static abstract class ReferenceCollectorTestCase extends PhotranWorkspaceTestCase
    {
        private File javaFile;
        private IFile file;
        private String[] markerText;
        
        public ReferenceCollectorTestCase(File file, String markerText) throws Exception
        {
            super("test");
            this.javaFile = file;
            this.markerText = parseMarker(markerText);
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
            
            IFortranAST ast = PhotranVPG.getInstance().acquireTransientAST(file);
            assertNotNull(ast);
            
            String lineNumber = markerText[0];
            String column = markerText[1];
            Token token = findTokenByLineCol(ast, Integer.parseInt(lineNumber), Integer.parseInt(column));
            assertNotNull(token);

            String line = getLine(ast, Integer.parseInt(lineNumber));
            String actual = line + token.getText() + " - " + token.getVariableAccessType().toString();
            String expected = line + token.getText() + " - " + markerText[2];
            if (!expected.equals(actual))
                System.err.println(file.getFullPath());
            assertEquals(expected, actual);
        }

        private String getLine(IFortranAST ast, int lineNumber)
        {
            final ArrayList<Token> lineTokens = new ArrayList<Token>();
            final int lineNum = lineNumber;
            String line = "";
            
            ast.accept(new ASTVisitorWithLoops(){
                @Override public void visitToken(Token token)
                {
                    if(token.getLine() == lineNum)
                        lineTokens.add(token);
                }
            });
            
            for(Token token : lineTokens)
            {
                line += token.getWhiteBefore() + token.getText() + token.getWhiteAfter();
            }
            
            line += "\n";
            
            return line;
        }
        
        private Token findTokenByLineCol(IFortranAST ast, int line, int col)
        {
            for (Token token : ast)
                if (token.getLine() == line && token.getCol() == col)
                    return token;

            return null;
        }
    }
}
