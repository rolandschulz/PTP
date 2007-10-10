package org.eclipse.photran.internal.ui.editor_vpg;

import org.eclipse.jface.text.TextPresentation;
import org.eclipse.jface.text.source.ISourceViewer;
import org.eclipse.photran.internal.core.lexer.Terminal;
import org.eclipse.photran.internal.core.lexer.Token;
import org.eclipse.photran.internal.core.parser.ASTExecutableProgramNode;
import org.eclipse.photran.internal.core.parser.ASTVisitor;
import org.eclipse.swt.custom.StyleRange;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.RGB;

final class SampleEditorASTTask implements IEditorASTTask
{
    private final ExperimentalFreeFormFortranEditor freeFormVPGEditor;
    private Color LIGHT_YELLOW = new Color(null, new RGB(240, 240, 128));

    SampleEditorASTTask(ExperimentalFreeFormFortranEditor freeFormVPGEditor)
    {
        this.freeFormVPGEditor = freeFormVPGEditor;
    }

    public void handle(ASTExecutableProgramNode astRootNode)
    {
        // Sample Action: Highlight all identifiers (and destroy most of the rest of the highlighting)
        
        final TextPresentation presentation = new TextPresentation();
        astRootNode.visitTopDownUsing(new ASTVisitor()
        {
            @Override public void visitToken(Token token)
            {
                if (token.getTerminal() == Terminal.T_IDENT)
                    presentation.addStyleRange(new StyleRange(token.getFileOffset(),
                                                              token.getLength(),
                                                              null,
                                                              LIGHT_YELLOW));
            }
        });
            
        this.freeFormVPGEditor.getSite().getShell().getDisplay().asyncExec(new Runnable()
        {
            public void run()
            {
                System.out.println("Updating");
                ISourceViewer sv = freeFormVPGEditor.getSourceViewerx();
                sv.changeTextPresentation(presentation, true);
            }
        });
    }
}