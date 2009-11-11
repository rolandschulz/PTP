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
package org.eclipse.photran.internal.tests.refactoring.infrastructure;

import org.eclipse.photran.internal.core.parser.ASTExecutableProgramNode;
import org.eclipse.photran.internal.core.parser.ASTMainProgramNode;
import org.eclipse.photran.internal.core.parser.IInternalSubprogram;
import org.eclipse.photran.internal.core.parser.Parser.IASTListNode;
import org.eclipse.photran.internal.tests.AbstractSourceEditorTestCase;

public class TestRemove extends AbstractSourceEditorTestCase
{
    public void testRemoveQ() throws Exception
    {
        ASTExecutableProgramNode ast = load("hello-1-initial.f90");
        IASTListNode<IInternalSubprogram> subprograms = ((ASTMainProgramNode)ast.getProgramUnitList().get(0)).getInternalSubprograms();
        subprograms.remove(0);
        assertEquals(load("hello-4-q-cut.f90"), ast);
    }
}
