package nsk.makarov.pavel.model;

/**
 * Created by pavel on 07.04.17.
 */
public interface Downloadable {
    void start();

    void pause();

    void resume();

    void cancel();

    void fail(String message);
}
