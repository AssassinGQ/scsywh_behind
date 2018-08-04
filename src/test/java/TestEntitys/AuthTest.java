package TestEntitys;

import TestUtils.WebInfo;
import com.Common.Utils.HttpUtils;
import org.junit.Test;

import java.io.UnsupportedEncodingException;
import java.util.HashMap;
import java.util.Map;


public class AuthTest {
    private String hostaddr = WebInfo.getHostaddr(true);

    @Test
    public void TestAdduser(){
        Map<String, String> params = new HashMap<>();
        params.put("token", "superadmin");
        params.put("usersid", "4");
        try {
            System.out.println(HttpUtils.Post(hostaddr +"/auth/dae/adduser", params));
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
    }

    @Test
    public void TestRemoveuser(){
        Map<String, String> params = new HashMap<>();
        params.put("token", "superadmin");
        params.put("usersid", "4");
        try {
            System.out.println(HttpUtils.Post(hostaddr +"/auth/dae/removeuser", params));
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
    }
}
