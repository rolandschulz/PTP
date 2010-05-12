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
package org.eclipse.photran.internal.core.preprocessor.c;

import java.io.File;
import java.io.IOException;
import java.io.Reader;
import java.util.Arrays;

import org.eclipse.cdt.core.CCorePlugin;
import org.eclipse.cdt.core.dom.parser.c.GCCScannerExtensionConfiguration;
import org.eclipse.cdt.core.parser.CodeReader;
import org.eclipse.cdt.core.parser.IScannerInfo;
import org.eclipse.cdt.core.parser.IScannerInfoProvider;
import org.eclipse.cdt.core.parser.NullLogService;
import org.eclipse.cdt.core.parser.ParserLanguage;
import org.eclipse.cdt.core.parser.ScannerInfo;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;

/**
 * A wrapper class for the CPreprocessor in order to make
 * functionality easier.
 * 
 * @author Matthew Michelotti
 */
public class CppHelper {
    /**
     * An option.
     * When it is true, CppHelper will use CPreprocessor.nextTokenRaw() to
     * get tokens. When it is false, CppHelper will use
     * CPreprocessor.nextToken() to get tokens. Currently, the
     * implementation of CPreprocessor is such that useRawTokens should
     * always be true, but this option is here in case a switch needs to
     * be made at some point.
     */
    private boolean useRawTokens = true;
    
    /**The CPreprocessor object to get tokens from*/
    private CPreprocessor cpp;
    /**True iff all Tokens have been obtained from the CPreprocessor*/
    private boolean finished = false;
    
    
    /**
     * Constructs a CPreprocessor to read tokens from the given file.
     * Included files are taken relative to this file
     * or the includeSearchPaths.<br/>
     * NOTE: If more control over the include search paths is desired,
     * consider making a new CppHelper constructor using the class
     * ExtendedScannerInfo instead of ScannerInfo.
     * @param filename - name of file that input stream belongs to
     * @param in - input stream to read file from
     * @param includeSearchPaths - file paths to search in for included files
     * @throws IOException
     */
    public CppHelper(String filename, Reader in, String[] includeSearchPaths)
    		throws IOException
    {
    	//NOTE: it is important that the CodeReader gets an ABSOLUTE file path
        cpp = new CPreprocessor(new ReaderBasedCodeReader(filename, in),
            new ScannerInfo(null, includeSearchPaths), ParserLanguage.C,
            new NullLogService(), new GCCScannerExtensionConfiguration(),
            FileCodeReaderFactory.getInstance());
    }
    
    /**
     * Constructs a CPreprocessor to read tokens from the given file.
     * Included files are taken relative to this file.
     * @param file - IFile of the input stream belongs to
     * @param filename - name of file that input stream belongs to
     * @param in - input stream to read file from
     * @throws IOException
     * NOTE: @param file could be null (if the opened file is not in the workspace).
     *      Also, @param in could be different than @param file, if for example there
     *      are un-saved changes in the editor.
     */
    public CppHelper(IResource infoProvider, String filename, Reader in) throws IOException {
        //NOTE: it is important that the CodeReader gets an ABSOLUTE file path
        if (filename == null) 
            filename = "";
        
        IScannerInfo scanInfo = new ScannerInfo();
        if(infoProvider != null)
        {
            filename = infoProvider.getFullPath().toString();
            IProject project = infoProvider.getProject();
            IScannerInfoProvider provider = CCorePlugin.getDefault().getScannerInfoProvider(project);
            if (provider != null) {
                IScannerInfo buildScanInfo = provider.getScannerInformation(infoProvider);
                if (buildScanInfo != null)
                    scanInfo = buildScanInfo;
            }
        }

        //Map<String, String> temp = scanInfo.getDefinedSymbols();
        cpp = new CPreprocessor(new ReaderBasedCodeReader(filename, in),
            new ScannerInfo(scanInfo.getDefinedSymbols(), scanInfo.getIncludePaths()), ParserLanguage.C, new NullLogService(), 
            new GCCScannerExtensionConfiguration(), FileCodeReaderFactory.getInstance());
    }
    
    /**
     * Constructs a CPreprocessor to read tokens from the given file.
     * Included files are taken relative to this file.
     * @param file - file to read tokens from
     * @throws IOException
     */
    public CppHelper(File file) throws IOException {
        //NOTE: it is important that the CodeReader gets an ABSOLUTE file path
        cpp = new CPreprocessor(new CodeReader(file.getAbsolutePath()),
            new ScannerInfo(), ParserLanguage.C, new NullLogService(), 
            new GCCScannerExtensionConfiguration(), FileCodeReaderFactory.getInstance());
    }
    
    /**
     * Constructs a CPreprocessor to read tokens from the given file.
     * Included files are taken relative to this file.
     * @param filename - name of file to read tokens from
     * @throws IOException
     */
    public CppHelper(String filename) throws IOException {
        this(new File(filename));
    }
    
    /**
     * Obtain the next token from the CPreprocessor
     * @return next token, or null if there are no more tokens
     * @throws OffsetLimitReachedException
     */
    public IToken nextToken() throws OffsetLimitReachedException {
        if(finished) return null;
        
        if(useRawTokens) {
            IToken next = cpp.nextTokenRaw();
            if(next.getType() == IToken.tEND_OF_INPUT) finished = true;
            return next;
        }
        else {
            IToken next = null;
            try {next = cpp.nextToken();}
            catch (EndOfFileException e) {
                finished = true;
                return null;
            }
            return next;
        }
    }
    
    /**
     * Call nextToken() repeatedly until the CPreprocessor is out of tokens.
     * @return The first token obtained. You can get to the rest of the
     *         tokens by using IToken.getNext()
     * @throws OffsetLimitReachedException
     */
    public IToken getRemainingTokens() throws OffsetLimitReachedException {
        IToken first = nextToken();
        IToken t = first;
        while(t != null) t = nextToken();
        return first;
    }
    
    /**
     * Find the highest ancestor of a given token. i.e., call
     * t.getParent() repeatedly and return the last non-null value.
     * May return the input token.
     * @param t - token to find ancestor of
     * @param ignoreCarriageReturnAncestors - just returns t
     *        if its ancestor is "\r\n"
     * @return highest ancestor of the given token
     */
    public static IToken getAncestor(IToken t, boolean ignoreCarriageReturnAncestors) {
    	IToken lastParent = t;
        IToken parent = lastParent.getParent();
        while(parent != null) {
        	lastParent = parent;
        	parent = lastParent.getParent();
        }
        
        if(ignoreCarriageReturnAncestors) {
        	if(t.getType() == Lexer.tNEWLINE && lastParent.getImage().equals("\r\n")
        		&& Arrays.equals(t.getCharPrecedingWhiteSpace(), lastParent.getCharPrecedingWhiteSpace()))
        	{
        		return t;
        	}
        }
        
        return lastParent;
    }
    
    /**
     * Same as getAncestor(IToken, boolean), but assumes
     * ignoreCaradgeReturnAncestors is false.
     * @param t - token to find ancestor of
     * @return highest ancestor of the given token
     */
    public static IToken getAncestor(IToken t) {
    	return getAncestor(t, false);
    }
    
    /**
     * find the first include directive which is an ancestor of this
     * token (does not consider the input token itself)
     * @param t - token to find an include directive parent of
     * @return the found include directive token, or null if none was found
     */
    public static IToken getIncludeParent(IToken t) {
    	for(IToken p = t.getParent(); p != null; p = p.getParent()) {
    		if(p.getIncludeFile() != null) return p;
    	}
    	return null;
    }
    
    /**@param t - token to get image of
     * @return the image of the token (without white-space)*/
    public static String getImage(IToken t) {
    	if(t.getType() == Lexer.tNEWLINE) return "\n";
    	else return t.getImage();
    }
    
    /**@param t - token
     * @return the white space before the token*/
    public static String getPreWhiteSpace(IToken t) {
    	return t.getPrecedingWhiteSpace();
    }
    
    /**@param t - token to get whitespace of
     * @return the length of the whitespace before the token*/
    public static int getPreWhiteSpaceLength(IToken t) {
    	return t.getCharPrecedingWhiteSpace().length;
    }
    
    /**@param t - token to get image of
     * @return the length of the token image (without white-space)*/
    public static int getImageLength(IToken t) {
    	if(t.getType() == Lexer.tNEWLINE) return 1; 
    	else return t.getCharImage().length;
    }
    
    /**@param t - token to get full image of
     * @return full image of the given token, including whitespace*/
    public static String getFullImage(IToken t) {
    	return getPreWhiteSpace(t) + getImage(t);
    }

    /**@param t - token to get length of
     * @return the length of the full token image, including whitespace*/
    public static int getFullImageLength(IToken t) {
    	return getPreWhiteSpaceLength(t) + getImageLength(t);
    }
    
    /**
     * Get a String describing the details of Token. Each parent in
     * the hierarchy will be listed, separated by tokenSeparator.
     * The preceding white space and image of each token in the
     * hierarchy will be listed, separated by tokenSeparator.
     * "\n" is replaced with "\\n", and "\r" is replaced with "\\r",
     * so assuming spaceSeparator and tokenSeparator don't have newline
     * characters in them, this String will be all on one line.
     * Note that this method may not be thread-safe even though it is static,
     * since it accesses a static variable.
     * @param t - token to describe
     * @param spaceSeparator - String that should be used to separate spaces
     * @param tokenSeparator - String that should be used to separate tokens
     * @param printTypes - true iff token types should be printed for the
     *        token t and its ancestors. The type will be placed before the
     *        pre-whitespace of each token, followed by a spaceSeparator.
     * @return String describing the token and its parents
     */
    public static String getTokenDetails(IToken t, String spaceSeparator,
                                    String tokenSeparator, boolean printTypes)
    {
        StringBuffer buffer = new StringBuffer(200);
        
        while(true) {
            if(printTypes) {
                buffer.append(TokenTypeTranslator.typeToString(t.getType()));
                buffer.append(spaceSeparator);
            }
            appendNeatString(buffer, t.getCharPrecedingWhiteSpace());
            buffer.append(spaceSeparator);
            if(t.getType() == Lexer.tNEWLINE) buffer.append("\\n");
            else appendNeatString(buffer, t.getCharImage());
            
            t = t.getParent();
            if(t == null) break;
            buffer.append(tokenSeparator);
        }
        return buffer.toString();
    }
    
    /**
     * Reproduce the original source code from a sequence of
     * tokens, and return as a string.
     * @param ignoreCarriageReturnAncestors - if a token has
     *        "\r\n" as its ancestor, ignore the ancestor
     * @param startToken - a token, which contains a series of
     *        tokens after it that can be obtained using
     *        IToken.getNext()
     * @return the original source code
     */
    public static String reproduceSourceCode(IToken startToken, boolean ignoreCarriageReturnAncestors) {
        StringBuffer buffer = new StringBuffer(4096);
        
        IToken lastAncestor = null;
        for(IToken t = startToken; t != null; t = t.getNext()) {
            IToken ancestor = CppHelper.getAncestor(t, ignoreCarriageReturnAncestors);
            if(ancestor != lastAncestor) {
                buffer.append(CppHelper.getFullImage(ancestor));
                lastAncestor = ancestor;
            }
        }
        
        return buffer.toString();
    }
    
    /**
     * Same as reproduceSourceCode(IToken, boolean), except
     * ignoreCarriageReturnAncestors is assumed to be false.
     * @param startToken - a token, which contains a series of
     *        tokens after it that can be obtained using
     *        IToken.getNext()
     * @return the original source code
     */
    public static String reproduceSourceCode(IToken startToken) {
    	return reproduceSourceCode(startToken, false);
    }
    
    /**
     * Helper function for getTokenDetails.
     * This will add characters to a buffer, but it will first
     * replace "\n" with "\\n" and "\r" with "\\r"
     * @param buffer - StringBuffer to add characters to
     * @param chars - characters to add to buffer
     */
    private static void appendNeatString(StringBuffer buffer, char[] chars) {
        for(int i = 0; i < chars.length; i++) {
            switch(chars[i]) {
            case('\n'): buffer.append("\\n"); break;
            case('\r'): buffer.append("\\r"); break;
            default: buffer.append(chars[i]); break;
            }
        }
    }
}
