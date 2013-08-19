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
idate = datetime.datetime(2001,1,1)
fdate = datetime.datetime(2001,12,31)

#2. Download all the requested data
date = idate
while date <= fdate:

 print date

 #Setup routines
 cl.Setup_Routines(date)

 #################################################
 #DOWNLOAD ALL THE REQUIRED DATA
 #################################################

 #PGF
 cl.Download_and_Process(date,dims,'DAILY','PGF')
 cl.Download_and_Process(date,dims,'MONTHLY','PGF')
 cl.Download_and_Process(date,dims,'YEARLY','PGF')
 
 #3B42RT_BC
 cl.Download_and_Process(date,dims,'DAILY','3B42RT_BC')
 cl.Download_and_Process(date,dims,'MONTHLY','3B42RT_BC')
 cl.Download_and_Process(date,dims,'YEARLY','3B42RT_BC')

 #################################################
 #CREATE ALL IMAGES
 #################################################

 #PGF forcing (historical
 #cl.Create_Images(date,dims,'pgf_daily','DAILY')

 #################################################
 #UPDATE CELL FILES
 #################################################

 #idate_pgf = datetime.datetime(1950,1,1)
 #cl.Create_and_Update_Point_Data(date,'pgf_daily',idate_pgf,'DAILY')

 date = date + dt

#3. Create images and new files
