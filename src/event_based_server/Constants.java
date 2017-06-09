package event_based_server;

/**
 * Created by soo on 2017. 6. 9..
 */
class Constants {
    static final int PORT = 8080;
    static final String HOST = "localhost";
    static final int MAIN_BUFFER_SIZE = 2048;
    static final int PERIODIC_SELECT = 300;
    static final int THREAD_POOL_SIZE = Runtime.getRuntime().availableProcessors();
    static final int CACHE_TIME_TO_LIVE = 200;
    static final int CACHE_TIMER_INTERVAL = 500;
    static final int CACHE_MAX_ITEMS = 100;
    static final long CACHE_BYTE_SIZE = 100 * 1024 * 1024; // 100M
}
