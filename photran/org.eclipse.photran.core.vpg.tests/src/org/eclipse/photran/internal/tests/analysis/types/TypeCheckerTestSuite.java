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
package org.eclipse.photran.internal.tests.analysis.types;

import java.io.File;

import junit.framework.Test;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.jface.text.TextSelection;
import org.eclipse.photran.core.IFortranAST;
import org.eclipse.photran.internal.core.analysis.loops.ASTVisitorWithLoops;
import org.eclipse.photran.internal.core.analysis.types.TypeChecker;
import org.eclipse.photran.internal.core.lexer.Token;
import org.eclipse.photran.internal.core.parser.IExpr;
import org.eclipse.photran.internal.core.vpg.PhotranVPG;
import org.eclipse.photran.internal.tests.Activator;
import org.eclipse.photran.internal.tests.PhotranWorkspaceTestCase;
import org.eclipse.rephraserengine.testing.junit3.GeneralTestSuiteFromMarkers;

/**
 * Unit tests for the type checker.
 * 
 * @author Abhishek Sharma - Fortran code
 * @author Jeff Overbey - this class
 */
public class TypeCheckerTestSuite extends GeneralTestSuiteFromMarkers
{
    private static final String DIR = "type-checker-test-code";
 
    public static Test suite() throws Exception
    {
        return new TypeCheckerTestSuite();
    }
    
    public TypeCheckerTestSuite() throws Exception
    {
        super("Type checking",
            PhotranWorkspaceTestCase.MARKER,
            new File(DIR),
            PhotranWorkspaceTestCase.FORTRAN_FILE_FILTER);
    }

    @Override
    protected Test createTestFor(File fileContainingMarker, int markerOffset, String markerText)
        throws Exception
    {
        return new TypeCheckerTestCase(fileContainingMarker, markerText) {};
    }
    
    public static abstract class TypeCheckerTestCase extends PhotranWorkspaceTestCase
    {
        private File javaFile;
        private IFile file;
        private String markerText;

        public TypeCheckerTestCase(File file, String markerText) throws Exception
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
            
            String[] markerFields = parseMarker(markerText);
            TextSelection selection = determineSelection(markerFields, file);
            
            IExpr expression = findExpression(ast, selection.getOffset(), selection.getLength());
            assertNotNull(expression); // If this fails, a marker's line/column information does not correspond to an expression
            
            String expectedType = markerFields[4];
            String actualType = TypeChecker.getTypeOf(expression).toString();
            assertEquals(expectedType, actualType);
        }

        private IExpr findExpression(IFortranAST ast, final int offset, final int length)
        {
            class V extends ASTVisitorWithLoops
            {
                private IExpr result = null;
                
                @Override public void visitIExpr(IExpr expr)
                {
                    Token firstToken = expr.findFirstToken();
                    Token lastToken = expr.findLastToken();
                    
                    int exprOffset = firstToken.getFileOffset();
                    int exprLength = lastToken.getFileOffset() + lastToken.getLength() - offset;
                    
                    if (exprOffset == offset && exprLength == length)
                        result = expr;
                }
            };
            
            V v = new V();
            ast.getRoot().accept(v);
            return v.result;
        }
    }
}