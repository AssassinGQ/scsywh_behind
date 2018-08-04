package TestEntitys;

import TestUtils.WebInfo;
import com.Common.Utils.HttpUtils;
import org.junit.Test;

import java.io.UnsupportedEncodingException;
import java.util.HashMap;
import java.util.Map;


public class FileTest {
    private String hostaddr = WebInfo.getHostaddr(true);

    @Test
    public void TestDownload(){
        Map<String, String> params = new HashMap<>();
        params.put("token", "superadmin");
        params.put("sid", "1");
        try {
            System.out.println(HttpUtils.Get(hostaddr +"/file/download", params));
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
    }
}
