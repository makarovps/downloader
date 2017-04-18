package nsk.makarov.pavel;

import nsk.makarov.pavel.model.Download;
import nsk.makarov.pavel.model.DownloadException;
import nsk.makarov.pavel.model.DownloadFactory;
import nsk.makarov.pavel.model.DownloadState;

import java.util.ArrayList;
import java.util.List;

public class DownloadManager {
    private List<Download> downloads = new ArrayList<>();
    private DownloadFactory factory = new DownloadFactory();

    public List<Download> getDownloads() {
        return downloads;
    }

    public DownloadManager() {

    }

    public Download add(String source, String dest) throws DownloadException {
        Download.validateSource(source);
        Download.validateDest(dest);

        Download download = factory.getDownload(source, dest);
        if (download == null) {
            throw new DownloadException("Unsupported protocol: " + source);
        }
        downloads.add(download);
        return download;
    }

    public void remove(Download download) throws DownloadException {
        if (download == null) {
            throw new DownloadException("Download cannot be empty");
        }

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

}
