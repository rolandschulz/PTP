package org.eclipse.ptp.rm.jaxb.core.runnable;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.UUID;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.SubMonitor;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.ptp.rm.jaxb.core.IJAXBNonNLSConstants;
import org.eclipse.ptp.rm.jaxb.core.JAXBCorePlugin;
import org.eclipse.ptp.rm.jaxb.core.data.Arglist;
import org.eclipse.ptp.rm.jaxb.core.data.ArglistImpl;
import org.eclipse.ptp.rm.jaxb.core.data.DirectiveDefinition;
import org.eclipse.ptp.rm.jaxb.core.data.DirectiveDefinitions;
import org.eclipse.ptp.rm.jaxb.core.data.EnvironmentDefinition;
import org.eclipse.ptp.rm.jaxb.core.data.EnvironmentDefinitions;
import org.eclipse.ptp.rm.jaxb.core.data.ExecuteCommand;
import org.eclipse.ptp.rm.jaxb.core.data.ManagedFiles;
import org.eclipse.ptp.rm.jaxb.core.data.PostExecuteCommands;
import org.eclipse.ptp.rm.jaxb.core.data.PreExecuteCommands;
import org.eclipse.ptp.rm.jaxb.core.data.Script;
import org.eclipse.ptp.rm.jaxb.core.messages.Messages;
import org.eclipse.ptp.rm.jaxb.core.variables.RMVariableMap;

public class ScriptHandler extends Job implements IJAXBNonNLSConstants {

	private Script script;
	private File file;
	private final String sourceDir;
	private final RMVariableMap map;

	public ScriptHandler(Script script, ManagedFiles files) {
		super(Messages.ScriptHandlerJob);
		map = RMVariableMap.getInstance();
		sourceDir = map.getString(files.getFileSourceLocation());
	}

	public File getFile() {
		return file;
	}

	@Override
	protected IStatus run(IProgressMonitor monitor) {
		SubMonitor progress = SubMonitor.convert(monitor, 20);
		String script = composeScript(monitor);
		progress.worked(10);
		try {
			writeScript(script);
		} catch (IOException t) {
			t.printStackTrace();
			progress.done();
			return new Status(Status.ERROR, JAXBCorePlugin.getUniqueIdentifier(), Messages.ScriptHandlerWriteError, t);
		}
		map.getVariables().put(SCRIPT_PATH, file);
		progress.done();
		return Status.OK_STATUS;
	}

	private void addDirectives(DirectiveDefinitions defs, StringBuffer buffer) {
		for (DirectiveDefinition def : defs.getDirectiveDefinition()) {
			String key = def.getValueFrom();
			String value = getValue(key);
			if (value != null && !ZEROSTR.equals(value)) {
				buffer.append(def.getContent()).append(value).append(REMOTE_LINE_SEP);
			}
		}
	}

	private void addEnvironment(EnvironmentDefinitions defs, StringBuffer buffer) {
		for (EnvironmentDefinition def : defs.getEnvironmentDefinition()) {
			String key = def.getValueFrom();
			String value = getValue(key);
			if (value != null && !ZEROSTR.equals(value)) {
				buffer.append(def.getContent()).append(value).append(REMOTE_LINE_SEP);
			}
		}
	}

	private void addExecute(ExecuteCommand commands, StringBuffer buffer) {
		new ArglistImpl(commands.getArglist()).toString(buffer);
		buffer.append(REMOTE_LINE_SEP);
	}

	private void addPostExecute(PostExecuteCommands commands, StringBuffer buffer) {
		for (Arglist args : commands.getArglist()) {
			new ArglistImpl(args).toString(buffer);
			buffer.append(REMOTE_LINE_SEP);
		}
	}

	private void addPreExecute(PreExecuteCommands commands, StringBuffer buffer) {
		for (Arglist args : commands.getArglist()) {
			new ArglistImpl(args).toString(buffer);
			buffer.append(REMOTE_LINE_SEP);
		}
	}

	private void addShell(String shell, StringBuffer buffer) {
		buffer.append(getValue(shell)).append(REMOTE_LINE_SEP);
	}

	private String composeScript(IProgressMonitor monitor) {
		SubMonitor progress = SubMonitor.convert(monitor, 30);
		StringBuffer buffer = new StringBuffer();
		addShell(script.getShell(), buffer);
		progress.worked(5);
		addDirectives(script.getDirectiveDefinitions(), buffer);
		progress.worked(5);
		addEnvironment(script.getEnvironmentDefinitions(), buffer);
		progress.worked(5);
		addPreExecute(script.getPreExecuteCommands(), buffer);
		progress.worked(5);
		addExecute(script.getExecuteCommand(), buffer);
		progress.worked(5);
		addPostExecute(script.getPostExecuteCommands(), buffer);
		progress.done();
		return buffer.toString();
	}

	private String getValue(String key) {
		String name = OPENV + key + CLOSVAL;
		return map.getString(name);
	}

	private void writeScript(String contents) throws IOException {
		file = new File(sourceDir, UUID.randomUUID() + SCRIPT + SH);
		FileWriter fw = null;
		try {
			fw = new FileWriter(file, false);
			fw.write(contents);
			fw.flush();
		} finally {
			try {
				if (fw != null) {
					fw.close();
				}
			} catch (IOException t) {
				t.printStackTrace();
			}
		}
	}
}
