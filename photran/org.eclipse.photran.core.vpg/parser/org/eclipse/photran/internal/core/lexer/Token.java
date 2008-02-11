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

import org.eclipse.core.resources.IFile;
import org.eclipse.photran.core.IFortranAST;
import org.eclipse.photran.core.vpg.PhotranTokenRef;
import org.eclipse.photran.core.vpg.PhotranVPG;
import org.eclipse.photran.core.vpg.util.OffsetLength;
import org.eclipse.photran.internal.core.analysis.binding.Definition;
import org.eclipse.photran.internal.core.analysis.binding.ScopingNode;
import org.eclipse.photran.internal.core.analysis.types.Type;
import org.eclipse.photran.internal.core.parser.ASTVisitor;
import org.eclipse.photran.internal.core.parser.CSTVisitor;
import org.eclipse.photran.internal.core.parser.GenericParseTreeVisitor;
import org.eclipse.photran.internal.core.parser.Parser.CSTNode;

import bz.over.vpg.VPGEdge;

/**
 * Implementation of <code>IToken</code> used by the Fortran parser.
 * 
 * @author Jeff Overbey
 */
public class Token extends CSTNode implements IToken
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
    protected String whiteBefore = "";

    /**
     * The token text
     */
    protected String text = "";

    /**
     * Whitespace and whitetext appearing after this token that should be associated with this token, not the next
     */
    protected String whiteAfter = "";
    
    ///////////////////////////////////////////////////////////////////////////
    // Additional Fields - Not updated when refactoring
    ///////////////////////////////////////////////////////////////////////////
    
    /**
     * If this <code>Token</code> resulted from expanding a preprocessor directive (e.g., an INCLUDE or a macro
     * expansion), this is the text of the preprocessor directive in the <i>top-level file</i> under which it was
     * expanded.  <code>Token</code>s expanded from the same directive will have pointer-identical
     * <code>preprocessorDirective</code>s.
     */
    protected String preprocessorDirective = null;
    
    protected IFile file = null;
    
    protected int line = -1, col = -1, fileOffset = -1, streamOffset = -1, length = -1;
    
    protected PhotranTokenRef tokenRef = null;

    ///////////////////////////////////////////////////////////////////////////
    // Constructors
    ///////////////////////////////////////////////////////////////////////////
    
    public Token(Terminal terminal, String whiteBefore, String tokenText, String whiteAfter)
    {
        this.terminal    = terminal;
        this.whiteBefore = whiteBefore == null ? "" : whiteBefore;
        this.text        = tokenText   == null ? "" : tokenText;
        this.whiteAfter  = whiteAfter  == null ? "" : whiteAfter;
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
        this.file                  = copyFrom.file;
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
    public void setText(String value) { text = value == null ? "" : value; }

    /**
     * Returns whitespace and whitetext appearing before this token that should be associated with this token
     */
    public String getWhiteBefore() { return whiteBefore; }

    /**
     * Sets whitespace and whitetext appearing before this token that should be associated with this token
     */
    public void setWhiteBefore(String value) { whiteBefore = value == null ? "" : value; }

    /**
     * Returns whitespace and whitetext appearing after this token that should be associated with this token, not the next
     */
    public String getWhiteAfter() { return whiteAfter; }

    /**
     * Sets whitespace and whitetext appearing after this token that should be associated with this token, not the next
     */
    public void setWhiteAfter(String value) { whiteAfter = value == null ? "" : value; }

    public String getPreprocessorDirective()
    {
        return preprocessorDirective;
    }

    public void setPreprocessorDirective(String preprocessorDirective)
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

    public IFile getFile()
    {
        return file;
    }

    public void setFile(IFile file)
    {
        this.file = file;
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
    // Visitor Support
    ///////////////////////////////////////////////////////////////////////////

    @Override protected void visitTopDownUsing(ASTVisitor visitor, boolean shouldVisitRoot)
    {
        if (shouldVisitRoot)
            visitor.visitToken(this);
    }

    @Override protected void visitBottomUpUsing(ASTVisitor visitor, boolean shouldVisitRoot)
    {
        if (shouldVisitRoot)
            visitor.visitToken(this);
    }

    @Override public void visitTopDownUsing(CSTVisitor visitor)
    {
        visitor.visitToken(this);
    }

    @Override public void visitBottomUpUsing(CSTVisitor visitor)
    {
        visitor.visitToken(this);
    }

    public void visitUsing(GenericParseTreeVisitor visitor)
    {
        visitor.visitToken(this);
    }

    ///////////////////////////////////////////////////////////////////////////
    // Debugging Output
    ///////////////////////////////////////////////////////////////////////////
    
    public String toString(int numSpaces) { return indent(numSpaces) + getDescription() + "\n"; }

    /**
     * Returns a string describing the token
     */
    public String getDescription() { return terminal.toString() + ": \"" + text + "\""; }
    
    ///////////////////////////////////////////////////////////////////////////
    // Source Code Reproduction
    ///////////////////////////////////////////////////////////////////////////
    
    public String printOn(PrintStream out, String currentPreprocessorDirective)
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
    
    ///////////////////////////////////////////////////////////////////////////
    // VPG Interaction
    ///////////////////////////////////////////////////////////////////////////

    public PhotranTokenRef getTokenRef()
    {
    	if (tokenRef == null) tokenRef = new PhotranTokenRef(file, fileOffset, length);
    	
    	return tokenRef;
    }
    
    ///////////////////////////////////////////////////////////////////////////

    public ScopingNode getEnclosingScope()
    {
    	return ScopingNode.getEnclosingScope(this);
    }
    
    ///////////////////////////////////////////////////////////////////////////
    
    public static class FakeToken extends Token
    {
    	private Token basedOn;
    	private FakeTokenRef tokenRef;
    	
    	public FakeToken(Token basedOn, String text)
    	{
    		super(basedOn);
    		this.setText(text);
    		this.basedOn = basedOn;
    		this.tokenRef = new FakeTokenRef(basedOn.getTokenRef(), text);
    	}
    	    	
    	@Override public String getText()
    	{
    		return tokenRef.getText();
    	}
    	
    	@Override public PhotranTokenRef getTokenRef()
    	{
    		return tokenRef;
    	}
    	
        public List<PhotranTokenRef> manuallyResolveBinding()
        {
        	return basedOn.getEnclosingScope().manuallyResolve(this);
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
		
		for (VPGEdge<IFortranAST, Token, PhotranTokenRef> edge : PhotranVPG.getDatabase().getOutgoingEdgesFrom(getTokenRef(), PhotranVPG.BINDING_EDGE_TYPE))
		{
    		def = PhotranVPG.getInstance().getDefinitionFor(edge.getSink());
    		if (def != null) result.add(def);
		}
		
		return result;
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
