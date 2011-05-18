/*******************************************************************************
 * Copyright (c) 2010 Dieter Krachtus and The University of Tennessee,
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Dieter Krachtus (dieter.krachtus@gmail.com) and Roland Schulz - initial API and implementation
 *    Benjamin Lindner (ben@benlabs.net) - Attribute Definitions and Mapping (bug 316671)
 
 *******************************************************************************/

package org.eclipse.ptp.rm.proxy.core.parser;

import java.beans.IntrospectionException;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.io.StringReader;
import java.lang.reflect.InvocationTargetException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.regex.Pattern;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.eclipse.ptp.core.attributes.IAttributeDefinition;
import org.eclipse.ptp.rm.proxy.core.element.IElement;
import org.eclipse.ptp.rm.proxy.core.element.IElement.UnknownValueExecption;
import org.eclipse.ptp.rm.proxy.core.element.ProxyModelElement;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

/**
 * XML file reader.
 */
public class XMLReader implements IParser {

	/**
	 * This class ensures that the InputStream has only valid XML unicode
	 * characters as specified by the XML 1.0 standard. For reference, please
	 * see <a href="http://www.w3.org/TR/2000/REC-xml-20001006#NT-Char">the
	 * standard</a>. This method will return an empty String if the input is
	 * null or empty.
	 * 
	 * From: http://cse-mjmcl.cse.bris.ac.uk/blog/2007/02/14/1171465494443.html
	 * Not OK for UTF longer than 2 bytes
	 * 
	 * If the XML is empty it provides "<D></D>"
	 * 
	 */
	class FixInValidXMLReader extends InputStreamReader {
		boolean isEmpty = false;
		int totalBytesRead = 0;
		Reader emptyInput = null;

		public FixInValidXMLReader(InputStream in) {
			super(in);
		}

		@Override
		public int read() throws IOException {
			char cbuf[] = new char[1];
			int ret;

			while ((ret = read(cbuf, 0, 1)) != -1) {
			}

			if (ret > 0) {
				return cbuf[0];
			} else {
				return -1;
			}
		}

		@Override
		public int read(char cbuf[], int offset, int length) throws IOException {
			if (emptyInput != null) {
				return emptyInput.read(cbuf, offset, length);
			}
			int ret = super.read(cbuf, offset, length);
			int skip = 0;
			if (totalBytesRead == 0 && ret < 1) { /*
												 * Send non-empty valid XML if
												 * input stream is empty
												 */
				emptyInput = new StringReader("<D></D>"); //$NON-NLS-1$
				return emptyInput.read(cbuf, offset, length);
			}
			for (int i = offset; i < offset + ret; i++) {
				char current = cbuf[i];
				if (!((current == 0x9) || (current == 0xA) || (current == 0xD) || ((current >= 0x20) && (current <= 0xD7FF))
						|| ((current >= 0xE000) && (current <= 0xFFFD)) || ((current >= 0x10000) && (current <= 0x10FFFF)))) {
					skip++;
					continue;
				}
				if (skip > 0) {
					cbuf[i - skip] = cbuf[i];
				}
			}
			ret -= skip;
			totalBytesRead += ret;
			return ret;
		}

	}

	/** The DEBUG. */
	private static boolean DEBUG = false;

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.eclipse.ptp.rm.proxy.core.parser.IParser#parse(org.eclipse.ptp.rm
	 * .proxy.core.attributes.AttributeDefinition, java.io.InputStream)
	 */
	/**
	 * @since 2.0
	 */
	public Set<IElement> parse(List<String> requiredAttributeKeys, List<IAttributeDefinition<?, ?, ?>> AttributeDefinitions,
			List<List<Object>> ParserKeyMap, List<List<Object>> ParserValueMap, InputStream in, String keyID, String parentkeyID)
			throws SAXException, IOException, ParserConfigurationException, UnknownValueExecption {
		// public <T extends IElement> Set<T> parse(Class<IElement> pojoClazz,
		// InputStream in) {
		Set<IElement> elementList = null;
		NodeList xmlNodes = null;
		xmlNodes = getXMLChildren(in);
		elementList = new HashSet<IElement>(xmlNodes.getLength());
		for (int i = 0; i < xmlNodes.getLength(); i++) {
			Node node = xmlNodes.item(i);
			if (node.getNodeType() == Node.ELEMENT_NODE) {
				Map<String, String> input = populateInput(node, null);
				IElement element = populateElement(requiredAttributeKeys, AttributeDefinitions, ParserKeyMap, ParserValueMap,
						input, keyID, parentkeyID);
				if (DEBUG) {
					System.out.println(element);
					System.out.println();
				}
				elementList.add(element);
			}
		}

		if (DEBUG) {
			System.out.println(elementList.size());
		}
		return elementList;
	}

	protected Map<String, String> populateInput(Node node, Map<String, String> input) {
		if (input == null) {
			input = new HashMap<String, String>();
		}

		// if (node.getNodeType() == Node.ELEMENT_NODE)
		// System.out.println(node.getNodeName());

		NodeList childNodes = node.getChildNodes();

		for (int i = 0; i < childNodes.getLength(); i++) {
			Node childNode = childNodes.item(i);

			if (childNode.getNodeType() == Node.ELEMENT_NODE) {
				if (childNode.getChildNodes().getLength() > 1) {
					populateInput(childNode, input);
				} else {
					input.put(childNode.getNodeName().toLowerCase(), childNode.getTextContent());
				}
			}

			// if (childNode.getNodeType() == Node.ELEMENT_NODE)
			// System.out.println("\t" + childNode.getNodeName().toLowerCase() +
			// " -> " + childNode.getTextContent());
		}

		return input;
	}

	private NodeList getXMLChildren(InputStream in) throws SAXException, IOException, ParserConfigurationException {
		DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
		DocumentBuilder builder = factory.newDocumentBuilder();

		Document doc = builder.parse(new InputSource(new FixInValidXMLReader(in)));
		// Document doc = builder.parse(in);
		Element root = doc.getDocumentElement();
		root.normalize();

		return root.getChildNodes();
	}

	private IElement populateElement(List<String> requiredAttributeKeys, List<IAttributeDefinition<?, ?, ?>> AttributeDefinitions,
			List<List<Object>> ParserKeyMap, List<List<Object>> ParserValueMap, Map<String, String> input, String keyID,
			String parentkeyID) throws UnknownValueExecption {
		IElement element = new ProxyModelElement(requiredAttributeKeys, AttributeDefinitions, keyID, parentkeyID);

		for (Entry<String, String> entry : input.entrySet()) {
			// save keys as lower case! (by convention)
			// then when matching they will be matched against the lower case of
			// the corresponding ParserKeyMap entry
			String newkey = entry.getKey();
			String newvalue = entry.getValue();
			String k = newkey;
			String v = newvalue;
			boolean keymatched = false;

			for (List<Object> ke : ParserKeyMap) {
				Pattern kp = (Pattern) ke.get(0);
				if (kp.matcher(k).matches()) {
					newkey = (String) ke.get(1);
					keymatched = true;
					break;
				}
			}

			for (List<Object> ve : ParserValueMap) {
				Pattern kp = (Pattern) ve.get(0);
				Pattern vp = (Pattern) ve.get(1);

				if (kp.matcher(k).matches() && vp.matcher(v).matches()) {
					newvalue = (String) ve.get(2);
					break;
				}
			}

			// System.err.println("trying to set: "+ newkey + " :: " +
			// newvalue);
			if (keymatched) {
				element.setAttribute(newkey, newvalue, false);
			}
		}

		return element;
	}

	/**
	 * Testing method
	 * 
	 * @param argv
	 *            the arguments
	 */
	public static void main(String argv[]) throws IntrospectionException, IllegalAccessException, InvocationTargetException,
			InstantiationException, FileNotFoundException {
		DEBUG = true;
		// parseXML(ModelQstatJob.class, new File("qstat_valid.xml"));
		// new XMLReader().parse(ModelNode.class, new FileInputStream(new
		// File("pbsnodes.helics.xml")));
	}

}