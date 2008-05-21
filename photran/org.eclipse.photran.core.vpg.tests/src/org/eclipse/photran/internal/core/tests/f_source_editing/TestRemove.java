package org.eclipse.photran.internal.core.tests.f_source_editing;

import org.eclipse.photran.internal.core.parser.ASTExecutableProgramNode;
import org.eclipse.photran.internal.core.parser.ASTMainProgramNode;
import org.eclipse.photran.internal.core.parser.IInternalSubprogram;
import org.eclipse.photran.internal.core.parser.Parser.IASTListNode;
import org.eclipse.photran.internal.core.tests.AbstractSourceEditorTestCase;

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
