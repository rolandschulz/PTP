/*******************************************************************************
 * Copyright (c) 2008 University of Illinois at Urbana-Champaign and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     UIUC - Initial API and implementation
 *******************************************************************************/
package org.eclipse.photran.internal.core.tests;

import java.io.File;

import org.eclipse.photran.internal.core.analysis.loops.LoopReplacer;
import org.eclipse.photran.internal.core.parser.ASTExecutableProgramNode;

public class LoopReplacerSRTestCase extends SourceReproductionTestCase
{
    private long elapsedTime = 0L;
    
    public LoopReplacerSRTestCase(File file, boolean isFixedForm, String testCaseDescription)
    {
        super(file, isFixedForm, testCaseDescription);
    }

    @Override protected void transform(ASTExecutableProgramNode ast)
    {
        long start = System.currentTimeMillis();
        
        LoopReplacer.replaceAllLoopsIn(ast);
        
        elapsedTime = System.currentTimeMillis() - start;
    }
    
    public void testPerformance()
    {
        assertTrue("Loop replacer must complete in less than 5 seconds ("
            + (this.file == null ? "???" : this.file.getName())
            + " took "
            + ((elapsedTime/1000) + (elapsedTime%1000 > 500 ? 1 : 0))
            + " seconds)",
            elapsedTime < 5000);
    }

    public LoopReplacerSRTestCase() { super(null, false, ""); } // to keep JUnit quiet
}
