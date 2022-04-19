package io.github.hengyunabc.zabbix.sender.test;

import com.alibaba.fastjson.JSONObject;
import io.github.hengyunabc.zabbix.sender.DataObject;
import io.github.hengyunabc.zabbix.sender.SenderResult;
import io.github.hengyunabc.zabbix.sender.ZabbixSender;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.atomic.AtomicReference;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

public class ZabbixSenderTest {

    String host = "127.0.0.1";
    int port = 49156;

    private final AtomicReference<byte[]> receivedResult = new AtomicReference<>();
    private final CountDownLatch countDownLatch = new CountDownLatch(1);

    @Before
    public void before() throws IOException
    {
        ServerSocket serverSocket = new ServerSocket();
        serverSocket.bind(new InetSocketAddress(0));
        port = serverSocket.getLocalPort();
        new Thread(() -> {
            try {
                Socket socket = serverSocket.accept();
                socket.setSoTimeout(400);
                socket.setTcpNoDelay(true);
                final var inputStream = socket.getInputStream();
                final var buffer = new byte[1000];
                int readCount = 0;
                try {
                    while (true) {
                        int value = inputStream.read();
                        if (value < 0) {
                            break;
                        }
                        buffer[readCount++] = (byte) value;
                    }
                } catch (IOException e) {
                    // Ignore
                }
                this.receivedResult.set(Arrays.copyOf(buffer, readCount));
                final byte[][] response =
                        {new byte[]{'[', 'Z', 'B', 'X', 'D', ' ', 0, 0, 0, 0, 0, 0, 0},
                                "{     \"response\":\"success\",     \"info\":\"processed: 1; failed: 0; total: 1; seconds spent: 0.060753\" }\n".getBytes(StandardCharsets.UTF_8)};
                final var outputStream = socket.getOutputStream();
                for (final byte[] bytes : response) {
                    outputStream.write(bytes);
                }
                outputStream.flush();
                outputStream.close();
                socket.close();
                this.countDownLatch.countDown();
                serverSocket.close();
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }).start();
    }

    @Test
    public void test_LLD_rule() throws IOException
    {
        ZabbixSender zabbixSender = new ZabbixSender(host, port);

        DataObject dataObject = new DataObject();
        dataObject.setHost("172.17.42.1");
        dataObject.setKey("healthcheck[dw,notificationserver]");

        JSONObject data = new JSONObject();
        List<JSONObject> array = new LinkedList<JSONObject>();
        JSONObject xxx = new JSONObject();
        xxx.put("hello", "hello");

        array.add(xxx);
        data.put("data", array);

        dataObject.setValue(data.toJSONString());
        dataObject.setClock(System.currentTimeMillis() / 1000);
        SenderResult result = zabbixSender.send(dataObject);

        System.out.println("result:" + result);
        if (result.success()) {
            System.out.println("send success.");
        } else {
            System.err.println("send fail!");
        }
        Assert.assertTrue(result.success());
    }

    @Test
    public void test() throws IOException
    {
        ZabbixSender zabbixSender = new ZabbixSender(host, port);

        DataObject dataObject = new DataObject();
        dataObject.setHost("172.17.42.1");
        dataObject.setKey("a[test, jvm.mem.non-heap.used]");
        dataObject.setValue("10");
        final var clock = System.currentTimeMillis() / 1000;
        dataObject.setClock(clock);
        SenderResult result = zabbixSender.send(dataObject);

        System.out.println("result:" + result);
        if (result.success()) {
            System.out.println("send success.");
        } else {
            System.err.println("send fail!");
        }
        Assert.assertTrue(result.success());
        byte[] buffer = new byte[receivedResult.get().length - 13];
        System.arraycopy(receivedResult.get(), 13, buffer, 0, buffer.length);
        final var jsonObject = JSONObject.parseObject(new String(buffer));
        System.out.println(jsonObject);
    }
}
