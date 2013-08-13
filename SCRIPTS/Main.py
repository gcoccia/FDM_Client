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
import numpy as np

#1. Determine the dimensions
dims = {}
dims['minlat'] = -34.875 #-89.8750
dims['minlon'] = -18.875 #0.1250
dims['maxlat'] = 37.875
dims['maxlon'] = 54.875
dims['res'] = 0.25
dims['nlat'] = np.int(np.ceil((dims['maxlat'] - dims['minlat'])/ dims['res'] + 1))
dims['nlon'] = np.int(np.ceil((dims['maxlon'] - dims['minlon'])/ dims['res'] + 1))
dims['maxlat'] = dims['minlat'] + dims['res']*(dims['nlat']-1)
dims['maxlon'] = dims['minlon'] + dims['res']*(dims['nlon']-1)
dt = datetime.timedelta(days=1)
date = datetime.datetime.today()
idate = datetime.datetime(date.year,date.month,date.day) - 6*dt
idate = datetime.datetime(1948,1,1)
fdate = datetime.datetime(1948,1,10)

#2. Download all the requested data
date = idate
while date <= fdate:

 print date

 #Setup routines
 cl.Setup_Routines(date)

 #################################################
 #DOWNLOAD ALL THE REQUIRED DATA
 #################################################

 #PGF forcing (historical)
 cl.Download_and_Process_Forcing(date,dims)

 #################################################
 #CREATE ALL IMAGES
 #################################################

 #PGF forcing (historical
 #cl.Create_Images(date,dims,'pgf_daily','DAILY')

 date = date + dt

#3. Create images and new files
