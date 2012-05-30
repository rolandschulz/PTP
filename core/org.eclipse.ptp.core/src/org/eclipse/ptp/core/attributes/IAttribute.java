/*******************************************************************************
 * Copyright (c) 2006 The Regents of the University of California. 
 * This material was produced under U.S. Government contract W-7405-ENG-36 
 * for Los Alamos National Laboratory, which is operated by the University 
 * of California for the U.S. Department of Energy. The U.S. Government has 
 * rights to use, reproduce, and distribute this software. NEITHER THE 
 * GOVERNMENT NOR THE UNIVERSITY MAKES ANY WARRANTY, EXPRESS OR IMPLIED, OR 
 * ASSUMES ANY LIABILITY FOR THE USE OF THIS SOFTWARE. If software is modified 
 * to produce derivative works, such modified software should be clearly marked, 
 * so as not to confuse it with the version available from LANL.
 * 
 * Additionally, this program and the accompanying materials 
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * LA-CC 04-115
 *******************************************************************************/
package org.eclipse.ptp.core.attributes;

@Deprecated
public interface IAttribute<T, A extends IAttribute<T, A, D>, D extends IAttributeDefinition<T, A, D>> extends Comparable<A> {

	/**
	 * @return a copy of this attribute
	 * @since 4.0
	 */
	public A copy();

	/**
	 * @return the attribute definition object associated with this attribute
	 */
	public D getDefinition();

	/**
	 * @return
	 */
	public T getValue();

	/**
	 * @return string
	 */
	public String getValueAsString();

	/**
	 * @return whether this attribute is enabled.
	 */
	public boolean isEnabled();

	/**
	 * Is the string parsable to a valid attribute of this type?
	 * 
	 * @param string
	 * @return
	 */
	public boolean isValid(String string);

	/**
	 * @param value
	 * @throws IllegalValueException
	 */
	public void setValue(T value) throws IllegalValueException;

	/**
	 * @param string
	 * @throws IAttribute.IllegalValue
	 */
	public void setValueAsString(String string) throws IllegalValueException;
}
