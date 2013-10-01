/* $Id: PS_ReportParser.java,v 1.36 2012/01/13 20:49:17 ruiliu Exp $ */

/*******************************************************************************
 * Copyright (c) 2008-2012 The Board of Trustees of the University of Illinois.
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

import java.io.PrintStream;
import java.io.InputStream;
import java.io.File;
import java.io.FileReader;
import java.io.ByteArrayInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.MalformedURLException;

import org.xml.sax.XMLReader;
import org.xml.sax.Attributes;
import org.xml.sax.InputSource;
import org.xml.sax.helpers.XMLReaderFactory;
import org.xml.sax.helpers.DefaultHandler;
import org.xml.sax.SAXException;
import org.xml.sax.SAXParseException;
import org.xml.sax.ErrorHandler;
import org.xml.sax.EntityResolver;

import javax.xml.parsers.SAXParserFactory;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.ParserConfigurationException;

import org.eclipse.ptp.internal.etfw.feedback.perfsuite.util.PS_Debug;
import org.eclipse.ptp.internal.etfw.feedback.perfsuite.util.PS_Environment;

/**
 * Class to parse a PerfSuite report XML document.
 *
 * @author Rui Liu
 */
// @@ Use Locator to report line number, col number if exceptions occur.
class PS_ReportParser extends DefaultHandler {

    static private XMLReader xmlReaderValidating;
    static private XMLReader xmlReaderNonValidating;

    private PS_Report report;
    private PS_MultiHwpcReport multiReport;
    private PS_MultiHwpcProfileReport multiProfileReport;
    private int genXmlType;
    /*
     0: don't generate XML file;
     1: generate the same level of indentation,
        with top level element tags such as
        "<hwpcreport" starting from the first column in a line
        -- for extract "--extract";
     2: add one level of indentation in XML content
        -- for combination "-c" and XML output "-x".
    */
    private StringBuilder xmlBuf;
    private String tmpValue = "";
    private int xmlDepth = 0;
    private static final int xmlIndent;

    static {
        String var = "PSPROCESS_XML_INDENT";
        int tmpind = PS_Environment.getNonNegativeInteger (var);
        if (tmpind != -1) {
            xmlIndent = tmpind;
        } else {
            PS_Debug.print
                (PS_Debug.WARNING, "Wrong format/value for " + var +
                 ". Using the default value of 4...");
            xmlIndent = 4;
        }
    }

    PS_ReportParser (String filename, boolean validateDTD, int genXmlType)
	throws FileNotFoundException, MalformedURLException {
        this.genXmlType = genXmlType;
        ctorHelper (filename, validateDTD);
    }

    PS_ReportParser (String filename, boolean validateDTD)
	throws FileNotFoundException, MalformedURLException {
        ctorHelper (filename, validateDTD);
    }

    PS_ReportParser (InputStream ais, boolean validateDTD)
        throws IOException {

        if (null == ais) {
            throw new IllegalArgumentException ("The input stream is null");
        }

	XMLReader xr = getXMLReader (validateDTD);
	xr.setContentHandler(this);
	xr.setEntityResolver(this);

        InputSource is = new InputSource (ais);
        try {
            xr.parse (is);
            // Could throw IOException, SAXException.
	} catch (SAXException e) {
            /* Change SAXException to RuntimeException, as using SAX or
             * DOM is a choice of implementation.  It should not be
             * exposed in the API. */
	    throw new RuntimeException (e);
        }
    }

    private void ctorHelper (String filename, boolean validateDTD)
	throws FileNotFoundException, MalformedURLException {

	XMLReader xr = getXMLReader (validateDTD);
	xr.setContentHandler(this);
	xr.setEntityResolver(this);

        InputSource is = null;

        if (filename != null) {
            try {
                FileReader r = new FileReader(filename);
                // could throw FileNotFoundException
                is = new InputSource (r);
            } catch (FileNotFoundException e) {
                // Is it a URL?
                is = new InputSource (filename);
            }
        } else {
            is = new InputSource (System.in);
        }

        try {
            xr.parse (is);
            // could throw IOException, SAXException
        } catch (FileNotFoundException e) {
            throw e;
        } catch (MalformedURLException e) {
            throw e;
        } catch (NullPointerException e) {
            PS_Debug.print
                (PS_Debug.WARNING, "A NullPointerException was thrown, " +
                 "usually due to a malformed URL such as http:/illinois.edu, " +
                 "so change it to a MalformedURLException. " +
                 "The original NullPointerException was: \n");
            if (PS_Debug.getLevel() >= PS_Debug.WARNING) {
                e.printStackTrace();
            }
	    throw new MalformedURLException (filename);
	} catch (IOException e) {
	    throw new RuntimeException (e);
	} catch (SAXException e) {
	    throw new RuntimeException (e);
        } catch (UnsupportedOperationException e) {
            throw e;
        } catch (Exception e) {
            throw new RuntimeException (e);
        }
    }

    PS_Report getReport() {
	if (multiReport != null) {
	    return multiReport;
        } else if (multiProfileReport != null) {
	    return multiProfileReport;
	} else {
	    return report;
	}
    }

    private XMLReader getXMLReader(boolean validateDTD) {

	if (validateDTD && (xmlReaderValidating != null)) {
	    return xmlReaderValidating;
	} else if ( (! validateDTD) && (xmlReaderNonValidating != null) ) {
	    return xmlReaderNonValidating;
	} else {
	    SAXParserFactory spf = SAXParserFactory.newInstance();
	    spf.setNamespaceAware(true);
	    spf.setValidating(validateDTD);
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
	    if (validateDTD) {
		xmlReaderValidating = xr;
	    } else {
		xmlReaderNonValidating = xr;
	    }
	    return xr;
	}

    }


    ////////////////////////////////////////////////////////////////////
    // Event handlers.
    ////////////////////////////////////////////////////////////////////

    public InputSource resolveEntity(String publicId, String systemId){
	// typically publicId = null, systemId =
        // "file:///usr/local/share/perfsuite/dtds/pshwpc/hwpcreport.dtd"
	String filename;
	final String FILE_PREFIX= "file://";
	if (systemId.startsWith (FILE_PREFIX)) {
	    filename = systemId.substring(FILE_PREFIX.length());
	} else {
	    filename = systemId;
	}
	// if for any reason the dtd file can not be found and read,
	// return an empty byte stream, to skip the DTD entity parsing.
	// This is particularly needed
        // before the DTD files are installed on the system.
	// An alternative implementation is to use File, and file.canRead().
	// Both are tested to be working.
	try {
	    FileReader dtd = new FileReader (filename);
	    return new InputSource (dtd);
	} catch (FileNotFoundException exception) {
            PS_Debug.print
                (PS_Debug.WARNING, "the DTD file \"" + filename +
                 "\" does not exist, " +
                 "is a directory rather than a regular file, " +
                 "or for some other reason cannot be opened for reading." +
                 " Ignoring the DTD file...");
	    return new InputSource(new ByteArrayInputStream(new byte[0]));    
	}
    }

    /* A helper method to fill the opening tag part.
     * For hwpcreport and hwpcprofilereport,
     *     adds xmlns, but does not prepend newline;
     * for other tags,
     *     does not add xmlns, but prepends newline.
     */
    private void fillXmlBuf (String tagname, Attributes attributes,
                             StringBuilder xmlBuf) {
        if ( (genXmlType != 0) && (xmlBuf != null) ) {
            String nsStr = "";
            if ( (tagname.equals ("hwpcreport")) ||
                 (tagname.equals ("hwpcprofilereport")) ) {
                nsStr = " xmlns:perfsuite=\"http://perfsuite.ncsa.uiuc.edu/\"";
            } else {
                xmlBuf.append ("\n");
            }
            xmlBuf.append (spaces(xmlDepth * xmlIndent))
                .append ("<").append (tagname).append (nsStr);
            int asize = attributes.getLength();
            if (asize > 0) {
                for (int aindex = 0; aindex < asize; aindex++) {
                    // Replace "&" and "<" with "&amp;" and "&lt;",
                    // so that the attribute values are valid XML-syntax-wise.
                    // "&" and "<" are used in C++ method signatures
                    // such as in NAMD:
                    //   ResizeArray<GenericMol*>::add(GenericMol* const&)
                    xmlBuf.append (" ")
                        .append (attributes.getLocalName(aindex))
                        .append ("=\"")
                        .append (attributes.getValue(aindex)
                                 .replace("&", "&amp;")
                                 .replace("<", "&lt;"))
                        .append ("\"");
                }
            }
            xmlBuf.append (">");
        }
    }

    public void startElement(String uri, String localName,
                             String qName, Attributes attributes)
    {
	String tagname = qName.trim();
	PS_Report tmpReport = null;
	if (tagname.equals ("multihwpcreport")) {
	    multiReport = new PS_MultiHwpcReport();
	    tmpReport = multiReport;
	}
	else if (tagname.equals ("multihwpcprofilereport")) {
	    multiProfileReport = new PS_MultiHwpcProfileReport();
	    tmpReport = multiProfileReport;
	}
	else if (tagname.equals ("resourcereport")) {
	    report = new PS_ResourceReport();
	    tmpReport = report;
	}
	else if (tagname.equals ("hwpcprofilereport")) {
	    report = new PS_HwpcProfileReport();
	    tmpReport = report;
            if (genXmlType != 0) {
                xmlDepth = 0;
                if (genXmlType == 2) {
                    // For combination "-c", start with depth 1.
                    xmlDepth = 1;
                }
                xmlBuf = new StringBuilder();
                report.setXmlBuf (xmlBuf);
            }
	}
	else if (tagname.equals ("hwpcreport")) {
	    String mode = attributes.getValue ("mode");
            if (genXmlType != 0) {
                xmlDepth = 0;
                if (genXmlType == 2) {
                    // For combination "-c", start with depth 1.
                    xmlDepth = 1;
                }
            }
	    if ( (mode == null) || (mode.equals ("count")) ) {
		// since the attribute "mode" has a default value of "count",
		// so when it is absent, we create a counting report,
		// that is why we put (mode == null) as one condition.
		report = new PS_HwpcCountingReport();
                if (genXmlType != 0) {
                    xmlBuf = new StringBuilder();
                    report.setXmlBuf (xmlBuf);
                }
	    } else if (mode.equals ("profile")) {
		report = new PS_HwpcPCProfileReport();
                if (genXmlType != 0) {
                    xmlBuf = new StringBuilder();
                    report.setXmlBuf (xmlBuf);
                }
	    } else {
		throw new UnsupportedOperationException
                    ("mode: \"" + mode + "\" not supported for hwpc report.  Only 'count' and 'profile' modes are supported now.");
	    }
            // here the mode should be valid,
            // otherwise it should throw exeception and goes out of scope
	    tmpReport = report;
	}
	else {
	    if (report != null) {
		report.startElement (uri, localName, qName, attributes);
	    }
	}

	if (tmpReport != null) {
	    // it's possible that such attribute is absent,
            // currently deal with them in the setter methods
	    tmpReport.setVersion (attributes.getValue("version"));
 	    tmpReport.setGenerator (attributes.getValue("generator"));
	}

        fillXmlBuf (tagname, attributes, xmlBuf);

        if (genXmlType != 0) {
            tmpValue = "";
            xmlDepth++;
        }

    }

    public void endElement(String uri, String localName, String qName) {
        tmpValue = tmpValue.trim();
	String tagname = qName.trim();
        if (genXmlType != 0 && xmlBuf != null) {
            xmlBuf.append (tmpValue);
            if (tmpValue.equals ("")) {
                // "hwpcevent" element in hpr is required to be EMPTY,
                // while in hr is required to contain a string.
                // so special handle it:
                //     write a newline and the indentation only
                //     if there's no character and it's not "hwpcevent" tag.
                if (! tagname.equals("hwpcevent")) {
                    xmlBuf.append ("\n" + spaces ((xmlDepth-1) * xmlIndent));
                }
            }
        }

	if (tagname.equals ("hwpcreport")) {
            if (multiReport != null) {
                multiReport.addReport ((PS_HwpcReport)report);
            }
            if (genXmlType != 0 && xmlBuf != null) {
                xmlBuf.append ("</hwpcreport>\n");
                xmlBuf = null;
            }
	}
	else if (tagname.equals ("hwpcprofilereport")) {
            if (multiProfileReport != null) {
                multiProfileReport.addReport ((PS_HwpcProfileReport)report);
            }
            if (genXmlType != 0 && xmlBuf != null) {
                xmlBuf.append ("</hwpcprofilereport>\n");
                xmlBuf = null;
            }
	}
	else if (report != null) {
            report.endElement(uri, localName, qName);
            if (genXmlType != 0 && xmlBuf != null) {
                xmlBuf.append ("</" + tagname + ">");
            }
        }

        if (genXmlType != 0) {
            tmpValue = "";
            xmlDepth--;
        }
    }

    private String spaces (int num) {
        if (num <= 0) {
            return "";
        }
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < num; i++) {
            sb.append (" ");
        }
        return sb.toString();
    }

    public void characters(char[] ch, int start, int length) {
	if (report != null) {
	    report.characters (ch, start, length);
            if (genXmlType != 0 && xmlBuf != null) {
                String tmpString = new String (ch, start, length);
                if (tmpString.length() != 0) {
                    if (tmpValue.length() == 0) {
                        tmpValue = tmpString;
                    } else {
                        tmpValue = tmpValue.concat (tmpString);
                    }
                }
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
            String info = "Public ID: " + spe.getPublicId() +
		", System ID: " + spe.getSystemId() +
                ", Line number: " + spe.getLineNumber() +
                ", Column number: " + spe.getColumnNumber() +
		", Message: " + spe.getMessage();
            return info;
        }

        // The following methods are standard SAX ErrorHandler methods.

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
