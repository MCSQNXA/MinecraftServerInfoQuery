import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetSocketAddress;
import java.util.ArrayList;


public class Demo {

    public static ArrayList<String> query(String address, int port) {
        ArrayList<String> list = new ArrayList<>();

        try {
            byte[] data = new byte[]{
                    (byte) 0x01, (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x01, (byte) 0x70, (byte) 0xC7, (byte) 0x00, (byte) 0xFF,
                    (byte) 0xFF, (byte) 0x00, (byte) 0xFE, (byte) 0xFE, (byte) 0xFE, (byte) 0xFE, (byte) 0xFD, (byte) 0xFD, (byte) 0xFD, (byte) 0xFD, (byte) 0x12,
                    (byte) 0x34, (byte) 0x56, (byte) 0x78, (byte) 0xAF, (byte) 0x36, (byte) 0x9C, (byte) 0xAC, (byte) 0x81, (byte) 0x36, (byte) 0x1D, (byte) 0x03
            };

            //耗时start
            long start = System.currentTimeMillis();
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
            //耗时
            list.add(System.currentTimeMillis() - start + "Ms");
            //获取有效字节长度,并且创建缓存区
            byte[] receive = new byte[packet.getLength() - 40];
            //取出有效数据
            System.arraycopy(packet.getData(), 40, receive, 0, packet.getLength() - 40);

            int k = 0;
            for (int i = 0; i < receive.length; i++) {
                if (receive[i] == ';') {
                    byte[] buffer = new byte[i - k];

                    for (int p = 0; p < buffer.length; p++) {
                        buffer[p] = receive[k];
                        k++;
                    }

                    k++;
                    list.add(new String(buffer));
                }
            }

            if (list.size() != 12) {
                list.clear();
            }

            socket.close();
        } catch (Exception e) {
            e.printStackTrace();
        }

        return list;
    }

    public static void main(String[] args) {
        ArrayList<String> list = Demo.query("mc.mcsq.cc", 19132);

        if (list.size() > 0) {
            StringBuilder builder = new StringBuilder();
            builder.append("[服务器名称]:").append(list.get(1)).append("\n");
            builder.append("[服务器版本]:").append(list.get(3)).append("\n");
            builder.append("[服务器在线]:").append(list.get(4)).append("/").append(list.get(5)).append("\n");
            builder.append("[服务器延迟]:").append(list.get(0)).append("\n");
            builder.append("[服务器模式]:").append(list.get(8));

            System.out.println(builder);
        } else {
            System.out.println("网络错误");
        }
    }


}
