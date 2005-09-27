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
	
DIRS=". bitvector"

for dir in $DIRS
do
	(cd $dir; autogen $dir)
done

./configure

exit 0
