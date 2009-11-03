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
package org.eclipse.photran.internal.core.tests.serialization;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.Serializable;

import junit.framework.TestCase;

import org.eclipse.photran.internal.core.analysis.binding.Definition;
import org.eclipse.photran.internal.core.analysis.binding.Definition.Classification;
import org.eclipse.photran.internal.core.analysis.types.DerivedType;
import org.eclipse.photran.internal.core.analysis.types.FunctionType;
import org.eclipse.photran.internal.core.analysis.types.Type;
import org.eclipse.photran.internal.core.vpg.PhotranTokenRef;
import org.eclipse.photran.internal.core.vpg.PhotranVPGSerializer;

/**
 * Tests serialization and deserialization using the {@link PhotranVPGSerializer}
 * 
 * @author Jeff Overbey
 */
public class TestVPGSerializer extends TestCase
{
    public void test() throws IOException
    {
        check(null);

        check("Hello");
        check("");
        
        check(3);
        check(100);
        check(-999);
        check(Integer.MAX_VALUE);
        check(Integer.MIN_VALUE);
        
        check(Type.INTEGER);
        check(Type.REAL);
        check(Type.DOUBLEPRECISION);
        check(Type.COMPLEX);
        check(Type.LOGICAL);
        check(Type.CHARACTER);
        check(Type.UNKNOWN);
        check(Type.VOID);
        check(Type.TYPE_ERROR);
        check(new PhotranTokenRef("/your/place/a.f90", -1, 0));
        check(new PhotranTokenRef("Test", 3, 5));
        check(new Definition("declaredName", new PhotranTokenRef("", -1, 0), Classification.ENUMERATOR, Type.INTEGER));
        check(new DerivedType("my_type"));
        check(new DerivedType("AnotherType"));
        check(new FunctionType("a_function"));
        check(new FunctionType("f"));
    }
    
    private void check(Serializable object) throws IOException
    {
        ByteArrayOutputStream out = new ByteArrayOutputStream();

        PhotranVPGSerializer.serialize(object, out);
        
        ByteArrayInputStream in = new ByteArrayInputStream(out.toByteArray());
        Serializable deserializedObject = PhotranVPGSerializer.deserialize(in);
        
        assertEquals(object, deserializedObject);
    }
}
