/*******************************************************************************
 * Copyright (c) 2010 University of Illinois at Urbana-Champaign and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     UIUC - Initial API and implementation
 *******************************************************************************/
package org.eclipse.photran.internal.ui.actions;

import java.io.IOException;
import java.io.StringReader;
import java.lang.reflect.InvocationTargetException;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.ITextSelection;
import org.eclipse.photran.core.IFortranAST;
import org.eclipse.photran.internal.core.FortranAST;
import org.eclipse.photran.internal.core.SyntaxException;
import org.eclipse.photran.internal.core.lexer.ASTLexerFactory;
import org.eclipse.photran.internal.core.lexer.IAccumulatingLexer;
import org.eclipse.photran.internal.core.lexer.LexerException;
import org.eclipse.photran.internal.core.lexer.TokenList;
import org.eclipse.photran.internal.core.parser.ASTExecutableProgramNode;
import org.eclipse.photran.internal.core.parser.Parser;
import org.eclipse.photran.internal.core.refactoring.infrastructure.SourcePrinter;
import org.eclipse.photran.internal.core.reindenter.Reindenter;
import org.eclipse.photran.internal.core.reindenter.Reindenter.Strategy;
import org.eclipse.photran.internal.core.sourceform.SourceForm;

/**
 * The Correct Indentation action in the Fortran editor's context menu.
 * 
 * @author Esfar Huq
 * @author Rui Wang
 * @author Jeff Overbey
 */
public class CorrectIndentationAction extends FortranEditorASTActionDelegate
{
    public void run(IProgressMonitor progressMonitor) throws InvocationTargetException, InterruptedException
    {
        try
        {
            if (ensureNotFixedForm())
            {
                ITextSelection selectedRegion = getFortranEditor().getSelection();
                String sourceCode = reindent(selectedRegion);
                setTextInEditor(sourceCode);
                setEditorSelectionTo(selectedRegion, sourceCode);
            }
        }
        catch (Exception e)
        {
            String message = e.getMessage();
            if (message == null) message = e.getClass().getName();
            MessageDialog.openError(getFortranEditor().getShell(), "Error", message); //$NON-NLS-1$
        }
    }

    /** @return true iff the file in the editor has free source form */
    private boolean ensureNotFixedForm()
    {
        IFile ifile = getFortranEditor().getIFile();
        if (ifile != null && SourceForm.of(ifile).isFixedForm())
        {
            MessageDialog.openError(getFortranEditor().getShell(), "Error", //$NON-NLS-1$
                Messages.CorrectIndentationAction_NotAvailableForFixedForm);
            return false;
        }
        else return true;
    }

    private String reindent(ITextSelection selection) throws IOException, LexerException, SyntaxException
    {
        IFortranAST ast = parseTextInEditor();

        if (selection.getLength() == 0)
            Reindenter.reindent(ast.getRoot(), ast, Strategy.REINDENT_EACH_LINE);
        else
            Reindenter.reindent(selection.getStartLine()+1, selection.getEndLine()+1, ast, Strategy.REINDENT_EACH_LINE);

        return SourcePrinter.getSourceCodeFromAST(ast);
    }

    private IFortranAST parseTextInEditor() throws IOException, LexerException, SyntaxException
    {
        IFile ifile = getFortranEditor().getIFile();
        String filename = ifile != null ? ifile.getName() : "new_file.f90"; //$NON-NLS-1$
        IAccumulatingLexer lexer = new ASTLexerFactory().createLexer(
            new StringReader(getDocument().get()),
            ifile,
            filename,
            SourceForm.of(ifile));
        ASTExecutableProgramNode astRoot = new Parser().parse(lexer);
        return new FortranAST(ifile, astRoot, new TokenList(astRoot));
    }

    private IDocument getDocument()
    {
        return getFortranEditor().getDocumentProvider().getDocument(getFortranEditor().getEditorInput());
    }

    private void setTextInEditor(String sourceCode)
    {
        getDocument().set(sourceCode);
    }

    private void setEditorSelectionTo(ITextSelection selection, String sourceCode)
    {
//        int newStartOffset = Math.min(selection.getOffset(), sourceCode.length()-1);
//        int newEndOffset = Math.min(newStartOffset+selection.getLength(), sourceCode.length()-1);
//        getFortranEditor().selectAndReveal(newStartOffset, newEndOffset-newStartOffset);

        int newOffset = Math.min(selection.getOffset(), sourceCode.length()-1);
        getFortranEditor().selectAndReveal(newOffset, 0);
    }
}
