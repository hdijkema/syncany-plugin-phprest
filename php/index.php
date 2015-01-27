<html>
<head>
<title>Syncany PHP Backend Administration</title>
<link rel="stylesheet" type="text/css" href="style.css">
</head>
</head>
<body>
<?php
  if ($_SESSION[started] != "true") {
  	session_start();
  	$_SESSION[started] = "true";
  }
  
  $action = $_GET[action];
  
  if ($action == "install") {
  	mkdir("syncany");
  	$fh = fopen("syncany/.htaccess","wt");
  	fputs($fh,"Options -Indexes\n");
  	fclose($fh);
  	$account = $_POST[account];
  	$passwd = $_POST[password];
  	$account = trim($account);
  	$passwd = trim($passwd);
  	$fh = fopen("users","wt");
  	fputs($fh, "$account\n$passwd\n");
  	fclose($fh);
  	?>
  	<h2>Your Syncany PHP Backend has been created</h2>
  	<p>
  		Account : ' <?php echo $account ?>'<br />
  		Password: ' <?php echo $passwd ?>'<br />
  	</p>
  	<p><br />
  	  <a href="index.php?action=none">Now start using it by logging in</a>
  	</p>
  	<?php
  } else if (!file_exists("users")) {
  	?>
  	<h2>Welcome at the <a href="https://github.com/hdijkema/syncany-plugin-phprest" target="_blank">Syncany PHP Backend</a> administration site</h2>
  	<a href="http://syncany.org" target="_blank"><img class="logo" src="syncany-logo.png" alt="logo" /></a>
  	<p>Create an administrator account for your syncany PHP Backend.</p>
  	<form action="index.php?action=install" method="POST">
  		<table class="accountcreate"><tr><td>Account:</td><td><input type="text" name="account" /></td></tr>
  		<tr><td>Password:</td><td><input type="password" name="password" /></td></tr>
  		<tr><td></td><td><input type="submit" value="submit" /></td></tr>
  		</table>
  	</form>
  	<?php
  } else {
  	if ($action == "logout") {
  		$_SESSION[logged_in] = false;
  	} else if ($action == "login") {
  		$account = $_POST[account];
  		$passwd = $_POST[password];
  		$fh = fopen("users","rt");
  		$a = fgets($fh);
  		$p = fgets($fh);
  		fclose($fh);
  		$a = trim($a);
  		$p = trim($p);
  		#echo "account = $a ($account)<br />";
  		#echo "passwd = $p ($passwd)<br />";
  		if (($a == $account) && ($p == $passwd)) {
  			$_SESSION[logged_in] = "true";
  		} else {
  			$_SESSION[logged_in] = "false";
  			?>
  			<p>Login failed</p>
  			<?php
  		}
  	}
  	
  	if ($_SESSION[logged_in] != "true") {
  		?>
  		  <h2><a href="https://github.com/hdijkema/syncany-plugin-phprest" target="_blank">Syncany PHP Backend</a> - Login</h2>
  		  <form action="index.php?action=login" method="POST">
  		  	<table class="login">
  		  	<tr><td rowspan="4" style="vertical-align: top;border: 0px solid black;width:70px;"><a href="http://syncany.org" target="_blank"><img class="logo" src="syncany-logo.png" alt="logo" /></a></td>
  		  	    <td>Account:</td><td><input type="text" name="account" /></td></tr>
  		  	<tr><td>Password:</td><td><input type="password" name="password" /></td></tr>
  		  	<tr><td /><td /></tr>
  		  	<tr><td /><td><input type="submit" value="submit" /></td></tr>
  		  	</table>
  		  </form>
  		<?php
  	} else {
  		?>
  		<table class="menu">
  		  <tr><th rowspan="2" style="border: 0px solid black;"><a href="http://syncany.org" target="_blank"><img class="logo" src="syncany-logo.png" alt="logo" /></a>
  		      </th><th colspan="3"><h2><a href="https://github.com/hdijkema/syncany-plugin-phprest" target="_blank">Syncany PHP Backend</a> - Administer places</h2></td></tr>
  		  <tr><td><a href="index.php">clear</a></td><td><a href="index.php?action=logout">logout</a></td><td width="80%"/></tr>
  		  </table>
  		  <hr />
  		<?php
  		if ($action == "new_entry") {
  			$entry = $_POST[entry];
  			$password = $_POST[password];
  			$entry = trim($entry);
  			$password = trim($password);
  			if($entry == "") {
  				echo "<b>Cannot create entry '$entry'</b>";
  		  } else {
  				if (file_exists("syncany/$entry")) {
  					echo "<b>Entry '$entry' already exists</b>";
  				} else {
  					mkdir("syncany/$entry");
  					$fh = fopen("syncany/$entry/users","wt");
  					fputs($fh,"$entry:$password\n");
  					fclose($fh);
  				}
  			}
  		} else if ($action == "entry") {
  			$entry = $_GET[entry];
  			?>
  			<h3>Context: <?php echo $entry ?></h3>
  			<table class="entrydetail">
  			 <tr><td>user id:</td><td><?php echo $entry ?></td></tr>
  			 <?php 
  			 	 $fh = fopen("syncany/$entry/users","rt");
  			 	 $up = fgets($fh);
  			 	 trim($up);
  			 	 list($entry,$passwd) = preg_split("/:/",$up,2);
  			 	 fclose($fh);
  			 ?>
  			 <tr><td>password:</td><td><?php echo $passwd ?></td></tr>
  			 <tr><td>size:</td><td>
  			   <?php 
  			     $bytes=getDirectorySize("syncany/$entry");
  			     $kb = bcdiv("$bytes", "1024");
  			     $mb = bcdiv("$bytes", "1048576", 1);
  			     $gb = bcdiv("$bytes", "1073741824", 1);
  			     printf("%s bytes, %s KB, %s MB, %s GB", $bytes,$kb,$mb,$gb); 
  			   ?>
  			  </td></tr>
  			</table>
  			<hr />
  		<?php
  		}
  		?>
  		  <h3>Backends</h3>
  		  <table class="entries">
  		  <?php
  		      $d = dir("syncany/");
  		      while (false !== ($entry = $d->read())) {
  		      	if ($entry == "." || $entry ==".." || substr($entry,0,1) == ".") {
  		      	} else {
  		      		echo "<tr><td><a href=\"index.php?action=entry&entry=$entry\">$entry</a></td></tr>";
  		      	}
  		      }
  		      $d->close();
  		   ?>
  		   </table>
  		   <hr />
  		   <h3>New backend</h3>
  		   <form action="index.php?action=new_entry" method="POST">
  		      <table class="newplace"><tr><td>Syncany place:</td><td><input type="text" name="entry" /></td></tr>
  		      <tr><td>Password:</td><td><input type="text" name="password" /></td></tr>
  		      <tr><td /><td><input type="submit" value="submit" /></td></tr>
  		      </table>
  		   </form>
  		   <?php
  	}
  }
  
  function getDirectorySize($path){
    $bytestotal = "0";
    $path = realpath($path);
    if($path!==false){
        foreach(new RecursiveIteratorIterator(new RecursiveDirectoryIterator($path, FilesystemIterator::SKIP_DOTS)) as $object){
        	$bytestotal = bcadd($bytestotal, "".$object->getSize());
        }
    }
    return $bytestotal;
}
?>
</body>
</html>