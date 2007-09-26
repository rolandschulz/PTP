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

import java.io.Serializable;
import java.util.ArrayList;

import org.eclipse.core.resources.IMarker;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.photran.core.vpg.PhotranTokenRef;
import org.eclipse.photran.core.vpg.PhotranVPG;
import org.eclipse.photran.internal.core.analysis.types.ArraySpec;
import org.eclipse.photran.internal.core.analysis.types.Type;
import org.eclipse.photran.internal.core.lexer.Token;
import org.eclipse.photran.internal.core.parser.ASTAccessSpecNode;
import org.eclipse.photran.internal.core.parser.ASTArraySpecNode;
import org.eclipse.photran.internal.core.parser.ASTAttrSpecNode;
import org.eclipse.photran.internal.core.parser.ASTAttrSpecSeqNode;
import org.eclipse.photran.internal.core.parser.ASTInternalSubprogramNode;
import org.eclipse.photran.internal.core.parser.ASTTypeSpecNode;
import org.eclipse.photran.internal.core.parser.Parser.CSTNode;

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
public class Definition implements Serializable
{
	private static final long serialVersionUID = 1L;
	
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
        VARIABLE_DECLARATION,
        WHERE;
        
        @Override public String toString()
        {
        	String name = super.toString().replaceAll("_", " ");
        	return name.charAt(0) + name.substring(1).toLowerCase();
        }
    }
    
    /** Enumerates visibilities of module entities */
    public static enum Visibility
    {
        PUBLIC,
        PRIVATE,
        INHERIT_FROM_SCOPE;
        
        @Override public String toString()
        {
        	String name = super.toString().replaceAll("_", " ");
        	return name.charAt(0) + name.substring(1).toLowerCase();
        }
    }

    protected Classification classification;
    protected PhotranTokenRef tokenRef;
    protected String canonicalizedName;
    protected Visibility visibility;
    protected Type type;
    protected ArraySpec arraySpec;
    
    private boolean subprogramArgument = false;

    protected Definition() {}
    
    /** Creates a definition and binds it to the given token */
    public Definition(PhotranTokenRef tokenRef, Classification classification, Type type)
    {
        this.classification = classification;
    	this.tokenRef = tokenRef;
    	this.canonicalizedName = canonicalize(tokenRef.getText());
        this.visibility = Visibility.INHERIT_FROM_SCOPE;
        this.type = type;
        this.arraySpec = null;
    }

	protected String canonicalize(String identifier)
    {
    	return identifier.toLowerCase();
    }
    
    public boolean matches(String canonicalizedName)
    {
    	return canonicalizedName.equals(this.canonicalizedName);
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
        for (CSTNode parent = tokenRef.findToken().getParent(); parent != null; parent = parent.getParent())
            if (parent instanceof ASTInternalSubprogramNode)
                return true;
        
        return false;
    }
    
    public boolean isExternallyVisibleSubprogramDefinition()
    {
        return !isInternal() && isSubprogram();
    }

    public boolean isExternallyVisibleModuleDefinition()
    {
        return !isInternal() && classification == Definition.Classification.MODULE;
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
        	result.append(" (");
        	result.append(type);
        	if (arraySpec != null) result.append(arraySpec);
        	result.append(")");
        }
        
        if (visibility != Visibility.INHERIT_FROM_SCOPE)
        	result.append(" (" + visibility + " Visibility)");
        
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
    void setAttributes(ASTAttrSpecSeqNode attrSpecSeq)
    {
        if (attrSpecSeq == null) return;
        
        for (int i = 0; i < attrSpecSeq.size(); i++)
            setAttribute(attrSpecSeq.getAttrSpec(i));
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

    private void setAttribute(ASTAttrSpecNode attrSpec)
    {
        ASTArraySpecNode arraySpec = attrSpec.getArraySpec();
        ASTAccessSpecNode accessSpec = attrSpec.getAccessSpec();
        
        if (arraySpec != null)
            setArraySpec(arraySpec);
        else if (accessSpec != null)
            setVisibility(accessSpec);

        // TODO: Intent, etc.
    }

    // # R511
    // <AccessSpec> ::=
    //     T_PUBLIC
    //   | T_PRIVATE

    void setVisibility(ASTAccessSpecNode accessSpec)
    {
        if (accessSpec.getTPublic() != null)
            this.visibility = Visibility.PUBLIC;
        else if (accessSpec.getTPrivate() != null)
            this.visibility = Visibility.PRIVATE;
    }
    
    boolean isPublic()
    {
        // TODO: Can interface blocks contain PRIVATE statements or can their members have visibilities specified?
        return this.visibility.equals(Visibility.PUBLIC)
            || this.visibility.equals(Visibility.INHERIT_FROM_SCOPE) && getTokenRef().findToken().getEnclosingScope().isDefaultVisibilityPrivate() == false;
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
    public ArrayList<PhotranTokenRef> findAllReferences()
    {
		ArrayList<PhotranTokenRef> result = new ArrayList<PhotranTokenRef>();
		
		for (TokenRef<Token> r : PhotranVPG.getInstance().getIncomingEdgeSources(tokenRef, PhotranVPG.BINDING_EDGE_TYPE))
			result.add((PhotranTokenRef)r);
		
		return result;
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
            && this.subprogramArgument == o.subprogramArgument
            && equals(this.tokenRef, o.tokenRef)
            && equals(this.type, o.type)
            && equals(this.visibility, o.visibility);
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
            + (this.subprogramArgument ? 1 : 0)
            + hashCode(this.tokenRef)
            + 0 //hashCode(this.type)
            + hashCode(this.visibility);
    }

    private int hashCode(Object o)
    {
        return o == null ? 0 : o.hashCode();
    }
}
