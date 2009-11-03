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
package org.eclipse.photran.refactoring.tests.infrastructure;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.core.runtime.OperationCanceledException;
import org.eclipse.ltk.core.refactoring.RefactoringStatus;
import org.eclipse.photran.core.IFortranAST;
import org.eclipse.photran.internal.core.parser.ASTMainProgramNode;
import org.eclipse.photran.internal.core.parser.ASTModuleNode;
import org.eclipse.photran.internal.core.parser.ASTSubroutineSubprogramNode;
import org.eclipse.photran.internal.core.refactoring.infrastructure.AbstractFortranRefactoring;
import org.eclipse.photran.internal.core.vpg.PhotranVPG;
import org.eclipse.photran.refactoring.tests.RefactoringTestCase;

/**
 * Unit tests for {@link PhotranVPG#listAllModules()}
 *
 * @author Jeff Overbey
 */
public class CheckCanAddDeclTestCase extends RefactoringTestCase
{
    private static final String DIR = "check-can-add-test-code";

    private ASTMainProgramNode mainProgram;
    private ASTSubroutineSubprogramNode internalSubroutine;
    private ASTModuleNode module;

    @Override public void setUp() throws Exception
    {
        super.setUp();

        IFile main_f90 = importFile(DIR, "main.f90");
        IFile module_f90 = importFile(DIR, "module.f90");
        importFile(DIR, "external.f90");
        PhotranVPG.getInstance().ensureVPGIsUpToDate(new NullProgressMonitor());

        project.refreshLocal(IResource.DEPTH_INFINITE, new NullProgressMonitor());
        compileAndRunFortranProgram("module.f90", "main.f90", "external.f90");

        PhotranVPG vpg = PhotranVPG.getInstance();
        IFortranAST mainFileAST = vpg.acquireTransientAST(main_f90);
        IFortranAST moduleFileAST = vpg.acquireTransientAST(module_f90);
        assertNotNull(mainFileAST);
        assertNotNull(moduleFileAST);

        mainProgram = mainFileAST.findFirstTokenOnLine(1).findNearestAncestor(ASTMainProgramNode.class);
        internalSubroutine = mainFileAST.findFirstTokenOnLine(23).findNearestAncestor(ASTSubroutineSubprogramNode.class);
        module = moduleFileAST.findFirstTokenOnLine(1).findNearestAncestor(ASTModuleNode.class);
        assertNotNull(mainProgram);
        assertNotNull(internalSubroutine);
        assertNotNull(module);
    }

    public void testAddDeclsToMainProgram() throws Exception
    {
        new FauxFortranRefactoring()
        {
            protected void runTest()
            {
                assertTrue(canAdd("something_new"));
                assertFalse(canAdd("assigned_local_variable"));
                assertFalse(canAdd("AsSiGnEd_LoCaL_vArIaBlE"));
                assertFalse(canAdd("unused_local_variable"));
                assertFalse(canAdd("local_variable_accessed_from_internal_subroutine"));
                assertFalse(canAdd("defined_external_subroutine"));
                assertFalse(canAdd("declared_external_subroutine"));
                assertFalse(canAdd("undeclared_external_subroutine"));
                assertFalse(canAdd("internal_subroutine"));
                assertFalse(canAdd("used_module_subroutine"));
                assertFalse(canAdd("unused_module_subroutine"));
                assertTrue(canAdd("private_module_subroutine"));
            }

            private boolean canAdd(String name)
            {
                return checkIfDeclarationCanBeAddedToScope(name, mainProgram, new NullProgressMonitor());
            }
        }.runTest();
    }

    public void testAddDeclsToModule() throws Exception
    {
        new FauxFortranRefactoring()
        {
            protected void runTest()
            {
                assertFalse(canAdd("private_module_subroutine"));

                // These are the same as above, since declaring these in the module
                // will cause them to be imported into the main program, preventing
                // the main program from redeclaring them

                assertTrue(canAdd("something_new"));
                assertFalse(canAdd("assigned_local_variable"));
                assertFalse(canAdd("AsSiGnEd_LoCaL_vArIaBlE"));
                assertFalse(canAdd("unused_local_variable"));
                assertFalse(canAdd("local_variable_accessed_from_internal_subroutine"));
                // FIXME assertFalse(canAdd("defined_external_subroutine"));
                assertFalse(canAdd("declared_external_subroutine"));
                assertFalse(canAdd("undeclared_external_subroutine"));
                assertFalse(canAdd("internal_subroutine"));
                assertFalse(canAdd("used_module_subroutine"));
                assertFalse(canAdd("unused_module_subroutine"));
            }

            private boolean canAdd(String name)
            {
                return checkIfDeclarationCanBeAddedToScope(name, module, new NullProgressMonitor());
            }
        }.runTest();
    }

    public void testAddDeclsToInternalSubprogram() throws Exception
    {
        new FauxFortranRefactoring()
        {
            protected void runTest()
            {
                assertTrue(canAdd("something_new"));
                assertFalse(canAdd("internal_subroutine"));
                assertFalse(canAdd("local_variable_accessed_from_internal_subroutine"));
                assertTrue(canAdd("private_module_subroutine"));

                // These will shadow declarations in an outer scope, but since they're not
                // referenced, that is OK
                assertTrue(canAdd("assigned_local_variable"));
                assertTrue(canAdd("AsSiGnEd_LoCaL_vArIaBlE"));
                assertTrue(canAdd("unused_local_variable"));
                assertTrue(canAdd("defined_external_subroutine"));
                assertTrue(canAdd("declared_external_subroutine"));
                assertTrue(canAdd("undeclared_external_subroutine"));
                assertTrue(canAdd("used_module_subroutine"));
                assertTrue(canAdd("unused_module_subroutine"));
            }

            private boolean canAdd(String name)
            {
                return checkIfDeclarationCanBeAddedToScope(name, internalSubroutine, new NullProgressMonitor());
            }
        }.runTest();
    }

    /**
     * This is intended to be subclassed in order to gain access to the protected methods of
     * {@link AbstractFortranRefactoring} to test those methods.
     *
     * @author Jeff Overbey
     */
    private class FauxFortranRefactoring extends AbstractFortranRefactoring
    {
        public FauxFortranRefactoring()
        {
            this.vpg = PhotranVPG.getInstance();
        }

        @Override
        protected void doCheckInitialConditions(RefactoringStatus status, IProgressMonitor pm)
            throws PreconditionFailure
        {
        }

        @Override
        protected void doCheckFinalConditions(RefactoringStatus status, IProgressMonitor pm)
            throws PreconditionFailure
        {
        }

        @Override
        protected void doCreateChange(IProgressMonitor pm) throws CoreException, OperationCanceledException
        {
        }

        @Override
        protected void ensureProjectHasRefactoringEnabled(RefactoringStatus status) throws PreconditionFailure
        {
        }

        @Override
        protected RefactoringStatus getAbstractSyntaxTree(RefactoringStatus status)
        {
            return null;
        }

        @Override
        public String getName()
        {
            return null;
        }
    }
}
