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

import org.eclipse.photran.internal.core.analysis.binding.Definition;

/**
 * Allows for double-dispatch on a {@link Definition} in order to process its type.
 * 
 * @author Jeff Overbey
 * @param <T>
 * 
 * @see org.eclipse.photran.internal.core.analysis.binding.Definition#getType()
 * @see Type#processUsing(TypeProcessor)
 */
public abstract class TypeProcessor<T>
{
	public T ifInteger(Type type) {return null;}
	public T ifReal(Type type) {return null;}
	public T ifDoublePrecision(Type type) {return null;}
    public T ifComplex(Type type) {return null;}
    public T ifDoubleComplex(Type type) {return null;}
	public T ifLogical(Type type) {return null;}
	public T ifCharacter(Type type) {return null;}
    public T ifDerivedType(String derivedTypeName, DerivedType type) {return null;}
    public T ifFunctionType(String name, FunctionType functionType) {return null;}
    public T ifUnknown(Type type) {return null;}
    public T ifUnclassified(Type type) {return null;}
    public T ifError(Type type) {return null;}
}
