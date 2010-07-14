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

import org.eclipse.photran.internal.core.analysis.binding.VariableAccess;
import org.eclipse.photran.internal.core.vpg.PhotranVPG;
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
    private List<String> argumentNames = new LinkedList<String>();
    private List<Type> argumentTypes = new LinkedList<Type>();
    private List<VariableAccess> argumentIntents = new LinkedList<VariableAccess>();

    public FunctionType(String name)
    {
        this.name = name.toLowerCase();
    }
    
    /** @return the return type of the function */
    public Type getReturnType()
    {
        return returnType;
    }

    /**
     * @param argument the index of the function argument (0 = first argument, 1 = second, etc.)
     * @return the type of the given argument
     */
    public Type getArgumentType(int argument)
    {
        if (argument < 0 || argument >= argumentIntents.size())
            return Type.TYPE_ERROR;
        else
            return argumentTypes.get(argument);
    }

    /**
     * @param argument the index of the function argument (0 = first argument, 1 = second, etc.)
     * @return the type of the given argument 
     */
    public Type getArgumentType(String argName)
    {
        return getArgumentType(argumentNames.indexOf(PhotranVPG.canonicalizeIdentifier(argName)));
    }

    /**
     * @param argument the index of the function argument (0 = first argument, 1 = second, etc.)
     * @return {@link VariableAccess#READ} if the given argument is intent(in),
     *         {@link VariableAccess#WRITE} if the given argument is intent(out), and
     *         {@link VariableAccess#RW} otherwise. 
     */
    public VariableAccess getArgumentAccess(int argument)
    {
        if (argument < 0 || argument >= argumentIntents.size())
            return VariableAccess.RW;
        else
            return argumentIntents.get(argument);
    }

    /**
     * @param argument the index of the function argument (0 = first argument, 1 = second, etc.)
     * @return {@link VariableAccess#READ} if the given argument is intent(in),
     *         {@link VariableAccess#WRITE} if the given argument is intent(out), and
     *         {@link VariableAccess#RW} otherwise. 
     */
    public VariableAccess getArgumentAccess(String argName)
    {
        return getArgumentAccess(argumentNames.indexOf(PhotranVPG.canonicalizeIdentifier(argName)));
    }

    public void setReturnType(Type type)
    {
        this.returnType = type;
    }

    public void addArgument(String name, Type type, VariableAccess intent)
    {
        if (name == null) name = ""; //$NON-NLS-1$
        if (type == null) type = Type.UNKNOWN;
        if (intent == null) intent = VariableAccess.RW;

        argumentNames.add(PhotranVPG.canonicalizeIdentifier(name));
        argumentTypes.add(type);
        argumentIntents.add(intent);
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
        
        int args = argumentTypes.size();
        
        PhotranVPGSerializer.serialize(args, out);
        for (int i = 0; i < args; i++)
        {
            PhotranVPGSerializer.serialize(argumentNames.get(i), out);
            PhotranVPGSerializer.serialize(argumentTypes.get(i), out);
            PhotranVPGSerializer.serialize(argumentIntents.get(i), out);
        }
    }

    public static Type finishReadFrom(InputStream in) throws IOException
    {
        String name = PhotranVPGSerializer.deserialize(in);
        FunctionType result = new FunctionType(name);
        
        result.returnType = PhotranVPGSerializer.deserialize(in);
        
        int args = PhotranVPGSerializer.deserialize(in);
        for (int i = 0; i < args; i++)
        {
            result.argumentNames.add((String)PhotranVPGSerializer.deserialize(in));
            result.argumentTypes.add((Type)PhotranVPGSerializer.deserialize(in));
            result.argumentIntents.add((VariableAccess)PhotranVPGSerializer.deserialize(in));
        }
        
        return result;
    }
}
