package org.eclipse.ptp.launch.core;


/**
 * @author Clement
 *
 */
public abstract class ParallelLaunchAdapter implements IParallelLaunchListener {    
    public void run() {
    }
    public void abort() {
    }
    public void exit() {
    }
	public void start() {
	}
	public void stopped() {
	}
    
    public void execStatusChangeEvent(Object object) {
    }
    public void sysStatusChangeEvent(Object object) {
    }
    public void processOutputEvent(Object object) {
    }
    public void errorEvent(Object object) {
    }
    public void updatedStatusEvent() {
    }
}
