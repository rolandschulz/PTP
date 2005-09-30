/**********************************************************************
 * Copyright (c) 2005 IBM Corporation.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/

package org.eclipse.ptp.mpi.core.help;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

/**
 * @author xue
 */
public class MPIDocXMLParser {
	
	public static void main(String [] args)
	{
		File file = new File(args[0]);
		try {
			List functions = parseDOM(new FileInputStream(file));
			System.out.println("num of functions gathered: "+functions.size());
            System.out.println("first function summary:");
            System.out.println((FunctionSummaryImpl)(functions.iterator().next()));
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		}
	}
	
	
	public static List /* of FunctionSummary */parseDOM(InputStream xmlIn) {
		List mpiFuncList = new ArrayList();
		DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
		Document document = null;
		try {
			DocumentBuilder builder = factory.newDocumentBuilder();
			if (xmlIn != null)
				document = builder.parse(xmlIn);
		} catch (ParserConfigurationException e) {
			e.printStackTrace();
		} catch (SAXException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}

		if (document != null) {
			NodeList functionList = document.getElementsByTagName("function");
			for (int i = 0; i < functionList.getLength(); i++) {
				Node func = functionList.item(i);
				NodeList child = func.getChildNodes();
				String cname = null;
				String desc = null;
				FunctionPrototypeSummaryImpl prototypeSummary = null;
				for (int j = 0; j < child.getLength(); j++) {
					Node sub = child.item(j);
					if (sub.getNodeName().equals("cname")) {
//						cname = sub.getTextContent(); // java 5 
                        cname = sub.getFirstChild().getNodeValue();
					} else if (sub.getNodeName().equals("description")) {
//						desc = sub.getTextContent(); // java 5
                        desc = sub.getFirstChild().getNodeValue();
					} else if (sub.getNodeName().equals("prototype")) {
						NodeList protoSub = sub.getChildNodes();
                        // java 5
//						prototypeSummary = new FunctionPrototypeSummary(
//								protoSub.item(3).getTextContent(), protoSub
//										.item(1).getTextContent(), protoSub
//										.item(5).getTextContent());
                        prototypeSummary = new FunctionPrototypeSummaryImpl(
                              protoSub.item(3).getFirstChild().getNodeValue(), protoSub
                                      .item(1).getFirstChild().getNodeValue(), protoSub
                                      .item(5).getFirstChild().getNodeValue());
					}
				}
				FunctionSummaryImpl functionSummary = new FunctionSummaryImpl(cname,
						"", desc, prototypeSummary, null);
				mpiFuncList.add(functionSummary);
			}
		}
		return mpiFuncList;
	}
}
