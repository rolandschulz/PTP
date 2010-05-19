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

import java.util.ArrayList;

import org.eclipse.photran.internal.core.analysis.binding.Definition;
import org.eclipse.photran.internal.core.lexer.Token;
import org.eclipse.photran.internal.core.lexer.TokenList;
import org.eclipse.photran.internal.core.parser.ASTExecutableProgramNode;
import org.eclipse.photran.internal.core.util.IterableWrapper;
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
    public boolean handle(ASTExecutableProgramNode ast,
                          TokenList tokenList,
                          DefinitionMap<Definition> defMap)
    {
        if (defMap == null) return true;
        
        final ArrayList<String> scopes = new ArrayList<String>();
        try
        {
            int lastLine = 0;
            for (Token token : new IterableWrapper<Token>(tokenList))
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
            
            synchronized (fortranCompletionProcessor)
            {
                fortranCompletionProcessor.scopes = scopes;
            }
        }
        catch (Throwable e)
        {
            // Ignore
        }
        return true;
    }
}