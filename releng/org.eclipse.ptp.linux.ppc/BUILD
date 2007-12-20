#!/bin/sh
#
# This script is used to build any executable fragments in the workspace
#
export KNOWN_FRAGMENTS="\
	org.eclipse.ptp.utils\
	org.eclipse.ptp.utils_*\
	org.eclipse.ptp.proxy\
	org.eclipse.ptp.proxy_*\
	org.eclipse.ptp.debug.sdm\
	org.eclipse.ptp.debug.sdm_*\
	org.eclipse.ptp.orte.proxy\
	org.eclipse.ptp.orte.proxy_*\
	org.eclipse.ptp.mpich2.proxy\
	org.eclipse.ptp.mpich2.proxy_*\
	org.eclipse.ptp.ibmpe.proxy\
	org.eclipse.ptp.ibmpe.proxy_*\
	org.eclipse.ptp.ibmll.proxy\
	org.eclipse.ptp.ibmll.proxy_*"
		
export PARENT=`dirname $PWD`
export FRAGMENT=`basename $PWD`

export PARENT=`dirname $PWD`
export FRAGMENT=`basename $PWD`

export PREFIX=$PARENT/$FRAGMENT

if [ ! -d bin ]; then
	mkdir bin
fi

for fragment in $KNOWN_FRAGMENTS
do
	path=$PARENT/$fragment
	if [ -d $path ]; then
		(cd $path; \
		sh configure --prefix=$PREFIX; \
		chmod +x install-sh; \
		make install)
	fi
done

exit 0

