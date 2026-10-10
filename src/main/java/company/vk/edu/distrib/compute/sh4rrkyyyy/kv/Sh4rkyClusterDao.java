package company.vk.edu.distrib.compute.sh4rrkyyyy.kv;

import company.vk.edu.distrib.compute.Dao;

import java.io.IOException;
import java.net.http.HttpClient;
import java.util.NoSuchElementException;

public class Sh4rkyClusterDao implements Dao<String> {
    private final HttpClient client = HttpClient.newHttpClient();
    private final int[] ports;
    private final Dao<String>[] nodes;

    public Sh4rkyClusterDao(int... ports) {
        this.ports = ports.clone();
        this.nodes = new Dao[ports.length];
        for (int i = 0; i < ports.length; ++i) {
            nodes[i] = new Sh4rkyRemoteDao(ports[i], client);
        }
    }

    @Override
    public String get(String key) throws NoSuchElementException, IllegalArgumentException, IOException {
        return getNode(key).get(key);
    }

    @Override
    public void upsert(String key, String value) throws IllegalArgumentException, IOException {
        getNode(key).upsert(key, value);
    }

    @Override
    public void delete(String key) throws IllegalArgumentException, IOException {
        getNode(key).delete(key);
    }

    @Override
    public void close() throws IOException {
        client.close();
    }

    private Dao<String> getNode(String key) {
        int hashMax = Integer.MIN_VALUE;
        int idx = 0;
        for (int i = 0; i < ports.length; ++i) {
            int hashRes = hash(key, ports[i]);
            if (hashRes > hashMax) {
                hashMax = hashRes;
                idx = i;
            }
        }
        return nodes[idx];
    }

    private static int hash(String key, int port) {
        int res = key.hashCode() * 31 + port;
        res ^= res >>> 16;
        res *= 0x85ebca6b;
        res ^= res >>> 13;
        res *= 0xc2b2ae35;
        res ^= res >>> 16;
        return res;
    }
}
