package org.eclipse.photran.internal.ui.editor_vpg.contentassist;

import java.util.ArrayList;

import org.eclipse.photran.core.IFortranAST;
import org.eclipse.photran.internal.core.lexer.Token;
import org.eclipse.photran.internal.ui.editor_vpg.DefinitionMap;
import org.eclipse.photran.internal.ui.editor_vpg.IFortranEditorASTTask;

final class FortranCompletionProcessorASTTask implements IFortranEditorASTTask
{
    private final FortranCompletionProcessor fortranCompletionProcessor;
    
    FortranCompletionProcessorASTTask(FortranCompletionProcessor fortranCompletionProcessor)
    {
        this.fortranCompletionProcessor = fortranCompletionProcessor;
    }
    
    // Will be run <i>outside</i> the UI thread
    public void handle(IFortranAST ast)
    {
        synchronized (fortranCompletionProcessor)
        {
            final ArrayList<String> scopes = fortranCompletionProcessor.scopes;
            scopes.clear();
            try
            {
                int lastLine = 0;
                for (Token token : ast)
                {
                    int line = token.getLine();
                    if (line > lastLine)
                    {
                        scopes.ensureCapacity(line);
                        String qualifier = DefinitionMap.getQualifier(token.getEnclosingScope());
                        while (scopes.size() < line)
                            scopes.add(qualifier);
                        lastLine = line;
                    }
                }
            }
            catch (Throwable e)
            {
                // Ignore
            }
        }
    }
}