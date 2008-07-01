package org.eclipse.core.runtime;

public interface IProgressMonitor
{
    int UNKNOWN = -1;

    void beginTask(String string, int unknown2);
    void subTask(String string);
    void done();
    boolean isCanceled();
}