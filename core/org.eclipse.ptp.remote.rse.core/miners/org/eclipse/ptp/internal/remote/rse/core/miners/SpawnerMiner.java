/*******************************************************************************
 * Copyright (c) 2010 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 * IBM - Initial API and implementation
 *******************************************************************************/
package org.eclipse.ptp.internal.remote.rse.core.miners;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.DataInputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.eclipse.cdt.utils.spawner.ProcessFactory;
import org.eclipse.cdt.utils.spawner.Spawner;
import org.eclipse.dstore.core.miners.Miner;
import org.eclipse.dstore.core.model.DE;
import org.eclipse.dstore.core.model.DataElement;
import org.eclipse.dstore.core.model.DataStoreResources;
import org.eclipse.dstore.core.model.DataStoreSchema;
import org.eclipse.rse.dstore.universal.miners.CommandMiner;
import org.eclipse.rse.dstore.universal.miners.UniversalServerUtilities;
import org.eclipse.rse.internal.dstore.universal.miners.command.patterns.Patterns;

/**
 * @author crecoskie
 *
 */
public class SpawnerMiner extends Miner {
	
    public class CommandMinerDescriptors
    {
        public DataElement _stdout;
        public DataElement _stderr;
        public DataElement _prompt;
        public DataElement _grep;
        public DataElement _pathenvvar;
        public DataElement _envvar;
        public DataElement _libenvvar;
        public DataElement _error;
        public DataElement _warning;
        public DataElement _informational;
        public DataElement _process;
        
        public DataElement getDescriptorFor(String type)
        {
            DataElement descriptor = null;
            if (type.equals("stdout")) //$NON-NLS-1$
            {
                descriptor = _stdout;
            }
            else if  (type.equals("pathenvvar")) //$NON-NLS-1$
            {
                descriptor = _pathenvvar;
            }
            else if (type.equals("envvar")) //$NON-NLS-1$
            {
                descriptor = _envvar;
            }
            else if  (type.equals("libenvvar")) //$NON-NLS-1$
            {
                descriptor = _libenvvar;
            }
            else if (type.equals("error")) //$NON-NLS-1$
            {
                descriptor = _error;
            }
            else if  (type.equals("warning")) //$NON-NLS-1$
            {
                descriptor = _warning;
            }
            else if (type.equals("informational")) //$NON-NLS-1$
            {
                descriptor = _informational;
            }
            else if  (type.equals("process")) //$NON-NLS-1$
            {
                descriptor = _process;
            }
            else if (type.equals("grep")) //$NON-NLS-1$
            {
                descriptor = _grep;
            }
            else if (type.equals("stderr")) //$NON-NLS-1$
            {
                descriptor = _stderr;
            }
            return descriptor;
        }
    }

	private final class WaitThread extends Thread {
		private final Process process;

		private boolean fIsDone = false;
		
		private WaitThread(Process process) {
			this.process = process;
		}

		/* (non-Javadoc)
		 * @see java.lang.Thread#run()
		 */
		@Override
		public void run() {
			try {
				while (!isDone()) {
					try {
						process.waitFor();
						setDone(true);
					} catch (InterruptedException e) {
						// swallow
					}

				}
			} finally {
				setDone(true);
			}

		}
		
		private synchronized void setDone(boolean value) {
			fIsDone = value;
		}
		
		public synchronized boolean isDone() {
			return fIsDone;
		}
	}

	public static final String C_SPAWN_REDIRECTED = "C_SPAWN_REDIRECTED"; //$NON-NLS-1$
	public static final String C_SPAWN_NOT_REDIRECTED = "C_SPAWN_NOT_REDIRECTED"; //$NON-NLS-1$
	public static final String C_SPAWN_TTY = "C_SPAWN_TTY"; //$NON-NLS-1$
	
	public static final String T_SPAWNER_STRING_DESCRIPTOR = "Type.Spawner.String"; //$NON-NLS-1$
	
	public static final String LOG_TAG = "SpawnerMiner"; //$NON-NLS-1$
	
	private CommandMinerDescriptors fDescriptors = new CommandMinerDescriptors();
	boolean fSupportsCharConversion;
	private DataElement _status;
	private Map<DataElement, Process> fProcessMap = new HashMap<DataElement, Process>();
	public boolean _supportsCharConversion = true;
	@SuppressWarnings("restriction")
	private Patterns _patterns;
	
	
	/* (non-Javadoc)
	 * @see org.eclipse.dstore.core.miners.Miner#getVersion()
	 */
	@Override
	public String getVersion() {
		return "0.0.1";  //$NON-NLS-1$
	}

	/* (non-Javadoc)
	 * @see org.eclipse.dstore.core.miners.Miner#handleCommand(org.eclipse.dstore.core.model.DataElement)
	 */
	@Override
	public DataElement handleCommand(DataElement theCommand) throws Exception {
		try {
			return doHandleCommand(theCommand);
		}
		catch(RuntimeException e) {
			UniversalServerUtilities.logError(LOG_TAG, e.toString(), e, _dataStore);
			throw e;
		}
	}

	private DataElement doHandleCommand(DataElement theCommand) {
		String name = getCommandName(theCommand);
		DataElement status = getCommandStatus(theCommand);
		_status = status;
		DataElement subject = getCommandArgument(theCommand, 0);
		
		if (name.equals(C_SPAWN_REDIRECTED)) {
			String cmd = getString(theCommand, 1);
			
			String directory = getString(theCommand, 2);
			File dir = new File(directory);
			
			int envSize = getInt(theCommand, 3);
			
			String[] envp = new String[envSize];
			for (int i = 0; i < envSize; i++) {
				envp[i] = getString(theCommand, i+4);
			}
			
			
			
			handleSpawnRedirected(subject, cmd, dir, envp, status);
			return status;
		}
		
		else if(name.equals(DataStoreSchema.C_CANCEL)) {
			DataElement cancelStatus = getCommandStatus(subject);
			
			// get the name of the command that is to be canceled
			String commandName = subject.getName().trim();
			
			if(commandName.equals(C_SPAWN_REDIRECTED) || commandName.equals(C_SPAWN_NOT_REDIRECTED) || commandName.equals(C_SPAWN_TTY)) {
				handleSpawnCancel(cancelStatus);
			}
			// add more cancelable commands here
		}
		
		else if (name.equals("C_CHAR_CONVERSION")) //$NON-NLS-1$
		{

			fSupportsCharConversion = true;

		}
		return null;
	}

	private synchronized void handleSpawnCancel(DataElement cancelStatus) {
		Process processToCancel = fProcessMap.get(cancelStatus);
		
		if(processToCancel != null) {
			processToCancel.destroy();
			fProcessMap.put(cancelStatus, null);
		}
		
	}

	/**
	 * @param subject 
	 * @param cmd
	 * @param dir
	 * @param envp
	 * @throws IOException
	 */
	private void handleSpawnRedirected(DataElement subject, String cmd, File dir, String[] envp, DataElement status) {
			
		try {
			final Process process = ProcessFactory.getFactory().exec(cmd, envp, dir);
			
			synchronized(this) {
				fProcessMap.put(status, process);
			}

			
//			InputStream stdout = process.getInputStream();
//			StringBuilder sb = new StringBuilder();
//			String line;
//
//			BufferedReader reader = new BufferedReader(new InputStreamReader(stdout, "UTF-8")); //$NON-NLS-1$
//			while ((line = reader.readLine()) != null) {
//				sb.append(line).append("\n"); //$NON-NLS-1$
//			}
//
//			System.out.print(sb.toString());
			
			CommandMinerThread newCommand = new CommandMinerThread(subject, process, cmd, dir.getAbsolutePath(), status, getPatterns(), fDescriptors);
			newCommand.start();
			
//			boolean done = false;
//			while(!done) {
//				try {
//					newCommand.join();
//					done = true;
//				} catch (InterruptedException e) {
//					// swallow
//				}
//			}
			
					
		} catch (IOException e) {
			statusDone(status);
			UniversalServerUtilities.logError(LOG_TAG, e.toString(), e, _dataStore);
		}
		
//		statusDone(status);
	}
	
	/**
	 * Complete status.
	 */
	public static DataElement statusDone(DataElement status) {
		status.setAttribute(DE.A_NAME, DataStoreResources.model_done);
		status.getDataStore().refresh(status);
		return status;
	}

	private String getString(DataElement command, int index) {
		DataElement element = getCommandArgument(command, index);
		return element.getName();
	}
	
	private int getInt(DataElement command, int index) {
		DataElement element = getCommandArgument(command, index);
		Integer i = new Integer(element.getName());
		return i.intValue();
	}

	/* (non-Javadoc)
	 * @see org.eclipse.dstore.core.model.ISchemaExtender#extendSchema(org.eclipse.dstore.core.model.DataElement)
	 */
	public void extendSchema(DataElement schemaRoot) {
		
		// make sure we can load the spawner... if we can't, then bail out
		try {
			System.loadLibrary("spawner"); //$NON-NLS-1$
		}
		
		catch(UnsatisfiedLinkError e) {
			// don't log this error, as it may just be that we are running a
			// generic server that doesn't have a spawner library
			return;
		}
		
		
		DataElement cancellable = _dataStore.findObjectDescriptor(DataStoreResources.model_Cancellable);
		
		DataElement e0_cmd = createCommandDescriptor(schemaRoot, "Spawn process without redirection", C_SPAWN_REDIRECTED, false); //$NON-NLS-1$
		_dataStore.createReference(cancellable, e0_cmd, DataStoreResources.model_abstracts, DataStoreResources.model_abstracted_by);
		
		fDescriptors = new CommandMinerDescriptors();
		fDescriptors._stdout = _dataStore.createObjectDescriptor(schemaRoot, "stdout"); //$NON-NLS-1$
		fDescriptors._stderr = _dataStore.createObjectDescriptor(schemaRoot, "stderr"); //$NON-NLS-1$
		fDescriptors._prompt = _dataStore.createObjectDescriptor(schemaRoot, "prompt"); //$NON-NLS-1$
		fDescriptors._grep = _dataStore.createObjectDescriptor(schemaRoot, "grep"); //$NON-NLS-1$
		fDescriptors._pathenvvar = _dataStore.createObjectDescriptor(schemaRoot, "pathenvvar"); //$NON-NLS-1$
		fDescriptors._envvar = _dataStore.createObjectDescriptor(schemaRoot, "envvar"); //$NON-NLS-1$
		fDescriptors._libenvvar = _dataStore.createObjectDescriptor(schemaRoot, "libenvvar"); //$NON-NLS-1$
		fDescriptors._error = _dataStore.createObjectDescriptor(schemaRoot, "error"); //$NON-NLS-1$
		fDescriptors._warning = _dataStore.createObjectDescriptor(schemaRoot, "warning"); //$NON-NLS-1$
		fDescriptors._informational = _dataStore.createObjectDescriptor(schemaRoot, "informational"); //$NON-NLS-1$
		fDescriptors._process =_dataStore.createObjectDescriptor(schemaRoot, "process"); //$NON-NLS-1$
		
		_dataStore.refresh(schemaRoot);

	}
	
	public void refreshStatus() 
	{
		_dataStore.refresh(_status);
	}
	
	@SuppressWarnings("restriction")
	private Patterns getPatterns()
	{
		if (_patterns == null)
		{
			_patterns = new Patterns(_dataStore);
		}
		return _patterns;
	}

}
