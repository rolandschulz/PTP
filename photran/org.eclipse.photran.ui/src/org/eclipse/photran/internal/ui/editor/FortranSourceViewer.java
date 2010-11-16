/*******************************************************************************
 * Copyright (c) 2006, 2009, 2010 QNX Software Systems and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     QNX Software Systems - initial API and implementation
 *     Sergey Prigogin, Google
 *     Anton Leherbauer (Wind River Systems)
 *     Markus Schorn (Wind River Systems)
 *     Jeff Overbey (UIUC) - Excerpted and modified CSourceViewer for Photran
 *******************************************************************************/
package org.eclipse.photran.internal.ui.editor;

import org.eclipse.cdt.internal.ui.editor.IndentUtil;
import org.eclipse.jface.text.BadLocationException;
import org.eclipse.jface.text.DocumentRewriteSession;
import org.eclipse.jface.text.DocumentRewriteSessionType;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.IDocumentExtension4;
import org.eclipse.jface.text.IRegion;
import org.eclipse.jface.text.source.IOverviewRuler;
import org.eclipse.jface.text.source.IVerticalRuler;
import org.eclipse.jface.text.source.projection.ProjectionViewer;
import org.eclipse.photran.internal.core.preferences.FortranPreferences;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.widgets.Composite;

/**
 * Custom source viewer that uses spaces, rather than tabs, to implement the Shift Left
 * and Shift Right actions if the corresponding workspace preference is enabled.
 * 
 * @author Jeff Overbey based on CSourceViewer (see attributions in copyright header)
 */
@SuppressWarnings("restriction")
public class FortranSourceViewer extends ProjectionViewer
{
    private static final int TAB_WIDTH = 4;
    
    public FortranSourceViewer(
        Composite parent,
        IVerticalRuler ruler,
        IOverviewRuler overviewRuler,
        boolean showsAnnotationOverview,
        int styles)
    {
        super(parent, ruler, overviewRuler, showsAnnotationOverview, styles);
    }

    @Override
    protected void shift(boolean useDefaultPrefixes, boolean right, boolean ignoreWhitespace) {
        if (!useDefaultPrefixes && FortranPreferences.CONVERT_TABS_TO_SPACES.getValue())
            adjustIndent(right, TAB_WIDTH, true);
        else
            super.shift(useDefaultPrefixes, right, ignoreWhitespace);
    }

    /**
     * Increase/decrease indentation of current selection.
     * 
     * @param increase  if <code>true</code>, indent is increased by one unit
     * @param shiftWidth  width in spaces of one indent unit
     * @param useSpaces  if <code>true</code>, only spaces are used for indentation
     */
    protected void adjustIndent(boolean increase, int shiftWidth, boolean useSpaces) {
        if (fUndoManager != null) {
            fUndoManager.beginCompoundChange();
        }
        IDocument d= getDocument();
        DocumentRewriteSession rewriteSession= null;
        try {
            if (d instanceof IDocumentExtension4) {
                IDocumentExtension4 extension= (IDocumentExtension4) d;
                rewriteSession= extension.startRewriteSession(DocumentRewriteSessionType.SEQUENTIAL);
            }

            Point selection= getSelectedRange();

            // perform the adjustment
            int tabWidth= getTextWidget().getTabs();
            int startLine= d.getLineOfOffset(selection.x);
            int endLine= selection.y == 0 ? startLine : d.getLineOfOffset(selection.x + selection.y - 1);
            for (int line= startLine; line <= endLine; ++line) {
                IRegion lineRegion= d.getLineInformation(line);
                String indent= IndentUtil.getCurrentIndent(d, line, false);
                int indentWidth= IndentUtil.computeVisualLength(indent, tabWidth);
                int newIndentWidth= Math.max(0, indentWidth + (increase ? shiftWidth : -shiftWidth));
                String newIndent= IndentUtil.changePrefix(indent.trim(), newIndentWidth, tabWidth, useSpaces);
                int commonLen= getCommonPrefixLength(indent, newIndent);
                if (commonLen < Math.max(indent.length(), newIndent.length())) {
                    if (commonLen > 0) {
                        indent= indent.substring(commonLen);
                        newIndent= newIndent.substring(commonLen);
                    }
                    final int offset= lineRegion.getOffset() + commonLen;
                    if (!increase && newIndent.length() > indent.length() && indent.length() > 0) {
                        d.replace(offset, indent.length(), ""); //$NON-NLS-1$
                        d.replace(offset, 0, newIndent);
                    } else {
                        d.replace(offset, indent.length(), newIndent);
                    }
                }
            }
            
        } catch (BadLocationException x) {
            // ignored
        } finally {
            if (rewriteSession != null) {
                ((IDocumentExtension4)d).stopRewriteSession(rewriteSession);
            }
            if (fUndoManager != null) {
                fUndoManager.endCompoundChange();
            }
        }
    }

    /**
     * Compute the length of the common prefix of two strings.
     * 
     * @param s1
     * @param s2
     * @return the length of the common prefix
     */
    private static int getCommonPrefixLength(String s1, String s2) {
        final int l1= s1.length();
        final int l2= s2.length();
        int i= 0;
        while (i < l1 && i < l2 && s1.charAt(i) == s2.charAt(i)) {
            ++i;
        }
        return i;
    }
}
