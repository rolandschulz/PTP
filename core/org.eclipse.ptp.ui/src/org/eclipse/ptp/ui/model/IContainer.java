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

/**
 * @author clement chu
 *
 */
public interface IContainer extends IElement {
	public static final int SET_TYPE = 1;
	public static final int ELEMENT_TYPE = 2;
	
	public boolean contains(String id);
	public String getElementID(int index);
	public void add(IElement element);
	public void remove(IElement element);
	public void remove(String id);
	public void clearAll();
	public int size();	

	public IElement[] get();
	public IElement[] getSorted();
	public IElement get(String id);
	public IElement get(int index);
	
	public void setData(String key, Object data);
	public Object getData(String key);
}
