import java.io.ByteArrayOutputStream;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetSocketAddress;
import java.util.ArrayList;


public class Demo {

    public static ArrayList<String> look (String address, int port )
    {
        final ArrayList<String> list = new ArrayList<> ( );
        final String hex = "01 00 00 00 00 00 01 70 c7 00 ff ff 00 fe fe fe fe fd fd fd fd 12 34 56 78 af 36 9c ac 81 36 1d 03".replaceAll ( " ", "" );
        final ByteArrayOutputStream baos = new ByteArrayOutputStream ( hex.length ( ) >> 1 );

        for ( int i = 0; i <= hex.length ( ) - 2; i += 2 )
        {
            baos.write ( Integer.parseInt ( hex.substring ( i, i + 2 ), 16 ) & 255 );
        }

        try
        {
            //发送数据
            byte[] data = baos.toByteArray ( );
            //耗时start
            long start = System.currentTimeMillis ( );
            //创建UDP
            DatagramSocket socket = new DatagramSocket ( );
            socket.connect ( new InetSocketAddress( address, port ) );
            socket.setSoTimeout ( 8000 );
            //发送请求
            socket.send ( new DatagramPacket( data, data.length ) );
            //创建接收包
            DatagramPacket packet = new DatagramPacket ( new byte[102400], 102400 );
            //接收返回数据;阻塞
            socket.receive ( packet );
            //耗时
            list.add ( System.currentTimeMillis ( ) - start + "Ms" );
            //获取有效字节长度,并且创建缓存区
            final byte[] receive = new byte[packet.getLength ( ) - 40];
            //取出有效数据
            System.arraycopy ( packet.getData ( ), 40, receive, 0, packet.getLength ( ) - 40 );
            int k = 0;
            for ( int i = 0;i < receive.length;i++ )
            {
                if ( receive [ i ] == ';' )
                {
                    byte[] buffer = new byte[i - k];

                    for ( int p = 0;p < buffer.length;p++ )
                    {
                        buffer [ p ] = receive [ k ];
                        k++;
                    }

                    k++;
                    list.add ( new String ( buffer ) );
                }
            }

            if ( list.size ( ) != 12 )
            {
                list.clear ( );
            }

            //关闭链接
            socket.close ( );
        }
        catch (Exception e)
        {
            e.printStackTrace ( );
        }

        return list;
    }

    public static void main ( String[] args )
    {
        ArrayList<String> list = Demo.look ( "mc.mcsq.cc", 19132 );

        if ( list.size ( ) > 0 )
        {
            StringBuilder builder = new StringBuilder ( );
            builder.append ( "[服务器名称]:" ).append ( list.get ( 1 ) ).append ( "\n" );
            builder.append ( "[服务器版本]:" ).append ( list.get ( 3 ) ).append ( "\n" );
            builder.append ( "[服务器在线]:" ).append ( list.get ( 4 ) ).append ( "/" ).append ( list.get ( 5 ) ).append ( "\n" );
            builder.append ( "[服务器延迟]:" ).append ( list.get ( 0 ) ).append ( "\n" );
            builder.append ( "[服务器模式]:" ).append ( list.get ( 8 ) );

            System.out.println ( builder );
        }
    }


}
