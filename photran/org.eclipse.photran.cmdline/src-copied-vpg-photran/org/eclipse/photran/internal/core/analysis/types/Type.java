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
package org.eclipse.photran.internal.core.analysis.types;

import java.io.Serializable;

import org.eclipse.photran.internal.core.parser.ASTTypeSpecNode;

/**
 * An incomplete representation of the type of a Fortran expression or variable.
 * 
 * Does not include any information about kinds or character lengths.
 * Does not include any information about array indexing; that is stored in the <code>Definition</code> for that variable.
 * 
 * @author Jeff Overbey
 */
public abstract class Type implements Serializable
{
	private static final long serialVersionUID = 1L;
	
    public abstract String toString();
    
    public abstract <T> T processUsing(TypeProcessor<T> p);
    
    public Type getCommonType(Type another)
    {
        return this.equals(another) ? this : null;
    }
    
    @Override public boolean equals(Object other)
    {
        return other instanceof Type && ((Type)other).toString().equals(this.toString());
    }

    // # R502
    // <TypeSpec> ::=
    //   T_INTEGER
    // | T_REAL
    // | T_DOUBLEPRECISION
    // | T_COMPLEX
    // | T_LOGICAL
    // | T_CHARACTER
    // | T_INTEGER <KindSelector>
    // | T_REAL <KindSelector>
    // | T_DOUBLE T_PRECISION
    // | T_COMPLEX <KindSelector>
    // | T_CHARACTER <CharSelector>
    // | T_LOGICAL <KindSelector>
    // | T_TYPE T_LPAREN <TypeName> T_RPAREN
    
    public static Type parse(ASTTypeSpecNode node)
    {
        if (node.isInteger())
            return Type.INTEGER;
        else if (node.isReal())
            return Type.REAL;
        else if (node.isDouble())
            return Type.DOUBLEPRECISION;
        else if (node.isComplex())
            return Type.COMPLEX;
        else if (node.isLogical())
            return Type.LOGICAL;
        else if (node.isCharacter())
            return Type.CHARACTER;
        else if (node.isDerivedType())
            return node.getTypeName() == null ? new DerivedType("") : new DerivedType(node.getTypeName().getText());
        else
            throw new Error("Unexpected case parsing <TypeSpec> node");
    }

    public static Type INTEGER = new Type()
    {
        public String toString()
        {
            return "integer";
        }
        
        public <T> T processUsing(TypeProcessor<T> p)
        {
            return p.ifInteger(this);
        }

        @Override
        public Type getCommonType(Type other)
        {
            return this.processUsing(new TypeProcessor<Type>()
                                     {
                                        @Override public Type ifComplex(Type type) { return Type.COMPLEX; }
                                        @Override public Type ifDoublePrecision(Type type) { return Type.DOUBLEPRECISION; }
                                        @Override public Type ifInteger(Type type) { return Type.INTEGER; }
                                        @Override public Type ifReal(Type type) { return Type.REAL; }
                                     });
        }
    };

    public static Type REAL = new Type()
    {
        public String toString()
        {
            return "real";
        }
        
        public <T> T processUsing(TypeProcessor<T> p)
        {
            return p.ifReal(this);
        }

        @Override
        public Type getCommonType(Type other)
        {
            return this.processUsing(new TypeProcessor<Type>()
                                     {
                                        @Override public Type ifComplex(Type type) { return Type.COMPLEX; }
                                        @Override public Type ifDoublePrecision(Type type) { return Type.DOUBLEPRECISION; }
                                        @Override public Type ifInteger(Type type) { return Type.REAL; }
                                        @Override public Type ifReal(Type type) { return Type.REAL; }
                                     });
        }
    };

    public static Type DOUBLEPRECISION = new Type()
    {
        public String toString()
        {
            return "double precision";
        }
        
        public <T> T processUsing(TypeProcessor<T> p)
        {
            return p.ifDoublePrecision(this);
        }

        @Override
        public Type getCommonType(Type other)
        {
            return this.processUsing(new TypeProcessor<Type>()
                                     {
                                        @Override public Type ifComplex(Type type) { return Type.COMPLEX; }
                                        @Override public Type ifDoublePrecision(Type type) { return Type.DOUBLEPRECISION; }
                                        @Override public Type ifInteger(Type type) { return Type.DOUBLEPRECISION; }
                                        @Override public Type ifReal(Type type) { return Type.DOUBLEPRECISION; }
                                     });
        }
    };

    public static Type COMPLEX = new Type()
    {
        public String toString()
        {
            return "complex";
        }
        
        public <T> T processUsing(TypeProcessor<T> p)
        {
            return p.ifComplex(this);
        }

        @Override
        public Type getCommonType(Type other)
        {
            return this.processUsing(new TypeProcessor<Type>()
                                     {
                                        @Override public Type ifComplex(Type type) { return Type.COMPLEX; }
                                        @Override public Type ifDoublePrecision(Type type) { return Type.COMPLEX; }
                                        @Override public Type ifInteger(Type type) { return Type.COMPLEX; }
                                        @Override public Type ifReal(Type type) { return Type.COMPLEX; }
                                     });
        }
    };

    public static Type LOGICAL = new Type()
    {
        public String toString()
        {
            return "logical";
        }
        
        public <T> T processUsing(TypeProcessor<T> p)
        {
            return p.ifLogical(this);
        }
    };

    public static Type CHARACTER = new Type()
    {
        public String toString()
        {
            return "character";
        }
        
        public <T> T processUsing(TypeProcessor<T> p)
        {
            return p.ifCharacter(this);
        }
    };

    /**
     * Type of
     * <ul>
     * <li> entities imported from a module that could not be found or was not loaded
     * <li> interface blocks
     * <li> external subprograms
     * <li> intrinsics
     * <li> functions
     * </ul>
     */
    public static Type UNKNOWN = new Type()
    {
        public String toString()
        {
            return "(unknown)";
        }
        
        public <T> T processUsing(TypeProcessor<T> p)
        {
            return p.ifUnknown(this);
        }
    };

    /**
     * Name of a derived type, namelist, common block, where statement, program name, etc.
     */
    public static Type VOID = new Type()
    {
        public String toString()
        {
            return "(unclassified)";
        }
        
        public <T> T processUsing(TypeProcessor<T> p)
        {
            return p.ifUnclassified(this);
        }
    };

    /**
     * Type of expressions which do not type check
     */
    public static Type TYPE_ERROR = new Type()
    {
        public String toString()
        {
            return "(type error)";
        }
        
        public <T> T processUsing(TypeProcessor<T> p)
        {
            return p.ifError(this);
        }
    };
}
