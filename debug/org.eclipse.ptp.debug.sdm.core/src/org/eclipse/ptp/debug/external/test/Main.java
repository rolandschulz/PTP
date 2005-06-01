/*
 * Created on Jan 20, 2005
 *
 * TODO To change the template for this generated file go to
 * Window - Preferences - Java - Code Style - Code Templates
 */
package org.eclipse.ptp.debug.external.test;

import java.io.File;

import org.eclipse.ptp.debug.external.gdb.mi.GDBSession;
import org.eclipse.ptp.debug.external.gdb.mi.MIException;
import org.eclipse.ptp.debug.external.gdb.mi.MISession;
import org.eclipse.ptp.debug.external.gdb.mi.command.CommandFactory;





/**
 * @author donny
 *
 * TODO To change the template for this generated type comment go to
 * Window - Preferences - Java - Code Style - Code Templates
 */
public class Main {

	public static void main(String[] args) {
		System.out.println("Hello World");
		
		GDBSession gdbSession = null;
		CommandFactory factory = null;
		MISession miSession = null;

		System.out.println(System.getProperty("user.dir"));
		
		String prg_arg = "/home/donny/tmp/gt3_debugging_test/test";
		String cwd_arg = "/home/donny/tmp/gt3_debugging_test";
		gdbSession = new GDBSession();
		factory = new CommandFactory();

		File cwd = new File(cwd_arg);
		File prg = new File(prg_arg);
				
		try {
			miSession = gdbSession.createSession(new String[] {"/usr/bin/gdb"}, prg, cwd, null);
		} catch (Exception e) {
		}    	

		try {
			//miSession.postCommand(factory.createMIBreakInsert("main"));
			miSession.postCommand(factory.createMIExecRun());
			//miSession.postCommand(factory.createMIExecNext());
			//miSession.postCommand(factory.createMIExecContinue());
			Thread.sleep(3000);
			miSession.postCommand(factory.createMIGDBExit());
			Thread.sleep(3000);
		} catch (MIException e) {
		} catch (Exception e) {
		}
		
		System.out.println("Hello World End");
	}
}
