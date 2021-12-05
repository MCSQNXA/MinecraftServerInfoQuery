import com.mcsqnxa.common.MinecraftServerInfoQuery;

import java.util.ArrayList;


public class Demo {


    public static void main(String[] args) {
        System.out.println(MinecraftServerInfoQuery.queryJava("mc.mcsq.cc", 25565));
        System.out.println(MinecraftServerInfoQuery.queryBedrock("mc.mcsq.cc", 19132));


    }


}
