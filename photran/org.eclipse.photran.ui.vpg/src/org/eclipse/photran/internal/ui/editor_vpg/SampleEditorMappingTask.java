package org.eclipse.photran.internal.ui.editor_vpg;

import org.eclipse.core.resources.IFile;
import org.eclipse.jface.text.ITextSelection;
import org.eclipse.photran.core.IFortranAST;
import org.eclipse.photran.internal.core.analysis.binding.Definition;
import org.eclipse.photran.internal.core.lexer.Token;
import org.eclipse.photran.internal.ui.editor.AbstractFortranEditor;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.RGB;

final class SampleEditorMappingTask
{
    IEditorVPGTask vpgTask = new VPGTask();
    IEditorASTTask astTask = new ASTTask();
    
    private final AbstractFortranEditor freeFormVPGEditor;
    private DefinitionMap defMap = null;
    
    private Color LIGHT_YELLOW = new Color(null, new RGB(240, 240, 128));

    SampleEditorMappingTask(AbstractFortranEditor freeFormVPGEditor)
    {
        this.freeFormVPGEditor = freeFormVPGEditor;
    }

    private class VPGTask implements IEditorVPGTask
    {
        public void handle(IFile file, IFortranAST ast)
        {
            defMap = new DefinitionMap(ast);
        }
    }
    
    private class ASTTask implements IEditorASTTask
    {
        public void handle(final IFortranAST ast)
        {
            if (defMap == null) return;

            freeFormVPGEditor.getSite().getShell().getDisplay().syncExec(new Runnable()
            {
                public void run()
                {
                    ITextSelection sel = freeFormVPGEditor.getSelection();
                    if (sel == null) return;

                    Token current = null;
                    for (Token t : ast)
                    {
                        if (t.containsFileOffset(sel.getOffset()))
                        {
                            current = t;
                            break;
                        }
                    }
                    if (current == null) return;
                    
                    Definition def = defMap.lookup(current);
                    if (def != null)
                        System.err.println("The identifier under the cursor is bound to "
                                           + def.toString());
                    else
                        System.err.println("No definition found for the identifier under the cursor");
                }
            });
        }
    }
}