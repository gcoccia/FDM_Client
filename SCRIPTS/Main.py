#################################################################
# Drought Monitor V2
# Written by: Nathaniel W. Chaney
# Date: 12 July 2013
# Location: Princeton University
# Contact: nchaney@princeton.edu
# Purpose: Driver of the client side of the monitor
##################################################################

import Library as cl
import datetime

#1. Determine the dimensions
dims = {}
dims['minlat'] = 0.0 #-89.8750
dims['minlon'] = -18.0 #0.1250
dims['nlat'] = 100 #720
dims['nlon'] = 100 #1440
dims['res'] = 0.250
dims['maxlat'] = dims['minlat'] + dims['res']*(dims['nlat']-1)
dims['maxlon'] = dims['minlon'] + dims['res']*(dims['nlon']-1)
dt = datetime.timedelta(days=1)
date = datetime.datetime.today()
idate = datetime.datetime(date.year,date.month,date.day) - 6*dt
idate = datetime.datetime(1948,1,1)
fdate = datetime.datetime(2008,12,31)

#Setup routines
cl.Setup_Routines()

#2. Download all the requested data
date = idate
while date <= fdate:

 print date

 #################################################
 #DOWNLOAD AND OUTPUT ALL THE REQUIRED DATA
 #################################################

 #PGF forcing (historical)
 cl.Download_and_Process_Forcing(date,dims)

 date = date + dt

#3. Create images and new files
