#!/bin/bash
#for CVS (and GIT from proj links):
base=`dirname $0`
base=${base%/*}
if test "x${base:0:1}" != 'x/'; then
    base=`pwd`/$base
fi
#echo `dirname $BASH_SOURCE`
cd $base/org.eclipse.ptp.rm.pbs.jproxy
java -cp $base/org.eclipse.ptp.proxy.protocol/bin/:$base/org.eclipse.ptp.utils.core/bin/:$base/org.eclipse.ptp.proxy.jproxy/bin/:bin/ org/eclipse/ptp/rm/pbs/jproxy/PBSProxyRuntimeServer $*
if test $? != 0 ; then 
    echo -e \\n\\nMake sure to run from proj folder!
fi
#for GIT:
#cd ${0%/*}
#java -cp ../../core/org.eclipse.ptp.proxy.protocol/bin/:bin/ org/eclipse/ptp/rm/pbs/jproxy/PBSProxyRuntimeServer $*