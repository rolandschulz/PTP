package org.eclipse.photran.internal.ui.editor_vpg;

import java.util.List;

import org.eclipse.core.resources.IFile;
import org.eclipse.photran.core.IFortranAST;
import org.eclipse.photran.internal.core.analysis.binding.Definition;
import org.eclipse.photran.internal.core.lexer.Terminal;
import org.eclipse.photran.internal.core.lexer.Token;

final class SampleEditorVPGTask implements IEditorVPGTask
{
    SampleEditorVPGTask(ExperimentalFreeFormFortranEditor freeFormVPGEditor)
    {
    }

    public void handle(IFile file, IFortranAST ast)
    {
        // Sample Action: Highlight all identifiers (and destroy most of the rest of the highlighting)
        // THIS IS ONLY AN EXAMPLE.  Try it, and notice how incredibly broken it is.
        
        for (Token t : ast)
        {
            if (t.getTerminal() != Terminal.T_IDENT) continue;
            
            List<Definition> bindings = t.resolveBinding();
            if (bindings.isEmpty()) continue;
            
            System.out.println(bindings.get(0));
        }
    }
}