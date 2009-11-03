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
package org.eclipse.photran.internal.core.tests.i_list_all;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.photran.internal.core.vpg.PhotranVPG;
import org.eclipse.photran.refactoring.tests.RefactoringTestCase;

/**
 * Unit tests for {@link PhotranVPG#listAllModules()}
 *
 * @author Jeff Overbey
 */
public class ListAllModulesTestCase extends RefactoringTestCase
{
    private static final String DIR = "vpg-list-test-code";

    @Override public void setUp() throws Exception
    {
        super.setUp();

        /* The files are imported in this order so that the USE
         * statement in use-module1.f90 is seen /before/
         * the module declaration, while the USE statement for
         * module 4 is seen after it.  Module2 and module3
         * are not used.
         */

        importFile(DIR, "use-module1.f90");
        importFile(DIR, "module1.f90");
        importFile(DIR, "module2-module3.f90");
        importFile(DIR, "module4.f90");
        importFile(DIR, "use-module4.f90");
        PhotranVPG.getInstance().ensureVPGIsUpToDate(new NullProgressMonitor());
        //PhotranVPG.getDatabase().printOn(System.out);
    }

    public void testListAllModules() throws Exception
    {
        assertCollectionsEqual(
            new String[] { "module1", "module2", "module3", "module4" },
            PhotranVPG.getInstance().listAllModules());
    }

    private <T> void assertCollectionsEqual(T[] expectedArray, Iterable<T> actualIterable)
    {
        List<T> expected = Arrays.asList(expectedArray);

        List<T> actual = new ArrayList<T>();
        for (T entry : actualIterable)
            actual.add(entry);

        assertEquals(expected, actual);
    }
}
