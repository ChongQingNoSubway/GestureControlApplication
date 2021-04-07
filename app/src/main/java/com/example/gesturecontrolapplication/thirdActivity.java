package com.example.gesturecontrolapplication;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.Manifest;
import android.app.DownloadManager;
import android.app.ProgressDialog;
import android.content.ContentUris;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.provider.DocumentsContract;
import android.provider.MediaStore;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.MimeTypeMap;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.Toast;
import android.widget.VideoView;

import org.apache.commons.io.FileUtils;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.Buffer;
import java.nio.file.Files;


public class thirdActivity extends AppCompatActivity{

    static int REQUEST_VIDEO_CAPTURE = 0;
    VideoView recordedVideoView;
    Uri recordedVideoUri;
    String gestureName;
    private String selected;
    private static String serverAddress = "http://127.0.0.1:8088/";
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_third);

        Intent gesturePracticeIntent = getIntent();
        gestureName = getIntent().getStringExtra("selected");

        Toast.makeText(thirdActivity.this,
                "Practice Gesture " + gestureName, Toast.LENGTH_LONG).show();

        if(ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED){
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.CAMERA}, 200);
        }
        if(ContextCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED){
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.READ_EXTERNAL_STORAGE, Manifest.permission.WRITE_EXTERNAL_STORAGE},
                    200);
        }

    }

    public void captureVideo(View view){
        REQUEST_VIDEO_CAPTURE = 1;
        dispatchTakeVideoIntent();
    }

    public void uploadVideo(View view){
        if(recordedVideoUri.toString().length() == 0){
            Toast.makeText(thirdActivity.this, "Please capture the gesture video",
                    Toast.LENGTH_LONG).show();
        }else{
            GestureVideoUploadTask uploadVideo = new GestureVideoUploadTask();
            String actualPath = getActualPath(thirdActivity.this, recordedVideoUri);
            File gestureRecordFile = new File(actualPath);
            String newFileName = actualPath.substring(0,actualPath.lastIndexOf("/"))+"/"+
                    gestureName+".mp4";
            try{
                FileUtils.copyFile(gestureRecordFile, new File(newFileName));
            }catch(Exception e){
                Log.d("File Rename Error", "Couldn't rename file");
            }

            Log.d("NewFileName",newFileName);
            uploadVideo.execute(newFileName);
        }
    }

    private void dispatchTakeVideoIntent(){
        Intent takeVideoIntent = new Intent(MediaStore.ACTION_VIDEO_CAPTURE);
        if (takeVideoIntent.resolveActivity(getPackageManager()) != null) {
            startActivityForResult(takeVideoIntent, REQUEST_VIDEO_CAPTURE);
        }
    }

    public void backToMain(View view){
        Intent learnNewGestureIntent = new Intent(thirdActivity.this, thirdActivity.class);
        startActivity(learnNewGestureIntent);
    }

    private String getActualPath(final Context context, final Uri uri){
        final boolean isKitKat = Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT;

        // DocumentProvider
        if (isKitKat && DocumentsContract.isDocumentUri(context, uri)) {
            // ExternalStorageProvider
            if (isExternalStorageDocument(uri)) {
                final String docId = DocumentsContract.getDocumentId(uri);
                final String[] split = docId.split(":");
                final String type = split[0];

                if ("primary".equalsIgnoreCase(type)) {
                    return Environment.getExternalStorageDirectory() + "/" + split[1];
                }

                // TODO handle non-primary volumes
            }
            // DownloadsProvider
            else if (isDownloadsDocument(uri)) {

                final String id = DocumentsContract.getDocumentId(uri);
                final Uri contentUri = ContentUris.withAppendedId(
                        Uri.parse("content://downloads/public_downloads"), Long.valueOf(id));

                return getDataColumn(context, contentUri, null, null);
            }
            // MediaProvider
            else if (isMediaDocument(uri)) {
                final String docId = DocumentsContract.getDocumentId(uri);
                final String[] split = docId.split(":");
                final String type = split[0];

                Uri contentUri = null;
                if ("image".equals(type)) {
                    contentUri = MediaStore.Images.Media.EXTERNAL_CONTENT_URI;
                } else if ("video".equals(type)) {
                    contentUri = MediaStore.Video.Media.EXTERNAL_CONTENT_URI;
                } else if ("audio".equals(type)) {
                    contentUri = MediaStore.Audio.Media.EXTERNAL_CONTENT_URI;
                }

                final String selection = "_id=?";
                final String[] selectionArgs = new String[] {
                        split[1]
                };

                return getDataColumn(context, contentUri, selection, selectionArgs);
            }
        }
        // MediaStore (and general)
        else if ("content".equalsIgnoreCase(uri.getScheme())) {
            return getDataColumn(context, uri, null, null);
        }
        // File
        else if ("file".equalsIgnoreCase(uri.getScheme())) {
            return uri.getPath();
        }

        return null;
    }

    public static String getDataColumn(Context context, Uri uri, String selection,
                                       String[] selectionArgs) {

        Cursor cursor = null;
        final String column = "_data";
        final String[] projection = {
                column
        };

        try {
            cursor = context.getContentResolver().query(uri, projection, selection, selectionArgs,
                    null);
            if (cursor != null && cursor.moveToFirst()) {
                final int column_index = cursor.getColumnIndexOrThrow(column);
                return cursor.getString(column_index);
            }
        } finally {
            if (cursor != null)
                cursor.close();
        }
        return null;
    }


    /**
     * @param uri The Uri to check.
     * @return Whether the Uri authority is ExternalStorageProvider.
     */
    public static boolean isExternalStorageDocument(Uri uri) {
        return "com.android.externalstorage.documents".equals(uri.getAuthority());
    }

    /**
     * @param uri The Uri to check.
     * @return Whether the Uri authority is DownloadsProvider.
     */
    public static boolean isDownloadsDocument(Uri uri) {
        return "com.android.providers.downloads.documents".equals(uri.getAuthority());
    }

    /**
     * @param uri The Uri to check.
     * @return Whether the Uri authority is MediaProvider.
     */
    public static boolean isMediaDocument(Uri uri) {
        return "com.android.providers.media.documents".equals(uri.getAuthority());
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent intent) {
        super.onActivityResult(requestCode, resultCode, intent);
        recordedVideoView = new VideoView(this);
        RelativeLayout.LayoutParams layoutParams = new RelativeLayout.LayoutParams(
                ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);

        layoutParams.setMargins(10, 10, 10, 10);
        recordedVideoView.setLayoutParams(layoutParams);

        if (requestCode == REQUEST_VIDEO_CAPTURE && resultCode == RESULT_OK) {
            RelativeLayout relativeLayout = findViewById(R.id.screen_3);
            // Add VideoView to Layout
            if (relativeLayout != null) {
                relativeLayout.addView(recordedVideoView);
            }
            recordedVideoUri = intent.getData();
            Log.d("RecordedVideo",recordedVideoUri.toString());
            String fullPath = getActualPath(thirdActivity.this, recordedVideoUri);
            Log.d("fullVideo",fullPath);
//            recordedVideoView.setVideoURI(Uri.parse(fullPath));
            recordedVideoView.setVideoURI(Uri.fromFile(new File(fullPath)));
            recordedVideoView.start();


        }
    }

    private class GestureVideoUploadTask extends AsyncTask<String, String, String> {

        private ProgressDialog progressDialog;
        private String videoLocationString;
        private final String uploadServer = serverAddress;
        private HttpURLConnection httpConn;
        private DataOutputStream request;
        private final String boundary =  "*****";
        private final String lineEnd = "\r\n";
        private final String twoHyphens = "--";
        int bytesRead, bytesAvailable, bufferSize;
        byte[] buffer;
        int maxBufferSize = 1 * 1024 * 1024;

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            this.progressDialog = new ProgressDialog(thirdActivity.this);
            this.progressDialog.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
            this.progressDialog.setCancelable(false);
            this.progressDialog.show();
        }

        @Override
        protected void onPostExecute(String message) {
            // dismiss the dialog after the file was downloaded
            Log.d("Execution complete", "Yes");
            this.progressDialog.dismiss();

            // Display File path after downloading
            Toast.makeText(getApplicationContext(),
                    message, Toast.LENGTH_LONG).show();
        }

        @Override
        protected void onProgressUpdate(String... values) {
            progressDialog.setProgress(Integer.parseInt(values[0]));
        }

        @Override
        protected String doInBackground(String... strings) {
            videoLocationString = strings[0];
            String message = "";
            Log.d("Record Video location", videoLocationString);
            try{
                File videoSource = new File(videoLocationString);
                Log.d("VideoSource",videoSource.getName());
                FileInputStream inputStream = new FileInputStream(videoSource);
                URL serverURL = new URL(
                        "https://192.168.0.81:8088/predic");
                httpConn = (HttpURLConnection) serverURL.openConnection();
                httpConn.setUseCaches(false);
                httpConn.setDoOutput(true); // indicates POST method
                httpConn.setDoInput(true);
                httpConn.setRequestMethod("POST");
                httpConn.setRequestProperty("Connection", "Keep-Alive");
                httpConn.setRequestProperty("Cache-Control", "no-cache");
                httpConn.setRequestProperty("ENCTYPE", "multipart/form-data");
                httpConn.setRequestProperty("uploaded_file",videoSource.getName());
                httpConn.setRequestProperty(
                        "Content-Type", "multipart/form-data;boundary=" + this.boundary);


                request =  new DataOutputStream(httpConn.getOutputStream());
                request.writeBytes(twoHyphens + boundary + lineEnd);
                request.writeBytes("Content-Disposition: form-data; name=\"file\";filename=\""
                        + videoSource + "\"" + lineEnd);
                request.writeBytes(lineEnd);

                bytesAvailable = inputStream.available();
                bufferSize = Math.min(bytesAvailable, maxBufferSize);
                buffer = new byte[bufferSize];

                bytesRead = inputStream.read(buffer, 0, bufferSize);

                Log.d("Bytes read", "Yes");
                long total = 0;
                long file_length = videoSource.length();
                Log.d("Source file length", String.valueOf(file_length));

                while(bytesRead > 0){
                    Log.d("Bytes read", "Yes");
                    try{
                        total+=bytesRead;
                        request.write(buffer, 0, bufferSize);
                        publishProgress("" + (int) ((total * 100) / file_length));
                    }catch (OutOfMemoryError e) {
                        e.printStackTrace();
                        message = "outofmemoryerror";
                        return message;
                    }
                    bytesAvailable = inputStream.available();
                    bufferSize = Math.min(bytesAvailable, maxBufferSize);
                    bytesRead = inputStream.read(buffer, 0, bufferSize);
                }

                request.writeBytes(lineEnd);
                request.writeBytes(twoHyphens + boundary + twoHyphens + lineEnd);
                Log.d("Writing Done", "Into responseCode");


                int responseCode = httpConn.getResponseCode();
                Log.d("response code", String.valueOf(responseCode));
                String responseMessage = httpConn.getResponseMessage();
                Log.d("response message", responseMessage);
                if(responseCode == 200){
                    message = "Video Uploaded successfully";
                }else{
                    message = "Failed to upload the video";
                }
                inputStream.close();
                request.flush();
                request.close();
                httpConn.disconnect();
            }catch(Exception error){
                Log.e("Fail message", error.toString());
                message = error.getMessage();
                return message;
            }

            return message;
        }


    }


}