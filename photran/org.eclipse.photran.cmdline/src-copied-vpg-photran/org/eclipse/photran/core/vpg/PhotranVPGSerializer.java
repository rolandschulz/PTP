/*******************************************************************************
 * Copyright (c) 2009 University of Illinois at Urbana-Champaign and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    UIUC - Initial API and implementation
 *******************************************************************************/
package org.eclipse.photran.core.vpg;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.Serializable;

import org.eclipse.photran.internal.core.analysis.binding.Definition;
import org.eclipse.photran.internal.core.analysis.binding.ImplicitSpec;
import org.eclipse.photran.internal.core.analysis.types.ArraySpec;
import org.eclipse.photran.internal.core.analysis.types.Dimension;
import org.eclipse.photran.internal.core.analysis.types.Type;

/**
 * Serializes {@link IPhotranSerializable} objects for storage as annotations in the VPG.
 * 
 * @author Jeff Overbey
 * 
 * @see IPhotranSerializable
 * @see PhotranVPGDB#setAnnotation(PhotranTokenRef, int, Serializable)
 */
/*
 * The VPG originally used Java's built-in serialization mechanism (see commented-out
 * code in PhotranVPGDB#serialize(Serializable)}, but according to a profile, that was
 * consuming significant time in the POP and LAPACK test applications; using this custom
 * serialization mechanism improved indexing time by 28% and reduced the database size by
 * about half (in POP's case).
 */
public class PhotranVPGSerializer
{
    private PhotranVPGSerializer() {;}
    
    // EACH SERIALIZABLE CLASS MUST HAVE A UNIQUE LETTER
    public static final byte CLASS_NULL         = '0';
    public static final byte CLASS_STRING       = 's';
    public static final byte CLASS_INT          = 'i';
    public static final byte CLASS_BOOLEAN      = 'b';
    public static final byte CLASS_TOKENREF     = 'T';
    public static final byte CLASS_DEFINITION   = 'D';
    public static final byte CLASS_TYPE         = 'Y';
    public static final byte CLASS_ARRAYSPEC    = 'A';
    public static final byte CLASS_DIMENSION    = 'M';
    public static final byte CLASS_IMPLICITSPEC = 'I';

    protected static IOException readFailure()
    {
        return new IOException("VPG annotation corrupted; read failed");
    }

    ////////////////////////////////////////////////////////////////////////////////
    // Serialization
    ////////////////////////////////////////////////////////////////////////////////

    /*
     * WHEN OBJECTS ARE SERIALIZED...
     * 
     * (1) The first byte written is the CLASS_* character, as defined above,
     *     indicating what the class of the object is
     * 
     * (2) The #writeTo method is called on the object, which then calls back to
     *     this class to serialize its sub-objects (i.e., the values of its fields).
     *     
     * Since null, String, int, and boolean don't have #readFrom methods, they are
     * handled using custom serialization code below.
     */
    
    public static void serialize(int annotation, OutputStream out) throws IOException
    {
        out.write(CLASS_INT);
        writeInt(annotation, out);
    }

    public static void serialize(boolean annotation, OutputStream out) throws IOException
    {
        out.write(CLASS_BOOLEAN);
        writeBoolean(annotation, out);
    }

    public static void serialize(String annotation, OutputStream out) throws IOException
    {
        if (annotation == null) { serializeNull(out); return; }

        out.write(CLASS_STRING);
        writeString((String)annotation, out);
    }

    public static void serialize(IPhotranSerializable annotation, OutputStream out) throws IOException
    {
        if (annotation == null) { serializeNull(out); return; }

        IPhotranSerializable ann = (IPhotranSerializable)annotation;
        out.write(ann.getSerializationCode());
        //serialize(annotation.getClass().getSimpleName(), out);
        ann.writeTo(out);
    }

    private static void serializeNull(OutputStream out) throws IOException
    {
        out.write(CLASS_NULL);
    }

    ////////////////////////////////////////////////////////////////////////////////
    // Deserialization
    ////////////////////////////////////////////////////////////////////////////////

    /*
     * WHEN OBJECTS ARE DESERIALIZED...
     * 
     * (1) The first byte read is matched against the CLASS_* constants
     *     above to determine the class of the serialized object
     * 
     * (2) The #readFrom method is called on the class, which then calls back to
     *     this class to deserialize its sub-objects (i.e., the values of its fields).
     *     
     * Since null, String, int, and boolean don't have #readFrom methods, they are
     * handled using custom serialization code below.
     */
    
    @SuppressWarnings("unchecked")
    public static <T extends Serializable> T deserialize(InputStream in)
    {
        try
        {
            int code = in.read();
            switch (code)
            {
                case CLASS_NULL:        return null;
                case CLASS_STRING:      return (T)readString(in);
                case CLASS_INT:         return (T)Integer.valueOf(readInt(in));
                case CLASS_BOOLEAN:     return (T)Boolean.valueOf(readBoolean(in));
                default:

                    //String className = deserialize(in); System.out.println(className);
                    switch (code)
                    {
                        case CLASS_TOKENREF:     return (T)PhotranTokenRef.readFrom(in);
                        case CLASS_DEFINITION:   return (T)Definition.readFrom(in);
                        case CLASS_TYPE:         return (T)Type.readFrom(in);
                        case CLASS_ARRAYSPEC:    return (T)ArraySpec.readFrom(in);
                        case CLASS_DIMENSION:    return (T)Dimension.readFrom(in);
                        case CLASS_IMPLICITSPEC: return (T)ImplicitSpec.readFrom(in);
                        default:                 throw new Error("Unknown class code in deserialization: " + Integer.toString(code));
                    }
            }
        }
        catch (IOException e)
        {
            throw new Error(e);
        }
    }
    
    ////////////////////////////////////////////////////////////////////////////////
    // Methods for (de)serializing primitives and Strings
    ////////////////////////////////////////////////////////////////////////////////

    private static void writeString(String string, OutputStream out) throws IOException
    {
        if (string == null)
        {
            writeInt(-1, out);
        }
        else
        {
            byte[] stringBytes = string.getBytes();
            writeInt(stringBytes.length, out);
            out.write(stringBytes);
        }
    }
    
    private static String readString(InputStream in) throws IOException
    {
        int numBytesInString = readInt(in);
        if (numBytesInString < 0)
        {
            return null;
        }
        else
        {
            byte[] stringBytes = new byte[numBytesInString];
            in.read(stringBytes);
            return new String(stringBytes);
        }
    }
    
    private static void writeInt(int value, OutputStream out) throws IOException
    {
        out.write((value & 0xFF000000) >>> 24);
        out.write((value & 0x00FF0000) >>> 16);
        out.write((value & 0x0000FF00) >>> 8);
        out.write((value & 0x000000FF) >>> 0);
    }
    
    private static int readInt(InputStream in) throws IOException
    {
        byte bytes[] = new byte[4];
        in.read(bytes);
        return
              ((bytes[0] & 0xFF) << 24)
            | ((bytes[1] & 0xFF) << 16)
            | ((bytes[2] & 0xFF) << 8)
            | ((bytes[3] & 0xFF) << 0);
    }
    
    private static void writeBoolean(boolean value, OutputStream out) throws IOException
    {
        out.write(value ? 'T' : 'F');
    }
    
    private static boolean readBoolean(InputStream in) throws IOException
    {
        switch (in.read())
        {
            case 'T':   return true;
            case 'F':   return false;
            default:    throw readFailure();
        }
    }

    ////////////////////////////////////////////////////////////////////////////////
    // Serializes arbitrary objects, including boxed primitives
    ////////////////////////////////////////////////////////////////////////////////

    public static void serialize(Serializable object, OutputStream out) throws IOException
    {
        // We could use reflection to find the right overload...
        //         PhotranVPGSerializer.class.getMethod("serialize",
        //                                              object.getClass(),
        //                                              OutputStream.class).invoke(object, out);
        //  ...but it's ridiculously slow

        if (object == null)
            serialize((String)null, out);
        else if (object instanceof String)
            serialize((String)object, out);
        else if (object instanceof Integer)
            serialize(((Integer)object).intValue(), out);
        else if (object instanceof Boolean)
            serialize(((Boolean)object).booleanValue(), out);
        else if (object instanceof IPhotranSerializable)
            serialize((IPhotranSerializable)object, out);
        else
            throw new IllegalArgumentException("Cannot serialize " + object.getClass().getSimpleName());
    }
}
