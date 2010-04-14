/*******************************************************************************
 * Copyright (c) 2010 Dieter Krachtus and The University of Tennessee,
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Dieter Krachtus (dieter.krachtus@gmail.com) and Roland Schulz - initial API and implementation

*******************************************************************************/

package org.eclipse.ptp.rm.pbs.jproxy.parser;

import java.beans.IntrospectionException;
import java.beans.Introspector;
import java.beans.PropertyDescriptor;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.Serializable;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.eclipse.ptp.proxy.jproxy.IParser;
import org.eclipse.ptp.proxy.jproxy.attributes.AttributeDefinition;
import org.eclipse.ptp.proxy.jproxy.element.IElement;
import org.eclipse.ptp.proxy.jproxy.element.IElement.UnknownValueExecption;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

public class UnmarshallingUtil implements IParser {
	 
	private static boolean DEBUG = false;

	public static void main (String argv []) throws IntrospectionException, IllegalAccessException, InvocationTargetException, InstantiationException, FileNotFoundException{
		DEBUG = true;
//		parseXML(ModelQstatJob.class, new File("qstat_valid.xml"));
//		new UnmarshallingUtil().parse(ModelNode.class, new FileInputStream(new File("pbsnodes.helics.xml")));
	}

	public Set<IElement> parse(AttributeDefinition attrDef, InputStream in) {
//	public <T  extends IElement> Set<T> parse(Class<IElement> pojoClazz, InputStream in) {
		Set<IElement> elementList = null;
		NodeList xmlNodes = null;
		try {
			 xmlNodes = getXMLChildren(in);
		} catch (Exception e) {
//			e.printStackTrace(); //Ignore Premature end of file, TODO: don't ignore other things
			return new HashSet<IElement>();	
		}
		try {
			in.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
		elementList = new HashSet<IElement>(xmlNodes.getLength());
		for (int i = 0; i < xmlNodes.getLength(); i++) {
			Node node = xmlNodes.item(i);
			if (node.getNodeType() == Node.ELEMENT_NODE) {
				try {
					Map<String, String> input = populateInput(node, null);
					IElement bean = populateElement(attrDef, input);
					if (DEBUG) {
						System.out.println(bean);
						System.out.println();
					}
					elementList.add(bean);
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		}

		if (DEBUG) {
			System.out.println(elementList.size());
		}
		return elementList;
	}
	
	
	private NodeList getXMLChildren(InputStream in) throws SAXException, IOException, ParserConfigurationException {
		DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
		DocumentBuilder builder = factory.newDocumentBuilder();
		Document doc = builder.parse(in);

		Element root = doc.getDocumentElement();
		root.normalize();

		return root.getChildNodes();
	}
	

	private Map<String, String> populateInput(Node node, Map<String, String> input) throws IntrospectionException, IllegalAccessException, InvocationTargetException, InstantiationException {
		if (input == null) input = new HashMap<String, String>();
		
//		if (node.getNodeType() == Node.ELEMENT_NODE) System.out.println(node.getNodeName());

		NodeList childNodes = node.getChildNodes();
		
		//TODO move this PBS specific hack out of UnmarshallingUtil
		// Hack to recover from not well-formed XML (old qstat versions generate not well-formed XML) 
		Node item = childNodes.item(0);
		String textContent = item.getTextContent().trim();
		// true when qstat XML output is not well-formed
		if (textContent.length() > 0 && item.getNodeType() == Node.TEXT_NODE) {
//			System.out.println("Invalid XML: "+textContent);
			input.put("job_id", textContent);
		}
		
		for (int i = 0; i < childNodes.getLength(); i++) {
			Node childNode = childNodes.item(i);
			
			if (childNode.getNodeType() == Node.ELEMENT_NODE) {
				if (childNode.getChildNodes().getLength() > 1) {
					populateInput(childNode, input);
				} else {
					input.put(childNode.getNodeName().toLowerCase(), childNode.getTextContent());
				}
			}
			
//			if (childNode.getNodeType() == Node.ELEMENT_NODE) System.out.println("\t" + childNode.getNodeName().toLowerCase() + " -> " + childNode.getTextContent());
		}
		
		return input;
	}
	
	
	private  IElement populateElement(AttributeDefinition attrDef, Map<String, String> input) throws UnknownValueExecption {
		IElement element = attrDef.createElement();

//		PropertyDescriptor[] properties = Introspector.getBeanInfo(attrDef).getPropertyDescriptors();
//		for (PropertyDescriptor property : properties) {
		for (String attr : attrDef.getRequiredAttributes()) {
			element.setAttribute(attr, input.get(attr));
//			String name = property.getName();
//			Method writeAccess = property.getWriteMethod();
//			if (writeAccess != null && !Modifier.isStatic(writeAccess.getModifiers())) {
//				System.out.println(name + " <- " + input.get(name));
//				writeAccess.invoke(pojo, new Object[]{input.get(name)});
//			}
		}
		return element;
	}


}