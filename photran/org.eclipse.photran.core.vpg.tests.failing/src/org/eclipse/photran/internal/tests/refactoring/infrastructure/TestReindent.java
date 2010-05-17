package org.eclipse.photran.internal.tests.refactoring.infrastructure;


public class TestReindent extends AbstractSourceEditorTestCase
{
    public void test() {}
    
//    public void testPasteQOutsideAndReindent() throws Exception
//    {
//        ASTExecutableProgramNode ast = load("hello-1-initial.f90");
//        
//        ASTSubroutineSubprogramNode q = ast.getASTProgramUnit(1).getASTMainProgram().getASTMainRange().getASTBodyPlusInternals().getASTInternalSubprogram(2).getASTSubroutineSubprogram();
//        SourceEditor.cut(q);
//        SourceEditor.pasteAsFirstChild(q, ast.getASTProgramUnit(0), ast, true);
//        assertEquals(load("hello-7-q-pasted-formatted.f90"), ast);
//    }
//
//    public void testPasteQAtBottomAndReindent() throws Exception
//    {
//        ASTExecutableProgramNode ast = load("hello-1-initial.f90");
//        
//        ASTSubroutineSubprogramNode q = ast.getASTProgramUnit(1).getASTMainProgram().getASTMainRange().getASTBodyPlusInternals().getASTInternalSubprogram(2).getASTSubroutineSubprogram();
//        SourceEditor.cut(q);
//        SourceEditor.pasteAsLastChild(q, ast.getASTProgramUnit(0), ast, true);
//        assertEquals(load("hello-8-q-pasted-at-bottom-formatted.f90"), ast);
//    }
//
//    public void testPasteTAboveQAndReindent() throws Exception
//    {
//        ASTExecutableProgramNode ast = load("hello-1-initial.f90");
//        
//        ASTSubroutineSubprogramNode t = ast.getASTProgramUnit(0).getASTSubroutineSubprogram();
//        ParseTreeNode internalSubprogram2 = ast.getASTProgramUnit(1).getASTMainProgram().getASTMainRange().getASTBodyPlusInternals().getASTInternalSubprogram(2);
//        SourceEditor.cut(t);
//        SourceEditor.pasteAsFirstChild(t, internalSubprogram2, ast, true);
//        assertEquals(load("hello-9-t-above-q.f90"), ast);
//    }
}
