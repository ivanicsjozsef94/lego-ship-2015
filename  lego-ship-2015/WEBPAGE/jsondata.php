<?php
$myfile = fopen("./coordinates.txt", "r") or die("Unable to open file!"); //open file
$json = array(); //create array for json object
while (($line = fgets($myfile)) != "") { //iterate over the lines of the text file
  $arr = explode(':', trim($line), 2); //explode a line to parts where the first : appears
  if (strstr($arr[1], ':')) { //if the last remaining part inlcludes : create an array in array
    $arrinarr = explode(';', trim($arr[1])); ////explode a line to parts where the ; appears
    $innerjson = array(); //create array in array
    for ($i = 0; $i < count($arrinarr); $i++) { //interate over the exploded inner array elements
      $arrinarr2 = explode(':', trim($arrinarr[$i])); //explode them to parts
      $innerjson[$arrinarr2[0]] = $arrinarr2[1];
    }
    $json[$arr[0]] = $innerjson;
  }
  else {
    $json[$arr[0]] = $arr[1];
  }
}
fclose($myfile);
header('Content-Type: text/javascript; charset=utf8');
echo json_encode($json);
?>