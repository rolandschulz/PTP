/*******************************************************************************
 * Copyright (c) 2011 University of Illinois All rights reserved. This program
 * and the accompanying materials are made available under the terms of the
 * Eclipse Public License v1.0 which accompanies this distribution, and is
 * available at http://www.eclipse.org/legal/epl-v10.html 
 * 	
 * Contributors: 
 * 	Albert L. Rossi - design and implementation
 ******************************************************************************/
package org.eclipse.ptp.rm.jaxb.core.utils;

import java.io.IOException;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.List;
import java.util.Map;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBElement;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Unmarshaller;
import javax.xml.transform.Source;
import javax.xml.transform.stream.StreamSource;
import javax.xml.validation.Schema;
import javax.xml.validation.SchemaFactory;
import javax.xml.validation.Validator;

import org.eclipse.ptp.rm.jaxb.core.IJAXBNonNLSConstants;
import org.eclipse.ptp.rm.jaxb.core.JAXBCorePlugin;
import org.eclipse.ptp.rm.jaxb.core.data.AttributeType;
import org.eclipse.ptp.rm.jaxb.core.data.ControlType;
import org.eclipse.ptp.rm.jaxb.core.data.PropertyType;
import org.eclipse.ptp.rm.jaxb.core.data.ResourceManagerData;
import org.eclipse.ptp.rm.jaxb.core.messages.Messages;
import org.eclipse.ptp.rm.jaxb.core.variables.RMVariableMap;
import org.xml.sax.SAXException;
import org.xml.sax.SAXParseException;

/**
 * Convenience methods for validating and unmarshaling XML using JAXB.
 * 
 * @author arossi
 * 
 */
public class JAXBInitializationUtils implements IJAXBNonNLSConstants {

	private JAXBInitializationUtils() {
	}

	/**
	 * Retrieves Property and Attribute definitions from the data tree and adds
	 * them to the environment map.
	 * 
	 * @param rmData
	 *            the JAXB data tree
	 * @param instance
	 *            the active instance of the resource manager environment map
	 */
	public static void initializeMap(ResourceManagerData rmData, RMVariableMap instance) {
		ControlType control = rmData.getControlData();
		instance.clear();
		Map<String, Object> env = instance.getVariables();
		addProperties(env, control);
		addAttributes(env, control);
		instance.setInitialized(true);
	}

	/**
	 * Delegates to {@link #unmarshalResourceManagerData(URL)}
	 * 
	 * @param xml
	 *            location of the configuration file.
	 * @return the constructed data tree
	 * @throws IOException
	 * @throws SAXException
	 * @throws URISyntaxException
	 * @throws JAXBException
	 */
	public static ResourceManagerData initializeRMData(URL xml) throws IOException, SAXException, URISyntaxException, JAXBException {
		return unmarshalResourceManagerData(xml);
	}

	/**
	 * Validates the XML against the internal XSD for JAXB resource managers.
	 * 
	 * @param instance
	 *            location of the configuration file.
	 * @throws SAXException
	 *             if invalid
	 * @throws IOException
	 * @throws URISyntaxException
	 */
	public static void validate(URL instance) throws SAXException, IOException, URISyntaxException {
		URL xsd = JAXBCorePlugin.getResource(RM_XSD);
		SchemaFactory factory = SchemaFactory.newInstance(XMLSchema);
		Schema schema = factory.newSchema(xsd);
		Validator validator = schema.newValidator();
		Source source = new StreamSource(instance.openStream());
		try {
			validator.validate(source);
		} catch (SAXParseException sax) {
			JAXBCorePlugin.log(printInfo(sax));
			throw sax;
		}
	}

	/**
	 * Adds the attributes. If the attribute value is <code>null</code>,
	 * overwrites it with the default.
	 * 
	 * @param env
	 *            the active instance of the resource manager environment map
	 * @param control
	 *            JAXB data subtree for control part of resource manager
	 */
	private static void addAttributes(Map<String, Object> env, ControlType control) {
		List<AttributeType> jobAttributes = control.getAttribute();
		for (AttributeType jobAttribute : jobAttributes) {
			env.put(jobAttribute.getName(), jobAttribute);
			if (jobAttribute.getValue() == null) {
				jobAttribute.setValue(jobAttribute.getDefault());
			}
		}
	}

	/**
	 * Adds the properties. If the property value is <code>null</code>,
	 * overwrites it with the default.
	 * 
	 * @param env
	 *            the active instance of the resource manager environment map
	 * @param control
	 *            JAXB data subtree for control part of resource manager
	 */
	private static void addProperties(Map<String, Object> env, ControlType control) {
		List<PropertyType> properties = control.getProperty();
		for (PropertyType property : properties) {
			env.put(property.getName(), property);
			if (property.getValue() == null) {
				property.setValue(property.getDefault());
			}
		}
	}

	/**
	 * Details from the parse exception.
	 * 
	 * @param e
	 *            thrown parse exception
	 * @return line, column and other info.
	 */
	private static String printInfo(SAXParseException e) {
		StringBuffer sb = new StringBuffer();
		sb.append(Messages.PublicId + e.getPublicId()).append(LINE_SEP);
		sb.append(Messages.SystemId + e.getSystemId()).append(LINE_SEP);
		sb.append(Messages.LineNumber + e.getLineNumber()).append(LINE_SEP);
		sb.append(Messages.ColumnNumber + e.getColumnNumber()).append(LINE_SEP);
		sb.append(Messages.Message + e.getMessage()).append(LINE_SEP);
		return sb.toString();
	}

	/**
	 * First validates the xml, then gets the JAXB context and calls the JAXB
	 * unmarshaller from it.
	 * 
	 * @param xml
	 *            location of the configuration file.
	 * @return the constructed data tree
	 * @throws JAXBException
	 *             problem encountered during unmarshaling
	 * @throws IOException
	 * @throws SAXException
	 *             validation error
	 * @throws URISyntaxException
	 */
	private static ResourceManagerData unmarshalResourceManagerData(URL xml) throws JAXBException, IOException, SAXException,
			URISyntaxException {
		validate(xml);
		JAXBContext jc = JAXBContext.newInstance(JAXB_CONTEXT, JAXBInitializationUtils.class.getClassLoader());
		Unmarshaller u = jc.createUnmarshaller();
		JAXBElement<?> o = (JAXBElement<?>) u.unmarshal(xml.openStream());
		ResourceManagerData rmdata = (ResourceManagerData) o.getValue();
		return rmdata;
	}
}
