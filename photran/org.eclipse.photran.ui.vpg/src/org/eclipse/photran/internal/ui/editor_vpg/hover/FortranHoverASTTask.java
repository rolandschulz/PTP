/*******************************************************************************
 * Copyright (c) 2008 University of Illinois at Urbana-Champaign and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    UIUC - Initial API and implementation
 *******************************************************************************/
package org.eclipse.photran.internal.ui.editor_vpg.hover;

import org.eclipse.photran.internal.core.analysis.binding.Definition;
import org.eclipse.photran.internal.core.lexer.TokenList;
import org.eclipse.photran.internal.core.parser.ASTExecutableProgramNode;
import org.eclipse.photran.internal.ui.editor_vpg.DefinitionMap;
import org.eclipse.photran.internal.ui.editor_vpg.IFortranEditorASTTask;

/**
 * This class gets AST, TokenList, and DefinitionMap from the Photran framework.
 * These informations can be used to make hovertip.
 * The method, 'handle' will pass them on to org.eclipse.photran.internal.ui.text.hover.FortranDeclarationHover. 
 * @author Jungyoon Lee, Kun Koh, Nam Kim, David Weiner
 */
public class FortranHoverASTTask implements IFortranEditorASTTask
{
    private FortranDeclarationHover fortranDeclarationHover;
    private boolean hoverTipEnabled;
    
    public FortranHoverASTTask(FortranDeclarationHover fortranDeclarationHover, boolean hoverTipEnabled)
    {
        this.fortranDeclarationHover = fortranDeclarationHover;
        this.hoverTipEnabled = hoverTipEnabled;
    }
    
    /* (non-Javadoc)
     * @see org.eclipse.photran.internal.ui.editor_vpg.IFortranEditorASTTask#handle(org.eclipse.photran.internal.core.parser.ASTExecutableProgramNode, org.eclipse.photran.internal.core.lexer.TokenList, org.eclipse.photran.internal.ui.editor_vpg.DefinitionMap)
     */
    public boolean handle(ASTExecutableProgramNode ast, TokenList tokenList,
        DefinitionMap<Definition> defMap)
    {
        if (hoverTipEnabled)
        {
            fortranDeclarationHover.setTokenList(tokenList);
            fortranDeclarationHover.setDefinitionMap(defMap);
        }
            
        
        return true;
    }
}
