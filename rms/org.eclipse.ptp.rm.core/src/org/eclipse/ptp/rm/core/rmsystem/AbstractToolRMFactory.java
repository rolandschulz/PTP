/*******************************************************************************
 * Copyright (c) 2008 IBM Corporation.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 ******************************************************************************/
package org.eclipse.ptp.rm.core.rmsystem;

import org.eclipse.ptp.core.elementcontrols.IResourceManagerControl;
import org.eclipse.ptp.rmsystem.AbstractResourceManagerFactory;
import org.eclipse.ptp.rmsystem.IResourceManagerConfiguration;
import org.eclipse.ui.IMemento;

public abstract class AbstractToolRMFactory extends AbstractResourceManagerFactory {

	public AbstractToolRMFactory(String name) {
		super(name);
	}

	public IResourceManagerConfiguration copyConfiguration(
			IResourceManagerConfiguration configuration) {
		return (IResourceManagerConfiguration) configuration.clone();
	}

	abstract public IResourceManagerControl create(IResourceManagerConfiguration config);

	abstract public IResourceManagerConfiguration createConfiguration();

	abstract public IResourceManagerConfiguration loadConfiguration(IMemento memento);

}
