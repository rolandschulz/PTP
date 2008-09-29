package org.eclipse.ptp.cell.pdt.xml.core;

import java.io.File;
import java.io.IOException;

import javax.xml.transform.Result;
import javax.xml.transform.Source;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import org.eclipse.core.runtime.IPath;
import org.eclipse.ptp.cell.pdt.xml.core.eventgroup.EventGroup;
import org.eclipse.ptp.cell.pdt.xml.core.eventgroup.EventGroupForest;
import org.eclipse.ptp.cell.pdt.xml.core.eventgroup.EventSubgroup;
import org.eclipse.ptp.cell.pdt.xml.debug.Debug;
import org.w3c.dom.DOMException;
import org.w3c.dom.Document;
import org.w3c.dom.Element;



/**
 * This class provides methods to build elements of a PDT XML configuration file.
 * It is intended to be extended by classes that will generate a architecture-specific configuration
 * file
 * 
 * @author Richard Maciel
 *
 */
public abstract class AbstractPdtXmlGenerator {

	protected IPath xmlFilePath;

	public abstract Document generatePdtXmlDocument();

	protected EventGroupForest eventGroupForest;
	protected boolean isCellArchitecture;

	/**
	 * Create new generator
	 * 
	 * @param xmlFilePath File path of the file that will be created
	 * @param eventGroupForest
	 */
	public AbstractPdtXmlGenerator(IPath xmlFilePath, EventGroupForest eventGroupForest) {
		this.xmlFilePath = xmlFilePath;
		this.eventGroupForest = eventGroupForest;
	}

	public void generatePdtXmlFile()
			throws ConfigurationFileGenerationException {
				Debug.read();
				Debug.POLICY.enter(Debug.DEBUG_XML_GENERATOR);
		
				Document doc = generatePdtXmlDocument();
				
				try {
					File xmlFile = xmlFilePath.toFile();
					if(!xmlFile.exists())
						xmlFile.createNewFile();
				
					TransformerFactory tranFactory = TransformerFactory.newInstance();
					Transformer aTransformer = tranFactory.newTransformer();
			
					Source src = new DOMSource(doc);
					Result dest = new StreamResult(xmlFile.getPath());
					aTransformer.transform(src, dest);
				} catch (TransformerException e) {
					Debug.POLICY.error(Debug.DEBUG_XML_GENERATOR, e);
					throw new ConfigurationFileGenerationException(e);
				} catch (IOException e) {
					Debug.POLICY.error(Debug.DEBUG_XML_GENERATOR, e);
					throw new ConfigurationFileGenerationException(e);
				}
				
				Debug.POLICY.exit(Debug.DEBUG_XML_GENERATOR);
			}

	/**
	 * @param xml
	 * @param subgroup
	 * @return
	 * @throws DOMException
	 */
	protected Element createSubgroupElement(Document xml, EventSubgroup subgroup)
			throws DOMException {
				Element subgroupElement = xml.createElement("sub_group");//$NON-NLS-1$
				// Set attributes
				subgroupElement.setAttribute("name", subgroup.getName());//$NON-NLS-1$
				subgroupElement.setAttribute("active", String.valueOf(subgroup.getActive()));//$NON-NLS-1$
				return subgroupElement;
			}

	/**
	 * @param xml
	 * @param group
	 * @return
	 * @throws DOMException
	 */
	protected Element createGroupElement(Document xml, EventGroup group)
			throws DOMException {
				Element groupElement = xml.createElement("group");//$NON-NLS-1$
				// Set attributes
				groupElement.setAttribute("name", group.getName());//$NON-NLS-1$
				groupElement.setAttribute("active", String.valueOf(group.getActive()));//$NON-NLS-1$
				return groupElement;
			}

	/**
	 * Creates the "configuration" element tag for the XML file, with the 
	 * "GENERAL" tag under (with "profile" tag set). Also, all attributes are
	 * filled.
	 * 
	 * @param xml
	 * @param pdtConfigElement
	 * @return the "groupsControl" tag where the event groups will be inserted
	 * @throws DOMException
	 */
	protected Element createConfigurationElement(Document xml, Element pdtConfigElement,
			String configName, String hostName, boolean isProfileActive) throws DOMException {
				// Create "configuration" tag under "pdt_configuration" and set
				// its attribute.
				Element configElement =  xml.createElement("configuration");//$NON-NLS-1$
				pdtConfigElement.appendChild(configElement);
				// We set it to the name of the architecture
				configElement.setAttribute("name", configName);//$NON-NLS-1$
				
				// Create "host" tag under "configuration" and set its attribute
				Element hostElement = xml.createElement("host");//$NON-NLS-1$
				configElement.appendChild(hostElement);
				hostElement.setAttribute("name", hostName);//$NON-NLS-1$
				
				// Create "groupsControl" under "configuration"
				Element groupsControlElement = xml.createElement("groupsControl");//$NON-NLS-1$
				configElement.appendChild(groupsControlElement);
				
				
				// Add the GENERAL group under the configuration
				Element generalGroupElement = xml.createElement("group");//$NON-NLS-1$
				groupsControlElement.appendChild(generalGroupElement);
				// Set attributes
				generalGroupElement.setAttribute("name", "GENERAL");//$NON-NLS-1$ //$NON-NLS-2$
				generalGroupElement.setAttribute("active", "true");//$NON-NLS-1$ //$NON-NLS-2$
				
				// Add the profile option under the GENERAL group
				Element profileElement = xml.createElement("profile");//$NON-NLS-1$
				generalGroupElement.appendChild(profileElement);
				// Set attribute
				profileElement.setAttribute("active", String.valueOf(isProfileActive));//$NON-NLS-1$
				
				return groupsControlElement;
			}

	/**
	 * @param xml
	 * @param pdtConfigElement
	 * @throws DOMException
	 */
	protected void createGroupsElement(Document xml, Element pdtConfigElement)
			throws DOMException {
				Element groupsElement = xml.createElement("groups"); //$NON-NLS-1$
				pdtConfigElement.appendChild(groupsElement);
				
				// Create "group" tag under "groups" tag and set its
				// attributes
				// IMPORTANT: We include all groups here, not only the visible ones, hence the
				// calling to the getGroupsUnion() method
				for (EventGroup group : eventGroupForest.getGroupsUnion()) {
					Element groupElement = xml.createElement("group"); //$NON-NLS-1$
					groupsElement.appendChild(groupElement);
					groupElement.setAttribute("name", group.getName()); //$NON-NLS-1$
					groupElement.setAttribute("description", ""); //$NON-NLS-1$ //$NON-NLS-2$
					groupElement.setAttribute("id", group.getId()); //$NON-NLS-1$
					
					// Create "view" tag under group and set its attributes
					Element viewElement = xml.createElement("view"); //$NON-NLS-1$
					groupElement.appendChild(viewElement);
					viewElement.setAttribute("yStart", String.valueOf(group.getYStart())); //$NON-NLS-1$
					viewElement.setAttribute("yEnd", String.valueOf(group.getYEnd()));//$NON-NLS-1$
					viewElement.setAttribute("color", "0x" + Integer.toHexString(group.getColor())); //$NON-NLS-1$ //$NON-NLS-2$
					
					// Create "include" tag under group and set its attribute
					Element includeElement = xml.createElement("include"); //$NON-NLS-1$
					groupElement.appendChild(includeElement);
					includeElement.setAttribute("href", group.getAssociatedPath().toOSString()); //$NON-NLS-1$
				}
			}

	/**
	 * @param xml
	 * @return
	 * @throws DOMException
	 */
	protected Element createPdtConfigElement(Document xml) throws DOMException {
		Element pdtConfigElement = xml.createElement("pdt_configuration"); //$NON-NLS-1$
		xml.appendChild(pdtConfigElement);
		pdtConfigElement.setAttribute("application_name", "");//$NON-NLS-1$ //$NON-NLS-2$
		pdtConfigElement.setAttribute("output_dir", ".");//$NON-NLS-1$ //$NON-NLS-2$
		pdtConfigElement.setAttribute("version", "3.0");//$NON-NLS-1$ //$NON-NLS-2$
		return pdtConfigElement;
	}

}