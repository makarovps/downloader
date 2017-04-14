package nsk.makarov.pavel;

import nsk.makarov.pavel.model.Download;
import nsk.makarov.pavel.model.DownloadException;
import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;

import java.util.List;

import static org.junit.Assert.*;

/**
 * Created by pavel on 12.04.17.
 */
public class DownloadManagerTest {
    private static DownloadManager manager;
    private static String correctHTTPSource;
    private static String incorrectHTTPSource;
    private List<Download> dowloads;

    @BeforeClass
    public static void beforeClass() {
        manager = new DownloadManager();
        correctHTTPSource = "http://server.with.url/file.ext";
        incorrectHTTPSource = "http://$inconsistent";
    }

    @Test
    public void getDownloads() throws Exception {

    }

    @Test
    public void testAddCorrectSourceWithFile() throws Exception {
        assertTrue(manager.add(correctHTTPSource, "") != null);
    }

    @Test(expected = DownloadException.class)
    public void testAddCorrectSource() throws Exception {
        manager.add(incorrectHTTPSource, "");
    }

    @Test
    public void remove() throws Exception {

    }

    @AfterClass
    public static void afterClass() {

    }
}