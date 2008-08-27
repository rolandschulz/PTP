#!/bin/sh
src-jacobi-3*/a.exe >data
cat <<EOF | gnuplot
set term gif
set output "jacobi-viz.gif"

#set pm3d       # 1a. Normal
 set pm3d map   # 1b. Display a 2-D color gradient rather than the 3-D visualization
#set pm3d at b  # 1c. Display a 2-D color gradient under the 3-D visualization

                                                                   # 2a. Nothing = Black to yellow via blue and red
#set palette model XYZ functions gray**0.35, gray**0.5, gray**0.8  # 2b. Brown to yellow
 set palette model XYZ functions gray**0.3, gray**0.5, 0          # 2b. Dark red to yellow (Jeff)
#set palette model XYZ functions gray**0.8, gray**0.5, gray**0.35  # 2c. Green to yellow
#set palette rgbformulae 21,22,23                                  # 2d. Black to red to yellow to white
#set palette rgbformulae 22,13,-31                                 # 2e. Blue to green to yellow to red
#set palette defined (0 "black", 2.5 "red", 5 "yellow")

 splot 'data' matrix notitle             # 3a. Plot normally
#splot 'data' matrix notitle with pm3d   # 3b. Colorize the 3-D visualization (set 1a above)
EOF
rm -f data
echo "Visualization saved as jacobi-viz.gif"
if [ `uname -s` == "Darwin" ]; then
	open jacobi-viz.gif
elif [ `which eog` != "" ]; then
	eog jacobi-viz.gif
fi
