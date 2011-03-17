/*******************************************************************************
 * Copyright (c) 2007 University of Illinois at Urbana-Champaign and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     UIUC - Initial API and implementation
 *******************************************************************************/
package org.eclipse.photran.internal.core.lexer;

import java.io.PrintStream;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.eclipse.core.resources.IFile;
import org.eclipse.photran.internal.core.analysis.binding.Definition;
import org.eclipse.photran.internal.core.analysis.binding.ScopingNode;
import org.eclipse.photran.internal.core.analysis.binding.VariableAccess;
import org.eclipse.photran.internal.core.parser.ASTNodeUtil;
import org.eclipse.photran.internal.core.parser.IASTNode;
import org.eclipse.photran.internal.core.parser.IASTVisitor;
import org.eclipse.photran.internal.core.vpg.AnnotationType;
import org.eclipse.photran.internal.core.vpg.EdgeType;
import org.eclipse.photran.internal.core.vpg.PhotranTokenRef;
import org.eclipse.photran.internal.core.vpg.PhotranVPG;
import org.eclipse.rephraserengine.core.util.OffsetLength;

/**
 * Tokens are returned by the lexical analyzer and serve as leaf nodes in the AST.
 * <p>
 * This is the implementation of {@link IToken} used by the Fortran parser.
 * 
 * @author Jeff Overbey
 */
public class Token implements IToken, IASTNode
{
    ///////////////////////////////////////////////////////////////////////////
    // Fields
    ///////////////////////////////////////////////////////////////////////////
    
    /**
     * The Terminal that this token is an instance of
     */
    protected Terminal terminal = null;

    /**
     * Whitespace and whitetext appearing before this token that should be associated with this token
     */
    protected String whiteBefore = ""; //$NON-NLS-1$

    /**
     * The token text
     */
    protected String text = ""; //$NON-NLS-1$

    /**
     * Whitespace and whitetext appearing after this token that should be associated with this token, not the next
     */
    protected String whiteAfter = ""; //$NON-NLS-1$
    
    ///////////////////////////////////////////////////////////////////////////
    // Additional Fields - Not updated when refactoring
    ///////////////////////////////////////////////////////////////////////////
    
    /**
     * If this <code>Token</code> resulted from expanding a preprocessor directive (e.g., an INCLUDE or a macro
     * expansion), this is the text of the preprocessor directive in the <i>top-level file</i> under which it was
     * expanded.  <code>Token</code>s expanded from the same directive will have pointer-identical
     * <code>preprocessorDirective</code>s.
     */
    protected IPreprocessorReplacement preprocessorDirective = null;
    
    protected FileOrIFile physicalFile = null;
    protected IFile logicalFile = null;
    
    protected int line = -1, col = -1, fileOffset = -1, streamOffset = -1, length = -1;
    
    protected PhotranTokenRef tokenRef = null;
    
    /** This is a cached value from the VPG; see {@link #getVariableAccessType()} */
    private VariableAccess varAccessType = null;

    ///////////////////////////////////////////////////////////////////////////
    // Constructors
    ///////////////////////////////////////////////////////////////////////////
    
    public Token(Terminal terminal, String whiteBefore, String tokenText, String whiteAfter)
    {
        this.terminal    = terminal;
        this.whiteBefore = whiteBefore == null ? "" : whiteBefore; //$NON-NLS-1$
        this.text        = tokenText   == null ? "" : tokenText; //$NON-NLS-1$
        this.whiteAfter  = whiteAfter  == null ? "" : whiteAfter; //$NON-NLS-1$
    }
    
    public Token(Terminal terminal, String tokenText)
    {
        this(terminal, null, tokenText, null);
    }
    
    protected Token(Token copyFrom)
    {
    	this.terminal              = copyFrom.terminal;
        this.whiteBefore           = copyFrom.whiteBefore;
        this.text                  = copyFrom.text;
        this.whiteAfter            = copyFrom.whiteAfter;
        this.preprocessorDirective = copyFrom.preprocessorDirective;
        this.physicalFile          = copyFrom.physicalFile;
        this.logicalFile           = copyFrom.logicalFile;
        this.line                  = copyFrom.line;
        this.col                   = copyFrom.col;
        this.fileOffset            = copyFrom.fileOffset;
        this.streamOffset          = copyFrom.streamOffset;
        this.length                = copyFrom.length;
        this.tokenRef              = copyFrom.tokenRef;
    }
    
    ///////////////////////////////////////////////////////////////////////////
    // Accessor/Mutator Methods
    ///////////////////////////////////////////////////////////////////////////

    /**
     * Returns the Terminal that this token is an instance of
     */
    public Terminal getTerminal() { return terminal; }

    /**
     * Sets the Terminal that this token is an instance of
     */
    public void setTerminal(Terminal value) { terminal = value; }

    /**
     * Returns the token text
     */
    public String getText() { return text; }

    /**
     * Sets the token text
     */
    public void setText(String value) { text = value == null ? "" : value; } //$NON-NLS-1$

    /**
     * Returns whitespace and whitetext appearing before this token that should be associated with this token
     */
    public String getWhiteBefore() { return whiteBefore; }

    /**
     * Sets whitespace and whitetext appearing before this token that should be associated with this token
     */
    public void setWhiteBefore(String value) { whiteBefore = value == null ? "" : value; } //$NON-NLS-1$

    /**
     * Returns whitespace and whitetext appearing after this token that should be associated with this token, not the next
     */
    public String getWhiteAfter() { return whiteAfter; }

    /**
     * Sets whitespace and whitetext appearing after this token that should be associated with this token, not the next
     */
    public void setWhiteAfter(String value) { whiteAfter = value == null ? "" : value; } //$NON-NLS-1$

    public IPreprocessorReplacement getPreprocessorDirective()
    {
        return preprocessorDirective;
    }

    public void setPreprocessorDirective(IPreprocessorReplacement preprocessorDirective)
    {
        this.preprocessorDirective = preprocessorDirective;
    }

    public int getLine()
    {
        return line;
    }

    public void setLine(int line)
    {
        this.line = line;
    }

    public int getCol()
    {
        return col;
    }

    public void setCol(int col)
    {
        this.col = col;
    }

    public FileOrIFile getPhysicalFile()
    {
        return physicalFile;
    }
    
    public void setPhysicalFile(FileOrIFile file)
    {
        this.physicalFile = file;
    }

    public IFile getLogicalFile()
    {
        return logicalFile;
    }
    
    public void setLogicalFile(IFile file)
    {
        this.logicalFile = file;
    }

    public int getFileOffset()
    {
        return fileOffset;
    }

    public void setFileOffset(int fileOffset)
    {
        this.fileOffset = fileOffset;
    }

    public int getStreamOffset()
    {
        return streamOffset;
    }

    public void setStreamOffset(int streamOffset)
    {
        this.streamOffset = streamOffset;
    }

    public int getLength()
    {
        return length;
    }

    public void setLength(int length)
    {
        this.length = length;
    }

    public boolean containsFileOffset(int offset)
    {
        return OffsetLength.contains(fileOffset, length, offset, 0);
    }

    public boolean containsFileOffset(OffsetLength other)
    {
        return OffsetLength.contains(fileOffset, length, other);
    }
    
    public boolean isOnOrAfterFileOffset(int targetOffset)
    {
        return fileOffset >= targetOffset;
    }

    public boolean containsStreamOffset(OffsetLength other)
    {
        return OffsetLength.contains(streamOffset, length, other);
    }
    
    public boolean isOnOrAfterStreamOffset(int targetOffset)
    {
        return streamOffset >= targetOffset;
    }

    ///////////////////////////////////////////////////////////////////////////
    // Whitetext Parsing
    ///////////////////////////////////////////////////////////////////////////

    private static final Pattern ompComment = Pattern.compile(
        "([Cc*!][ \\t]*\\$[Oo][Mm][Pp][ \\t]*)([^\\r\\n]*\\r?\\n)"); //$NON-NLS-1$

    /**
     * Returns a list of {@link Token}s representing OpenMP directives in the comments preceding
     * this token (i.e., {@link #getWhiteBefore()}).  These are {@link Token} objects simply
     * for convenience (they have text and location information); they <i>do not</i> appear in
     * the AST, and each invocation of this method will return (pointerwise) different Tokens.
     * <p>
     * This method is used by the OpenMP Artifact Analysis in PLDT (the Parallel Language
     * Development Tools in PTP).
     * 
     * @return a list of (artificial) {@link Token} objects, one per OpenMP directive in the
     *         comments preceding this Token
     * 
     * @see org.eclipse.ptp.pldt.openmp.core.analysis.OpenMPFortranASTVisitor
     */
    public List<Token> getOpenMPComments()
    {
        String whitetext = getWhiteBefore();
        Matcher m = ompComment.matcher(whitetext);
        int startStreamOffset = getStreamOffset() - whitetext.length();
        int startFileOffset = getFileOffset() - whitetext.length();
        List<Token> result = new LinkedList<Token>();
        
        for (int startSearchFrom = 0; m.find(startSearchFrom); startSearchFrom = m.end())
        {
            Token token = new Token(this);
            token.setTerminal(Terminal.SKIP);
            String prefix = m.group(1);
            String directive = m.group(2).trim();
            String suffix = directive.length() >= m.group(2).length() ? ""  : m.group(2).substring(directive.length()); //$NON-NLS-1$
            token.setWhiteBefore(prefix);
            token.setText(directive);
            token.setWhiteAfter(suffix);
            token.setStreamOffset(startStreamOffset + m.start());
            token.setFileOffset(startFileOffset + m.start());
            token.setLength(prefix.length() + directive.length());
            token.setParent(null);
            token.setLine(token.getLine() - countNewlines(whitetext.substring(m.start())));
            token.setCol(1);
            result.add(token);
        }
        
        return result;
    }

    private int countNewlines(String s)
    {
        int n = 0;
        for (int i = 0; i < s.length(); i++)
            if (s.charAt(i) == '\n')
                n++;
        return n;
    }
    
    ///////////////////////////////////////////////////////////////////////////
    // IASTNode Implementation
    ///////////////////////////////////////////////////////////////////////////

    private IASTNode parent = null;
    
    public IASTNode getParent()
    {
        return parent;
    }

    public void setParent(IASTNode parent)
    {
        this.parent = parent;
    }

    public void accept(IASTVisitor visitor)
    {
        visitor.visitToken(this);
    }

    public <T extends IASTNode> Set<T> findAll(Class<T> targetClass)
    {
        return ASTNodeUtil.findAll(this, targetClass);
    }

    public <T extends IASTNode> T findFirst(Class<T> targetClass)
    {
        return ASTNodeUtil.findFirst(this, targetClass);
    }

    public <T extends IASTNode> T findLast(Class<T> targetClass)
    {
        return ASTNodeUtil.findLast(this, targetClass);
    }

    public Token findFirstToken()
    {
        return this;
    }

    public Token findLastToken()
    {
        return this;
    }

    public <T extends IASTNode> T findNearestAncestor(Class<T> targetClass)
    {
        return ASTNodeUtil.findNearestAncestor(this, targetClass);
    }

    public Iterable<? extends IASTNode> getChildren()
    {
        return new LinkedList<IASTNode>();
    }

    public boolean isFirstChildInList()
    {
        return ASTNodeUtil.isFirstChildInList(this);
    }

    public void replaceChild(IASTNode node, IASTNode withNode)
    {
        throw new UnsupportedOperationException();
    }

//    public boolean matches(IASTNode pattern)
//    {
//        return ASTNodeUtil.match(this, pattern).succeeded();
//    }
//
//    public <T extends IASTNode> ASTMatcher.Match<T> match(T pattern)
//    {
//        return ASTNodeUtil.match(this, pattern);
//    }
//
//    public <T extends IASTNode> List<ASTMatcher.Match<T>> matchAll(T pattern)
//    {
//        return ASTNodeUtil.matchAll(this, pattern);
//    }
//
//    public <T extends IASTNode> void replaceAll(T pattern, T replacement)
//    {
//        ASTNodeUtil.replaceAll(this, pattern, replacement);
//    }
    
    public void removeFromTree()
    {
        ASTNodeUtil.removeFromTree(this);
    }
    
    public void replaceWith(IASTNode newNode)
    {
        ASTNodeUtil.replaceWith(this, newNode);
    }
    
    public void replaceWith(String string)
    {
        ASTNodeUtil.replaceWith(this, string);
    }
    
    @Override
    public Object clone()
    {
        return new Token(this);
    }
    
    ///////////////////////////////////////////////////////////////////////////
    // Debugging Output
    ///////////////////////////////////////////////////////////////////////////
    
    //public String toString(int numSpaces) { return indent(numSpaces) + getDescription() + "\n"; }

    /**
     * Returns a string describing the token
     */
    public String getDescription() { return terminal.toString() + ": \"" + text + "\""; } //$NON-NLS-1$ //$NON-NLS-2$
    
    ///////////////////////////////////////////////////////////////////////////
    // Source Code Reproduction
    ///////////////////////////////////////////////////////////////////////////
    
    public IPreprocessorReplacement printOn(PrintStream out, IPreprocessorReplacement currentPreprocessorDirective)
    {
        if (this.preprocessorDirective != currentPreprocessorDirective)
        {
            if (this.preprocessorDirective != null)
            {
                out.print(whiteBefore);
                out.print(this.preprocessorDirective);
            }
            currentPreprocessorDirective = this.preprocessorDirective;
        }
        
        if (currentPreprocessorDirective == null && this.preprocessorDirective == null)
        {
            out.print(whiteBefore);
            out.print(text);
            out.print(whiteAfter);
        }
        
        return currentPreprocessorDirective;
    }
    
    @Override public String toString()
    {
        return terminal + ": " + text.replace("\n", "\\n").replace("\r", "\\r"); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$ //$NON-NLS-5$
    }
    
    ///////////////////////////////////////////////////////////////////////////
    // VPG Interaction
    ///////////////////////////////////////////////////////////////////////////

    public PhotranTokenRef getTokenRef()
    {
    	if (tokenRef == null) tokenRef = new PhotranTokenRef(logicalFile, streamOffset, length);
    	
    	return tokenRef;
    }
    
    ///////////////////////////////////////////////////////////////////////////

    public ScopingNode getEnclosingScope()
    {
        return ScopingNode.getEnclosingScope(this);
    }

    public ScopingNode getLocalScope()
    {
        return ScopingNode.getLocalScope(this);
    }
    
    ///////////////////////////////////////////////////////////////////////////
    
    public static class FakeToken extends Token
    {
    	private ScopingNode scope;
    	private FakeTokenRef tokenRef;
        
        public FakeToken(Token basedOn, String text)
        {
            super(basedOn);
            this.setText(text);
            this.scope = basedOn.getEnclosingScope();
            this.tokenRef = new FakeTokenRef(basedOn.getTokenRef(), text);
        }
        
        public FakeToken(ScopingNode scope, String text)
        {
            super(new Token(null, text));
            this.setText(text);
            this.scope = scope;
            this.tokenRef = new FakeTokenRef(text);
        }
        
    	@Override public String getText()
    	{
    		return tokenRef.getText();
    	}
    	
    	@Override public PhotranTokenRef getTokenRef()
    	{
    		return tokenRef;
    	}
    	
    	@Override public List<PhotranTokenRef> manuallyResolveBinding()
        {
        	return scope.manuallyResolve(this);
        }
    	
    	private static class FakeTokenRef extends PhotranTokenRef
        {
        	private static final long serialVersionUID = 1L;
        	
			private String text;
            
            public FakeTokenRef(PhotranTokenRef basedOn, String text)
            {
                super(basedOn);
                this.text = text;
            }
            
            public FakeTokenRef(String text)
            {
                this(new PhotranTokenRef("", -1, -1), text); //$NON-NLS-1$
            }
        	
        	@Override public String getText()
        	{
        		return text;
        	}
        }
    }
    
    ///////////////////////////////////////////////////////////////////////////

    public List<PhotranTokenRef> manuallyResolveBinding()
    {
        return getEnclosingScope().manuallyResolve(this);
    }
    
    public List<PhotranTokenRef> manuallyResolveBindingNoImplicits()
    {
        return getEnclosingScope().manuallyResolveNoImplicits(this);
    }
    
	public ScopingNode findScopeDeclaringOrImporting(Definition definition)
	{
		return getEnclosingScope().findScopeDeclaringOrImporting(definition.getTokenRef().findToken());
	}

    ///////////////////////////////////////////////////////////////////////////
    
    public List<Definition> resolveBinding()
    {
    	List<Definition> result = new LinkedList<Definition>();
    	
		Definition def = PhotranVPG.getInstance().getDefinitionFor(getTokenRef());
		if (def != null)
		{
			result.add(def);
			return result;
		}
		
		for (PhotranTokenRef tokenRef : getTokenRef().followOutgoing(EdgeType.BINDING_EDGE_TYPE))
		{
    		def = PhotranVPG.getInstance().getDefinitionFor(tokenRef);
    		if (def != null) result.add(def);
		}
		
		return result;
    }
    
    ///////////////////////////////////////////////////////////////////////////
    
    /**
     * Assuming this token is an identifier ({@link #isIdentifier()}) representing a variable,
     * returns the type of access represented by this occurrence of the variable.
     * <p>
     * If this token is not a variable, or if it is used in a declarative content (e.g., a
     * specification statement), this method will return {@link VariableAccess#NONE}.
     */
    public VariableAccess getVariableAccessType()
    {
        if (varAccessType == null)
        {
            varAccessType = (VariableAccess)
                getTokenRef().getAnnotation(
                    AnnotationType.VARIABLE_ACCESS_ANNOTATION_TYPE);
            if (varAccessType == null)
                varAccessType = VariableAccess.NONE;
        }

        return varAccessType;
    }
    
    ///////////////////////////////////////////////////////////////////////////

//    public Type getType()
//    {
//		return PhotranVPG.getInstance().getTypeFor(getTokenRef());
//    }
    
    public boolean isIdentifier()
    {
        return getTerminal() == Terminal.T_IDENT;
    }
}
