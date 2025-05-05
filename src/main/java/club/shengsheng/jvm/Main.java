package club.shengsheng.jvm;

/**
 * @author gongxuanzhangmelt@gmail.com
 **/
public class Main {

    public static void main(String[] args) throws Exception {
        Hotspot hotspot = new Hotspot("club.shengsheng.code.Demo", "/Users/gongxuanzhang/dev/lab/mini_jvm/target" +
            "/classes");
        hotspot.start();
    }
}
