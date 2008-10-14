/*******************************************************************************
 * Copyright (c) 2008 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.ptp.internal.rdt.ui.wizards.settings;

import java.util.ArrayList;
import java.util.List;

import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.w3c.dom.Text;

class XMLUtils {

	private XMLUtils() {}
	
	
	public static List<Element> extractChildElements(Element node, String childElementName) throws SettingsImportExportException {
		List<Element> extracted = new ArrayList<Element>();
		
		NodeList children = node.getChildNodes();
		for(int i = 0; i < children.getLength(); i++) {
			Node child = children.item(i);
			switch(child.getNodeType()) {
			case Node.ELEMENT_NODE:
				Element element = (Element)child;
				if(element.getTagName().equals(childElementName)) {
					extracted.add(element);
				}
				else
					throw new SettingsImportExportException("Unknown tag: " + element.getTagName()); //$NON-NLS-1$
				break;
			case Node.TEXT_NODE:
				Text text = (Text)child;
				if(isWhitespace(text.getData()))
					break;
				throw new SettingsImportExportException("Unknown text: '" + text.getData() + "'"); //$NON-NLS-1$ //$NON-NLS-2$
			default:
				throw new SettingsImportExportException("Unknown node: " + child.getNodeName()); //$NON-NLS-1$
			}
		}
		
		return extracted;
	}
	
	
	public static boolean isWhitespace(String s) {
		if(s == null)
			return false;
		
		for(char c : s.toCharArray()) {
			if(!Character.isWhitespace(c)) {
				return false;
			}
		}
		
		return true;
	}
}
