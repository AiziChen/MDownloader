import java.util.ArrayList;
import java.util.HashMap;

/**
 * Created by quanyechen on 2017/5/8.
 */
public class UAConstants {
    public static HashMap<String, String> getUAMap() {
        HashMap<String, String> map = new HashMap<>();
        map.put("safari 5.1", "Mozilla/5.0 (Windows; U; Windows NT 6.1; en-us) AppleWebKit/534.50 (KHTML, like Gecko) Version/5.1 Safari/534.50");
        map.put("IE 9.0", "Mozilla/5.0 (compatible; MSIE 9.0; Windows NT 6.1; Trident/5.0");
        map.put("Opera 11.11", "Opera/9.80 (Windows NT 6.1; U; en) Presto/2.8.131 Version/11.11");
        map.put("Chrome 17.0 - MAC", "Mozilla/5.0 (Macintosh; Intel Mac OS X 10_7_0) AppleWebKit/535.11 (KHTML, like Gecko) Chrome/17.0.963.56 Safari/535.11");
        map.put("PC BaiduYun", "netdisk;5.5.2.0;PC;PC-Windows;10.0.14393;WindowsBaiduYunGuanJia");

        return map;
    }
}
