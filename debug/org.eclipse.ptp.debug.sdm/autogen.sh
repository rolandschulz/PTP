#!/bin/sh

ACLOCAL=aclocal
AUTOMAKE=automake
AUTOCONF=autoconf

autogen()
{
	echo "autogen in $1"
	$ACLOCAL
	$AUTOMAKE
	$AUTOCONF
}
	
DIRS="."

for dir in $DIRS
do
	(cd $dir; autogen $dir)
done

exit 0
