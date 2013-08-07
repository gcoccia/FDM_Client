#!/usr/bin/env python
#
# Simple script demonstrating the interp() method.
#

from pylab import *
from numpy import float32
from grads import GrADS, GaField

# Start GrADS and open the data file
# ----------------------------------
ga = GrADS(Bin='grads',Echo=False,Port=True,Window=False)
ga.open('../data/model.ctl')

# Create sample trajectory
# ------------------------
lats = [ -90, -60, -45, 0, 45, 60, 90 ]
lons = [-180, -90, -45, 0, 45, 90, 180 ]

# Either do the plotting in Python ...
# ------------------------------------
ga('set t 1')
ts, lev = ga.interp('ts',lons,lats)

clf()

subplot(211)
plot(lats,ts)
title("Surface Temperature")
xlabel('Latitude')

subplot(212)
plot(lons,ts)
xlabel('Longitude')
savefig('interp1.png')

figure()

ga('set lev 1000 200')
ua, lev = ga.interp('ua',lons,lats)
ta, lev = ga.interp('ta',lons,lats)
zg, lev = ga.interp('zg',lons,lats)

for i in range(len(lats)):
    subplot(121)
    plot(ta[i],zg[i]/1000); ylabel('Z'); xlabel('T')
    subplot(122)
    plot(ua[i],zg[i]/1000); ylabel('Z'); xlabel('U')

savefig('interp2.png')
figure()
imshow(ua)
savefig('interp3.png')







    

