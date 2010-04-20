package org.eclipse.ptp.rm.pbs.jproxy.parser;

import java.beans.IntrospectionException;
import java.lang.reflect.InvocationTargetException;
import java.util.HashMap;
import java.util.Map;

import org.eclipse.ptp.rm.proxy.core.parser.XMLReader;
import org.w3c.dom.Node;

/**
 * XML reader with fix for broken pbs job format (qstat -x).
 */
public class QstatJobXMLReader extends XMLReader {
	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.eclipse.ptp.rm.proxy.core.parser.XMLReader#populateInput(org.w3c.
	 * dom.Node, java.util.Map)
	 */
	@Override
	protected Map<String, String> populateInput(Node node,
			Map<String, String> input) throws IntrospectionException,
			IllegalAccessException, InvocationTargetException,
			InstantiationException {

		// Hack to recover from not well-formed XML (old qstat versions generate
		// not well-formed XML)
		Node item = node.getChildNodes().item(0);

		// true when qstat XML output is not well-formed
		if (item.getNodeType() == Node.TEXT_NODE) {
			String textContent = item.getTextContent().trim();
			if (textContent.length() > 0) { // make sure textnode is not empty
				// System.out.println("Invalid XML: "+textContent);
				if (input == null) {
					input = new HashMap<String, String>();
				}
				input.put("job_id", textContent); //$NON-NLS-1$
			}
		}
		return super.populateInput(node, input);

	}
}
