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

import java.util.Collection;

/**
 * @author clement chu
 *
 */
public interface IContainer extends IElement {
	public static final int SET_TYPE = 1;
	public static final int ELEMENT_TYPE = 2;
	
	/** Is Element contained
	 * @param id Element ID
	 * @return true if contain
	 */
	public boolean contains(String id);
	/** Get Element ID
	 * @param index Element index
	 * @return element ID
	 */
	public String getElementID(int index);
	/** Add element 
	 * @param element Target element
	 */
	public void add(IElement element);
	/** Remove element
	 * @param element Target element
	 */
	public void remove(IElement element);
	/** Remove element
	 * @param id Targt element ID
	 */
	public void remove(String id);
	/** Clean all elements
	 * 
	 */
	public void clearAll();
	/** Get element size
	 * @return total elements
	 */
	public int size();

	/** Get elements
	 * @return elements
	 */
	public Collection<IElement> get();
	/** Get sorted elements
	 * @return sorted elements
	 */
	public IElement[] getSorted();
	/** Get element
	 * @param id Element ID
	 * @return element
	 */
	public IElement get(String id);
	/** Get element
	 * @param index Element index
	 * @return element
	 */
	public IElement get(int index);
	
	/** Set data
	 * @param key Unique key of data
	 * @param data Data
	 */
	public void setData(String key, Object data);
	/** Get data
	 * @param key Unique key of data
	 * @return Data
	 */
	public Object getData(String key);
}
