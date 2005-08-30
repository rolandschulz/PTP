package org.eclipse.ptp.rtsystem.simulation;

public class SimJobState {
	public String jobname = null;
	
	public String spawned_app_state = null;

	public int spawned_num_procs = 0;

	public int spawned_procs_per_node = 0;

	public int spawned_first_node = 0;

	public String spawned_app_signal = new String("");

	public String spawned_app_exit_code = new String("");
}
