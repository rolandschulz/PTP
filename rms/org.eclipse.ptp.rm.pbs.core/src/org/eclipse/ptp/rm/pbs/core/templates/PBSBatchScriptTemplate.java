/*******************************************************************************
 * Copyright (c) 2010 University of Illinois All rights reserved. This program
 * and the accompanying materials are made available under the terms of the
 * Eclipse Public License v1.0 which accompanies this distribution, and is
 * available at http://www.eclipse.org/legal/epl-v10.html 
 * 	
 * Contributors: 
 * 	Albert L. Rossi - design and implementation
 *                    This is the second version of this data structure
 *                    (05/11/2010)
 *                    Third revision. (09/14/2010) -- removed Ben's changes,
 *                    added use of the new converter class.
 ******************************************************************************/

/*******************************************************************************
 * Copyright (c) 2010 The University of Tennessee,
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Benjamin Lindner (ben@benlabs.net) - Attribute Definitions and Mapping (bug 316671)

 *******************************************************************************/

package org.eclipse.ptp.rm.pbs.core.templates;

import java.io.BufferedReader;
import java.io.EOFException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.text.ParseException;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Properties;
import java.util.TreeMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.Path;
import org.eclipse.debug.core.ILaunchConfiguration;
import org.eclipse.debug.core.ILaunchConfigurationWorkingCopy;
import org.eclipse.debug.core.ILaunchManager;
import org.eclipse.ptp.core.IPTPLaunchConfigurationConstants;
import org.eclipse.ptp.core.attributes.BooleanAttribute;
import org.eclipse.ptp.core.attributes.IAttribute;
import org.eclipse.ptp.core.attributes.IAttributeDefinition;
import org.eclipse.ptp.core.attributes.IllegalValueException;
import org.eclipse.ptp.core.attributes.IntegerAttribute;
import org.eclipse.ptp.core.attributes.StringAttribute;
import org.eclipse.ptp.rm.pbs.core.ConfigUtils;
import org.eclipse.ptp.rm.pbs.core.IPBSNonNLSConstants;
import org.eclipse.ptp.rm.pbs.core.attributes.AttributePlaceholder;
import org.eclipse.ptp.rm.pbs.core.messages.Messages;

/**
 * Encapsulates the template used to generate a full (realized) PBS script. <br>
 * 
 * The object is populated by reading in a template file; if a launch
 * configuration is also provided, attribute values that are in the
 * configuration are set.<br>
 * 
 * Configuration can take place through:
 * 
 * @see org.eclipse.ptp.rm.pbs.ui.managers.PBSBatchScriptTemplateManager
 * 
 * @author arossi
 * @since 5.0
 * 
 */
public class PBSBatchScriptTemplate implements IPBSNonNLSConstants {
	public static final int BUFFER_SIZE = 16 * 1024;

	private ILaunchConfiguration configuration;
	private final Map<String, AttributePlaceholder> internalAttributes;
	private String name;
	private final Map<String, AttributePlaceholder> pbsJobAttributes;
	private final StringBuffer text;
	private final IPBSAttributeToTemplateConverter converter;

	public PBSBatchScriptTemplate(IPBSAttributeToTemplateConverter converter) {
		this.converter = converter;
		pbsJobAttributes = new HashMap<String, AttributePlaceholder>();
		internalAttributes = new TreeMap<String, AttributePlaceholder>();
		text = new StringBuffer();
	}

	/**
	 * Potentially sets the values on existing attribute placeholders from the
	 * stored values in the configuration.
	 * 
	 * @throws CoreException
	 * @throws IllegalValueException
	 */
	public void configure() throws CoreException, IllegalValueException {
		for (Iterator<AttributePlaceholder> i = pbsJobAttributes.values().iterator(); i.hasNext();)
			maybeSetValue(i.next());
		for (Iterator<AttributePlaceholder> i = internalAttributes.values().iterator(); i.hasNext();)
			maybeSetValue(i.next());
	}

	/**
	 * Creates the script (with escaped whitespace and line-breaks) and packs it
	 * into an attribute.
	 * 
	 * @return
	 * @throws Throwable
	 */
	public IAttribute<?, ?, ?> createScriptAttribute() throws Throwable {
		Map<String, IAttributeDefinition<?, ?, ?>> defs = converter.getData().getAttributeDefinitionMap();
		IAttributeDefinition<?, ?, ?> def = defs.get(TAG_SCRIPT);
		IAttribute<?, ?, ?> attr = def.create();
		String value = denormalize(realize());
		attr.setValueAsString(value);
		return attr;
	}

	public ILaunchConfiguration getConfiguration() {
		return configuration;
	}

	public IPBSAttributeToTemplateConverter getConverter() {
		return converter;
	}

	public Map<String, AttributePlaceholder> getInternalAttributes() {
		return internalAttributes;
	}

	public AttributePlaceholder getMpiCommand() {
		return internalAttributes.get(TAG_MPICMD);
	}

	public String getName() {
		return name;
	}

	public Map<String, AttributePlaceholder> getPbsJobAttributes() {
		return pbsJobAttributes;
	}

	public AttributePlaceholder getPostpendedBashCommands() {
		return internalAttributes.get(TAG_PSTCMD);
	}

	public AttributePlaceholder getPrependedBashCommands() {
		return internalAttributes.get(TAG_PRECMD);
	}

	public String getText() {
		return text.toString();
	}

	/**
	 * Reads in the template and parses it line by line, creating placeholder
	 * objects for all the <code>@placeholder@</code> strings in the template.
	 * 
	 * @param input
	 *            stream for reading the template
	 * @throws Throwable
	 */
	public void load(InputStream input) throws Throwable {
		clearAll();
		Map<String, IAttributeDefinition<?, ?, ?>> defs = converter.getData().getAttributeDefinitionMap();
		if (defs == null)
			return;
		BufferedReader br = null;
		String line = null;
		AttributePlaceholder ap = null;
		boolean processedExecLine = false;
		String separator = LINE_SEP;
		try {
			br = new BufferedReader(new InputStreamReader(input), BUFFER_SIZE);
			while (true) {
				ap = null;
				try {
					line = br.readLine();
				} catch (EOFException eof) {
					break;
				}
				if (line == null)
					break;
				text.append(line).append(separator);
				if (line.startsWith(PBSDIRECTIVE) && line.indexOf(MARKER) >= 0) {
					ap = handlePBSJobAttribute(line, defs);
					if (ap != null) {
						pbsJobAttributes.put(ap.getName(), ap);
						continue;
					}
				} else if (line.startsWith(PD))
					continue;

				if (processedExecLine) {
					ap = handleInternalPlaceholder(line, PSTCMD_PLACEHOLDER, defs);
					if (ap != null)
						internalAttributes.put(TAG_PSTCMD, ap);
					continue;
				}
				ap = handleInternalPlaceholder(line, PRECMD_PLACEHOLDER, defs);
				if (ap != null) {
					internalAttributes.put(TAG_PRECMD, ap);
					continue;
				}
				ap = handleInternalPlaceholder(line, MPICMD_PLACEHOLDER, defs);
				if (ap != null)
					internalAttributes.put(TAG_MPICMD, ap);
				ap = handleInternalPlaceholder(line, MPIOPT_PLACEHOLDER, defs);
				if (ap != null)
					internalAttributes.put(TAG_MPIOPT, ap);
				if (line.contains(EXECMD_PLACEHOLDER))
					processedExecLine = true;
			}
			if (configuration != null)
				configure();
		} finally {
			if (br != null)
				try {
					br.close();
				} catch (IOException ignore) {
				}
		}
	}

	/**
	 * Takes the original template and substitutes the attribute values for the
	 * <code>@placeholder@</code> strings, or eliminates them (or the entire PBS
	 * directive line in the case of Job Attributes) if their value is empty.
	 * 
	 * @return the script with all placeholders replaced
	 * @throws CoreException
	 */
	public String realize() throws CoreException {
		String template = text.toString();

		if (configuration != null) {
			template = captureEnvironment(template);
			template = maybeReplaceChdir(template);
			template = maybeReplaceExec(template);
			template = maybeReplaceArgs(template);
		}

		for (Iterator<AttributePlaceholder> i = pbsJobAttributes.values().iterator(); i.hasNext();) {
			AttributePlaceholder ap = i.next();
			String name = ap.getName();
			if (TAG_EXPORT_ENV.equals(name)) {
				if ((Boolean) ap.getAttribute().getValue())
					template = replaceWithValue(name, ZEROSTR, template);
				else
					template = removeLine(name, template);
				continue;
			}
			String value = ap.getAttribute().getValueAsString();
			if (ZEROSTR.equals(value))
				template = removeLine(name, template);
			else
				template = replaceWithValue(name, value, template);
		}

		for (Iterator<AttributePlaceholder> i = internalAttributes.values().iterator(); i.hasNext();) {
			AttributePlaceholder ap = i.next();
			String name = ap.getName();
			String value = ap.getAttribute().getValueAsString();
			if (ZEROSTR.equals(value))
				template = removePlaceholder(name, template);
			else
				template = replaceWithValue(name, value, template);
		}

		return template;
	}

	/**
	 * Writes the current attribute values to the passed in launch
	 * configuration. Also updates the internal configuration object by
	 * replacing it with the parameter.
	 * 
	 * @param config
	 *            to which to save the values.
	 */
	public void saveValues(ILaunchConfigurationWorkingCopy config) {
		for (Iterator<AttributePlaceholder> i = pbsJobAttributes.values().iterator(); i.hasNext();) {
			AttributePlaceholder ap = i.next();
			IAttribute<?, ?, ?> attr = ap.getAttribute();
			if (attr == null)
				continue;
			String id = attr.getDefinition().getId();
			Object value = attr.getValue();
			if (value instanceof Boolean)
				config.setAttribute(id, (Boolean) value);
			else if (value instanceof Integer)
				config.setAttribute(id, (Integer) value);
			else if (value instanceof String)
				config.setAttribute(id, (String) value);
		}

		for (Iterator<AttributePlaceholder> i = internalAttributes.values().iterator(); i.hasNext();) {
			AttributePlaceholder ap = i.next();
			IAttribute<?, ?, ?> attr = ap.getAttribute();
			if (attr == null)
				continue;
			String id = attr.getDefinition().getId();
			Object value = attr.getValue();
			if (value instanceof Boolean)
				config.setAttribute(id, (Boolean) value);
			else if (value instanceof Integer)
				config.setAttribute(id, (Integer) value);
			else if (value instanceof String)
				config.setAttribute(id, (String) value);
		}
		configuration = config;
	}

	public void setConfiguration(ILaunchConfiguration configuration) {
		this.configuration = configuration;
	}

	/**
	 * Adds the MPI command attribute and determines the value of the "-n"
	 * argument (<code>@mpiOptions@</code>).
	 * 
	 * @param command
	 *            which MPI command to use
	 * @param selected
	 *            map of current attribute choices
	 * @throws Throwable
	 */
	public void setMPIAttributes(String command) throws Throwable {
		AttributePlaceholder mpiExec = internalAttributes.get(TAG_MPICMD);
		AttributePlaceholder mpiOpt = internalAttributes.get(TAG_MPIOPT);
		Map<String, IAttributeDefinition<?, ?, ?>> defs = converter.getData().getAttributeDefinitionMap();

		if (mpiExec == null) {
			mpiExec = ConfigUtils.getAttributePlaceholder(TAG_MPICMD, ZEROSTR, TAG_INTERNAL, defs);
			if (mpiExec != null)
				internalAttributes.put(TAG_MPICMD, mpiExec);
		}

		if (mpiExec != null)
			mpiExec.getAttribute().setValueAsString(command);

		if (mpiOpt == null) {
			mpiOpt = ConfigUtils.getAttributePlaceholder(TAG_MPIOPT, ZEROSTR, TAG_INTERNAL, defs);
			if (mpiOpt != null)
				internalAttributes.put(TAG_MPIOPT, mpiOpt);
		}

		String cores = null;
		if (ZEROSTR.equals(command))
			cores = ZEROSTR;
		else {
			AttributePlaceholder ap = pbsJobAttributes.get(TAG_NCPUS);
			if (ap != null)
				cores = ap.getAttribute().getValueAsString();
			else {
				ap = pbsJobAttributes.get(TAG_NODES);
				if (ap != null)
					cores = computeMPICoresFromNodesString(ap.getAttribute().getValueAsString());
			}
			cores = MPICORES_FLAG + SP + cores;
		}

		if (mpiOpt != null)
			mpiOpt.getAttribute().setValueAsString(cores);
	}

	public void setName(String name) {
		this.name = name;
	}

	/*
	 * Adds additional environment variables set by the user. These are added as
	 * export commands to the bash script.
	 */
	@SuppressWarnings({ "unchecked", "rawtypes" })
	private String captureEnvironment(String template) throws CoreException {
		Map vars = configuration.getAttribute(ILaunchManager.ATTR_ENVIRONMENT_VARIABLES, (Map) null);
		if (vars == null || vars.isEmpty())
			template = removeLine(TAG_ENV, template);
		else {
			StringBuffer sb = new StringBuffer();
			for (Iterator<Entry> i = vars.entrySet().iterator(); i.hasNext();) {
				Entry entry = i.next();
				sb.append(TAG_EXPORT).append(SP).append(entry.getKey()).append(EQ).append(entry.getValue()).append(LINE_SEP);
			}
			template = replaceWithValue(TAG_ENV, sb.toString(), template);
		}
		return template;
	}

	/*
	 * For potential re-use.
	 */
	private void clearAll() {
		pbsJobAttributes.clear();
		internalAttributes.clear();
		text.setLength(0);
	}

	/*
	 * Creates the internal attribute placeholder, setting its tooltip to the
	 * internal tag.
	 */
	private AttributePlaceholder handleInternalPlaceholder(String line, String marker,
			Map<String, IAttributeDefinition<?, ?, ?>> defs) throws IllegalValueException {
		AttributePlaceholder ap = internalAttributes.get(marker);
		if (ap != null)
			return null;
		if (line.contains(marker)) {
			String name = marker.substring(1, marker.length() - 1);
			ap = ConfigUtils.getAttributePlaceholder(name, ZEROSTR, TAG_INTERNAL, defs);
		}
		return ap;
	}

	/*
	 * Creates the pbs job attribute placeholder. Sets tooltip for eventual
	 * display (on the name label).
	 */
	private AttributePlaceholder handlePBSJobAttribute(String line, Map<String, IAttributeDefinition<?, ?, ?>> defs)
			throws Throwable {
		String name = extractPBSAttributeName(line);
		Properties tooltips = converter.getData().getToolTips();
		return ConfigUtils.getAttributePlaceholder(name, ZEROSTR, tooltips.getProperty(name), defs);
	}

	/*
	 * Specific routine for handling <code>@progArgs@</code>, the program
	 * arguments.
	 */
	private String maybeReplaceArgs(String template) throws CoreException {
		if (configuration == null)
			return template;

		String args = configuration.getAttribute(IPTPLaunchConfigurationConstants.ATTR_ARGUMENTS, ZEROSTR);

		if (ZEROSTR.equals(args))
			return removePlaceholder(TAG_PRARGS, template);
		return replaceWithValue(TAG_PRARGS, args, template);
	}

	/*
	 * Specific routine for handling <code>@directory@</code>, the working
	 * directory.
	 */
	private String maybeReplaceChdir(String template) throws CoreException {
		if (configuration == null)
			return template;
		String wdir = configuration.getAttribute(IPTPLaunchConfigurationConstants.ATTR_WORKING_DIR, ZEROSTR);

		// do what the launch manager does
		if (ZEROSTR.equals(wdir)) {
			String exec = configuration.getAttribute(IPTPLaunchConfigurationConstants.ATTR_EXECUTABLE_PATH, ZEROSTR);
			if (!ZEROSTR.equals(exec))
				// TODO: not platform independent - needs IRemotePath
				wdir = new Path(exec).removeLastSegments(1).toString();
		}

		if (ZEROSTR.equals(wdir))
			return removeLine(TAG_CHGDIR, template);
		return replaceWithValue(TAG_CHGDIR, wdir, template);
	}

	/*
	 * Specific routine for handling <code>@executablePath@</code>, the full
	 * path to the executable.
	 */
	private String maybeReplaceExec(String template) throws CoreException {
		if (configuration == null)
			return template;
		String value = configuration.getAttribute(IPTPLaunchConfigurationConstants.ATTR_EXECUTABLE_PATH, ZEROSTR);
		if (ZEROSTR.equals(value))
			return removePlaceholder(TAG_EXECMD, template);
		return replaceWithValue(TAG_EXECMD, value, template);
	}

	/*
	 * General purpose routine for setting the value on the placeholder from the
	 * passed in configuration. Currently accepts only three types: Boolean,
	 * Integer and String.
	 */
	private void maybeSetValue(AttributePlaceholder ap) throws CoreException, IllegalValueException {
		if (configuration == null || ap == null)
			return;
		IAttribute<?, ?, ?> attr = ap.getAttribute();
		if (attr == null)
			return;
		String id = attr.getDefinition().getId();
		Object value = null;
		if (attr instanceof StringAttribute) {
			value = configuration.getAttribute(id, ap.getDefaultString());
			if (ZEROSTR.equals(value))
				value = null;
		} else if (attr instanceof BooleanAttribute)
			value = configuration.getAttribute(id, new Boolean(ap.getDefaultString()));
		else if (attr instanceof IntegerAttribute)
			value = configuration.getAttribute(id, new Integer(ap.getDefaultString()));
		if (value != null)
			attr.setValueAsString(value.toString());
	}

	/*
	 * Eliminates the entire line on which an empty attribute placeholder
	 * occurs.
	 */
	private String removeLine(String name, String script) {
		StringBuffer p = new StringBuffer();
		p.append(LINE_SEP).append(END_MARKER).append(name).append(END_MARKER).append(LINE_SEP);
		Matcher m = Pattern.compile(p.toString()).matcher(script);
		if (m.find())
			return m.replaceAll(LINE_SEP);
		return script;
	}

	/*
	 * Eliminates an empty attribute placeholder.
	 */
	private String removePlaceholder(String name, String script) {
		name = MARKER + name + MARKER + SP;
		Matcher m = Pattern.compile(name).matcher(script);
		if (m.find())
			return m.replaceAll(ZEROSTR);
		name = name.trim();
		m = Pattern.compile(name).matcher(script);
		if (m.find())
			return m.replaceAll(ZEROSTR);
		return script;
	}

	/*
	 * Replaces attribute placeholder with the given value.
	 */
	private String replaceWithValue(String name, String value, String script) {
		Matcher m = Pattern.compile(MARKER + name + MARKER).matcher(script);
		if (m.find()) {
			value = value.replaceAll(BKESC, BKBKESC); // \ -> \\
			value = value.replaceAll(DLESC, DLESCESC); // $ -> \$
			return m.replaceAll(value);
		}
		return script;
	}

	/*
	 * Computes the total MPI nodes by parsing the node_specification string.
	 * 
	 * Sample request:
	 * 
	 * nodes=2:blue:ppn=2+red:ppn=3.
	 * 
	 * PBS allows the default for nodes (the number at the beginning of the
	 * spec) and ppn to be 1.
	 * 
	 * Note: the current approach may not be entirely correct and may need
	 * modification ...
	 */
	private static String computeMPICoresFromNodesString(String value) {
		int cores = 0;
		try {
			String[] nodeSpec = value.split(TAG_NDSEP);
			for (int i = 0; i < nodeSpec.length; i++) {
				int nodes = 1;
				int ppn = 1;
				String[] part = nodeSpec[i].split(CO);
				for (int j = 0; j < part.length; j++) {
					try {
						nodes = Integer.parseInt(part[j]);
					} catch (NumberFormatException nfe) {
					}
					if (part[j].startsWith(TAG_PPN)) {
						String[] ppnDef = part[j].split(EQ);
						if (ppnDef.length > 1)
							ppn = Integer.parseInt(ppnDef[1]);
					}
				}
				cores += (nodes * ppn);
			}
		} catch (Throwable t) {
			t.printStackTrace();
		}

		return ZEROSTR + cores;
	}

	/*
	 * Escaping to conform with the parsing of the proxy protocol.
	 */
	private static String denormalize(String content) {
		content = content.replaceAll(BKESC, BKBKESC);
		content = content.replaceAll(LINE_SEP, LNSEPESC);
		content = content.replaceAll(TBESC, TBESCESC);
		content = content.replaceAll(SP, SPESC);
		return content;
	}

	/*
	 * Finds the PBS Job Attribute name from a line containing the corresponding
	 * placeholder.
	 */
	private static String extractPBSAttributeName(String line) throws ParseException {
		StringBuffer name = new StringBuffer();
		boolean firstAt = false;
		boolean lastAt = false;
		for (int i = 0; i < line.length(); i++) {
			char c = line.charAt(i);
			switch (c) {
			case '@':
				if (firstAt)
					lastAt = true;
				firstAt = true;
				break;
			default:
				if (firstAt)
					name.append(c);
			}
			if (lastAt)
				break;
		}

		if ((!firstAt && !lastAt) || (firstAt && lastAt))
			return name.toString();
		throw new ParseException(line + Messages.PBSBatchScriptTemplate_parseError, 0);
	}
}
