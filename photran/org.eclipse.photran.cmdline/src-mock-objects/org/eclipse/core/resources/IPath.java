package org.eclipse.core.resources;

public interface IPath
{
    String toOSString();

    IPath addTrailingSeparator();
}
