/*******************************************************************************
 * Copyright (c) 2009 University of Illinois at Urbana-Champaign and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     UIUC - Initial API and implementation
 *******************************************************************************/
package org.eclipse.photran.internal.ui.editor_vpg.contentassist;

import java.util.HashMap;
import java.util.List;
import java.util.TreeSet;

import org.eclipse.core.resources.IFile;
import org.eclipse.photran.core.IFortranAST;
import org.eclipse.photran.internal.core.analysis.binding.Definition;
import org.eclipse.photran.internal.core.analysis.binding.ScopingNode;
import org.eclipse.photran.internal.core.parser.GenericASTVisitor;
import org.eclipse.photran.internal.core.parser.IASTNode;
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
        if (ast == null) return;
        
        try
        {
            final HashMap<String, TreeSet<Definition>> defs = new HashMap<String, TreeSet<Definition>>();
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
                        for (Definition def : allDefs)
                            if (def != null)
                                set.add(def);
                        defs.put(qualifier, set);
                    }
                    
                    traverseChildren(node);
                }
            });

            synchronized (fortranCompletionProcessor)
            {
                fortranCompletionProcessor.defs = defs;
            }
        }
        catch (Throwable e)
        {
            // Ignore
        }
    }
}