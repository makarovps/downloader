package nsk.makarov.pavel.model;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;
import org.powermock.reflect.Whitebox;

import java.io.IOException;
import java.io.InputStream;
import java.io.RandomAccessFile;
import java.util.concurrent.TimeUnit;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.mockito.Matchers.anyByte;
import static org.mockito.Matchers.anyInt;
import static org.mockito.Mockito.any;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.powermock.api.mockito.PowerMockito.doNothing;
import static org.powermock.api.mockito.PowerMockito.mock;
import static org.powermock.api.mockito.PowerMockito.verifyPrivate;

/**
 * Created by pavel on 15.04.17.
 */

class DownloadRunner implements Runnable {
    Download download;
    InputStream inputStream;
    RandomAccessFile outputFile;
    int bufferSize;

    DownloadRunner(Download download, InputStream inputStream, RandomAccessFile outputFile, int bufferSize) {
        this.download = download;
        this.inputStream = inputStream;
        this.outputFile = outputFile;
        this.bufferSize = bufferSize;
    }
    @Override
    public void run() {
        try {
            Whitebox.invokeMethod(download, "download", inputStream, outputFile, bufferSize);
        } catch (Exception e) {

        }
    }
}

@RunWith(PowerMockRunner.class)
@PrepareForTest(Download.class)
public class DownloadTest {
    private static Download httpDownload;
    private static int bufferSize = 1024;

    private static <E> org.mockito.stubbing.Stubber doSleep(final long delay, final E ret) {
        return doAnswer(new Answer() {
            @Override
            public Object answer(InvocationOnMock invocationOnMock) throws Throwable {
                TimeUnit.MILLISECONDS.sleep(delay);
                return ret;
            }
        });
    }

    private static InputStream mockInputStream() {
        InputStream inputStream = mock(InputStream.class);
        /* Mock "inputStream's" behavior */
        try {
            when(inputStream.read(any(byte[].class))).thenReturn(bufferSize, bufferSize, bufferSize, bufferSize, bufferSize, -1);
        } catch (IOException e) {
        }

        return inputStream;
    }

    private static InputStream mockSlowInputStream() {
        InputStream inputStream = mock(InputStream.class);
        /* Mock "inputStream's" behavior */
        try {
            doSleep(1000, 1024).when(inputStream).read(any(byte[].class));
        } catch (IOException e) {
        }

        return inputStream;
    }

    private static InputStream mockInputStreamWithException() {
        InputStream inputStream = mock(InputStream.class);
        try {
            when(inputStream.read(any(byte[].class))).thenThrow(IOException.class);
        } catch (IOException e) {
        }
        return inputStream;
    }

    private static RandomAccessFile mockRandomAccessFile() {
        RandomAccessFile outputFile = mock(RandomAccessFile.class);
        /* Mock "file's" behavior */
        try {
            doNothing().when(outputFile).seek(anyInt());
            doNothing().when(outputFile).write(anyByte());
        } catch (IOException e) {
        }
        return outputFile;
    }

    private static RandomAccessFile mockRandomAccessFileWithException() {
        RandomAccessFile outputFile = mock(RandomAccessFile.class);
        try {
            doNothing().when(outputFile).seek(anyInt());
            doThrow(IOException.class).when(outputFile).write(any(byte[].class), anyInt(), anyInt());
        } catch (IOException e) {
        }
        return outputFile;
    }

    @Before
    public void before() {
        httpDownload = (new DownloadFactory()).getDownload("http://1/2.file", "/tmp/");
        httpDownload.setState(DownloadState.IN_PROGRESS);
    }

    @Test
    public void testValidateCorrectSource() throws Exception {

    }

    @Test
    public void testCorrectDownload() throws Exception {
        InputStream inputStream = mockInputStream();
        RandomAccessFile outputFile = mockRandomAccessFile();

        Whitebox.invokeMethod(httpDownload, "download", inputStream, outputFile, bufferSize);

        assertEquals(httpDownload.getState(), DownloadState.COMPLETED);
        verify(inputStream, times(6)).read(any(byte[].class));
        verify(outputFile, times(5)).write(any(byte[].class), anyInt(), anyInt());
    }

    @Test
    public void testDownloadWithInputStreamException() throws Exception {
        InputStream inputStream = mockInputStreamWithException();
        RandomAccessFile outputFile = mockRandomAccessFile();

        Whitebox.invokeMethod(httpDownload, "download", inputStream, outputFile, bufferSize);

        assertEquals(httpDownload.getState(), DownloadState.FAILED);
        assertNotNull(httpDownload.getFailure());
    }

    @Test
    public void testDownloadWithOutputFileException() throws Exception {
        InputStream inputStream = mockInputStream();
        RandomAccessFile outputFile = mockRandomAccessFileWithException();

        Whitebox.invokeMethod(httpDownload, "download", inputStream, outputFile, bufferSize);

        assertEquals(httpDownload.getState(), DownloadState.FAILED);
        assertNotNull(httpDownload.getFailure());
    }

    @Test
    public void testDownloadAndPause() throws Exception {
        InputStream inputStream = mockSlowInputStream();
        RandomAccessFile outputFile = mockRandomAccessFile();

        (new Thread((new DownloadRunner(httpDownload, inputStream, outputFile, bufferSize)))).start();

        Thread.sleep(250);
        httpDownload.pause();

        assertEquals(httpDownload.getState(), DownloadState.PAUSED);
    }

    @Test
    public void testDownloadAndCancel() throws Exception {
        InputStream inputStream = mockSlowInputStream();
        RandomAccessFile outputFile = mockRandomAccessFile();

        Download spyDownload = spy(httpDownload);

        (new Thread((new DownloadRunner(spyDownload, inputStream, outputFile, bufferSize)))).start();

        Thread.sleep(250);

        spyDownload.cancel();

        /* method must be called due to cancel invoked */
        verifyPrivate(spyDownload, times(1)).invoke("clear");
        /* we won't be able to delete nonexistent file thus spyDownload state is FAILED*/
        assertEquals(spyDownload.getState(), DownloadState.FAILED);
    }
}