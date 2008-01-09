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
package org.eclipse.ptp.rmsystem;

import org.eclipse.ui.IMemento;

public interface IResourceManagerConfiguration extends Cloneable {
	
	/**
	 * Create a copy of this configuration.
	 * 
	 * @return a copy of this configuration
	 */
	public Object clone();

	/**
	 * Returns the description of the resource manager.
	 *
	 * @return the description of the resource manager
	 */
	public String getDescription();

	/**
	 * Returns the name of the resource manager.
	 *
	 * @return the name of the resource manager
	 */
	public String getName();
	
	/**
	 * Returns the id of the resource manager
	 *
	 * @return the id of the resource manager
	 */
	public String getResourceManagerId();

	/**
	 * Returns the type of the resource manager. This is the name of the resource manager factory.
	 *
	 * @return the type of the resource manager
	 */
	public String getType();

	/**
	 * Get a unique name for this resource manager
	 * 
	 * @return unique name
	 */
	public String getUniqueName();

	/**
	 * Save the state of the configuration.
	 * 
	 * @param memento
	 */
	public void save(IMemento memento);

	/**
	 * The the name and description to default values
	 */
	public void setDefaultNameAndDesc();

	/**
	 * Set the description of the resource manager
	 * 
	 * @param description
	 */
	public void setDescription(String description);

	/**
	 * Set the name of the resource manager
	 * 
	 * @param name
	 */
	public void setName(String name);
}
