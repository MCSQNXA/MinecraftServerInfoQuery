import com.mcsqnxa.common.MinecraftServerInfoQuery;

import java.util.ArrayList;


public class Demo {


    public static void main(String[] args) {
        ArrayList<String> list = new MinecraftServerInfoQuery().query("mc.mcsq.cc", 19132);

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
