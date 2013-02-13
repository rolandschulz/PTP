/*******************************************************************************
 * Copyright (c) 2011 University of Illinois All rights reserved. This program
 * and the accompanying materials are made available under the terms of the
 * Eclipse Public License v1.0 which accompanies this distribution, and is
 * available at http://www.eclipse.org/legal/epl-v10.html 
 * 	
 * Contributors: 
 * 	Albert L. Rossi - design and implementation
 ******************************************************************************/
package org.eclipse.ptp.internal.rm.jaxb.core;

import java.io.EOFException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.StringReader;
import java.io.StringWriter;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.List;
import java.util.Map;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBElement;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;
import javax.xml.bind.Unmarshaller;
import javax.xml.namespace.QName;
import javax.xml.transform.Source;
import javax.xml.transform.stream.StreamSource;
import javax.xml.validation.Schema;
import javax.xml.validation.SchemaFactory;
import javax.xml.validation.Validator;

import org.eclipse.ptp.internal.rm.jaxb.core.messages.Messages;
import org.eclipse.ptp.rm.jaxb.core.IVariableMap;
import org.eclipse.ptp.rm.jaxb.core.data.AttributeType;
import org.eclipse.ptp.rm.jaxb.core.data.ControlType;
import org.eclipse.ptp.rm.jaxb.core.data.MonitorType;
import org.eclipse.ptp.rm.jaxb.core.data.ResourceManagerData;
import org.xml.sax.SAXException;
import org.xml.sax.SAXParseException;

/**
 * Convenience methods for validating and unmarshaling XML using JAXB.
 * 
 * @author arossi
 * @since 1.1
 * 
 */
public class JAXBInitializationUtils {

	private static Unmarshaller unmarshaller;
	private static Validator validator;

	/**
	 * Streams the content from the url.
	 * 
	 * @param url
	 *            location of XML resource
	 * @return xml string
	 */
	public static String getRMConfigurationXML(URL url) throws IOException {
		StringBuffer buffer = new StringBuffer();
		if (url != null) {
			InputStreamReader reader = new InputStreamReader(url.openStream());
			char[] chars = new char[4096];
			int read = 0;
			while (true) {
				try {
					read = reader.read(chars, 0, chars.length);
				} catch (EOFException eof) {
					break;
				}
				if (read <= 0) {
					break;
				}
				buffer.append(chars, 0, read);
			}
			return buffer.toString();
		}
		return null;
	}

	/**
	 * Retrieves Property and Attribute definitions from the data tree and adds them to the environment map.
	 * 
	 * @param rmData
	 *            the JAXB data tree
	 * @param instance
	 *            the active instance of the resource manager environment map
	 */
	public static void initializeMap(ResourceManagerData rmData, IVariableMap instance) {
		ControlType control = rmData.getControlData();
		instance.clear();
		Map<String, AttributeType> env = instance.getAttributes();
		addAttributes(env, control);
		instance.setInitialized(true);
	}

	/**
	 * Delegates to {@link #unmarshalResourceManagerData(URL)}
	 * 
	 * @param xml
	 *            the configuration file.
	 * @return the constructed data tree
	 * @throws IOException
	 * @throws SAXException
	 * @throws URISyntaxException
	 * @throws JAXBException
	 */
	public static ResourceManagerData initializeRMData(String xml) throws IOException, SAXException, URISyntaxException,
			JAXBException {
		return unmarshalResourceManagerData(xml);
	}

	/**
	 * Delegates to {@link #getRMConfigurationXML(URL)}, {@link #initializeRMData(String)}
	 * 
	 * @param url
	 *            of the XML for the configuration
	 * @return xml for the configuration
	 * @throws IOException
	 * @throws SAXException
	 * @throws URISyntaxException
	 * @throws JAXBException
	 */
	public static ResourceManagerData initializeRMData(URL url) throws IOException, SAXException, URISyntaxException, JAXBException {
		return initializeRMData(getRMConfigurationXML(url));
	}

	/**
	 * Marshal a subtree into a string. This can be used to convert part of the XML tree into a string.
	 * 
	 * @param jaxbElement
	 *            element to marshal
	 * @param className
	 *            class of the root element
	 * @root root element tag
	 * @return
	 */
	@SuppressWarnings({ "rawtypes", "unchecked" })
	public static String marshalData(Object jaxbElement, Class className, String root) {
		StringWriter writer = new StringWriter();
		try {
			JAXBContext jc = JAXBContext
					.newInstance(JAXBCoreConstants.JAXB_CONTEXT, JAXBInitializationUtils.class.getClassLoader());
			Marshaller marshaller = jc.createMarshaller();
			marshaller.setProperty(Marshaller.JAXB_FRAGMENT, true);
			marshaller.marshal(new JAXBElement(new QName("", root), className, jaxbElement), writer); //$NON-NLS-1$
		} catch (JAXBException e) {
			// return default
		}
		return writer.toString();
	}

	/**
	 * Validates the XML against the internal XSD for JAXB resource managers.
	 * 
	 * @param url
	 *            of the configuration xml.
	 * @throws SAXException
	 *             if invalid
	 * @throws IOException
	 * @throws URISyntaxException
	 */
	public static void validate(URL url) throws SAXException, IOException, URISyntaxException {
		validate(new StreamSource(url.openStream()));
	}

	/**
	 * Adds the attributes. If the attribute value is <code>null</code>, overwrites it with the default.
	 * 
	 * @param env
	 *            the active instance of the resource manager environment map
	 * @param control
	 *            JAXB data subtree for control part of resource manager
	 */
	private static void addAttributes(Map<String, AttributeType> env, ControlType control) {
		if (control == null) {
			return;
		}
		List<AttributeType> jobAttributes = control.getAttribute();
		for (AttributeType jobAttribute : jobAttributes) {
			env.put(jobAttribute.getName(), jobAttribute);
			if (jobAttribute.getValue() == null) {
				jobAttribute.setValue(jobAttribute.getDefault());
			}
		}
	}

	/**
	 * Uses the ResourceManagerData context.
	 * 
	 * @return static singleton
	 * @throws JAXBException
	 */
	private synchronized static Unmarshaller getUnmarshaller() throws JAXBException {
		if (unmarshaller == null) {
			JAXBContext jc = JAXBContext
					.newInstance(JAXBCoreConstants.JAXB_CONTEXT, JAXBInitializationUtils.class.getClassLoader());
			unmarshaller = jc.createUnmarshaller();
		}
		return unmarshaller;
	}

	/**
	 * * Uses the ResourceManagerData schema.
	 * 
	 * @return static singleton
	 * @throws IOException
	 * @throws SAXException
	 */
	private synchronized static Validator getValidator() throws IOException, SAXException {
		if (validator == null) {
			URL xsd = JAXBCorePlugin.getResource(JAXBCoreConstants.RM_XSD);
			SchemaFactory factory = SchemaFactory.newInstance(JAXBCoreConstants.XMLSchema);
			Schema schema = factory.newSchema(xsd);
			validator = schema.newValidator();
		}
		return validator;
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
		sb.append(Messages.PublicId + e.getPublicId()).append(JAXBCoreConstants.LINE_SEP);
		sb.append(Messages.SystemId + e.getSystemId()).append(JAXBCoreConstants.LINE_SEP);
		sb.append(Messages.LineNumber + e.getLineNumber()).append(JAXBCoreConstants.LINE_SEP);
		sb.append(Messages.ColumnNumber + e.getColumnNumber()).append(JAXBCoreConstants.LINE_SEP);
		sb.append(Messages.Message + e.getMessage()).append(JAXBCoreConstants.LINE_SEP);
		return sb.toString();
	}

	/**
	 * First validates the xml, then gets the JAXB context and calls the JAXB unmarshaller from it.
	 * 
	 * @param xml
	 *            of the configuration file.
	 * @return the constructed data tree
	 * @throws JAXBException
	 *             problem encountered during unmarshaling
	 * @throws IOException
	 * @throws SAXException
	 *             validation error
	 * @throws URISyntaxException
	 */
	private static ResourceManagerData unmarshalResourceManagerData(String xml) throws JAXBException, IOException, SAXException,
			URISyntaxException {
		Source source = new StreamSource(new StringReader(xml));
		validate(source);
		source = new StreamSource(new StringReader(xml));
		JAXBElement<?> o = (JAXBElement<?>) getUnmarshaller().unmarshal(source);
		ResourceManagerData rmdata = (ResourceManagerData) o.getValue();
		if (rmdata != null) {
			if (rmdata.getControlData() == null) {
				rmdata.setControlData(new ControlType());
			}
			if (rmdata.getMonitorData() == null) {
				rmdata.setMonitorData(new MonitorType());
			}
		}
		return rmdata;
	}

	/**
	 * Validates the XML against the internal XSD for JAXB resource managers.
	 * 
	 * @param source
	 *            of the configuration xml.
	 * @throws SAXException
	 *             if invalid
	 * @throws IOException
	 * @throws URISyntaxException
	 */
	private static void validate(Source source) throws SAXException, IOException, URISyntaxException {
		try {
			getValidator().validate(source);
		} catch (SAXParseException sax) {
			JAXBCorePlugin.log(printInfo(sax));
			throw sax;
		}
	}

	private JAXBInitializationUtils() {
	}
}
