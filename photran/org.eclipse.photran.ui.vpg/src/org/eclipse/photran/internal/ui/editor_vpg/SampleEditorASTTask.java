package org.eclipse.photran.internal.ui.editor_vpg;

import org.eclipse.jface.text.TextPresentation;
import org.eclipse.photran.core.IFortranAST;
import org.eclipse.photran.internal.core.lexer.Terminal;
import org.eclipse.photran.internal.core.lexer.Token;
import org.eclipse.photran.internal.ui.editor.AbstractFortranEditor;
import org.eclipse.swt.custom.StyleRange;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.RGB;

final class SampleEditorASTTask implements IEditorASTTask
{
    private final AbstractFortranEditor freeFormVPGEditor;
    private Color LIGHT_YELLOW = new Color(null, new RGB(240, 240, 128));

    SampleEditorASTTask(AbstractFortranEditor freeFormVPGEditor)
    {
        this.freeFormVPGEditor = freeFormVPGEditor;
    }

    public void handle(IFortranAST ast)
    {
        // Sample Action: Highlight all identifiers (and destroy most of the rest of the highlighting)
        
        final TextPresentation presentation = new TextPresentation();
        for (Token token : ast)
        {
            if (token.getTerminal() == Terminal.T_IDENT)
                presentation.addStyleRange(new StyleRange(token.getFileOffset(),
                                                          token.getLength(),
                                                          null,
                                                          LIGHT_YELLOW));
        }
            
        this.freeFormVPGEditor.getSite().getShell().getDisplay().asyncExec(new Runnable()
        {
            public void run()
            {
                freeFormVPGEditor.getSourceViewerx().changeTextPresentation(presentation, true);
            }
        });
    }
}