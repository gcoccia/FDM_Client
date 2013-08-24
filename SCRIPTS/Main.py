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

def Read_and_Process_Main_Info():

 #Declare dimensions dictionary
 dims = {}
 datasets = {}
 file = "INPUT.txt"
 fp = open(file,'r')
 for line in fp:
  array = line.split()
  if array[0] != '#':
   if array[0] == "DIMENSIONS":
    dims[array[1]] = float(array[2])
   if array[0] == "VARIABLE":
    dataset = array[1]
    try:
     datasets[dataset]
    except:
     datasets[dataset] = {}
    variable = array[2]
    try: 
     datasets[dataset]['variables']
    except:
     datasets[dataset]['variables'] = {}
     datasets[dataset]['timestep'] = array[5].split('/')
    datasets[dataset]['variables'][variable] = {}
    datasets[dataset]['variables'][variable]['units'] = array[3]
    datasets[dataset]['variables'][variable]['group'] = array[4]
 
 #Finish up the dimensions array
 dims['nlat'] = int(dims['nlat'])
 dims['nlon'] = int(dims['nlon'])
 dims['maxlat'] = dims['minlat'] + dims['res']*(dims['nlat']-1)
 dims['maxlon'] = dims['minlon'] + dims['res']*(dims['nlon']-1)

 return (dims,datasets)
 
#1. Determine the dimensions
(dims,datasets) = Read_and_Process_Main_Info()
dt = datetime.timedelta(days=1)
date = datetime.datetime.today()
idate = datetime.datetime(date.year,date.month,date.day) - 6*dt
idate = datetime.datetime(2001,1,1)
fdate = datetime.datetime(2001,1,1)

#2. Download all the requested data
date = idate
while date <= fdate:

 print date

 #Setup routines
 #cl.Setup_Routines(date)

 #For each availabe data set:
 for dataset in datasets:
  for tstep in datasets[dataset]['timestep']:
   print "%s %s" % (dataset,tstep)
   #Download and process the data
   #cl.Download_and_Process(date,dims,tstep,dataset,datasets[dataset]['variables'])
   
   #Create Images
   cl.Create_Images(date,dims,dataset,tstep)

 date = date + dt

#3. Create and update the point data

for dataset in datasets:
 for tstep in datasets[dataset]['timestep']:
  print "%s %s" % (dataset,tstep)
  cl.Create_and_Update_Point_Data(idate,fdate,tstep,dataset,datasets[dataset]['variables'])

#3. Create images and new files
