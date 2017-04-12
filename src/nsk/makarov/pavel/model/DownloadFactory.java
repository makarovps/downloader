package nsk.makarov.pavel.model;

import nsk.makarov.pavel.impl.FTPDownload;
import nsk.makarov.pavel.impl.HTTPDownload;

/**
 * Created by pavel on 07.04.17.
 */
public class DownloadFactory {
    public Download getDownload(String source, String dest) {
        String protocol = source.toLowerCase().split("://")[0];
        switch(protocol) {
            case "http": {
                return new HTTPDownload(source, dest);
            }
            case "ftp": {
                return new FTPDownload(source, dest);
            }
            default :{
                return null;
            }
        }
    }
}
