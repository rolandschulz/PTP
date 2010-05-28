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

import org.eclipse.photran.internal.core.vpg.PhotranVPGSerializer;

/**
 * A {@link Type} representing a Fortran derived type with a particular name.
 * 
 * @author Jeff Overbey
 */
public class DerivedType extends Type
{
    private static final long serialVersionUID = 1L;
    
    // ***WARNING*** If any fields change, the serialization methods (below) must also change!
    private String name;

    public DerivedType(String name)
    {
        this.name = name.toLowerCase();
    }

    @Override public String toString()
    {
        return "type(" + name + ")"; //$NON-NLS-1$ //$NON-NLS-2$
    }
   
    @Override public <T> T processUsing(TypeProcessor<T> p)
    {
        return p.ifDerivedType(name, this);
    }
    
    @Override public boolean equals(Object other)
    {
        // TODO: Does not consider scope
        return other instanceof DerivedType && ((DerivedType)other).name.equals(this.name);
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
        return "dtv"; //$NON-NLS-1$
    }
    
    @Override public String getThreeLetterTypeSerializationCode()
    {
        return "dtv"; //$NON-NLS-1$
    }

    @Override void finishWriteTo(OutputStream out) throws IOException
    {
        PhotranVPGSerializer.serialize(name, out);
    }

    public static Type finishReadFrom(InputStream in) throws IOException
    {
        String name = PhotranVPGSerializer.deserialize(in);
        return new DerivedType(name);
    }
}
