package TestEntitys;


import TestUtils.WebInfo;
import com.Common.Utils.HttpUtils;
import org.junit.Test;

import java.io.UnsupportedEncodingException;
import java.util.HashMap;
import java.util.Map;


public class LockTest {
    private String hostaddr = WebInfo.getHostaddr(false);

    @Test
    public void TestCreate(){
        Map<String, String> params = new HashMap<String, String>();
        params.put("token", "007B92EB45504D0FBD9A6502F15FD568");
        params.put("title", "123456");
        params.put("desc", "11111");
        params.put("content", "111");
        try {
            System.out.println(HttpUtils.Post(hostaddr +"/document/create", params));
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
    }

    @Test
    public void TestQuery(){
        Map<String, String> params = new HashMap<String, String>();
        params.put("token", "83BB3E3D8A5B4C69A74044D7B0B903EE");
        params.put("corporationsid", "1");
        params.put("limit", "10");
        params.put("page", "1");
        try {
            System.out.println(HttpUtils.Post(hostaddr +"/lock/query", params));
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
    }

    @Test
    public void TestGetList(){
        Map<String, String> params = new HashMap<String, String>();
        params.put("token", "24D07B45A8C34B6DB4A706FB4D6A3EC6");
        params.put("limit", "3");
        params.put("page", "1");
        try {
            System.out.println(HttpUtils.Get(hostaddr +"/document/getList", params));
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
    }
}
