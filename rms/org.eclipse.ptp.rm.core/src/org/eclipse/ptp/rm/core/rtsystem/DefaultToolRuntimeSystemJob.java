/*******************************************************************************
 * Copyright (c) 2008 IBM Corporation.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 ******************************************************************************/
package org.eclipse.ptp.rm.core.rtsystem;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.eclipse.core.filesystem.IFileStore;
import org.eclipse.core.runtime.Assert;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.ptp.core.attributes.ArrayAttribute;
import org.eclipse.ptp.core.attributes.AttributeManager;
import org.eclipse.ptp.core.attributes.IAttribute;
import org.eclipse.ptp.core.attributes.IAttributeDefinition;
import org.eclipse.ptp.core.attributes.StringAttribute;
import org.eclipse.ptp.core.elements.attributes.JobAttributes;
import org.eclipse.ptp.rm.core.utils.ArgumentParser;
import org.eclipse.ptp.rm.core.utils.ITextInputStreamListener;
import org.eclipse.ptp.rm.core.utils.TextInputStreamObserver;

public class DefaultToolRuntimeSystemJob extends AbstractToolRuntimeSystemJob {

	private TextInputStreamObserver stderrObserver;
	private TextInputStreamObserver stdoutObserver;

	public DefaultToolRuntimeSystemJob(String jobID, String queueID, String name, AbstractToolRuntimeSystem rtSystem,
			AttributeManager attrMgr) {
		super(jobID, queueID, name, rtSystem, attrMgr);
	}

	/*
	 * Pattern to fined variables according these rules:
	 * Starts with "${" and ends with "}"
	 * The content is a name and a set of parameters separated by ":"
	 * In the parameters, "\" may be used to quote following chars: '\', '}' and ':'
	 */
	static final Pattern variablePattern = Pattern.compile(("/$/{(/w+)("+"(?:(?:////)|(?:///})|[^/}])*"+")/}").replace('/','\\'));
	static final Pattern parameterPattern = Pattern.compile(":((?:(?:////)|(?:///:)|(?:///})|[^:])*)".replace('/', '\\'));

	/**
	 * Performs substitution of variables using attributes from the attribute manager as variables.
	 * @param input the string with variables.
	 * @param substitutionAttributeManager
	 * @return The string after substitution of variables.
	 */
	protected String replaceVariables(String input, AttributeManager substitutionAttributeManager) {
		StringBuffer output = new StringBuffer();
		Matcher matcher = variablePattern.matcher(input);

		int lastPos = 0;
		while (matcher.find()) {
			int startPos = matcher.start();
			int endPos = matcher.end();
			String name = matcher.group(1);
			String parameterList = matcher.group(2);
			String variable = matcher.group();
			output.append(input.substring(lastPos, startPos));

			/*
			 * Resolve variable.
			 */
			String resolvedValue = null;
			IAttribute<?,?,?> attribute = substitutionAttributeManager.getAttribute(name);
			if (attribute != null) {
				if (attribute instanceof ArrayAttribute<?>) {
					/*
					 * Retrieve parameters or use defaults.
					 */
					String optStartStr = "";
					String optEndStr = "";
					String startStr = "";
					String endStr = "";
					String separatorStr = " ";
					Matcher paramMatcher = parameterPattern.matcher(parameterList);
					if (paramMatcher.find()) {
						startStr = paramMatcher.group(1);
						if (paramMatcher.find()) {
							separatorStr = paramMatcher.group(1);
							if (paramMatcher.find()) {
								endStr = paramMatcher.group(1);
								if (paramMatcher.find()) {
									optStartStr = paramMatcher.group(1);
									if (paramMatcher.find()) {
										optEndStr = paramMatcher.group(1);
									}
								}
							}
						}
					}

					/*
					 * Build content.
					 */
					ArrayAttribute<?> array_attr = (ArrayAttribute<?>) attribute;
					StringBuffer buffer = new StringBuffer();
					boolean first = true;
					List<?> array = array_attr.getValue();
					if (array.size() > 0) {
						buffer.append(optStartStr);
					}
					buffer.append(startStr);
					for (Object element : array) {
						if (first) {
							first = false;
						} else {
							buffer.append(separatorStr);
						}
						assert element != null;
						buffer.append(element);
					}
					buffer.append(endStr);
					if (array.size() > 0) {
						buffer.append(optEndStr);
					}
					resolvedValue = buffer.toString();
				} else {
					resolvedValue = attribute.getValueAsString();
				}
			}

			/*
			 * If failed to resolve variable, keep it on the string. Else replace by its value.
			 */
			if (resolvedValue == null) {
				output.append(variable);
			} else {
				// Recursive macro substitution
				resolvedValue = replaceVariables(resolvedValue, substitutionAttributeManager);
				output.append(resolvedValue);
			}
			lastPos = endPos;
		}
		output.append(input.substring(lastPos));
		String result = output.toString();
		return result;
	}

	@Override
	protected List<String> doCreateLaunchCommand(AttributeManager substitutionAttributeManager) throws CoreException {
		/*
		 * Create launch command. If there is no launch command, simply launch the executable.
		 */
		List<String> command = new ArrayList<String>();
		if (! rtSystem.rmConfiguration.hasLaunchCmd()) {
			// Fall back to calling the executable.
			StringAttribute execPath = getAttrMgr().getAttribute(JobAttributes.getExecutablePathAttributeDefinition());
			ArrayAttribute<String> arguments = getAttrMgr().getAttribute(JobAttributes.getProgramArgumentsAttributeDefinition());
			command.add(execPath.getValue());
			command.addAll(arguments.getValue());
		} else {
			// Use the tool to launch executable
			String launchCommand = rtSystem.rmConfiguration.getLaunchCmd();
			Assert.isNotNull(launchCommand);
			Assert.isTrue(launchCommand.trim().length() > 0);
			launchCommand = replaceVariables(launchCommand, substitutionAttributeManager);
			ArgumentParser argumentParser = new ArgumentParser(launchCommand);
			command = argumentParser.getTokenList();
		}
		return command;
	}

	protected List<String> doCreateDebugCommand(AttributeManager substitutionAttributeManager) throws CoreException {
		/*
		 * Create debug command. If there is no debug command, simply launch the executable.
		 */
		List<String> command = new ArrayList<String>();
		if (! rtSystem.rmConfiguration.hasDebugCmd()) {
			// Fall back to calling the executable.
			StringAttribute execPath = getAttrMgr().getAttribute(JobAttributes.getExecutablePathAttributeDefinition());
			ArrayAttribute<String> arguments = getAttrMgr().getAttribute(JobAttributes.getProgramArgumentsAttributeDefinition());
			command.add(execPath.getValue());
			command.addAll(arguments.getValue());
		} else {
			// Use the tool to launch executable
			String debugCommand = rtSystem.rmConfiguration.getDebugCmd();
			Assert.isNotNull(debugCommand);
			Assert.isTrue(debugCommand.trim().length() > 0);
			debugCommand = replaceVariables(debugCommand, substitutionAttributeManager);
			ArgumentParser argumentParser = new ArgumentParser(debugCommand);
			command = argumentParser.getTokenList();
		}
		return command;
	}

	@Override
	protected IAttributeDefinition<?, ?, ?>[] getDefaultSubstitutionAttributes() {
		return new IAttributeDefinition[]{
				JobAttributes.getEnvironmentAttributeDefinition(),
				JobAttributes.getExecutableNameAttributeDefinition(),
				JobAttributes.getExecutablePathAttributeDefinition(),
				JobAttributes.getJobIdAttributeDefinition(),
				JobAttributes.getNumberOfProcessesAttributeDefinition(),
				JobAttributes.getProgramArgumentsAttributeDefinition(),
				JobAttributes.getQueueIdAttributeDefinition(),
				JobAttributes.getSubIdAttributeDefinition(),
				JobAttributes.getUserIdAttributeDefinition(),
				JobAttributes.getWorkingDirectoryAttributeDefinition()
			};
	}

	@Override
	protected Map<String,String> doCreateEnvironment(AttributeManager substitutionAttributeManager) throws CoreException {
		HashMap<String, String> environmentMap = new HashMap<String, String>();
		/*
		 * Note that environment attribute might not be set if user has not give any environment variables in the launcher configuration.
		 * If the attribute was set, parse environment variables to create a map of entries as "name=value".
		 */
		ArrayAttribute<String> environmentAttribute = getAttrMgr().getAttribute(JobAttributes.getEnvironmentAttributeDefinition());
		if (environmentAttribute == null) {
			return environmentMap; // No attribute set.
		}
		List<String> environment = environmentAttribute.getValue();
		for (String entry : environment) {
			int i = entry.indexOf('=');
			String key = entry.substring(0, i);
			String value = entry.substring(i+1);
			environmentMap.put(key, value);
		}
		return environmentMap;
	}

	@Override
	protected void doBeforeExecution() throws CoreException {
		// Nothing
	}

	@Override
	protected void doExecutionFinished() throws CoreException {
		// Nothing
	}

	@Override
	protected void doExecutionStarted() throws CoreException {
		// Nothing
	}

	@Override
	protected void doExecutionCleanUp() {
		// Nothing
	}

	/**
	 * Only consumes application output.
	 */
	@Override
	protected void doWaitExecution() throws CoreException {
		BufferedReader inReader = new BufferedReader(new InputStreamReader(process.getInputStream()));
		BufferedReader errReader = new BufferedReader(new InputStreamReader(process.getErrorStream()));

		stdoutObserver = new TextInputStreamObserver(
				inReader,
				new ITextInputStreamListener() {
					public void newLine(String line) {
						System.out.println(line);
					}

					public void streamClosed() {
					}

					public void streamError(Exception e) {
					}
				}
		);
		stdoutObserver.start();

		stderrObserver = new TextInputStreamObserver(
				errReader,
				new ITextInputStreamListener() {
					public void newLine(String line) {
						System.err.println(line);
					}

					public void streamClosed() {
					}

					public void streamError(Exception e) {
					}
				}
		);
		stderrObserver.start();

		try {
			stderrObserver.join();
		} catch (InterruptedException e1) {
			e1.printStackTrace();
		}
		try {
			stdoutObserver.join();
		} catch (InterruptedException e1) {
			e1.printStackTrace();
		}
	}

	@Override
	protected void doTerminateJob() {
		if (stderrObserver != null) {
			stderrObserver.kill();
			stderrObserver = null;
		}
		if (stdoutObserver != null) {
			stdoutObserver.kill();
			stdoutObserver = null;
		}
	}

	@Override
	protected IAttribute<?, ?, ?>[] getExtraSubstitutionVariables() throws CoreException {
		return new IAttribute<?, ?, ?>[0];
	}

	@Override
	protected String coCreateWorkingDirectory() {
		return attrMgr.getAttribute(JobAttributes.getWorkingDirectoryAttributeDefinition()).getValue();
	}
}
