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
import java.util.LinkedList;
import java.util.List;

import org.eclipse.photran.internal.core.vpg.PhotranVPGSerializer;

/**
 * Represents a function type, i.e., a {@link Type} with a list of argument types and a return type.
 * 
 * @author Jeff Overbey
 */
public class FunctionType extends Type
{
    private static final long serialVersionUID = 1L;
    
    // ***WARNING*** If any fields change, the serialization methods (below) must also change!
    private String name;
    private Type returnType = Type.UNKNOWN;
    private List<Type> argumentTypes = new LinkedList<Type>();

    public FunctionType(String name)
    {
        this.name = name.toLowerCase();
    }

    @Override public String toString()
    {
        return "function(" + name + "): " + argumentTypes + " -> " + returnType; //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
    }

    @Override public <T> T processUsing(TypeProcessor<T> p)
    {
        return p.ifFunctionType(name, this);
    }
    
    @Override public boolean equals(Object other)
    {
        // TODO: Does not consider scope
        return other instanceof FunctionType && ((FunctionType)other).name.equals(this.name);
    }
    
    @Override public int hashCode()
    {
        return name.hashCode();
    }

    ////////////////////////////////////////////////////////////////////////////////
    // IPhotranSerializable Implementation
    ////////////////////////////////////////////////////////////////////////////////
    
    public static String getStaticThreeLetterTypeSerializationCode()
    {
        return "fun"; //$NON-NLS-1$
    }
    
    @Override public String getThreeLetterTypeSerializationCode()
    {
        return "fun"; //$NON-NLS-1$
    }

//    private String name;
//    private Type returnType = Type.UNKNOWN;
//    private List<Type> argumentTypes = new LinkedList<Type>();

    @Override void finishWriteTo(OutputStream out) throws IOException
    {
        PhotranVPGSerializer.serialize(name, out);
        
        PhotranVPGSerializer.serialize(returnType, out);
        
        PhotranVPGSerializer.serialize(argumentTypes.size(), out);
        for (Type argType : argumentTypes)
            PhotranVPGSerializer.serialize(argType, out);
    }

    public static Type finishReadFrom(InputStream in) throws IOException
    {
        String name = PhotranVPGSerializer.deserialize(in);
        FunctionType result = new FunctionType(name);
        
        result.returnType = PhotranVPGSerializer.deserialize(in);
        
        int args = PhotranVPGSerializer.deserialize(in);
        for (int i = 0; i < args; i++)
            result.argumentTypes.add((Type)PhotranVPGSerializer.deserialize(in));
        
        return result;
    }
}
