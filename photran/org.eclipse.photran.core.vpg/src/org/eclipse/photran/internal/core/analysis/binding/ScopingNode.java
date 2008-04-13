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
package org.eclipse.photran.internal.core.analysis.binding;

import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IMarker;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.photran.core.vpg.PhotranTokenRef;
import org.eclipse.photran.core.vpg.PhotranVPG;
import org.eclipse.photran.core.vpg.PhotranVPGBuilder;
import org.eclipse.photran.core.vpg.util.Notification;
import org.eclipse.photran.internal.core.analysis.types.Type;
import org.eclipse.photran.internal.core.lexer.Token;
import org.eclipse.photran.internal.core.lexer.Token.FakeToken;
import org.eclipse.photran.internal.core.parser.ASTBlockDataNameNode;
import org.eclipse.photran.internal.core.parser.ASTBlockDataStmtNode;
import org.eclipse.photran.internal.core.parser.ASTBlockDataSubprogramNode;
import org.eclipse.photran.internal.core.parser.ASTDerivedTypeDefNode;
import org.eclipse.photran.internal.core.parser.ASTDerivedTypeStmtNode;
import org.eclipse.photran.internal.core.parser.ASTEndBlockDataStmtNode;
import org.eclipse.photran.internal.core.parser.ASTEndFunctionStmtNode;
import org.eclipse.photran.internal.core.parser.ASTEndInterfaceStmtNode;
import org.eclipse.photran.internal.core.parser.ASTEndModuleStmtNode;
import org.eclipse.photran.internal.core.parser.ASTEndNameNode;
import org.eclipse.photran.internal.core.parser.ASTEndProgramStmtNode;
import org.eclipse.photran.internal.core.parser.ASTEndSubroutineStmtNode;
import org.eclipse.photran.internal.core.parser.ASTEndTypeStmtNode;
import org.eclipse.photran.internal.core.parser.ASTExecutableProgramNode;
import org.eclipse.photran.internal.core.parser.ASTFunctionNameNode;
import org.eclipse.photran.internal.core.parser.ASTFunctionStmtNode;
import org.eclipse.photran.internal.core.parser.ASTFunctionSubprogramNode;
import org.eclipse.photran.internal.core.parser.ASTGenericNameNode;
import org.eclipse.photran.internal.core.parser.ASTGenericSpecNode;
import org.eclipse.photran.internal.core.parser.ASTInterfaceBlockNode;
import org.eclipse.photran.internal.core.parser.ASTInterfaceStmtNode;
import org.eclipse.photran.internal.core.parser.ASTMainProgramNode;
import org.eclipse.photran.internal.core.parser.ASTModuleNameNode;
import org.eclipse.photran.internal.core.parser.ASTModuleNode;
import org.eclipse.photran.internal.core.parser.ASTModuleStmtNode;
import org.eclipse.photran.internal.core.parser.ASTProgramNameNode;
import org.eclipse.photran.internal.core.parser.ASTProgramStmtNode;
import org.eclipse.photran.internal.core.parser.ASTSubroutineNameNode;
import org.eclipse.photran.internal.core.parser.ASTSubroutineStmtNode;
import org.eclipse.photran.internal.core.parser.ASTSubroutineSubprogramNode;
import org.eclipse.photran.internal.core.parser.ASTTypeNameNode;
import org.eclipse.photran.internal.core.parser.GenericParseTreeVisitor;
import org.eclipse.photran.internal.core.parser.IInternalSubprogram;
import org.eclipse.photran.internal.core.parser.Parser.CSTNode;
import org.eclipse.photran.internal.core.parser.Parser.InteriorNode;
import org.eclipse.photran.internal.core.parser.Parser.Production;

import bz.over.vpg.TokenRef;

/**
 * An AST node representing a scope.
 * <p>
 * (View the type hierarchy to see which nodes are scoping nodes.)
 * 
 * @author Jeff Overbey
 */
public abstract class ScopingNode extends InteriorNode
{
	public ScopingNode(Production p)
	{
		super(p);
	}

    public static ScopingNode getEnclosingScope(CSTNode node)
    {
        for (CSTNode candidate = node.getParent(); candidate != null; candidate = candidate.getParent())
        {
            if (isScopingNode(candidate))
            {
                ScopingNode scope = (ScopingNode)candidate;
                
                //if (node == scope.getRepresentativeToken().findToken())
                if (shouldBeBoundToOuterScope(node))
                    return getEnclosingScope(scope);
                else
                    return scope;
            }
        }
        
        return null;
    }

    public static ScopingNode getLocalScope(CSTNode node)
    {
        for (CSTNode candidate = node.getParent(); candidate != null; candidate = candidate.getParent())
            if (isScopingNode(candidate))
                return (ScopingNode)candidate;
        
        return null;
    }
    
	/**
	 * In cases such as
	 * <pre>
	 * SUBROUTINE S
	 *   INTEGER :: S
	 * END SUBROUTINE S
	 * </pre>
	 * the <code>S</code>'s in the Subroutine and End Subroutine statements are under the
	 * ASTSubroutineSubprogram node in the AST, but they should actually be bound in the
	 * <i>outer</i> scope.
	 * <p>
	 * In general, this is true for 
	 * derived type names, program names, module names, block data names,
	 * interface names, subroutine names, and function names in the beginning and ending
	 * statements for their respective scoping nodes.
	 */
    private static boolean shouldBeBoundToOuterScope(CSTNode node)
    {
    	CSTNode parent = node.getParent();
    	if (parent == null) return false;
    	CSTNode grandparent = parent.getParent();
    	if (grandparent == null) return false;
    	
    	if (parent instanceof ASTProgramNameNode
    		|| parent instanceof ASTFunctionNameNode
    		|| parent instanceof ASTSubroutineNameNode
    		|| parent instanceof ASTModuleNameNode
    		|| parent instanceof ASTBlockDataNameNode
    		|| parent instanceof ASTTypeNameNode       // <-   These are used in other contexts
    		|| parent instanceof ASTGenericNameNode    //   |  as well, so we must test the
    		|| parent instanceof ASTGenericSpecNode    //   |  grandparent node to make a
    		|| parent instanceof ASTEndNameNode)       // <-   decision
		{
    		if (inAnonymousInterface(grandparent))
    			return false;
    		else    		
				return grandparent instanceof ASTProgramStmtNode
					|| grandparent instanceof ASTFunctionStmtNode
					|| grandparent instanceof ASTSubroutineStmtNode
					|| grandparent instanceof ASTModuleStmtNode
					|| grandparent instanceof ASTBlockDataStmtNode
					|| grandparent instanceof ASTDerivedTypeStmtNode
					|| grandparent instanceof ASTInterfaceStmtNode
					|| grandparent instanceof ASTEndProgramStmtNode
					|| grandparent instanceof ASTEndFunctionStmtNode
					|| grandparent instanceof ASTEndSubroutineStmtNode
					|| grandparent instanceof ASTEndModuleStmtNode
					|| grandparent instanceof ASTEndBlockDataStmtNode
					|| grandparent instanceof ASTEndTypeStmtNode
					|| grandparent instanceof ASTEndInterfaceStmtNode;
		}
    	else return false;
	}

	private static boolean inAnonymousInterface(CSTNode n)
	{
		for (InteriorNode node = n.getParent(); node != null; node = node.getParent())
			if (node instanceof ASTInterfaceBlockNode && isAnonymousInterface((ASTInterfaceBlockNode)node))
				return true;
		return false;
	}

	public static boolean isScopingNode(CSTNode node)
    {
    	return node instanceof ASTExecutableProgramNode
    		|| node instanceof ASTMainProgramNode
    		|| node instanceof ASTFunctionSubprogramNode
    		|| node instanceof ASTSubroutineSubprogramNode
    		|| node instanceof ASTModuleNode
    		|| node instanceof ASTBlockDataSubprogramNode
    		|| node instanceof ASTDerivedTypeDefNode
    		|| (node instanceof ASTInterfaceBlockNode && !isAnonymousInterface((ASTInterfaceBlockNode)node));
    }
	
	private static boolean isAnonymousInterface(ASTInterfaceBlockNode node)
	{
		return node.getInterfaceStmt().getGenericName() != null
			&& node.getInterfaceStmt().getGenericSpec() != null;
	}

    public ScopingNode getEnclosingScope()
    {
    	return getEnclosingScope(this);
    }
    
    public ScopingNode getGlobalScope()
    {
    	CSTNode result = this;
    	
    	// Find root of AST (i.e., topmost ASTExecutableProgramNode)
    	while (result.getParent() != null) result = result.getParent();
    	
    	return (ScopingNode)result;
    }

    public PhotranTokenRef getRepresentativeToken()
    {
    	// TODO: GET RID OF THIS MESS AFTER INDIVIDUAL NODES CAN BE CUSTOMIZED
    	// AND DYNAMICALLY DISPATCHED TO!
    	
    	if (this instanceof ASTExecutableProgramNode)
    	{
    		try
    		{
	    		this.visitUsing(new GenericParseTreeVisitor()
	    		{
					@Override public void visitToken(Token token)
					{
						throw new Notification(token.getFile());
					}
	    			
	    		});
    		}
    		catch (Notification n)
    		{
    			return new PhotranTokenRef((IFile)n.getResult(), -1, 0);
    		}
    		throw new Error("Empty file");
    	}
    	else if (this instanceof ASTMainProgramNode)
    	{
    		ASTProgramStmtNode m = ((ASTMainProgramNode)this).getProgramStmt();
    		if (m != null)
    		{
    			if (m.getProgramName() != null)
    				return m.getProgramName().getProgramName().getTokenRef();
    			else
    				return m.getProgramToken().getTokenRef();
    		}
    		else
    		{
	    		ASTEndProgramStmtNode s = ((ASTMainProgramNode)this).getEndProgramStmt();
	    		return s.getEndToken().getTokenRef();
    		}
    	}
    	else if (this instanceof ASTFunctionSubprogramNode)
    	{
    		return ((ASTFunctionSubprogramNode)this).getFunctionStmt().getFunctionName().getFunctionName().getTokenRef();
    	}
    	else if (this instanceof ASTSubroutineSubprogramNode)
    	{
    		return ((ASTSubroutineSubprogramNode)this).getSubroutineStmt().getSubroutineName().getSubroutineName().getTokenRef();
    	}
    	else if (this instanceof ASTModuleNode)
    	{
    		return ((ASTModuleNode)this).getModuleStmt().getModuleName().getModuleName().getTokenRef();
    	}
    	else if (this instanceof ASTBlockDataSubprogramNode)
    	{
    		ASTBlockDataStmtNode s = ((ASTBlockDataSubprogramNode)this).getBlockDataStmt();
    		if (s.getBlockDataName() != null)
    			return s.getBlockDataName().getBlockDataName().getTokenRef();
    		else
    			return s.getBlockDataToken().getTokenRef();
    	}
    	else if (this instanceof ASTDerivedTypeDefNode)
    	{
    		return ((ASTDerivedTypeDefNode)this).getDerivedTypeStmt().getTypeName().getTokenRef();
    	}
    	else if (this instanceof ASTInterfaceBlockNode)
		{
    		ASTInterfaceStmtNode s = ((ASTInterfaceBlockNode)this).getInterfaceStmt();
			if (s.getGenericName() != null)
				return s.getGenericName().getGenericName().getTokenRef();
			else if (s.getGenericSpec() != null && s.getGenericSpec().getEqualsToken() != null)
				return s.getGenericSpec().getEqualsToken().getTokenRef();
			else
				return s.getInterfaceToken().getTokenRef();
		}
    	else
    	{
    		throw new UnsupportedOperationException();
    	}
    }

    public InteriorNode getHeaderStmt()
	{
    	// TODO: GET RID OF THIS MESS AFTER INDIVIDUAL NODES CAN BE CUSTOMIZED
    	// AND DYNAMICALLY DISPATCHED TO!
    	
    	if (this instanceof ASTExecutableProgramNode)
    		return null;
    	else if (this instanceof ASTMainProgramNode)
    		return ((ASTMainProgramNode)this).getProgramStmt();
    	else if (this instanceof ASTFunctionSubprogramNode)
    		return ((ASTFunctionSubprogramNode)this).getFunctionStmt();
    	else if (this instanceof ASTSubroutineSubprogramNode)
    		return ((ASTSubroutineSubprogramNode)this).getSubroutineStmt();
    	else if (this instanceof ASTModuleNode)
    		return ((ASTModuleNode)this).getModuleStmt();
    	else if (this instanceof ASTBlockDataSubprogramNode)
    		return ((ASTBlockDataSubprogramNode)this).getBlockDataStmt();
    	else if (this instanceof ASTDerivedTypeDefNode)
    		return ((ASTDerivedTypeDefNode)this).getDerivedTypeStmt();
    	else if (this instanceof ASTInterfaceBlockNode)
    		return ((ASTInterfaceBlockNode)this).getInterfaceStmt();
    	else
    		throw new UnsupportedOperationException();
	}

    public boolean isInternal()
    {
    	return getParent() instanceof IInternalSubprogram;
    }
    
    public ImplicitSpec getImplicitSpec()
    {
    	return (ImplicitSpec)PhotranVPG.getDatabase().getAnnotation(getRepresentativeToken(), PhotranVPG.SCOPE_IMPLICIT_SPEC_ANNOTATION_TYPE);
    }
    
    public boolean isImplicitNone()
    {
    	return getImplicitSpec() == null;
    }
    
    public boolean isDefaultVisibilityPrivate()
    {
		return PhotranVPG.getDatabase().getAnnotation(getRepresentativeToken(), PhotranVPG.SCOPE_DEFAULT_VISIBILITY_IS_PRIVATE_ANNOTATION_TYPE) != null;
    }
    
    public boolean isParentScopeOf(ScopingNode scope)
    {
    	for (CSTNode node = scope.getParent(); node != null && node.getParent() != null; node = node.getParent())
    		if (node == this)
    			return true;
    	
    	return false;
    }

    /** @return this scope and all scopes nested within it */
    public List<ScopingNode> getAllContainedScopes()
    {
		final List<ScopingNode> scopes = new LinkedList<ScopingNode>();

		this.visitUsing(new GenericParseTreeVisitor()
		{
			@Override public void visitParseTreeNode(InteriorNode node)
			{
				if (isScopingNode(node))
					scopes.add((ScopingNode)node);
			}
			
		});

	    return scopes;
    }
    
    private static interface BindingResolutionCallback
    {
    	void foundDefinition(PhotranTokenRef definition, ScopingNode scope);
    }
    
	public ScopingNode findScopeDeclaringOrImporting(Token identifier)
	{
		try
		{
			manuallyResolve(identifier, new BindingResolutionCallback()
	    	{
				public void foundDefinition(PhotranTokenRef definition, ScopingNode scope)
				{
					throw new Notification(scope);
				}
	    	});
			
			return null;
		}
		catch (Notification n)
		{
			return (ScopingNode)n.getResult();
		}
	}
	
	public List<ScopingNode> findImportingScopes()
	{
		PhotranVPG vpg = PhotranVPG.getInstance();
		
		Set<PhotranTokenRef> result = new HashSet<PhotranTokenRef>();
		for (Definition def : getAllDefinitions())
		    if (def != null) // TODO: Why are we getting null here?
		        for (TokenRef<Token> t : vpg.db.getOutgoingEdgeTargets(def.getTokenRef(), PhotranVPG.IMPORTED_INTO_SCOPE_EDGE_TYPE))
		            result.add((PhotranTokenRef)t);
	    	
	    List<ScopingNode> scopes = new LinkedList<ScopingNode>();
	    for (PhotranTokenRef tokenRef : result)
	    	scopes.add(tokenRef.findToken().getEnclosingScope());
	    return scopes;
	}

    public List<PhotranTokenRef> manuallyResolve(Token identifier)
    {
    	final List<PhotranTokenRef> bindings = new LinkedList<PhotranTokenRef>();
    	
    	manuallyResolve(identifier, new BindingResolutionCallback()
    	{
			public void foundDefinition(PhotranTokenRef definition, ScopingNode scope)
			{
				bindings.add(definition);
			}
    	});
    	
    	return bindings;
    }

    public List<PhotranTokenRef> manuallyResolveInLocalScope(Token identifier)
    {
    	final List<PhotranTokenRef> bindings = new LinkedList<PhotranTokenRef>();
    	
    	manuallyResolveInLocalScope(identifier, new BindingResolutionCallback()
    	{
			public void foundDefinition(PhotranTokenRef definition, ScopingNode scope)
			{
				bindings.add(definition);
			}
    	});
    	
    	return bindings;
    }

    private void manuallyResolve(Token identifier, BindingResolutionCallback result)
    {
    	if (!manuallyResolveInLocalScope(identifier, result))
    		if (!manuallyResolveInParentScopes(identifier, result))
    			if (!manuallyResolveIntrinsic(identifier, result))
    				attemptToDeclareImplicit(identifier, result);
    }
    
	private boolean manuallyResolveInLocalScope(Token identifier, BindingResolutionCallback bindings)
	{
    	String name = PhotranVPG.canonicalizeIdentifier(identifier.getText());
    	boolean wasSuccessful = false;

    	for (Definition def : getAllDefinitions())
    	{
    		if (def != null && def.matches(name)) // TODO: Why are we getting null here?
    		{
    			bindings.foundDefinition(def.getTokenRef(), this);
    			wasSuccessful = true;
    		}
    	}
    	
		return wasSuccessful;
	}
	
	private boolean manuallyResolveInParentScopes(Token identifier, BindingResolutionCallback bindings)
	{
		for (ScopingNode scope = getEnclosingScope(); scope != null; scope = scope.getEnclosingScope())
			if (scope.manuallyResolveInLocalScope(identifier, bindings))
				return true;
		
		return false;
	}

	private boolean manuallyResolveIntrinsic(Token identifier, BindingResolutionCallback bindings)
	{
    	Definition def = Intrinsics.resolveIntrinsic(identifier);
		if (def == null) return false;
    	
		((PhotranVPGBuilder)PhotranVPG.getInstance()).setDefinitionFor(identifier.getTokenRef(), def);
		return true;
	}

	private void attemptToDeclareImplicit(Token identifier, BindingResolutionCallback bindings)
	{
		PhotranVPGBuilder vpg = (PhotranVPGBuilder)PhotranVPG.getInstance();
		ImplicitSpec implicitSpec = getImplicitSpec();
		
		if (implicitSpec == null) return; // Implicit None
		
		if (identifier instanceof FakeToken) return; // Not a real token; used to test bindings only
		
    	String name = PhotranVPG.canonicalizeIdentifier(identifier.getText());
    	
    	PhotranTokenRef tokenRef = identifier.getTokenRef();
		Type type = implicitSpec.getType(name.charAt(0));
		Definition def = new Definition(tokenRef, Definition.Classification.IMPLICIT_LOCAL_VARIABLE, type);
		
    	vpg.setDefinitionFor(tokenRef, def);
    	vpg.markScope(tokenRef, this);
    	bindings.foundDefinition(tokenRef, getGlobalScope());
	}

	public List<Definition> getAllDefinitions()
	{
		PhotranVPG vpg = PhotranVPG.getInstance();
		List<Definition> result = new LinkedList<Definition>();
		
    	for (PhotranTokenRef t : vpg.db.getIncomingEdgeSources(this.getRepresentativeToken(), PhotranVPG.DEFINED_IN_SCOPE_EDGE_TYPE))
    		result.add(vpg.getDefinitionFor(t));
		
    	for (PhotranTokenRef t : vpg.db.getIncomingEdgeSources(this.getRepresentativeToken(), PhotranVPG.IMPORTED_INTO_SCOPE_EDGE_TYPE))
    		result.add(vpg.getDefinitionFor(t));
    	
    	return result;
	}

	public List<Definition> getAllPublicDefinitions()
	{
		List<Definition> result = new LinkedList<Definition>();
		
    	for (Definition def : getAllDefinitions())
    		if (def != null && def.isPublic())
    			result.add(def);
    	
    	return result;
	}

    public IMarker createMarker()
    {
        try
        {
        	Token firstToken = findFirstTokenIn(this);
        	Token lastToken = findLastTokenIn(this);
        	
        	if (firstToken == null || lastToken == null) return null;

            int startOffset = firstToken.getFileOffset();
            startOffset -= firstToken.getWhiteBefore().length();
            
            int endOffset = lastToken.getFileOffset()+lastToken.getLength();
            //endOffset += lastToken.getWhiteAfter().length();

            IMarker marker = firstToken.getFile().createMarker(IMarker.TEXT);
			marker.setAttribute(IMarker.CHAR_START, startOffset);
			marker.setAttribute(IMarker.CHAR_END, endOffset);
            return marker;
        }
        catch (CoreException e)
        {
            return null;
        }
    }

    // TODO: This was copied from FortranRefactoring.java
    // Parse Tree Searching ///////////////////////////////////////////////////
    
    protected Token findFirstTokenIn(InteriorNode node)
    {
        try
        {
            node.visitUsing(new GenericParseTreeVisitor()
            {
                @Override
                public void visitToken(Token token)
                {
                    throw new Notification(token);
                }
                
            });
        }
        catch (Notification n)
        {
            return (Token)n.getResult();
        }
        return null;
    }
    
    private final static class LastTokenVisitor extends GenericParseTreeVisitor
    {
        private Token lastToken;
        
        @Override
        public void visitToken(Token token)
        {
            lastToken = token;
        }
        
        public Token getLastToken() { return lastToken; }
    }

    protected Token findLastTokenIn(InteriorNode node)
    {
        LastTokenVisitor lastTokenVisitor = new LastTokenVisitor();
        node.visitUsing(lastTokenVisitor);
        return lastTokenVisitor.getLastToken();
    }
}
