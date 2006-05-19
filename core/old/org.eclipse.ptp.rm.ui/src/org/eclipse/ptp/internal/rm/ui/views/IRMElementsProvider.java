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
package org.eclipse.ptp.internal.rm.ui.views;

import org.eclipse.ptp.rm.core.IRMElement;
import org.eclipse.ptp.rm.core.IRMResourceManager;
import org.eclipse.ptp.rm.core.attributes.IAttrDesc;

/**
 * Factored out class to provide the elements of the subclass of
 * {@link {@link IRMElement}}.
 * 
 * @author rsqrd
 * 
 */
public interface IRMElementsProvider {

	/**
	 * @param manager
	 * @return the list of element descriptions
	 */
	public abstract IAttrDesc[] getElementAttrDescs(IRMResourceManager manager);

	/**
	 * @param manager
	 * @return the list of elements of the correct subclass of
	 *         {@link {@link IRMElement}}
	 */
	public abstract IRMElement[] getElements(IRMResourceManager manager);

	/**
	 * @return whether this element subclass has a status associated with it.
	 */
	public abstract boolean hasStatus();

	/**
	 * @return the name of this element's type, e.g. "Node" or "Machine,"
	 *         perhaps for use as a table column header.
	 */
	public abstract String getNameFieldName();

	/**
	 * @param element
	 * @return an object that is capable of displaying the text and icon for
	 *         whatever type of status the subclass of {@link {@link IRMElement}}
	 *         provides.
	 */
	public abstract IStatusDisplayProvider getStatus(IRMElement element);

	/**
	 * @return all of the possible statuses that may be displayed.
	 */
	public abstract IStatusDisplayProvider[] getAllStatuses();

}