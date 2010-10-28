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

package org.eclipse.ptp.rm.pbs.ui.data;

import java.io.BufferedReader;
import java.io.EOFException;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.FileInputStream;
import java.io.InputStreamReader;
import java.net.URL;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Properties;
import java.util.TreeMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.FileLocator;
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
import org.eclipse.ptp.rm.pbs.core.parser.AttributeDefinitionReader;
import org.eclipse.ptp.rm.pbs.ui.PBSUIPlugin;
import org.eclipse.ptp.rm.pbs.ui.messages.Messages;
import org.eclipse.ptp.rm.pbs.ui.utils.ConfigUtils;
import org.osgi.framework.Bundle;

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
 * 
 */
public class PBSBatchScriptTemplate {
	private static final int BUFFER_SIZE = 512 * 1024;
	private static final String EXECMD_PLACEHOLDER = Messages.PBSBatchScriptTemplate_execmdPlaceholder;
	private static final String MPICMD_PLACEHOLDER = Messages.PBSBatchScriptTemplate_mpicmdPlaceholder;
	private static final String MPICORES_FLAG = Messages.PBSBatchScriptTemplate_mpiCores_flag;
	private static final String MPIOPT_PLACEHOLDER = Messages.PBSBatchScriptTemplate_mpioptPlaceholder;
	private static final String PRECMD_PLACEHOLDER = Messages.PBSBatchScriptTemplate_precmdPlaceholder;
	private static final String PSTCMD_PLACEHOLDER = Messages.PBSBatchScriptTemplate_pstcmdPlaceholder;
	private static final String TAG_CHGDIR = Messages.PBSBatchScriptTemplate_chdirTag;
	private static final String TAG_ENV = Messages.PBSBatchScriptTemplate_envTag;
	private static final String TAG_EXECMD = Messages.PBSBatchScriptTemplate_execTag;
	private static final String TAG_EXPORT_ENV = Messages.PBSJobAttributeName_5;
	private static final String TAG_INTERNAL = Messages.PBSAttributeInternalExtension;
	private static final String TAG_MPICMD = Messages.PBSJobAttributeName_39;
	private static final String TAG_MPIOPT = Messages.PBSJobAttributeName_37;
	private static final String TAG_NCPUS = Messages.PBSJobAttributeName_23;
	private static final String TAG_NODES = Messages.PBSJobAttributeName_25;
	private static final String TAG_PRARGS = Messages.PBSBatchScriptTemplate_prargsTag;
	private static final String TAG_PRECMD = Messages.PBSJobAttributeName_40;
	private static final String TAG_PSTCMD = Messages.PBSJobAttributeName_41;
	private static final String TAG_SCRIPT = Messages.PBSJobAttributeName_38;

	private ILaunchConfiguration configuration;
	private final Map<String, AttributePlaceholder> internalAttributes;
	private String name;
	private final Map<String, AttributePlaceholder> pbsJobAttributes;
	private final StringBuffer text;
	private Properties toolTips;
	
	private Map<String, IAttributeDefinition<?,?,?>>  AttributeDefinitionMap;
	
	public PBSBatchScriptTemplate() {
		pbsJobAttributes = new TreeMap<String, AttributePlaceholder>();
		internalAttributes = new TreeMap<String, AttributePlaceholder>();
		text = new StringBuffer();
		loadToolTips();
		String USER_DIR_KEY = "user.dir";
		String currentDir = System.getProperty(USER_DIR_KEY);

		System.out.println("Working Directory: " + currentDir);
			FileInputStream AttributeDefinitionStream = null;
			try {
				AttributeDefinitionStream = new FileInputStream("org.eclipse.ptp.rm.pbs.core/PBSAttributes/Definitions.txt");
			} catch (FileNotFoundException e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
			}
			List<IAttributeDefinition<?, ?, ?>> AttributeDefinitions = new ArrayList<IAttributeDefinition<?, ?, ?>>();
			try {
				AttributeDefinitions = AttributeDefinitionReader.parse(AttributeDefinitionStream);
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}			

		for (IAttributeDefinition<?,?,?> attr : AttributeDefinitions) {
			AttributeDefinitionMap.put(attr.getId(), attr);
		}
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
	 * @throws IllegalValueException
	 */
	public IAttribute<?, ?, ?> createScriptAttribute() throws IllegalValueException, CoreException {
		//Map<String, IAttributeDefinition<?, ?, ?>> defs = PBSJobAttributes.getAttributeDefinitionMap();
		Map<String, IAttributeDefinition<?, ?, ?>> defs = AttributeDefinitionMap;
		IAttributeDefinition<?, ?, ?> def = defs.get(TAG_SCRIPT);
		IAttribute<?, ?, ?> attr = def.create();
		String value = denormalize(realize());
		attr.setValueAsString(value);
		return attr;
	}

	public ILaunchConfiguration getConfiguration() {
		return configuration;
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
		//Map<String, IAttributeDefinition<?, ?, ?>> defs = PBSJobAttributes.getAttributeDefinitionMap();
		Map<String, IAttributeDefinition<?, ?, ?>> defs = AttributeDefinitionMap;
		BufferedReader br = null;
		String line = null;
		AttributePlaceholder ap = null;
		boolean processedExecLine = false;
		String separator = ConfigUtils.LINE_SEP;
		try {
			br = new BufferedReader(new InputStreamReader(input), BUFFER_SIZE);
			while (true) {
				ap = null;
				try {
					line = br.readLine();
				} catch (EOFException eof) {
				}
				if (line == null)
					break;
				text.append(line).append(separator);
				if (line.startsWith("#PBS")) { //$NON-NLS-1$
					ap = handlePBSJobAttribute(line, defs);
					if (ap != null) {
						pbsJobAttributes.put(ap.getName(), ap);
						continue;
					}
				} else if (line.startsWith("#")) //$NON-NLS-1$
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
					template = replaceWithValue(name, ConfigUtils.EMPTY_STRING, template);
				else
					template = removeLine(name, template);
				continue;
			}
			String value = ap.getAttribute().getValueAsString();
			if (ConfigUtils.EMPTY_STRING.equals(value))
				template = removeLine(name, template);
			else
				template = replaceWithValue(name, value, template);
		}

		for (Iterator<AttributePlaceholder> i = internalAttributes.values().iterator(); i.hasNext();) {
			AttributePlaceholder ap = i.next();
			String name = ap.getName();
			String value = ap.getAttribute().getValueAsString();
			if (ConfigUtils.EMPTY_STRING.equals(value))
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
	 * @throws IllegalValueException
	 */
	public void setMPIAttributes(String command) throws IllegalValueException {
		AttributePlaceholder mpiExec = internalAttributes.get(TAG_MPICMD);
		AttributePlaceholder mpiOpt = internalAttributes.get(TAG_MPIOPT);
		//Map<String, IAttributeDefinition<?, ?, ?>> defs = PBSJobAttributes.getAttributeDefinitionMap();
		Map<String, IAttributeDefinition<?, ?, ?>> defs = AttributeDefinitionMap;
		if (mpiExec == null) {
			mpiExec = ConfigUtils.getAttributePlaceholder(TAG_MPICMD, ConfigUtils.EMPTY_STRING,
					Messages.PBSAttributeInternalExtension, defs);
			if (mpiExec != null)
				internalAttributes.put(TAG_MPICMD, mpiExec);
		}
		mpiExec.getAttribute().setValueAsString(command);

		if (mpiOpt == null) {
			mpiOpt = ConfigUtils.getAttributePlaceholder(TAG_MPIOPT, ConfigUtils.EMPTY_STRING,
					Messages.PBSAttributeInternalExtension, defs);
			if (mpiOpt != null)
				internalAttributes.put(TAG_MPIOPT, mpiOpt);
		}

		String cores = null;
		if (ConfigUtils.EMPTY_STRING.equals(command))
			cores = ConfigUtils.EMPTY_STRING;
		else {
			AttributePlaceholder ap = pbsJobAttributes.get(TAG_NCPUS);
			if (ap != null)
				cores = ap.getAttribute().getValueAsString();
			else {
				ap = pbsJobAttributes.get(TAG_NODES);
				if (ap != null)
					cores = computeMPICoresFromNodesString(ap.getAttribute().getValueAsString());
			}
			cores = MPICORES_FLAG + " " + cores; //$NON-NLS-1$
		}
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
				sb.append(Messages.BASH_EXPORT).append(" ").append(entry.getKey()).append("=").append(entry.getValue()) //$NON-NLS-1$ //$NON-NLS-2$
						.append(ConfigUtils.LINE_SEP);
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
			ap = ConfigUtils.getAttributePlaceholder(name, ConfigUtils.EMPTY_STRING, TAG_INTERNAL, defs);
		}
		return ap;
	}

	/*
	 * Creates the pbs job attribute placeholder. Sets tooltip for eventual
	 * display (on the name label).
	 */
	private AttributePlaceholder handlePBSJobAttribute(String line, Map<String, IAttributeDefinition<?, ?, ?>> defs)
			throws IllegalValueException, ParseException {
		String name = extractPBSAttributeName(line);
		return ConfigUtils.getAttributePlaceholder(name, ConfigUtils.EMPTY_STRING, toolTips.getProperty(name), defs);
	}

	/*
	 * Constructs the mapping of attributes to tooltips from the
	 * tooltip.properties resource.
	 */
	private void loadToolTips() {
		Bundle bundle = PBSUIPlugin.getDefault().getBundle();
		URL url = FileLocator.find(bundle, new Path(Messages.PBSBatchScriptTemplate_tooltips), null);
		if (url == null)
			return;
		InputStream s = null;
		toolTips = new Properties();
		try {
			s = url.openStream();
			toolTips.load(s);
		} catch (IOException e) {
			e.printStackTrace();
		} finally {
			try {
				s.close();
			} catch (IOException e) {
			}
		}
	}

	/*
	 * Specific routine for handling <code>@progArgs@</code>, the program
	 * arguments.
	 */
	private String maybeReplaceArgs(String template) throws CoreException {
		if (configuration == null)
			return template;

		String args = configuration.getAttribute(IPTPLaunchConfigurationConstants.ATTR_ARGUMENTS, ConfigUtils.EMPTY_STRING);

		if (ConfigUtils.EMPTY_STRING.equals(args))
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
		String wdir = configuration.getAttribute(IPTPLaunchConfigurationConstants.ATTR_WORK_DIRECTORY, ConfigUtils.EMPTY_STRING);

		// do what the launch manager does
		if (ConfigUtils.EMPTY_STRING.equals(wdir)) {
			String exec = configuration.getAttribute(IPTPLaunchConfigurationConstants.ATTR_EXECUTABLE_PATH,
					ConfigUtils.EMPTY_STRING);
			if (!ConfigUtils.EMPTY_STRING.equals(exec))
				// TODO: not platform independent - needs IRemotePath
				wdir = new Path(exec).removeLastSegments(1).toString();
		}

		if (ConfigUtils.EMPTY_STRING.equals(wdir))
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
		String value = configuration.getAttribute(IPTPLaunchConfigurationConstants.ATTR_EXECUTABLE_PATH, ConfigUtils.EMPTY_STRING);
		if (ConfigUtils.EMPTY_STRING.equals(value))
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
			if (ConfigUtils.EMPTY_STRING.equals(value))
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
		p.append(ConfigUtils.LINE_SEP).append(".*@").append(name).append(".*@").append(ConfigUtils.LINE_SEP); //$NON-NLS-1$ //$NON-NLS-2$
		Matcher m = Pattern.compile(p.toString()).matcher(script);
		if (m.find())
			return m.replaceAll(ConfigUtils.LINE_SEP);
		return script;
	}

	/*
	 * Eliminates an empty attribute placeholder.
	 */
	private String removePlaceholder(String name, String script) {
		name = "@" + name + "@ "; //$NON-NLS-1$ //$NON-NLS-2$
		Matcher m = Pattern.compile(name).matcher(script);
		if (m.find())
			return m.replaceAll(ConfigUtils.EMPTY_STRING);
		name = name.trim();
		m = Pattern.compile(name).matcher(script);
		if (m.find())
			return m.replaceAll(ConfigUtils.EMPTY_STRING);
		return script;
	}

	/*
	 * Replaces attribute placeholder with the given value.
	 */
	private String replaceWithValue(String name, String value, String script) {
		Matcher m = Pattern.compile("@" + name + "@").matcher(script); //$NON-NLS-1$ //$NON-NLS-2$
		if (m.find()) {
			value = value.replaceAll("\\\\", "\\\\\\\\"); //$NON-NLS-1$ //$NON-NLS-2$ // \ -> \\
			value = value.replaceAll("\\$", "\\\\\\$"); //$NON-NLS-1$ //$NON-NLS-2$ // $ -> \$
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
			String[] nodeSpec = value.split("[+]"); //$NON-NLS-1$
			for (int i = 0; i < nodeSpec.length; i++) {
				int nodes = 1;
				int ppn = 1;
				String[] part = nodeSpec[i].split(":"); //$NON-NLS-1$
				for (int j = 0; j < part.length; j++) {
					try {
						nodes = Integer.parseInt(part[j]);
					} catch (NumberFormatException nfe) {
					}
					if (part[j].startsWith("ppn")) { //$NON-NLS-1$
						String[] ppnDef = part[j].split("="); //$NON-NLS-1$
						if (ppnDef.length > 1)
							ppn = Integer.parseInt(ppnDef[1]);
					}
				}
				cores += (nodes * ppn);
			}
		} catch (Throwable t) {
			t.printStackTrace();
		}

		return "" + cores; //$NON-NLS-1$
	}

	/*
	 * Escaping to conform with the parsing of the proxy protocol.
	 */
	private static String denormalize(String content) {
		content = content.replaceAll("\\\\", "\\\\\\\\"); //$NON-NLS-1$ //$NON-NLS-2$
		content = content.replaceAll(ConfigUtils.LINE_SEP, "\\\\n"); //$NON-NLS-1$ 
		content = content.replaceAll("\\t", "\\\\t"); //$NON-NLS-1$ //$NON-NLS-2$
		content = content.replaceAll(" ", "\\\\s"); //$NON-NLS-1$ //$NON-NLS-2$
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

		if (!firstAt || !lastAt)
			throw new ParseException(line + Messages.PBSBatchScriptTemplate_parseError, 0);
		return name.toString();
	}
}
