package org.eclipse.photran.internal.core.tests.f_source_editing;

import org.eclipse.photran.internal.core.parser.ASTExecutableProgramNode;
import org.eclipse.photran.internal.core.parser.ASTSubroutineSubprogramNode;
import org.eclipse.photran.internal.core.refactoring.infrastructure.SourceEditor;
import org.eclipse.photran.internal.core.tests.AbstractSourceEditorTestCase;

public class TestRemove extends AbstractSourceEditorTestCase
{
    public void testRemoveQ() throws Exception
    {
        ASTExecutableProgramNode ast = load("hello-1-initial.f90");
        ASTSubroutineSubprogramNode q = ast.getProgramUnit(0).getMainProgram().getBodyPlusInternals().getInternalSubprogram(0).getSubroutineSubprogram();
        assertNotNull(SourceEditor.cut(q));
        assertEquals(load("hello-4-q-cut.f90"), ast);
    }
}
