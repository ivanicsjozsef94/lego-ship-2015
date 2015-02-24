<!doctype html>
<html lang="en">
<head>
  <meta charset="utf-8">
  <title>Lego Ship 2015 Group 4 Tracking Site</title>
  <link rel="stylesheet" type="text/css" href="style.css">
</head>
<body>
<?php
if ($_GET) {
  $myfile = fopen("./currcoords.txt", "w") or die("Unable to open file!");
    $coords =
        "ccoordx:" . $_GET['ccoordx'] . "\n".
        "ccoordy:" . $_GET['ccoordy'] . "\n" .
        "speed:" . $_GET['speed'];
    fwrite($myfile, $coords);
    fclose($myfile);
}
else {
?>

<?php
}
?>
</body>
</html>