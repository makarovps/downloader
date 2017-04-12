package nsk.makarov.pavel.impl;

import nsk.makarov.pavel.model.Download;
import nsk.makarov.pavel.model.DownloadException;
import nsk.makarov.pavel.model.DownloadState;

import javax.swing.plaf.synth.SynthEditorPaneUI;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.RandomAccessFile;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.channels.Channels;
import java.nio.channels.ReadableByteChannel;

import static java.net.HttpURLConnection.HTTP_OK;

/**
 * Created by pavel on 07.04.17.
 */
public class HTTPDownload extends Download {
    private static final int MAX_BUFFER_SIZE = 1024;
    
    public HTTPDownload(String source, String dest) {
        super(source, dest);
    }

    @Override
    public void run() {
        setState(DownloadState.IN_PROGRESS);

        URL url = null;
        try {
            url = Download.validateSource(getSource());
        } catch (DownloadException e) {
            fail(e.getMessage());
            return;
        }

        try {
            /* Open connection to the url. */
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();

            /* Specify what portion of file to download. */
            connection.setRequestProperty("Range", "bytes=" + getCurrentsize() + "-");

            /* Connect to server. */
            connection.connect();

            /* Make sure response code is 200 */
            if (connection.getResponseCode() != HTTP_OK) {
                fail(connection.getResponseMessage());
                return;
            }

            /* Check for valid content length. */
            int contentLength = connection.getContentLength();
            if (contentLength < 1) {
                fail("Invalid file size");
                return;
            }

            /* Check if server supports resume downloads */
            if (connection.getHeaderFields().get("Accept-Ranges") == null) {
                setCurrentsize(0);
            }

            /* Set total size of download */
            if (getTotalsize() == 0) {
                setTotalsize(contentLength);
            }

            download(connection.getInputStream());
        } catch (IOException e) {
            fail(e.getMessage());
        }
    }

    @Override
    public void start() {
        DownloadState state = getState();
        if (state == DownloadState.COMPLETED) {
            setCurrentsize(0);
        }
        (new Thread(this)).start();
    }

    @Override
    public void pause() {
        setState(DownloadState.PAUSED);
    }

    @Override
    public void resume() {
        start();
    }

    @Override
    public void cancel() {
        setState(DownloadState.CANCELED);
    }

    @Override
    public void fail(String message) {
        setFailure(message);
        setState(DownloadState.FAILED);
    }
}
