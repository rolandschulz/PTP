package org.eclipse.core.runtime;

public interface IPath
{
    String toOSString();

    IPath addTrailingSeparator();
}
