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

import java.io.File;
import java.io.FileNotFoundException;
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
import org.eclipse.ptp.rm.jaxb.core.data.Attribute;
import org.eclipse.ptp.rm.jaxb.core.data.Control;
import org.eclipse.ptp.rm.jaxb.core.data.Property;
import org.eclipse.ptp.rm.jaxb.core.data.ResourceManagerData;
import org.eclipse.ptp.rm.jaxb.core.variables.RMVariableMap;
import org.xml.sax.SAXException;

public class JAXBInitializationUtils implements IJAXBNonNLSConstants {

	private JAXBInitializationUtils() {
	}

	public static URL getURL(String name) throws IOException {
		URL instance = JAXBCorePlugin.getResource(name);
		if (instance == null) {
			File f = new File(name);
			if (f.exists() && f.isFile()) {
				instance = f.toURL();
			} else {
				throw new FileNotFoundException(name);
			}
		}
		return instance;
	}

	public static void initializeMap(ResourceManagerData rmData, RMVariableMap instance) {
		Control control = rmData.getControlData();
		instance.clear();
		Map<String, Object> env = instance.getVariables();
		addProperties(env, control);
		addAttributes(env, control);
		instance.setInitialized(true);
	}

	public static ResourceManagerData initializeRMData(URL xml) throws IOException, SAXException, URISyntaxException, JAXBException {
		return unmarshalResourceManagerData(xml);
	}

	public static void validate(String xml) throws SAXException, IOException, URISyntaxException {
		validate(getURL(xml));
	}

	public static void validate(URL instance) throws SAXException, IOException, URISyntaxException {
		URL xsd = JAXBCorePlugin.getResource(RM_XSD);
		SchemaFactory factory = SchemaFactory.newInstance(XMLSchema);
		Schema schema = factory.newSchema(xsd);
		Validator validator = schema.newValidator();
		Source source = new StreamSource(instance.openStream());
		validator.validate(source);
	}

	private static void addAttributes(Map<String, Object> env, Control control) {
		List<Attribute> jobAttributes = control.getAttribute();
		for (Attribute jobAttribute : jobAttributes) {
			String name = jobAttribute.getName();
			env.put(name, jobAttribute);
		}
	}

	private static void addProperties(Map<String, Object> env, Control control) {
		List<Property> properties = control.getProperty();
		for (Property property : properties) {
			env.put(property.getName(), property);
		}
	}

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
