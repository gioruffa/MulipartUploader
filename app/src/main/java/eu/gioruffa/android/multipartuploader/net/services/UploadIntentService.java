package eu.gioruffa.android.multipartuploader.net.services;

import android.app.IntentService;
import android.content.Intent;
import android.content.Context;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;
import com.google.gson.Gson;


import org.apache.http.HttpEntity;
import org.apache.http.entity.ContentType;
import org.apache.http.entity.mime.HttpMultipartMode;
import org.apache.http.entity.mime.MultipartEntityBuilder;


import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;

import eu.gioruffa.android.multipartuploader.net.pojo.FileMetadata;

/**
 * An {@link IntentService} subclass for handling asynchronous task requests in
 * a service on a separate handler thread.
 * <p>
 * helper methods.
 */
public class UploadIntentService extends IntentService {
    private static final String TAG = UploadIntentService.class.getSimpleName();

    private static final String ACTION_UPLOAD_FILE =
            "eu.gioruffa.android.multipartuploader.net.services.action.ACTION_UPLOAD_FILE";

    private static final String EXTRA_FILE_PATH =
            "eu.gioruffa.android.multipartuploader.net.services.extra.FILE_PATH";

    public static final String ACTION_UPLOAD_RESULT =
            "eu.gioruffa.android.multipartuploader.net.services.action.ACTION_UPLOAD_RESULT";


    public static final String EXTRA_UPLOAD_RESULT =
            "eu.gioruffa.android.multipartuploader.net.services.extra.UPLOAD_RESULT";

    public static final String VALUE_UPLOAD_RESULT_OK = "OK";

    public static final String VALUE_UPLOAD_RESULT_FAILED = "FAILED";

    private static final String UPLOAD_ENDPOINT = "http://10.0.2.2/upload.php";

    private Gson gson = new Gson();



    public UploadIntentService() {
        super("UploadIntentService");
    }

    /**
     * Starts this service to perform action UploadFile with the given parameters. If
     * the service is already performing a task this action will be queued.
     *
     * @see IntentService
     */
    public static void startActionUploadFile(Context context, String filePath) {
        Intent intent = new Intent(context, UploadIntentService.class);
        intent.setAction(ACTION_UPLOAD_FILE);
        intent.putExtra(EXTRA_FILE_PATH, filePath);
        context.startService(intent);
    }


    @Override
    protected void onHandleIntent(Intent intent) {
        if (intent != null) {
            final String action = intent.getAction();
            if (ACTION_UPLOAD_FILE.equals(action)) {
                final String filePath = intent.getStringExtra(EXTRA_FILE_PATH);
                handleActionUploadFIle(filePath);
            }
        }
    }

    /**
     * Handle action UploadFile in the provided background thread with the provided
     * parameters.
     */
    private void handleActionUploadFIle(String filePath) {
        String stringReply = "";
        HttpURLConnection conn = null;

        try {
            //LISTING_AFTER_1
            //create the payload
            String fileOwner = "gioruffa";
            String [] fileTags = {"cute","anime"};
            FileMetadata fileMetadata = new FileMetadata(
                    fileOwner,
                    fileTags
            );

            String metadataJsonString = gson.toJson(fileMetadata);
            //LISTING_BEFORE_1
            //LISTING_AFTER_2
            //Create a Multipart Builder which we'll use to forge the request
            MultipartEntityBuilder multipartBuilder = MultipartEntityBuilder.create();
            multipartBuilder.setMode(HttpMultipartMode.STRICT);

            //add the metadata
            multipartBuilder.addTextBody(
                    "picmetadata",
                    metadataJsonString,
                    ContentType.APPLICATION_JSON
            );

            //get the file to upload
            File fileToUpload = new File(filePath);
            InputStream is = new FileInputStream(fileToUpload);
            //encode the file and add it to the request
            multipartBuilder.addBinaryBody(
                    "file",
                    is,
                    ContentType.APPLICATION_OCTET_STREAM,
                    fileToUpload.getName()
            );

            HttpEntity multipartEntity = multipartBuilder.build();
            //LISTING_BEFORE_2
            //LISTING_AFTER_3
            //We are all set to make the connection
            URL url = new URL(UPLOAD_ENDPOINT);
            conn = (HttpURLConnection) url.openConnection();
            conn.setRequestMethod("POST");
            conn.setUseCaches(false);
            conn.setDoInput(true);
            conn.setDoOutput(true);

            //To be HTTP 1.0 compatible
            conn.setRequestProperty("Connection", "Keep-Alive");

            conn.addRequestProperty(
                    multipartEntity.getContentType().getName(),
                    multipartEntity.getContentType().getValue()
            );

            //Write the request to the connection object
            OutputStream os = conn.getOutputStream();
            multipartEntity.writeTo(conn.getOutputStream());
            os.flush();
            os.close();

            //Up to this point no network operations were made, we just prepared the request.
            //This is the blocking network call that will actually communicate with the web server.
            conn.connect();

            //Process the reply from the server
            InputStream replyStream = new BufferedInputStream(conn.getInputStream());
            BufferedReader br = new BufferedReader(new InputStreamReader(replyStream));

            StringBuilder sb = new StringBuilder();
            String line;

            while ((line = br.readLine()) != null) {
                sb.append(line);
            }
            br.close();

            stringReply = sb.toString();
            Log.d(TAG, "REPLY: " + stringReply);
            //LISTING_BEFORE_3
            broadcastUploadResult(VALUE_UPLOAD_RESULT_OK);
        }
        catch (Exception ex)
        {
            ex.printStackTrace();
            broadcastUploadResult(VALUE_UPLOAD_RESULT_FAILED);
        }
        finally
        {
            if (conn != null )
            {
                conn.disconnect();
            }
        }

    }

    private void broadcastUploadResult(String result)
    {
        LocalBroadcastManager localBroadcastManager =
                LocalBroadcastManager.getInstance(getApplicationContext());
        Intent it = new Intent(ACTION_UPLOAD_RESULT);
        it.putExtra(EXTRA_UPLOAD_RESULT,result);
        localBroadcastManager.sendBroadcast(it);
    }

}
