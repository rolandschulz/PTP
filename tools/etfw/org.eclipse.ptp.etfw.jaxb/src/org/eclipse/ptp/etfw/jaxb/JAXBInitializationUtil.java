/*******************************************************************************
 * Copyright (c) 2012 University of Illinois and others.  All rights reserved.
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html 
 * 
 * Contributors: 
 *     Chris Navarro (Illinois/NCSA) - Design and implementation
 ******************************************************************************/
package org.eclipse.ptp.etfw.jaxb;

import java.io.EOFException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.StringReader;
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

import org.eclipse.ptp.etfw.jaxb.data.ControlDataType;
import org.eclipse.ptp.etfw.jaxb.data.EtfwToolProcessType;
import org.eclipse.ptp.etfw.jaxb.osgi.Activator;
import org.eclipse.ptp.internal.rm.jaxb.core.JAXBCoreConstants;
import org.eclipse.ptp.rm.jaxb.core.IVariableMap;
import org.eclipse.ptp.rm.jaxb.core.data.AttributeType;
import org.xml.sax.SAXException;
import org.xml.sax.SAXParseException;

/**
 * Validates and unmarshalls ETFw tool xml files using JAXB parser
 * 
 * @author Chris Navarro
 * 
 */
public class JAXBInitializationUtil {

	private static Unmarshaller unmarshaller;
	private static Validator validator;

	public static String getETFWConfigurationXML(URL url) throws IOException {
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

	public static EtfwToolProcessType initializeEtfwToolProcessType(String xml) throws IOException, SAXException,
			URISyntaxException,
			JAXBException {
		return unmarshallEtfwToolProcessType(xml);
	}

	private static EtfwToolProcessType unmarshallEtfwToolProcessType(String xml) throws JAXBException, IOException, SAXException,
			URISyntaxException {
		Source source = new StreamSource(new StringReader(xml));
		validate(source);
		source = new StreamSource(new StringReader(xml));
		JAXBElement<?> o = (JAXBElement<?>) getUnmarshaller().unmarshal(source);
		EtfwToolProcessType toolData = (EtfwToolProcessType) o.getValue();

		return toolData;
	}

	private static void validate(Source source) throws SAXException, IOException, URISyntaxException {
		try {
			getValidator().validate(source);
		} catch (SAXParseException e) {
			throw e;
		}

	}

	private synchronized static Validator getValidator() throws IOException, SAXException {
		if (validator == null) {
			URL xsd = Activator.getResource(ETFWCoreConstants.ETFW_XSD);

			SchemaFactory factory = SchemaFactory.newInstance(JAXBCoreConstants.XMLSchema);

			Schema schema = factory.newSchema(new StreamSource[] { new StreamSource(xsd.openStream()) });// .newSchema(xsd);

			validator = schema.newValidator();
		}
		return validator;
	}

	private static Unmarshaller getUnmarshaller() throws JAXBException {
		if (unmarshaller == null) {
			JAXBContext jc = JAXBContext.newInstance(ETFWCoreConstants.JAXB_CONTEXT, JAXBInitializationUtil.class.getClassLoader());
			unmarshaller = jc.createUnmarshaller();
		}
		return unmarshaller;
	}

	public static void initializeMap(EtfwToolProcessType etfwTool, IVariableMap instance) {
		ControlDataType control = etfwTool.getControlData();
		instance.clear();
		Map<String, AttributeType> env = instance.getAttributes();
		addAttributes(env, control);
		instance.setInitialized(true);
	}

	public static void addAttributes(Map<String, AttributeType> env, ControlDataType control) {
		if (control == null) {
			return;
		}

		List<AttributeType> attributes = control.getAttribute();
		for (AttributeType attribute : attributes) {
			env.put(attribute.getName(), attribute);
			if (attribute.getValue() == null) {
				attribute.setValue(attribute.getDefault());
			}
		}
	}
}
