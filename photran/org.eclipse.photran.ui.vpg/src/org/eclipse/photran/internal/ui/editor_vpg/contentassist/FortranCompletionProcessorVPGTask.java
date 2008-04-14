package org.eclipse.photran.internal.ui.editor_vpg.contentassist;

import java.util.HashMap;
import java.util.TreeSet;

import org.eclipse.core.resources.IFile;
import org.eclipse.photran.core.IFortranAST;
import org.eclipse.photran.internal.core.analysis.binding.Definition;
import org.eclipse.photran.internal.core.analysis.binding.ScopingNode;
import org.eclipse.photran.internal.core.parser.GenericParseTreeVisitor;
import org.eclipse.photran.internal.core.parser.Parser.InteriorNode;
import org.eclipse.photran.internal.ui.editor_vpg.DefinitionMap;
import org.eclipse.photran.internal.ui.editor_vpg.IFortranEditorVPGTask;

final class FortranCompletionProcessorVPGTask implements IFortranEditorVPGTask
{
    private final FortranCompletionProcessor fortranCompletionProcessor;
    
    FortranCompletionProcessorVPGTask(FortranCompletionProcessor fortranCompletionProcessor)
    {
        this.fortranCompletionProcessor = fortranCompletionProcessor;
    }
    
    // IFortranEditorVPGTask - Will be run <i>outside</i> the UI thread
    public void handle(IFile file, IFortranAST ast, DefinitionMap<Definition> defMap)
    {
        synchronized (fortranCompletionProcessor)
        {
            try
            {
                final HashMap<String, TreeSet<Definition>> defs = fortranCompletionProcessor.defs;
                defs.clear();
                ast.visitUsing(new GenericParseTreeVisitor()
                {
                    @Override public void visitParseTreeNode(InteriorNode node)
                    {
                        if (ScopingNode.isScopingNode(node))
                        {
                            ScopingNode n = (ScopingNode)node;
                            String qualifier = DefinitionMap.getQualifier(n);
                            if (!defs.containsKey(qualifier))
                                defs.put(qualifier, new TreeSet<Definition>());
                            defs.get(qualifier).addAll(n.getAllDefinitions());
                        }
                    }
                });
            }
            catch (Throwable e)
            {
                // Ignore
            }
        }
    }
}