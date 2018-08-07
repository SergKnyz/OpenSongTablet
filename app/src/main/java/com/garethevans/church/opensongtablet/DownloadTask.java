package com.garethevans.church.opensongtablet;

import android.annotation.SuppressLint;
import android.content.Context;
import android.os.AsyncTask;
import android.support.v4.provider.DocumentFile;

import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;

class DownloadTask extends AsyncTask<String, Integer, String> {

    public interface MyInterface {
        void openFragment();
    }

    private MyInterface mListener;
    private String address;
    private String filename;
    @SuppressLint("StaticFieldLeak")
    private Context c;
    private DocumentFile homeFolder;
        DownloadTask(Context context, DocumentFile home, String address) {
            homeFolder = home;
            this.address = address;
            this.c = context;
            mListener = (MyInterface) context;
            switch (FullscreenActivity.whattodo) {
                case "download_band":
                    filename = "Band.osb";
                    break;
                case "download_church":
                    filename = "Church.osb";
                    break;
                default:
                    filename = "Download.osb";
                    break;
            }
        }

        @Override
        protected String doInBackground(String... sUrl) {
            InputStream input = null;
            OutputStream output = null;
            HttpURLConnection connection = null;
            try {
                URL url = new URL(address);
                connection = (HttpURLConnection) url.openConnection();
                connection.connect();

                // expect HTTP 200 OK, so we don't mistakenly save error report
                // instead of the file
                if (connection.getResponseCode() != HttpURLConnection.HTTP_OK) {
                    return "Server returned HTTP " + connection.getResponseCode()
                            + " " + connection.getResponseMessage();
                }

                // this will be useful to display download percentage
                // might be -1: server did not report the length
                int fileLength = connection.getContentLength();

                // download the file
                input = connection.getInputStream();
                StorageAccess storageAccess = new StorageAccess();
                output = storageAccess.getOutputStream(c,homeFolder,"","",filename);

                byte data[] = new byte[4096];
                long total = 0;
                int count;
                while ((count = input.read(data)) != -1) {
                    // allow cancelling with back button
                    if (isCancelled()) {
                        input.close();
                        return null;
                    }
                    total += count;
                    // publishing the progress....
                    if (fileLength > 0) // only if total length is known
                        publishProgress((int) (total * 100 / fileLength));
                    output.write(data, 0, count);
                }
            } catch (Exception e) {
                return e.toString();
            } finally {
                try {
                    if (output != null)
                        output.close();
                    if (input != null)
                        input.close();
                } catch (Exception ignored) {
                    ignored.printStackTrace();
                }

                if (connection != null)
                    connection.disconnect();
            }
            return null;
        }

        @Override
        protected void onPostExecute(String s) {
            FullscreenActivity.whattodo = "processimportosb";
            StorageAccess sa = new StorageAccess();
            FullscreenActivity.filechosen = sa.getFileLocationAsDocumentFile(c, homeFolder,"","",filename);
            if (mListener!=null) {
                mListener.openFragment();
            }
        }
}
