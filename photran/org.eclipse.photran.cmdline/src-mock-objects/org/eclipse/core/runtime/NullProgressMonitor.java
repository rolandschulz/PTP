package org.eclipse.core.runtime;

public class NullProgressMonitor implements IProgressMonitor
{
    public void beginTask(String string, int unknown2) {}
    public void subTask(String string) {}
    public void done() {}
    public boolean isCanceled() { return false; }
}
