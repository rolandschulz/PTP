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
package org.eclipse.photran.internal.core.lexer;

import java.util.ArrayList;

import org.eclipse.core.resources.IFile;
import org.eclipse.photran.internal.core.preprocessor.c.CPreprocessor;
import org.eclipse.photran.internal.core.preprocessor.c.CppHelper;
import org.eclipse.photran.internal.core.preprocessor.c.IToken;

/**
 *
 *
 * @author Matthew Michelotti
 */
public class IncludeMap {

	private ArrayList<FileLoc> fileLocStack = new ArrayList<FileLoc>();
	private IToken curToken = null;
	private int imageOffset = 0;
	private int streamOffset = 0;

	public IncludeMap(IToken startToken) {
		this(startToken, null);
	}

	public IncludeMap(IToken startToken, IFile file) {
		fileLocStack.add(new FileLoc(file));
		changeCurToken(startToken);
		setStreamOffset(0);
	}

	public void setStreamOffset(int newStreamOffset) {
		int distance = newStreamOffset - streamOffset;
		if(distance < 0) throw new IllegalArgumentException(
				"newStreamOffset must be >= the previous stream offset");
		streamOffset = newStreamOffset;

		if(curToken == null) return;

		String tokenImage = CppHelper.getFullImage(curToken).substring(imageOffset);
		while(distance >= tokenImage.length()) {
			distance -= tokenImage.length();
			if(isTokenSourceCode(curToken)) passSourceCode(tokenImage);
			changeCurToken(curToken.getNext());
			imageOffset = 0;
			if(curToken == null) return;
			tokenImage = CppHelper.getFullImage(curToken);
		}

		if(isTokenSourceCode(curToken)) {
			passSourceCode(tokenImage.substring(0, distance));
		}
		imageOffset += distance;
	}

    public FileOrIFile getFileOrIFile() {
        return activeFileLoc().fileOrIFile;
    }

	public int getFileOffset() {
		return activeFileLoc().offset;
	}

	public int getLine() {
		return activeFileLoc().line;
	}

	public int getCol() {
		return activeFileLoc().col;
	}

	private void changeCurToken(IToken nextToken) {
		if(curToken != null &&
			curToken.getType() == CPreprocessor.tINCLUDED_FILE_END)
		{
			fileLocStack.remove(fileLocStack.size()-1);
		}

		FileLoc fileLoc = activeFileLoc();
		IToken oldProducer = getProducer(curToken, fileLoc.includeDir);
		IToken newProducer = getProducer(nextToken, fileLoc.includeDir);
		if(oldProducer != null && oldProducer != newProducer) {
			passSourceCode(CppHelper.getFullImage(oldProducer));
		}

		if(nextToken != null &&
			nextToken.getType() == CPreprocessor.tINCLUDED_FILE_START)
		{
			IToken includeDir = nextToken.getParent();
			while(includeDir.getIncludeFile() == null) {
				includeDir = includeDir.getParent();
			}
			fileLocStack.add(new FileLoc(includeDir));
		}

		curToken = nextToken;
	}

	private void passSourceCode(String code) {
		FileLoc fileLoc = activeFileLoc();
		int codeLength = code.length();
		fileLoc.offset += codeLength;
		for(int i = 0; i < codeLength; i++) {
			switch(code.charAt(i)) {
			case('\n'): fileLoc.line++; fileLoc.col = 1; break;
			default: fileLoc.col++; break;
			}
		}
	}

	private FileLoc activeFileLoc() {
		return fileLocStack.get(fileLocStack.size()-1);
	}

	private static IToken getProducer(IToken token, IToken blockingAncestor) {
		if(token == null) return null;

		IToken last = null;
		for(IToken p = token.getParent(); p != blockingAncestor; p = p.getParent())
		{
			last = p;
		}
		return last;
	}

	private static boolean isTokenSourceCode(IToken token) {
		IToken parent = token.getParent();
		return (parent == null || parent.getIncludeFile() != null);
	}

	private static class FileLoc {
		private final IToken includeDir;
        private final FileOrIFile fileOrIFile;
		private int line = 1;
		private int col = 1;
		private int offset = 0; //should this start at 0 or 1?

		private FileLoc(IFile file) {
			this.includeDir = null;
			this.fileOrIFile = new FileOrIFile(file);
		}

		private FileLoc(IToken includeDir) {
			this.includeDir = includeDir;
			this.fileOrIFile = new FileOrIFile(new java.io.File(includeDir.getIncludeFile()));
		}
	}
}
