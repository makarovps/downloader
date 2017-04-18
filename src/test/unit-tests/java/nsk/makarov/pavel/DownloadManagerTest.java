package nsk.makarov.pavel;

import nsk.makarov.pavel.model.Download;
import nsk.makarov.pavel.model.DownloadException;
import nsk.makarov.pavel.model.DownloadState;
import org.junit.BeforeClass;
import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.mockito.Mockito.*;

/**
 * Created by pavel on 12.04.17.
 */

public class DownloadManagerTest {
    private static DownloadManager manager;
    private static DownloadManager emptyManager;

    private static Download mockDownloadWithState(DownloadState state) {
        Download download = mock(Download.class);
        when(download.getState()).thenReturn(state);
        return download;
    }

    @BeforeClass
    public static void beforeClass() {
        emptyManager = new DownloadManager();
        manager = new DownloadManager();
        manager.getDownloads().add(mockDownloadWithState(DownloadState.CREATED));
        manager.getDownloads().add(mockDownloadWithState(DownloadState.IN_PROGRESS));
        manager.getDownloads().add(mockDownloadWithState(DownloadState.PAUSED));
        manager.getDownloads().add(mockDownloadWithState(DownloadState.COMPLETED));
        manager.getDownloads().add(mockDownloadWithState(DownloadState.FAILED));
        manager.getDownloads().add(mockDownloadWithState(DownloadState.CANCELED));
    }

    @Test
    public void testAddCorrectSource() throws Exception {
        int length = manager.getDownloads().size();

        assertNotNull(manager.add("http://correct.http.source/1.gif", ""));
        assertEquals(manager.getDownloads().size(), length + 1);

        /* Remove "real" mockDownloadWithState from the list to not interfere with mocks in other tests */
        manager.getDownloads().remove(length);
    }

    @Test(expected = DownloadException.class)
    public void testAddCorrectSourceWithoutFile() throws Exception {
        manager.add("http://just.correct.source", "");
    }

    @Test(expected = DownloadException.class)
    public void testAddIncorrectSource() throws Exception {
        manager.add("http://$incorrectsource", "");
    }

    @Test(expected = DownloadException.class)
    public void testRemoveFromEmptyList() throws Exception {
        emptyManager.remove(mockDownloadWithState(DownloadState.CREATED));
    }

    @Test(expected = DownloadException.class)
    public void testRemoveEmptyDownload() throws Exception {
        manager.remove(null);
    }

    @Test(expected = DownloadException.class)
    public void testRemoveNonExistentDownload() throws Exception {
        Download download = mock(Download.class);
        manager.remove(download);
    }

    @Test
    public void testRemoveFromNonEmptyList() throws Exception {
        int length = manager.getDownloads().size();
        Download download = manager.getDownloads().get(0);

        manager.remove(download);
        assertEquals(manager.getDownloads().size(), length - 1);

        for (Download downloadIdx: manager.getDownloads()) {
            if (download.getState().equals(DownloadState.COMPLETED) || download.getState().equals(DownloadState.CREATED) ) {
                verify(downloadIdx, times(0)).cancel();
            } else {
                verify(downloadIdx, times(1)).cancel();
            }
        }
    }
}