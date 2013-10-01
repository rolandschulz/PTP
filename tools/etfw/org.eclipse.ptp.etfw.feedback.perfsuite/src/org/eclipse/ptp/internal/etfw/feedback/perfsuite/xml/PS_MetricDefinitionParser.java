/* $Id: PS_MetricDefinitionParser.java,v 1.22 2012/01/13 20:49:17 ruiliu Exp $ */

/*******************************************************************************
 * Copyright (c) 2008-2009, 2011-2012 The Board of Trustees of
 * the University of Illinois.
 * 
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors: 
 * 	   NCSA - initial API and implementation
 *******************************************************************************/

package org.eclipse.ptp.internal.etfw.feedback.perfsuite.xml;

import java.util.List;
import java.util.ArrayList;
import java.util.Map;
import java.util.TreeMap;
import java.util.LinkedHashMap;
import java.util.Properties;
import java.io.PrintStream;
import java.io.File;
import java.io.FileReader;
import java.io.FileOutputStream;
import java.io.FileNotFoundException;
import java.io.IOException;

import org.xml.sax.XMLReader;
import org.xml.sax.Attributes;
import org.xml.sax.InputSource;
import org.xml.sax.helpers.DefaultHandler;
import org.xml.sax.SAXException;
import org.xml.sax.SAXParseException;
import org.xml.sax.ErrorHandler;

import javax.xml.parsers.SAXParserFactory;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.ParserConfigurationException;

import org.eclipse.ptp.internal.etfw.feedback.perfsuite.util.PS_Debug;

/**
 * Class to parse a PerfSuite metrics definition XML document.
 *
 * @author Rui Liu
 */
class PS_MetricDefinitionParser extends DefaultHandler {

    // data members
    private StringBuilder res;
    private Map<String,List<String>> metricDefinitionMap;
    private Map<String,Map<String,String>> metricDescriptionMap;
    private String language;
    private boolean needsResourceFileGeneration = false;

    // temporary values
    private String tmpValue = "";
    private String eventName;
    private List<String> expressionList;
    private Map<String,Properties> messageMap;

    private static final String LANGATT        = "lang";
    private static final String DEFAULT_LANG   = "en_US";

    private String RESOURCE_FILE_PREFIX;

    PS_MetricDefinitionParser (String filename)
	throws FileNotFoundException {
        init (filename);
    }

    PS_MetricDefinitionParser (String filename, boolean needsResGen)
	throws FileNotFoundException {
        needsResourceFileGeneration = needsResGen;
        init (filename);
    }

    void init (String filename)
        throws FileNotFoundException {

	// The metric definition map is changed from TreeMap to LinkedHashMap
	// to preserve the order in which entries are inserted, which is
	// the order they appear in the metrics definition XML file.
	metricDefinitionMap = new LinkedHashMap<String,List<String>>();
	metricDescriptionMap = new LinkedHashMap<String,Map<String,String>>();
	messageMap = new TreeMap<String,Properties>();
        RESOURCE_FILE_PREFIX = strip (filename);

	XMLReader xr = getXMLReader (); // could throw RuntimeException
	xr.setContentHandler(this);
	FileReader r = new FileReader(filename);
        // could throw FileNotFoundException, which is one type of IOException
	try {
	    xr.parse(new InputSource(r));
            // could throw IOException, SAXException
	} catch (IOException e) {
	    throw new RuntimeException (e);
	} catch (SAXException e) {
	    throw new RuntimeException (e);
	}
    }

    /**
     * Strips the leading path elements, then strips out all the parts
     * after the first dot character ".", to be used as the base name
     * of generated resource files.
     */
    private String strip (String filename) {
        int lastSeparatorIndex = filename.lastIndexOf (File.separator);
        // In the case of not finding the File.separator string,
        // thus lastSeparatorIndex = -1:
        // since we use lastSeparatorIndex+1 (which is now 0) later,
        // the value happens to be still correct for use
        // as first argument of substring() method.
        // So no adjustment is needed here.
        int firstDotIndex = filename.indexOf (".", lastSeparatorIndex);
        if (-1 == firstDotIndex) {
            return filename.substring (lastSeparatorIndex+1);
        } else {
            return filename.substring (lastSeparatorIndex+1, firstDotIndex);
        }
    }

    Map<String,List<String>> getDefinitionMap() {
	return metricDefinitionMap;
    }

    Map<String,Map<String,String>> getMetricDescriptionMap() {
	return metricDescriptionMap;
    }

    public String toString() {
	StringBuilder resultString = new StringBuilder();
	// expression string per metric
	resultString.append ("Expressions:\n");
	resultString.append (getExpressionString());
	// description strings (of multiple languages) per metric
	resultString.append ("\nDescriptions:\n");
	for (Map.Entry entry : metricDescriptionMap.entrySet()) {
	    resultString.append (entry.getKey() + ": " + entry.getValue() + "\n");
	}
	return resultString.toString();
    }

    private String getExpressionString() {
	StringBuilder resultString = new StringBuilder();
	for (Map.Entry<String,List<String>> entry :
                 metricDefinitionMap.entrySet()) {
	    resultString.append (entry.getKey());
	    resultString.append (":");
	    List<String> expressionL = entry.getValue();
	    for (String tmps : expressionL) {
		resultString.append (" ").append (tmps);
	    }
	    resultString.append ("\n");
	}
	return resultString.toString();
    }

    String getResourceFileBaseName () { return RESOURCE_FILE_PREFIX; }

    private void generatePropertiesFiles() {
	String filename = null;
	try {
	    for (Map.Entry<String,Properties> entry : messageMap.entrySet()) {
		Properties properties = entry.getValue();
		filename = RESOURCE_FILE_PREFIX + "_" + entry.getKey() + ".properties";
		properties.store (new FileOutputStream(filename), null);
	    }
	    filename = RESOURCE_FILE_PREFIX + ".properties";
	    messageMap.get(DEFAULT_LANG).store (new FileOutputStream(filename), null);
	} catch (IOException e) {
	    System.err.println
                ("error in writing properties to file \"" + filename +
                 "\", error message: " + e.getMessage());
	    throw new RuntimeException (e);
	}
    }

    private XMLReader getXMLReader() {
	SAXParserFactory spf = SAXParserFactory.newInstance();
	spf.setNamespaceAware(true);
	SAXParser saxParser;
	XMLReader xr;
	try {
	    saxParser = spf.newSAXParser();
            // could throw ParserConfigurationException, SAXException
	    xr = saxParser.getXMLReader(); // could throw SAXException
	} catch (SAXException e) {
	    throw new RuntimeException (e);
	} catch (ParserConfigurationException e) {
	    throw new RuntimeException (e);
	}
	xr.setErrorHandler(new MyErrorHandler(System.err));
	return xr;
    }


    ////////////////////////////////////////////////////////////////////
    // Event handlers.
    ////////////////////////////////////////////////////////////////////

    public void startElement(String uri, String localName,
                             String qName, Attributes attributes) {
	if (qName.equals ("metric")) {
	    expressionList = new ArrayList<String>();
	}
	else if (qName.equals ("description")) {
	    language = attributes.getValue (LANGATT);
	    if (language == null) {
		language = DEFAULT_LANG;
	    }
	}

	tmpValue = "";
    }

    public void endElement(String uri, String localName, String qName) {

        tmpValue = tmpValue.trim();
	if (qName.equals ("name")) {
	    eventName = new String(tmpValue);
	} else if (qName.equals("ci") || qName.equals("cn")) {
	    expressionList.add (tmpValue);
	} else if (qName.equals("plus")) {
	    expressionList.add ("+");
	} else if (qName.equals("minus")) {
	    expressionList.add ("-");
	} else if (qName.equals("times")) {
	    expressionList.add ("*");
	} else if (qName.equals("divide")) {
	    expressionList.add ("/");
	} else if (qName.equals("abs")) {
	    expressionList.add ("||");
	} else if (qName.equals("metric")) {
	    if (metricDefinitionMap.put (eventName, expressionList) != null) {
                PS_Debug.print
                    (PS_Debug.WARNING,
                     "Two metric definitions with the same name '" +
                     eventName + "' appeared.");
            }
	} else if (qName.equals("description")) {
	    Map<String,String> map = metricDescriptionMap.get (eventName);
	    if (map == null) {
		map = new TreeMap<String,String>();
	    }
	    map.put (language, tmpValue);
	    metricDescriptionMap.put (eventName, map);

	    Properties properties = messageMap.get (language);
	    if (properties == null) {
		properties = new Properties();
	    }
	    properties.setProperty (eventName, tmpValue);
	    messageMap.put (language, properties);

	} else if (qName.equals("psmetrics")) {
            if (needsResourceFileGeneration) {
                generatePropertiesFiles();
            }
	}
	
	tmpValue = "";
    }


    public void characters(char[] ch, int start, int length) {
	String tmpString = new String (ch, start, length);
	if (tmpString.length() != 0) {
	    if (tmpValue.length() == 0) {
		tmpValue = tmpString;
	    } else {
		tmpValue = tmpValue.concat (tmpString);
	    }
	}
    }

    // Error handler to report errors and warnings
    private static class MyErrorHandler implements ErrorHandler {
        private PrintStream out;

        MyErrorHandler(PrintStream out) {
            this.out = out;
        }

        /**
         * Returns a string describing parse exception details
         */
        private String getParseExceptionInfo(SAXParseException spe) {
            String systemId = spe.getSystemId();
            if (systemId == null) {
                systemId = "null";
            }
            String info = "Public ID: " + spe.getPublicId() +
		", System ID: " + spe.getSystemId() +
                ", Line number: " + spe.getLineNumber() +
                ", Column number: " + spe.getColumnNumber() +
		", Message: " + spe.getMessage();
            return info;
        }

        // The following methods are standard SAX ErrorHandler methods.
        // See SAX documentation for more info.

        public void warning(SAXParseException spe) throws SAXException {
            String message = "Warning: " + getParseExceptionInfo(spe);
            throw new SAXException(message);
        }
        
        public void error(SAXParseException spe) throws SAXException {
            String message = "Error: " + getParseExceptionInfo(spe);
            throw new SAXException(message);
        }

        public void fatalError(SAXParseException spe) throws SAXException {
            String message = "Fatal Error: " + getParseExceptionInfo(spe);
            throw new SAXException(message);
        }
    }
}
