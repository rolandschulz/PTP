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
package org.eclipse.ptp.internal.rmsystem;

import org.eclipse.ptp.rmsystem.AbstractResourceManagerFactory;
import org.eclipse.ptp.rmsystem.IResourceManager;
import org.eclipse.ptp.rmsystem.IResourceManagerConfiguration;
import org.eclipse.ui.IMemento;

public class NullResourceManagerFactory extends AbstractResourceManagerFactory {

	public class NullConfiguration implements IResourceManagerConfiguration {

		public String getDescription() {
			return "A null Resource Manager that does nothing.";
		}

		public String getName() {
			return "Null";
		}

		public String getResourceManagerId() {
			return "--";
		}

		public void save(IMemento memento) {
			// no-op
		}

		public void setDescription(String description) {
			// no-op
		}

		public void setName(String name) {
			// no-op
		}

		public void setDefaultNameAndDesc() {
			// no-op			
		}

	}

	public NullResourceManagerFactory() {
		super("Null");
	}

	public IResourceManager create(IResourceManagerConfiguration configuration) {
		return new NullResourceManager(configuration);
	}

	public IResourceManagerConfiguration createConfiguration() {
		return new NullConfiguration();
	}

	public String getId() {
		return "--";
	}

	public String getName() {
		return "Null RM type";
	}

	public IResourceManagerConfiguration loadConfiguration(IMemento memento) {
		return new NullConfiguration();
	}

}
