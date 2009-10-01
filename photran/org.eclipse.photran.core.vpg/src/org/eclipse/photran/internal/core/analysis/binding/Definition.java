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

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;
import java.util.TreeSet;

import org.eclipse.core.resources.IMarker;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.photran.core.vpg.IPhotranSerializable;
import org.eclipse.photran.core.vpg.PhotranTokenRef;
import org.eclipse.photran.core.vpg.PhotranVPG;
import org.eclipse.photran.core.vpg.PhotranVPGBuilder;
import org.eclipse.photran.core.vpg.PhotranVPGSerializer;
import org.eclipse.photran.internal.core.analysis.types.ArraySpec;
import org.eclipse.photran.internal.core.analysis.types.DerivedType;
import org.eclipse.photran.internal.core.analysis.types.FunctionType;
import org.eclipse.photran.internal.core.analysis.types.Type;
import org.eclipse.photran.internal.core.analysis.types.TypeProcessor;
import org.eclipse.photran.internal.core.lexer.Token;
import org.eclipse.photran.internal.core.lexer.Token.FakeToken;
import org.eclipse.photran.internal.core.parser.ASTAccessSpecNode;
import org.eclipse.photran.internal.core.parser.ASTArraySpecNode;
import org.eclipse.photran.internal.core.parser.ASTAttrSpecNode;
import org.eclipse.photran.internal.core.parser.ASTAttrSpecSeqNode;
import org.eclipse.photran.internal.core.parser.ASTExternalStmtNode;
import org.eclipse.photran.internal.core.parser.ASTFunctionSubprogramNode;
import org.eclipse.photran.internal.core.parser.ASTIntentSpecNode;
import org.eclipse.photran.internal.core.parser.ASTInterfaceBlockNode;
import org.eclipse.photran.internal.core.parser.ASTMainProgramNode;
import org.eclipse.photran.internal.core.parser.ASTModuleNode;
import org.eclipse.photran.internal.core.parser.ASTSubroutineSubprogramNode;
import org.eclipse.photran.internal.core.parser.ASTTypeSpecNode;
import org.eclipse.photran.internal.core.parser.ISpecificationStmt;
import org.eclipse.photran.internal.core.parser.Parser.GenericASTVisitor;
import org.eclipse.photran.internal.core.parser.Parser.IASTListNode;
import org.eclipse.photran.internal.core.parser.Parser.IASTNode;

import bz.over.vpg.TokenRef;

/**
 * A declaration of a variable, subprogram, main program, interface,
 * or similar entity.  Note that IF-statements, DO-loops, etc. can
 * also be named, so they may have Definitions as well.
 * <p>
 * A Definition's <b>classification</b> indicates the entity it is defining: variable, function, module, etc.
 * <p>
 * When applicable, the Definition's <b>type</b> gives the type of that entity (integer, real, etc.); its
 * <b>array spec</b> may give an array specification (e.g., one-dimensional, indexed from 3 to 5).
 *
 * @author Jeff Overbey
 */
public class Definition implements IPhotranSerializable, Comparable<Definition>
{
	private static final long serialVersionUID = 1L;

    // ***WARNING*** If the enum values change order or new values are inserted in the middle, serialization will break!

	/** Enumerates the various entities that can be named with an identifier: variables, functions, namelists, etc. */
    public static enum Classification
    {
        BLOCK_DATA,
        COMMON_BLOCK,
        DERIVED_TYPE,
        DERIVED_TYPE_COMPONENT,
        DO,
        ENTRY,
        EXTERNAL,
        FORALL,
        FUNCTION,
        IMPLICIT_LOCAL_VARIABLE,
        IF,
        INTERFACE,
        INTRINSIC,
        MAIN_PROGRAM,
        MODULE,
        MODULE_ENTITY_BEFORE_RENAME, // subprogram name before rename
        NAMELIST,
        RENAMED_MODULE_ENTITY, // subprogram name after rename
        SUBROUTINE,
        SELECT,
        VARIABLE_DECLARATION { @Override public String toString() { return "Local variable"; } },
        WHERE,
        ENUMERATOR, // F03
        ;

        @Override public String toString()
        {
        	String name = super.toString().replaceAll("_", " ");
        	return name.charAt(0) + name.substring(1).toLowerCase();
        }
    }

    // ***WARNING*** If the enum values change order or new values are inserted in the middle, serialization will break!

    /** Enumerates visibilities of module entities */
    public static enum Visibility
    {
        PUBLIC,
        PRIVATE;
        //INHERIT_FROM_SCOPE;

        @Override public String toString()
        {
        	String name = super.toString().replaceAll("_", " ");
        	return name.charAt(0) + name.substring(1).toLowerCase();
        }
    }

    // ***WARNING*** If any fields change, the serialization methods (below) must also change!

    protected Classification classification;
    protected PhotranTokenRef tokenRef;
    protected String declaredName, canonicalizedName;
    //protected Visibility visibility;
    protected Type type;
    protected ArraySpec arraySpec;

    private boolean subprogramArgument = false;
    private boolean parameter = false;
    private boolean typeBoundProcedure = false;
    private boolean renamedTypeBoundProcedure = false;
    private boolean target = false;
    private boolean pointer = false;
    private boolean allocatable = false;
    private boolean intent_in = false;
    private boolean intent_out = false;
    private boolean optional = false;
    private boolean save = false;

    // ***WARNING*** If any fields change, the serialization methods (below) must also change!

    protected Definition() {}

    /** Creates a definition and binds it to the given token */
    public Definition(String declaredName, PhotranTokenRef tokenRef, Classification classification, /*Visibility visibility,*/ Type type)
    {
        this.classification = classification;
    	this.tokenRef = tokenRef;
    	this.declaredName = declaredName;
    	this.canonicalizedName = canonicalize(declaredName);
        //this.visibility = visibility; //Visibility.INHERIT_FROM_SCOPE;
        this.type = type;
        this.arraySpec = null;
    }

	protected String canonicalize(String identifier)
    {
    	return identifier.toLowerCase();
    }

    public boolean matches(String canonicalizedName)
    {
        return canonicalizedName != null &&
            (canonicalizedName.equals(this.canonicalizedName) || this.canonicalizedName.matches(canonicalizedName));
    }

    void markAsSubprogramArgument()
    {
        this.subprogramArgument = true;
    }

    public boolean isSubprogramArgument()
    {
        return subprogramArgument;
    }

    private boolean isInternal()
    {
        IASTNode startFrom = tokenRef.findToken();
        if (this.isSubprogram()) startFrom = startFrom.findNearestAncestor(ScopingNode.class); // Look upward from this function/subroutine

        for (IASTNode parent = startFrom.getParent(); parent != null; parent = parent.getParent())
            if (parent instanceof ASTModuleNode
                || parent instanceof ASTSubroutineSubprogramNode
                || parent instanceof ASTFunctionSubprogramNode
                || parent instanceof ASTMainProgramNode)
                return true;

        return false;
    }

    public boolean isInternalSubprogramDefinition()
    {
        return isInternal() && isSubprogram();
    }

    public boolean isExternallyVisibleSubprogramDefinition()
    {
        return !isInternal() && isSubprogram();
    }

    public boolean isModuleReference()
    {
        return false;
    }

    public boolean isLocalVariable()
    {
        return classification == Classification.IMPLICIT_LOCAL_VARIABLE
            || classification == Classification.VARIABLE_DECLARATION
            ;//|| classification == Classification.IMPLIED_FUNCTION_RESULT_VARIABLE;
        // TODO: More?
    }

    public boolean isDerivedType()
    {
        return classification == Classification.DERIVED_TYPE;
    }

    public boolean isSubprogram()
    {
        return classification == Classification.SUBROUTINE
            || classification == Classification.FUNCTION;
    }

    public boolean isModule()
    {
        return classification == Classification.MODULE;
    }

    public boolean isExternal()
    {
        return classification == Classification.EXTERNAL;
    }

    public boolean isInterface()
    {
        return classification == Classification.INTERFACE;
    }

    public boolean isRenamedModuleEntity()
    {
        return classification == Classification.RENAMED_MODULE_ENTITY;
    }

    public boolean isModuleEntityBeforeRename()
    {
        return classification == Classification.MODULE_ENTITY_BEFORE_RENAME;
    }

    public boolean isMainProgram()
    {
        return classification == Classification.MAIN_PROGRAM;
    }

    public boolean isIntrinsic()
    {
        return classification == Classification.INTRINSIC;
    }

    public boolean isImplicit()
    {
        return this.classification == Classification.IMPLICIT_LOCAL_VARIABLE
        ;//|| this.classification == Classification.IMPLIED_FUNCTION_RESULT_VARIABLE;
    }

    public boolean isNamelist()
    {
        return classification == Classification.NAMELIST;
    }

    public boolean isCommon()
    {
        return classification == Classification.COMMON_BLOCK;
    }

    public boolean isBlockData()
    {
        return classification == Classification.BLOCK_DATA;
    }

    /** @return the classification */
    public Classification getClassification()
    {
        return classification;
    }

    /** Sets the type of this definition (integer, real, etc.) according to the given TypeSpec node */
    void setType(ASTTypeSpecNode typeSpecNode)
    {
        this.type = Type.parse(typeSpecNode);
    }

    /** Sets the type of this definition (integer, real, etc.) */
    void setType(Type type)
    {
        this.type = type;
    }

    /** @return the type of this node (integer, real, etc.) */
    public Type getType()
    {
        return type;
    }

    /** @return the name of the entity this defines, cased according to its declaration */
    public String getDeclaredName()
    {
        return declaredName;
    }

    /** @return the name of the entity this defines, canonicalized by <code>PhotranVPG.canonicalizeIdentifier</code> */
    public String getCanonicalizedName()
    {
        return canonicalizedName;
    }

    /** @return a description of the type of entity being defined */
    public String describeClassification()
    {
    	StringBuilder result = new StringBuilder();

        result.append(classification.toString());

        if (!type.equals(Type.VOID))
        {
        	result.append(" - ");
        	result.append(type);
        	if (arraySpec != null) result.append(arraySpec);
        }

//        if (visibility != Visibility.INHERIT_FROM_SCOPE)
//        	result.append(" (" + visibility + ")");

        return result.toString();
    }

    /** @return the location of the token containing this definition */
    public PhotranTokenRef getTokenRef()
    {
    	return tokenRef;
    }

    /** Sets the array spec for this definition */
    void setArraySpec(ASTArraySpecNode arraySpecNode)
    {
        if (arraySpecNode != null)
            this.arraySpec = new ArraySpec(arraySpecNode);
    }

    /** @return the array spec for this definition */
    public ArraySpec getArraySpec()
    {
        return arraySpec;
    }

    /** @return true iff this is the definition of an array */
    public boolean isArray()
    {
        return arraySpec != null;
    }

    // <AttrSpecSeq> ::=
    //   T_COMMA <AttrSpec>
    // | @:<AttrSpecSeq> T_COMMA <AttrSpec>

    /** Sets the attributes according to an AttrSpecSeq node */
    void setAttributes(IASTListNode<ASTAttrSpecSeqNode> listNode, ScopingNode setInScope)
    {
        if (listNode == null) return;

        for (int i = 0; i < listNode.size(); i++)
            setAttribute(listNode.get(i).getAttrSpec(), setInScope);
    }

    // # R503
    // <AttrSpec> ::=
    //   T_PARAMETER
    // | <AccessSpec>
    // | T_ALLOCATABLE
    // | T_DIMENSION T_LPAREN <ArraySpec> T_RPAREN
    // | T_EXTERNAL
    // | T_INTENT T_LPAREN <IntentSpec> T_RPAREN
    // | T_INTRINSIC
    // | T_OPTIONAL
    // | T_POINTER
    // | T_SAVE
    // | T_TARGET

    private void setAttribute(ASTAttrSpecNode attrSpec, ScopingNode setInScope)
    {
        ASTArraySpecNode arraySpec = attrSpec.getArraySpec();
        ASTAccessSpecNode accessSpec = attrSpec.getAccessSpec();

        if (arraySpec != null)
            setArraySpec(arraySpec);
        else if (accessSpec != null)
            setVisibility(accessSpec, setInScope);
        else if (attrSpec.isParameter())
            setParameter();
        else if (attrSpec.isPointer())
            setPointer();
        else if (attrSpec.isTarget())
            setTarget();
        else if (attrSpec.isAllocatable())
            setAllocatable();
        else if (attrSpec.isIntent())
            setIntent(attrSpec.getIntentSpec());
        else if (attrSpec.isOptional())
            setOptional();
        else if (attrSpec.isSave())
            setSave();
    }

    // # R511
    // <AccessSpec> ::=
    //     T_PUBLIC
    //   | T_PRIVATE

    void setVisibility(ASTAccessSpecNode accessSpec, ScopingNode setInScope)
    {
//        if (accessSpec.isPublic())
//            this.visibility = Visibility.PUBLIC;
//        else if (accessSpec.isPrivate())
//            this.visibility = Visibility.PRIVATE;

        ((PhotranVPGBuilder)PhotranVPG.getInstance()).markDefinitionVisibilityInScope(
            tokenRef,
            setInScope,
            accessSpec.isPrivate() ? Visibility.PRIVATE : Visibility.PUBLIC);
    }

    /** @return true iff this entity was declared as a PARAMETER (i.e., it a constant variable) */
    public boolean isParameter() { return parameter; }
    void setParameter() { this.parameter = true; }

    /** @return true iff this entity was declared as a POINTER */
    public boolean isPointer() { return pointer; }
    void setPointer() { this.pointer = true; }

    /** @return true iff this entity was declared as a POINTER */
    public boolean isTarget() { return target; }
    void setTarget() { this.target = true; }

    /** @return true iff this entity was declared as ALLOCATABLE */
    public boolean isAllocatable() { return allocatable; }
    void setAllocatable() { this.allocatable = true; }

    /** @return true iff this entity was declared as OPTIONAL */
    public boolean isOptional() { return optional; }
    void setOptional() { this.optional = true; }

    /** @return true iff this entity was declared as SAVE */
    public boolean isSave() { return save; }
    void setSave() { this.save = true; }

    /** @return true iff this entity was declared with INTENT(IN) */
    public boolean isIntentIn() { return intent_in; }
    /** @return true iff this entity was declared with INTENT(OUT) */
    public boolean isIntentOut() { return intent_out; }
    void setIntent(ASTIntentSpecNode intent)
    {
        if (intent.isIntentIn() || intent.isIntentInOut())
            this.intent_in = true;
        if (intent.isIntentOut() || intent.isIntentInOut())
            this.intent_out = true;
    }

//    boolean isPublic()
//    {
//        // TODO: Can interface blocks contain PRIVATE statements or can their members have visibilities specified?
//        return this.visibility.equals(Visibility.PUBLIC);
//            //|| this.visibility.equals(Visibility.INHERIT_FROM_SCOPE) && getTokenRef().findToken().getEnclosingScope().isDefaultVisibilityPrivate() == false;
//    }

    void markAsTypeBoundProcedure(boolean renamed)
    {
        this.typeBoundProcedure = true;
        this.renamedTypeBoundProcedure = renamed;
    }

    public boolean isTypeBoundProcedure()
    {
        return this.typeBoundProcedure;
    }

    public boolean isRenamedTypeBoundProcedure()
    {
        return this.renamedTypeBoundProcedure;
    }

    public IMarker createMarker()
    {
        try
        {
            IMarker marker = tokenRef.getFile().createMarker(IMarker.TEXT);
            marker.setAttribute(IMarker.CHAR_START, tokenRef.getOffset());
            marker.setAttribute(IMarker.CHAR_END, tokenRef.getEndOffset());
            return marker;
        }
        catch (CoreException e)
        {
            return null;
        }
    }

    /** @return all workspace references to this definition, not including renamed references */
    public Set<PhotranTokenRef> findAllReferences(boolean shouldBindInterfacesAndExternals)
    {
        if (this.isImpliedFunctionResultVar() && findEnclosingFunctionDefinition() != null)
            return findAllReferencesToEnclosingSubprogramInstead(shouldBindInterfacesAndExternals);
        else
            return internalFindAllReferences(shouldBindInterfacesAndExternals);
    }

    private Set<PhotranTokenRef> findAllReferencesToEnclosingSubprogramInstead(boolean aggressive)
    {
        Definition fnDef = findEnclosingFunctionDefinition();
        Set<PhotranTokenRef> result = fnDef.internalFindAllReferences(aggressive);
        result.add(fnDef.getTokenRef());
        result.remove(this.getTokenRef());
        return result;
    }

    private boolean isImpliedFunctionResultVar()
    {
        if (!this.isLocalVariable()) return false;

        ASTFunctionSubprogramNode fn = findEnclosingFunction();
        if (fn != null && !fn.getFunctionStmt().hasResultClause())
        {
            Definition fnDef = findEnclosingFunctionDefinition();
            return this.getCanonicalizedName().equals(fnDef.getCanonicalizedName());
        }
        return false;
    }

    private ASTFunctionSubprogramNode findEnclosingFunction()
    {
        return this.getTokenRef().findToken().findNearestAncestor(ASTFunctionSubprogramNode.class);
    }

    private Definition findEnclosingFunctionDefinition()
    {
        ASTFunctionSubprogramNode fn = findEnclosingFunction();
        return fn == null ? null : PhotranVPG.getInstance().getDefinitionFor(fn.getRepresentativeToken());
    }

    private Set<PhotranTokenRef> internalFindAllReferences(boolean shouldBindInterfacesAndExternals)
    {
		if ((this.isSubprogram() || this.isExternal()) && shouldBindInterfacesAndExternals)
		    return internalFindAllReferencesToSubprogramAggressively();
		else
		    return findAllImmediateReferences();
    }

    private Set<PhotranTokenRef> findAllImmediateReferences()
    {
        Set<PhotranTokenRef> result = new TreeSet<PhotranTokenRef>();
        addImmediateBindings(result);
        if (this.isFunction())
            matchFunctionAndImpliedResultVariable(result);
        result.remove(this.getTokenRef()); // By contract, the set of references does not include this
        return result;
    }

    private void matchFunctionAndImpliedResultVariable(Collection<PhotranTokenRef> result)
    {
        try
        {
            ASTFunctionSubprogramNode fn = findEnclosingFunction();
            if (fn == null || fn.getFunctionStmt().hasResultClause()) return;

            for (Definition def : fn.getAllDefinitions())
            {
                if (def.getCanonicalizedName().equals(this.getCanonicalizedName()))
                {
                    result.add(def.getTokenRef());
                    result.addAll(def.internalFindAllReferences(false));
                }
            }
        }
        catch (Throwable t)
        {
            // Ignore
        }
    }

    private boolean isFunction()
    {
        return this.getClassification().equals(Classification.FUNCTION);
    }

    private void addImmediateBindings(Collection<PhotranTokenRef> result)
    {
        result.add(this.getTokenRef());

        for (TokenRef<Token> r : PhotranVPG.getDatabase().getIncomingEdgeSources(tokenRef, PhotranVPG.BINDING_EDGE_TYPE))
            result.add((PhotranTokenRef)r);
    }

    private Set<PhotranTokenRef> internalFindAllReferencesToSubprogramAggressively()
    {
        assert this.isSubprogram() || this.isExternal();

        Collection<Definition> subprogramDefinitions;
        if (this.isInInterfaceBlock())
            subprogramDefinitions = this.resolveInterfaceBinding();
        else if (this.isInExternalStmt())
            subprogramDefinitions = this.resolveExternalBinding();
        else if (this.isExternallyVisibleSubprogramDefinition())
            subprogramDefinitions = this.findAllSimilarlyNamedExternalSubprograms();
        else // probably an internal subprogram
            return findAllImmediateReferences();

        Set<PhotranTokenRef> result = new TreeSet<PhotranTokenRef>();
        result.addAll(findAllImmediateReferences()); // e.g., PRIVATE referring to subprogram in an INTERFACE block
        for (Definition subprogram : subprogramDefinitions)
            result.addAll(subprogram.internalFindAllReferencesToSubprogIncludingInterfacesAndExternalStmts());
        result.remove(this.getTokenRef()); // By contract, the set of references does not include this
        return result;
    }

    private Set<PhotranTokenRef> internalFindAllReferencesToSubprogIncludingInterfacesAndExternalStmts()
    {
        assert this.isExternallyVisibleSubprogramDefinition();

        Set<PhotranTokenRef> result = new TreeSet<PhotranTokenRef>();
        addExternalSubprogramDefinitions(result);
        addInterfaceDecls(result);
        addExternalStmts(result);
        return result;
    }

    private void addExternalSubprogramDefinitions(Collection<PhotranTokenRef> result)
    {
        for (Definition externalSubprogDef : this.findAllSimilarlyNamedExternalSubprograms())
        {
            result.add(externalSubprogDef.getTokenRef());
            result.addAll(externalSubprogDef.internalFindAllReferences(false));
        }
    }

    private void addInterfaceDecls(Collection<PhotranTokenRef> result)
    {
        for (Definition interfaceDef : this.findMatchingDeclarationsInInterfaces())
        {
            result.add(interfaceDef.getTokenRef());
            result.addAll(interfaceDef.internalFindAllReferences(false));
        }
    }

    private void addExternalStmts(Collection<PhotranTokenRef> result)
    {
        for (Definition externalDef : this.findMatchingDeclarationsInExternalStmts())
        {
            result.add(externalDef.getTokenRef());
            result.addAll(externalDef.internalFindAllReferences(false));
        }
    }

    /** @return true iff this is an entity defined inside an INTERFACE block */
    public boolean isExternalSubprogramReferenceInInterfaceBlock()
    {
        if (!isInInterfaceBlock()) return false;

        Token token = getTokenRef().findToken();
        ScopingNode scopeOfThisDef = token.getEnclosingScope();

        HashSet<Definition> result = collectResolutions(token, scopeOfThisDef);
        return !resolvesToSubprogramArgument(result);
    }

//    private boolean isInAnonymousInterfaceBlock()
//    {
//        if (!isInInterfaceBlock()) return false;
//        ASTInterfaceStmtNode stmt = tokenRef.findToken().findNearestAncestor(ASTInterfaceBlockNode.class).getInterfaceStmt();
//        return stmt.getGenericName() == null && stmt.getGenericSpec() == null;
//    }

    ///////////////////////////////////////////////////////////////////////////////////////////////////////////////////

    /**
     * @return if this is a subprogram declared in an INTERFACE block, a list of all possible matching subprogram
     * Definitions; otherwise, <code>null</code>
     *
     * @see #findMatchingDeclarationsInInterfaces()
     */
    public Collection<Definition> resolveInterfaceBinding()
    {
        if (!isInInterfaceBlock()) return Collections.emptySet();

        Token token = getTokenRef().findToken();
        ScopingNode scopeOfThisDef = token.getEnclosingScope();
        ScopingNode parentScope = scopeOfThisDef.getEnclosingScope();

        HashSet<Definition> result = collectResolutions(token, scopeOfThisDef);
        if (resolvesToSubprogramArgument(result)) return Collections.emptyList();
        if (needToResolveInParentScope(result)) result = collectResolutions(token, parentScope);
        result.addAll(collectMatchingExternalSubprograms(token));
        return result;
    }

    /** @return true iff this is an entity defined inside an INTERFACE block */
    public boolean isInInterfaceBlock()
    {
        Token tok = getTokenRef().findTokenOrReturnNull();
        return tok != null && tok.findNearestAncestor(ASTInterfaceBlockNode.class) != null;
    }

    private HashSet<Definition> collectResolutions(Token token, ScopingNode scope)
    {
        HashSet<Definition> result = new HashSet<Definition>();
        for (PhotranTokenRef d : scope.manuallyResolve(new FakeToken(token, token.getText())))
        {
            Definition def = PhotranVPG.getInstance().getDefinitionFor(d);
            if (def != null)
                if ((def.isSubprogram() && !def.isInInterfaceBlock()) || def.isSubprogramArgument())
                    result.add(def);
        }
        return result;
    }

    private boolean resolvesToSubprogramArgument(Set<Definition> listOfDefs)
    {
        for (Definition def : listOfDefs)
            if (def != null && def.isSubprogramArgument())
                return true;

        return false;
    }

    private boolean needToResolveInParentScope(HashSet<Definition> result)
    {
        // If the subprogram declaration in the INTERFACE block only resolves to
        // itself, then we should check the parent scope: There might be a
        // matching subprogram imported from a module (or defined as an external
        // subprogram) there.

        return result.size() < 2;
    }

    private ArrayList<Definition> collectMatchingExternalSubprograms(Token token)
    {
        return PhotranVPG.getInstance().findAllExternalSubprogramsNamed(token.getText());
    }

    /**
     * @return if this is an external subprogram, a list of all other external subprograms with the same name
     * (i.e., subprograms that might also be referenced by a similar INTERFACE block or EXTERNAL statement)
     */
    public Collection<Definition> findAllSimilarlyNamedExternalSubprograms()
    {
        return PhotranVPG.getInstance().findAllExternalSubprogramsNamed(this.canonicalizedName);
    }

    ///////////////////////////////////////////////////////////////////////////////////////////////////////////////////

    /**
     * @return if this is a subprogram declared in an EXTERNAL statement, a list of all possible matching subprogram
     * Definitions; otherwise, <code>null</code>
     *
     * @see #findMatchingDeclarationsInExternalStmts()
     */
    public Collection<Definition> resolveExternalBinding()
    {
        if (!isInExternalStmt()) return Collections.emptySet();

        Token token = getTokenRef().findToken();
        ScopingNode scopeOfThisDef = token.getEnclosingScope();
        ScopingNode parentScope = scopeOfThisDef.getEnclosingScope();

        HashSet<Definition> result = collectExternalResolutions(token, scopeOfThisDef);
        if (resolvesToSubprogramArgument(result)) return Collections.emptyList();
        if (needToResolveInParentScope(result)) result = collectResolutions(token, parentScope);
        result.addAll(collectMatchingExternalSubprograms(token));
        return result;
    }

    /** @return true iff this is an entity defined inside an EXTERNAL statement */
    public boolean isInExternalStmt()
    {
        Token tok = getTokenRef().findTokenOrReturnNull();
        return tok != null && tok.findNearestAncestor(ASTExternalStmtNode.class) != null;
    }

    private HashSet<Definition> collectExternalResolutions(Token token, ScopingNode scope)
    {
        HashSet<Definition> result = new HashSet<Definition>();
        for (PhotranTokenRef d : scope.manuallyResolve(new FakeToken(token, token.getText())))
        {
            Definition def = PhotranVPG.getInstance().getDefinitionFor(d);
            if (def != null)
                if ((def.isSubprogram() && !def.isInInterfaceBlock()) || def.isSubprogramArgument())
                    result.add(def);
        }
        return result;
    }

    ///////////////////////////////////////////////////////////////////////////////////////////////////////////////////

    /**
     * @return if this is an external subprogram, a list of all possible matching declarations in INTERFACE blocks;
     * otherwise, the empty set
     *
     * @see #resolveInterfaceBinding()
     */
    public Collection<Definition> findMatchingDeclarationsInInterfaces()
    {
        if /*(isModuleSubprogram())
            return findMatchesForModuleSubprogram();
        else if*/ (isExternallyVisibleSubprogramDefinition())
            return PhotranVPG.getInstance().findAllDeclarationsInInterfacesForExternalSubprogram(canonicalizedName);
        else
            return Collections.emptySet();
    }

//    private boolean isModuleSubprogram()
//    {
//        return isSubprogram() && getTokenRef().findToken().findNearestAncestor(ASTModuleNode.class) != null;
//    }
//
//    private List<PhotranTokenRef> findMatchesForModuleSubprogram()
//    {
//        // TODO Auto-generated method stub
//        return null;
//    }

    /**
     * @return if this is an external subprogram, a list of all possible matching declarations in EXTERNAL statements;
     * otherwise, <code>null</code>
     *
     * @see #resolveExternalBinding()
     */
    public Collection<Definition> findMatchingDeclarationsInExternalStmts()
    {
        return PhotranVPG.getInstance().findAllDeclarationsInExternalStmts(canonicalizedName);
    }

    @Override public String toString()
    {
        return canonicalizedName
        	+ " - "
        	+ classification
        	+ (subprogramArgument ? " (Subprogram Argument)" : "")
        	+ " ("
        	+ tokenRef.getFilename()
        	+ ", offset "
        	+ tokenRef.getOffset()
        	+ ")";
    }


    @Override public boolean equals(Object other)
    {
        if (!(other instanceof Definition)) return false;

        Definition o = (Definition)other;
        return equals(this.arraySpec, o.arraySpec)
            && equals(this.canonicalizedName, o.canonicalizedName)
            && equals(this.classification, o.classification)
            && this.parameter == o.parameter
            && this.subprogramArgument == o.subprogramArgument
            && equals(this.tokenRef, o.tokenRef)
            && equals(this.type, o.type)
            ; // && equals(this.visibility, o.visibility);
    }

    private boolean equals(Object a, Object b)
    {
        if (a == null && b == null)
            return true;
        else if (a != null && b != null)
            return a.equals(b);
        else
            return false;
    }

    @Override public int hashCode()
    {
        return hashCode(this.arraySpec)
            + hashCode(this.canonicalizedName)
            + hashCode(this.classification)
            + (this.parameter ? 1 : 0)
            + (this.subprogramArgument ? 1 : 0)
            + hashCode(this.tokenRef)
            + 0 //hashCode(this.type)
            ; // + hashCode(this.visibility);
    }

    private int hashCode(Object o)
    {
        return o == null ? 0 : o.hashCode();
    }

    public int compareTo(Definition o)
    {
        return canonicalizedName.compareTo(o.canonicalizedName);
    }

    public String describe()
    {
        String commentsBefore = "\n", name = getCanonicalizedName(), commentsAfter = "";
        boolean isScopingUnit = false;

        Token tok = tokenRef.findTokenOrReturnNull();
        if (tok != null)
        {
            if (!name.equals("(anonymous)"))
                name = tok.getText();

            ScopingNode localScope = tok.getLocalScope();
            if (localScope != null)
            {
                isScopingUnit = (localScope != tok.getEnclosingScope());

                IASTNode headerStmt;
                if (isScopingUnit)
                    headerStmt = localScope.getHeaderStmt();
                else
                    headerStmt = findEnclosingSpecificationStmt(tok);

                if (headerStmt != null)
                {
                    Token first = headerStmt.findFirstToken();
                    Token last = headerStmt.findLastToken();
                    if (first != null) commentsBefore = first.getWhiteBefore();
                    if (last != null) commentsAfter = last.getWhiteAfter();

                    Token after = findFirstTokenAfter(last, localScope);
                    if (after != null && !startsWithBlankLine(after.getWhiteBefore()))
                        commentsAfter += after.getWhiteBefore();
                }
            }
        }

        return commentsBefore + describe(name) + "\n" + commentsAfter;
    }

    private boolean startsWithBlankLine(String string)
    {
        while (string.startsWith(" ") || string.startsWith("\t"))
            string = string.substring(1);

        return string.startsWith("\r") || string.startsWith("\n");
    }

    private Token findFirstTokenAfter(final Token target, ScopingNode localScope)
    {
        class TokenFinder extends GenericASTVisitor
        {
            private Token lastToken = null;
            private Token result = null;

            @Override public void visitToken(Token thisToken)
            {
                if (lastToken == target)
                    result = thisToken;

                lastToken = thisToken;
            }
        }

        TokenFinder t = new TokenFinder();
        localScope.accept(t);
        return t.result;
    }

    private IASTNode findEnclosingSpecificationStmt(Token tok)
    {
        for (IASTNode candidate = tok.getParent(); candidate != null; candidate = candidate.getParent())
            if (candidate instanceof ISpecificationStmt)
                return candidate;

        return null;
    }

    private String describe(String name)
    {
        StringBuilder sb = new StringBuilder();

        switch (classification)
        {
        case VARIABLE_DECLARATION:
            sb.append("! "); sb.append(describeClassification()); sb.append('\n');
            sb.append(describeType());
            sb.append(":: ");
            sb.append(name);
            break;

        case IMPLICIT_LOCAL_VARIABLE:
            sb.append("! "); sb.append(describeClassification()); sb.append('\n');
            sb.append(describeType());
            sb.append(":: ");
            sb.append(name);
            break;

        case SUBROUTINE:
            sb.append("subroutine ");
            sb.append(name);
            //TODO: describeParameters(def.getType());
            break;

        case FUNCTION:
            //TODO: describeReturnType(def.getType());
            sb.append("function ");
            sb.append(name);
            //TODO: describeParameters(def.getType());
            break;

        case INTERFACE:
            sb.append("interface ");
            sb.append(name);
            // TODO: Describe contents
            break;

        case MODULE:
            sb.append("module ");
            sb.append(name);
            // TODO: Describe contents
            break;

        case DERIVED_TYPE:
            sb.append("type :: ");
            sb.append(name);
            // TODO: Describe contents
            break;

        default:
            sb.append("! "); sb.append(describeClassification()); sb.append('\n');
            sb.append(name);
        }

        return sb.toString();
    }

    private String describeType()
    {
        return type.processUsing(new TypeProcessor<String>()
        {
            @Override
            public String ifCharacter(Type type)
            {
                return "character ";
            }

            @Override
            public String ifComplex(Type type)
            {
                return "complex ";
            }

            @Override
            public String ifDerivedType(String derivedTypeName,
                                        DerivedType type)
            {
                return "type(" + derivedTypeName + ") ";
            }

            @Override
            public String ifDoublePrecision(Type type)
            {
                return "double precision ";
            }

            @Override
            public String ifFunctionType(String name,
                                         FunctionType functionType)
            {
                return "function ";
            }

            @Override
            public String ifInteger(Type type)
            {
                return "integer ";
            }

            @Override
            public String ifLogical(Type type)
            {
                return "logical ";
            }

            @Override
            public String ifReal(Type type)
            {
                return "real ";
            }

            @Override
            public String ifUnclassified(Type type)
            {
                return "";
            }

            @Override
            public String ifUnknown(Type type)
            {
                return "";
            }
        });
    }

    ////////////////////////////////////////////////////////////////////////////////
    // IPhotranSerializable Implementation
    ////////////////////////////////////////////////////////////////////////////////

    public static Definition readFrom(InputStream in) throws IOException
    {
        Definition result = new Definition();
        result.classification = Classification.values()[PhotranVPGSerializer.deserialize(in)];
        result.tokenRef = PhotranVPGSerializer.deserialize(in);
        result.declaredName = PhotranVPGSerializer.deserialize(in);
        result.canonicalizedName = PhotranVPG.canonicalizeIdentifier(result.declaredName);
        result.type = PhotranVPGSerializer.deserialize(in);
        result.arraySpec = PhotranVPGSerializer.deserialize(in);
        result.subprogramArgument = PhotranVPGSerializer.deserialize(in);
        result.parameter = PhotranVPGSerializer.deserialize(in);
        result.typeBoundProcedure = PhotranVPGSerializer.deserialize(in);
        result.renamedTypeBoundProcedure = PhotranVPGSerializer.deserialize(in);
        result.pointer = PhotranVPGSerializer.deserialize(in);
        result.target = PhotranVPGSerializer.deserialize(in);
        result.allocatable = PhotranVPGSerializer.deserialize(in);
        result.intent_in = PhotranVPGSerializer.deserialize(in);
        result.intent_out = PhotranVPGSerializer.deserialize(in);
        result.optional = PhotranVPGSerializer.deserialize(in);
        result.save = PhotranVPGSerializer.deserialize(in);
        return result;
    }

    public void writeTo(OutputStream out) throws IOException
    {
        PhotranVPGSerializer.serialize(classification.ordinal(), out);
        PhotranVPGSerializer.serialize(tokenRef, out);
        PhotranVPGSerializer.serialize(declaredName, out);
        PhotranVPGSerializer.serialize(type, out);
        PhotranVPGSerializer.serialize(arraySpec, out);
        PhotranVPGSerializer.serialize(subprogramArgument, out);
        PhotranVPGSerializer.serialize(parameter, out);
        PhotranVPGSerializer.serialize(typeBoundProcedure, out);
        PhotranVPGSerializer.serialize(renamedTypeBoundProcedure, out);
        PhotranVPGSerializer.serialize(pointer, out);
        PhotranVPGSerializer.serialize(target, out);
        PhotranVPGSerializer.serialize(allocatable, out);
        PhotranVPGSerializer.serialize(intent_in, out);
        PhotranVPGSerializer.serialize(intent_out, out);
        PhotranVPGSerializer.serialize(optional, out);
        PhotranVPGSerializer.serialize(save, out);
    }

    public char getSerializationCode()
    {
        return PhotranVPGSerializer.CLASS_DEFINITION;
    }
}
