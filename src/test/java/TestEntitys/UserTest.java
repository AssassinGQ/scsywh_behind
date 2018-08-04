package TestEntitys;

import TestUtils.WebInfo;
import com.Common.Utils.HttpUtils;
import com.Common.Utils.MD5Utils;
import org.junit.Test;

import java.io.UnsupportedEncodingException;
import java.util.HashMap;
import java.util.Map;

public class UserTest {
    private String hostaddr = WebInfo.getHostaddr(true);

    @Test
    public void TestGetVcode(){
        Map<String, String> params = new HashMap<>();
        params.put("phone", "18058180236");
        try {
            System.out.println(HttpUtils.Get(hostaddr +"/user/getvcode", params));
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
    }

    @Test
    public void TestChangePsw() {
        Map<String, String> params = new HashMap<>();
        params.put("vcode", "lQ511M");
        params.put("phone", "18058180236");
        params.put("password", "Sjq123456");
        try {
            System.out.println(HttpUtils.Post(hostaddr +"/user/changepsw", params));
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
    }

    @Test
    public void TestLogin(){
        Map<String, String> params = new HashMap<>();
        params.put("username", "superadmin");
        params.put("password", "qwer1234ABCD4321");
        try {
            System.out.println(HttpUtils.Post(hostaddr +"/user/login", params));
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
    }

    @Test
    public void TestGetAccount(){
        String TYPE_CY = "0";
        String TYPE_GO = "1";
        String TYPE_MA = "2";
        Map<String, String> params = new HashMap<>();
        params.put("token", "31CF37D75C5C4E44A295DBAE9D3AC62B");
        params.put("type", TYPE_CY);
        params.put("name", "hgq");
        params.put("phone", "18868187528");
        params.put("username", "CorpZJUtest");
        params.put("password", "CorpZJU1234");
        try {
            System.out.println(HttpUtils.Post(hostaddr +"/user/getAccount", params));
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
    }

    @Test
    public void JavaTest(){
        //////////////
//        System.out.println(System.currentTimeMillis());
        ///////////////
        System.out.println(MD5Utils.MD5("superadmin", "qwer1234ABCD4321", "suger"));
    }
}
