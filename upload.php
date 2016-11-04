<?php

$UPLOAD_DIR = "/tmp/"; # where to store uploaded files

#You may need to redefine this in older php versions
#function http_response_code($code)
#{
#	header('X-PHP-Response-Code: '.$code, true, $code);
#}

//validate the post, we need at least the metadata
if ( !isset($_POST["picmetadata"]) )
{
	http_response_code(400); # bad request error code
	die();
}

$picMetadata = json_decode($_POST["picmetadata"],false);

if (json_last_error() != JSON_ERROR_NONE )
{
	http_response_code(400);
	die();
}

//validate metadata
if (! (isset($picMetadata->owner) && isset($picMetadata->tags)) )
{
	http_response_code(400);
	die();
}


//get the metadata and log them
$fileOwner = $picMetadata->owner;
$tags = $picMetadata->tags;
error_log("owner: ".$fileOwner." tags:".print_r($tags,true));

//check if we have the "file" field in the multipar request
if (! isset($_FILES["file"]))
{
	http_response_code(400);
	die();
}

//check if the upload was completed successfully
if ($_FILES["file"]["error"] != UPLOAD_ERR_OK )
{
	http_response_code(500);
	die();
}

//retrieve the original file name
$fileName = $_FILES["file"]["name"];

//if the output directory does not exist, create it.
if (!file_exists($UPLOAD_DIR)) {
    if (!mkdir($UPLOAD_DIR,0755, true)) //create the directory and check the result
	{
		error_log("Unable to create directory: ".$UPLOAD_DIR);
		http_response_code(500);
		die;
	}
}

//now we can get the file and move it to its final destination
$fileDest = $UPLOAD_DIR."/".$fileName;

if (!move_uploaded_file($_FILES["file"]["tmp_name"],$fileDest))
{
	http_response_code(500);
	die();
}

//all good in the hood
//the client will receive a 200 HTTP response code
?>
