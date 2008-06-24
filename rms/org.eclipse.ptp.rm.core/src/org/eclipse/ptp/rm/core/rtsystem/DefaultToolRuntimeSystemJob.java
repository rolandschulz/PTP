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
import java.util.List;

import org.eclipse.core.runtime.Assert;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.ptp.core.attributes.ArrayAttribute;
import org.eclipse.ptp.core.attributes.AttributeManager;
import org.eclipse.ptp.core.attributes.IAttribute;
import org.eclipse.ptp.core.attributes.StringAttribute;
import org.eclipse.ptp.core.elements.attributes.JobAttributes;
import org.eclipse.ptp.rm.core.utils.ArgumentParser;
import org.eclipse.ptp.rm.core.utils.ILineStreamListener;
import org.eclipse.ptp.rm.core.utils.TextStreamObserver;

public class DefaultToolRuntimeSystemJob extends AbstractToolRuntimeSystemJob {

	private TextStreamObserver stderrObserver;
	private TextStreamObserver stdoutObserver;

	public DefaultToolRuntimeSystemJob(String jobID, String name, AbstractToolRuntimeSystem rtSystem,
			AttributeManager attrMgr) {
		super(jobID, name, rtSystem, attrMgr);
	}

	protected String replaceVariables(String s) {
		AttributeManager manager = getAttrMgr();
		IAttribute<?,?,?>[] attributes = manager.getAttributes();
		for (IAttribute<?, ?, ?> attribute : attributes) {
			String variable = "${"+attribute.getDefinition().getId()+"}";
			if (attribute instanceof ArrayAttribute<?>) {
				ArrayAttribute<?> arrayAttribute = (ArrayAttribute<?>) attribute;
				List<?> list = arrayAttribute.getValue();
				String t = null;
				for (Object o : list) {
					if (t == null) {
						t = o.toString();
					} else {
						t += " " + o.toString();
					}
				}
				if (t != null) {
					s = s.replace(variable, t);
				} else {
					s = s.replace(variable, "");
				}
			} else {
				String value = attribute.getValueAsString();
				s = s.replace(variable, value);
			}
		}
		return s;
	}

	@Override
	protected List<String> doCreateCommand() throws CoreException {
		/*
		 * Create launch command. If the is not launch command, simply launch the executable.
		 * If there is a launch command, suppose that the program executable is the last argument, followed
		 * by program arguments.
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
			launchCommand = replaceVariables(launchCommand);
			ArgumentParser argumentParser = new ArgumentParser(launchCommand);
			command = argumentParser.getTokenList();
		}
		return command;
	}

	@Override
	protected void doBeforeExecution() throws CoreException {
		// TODO Auto-generated method stub
	}

	@Override
	protected void doExecutionFinished() throws CoreException {
		// TODO Auto-generated method stub
	}

	@Override
	protected void doExecutionStarted() throws CoreException {
		// TODO Auto-generated method stub
	}
	
	@Override
	protected void doExecutionCleanUp() {
		// TODO Auto-generated method stub
	}

	/**
	 * Only consumes application output.
	 */
	@Override
	protected void doWaitExecution() throws CoreException {
		BufferedReader inReader = new BufferedReader(new InputStreamReader(process.getInputStream()));
		BufferedReader errReader = new BufferedReader(new InputStreamReader(process.getErrorStream()));

		stdoutObserver = new TextStreamObserver(
				inReader,
				new ILineStreamListener() {
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

		stderrObserver = new TextStreamObserver(
				errReader,
				new ILineStreamListener() {
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
}
