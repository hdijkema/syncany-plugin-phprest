<?php

$userid = $_POST[userid];
$passwd = $_POST[passwd];
$context = $_POST[passwd];
$action = $_POST[action];

$dir_context = "syncany/$context";

if ($action) {
  if ($context) {
    if (is_dir($dir_context)) {
      if ($action == "login") {
        echo action_login($userid, $passwd, $dir_context);
      } else if ($action == "exists") {
        $file = $_POST[filename];
        echo action_exists($userid, $passwd, $dir_context, $file);
      } else if ($action == "list") {
        $type = $_POST[type];
        echo action_list($userid, $passwd, $dir_context, $type);
      } else if ($action == "upload") {
        $file = $_POST[filename];
        echo action_upload($userid, $passwd, $dir_context, $file);
      } else if ($action == "download") {
        $file = $_POST[filename];
        echo action_download($userid, $passwd, $dir_context, $file);
      } else if ($action == "delete") {
        $file = $_POST[filename];
        echo action_delete($userid, $passwd, $dir_context, $file);
      } else if ($action == "move") {
        $file = $_POST[filename];
        $fileto = $_POST[to_filename];
        echo action_delete($userid, $passwd, $dir_context, $file, $fileto);
      }
    } else {
      echo "nok - context '$context' doesn't exist";
    }
  } else {
    echo "nok - no context";
  }
} else {
  echo "nok - no action";
}

function action_login($userid, $passwd, $dir_context) {
  $file_users = "$dir_context/users";
  $fh = fopen($file_users,"rt");
  if ($fh) {
    while ($line = fgets($fh)) {
       $line = trim($line);
       list($user, $pass) = preg_split("/:/", $line, 2);
       if ($user == $userid && $pass == $passwd) {
         fclose($fh);
         return "true";
       }
    }
    fclose($fh);
    return "nok - userid or password not correct";
  } else {
    return "nok - login - no user file ($file_users)";
  }
}

function action_exists($userid, $passwd, $dir_context, $name) {
  $li = action_login($userid, $passwd, $dir_context);
  if ($li == "true") {
    $name = trim($name);
    if (preg_match("/[.][.]/",$name)) {
      return "nok - '$name' is invalid";
    } else if ($name == "" || $name == ".") {
      return "nok - '$name' is an invalid filename";
    } else {
      $file = "$dir_context/$name";
      if (file_exists($file)) {
        error_log("file exists: $file");
        return "true";
      } else {
        return "nok - '$file' does not exist";
      }
    }
  } else {
    return $li;
  }
}

function action_list($userid, $passwd, $dir_context, $type) {
  error_log("listing for type $type");
  $li = action_login($userid, $passwd, $dir_context);
  if ($li == "true") {
    $dir = "$dir_context";
    echo "true\n";
    $d = dir($dir);
    $m = "/^$type/";
    while (false !== ($entry = $d->read())) {
      error_log("checking entry $entry");
      if (preg_match($m, $entry)) {
        error_log("listing entry $entry");
        echo "$entry\n";
      }
    }
    $d->close();
    return "";
  } else {
    return $li;
  }
}

function action_upload($userid, $passwd, $dir_context, $name) {
  $li = action_login($userid, $passwd, $dir_context);
  if ($li == "true") {
    $tmpname = $_FILES[file][tmp_name];
    $size = $_FILES[file][size];
    $name = trim($name);
    if (preg_match("/[.][.]/",$name)) {
      return "nok - '$name' is invalid";
    } else if ($name == "" || $name == ".") {
      return "nok - '$name' is an invalid filename";
    } else {
      $file = "$dir_context/$name";
      if (copy($tmpname, $file)) {
        error_log("uploaded $tmpname (size=$size) to $file");
        if (file_exists($file)) {
          error_log("file $file exists");
        }
        unlink($tmpname);
        return "true";
      } else {
        unlink($tmpname);
        return "nok - copy failed name = $name, tmpname = $tmpname, size = $size";
      }
    }
  } else {
    return $li;
  }
}

function action_download($userid, $passwd, $dir_context, $name) {
  $li = action_login($userid, $passwd, $dir_context);
  if ($li == "true") {
    $tmpname = $_FILES[file][tmp_name];
    $size = $_FILES[file][size];
    $name = trim($name);
    if (preg_match("/[.][.]/",$name)) {
      return "nok - '$name' is invalid";
    } else if ($name == "" || $name == ".") {
      return "nok - '$name' is an invalid filename";
    } else {
      $file = "$dir_context/$name";
      if (file_exists($file)) {
        error_log("file can be downloaded: $file");
        echo "true\n";
        readfile($file);
        return "";
      } else {
        return "nok - '$file' does not exist";
      }
    }
  } else {
    return $li;
  }
}

function action_delete($userid, $passwd, $dir_context, $name) {
  $li = action_login($userid, $passwd, $dir_context);
  if ($li == "true") {
    $name = trim($name);
    if (preg_match("/[.][.]/",$name)) {
      return "nok - '$name' is invalid";
    } else if ($name == "" || $name == ".") {
      return "nok - '$name' is an invalid filename";
    } else {
      $file = "$dir_context/$name";
      if (file_exists($file)) {
        error_log("file deleted: $file");
        unlink($file);
        return "true";
      } else {
        return "nok - '$file' does not exist";
      }
    }
  } else {
    return $li;
  }
}

function action_move($userid, $passwd, $dir_context, $name, $name_to) {
  $li = action_login($userid, $passwd, $dir_context);
  if ($li == "true") {
    $name = trim($name);
    $name_to = trim($name_to);
    if (preg_match("/[.][.]/",$name)) {
      return "nok - '$name' is invalid";
    } else if ($name == "" || $name == ".") {
      return "nok - '$name' is an invalid filename";
    } else if (preg_match("/[.][.]/",$name_to)) {
      return "nok - '$name_to' (to) is invalid";
    } else if ($name_to == "" || $name_to == ".") {
      return "nok - '$name_to' (to) is an invalid filename";
    } else {
      $file = "$dir_context/$name";
      $file_to = "$dir_context/$name_to";
      error_log("move: $file to $file_to");
      if (file_exists($file)) {
        if (file_exists($file_to)) {
          return "nok - '$file_to' (to) already exists";
        } else {
          if (rename($file,$file_to)) {
            error_log("file renamed: $file to $file_to");
            return "true";
          } else {
            return "nok - cannot rename '$file'  to '$file_to'";
          }
        }
      } else {
        return "nok - '$file' does not exist";
      }
    }
  } else {
    return $li;
  }
}


?>
