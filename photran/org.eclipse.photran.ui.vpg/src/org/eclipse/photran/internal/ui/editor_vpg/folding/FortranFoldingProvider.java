/*******************************************************************************
 * Copyright (c) 2009 University of Illinois at Urbana-Champaign and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    UIUC - Initial API and implementation
 *******************************************************************************/
package org.eclipse.photran.internal.ui.editor_vpg.folding;

import java.util.ArrayList;

import org.eclipse.jface.text.Position;
import org.eclipse.photran.internal.core.analysis.binding.Definition;
import org.eclipse.photran.internal.core.analysis.binding.ScopingNode;
import org.eclipse.photran.internal.core.lexer.Token;
import org.eclipse.photran.internal.core.lexer.TokenList;
import org.eclipse.photran.internal.core.parser.ASTExecutableProgramNode;
import org.eclipse.photran.internal.core.parser.GenericASTVisitor;
import org.eclipse.photran.internal.core.parser.IASTNode;
import org.eclipse.photran.internal.ui.editor.FortranEditor;
import org.eclipse.photran.internal.ui.editor_vpg.DefinitionMap;
import org.eclipse.photran.internal.ui.editor_vpg.FortranEditorTasks;
import org.eclipse.photran.internal.ui.editor_vpg.FortranVPGSourceViewerConfigurationFactory;
import org.eclipse.photran.internal.ui.editor_vpg.IFortranEditorASTTask;
import org.eclipse.swt.widgets.Display;

/**
 * Provides folding structure for the Fortran editor (i.e., its ability to collapse functions, modules, etc.).
 * <p>
 * This class implements {@link IFortranEditorASTTask} and is installed by
 * {@link FortranVPGSourceViewerConfigurationFactory#create(org.eclipse.photran.internal.ui.editor.FortranEditor)}
 * 
 * @author Jeff Overbey
 * @author Kurt Hendle
 */
public class FortranFoldingProvider implements IFortranEditorASTTask
{
    protected FortranEditor editor = null;

    public void setup(FortranEditor editor)
    {
        this.editor = editor;
        FortranEditorTasks.instance(editor).addASTTask(this);
    }
    
    public boolean handle(ASTExecutableProgramNode ast, TokenList tokenList, DefinitionMap<Definition> defMap)
    {
        if (editor == null) return false;
        
        final FoldingVisitor visitor = new FoldingVisitor();
        ast.accept(visitor);
        
        Display.getDefault().asyncExec(new Runnable()
        {
            public void run()
            {
                editor.updateFoldingStructure(visitor.positions);
            }
        });
        
        return true;
    }
    
    private static class FoldingVisitor extends GenericASTVisitor
    {
        final ArrayList<Position> positions = new ArrayList<Position>();

        @Override
        public void visitASTNode(IASTNode node)
        {
            if (node instanceof ScopingNode && !(node instanceof ASTExecutableProgramNode))
                fold(node);

            traverseChildren(node);
        }

        private void fold(IASTNode node)
        {
            Token firstToken = node.findFirstToken();
            Token lastToken = node.findLastToken();
            if (firstToken == null || lastToken == null) return;
            
            int offset = firstToken.getFileOffset();
            int length = (lastToken.getFileOffset() + lastToken.getLength() - firstToken.getFileOffset());
            
            positions.add(new Position(offset, length));            
        }
    }
}
