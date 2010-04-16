/*******************************************************************************
 * Copyright (c) 2010 Dieter Krachtus and The University of Tennessee,
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Dieter Krachtus (dieter.krachtus@gmail.com) and Roland Schulz - initial API and implementation

*******************************************************************************/

package org.eclipse.ptp.rm.proxy.core.element;

import java.util.Collection;
import java.util.Map;


// TODO: Auto-generated Javadoc
/**
 * The Interface IElement.
 * 
 * @author Dieter Krachtus
 * 
 * Interface every ModelBean has to implement. Establishes identity for
 * each model object (elementID) and hierarchy (parentElementID,
 * possibly also childElementID)
 */
public interface IElement {
	
	
	
	/**
	 * The Class UnknownValueExecption.
	 */
	public class UnknownValueExecption extends Exception {
		
		/** The Constant serialVersionUID. */
		private static final long serialVersionUID = -6387499727989362192L;
	}

	/**
	 * Gets the element id.
	 * 
	 * @return the element id
	 */
	public int getElementID();
	
	/**
	 * Sets the element id.
	 * 
	 * @param elementID the new element id
	 */
	public void setElementID(int elementID);
	
	/*Returns value of Unique identifier (attribute value) of this element*/
	/**
	 * Gets the key.
	 * 
	 * @return the key
	 */
	public String getKey();
	
	/*Returns unique identifier of parent element*/
	/**
	 * Gets the parent key.
	 * 
	 * @return the parent key
	 */
	public String getParentKey();
	
	/* Produces String Array for Protocol */
	/**
	 * To string array.
	 * 
	 * @return the collection
	 */
	public Collection<String> toStringArray();
	
	/* Set one Attribute*/
	/**
	 * Sets the attribute.
	 * 
	 * @param key the key
	 * @param value the value
	 * @throws UnknownValueExecption the unknown value execption
	 */
	public void setAttribute(String key, String value) throws UnknownValueExecption;

	/**
	 * Gets the attribute.
	 * 
	 * @param key the key
	 * @return the attribute
	 */
	public String getAttribute(String key);
	
	/*checks that all required attributes are set*/
	/**
	 * Checks if is complete.
	 * 
	 * @return true, if is complete
	 */
	public boolean isComplete();

	public Map<String, String> getAttributes();
	
	public void setAttributes(IElement element);	

	@Override
	public boolean equals(Object obj);
	
	@Override
	public int hashCode();
	
}
