/**********************************************************************
 * Copyright (c) 2004 Intel Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors: 
 * Intel Corporation - Initial API and implementation
 **********************************************************************/
package org.eclipse.cldt.managedbuilder.internal.core;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.eclipse.cldt.managedbuilder.core.IConfiguration;
import org.eclipse.cldt.managedbuilder.core.IManagedConfigElement;
import org.eclipse.cldt.managedbuilder.core.IProjectType;
import org.eclipse.cldt.managedbuilder.core.ManagedBuildManager;

public class ProjectType extends BuildObject implements IProjectType {
	
	private static final String EMPTY_STRING = new String();
	private static final IConfiguration[] emptyConfigs = new IConfiguration[0];
	
	//  Superclass
	private IProjectType superClass;
	private String superClassId;
	//  Parent and children
	private List configList;	//  Configurations of this project type
	private Map configMap;
	//  Managed Build model attributes
	private Boolean isAbstract;
	private Boolean isTest;
	private String unusedChildren;
	//  Miscellaneous
	private boolean resolved = true;

	/*
	 *  C O N S T R U C T O R S
	 */

	/**
	 * This constructor is called to create a projectType defined by an extension point in 
	 * a plugin manifest file.
	 * 
	 * @param element
	 */
	public ProjectType(IManagedConfigElement element) {
		// setup for resolving
		resolved = false;

		loadFromManifest(element);
		
		// Hook me up to the Managed Build Manager
		ManagedBuildManager.addExtensionProjectType(this);

		// Load the configuration children
		IManagedConfigElement[] configs = element.getChildren(IConfiguration.CONFIGURATION_ELEMENT_NAME);
		for (int n = 0; n < configs.length; ++n) {
			Configuration config = new Configuration(this, configs[n]);
		}
	}

	/**
	 * This constructor is called to create a project type whose attributes and children will be 
	 * added by separate calls.
	 * 
	 * @param ProjectType The superClass, if any
	 * @param String The id for the new project type
	 * @param String The name for the new project type
	 */
	public ProjectType(ProjectType superClass, String Id, String name) {
		// setup for resolving
		resolved = false;

		this.superClass = superClass;
		if (this.superClass != null) {
			superClassId = this.superClass.getId();
		}
		setId(Id);
		setName(name);
		// Hook me up to the Managed Build Manager
		ManagedBuildManager.addExtensionProjectType(this);
	}

	/*
	 *  E L E M E N T   A T T R I B U T E   R E A D E R S   A N D   W R I T E R S
	 */
	
	/* (non-Javadoc)
	 * Load the project-type information from the XML element specified in the 
	 * argument
	 * @param element An XML element containing the project type information 
	 */
	protected void loadFromManifest(IManagedConfigElement element) {
		ManagedBuildManager.putConfigElement(this, element);
		
		// id
		setId(element.getAttribute(ID));
		
		// Get the name
		setName(element.getAttribute(NAME));
		
		// superClass
		superClassId = element.getAttribute(SUPERCLASS);

		// Get the unused children, if any
		unusedChildren = element.getAttribute(UNUSED_CHILDREN); 
		
		// isAbstract
        String isAbs = element.getAttribute(IS_ABSTRACT);
        if (isAbs != null){
    		isAbstract = new Boolean("true".equals(isAbs)); //$NON-NLS-1$
        }

		// Is this a test project type
		String isTestStr = element.getAttribute(IS_TEST); //$NON-NLS-1$
        if (isTestStr != null){
    		isTest = new Boolean("true".equals(isTestStr)); //$NON-NLS-1$
        }
	}

	/*
	 *  P A R E N T   A N D   C H I L D   H A N D L I N G
	 */

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.core.build.managed.IProjectType#createConfiguration(org.eclipse.cdt.core.build.managed.IConfiguration)
	 */
	public IConfiguration createConfiguration(IConfiguration parent, String id, String name) {
		Configuration config = new Configuration(this, parent, id, name);
		return (IConfiguration)config;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.core.build.managed.IProjectType#getConfiguration()
	 */
	public IConfiguration getConfiguration(String id) {
		return (IConfiguration)getConfigurationMap().get(id);
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.cdt.managedbuilder.core.IProjectType#getConfigurations()
	 */
	public IConfiguration[] getConfigurations() {
		IConfiguration[] configs = new IConfiguration[getConfigurationList().size()];
		Iterator iter = getConfigurationList().listIterator();
		int i = 0;
		while (iter.hasNext()) {
			Configuration config = (Configuration)iter.next();
			configs[i++] = (IConfiguration)config; 
		}
		return configs;
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.cdt.managedbuilder.core.IProjectType#removeConfiguration(java.lang.String)
	 */
	public void removeConfiguration(String id) {
		// Remove the specified configuration from the list and map
		Iterator iter = getConfigurationList().listIterator();
		while (iter.hasNext()) {
			 IConfiguration config = (IConfiguration)iter.next();
			 if (config.getId().equals(id)) {
			 	getConfigurationList().remove(config);
				getConfigurationMap().remove(id);
			 	break;
			 }
		}
	}
	
	/* (non-Javadoc)
	 * Adds the Configuration to the Configuration list and map
	 * 
	 * @param Tool
	 */
	public void addConfiguration(Configuration configuration) {
		getConfigurationList().add(configuration);
		getConfigurationMap().put(configuration.getId(), configuration);
	}
	
	/* (non-Javadoc)
	 * Safe accessor for the list of configurations.
	 * 
	 * @return List containing the configurations
	 */
	private List getConfigurationList() {
		if (configList == null) {
			configList = new ArrayList();
		}
		return configList;
	}
	
	/* (non-Javadoc)
	 * Safe accessor for the map of configuration ids to configurations
	 * 
	 * @return
	 */
	private Map getConfigurationMap() {
		if (configMap == null) {
			configMap = new HashMap();
		}
		return configMap;
	}

	/*
	 *  M O D E L   A T T R I B U T E   A C C E S S O R S
	 */

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.managedbuilder.core.IBuildObject#getName()
	 */
	public String getName() {
		// If I am unnamed, see if I can inherit one from my parent
		if (name == null) {
			if (superClass != null) {
				return superClass.getName();
			} else {
				return new String(""); //$NON-NLS-1$
			}
		} else {
			return name;
		}
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.managedbuilder.core.IProjectType#getSuperClass()
	 */
	public IProjectType getSuperClass() {
		return superClass;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.core.build.managed.IProjectType#isAbstract()
	 */
	public boolean isAbstract() {
		if (isAbstract != null) {
			return isAbstract.booleanValue();
		} else {
			return false;	// Note: no inheritance from superClass
		}
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.core.build.managed.IProjectType#unusedChildren()
	 */
	public String getUnusedChildren() {
		if (unusedChildren != null) {
			return unusedChildren;
		} else
			return EMPTY_STRING;	// Note: no inheritance from superClass
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.cdt.core.build.managed.IProjectType#isTestProjectType()
	 */
	public boolean isTestProjectType() {
		if (isTest == null) {
			// If I have a superClass, ask it
			if (superClass != null) {
				return superClass.isTestProjectType();
			} else {
				return false;
			}
		}
		return isTest.booleanValue();
	}

	/* (non-Javadoc)
	 * Sets the isAbstract attribute
	 */
	public void setIsAbstract(boolean b) {
		isAbstract = new Boolean(b);
	}

	/* (non-Javadoc)
	 * Sets the isTest attribute
	 */
	public void setIsTest(boolean b) {
		isTest = new Boolean(b);
	}

	/*
	 *  O B J E C T   S T A T E   M A I N T E N A N C E
	 */
	
	/* (non-Javadoc)
	 *  Resolve the element IDs to interface references
	 */
	public void resolveReferences() {
		if (!resolved) {
			resolved = true;
			// Resolve superClass
			if (superClassId != null && superClassId.length() > 0) {
				superClass = ManagedBuildManager.getExtensionProjectType(superClassId);
				if (superClass == null) {
					// Report error
					ManagedBuildManager.OutputResolveError(
							"superClass",	//$NON-NLS-1$
							superClassId,
							"projectType",	//$NON-NLS-1$
							getId());
				}
			}
			
			// Add configurations from our superClass that are not overridden here
			if (superClass != null) {
			    ((ProjectType)superClass).resolveReferences();
			    IConfiguration[] superConfigs = superClass.getConfigurations();
			    for (int i = 0; i < superConfigs.length; i++) {
			        String superId = superConfigs[i].getId();
				    
				    check: { 
					    IConfiguration[] currentConfigs = getConfigurations();
				        for (int j = 0; j < currentConfigs.length; j++) {
					        IConfiguration config = currentConfigs[j];
					        while (config.getParent() != null) {
					            if (config.getParent().getId().equals(superId)) break check;
					            config = config.getParent();
					        }
				        }
				        addConfiguration((Configuration)superConfigs[i]);
				    } // end check
				    
			    }
			}

			// Call resolve references on any children
			Iterator configIter = getConfigurationList().iterator();
			while (configIter.hasNext()) {
				Configuration current = (Configuration)configIter.next();
				current.resolveReferences();
			}
		}
	}

}
