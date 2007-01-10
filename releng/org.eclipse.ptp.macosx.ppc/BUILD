#!/bin/sh
#
# This script is used to build any executable fragments in the workspace
#
export KNOWN_FRAGMENTS="\
	org.eclipse.ptp.utils\
	org.eclipse.ptp.proxy\
	org.eclipse.ptp.debug.sdm\
	org.eclipse.ptp.orte.proxy\
	org.eclipse.ptp.mpich2.proxy"
	
export PARENT=`dirname $PWD`
export FRAGMENT=`basename $PWD`

proc=`uname -p`
if [ -z "$proc" -o "$proc" = "unknown" ];
then
	proc=`uname -m`
fi
export ARCH=`echo "$proc" | sed -e 's/powerpc/ppc/' -e 's/ppc64/ppc/' -e 's/i.86/x86/'`
export OS=`uname -s | sed -e 's/Darwin/macosx/' -e 's/Linux/linux/'`

# Check we're building for the correct OS/ARCH

if [ $FRAGMENT != org.eclipse.ptp.$OS.$ARCH ]; then
	echo "Invalid OS/ARCH: can only build org.eclipse.ptp.$OS.$ARCH on this machine"
	exit 1
fi

export PREFIX=$PARENT/$FRAGMENT

for fragment in $KNOWN_FRAGMENTS
do
	path=$PARENT/$fragment
	if [ -d $path ]; then
		(cd $path; \
		sh configure --prefix=$PREFIX; \
		make install)
	else
		echo "FRAGMENT: $fragment not found, skipping"
	fi
done

exit 0
