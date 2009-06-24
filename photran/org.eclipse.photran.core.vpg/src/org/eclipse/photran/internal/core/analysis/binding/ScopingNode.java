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
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IMarker;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.photran.core.vpg.PhotranTokenRef;
import org.eclipse.photran.core.vpg.PhotranVPG;
import org.eclipse.photran.core.vpg.PhotranVPGBuilder;
import org.eclipse.photran.core.vpg.util.Notification;
import org.eclipse.photran.internal.core.analysis.binding.Definition.Visibility;
import org.eclipse.photran.internal.core.analysis.types.Type;
import org.eclipse.photran.internal.core.lexer.Terminal;
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
import org.eclipse.photran.internal.core.parser.IInternalSubprogram;
import org.eclipse.photran.internal.core.parser.Parser.ASTNode;
import org.eclipse.photran.internal.core.parser.Parser.ASTVisitor;
import org.eclipse.photran.internal.core.parser.Parser.IASTListNode;
import org.eclipse.photran.internal.core.parser.Parser.IASTNode;

/**
 * An AST node representing a scope.
 * <p>
 * (View the type hierarchy to see which nodes are scoping nodes.)
 * 
 * @author Jeff Overbey
 */
public abstract class ScopingNode extends ASTNode
{
    public static ScopingNode getEnclosingScope(IASTNode node)
    {
        for (IASTNode candidate = node.getParent(); candidate != null; candidate = candidate.getParent())
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

    public static ScopingNode getLocalScope(IASTNode node)
    {
        for (IASTNode candidate = node.getParent(); candidate != null; candidate = candidate.getParent())
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
    private static boolean shouldBeBoundToOuterScope(IASTNode node)
    {
    	IASTNode parent = node.getParent();
    	if (parent == null) return false;
    	IASTNode grandparent = parent.getParent();
    	if (grandparent == null) return false;
    	
    	if (isDeclStmtForScope(parent))
    	{
    	    if (parent instanceof ASTFunctionStmtNode && node == ((ASTFunctionStmtNode)parent).getName()) // result clause
    	        return false;
    	    else
    	        return true;
    	}
    	else if (parent instanceof ASTProgramNameNode
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
                return isDeclStmtForScope(grandparent);
		}
    	else return false;
	}

    private static boolean isDeclStmtForScope(IASTNode node)
    {
        return node instanceof ASTProgramStmtNode
        	|| node instanceof ASTFunctionStmtNode
        	|| node instanceof ASTSubroutineStmtNode
        	|| node instanceof ASTModuleStmtNode
        	|| node instanceof ASTBlockDataStmtNode
        	|| node instanceof ASTDerivedTypeStmtNode
        	|| node instanceof ASTInterfaceStmtNode
        	|| node instanceof ASTEndProgramStmtNode
        	|| node instanceof ASTEndFunctionStmtNode
        	|| node instanceof ASTEndSubroutineStmtNode
        	|| node instanceof ASTEndModuleStmtNode
        	|| node instanceof ASTEndBlockDataStmtNode
        	|| node instanceof ASTEndTypeStmtNode
        	|| node instanceof ASTEndInterfaceStmtNode;
    }

	private static boolean inAnonymousInterface(IASTNode n)
	{
		for (IASTNode node = n.getParent(); node != null; node = node.getParent())
			if (node instanceof ASTInterfaceBlockNode && isAnonymousInterface((ASTInterfaceBlockNode)node))
				return true;
		return false;
	}

	public static boolean isScopingNode(IASTNode node)
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
    	IASTNode result = this;
    	
    	// Find root of AST (i.e., topmost ASTExecutableProgramNode)
    	while (result.getParent() != null) result = result.getParent();
    	
    	return (ScopingNode)result;
    }

    /*
     * According to a profile obtained (6/24/09) by running
     *     org.eclipse.photran.cmdline/vpgstats-profiled
     * on
     *     org.eclipse.photran-projects.confidential.fmlib
     * caching the representative token for a scope reduced
     * the Binder's maximal times as follows:
     *                            BEFORE============   AFTER============
     *     DefinitionCollector:     8771 ms (FM.f90)    3475 ms (FM.f90)
     *     ImplicitSpecCollector:   1816 ms (FM.f90)     404 ms (FM.f90)
     *     ReferenceCollector:    122225 ms (FM.f90)   35226 ms (FM.f90)
     */
    private PhotranTokenRef cachedRepresentataiveToken = null;

    public PhotranTokenRef getRepresentativeToken()
    {
        if (cachedRepresentataiveToken == null)
            cachedRepresentataiveToken = internalGetRepresentativeToken();
        
        return cachedRepresentataiveToken;
    }

    private PhotranTokenRef internalGetRepresentativeToken()
    {
    	// TODO: GET RID OF THIS MESS AFTER INDIVIDUAL NODES CAN BE CUSTOMIZED
    	// AND DYNAMICALLY DISPATCHED TO!
    	
    	if (this instanceof ASTExecutableProgramNode)
    	{
    		try
    		{
	    		this.accept(new ASTVisitor()
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

    public ASTNode getHeaderStmt()
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

    public IASTListNode<? extends IASTNode /*IBodyConstruct*/> getBody()
    {
        // TODO: GET RID OF THIS MESS AFTER INDIVIDUAL NODES CAN BE CUSTOMIZED
        // AND DYNAMICALLY DISPATCHED TO!
        
        if (this instanceof ASTExecutableProgramNode)
            return null;
        else if (this instanceof ASTMainProgramNode)
            return ((ASTMainProgramNode)this).getBody();
        else if (this instanceof ASTFunctionSubprogramNode)
            return ((ASTFunctionSubprogramNode)this).getBody();
        else if (this instanceof ASTSubroutineSubprogramNode)
            return ((ASTSubroutineSubprogramNode)this).getBody();
        else if (this instanceof ASTModuleNode)
            return ((ASTModuleNode)this).getModuleBody();
        else if (this instanceof ASTBlockDataSubprogramNode)
            return ((ASTBlockDataSubprogramNode)this).getBlockDataBody();
        else if (this instanceof ASTDerivedTypeDefNode)
            return ((ASTDerivedTypeDefNode)this).getDerivedTypeBody();
        else if (this instanceof ASTInterfaceBlockNode)
            return ((ASTInterfaceBlockNode)this).getInterfaceBlockBody();
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
    	for (IASTNode node = scope.getParent(); node != null; node = node.getParent())
    		if (node == this)
    			return true;
    	
    	return false;
    }

    /** @return this scope and all scopes nested within it */
    public List<ScopingNode> getAllContainedScopes()
    {
		final List<ScopingNode> scopes = new LinkedList<ScopingNode>();

		this.accept(new ASTVisitor()
		{
			@Override public void visitASTNode(IASTNode node)
			{
				if (isScopingNode(node))
					scopes.add((ScopingNode)node);
				super.visitASTNode(node);
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
	
	public Iterable<ScopingNode> findImportingScopes()
	{
//		PhotranVPG vpg = PhotranVPG.getInstance();
//		
//		Set<PhotranTokenRef> result = new HashSet<PhotranTokenRef>();
//		for (Definition def : getAllDefinitions())
//		    if (def != null) // TODO: Why are we getting null here?
//		        for (TokenRef<Token> t : vpg.db.getOutgoingEdgeTargets(def.getTokenRef(), PhotranVPG.IMPORTED_INTO_SCOPE_EDGE_TYPE))
//		            result.add((PhotranTokenRef)t);
//	    	
//	    List<ScopingNode> scopes = new LinkedList<ScopingNode>();
//	    for (PhotranTokenRef tokenRef : result)
//	    	scopes.add(tokenRef.findToken().getEnclosingScope());
//	    return scopes;

		// Find all references to things defined in this scope (presumably a module),
		// and sort them by file and offset
        Set<PhotranTokenRef> allReferences = new TreeSet<PhotranTokenRef>();
        for (Definition def : getAllDefinitions())
            if (def != null)
                allReferences.addAll(def.findAllReferences(false));

        // Now go through each file, parse it, and collect the containing scope's representative token
        final Set<PhotranTokenRef> scopes = new HashSet<PhotranTokenRef>();
        for (PhotranTokenRef ref : allReferences)
            scopes.add(ref.findToken().findNearestAncestor(ScopingNode.class).getRepresentativeToken());
        
        // And return an iterable that will parse files and change the representative tokens back into ScopingNodes
        return new Iterable<ScopingNode>()
        {
            public Iterator<ScopingNode> iterator()
            {
                return new TokenRefToScopeIterator(scopes);
            }
        };
	}
	
	private static class TokenRefToScopeIterator implements Iterator<ScopingNode>
	{
        private Iterator<PhotranTokenRef> it;
        
        public TokenRefToScopeIterator(Set<PhotranTokenRef> scopes)
        {
            this.it = scopes.iterator();
        }

        public boolean hasNext()
        {
            return it.hasNext();
        }

        public ScopingNode next()
        {
            return findScopingNodeForRepresentativeToken(it.next());
        }

        public void remove()
        {
            throw new UnsupportedOperationException();
        }
	}
	
	public static ScopingNode findScopingNodeForRepresentativeToken(PhotranTokenRef tr)
	{
	    PhotranVPG vpg = PhotranVPG.getInstance();
	    
	    if (tr.getOffset() < 0)
	        return vpg.acquireTransientAST(tr.getFilename()).getRoot();
	    else
	        return vpg.findToken(tr).findNearestAncestor(ScopingNode.class);
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

    public List<PhotranTokenRef> manuallyResolveNoImplicits(Token identifier)
    {
        final List<PhotranTokenRef> bindings = new LinkedList<PhotranTokenRef>();
        
        manuallyResolveNoImplicits(identifier, new BindingResolutionCallback()
        {
            public void foundDefinition(PhotranTokenRef definition, ScopingNode scope)
            {
                bindings.add(definition);
            }
        });
        
        return bindings;
    }

    private void manuallyResolveNoImplicits(Token identifier, BindingResolutionCallback result)
    {
        if (!manuallyResolveInLocalScope(identifier, result))
            if (!manuallyResolveInParentScopes(identifier, result))
                manuallyResolveIntrinsic(identifier, result);
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
		Definition def = new Definition(identifier.getText(), tokenRef, Definition.Classification.IMPLICIT_LOCAL_VARIABLE, /*Visibility.PUBLIC,*/ type);
		
    	vpg.setDefinitionFor(tokenRef, def);
    	vpg.markScope(tokenRef, this);
    	vpg.markDefinitionVisibilityInScope(tokenRef, this, Visibility.PUBLIC);
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
        PhotranVPG vpg = PhotranVPG.getInstance();
		List<Definition> result = new LinkedList<Definition>();
		
    	for (Definition def : getAllDefinitions())
    		if (def != null && vpg.getVisibilityFor(def, this).equals(Visibility.PUBLIC))
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
    
    protected Token findFirstTokenIn(ASTNode node)
    {
        try
        {
            node.accept(new ASTVisitor()
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
    
    private final static class LastTokenVisitor extends ASTVisitor
    {
        private Token lastToken;
        
        @Override
        public void visitToken(Token token)
        {
            lastToken = token;
        }
        
        public Token getLastToken() { return lastToken; }
    }

    protected Token findLastTokenIn(ASTNode node)
    {
        LastTokenVisitor lastTokenVisitor = new LastTokenVisitor();
        node.accept(lastTokenVisitor);
        return lastTokenVisitor.getLastToken();
    }

    public boolean isNamed(String targetName)
    {
        String name = getName();
        if (name == null) return false;
        
        String actualName = PhotranVPG.canonicalizeIdentifier(name);
        String expectedName = PhotranVPG.canonicalizeIdentifier(targetName);
        return actualName.equals(expectedName);
    }

    public String getName()
    {
        Token nameToken = getNameToken();
        return nameToken == null ? null : nameToken.getText();
    }
    
    public Token getNameToken()
    {
        Token repToken = getRepresentativeToken().findTokenOrReturnNull();
        if (repToken == null || repToken.getTerminal() != Terminal.T_IDENT)
            return null;
        else
            return repToken;
    }
}
