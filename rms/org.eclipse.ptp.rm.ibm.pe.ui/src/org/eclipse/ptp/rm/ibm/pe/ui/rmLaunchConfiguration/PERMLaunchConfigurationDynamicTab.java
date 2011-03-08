/*******************************************************************************
 * Copyright (c) 2005, 2006, 2007 Los Alamos National Security, LLC.
 * This material was produced under U.S. Government contract DE-AC52-06NA25396
 * for Los Alamos National Laboratory (LANL), which is operated by the Los Alamos
 * National Security, LLC (LANS) for the U.S. Department of Energy.  The U.S. Government has
 * rights to use, reproduce, and distribute this software. NEITHER THE
 * GOVERNMENT NOR LANS MAKES ANY WARRANTY, EXPRESS OR IMPLIED, OR
 * ASSUMES ANY LIABILITY FOR THE USE OF THIS SOFTWARE. If software is modified
 * to produce derivative works, such modified software should be clearly marked,
 * so as not to confuse it with the version available from LANL.
 *
 * Additionally, this program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Copyright (c) 2006, 2007 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *******************************************************************************/
package org.eclipse.ptp.rm.ibm.pe.ui.rmLaunchConfiguration;

import java.io.File;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.StringTokenizer;
import java.util.Vector;

import org.eclipse.core.filesystem.IFileInfo;
import org.eclipse.core.filesystem.IFileStore;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.Path;
import org.eclipse.debug.core.ILaunchConfiguration;
import org.eclipse.debug.core.ILaunchConfigurationWorkingCopy;
import org.eclipse.debug.ui.ILaunchConfigurationDialog;
import org.eclipse.ptp.core.attributes.BigIntegerAttribute;
import org.eclipse.ptp.core.attributes.BigIntegerAttributeDefinition;
import org.eclipse.ptp.core.attributes.IAttribute;
import org.eclipse.ptp.core.attributes.IAttributeDefinition;
import org.eclipse.ptp.core.attributes.IllegalValueException;
import org.eclipse.ptp.core.attributes.IntegerAttribute;
import org.eclipse.ptp.core.attributes.IntegerAttributeDefinition;
import org.eclipse.ptp.core.attributes.StringAttributeDefinition;
import org.eclipse.ptp.core.attributes.StringSetAttribute;
import org.eclipse.ptp.core.attributes.StringSetAttributeDefinition;
import org.eclipse.ptp.core.elements.IPQueue;
import org.eclipse.ptp.core.elements.IPResourceManager;
import org.eclipse.ptp.launch.ui.extensions.AbstractRMLaunchConfigurationDynamicTab;
import org.eclipse.ptp.launch.ui.extensions.RMLaunchValidation;
import org.eclipse.ptp.remote.core.IRemoteConnection;
import org.eclipse.ptp.remote.core.IRemoteConnectionManager;
import org.eclipse.ptp.remote.core.IRemoteServices;
import org.eclipse.ptp.remote.ui.IRemoteUIFileManager;
import org.eclipse.ptp.remote.ui.IRemoteUIServices;
import org.eclipse.ptp.remote.ui.PTPRemoteUIPlugin;
import org.eclipse.ptp.rm.ibm.pe.core.rmsystem.IPEResourceManagerConfiguration;
import org.eclipse.ptp.rm.ibm.pe.core.rmsystem.PEResourceManager;
import org.eclipse.ptp.rm.ibm.pe.ui.messages.Messages;
import org.eclipse.ptp.rm.ibm.pe.ui.widgets.BooleanRowWidget;
import org.eclipse.ptp.rm.ibm.pe.ui.widgets.CheckboxRowWidget;
import org.eclipse.ptp.rm.ibm.pe.ui.widgets.ComboRowWidget;
import org.eclipse.ptp.rm.ibm.pe.ui.widgets.DualFieldRowWidget;
import org.eclipse.ptp.rm.ibm.pe.ui.widgets.FileSelectorRowWidget;
import org.eclipse.ptp.rm.ibm.pe.ui.widgets.TextRowWidget;
import org.eclipse.ptp.rm.ibm.pe.ui.widgets.WidgetAttributes;
import org.eclipse.ptp.rmsystem.AbstractResourceManager;
import org.eclipse.ptp.rmsystem.IResourceManager;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Layout;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.TabFolder;
import org.eclipse.swt.widgets.TabItem;
import org.eclipse.swt.widgets.Widget;

public class PERMLaunchConfigurationDynamicTab extends AbstractRMLaunchConfigurationDynamicTab {
	// TODO
	// 8) popup 'notepad' editor to create host file
	/*
	 * The following constants define the names of all attributes which may be
	 * known by the PE proxy. Names starting with MP_* represent the
	 * corresponding PE environment variables. Names starting with PE_*
	 * represent additional attributes used internally by the PE proxy.
	 */
	private static final String MP_ACK_THRESH = "MP_ACK_THRESH"; //$NON-NLS-1$
	private static final String MP_ADAPTER_USE = "MP_ADAPTER_USE"; //$NON-NLS-1$
	private static final String MP_BULK_MIN_MSG_SIZE = "MP_BULK_MIN_MSG_SIZE"; //$NON-NLS-1$
	private static final String MP_CC_SCRATCH_BUF = "MP_CC_SCRATCH_BUF"; //$NON-NLS-1$
	private static final String MP_CLOCK_SOURCE = "MP_CLOCK_SOURCE"; //$NON-NLS-1$
	private static final String MP_CKPTDIR = "MP_CKPTDIR"; //$NON-NLS-1$
	private static final String MP_CKPTDIR_PERTASK = "MP_CKPTDIR_PERTASK"; //$NON-NLS-1$
	private static final String MP_CKPTFILE = "MP_CKPTFILE"; //$NON-NLS-1$
	private static final String MP_CMDFILE = "MP_CMDFILE"; //$NON-NLS-1$
	private static final String MP_COREDIR = "MP_COREDIR"; //$NON-NLS-1$
	private static final String MP_COREFILE_FORMAT = "MP_COREFILE_FORMAT"; //$NON-NLS-1$
	private static final String MP_COREFILE_SIGTERM = "MP_COREFILE_SIGTERM"; //$NON-NLS-1$
	private static final String MP_CPU_USE = "MP_CPU_USE"; //$NON-NLS-1$
	private static final String MP_CSS_INTERRUPT = "MP_CSS_INTERRUPT"; //$NON-NLS-1$
	private static final String MP_DEBUG_INITIAL_STOP = "MP_DEBUG_INITIAL_STOP"; //$NON-NLS-1$
	private static final String MP_DEBUG_NOTIMEOUT = "MP_DEBUG_NOTIMEOUT"; //$NON-NLS-1$
	private static final String MP_DEVTYPE = "MP_DEVTYPE"; //$NON-NLS-1$
	private static final String MP_EAGER_LIMIT = "MP_EAGER_LIMIT"; //$NON-NLS-1$
	private static final String MP_EUIDEVELOP = "MP_EUIDEVELOP"; //$NON-NLS-1$
	private static final String MP_EUIDEVICE = "MP_EUIDEVICE"; //$NON-NLS-1$
	private static final String MP_EUILIB = "MP_EUILIB"; //$NON-NLS-1$
	private static final String MP_EUILIBPATH = "MP_EUILIBPATH"; //$NON-NLS-1$
	private static final String MP_HINTS_FILTERED = "MP_HINTS_FILTERED"; //$NON-NLS-1$
	private static final String MP_HOSTFILE = "MP_HOSTFILE"; //$NON-NLS-1$
	private static final String MP_INFOLEVEL = "MP_INFOLEVEL"; //$NON-NLS-1$
	private static final String MP_INSTANCES = "MP_INSTANCES"; //$NON-NLS-1$
	private static final String MP_IO_BUFFER_SIZE = "MP_IO_BUFFER_SIZE"; //$NON-NLS-1$
	private static final String MP_IO_ERRLOG = "MP_IO_ERRLOG"; //$NON-NLS-1$
	private static final String MP_IONODEFILE = "MP_IONODEFILE"; //$NON-NLS-1$
	private static final String MP_LABELIO = "MP_LABELIO"; //$NON-NLS-1$
	private static final String MP_LAPI_TRACE_LEVEL = "MP_LAPI_TRACE_LEVEL"; //$NON-NLS-1$
	private static final String MP_LLFILE = "MP_LLFILE"; //$NON-NLS-1$
	private static final String MP_MSG_API = "MP_MSG_API"; //$NON-NLS-1$
	private static final String MP_MSG_ENVELOPE_BUF = "MP_MSG_ENVELOPE_BUF"; //$NON-NLS-1$
	private static final String MP_NEWJOB = "MP_NEWJOB"; //$NON-NLS-1$
	private static final String MP_NODES = "MP_NODES"; //$NON-NLS-1$
	private static final String MP_PGMMODEL = "MP_PGMMODEL"; //$NON-NLS-1$
	private static final String MP_PMDLOG = "MP_PMDLOG"; //$NON-NLS-1$
	private static final String MP_PMDLOG_DIR = "MP_PMDLOG_DIR"; //$NON-NLS-1$
	private static final String MP_POLLING_INTERVAL = "MP_POLLING_INTERVAL"; //$NON-NLS-1$
	private static final String MP_PRINTENV = "MP_PRINTENV"; //$NON-NLS-1$
	private static final String MP_PRIORITY = "MP_PRIORITY"; //$NON-NLS-1$
	private static final String MP_PRIORITY_LOG = "MP_PRIORITY_LOG"; //$NON-NLS-1$
	private static final String MP_PRIORITY_LOG_DIR = "MP_PRIORITY_LOG_DIR"; //$NON-NLS-1$
	private static final String MP_PRIORITY_LOG_NAME = "MP_PRIORITY_LOG_NAME"; //$NON-NLS-1$
	private static final String MP_PRIORITY_NTP = "MP_PRIORITY_NTP"; //$NON-NLS-1$
	private static final String MP_PROCS = "MP_PROCS"; //$NON-NLS-1$
	private static final String MP_PROFDIR = "MP_PROFDIR"; //$NON-NLS-1$
	private static final String MP_PULSE = "MP_PULSE"; //$NON-NLS-1$
	private static final String MP_REMOTEDIR = "MP_REMOTEDIR"; //$NON-NLS-1$
	private static final String MP_RETRANSMIT_INTERVAL = "MP_RETRANSMIT_INTERVAL"; //$NON-NLS-1$
	private static final String MP_RETRY = "MP_RETRY"; //$NON-NLS-1$
	private static final String MP_RETRY_COUNT = "MP_RETRY_COUNT"; //$NON-NLS-1$
	private static final String MP_REXMIT_BUF_CNT = "MP_REXMIT_BUF_CNT"; //$NON-NLS-1$
	private static final String MP_REXMIT_BUF_SIZE = "MP_REXMIT_BUF_SIZE"; //$NON-NLS-1$
	private static final String MP_RMLIB = "MP_RMLIB"; //$NON-NLS-1$
	private static final String MP_RMPOOL = "MP_RMPOOL"; //$NON-NLS-1$
	private static final String MP_SAVE_LLFILE = "MP_SAVE_LLFILE"; //$NON-NLS-1$
	private static final String MP_SAVEHOSTFILE = "MP_SAVEHOSTFILE"; //$NON-NLS-1$
	private static final String MP_SHARED_MEMORY = "MP_SHARED_MEMORY"; //$NON-NLS-1$
	private static final String MP_SINGLE_THREAD = "MP_SINGLE_THREAD"; //$NON-NLS-1$
	private static final String MP_STATISTICS = "MP_STATISTICS"; //$NON-NLS-1$
	private static final String MP_STDINMODE = "MP_STDINMODE"; //$NON-NLS-1$
	private static final String MP_STDOUTMODE = "MP_STDOUTMODE"; //$NON-NLS-1$
	private static final String MP_TASK_AFFINITY = "MP_TASK_AFFINITY"; //$NON-NLS-1$
	private static final String MP_TASKS_PER_NODE = "MP_TASKS_PER_NODE"; //$NON-NLS-1$
	private static final String MP_THREAD_STACKSIZE = "MP_THREAD_STACKSIZE"; //$NON-NLS-1$
	private static final String MP_TIMEOUT = "MP_TIMEOUT"; //$NON-NLS-1$
	private static final String MP_TLP_REQUIRED = "MP_TLP_REQUIRED"; //$NON-NLS-1$
	private static final String MP_UDP_PACKET_SIZE = "MP_UDP_PACKET_SIZE"; //$NON-NLS-1$
	private static final String MP_USE_BULK_XFER = "MP_USE_BULK_XFER"; //$NON-NLS-1$
	private static final String MP_WAIT_MODE = "MP_WAIT_MODE"; //$NON-NLS-1$
	private static final String PE_ADVANCED_MODE = "PE_ADVANCED_MODE"; //$NON-NLS-1$
	private static final String PE_BUFFER_MEM = "PE_BUFFER_MEM"; //$NON-NLS-1$
	private static final String PE_BUFFER_MEM_MAX = "PE_BUFFER_MEM_MAX"; //$NON-NLS-1$
	private static final String PE_ENV_SCRIPT = "PE_ENV_SCRIPT"; //$NON-NLS-1$
	private static final String PE_RDMA_COUNT = "PE_RDMA_COUNT"; //$NON-NLS-1$
	private static final String PE_RDMA_COUNT_2 = "PE_RDMA_COUNT_2"; //$NON-NLS-1$
	private static final String PE_SPLIT_STDOUT = "PE_SPLIT_STDOUT"; //$NON-NLS-1$
	private static final String PE_STDERR_PATH = "PE_STDERR_PATH"; //$NON-NLS-1$
	private static final String PE_STDIN_PATH = "PE_STDIN_PATH"; //$NON-NLS-1$
	private static final String PE_STDOUT_PATH = "PE_STDOUT_PATH"; //$NON-NLS-1$
	private static final String MP_INSTANCES_INT = "MP_INSTANCES_INT"; //$NON-NLS-1$
	private static final String MP_RETRY_INT = "MP_RETRY_INT"; //$NON-NLS-1$
	/*
	 * End of attribute name list.
	 */
	private static final String ENABLE_STATE = "ENABLE_STATE"; //$NON-NLS-1$
	private static final RMLaunchValidation success = new RMLaunchValidation(true, ""); //$NON-NLS-1$
	private static final int MP_IONODEFILE_SELECTOR = 1;
	private static final int PE_STDIN_PATH_SELECTOR = 2;
	private static final int PE_STDOUT_PATH_SELECTOR = 3;
	private static final int PE_STDERR_PATH_SELECTOR = 4;
	private static final int MP_COREDIR_SELECTOR = 5;
	private static final int MP_CMDFILE_SELECTOR = 6;
	private static final int MP_HOSTFILE_SELECTOR = 7;
	private static final int MP_REMOTEDIR_SELECTOR = 8;
	private static final int MP_LLFILE_SELECTOR = 9;
	private static final int MP_EUILIBPATH_SELECTOR = 10;
	private static final int MP_SAVE_LLFILE_SELECTOR = 11;
	private static final int MP_SAVEHOSTFILE_SELECTOR = 12;
	private static final int MP_RMLIB_SELECTOR = 13;
	private static final int PE_ENV_SCRIPT_SELECTOR = 14;
	private static final int MP_PROFDIR_SELECTOR = 15;
	private static final int MP_PRIORITY_LOG_DIR_SELECTOR = 16;
	@SuppressWarnings("unused")
	private static final int MP_PRIORITY_LOG_NAME_SELECTOR = 17;
	private static final int MP_CKPTFILE_SELECTOR = 18;
	private static final int MP_CKPTDIR_SELECTOR = 19;
	private static final int MP_PMDLOG_DIR_SELECTOR = 20;
	private static final int PE_ADVANCED_MODE_CHECKBOX = 100;
	private static final int KBYTE = 1024;
	private static final int MBYTE = 1024 * 1024;
	private static final int GBYTE = 1024 * 1024 * 1024;
	/*
	 * List of valid PE Environment variables. This list must be kept in sorted
	 * ascending order. MP_FENCE and MP_NOARGLIST must not appear in this list
	 * since they are allowed to be used to control command line parsing.
	 */
	private static final String PEEnvVars[] = { "MP_ACK_THRESH", //$NON-NLS-1$
			"MP_ADAPTER_USE", "MP_BUFFER_MEM", "MP_BUFFER_MEM_MAX", //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
			"MP_BULK_MIN_MSG_SIZE", "MP_CC_SCRATCH_BUF", "MP_CKPTDIR", //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
			"MP_CKPTDIR_PERTASK", "MP_CKPTFILE", "MP_CLOCK_SOURCE", //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
			"MP_CMDFILE", "MP_COREDIR", "MP_COREFILE_FORMAT", //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
			"MP_COREFILE_SIGTERM", "MP_CPU_USE", "MP_CSS_INTERRUPT", //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
			"MP_DEBUG_INITIAL_STOP", "MP_DEBUG_NOTIMEOUT", "MP_DEVTYPE", //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
			"MP_EAGER_LIMIT", "MP_EUIDEVELOP", "MP_EUIDEVICE", "MP_EUILIB", //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$
			"MP_EUILIBPATH", "MP_HINTS_FILTERED", "MP_HOSTFILE", //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
			"MP_INFOLEVEL", "MP_INSTANCES", "MP_INSTANCES_INT", //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
			"MP_IO_BUFFER_SIZE", "MP_IO_ERRLOG", "MP_IONODEFILE", "MP_LABELIO", //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$
			"MP_LAPI_TRACE_LEVEL", "MP_LLFILE", "MP_MSG_API", //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
			"MP_MSG_ENVELOPE_BUF", "MP_NEWJOB", "MP_NODES", "MP_PGMMODEL", //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$
			"MP_PMDLOG", "MP_PMDLOG_DIR", "MP_POLLING_INTERVAL", "MP_PRINTENV", //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$
			"MP_PRIORITY", "MP_PRIORITY_LOG", "MP_PRIORITY_LOG_DIR", //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
			"MP_PRIORITY_LOG_NAME", "MP_PRIORITY_NTP", "MP_PROCS", //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
			"MP_PROFDIR", "MP_PULSE", "MP_RDMA_COUNT", "MP_REMOTEDIR", //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$
			"MP_RETRANSMIT_INTERVAL", "MP_RETRY", "MP_RETRY_COUNT", //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
			"MP_RETRY_INT", "MP_REXMIT_BUF_CNT", "MP_REXMIT_BUF_SIZE", //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
			"MP_RMLIB", "MP_RMPOOL", "MP_SAVEHOSTFILE", "MP_SAVE_LLFILE", //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$
			"MP_SHARED_MEMORY", "MP_SINGLE_THREAD", "MP_STATISTICS", //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
			"MP_STDINMODE", "MP_STDOUTMODE", "MP_TASK_AFFINITY", //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
			"MP_TASKS_PER_NODE", "MP_THREAD_STACKSIZE", "MP_TIMEOUT", //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
			"MP_TLP_REQUIRED", "MP_UDP_PACKET_SIZE", "MP_USE_BULK_XFER", //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
			"MP_WAIT_MODE" }; //$NON-NLS-1$
	/*
	 * List of valid Parallel Environment options. This list must be kept in
	 * sorted ascending order.
	 */
	private static final String PEOptions[] = { "-ack_thresh", "-adapter_use", //$NON-NLS-1$ //$NON-NLS-2$
			"-buffer_mem", "-bulk_min_msg_size", "-cc_scratch_buf", //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
			"-clock_source", "-cmdfile", "-coredir", "-corefile_format", //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$
			"-corefile_sigterm", "-cpu_use", "-css_interrupt", //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
			"-debug_notimeout", "-devtype", "-eager_limit", "-euidevelop", //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$
			"-euidevice", "-euilib", "-euilibpath", "-hfile", //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$
			"-hints_filtered", "-hostfile", "-ilevel", "-infolevel", //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$
			"-instances", "-io_buffer_size", "-io_errlog", "-ionodefile", //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$
			"-labelio", "-llfile", "-msg_api", "-msg_envelope_buf", "-newjob", //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$ //$NON-NLS-5$
			"-nodes", "-pgmmodel", "-pmdlog", "-pmdlog_dir", //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$
			"-polling_interval", "-printenv", "-priority_log", //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
			"-priority_log_dir", "-priority_log_name", "-priority_ntp", //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
			"-procs", "-profdir", "-pulse", "-rdma_count", "-resd", //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$ //$NON-NLS-5$
			"-retransmit_interval", "-retry", "-retrycount", "-rexmit_buf_cnt", //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$
			"-rmpool", "-savehostfile", "-save_llfile", "-shared_memory", //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$
			"-single_thread", "-statistics", "-stdinmode", "-stdoutmode", //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$
			"-task_affinity", "-tasks_per_node", "-thread_stacksize", //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
			"-tlp_required", "-udp_packet_size", "-use_bulk_xfer", "-wait_mode" }; //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$

	private boolean ignoreModifyEvents = false;
	private EventMonitor eventMonitor;
	private Composite mainPanel;
	private final boolean useLoadLeveler = false;
	private TabFolder tabbedPane;
	private ILaunchConfigurationWorkingCopy currentLaunchConfig;
	private PEResourceManager currentRM;
	private CheckboxRowWidget peAdvancedMode;
	private FileSelectorRowWidget peEnvScript;
	private boolean allFieldsValid = true;
	private String errorMessage;
	private Composite tasksTabPane;
	private Composite ioTabPane;
	private Composite diagTabPane;
	private Composite debugTabPane;
	private Composite systemTabPane;
	private Composite nodeTabPane;
	private Composite performanceTab1Pane;
	private Composite performanceTab2Pane;
	private Composite alternateRMTabPane;
	private Composite miscTabPane;
	private Vector<Object> activeWidgets;
	private IRemoteConnection remoteConnection;
	private IRemoteServices remoteService;
	private IRemoteUIServices remoteUIService;
	private Shell parentShell;
	// private RSEFileManager remoteFileManager;
	/*
	 * Widgets for IO tab
	 */
	@SuppressWarnings("unused")
	private ComboRowWidget mpDevType;
	private TextRowWidget mpIOBufferSize;
	private FileSelectorRowWidget mpIONodeFile;
	@SuppressWarnings("unused")
	private BooleanRowWidget mpLabelIO;
	private ComboRowWidget mpStdinMode;
	private ComboRowWidget mpStdoutMode;
	@SuppressWarnings("unused")
	private BooleanRowWidget mpIOErrLog;
	@SuppressWarnings("unused")
	private BooleanRowWidget peSplitStdout;
	private FileSelectorRowWidget peStdinPath;
	private FileSelectorRowWidget peStdoutPath;
	private FileSelectorRowWidget peStderrPath;
	/*
	 * Widgets for diagnostic tab
	 */
	@SuppressWarnings("unused")
	private ComboRowWidget mpInfoLevel;
	@SuppressWarnings("unused")
	private ComboRowWidget mpLAPITraceLevel;
	@SuppressWarnings("unused")
	private BooleanRowWidget mpPMDLog;
	@SuppressWarnings("unused")
	private ComboRowWidget mpPrintEnv;
	private BooleanRowWidget mpPriorityLog;
	@SuppressWarnings("unused")
	private ComboRowWidget mpStatistics;
	private FileSelectorRowWidget mpPmdLogDir;
	/*
	 * Widgets for debug tab
	 */
	private FileSelectorRowWidget mpCoreDir;
	private ComboRowWidget mpCorefileFormat;
	@SuppressWarnings("unused")
	private BooleanRowWidget mpCorefileSigterm;
	@SuppressWarnings("unused")
	private TextRowWidget mpDebugInitialStop;
	@SuppressWarnings("unused")
	private BooleanRowWidget mpDebugNotimeout;
	@SuppressWarnings("unused")
	private ComboRowWidget mpEuiDevelop;
	private FileSelectorRowWidget mpProfDir;
	/*
	 * Widgets for system resources tab
	 */
	private BooleanRowWidget mpAdapterUse;
	private BooleanRowWidget mpCpuUse;
	private ComboRowWidget mpEuiDevice;
	private ComboRowWidget mpInstances;
	private ComboRowWidget mpEuiLib;
	/*
	 * Widgets for node allocation tab
	 */
	private FileSelectorRowWidget mpCmdFile;
	private FileSelectorRowWidget mpHostFile;
	private TextRowWidget mpNodes;
	@SuppressWarnings("unused")
	private ComboRowWidget mpPgmModel;
	private TextRowWidget mpProcs;
	private FileSelectorRowWidget mpRemoteDir;
	private TextRowWidget mpTasksPerNode;
	private FileSelectorRowWidget mpLLFile;
	@SuppressWarnings("unused")
	private BooleanRowWidget mpNewJob;
	private TextRowWidget mpRMPool;
	private ComboRowWidget mpRetry;
	private TextRowWidget mpRetryCount;
	/*
	 * Widgets for performance tabs
	 */
	private TextRowWidget mpAckThresh;
	private DualFieldRowWidget peBufferMem;
	@SuppressWarnings("unused")
	private BooleanRowWidget mpCCScratchBuf;
	@SuppressWarnings("unused")
	private BooleanRowWidget mpCSSInterrupt;
	private TextRowWidget mpEagerLimit;
	private TextRowWidget mpMsgEnvelopeBuf;
	private TextRowWidget mpPollingInterval;
	private TextRowWidget mpPriority;
	private BooleanRowWidget mpPriorityNTP;
	private DualFieldRowWidget peRDMACount;
	private TextRowWidget mpRetransmitInterval;
	private TextRowWidget mpRexmitBufCnt;
	private TextRowWidget mpRexmitBufSize;
	@SuppressWarnings("unused")
	private BooleanRowWidget mpSharedMemory;
	@SuppressWarnings("unused")
	private BooleanRowWidget mpSingleThread;
	private ComboRowWidget mpTaskAffinity;
	private TextRowWidget mpUDPPacketSize;

	@SuppressWarnings("unused")
	private ComboRowWidget mpWaitMode;
	private BooleanRowWidget mpUseBulkXfer;
	private TextRowWidget mpBulkMinMsgSize;
	/*
	 * Widgets for miscellaneous tab
	 */
	@SuppressWarnings("unused")
	private ComboRowWidget mpClockSource;
	private FileSelectorRowWidget mpEuiLibPath;
	@SuppressWarnings("unused")
	private BooleanRowWidget mpHintsFiltered;
	private ComboRowWidget mpMsgApi;
	private TextRowWidget mpPulse;
	private TextRowWidget mpThreadStackSize;
	private TextRowWidget mpTimeout;
	@SuppressWarnings("unused")
	private ComboRowWidget mpTLPRequired;
	private FileSelectorRowWidget mpSaveLLFile;
	private FileSelectorRowWidget mpSaveHostFile;
	private FileSelectorRowWidget mpPriorityLogDir;
	private TextRowWidget mpPriorityLogName;
	private FileSelectorRowWidget mpCkptDir;
	private FileSelectorRowWidget mpCkptFile;
	@SuppressWarnings("unused")
	private BooleanRowWidget mpCkptDirPerTask;
	/*
	 * Widgets for other RM tab
	 */
	private FileSelectorRowWidget mpRMLib;

	/**
	 * Exception class intended for use in validating fields within this panel.
	 * When a validation error occurs, the validation code should create and
	 * throw a ValidationException, which is intended to be caught by the top
	 * level validation method.
	 */
	private class ValidationException extends Exception {
		/**
	 *
	 */
		private static final long serialVersionUID = 1L;

		@SuppressWarnings("unused")
		private ValidationException() {
			throw new IllegalAccessError(Messages.getString("PERMLaunchConfigurationDynamicTab.230")); //$NON-NLS-1$
		}

		/**
		 * Create a ValidationException with error message
		 * 
		 * @param message
		 *            The error message
		 */
		public ValidationException(String message) {
			super(message);
		}
	}

	/**
	 * Internal class which handles events of interest to this panel
	 */
	private class EventMonitor implements ModifyListener, SelectionListener {
		public EventMonitor() {
		}

		public void widgetDefaultSelected(SelectionEvent e) {
		}

		/**
		 * Handle events sent when registered buttons are clicked
		 */
		public void widgetSelected(SelectionEvent e) {
			Object widgetData;

			// Determine which button was clicked based on the data value stored
			// in that button object
			// and handle processing for that button.
			widgetData = e.widget.getData(WidgetAttributes.BUTTON_ID);
			if (widgetData == null) {
				// A widget other than a file selector browse button was
				// clicked. Just call
				// fireContentsChanged to drive enabling the Apply/Revert
				// buttons. Since the widget is supposed
				// to be a button widget, no validation of user data is needed.
				fireContentsChanged();
				if (e.getSource() == mpMsgApi) {
					setMsgApiDependentsState();
				} else if (e.getSource() == mpRetry) {
					setRetryDependentsState();
				}
			} else {
				switch (((Integer) widgetData).intValue()) {
				case MP_IONODEFILE_SELECTOR:
					getInputFile(mpIONodeFile, "File.mpIONodeFileTitle", //$NON-NLS-1$
							"mpIONodeFilePath"); //$NON-NLS-1$
					break;
				case PE_STDIN_PATH_SELECTOR:
					getInputFile(peStdinPath, "File.peStdinPathTitle", //$NON-NLS-1$
							"peStdinPath"); //$NON-NLS-1$
					break;
				case PE_STDOUT_PATH_SELECTOR:
					getOutputFile(peStdoutPath, "File.peStdoutPathTitle", //$NON-NLS-1$
							"peStdoutPath"); //$NON-NLS-1$
					break;
				case PE_STDERR_PATH_SELECTOR:
					getOutputFile(peStderrPath, "File.peStderrPathTitle", //$NON-NLS-1$
							"peStderrPath"); //$NON-NLS-1$
					break;
				case MP_COREDIR_SELECTOR:
					getDirectory(mpCoreDir, "File.mpCoredirTitle", //$NON-NLS-1$
							"mpCoredirPath"); //$NON-NLS-1$
					break;
				case MP_CMDFILE_SELECTOR:
					getInputFile(mpCmdFile, "File.mpCmdfileTitle", //$NON-NLS-1$
							"mpCmdfilePath"); //$NON-NLS-1$
					break;
				case MP_HOSTFILE_SELECTOR:
					getInputFile(mpHostFile, "File.mpHostfileTitle", //$NON-NLS-1$
							"mpHostfilePath"); //$NON-NLS-1$
					break;
				case MP_REMOTEDIR_SELECTOR:
					getInputFile(mpRemoteDir, "File.mpRemotedirTitle", //$NON-NLS-1$
							"mpRemotedirPath"); //$NON-NLS-1$
					break;
				case MP_LLFILE_SELECTOR:
					getInputFile(mpLLFile, "File.mpLLFileTitle", "mpLLFilePath"); //$NON-NLS-1$ //$NON-NLS-2$
					break;
				case MP_EUILIBPATH_SELECTOR:
					getInputFile(mpEuiLibPath, "File.mpEuilibPathTitle", //$NON-NLS-1$
							"mpEuilibPathPath"); //$NON-NLS-1$
					break;
				case MP_SAVE_LLFILE_SELECTOR:
					getOutputFile(mpSaveLLFile, "File.mpSaveLLFileTitle", //$NON-NLS-1$
							"mpSaveLLFilePath"); //$NON-NLS-1$
					break;
				case MP_SAVEHOSTFILE_SELECTOR:
					getOutputFile(mpSaveHostFile, "File.mpSaveHostFileTitle", //$NON-NLS-1$
							"mpSaveHostFilePath"); //$NON-NLS-1$
					break;
				case MP_RMLIB_SELECTOR:
					getInputFile(mpRMLib, "File.peRMLibTitle", "mpRMLibPath"); //$NON-NLS-1$ //$NON-NLS-2$
					break;
				case PE_ENV_SCRIPT_SELECTOR:
					getInputFile(peEnvScript, "File.peEnvScriptTitle", //$NON-NLS-1$
							"peEnvScriptPath"); //$NON-NLS-1$
					break;
				case MP_PROFDIR_SELECTOR:
					getDirectory(mpProfDir, "File.mpProfDirTitle", //$NON-NLS-1$
							"mpProfDirPath"); //$NON-NLS-1$
					break;
				case MP_PRIORITY_LOG_DIR_SELECTOR:
					getDirectory(mpPriorityLogDir, "File.mpPriorityLogDirTitle", //$NON-NLS-1$
							"mpPriorityLogDirPath"); //$NON-NLS-1$
					break;
				case MP_CKPTDIR_SELECTOR:
					getDirectory(mpCkptDir, "File.mpCkptDirTitle", //$NON-NLS-1$
							"mpCkptDirPath"); //$NON-NLS-1$
					break;
				case MP_CKPTFILE_SELECTOR:
					getOutputFile(mpCkptFile, "File.mpCkptFileTitle", //$NON-NLS-1$
							"mpCkptFilePath"); //$NON-NLS-1$
					break;
				case MP_PMDLOG_DIR_SELECTOR:
					getDirectory(mpPmdLogDir, "File.mpPMDLogDirTitle", //$NON-NLS-1$
							"mpPMDLogDirPath"); //$NON-NLS-1$
					break;
				case PE_ADVANCED_MODE_CHECKBOX:
					setLaunchPanelMode();
					validateAllFields();
					break;
				}
			}
		}

		/**
		 * Handle events sent when registered Text and Combo widgets have their
		 * text field modified.
		 */
		public void modifyText(ModifyEvent e) {
			// Text and Combo widgets send ModifyEvents any time their text
			// value is modified, including
			// when the value is modified by a setText() call. The only time
			// ModifyEvents are of interest is
			// when the user has entered text. Code which calls setText() on a
			// widget should set the
			// ignoreModifyEvents before calling setText() and reset
			// ignoreModifyEvents after the call.
			setFieldValidationRequired((Widget) e.getSource());
			if (!ignoreModifyEvents) {
				validateAllFields();
			}
			if (e.getSource() == mpPriority) {
				setPriorityDependentsState();
			} else if (e.getSource() == mpRetry) {
				setRetryDependentsState();
			} else if ((mpLLFile != null) && (mpLLFile.isMatchingWidget((Widget) e.getSource()))) {
				setLLFileDependentState();
			}
		}
	}

	public PERMLaunchConfigurationDynamicTab(IResourceManager rm, ILaunchConfigurationDialog dialog) {
		super(dialog);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.eclipse.ptp.launch.ui.extensions.IRMLaunchConfigurationDynamicTab
	 * #canSave(org.eclipse.swt.widgets.Control,
	 * org.eclipse.ptp.rmsystem.IResourceManager, org.eclipse.ptp.core.IPQueue)
	 */
	public RMLaunchValidation canSave(Control control, IResourceManager rm, IPQueue queue) {
		if (allFieldsValid) {
			return success;
		}
		return new RMLaunchValidation(false, errorMessage);
	}

	/**
	 * Get the directory path from the launch configuration
	 * 
	 * @param attrName
	 *            Launch configuration attribute name for this directory
	 * @return Directory path
	 */
	private String getFileDialogPath(String attrName) {
		String dir;

		dir = ""; //$NON-NLS-1$
		if (currentLaunchConfig != null) {
			try {
				dir = currentLaunchConfig.getAttribute(attrName, ""); //$NON-NLS-1$
			} catch (CoreException e) {
				dir = ""; //$NON-NLS-1$
			}
		}
		return dir;
	}

	/**
	 * Save directory path in the launch configuration
	 * 
	 * @param attrName
	 *            Launch configuration attribute name for this directory
	 * @param path
	 *            Directory path
	 */
	private void saveFileDialogPath(String attrName, String path) {
		if (currentLaunchConfig != null) {
			currentLaunchConfig.setAttribute(attrName, path);
		}
	}

	/**
	 * Display a file selector dialog prompting the user for the path of an
	 * input file. If the user clicks 'open', then set the pathname into the
	 * text field of the specified FileSelector object.
	 * 
	 * @param selector
	 *            The FileSelector object to hold path name
	 * @param titleID
	 *            Title for the dialog
	 * @param pathAttrID
	 *            Launch configuration attribute id for saving path info
	 */
	protected void getInputFile(FileSelectorRowWidget selector, String titleID, String pathAttrID) {
		String selectedFile = null;

		if (remoteUIService != null) {
			File inputFile;
			String inputDir;

			IRemoteUIFileManager fmgr = remoteUIService.getUIFileManager();
			fmgr.setConnection(remoteConnection);
			inputDir = getFileDialogPath(pathAttrID);
			if (inputDir.length() == 0) {
				inputDir = remoteConnection.getWorkingDirectory();
			} else {
				inputFile = new File(inputDir);
				inputDir = inputFile.getParent();
				if (inputDir == null) {
					inputDir = remoteConnection.getWorkingDirectory();
				}
			}
			selectedFile = fmgr.browseFile(parentShell, Messages.getString(titleID), inputDir, 0);
		}
		if (selectedFile != null) {
			saveFileDialogPath(pathAttrID, selectedFile);
			selector.setPath(selectedFile);
			selector.setFocus();
		}
	}

	/**
	 * Display a file selector dialog prompting the user for the path of an
	 * output file. If the user clicks 'save', then set the pathname into the
	 * text field of the specified FileSelector object.
	 * 
	 * @param selector
	 *            The FileSelector object to hold path name
	 * @param titleID
	 *            Title for the dialog
	 * @param pathAttrID
	 *            Launch configuration attribute id for saving path info
	 */
	protected void getOutputFile(FileSelectorRowWidget selector, String titleID, String pathAttrID) {
		String selectedFile = null;

		if (remoteUIService != null) {
			String outputPath;
			File outputDir;

			IRemoteUIFileManager fmgr = remoteUIService.getUIFileManager();
			fmgr.setConnection(remoteConnection);
			outputPath = getFileDialogPath(pathAttrID);
			if (outputPath.length() == 0) {
				outputPath = remoteConnection.getWorkingDirectory();
			} else {
				outputDir = new File(outputPath);
				outputPath = outputDir.getParent();
				if (outputPath == null) {
					outputPath = remoteConnection.getWorkingDirectory();
				}
			}
			selectedFile = fmgr.browseFile(parentShell, Messages.getString(titleID), outputPath, 0);
		}
		if (selectedFile != null) {
			saveFileDialogPath(pathAttrID, selectedFile);
			selector.setPath(selectedFile);
			selector.setFocus();
		}
	}

	/**
	 * Display a directory selector dialog prompting the user for the pathname
	 * of a directory. If the user clocks 'ok', then set the pathname into the
	 * text field of the specified FileSelector.
	 * 
	 * @param selector
	 *            FileSelector object to be updated
	 * @param titleID
	 *            Title for the dialog
	 * @param pathAttrID
	 *            Launch configuration attribute id for saving path info
	 */
	protected void getDirectory(FileSelectorRowWidget selector, String titleID, String pathAttrID) {
		String selectedFile = null;

		if (remoteUIService != null) {
			String dirName;

			IRemoteUIFileManager fmgr = remoteUIService.getUIFileManager();
			fmgr.setConnection(remoteConnection);
			dirName = getFileDialogPath(pathAttrID);
			if (dirName.length() == 0) {
				dirName = remoteConnection.getWorkingDirectory();
			}
			selectedFile = fmgr.browseDirectory(parentShell, Messages.getString(titleID), dirName, 0).toString();
		}
		if (selectedFile != null) {
			String parentDir;

			parentDir = new File(selectedFile).getParent();
			if (parentDir == null) {
				saveFileDialogPath(pathAttrID, "/"); //$NON-NLS-1$
			} else {
				saveFileDialogPath(pathAttrID, parentDir);
			}
			selector.setPath(selectedFile);
			selector.setFocus();
		}
	}

	/**
	 * Mark the validation state for the specified widget to indicate that the
	 * widget value must be validated.
	 * 
	 * @param source
	 *            The widget to validate.
	 */
	protected void setFieldValidationRequired(Widget source) {
		// Iterate thru the list of widgets looking for the widget which needs
		// to be validated. When found, set
		// that widget's validation state to indicate validation is needed.
		// Widget class needs
		// to be checked since although these widgets perform similar functions,
		// they do not comprise a set of
		// logically related widgets that can be easily organized in a class
		// hierarchy.
		Iterator<Object> i;

		i = activeWidgets.iterator();
		while (i.hasNext()) {
			Object widget;

			widget = i.next();
			if (widget instanceof BooleanRowWidget) {
				if (((BooleanRowWidget) widget).isMatchingWidget(source)) {
					((BooleanRowWidget) widget).setValidationRequired();
					return;
				}
			} else if (widget instanceof CheckboxRowWidget) {
				if (((CheckboxRowWidget) widget).isMatchingWidget(source)) {
					((CheckboxRowWidget) widget).setValidationRequired();
					return;
				}
			} else if (widget instanceof ComboRowWidget) {
				if (((ComboRowWidget) widget).isMatchingWidget(source)) {
					((ComboRowWidget) widget).setValidationRequired();
					return;
				}
			} else if (widget instanceof DualFieldRowWidget) {
				if (((DualFieldRowWidget) widget).isMatchingWidget(source)) {
					((DualFieldRowWidget) widget).setValidationRequired();
					return;
				}
			} else if (widget instanceof FileSelectorRowWidget) {
				if (((FileSelectorRowWidget) widget).isMatchingWidget(source)) {
					((FileSelectorRowWidget) widget).setValidationRequired();
					return;
				}
			} else if (widget instanceof TextRowWidget) {
				if (((TextRowWidget) widget).isMatchingWidget(source)) {
					((TextRowWidget) widget).setValidationRequired();
					return;
				}
			}
		}
	}

	/**
	 * Mark all widget's validation state to indicate that the widget value has
	 * changed, meaning validation is required.
	 */
	private void markAllFieldsChanged() {
		Iterator<Object> i;

		i = activeWidgets.iterator();
		while (i.hasNext()) {
			Object widget;

			widget = i.next();
			if (widget instanceof BooleanRowWidget) {
				((BooleanRowWidget) widget).setValidationRequired();
			} else if (widget instanceof CheckboxRowWidget) {
				((CheckboxRowWidget) widget).setValidationRequired();
			} else if (widget instanceof ComboRowWidget) {
				((ComboRowWidget) widget).setValidationRequired();
			} else if (widget instanceof DualFieldRowWidget) {
				((DualFieldRowWidget) widget).setValidationRequired();
			} else if (widget instanceof FileSelectorRowWidget) {
				((FileSelectorRowWidget) widget).setValidationRequired();
			} else if (widget instanceof TextRowWidget) {
				((TextRowWidget) widget).setValidationRequired();
			}
		}
	}

	/**
	 * Disable the tab pane widget and all children of the tab pane. Calling
	 * setEnabled(false) on the tab pane widget disables the tab pane and
	 * prevents interaction with child widgets, but does not change the visible
	 * state of the child widget. This method changes the state of all widgets
	 * to correctly indicate they are disabled.
	 * 
	 * @param widget
	 *            The widget to be disabled.
	 */
	private void disableTabPaneWidget(Control widget) {
		Control children[];

		// For any Composite widget, recursively call this method for each child
		// of the Composite. This must be
		// done before disabling the Composite since disabling the Composite
		// also marks its children disabled
		// and the real enable/disable state of the child cannot be preserved.
		if (widget instanceof Composite) {
			children = ((Composite) widget).getChildren();
			for (int i = 0; i < children.length; i++) {
				disableTabPaneWidget(children[i]);
			}
		}
		// Remember the current state of the widget, then disable it.
		widget.setData(ENABLE_STATE, Boolean.valueOf(widget.isEnabled()));
		widget.setEnabled(false);
	}

	/**
	 * Restore widget back to its previous enable/disable state
	 * 
	 * @param widget
	 *            The widget whose state is to be restored.
	 */
	private void restoreTabPaneWidgetState(Control widget) {
		Control children[];
		Boolean state;
		boolean enableFlag;

		// Get widget's previous enable/disable state. If there is no saved
		// state, such as when initially
		// creating the parallel tab in basic mode, then enable the widget.
		state = (Boolean) widget.getData(ENABLE_STATE);
		if (state == null) {
			enableFlag = true;
		} else {
			enableFlag = state.booleanValue();
		}
		widget.setEnabled(enableFlag);
		// Recursively call this method to handle children of a Composite
		// widget. Note that ordering of processing
		// here does not matter since enabling a Composite widget does not
		// automatically enable its children.
		if (widget instanceof Composite) {
			children = ((Composite) widget).getChildren();
			for (int i = 0; i < children.length; i++) {
				restoreTabPaneWidgetState(children[i]);
			}
		}
	}

	/**
	 * Set launch panel mode based on peAdvancedMode setting. If checked, then
	 * set advanced mode, where the user supplies a PE setup script. Otherwise
	 * set basic mode, where the user chooses PE options from a tabbed dialog
	 * panel.
	 */
	protected void setLaunchPanelMode() {
		if (peAdvancedMode != null) {
			if (peAdvancedMode.getSelection()) {
				peEnvScript.setEnabled(true);
				disableTabPaneWidget(tabbedPane);
			} else {
				peEnvScript.setEnabled(false);
				restoreTabPaneWidgetState(tabbedPane);
			}
		}
	}

	/**
	 * Create a text widget in the tabbed view. The text field spans columns 2
	 * and 3 of the tabbed pane. The label and tooltip text are obtained from
	 * the attribute definition object.
	 * 
	 * @param parent
	 *            Parent widget (the pane in the tabbed view)
	 * @param id
	 *            Attribute id for rm attribute this widget represents
	 * @return TextRowWidget entry widget
	 */
	private TextRowWidget createTextWidget(Composite parent, String id) {
		TextRowWidget widget;
		IAttributeDefinition<?, ?, ?> attr;

		widget = null;
		attr = currentRM.getAttributeDefinitionManager().getAttributeDefinition(id);
		if (attr != null) {
			widget = new TextRowWidget(parent, id, attr);
			widget.addModifyListener(eventMonitor);
			activeWidgets.add(widget);
		}
		return widget;
	}

	/**
	 * Create a text widget in the tabbed view. The text field spans columns 2
	 * and 3 of the tabbed pane. The label and tooltip text are obtained from
	 * the attribute definition object.
	 * 
	 * @param parent
	 *            Parent widget (the pane in the tabbed view)
	 * @param id1
	 *            Attribute id for first rm attribute this widget represents
	 * @param id2
	 *            Attribute id for second rm attribute this widget represents
	 * @return Text entry widget
	 */
	private DualFieldRowWidget createDualField(Composite parent, String id1, String id2) {
		DualFieldRowWidget widget;
		IAttributeDefinition<?, ?, ?> attr1;
		IAttributeDefinition<?, ?, ?> attr2;

		widget = null;
		attr1 = currentRM.getAttributeDefinitionManager().getAttributeDefinition(id1);
		attr2 = currentRM.getAttributeDefinitionManager().getAttributeDefinition(id2);
		if ((attr1 != null) && (attr2 != null)) {
			widget = new DualFieldRowWidget(parent, id1, id2, attr1, attr2);
			widget.addModifyListener(eventMonitor);
			activeWidgets.add(widget);
		}
		return widget;
	}

	/**
	 * Create a checkbox widget in the tabbed view. The checkbox is in column 2
	 * and column 3 is a filler (Label) widget. To ensure consistent alignment,
	 * this method allocates extra horizontal space to the 2nd column. The label
	 * and tooltip text are obtained from the attribute definition object.
	 * 
	 * @param parent
	 *            Parent widget (the pane in the tabbed view)
	 * @param id
	 *            Attribute id for rm attribute this widget represents
	 * @return Checkbox button for this attribute
	 */
	private CheckboxRowWidget createCheckbox(Composite parent, String id) {
		CheckboxRowWidget widget;
		StringAttributeDefinition attrDef;

		widget = null;
		attrDef = (StringAttributeDefinition) currentRM.getAttributeDefinitionManager().getAttributeDefinition(id);
		if (attrDef != null) {
			widget = new CheckboxRowWidget(parent, id, attrDef);
			activeWidgets.add(widget);
		}
		return widget;
	}

	/**
	 * Create a radio button pair in the tabbed view. The label, button labels,
	 * and tooltip text are obtained from the attribute definition object.
	 * 
	 * @param parent
	 *            Parent widget (the pane in the tabbed view)
	 * @param id
	 *            Attribute id for rm attribute this widget represents
	 * @return Checkbox button for this attribute
	 */
	private BooleanRowWidget createBooleanOption(Composite parent, String id) {
		BooleanRowWidget widget;
		StringSetAttributeDefinition attrDef;

		widget = null;
		attrDef = (StringSetAttributeDefinition) currentRM.getAttributeDefinitionManager().getAttributeDefinition(id);
		if (attrDef != null) {
			widget = new BooleanRowWidget(parent, id, attrDef, -1);
			widget.addSelectionListener(eventMonitor);
			activeWidgets.add(widget);
		}
		return widget;
	}

	/**
	 * Create a text field and pushbutton in this row. The text field is in
	 * column 2 and the pushbutton in column 3. The user either fills in the
	 * text field with a pathname, or clicks the button to pop up a file
	 * selector dialog that then fills in the text field. To ensure consistent
	 * alignment, this method allocates extra horizontal space to the 2nd
	 * column. The label and tooltip text are obtained from the attribute
	 * definition object.
	 * 
	 * @param parent
	 *            Parent widget (the pane in the tabbed view)
	 * @param id
	 *            Attribute id for rm attribute this widget represents
	 * @param selectorID
	 *            Identifier used to identify the browse button associated with
	 *            this widget
	 * @return Text entry field for this attribute
	 */
	private FileSelectorRowWidget createFileSelector(Composite parent, String id, int selectorID) {
		FileSelectorRowWidget widget;
		StringAttributeDefinition attr;

		widget = null;
		attr = (StringAttributeDefinition) currentRM.getAttributeDefinitionManager().getAttributeDefinition(id);
		if (attr != null) {
			widget = new FileSelectorRowWidget(parent, id, selectorID, attr);
			widget.setData(id);
			widget.addModifyListener(eventMonitor);
			widget.addSelectionListener(eventMonitor);
			activeWidgets.add(widget);
		}
		return widget;
	}

	/**
	 * Create a combobox widget in the tabbed view. The widget spans columns 2
	 * and 3 of the tabbed pane. The label and tooltip text are obtained from
	 * the attribute definition object.
	 * 
	 * @param parent
	 *            Parent widget (the pane in the tabbed view)
	 * @param id
	 *            Attribute id for rm attribute this widget represents
	 * @return ComboRowWidget used by this attribute
	 */
	private ComboRowWidget createCombobox(Composite parent, String id) {
		ComboRowWidget widget;
		IAttributeDefinition<?, ?, ?> attr;

		widget = null;
		attr = currentRM.getAttributeDefinitionManager().getAttributeDefinition(id);
		if (attr != null) {
			widget = new ComboRowWidget(parent, id, attr, true);
			widget.addSelectionListener(eventMonitor);
			activeWidgets.add(widget);
		}
		return widget;
	}

	/**
	 * Create an editable combobox in the tabbed view. The widget spans columns
	 * 2 and 3 of the tabbed pane. The label and tooltip text are obtained from
	 * the attribute definition object.
	 * 
	 * @param parent
	 *            Parent widget (the pane in the tabbed view)
	 * @param id
	 *            Attribute id for rm attribute this widget represents
	 * @return Editable ComboRowWidget used by this attribute
	 */
	private ComboRowWidget createEditableCombobox(Composite parent, String id) {
		ComboRowWidget widget;
		IAttributeDefinition<?, ?, ?> attr;

		widget = null;
		attr = currentRM.getAttributeDefinitionManager().getAttributeDefinition(id);
		if (attr != null) {
			widget = new ComboRowWidget(parent, id, attr, false);
			widget.addSelectionListener(eventMonitor);
			widget.addModifyListener(eventMonitor);
			activeWidgets.add(widget);
		}
		return widget;
	}

	/**
	 * Reset all widgets within this pane to null as part of panel
	 * initialization. Depending on OS and operation mode (with or without
	 * LoadLeveler), some widgets will not appear on the panels, where the set
	 * of attribute definitions sent by the proxy determines that set. New
	 * widgets will be generated only when a corresponding attribute definition
	 * is sent by the proxy. Any code which accesses a widget should ensure the
	 * widget is not null before accessing the widget object.
	 */
	private void clearAllWidgets() {
		mpIOBufferSize = null;
		mpIONodeFile = null;
		mpLabelIO = null;
		mpDevType = null;
		mpPriorityLogDir = null;
		mpPriorityLogName = null;
		mpStdinMode = null;
		mpStdoutMode = null;
		mpIOErrLog = null;
		peSplitStdout = null;
		peStdinPath = null;
		peStdoutPath = null;
		peStderrPath = null;
		mpInfoLevel = null;
		mpLAPITraceLevel = null;
		mpPMDLog = null;
		mpPrintEnv = null;
		mpPriorityLog = null;
		mpStatistics = null;
		mpCoreDir = null;
		mpCorefileFormat = null;
		mpCorefileSigterm = null;
		mpDebugInitialStop = null;
		mpDebugNotimeout = null;
		mpEuiDevelop = null;
		mpProfDir = null;
		mpAdapterUse = null;
		mpCpuUse = null;
		mpEuiDevice = null;
		mpInstances = null;
		mpEuiLib = null;
		mpCmdFile = null;
		mpHostFile = null;
		mpNodes = null;
		mpPgmModel = null;
		mpProcs = null;
		mpRemoteDir = null;
		mpTasksPerNode = null;
		mpLLFile = null;
		mpNewJob = null;
		mpRMPool = null;
		mpRetry = null;
		mpRetryCount = null;
		mpAckThresh = null;
		peBufferMem = null;
		mpCCScratchBuf = null;
		mpCSSInterrupt = null;
		mpEagerLimit = null;
		mpMsgEnvelopeBuf = null;
		mpPollingInterval = null;
		mpPriority = null;
		mpPriorityNTP = null;
		peRDMACount = null;
		mpRetransmitInterval = null;
		mpRexmitBufCnt = null;
		mpRexmitBufSize = null;
		mpSharedMemory = null;
		mpSingleThread = null;
		mpTaskAffinity = null;
		mpUDPPacketSize = null;
		mpWaitMode = null;
		mpUseBulkXfer = null;
		mpBulkMinMsgSize = null;
		mpClockSource = null;
		mpEuiLibPath = null;
		mpHintsFiltered = null;
		mpMsgApi = null;
		mpPulse = null;
		mpThreadStackSize = null;
		mpTimeout = null;
		mpTLPRequired = null;
		mpSaveLLFile = null;
		mpSaveHostFile = null;
		mpRMLib = null;
		mpCkptDir = null;
		mpCkptFile = null;
		mpCkptDirPerTask = null;
		mpPmdLogDir = null;
	}

	/**
	 * Set enable state for widgets which are dependent on the setting for
	 * mpPriority.
	 */
	private void setPriorityDependentsState() {
		if ((mpPriority.getValue().length() == 0) || (mpPriority.getValue().equals(getDefaultAttributeValue(MP_PRIORITY)))) {
			if (mpPriorityLog != null) {
				mpPriorityLog.setEnabled(false);
			}
			if (mpPriorityNTP != null) {
				mpPriorityNTP.setEnabled(false);
			}
			if (mpPriorityLogDir != null) {
				mpPriorityLogDir.setEnabled(false);
			}
			if (mpPriorityLogName != null) {
				mpPriorityLogName.setEnabled(false);
			}
		} else {
			if (mpPriorityLog != null) {
				mpPriorityLog.setEnabled(true);
			}
			if (mpPriorityNTP != null) {
				mpPriorityNTP.setEnabled(true);
			}
			if (mpPriorityLogDir != null) {
				mpPriorityLogDir.setEnabled(true);
			}
			if (mpPriorityLogName != null) {
				mpPriorityLogName.setEnabled(true);
			}
		}
	}

	/**
	 * Set enable state for widgets which are dependent on the setting for
	 * mpMsgApi
	 */
	private void setMsgApiDependentsState() {
		if (mpMsgApi.getValue().equals("MPI,LAPI")) { //$NON-NLS-1$
			if (peRDMACount != null) {
				peRDMACount.setEnabled(true);
			}
		} else {
			if (peRDMACount != null) {
				peRDMACount.setEnabled(false);
			}
		}
	}

	/**
	 * Set enable state for MP_RETRY_COUNT, where it is not enabled if MP_RETRY
	 * = 'wait' and enabled otherwise.
	 */
	private void setRetryDependentsState() {
		if (mpRetryCount != null) {
			if (mpRetry.getValue().equals("wait")) { //$NON-NLS-1$
				mpRetryCount.setEnabled(true);
			} else {
				mpRetryCount.setEnabled(false);
			}
		}
	}

	/**
	 * Set enable state for widgets dependent on MP_LLFILE setting
	 */
	private void setLLFileDependentState() {
		boolean enableState;

		if ((mpLLFile != null) && (mpLLFile.getValue().length() > 0)) {
			enableState = false;
		} else {
			enableState = true;
		}
		if ((mpMsgApi != null) && (!mpMsgApi.getValue().equals("MPI,LAPI"))) { //$NON-NLS-1$
			if (peRDMACount != null) {
				peRDMACount.setEnabled(enableState);
			}
		} else {
			if (peRDMACount != null) {
				peRDMACount.setEnabled(false);
			}
		}
		if (mpAdapterUse != null) {
			mpAdapterUse.setEnabled(false);
		}
		if (mpCpuUse != null) {
			mpCpuUse.setEnabled(enableState);
		}
		if (mpEuiDevice != null) {
			mpEuiDevice.setEnabled(enableState);
		}
		if (mpEuiLib != null) {
			mpEuiLib.setEnabled(enableState);
		}
		if (mpInstances != null) {
			mpInstances.setEnabled(enableState);
		}
		if (mpNodes != null) {
			mpNodes.setEnabled(enableState);
		}
		if (mpProcs != null) {
			mpProcs.setEnabled(enableState);
		}
		if (mpRMPool != null) {
			mpRMPool.setEnabled(enableState);
		}
		if (mpTasksPerNode != null) {
			mpTasksPerNode.setEnabled(enableState);
		}
		if (mpTaskAffinity != null) {
			mpTaskAffinity.setEnabled(enableState);
		}
		if (mpUseBulkXfer != null) {
			mpUseBulkXfer.setEnabled(enableState);
		}
		if (mpSaveLLFile != null) {
			mpSaveLLFile.setEnabled(enableState);
		}
		if (mpSaveHostFile != null) {
			mpSaveHostFile.setEnabled(enableState);
		}
	}

	/**
	 * Create the layout object for a pane in the TabFolder
	 * 
	 * @return Layout for use in the tabbed pane
	 */
	private Layout createTabPaneLayout() {
		GridLayout layout;

		layout = new GridLayout(4, false);
		layout.marginWidth = 4;
		layout.horizontalSpacing = 8;
		layout.verticalSpacing = 4;
		return layout;
	}

	/**
	 * Create the tasks tab of the attributes pane
	 * 
	 */
	private void createTasksTab() {
		TabItem tab;

		tab = new TabItem(tabbedPane, SWT.NONE);
		tasksTabPane = new Composite(tabbedPane, SWT.NONE);
		tab.setControl(tasksTabPane);
		tab.setText(Messages.getString("TasksTab.title")); //$NON-NLS-1$
		tasksTabPane.setLayout(createTabPaneLayout());
		mpHostFile = createFileSelector(tasksTabPane, MP_HOSTFILE, MP_HOSTFILE_SELECTOR);
		mpProcs = createTextWidget(tasksTabPane, MP_PROCS);
		mpNodes = createTextWidget(tasksTabPane, MP_NODES);
		mpTasksPerNode = createTextWidget(tasksTabPane, MP_TASKS_PER_NODE);
	}

	/**
	 * Create the I/O tab of the attributes pane
	 * 
	 */
	private void createIOTab() {
		TabItem tab;

		tab = new TabItem(tabbedPane, SWT.NONE);
		ioTabPane = new Composite(tabbedPane, SWT.NONE);
		tab.setControl(ioTabPane);
		tab.setText(Messages.getString("IOTab.title")); //$NON-NLS-1$
		ioTabPane.setLayout(createTabPaneLayout());
		mpStdinMode = createEditableCombobox(ioTabPane, MP_STDINMODE);
		mpStdoutMode = createEditableCombobox(ioTabPane, MP_STDOUTMODE);
		mpLabelIO = createBooleanOption(ioTabPane, MP_LABELIO);
		peSplitStdout = createBooleanOption(ioTabPane, PE_SPLIT_STDOUT);
	}

	/**
	 * Create the diagnostics tab of the attributes pane
	 * 
	 */
	private void createDiagnosticTab() {
		TabItem tab;

		tab = new TabItem(tabbedPane, SWT.NONE);
		diagTabPane = new Composite(tabbedPane, SWT.NONE);
		tab.setControl(diagTabPane);
		tab.setText(Messages.getString("DIAGTab.title")); //$NON-NLS-1$
		diagTabPane.setLayout(createTabPaneLayout());
		mpPmdLogDir = createFileSelector(diagTabPane, MP_PMDLOG_DIR, MP_PMDLOG_DIR_SELECTOR);
		mpInfoLevel = createCombobox(diagTabPane, MP_INFOLEVEL);
		mpPrintEnv = createEditableCombobox(diagTabPane, MP_PRINTENV);
		mpPMDLog = createBooleanOption(diagTabPane, MP_PMDLOG);
		mpPriorityLog = createBooleanOption(diagTabPane, MP_PRIORITY_LOG);
		mpStatistics = createCombobox(diagTabPane, MP_STATISTICS);
	}

	/**
	 * Create the debug tab of the attributes pane
	 * 
	 */
	private void createDebugTab() {
		TabItem tab;

		tab = new TabItem(tabbedPane, SWT.NONE);
		debugTabPane = new Composite(tabbedPane, SWT.NONE);
		tab.setControl(debugTabPane);
		tab.setText(Messages.getString("DEBUGTab.title")); //$NON-NLS-1$
		debugTabPane.setLayout(createTabPaneLayout());
		mpEuiDevelop = createCombobox(debugTabPane, MP_EUIDEVELOP);
		mpCorefileFormat = createEditableCombobox(debugTabPane, MP_COREFILE_FORMAT);
		mpCoreDir = createFileSelector(debugTabPane, MP_COREDIR, MP_COREDIR_SELECTOR);
		mpProfDir = createFileSelector(debugTabPane, MP_PROFDIR, MP_PROFDIR_SELECTOR);
		mpDebugInitialStop = createTextWidget(debugTabPane, MP_DEBUG_INITIAL_STOP);
		mpDebugNotimeout = createBooleanOption(debugTabPane, MP_DEBUG_NOTIMEOUT);
		mpCorefileSigterm = createBooleanOption(debugTabPane, MP_COREFILE_SIGTERM);
	}

	/**
	 * Create the system resources tab of the attributes pane
	 * 
	 */
	private void createSystemTab() {
		TabItem tab;

		tab = new TabItem(tabbedPane, SWT.NONE);
		systemTabPane = new Composite(tabbedPane, SWT.NONE);
		tab.setControl(systemTabPane);
		tab.setText(Messages.getString("SYSTab.title")); //$NON-NLS-1$
		systemTabPane.setLayout(createTabPaneLayout());
		mpEuiDevice = createCombobox(systemTabPane, MP_EUIDEVICE);
		mpEuiLib = createCombobox(systemTabPane, MP_EUILIB);
		mpInstances = createEditableCombobox(systemTabPane, MP_INSTANCES);
		mpAdapterUse = createBooleanOption(systemTabPane, MP_ADAPTER_USE);
		mpCpuUse = createBooleanOption(systemTabPane, MP_CPU_USE);
	}

	/**
	 * Create the node allocation tab of the attributes pane
	 * 
	 */
	private void createNodeAllocationTab() {
		TabItem tab;

		tab = new TabItem(tabbedPane, SWT.NONE);
		nodeTabPane = new Composite(tabbedPane, SWT.NONE);
		tab.setControl(nodeTabPane);
		tab.setText(Messages.getString("NODETab.title")); //$NON-NLS-1$
		nodeTabPane.setLayout(createTabPaneLayout());
		mpCmdFile = createFileSelector(nodeTabPane, MP_CMDFILE, MP_CMDFILE_SELECTOR);
		mpRemoteDir = createFileSelector(nodeTabPane, MP_REMOTEDIR, MP_REMOTEDIR_SELECTOR);
		mpLLFile = createFileSelector(nodeTabPane, MP_LLFILE, MP_LLFILE_SELECTOR);
		mpRMPool = createTextWidget(nodeTabPane, MP_RMPOOL);
		mpRetryCount = createTextWidget(nodeTabPane, MP_RETRY_COUNT);
		mpPgmModel = createCombobox(nodeTabPane, MP_PGMMODEL);
		mpRetry = createEditableCombobox(nodeTabPane, MP_RETRY);
		mpNewJob = createBooleanOption(nodeTabPane, MP_NEWJOB);
		if (mpLLFile != null) {
			mpLLFile.addModifyListener(eventMonitor);
		}
	}

	/**
	 * Create the first performance tab of the attributes pane. Due to the
	 * number of performance related attributes, there are two performance tabs.
	 */
	private void createPerformanceTab1() {
		TabItem tab;

		tab = new TabItem(tabbedPane, SWT.NONE);
		performanceTab1Pane = new Composite(tabbedPane, SWT.NONE);
		tab.setControl(performanceTab1Pane);
		tab.setText(Messages.getString("PERFTab1.title")); //$NON-NLS-1$
		performanceTab1Pane.setLayout(createTabPaneLayout());
		mpAckThresh = createTextWidget(performanceTab1Pane, MP_ACK_THRESH);
		mpPollingInterval = createTextWidget(performanceTab1Pane, MP_POLLING_INTERVAL);
		mpPriority = createTextWidget(performanceTab1Pane, MP_PRIORITY);
		mpBulkMinMsgSize = createTextWidget(performanceTab1Pane, MP_BULK_MIN_MSG_SIZE);
		mpUDPPacketSize = createTextWidget(performanceTab1Pane, MP_UDP_PACKET_SIZE);
		peRDMACount = createDualField(performanceTab1Pane, PE_RDMA_COUNT, PE_RDMA_COUNT_2);
		mpWaitMode = createCombobox(performanceTab1Pane, MP_WAIT_MODE);
		mpPriorityNTP = createBooleanOption(performanceTab1Pane, MP_PRIORITY_NTP);
		mpCCScratchBuf = createBooleanOption(performanceTab1Pane, MP_CC_SCRATCH_BUF);
		mpCSSInterrupt = createBooleanOption(performanceTab1Pane, MP_CSS_INTERRUPT);
		mpUseBulkXfer = createBooleanOption(performanceTab1Pane, MP_USE_BULK_XFER);
	}

	/**
	 * Create the second performance tab for the attributes pane
	 * 
	 */
	private void createPerformanceTab2() {
		TabItem tab;

		tab = new TabItem(tabbedPane, SWT.NONE);
		performanceTab2Pane = new Composite(tabbedPane, SWT.NONE);
		tab.setControl(performanceTab2Pane);
		tab.setText(Messages.getString("PERFTab2.title")); //$NON-NLS-1$
		performanceTab2Pane.setLayout(createTabPaneLayout());
		peBufferMem = createDualField(performanceTab2Pane, PE_BUFFER_MEM, PE_BUFFER_MEM_MAX);
		mpMsgEnvelopeBuf = createTextWidget(performanceTab2Pane, MP_MSG_ENVELOPE_BUF);
		mpEagerLimit = createTextWidget(performanceTab2Pane, MP_EAGER_LIMIT);
		mpRetransmitInterval = createTextWidget(performanceTab2Pane, MP_RETRANSMIT_INTERVAL);
		mpRexmitBufCnt = createTextWidget(performanceTab2Pane, MP_REXMIT_BUF_CNT);
		mpRexmitBufSize = createTextWidget(performanceTab2Pane, MP_REXMIT_BUF_SIZE);
		mpTaskAffinity = createEditableCombobox(performanceTab2Pane, MP_TASK_AFFINITY);
		mpSharedMemory = createBooleanOption(performanceTab2Pane, MP_SHARED_MEMORY);
		mpSingleThread = createBooleanOption(performanceTab2Pane, MP_SINGLE_THREAD);
	}

	/**
	 * Create the miscellaneous tab of the attributes pane
	 * 
	 */
	private void createMiscellaneousTab() {
		TabItem tab;

		tab = new TabItem(tabbedPane, SWT.NONE);
		miscTabPane = new Composite(tabbedPane, SWT.NONE);
		tab.setControl(miscTabPane);
		tab.setText(Messages.getString("MISCTab.title")); //$NON-NLS-1$
		miscTabPane.setLayout(createTabPaneLayout());
		mpClockSource = createCombobox(miscTabPane, MP_CLOCK_SOURCE);
		mpMsgApi = createCombobox(miscTabPane, MP_MSG_API);
		mpTLPRequired = createCombobox(miscTabPane, MP_TLP_REQUIRED);
		mpDevType = createCombobox(miscTabPane, MP_DEVTYPE);
		mpLAPITraceLevel = createCombobox(miscTabPane, MP_LAPI_TRACE_LEVEL);
		mpEuiLibPath = createFileSelector(miscTabPane, MP_EUILIBPATH, MP_EUILIBPATH_SELECTOR);
		mpSaveLLFile = createFileSelector(miscTabPane, MP_SAVE_LLFILE, MP_SAVE_LLFILE_SELECTOR);
		mpSaveHostFile = createFileSelector(miscTabPane, MP_SAVEHOSTFILE, MP_SAVEHOSTFILE_SELECTOR);
		mpPriorityLogDir = createFileSelector(miscTabPane, MP_PRIORITY_LOG_DIR, MP_PRIORITY_LOG_DIR_SELECTOR);
		mpPriorityLogName = createTextWidget(miscTabPane, MP_PRIORITY_LOG_NAME);
		mpCkptDir = createFileSelector(miscTabPane, MP_CKPTDIR, MP_CKPTDIR_SELECTOR);
		mpCkptFile = createFileSelector(miscTabPane, MP_CKPTFILE, MP_CKPTFILE_SELECTOR);
		mpIONodeFile = createFileSelector(miscTabPane, MP_IONODEFILE, MP_IONODEFILE_SELECTOR);
		mpPulse = createTextWidget(miscTabPane, MP_PULSE);
		mpThreadStackSize = createTextWidget(miscTabPane, MP_THREAD_STACKSIZE);
		mpTimeout = createTextWidget(miscTabPane, MP_TIMEOUT);
		mpIOBufferSize = createTextWidget(miscTabPane, MP_IO_BUFFER_SIZE);
		mpHintsFiltered = createBooleanOption(miscTabPane, MP_HINTS_FILTERED);
		mpIOErrLog = createBooleanOption(miscTabPane, MP_IO_ERRLOG);
		mpCkptDirPerTask = createBooleanOption(miscTabPane, MP_CKPTDIR_PERTASK);
	}

	/**
	 * Create the alternate resource manager tab of the resources pane
	 * 
	 */
	private void createOtherRMTab() {
		TabItem tab;

		tab = new TabItem(tabbedPane, SWT.NONE);
		alternateRMTabPane = new Composite(tabbedPane, SWT.NONE);
		tab.setControl(alternateRMTabPane);
		tab.setText(Messages.getString("RMTab.title")); //$NON-NLS-1$
		alternateRMTabPane.setLayout(createTabPaneLayout());
		mpRMLib = createFileSelector(alternateRMTabPane, MP_RMLIB, MP_RMLIB_SELECTOR);
	}

	/**
	 * Create a pane containing the advanced mode checkbox and PE setup script
	 * name
	 * 
	 */
	private void createModeBox() {
		GridData gd;
		GridLayout layout;
		Composite pane;

		pane = new Composite(mainPanel, SWT.NONE);
		layout = new GridLayout(4, false);
		layout.marginWidth = 4;
		layout.horizontalSpacing = 8;
		layout.verticalSpacing = 4;
		pane.setLayout(layout);
		gd = new GridData(GridData.FILL_HORIZONTAL);
		gd.grabExcessHorizontalSpace = true;
		pane.setLayoutData(gd);
		peAdvancedMode = createCheckbox(pane, PE_ADVANCED_MODE);
		if (peAdvancedMode != null) {
			peAdvancedMode.setData(WidgetAttributes.BUTTON_ID, new Integer(PE_ADVANCED_MODE_CHECKBOX));
			peAdvancedMode.addSelectionListener(eventMonitor);
		}
		peEnvScript = createFileSelector(pane, PE_ENV_SCRIPT, PE_ENV_SCRIPT_SELECTOR);
	}

	/**
	 * Create a pane containing the file selectors for stdio redirection
	 * 
	 */
	private void createRedirectBox() {
		GridData gd;
		GridLayout layout;
		Composite pane;

		pane = new Composite(mainPanel, SWT.NONE);
		layout = new GridLayout(4, false);
		layout.marginWidth = 8;
		layout.horizontalSpacing = 8;
		layout.verticalSpacing = 8;
		pane.setLayout(layout);
		gd = new GridData(GridData.FILL_HORIZONTAL);
		gd.grabExcessHorizontalSpace = true;
		pane.setLayoutData(gd);
		peStdinPath = createFileSelector(pane, PE_STDIN_PATH, PE_STDIN_PATH_SELECTOR);
		peStdoutPath = createFileSelector(pane, PE_STDOUT_PATH, PE_STDOUT_PATH_SELECTOR);
		peStderrPath = createFileSelector(pane, PE_STDERR_PATH, PE_STDERR_PATH_SELECTOR);
	}

	/**
	 * This method creates all of the GUI elements of the resource-manager
	 * specific pane within the parallel tab of the launch configuration dialog.
	 * 
	 * @param parent
	 *            This control's parent
	 * @param rm
	 *            The resource manager associated with this launch configuration
	 * @param queue
	 *            Currently selected queue
	 */
	public void createControl(Composite parent, IResourceManager rm, IPQueue queue) {
		IPEResourceManagerConfiguration config;
		IRemoteConnectionManager connMgr;
		currentRM = (PEResourceManager) rm;

		config = (IPEResourceManagerConfiguration) ((AbstractResourceManager) rm).getConfiguration();
		if (config != null) {
			remoteService = PTPRemoteUIPlugin.getDefault().getRemoteServices(config.getRemoteServicesId(),
					getLaunchConfigurationDialog());
			if (remoteService != null) {
				remoteUIService = PTPRemoteUIPlugin.getDefault().getRemoteUIServices(remoteService);
				connMgr = remoteService.getConnectionManager();
				if (connMgr != null) {
					remoteConnection = connMgr.getConnection(config.getConnectionName());
				}
			}
		}
		parentShell = parent.getShell();
		clearAllWidgets();
		activeWidgets = new Vector<Object>();
		eventMonitor = new EventMonitor();
		mainPanel = new Composite(parent, SWT.NONE);
		mainPanel.setLayout(new GridLayout(1, false));
		createModeBox();
		createRedirectBox();
		tabbedPane = new TabFolder(mainPanel, SWT.TOP);
		createTasksTab();
		createIOTab();
		createDiagnosticTab();
		createDebugTab();
		createSystemTab();
		createNodeAllocationTab();
		createPerformanceTab1();
		createPerformanceTab2();
		if (!useLoadLeveler) {
			createOtherRMTab();
		}
		createMiscellaneousTab();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.eclipse.ptp.launch.ui.extensions.IRMLaunchConfigurationDynamicTab
	 * #getControl()
	 */
	public Control getControl() {
		return mainPanel;
	}

	/**
	 * Get the default value for an attribute from the resource manager, giving
	 * preference to a user override of the default value (which the user does
	 * by setting of the corresponding environment variable before starting the
	 * proxy.) The user's override is passed to the front end by the proxy as a
	 * string attribute where the leading 'MP_' of the attribute name is
	 * replaced with 'EN_'
	 * 
	 * @param attrName
	 *            The name of the attribute
	 * @return The value of the attribute
	 */
	private String getAttrLocalDefaultValue(String attrName) {
		IAttributeDefinition<?, ?, ?> attrDef;
		String localDefaultEnv;

		localDefaultEnv = attrName.replaceFirst("^MP_", "EN_"); //$NON-NLS-1$ //$NON-NLS-2$
		attrDef = currentRM.getAttributeDefinitionManager().getAttributeDefinition(localDefaultEnv);
		if (attrDef != null) {
			try {
				return attrDef.create().getValueAsString();
			} catch (IllegalValueException e) {
			}
		}
		attrDef = currentRM.getAttributeDefinitionManager().getAttributeDefinition(attrName);
		if (attrDef != null) {
			try {
				return attrDef.create().getValueAsString();
			} catch (IllegalValueException e) {
				return ""; //$NON-NLS-1$
			}
		}
		return ""; //$NON-NLS-1$
	}

	/**
	 * Get the attribute value for the specified attribute. If the value is
	 * stored in the launch configuration, that value is used. Otherwise the
	 * default value from the resource manager is used.
	 * 
	 * @param config
	 *            The current launch configuration
	 * @param attrName
	 *            The name of the attribute
	 * @return The value of the attribute
	 */
	private String getAttrInitialValue(ILaunchConfiguration config, String attrName) {
		String value;
		IAttributeDefinition<?, ?, ?> rmAttrDef;

		try {
			value = config.getAttribute(attrName, "_no_value_"); //$NON-NLS-1$
		} catch (CoreException e) {
			value = "_no_value_"; //$NON-NLS-1$
		}
		if (value.equals("_no_value_")) { //$NON-NLS-1$
			// Get the default attribute value, where that default may be the
			// value
			// specified by the user as an override to the PE default value.
			value = getAttrLocalDefaultValue(attrName);
		}
		// If an attribute is defined as an integer attribute, then determine if
		// the attribute is evenly divisible by 1G, 1M or 1K, and if so, then
		// convert the
		// value accordingly. The tests must be done largest to smallest so that
		// the largest conversion factor is used. The attribute value may
		// already be
		// in the form 999[gGmMkK]. Converting that to a long will result in a
		// NumberFormatException, so a try/catch block is required, where the
		// string
		// value is returned in thecase of a NumberFormatException
		rmAttrDef = currentRM.getAttributeDefinitionManager().getAttributeDefinition(attrName);
		if (rmAttrDef instanceof IntegerAttributeDefinition || rmAttrDef instanceof BigIntegerAttributeDefinition) {
			long intVal;

			try {
				intVal = Long.valueOf(value);
				if (intVal != 0) {
					if ((intVal % GBYTE) == 0) {
						return String.valueOf(intVal / GBYTE) + "G"; //$NON-NLS-1$
					} else {
						if ((intVal % MBYTE) == 0) {
							return String.valueOf(intVal / MBYTE) + "M"; //$NON-NLS-1$
						} else {
							if ((intVal % KBYTE) == 0) {
								return String.valueOf(intVal / KBYTE) + "K"; //$NON-NLS-1$
							}
						}
					}
				}
			} catch (NumberFormatException e) {
				return value;
			}
		}
		return value;
	}

	/**
	 * Set checkbox to checked state if attribute has value equal to checkValue
	 * otherwise set it unchecked
	 * 
	 * @param checkbox
	 *            The checkbox to set
	 * @param attrValue
	 *            The attribute value to check
	 * @param checkValue
	 *            The value corresponding to a checked checkbox
	 */
	private void setValue(CheckboxRowWidget checkbox, String attrValue, String checkValue) {
		if (checkbox != null) {
			if (attrValue.equals(checkValue)) {
				checkbox.setSelection(true);
			} else {
				checkbox.setSelection(false);
			}
		}
	}

	private void setValue(BooleanRowWidget option, String checkValue) {
		if (option != null) {
			option.setValue(checkValue);
		}
	}

	/**
	 * Set the text value for a Text widget to the specified value if the widget
	 * is not null.
	 * 
	 * @param widget
	 *            The widget to set
	 * @param value
	 *            The value to be set
	 */
	private void setValue(TextRowWidget widget, String value) {
		if (widget != null) {
			widget.setValue(value);
		}
	}

	/**
	 * Set the text value for a DualField widget to the specified value if the
	 * widget is not null.
	 * 
	 * @param widget
	 *            The widget to set
	 * @param value1
	 *            The value to be set in field 1
	 * @param value2
	 *            The value to be set in field 2
	 */
	private void setValue(DualFieldRowWidget widget, String value1, String value2) {
		if (widget != null) {
			widget.setValue(value1, value2);
		}
	}

	/**
	 * Set the text value for a ComboRowWidget to the specified value if the
	 * widget is not null.
	 * 
	 * @param widget
	 *            The widget to set
	 * @param value
	 *            The value to be set
	 */
	private void setValue(ComboRowWidget widget, String value) {
		if (widget != null) {
			widget.setValue(value);
		}
	}

	/**
	 * Set the pathname for a file selector if the file selector is not null
	 * 
	 * @param selector
	 *            File selector to be updated
	 * @param path
	 *            Pathname
	 */
	private void setValue(FileSelectorRowWidget selector, String path) {
		if (selector != null) {
			selector.setPath(path);
		}
	}

	/**
	 * Set initial values for all widgets
	 * 
	 * @param configuration
	 *            The current launch configuration
	 */
	private void setInitialValues(ILaunchConfiguration config) {
		Object widget;
		Iterator<Object> i;

		// All Text and Combo widgets have ModifyListeners registered on them in
		// order to invoke field validation
		// when contents of the widget's text field change. The ModifyListener
		// is invoked for each widget as a
		// result of calling the setText() method for the widget. The resulting
		// ModifyEvent results in calling
		// validateAllFields(), which invokesfireContentsChanged(), which in
		// turn results in performApply() being
		// invoked. The performApply() method saves all widget's values into the
		// current launch configuration.
		// Since at the time that setInitialValues is called, widgets may be
		// blank, this results in storing
		// blanks for all attributes in the launch configuration, wiping out the
		// saved values in the launch
		// configuration.
		// To avoid this, ignoreModifyEvents is set, so that the ModifyListener
		// does nothing.
		ignoreModifyEvents = true;
		i = activeWidgets.iterator();
		while (i.hasNext()) {
			widget = i.next();
			if (widget instanceof FileSelectorRowWidget) {
				setValue((FileSelectorRowWidget) widget,
						getAttrInitialValue(config, (String) ((FileSelectorRowWidget) widget).getData()));
			} else if (widget instanceof DualFieldRowWidget) {
				setValue((DualFieldRowWidget) widget,
						getAttrInitialValue(config, (String) ((DualFieldRowWidget) widget).getData1()),
						getAttrInitialValue(config, (String) ((DualFieldRowWidget) widget).getData2()));
			} else if (widget instanceof TextRowWidget) {
				setValue((TextRowWidget) widget,
						getAttrInitialValue(config, ((TextRowWidget) widget).getData(WidgetAttributes.ATTR_NAME)));
			} else if (widget instanceof ComboRowWidget) {
				setValue((ComboRowWidget) widget,
						getAttrInitialValue(config, ((ComboRowWidget) widget).getData(WidgetAttributes.ATTR_NAME)));
			} else if (widget instanceof BooleanRowWidget) {
				setValue((BooleanRowWidget) widget, getAttrInitialValue(config, ((BooleanRowWidget) widget).getData()));
			} else if (widget instanceof CheckboxRowWidget) {
				setValue((CheckboxRowWidget) widget,
						getAttrInitialValue(config, ((CheckboxRowWidget) widget).getData(WidgetAttributes.ATTR_NAME)), "yes"); //$NON-NLS-1$
			}

		}
		setLaunchPanelMode();
		// Setup complete, re-enable ModifyListener
		ignoreModifyEvents = false;
		markAllFieldsChanged();
		// All fields need to be validated because a different resource manager
		// may have been selected, and therefore
		// values saved in the launch configuration, such as pathnames may no
		// longer be valid.
		validateAllFields();
	}

	/**
	 * Set state for widgets based on dependencies between widget values. At the
	 * point this method is called, all widgets are in enabled state, so it is
	 * only necessary to disable widgets.
	 */
	private void setInitialWidgetState() {
		String mpPriorityDefaultValue;
		boolean enableState;

		mpPriorityDefaultValue = getDefaultAttributeValue(MP_PRIORITY);
		if ((mpPriority != null) && ((mpPriority.getValue().length() == 0) || mpPriority.getValue().equals(mpPriorityDefaultValue))) {
			enableState = false;
		} else {
			enableState = true;
		}
		if (mpPriorityLog != null) {
			mpPriorityLog.setEnabled(enableState);
		}
		if (mpPriorityNTP != null) {
			mpPriorityNTP.setEnabled(enableState);
		}
		if (mpPriorityLogDir != null) {
			mpPriorityLogDir.setEnabled(enableState);
		}
		if (mpPriorityLogName != null) {
			mpPriorityLogName.setEnabled(enableState);
		}
		if ((mpRetry != null) && (mpRetry.getValue().equals("wait"))) { //$NON-NLS-1$
			if (mpRetryCount != null) {
				mpRetryCount.setEnabled(false);
			}
		} else {
			if (mpRetryCount != null) {
				mpRetryCount.setEnabled(true);
			}
		}
		setLLFileDependentState();
	}

	/**
	 * Get the default value for an attribute
	 * 
	 * @param rm
	 *            The current resource manager
	 * @param attributeName
	 *            The name of the attribute
	 * @return The default attribute value or empty string if value cannot be
	 *         retrieved
	 */
	private String getDefaultAttributeValue(String attributeName) {
		String defaultValue;

		try {
			IAttributeDefinition<?, ?, ?> def;

			def = currentRM.getAttributeDefinitionManager().getAttributeDefinition(attributeName);
			if (def != null) {
				defaultValue = def.create().getValueAsString();
			} else {
				defaultValue = ""; //$NON-NLS-1$
			}
		} catch (IllegalValueException e) {
			defaultValue = ""; //$NON-NLS-1$
		}
		return defaultValue;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.eclipse.ptp.launch.ui.extensions.IRMLaunchConfigurationDynamicTab
	 * #initializeFrom(org.eclipse.swt.widgets.Control,
	 * org.eclipse.ptp.rmsystem.IResourceManager, org.eclipse.ptp.core.IPQueue,
	 * org.eclipse.debug.core.ILaunchConfiguration)
	 */
	public RMLaunchValidation initializeFrom(Control control, IResourceManager rm, IPQueue queue, ILaunchConfiguration configuration) {
		if (configuration instanceof ILaunchConfigurationWorkingCopy) {
			currentLaunchConfig = (ILaunchConfigurationWorkingCopy) configuration;
		}
		currentRM = (PEResourceManager) rm;
		setInitialValues(configuration);
		setInitialWidgetState();
		return success;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.eclipse.ptp.launch.ui.extensions.IRMLaunchConfigurationDynamicTab
	 * #isValid(org.eclipse.debug.core.ILaunchConfiguration,
	 * org.eclipse.ptp.rmsystem.IResourceManager, org.eclipse.ptp.core.IPQueue)
	 */
	@SuppressWarnings("unchecked")
	public RMLaunchValidation isValid(ILaunchConfiguration configuration, IResourceManager rm, IPQueue queue) {
		// If running in basic mode, then any PE command line options and
		// environment variables are disallowed since those settings may
		// conflict with what is specified in the resources tab panel.
		if ((peAdvancedMode != null) && (!peAdvancedMode.getSelection())) {
			Map<String, String> environment;
			String optionsFence;
			String noPEArgs;
			String commandOptions;
			StringTokenizer tokenizedOptions;

			try {
				Iterator<String> iter;
				environment = configuration.getAttribute("org.eclipse.debug.core.environmentVariables", //$NON-NLS-1$
						new HashMap<String, String>());
				iter = environment.keySet().iterator();
				while (iter.hasNext()) {
					if (Arrays.binarySearch(PEEnvVars, iter.next()) >= 0) {
						return new RMLaunchValidation(false, Messages.getString("Invalid.disallowedEnvVar")); //$NON-NLS-1$
					}
				}
				// If MP_NOARGLIST or MP_FENCE environment variables are set
				// then handle parsing
				// of command line options accordingly.
				noPEArgs = environment.get("MP_NOARGLIST"); //$NON-NLS-1$
				if ((noPEArgs == null) || (noPEArgs.equalsIgnoreCase("no"))) { //$NON-NLS-1$
					optionsFence = environment.get("MP_FENCE"); //$NON-NLS-1$
					if (optionsFence == null) {
						optionsFence = ""; //$NON-NLS-1$
					}
					commandOptions = configuration.getAttribute("org.eclipse.ptp.launch.ARGUMENT_ATTR", ""); //$NON-NLS-1$ //$NON-NLS-2$
					tokenizedOptions = new StringTokenizer(commandOptions, " "); //$NON-NLS-1$
					while (tokenizedOptions.hasMoreTokens()) {
						String option;

						option = tokenizedOptions.nextToken();
						if (option.equals(optionsFence)) {
							break;
						}
						if (Arrays.binarySearch(PEOptions, option) >= 0) {
							return new RMLaunchValidation(false, Messages.getString("Invalid.disallowedOption")); //$NON-NLS-1$
						}
					}
				}
			} catch (CoreException e) {
			}
		}
		if (allFieldsValid) {
			return success;
		}
		return new RMLaunchValidation(false, errorMessage);
	}

	/**
	 * Store the value from a Text widget into the specified launch
	 * configuration if the widget is not null
	 * 
	 * @param config
	 *            The launch configuration
	 * @param attr
	 *            The name of the attribute
	 * @param control
	 *            The widget to obtain the value from
	 */
	private void setConfigAttr(ILaunchConfigurationWorkingCopy config, String attr, TextRowWidget control) {
		IAttributeDefinition<?, ?, ?> attrDef;

		if (control != null) {
			String attrValue;

			attrDef = currentRM.getAttributeDefinitionManager().getAttributeDefinition(attr);
			try {
				if ((attrDef instanceof IntegerAttributeDefinition) || (attrDef instanceof BigIntegerAttributeDefinition)) {
					attrValue = getIntegerValue(control.getValue());
				} else {
					attrValue = control.getValue();
				}
				config.setAttribute(attr, attrValue);
			} catch (NumberFormatException e) {
				// If the field has an invalid numeric value, then don't save it
				// in the launch configuration
			}
		}
	}

	/**
	 * Store the value from a DialField widget into the specified launch
	 * configuration if the widget is not null
	 * 
	 * @param config
	 *            The launch configuration
	 * @param attr
	 *            The name of the attribute
	 * @param control
	 *            The widget to obtain the value from
	 */
	private void setConfigAttr(ILaunchConfigurationWorkingCopy config, String attr1, String attr2, DualFieldRowWidget control) {
		if (control != null) {
			String value[];

			value = control.getValue();
			config.setAttribute(attr1, value[0].trim());
			config.setAttribute(attr2, value[1].trim());
		}
	}

	/**
	 * Store the value from a ComboRowWidget into the specified launch
	 * configuration if the widget is not null
	 * 
	 * @param config
	 *            The launch configuration
	 * @param attr
	 *            The name of the attribute
	 * @param control
	 *            The widget to obtain the value from
	 */
	private void setConfigAttr(ILaunchConfigurationWorkingCopy config, String attr, ComboRowWidget control) {
		if (control != null) {
			config.setAttribute(attr, control.getValue());
		}
	}

	/**
	 * Store the value from a file selector into the specified launch
	 * configuration if the file selector is not null
	 * 
	 * @param config
	 *            The launch configuration
	 * @param attr
	 *            The name of the attribute
	 * @param control
	 *            The widget to obtain the value from
	 */
	private void setConfigAttr(ILaunchConfigurationWorkingCopy config, String attr, FileSelectorRowWidget control) {
		if (control != null) {
			config.setAttribute(attr, control.getValue());
		}
	}

	/**
	 * Store the value from a Button widget into the specified launch
	 * configuration if the widget is not null
	 * 
	 * @param config
	 *            The launch configuration
	 * @param attr
	 *            The name of the attribute
	 * @param control
	 *            The widget to obtain the value from
	 * @param trueVal
	 *            The value to set if the button is selected
	 */
	private void setConfigAttr(ILaunchConfigurationWorkingCopy config, String attr, CheckboxRowWidget control, String trueVal,
			String falseVal) {
		if (control != null) {
			config.setAttribute(attr, (control.getSelection() ? trueVal : falseVal));
		}
	}

	private void setConfigAttr(ILaunchConfigurationWorkingCopy config, String attr, BooleanRowWidget control) {
		if (control != null) {
			config.setAttribute(attr, control.getValue());
		}
	}

	/**
	 * Save the values entered in this panel in the launch configuration
	 * 
	 * @param config
	 */
	private void saveConfigurationData(ILaunchConfigurationWorkingCopy config) {
		Object widget;
		Iterator<Object> i;

		if (activeWidgets != null) {
			i = activeWidgets.iterator();
			while (i.hasNext()) {
				widget = i.next();
				if (widget instanceof TextRowWidget) {
					setConfigAttr(config, ((TextRowWidget) widget).getData(WidgetAttributes.ATTR_NAME), (TextRowWidget) widget);
				} else if (widget instanceof ComboRowWidget) {
					setConfigAttr(config, ((ComboRowWidget) widget).getData(WidgetAttributes.ATTR_NAME), (ComboRowWidget) widget);
				} else if (widget instanceof CheckboxRowWidget) {
					setConfigAttr(config, ((CheckboxRowWidget) widget).getData(WidgetAttributes.ATTR_NAME),
							(CheckboxRowWidget) widget, "yes", "no"); //$NON-NLS-1$ //$NON-NLS-2$
				} else if (widget instanceof BooleanRowWidget) {
					setConfigAttr(config, ((BooleanRowWidget) widget).getData(), (BooleanRowWidget) widget);
				} else if (widget instanceof FileSelectorRowWidget) {
					setConfigAttr(config, (String) ((FileSelectorRowWidget) widget).getData(), (FileSelectorRowWidget) widget);
				} else if (widget instanceof DualFieldRowWidget) {
					setConfigAttr(config, (String) ((DualFieldRowWidget) widget).getData1(),
							(String) ((DualFieldRowWidget) widget).getData2(), (DualFieldRowWidget) widget);
				}
			}
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.eclipse.ptp.launch.ui.extensions.IRMLaunchConfigurationDynamicTab
	 * #performApply(org.eclipse.debug.core.ILaunchConfigurationWorkingCopy,
	 * org.eclipse.ptp.rmsystem.IResourceManager, org.eclipse.ptp.core.IPQueue)
	 */
	public RMLaunchValidation performApply(ILaunchConfigurationWorkingCopy configuration, IResourceManager rm, IPQueue queue) {
		currentLaunchConfig = configuration;
		currentRM = (PEResourceManager) rm;
		saveConfigurationData(configuration);
		return new RMLaunchValidation(true, ""); //$NON-NLS-1$
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.eclipse.ptp.launch.ui.extensions.IRMLaunchConfigurationDynamicTab
	 * #setDefaults(org.eclipse.debug.core.ILaunchConfigurationWorkingCopy,
	 * org.eclipse.ptp.rmsystem.IResourceManager, org.eclipse.ptp.core.IPQueue)
	 */
	public RMLaunchValidation setDefaults(ILaunchConfigurationWorkingCopy config, IResourceManager rmc, IPQueue queue) {
		IAttribute<?, ?, ?> rmAttrs[];

		currentLaunchConfig = config;
		IPResourceManager rm = (IPResourceManager) rmc.getAdapter(IPResourceManager.class);
		rmAttrs = rm.getAttributes();
		for (int i = 0; i < rmAttrs.length; i++) {
			try {
				config.setAttribute(rmAttrs[i].getDefinition().getId(), rmAttrs[i].getDefinition().create().getValueAsString());
			} catch (IllegalValueException e) {
			}
		}
		// setDefaultValues(config, rm);
		return success;
	}

	/**
	 * Handle validation of all fields in the tabbed pane of the launch
	 * configuration's parallel tab.
	 */
	protected void validateAllFields() {
		// This method is the top level driver for validating the fields in the
		// tabbed pane. It is called when a field in the tabbed pane is
		// modified, via a ModifyListener registered on
		// each text or editable combobox widget It calls a validation method
		// for each tab in the pane. Validation
		// should be done in tab order, left to right,
		// All validation is done in the scope of a try block which catches any
		// ValidationException propagated up
		// from lower level methods. The idea is to validate the fields within
		// tab order left to right, then within each
		// tab, from top to bottom. Each validation method will throw a
		// ValidationException if that field fails
		// validation, where the thrown exception will stop further validation
		// of the pane. This structure allows fields
		// to be easily moved to another pane, or to reorder fields within a
		// pane. Using an exception to terminate
		// validation also avoids cluttering the logic of the validation method
		// with deeply nested 'if ... else ...'
		// logic.
		// If all fields are valid, then the allFieldsValid flag is set so that
		// the isValid() and canSave() methods can
		// easily check panel validity.
		// Validation of valid dependencies between fields should be performed
		// as a second step after all fields have
		// been individually validated for correct values.
		try {
			if (peAdvancedMode != null && peAdvancedMode.getSelection()) {
				validateInputPath(peEnvScript, "Invalid.peEnvScript"); //$NON-NLS-1$
				validateRedirectBox();
			} else {
				validateRedirectBox();
				validateTasksTab();
				validateIOTab();
				validateDiagnosticTab();
				validateDebugTab();
				validateSystemTab();
				validateNodeTab();
				validatePerformanceTab1();
				validatePerformanceTab2();
				validateRMTab();
				validateMiscTab();
			}
			allFieldsValid = true;
		} catch (ValidationException e) {
			errorMessage = e.getMessage();
			allFieldsValid = false;
		}
		fireContentsChanged();
	}

	/**
	 * Validate that an integer value is within the range allowed for the
	 * attribute.
	 * 
	 * @param value
	 *            The value to be verified
	 * @param attrName
	 *            The name of the attribute
	 * @param errorID
	 *            The id of the error message used if validation fails
	 * @throws ValidationException
	 *             Indicates that Text widget failed validation
	 */
	private void validateNumericRange(String value, String attrName, String errorID) throws ValidationException {
		int testValue;
		int len;
		char suffix;

		len = value.length();
		suffix = value.charAt(len - 1);
		if (Character.isDigit(suffix)) {
			try {
				testValue = Integer.valueOf(value);
			} catch (NumberFormatException e) {
				throw new ValidationException(Messages.getString(errorID));
			}
		} else {
			try {
				testValue = Integer.valueOf(value.substring(0, len - 1));
				if ((suffix == 'G') || (suffix == 'g')) {
					testValue = testValue * GBYTE;
				} else if ((suffix == 'M') || (suffix == 'm')) {
					testValue = testValue * MBYTE;
				} else if ((suffix == 'K') || (suffix == 'k')) {
					testValue = testValue * KBYTE;
				} else {
					throw new ValidationException(Messages.getString(errorID));
				}
			} catch (NumberFormatException e) {
				throw new ValidationException(Messages.getString(errorID));
			}
		}
		validateNumericRange(testValue, attrName, errorID);
	}

	/**
	 * Validate file selectors used to specify stdio redirection
	 * 
	 * @throws ValidationException
	 */
	private void validateRedirectBox() throws ValidationException {
		validateInputPath(peStdinPath, "Invalid.peStdinPath"); //$NON-NLS-1$
		validateOutputPath(peStdoutPath, "Invalid.peStdoutPath"); //$NON-NLS-1$
		validateOutputPath(peStderrPath, "Invalid.peStderrPath"); //$NON-NLS-1$
	}

	/**
	 * Validate fields in task specification tab
	 * 
	 * @throws ValidationException
	 */
	private void validateTasksTab() throws ValidationException {
		validateInputPath(mpHostFile, "Invalid.mpHostFile"); //$NON-NLS-1$
		validateNumericRange(mpProcs, MP_PROCS, "Invalid.mpProcs"); //$NON-NLS-1$
		validateNumericRange(mpNodes, MP_NODES, "Invalid.mpNodes"); //$NON-NLS-1$
		validateNumericRange(mpTasksPerNode, MP_TASKS_PER_NODE, "Invalid.mpTasksPerNode"); //$NON-NLS-1$
	}

	/**
	 * Validate all text and editable combobox fields in the I/O tab, top to
	 * bottom
	 * 
	 * @throws ValidationException
	 */
	private void validateIOTab() throws ValidationException {
		validateStdinMode();
		validateStdoutMode();
	}

	/**
	 * Validate all text and editable combobox fields in diagnostic tab, top to
	 * bottom
	 * 
	 * @throws ValidationException
	 */
	private void validateDiagnosticTab() throws ValidationException {
	}

	/**
	 * Validate all text and editable combobox fields in the debug tab, top to
	 * bottom
	 * 
	 * @throws ValidationException
	 */
	private void validateDebugTab() throws ValidationException {
		validateCorefileFormat();
		validateDirectory(mpCoreDir, "Invalid.mpCoreDir"); //$NON-NLS-1$
		validateDirectory(mpProfDir, "Invalid.mpProfDir"); //$NON-NLS-1$
	}

	/**
	 * Validate all text and editable combobox fields in the system resources
	 * tab, top to bottom
	 * 
	 * @throws ValidationException
	 */
	private void validateSystemTab() throws ValidationException {
		validateInstances();
	}

	/**
	 * Validate all text and editable combobox fields in the node allocation
	 * tab, top to bottom
	 * 
	 * @throws ValidationException
	 */
	private void validateNodeTab() throws ValidationException {
		validateNumericRange(mpTasksPerNode, MP_TASKS_PER_NODE, "Invalid.mpTasksPerNode"); //$NON-NLS-1$
		validateInputPath(mpRemoteDir, "Invalid.mpRemoteDir"); //$NON-NLS-1$
		validateInputPath(mpCmdFile, "Invalid.mpCmdFile"); //$NON-NLS-1$
		validateInputPath(mpLLFile, "Invalid.mpLLFile"); //$NON-NLS-1$
		validateRetry();
		validateNumericRange(mpRetryCount, MP_RETRY_COUNT, "Invalid.mpRetryCount"); //$NON-NLS-1$
	}

	/**
	 * Validate all text and editable combobox fields in performance tab 1, top
	 * to bottom
	 * 
	 * @throws ValidationException
	 */
	private void validatePerformanceTab1() throws ValidationException {
		validateNumericRange(mpAckThresh, MP_ACK_THRESH, "Invalid.mpAckThresh"); //$NON-NLS-1$
		validateNumericRange(mpPollingInterval, MP_POLLING_INTERVAL, "Invalid.mpPollingInterval"); //$NON-NLS-1$
		validateRDMACount();
		validateNumericRange(mpUDPPacketSize, MP_UDP_PACKET_SIZE, "Invalid.mpUDPPacketSize"); //$NON-NLS-1$
		validateNumericRange(mpBulkMinMsgSize, MP_BULK_MIN_MSG_SIZE, "Invalid.mpBulkMinMsgSize"); //$NON-NLS-1$
	}

	/**
	 * Validate all text and editable combobox fields in performance tab 2, top
	 * to bottom
	 * 
	 * @throws ValidationException
	 */
	private void validatePerformanceTab2() throws ValidationException {
		validateBufferMem();
		validateNumericRange(mpMsgEnvelopeBuf, MP_MSG_ENVELOPE_BUF, "Invalid.mpMsgEnvelopeBuf"); //$NON-NLS-1$
		validateNumericRange(mpEagerLimit, MP_EAGER_LIMIT, "Invalid.mpEagerLimit"); //$NON-NLS-1$
		validateNumericRange(mpRetransmitInterval, MP_RETRANSMIT_INTERVAL, "Invalid.mpRetransmitInterval"); //$NON-NLS-1$
		validateNumericRange(mpRexmitBufCnt, MP_REXMIT_BUF_CNT, "Invalid.mpRexmitBufCnt"); //$NON-NLS-1$
		validateNumericRange(mpRexmitBufSize, MP_REXMIT_BUF_SIZE, "Invalid.mpRexmitBufSize"); //$NON-NLS-1$
	}

	/**
	 * Validate all text and editable combobox fields in the alternate resource
	 * manager tab
	 * 
	 * @throws ValidationException
	 */
	private void validateRMTab() throws ValidationException {
		validateInputPath(mpRMLib, "Invalid.mpRMLib"); //$NON-NLS-1$
	}

	/**
	 * Validate all text fields and editable combo boxes in the miscellaneous
	 * tab
	 * 
	 * @throws ValidationException
	 */
	private void validateMiscTab() throws ValidationException {
		validateInputPath(mpEuiLibPath, "Invalid.mpEuiLibPath"); //$NON-NLS-1$
		validateNumericRange(mpPulse, MP_PULSE, "Invalid.mpPulse"); //$NON-NLS-1$
		validateNumericRange(mpThreadStackSize, MP_THREAD_STACKSIZE, "Invalid.mpThreadStackSize"); //$NON-NLS-1$
		validateNumericRange(mpTimeout, MP_TIMEOUT, "Invalid.mpTimeout"); //$NON-NLS-1$
		validateNumericRange(mpIOBufferSize, MP_IO_BUFFER_SIZE, "Invalid.mpIOBufferSize"); //$NON-NLS-1$
		validateOutputPath(mpSaveLLFile, "Invalid.mpSaveLLFile"); //$NON-NLS-1$
		validateOutputPath(mpSaveHostFile, "Invalid.mpSaveHostFile"); //$NON-NLS-1$
		validateDirectory(mpPriorityLogDir, "Invalid.mpPriorityLogDir"); //$NON-NLS-1$
		validateInputPath(mpIONodeFile, "Invalid.mpIONodeFile"); //$NON-NLS-1$
	}

	/**
	 * Validate that an input file is accessible
	 * 
	 * @param selector
	 *            The file selector containing the pathname
	 * @param errorID
	 *            id of the error string used if file is inaccessible
	 * @throws ValidationException
	 */
	private void validateInputPath(FileSelectorRowWidget selector, String errorID) throws ValidationException {
		String path;

		if ((selector != null) && selector.isEnabled() && selector.isValidationRequired()) {
			path = selector.getValue();
			if (path.length() == 0) {
				selector.resetValidationState();
				return;
			}
			try {
				validateInputPath(path, errorID);
				selector.resetValidationState();
			} catch (ValidationException e) {
				selector.setFieldInError();
				throw e;
			}
		}
	}

	/**
	 * Validate that an output file is accessible
	 * 
	 * @param selector
	 *            The file selector containing the pathname
	 * @param errorID
	 *            id of the error string used if the file is inaccessible
	 * @throws ValidationException
	 */
	private void validateOutputPath(FileSelectorRowWidget selector, String errorID) throws ValidationException {
		String path;

		if ((selector != null) && selector.isEnabled() && selector.isValidationRequired()) {
			path = selector.getValue();
			if (path.length() == 0) {
				selector.resetValidationState();
				return;
			}
			try {
				validateOutputPath(path, errorID);
				selector.resetValidationState();
			} catch (ValidationException e) {
				selector.setFieldInError();
				throw e;
			}
		}
	}

	/**
	 * Validate that in input file is accessible
	 * 
	 * @param path
	 *            Pathname of the input file
	 * @param errorID
	 *            id of the error string used if the file is inaccessible
	 * @throws ValidationException
	 */
	private void validateInputPath(String path, String errorID) throws ValidationException {
		IPath testPath;
		IFileStore remoteResource;
		IFileInfo fileInfo;

		testPath = new Path(path);
		if (!testPath.isValidPath(path)) {
			throw new ValidationException(Messages.getString(errorID));
		}
		if (remoteService == null || remoteConnection == null) {
			throw new ValidationException(Messages.getString("PERMLaunchConfigurationDynamicTab.0")); //$NON-NLS-1$
		}
		remoteResource = remoteService.getFileManager(remoteConnection).getResource(testPath.toString());
		fileInfo = remoteResource.fetchInfo();
		if ((!fileInfo.exists()) || (fileInfo.isDirectory())) {
			throw new ValidationException(Messages.getString(errorID));
		}
	}

	/**
	 * Validate that an output file is accessible
	 * 
	 * @param path
	 *            Pathname of the output file
	 * @param errorID
	 *            id of the error string used if the file is not accessible
	 * @throws ValidationException
	 */
	private void validateOutputPath(String path, String errorID) throws ValidationException {
		IPath testPath;
		IFileStore remoteResource;
		IFileInfo fileInfo;

		testPath = new Path(path);
		if (!testPath.isValidPath(path)) {
			throw new ValidationException(Messages.getString(errorID));
		}
		if (remoteService == null || remoteConnection == null) {
			throw new ValidationException(Messages.getString("PERMLaunchConfigurationDynamicTab.0")); //$NON-NLS-1$
		}
		remoteResource = remoteService.getFileManager(remoteConnection).getResource(testPath.toString());
		fileInfo = remoteResource.fetchInfo();
		if (fileInfo.isDirectory()) {
			throw new ValidationException(Messages.getString(errorID));
		}
	}

	/**
	 * Validate that the directory pathname is valid
	 * 
	 * @param selector
	 *            File selector containing the directory name
	 * @param errorID
	 *            id of the error string used if the directory is invalid
	 * @throws ValidationException
	 */
	private void validateDirectory(FileSelectorRowWidget selector, String errorID) throws ValidationException {
		String path;
		IPath testPath;
		IFileStore remoteResource;
		IFileInfo fileInfo;

		if ((selector != null) && selector.isEnabled() && selector.isValidationRequired()) {
			path = selector.getValue();
			try {
				if (path.length() == 0) {
					selector.resetValidationState();
					return;
				}

				testPath = new Path(path);
				if (!testPath.isValidPath(path)) {
					throw new ValidationException(Messages.getString(errorID));
				}
				if (remoteService == null || remoteConnection == null) {
					throw new ValidationException(Messages.getString("PERMLaunchConfigurationDynamicTab.0")); //$NON-NLS-1$
				}
				remoteResource = remoteService.getFileManager(remoteConnection).getResource(testPath.toString());
				fileInfo = remoteResource.fetchInfo();
				if (!fileInfo.isDirectory()) {
					throw new ValidationException(Messages.getString(errorID));
				}
				selector.resetValidationState();
			} catch (ValidationException e) {
				selector.setFieldInError();
				throw e;
			}
		}
	}

	/**
	 * Validate the MP_STDINMODE setting
	 * 
	 * @throws ValidationException
	 */
	private void validateStdinMode() throws ValidationException {
		String widgetValue;
		int numProcs;

		if ((mpStdinMode != null) && (mpStdinMode.isEnabled())) {
			widgetValue = mpStdinMode.getValue();
			if (widgetValue.length() == 0) {
				return;
			}
			if (isValidListSelection(mpStdinMode, MP_STDINMODE)) {
				return;
			}
			try {
				numProcs = Integer.valueOf(mpProcs.getValue());
			} catch (NumberFormatException e) {
				throw new ValidationException(Messages.getString("Invalid.mpStdinMode")); //$NON-NLS-1$
			}
			validateNumericRange(widgetValue, 0, numProcs, "Invalid.mpStdinMode"); //$NON-NLS-1$
		}
	}

	/**
	 * Validate the MP_STDOUTMODE setting
	 * 
	 * @throws ValidationException
	 */
	private void validateStdoutMode() throws ValidationException {
		String widgetValue;
		int numProcs;

		if ((mpStdoutMode != null) && (mpStdoutMode.isEnabled())) {
			widgetValue = mpStdoutMode.getValue();
			if (widgetValue.length() == 0) {
				return;
			}
			if (isValidListSelection(mpStdoutMode, MP_STDOUTMODE)) {
				return;
			}
			try {
				numProcs = Integer.valueOf(mpProcs.getValue());
			} catch (NumberFormatException e) {
				throw new ValidationException(Messages.getString("Invalid.mpStdoutMode")); //$NON-NLS-1$
			}
			validateNumericRange(widgetValue, 0, numProcs - 1, "Invalid.mpStdoutMode"); //$NON-NLS-1$
		}
	}

	private void validateCorefileFormat() throws ValidationException {

		if ((mpCorefileFormat != null) && mpCorefileFormat.isEnabled() && mpCorefileFormat.isValidationRequired()) {
			String widgetValue;
			widgetValue = mpCorefileFormat.getValue();
			try {
				if (widgetValue.length() == 0) {
					return;
				}
				if (isValidListSelection(mpCorefileFormat, MP_COREFILE_FORMAT)) {
					return;
				}
				validateOutputPath(widgetValue, "Invalid.mpCorefileFormat"); //$NON-NLS-1$
				mpCorefileFormat.resetValidationState();
			} catch (ValidationException e) {
				mpCorefileFormat.setFieldInError();
				throw e;
			}
		}
	}

	/**
	 * Validate the MP_INSTANCES setting
	 * 
	 * @throws ValidationException
	 */
	private void validateInstances() throws ValidationException {
		if ((mpInstances != null) && (mpInstances.isEnabled())) {
			String widgetValue;

			widgetValue = mpInstances.getValue();
			if (widgetValue.length() == 0) {
				return;
			}
			if (isValidListSelection(mpInstances, MP_INSTANCES)) {
				return;
			}
			validateNumericRange(widgetValue, MP_INSTANCES_INT, "Invalid.mpInstances"); //$NON-NLS-1$
		}
	}

	/**
	 * Validate the MP_RETRY setting
	 * 
	 * @throws ValidationException
	 */
	private void validateRetry() throws ValidationException {
		if ((mpRetry != null) && (mpRetry.isEnabled())) {
			String widgetValue;

			widgetValue = mpRetry.getValue();
			if (widgetValue.length() == 0) {
				return;
			}
			if (isValidListSelection(mpRetry, MP_RETRY)) {
				return;
			}
			validateNumericRange(widgetValue, MP_RETRY_INT, "Invalid.mpRetry"); //$NON-NLS-1$
		}
	}

	/**
	 * Validate the MP_RDMA_COUNT setting
	 * 
	 * @throws ValidationException
	 */
	private void validateRDMACount() throws ValidationException {
		if ((peRDMACount != null) && (peRDMACount.isEnabled())) {
			String widgetValue[];

			widgetValue = peRDMACount.getValue();
			if ((widgetValue[0].length() == 0) && (widgetValue[1].length() > 0)) {
				throw new ValidationException(Messages.getString("Invalid.peRDMACountPair")); //$NON-NLS-1$
			}
			if (widgetValue[0].length() > 0) {
				validateNumericRange(widgetValue[0], PE_BUFFER_MEM, "Invalid.peRDMACount"); //$NON-NLS-1$
			}
			if (widgetValue[1].length() > 0) {
				validateLongNumericRange(widgetValue[1], PE_BUFFER_MEM_MAX, "Invalid.peRDMACount"); //$NON-NLS-1$
			}
		}
	}

	/**
	 * Validate the MP_BUFFER_MEM setting
	 * 
	 * @throws ValidationException
	 */
	private void validateBufferMem() throws ValidationException {
		if (peBufferMem != null && peBufferMem.isEnabled() && peBufferMem.isValidationRequired()) {
			String widgetValue[];

			widgetValue = peBufferMem.getValue();
			try {
				if (widgetValue[0].length() > 0) {
					validateNumericRange(widgetValue[0], PE_BUFFER_MEM, "Invalid.peBufferMem"); //$NON-NLS-1$
				}
				if (widgetValue[1].length() > 0) {
					validateLongNumericRange(widgetValue[1], PE_BUFFER_MEM_MAX, "Invalid.peBufferMem2"); //$NON-NLS-1$
				}
				peBufferMem.resetValidationState();
			} catch (ValidationException e) {
				peBufferMem.setFieldInError();
				throw e;
			}
		}
	}

	/**
	 * Verify that the value selected or entered in an editable combobox is a
	 * valid value, as determined by checking the attribute definition for the
	 * attribute.
	 * 
	 * @param widget
	 *            The combobox to be checked
	 * @param attrName
	 *            The attribute name
	 * @return true if the value is a valid selection, false otherwise
	 */
	private boolean isValidListSelection(ComboRowWidget widget, String attrName) {
		StringSetAttributeDefinition attrDef;
		@SuppressWarnings("unused")
		StringSetAttribute attr;

		attrDef = (StringSetAttributeDefinition) currentRM.getAttributeDefinitionManager().getAttributeDefinition(attrName);
		if (attrDef != null) {
			try {
				attr = attrDef.create(widget.getValue());
				return true;
			} catch (IllegalValueException e) {
				return false;
			}
		}
		return true;
	}

	/**
	 * Validate that an integer value is within the range allowed for the
	 * attribute.
	 * 
	 * @param control
	 *            The Text widget to be verified
	 * @param attrName
	 *            The name of the attribute
	 * @param errorID
	 *            The id of the error message used if validation fails
	 * @throws ValidationException
	 *             Indicates that Text widget failed validation
	 */
	private void validateNumericRange(TextRowWidget control, String attrName, String errorID) throws ValidationException {
		String value;

		if ((control != null) && control.isEnabled() && control.isValidationRequired()) {
			value = control.getValue();
			if (value.length() > 0) {
				try {
					validateNumericRange(value, attrName, errorID);
				} catch (ValidationException e) {
					control.setFieldInError();
					throw e;
				}
				control.resetValidationState();
			}
		}
	}

	/**
	 * Validate that an integer value is within the range allowed for the
	 * attribute.
	 * 
	 * @param value
	 *            The value to be verified
	 * @param attrName
	 *            The name of the attribute
	 * @param errorID
	 *            The id of the error message used if validation fails
	 * @throws ValidationException
	 *             Indicates that Text widget failed validation
	 */
	private void validateNumericRange(int value, String attrName, String errorID) throws ValidationException {
		IntegerAttributeDefinition attrDef;
		@SuppressWarnings("unused")
		IntegerAttribute attr;

		attrDef = (IntegerAttributeDefinition) currentRM.getAttributeDefinitionManager().getAttributeDefinition(attrName);
		try {
			attr = attrDef.create(value);
		} catch (IllegalValueException e) {
			throw new ValidationException(Messages.getString(errorID));
		}
	}

	/**
	 * Convert a string which may have a suffix 'k', 'm' or 'g' to it's actual
	 * numeric value, multiplying by the appropriate multiplier
	 * 
	 * @param value
	 *            The number to be converted
	 * @return The converted number
	 */
	private String getIntegerValue(String value) {
		int testValue;
		int len;
		char suffix;

		testValue = 0;
		len = value.length();
		if (len == 0) {
			return ""; //$NON-NLS-1$
		} else {
			suffix = value.charAt(len - 1);
			if (Character.isDigit(suffix)) {
				return value;
			} else {
				if (len >= 2) {
					testValue = Integer.valueOf(value.substring(0, len - 1));
					if ((suffix == 'G') || (suffix == 'g')) {
						testValue = testValue * GBYTE;
					} else if ((suffix == 'M') || (suffix == 'm')) {
						testValue = testValue * MBYTE;
					} else if ((suffix == 'K') || (suffix == 'k')) {
						testValue = testValue * KBYTE;
					} else {
						return ""; //$NON-NLS-1$
					}
				}
			}
			return String.valueOf(testValue);
		}
	}

	/**
	 * Validate that a BigInteger value is within the range allowed for the
	 * attribute.
	 * 
	 * @param value
	 *            The value to be verified
	 * @param attrName
	 *            The name of the attribute
	 * @param errorID
	 *            The id of the error message used if validation fails
	 * @throws ValidationException
	 *             Indicates that Text widget failed validation
	 */
	private void validateLongNumericRange(String value, String attrName, String errorID) throws ValidationException {
		BigIntegerAttributeDefinition attrDef;
		@SuppressWarnings("unused")
		BigIntegerAttribute attr;

		attrDef = (BigIntegerAttributeDefinition) currentRM.getAttributeDefinitionManager().getAttributeDefinition(attrName);
		try {
			attr = attrDef.create(value);
		} catch (IllegalValueException e) {
			throw new ValidationException(Messages.getString(errorID));
		}
	}

	/**
	 * Validate a String's value to verify it is within the allowed range
	 * 
	 * @param value
	 *            String to be verified
	 * @param lowLimit
	 *            Low limit of range
	 * @param highLimit
	 *            High limit of range
	 * @param errorID
	 *            id of the error message used if value is not in allowable
	 *            range
	 * @throws ValidationException
	 */
	private void validateNumericRange(String value, int lowLimit, int highLimit, String errorID) throws ValidationException {
		int n;

		try {
			n = Integer.valueOf(value);
			if ((n < lowLimit) || (n > highLimit)) {
				throw new ValidationException(Messages.getString(errorID));
			}
		} catch (NumberFormatException e) {
			throw new ValidationException(Messages.getString(errorID));
		}
	}
}
