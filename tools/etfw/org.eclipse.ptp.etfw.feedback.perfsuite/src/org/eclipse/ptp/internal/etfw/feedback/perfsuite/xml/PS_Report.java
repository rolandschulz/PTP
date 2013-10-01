/* $Id: PS_Report.java,v 1.20 2011/03/30 20:42:48 ruiliu Exp $ */

/*******************************************************************************
 * Copyright (c) 2008-2011 The Board of Trustees of the University of Illinois.
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

import java.io.InputStream;
import org.xml.sax.Attributes;

import java.io.FileNotFoundException;
import java.net.MalformedURLException;
import java.io.IOException;

/**
 * Abstract base class representing a PerfSuite
 * report XML document.
 *
 * @author Rui Liu
 */
public abstract class PS_Report {

    /**
     * Returns an object of class PS_Report, which is
     * the result of parsing a PerfSuite report XML document
     * with the given file name, with the DTD validation on.
     * The returned type will be one of the types of the subclass
     * of PS_Report, which is one of a hwpc counting report,
     * a hwpc profile report, a resource report, or a multi hwpc report.
     * <p>
     * If the given file name is <code>null</code>, standard input is used
     * as input. This functionality can be used to pipe data in.
     * <p>
     * The environment variable <code>PS_DEBUG</code> can be used to control
     * whether or not debug information on standard out and standard error
     * are printed out. If it is set to a positive integer, the
     * debug information will be printed out.
     *
     * @param   filename   the name of the PerfSuite report to parse.
     * @return  an object of class PS_Report, which is the result of parsing
     *          the given PerfSuite report; <code>null</code> is returned
     *          if the given file is not a valid, currently supported
     *          PerfSuite report XML file.
     * @throws  FileNotFoundException  if the file does not exist,
     *          is a directory rather than a regular file,
     *          or for some other reason cannot be opened for reading.
     * @throws  MalformedURLException  if an unknown protocol is specified.
     *
     */
    static public PS_Report newInstance (String filename)
	throws FileNotFoundException, MalformedURLException {

	//if (null == filename)
	//    throw new IllegalArgumentException("filename is null");

	return newInstance (filename, true);
    }

    /**
     * Returns an object of class PS_Report, which is
     * the result of parsing a PerfSuite report XML document
     * with the given file name, with the given DTD validation option.
     * The returned type will be one of the types of the subclass
     * of PS_Report, which is one of a hwpc counting report,
     * a hwpc profile report, a resource report, or a multi hwpc report.
     * <p>
     * If the given file name is <code>null</code>, standard input is used
     * as input. This functionality can be used to pipe data in.
     * <p>
     * The environment variable <code>PS_DEBUG</code> can be used to control
     * whether or not debug information on standard out and standard error
     * are printed out. If it is set to a positive integer, the
     * debug information will be printed out.
     *
     * @param   filename   the name of the PerfSuite report to parse.
     * @param   validateDTD
     *              a flag indicating whether to do DTD validation or not.
     * @return  an object of class PS_Report, which is the result of parsing
     *          the given PerfSuite report; <code>null</code> is returned
     *          if the given file is not a valid, currently supported
     *          PerfSuite report XML file.
     * @throws  FileNotFoundException  if the file does not exist,
     *          is a directory rather than a regular file,
     *          or for some other reason cannot be opened for reading.
     * @throws  MalformedURLException  if an unknown protocol is specified.
     * @throws  UnsupportedOperationException if the given report is a
     *          PerfSuite HWPC report with the <code>mode</code> attribute
     *          present, but the value of the <code>mode</code> attribute
     *          is neither &quot;count&quot; nor &quot;profile&quot;.
     *          <br>
     *          The <code>UnsupportedOperationException</code>
     *          can be thrown only if DTD validation is set to false;
     *          if DTD validation is set to true,
     *          since hwpcreport.dtd specifies that the only allowed values
     *          for <code>mode</code> attribute
     *          are &quot;count&quot; and &quot;profile&quot;,
     *          SAX parser will validate it
     *              and throw a <code>SAXException</code>,
     *          then this API will convert it
     *              to a <code>RuntimeException</code>.
     */
    static public PS_Report newInstance (String filename, boolean validateDTD)
	throws FileNotFoundException, MalformedURLException {

	//if (null == filename)
	//    throw new IllegalArgumentException("filename is null");

	PS_ReportParser rp = new PS_ReportParser (filename, validateDTD);
	return rp.getReport();
    }

    /**
     * Returns an object of class PS_Report, which is
     * the result of parsing a PerfSuite report XML document
     * with the given input stream, with the given DTD validation option.
     * The returned type will be one of the types of the subclass
     * of PS_Report, which is one of a hwpc counting report,
     * a hwpc profile report, a resource report, or a multi hwpc report.
     * <p>
     * The environment variable <code>PS_DEBUG</code> can be used to control
     * whether or not debug information on standard out and standard error
     * are printed out. If it is set to a positive integer, the
     * debug information will be printed out.
     *
     * @param   ais   an InputStream object containing the content to parse.
     * @param   validateDTD
     *              a flag indicating whether to do DTD validation or not.
     * @return  an object of class PS_Report, which is the result of parsing
     *          the given PerfSuite report; <code>null</code> is returned
     *          if the given file is not a valid, currently supported
     *          PerfSuite report XML file.
     * @throws  IOException   an IO exception from the parser, possibly from
     *          a byte stream or character stream supplied by the application.
     * @throws  UnsupportedOperationException if the given report is a
     *          PerfSuite HWPC report with the <code>mode</code> attribute
     *          present, but the value of the <code>mode</code> attribute
     *          is neither &quot;count&quot; nor &quot;profile&quot;.
     *          <br>
     *          The <code>UnsupportedOperationException</code>
     *          can be thrown only if DTD validation is set to false;
     *          if DTD validation is set to true,
     *          since hwpcreport.dtd specifies that the only allowed values
     *          for <code>mode</code> attribute
     *          are &quot;count&quot; and &quot;profile&quot;,
     *          SAX parser will validate it
     *              and throw a <code>SAXException</code>,
     *          then this API will convert it
     *              to a <code>RuntimeException</code>.
     */
    static public PS_Report newInstance (InputStream ais, boolean validateDTD)
        throws IOException {

	if (null == ais) {
	    throw new IllegalArgumentException("The input stream is null");
        }

	PS_ReportParser rp = new PS_ReportParser (ais, validateDTD);
	return rp.getReport();
    }

    /**
     * Exactly like the method
     * <code>newInstance (String filename, boolean validateDTD)</code>,
     * except that the XML content could be generated in a string buffer,
     * which can be obtained after this method by calling
     * <code>getXmlBuf()</code>.
     *
     * Additional parameter besides those in
     * <code>newInstance (String filename, boolean validateDTD)</code>:
     *
     * @param genXmlType 
     *      the type indicating how the XML content should be generated,
     *      with the following meanings:
     *       0: don't generate XML file;
     *       1: generate the same level of indentation,
     *          with top level element tags such as
     *          "&lt;hwpcreport" starting from the first column in a line
     *          -- for extract "--extract";
     *       2: add one level of indentation in XML content
     *           -- for combination "-c" and XML output "-x".
     */
     static public PS_Report newInstanceGenXML
        (String filename, boolean validateDTD, int genXmlType)
	throws FileNotFoundException, MalformedURLException {

	PS_ReportParser rp = new PS_ReportParser
            (filename, validateDTD, genXmlType);
	return rp.getReport();
    }

    /**
     * Added package private constructor to disallow use of the constructor
     * outside of this package.
     */
    PS_Report() {
    }

    /**
     * Returns a string describing the DTD version of
     * this PerfSuite report XML document.
     */
    public String getVersion() {
	return version;
    }

    /**
     * Returns a string describing the program
     * that generates this PerfSuite report XML document.
     */
    public String getGenerator() {
	return generator;
    }

    void setVersion(String ver) {
	if (ver == null) {
	    version = "N/A";
	} else {
	    version = ver;
	}
    }

    void setGenerator(String gen) {
	if (gen == null) {
	    generator = "N/A";
	} else {
	    generator = gen;
	}
    }

    void startElement(String uri, String localName,
                      String qName, Attributes attributes) {
    }

    void endElement(String uri, String localName, String qName) {

    }

    void characters(char[] ch, int start, int length) {

    }

    /**
     * Returns a string describing the DTD version
     * and the generator of this report.
     */
    public String toString() {
        StringBuilder res = new StringBuilder();
	res.append ("  Version:   " + version + "\n");
	res.append ("  Generator: " + generator + "\n");
        return res.toString();
    }

    public void setXmlBuf (StringBuilder buf) { xmlBuf = buf; }

    /**
     * Returns the string buffer that contains the XML content
     * generated at the parsing time.
     */
    public StringBuilder getXmlBuf() { return xmlBuf; }

    private String version;
    private String generator;

    private StringBuilder xmlBuf;
}
