package eu.gioruffa.android.multipartuploader.gui.activities;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.res.AssetManager;
import android.content.res.Resources;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;

import eu.gioruffa.android.multipartuploader.R;
import eu.gioruffa.android.multipartuploader.net.services.UploadIntentService;

/**
 * thanks to http://mrrevenge.deviantart.com/art/Birthday-Totoro-534613534
 */
public class MainActivity extends AppCompatActivity {

    String sourceFilePath;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        sourceFilePath = getCacheDir()+"/totoro-cc.jpg";
        copyAssetToCache();

        //Register the receiver to get results
        IntentFilter intentFilter = new IntentFilter(
                UploadIntentService.ACTION_UPLOAD_RESULT
        );
        LocalBroadcastManager.getInstance(this).registerReceiver(
                uploadResultBroadcastReceiver,
                intentFilter
        );
        Button uploadButton = (Button) findViewById(R.id.main_button);
        uploadButton.setOnClickListener(
                new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        UploadIntentService.startActionUploadFile(
                                getApplicationContext(),
                                sourceFilePath
                                );
                    }
                }
        );
    }

    BroadcastReceiver uploadResultBroadcastReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            if (intent.getAction().equals(UploadIntentService.ACTION_UPLOAD_RESULT))
            {
                String uploadResult = intent.getStringExtra(
                        UploadIntentService.EXTRA_UPLOAD_RESULT
                );
                if (uploadResult.equals(UploadIntentService.VALUE_UPLOAD_RESULT_OK))
                {
                    Toast.makeText(MainActivity.this,"Upload successful.",Toast.LENGTH_LONG).show();
                }
                else if (uploadResult.equals(UploadIntentService.VALUE_UPLOAD_RESULT_FAILED))
                {
                    Toast.makeText(MainActivity.this,"Upload failed!",Toast.LENGTH_LONG).show();
                }
            }
        }
    };

    /**
     * In order to upload a real file it's necessary to copy the asset on the filesystem.
     * The cache directory should work fine.
     */
    private void copyAssetToCache()
    {
        File f = new File(getCacheDir()+"/totoro-cc.jpg");
        if (!f.exists()) try {
            InputStream is = getAssets().open("totoro-cc.jpg");
            int size = is.available();
            byte[] buffer = new byte[size];
            is.read(buffer);
            is.close();


            FileOutputStream fos = new FileOutputStream(f);
            fos.write(buffer);
            fos.close();
        } catch (Exception e) { throw new RuntimeException(e); }

    }

}
