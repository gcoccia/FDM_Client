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
import xml.etree.ElementTree as ET
import datetime
import time
import dateutil.relativedelta as relativedelta

#1. Determine the dimensions
(dims,datasets) = cl.Read_and_Process_Main_Info()
dt = datetime.timedelta(days=1)
date = datetime.datetime.today()

#Always redownload and reprocess the last 30 days
idate = datetime.datetime(2008,1,1)
fdate = datetime.datetime(2013,9,15)

#Prepare the mask
cl.Create_Mask(dims,True)

#Determine the new dataset boundaries
for dataset in datasets:
 for tstep in datasets[dataset]['timestep']:
  (datasets[dataset],idate,fdate) = cl.Determine_Dataset_Boundaries(dataset,tstep,datasets[dataset],dims,idate,fdate)
#idate = fdate - datetime.timedelta(days=15)
idate = datetime.datetime(2013,1,1)
#fdate = idate

#Download all the requested data
date = idate
while date <= fdate:

 print date

 #Setup routines
 cl.Setup_Routines(date)

 #For each availabe data set:
 for dataset in datasets:
  for tstep in datasets[dataset]['timestep']:

   #Download and process the data
   datasets[dataset] = cl.Download_and_Process(date,dims,tstep,dataset,datasets[dataset],False)
 date = date + dt

#Preparing all the images
date = idate 
while date <= fdate:

 print date

 #Create Images

 #For each availabe data set:
 for dataset in datasets:
  for tstep in datasets[dataset]['timestep']:

   #Create Images
   cl.Create_Images(date,dims,dataset,tstep,datasets[dataset],False)
 
 date = date + dt

#4. Update the xml file
cl.Update_XML_File(datasets)

#3. Create and update the point data
idate_tmp = idate
while idate_tmp <= fdate:
 fdate_tmp = idate + relativedelta.relativedelta(years=1)
 if fdate_tmp > fdate:
  fdate_tmp = fdate
 print idate_tmp,fdate_tmp
 cl.Create_and_Update_Point_Data(idate_tmp,fdate_tmp,datasets)
 idate_tmp = fdate_tmp + dt

