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

import java.io.File;
import java.io.IOException;
import java.util.HashSet;
import java.util.Set;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.Path;
import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.osgi.util.NLS;
import org.eclipse.ptp.cell.pdt.xml.core.eventgroup.Event;
import org.eclipse.ptp.cell.pdt.xml.core.eventgroup.EventGroup;
import org.eclipse.ptp.cell.pdt.xml.core.eventgroup.EventGroupForest;
import org.eclipse.ptp.cell.pdt.xml.core.eventgroup.EventSubgroup;
import org.eclipse.ptp.cell.pdt.xml.debug.Debug;
import org.eclipse.ptp.cell.preferences.PreferencesPlugin;
import org.eclipse.ptp.cell.preferences.ui.PreferenceConstants;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;


/** 
 * This class is responsible for creating a set of {@link EventGroup} data-structure, which provides all information about
 * the PDT events, organized in groups and subgroups
 * 
 * @author Richard Maciel
 *
 */
public class PdtEventForestFactory {
	IPath eventGroupsDir;
	
	public PdtEventForestFactory(IPath eventGroupsDir) {
		this.eventGroupsDir = eventGroupsDir;
	}
	
	/**
	 * Creates an EventGroupForest containing zero EventGroups
	 * 
	 * @return
	 */
	public EventGroupForest createEmptyEventGroupForest() {
		Debug.read();
		Debug.POLICY.pass(Debug.DEBUG_EVENT_FOREST_GENERATOR);
		
		EventGroupForest newForest = new EventGroupForest();
		newForest.setVisibleGroups(new HashSet<EventGroup>());
		newForest.setInvisibleGroups(new HashSet<EventGroup>());
		
		
		return newForest;
	}
	
	/**
	 * Generate an EventGroupForest containing all ConfigGroups found in the selected directory.
	 * 
	 * @return A EventGroupForest
	 * @throws ConfigGroupParserException
	 */
	public EventGroupForest createEventGroupForest() throws ConfigGroupParserException {
		Debug.read();
		Debug.POLICY.enter(Debug.DEBUG_EVENT_FOREST_GENERATOR);
		Debug.POLICY.trace(Debug.DEBUG_EVENT_FOREST_GENERATOR, "Event group dir {0}", eventGroupsDir); //$NON-NLS-1$
		
		DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
		
		File dir = new File(eventGroupsDir.toOSString());
		
		if(!dir.isDirectory()) {
			ConfigGroupParserException parserException = new ConfigGroupParserException(
					NLS.bind(Messages.PdtEventForestFactory_createEventGroupForest_PathNotADir, 
							eventGroupsDir.toOSString()) ); 
			
			Debug.POLICY.error(Debug.DEBUG_EVENT_FOREST_GENERATOR, parserException);
			throw parserException;
		}
		
		// The PDT event group path is relative to the SDK SYSROOT
		IPath sdkroot = getSdkSysroot();
		int numofseg = eventGroupsDir.matchingFirstSegments(sdkroot);
		IPath evtGrpDirSysrootRel = eventGroupsDir.removeFirstSegments(numofseg);
		evtGrpDirSysrootRel = evtGrpDirSysrootRel.makeAbsolute();
		
		// Create Sets where the groups will be stored (both visible and invisible)
		Set<EventGroup> visibleGroupSet = new HashSet<EventGroup>();
		Set<EventGroup> invisibleGroupSet = new HashSet<EventGroup>();
		
		// Scan files in the directory
		File [] files = dir.listFiles();
		for(int i=0; i < files.length; i++) {
			
			// Check if is not a directory or the wrong file
			if(files[i].isDirectory()|| 
				files[i].getName().matches("^pdt_.*_event_header\\.xml$")) { //$NON-NLS-1$
				continue;
			}
			
			// Use the document build to extract the xml
			DocumentBuilder parser;	
			try {
				// Fetch a xml parsed document
				parser = factory.newDocumentBuilder();
				Document xmlFile = parser.parse(files[i]);
				
				Node groupNode = xmlFile.getFirstChild();
				
				// Check element validity
				if(!(groupNode instanceof Element) || !(groupNode.getNodeName().equals("pdtGroup"))) { //$NON-NLS-1$
					// Ignore this group, but trace it
					//ConfigGroupParserException confParseException = new ConfigGroupParserException(Messages.PdtEventForestFactory_CreateEventForest_Error_InvalidElementType + groupNode.getNodeName()); 
					
					Debug.POLICY.trace(Debug.DEBUG_EVENT_FOREST_GENERATOR, "File {0} doesn't specify a valid group - ignoring it", files[0]);//$NON-NLS-1$
					continue;
					//throw confParseException;
				} 
				Element groupElem = (Element)groupNode;
				
				// Create new EventGroup
				EventGroup eventGroup = new EventGroup();
				
				// Set group attributes
				eventGroup.setName(groupElem.getAttribute("name")); //$NON-NLS-1$
				eventGroup.setId(groupElem.getAttribute("id")); //$NON-NLS-1$
				eventGroup.setYStart(new Float(0.0));
				eventGroup.setYEnd(new Float(0.0));
				eventGroup.setColor(0x0);
				eventGroup.setActive(false);
				eventGroup.setAssociatedPath(evtGrpDirSysrootRel.append(files[i].getName()));
				//IPath teste = new Path("/"); //$NON-NLS-1$
				//teste.
				 
				// GENERAL group events is always active, so it doesn't need
				// to detail its subgroups and events.
				// It also is added on the invisible set, instead of into the visible
				if(eventGroup.getName().equals(EventGroup.GENERAL_GROUP)) {
					eventGroup.setSubgroups(new HashSet<EventSubgroup>());
					invisibleGroupSet.add(eventGroup);
					continue;
				}
				
				// Insert group into the set
				visibleGroupSet.add(eventGroup);
				
				// Build structure from the elements and attributes
				NodeList subgroups = groupElem.getChildNodes();
				for(int j=0; j < subgroups.getLength(); j++) {
					if(!(subgroups.item(j) instanceof Element)) {
						continue;
					}
					
					if(!subgroups.item(j).getNodeName().equals("subGroup")) { //$NON-NLS-1$
						ConfigGroupParserException cfgGrpParserException = new ConfigGroupParserException(Messages.PdtEventForestFactory_createEventGroupForest_InvalidElementType + subgroups.item(j).getNodeName());
						Debug.POLICY.error(Debug.DEBUG_EVENT_FOREST_GENERATOR, cfgGrpParserException);
						throw cfgGrpParserException;
					}
					
					Element subgroupElem = (Element)subgroups.item(j);
					
					// Create new EventSubgroup and set it as a subgroup of the actual group
					EventSubgroup eventSubgroup = new EventSubgroup();
					eventGroup.addSubgroup(eventSubgroup);
					
					// Set subgroup attributes
					eventSubgroup.setName(subgroupElem.getAttribute("name")); //$NON-NLS-1$
					eventSubgroup.setId(subgroupElem.getAttribute("id")); //$NON-NLS-1$
					eventSubgroup.setParent(eventGroup);
					eventSubgroup.setActive(false);
					
					// Get events from subgroups
					NodeList events = subgroupElem.getChildNodes();
					for(int k=0; k < events.getLength(); k++) {
						if(!(events.item(k) instanceof Element)) {
							continue;
						}
						if(!events.item(k).getNodeName().equals("recordType")) { //$NON-NLS-1$
							ConfigGroupParserException confGroupParseException = 
								new ConfigGroupParserException(Messages.PdtEventForestFactory_createEventGroupForest_InvalidElementType + subgroups.item(j).getNodeName());
							Debug.POLICY.error(Debug.DEBUG_EVENT_FOREST_GENERATOR, confGroupParseException);
							
							throw confGroupParseException;
						}
						Element eventElem = (Element)events.item(k);
						
						// Create new Event and set it as a event of the actual subgroup
						Event event = new Event();
						eventSubgroup.addEvent(event);
						
						// Set event attributes
						event.setName(eventElem.getAttribute("name")); //$NON-NLS-1$
						event.setId(eventElem.getAttribute("id")); //$NON-NLS-1$
						event.setDescription(eventElem.getAttribute("description")); //$NON-NLS-1$
						event.setType(eventElem.getAttribute("type")); //$NON-NLS-1$
						event.setParent(eventSubgroup);
						event.setActive(false);
						
						// Set the include attribute from the "include" tag element
						NodeList include = eventElem.getElementsByTagName("include"); //$NON-NLS-1$
						assert include.getLength() == 1 : "Single include tag is necessary and allowed"; //$NON-NLS-1$
						Element includeElem = (Element)include.item(0);
						
						event.setInclude(includeElem.getAttribute("href")); //$NON-NLS-1$
					}
				}
				
			} catch (ParserConfigurationException e) {
				ConfigGroupParserException confParseException = new ConfigGroupParserException(e);
				Debug.POLICY.error(Debug.DEBUG_EVENT_FOREST_GENERATOR, confParseException);
				throw confParseException;
			} catch (SAXException e) {
				ConfigGroupParserException confParseException = new ConfigGroupParserException(e);
				Debug.POLICY.error(Debug.DEBUG_EVENT_FOREST_GENERATOR, confParseException);
				throw confParseException;
			} catch (IOException e) {
				ConfigGroupParserException confParseException = new ConfigGroupParserException(e);
				Debug.POLICY.error(Debug.DEBUG_EVENT_FOREST_GENERATOR, confParseException);
				throw confParseException;
			}
			
		}
		Debug.POLICY.exit(Debug.DEBUG_EVENT_FOREST_GENERATOR);
		
		EventGroupForest groups = new EventGroupForest();
		groups.setVisibleGroups(visibleGroupSet);
		groups.setInvisibleGroups(invisibleGroupSet);
		
		return groups;
	}

	/**
	 * Get the SDK Sysroot path from the Cell IDE preferences
	 * 
	 * @return
	 */
	private IPath getSdkSysroot() {
		IPreferenceStore store = PreferencesPlugin.getDefault().getPreferenceStore();
		
		Path sdkSysroot = new Path(store.getString(PreferenceConstants.SDK_SYSROOT));
		return sdkSysroot;
	}
	
	/*public static void main(String[] args) {
		ConfigFactory cf = new ConfigFactory(new Path("/home/richardm/doc/Cell SDK docs/PDT/event groups"));
		Set<EventGroup> groupSet;
		try {
			groupSet = cf.createAllConfigGroups();
		} catch (ConfigGroupParserException e) {
			throw new RuntimeException(e);
		}
		
		for (EventGroup eventGroup : groupSet) {
			System.out.println("Group: " + eventGroup.getName());
			for (EventSubgroup eventSubgroup : eventGroup.getSubgroups()) {
				System.out.println("\tSubgroup: " + eventSubgroup.getName());
				for(Event event : eventSubgroup.getEvents()) {
					System.out.println("\t\tEvents: " + event.getName() + " type: " + event.getType());
				}
			}
		}
	}*/
}
