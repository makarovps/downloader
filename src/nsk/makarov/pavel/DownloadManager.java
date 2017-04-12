package nsk.makarov.pavel;

import nsk.makarov.pavel.model.Download;
import nsk.makarov.pavel.model.DownloadException;
import nsk.makarov.pavel.model.DownloadFactory;
import nsk.makarov.pavel.model.DownloadState;

import java.util.ArrayList;
import java.util.List;

public class DownloadManager {
    static final String SOURCE_PATTERN = "";

    private List<Download> downloads = new ArrayList<>();
    private DownloadFactory factory = new DownloadFactory();

    public List<Download> getDownloads() {
        return downloads;
    }

    private static boolean validateSource(String source) {
        return true;
    }

    public DownloadManager() {

    }

    public Download add(String source, String dest) throws DownloadException {
        validateSource(source);

        Download download = factory.getDownload(source, dest);
        if (download != null) {
            downloads.add(download);
            return download;
        } else {
            throw new DownloadException("Unsupported protocol: " + source);
        }
    }

    public void start(int index) throws DownloadException {
        Download download = null;
        try {
            download = downloads.get(index);
        } catch (IndexOutOfBoundsException e) {
            throw new DownloadException("Download #" + index + " doesn't exist");
        }

        start(download);
    }

    private void start(Download download) throws DownloadException {
        if (download == null) {
            throw new DownloadException("Download cannot be started");
        }

        (new Thread(download)).start();
    }

    public void startAll() throws DownloadException {
        if (downloads == null || downloads.size() == 0) {
            throw new DownloadException("Download list is already empty, nothing to start");
        }
        for (Download download: downloads) {
            start(download);
        }
    }

    public void remove(int index) throws DownloadException {
        if (downloads == null || downloads.size() == 0) {
            throw new DownloadException("Download list is already empty, nothing to remove");
        }

        Download download = null;
        try {
            download = downloads.get(index);
        } catch (IndexOutOfBoundsException e) {
            throw new DownloadException("Download #" + index + " doesn't exist");
        }

        if (download.getState() != DownloadState.COMPLETED) {
            download.cancel();
        }

        downloads.remove(index);
    }

    public void remove(Download download) throws DownloadException {
        if (downloads == null || downloads.size() == 0) {
            throw new DownloadException("Download list is already empty, nothing to remove");
        }

        int index = downloads.indexOf(download);
        if (index == -1) {
            throw new DownloadException("Download doesn't exists");
        }

        if (download.getState() != DownloadState.COMPLETED && download.getState() != DownloadState.CREATED) {
            download.cancel();
        }

        downloads.remove(index);
    }

    public void removeAll() throws DownloadException {
        if (downloads == null || downloads.size() == 0) {
            throw new DownloadException("Download list is already empty, nothing to remove");
        }
        for (Download download: downloads) {
            remove(download);
        }
    }

    public void quit() {
        try {
            removeAll();
        } catch (DownloadException e) {
            e.printStackTrace();
        }
    }
}
