package com.Common.Utils;

import java.util.Iterator;
import java.util.List;
import java.util.Map.Entry;

import org.hibernate.Session;

import com.Common.Entitys.Bean;
import com.Entitys.WebSocketInfo;
import com.Entitys.Truck.Entity.Truck;
import com.Entitys.User.Entity.LoginInfo;
import com.gexin.rp.sdk.base.IPushResult;
import com.gexin.rp.sdk.base.em.EPushResult;
import com.gexin.rp.sdk.base.impl.SingleMessage;
import com.gexin.rp.sdk.base.impl.Target;
import com.gexin.rp.sdk.exceptions.PushSingleException;
import com.gexin.rp.sdk.http.IGtPush;
import com.gexin.rp.sdk.template.TransmissionTemplate;

import io.netty.channel.Channel;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.http.websocketx.TextWebSocketFrame;

public class PushUtils {
	private final static String host = "http://sdk.open.api.igexin.com/apiex.htm";
	private final static String[] appIds = { "VFA69Ve3mw6WBSuFSzX4s8", "ROmdRc0E0EAE4DPlKr6AR9", "XBkFj9drJAAxku9jfpTuu1" };
	private final static String[] appKeys = { "bQepDsu5GY5Dzq5eapXHV2", "ST46nGM9O77SvqCOgHxQS2", "OazmMVsjrv94bw6nDZkza1" };
	private final static String[] masterSecrets = { "HWzPp8UILQ7eHfqImtsLVA", "BxcjZqAAHH9VqmXwjsJNE1", "hlIgt1s0GM670g2bw3JEt2" };
	public final static int ID_TRUCK = 0;
	public final static int ID_OTHER = 1;
	public final static int ID_TEST = 2;

	// 推送给单个getui
	@SuppressWarnings("deprecation")
	public static void Push2Getui(String cid, String content, int id) throws Exception {
		if (id != ID_OTHER && id != ID_TRUCK && id != ID_TEST)
			throw new Exception("推送给小齐还是小单标志位出错");
		String appId = appIds[id];
		String appKey = appKeys[id];
		String masterSecret = masterSecrets[id];
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
		message.setOfflineExpireTime(24 * 3600 * 1000);
		message.setData(template);
		message.setPushNetWorkType(0);

		Target target = new Target();
		target.setAppId(appId);
		target.setClientId(cid);
		IPushResult ret = null;
		try {
			ret = push.pushMessageToSingle(message, target);
		} catch (PushSingleException e) {
			try {
				ret = push.pushMessageToSingle(message, target, e.getRequestId());
			} catch (Exception e2) {
				throw new Exception(e2.getMessage());
			}
		}
		if (ret != null) {
			if(ret.getResultCode().equals(EPushResult.RESULT_OK)){
				System.err.print(",个推推送结果：" + ret.getResponse().toString());
				return;				
			}else
				throw new Exception("个推推送失败："+ ret.getResponse().toString());
		} else
			throw new Exception("个推未知错误");
	}
	// 推送给单个websocket
	private static void Push2WebSocket(String name, String content) throws Exception {
		if (WebSocketUtils.ctxs == null)
			throw new Exception("wsid对应的推送信息为空");
		Iterator<Entry<String, WebSocketInfo>> iterator = WebSocketUtils.ctxs.entrySet().iterator();
		while (iterator.hasNext()) {
			Entry<String, WebSocketInfo> entry = iterator.next();
			if (entry.getKey().equals(name)) {
				ChannelHandlerContext ctx = entry.getValue().getCtx();
				Channel ch = ctx.channel();
				if (ctx.isRemoved() || !ch.isActive() || !ch.isOpen() || !ch.isRegistered() || !ch.isWritable())
					throw new Exception("WebSocket通道已经关闭");
				ch.writeAndFlush(new TextWebSocketFrame(content));
				return;
			}
		}
		throw new Exception("没有找到wsid对应的Websocket推送信息");
	}
	// 推送给单用logininfo对应的用户
	public static void Push2Single(LoginInfo loginInfo, String content, int id) throws Exception {
		if (loginInfo == null) {
			throw new Exception("该用户登录信息为空");
		}
		if (loginInfo.getOnlinetype() == null) {
			throw new Exception("该用户未上传cid或者wsid");
		}
		System.err.print(",username="+loginInfo.getUsername());
		if (loginInfo.getOnlinetype() == LoginInfo.ONLINETYPE_APP) {
			try {
				Push2Getui(loginInfo.getCid(), content, id);
				return;
			} catch (Exception e) {
				throw new Exception("个推推送失败" + e.getMessage());
			}
		} else if (loginInfo.getOnlinetype() == LoginInfo.ONLINETYPE_WEB) {
			try {
				Push2WebSocket(loginInfo.getToken(), content);
				return;
			} catch (Exception e) {
				throw new Exception("WebSocket推送失败:" + e.getMessage());
			}
		} else {
			throw new Exception("该用户登记的推送信息有误");
		}
	}
	// 推送给某个承运方下的某一类用户（可以不筛选）
	public static boolean Push2Type(String content, Session session, Long corporationsid, Integer type) {
		try {
			Push2TypeWithExp(content, session, corporationsid, type);
			return true;
		} catch (Exception e) {
			//System.err.println("Push2Type失败：" + e.getMessage());
			return false;
		}
	}
	public static void Push2TypeWithExp(String content, Session session, Long corporationsid, Integer type)
			throws Exception {
		StringBuilder sb = new StringBuilder();
		sb.append("from LoginInfo where datastatus = " + Bean.CREATED + " and status = 1");
		if (corporationsid != null)
			sb.append(" and corporationsid = " + corporationsid);
		if (type != null)
			sb.append(" and type = " + type);
		List<LoginInfo> loginInfos = session.createQuery(sb.toString(), LoginInfo.class).list();
		if (loginInfos.size() == 0)
			throw new Exception("查无此用户");
		for (int i = 0; i < loginInfos.size(); i++) {
			try {
				Push2Single(loginInfos.get(i), content, ID_OTHER);
			} catch (Exception e) {
				throw new Exception("推送给用户" + loginInfos.get(i).getUsername() + "失败，" + e.getMessage());
			}
		}
	}
	// 推送给硬件平台
	public static boolean Push2Truck(String cid, String content) {
		System.err.print("推送给车辆，cid："+cid);
		try {
			Push2Getui(cid, content, ID_TRUCK);
			System.err.println(",推送成功");
			return true;
		} catch (Exception e) {
			System.err.println(",推送失败,"+e.getMessage());
			return false;
		}
	}
	public static boolean Push2Truck(Long trucksid, Session session, String content) {
		Truck truck = null;
		try {
			truck = session
					.createQuery("from Truck where datastatus = " + Bean.CREATED + " and truckstatus = "+Truck.TRUCKSTATUS_TASK+
							" and sid = " + trucksid, Truck.class)
					.uniqueResult();
			if(truck == null)
				throw new Exception();
			if(truck.getCid() == null)
				throw new Exception();
		} catch (Exception e) {
			return false;
		}
		return Push2Truck(truck.getCid(), content);
	}
	public static boolean Push2Truck(String trucknumber, Session session, String content) {
		Truck truck = null;
		try {
			truck = session
					.createQuery("from Truck where datastatus = " + Bean.CREATED + " and truckstatus = "+Truck.TRUCKSTATUS_TASK+
							" and trucknumber = '" + trucknumber + "'", Truck.class)
					.uniqueResult();
			if(truck == null)
				throw new Exception();
			if(truck.getCid() == null)
				throw new Exception();
		} catch (Exception e) {
			return false;
		}
		return Push2Truck(truck.getCid(), content);
	}
	// 推送给特定司机
	public static boolean Push2Driver(Long driversid, String content, Session session, Long corporationsid) {
		System.err.print("推送给司机,sid="+driversid);
		LoginInfo loginInfo_driver = null;
		try {
			loginInfo_driver = session
					.createQuery("from LoginInfo where datastatus = " + Bean.CREATED + " and status = 1 and type = "
							+ LoginInfo.TYPE_DRIVER + " and usersid = " + driversid, LoginInfo.class)
					.uniqueResult();
		} catch (Exception e) {
			return false;
		}
		try {
			Push2Single(loginInfo_driver, content, ID_OTHER);
			System.err.println(",推送成功");
			return true;
		} catch (Exception e) {
			System.err.println(",推送失败,"+e.getMessage());
			return false;
		}
	}
	// 推送给管理员.指定corporationsid推送个某个承运方下所有admin，指定dept推送给所有该部门下的admin，指定adminsid推送给单个admin
	public static boolean Push2Admin(String content, Session session, Long corporationsid, Integer dept,
			Long adminsid) {
		StringBuilder sb = new StringBuilder();
		sb.append("from LoginInfo where datastatus = " + Bean.CREATED + " and status = 1 and type = "
				+ LoginInfo.TYPE_ADMIN);
		if (corporationsid != null)
			sb.append(" and corporationsid = " + corporationsid);
		if (adminsid != null)
			sb.append(" and usersid = " + adminsid);
		if (dept != null)
			sb.append(" and dept = " + dept);
		List<LoginInfo> loginInfos_admin = null;
		try {
			loginInfos_admin = session.createQuery(sb.toString(), LoginInfo.class).list();
		} catch (Exception e) {
			return false;
		}
		if (loginInfos_admin == null)
			return false;
		for (int i = 0; i < loginInfos_admin.size(); i++) {
			try {
				System.err.print("推送给管理员");
				Push2Single(loginInfos_admin.get(i), content, ID_OTHER);
				System.err.println(",推送成功");
			} catch (Exception e) {
				System.err.println(",推送失败,"+e.getMessage());
			}
		}
		return true;
	}
	// 推送给政府.指定dept推送给所有该部门下的政府账号，指定governmentsid推送给单个政府账号
	public static boolean Push2Government(String content, Session session, Integer dept, Long governmentsid) {
		StringBuilder sb = new StringBuilder();
		sb.append("from LoginInfo where datastatus = " + Bean.CREATED + " and status = 1 and type = "
				+ LoginInfo.TYPE_GOVERNMENT);
		if (governmentsid != null)
			sb.append(" and usersid = " + governmentsid);
		if (dept != null)
			sb.append(" and dept = " + dept);
		List<LoginInfo> loginInfos_government = null;
		try {
			loginInfos_government = session.createQuery(sb.toString(), LoginInfo.class).list();
		} catch (Exception e) {
			return false;
		}
		if (loginInfos_government == null)
			return false;
		for (int i = 0; i < loginInfos_government.size(); i++) {
			try {
				System.err.print("推送给政府");
				Push2Single(loginInfos_government.get(i), content, ID_OTHER);
				System.err.println(",推送成功");
			} catch (Exception e) {
				System.err.println(",推送失败,"+e.getMessage());
			}
		}
		return true;
	}
}
