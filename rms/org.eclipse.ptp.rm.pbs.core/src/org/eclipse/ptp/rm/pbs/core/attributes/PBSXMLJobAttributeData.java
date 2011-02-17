/*******************************************************************************
 * Copyright (c) 2010 University of Illinois All rights reserved. This program
 * and the accompanying materials are made available under the terms of the
 * Eclipse Public License v1.0 which accompanies this distribution, and is
 * available at http://www.eclipse.org/legal/epl-v10.html 
 * 	
 * Contributors: 
 * 	Albert L. Rossi - design and implementation
 ******************************************************************************/
package org.eclipse.ptp.rm.pbs.core.attributes;

import java.io.InputStream;
import java.io.OutputStream;
import java.text.ParseException;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;
import java.util.TreeMap;

import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import org.eclipse.ptp.core.attributes.BooleanAttributeDefinition;
import org.eclipse.ptp.core.attributes.DateAttributeDefinition;
import org.eclipse.ptp.core.attributes.DoubleAttributeDefinition;
import org.eclipse.ptp.core.attributes.IAttributeDefinition;
import org.eclipse.ptp.core.attributes.IllegalValueException;
import org.eclipse.ptp.core.attributes.IntegerAttributeDefinition;
import org.eclipse.ptp.core.attributes.StringAttributeDefinition;
import org.eclipse.ptp.rm.pbs.core.IPBSNonNLSConstants;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import com.ibm.icu.text.DateFormat;

/**
 * Abstraction of Job Attribute XML. Serializes and deserializes; maintains
 * mappings of definitions, string constraints, qsub flags and tooltips. <br>
 * 
 * This implementation of IPBSJobAttributeData is currently used for both
 * initial configuration and storage; eventually initialization will take place
 * solely via the model definition.
 * 
 * @author arossi
 * @since 5.0
 */
public class PBSXMLJobAttributeData implements IPBSJobAttributeData, IPBSNonNLSConstants {

	protected Map<String, IAttributeDefinition<?, ?, ?>> definitions;
	protected Map<String, String[]> constrained;
	protected Properties flags;
	protected Properties tooltips;
	protected Map<String, String> minSet;

	public void deserialize(InputStream inputStream) throws Throwable {
		clearAll();
		DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
		factory.setValidating(false);
		Document doc = factory.newDocumentBuilder().parse(inputStream);
		if (doc != null)
			parseDocument(doc);
	}

	public Map<String, IAttributeDefinition<?, ?, ?>> getAttributeDefinitionMap() throws Throwable {
		return definitions;
	}

	public Map<String, String[]> getConstrained() throws Throwable {
		return constrained;
	}

	public Map<String, String> getMinSet() throws Throwable {
		if (minSet == null)
			minSet = new HashMap<String, String>();
		return minSet;
	}

	public Properties getPBSQsubFlags() throws Throwable {
		return flags;
	}

	public Properties getToolTips() throws Throwable {
		return tooltips;
	}

	public void serialize(OutputStream outputStream) throws Throwable {
		DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
		factory.setValidating(false);
		Document doc = factory.newDocumentBuilder().newDocument();
		Element top = doc.createElement(ATTRIBUTES);
		for (String name : definitions.keySet())
			appendJobAttribute(name, doc, top);
		doc.appendChild(top);
		serialize(doc, outputStream);
	}

	private void appendAttributeDefinition(BooleanAttributeDefinition definition, Document doc, Element parent) {
		Element child = doc.createElement(DEFINITION);
		setCommonAttributes(definition.getId(), BOOLEAN, definition.getDisplay(), child);
		appendDescription(definition, doc, child);
		parent.appendChild(child);
	}

	private void appendAttributeDefinition(DateAttributeDefinition definition, Document doc, Element parent) {
		Element child = doc.createElement(DEFINITION);
		setCommonAttributes(definition.getId(), DATE, definition.getDisplay(), child);
		appendDescription(definition, doc, child);
		DateFormat df = definition.getDateFormat();
		if (df != null) {
			Element format = doc.createElement(DATE);
			format.setTextContent(df.toString());
			child.appendChild(format);
		}
		appendMinMax(definition.getMinDate(), definition.getMaxDate(), doc, child);
		parent.appendChild(child);
	}

	private void appendAttributeDefinition(DoubleAttributeDefinition definition, Document doc, Element parent) {
		Element child = doc.createElement(DEFINITION);
		setCommonAttributes(definition.getId(), DOUBLE, definition.getDisplay(), child);
		appendDescription(definition, doc, child);
		appendMinMax(definition.getMinValue(), definition.getMaxValue(), doc, child);
		parent.appendChild(child);
	}

	private void appendAttributeDefinition(IntegerAttributeDefinition definition, Document doc, Element parent) {
		Element child = doc.createElement(DEFINITION);
		setCommonAttributes(definition.getId(), INTEGER, definition.getDisplay(), child);
		appendDescription(definition, doc, child);
		appendMinMax(definition.getMinValue(), definition.getMaxValue(), doc, child);
		parent.appendChild(child);
	}

	private void appendAttributeDefinition(StringAttributeDefinition definition, Document doc, Element parent) {
		Element child = doc.createElement(DEFINITION);
		String name = definition.getName();
		String[] choices = constrained.get(name);
		Element choice = null;
		String type = STRING;
		if (choices != null && choices.length > 0) {
			type = CHOICE;
			choice = doc.createElement(CHOICE);
			StringBuffer sb = new StringBuffer(choices[0]);
			for (int i = 1; i < choices.length; i++)
				sb.append(CM).append(choices[i]);
			choice.setTextContent(sb.toString());
		}
		setCommonAttributes(definition.getId(), type, definition.getDisplay(), child);
		appendDescription(definition, doc, child);
		parent.appendChild(child);
		if (choice != null)
			parent.appendChild(choice);
	}

	private void appendDescription(IAttributeDefinition<?, ?, ?> definition, Document doc, Element parent) {
		String d = definition.getDescription();
		if (d != null) {
			Element description = doc.createElement(DESCRIPTION);
			description.setTextContent(d);
			parent.appendChild(description);
		}
	}

	private void appendJobAttribute(String name, Document doc, Element parent) throws Throwable {
		String tooltip = tooltips.getProperty(name);
		if (TAG_INTERNAL.equals(tooltip))
			return;

		Element jobAttribute = doc.createElement(ATTRIBUTE);
		jobAttribute.setAttribute(NAME, name);
		if (minSet.containsKey(name))
			jobAttribute.setAttribute(MINSET, TRUE);
		IAttributeDefinition<?, ?, ?> attribute = definitions.get(name);
		if (attribute instanceof BooleanAttributeDefinition)
			appendAttributeDefinition((BooleanAttributeDefinition) attribute, doc, jobAttribute);
		else if (attribute instanceof DateAttributeDefinition)
			appendAttributeDefinition((DateAttributeDefinition) attribute, doc, jobAttribute);
		else if (attribute instanceof DoubleAttributeDefinition)
			appendAttributeDefinition((DoubleAttributeDefinition) attribute, doc, jobAttribute);
		else if (attribute instanceof IntegerAttributeDefinition)
			appendAttributeDefinition((IntegerAttributeDefinition) attribute, doc, jobAttribute);
		else if (attribute instanceof StringAttributeDefinition)
			appendAttributeDefinition((StringAttributeDefinition) attribute, doc, jobAttribute);
		appendQsubFlag(name, doc, jobAttribute);
		appendTooltip(tooltip, doc, jobAttribute);
		parent.appendChild(jobAttribute);
	}

	private void appendMinMax(Object min, Object max, Document doc, Element child) {
		if (min != null) {
			Element e = doc.createElement(MIN);
			e.setTextContent(min.toString());
			child.appendChild(e);
		}
		if (max != null) {
			Element e = doc.createElement(MAX);
			e.setTextContent(max.toString());
			child.appendChild(e);
		}
	}

	private void appendQsubFlag(String name, Document doc, Element parent) {
		String flag = flags.getProperty(name);
		Element e = doc.createElement(FLAG);
		e.setTextContent(flag);
		parent.appendChild(e);
	}

	private void appendTooltip(String tooltip, Document doc, Element parent) throws IllegalValueException {
		Element e = doc.createElement(TOOLTIP);
		e.setTextContent(tooltip);
		parent.appendChild(e);
	}

	private void clearAll() {
		if (definitions == null)
			definitions = new TreeMap<String, IAttributeDefinition<?, ?, ?>>();
		else
			definitions.clear();
		if (flags == null)
			flags = new Properties();
		else
			flags.clear();
		if (tooltips == null)
			tooltips = new Properties();
		else
			tooltips.clear();
		if (constrained == null)
			constrained = new HashMap<String, String[]>();
		else
			constrained.clear();
		if (minSet == null)
			minSet = new HashMap<String, String>();
		else
			minSet.clear();
	}

	private Element getSingletonElement(Element element, String name) {
		NodeList nodelist = element.getElementsByTagName(name);
		if (nodelist != null) {
			Node node = nodelist.item(0);
			if (node != null)
				if (node.getNodeType() == Node.ELEMENT_NODE)
					return (Element) node;
		}
		return null;
	}

	private void handleAttributeDefinition(String name, Element child) throws ParseException, IllegalValueException {
		String description = null;
		Element e = getSingletonElement(child, DESCRIPTION);
		if (e != null)
			description = e.getTextContent().trim();
		String id = child.getAttribute(ID);
		String type = child.getAttribute(TYPE);
		String displayStr = child.getAttribute(DISPLAY);
		boolean display = displayStr == null ? false : Boolean.valueOf(displayStr).booleanValue();

		String defaultValue = null;
		e = getSingletonElement(child, DEFAULT);
		if (e != null)
			defaultValue = e.getTextContent();

		if (BOOLEAN.equals(type))
			handleBooleanAttribute(id, name, display, description, defaultValue);
		else if (STRING.equals(type))
			handleStringAttribute(id, name, display, description, defaultValue);
		else if (CHOICE.equals(type)) {
			e = getSingletonElement((Element) child.getParentNode(), CHOICE);
			if (e != null)
				constrained.put(name, e.getTextContent().split(CM));
			handleStringAttribute(id, name, display, description, defaultValue);
		} else {
			String min = null;
			String max = null;
			e = getSingletonElement(child, MIN);
			if (e != null)
				min = e.getTextContent().trim();
			e = getSingletonElement(child, MAX);
			if (e != null)
				max = e.getTextContent().trim();

			if (DATE.equals(type)) {
				String format = null;
				e = getSingletonElement(child, FORMAT);
				if (e != null)
					format = e.getTextContent().trim();
				handleDateAttribute(id, name, display, description, defaultValue, format, min, max);
			} else if (DOUBLE.equals(type))
				handleDoubleAttribute(id, name, display, description, defaultValue, min, max);
			else if (INTEGER.equals(type))
				handleIntegerAttribute(id, name, display, description, defaultValue, min, max);
		}
	}

	private void handleBooleanAttribute(String id2, String name2, boolean display, String description2, String defaultValue) {
		boolean dval = defaultValue == null ? false : Boolean.valueOf(defaultValue).booleanValue();
		definitions.put(name2, new BooleanAttributeDefinition(id2, name2, description2, display, dval));
	}

	private void handleDateAttribute(String id2, String name2, boolean display, String description2, String defaultValue,
			String format2, String min2, String max2) throws ParseException, IllegalValueException {
		DateFormat df = DateFormat.getPatternInstance(format2);
		Date dval = defaultValue == null ? null : df.parse(defaultValue);
		Date min = min2 == null ? null : df.parse(min2);
		Date max = max2 == null ? null : df.parse(max2);
		if (min != null && max != null) {
			if (dval == null)
				dval = min;
			definitions.put(name2, new DateAttributeDefinition(id2, name2, description2, display, dval, df, min, max));
		} else
			definitions.put(name2, new DateAttributeDefinition(id2, name2, description2, display, dval, df));
	}

	private void handleDoubleAttribute(String id2, String name2, boolean display, String description2, String defaultValue,
			String min2, String max2) throws IllegalValueException {
		Double dval = defaultValue == null ? null : new Double(defaultValue);
		Double min = min2 == null ? null : new Double(min2);
		Double max = max2 == null ? null : new Double(max2);
		if (min != null) {
			if (max == null)
				max = Double.MAX_VALUE;
		} else if (max != null)
			if (min == null)
				max = Double.MIN_VALUE;
		if (min != null && max != null) {
			if (dval == null)
				dval = min;
			definitions.put(name2, new DoubleAttributeDefinition(id2, name2, description2, display, dval, min, max));
		} else
			definitions.put(name2, new DoubleAttributeDefinition(id2, name2, description2, display, dval));
	}

	private void handleIntegerAttribute(String id2, String name2, boolean display, String description2, String defaultValue,
			String min2, String max2) throws IllegalValueException {
		Integer dval = defaultValue == null ? null : new Integer(defaultValue);
		Integer min = min2 == null ? null : new Integer(min2);
		Integer max = max2 == null ? null : new Integer(max2);
		if (min != null) {
			if (max == null)
				max = Integer.MAX_VALUE;
		} else if (max != null)
			if (min == null)
				max = Integer.MIN_VALUE;
		if (min != null && max != null) {
			if (dval == null)
				dval = min;
			definitions.put(name2, new IntegerAttributeDefinition(id2, name2, description2, display, dval, min, max));
		} else
			definitions.put(name2, new IntegerAttributeDefinition(id2, name2, description2, display, dval));
	}

	private void handleJobAttributeDefinitionElement(Element element) throws ParseException, IllegalValueException {
		String name = element.getAttribute(NAME);
		String isMinSet = element.getAttribute(MINSET);
		if (TRUE.equals(isMinSet))
			minSet.put(name, null);
		Element child = getSingletonElement(element, TOOLTIP);
		if (child != null)
			try {
				handleTooltip(name, child);
			} catch (IllegalValueException ive) {
				return;
			}
		child = getSingletonElement(element, DEFINITION);
		if (child != null)
			handleAttributeDefinition(name, child);
		child = getSingletonElement(element, FLAG);
		if (child != null)
			handleQsubFlag(name, child);

	}

	private void handleQsubFlag(String name, Element child) {
		flags.put(name, child.getTextContent());
	}

	private void handleStringAttribute(String id2, String name2, boolean display, String description2, String defaultValue) {
		definitions.put(name2, new StringAttributeDefinition(id2, name2, description2, display, defaultValue));
	}

	private void handleTooltip(String name, Element child) throws IllegalValueException {
		if (TAG_INTERNAL.equals(child.getTextContent()))
			throw new IllegalValueException(TAG_INTERNAL);
		tooltips.put(name, child.getTextContent());
	}

	private void parseDocument(Document doc) throws ParseException, IllegalValueException {
		NodeList attributeDefinitions = doc.getElementsByTagName(ATTRIBUTE);
		for (int i = 0; i < attributeDefinitions.getLength(); i++) {
			Node node = attributeDefinitions.item(i);
			if (node.getNodeType() == Node.ELEMENT_NODE) {
				Element e = (Element) node;
				handleJobAttributeDefinitionElement(e);
			}
		}
	}

	private void serialize(Document doc, OutputStream out) throws Exception {
		TransformerFactory tfactory = TransformerFactory.newInstance();
		Transformer serializer;
		try {
			serializer = tfactory.newTransformer();
			// Setup indenting to "pretty print"
			serializer.setOutputProperty(OutputKeys.INDENT, YES);
			serializer.setOutputProperty(INDENTATION, INDENT_SPACES);
			serializer.transform(new DOMSource(doc), new StreamResult(out));
		} catch (TransformerException e) {
			// this is fatal, just dump the stack and throw a runtime exception
			e.printStackTrace();
			throw new RuntimeException(e);
		}
	}

	private void setCommonAttributes(String id, String type, boolean display, Element child) {
		child.setAttribute(ID, id);
		child.setAttribute(TYPE, type);
		child.setAttribute(DISPLAY, ZEROSTR + display);
	}
}
