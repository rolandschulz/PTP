/******************************************************************************
 * Copyright (c) 2007 IBM Corporation.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - Initial Implementation
 *

 *****************************************************************************/
package org.eclipse.ptp.cell.pdt.xml.core;

import java.util.HashMap;
import java.util.Map;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.eclipse.core.runtime.IPath;
import org.eclipse.ptp.cell.pdt.xml.core.eventgroup.Event;
import org.eclipse.ptp.cell.pdt.xml.core.eventgroup.EventGroup;
import org.eclipse.ptp.cell.pdt.xml.core.eventgroup.EventGroupForest;
import org.eclipse.ptp.cell.pdt.xml.core.eventgroup.EventSubgroup;
import org.eclipse.ptp.cell.pdt.xml.debug.Debug;
import org.w3c.dom.Document;
import org.w3c.dom.Element;


/**
 * This class has methods to generate a PDT XML configuration file for the
 * Cell B.E. architecture
 * 
 * @author Richard Maciel
 *
 */
public class CellPdtXmlGenerator extends AbstractPdtXmlGenerator {

	protected Boolean ppeProfiling, speProfiling;  
	
	/**
	 * Generates a Xml generator for the Cell B.E. architecture
	 * 
	 * @param xmlFilePath
	 * @param eventGroupForest
	 */
	public CellPdtXmlGenerator(IPath xmlFilePath, EventGroupForest eventGroupForest, Boolean ppeProfiling,
			Boolean speProfiling) {
		super(xmlFilePath, eventGroupForest);
		this.ppeProfiling = ppeProfiling;
		this.speProfiling = speProfiling;
	}

	@Override
	public Document generatePdtXmlDocument() {
		Debug.read();
		
		if(Debug.DEBUG_XML_GENERATOR) {
			Debug.POLICY.enter();
			Debug.POLICY.trace("EventGroupForest: {0}", (Object [])eventGroupForest.toStringVector()); //$NON-NLS-1$
			Debug.POLICY.trace("PPE profiling enable: {0}  SPE profiling enable: {1}", ppeProfiling, speProfiling); //$NON-NLS-1$
		}
		
		DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();

		// Use the document build to create a new XML file
		DocumentBuilder xmlBuilder;
		//DocumentBuilder parser;	

		try {
			xmlBuilder = factory.newDocumentBuilder();
			Document xml = xmlBuilder.newDocument();

			// Create "pdt_configuration" tag (root) and set its attributes
			Element pdtConfigElement = createPdtConfigElement(xml);

			// Create the "groups" tag under "pdt_configuration"
			createGroupsElement(xml, pdtConfigElement);

			// Create the first configuration element
			String confName = "CBE"; //$NON-NLS-1$
			Element parentGrpCtlElement = createConfigurationElement(xml, pdtConfigElement, confName, "none", ppeProfiling); //$NON-NLS-1$

			// Create another if the architecture is cell b.e.
			// And fill it with the event groups.
			// In case this isn't Cell B.E. architecture, XML file is done.
			Element childGrpCtlElement = null;
			childGrpCtlElement = 
				createConfigurationElement(xml, pdtConfigElement, "SPE", confName, speProfiling); //$NON-NLS-1$

			Map<EventGroup, Element> ppeGroupMap, speGroupMap;
			ppeGroupMap = new HashMap<EventGroup, Element>();
			speGroupMap = new HashMap<EventGroup, Element>();

			// Create all "groups" tag under "groupsControl", but assign them to
			// the right configuration according to the configuration type.
			for (EventGroup group : eventGroupForest.getVisibleGroups()) {
				//Element groupElement = createGroupElement(xml, group);


				Map<EventSubgroup, Element> ppeSubgroupMap, speSubgroupMap;
				ppeSubgroupMap = new HashMap<EventSubgroup, Element>();
				speSubgroupMap = new HashMap<EventSubgroup, Element>();

				// Create subgroups
				for (EventSubgroup subgroup : group.getSubgroups()) {

					// Create event
					for(Event event: subgroup.getEvents()) {
						Element eventElement = xml.createElement("event");//$NON-NLS-1$
						// Set attributes
						eventElement.setAttribute("name", event.getName());//$NON-NLS-1$
						eventElement.setAttribute("active", String.valueOf(event.getActive()));//$NON-NLS-1$

						/*
						 * Following code verifies in which configuration the
						 * event should be inserted.
						 */
						String include = event.getInclude();
						Map<EventGroup, Element> selGroupMap = null;
						Map<EventSubgroup, Element> selSubgroupMap = null;
						Element selGrpCtlElement = null;
						if(include.matches(".*pdt_ppe_event_header\\.xml$")) {//$NON-NLS-1$
							selGroupMap = ppeGroupMap;
							selSubgroupMap = ppeSubgroupMap;
							selGrpCtlElement = parentGrpCtlElement;
						} else if(include.matches(".*pdt_spe_event_header\\.xml$")) {//$NON-NLS-1$
							selGroupMap = speGroupMap;
							selSubgroupMap = speSubgroupMap;
							selGrpCtlElement = childGrpCtlElement;
						}

						/* 
						 * Insert subgroup and group tags into the
						 * right configuration, creating its groups and
						 * subgroups on demand (if needed).
						 */ 
						// Get the xml subgroup element associated
						Element selSubgroupElement = selSubgroupMap.get(subgroup);
						if(selSubgroupElement == null) {
							// subgroup doesn't exist. Create one
							selSubgroupElement = createSubgroupElement(xml, subgroup);
							//selSubgroupElement.appendChild(eventElement);
							selSubgroupMap.put(subgroup, selSubgroupElement);

							// Get the xml group element associated
							Element selGroupElement = selGroupMap.get(group);
							if(selGroupElement == null) {
								// group doesn't exist. Create one.
								selGroupElement = createGroupElement(xml, group);
								selGroupMap.put(group, selGroupElement);
								selGrpCtlElement.appendChild(selGroupElement);
							}
							// Insert the selected subgroup element under the selected group element
							selGroupElement.appendChild(selSubgroupElement);
						} 
						// Insert the event element under the selected subgroup element
						selSubgroupElement.appendChild(eventElement);
					}		
				}
			}

			Debug.POLICY.exit(Debug.DEBUG_XML_GENERATOR);
			
			return xml;

		} catch (ParserConfigurationException e) {
			Debug.POLICY.error(Debug.DEBUG_XML_GENERATOR, e);
			throw new PdtXmlGenerationException(e);
		}

		// Fetch a xml parsed document
		//parser = factory.newDocumentBuilder();
		//Document xmlFile = parser.parse(files[i]);
	}
}
