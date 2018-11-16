package com.wizecore.graylog2.plugin.test;

import org.apache.commons.io.HexDump;

import java.io.IOException;
import java.io.InputStream;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Formatter;
import java.util.concurrent.Executors;

import static org.junit.Assert.assertTrue;

public class SimpleSocketTcpServer implements Runnable {
    protected int portStart = 45000;
    protected int portEnd = 45100;
    protected int port = 0;
    protected ServerSocket listen;
    protected String hostname;
    protected int receivedBytes;

    public void startServer() throws IOException {
        port = 0;
        for (int i = portStart; i <= portEnd; i++) {
            try {
                InetAddress local = InetAddress.getLocalHost();
                hostname = local.getHostName();
                System.err.println("Trying to listen on tcp://" + hostname + ":" + i);
                listen = new ServerSocket(i, 10, local);
                Executors.newSingleThreadExecutor().execute(this);
                port = i;
                break;
            } catch (IOException e) {
                // Failed to create socket
            }
        }
        if (port == 0) {
            throw new IOException("Can`t bind to listen on one of ports " + portStart + "..." + portEnd);
        }
    }

    /**
     * Formats Hex view of a byte array<br/>
     * Example : DebugUtil.prettyHexView(testString.getBytes())<br/>
     * testString = "Test1234567890"<br/>
     * output : <pre>00   |   54657374 31323334
     01  |   35363738 3930</pre>
     * @link https://gist.github.com/I3rixon/6101489#file-main-java-L15
     * @param ba the byte array to format
     * @return String representation "pretty Hex View" of input parameter
     */
    public static String prettyHexView(int pos, byte[] ba, int cnt) {
        StringBuilder sb = new StringBuilder();
        Formatter formatter = new Formatter(sb);
        for (int j = 0; j < cnt; j++) {
            if (j % 8 == 0) {
                if (j != 0) {
                    sb.append("\n");
                }
                formatter.format("0%d | ", pos + j / 8);
            }
            formatter.format("%02X ", ba[j]);
        }
        sb.append("\n");
        return sb.toString();
    }

    @Override
    public void run() {
        try {
            Socket conn = listen.accept();
            InputStream is = conn.getInputStream();
            byte[] s = new byte[1024];
            int c = 0;
            while ((c = is.read()) >= 0) {
                System.out.println(prettyHexView(receivedBytes, s, c));
                receivedBytes += c;
            }
            conn.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
