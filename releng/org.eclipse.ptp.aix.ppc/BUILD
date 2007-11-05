#!/bin/sh
#
# This script is used to build any executable fragments in the workspace
#
export KNOWN_FRAGMENTS="\
	org.eclipse.ptp.utils\
	org.eclipse.ptp.utils_2.0.0\
	org.eclipse.ptp.proxy\
	org.eclipse.ptp.proxy_2.0.0\
	org.eclipse.ptp.ibmpe.proxy\
	org.eclipse.ptp.ibmpe.proxy_2.0.0"
		
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

