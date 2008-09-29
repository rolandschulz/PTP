/******************************************************************************
 * Copyright (c) 2006 IBM Corporation.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - Initial Implementation
 *
 *****************************************************************************/

package org.eclipse.ptp.cell.simulator.internal;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.ConnectException;
import java.net.Socket;
import java.net.URL;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.eclipse.osgi.util.NLS;
import org.eclipse.ptp.cell.simulator.conf.AttributeNames;
import org.eclipse.ptp.cell.simulator.conf.Parameters;
import org.eclipse.ptp.cell.simulator.core.ISimulatorControl;
import org.eclipse.ptp.cell.simulator.core.ISimulatorDelegate;
import org.eclipse.ptp.cell.simulator.core.ISimulatorListener;
import org.eclipse.ptp.cell.simulator.core.ISimulatorParameters;
import org.eclipse.ptp.cell.simulator.core.ISimulatorStatus;
import org.eclipse.ptp.cell.simulator.core.IllegalConfigurationException;
import org.eclipse.ptp.cell.simulator.core.IllegalParameterException;
import org.eclipse.ptp.cell.simulator.core.MissingParameterException;
import org.eclipse.ptp.cell.simulator.core.SimulatorException;
import org.eclipse.ptp.cell.simulator.core.SimulatorKilledException;
import org.eclipse.ptp.cell.simulator.core.SimulatorOperationException;
import org.eclipse.ptp.cell.simulator.core.SimulatorTerminatedException;
import org.eclipse.ptp.cell.utils.stream.StreamObserver;
import org.eclipse.ptp.cell.utils.stream.TextStreamObserver;
import org.eclipse.ptp.cell.utils.terminal.ITerminalProvider;
import org.eclipse.ptp.cell.utils.terminal.TerminalProviderWriter;
import org.eclipse.ptp.cell.utils.vt100.VT100Decoder;
import org.eclipse.ptp.cell.utils.vt100.VT100Listener;


public class SimulatorControl implements VT100Listener, ISimulatorControl {
	// Execution status
	final static int UNKNOWN = 0;
	final static int NOT_STARTED = 1;
	final static int DEPLOYING = 6;
	final static int LAUNCHING = 2;
	final static int OPERATIONAL = 3;
	final static int SHUTTING_DOWN = 4;
	final static int KILLING = 5;
	int processState = NOT_STARTED;

	// Simulation status
	final static int STOPPED = 1;
	final static int STARTED = 2;
	int simulatorState = UNKNOWN;
	
	// Events detected by control
	static final int SIMULATOR_START = 1;
	static final int SIMULATOR_STOP = 2;

	static final int INIT_PARSE = 100;
	static final int INIT_CHECK = 101;
	static final int INIT_CONFIGURE = 103;
	static final int INIT_BOGUSNET = 104;
	static final int INIT_CONSOLE = 105;
	static final int INIT_CONFIGURED = 107;

	static final int BOOT_BIOS = 110;
	static final int BOOT_LINUX = 111;
	static final int BOOT_SYSTEM = 112;
	static final int BOOT_CONFIGURE = 113;
	static final int BOOT_COMPLETE = 114;
	
	static final int SHUTDOWN_PREPARED = 200;
	static final int SHUTDOWN_START = 201;
	static final int SHUTDOWN_COMPLETE = 202;
	
	static final int TCL_STREAM_CLOSED = 301;
	static final int LINUX_STREAM_CLOSED = 302;
	static final int TCL_ERROR_CLOSED = 303;
	
	// Reference to Simulator executable process 
	Process simulatorProcess = null;

	// Simulator 
	ISimulatorStatus status = new SimulatorStatus(this);

	// Auxiliary status information
	/** An error detected by an observing thread. */
	SimulatorException pendingSimulatorException = null;
	String [] launchEnvironment = null;
	ISimulatorParameters launchConfiguration = null;

	// Semaphores
	Semaphore semaphoreReadyToConnect = new Semaphore();
	Semaphore semaphoreLaunchComplete = new Semaphore();
	Semaphore semaphoreShutdownComplete = new Semaphore();
	Semaphore semaphoreKillComplete = new Semaphore();
	Semaphore semaphoreWaitConnection = new Semaphore();
	
	// Process console (TCL)
	TextStreamObserver stdoutObserver = null;
	TextStreamObserver stderrObserver = null;
	ProcessTerminalProvider processTerminal = new ProcessTerminalProvider(this);
	TerminalProviderWriter processTerminalWriter = new TerminalProviderWriter(processTerminal);
	OutputStream processOutputStream;
	
	// Linux console
	Socket terminalSocket = null;
	StreamObserver terminalObserver = null;
	LinuxTerminalProvider linuxTerminal = new LinuxTerminalProvider(this);
	TerminalProviderWriter linuxTerminalWriter = new TerminalProviderWriter(linuxTerminal);
	VT100Decoder vt100Decoder = null;
	
	// TSim socket that processes TCL commands (from Java API)
	TSimDispatcher tsimDispatcher = null;
	
	// Listeners
	List listeners = new ArrayList();
	
	private boolean printDebug;

	/**
	 * Default constructor.
	 */
	public SimulatorControl() {
		printDebug = false;
	}

	public SimulatorControl(boolean printDebug) {
		this.printDebug = printDebug;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.ptp.cell.simulator.ISimulatorControl#getProcessTerminal()
	 */
	public ITerminalProvider getProcessTerminal() {
		return processTerminal;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.ptp.cell.simulator.ISimulatorControl#getLinuxTerminal()
	 */
	public ITerminalProvider getLinuxTerminal() {
		return linuxTerminal;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.ptp.cell.simulator.ISimulatorControl#getStatus()
	 */
	public ISimulatorStatus getStatus() {
		return status;
	}

	/*
	 * (non-Javadoc)
	 * @see org.eclipse.ptp.cell.simulator.core.ISimulatorControl#getParameters()
	 */
	public ISimulatorParameters getParameters() {
		return launchConfiguration;
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.ptp.cell.simulator.ISimulatorControl#addListener(org.eclipse.ptp.cell.simulator.ISimulatorListener)
	 */
	public void addListener(ISimulatorListener listener) {
		synchronized (listeners) {
			listeners.add(listener);			
		}
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.ptp.cell.simulator.ISimulatorControl#removeListener(org.eclipse.ptp.cell.simulator.ISimulatorListener)
	 */
	public void  removeListener(ISimulatorListener listener) {
		synchronized (listeners) {
			listeners.remove(listener);
		}
	}

	/* (non-Javadoc)
	 * @see org.eclipse.ptp.cell.simulator.ISimulatorControl#launch(org.eclipse.ptp.cell.simulator.SimulatorConfig)
	 */
	public void launch(ISimulatorParameters initconfig) throws SimulatorException {
	
		synchronized (this) {
			if (processState != NOT_STARTED) {
				throw new SimulatorException(Messages.SimulatorControl_SimulatorAlreadyStarted);
			}
			/*
			 * Store a copy of the configuraiton, not the configuration itself.
			 * The configuration may be changed by another thread during the launch,
			 * therefore a copy is required.
			 * Test the configuration for errors.
			 */
			try {
				launchConfiguration = (ISimulatorParameters) initconfig.clone();
				validateConfiguration(launchConfiguration);
				launchConfiguration.verify();
			} catch (CloneNotSupportedException e1) {
				// Ignore, clone of configuration is always supported.
			}
		
			/*
			 * Set internal variables to valid state. Test if the working directory
			 * does not contain a lock from another launch.
			 */
			prepareControl();
		}
		
		try {	
			/*
			 * Deploy the files to the working directory.
			 */
			deploySimulatorConfiguration();
			
			/*
			 * Launch the simulator. This will require some time
			 * until the simulator is ready for the next step.
			 * This will start two observers: for stdout and for stderr.
			 */
			launchSimulator();
	
			/*
			 * A semaphore is used to wait until the simulator has launched far
			 * enough to be ready for the next step. The semaphore is notified
			 * and updated by the observers that check the simulator output on
			 * the terminal. The semaphore may throw exceptions because the
			 * launch was cancelled or due some error that was detected by an
			 * observer while the semaphore was waiting.
			 * 
			 * Then, do the next step, that is connecting the linux console and
			 * to the TSim socket.
			 */
			if (launchConfiguration.doConsoleSocketInit()) {
				semaphoreReadyToConnect.waitToOpen();
				connectLinuxConsole();
			}
			if (launchConfiguration.doJavaApiSocketInit()) {
				connectJavaAPIConsole();
			}
			
			/*
			 * Finally, wait until the launch is totally complete.
			 * The last step is changing state variables to an operational simulator.
			 */
			semaphoreLaunchComplete.waitToOpen();
			notifyLifecycleState(ISimulatorListener.OPERATIONAL);
		} catch (SimulatorException e) {
			cleanupControl();
			throw e;
		} catch (Exception e) {
			cleanupControl();
			throw new RuntimeException(Messages.SimulatorControl_UnexpectedError, e);
		}
	}

	/* (non-Javadoc)
	 * @see org.eclipse.ptp.cell.simulator.ISimulatorControl#shutdown()
	 */
	public void shutdown() throws SimulatorException {
		synchronized (this) {
			if (processState != OPERATIONAL) {
				/*
				 * If the simulator is not running, then ignore.
				 */
				cleanupControl();
				return;
			}
			if (! launchConfiguration.doConsoleSocketInit()) {
				/* 
				 * Cannot send shutdown if there is no console.
				 * Simply kill without kind simulator shutdown.
				 */
				cleanupControl();
				return;
			}			
		}
		
		try {
			/*
			 * The simulator must be running to execute the shutdown command.
			 */
			throwPendingError();
			resume();
			throwPendingError();

			/*
			 * Send the shutdown command.
			 */
			linuxTerminalWriter.writeBoth(Parameters.COMMAND_SHUTDOWN+'\n');
			semaphoreShutdownComplete.waitToOpen();
			throwPendingError();
			semaphoreKillComplete.waitToOpen();
		} catch (SimulatorException e) {
			cleanupControl();
			throw e;
		}
	}

	/* (non-Javadoc)
	 * @see org.eclipse.ptp.cell.simulator.ISimulatorControl#pause()
	 */
	public synchronized void pause() throws SimulatorException {
		throwPendingError();
		if (processState != OPERATIONAL) {
			throw new SimulatorException(Messages.SimulatorControl_SimulatorNotOperatioal);
		}
	
		if (simulatorState == STOPPED) {
			return;
		}

		/*
		 * If Java API is enabled, use it to stop simulator. 
		 */
		if (launchConfiguration.doJavaApiSocketInit()) {
			tsimDispatcher.getQueue().addRequest(CommandRequest.stopCommand());
		} else {
			launchConfiguration.getDelegate().stopSimulatorProcess(simulatorProcess);
		}
	}

	/* (non-Javadoc)
	 * @see org.eclipse.ptp.cell.simulator.ISimulatorControl#kill()
	 */
	public void kill() {
		synchronized (this) {
			if (processState == NOT_STARTED) {
				return;
			}
			pendingSimulatorException = new SimulatorKilledException();
			cleanupControl();
		}
	}

	/* (non-Javadoc)
	 * @see org.eclipse.ptp.cell.simulator.ISimulatorControl#resume()
	 */
	public synchronized void resume() throws SimulatorException {
		throwPendingError();
		if (processState != OPERATIONAL) {
			throw new SimulatorException(Messages.SimulatorControl_SimulatorNotOperatioal);
		}
	
		if (simulatorState == STARTED) {
			return;
		}
		
		/*
		 * Send TCL command to stdin. Ufortunately, there is no Java API
		 * command to resume simulator.
		 */
		processTerminalWriter.writeBoth("mysim go\n"); //$NON-NLS-1$
	}

	/**
	 * Facility method that raises a SimulatorException with the last error detected from and observing thread.
	 * @throws SimulatorException
	 */
	private synchronized void throwPendingError() throws SimulatorException {
		if (pendingSimulatorException != null) {
			throw pendingSimulatorException;
		}
	}

	private synchronized void prepareControl() throws SimulatorException {
		simulatorState = UNKNOWN;
		pendingSimulatorException = null;
		launchEnvironment = null;
		semaphoreLaunchComplete.reset();
		semaphoreReadyToConnect.reset();
		semaphoreShutdownComplete.reset();
		semaphoreKillComplete.reset();
		semaphoreWaitConnection.reset();

		vt100Decoder = new VT100Decoder(this);

		/*
		 * Add environment variables that pass parameters to simulator. Many
		 * configuration parameters are passed to the simulator as environment
		 * variables.
		 */
		launchEnvironment = launchConfiguration.getDelegate()
				.createCellSimEnvironment();
		if (printDebug) {
			System.out.println("Simulator environment:"); //$NON-NLS-1$
			for (int i = 0; i < launchEnvironment.length; i++) {
				String item = launchEnvironment[i];
				if (item.startsWith("CELL_")) { //$NON-NLS-1$
					System.out.print("export "); //$NON-NLS-1$
					System.out.println(launchEnvironment[i]);
				}
			}
		}
	}
	
	/**
	 * Checks consistency of the data structure in launch configuration.
	 * Basically, checks if all required attributed are not null or not empty.
	 * @throws IllegalConfigurationException
	 */
	public static void validateConfiguration(ISimulatorParameters launchConfiguration) throws IllegalConfigurationException {
		ISimulatorParameters c = launchConfiguration;
		
		if (c.getDelegate() == null) {
			throw new MissingParameterException(AttributeNames.LAUNCH_DELEGATE);
		}
		if (c.getWorkDirectory() == null) {
			throw new MissingParameterException(AttributeNames.WORK_DIRECTORY);
		}
		if (c.getSimulatorBaseDirectory() == null) {
			throw new IllegalConfigurationException(AttributeNames.SIMULATOR_BASE_DIRECTORY);
		}
		if (c.getSimulatorExecutable() == null) {
			throw new IllegalConfigurationException(AttributeNames.SIMULATOR_EXECUTABLE);
		}
		if (c.getSnifExecutable() == null) {
			throw new IllegalConfigurationException(AttributeNames.SNIF_EXECUTABLE);
		}
		if (c.getLogDirectory() == null) {
			throw new IllegalConfigurationException(AttributeNames.LOG_PATH);
		}
		if (c.getTapDevicePath() == null) {
			throw new IllegalConfigurationException(AttributeNames.TAP_PATH);
		}
		if (c.getTclScriptName() == null) {
			throw new IllegalConfigurationException(AttributeNames.TCL_SCRIPT_NAME);
		} else if (c.getTclScriptName().length() == 0) {
			throw new IllegalConfigurationException(AttributeNames.TCL_SCRIPT_NAME);
		}
		if (c.getTclScriptSource() == null) {
			throw new IllegalConfigurationException(AttributeNames.TCL_SOURCE_NAME);
		}
		String deployFileNames[] = c.getDeployFileNames();
		URL deployFileSources[] = c.getDeployFileSources();
		if (deployFileNames != null && deployFileSources != null) {
			if (deployFileNames.length != deployFileSources.length) {
				throw new IllegalConfigurationException(NLS.bind(Messages.SimulatorControl_SameSizeArray, new String[] {AttributeNames.DEPLOY_FILE_NAMES, AttributeNames.DEPLOY_FILE_SOURCES}));
			}
			for (int i = 0; i < deployFileNames.length; i++) {
				if (deployFileNames[i] == null) {
					throw new MissingParameterException(NLS.bind(Messages.SimulatorControl_EmptyArrayElement, new String[] {AttributeNames.DEPLOY_FILE_NAMES, Integer.toString(i)}));
				} else if (deployFileNames[i].length() == 0) {
					throw new MissingParameterException(NLS.bind(Messages.SimulatorControl_EmptyArrayElement, new String[] {AttributeNames.DEPLOY_FILE_NAMES, Integer.toString(i)}));
				}
				if (deployFileSources[i] == null) {
					throw new MissingParameterException(NLS.bind(Messages.SimulatorControl_EmptyArrayElement, new String[] {AttributeNames.DEPLOY_FILE_SOURCES, Integer.toString(i)}));
				}
			}
		}
		
		if (c.getMemorySize() < Parameters.getMinMemorySize()) {
			throw new IllegalParameterException(
					NLS.bind(Messages.ConfigurationControl_MemorySize, Parameters.MIN_MEMORY_SIZE),
					AttributeNames.MEMORY_SIZE,
					c.getMemorySize());
		}
		if (c.doJavaApiSocketInit()) {
			if (! Parameters.doUseJavaAPI()) {
				throw new IllegalParameterException(Messages.SimulatorControl_JavaAPINotAllowed, AttributeNames.JAVA_API_INIT, Boolean.TRUE.toString());
			}
			if (Parameters.doHandleJavaApiGuiIssue()) {
				if (c.doShowSimulatorGUI()) {
					throw new IllegalParameterException(Messages.SimulatorControl_CannotShowSimulatorGUI, AttributeNames.SHOW_SIMULATOR_GUI, Boolean.TRUE.toString());					
				}
			}
		}

	}

	private void deploySimulatorConfiguration() throws SimulatorException {
		try {
			/*
			 * This method is not enterily synchronized since it implements a lengthy operation.
			 * Synchronized would prevent the kill() method to interrupt this method.
			 * Only parts of the method are synchronized instead.
			 * 
			 * Note that each time that an attribute is accessed, the access must be inside a 
			 * synchronized block, since the may happend a race condition when kill tries to clear its value.
			 * On each operation, the attributes are copied to a local variable in a synchronized block,
			 * then, the lengthy operation is done outside the synchronized block. After a synchronized block,
			 * throwPendingError() must called to check of interruptions from errors or from kill.
			 */
			synchronized (this) {
				processState = DEPLOYING;
			}
			throwPendingError();
			notifyLifecycleState(ISimulatorListener.DEPLOYING);	
			
			/*
			 * Test for previous instance of Simulator.
			 */
			// Get some synchronized data.
			String workDirectoryPath = null;
			String logPath = null;
			ISimulatorDelegate delegate = null;
			synchronized (this) {
				delegate = launchConfiguration.getDelegate();
				workDirectoryPath = launchConfiguration.getWorkDirectory();
				logPath = launchConfiguration.getLogDirectory();
			}
			throwPendingError();
			
			try {
				delegate.verifyPath(ISimulatorDelegate.EXIST, logPath);
				throw new SimulatorOperationException(SimulatorOperationException.EXISTING_SIMULATOR_INSTANCE);
			} catch (SimulatorOperationException e) {
				if (e.getReason() == SimulatorOperationException.PATH_NOT_EXIST) {
					// It's ok. This is the expected exception when the directory does not exist.
				} else if (e.getReason() == SimulatorOperationException.EXISTING_SIMULATOR_INSTANCE) {
					throw e;
				} else {
					throw new SimulatorOperationException(SimulatorOperationException.UNEXPECTED_EXCEPTION, e);					
				}
			}
			
			/*
			 * Create work directory if necessary. Also check if path is writeable.
			 */
			try {
				delegate.recursiveCreateDirectory(workDirectoryPath);
			} catch (Throwable e) {
				throw new SimulatorOperationException(SimulatorOperationException.CREATE_WORKING_DIRECTORY, workDirectoryPath, e);
			}
			
			try {
				delegate.verifyPath(ISimulatorDelegate.ACCESSIBLE_DIR | ISimulatorDelegate.WRITE, workDirectoryPath);
			} catch (SimulatorOperationException e) {
				if (e.getReason() == SimulatorOperationException.PATH_NOT_WRITABLE) {
					throw new SimulatorOperationException(SimulatorOperationException.WORKING_DIRECTORY_READONLY, (e.getCause() == null ? e : e.getCause()));
				} else {
					throw new SimulatorOperationException(SimulatorOperationException.UNEXPECTED_EXCEPTION, e);					
				}			
			}
			
			/*
			 * Copy files to work directory.
			 */
			// TCL script
			String deployPath = null;
			URL sourceURL = null;
			String fileName = null;
			synchronized (this) {
				fileName = launchConfiguration.getTclScriptName();
				deployPath = launchConfiguration.getWorkDirectoryRelativePath(fileName);
				sourceURL = launchConfiguration.getTclScriptSource();
			}
			InputStream inputStream = null;
			try {
				inputStream = sourceURL.openStream();
				delegate.writeFile(deployPath, inputStream);
			} catch (IOException e) {
				throw new SimulatorOperationException(SimulatorOperationException.DEPLOY_FILE, fileName, e);
			}
			
			// Other files
			String deployFiles [] = null;
			URL deploySources[] = null;			
			synchronized (this) {
				deployFiles = launchConfiguration.getDeployFileNames();
				deploySources = launchConfiguration.getDeployFileSources();			
			}
			if (deployFiles == null || deploySources == null) {
				return;
			}
			
			for (int i = 0; i < deploySources.length; i++) {
				throwPendingError();
				sourceURL = deploySources[i];
				fileName = deployFiles[i];
				synchronized (this) {
					deployPath = launchConfiguration.getWorkDirectoryRelativePath(fileName);
				}
				throwPendingError();
				try {
					inputStream = sourceURL.openStream();
					delegate.writeFile(deployPath, inputStream);
				} catch (IOException e) {
					throw new SimulatorOperationException(SimulatorOperationException.DEPLOY_FILE, fileName, e);
				}
			}
		} catch (SimulatorException e) {
			throw e;
		} catch (Throwable e) {
			throw new SimulatorOperationException(SimulatorOperationException.UNEXPECTED_EXCEPTION, e);					
		}
	}
	
	private synchronized void launchSimulator() throws SimulatorException {
		processState = LAUNCHING;
		notifyLifecycleState(ISimulatorListener.LAUNCHING);
		notifyProgress(ISimulatorListener.LAUNCH);
		
		/*
		 * Create command line.
		 */
		String[] commandArray = launchConfiguration.getDelegate().createCommandLine();
		if (printDebug) {
			System.out.println("Simulator command line: "); //$NON-NLS-1$
			for (int i = 0; i < commandArray.length; i++) {
				String item = commandArray[i];
				System.out.print(item + " "); //$NON-NLS-1$
			}
			System.out.println();
		}
		
		/*
		 * Create process and prepare input, output and error streams. The simulator may terminate before we can get
		 * the streams. The CDT launcher may fail if the Spawner is not supported on the platform. Theoretically,
		 * this will never happen, since the spawner is available for Linux, which is the only platform supported by
		 * Cell IDE plugins running the simulator.
		 */
		simulatorProcess = launchConfiguration.getDelegate().createSimulatorProcess(launchConfiguration.getWorkDirectory(), commandArray, launchEnvironment);
//		simulatorProcess = launchConfiguration.getDelegate().createGenericProcess(launchConfiguration.getWorkDirectory(), commandArray, launchEnvironment);
	
		InputStream processInputStream = simulatorProcess.getInputStream();
		processOutputStream = simulatorProcess.getOutputStream();
		InputStream processErrorStream = simulatorProcess.getErrorStream();
	
		if ((processErrorStream == null) || (processOutputStream == null) || (processInputStream == null)) {
			throw new SimulatorException(Messages.SimulatorControl_SimulatorTerminated);
		}

		/*
		 * Try to create listeners. They may not be created if the streams are not defined.
		 */
		if (processInputStream != null) {
			stdoutObserver = new TextStreamObserver(processInputStream, new StdoutListener(this), "stdout"); //$NON-NLS-1$
		}
		if (processErrorStream != null) {
			stderrObserver = new TextStreamObserver(processErrorStream, new StderrListener(this), "stderr"); //$NON-NLS-1$
		}
		if ((stdoutObserver == null) || (stderrObserver == null)) {
			// Is this condition really possible? Null is already being checked.
			throw new SimulatorException(Messages.SimulatorControl_CouldNotConnectProcess);
		}
	
		stdoutObserver.start();
		stderrObserver.start();

		/*
		 * So far, the Simulator process was created successfully and its output and error are being observed for
		 * messages.
		 */
	}

	private void connectLinuxConsole() throws SimulatorException {
		/*
		 * This method is not enterily synchronized since it waits on a semaphore..
		 * Having the semaphore wait inside a synchronized block can cause deadlocks
		 * when another synchronized methods needs to be executed to open the semaphore.
		 */
		int consoleSocketPortTryWait;
		int consoleSocketPortMaxTries;
		synchronized (this) {
			consoleSocketPortTryWait = launchConfiguration.getConsoleSocketPortTryWait();
			consoleSocketPortMaxTries = launchConfiguration.getConsoleSocketPortMaxTries();
		}
		throwPendingError();

		try {
			int tries = 0;
		
			while (terminalObserver == null) {	
				throwPendingError();
				try {
					synchronized (this) {
						terminalSocket = new Socket(launchConfiguration.getConsoleSocketHost(), launchConfiguration.getConsoleSocketPort());
						InputStream terminalInputStream = terminalSocket.getInputStream();
//						OutputStream terminalOutputStream = terminalSocket.getOutputStream();
		
						terminalObserver = new StreamObserver(terminalInputStream, new ConsoleListener(this), "terminal"); //$NON-NLS-1$
						terminalObserver.start();
					}
					throwPendingError();
					break;
				} catch (ConnectException e) {
					// If cannot connect due unavailable socket, ignore to be tried again soon.
					semaphoreWaitConnection.waitToOpen(consoleSocketPortTryWait);
					tries++;
				}
				if (tries > consoleSocketPortMaxTries) {
					throw new SimulatorException(Messages.SimulatorControl_MaxConsoleTries);
				}
			}
		} catch (SimulatorException e) {
			throw e;
		} catch (Exception e) {
			// Catch for remaining connection errors
			throw new SimulatorException(Messages.SimulatorControl_CouldNotConnectConsole, e);
		}
	}
	
	private synchronized void connectJavaAPIConsole() throws SimulatorException {
		/*
		 * This method is not enterily synchronized since it waits on a semaphore..
		 * Having the semaphore wait inside a synchronized block can cause deadlocks
		 * when another synchronized methods needs to be executed to open the semaphore.
		 */
		if (! Parameters.doUseJavaAPI()) {
			return;
		}
		int consoleSocketPortTryWait;
		int consoleSocketPortMaxTries;
		synchronized (this) {
			consoleSocketPortTryWait = launchConfiguration.getJavaApiSocketPortTryWait();
			consoleSocketPortMaxTries = launchConfiguration.getJavaApiSocketPortMaxTries();
		}
		throwPendingError();

		try {
			int tries = 0;
			
			while (true) {	
				throwPendingError();
				try {
					synchronized (this) {
						TSimSocket socket = new TSimSocket();
						socket.connect(launchConfiguration.getJavaApiSocketHost(), launchConfiguration.getJavaApiSocketPort());	
						tsimDispatcher = new TSimDispatcher(socket);
						tsimDispatcher.start();
					}
					throwPendingError();
					break;
				} catch (ConnectException e) {
					// If cannot connect due unavailable socket, ignore to be tried again soon.
					semaphoreWaitConnection.waitToOpen(consoleSocketPortTryWait);
					tries++;
				}
				if (tries > consoleSocketPortMaxTries) {
					throw new SimulatorException(Messages.SimulatorControl_MaxJavaAPITries);
				}
			}
		} catch (SimulatorException e) {
			throw e;
		} catch (Exception e) {
			// Catch for remaining connection errors
			throw new SimulatorException(Messages.SimulatorControl_CouldNotConnectJavaAPI, e);
		}
	}

	/**
	 * Force simulator process to terminate. Reset all control attributes to default values. May be called at any time,
	 * regardless of simulator state.
	 */
	private synchronized void cleanupControl() {

		if (processState == NOT_STARTED) {
			return;
		}

		/*
		 * Get process exit value, only to check if it has finished or needs 
		 * to be killed.
		 */
		if (simulatorProcess != null) {
			try {
				simulatorProcess.exitValue();
			} catch (IllegalThreadStateException e) {
				launchConfiguration.getDelegate().destroySimulatorProcess(simulatorProcess);
//				simulatorProcess.destroy();
			}
//			launchConfiguration.getDelegate().destroySimulatorProcess(simulatorProcess);
			simulatorProcess = null;
			linuxTerminalWriter.writeMetaMessage(Messages.SimulatorControl_SimulatorNotRunningAnymore);
			processTerminalWriter.writeMetaMessage(Messages.SimulatorControl_SimulatorNotRunningAnymore);

			/*
			 * Do clean up of bogusnet. Bogusnet was only started if the simulator
			 * was launched (if a simulatorProcess exists).
			 */
			removeBogusnet();
			removeLogDirectory();
		}
		
		/*
		 * Stop observers and close streams before killing the simulator process.
		 */
		if (tsimDispatcher != null) {
			tsimDispatcher.getSocket().disconnect();
			tsimDispatcher.interrupt();
			tsimDispatcher = null;
		}
		
		if (stderrObserver != null) {
			stderrObserver.kill();
			stderrObserver = null;
		}
		if (stdoutObserver != null) {
			stdoutObserver.kill();
			stdoutObserver = null;
		}

		if (terminalObserver != null) {
			terminalObserver.kill();
			terminalObserver = null;
		}
		vt100Decoder = null;
		
		if (terminalSocket != null) {
			try {
				terminalSocket.close();
			} catch (IOException e1) {
				// Ignore
			}
			terminalSocket = null;
		}
		
		/*
		 * Set state to initial state.
		 */
		processState = NOT_STARTED;
		simulatorState = UNKNOWN;
		// must not reset errorException, since the value might not have been read yet
		// errorException = null;
		launchEnvironment = null;

		/*
		 * The exception must be guaranteed to be set, since this will prevent
		 * any waiting semaphore to open and continue execution of the waiting thread.
		 * In normal circunstances, if the process terminates without having an exception,
		 * then the event observer will automatically set a proper exception.
		 */
		if (pendingSimulatorException == null) {
			pendingSimulatorException = new SimulatorKilledException();
		}

		/*
		 * The semaphores must not be unset, since a thread may be waiting on them. 
		 */
		semaphoreLaunchComplete.open(pendingSimulatorException);
		semaphoreReadyToConnect.open(pendingSimulatorException);
		semaphoreShutdownComplete.open(pendingSimulatorException);
		semaphoreWaitConnection.open(pendingSimulatorException);
	
		semaphoreKillComplete.open();
		
		notifyLifecycleState(ISimulatorListener.TERMINATED);
	}

	private void removeLogDirectory() {
		try {
			launchConfiguration.getDelegate().removeFile(launchConfiguration.getLogDirectory());
		} catch (SimulatorException e) {
			launchConfiguration.getDelegate().logError(Messages.SimulatorControl_WorkDirCleanUpError, e);
		}
	}

	private void removeProcess() {
		try {
			ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
			launchConfiguration.getDelegate().readFile(launchConfiguration.getPIDPath(), byteArrayOutputStream);
			
			ByteArrayInputStream inputStream = new ByteArrayInputStream(byteArrayOutputStream.toByteArray());
			InputStreamReader reader = new InputStreamReader(inputStream);
			BufferedReader bufferedReader = new BufferedReader(reader);
			String content = bufferedReader.readLine();
			if (content != null) {
				String commandArray[] = new String[3];
				commandArray[0] = "kill"; //$NON-NLS-1$
				commandArray[1] = "-9"; //$NON-NLS-1$
				commandArray[2] = content.trim();
				Process process = launchConfiguration.getDelegate().createGenericProcess(null, commandArray, null);
				process.waitFor();
			}
		} catch (SimulatorException e) {
			launchConfiguration.getDelegate().logError(Messages.SimulatorControl_BogusnetCleanUpError, e);
		} catch (IOException e) {
			launchConfiguration.getDelegate().logError(Messages.SimulatorControl_BogusnetCleanUpError, e);
		} catch (InterruptedException e) {
			launchConfiguration.getDelegate().logError(Messages.SimulatorControl_BogusnetCleanUpError, e);
		}
	}
	
	private void removeBogusnet() {
		try {
			ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
			launchConfiguration.getDelegate().readFile(launchConfiguration.getTapDevicePath(), byteArrayOutputStream);
			ByteArrayInputStream inputStream = new ByteArrayInputStream(byteArrayOutputStream.toByteArray());
			InputStreamReader reader = new InputStreamReader(inputStream);
			BufferedReader bufferedReader = new BufferedReader(reader);
			String content = bufferedReader.readLine();
			if (content != null) {
				String commandArray[] = new String[3];
				commandArray[0] = launchConfiguration.getSnifExecutable();
				commandArray[1] = "-d"; //$NON-NLS-1$
				commandArray[2] = content;
				Process process = launchConfiguration.getDelegate().createGenericProcess(null, commandArray, null);
				process.waitFor();
			}
		} catch (SimulatorException e) {
			launchConfiguration.getDelegate().logError(Messages.SimulatorControl_BogusnetCleanUpError, e);
		} catch (IOException e) {
			launchConfiguration.getDelegate().logError(Messages.SimulatorControl_BogusnetCleanUpError, e);
		} catch (InterruptedException e) {
			launchConfiguration.getDelegate().logError(Messages.SimulatorControl_BogusnetCleanUpError, e);
		}
	}

	void notifyEvent(int event) {
		/* 
		 * The event needs to be hold untile the synchronized block
		 * is exited. Else, calling the listeners inside the synchronized
		 * block may potentially cause deadlocks, since the listener may
		 * call a synchronized method 
		 */
		int lifecycleChange = ISimulatorListener.UNKNOWN;
		int statusChange = ISimulatorListener.UNKNOWN;
		int progressChange = ISimulatorListener.UNKNOWN;

		synchronized (this) {
			/*
			 * Events can happen only if simulator was started.
			 * It may happend to receive events when control was cleaned up, since the observing
			 * threads may be signaling that they are exiting because the simulator terminated.
			 */
			if (processState == NOT_STARTED) {
				return;
			}
	
			if (printDebug) {
				System.err.print("Cell Simulator Control detected event: #"); //$NON-NLS-1$
				System.err.println(event);
			}
	
			/*
			 * Check if the simulator process is not running anymore. If so, no further 
			 * events can be handled.
			 * All threads have to confirm that the simulator has been killed before
			 * clearing up.
			 */
			switch (event) {
			case LINUX_STREAM_CLOSED:
				terminalObserver = null;
				processState = KILLING;
				break;
			case TCL_STREAM_CLOSED:
				stdoutObserver = null;
				processState = KILLING;
				break;
			case TCL_ERROR_CLOSED:
				stderrObserver = null;
				processState = KILLING;
				break;
			}
			
			if (processState == KILLING) {
				if (stdoutObserver == null && stderrObserver == null && terminalObserver == null) {
					if (pendingSimulatorException == null) {
						int exitValue = 0;
						try {
							exitValue = simulatorProcess.exitValue();
						} catch (IllegalThreadStateException e) {
							// Ignore, it is terminated, or execution would not have entered this block.
						}
						if (exitValue != 0) {
							pendingSimulatorException = new SimulatorTerminatedException(exitValue);
						} else {
							pendingSimulatorException = new SimulatorTerminatedException();
						}
					}
					semaphoreLaunchComplete.open(pendingSimulatorException);
					semaphoreReadyToConnect.open(pendingSimulatorException);
					semaphoreShutdownComplete.open(pendingSimulatorException);
					semaphoreWaitConnection.open(pendingSimulatorException);
					cleanupControl();
				}
				if (printDebug) {
					System.err.print("Ignored event due KILLING: #"); //$NON-NLS-1$
					System.err.println(event);
				}
				return;
			}
			
			switch (event) {
			/*
			 * Simulation status.
			 */
			case SIMULATOR_STOP:
				if (simulatorState != STOPPED) {
					linuxTerminalWriter.writeMetaMessage(Messages.SimulatorControl_SimulatorNowPaused);
					processTerminalWriter.writeMetaMessage(Messages.SimulatorControl_SimulatorNowPaused);
				}
				simulatorState = STOPPED;
				if (processState == OPERATIONAL) {
					notifyStatus(ISimulatorListener.PAUSED);
				}
				return;
	
			case SIMULATOR_START:
				if (simulatorState != STARTED) {
					linuxTerminalWriter.writeMetaMessage(Messages.SimulatorControl_SimulatorNowResumed);
					processTerminalWriter.writeMetaMessage(Messages.SimulatorControl_SimulatorNowResumed);
				}
				simulatorState = STARTED;
				if (processState == OPERATIONAL) {
					notifyStatus(ISimulatorListener.RESUMED);
				}
				return;
			}
			
			switch (event) {
			/*
			 * Progress during launch.
			 */
			case INIT_PARSE:
				if (processState == LAUNCHING) {
					notifyProgress(ISimulatorListener.INIT_PARSE);
				} else {
					launchConfiguration.getDelegate().logError(Messages.SimulatorControl_InvalidEvent1);
				}
				break;
			case INIT_CHECK:
				if (processState == LAUNCHING) {
					notifyProgress(ISimulatorListener.INIT_CHECK);
				} else {
					launchConfiguration.getDelegate().logError(Messages.SimulatorControl_InvalidEvent1);
				}
				break;
			case INIT_CONFIGURE:
				if (processState == LAUNCHING) {
					notifyProgress(ISimulatorListener.INIT_CONFIGURE);
				} else {
					launchConfiguration.getDelegate().logError(Messages.SimulatorControl_InvalidEvent2);
				}
				break;
			case INIT_BOGUSNET:
				if (processState == LAUNCHING) {
					notifyProgress(ISimulatorListener.INIT_BOGUSNET);
				} else {
					launchConfiguration.getDelegate().logError(Messages.SimulatorControl_InvalidEvent3);
				}
				break;
			case INIT_CONSOLE:
				if (processState == LAUNCHING) {
					semaphoreReadyToConnect.open();
					notifyProgress(ISimulatorListener.INIT_CONSOLE);
				} else {
					launchConfiguration.getDelegate().logError(Messages.SimulatorControl_InvalidEvent4);
				}
				break;
			case BOOT_BIOS:
				if (processState == LAUNCHING) {
					notifyProgress(ISimulatorListener.BOOT_BIOS);
				} else {
					launchConfiguration.getDelegate().logError(Messages.SimulatorControl_InvalidEvent5);
				}
				break;
			case BOOT_LINUX:
				if (processState == LAUNCHING) {
					notifyProgress(ISimulatorListener.BOOT_LINUX);
				} else {
					launchConfiguration.getDelegate().logError(Messages.SimulatorControl_InvalidEvent6);
				}
				break;
			case BOOT_SYSTEM:
				if (processState == LAUNCHING) {
					notifyProgress(ISimulatorListener.BOOT_SYSTEM);
				} else {
					launchConfiguration.getDelegate().logError(Messages.SimulatorControl_InvalidEvent7);
				}
				break;
			case BOOT_CONFIGURE:
				if (processState == LAUNCHING) {
					notifyProgress(ISimulatorListener.BOOT_CONFIGURE);
				} else {
					launchConfiguration.getDelegate().logError(Messages.SimulatorControl_InvalidEvent8);
				}
				break;
			case BOOT_COMPLETE:
				if (processState == LAUNCHING) {
					linuxTerminalWriter.writeMetaMessage(Messages.SimulatorControl_UseThisLinuxConsole);
					processTerminalWriter
							.writeMetaMessage(Messages.SimulatorControl_UseThisTCLConsole);
					notifyProgress(ISimulatorListener.BOOT_COMPLETE);
					processState = OPERATIONAL;
					notifyLifecycleState(ISimulatorListener.OPERATIONAL);
					semaphoreReadyToConnect.open();
					semaphoreLaunchComplete.open();
				} else {
					launchConfiguration.getDelegate().logError(Messages.SimulatorControl_InvalidEvent9);
				}
				break;
	
				/*
				 * Process state for Shutdown.
				 */
			case SHUTDOWN_PREPARED:
				processState = SHUTTING_DOWN;
				notifyLifecycleState(ISimulatorListener.SHUTTING_DOWN);
				notifyProgress(ISimulatorListener.SHUTDOWN_PREPARED);
				break;
			case SHUTDOWN_START:
				if (processState != SHUTTING_DOWN) {
					processState = SHUTTING_DOWN;
					notifyLifecycleState(ISimulatorListener.SHUTTING_DOWN);
				}
				notifyProgress(ISimulatorListener.SHUTDOWN_START);				
				break;
			case SHUTDOWN_COMPLETE:
				if (processState == SHUTTING_DOWN) {
					notifyProgress(ISimulatorListener.SHUTDOWN_COMPLETE);
				} else {
					launchConfiguration.getDelegate().logError(Messages.SimulatorControl_InvalidEvent10);
				}
				break;
			}
		}
		if (lifecycleChange != ISimulatorListener.UNKNOWN) {
			notifyLifecycleState(lifecycleChange);
		}
		if (statusChange != ISimulatorListener.UNKNOWN) {
			notifyStatus(statusChange);
		}
		if (progressChange != ISimulatorListener.UNKNOWN) {
			notifyProgress(statusChange);
		}
	}

	synchronized void notifyError(SimulatorException e) {
		if (printDebug) {
			System.err.println("Cell Simulator Control detected error: "); //$NON-NLS-1$
			System.err.println(e.getMessage());
		}
		
		launchConfiguration.getDelegate().logError(Messages.SimulatorControl_ErrorNotifiedBySimulator, e);
		pendingSimulatorException = e;
		semaphoreLaunchComplete.open(pendingSimulatorException);
		semaphoreReadyToConnect.open(pendingSimulatorException);
		semaphoreShutdownComplete.open(pendingSimulatorException);
		simulatorProcess.destroy();
	}
	
	void notifyError(String message) {
		notifyError(new SimulatorException(message));
	}

	void receiveLinuxConsoleBytes(byte[] bytes, int length) {
		vt100Decoder.receive(bytes, length);
	}

	public void textSequence(byte[] bytes, int length) {
		linuxTerminal.receiveDataFromTerminal(bytes, length);
	}

	void receiveProcessErrorLine(String line) {
		if (printDebug) {
			System.err.println(line);
		}
		byte bytes[] = (line + "\n").getBytes(); //$NON-NLS-1$
		processTerminal.receiveErrorFromTerminal(bytes, bytes.length);
	}

	void receiveProcessLine(String line) {
		if (printDebug) {
			System.out.println(line);
		}
		byte bytes[] = (line + "\n").getBytes(); //$NON-NLS-1$
		processTerminal.receiveDataFromTerminal(bytes, bytes.length);
	}
	
	private void notifyLifecycleState(int state) {
		synchronized (listeners) {
			Iterator iterator = listeners.iterator();
			while (iterator.hasNext()) {
				ISimulatorListener listener = (ISimulatorListener) iterator.next();
				listener.lifecycleStateChanged(state);
			}
		}
	}
	
	private void notifyStatus(int state) {
		synchronized (listeners) {
			Iterator iterator = listeners.iterator();
			while (iterator.hasNext()) {
				ISimulatorListener listener = (ISimulatorListener) iterator.next();
				listener.simulationStatus(state);
			}
		}
	}
	
	private void notifyProgress(int state) {
		synchronized (listeners) {
			Iterator iterator = listeners.iterator();
			while (iterator.hasNext()) {
				ISimulatorListener listener = (ISimulatorListener) iterator.next();
				listener.progressChanged(state);
			}
		}
	}

	public void clear() throws SimulatorException {
		removeProcess();
		/*
		 * We need to wait some time for the simulator process to get killed before cleaning up bogusnet.
		 * Otherwise, the bogusnet cleanup will fail since the device will still be in used by 
		 * the simulator process.
		 */
		try {
			Thread.sleep(3000);
		} catch (InterruptedException e) {
			// Ignore
		}
		removeBogusnet();
		removeLogDirectory();
	}
}
