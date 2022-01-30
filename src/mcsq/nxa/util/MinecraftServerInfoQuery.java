package mcsq.nxa.util;

import org.json.JSONObject;

import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetSocketAddress;
import java.net.Socket;


public class MinecraftServerInfoQuery {
    /**
     * @param address 爪哇版服务器地址
     * @param port    爪哇版服务器端口
     * @return 查询成功，返回服务器信息
     */
    public static String queryJava(String address, int port) {
        StringBuilder info = new StringBuilder();

        try {
            Socket socket = new Socket(address, port);
            socket.setSoTimeout(8000);

            ByteArrayOutputStream head = new ByteArrayOutputStream();
            head.write(7 + address.getBytes().length);
            head.write(new byte[]{0x00, (byte) 0xF5, 0x05});
            head.write(address.getBytes().length);
            head.write(address.getBytes());
            head.write((port >> 0x08) & 0xff);
            head.write((port >> 0x00) & 0xff);
            head.write(new byte[]{0x01, 0x01, 0x00});

            OutputStream os = socket.getOutputStream();
            os.write(head.toByteArray());
            os.flush();
            socket.shutdownOutput();

            InputStream is = socket.getInputStream();
            is.read();
            is.read();
            is.read();//去除头部无用字节数据

            int len;
            ByteArrayOutputStream buff = new ByteArrayOutputStream(128);

            while ((len = is.read()) != -1) {
                buff.write(len);
            }

            socket.close();

            JSONObject data = new JSONObject(buff.toString());

            if (data.has("description")) {
                JSONObject description = data.getJSONObject("description");
                JSONObject players = data.getJSONObject("players");
                JSONObject version = data.getJSONObject("version");

                info.append("[服务器类型]:Java").append("\n");
                info.append("[服务器名称]:").append(description.getString("text")).append("\n");
                info.append("[服务器在线]:").append(players.getInt("online")).append("/").append(players.getLong("max")).append("\n");
                info.append("[服务器版本]:").append(version.getString("name")).append("\n");
                info.append("[服务器协议]:").append(version.getInt("protocol"));
            } else {
                info.append(data);
            }
        } catch (Exception e) {
            info.append(e);
        }

        return info.toString();
    }

    /**
     * @param address 基岩版服务器地址
     * @param port    基岩版服务器端口
     * @return 查询成功，返回服务器信息
     */
    public static String queryBedrock(String address, int port) {
        StringBuilder info = new StringBuilder();

        try {
            byte[] data = new byte[]{
                    (byte) 0x01, (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x01, (byte) 0x70, (byte) 0xC7, (byte) 0x00, (byte) 0xFF,
                    (byte) 0xFF, (byte) 0x00, (byte) 0xFE, (byte) 0xFE, (byte) 0xFE, (byte) 0xFE, (byte) 0xFD, (byte) 0xFD, (byte) 0xFD, (byte) 0xFD, (byte) 0x12,
                    (byte) 0x34, (byte) 0x56, (byte) 0x78, (byte) 0xAF, (byte) 0x36, (byte) 0x9C, (byte) 0xAC, (byte) 0x81, (byte) 0x36, (byte) 0x1D, (byte) 0x03
            };

            //创建UDP
            DatagramSocket socket = new DatagramSocket();
            socket.connect(new InetSocketAddress(address, port));
            socket.setSoTimeout(8000);
            //发送请求
            socket.send(new DatagramPacket(data, data.length));
            //创建接收包
            DatagramPacket packet = new DatagramPacket(new byte[102400], 102400);
            //接收返回数据;阻塞
            socket.receive(packet);
            //获取有效字节长度,并且创建缓存区
            byte[] receive = new byte[packet.getLength() - 40];
            //取出有效数据
            System.arraycopy(packet.getData(), 40, receive, 0, packet.getLength() - 40);
            //断开连接
            socket.close();

            info.append("[服务器类型]:Bedrock").append("\n");

            for (int i = 0, k = 0, c = 0; i < receive.length; i++) {
                if (receive[i] == ';') {
                    byte[] buffer = new byte[i - k];

                    for (int p = 0; p < buffer.length; p++, k++) {
                        buffer[p] = receive[k];
                    }

                    k++;
                    c++;

                    switch (c) {
                        case 1:
                            info.append("[服务器名称]:").append(new String(buffer)).append("\n");
                            break;

                        case 2:
                            info.append("[服务器协议]:").append(new String(buffer)).append("\n");
                            break;

                        case 3:
                            info.append("[服务器版本]:").append(new String(buffer)).append("\n");
                            break;

                        case 4:
                            info.append("[服务器在线]:").append(new String(buffer)).append("/");
                            break;

                        case 5:
                            info.append(new String(buffer)).append("\n");
                            break;

                        case 7:
                            info.append("[服务器存档]:").append(new String(buffer)).append("\n");
                            break;

                        case 8:
                            info.append("[服务器模式]:").append(new String(buffer));
                            break;
                    }
                }
            }
        } catch (Exception e) {
            info.append(e);
        }

        return info.toString();
    }


}
