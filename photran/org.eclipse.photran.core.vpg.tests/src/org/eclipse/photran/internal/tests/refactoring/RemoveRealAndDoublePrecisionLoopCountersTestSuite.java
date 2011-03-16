/*******************************************************************************
 * Copyright (c) 2010 Rita Chow, Nicola Hall, Jerry Hsiao, Mark Mozolewski, Chamil Wijenayaka
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Rita Chow - Initial Implementation
 *    Nicola Hall - Initial Implementation
 *    Jerry Hsiao - Initial Implementation
 *    Mark Mozolewski - Initial Implementation
 *    Chamil Wijenayaka - Initial Implementation
 *******************************************************************************/
package org.eclipse.photran.internal.tests.refactoring;

import junit.framework.Test;

import org.eclipse.core.resources.IFile;
import org.eclipse.jface.text.TextSelection;
import org.eclipse.photran.internal.core.refactoring.RemoveRealAndDoublePrecisionLoopCountersRefactoring;
import org.eclipse.photran.internal.tests.Activator;
import org.eclipse.photran.internal.tests.PhotranRefactoringTestSuiteFromMarkers;

/**
 * Unit tests for the Remove Branch To End If Refactoring.
 * 
 * @author Rita Chow (chow15), Jerry Hsiao (jhsiao2), Mark Mozolewski (mozolews), Chamil Wijenayaka
 *         (wijenay2), Nicola Hall (nfhall2)
 */
public class RemoveRealAndDoublePrecisionLoopCountersTestSuite extends
    PhotranRefactoringTestSuiteFromMarkers<RemoveRealAndDoublePrecisionLoopCountersRefactoring>
{
    private static final String DIR = "refactoring-test-code/remove-real-and-double-precision-loop-counters";

    public static Test suite() throws Exception
    {
        return new RemoveRealAndDoublePrecisionLoopCountersTestSuite();
    }

    public RemoveRealAndDoublePrecisionLoopCountersTestSuite() throws Exception
    {
        super(Activator.getDefault(),
            "Running Remove Real and Double Precision Loop Counters Refactoring in", DIR,
            RemoveRealAndDoublePrecisionLoopCountersRefactoring.class);
    }

    @Override
    protected boolean configureRefactoring(
        RemoveRealAndDoublePrecisionLoopCountersRefactoring refactoring, IFile file,
        TextSelection selection, String[] markerText)
    {
        boolean shouldSucceed = super
            .configureRefactoring(refactoring, file, selection, markerText);

        boolean shouldReplaceWithDoWhileLoop;
        if (markerText[4].equals("1"))
            shouldReplaceWithDoWhileLoop = true;
        else
            shouldReplaceWithDoWhileLoop = false;
        refactoring.setShouldReplaceWithDoWhileLoop(shouldReplaceWithDoWhileLoop);

        return shouldSucceed;
    }
}
