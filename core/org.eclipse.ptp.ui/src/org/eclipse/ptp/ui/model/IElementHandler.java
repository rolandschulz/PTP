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

import java.util.BitSet;

import org.eclipse.ptp.core.PreferenceConstants;

/**
 * @author clement chu
 * 
 */
public interface IElementHandler {
	public final static String SET_ROOT_ID = PreferenceConstants.SET_ROOT_ID;

	/**
	 * @since 7.0
	 */
	public IElementSet createSet(String id, String name, BitSet elements);

	/**
	 * Get registered elements
	 * 
	 * @return registered elements
	 * @since 7.0
	 */
	public BitSet getRegistered();

	/**
	 * @param id
	 * @return
	 * @since 7.0
	 */
	public IElementSet getSet(String id);

	/**
	 * @since 7.0
	 */
	public IElementSet[] getSets();

	/**
	 * @since 7.0
	 */
	public IElementSet[] getSetsContaining(int element);

	/**
	 * @since 7.0
	 */
	public boolean isRegistered(int index);

	/**
	 * Add element to registered list
	 * 
	 * @param element
	 *            Target element
	 * @since 7.0
	 */
	public void register(BitSet elements);

	/**
	 * Remove all registered elements
	 */
	public void removeAllRegistered();

	/**
	 * @since 7.0
	 */
	public IElementSet removeSet(String id);

	/**
	 * @since 7.0
	 */
	public int size();

	/**
	 * Get total of registered elements
	 * 
	 * @return number of registered elements
	 */
	public int totalRegistered();

	/**
	 * Remove element from registered list
	 * 
	 * @param element
	 *            Target element
	 * @since 7.0
	 */
	public void unRegister(BitSet elements);
}
