package TestUtils;

import com.gexin.rp.sdk.base.IPushResult;
import com.gexin.rp.sdk.base.impl.SingleMessage;
import com.gexin.rp.sdk.base.impl.Target;
import com.gexin.rp.sdk.exceptions.PushSingleException;
import com.gexin.rp.sdk.http.IGtPush;
import com.gexin.rp.sdk.template.TransmissionTemplate;

public class GetuiUtils {
	private static String appId = "VFA69Ve3mw6WBSuFSzX4s8";
	private static String appKey = "bQepDsu5GY5Dzq5eapXHV2";
	//private static String appSecret = "NxSK6osdnU6PzvLt4InRc7";
	private static String masterSecret = "HWzPp8UILQ7eHfqImtsLVA";
	private static String host = "http://sdk.open.api.igexin.com/apiex.htm";
	public static String Push2Single(String cid, String content){
		IGtPush push = new IGtPush(host, appKey, masterSecret);
		
		TransmissionTemplate template = new TransmissionTemplate();
	    template.setAppId(appId);
	    template.setAppkey(appKey);
	    // 透传消息设置，1为强制启动应用，客户端接收到消息后就会立即启动应用；2为等待应用启动
	    template.setTransmissionType(2);
	    template.setTransmissionContent(content);
	    // 设置定时展示时间
	    // template.setDuration("2015-01-16 11:40:00", "2015-01-16 12:24:00");
		
		SingleMessage message = new SingleMessage();
		message.setOffline(true);
		message.setOfflineExpireTime(24*3600*1000);
		message.setData(template);
		message.setPushNetWorkType(0);
		
		Target target = new Target();
		target.setAppId(appId);
		target.setClientId(cid);
		IPushResult ret = null;
		try {
			ret = push.pushMessageToSingle(message, target);			
		} catch (PushSingleException e) {
			// TODO: handle exception
			try {
				ret = push.pushMessageToSingle(message, target, e.getRequestId());				
			} catch (Exception e2) {
				// TODO: handle exception
				System.err.println(e2.getMessage());
			}
		}
		if(ret != null)
			return ret.getResponse().toString();
		else
			return null;
	}
}
