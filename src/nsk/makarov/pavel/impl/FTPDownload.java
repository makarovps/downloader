package nsk.makarov.pavel.impl;

import nsk.makarov.pavel.model.Download;
import nsk.makarov.pavel.model.DownloadException;
import nsk.makarov.pavel.model.DownloadState;

import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLConnection;

import static java.net.HttpURLConnection.HTTP_OK;

/**
 * Created by pavel on 07.04.17.
 */
public class FTPDownload extends Download {
    private static final int MAX_BUFFER_SIZE = 1024;

    public FTPDownload(String source, String dest) {
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
            URLConnection connection = url.openConnection();

            /* Connect to server. */
            connection.connect();

            /* Check for valid content length. */
            int contentLength = connection.getContentLength();
            if (contentLength < 1) {
                fail("Invalid file size");
                return;
            }

            /* Resume download will start with the very beginning */
            setCurrentsize(0);

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
