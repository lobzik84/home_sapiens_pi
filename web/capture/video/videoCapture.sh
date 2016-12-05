#!/bin/bash
date
MONITOR=$1
FRAMES=$2
#FPS=$3
CF=FRAMES
CT=1
SSN=""
#PRE=""
let CF=FRAMES-1;

rm $MONITOR-out.mp4
rm Monitor$MONITOR.*
rm Monitor.tmp$MONITOR -r

while [ $CF -gt -1 ]
do
SSN="$(/usr/bin/zmu -m $MONITOR -i$CF -t)";
wait
#echo 'SSN>'$SSN;
let CT=CF;#FRAMES-CF-1;

if [ $CT -gt 9 ]; then
PRE="";
else 
 let PRE='0';
fi 

#mv Monitor$MONITOR.jpg Monitor$MONITOR.$PRE$CT.$sn.jpg;
mv Monitor$MONITOR.jpg Monitor$MONITOR.$SSN.jpg;
let CF=CF-1
done

mkdir Monitor.tmp$MONITOR
x=0; 
for i in Monitor$MONITOR.*jpg; 
do 
counter=$(printf %03d $x); 
ln "$i" ./Monitor.tmp$MONITOR/img"$counter".jpg; 
x=$(($x+1)); 
done

ffmpeg -loglevel panic -framerate $3 -i ./Monitor.tmp$MONITOR/img%03d.jpg -c:v libx264 -r 2 -pix_fmt yuv420p $MONITOR-out.mp4
wait
rm Monitor.tmp$MONITOR -r
rm Monitor$MONITOR.*
date

exit 0