package event_based_server;

/**
 * Created by soo on 2017. 6. 10..
 */
class ByteArrayWrapper {
    private byte[] byteArray;

    ByteArrayWrapper(byte[] byteArray) {
        this.byteArray = byteArray;
    }

    byte[] getByteArray() {
        return byteArray;
    }
}
