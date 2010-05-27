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

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

import org.eclipse.photran.internal.core.analysis.binding.Definition;
import org.eclipse.photran.internal.core.parser.ASTTypeSpecNode;
import org.eclipse.photran.internal.core.vpg.IPhotranSerializable;
import org.eclipse.photran.internal.core.vpg.PhotranVPGSerializer;

/**
 * Represents the type of a Fortran expression or variable.
 * <p>
 * Does not include any information about kinds or character lengths. Does not include any
 * information about array indexing; that is stored in the variable's {@link Definition}.
 * 
 * @author Jeff Overbey
 */
@SuppressWarnings("serial")
public abstract class Type implements IPhotranSerializable, Serializable
{
	private static final long serialVersionUID = 1L;

    // ***WARNING*** If any fields change, the serialization methods (below) must also change!

	@Override public abstract String toString();
    
    public abstract String getThreeLetterTypeSerializationCode();
    
    abstract void finishWriteTo(OutputStream out) throws IOException;
    
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

    private static abstract class PrimitiveType extends Type
    {
        @Override final void finishWriteTo(OutputStream out) throws IOException
        {
            // Nothing extra to write for primitive types
        }
    }
    
    public static Type INTEGER = new PrimitiveType()
    {
        @Override public String toString()
        {
            return "integer";
        }
        
        @Override public String getThreeLetterTypeSerializationCode()
        {
           return "int"; 
        }
        
        @Override public <T> T processUsing(TypeProcessor<T> p)
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

    public static Type REAL = new PrimitiveType()
    {
        @Override public String toString()
        {
            return "real";
        }
        
        @Override public String getThreeLetterTypeSerializationCode()
        {
           return "rea"; 
        }
        
        @Override public <T> T processUsing(TypeProcessor<T> p)
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

    public static Type DOUBLEPRECISION = new PrimitiveType()
    {
        @Override public String toString()
        {
            return "double precision";
        }
        
        @Override public String getThreeLetterTypeSerializationCode()
        {
           return "dbl"; 
        }
        
        @Override public <T> T processUsing(TypeProcessor<T> p)
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

    public static Type COMPLEX = new PrimitiveType()
    {
        @Override public String toString()
        {
            return "complex";
        }
        
        @Override public String getThreeLetterTypeSerializationCode()
        {
           return "cpx"; 
        }

        @Override public <T> T processUsing(TypeProcessor<T> p)
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

    public static Type LOGICAL = new PrimitiveType()
    {
        @Override public String toString()
        {
            return "logical";
        }
        
        @Override public String getThreeLetterTypeSerializationCode()
        {
           return "log"; 
        }

        @Override public <T> T processUsing(TypeProcessor<T> p)
        {
            return p.ifLogical(this);
        }
    };

    public static Type CHARACTER = new PrimitiveType()
    {
        @Override public String toString()
        {
            return "character";
        }
        
        @Override public String getThreeLetterTypeSerializationCode()
        {
           return "chr"; 
        }

        @Override public <T> T processUsing(TypeProcessor<T> p)
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
    public static Type UNKNOWN = new PrimitiveType()
    {
        @Override public String toString()
        {
            return "(unknown)";
        }
        
        @Override public String getThreeLetterTypeSerializationCode()
        {
           return "unk"; 
        }

        @Override public <T> T processUsing(TypeProcessor<T> p)
        {
            return p.ifUnknown(this);
        }
    };

    /**
     * Name of a derived type, namelist, common block, where statement, program name, etc.
     */
    public static Type VOID = new PrimitiveType()
    {
        @Override public String toString()
        {
            return "(unclassified)";
        }
        
        @Override public String getThreeLetterTypeSerializationCode()
        {
           return "voi"; 
        }

        @Override public <T> T processUsing(TypeProcessor<T> p)
        {
            return p.ifUnclassified(this);
        }
    };

    /**
     * Type of expressions which do not type check
     */
    public static Type TYPE_ERROR = new PrimitiveType()
    {
        @Override public String toString()
        {
            return "(type error)";
        }
        
        @Override public String getThreeLetterTypeSerializationCode()
        {
           return "err"; 
        }

        @Override public <T> T processUsing(TypeProcessor<T> p)
        {
            return p.ifError(this);
        }
    };
    
    ////////////////////////////////////////////////////////////////////////////////
    // IPhotranSerializable Implementation
    ////////////////////////////////////////////////////////////////////////////////
    
    private static Map<String, Object> threeLetterTypeSerializationCodes;
    
    private static void setThreeLetterTypeSerializationCode(Type type)
    {
        String code = type.getThreeLetterTypeSerializationCode();
        checkCode(code);
        threeLetterTypeSerializationCodes.put(code, type);
    }
    
    private static void setThreeLetterSerializationCode(Class<? extends Type> typeClass)
    {
        threeLetterTypeSerializationCodes.put((String)invokeStatic("getStaticThreeLetterTypeSerializationCode", typeClass, new Class<?>[0]), typeClass);
    }

    private static void checkCode(String code)
    {
        if (code == null || code.length() != 3)
            throw new IllegalArgumentException("Invalid three-letter code for Type serialization: " + code);
        else if (threeLetterTypeSerializationCodes.containsKey(code))
            throw new IllegalArgumentException("Duplicate three-letter code for Type serialization: " + code);
    }

    static
    {
        threeLetterTypeSerializationCodes = new HashMap<String, Object>();
        setThreeLetterTypeSerializationCode(INTEGER);
        setThreeLetterTypeSerializationCode(REAL);
        setThreeLetterTypeSerializationCode(DOUBLEPRECISION);
        setThreeLetterTypeSerializationCode(COMPLEX);
        setThreeLetterTypeSerializationCode(LOGICAL);
        setThreeLetterTypeSerializationCode(CHARACTER);
        setThreeLetterTypeSerializationCode(UNKNOWN);
        setThreeLetterTypeSerializationCode(VOID);
        setThreeLetterTypeSerializationCode(TYPE_ERROR);
        setThreeLetterSerializationCode(DerivedType.class);
        setThreeLetterSerializationCode(FunctionType.class);
    }
    
    public static Type readFrom(InputStream in) throws IOException
    {
        String code = PhotranVPGSerializer.deserialize(in);
        if (!threeLetterTypeSerializationCodes.containsKey(code))
            throw new IOException("Unrecognized type code: " + code);
        
        Object o = threeLetterTypeSerializationCodes.get(code);
        if (o instanceof Type)
            return (Type)o;
        else if (o instanceof Class)
            return invokeStatic("finishReadFrom", (Class<?>)o, new Class<?>[] { InputStream.class }, in);
        else
            throw new IOException();
    }

    public void writeTo(OutputStream out) throws IOException
    {
        PhotranVPGSerializer.serialize(getThreeLetterTypeSerializationCode(), out);
        finishWriteTo(out);
    }
    
    public char getSerializationCode()
    {
        return PhotranVPGSerializer.CLASS_TYPE;
    }

    
    @SuppressWarnings("unchecked")
    private static <T> T invokeStatic(String method, Class<?> o, Class<?>[] argTypes, Object... args)
    {
        try
        {
            return (T)o.getMethod(method, argTypes).invoke(null, args);
        }
        catch (Exception e)
        {
            throw new Error(e.getClass().getSimpleName() + ": " + e.getMessage());
        }
    }
}
