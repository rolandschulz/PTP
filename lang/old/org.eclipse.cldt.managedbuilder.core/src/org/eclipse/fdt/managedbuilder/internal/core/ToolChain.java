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
package org.eclipse.fdt.managedbuilder.internal.core;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.StringTokenizer;

import org.eclipse.fdt.managedbuilder.core.IBuildObject;
import org.eclipse.fdt.managedbuilder.core.IBuilder;
import org.eclipse.fdt.managedbuilder.core.IConfiguration;
import org.eclipse.fdt.managedbuilder.core.IManagedConfigElement;
import org.eclipse.fdt.managedbuilder.core.IProjectType;
import org.eclipse.fdt.managedbuilder.core.ITargetPlatform;
import org.eclipse.fdt.managedbuilder.core.ITool;
import org.eclipse.fdt.managedbuilder.core.IToolChain;
import org.eclipse.fdt.managedbuilder.core.ManagedBuildManager;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

public class ToolChain extends BuildObject implements IToolChain {

	private static final String EMPTY_STRING = new String();

	//  Superclass
	private IToolChain superClass;
	private String superClassId;
	//  Parent and children
	private IConfiguration parent;
	private List toolList;
	private Map toolMap;
	private TargetPlatform targetPlatform;
	private Builder builder;
	//  Managed Build model attributes
	private String unusedChildren;
	private String errorParserIds;
	private List osList;
	private List archList;
	private Boolean isAbstract;
    private String scannerConfigDiscoveryProfileId;
	//  Miscellaneous
	private boolean isExtensionToolChain = false;
	private boolean isDirty = false;
	private boolean resolved = true;

	/*
	 *  C O N S T R U C T O R S
	 */
	
	/**
	 * This constructor is called to create a tool-chain defined by an extension point in 
	 * a plugin manifest file, or returned by a dynamic element provider
	 * 
	 * @param parent  The IConfiguration parent of this tool-chain, or <code>null</code> if
	 *                defined at the top level
	 * @param element The tool-chain definition from the manifest file or a dynamic element
	 *                provider
	 */
	public ToolChain(IConfiguration parent, IManagedConfigElement element) {
		this.parent = parent;
		isExtensionToolChain = true;
		
		// setup for resolving
		resolved = false;

		loadFromManifest(element);
		
		// Hook me up to the Managed Build Manager
		ManagedBuildManager.addExtensionToolChain(this);
		
		// Load the TargetPlatform child
		IManagedConfigElement[] targetPlatforms = 
			element.getChildren(ITargetPlatform.TARGET_PLATFORM_ELEMENT_NAME);
		if (targetPlatforms.length < 1 || targetPlatforms.length > 1) {
			// TODO: Report error
		}
		if (targetPlatforms.length > 0) {
			targetPlatform = new TargetPlatform(this, targetPlatforms[0]);
		}
		
		// Load the Builder child
		IManagedConfigElement[] builders = 
			element.getChildren(IBuilder.BUILDER_ELEMENT_NAME);
		if (builders.length < 1 || builders.length > 1) {
			// TODO: Report error
		}
		if (builders.length > 0) {
			builder = new Builder(this, builders[0]);
		}

		// Load the tool children
		IManagedConfigElement[] tools = element.getChildren(ITool.TOOL_ELEMENT_NAME);
		for (int n = 0; n < tools.length; ++n) {
			Tool toolChild = new Tool(this, tools[n]);
			addTool(toolChild);
		}
	}

	/**
	 * This constructor is called to create a ToolChain whose attributes and children will be 
	 * added by separate calls.
	 * 
	 * @param Configuration The parent of the tool chain, if any
	 * @param ToolChain The superClass, if any
	 * @param String The id for the new tool chain
	 * @param String The name for the new tool chain
	 * @param boolean Indicates whether this is an extension element or a managed project element
	 */
	public ToolChain(Configuration parent, IToolChain superClass, String Id, String name, boolean isExtensionElement) {
		this.parent = parent;
		this.superClass = superClass;
		if (this.superClass != null) {
			superClassId = this.superClass.getId();
		}
		setId(Id);
		setName(name);
		isExtensionToolChain = isExtensionElement;
		if (isExtensionElement) {
			// Hook me up to the Managed Build Manager
			ManagedBuildManager.addExtensionToolChain(this);
		} else {
			setDirty(true);
		}
	}

	/**
	 * Create a <code>ToolChain</code> based on the specification stored in the 
	 * project file (.cdtbuild).
	 * 
	 * @param parent The <code>IConfiguration</code> the tool-chain will be added to. 
	 * @param element The XML element that contains the tool-chain settings.
	 */
	public ToolChain(IConfiguration parent, Element element) {
		this.parent = parent;
		isExtensionToolChain = false;
		
		// Initialize from the XML attributes
		loadFromProject(element);

		// Load children
		NodeList configElements = element.getChildNodes();
		for (int i = 0; i < configElements.getLength(); ++i) {
			Node configElement = configElements.item(i);
			if (configElement.getNodeName().equals(ITool.TOOL_ELEMENT_NAME)) {
				Tool tool = new Tool(this, (Element)configElement);
				addTool(tool);
			}else if (configElement.getNodeName().equals(ITargetPlatform.TARGET_PLATFORM_ELEMENT_NAME)) {
				if (targetPlatform != null) {
					// TODO: report error
				}
				targetPlatform = new TargetPlatform(this, (Element)configElement);
			}else if (configElement.getNodeName().equals(IBuilder.BUILDER_ELEMENT_NAME)) {
				if (builder != null) {
					// TODO: report error
				}
				builder = new Builder(this, (Element)configElement);
			}
		}
	}

	/**
	 * Create a <code>ToolChain</code> based upon an existing tool chain.
	 * 
	 * @param parent The <code>IConfiguration</code> the tool-chain will be added to. 
	 * @param toolChain The existing tool-chain to clone.
	 */
	public ToolChain(IConfiguration parent, String Id, String name, ToolChain toolChain) {
		this.parent = parent;
		superClass = toolChain.superClass;
		if (superClass != null) {
			if (toolChain.superClassId != null) {
				superClassId = new String(toolChain.superClassId);
			}
		}
		setId(Id);
		setName(name);
		isExtensionToolChain = false;
		
		//  Copy the remaining attributes
		if (toolChain.unusedChildren != null) {
			unusedChildren = new String(toolChain.unusedChildren);
		}
		if (toolChain.errorParserIds != null) {
			errorParserIds = new String(toolChain.errorParserIds);
		}
		if (toolChain.osList != null) {
			osList = new ArrayList(toolChain.osList);
		}
		if (toolChain.archList != null) {
			archList = new ArrayList(toolChain.archList);
		}
		if (toolChain.isAbstract != null) {
			isAbstract = new Boolean(toolChain.isAbstract.booleanValue());
		}
        if (toolChain.scannerConfigDiscoveryProfileId != null) {
            scannerConfigDiscoveryProfileId = new String(toolChain.scannerConfigDiscoveryProfileId);
        }

		//  Clone the children
		if (toolChain.builder != null) {
			int nnn = ManagedBuildManager.getRandomNumber();
			String subId;
			String subName;
			if (toolChain.builder.getSuperClass() != null) {
				subId = toolChain.builder.getSuperClass().getId() + "." + nnn;		//$NON-NLS-1$
				subName = toolChain.builder.getSuperClass().getName();
			} else {
				subId = toolChain.builder.getId() + "." + nnn;		//$NON-NLS-1$
				subName = toolChain.builder.getName();
			}
			builder = new Builder(this, subId, subName, toolChain.builder);
		}
		if (toolChain.targetPlatform != null) {
			int nnn = ManagedBuildManager.getRandomNumber();
			String subId;
			String subName;
			if (toolChain.targetPlatform.getSuperClass() != null) {
				subId = toolChain.targetPlatform.getSuperClass().getId() + "." + nnn;		//$NON-NLS-1$
				subName = toolChain.targetPlatform.getSuperClass().getName(); 
			} else {
				subId = toolChain.targetPlatform.getId() + "." + nnn;		//$NON-NLS-1$
				subName = toolChain.targetPlatform.getName();
			}
			targetPlatform = new TargetPlatform(this, subId, subName, toolChain.targetPlatform);
		}
		if (toolChain.toolList != null) {
			Iterator iter = toolChain.getToolList().listIterator();
			while (iter.hasNext()) {
			    Tool toolChild = (Tool) iter.next();
				int nnn = ManagedBuildManager.getRandomNumber();
				String subId;
				String subName;
				if (toolChild.getSuperClass() != null) {
					subId = toolChild.getSuperClass().getId() + "." + nnn;		//$NON-NLS-1$
					subName = toolChild.getSuperClass().getName();
				} else {
					subId = toolChild.getId() + "." + nnn;		//$NON-NLS-1$
					subName = toolChild.getName();
				}
				Tool newTool = new Tool(this, null, subId, subName, toolChild);
				addTool(newTool);
			}
		}
		
		setDirty(true);
	}

	/*
	 *  E L E M E N T   A T T R I B U T E   R E A D E R S   A N D   W R I T E R S
	 */
	
	/* (non-Javadoc)
	 * Loads the tool-chain information from the ManagedConfigElement specified in the 
	 * argument.
	 * 
	 * @param element Contains the tool-chain information 
	 */
	protected void loadFromManifest(IManagedConfigElement element) {
		ManagedBuildManager.putConfigElement(this, element);
		
		// id
		setId(element.getAttribute(IBuildObject.ID));
		
		// Get the name
		setName(element.getAttribute(IBuildObject.NAME));
		
		// superClass
		superClassId = element.getAttribute(IProjectType.SUPERCLASS);

		// Get the unused children, if any
		unusedChildren = element.getAttribute(IProjectType.UNUSED_CHILDREN); 
		
		// isAbstract
        String isAbs = element.getAttribute(IProjectType.IS_ABSTRACT);
        if (isAbs != null){
    		isAbstract = new Boolean("true".equals(isAbs)); //$NON-NLS-1$
        }
		
		// Get the semicolon separated list of IDs of the error parsers
		errorParserIds = element.getAttribute(ERROR_PARSERS);
		
		// Get the scanner config discovery profile id
        scannerConfigDiscoveryProfileId = element.getAttribute(SCANNER_CONFIG_PROFILE_ID);
        
		// Get the comma-separated list of valid OS
		String os = element.getAttribute(OS_LIST);
		if (os != null) {
			osList = new ArrayList();
			String[] osTokens = os.split(","); //$NON-NLS-1$
			for (int i = 0; i < osTokens.length; ++i) {
				osList.add(osTokens[i].trim());
			}
		}
		
		// Get the comma-separated list of valid Architectures
		String arch = element.getAttribute(ARCH_LIST);
		if (arch != null) {
			archList = new ArrayList();
			String[] archTokens = arch.split(","); //$NON-NLS-1$
			for (int j = 0; j < archTokens.length; ++j) {
				archList.add(archTokens[j].trim());
			}
		}
	}
	
	/* (non-Javadoc)
	 * Initialize the tool-chain information from the XML element 
	 * specified in the argument
	 * 
	 * @param element An XML element containing the tool-chain information 
	 */
	protected void loadFromProject(Element element) {
		
		// id
		setId(element.getAttribute(IBuildObject.ID));

		// name
		if (element.hasAttribute(IBuildObject.NAME)) {
			setName(element.getAttribute(IBuildObject.NAME));
		}
		
		// superClass
		superClassId = element.getAttribute(IProjectType.SUPERCLASS);
		if (superClassId != null && superClassId.length() > 0) {
			superClass = ManagedBuildManager.getExtensionToolChain(superClassId);
			if (superClass == null) {
				// TODO:  Report error
			}
		}

		// Get the unused children, if any
		if (element.hasAttribute(IProjectType.UNUSED_CHILDREN)) {
				unusedChildren = element.getAttribute(IProjectType.UNUSED_CHILDREN); 
		}
		
		// isAbstract
		if (element.hasAttribute(IProjectType.IS_ABSTRACT)) {
			String isAbs = element.getAttribute(IProjectType.IS_ABSTRACT);
			if (isAbs != null){
				isAbstract = new Boolean("true".equals(isAbs)); //$NON-NLS-1$
			}
		}
		
		// Get the semicolon separated list of IDs of the error parsers
		if (element.hasAttribute(ERROR_PARSERS)) {
			errorParserIds = element.getAttribute(ERROR_PARSERS);
		}
		
        // Get the scanner config discovery profile id
        if (element.hasAttribute(SCANNER_CONFIG_PROFILE_ID)) {
            scannerConfigDiscoveryProfileId = element.getAttribute(SCANNER_CONFIG_PROFILE_ID);
        }
        
		// Get the comma-separated list of valid OS
		if (element.hasAttribute(OS_LIST)) {
			String os = element.getAttribute(OS_LIST);
			if (os != null) {
				osList = new ArrayList();
				String[] osTokens = os.split(","); //$NON-NLS-1$
				for (int i = 0; i < osTokens.length; ++i) {
					osList.add(osTokens[i].trim());
				}
			}
		}
		
		// Get the comma-separated list of valid Architectures
		if (element.hasAttribute(ARCH_LIST)) {
			String arch = element.getAttribute(ARCH_LIST);
			if (arch != null) {
				archList = new ArrayList();
				String[] archTokens = arch.split(","); //$NON-NLS-1$
				for (int j = 0; j < archTokens.length; ++j) {
					archList.add(archTokens[j].trim());
				}
			}
		}
	}

	/**
	 * Persist the tool-chain to the project file.
	 * 
	 * @param doc
	 * @param element
	 */
	public void serialize(Document doc, Element element) {
		if (superClass != null)
			element.setAttribute(IProjectType.SUPERCLASS, superClass.getId());
		
		element.setAttribute(IBuildObject.ID, id);
		
		if (name != null) {
			element.setAttribute(IBuildObject.NAME, name);
		}

		if (unusedChildren != null) {
			element.setAttribute(IProjectType.UNUSED_CHILDREN, unusedChildren);
		}
		
		if (isAbstract != null) {
			element.setAttribute(IProjectType.IS_ABSTRACT, isAbstract.toString());
		}

		if (errorParserIds != null) {
			element.setAttribute(ERROR_PARSERS, errorParserIds);
		}
        
        if (scannerConfigDiscoveryProfileId != null) {
            element.setAttribute(SCANNER_CONFIG_PROFILE_ID, scannerConfigDiscoveryProfileId);
        }

		if (osList != null) {
			Iterator osIter = osList.listIterator();
			String listValue = EMPTY_STRING;
			while (osIter.hasNext()) {
				String current = (String) osIter.next();
				listValue += current;
				if ((osIter.hasNext())) {
					listValue += ","; //$NON-NLS-1$
				}
			}
			element.setAttribute(OS_LIST, listValue);
		}

		if (archList != null) {
			Iterator archIter = archList.listIterator();
			String listValue = EMPTY_STRING;
			while (archIter.hasNext()) {
				String current = (String) archIter.next();
				listValue += current;
				if ((archIter.hasNext())) {
					listValue += ","; //$NON-NLS-1$
				}
			}
			element.setAttribute(ARCH_LIST, listValue);
		}
		
		// Serialize my children
		if (targetPlatform != null) {
			Element targetPlatformElement = doc.createElement(ITargetPlatform.TARGET_PLATFORM_ELEMENT_NAME);
			element.appendChild(targetPlatformElement);
			targetPlatform.serialize(doc, targetPlatformElement);
		}
		if (builder != null) {
			Element builderElement = doc.createElement(IBuilder.BUILDER_ELEMENT_NAME);
			element.appendChild(builderElement);
			builder.serialize(doc, builderElement);
		}
		List toolElements = getToolList();
		Iterator iter = toolElements.listIterator();
		while (iter.hasNext()) {
			Tool tool = (Tool) iter.next();
			Element toolElement = doc.createElement(ITool.TOOL_ELEMENT_NAME);
			element.appendChild(toolElement);
			tool.serialize(doc, toolElement);
		}
		
		// I am clean now
		isDirty = false;
	}

	/*
	 *  P A R E N T   A N D   C H I L D   H A N D L I N G
	 */

	/* (non-Javadoc)
	 * @see org.eclipse.fdt.core.build.managed.IToolChain#getConfiguration()
	 */
	public IConfiguration getParent() {
		return parent;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.fdt.core.build.managed.IToolChain#createTargetPlatform(ITargetPlatform, String, String, boolean)
	 */
	public ITargetPlatform createTargetPlatform(ITargetPlatform superClass, String id, String name, boolean isExtensionElement) {
		targetPlatform = new TargetPlatform(this, superClass, id, name, isExtensionElement);
		setDirty(true);
		return (ITargetPlatform)targetPlatform;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.fdt.core.build.managed.IToolChain#getTargetPlatform()
	 */
	public ITargetPlatform getTargetPlatform() {
		if (targetPlatform == null) {
			if (superClass != null) {
				return superClass.getTargetPlatform();
			}
		}
		return (ITargetPlatform)targetPlatform;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.fdt.core.build.managed.IToolChain#removeLocalTargetPlatform()
	 */
	public void removeLocalTargetPlatform() {
		if (targetPlatform == null) return;
		targetPlatform = null;
		setDirty(true);
	}

	/* (non-Javadoc)
	 * @see org.eclipse.fdt.core.build.managed.IToolChain#createBuilder(IBuilder, String, String, boolean)
	 */
	public IBuilder createBuilder(IBuilder superClass, String id, String name, boolean isExtensionElement) {
		builder = new Builder(this, superClass, id, name, isExtensionElement);
		setDirty(true);
		return (IBuilder)builder;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.fdt.core.build.managed.IToolChain#getBuilder()
	 */
	public IBuilder getBuilder() {
		if (builder == null) {
			if (superClass != null) {
				return superClass.getBuilder();
			}
		}
		return (IBuilder)builder;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.fdt.core.build.managed.IToolChain#removeLocalBuilder()
	 */
	public void removeLocalBuilder() {
		if (builder == null) return;
		builder = null;
		setDirty(true);
	}

	/* (non-Javadoc)
	 * @see org.eclipse.fdt.core.build.managed.IToolChain#createTool(ITool, String, String, boolean)
	 */
	public ITool createTool(ITool superClass, String id, String name, boolean isExtensionElement) {
		Tool tool = new Tool(this, superClass, id, name, isExtensionElement);
		addTool(tool);
		setDirty(true);
		return (ITool)tool;
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.fdt.core.build.managed.IToolChain#getTools()
	 */
	public ITool[] getTools() {
		ITool[] tools = null;
		//  Merge our tools with our superclass' tools
		if (superClass != null) {
			tools = superClass.getTools();
		}
		//  Our tools take precedence
		if (tools != null) {
			Iterator iter = getToolList().listIterator();
			while (iter.hasNext()) {
				Tool tool = (Tool)iter.next();
				int j;
				for (j = 0; j < tools.length; j++) {
					if (tool.getSuperClass().getId().equals(tools[j].getId())) {
						tools[j] = tool;
						break;
					}
 				}
				//  No Match?  Add it.
				if (j == tools.length) {
					ITool[] newTools = new ITool[tools.length + 1];
					for (int k = 0; k < tools.length; k++) {
						newTools[k] = tools[k];
					}
					newTools[j] = tool;
					tools = newTools;
				}
			}
		} else {
			tools = new ITool[getToolList().size()];
			Iterator iter = getToolList().listIterator();
			int i = 0;
			while (iter.hasNext()) {
				Tool tool = (Tool)iter.next();
				tools[i++] = (ITool)tool; 
			}
		}
		return tools;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.fdt.managedbuilder.core.IToolChain#getTool(java.lang.String)
	 */
	public ITool getTool(String id) {
		Tool tool = (Tool)getToolMap().get(id);
		return (ITool)tool;
	}
	
	/* (non-Javadoc)
	 * Safe accessor for the list of tools.
	 * 
	 * @return List containing the tools
	 */
	public List getToolList() {
		if (toolList == null) {
			toolList = new ArrayList();
		}
		return toolList;
	}
	
	/* (non-Javadoc)
	 * Safe accessor for the map of tool ids to tools
	 * 
	 * @return
	 */
	private Map getToolMap() {
		if (toolMap == null) {
			toolMap = new HashMap();
		}
		return toolMap;
	}

	/* (non-Javadoc)
	 * Adds the Tool to the Tool-chain list and map
	 * 
	 * @param Tool
	 */
	public void addTool(Tool tool) {
		getToolList().add(tool);
		getToolMap().put(tool.getId(), tool);
	}

	/*
	 *  M O D E L   A T T R I B U T E   A C C E S S O R S
	 */

	/* (non-Javadoc)
	 * @see org.eclipse.fdt.managedbuilder.core.IToolChain#getSuperClass()
	 */
	public IToolChain getSuperClass() {
		return superClass;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.fdt.core.build.managed.IToolChain#getName()
	 */
	public String getName() {
		return (name == null && superClass != null) ? superClass.getName() : name;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.fdt.core.build.managed.IToolChain#isAbstract()
	 */
	public boolean isAbstract() {
		if (isAbstract != null) {
			return isAbstract.booleanValue();
		} else {
			return false;	// Note: no inheritance from superClass
		}
	}

	/* (non-Javadoc)
	 * @see org.eclipse.fdt.core.build.managed.IToolChain#getUnusedChildren()
	 */
	public String getUnusedChildren() {
		if (unusedChildren != null) {
			return unusedChildren;
		} else
			return EMPTY_STRING;	// Note: no inheritance from superClass
	}

	/* (non-Javadoc)
	 * @see org.eclipse.fdt.managedbuilder.core.IToolChain#getErrorParserIds()
	 */
	public String getErrorParserIds() {
		String ids = errorParserIds;
		if (ids == null) {
			// If I have a superClass, ask it
			if (superClass != null) {
				ids = superClass.getErrorParserIds();
			}
		}
		if (ids == null) {
			// Collect the error parsers from my children
			ids = builder.getErrorParserIds();
			ITool[] tools = getTools();
			for (int i = 0; i < tools.length; i++) {
				ITool tool = tools[i];
				String toolIds = tool.getErrorParserIds(); 
				if (toolIds != null && toolIds.length() > 0) {
					if (ids != null) {
						ids += ";"; //$NON-NLS-1$
						ids += toolIds;
					} else {
						ids = toolIds;
					}
				}
			}
		}
		return ids;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.fdt.managedbuilder.core.IToolChain#getErrorParserIds(IConfiguration)
	 */
	public String getErrorParserIds(IConfiguration config) {
		String ids = errorParserIds;
		if (ids == null) {
			// If I have a superClass, ask it
			if (superClass != null) {
				ids = superClass.getErrorParserIds(config);
			}
		}
		if (ids == null) {
			// Collect the error parsers from my children
		    if (builder != null) {
		        ids = builder.getErrorParserIds();
		    }
			ITool[] tools = config.getFilteredTools();
			for (int i = 0; i < tools.length; i++) {
				ITool tool = tools[i];
				String toolIds = tool.getErrorParserIds(); 
				if (toolIds != null && toolIds.length() > 0) {
					if (ids != null) {
						ids += ";"; //$NON-NLS-1$
						ids += toolIds;
					} else {
						ids = toolIds;
					}
				}
			}
		}
		return ids;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.fdt.managedbuilder.core.IToolChain#getErrorParserList()
	 */
	public String[] getErrorParserList() {
		String parserIDs = getErrorParserIds();
		String[] errorParsers;
		if (parserIDs != null) {
			// Check for an empty string
			if (parserIDs.length() == 0) {
				errorParsers = new String[0];
			} else {
				StringTokenizer tok = new StringTokenizer(parserIDs, ";"); //$NON-NLS-1$
				List list = new ArrayList(tok.countTokens());
				while (tok.hasMoreElements()) {
					list.add(tok.nextToken());
				}
				String[] strArr = {""};	//$NON-NLS-1$
				errorParsers = (String[]) list.toArray(strArr);
			}
		} else {
			errorParsers = new String[0];
		}
		return errorParsers;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.fdt.managedbuilder.core.IToolChain#getArchList()
	 */
	public String[] getArchList() {
		if (archList == null) {
			// Ask superClass for its list
			if (superClass != null) {
				return superClass.getArchList();
			} else {
				// I have no superClass and no defined list
				return new String[] {"all"}; //$NON-NLS-1$
			}
		}
		return (String[]) archList.toArray(new String[archList.size()]);
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.fdt.managedbuilder.core.IToolChain#getOSList()
	 */
	public String[] getOSList() {
		if (osList == null) {
			// Ask superClass for its list
			if (superClass != null) {
				return superClass.getOSList();
			} else {
				// I have no superClass and no defined filter list
				return new String[] {"all"};	//$NON-NLS-1$
			}
		}
		return (String[]) osList.toArray(new String[osList.size()]);
	}

	/* (non-Javadoc)
	 * @see org.eclipse.fdt.managedbuilder.core.IToolChain#setIsAbstract(boolean)
	 */
	public void setIsAbstract(boolean b) {
		isAbstract = new Boolean(b);
		setDirty(true);
	}

	/* (non-Javadoc)
	 * @see org.eclipse.fdt.managedbuilder.core.IToolChain#setErrorParserIds(String)
	 */
	public void setErrorParserIds(String ids) {
		String currentIds = getErrorParserIds();
		if (ids == null && currentIds == null) return;
		if (currentIds == null || ids == null || !(currentIds.equals(ids))) {
			errorParserIds = ids;
			isDirty = true;
		}
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.fdt.managedbuilder.core.IToolChain#setOSList(String[])
	 */
	public void setOSList(String[] OSs) {
		if (osList == null) {
			osList = new ArrayList();
		} else {
			osList.clear();
		}
		for (int i = 0; i < OSs.length; i++) {
			osList.add(OSs[i]);
		}		
		setDirty(true);
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.fdt.managedbuilder.core.IToolChain#setArchList(String[])
	 */
	public void setArchList(String[] archs) {
		if (archList == null) {
			archList = new ArrayList();
		} else {
			archList.clear();
		}
		for (int i = 0; i < archs.length; i++) {
			archList.add(archs[i]);
		}		
		setDirty(true);
	}

    /* (non-Javadoc)
     * @see org.eclipse.fdt.managedbuilder.core.IToolChain#getScannerConfigDiscoveryProfileId()
     */
    public String getScannerConfigDiscoveryProfileId() {
        if (scannerConfigDiscoveryProfileId == null) {
            if (superClass != null) {
                return superClass.getScannerConfigDiscoveryProfileId();
            }
        }
        return scannerConfigDiscoveryProfileId;
    }

    /* (non-Javadoc)
     * @see org.eclipse.fdt.managedbuilder.core.IToolChain#setScannerConfigDiscoveryProfileId(java.lang.String)
     */
    public void setScannerConfigDiscoveryProfileId(String profileId) {
		if (scannerConfigDiscoveryProfileId == null && profileId == null) return;
        if (scannerConfigDiscoveryProfileId == null ||
                !scannerConfigDiscoveryProfileId.equals(profileId)) {
            scannerConfigDiscoveryProfileId = profileId;
            setDirty(true);
        }
    }
	
	/*
	 *  O B J E C T   S T A T E   M A I N T E N A N C E
	 */
	
	/* (non-Javadoc)
	 * @see org.eclipse.fdt.managedbuilder.core.IToolChain#isExtensionElement()
	 */
	public boolean isExtensionElement() {
		return isExtensionToolChain;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.fdt.managedbuilder.core.IToolChain#isDirty()
	 */
	public boolean isDirty() {
		// This shouldn't be called for an extension tool-chain
 		if (isExtensionToolChain) return false;
		
		// If I need saving, just say yes
		if (isDirty) return true;
		
		// Otherwise see if any tools need saving
		Iterator iter = getToolList().listIterator();
		while (iter.hasNext()) {
			Tool toolChild = (Tool) iter.next();
			if (toolChild.isDirty()) return true;
		}
		
		return isDirty;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.fdt.managedbuilder.core.IToolChain#setDirty(boolean)
	 */
	public void setDirty(boolean isDirty) {
		this.isDirty = isDirty;
		// Propagate "false" to the children
		if (!isDirty) {
			Iterator iter = getToolList().listIterator();
			while (iter.hasNext()) {
				Tool toolChild = (Tool) iter.next();
				toolChild.setDirty(false);
			}		    
		}
	}
	
	/* (non-Javadoc)
	 *  Resolve the element IDs to interface references
	 */
	public void resolveReferences() {
		if (!resolved) {
			resolved = true;
			// Resolve superClass
			if (superClassId != null && superClassId.length() > 0) {
				superClass = ManagedBuildManager.getExtensionToolChain(superClassId);
				if (superClass == null) {
					// Report error
					ManagedBuildManager.OutputResolveError(
							"superClass",	//$NON-NLS-1$
							superClassId,
							"toolChain",	//$NON-NLS-1$
							getId());
				}
			}
			//  Call resolveReferences on our children
			if (targetPlatform != null) {
				targetPlatform.resolveReferences();
			}
			if (builder != null) {
				builder.resolveReferences();
			}
			Iterator iter = getToolList().listIterator();
			while (iter.hasNext()) {
				Tool toolChild = (Tool) iter.next();
				toolChild.resolveReferences();
			}
		}
	}
	
	/* (non-Javadoc)
	 * Normalize the list of output extensions,for all tools in the toolchain by populating the list
	 * with an empty string for those tools which have no explicit output extension (as defined in the
	 * manifest file. In a post 2.1 manifest, all tools must have a specifed output extension, even
	 * if it is "")
	 */
	public void normalizeOutputExtensions(){
		ITool[] tools = getTools();
		if (tools != null) {
			for (int i = 0; i < tools.length; i++) {
				ITool tool = tools[i];
				String[] extensions = tool.getOutputExtensions();
				if (extensions == null) {
					tool.setOutputExtensions(""); //$NON-NLS-1$
					continue;
				}
				if (extensions.length == 0){ 
					tool.setOutputExtensions(""); //$NON-NLS-1$
				    continue;
				}
			}
		}
	}

}
