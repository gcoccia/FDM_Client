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

#1. Determine the dimensions
(dims,datasets) = cl.Read_and_Process_Main_Info()
dt = datetime.timedelta(days=1)
date = datetime.datetime.today()

#Always redownload and reprocess the last 30 days
idate = datetime.datetime(2013,9,12)
fdate = datetime.datetime(2013,9,12)

#Prepare the mask
cl.Create_Mask(dims)

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
   print dataset
   #cl.Create_Images(date,dims,dataset,tstep,datasets[dataset],True)
 
 date = date + dt

'''
#3. Create and update the point data
#for dataset in datasets:
for tstep in ['DAILY','MONTHLY','YEARLY']:
  print "%s" % (tstep)
  cl.Create_and_Update_All_Point_Data(idate,fdate,datasets,tstep)
  #cl.Create_and_Update_Point_Data(idate,fdate,tstep,dataset,datasets[dataset]['variables'])
'''

#4. Update the xml file
cl.Update_XML_File(datasets)
'''
xml_settings = '../web_nchaney/settings.xml'
tree = ET.parse(xml_settings)
for dataset in datasets:
 itime = datasets[dataset]['itime']
 ftime = datasets[dataset]['ftime']
 for variable in datasets[dataset]['variables']:
  print dataset,variable
  Update_XML(tree,variable,dataset,itime,ftime)
#Write the new xml file
tree.write('settings.xml')
'''
