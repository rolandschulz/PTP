package org.eclipse.photran.internal.core.parser;

import org.eclipse.photran.internal.core.analysis.types.Type;
import org.eclipse.photran.internal.core.analysis.types.TypeError;

public interface ITypedNode
{
    Type getType() throws TypeError;
}
