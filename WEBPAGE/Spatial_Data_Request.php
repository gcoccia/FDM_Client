<html>
<body>

time step: <?php echo $_POST["tstep"]; ?><br>
llclat: <?php echo $_POST["llclat"]; ?><br>
llclon: <?php echo $_POST["llclon"]; ?><br> 
urclat: <?php echo $_POST["urclat"]; ?><br>
urclon: <?php echo $_POST["urclon"]; ?><br>
iyear: <?php echo $_POST["iyear"]; ?><br>
imonth: <?php echo $_POST["imonth"]; ?><br>
iday: <?php echo $_POST["iday"]; ?><br>
fyear: <?php echo $_POST["fyear"]; ?><br>
fmonth: <?php echo $_POST["fmonth"]; ?><br>
fday: <?php echo $_POST["fday"]; ?><br>
sres: <?php echo $_POST["sres"]; ?><br>
format: <?php echo $_POST["format"]; ?><br>
email: <?php echo $_POST["email"]; ?><br>
variables: <?php echo implode(";",$_POST['variables']);?><br>
<br>
The data is being processed. You will be notified when it is available for download.

</body>
</html>

<?php
#Collect the information
$tstep = $_POST["tstep"];
$llclat = $_POST["llclat"];
$llclon = $_POST["llclon"];
$urclat = $_POST["urclat"];
$urclon = $_POST["urclon"];
$iyear = $_POST["iyear"];
$imonth = $_POST["imonth"];
$iday = $_POST["iday"];
$fyear = $_POST["fyear"];
$fmonth = $_POST["fmonth"];
$fday = $_POST["fday"];
$format = $_POST["format"];
$sres = $_POST["sres"];
$email = $_POST["email"];
$variables = implode("/",$_POST['variables']);

#Run code to extract data and send email
system("(python Spatial_Data_Request.py $tstep $llclat $llclon $urclat $urclon $iyear $imonth $iday $fyear $fmonth $fday $format $email $variables $sres >& /dev/null &) ");
#echo "python Spatial_Data_Request.py $tstep $llclat $llclon $urclat $urclon $iyear $imonth $iday $fyear $fmonth $fday $format $email $variables $sres";
?>

