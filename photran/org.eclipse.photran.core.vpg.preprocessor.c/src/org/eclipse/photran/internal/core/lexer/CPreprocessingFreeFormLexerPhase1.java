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
package org.eclipse.photran.internal.core.lexer;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.nio.charset.Charset;
import java.util.ArrayList;

import org.eclipse.core.resources.IFile;
import org.eclipse.photran.internal.core.lexer.preprocessor.fortran_include.IncludeLoaderCallback;

/**
 * Phase 1 lexer that handles C preprocessor directives.
 * <p>
 * This class feeds {@link FreeFormLexerPhase1} with a {@link CPreprocessingReader},
 * an {@link InputStream} that interprets C preprocessor directives.
 * {@link FreeFormLexerPhase1} tokenizes the preprocessed stream.  However, this
 * class subclasses {@link FreeFormLexerPhase1} and overrides its tokenization
 * method ({@link #yylex()}), so when it produces a token, it can reset the
 * filename, line number, file offset, and guarding preprocessor directive
 * correctly.
 *
 * @author Matthew Michelotti, Jeff Overbey
 */
public class CPreprocessingFreeFormLexerPhase1 extends FreeFormLexerPhase1
{
	/**ProducerMap obatined from CPreprocessingInputStream.
	 * Used to find directives and macros.*/
	private ProducerMap producerMap;

	private IncludeMap includeMap;

	/**an array of tokens, treated as a stack, that should be returned
	 * when yylex is called so long as the stack is not empty*/
	private ArrayList<Token> processedTokensStack = new ArrayList<Token>();
	/**a token which should be processed next instead of calling
	 * super.yylex() to get the next unprocessed token*/
	private Token nextRawToken = null;

	private Token lastToken = null;
	
	private Token lastReadInFile = null;

    public CPreprocessingFreeFormLexerPhase1(Reader in, IFile file, String filename, IncludeLoaderCallback callback, boolean accumulateWhitetext) throws IOException
    {
        this(new CPreprocessingReader(file, filename, new LineAppendingReader(in)), file, filename, ASTTokenFactory.getInstance(), accumulateWhitetext);
    }

    // This would not be here if we could assign the preprocessor to a variable in the above ctor (grrr)
    private CPreprocessingFreeFormLexerPhase1(CPreprocessingReader cpp, IFile file, String filename, TokenFactory tokenFactory, boolean accumulateWhitetext)
    {
        super(cpp, file, filename, tokenFactory, accumulateWhitetext);
        this.producerMap = cpp.getProducerMap();
        this.includeMap = cpp.getIncludeMap();

        if (accumulateWhitetext == false)
            throw new IllegalArgumentException("C preprocessor can only be used if accumulateWhitetext is true");
    }

    public Token yylex() throws IOException, LexerException
    {
        /*HACK: This conditional fixes the following bug: When a Fortran file has an invalid format
         * which is detected after the end-of-file token was read, it breaks the syntaxErrorRecovery 
         * routine. It breaks it because the routine (supposedly) strips the new-line from the token 
         * on which parsing failed, which in this case is the end-of-file token, with new-line being 
         * the last thing in the file. Because that new-line is discarded, some offset algebra in yylex()
         * produces negative string offset, which then triggers a StringIndexOutOfBounds Exception. 
         * By remembering the end-of-file token when we read it, we are bypassing that problem.
         */
    	if(lastReadInFile != null)
    	    return lastReadInFile;
    	
    	//return a token from the processedTokensStack if there is one
        Token token;
    	if(processedTokensStack.size() > 0) {
    		token = processedTokensStack.remove(processedTokensStack.size()-1);

    		//return the token
    		lastToken = token;
    		setTokenAsCurrent(token);
    		return token;
    	}

    	//obtain the next raw token, if it is not already obtained
    	if(nextRawToken != null) {
    		token = nextRawToken;
    		nextRawToken = null;
    	}
    	else token = rawYYLex();

    	//handle the final token specially...
    	if(token.getTerminal() == Terminal.END_OF_INPUT) {
    		//expand the whiteAfter on the last token using the producerMap
    		if(lastToken != null) {
    			String whiteAfter = lastToken.getWhiteAfter();

    			int whiteStartOffset = lastToken.getStreamOffset()
    								+ lastToken.getText().length();
    			int whiteEndOffset = whiteStartOffset + whiteAfter.length();

    			//The original whiteAfter on the last token usually has an
    			//extra newline or something, but we cannot exceed the final
    			//offset in the producerMap when setting markB.
    			//Thus, this stores any extra characters in a separate
    			//string called appendedWhite.
    			String appendedWhite;
    			if(whiteEndOffset > producerMap.getFinalOffset()) {
    				int newLength = producerMap.getFinalOffset() - whiteStartOffset;
    				whiteEndOffset = whiteStartOffset + newLength;
    				appendedWhite = whiteAfter.substring(newLength);
    				whiteAfter = whiteAfter.substring(0, newLength);
    			}
    			else appendedWhite = "";

    			producerMap.setMarkA(whiteStartOffset);
    			producerMap.setMarkB(whiteEndOffset);
    			String newWhiteAfter = producerMap.expandWhite(whiteAfter);

    			if(newWhiteAfter != null)
    				lastToken.setWhiteAfter(newWhiteAfter+appendedWhite);
    		}

    		//return the token
    		lastToken = token;
    		setTokenAsCurrent(token);
    		lastReadInFile = token;
    		return token;
    	}

    	//expand the white-space using the producerMap
    	String whiteBefore = token.getWhiteBefore();
    	producerMap.setMarkA(token.getStreamOffset() - whiteBefore.length());
    	producerMap.setMarkB(token.getStreamOffset());
    	String newWhiteBefore = producerMap.expandWhite(whiteBefore);
    	if(newWhiteBefore != null) token.setWhiteBefore(newWhiteBefore);

    	//prepare to expand the text using the producerMap
    	String text = token.getText();
    	producerMap.setMarkA(token.getStreamOffset());
    	producerMap.setMarkB(token.getStreamOffset() + text.length());

    	//If a producer overlaps the end of this token, more tokens will
    	//have to be read in until there is no such overlap (in this case,
    	//processedTokensStack may need to be filled and nextRawToken
    	//may need to be set).
    	//Otherwise, just update the producer of the token immediately.
    	if(producerMap.isMarkBInProducer()) {
    		int strLen = token.getText().length();
    		ArrayList<Token> postTokens = new ArrayList<Token>();
    		do {
    			Token postToken = rawYYLex();
    			if(postToken.getTerminal() == Terminal.END_OF_INPUT ||
    					producerMap.isBreakAfterMarkB(postToken.getStreamOffset()))
    			{
    				//WARNING: It seems like this code is not enough to handle
    				//the case when postToken.getTerminal() == Terminal.END_OF_INPUT.
    				//I suspect that getting the END_OF_INPUT token early from
    				//the FreeFormLexerPhase1 is causing problems.
    				nextRawToken = postToken;
    				break;
    			}
    			whiteBefore = postToken.getWhiteBefore();
    			postTokens.add(postToken);
    			text = postToken.getText();
    			producerMap.setMarkB(postToken.getStreamOffset() + text.length());
    			strLen += whiteBefore.length() + text.length();
    		}while(producerMap.isMarkBInProducer());

    		StringBuffer buffer = new StringBuffer(strLen);
    		buffer.append(token.getText());
    		for(int i = 0; i < postTokens.size(); i++) {
    			Token postToken = postTokens.get(i);
    			buffer.append(postToken.getWhiteBefore());
    			buffer.append(postToken.getText());
    		}
    		CPreprocessorReplacement producer =
    		    CPreprocessorReplacement.createFor(producerMap.expandNormal(buffer.toString()));

    		token.setPreprocessorDirective(producer);
    		for(int i = postTokens.size()-1; i >= 0; i--) {
    			Token postToken = postTokens.get(i);
    			postToken.setPreprocessorDirective(producer);
    			processedTokensStack.add(postToken);
    		}
    	}
    	else {
    	    CPreprocessorReplacement producer =
    	        CPreprocessorReplacement.createFor(producerMap.expandNormal(text));
    		if(producer != null) token.setPreprocessorDirective(producer);
    	}

    	//return the token
    	lastToken = token;
    	setTokenAsCurrent(token);
    	return token;
    }

    /**
     * Calls super.yylex() and returns the token. Updates the token's
     * file, line, col, file offset, stream offset, and length.
     * @return token obtained from super.yylex()
     * @throws IOException
     * @throws LexerException
     */
    private Token rawYYLex() throws IOException, LexerException {
    	Token token = (Token)super.yylex();
    	
    	includeMap.setStreamOffset(lastTokenStreamOffset);

    	//token.setFile(includeMap.getFile());
    	token.setLine(includeMap.getLine());
    	token.setCol(includeMap.getCol());
    	token.setPhysicalFile(includeMap.getFileOrIFile());
    	token.setFileOffset(includeMap.getFileOffset());
    	token.setStreamOffset(lastTokenStreamOffset);
    	token.setLength(lastTokenLength);

    	return token;
    }

    private void setTokenAsCurrent(Token token) {
    	//lastTokenFile = token.getFile();
    	lastTokenLine = token.getLine();
		lastTokenCol = token.getCol();
		lastTokenFile = token.getPhysicalFile();
		lastTokenFileOffset = token.getFileOffset();
		lastTokenStreamOffset = token.getStreamOffset();
		lastTokenLength = token.getLength();
    }
}
