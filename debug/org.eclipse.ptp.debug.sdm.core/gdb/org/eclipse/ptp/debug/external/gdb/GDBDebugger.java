/*
 * Created on Feb 18, 2005
 *
 */
package org.eclipse.ptp.debug.external.gdb;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;

import org.eclipse.ptp.debug.external.AbstractDebugger;
import org.eclipse.ptp.debug.external.DebugConfig;
import org.eclipse.ptp.debug.external.actionpoint.ABreakpoint;
import org.eclipse.ptp.debug.external.actionpoint.AWatchpoint;
import org.eclipse.ptp.debug.external.actionpoint.DebugActionpoint;
import org.eclipse.ptp.debug.external.event.EExit;
import org.eclipse.ptp.debug.external.gdb.mi.GDBSession;
import org.eclipse.ptp.debug.external.gdb.mi.MIException;
import org.eclipse.ptp.debug.external.gdb.mi.MISession;
import org.eclipse.ptp.debug.external.gdb.mi.command.Command;
import org.eclipse.ptp.debug.external.gdb.mi.command.CommandFactory;
import org.eclipse.ptp.debug.external.gdb.mi.command.MIBreakInsert;
import org.eclipse.ptp.debug.external.gdb.mi.command.MIBreakWatch;
import org.eclipse.ptp.debug.external.model.MProcess;
import org.eclipse.ptp.debug.external.model.MProcessSet;




/**
 * @author donny
 *
 */
public class GDBDebugger extends AbstractDebugger {
	GDBSession gdbSession = null;
	CommandFactory factory = null;

	public GDBDebugger(DebugConfig dConf) {
		super(dConf);
	}

	public void initDebugger() {
		super.initDebugger();
		gdbSession = new GDBSession();
		factory = new CommandFactory();
	}
	
	public void destroyDebugger() {
		postGDBCommand(allSet, factory.createMIGDBExit());
		allSet.clear();
		//fireEvent(new EExit());
	}
	
	public void exit() {
		super.exit();
		gdbSession = null;
		factory = null;
	}
	
	public void quit() {
		super.quit();
		gdbSession = null;
		factory = null;
	}
	
	/* Serial program */
	public void load(String prg) {
		this.load(prg, 1);
	}
	
	/* Parallel progam */
	public void load(String prg, int numProcs) {
		super.load(prg, numProcs);
		if (allSet.getSize() != 0)
			destroyDebugger();
		MProcess.resetGlobalCounter();
		File cwd = new File(debugConfig.getUserHome());
		try {
			for (int i = 0; i < numProcs; i++) {
				MProcess proc = new MProcess();
				String[] gdbPath = debugConfig.getDebuggerPath();
				MISession miS = gdbSession.createSession(gdbPath, null, cwd, null);
				miS.setParentDebugger(this);
				miS.postCommand(factory.createMIFileSymbolFile(prg));
				miS.postCommand(factory.createMIFileExecFile(prg));
				proc.setDebugInfo(miS);
				allSet.addProcess(proc);
			}
		} catch (MIException e) {
		} catch (IOException e) {
		}
	}
	
	public void run(String[] args) {
		super.run(args);
		int numProcs = currentFocus.getSize();
		if (numProcs == 1) {
			try {
				MISession miS = (MISession) (currentFocus.getProcess(0)).getDebugInfo();
				if (args == null)
					miS.postCommand(factory.createMIExecRun());
				else
					miS.postCommand(factory.createMIExecRun(args));
			} catch (MIException e) {
			}
		} else if (numProcs > 1) {
			try {
				if (args == null)
					args = new String[0];
				String str;	
				String[] cmd = new String[6 + args.length];
				cmd[0] = debugConfig.getMpirunPath();
				cmd[1] = "-t";
				cmd[2] = "-np";
				cmd[3] = Integer.toString(numProcs);
				cmd[4] = debuggedProgram;
				cmd[5] = "-p4norem";
				
				for (int i = 0; i < args.length; i++)
					cmd[6 + i] = args[i];
				
				/* Create the PG file */
				Process p = Runtime.getRuntime().exec(cmd);
				BufferedReader stdInput = new BufferedReader(new InputStreamReader(
						p.getInputStream()));
				// read the output from the command
				ArrayList out = new ArrayList();
				while ((str = stdInput.readLine()) != null) {
					out.add(str);
				}
		        // Create temp file.
		        File temp = File.createTempFile("mpi", ".pgfile");
		        // Delete temp file when program exits.
		        temp.deleteOnExit();
		        // Write to temp file
		        BufferedWriter output = new BufferedWriter(new FileWriter(temp));
		        for (int i = 1; i < out.size() - 1; i++)
		        	output.write((String) out.get(i) + "\n");
		        output.close();
		        
		        /* PG file is: */
		        //System.out.println(temp.getPath());

				/* Run the master */
				cmd = new String[3 + args.length];
				cmd[0] = "-p4norem";
				cmd[1] = "-p4pg";
				cmd[2] = temp.getPath();

				for (int i = 0; i < args.length; i++)
					cmd[3 + i] = args[i];
				
				MISession miS = (MISession) (currentFocus.getProcess(0)).getDebugInfo();
				miS.postCommand(factory.createMIExecRun(cmd));

				/* Run the slaves */
				cmd = new String[3];
				cmd[0] = debugConfig.getMpirunMasterHost();
				cmd[1] = debugConfig.getMpirunMasterPort();
				cmd[2] = "-p4amslave";

				for(int i = 1; i < numProcs; i++) {
					miS = (MISession) (currentFocus.getProcess(i)).getDebugInfo();
					miS.postCommand(factory.createMIExecRun(cmd));
				}
			} catch (IOException e) {
			} catch (MIException e) {
			}
		}
	}
	
	public void run() {
		this.run(null);
	}
	
	public void detach() {
		super.detach();
		postGDBCommand(currentFocus, factory.createMITargetDetach());
	}
	
	private void addBreakpoint(String loc, int count, String cond) {
		DebugActionpoint da;
		if (count != -1 && cond == null)
			da = new ABreakpoint(loc, count);
		else if (count == -1 && cond != null)
			da = new ABreakpoint(loc, cond);
		else // (count == -1 && cond == null)
			da = new ABreakpoint(loc);
		
		int gdbId = 0;
		int numProcs = currentFocus.getSize();
		try {
			for (int i = 0; i < numProcs; i++) {
				MIBreakInsert brk;
				if (count != -1 && cond == null)
					/* The ignore count for GDB is 0 while HPDF spec says
					 * it defaults to 1. Thus we put "count - 1"
					 */
					brk = factory.createMIBreakInsert(false, false, null, count-1, loc, 0);
				else if (count == -1 && cond != null)
					brk = factory.createMIBreakInsert(false, false, cond, 0, loc, 0);
				else // (count == -1 && cond == null)
					brk = factory.createMIBreakInsert(loc);
				
				MISession miS = (MISession) (currentFocus.getProcess(i)).getDebugInfo();
				miS.postCommand(brk);
				gdbId = brk.getMIBreakInsertInfo().getMIBreakpoints()[0].getNumber();
				da.addProcess(currentFocus.getProcess(i), gdbId);
			}
		} catch (MIException e) {
		}
		actionpointList.add(da);
	}
	
	public void breakpoint(String loc) {
		super.breakpoint(loc);
		addBreakpoint(loc, -1, null);
	}
	
	public void breakpoint(String loc, int count) {
		super.breakpoint(loc, count);
		addBreakpoint(loc, count, null);
	}
	
	public void breakpoint(String loc, String cond) {
		super.breakpoint(loc, cond);
		addBreakpoint(loc, -1, cond);
	}
	
	public void watchpoint(String var) {
		super.watchpoint(var);
		DebugActionpoint da = new AWatchpoint(var);
		int gdbId = 0;
		int numProcs = currentFocus.getSize();
		try {
			for (int i = 0; i < numProcs; i++) {
				MIBreakWatch brk = factory.createMIBreakWatch(false, false, var);
				MISession miS = (MISession) (currentFocus.getProcess(i)).getDebugInfo();
				miS.postCommand(brk);
				gdbId = brk.getMIBreakWatchInfo().getMIBreakpoints()[0].getNumber();
				da.addProcess(currentFocus.getProcess(i), gdbId);
			}
		} catch (MIException e) {
		}
		actionpointList.add(da);
	}

	public void delete(int[] ids) {
		super.delete(ids);
		int size = actionpointList.size();
		int argSize = ids.length;
		
		for (int i = 0; i < argSize; i++) {
			int id = ids[i] - 1;
			if (id >= size)
				continue;
			DebugActionpoint da = (DebugActionpoint) actionpointList.get(id);
			da.setStatus("DELETED");
			ArrayList daProcessList = da.getProcessList();
			ArrayList daDebuggerIdList = da.getDebuggerIdList();
			try {
				for (int j = 0; j < daProcessList.size(); j++) {
					int debuggerId = ((Integer) daDebuggerIdList.get(j)).intValue();
					MISession miS = (MISession) ((MProcess) daProcessList.get(j)).getDebugInfo();
					miS.postCommand(factory.createMIBreakDelete(new int[] {debuggerId}));
				}
			} catch (MIException e) {
			}
		}
	}
	
	public void delete(String type) {
		super.delete(type);
		int size = actionpointList.size();
		for (int i = 0; i < size; i++) {
			DebugActionpoint da = (DebugActionpoint) actionpointList.get(i);
			if (type.equals("DISABLED") && da.getStatus().equals("DISABLED"))
				da.setStatus("DELETED");
			else if (type.equals("ENABLED") && da.getStatus().equals("ENABLED"))
				da.setStatus("DELETED");
			else if (type.equals("WATCH") && (da instanceof AWatchpoint))
				da.setStatus("DELETED");
			else if (type.equals("BREAK") && (da instanceof ABreakpoint))
				da.setStatus("DELETED");
			else if (type.equals("ALL"))
				da.setStatus("DELETED");
			ArrayList daProcessList = da.getProcessList();
			ArrayList daDebuggerIdList = da.getDebuggerIdList();
			try {
				for (int j = 0; j < daProcessList.size(); j++) {
					int debuggerId = ((Integer) daDebuggerIdList.get(j)).intValue();
					MISession miS = (MISession) ((MProcess) daProcessList.get(j)).getDebugInfo();
					miS.postCommand(factory.createMIBreakDelete(new int[] {debuggerId}));
				}
			} catch (MIException e) {
			}
		}
	}
	
	private void modifyActionpoint(String mod, String type) {
		int size = actionpointList.size();
		for (int i = 0; i < size; i++) {
			DebugActionpoint da = (DebugActionpoint) actionpointList.get(i);
			if (type.equals("WATCH") && (da instanceof AWatchpoint))
				da.setStatus(mod);
			else if (type.equals("BREAK") && (da instanceof ABreakpoint))
				da.setStatus(mod);
			else if (type.equals("ALL"))
				da.setStatus(mod);
			ArrayList daProcessList = da.getProcessList();
			ArrayList daDebuggerIdList = da.getDebuggerIdList();
			try {
				for (int j = 0; j < daProcessList.size(); j++) {
					int debuggerId = ((Integer) daDebuggerIdList.get(j)).intValue();
					MISession miS = (MISession) ((MProcess) daProcessList.get(j)).getDebugInfo();
					if (mod.equals("DISABLED"))
						miS.postCommand(factory.createMIBreakDisable(new int[] {debuggerId}));
					else if (mod.equals("ENABLED"))
						miS.postCommand(factory.createMIBreakEnable(new int[] {debuggerId}));
				}
			} catch (MIException e) {
			}
		}
	}
	
	private void modifyActionpoint(String mod, int[] ids) {
		int size = actionpointList.size();
		int argSize = ids.length;
		
		for (int i = 0; i < argSize; i++) {
			int id = ids[i] - 1;
			if (id >= size)
				continue;
			DebugActionpoint da = (DebugActionpoint) actionpointList.get(id);
			da.setStatus(mod);
			ArrayList daProcessList = da.getProcessList();
			ArrayList daDebuggerIdList = da.getDebuggerIdList();
			try {
				for (int j = 0; j < daProcessList.size(); j++) {
					int debuggerId = ((Integer) daDebuggerIdList.get(j)).intValue();
					MISession miS = (MISession) ((MProcess) daProcessList.get(j)).getDebugInfo();
					if (mod.equals("DISABLED"))
						miS.postCommand(factory.createMIBreakDisable(new int[] {debuggerId}));
					else if (mod.equals("ENABLED"))
						miS.postCommand(factory.createMIBreakEnable(new int[] {debuggerId}));
				}
			} catch (MIException e) {
			}
		}
	}
	
	public void disable(int[] ids) {
		super.disable(ids);
		modifyActionpoint("DISABLED", ids);
	}
	
	public void disable(String type) {
		super.disable(type);
		modifyActionpoint("DISABLED", type);
	}
	
	public void enable(int[] ids) {
		super.enable(ids);
		modifyActionpoint("ENABLED", ids);
	}
	
	public void enable(String type) {
		super.enable(type);
		modifyActionpoint("ENABLED", type);
	}
	
	public void cont() {
		super.cont();
		postGDBCommand(currentFocus, factory.createMIExecContinue());
	}

	public void halt() {
		super.halt();
		postGDBCommand(currentFocus, factory.createMIExecInterrupt());
	}

	public void stepFinish() {
		super.stepFinish();
		postGDBCommand(currentFocus, factory.createMIExecFinish());
	}

	public void step() {
		super.step();
		postGDBCommand(currentFocus, factory.createMIExecStep());
	}

	public void step(int count) {
		super.step(count);
		postGDBCommand(currentFocus, factory.createMIExecStep(count));
	}

	public void stepOver() {
		super.stepOver();
		postGDBCommand(currentFocus, factory.createMIExecNext());
	}

	public void stepOver(int count) {
		super.stepOver(count);
		postGDBCommand(currentFocus, factory.createMIExecNext(count));
	}
	
	public void remote(String host, int port) {
		super.remote(host, port);
		try {
			/* Only serial support at the moment */
			MISession miS = (MISession) ((MProcess) allSet.getProcess(0)).getDebugInfo();
			miS.postCommand(factory.createMITargetRemote(host, port));
		} catch (MIException e) {
		}
	}
	
	private void postGDBCommand(MProcessSet procSet, Command cmd) {
		int listSize = procSet.getSize();
		try {
			for (int i = 0; i < listSize; i++) {
				MISession miS = (MISession) (procSet.getProcess(i)).getDebugInfo();
				miS.postCommand(cmd);
			}
		} catch (MIException e) {
		}
	}
	
	/* GDBDebugger Specific Methods */
	
	public Process[] getProcesses() {
		int listSize = allSet.getSize();
		Process[] procs = new Process[listSize];
		for (int i = 0; i < listSize; i++) {
			MISession miS = (MISession) (allSet.getProcess(i)).getDebugInfo();
			procs[i] = miS.getMIInferior();
		}
		return procs;
	}
	
	public Process getSessionProcess() {
		int listSize = allSet.getSize();
		Process proc = null;
		/* GDBDebugger somehow needs to combine output stream (i.e. SessionProcess)
		 * from all processes, currently we return SessionProcess of Process 0
		 */
		if (listSize > 0) {
			MISession miS = (MISession) (allSet.getProcess(0)).getDebugInfo();
			proc = miS.getSessionProcess(); 
		}
		return proc;
	}
}