package org.eclipse.photran.internal.ui.editor_vpg.contentassist;

import java.util.HashMap;
import java.util.List;
import java.util.TreeSet;

import org.eclipse.core.resources.IFile;
import org.eclipse.photran.core.IFortranAST;
import org.eclipse.photran.internal.core.analysis.binding.Definition;
import org.eclipse.photran.internal.core.analysis.binding.ScopingNode;
import org.eclipse.photran.internal.core.parser.Parser.GenericASTVisitor;
import org.eclipse.photran.internal.core.parser.Parser.IASTNode;
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
                ast.accept(new GenericASTVisitor()
                {
                    @Override public void visitASTNode(IASTNode node)
                    {
                        if (ScopingNode.isScopingNode(node))
                        {
                            ScopingNode n = (ScopingNode)node;
                            String qualifier = DefinitionMap.getQualifier(n);
                            List<Definition> allDefs = n.getAllDefinitions();
                            
                            TreeSet<Definition> set = defs.get(qualifier);
                            if (set == null) set = new TreeSet<Definition>();
                            set.addAll(allDefs);
                            defs.put(qualifier, set);
                        }
                        
                        traverseChildren(node);
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