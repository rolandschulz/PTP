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
package org.eclipse.photran.internal.core.vpg;

import java.io.IOException;
import java.io.OutputStream;
import java.io.Serializable;

/**
 * An object that can be serialized as an annotation in the VPG (or part of an annotation).
 * 
 * @author Jeff Overbey
 * 
 * @see PhotranVPGSerializer
 * @see PhotranVPGDB#setAnnotation(PhotranTokenRef, int, Serializable)
 */
public interface IPhotranSerializable extends Serializable
{
    /**
     * @return a character unique to this class that identifies it when deserializing
     *  objects; this should be one of the CLASS_* constants defined at the top of
     *  {@link PhotranVPGSerializer} 
     */
    public char getSerializationCode();
    
    /**
     * Serializes this object to the given stream by invoking
     * {@link PhotranVPGSerializer#serialize(Serializable, OutputStream)}
     * on each of its fields
     */
    public void writeTo(OutputStream out) throws IOException;    
}
