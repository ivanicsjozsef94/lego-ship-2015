<!doctype html>
<html lang="en">
<head>
  <meta charset="utf-8">
  <title>Lego Ship 2015 Group 4 Startpage</title>
  <link rel="stylesheet" type="text/css" href="style.css">
</head>
<body>
<?php
if ($_GET) {
  if ($_GET['fromandroid'] && $_GET['fromandroid'] == 1) {
    header('Location: ./jsondata.php');
    exit();
  }
  else if ($_GET['tcoordx'] && $_GET['tcoordy'] && $_GET['fcoordx'] && $_GET['fcoordy']) {
    $myfile = fopen("./coordinates.txt", "w") or die("Unable to open file!");
    $coords =
        "tcoordx:" . $_GET['tcoordx'] . "\n".
        "tcoordy:" . $_GET['tcoordy'] . "\n" .
        "fcoordx:" . $_GET['fcoordx'] . "\n" .
        "fcoordy:" . $_GET['fcoordy'];
    fwrite($myfile, $coords);
    fclose($myfile);
    echo "Coordinates sent!";
    echo "<br>Transit point coordinates:<br>X: " . $_GET['tcoordx'] . ", Y: " . $_GET['tcoordy'];
    echo "<br>Finish point coordinates:<br>X: " . $_GET['fcoordx'] . ", Y: " . $_GET['fcoordy'];
    header( "Refresh: 3; url=tracking.php" );
    exit();
  }
  else {
    echo "Fill all the coordinate values!";
  }
}
else {
?>
  <form action="<?=$_SERVER['PHP_SELF']?>" method="GET">
    <!--<fieldset>
      <legend>Starting point</legend>
      <p><label>X coordinate:<br><input type="text" name="scoordx"></label></p>
      <p><label>Y coordinate:<br><input type="text" name="scoordy"></label></p>
    </fieldset>-->
    <fieldset>
      <legend>Transit point</legend>
      <p><label>X coordinate:<br><input type="text" name="tcoordx"></label></p>
      <p><label>Y coordinate:<br><input type="text" name="tcoordy"></label></p>
    </fieldset>
    <fieldset>
      <legend>Finish point</legend>
      <p><label>X coordinate:<br><input type="text" name="fcoordx"></label></p>
      <p><label>Y coordinate:<br><input type="text" name="fcoordy"></label></p>
    </fieldset>
    <p><input type="submit" name="submit" id="submit" value="Send"></p>
  </form>
<?php
}
?>
</body>
</html>