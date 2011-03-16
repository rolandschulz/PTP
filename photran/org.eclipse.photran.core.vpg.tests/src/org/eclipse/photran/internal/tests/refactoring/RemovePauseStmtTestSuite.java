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

import org.eclipse.photran.internal.core.refactoring.RemovePauseStmtRefactoring;
import org.eclipse.photran.internal.tests.Activator;
import org.eclipse.photran.internal.tests.PhotranRefactoringTestSuiteFromMarkers;

/**
 * Unit tests for the Remove Pause Statement Refactoring.
 * 
 * @author Rita Chow (chow15), Jerry Hsiao (jhsiao2), Mark Mozolewski (mozolews), Chamil Wijenayaka
 *         (wijenay2), Nicola Hall (nfhall2)
 */
public class RemovePauseStmtTestSuite extends
    PhotranRefactoringTestSuiteFromMarkers<RemovePauseStmtRefactoring>
{
    private static final String DIR = "refactoring-test-code/remove-pause-stmt";

    public static Test suite() throws Exception
    {
        return new RemovePauseStmtTestSuite();
    }

    public RemovePauseStmtTestSuite() throws Exception
    {
        super(Activator.getDefault(), "Running Remove Pause Stmt Refactoring in", DIR,
            RemovePauseStmtRefactoring.class);
    }

}
