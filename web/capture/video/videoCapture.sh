#!/bin/bash
#date
MONITOR=$1
FRAMES=$2
FPS=$3
FILENAME=$4

CF=FRAMES
CT=1
PRE=""
let CF=FRAMES-1;

rm $FILENAME
rm Monitor$MONITOR.*

while [ $CF -gt -1 ]
do
/usr/bin/zmu -m $MONITOR -i$CF
wait
let CT=FRAMES-CF-1;

if [ $CT -gt 9 ]; then
PRE="";
else 
 let PRE='0';
fi 

mv Monitor$MONITOR.jpg Monitor$MONITOR.$PRE$CT.jpg;
let CF=CF-1
done
ffmpeg -loglevel panic -framerate $3 -i Monitor$MONITOR.%02d.jpg -c:v libx264 -r 2 -pix_fmt yuv420p $FILENAME
wait
rm Monitor$MONITOR.*
#date

exit 0
