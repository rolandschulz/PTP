/*******************************************************************************
 * Copyright (c) 2000, 2005 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.fdt.refactoring;

import java.util.ArrayList;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.filebuffers.FileBuffers;
import org.eclipse.core.filebuffers.ITextFileBuffer;
import org.eclipse.core.filebuffers.ITextFileBufferManager;
import org.eclipse.core.resources.IFile;
import org.eclipse.jface.text.BadLocationException;
import org.eclipse.jface.text.IDocument;
import org.eclipse.photran.internal.core.f95refactoringparser.ILexer;
import org.eclipse.photran.internal.core.f95refactoringparser.Terminal;
import org.eclipse.photran.internal.core.f95refactoringparser.Token;


/**
 * Helper class for text file changes.
 * getDocument and getTextBuffer copied from org.eclipse.ltk.internal.core.refactoring.
 */
public class TextChanges {
	private int fPrevLine = -1;
	private int fExtraColumns = 0;

	private IProgressMonitor fMonitor;
	private IFile fFile;
	private ITextFileBuffer fTextBuffer;
	private IDocument fDocument;
	private final boolean fDidConnect;
	
	public static IDocument getDocument(IFile file) throws CoreException {
		ITextFileBufferManager manager= FileBuffers.getTextFileBufferManager();
		IPath path= file.getFullPath();
		ITextFileBuffer buffer= manager.getTextFileBuffer(path);
		if (buffer == null)
			return null;
		return buffer.getDocument();
	}

	
	public static ITextFileBuffer getTextBuffer(IFile file) throws CoreException {
		ITextFileBufferManager manager= FileBuffers.getTextFileBufferManager();
		IPath path= file.getFullPath();
		ITextFileBuffer buffer= manager.getTextFileBuffer(path);
		return buffer;
	}


	public static String[] changeElements(String changeRepresentation) {
		return changeRepresentation.split(":");
	}
	

	public static String text(String[] elements) {
		return elements[3].substring(8, elements[3].length() - 1);
	}

	
	public static int line(String[] elements) {
		return Integer.decode(elements[0]).intValue() - 1;
	}
	
	
	public static int column(String[] elements) {
		return Integer.decode(elements[1]).intValue() - 1;

	}
	
	
	public static int length(String[] elements) {
		return Integer.decode(elements[2]).intValue() - TextChanges.column(elements);
	}

	
	public static String replacement(String constant)
	{
		String str;
		if (constant.indexOf('e') > -1) {
			str = constant.replace('e', 'D');
		} else if (constant.indexOf('E') > -1) {
			str = constant.replace('E', 'D');
		} else {
			str = constant + "D0";
		}
		return str;
	}
	
	
	/*
	 * Create an array of strings representing location and text of real constants
	 * Representation is "line:start_column:end_column: text = REAL_CONSTANT"
	 */
    public static String[] processConstants(ILexer scanner)
    {
    	final ArrayList /*<String>*/ sa = new ArrayList();
    	final StringBuffer sb = new StringBuffer();
    	
    	try {
    		Token thisToken = scanner.yylex();
    		while (thisToken.getTerminal() != Terminal.END_OF_INPUT) {
    			Token nextToken = scanner.yylex();
   		 		if (thisToken.getTerminal() == Terminal.T_RCON) {
   		 			// TODO Ignore (for now) explicit kind specification using underscore, e.g., 3.0_4
   		 			if (nextToken.getTerminal() != Terminal.T_UNDERSCORE) {
   		 				sb.replace(0, sb.length(), thisToken.getStartLine() + ":" + thisToken.getStartCol() + ":" + thisToken.getEndCol());
   		 				sb.append(": text = " + thisToken.getText() + "\n");
   		 				sa.add(sb.toString());
   		 			}
   		 		}
   		 		thisToken = nextToken;
    		}
    	} catch (Exception e) {
    		return new String[0];
    	}
        
        String[] s = new String[sa.size()];
        for (int i = 0; i < sa.size(); i++) {
        	  s[i] = (String) sa.get(i);
        }
        return s;
    }
    

	public TextChanges(IProgressMonitor monitor, IFile file) throws CoreException {
		fMonitor = monitor;
		fFile = file;
		
		ITextFileBufferManager manager= FileBuffers.getTextFileBufferManager();
		IPath path= file.getFullPath();
		fTextBuffer = manager.getTextFileBuffer(path);
		
		if (fTextBuffer == null) {
			manager.connect(path, fMonitor);
			fTextBuffer = manager.getTextFileBuffer(path);
			fDidConnect = true;
		} else {
			fDidConnect = false;
		}
		fDocument = fTextBuffer.getDocument();
	}


	public void apply(String changeRepresentation) throws BadLocationException {
		String[] elements = TextChanges.changeElements(changeRepresentation);
		
		int line = TextChanges.line(elements);
		String text = TextChanges.text(elements);
		String replacement = TextChanges.replacement(text);
		
		int column = Integer.decode(elements[1]).intValue() - 1;
		int length = Integer.decode(elements[2]).intValue() - column;
		
		if (fPrevLine < line) {
			fPrevLine = line;
			fExtraColumns = 0;
		}
		
		column += fExtraColumns;
		int offset = column + fDocument.getLineOffset(line);
		fDocument.replace(offset, length, replacement);
		fExtraColumns += replacement.length() - length;
	}
	
	public void commit() throws CoreException {
	   	fTextBuffer.commit(fMonitor, true);
	   	if (fDidConnect) {
			ITextFileBufferManager fManager= FileBuffers.getTextFileBufferManager();
			IPath path= fFile.getFullPath();
			fManager.disconnect(path, fMonitor);
	   	}
	}
	
}
