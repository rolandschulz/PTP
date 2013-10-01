/* $Id: PS_MetricDefinition.java,v 1.4 2009/11/04 22:52:00 ruiliu Exp $ */

/*******************************************************************************
 * Copyright (c) 2009 The Board of Trustees of the University of Illinois.
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

import java.util.Map;
import java.util.List;
import java.io.FileNotFoundException;

/**
 * Class representing a PerfSuite metric definition.
 *
 * @author Rui Liu
 */
public class PS_MetricDefinition {

    private PS_MetricDefinitionParser parser;

    /**
     *  Constructs a <code>PS_MetricDefinition</code> object,
     *  given the name of a metric definition file,
     *  and a flag indicating whether resource files for
     *  localized description strings need to be generated.
     *  If the flag is true, resource files will be generated
     *  in the current working directory.
     *  <p>
     *  For example, if the given metric definition file name is
     *  &quot;/foo/bar/xyz.xml&quot;, and it contains description strings of
     *  the languages &quot;en_US&quot;, &quot;en_UK&quot;,
     *  &quot;es&quot; and &quot;it&quot;,
     *  then the base name, as return value of the
     *  <code>getResourceFileBaseName</code> method, will be &quot;xyz&quot;,
     *  and the generated resource files will be named
     *  &quot;xyz_en_US.properties&quot;, &quot;xyz_en_UK.properties&quot;,
     *  &quot;xyz_es.properties&quot;, &quot;xyz_it.properties&quot;,
     *  and &quot;xyz.properties&quot;. The last one,
     *  &quot;xyz.properties&quot;, is the default file to be used if
     *  the current locale does not match any of the above 4 locales.
     *  <p>
     *  Two (2) standard metric definition files
     *  are included in the PerfSuite distribution:
     *  &quot;PAPI_metrics.xml&quot; and &quot;perfmon_metrics.xml&quot;.
     *  Their corresponding resource files containing localized description
     *  strings are included as well, and will be installed when
     *  "make install" is performed.
     *
     *  @param  filename  the name of a metric definition file
     *  @param  needsResourceGeneration
     *     a flag indicating whether resource files for
     *     localized description strings need to be generated.
     *     Its value should be <code>false</code>
     *     if one of the standard metric definition files is used;
     *     and <code>true</code> if a non-standard metric definition file
     *     is used.
     *  @throws FileNotFoundException
     *     if the file <code>filename</code> does not exist,
     *     is a directory rather than a regular file,
     *     or for some other reason cannot be opened for reading.
     *  @throws IllegalArgumentException
     *     if <code>filename</code> is <code>null</code>.
     */
    public PS_MetricDefinition (String filename,
                                boolean needsResourceGeneration)
	throws FileNotFoundException {

	// check the arguments
	if (filename == null) {
	    throw new IllegalArgumentException
                ("Error: metric definition file name is null.");
	}
        parser = new PS_MetricDefinitionParser
            (filename, needsResourceGeneration);
    }

    /**
     *  Returns a map of metric definition.
     *  <p>
     *  For each entry in the map, the key is the name of the metric,
     *  and the value is a list of expression tokens as defined in the metric
     *  definition file. Currently the expression is in postfix notation order,
     *  also known as reverse Polish notation.
     *  But there is no commitment to maintain this order forever.
     *  <p>
     *  An example map entry is: <br>
     *  key: &quot;PS_RATIO_GFPINS_CYC&quot;, <br>
     *  value: a list containing 3 strings in the following order:
     *  &quot;PAPI_FP_INS&quot;, &quot;PAPI_TOT_CYC&quot;, and &quot;/&quot;.
     */
    public Map<String,List<String>> getDefinitionMap() {
	return parser.getDefinitionMap();
    }

    /**
     *  Returns a string representation of the metric definitions,
     *  including the expression definition and descriptions of various
     *  languages for each metric defined in the metric definition file.
     */
    public String toString() {
	return parser.toString();
    }

    /**
     *  Returns the base name of the resource files associated with
     *  this metric definition file.
     *  When generating localized string output,
     *  use return value of this method to specify the resource file name.
     */
    public String getResourceFileBaseName () {
        return parser.getResourceFileBaseName();
    }

}
