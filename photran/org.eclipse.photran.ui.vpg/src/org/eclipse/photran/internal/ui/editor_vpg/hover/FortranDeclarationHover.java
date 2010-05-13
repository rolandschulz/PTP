/***************************************************************************************
 * Copyright (c) 2008 University of Illinois at Urbana-Champaign and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    UIUC - Initial API and implementation
 **************************************************************************************/
package org.eclipse.photran.internal.ui.editor_vpg.hover;

import org.eclipse.core.runtime.Assert;
import org.eclipse.jface.text.BadLocationException;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.IInformationControl;
import org.eclipse.jface.text.IInformationControlCreator;
import org.eclipse.jface.text.IRegion;
import org.eclipse.jface.text.ITextHover;
import org.eclipse.jface.text.ITextHoverExtension;
import org.eclipse.jface.text.ITextViewer;
import org.eclipse.jface.text.Region;
import org.eclipse.jface.text.TextSelection;
import org.eclipse.jface.text.source.ISourceViewer;
import org.eclipse.photran.internal.core.analysis.binding.Definition;
import org.eclipse.photran.internal.core.lexer.TokenList;
import org.eclipse.photran.internal.core.properties.SearchPathProperties;
import org.eclipse.photran.internal.ui.editor.FortranEditor;
import org.eclipse.photran.internal.ui.editor_vpg.DefinitionMap;
import org.eclipse.photran.internal.ui.editor_vpg.FortranEditorTasks;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.editors.text.EditorsUI;
import org.eclipse.ui.part.IWorkbenchPartOrientation;

/**
 * FortranDeclarationHover
 * It displays a tool-tip when a mouse is hovered over a identifier.
 * The content of it is identical to that of org.eclipse.photran.internal.ui.views.DeclarationView
 * @author Jungyoon Lee, Kun Koh, Nam Kim, David Weiner
 */
public class FortranDeclarationHover implements ITextHover, ITextHoverExtension
{
    private FortranEditor fEditor;
    private boolean hoverTipEnabled;

    private TokenList activeTokenList = null;
    private DefinitionMap<Definition> activeDefinitionMap = null;

    public void setTokenList(TokenList tokenList)
    {
        this.activeTokenList = tokenList;
    }    

    public void setDefinitionMap(DefinitionMap<Definition> defMap)
    {
        this.activeDefinitionMap = defMap;
    }
    

    /**
     * Constructor
     * @param sourceViewer
     * @param editor
     */
    public FortranDeclarationHover(ISourceViewer sourceViewer, FortranEditor editor)
    {
        Assert.isNotNull(sourceViewer);
        fEditor = editor;
        
        if (editor == null) return;

        hoverTipEnabled = new SearchPathProperties().getProperty(editor.getIFile(), 
            SearchPathProperties.ENABLE_HOVER_TIP_PROPERTY_NAME).equals("true");

        
        FortranEditorTasks instance = FortranEditorTasks.instance(editor);
        instance.addASTTask(new FortranHoverASTTask(this, hoverTipEnabled));
    }

    /* (non-Javadoc)
     * @see org.eclipse.jface.text.ITextHover#getHoverInfo(org.eclipse.jface.text.ITextViewer, org.eclipse.jface.text.IRegion)
     */
    public String getHoverInfo(ITextViewer textViewer, IRegion hoverRegion)
    {
        String str = "";
        if (activeTokenList != null && activeDefinitionMap != null)
        {
            try
            {
                TextSelection ts = new TextSelection(textViewer.getDocument(),
                    hoverRegion.getOffset(),
                    hoverRegion.getLength());
                Definition def = activeDefinitionMap.lookup(ts, activeTokenList);
                if (def != null) str = def.describe();
            }
            catch (Throwable t)
            {
                // Ignore
            }
        }

        return str;
    }

    /* (non-Javadoc)
     * @see org.eclipse.jface.text.ITextHover#getHoverRegion(org.eclipse.jface.text.ITextViewer, int)
     */
    public IRegion getHoverRegion(ITextViewer textViewer, int offset)
    {       
        if (hoverTipEnabled)
            return findWord(textViewer.getDocument(), offset);
        
        return null;
    }

    /* (non-Javadoc)
     * @see org.eclipse.jface.text.ITextHoverExtension#getHoverControlCreator()
     */
    public IInformationControlCreator getHoverControlCreator() 
    {
        return new IInformationControlCreator() {
            public IInformationControl createInformationControl(Shell parent) 
            {
                IEditorPart editor= fEditor;
                int orientation = SWT.NONE;
                if (editor instanceof IWorkbenchPartOrientation)
                    orientation = ((IWorkbenchPartOrientation) editor).getOrientation();
                SourceViewerInformationControl sv = new SourceViewerInformationControl(parent, false, orientation, EditorsUI.getTooltipAffordanceString());
                return sv;
            }
        };
    }


    /**
     * findWord
     * It calculates the start position and the end position of the word
     * on which the 'offset' is located. 
     * The main idea is borrowed from findWord in org.eclipse.jface.text.DefaultTextHover
     * @param document
     * @param offset
     * @return
     */
    private IRegion findWord(IDocument document, int offset) 
    {
        int start= -2;
        int end= -1;
        
        start = getOffsetOfWord(offset, document, -1);            
        end = getOffsetOfWord(offset, document, 1);

        // if a mouse is hovered over an invalid word
        if (start >= -1 && end > -1) 
        {
            // if hovered over an one-letter identifier
            if (start == offset && end == offset)
                return new Region(offset, 0);
            else if (start == offset)
                return new Region(start, end - start);
            else
                return new Region(start + 1, end - start - 1);
        }

        return null;
    }
    
    /**
     * getOffsetOfWord
     * @param current current offset of a mouse cursor from the beginning of a document
     * @param document document
     * @param direction '-1' if we need to get the left most position of a word, '1' otherwise
     * @param length length of a document
     * @return
     */
    private int getOffsetOfWord(int current, IDocument document, int direction)
    {
        char c;
        int length = document.getLength();
        while (0 <= current && current < length) 
        {
            try
            {
                c = document.getChar(current);
                // this condition is from FortranWordDetector class
                if (!(Character.isJavaIdentifierPart(c) || c == '.'))
                    break;
                current += direction;
            }
            catch (BadLocationException e)
            {
                e.printStackTrace();
            }
        }
        return current;
    }
}
