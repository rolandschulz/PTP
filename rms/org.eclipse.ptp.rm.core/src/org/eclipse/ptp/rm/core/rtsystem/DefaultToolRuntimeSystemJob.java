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
import java.util.HashMap;
import java.util.Map;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.ptp.core.attributes.AttributeManager;
import org.eclipse.ptp.core.attributes.IAttribute;
import org.eclipse.ptp.rm.core.utils.ITextInputStreamListener;
import org.eclipse.ptp.rm.core.utils.TextInputStreamObserver;

/**
 * Sample implementation of {@link AbstractToolRuntimeSystemJob}. Not used anymore.
 * @deprecated
 * @author Daniel Felix Ferber
 */
@Deprecated
public class DefaultToolRuntimeSystemJob extends AbstractToolRuntimeSystemJob {

	private TextInputStreamObserver stderrObserver;
	private TextInputStreamObserver stdoutObserver;

	public DefaultToolRuntimeSystemJob(String jobID, String queueID, String name, AbstractToolRuntimeSystem rtSystem,
			AttributeManager attrMgr) {
		super(jobID, queueID, name, rtSystem, attrMgr);
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
						// Empty implementation.
					}

					public void streamError(Exception e) {
						// Empty implementation.
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
						// Empty implementation.
					}

					public void streamError(Exception e) {
						// Empty implementation.
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
	protected IAttribute<?, ?, ?>[] doRetrieveToolBaseSubstitutionAttributes() throws CoreException {
		return new IAttribute<?, ?, ?>[0];
	}

	@Override
	protected HashMap<String, String> doRetrieveToolEnvironment()
	throws CoreException {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	protected IAttribute<?, ?, ?>[] doRetrieveToolCommandSubstitutionAttributes(
			AttributeManager baseSubstitutionAttributeManager,
			String directory, Map<String, String> environment) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	protected void doPrepareExecution() throws CoreException {
		// Nothing to do
	}

}
