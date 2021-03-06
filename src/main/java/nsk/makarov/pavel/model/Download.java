package nsk.makarov.pavel.model;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.RandomAccessFile;
import java.net.URL;
import java.nio.file.Paths;
import java.util.Observable;

/**
 * Created by pavel on 07.04.17.
 */
public abstract class Download extends Observable implements Downloadable, Runnable {
    private static int MAX_BUFFER_SIZE = 806;

    private String source;
    private String dest;
    private DownloadState state;
    private int currentsize;
    private int totalsize;
    private String failure;

    public static URL validateSource(String source) throws DownloadException {
        URL url = null;
        try {
            url = new URL(source);
        } catch (Exception e) {
            throw new DownloadException(e.getMessage());
        }

        if (url.getFile().length() < 2) {
            throw new DownloadException("Source url must specify a file");
        }

        return url;
    }

    public static boolean validateDest(String dest) throws DownloadException {
        File f = new File(dest);
        try {
            f.getCanonicalPath();
            return true;
        } catch (IOException e) {
            throw new DownloadException(e.getMessage());
        }
    }

    private String fileName() {
        return Paths.get(dest).toString() + File.separator + source.substring(source.lastIndexOf("/") + 1);
    }

    private void clear() {
        try {
            File file = new File(fileName());

            if (!file.delete()) {
                fail("Cannot delete file");
            }
        } catch(Exception e) {
            fail(e.getMessage());
        }
    }

    public Download(String source, String dest) {
        this.source = source;
        this.dest = dest;
        this.currentsize = 0;
        this.totalsize = 0;
        this.state = DownloadState.CREATED;
    }

    public DownloadState getState() {
        return this.state;
    }

    private void changeAndNotify() {
        setChanged();
        notifyObservers();
    }

    protected void setState(DownloadState state) {
        this.state = state;
        changeAndNotify();
    }

    public String getSource() {
        return source;
    }

    protected void setSource(String source) {
        this.source = source;
    }

    public String getDest() {
        return dest;
    }

    protected void setDest(String dest) {
        this.dest = dest;
    }

    public int getCurrentsize() {
        return currentsize;
    }

    protected void setCurrentsize(int currentsize) {
        this.currentsize = currentsize;
        changeAndNotify();
    }

    public int getTotalsize() {
        return totalsize;
    }

    protected void setTotalsize(int totalsize) {
        this.totalsize = totalsize;
    }

    public double getProgress() {
        if (currentsize == 0) {
            return 0;
        } else if (totalsize == 0) {
            return -1;
        } else {
            return (((double) currentsize / totalsize));
        }
    }

    private void setFailure(String failure) {
        this.failure = failure;
    }

    public String getFailure() {
        return failure;
    }

    public String toString() {
        return "[" + state + "] " + source + " --> " + dest;
    }

    protected void download(InputStream inputStream) {
        download(inputStream, MAX_BUFFER_SIZE);
    }

    private void download(InputStream inputStream, int bufferSize) {
        try {
            RandomAccessFile file = new RandomAccessFile(fileName(), "rw");
            download(inputStream, file, bufferSize);
        } catch (IOException e) {
            fail(e.getMessage());
        }
    }

    private void download(InputStream inputStream, RandomAccessFile outputFile, int bufferSize) {
        try (
                InputStream stream = inputStream;
                RandomAccessFile file = outputFile;
        ) {
            file.seek(getCurrentsize());
            byte[] buffer = new byte[bufferSize];
            int read = 0;

            while (getState() == DownloadState.IN_PROGRESS) {
                if ( (read = stream.read(buffer)) == -1) {
                    break;
                }

                file.write(buffer, 0, read);
                setCurrentsize(getCurrentsize() + read);
            }
        } catch (IOException e) {
            fail(e.getMessage());
        }

        if (getState() == DownloadState.IN_PROGRESS) {
            setState(DownloadState.COMPLETED);
        } else if (getState() == DownloadState.CANCELED || getState() == DownloadState.FAILED) {
            clear();
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
