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
import org.eclipse.core.filebuffers.FileBuffers;
import org.eclipse.core.filebuffers.ITextFileBuffer;
import org.eclipse.core.filebuffers.ITextFileBufferManager;
import org.eclipse.core.resources.IFile;
import org.eclipse.jface.text.IDocument;
import org.eclipse.photran.internal.core.f95modelparser.ILexer;
import org.eclipse.photran.internal.core.f95modelparser.Terminal;
import org.eclipse.photran.internal.core.f95modelparser.Token;


/**
 * Helper class for text file changes.
 * getDocument and getTextBuffer copied from org.eclipse.ltk.internal.core.refactoring.
 */
public class TextChanges {
	private int fLine;
	private int fColumn;
	private int fLength;
	private String fText;
	
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
    

	public TextChanges(String changeRepresentation) {
		String[] elements = changeRepresentation.split(":");
		fLine = Integer.decode(elements[0]).intValue() - 1;
		fColumn = Integer.decode(elements[1]).intValue() - 1;
		fLength = Integer.decode(elements[2]).intValue() - fColumn;
		fText = elements[3].substring(8, elements[3].length() - 1);
	}

	
    public int line() {
    	return fLine;
    }

    public int column() {
    	return fColumn;
    }

    public int length() {
    	return fLength;
    }
    
    public String text() {
    	return fText;
    }
    
}
