import mcsq.nxa.util.MinecraftServerInfoQuery;


public class Demo {


    public static void main(String[] args) {
        System.out.println(MinecraftServerInfoQuery.queryJava("106.52.16.139", 25565));

        System.out.println(MinecraftServerInfoQuery.queryBedrock("mc.nxa.mcsq.cc", 19132));


    }


}
