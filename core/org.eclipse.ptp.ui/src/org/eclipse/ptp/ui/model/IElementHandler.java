/*******************************************************************************
 * Copyright (c) 2005 The Regents of the University of California. 
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
package org.eclipse.ptp.ui.model;

import org.eclipse.ptp.core.PreferenceConstants;

/**
 * @author clement chu
 *
 */
public interface IElementHandler extends IElementSet {
	public final static String SET_ROOT_ID = PreferenceConstants.SET_ROOT_ID;
	/** Get Set root
	 * @return root set
	 */
	IElementSet getSetRoot();
	
	/** Get sets included
	 * @param id Target element ID
	 * @return included sets
	 */
	IElementSet[] getSetsWithElement(String id);
	
	/** Is element registered
	 * @param element Target element
	 * @return true if element is registered
	 */
	boolean containsRegister(IElement element);
	/** Add element to registered list
	 * @param element Target element
	 */
	void addToRegister(IElement[] elements);
	/** Remove element from registered list
	 * @param element Target element
	 */
	void removeFromRegister(IElement[] elements);
	/** Get registered elements
	 * @return registered elements
	 */
	IElement[] getRegistered();
	/** 
	 * Remove all registered elements
	 */
	void removeAllRegistered();
	/** Get total of registered elements
	 * @return number of registered elements
	 */
	int totalRegistered();
}
