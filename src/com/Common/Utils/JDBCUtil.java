package com.Common.Utils;

import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.HashSet;

public class JDBCUtil {

	private static String driverclass = "com.mysql.jdbc.Driver";
	private static String mysqlurl = "JDBC:mysql://localhost:3306?characterEncoding=utf8&useSSL=true";
	private static String dburl = "JDBC:mysql://localhost:3306/scsywh?characterEncoding=utf8&useSSL=true";
	private static String user = "root";
	private static String password = "abCD1234";



	public static void initDB() throws ClassNotFoundException, SQLException {

		boolean isexist = false;
		boolean test = false;
		try {
			Class.forName(driverclass);
			Connection connection = DriverManager.getConnection(dburl, user, password);
			isexist = true;
			ReleseDB(connection, null, null);
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			isexist = false;
		}

		if (!isexist) {
			System.out.println("Creating Database scsywh");
			Class.forName(driverclass);
			Connection connection = null;
			Statement statement = null;
			try {
				connection = DriverManager.getConnection(mysqlurl, user, password);
				String sql = "CREATE DATABASE scsywh";
				statement = connection.createStatement();
				statement.execute(sql);

			} catch (SQLException e) {
				// TODO Auto-generated catch block
				System.err.println("Exception: " + e.getMessage());
				throw e;
			} finally {
				ReleseDB(connection, statement, null);
			}
		}

		Connection connection = null;
		Statement statement = null;
		try {
			Class.forName(driverclass);
			connection = DriverManager.getConnection(dburl, user, password);
			statement = connection.createStatement();
			String sql;
			
			//for test
			if(test){
				if (!doesTableExist(connection, "t_test1")) {
					System.out.println("Creating Table t_test1");
					sql = "CREATE TABLE `t_test1` (" + "`sid` bigint(10) NOT NULL AUTO_INCREMENT,"
							+ "`thistime` bigint(30) DEFAULT NULL,"
							+ "PRIMARY KEY (`sid`)) ENGINE=InnoDB DEFAULT CHARSET=utf8;";
					statement.execute(sql);
					sql = "ALTER TABLE `t_test1` MODIFY COLUMN `sid` bigint(10) UNSIGNED AUTO_INCREMENT";
					statement.execute(sql);
				}
				
				if (!doesTableExist(connection, "t_test2")) {
					System.out.println("Creating Table t_test2");
					sql = "CREATE TABLE `t_test2` (" + "`sid` bigint(10) NOT NULL AUTO_INCREMENT,"
							+ "`thitime` bigint(30) DEFAULT NULL,"
							+ "PRIMARY KEY (`sid`)) ENGINE=InnoDB DEFAULT CHARSET=utf8;";
					statement.execute(sql);
					sql = "ALTER TABLE `t_test2` MODIFY COLUMN `sid` bigint(10) UNSIGNED AUTO_INCREMENT";
					statement.execute(sql);
				}
				
				sql = "DROP TRIGGER trg_t_test1;";
				statement.execute(sql);
				sql = "CREATE TRIGGER trg_t_test1 "
						+ "BEFORE DELETE ON t_test1 "
						+ "FOR EACH ROW "
						+ "BEGIN "
						+ "INSERT INTO t_test2 "
						+ "SELECT * FROM t_test1 WHERE sid = OLD.sid;"
						+ "END";
				statement.execute(sql);
				sql = "CREATE EVENT IF NOT EXISTS event_test_insert "
						+ "ON SCHEDULE EVERY 1 SECOND "
						+ "DO INSERT INTO t_test1 (thistime) VALUES(CURRENT_TIMESTAMP)";
				statement.execute(sql);
				sql = "ALTER EVENT event_test_insert ENABLE;";
				statement.execute(sql);
				sql = "CREATE EVENT IF NOT EXISTS event_test_delete "
						+ "ON SCHEDULE EVERY 1 SECOND "
						+ "DO DELETE FROM t_test1 where (CURRENT_TIMESTAMP - t_test1.thistime) > 10";
				statement.execute(sql);
				sql = "ALTER EVENT event_test_delete ENABLE;";
				statement.execute(sql);
			}
			
			String Bean_table = "`sid` bigint(20) NOT NULL,"
								+ "`datastatus` int(4) DEFAULT NULL,"
								+ "`corporationsid` bigint(5) DEFAULT NULL,"
								+ "`createdat` bigint(13) DEFAULT NULL," 
								+ "`createdid` bigint(10) DEFAULT NULL,"
								+ "`updatedat` bigint(13) DEFAULT NULL," 
								+ "`updatedid` bigint(10) DEFAULT NULL,";
			
			String LoginInfo_table = "`username` varchar(30) DEFAULT NULL,"
									+ "`name` varchar(20) DEFAULT NULL," 
									+ "`phone` varchar(11) DEFAULT NULL,";  
			
			for (int i = 0; i < 10; i++){
				if (!doesTableExist(connection, "orderp_"+i)) {
					System.out.println("Creating Table orderp_"+i);
					sql = "CREATE TABLE `orderp_"+i+"` (" 
							+ Bean_table
							+ "`orderstatus` int(4) DEFAULT NULL,"
							+ "`ordertime` bigint(13) DEFAULT NULL," 
							+ "`sellersid` bigint(10) DEFAULT NULL," 
							+ "`buyersid` bigint(10) DEFAULT NULL,"
							+ "`productsid` bigint(10) DEFAULT NULL," 
							+ "`loaddateddl` bigint(13) DEFAULT NULL,"
							+ "`unloaddateddl` bigint(13) DEFAULT NULL,"
							+ "`loadaddr` text DEFAULT NULL," 
							+ "`unloadaddr` text DEFAULT NULL,"
							+ "`productweight` double(10,3) DEFAULT NULL," 
							+ "`productvol` double(10,3) DEFAULT NULL,"
							+ "`dispatchtime` bigint(13) DEFAULT NULL," 
							+ "`trucksid` bigint(10) DEFAULT NULL,"
							+ "`trailersid` bigint(10) DEFAULT NULL," 
							+ "`driversid` bigint(10) DEFAULT NULL,"
							+ "`escortsid` bigint(10) DEFAULT NULL,"
							+ "`routesid` bigint(10) DEFAULT NULL,"
							+ "`price` Double(10,3) DEFAULT NULL,"
							+ "`remark` text DEFAULT NULL," 
							+ "`checktime` bigint(13) DEFAULT NULL," 
							+ "`checkret` varchar(30) DEFAULT NULL," 
							+ "`checkstatus` int(4) DEFAULT NULL,"
							+ "`distributetime` bigint(13) DEFAULT NULL,"
							+ "`fareformsid` bigint(20) DEFAULT NULL," 
							+ "`receivetime` bigint(13) DEFAULT NULL,"
							+ "`loadtime` bigint(13) DEFAULT NULL," 
							+ "`loadweight` double(10,3) DEFAULT NULL,"
							+ "`zbweight` double(10,3) DEFAULT NULL," 
							+ "`unloadtime` bigint(13) DEFAULT NULL,"
							+ "`unloadweight` double(10,3) DEFAULT NULL," 
							+ "`returntime` bigint(13) DEFAULT NULL,"
							+ "`returnaddr` text DEFAULT NULL," 
							+ "`verifyppptime` bigint(13) DEFAULT NULL,"
							+ "`verifypppstatus` int(4) DEFAULT NULL," 
							+ "`verifypppret` text DEFAULT NULL,"
							+ "`verifypptime` bigint(13) DEFAULT NULL,"
							+ "`verifyppstatus` int(4) DEFAULT NULL," 
							+ "`verifyppret` text DEFAULT NULL," 
							+ "`verifyptime` bigint(13) DEFAULT NULL,"
							+ "`verifypstatus` int(4) DEFAULT NULL,"
							+ "`verifypret` text DEFAULT NULL,"
							+ "`verifytime` bigint(13) DEFAULT NULL,"
							+ "`verifystatus` int(4) DEFAULT NULL," 
							+ "`verifyret` text DEFAULT NULL," 
							+ "`fuelused` double(10,3) DEFAULT NULL,"
							+ "`distance` double(10,3) DEFAULT NULL," 
							+ "`output` double(10,3) DEFAULT NULL,"
							+ "PRIMARY KEY (`sid`)" 
							+ ") ENGINE=InnoDB DEFAULT CHARSET=utf8;";
					statement.execute(sql);
					sql = "ALTER TABLE `orderp_"+i+"` MODIFY COLUMN `sid` bigint(20) UNSIGNED";
					statement.execute(sql);
				}

				if (!doesTableExist(connection, "fareform_"+i)) {
					System.out.println("Creating Table fareform_"+i);
					sql = "CREATE TABLE `fareform_"+i+"` (" 
							+ Bean_table
							+ "`ordersid` bigint(20) DEFAULT NULL," 
							+ "`trucksid` bigint(10) DEFAULT NULL," 
							+ "`driversid` bigint(10) DEFAULT NULL," 
							+ "`escortsid` bigint(10) DEFAULT NULL,"
							+ "`buyersid` bigint(10) DEFAULT NULL," 
							+ "`loadaddr` text DEFAULT NULL,"
							+ "`unloadaddr` text DEFAULT NULL," 
							+ "`loaddate` bigint(13) DEFAULT NULL," 
							+ "`loadweight` double(10,3) DEFAULT NULL," 
							+ "`zbweight` double(10,3) DEFAULT NULL,"
							+ "`unloaddate` bigint(13) DEFAULT NULL,"
							+ "`returnaddr` text DEFAULT NULL,"
							+ "`price` double(10,3) DEFAULT NULL," 
							+ "`mileload` double(10,3) DEFAULT NULL," 
							+ "`mileunload` double(10,3) DEFAULT NULL,"
							+ "`roadtollload` double(10,3) DEFAULT NULL," 
							+ "`roadtollunload` double(10,3) DEFAULT NULL,"
							+ "`roadtollcash` double(10,3) DEFAULT NULL," 
							+ "`addfuelvol` double(10,3) DEFAULT NULL,"
							+ "`addfuelmoney` double(10,3) DEFAULT NULL," 
							+ "`addfuelcash` double(10,3) DEFAULT NULL,"
							+ "`addfuel` text DEFAULT NULL," 
							+ "`allowancetravel` double(10,3) DEFAULT NULL,"
							+ "`allowancenationalroad` double(10,3) DEFAULT NULL,"
							+ "`fareaddwater` double(10,3) DEFAULT NULL," 
							+ "`faremaintain` double(10,3) DEFAULT NULL,"
							+ "`farefine` double(10,3) DEFAULT NULL," 
							+ "`fareother` double(10,3) DEFAULT NULL,"
							+ "`remark` text DEFAULT NULL," 
							+ "`realloadweight` text DEFAULT NULL," 
							+ "`realunloadweight` text DEFAULT NULL,"
							+ "`fareformstatus` int(4) DEFAULT NULL,"
							+ "`editable` int(4) DEFAULT NULL," 
							+ "`images` text DEFAULT NULL,"
							+ "`freight` double(10,3) DEFAULT NULL,"
							+ "`miletotal` double(10,3) DEFAULT NULL,"
							+ "`allowanceloadroadtoll` double(10,3) DEFAULT NULL,"
							+ "`allowanceunloadroadtoll` double(10,3) DEFAULT NULL,"
							+ "`addfueltotal` double(10,3) DEFAULT NULL," 
							+ "`roadtolltotal` double(10,3) DEFAULT NULL,"
							+ "`allowancetotal` double(10,3) DEFAULT NULL," 
							+ "`drivercash` double(10,3) DEFAULT NULL,"
							+ "PRIMARY KEY (`sid`)" 
							+ ") ENGINE=InnoDB DEFAULT CHARSET=utf8;";
					statement.execute(sql);
					sql = "ALTER TABLE `fareform_"+i+"` MODIFY COLUMN `sid` bigint(20) UNSIGNED";
					statement.execute(sql);
				}
				
				if (!doesTableExist(connection, "trucklog_"+i)) {
					System.out.println("Creating Table trucklog_"+i);
					sql = "CREATE TABLE `trucklog_"+i+"` (" 
							+ Bean_table
							+ "`trucksid` bigint(10) DEFAULT NULL," 
							+ "`ordersid` bigint(20) DEFAULT NULL,"
							+ "`time` bigint(13) DEFAULT NULL,"
							+ "`distance` Double(10,3) DEFAULT NULL," 
							+ "`gpsx` Double(13,9) DEFAULT NULL," 
							+ "`gpsy` Double(13,9) DEFAULT NULL,"
							+ "`speed` Double(10,3) DEFAULT NULL," 
							+ "`battery` varchar(100) DEFAULT NULL," 
							+ "`fuelvol` Double(10,3) DEFAULT NULL,"
							+ "`lefttirepressure` Double(10,3) DEFAULT NULL,"
							+ "`righttirepressure` Double(10,3) DEFAULT NULL," 
							+ "`lefttiretemp` Double(10,3) DEFAULT NULL,"
							+ "`righttiretemp` Double(10,3) DEFAULT NULL," 
							+ "`lock` varchar(10) DEFAULT NULL," 
							+ "`posture` varchar(200) DEFAULT NULL,"
							+ "`haswarn` int(4) DEFAULT NULL,"
							+ "`warnsid` bigint(10) DEFAULT NULL," 
							+ "PRIMARY KEY (`sid`)" 
							+ ") ENGINE=InnoDB DEFAULT CHARSET=utf8;";
					statement.execute(sql);
					sql = "ALTER TABLE `trucklog_"+i+"` MODIFY COLUMN `sid` bigint(20) UNSIGNED";
					statement.execute(sql);
				}
				
				if (!doesTableExist(connection, "lasttrucklog_"+i)) {
					System.out.println("Creating Table lasttrucklog_"+i);
					sql = "CREATE TABLE `lasttrucklog_"+i+"` (" 
							+ Bean_table
							+ "`trucklogsid` bigint(20) DEFAULT NULL," 
							+ "`trucksid` bigint(10) DEFAULT NULL," 
							+ "`ordersid` bigint(20) DEFAULT NULL,"
							+ "`time` bigint(13) DEFAULT NULL,"
							+ "`distance` Double(10,3) DEFAULT NULL," 
							+ "`gpsx` Double(13,9) DEFAULT NULL," 
							+ "`gpsy` Double(13,9) DEFAULT NULL,"
							+ "`speed` Double(10,3) DEFAULT NULL," 
							+ "`battery` varchar(100) DEFAULT NULL," 
							+ "`fuelvol` Double(10,3) DEFAULT NULL,"
							+ "`lefttirepressure` Double(10,3) DEFAULT NULL,"
							+ "`righttirepressure` Double(10,3) DEFAULT NULL," 
							+ "`lefttiretemp` Double(10,3) DEFAULT NULL,"
							+ "`righttiretemp` Double(10,3) DEFAULT NULL," 
							+ "`lock` varchar(10) DEFAULT NULL," 
							+ "`posture` varchar(200) DEFAULT NULL,"
							+ "`haswarn` int(4) DEFAULT NULL,"
							+ "`warnsid` bigint(10) DEFAULT NULL," 
							+ "PRIMARY KEY (`sid`)" 
							+ ") ENGINE=InnoDB DEFAULT CHARSET=utf8;";
					statement.execute(sql);
					sql = "ALTER TABLE `lasttrucklog_"+i+"` MODIFY COLUMN `sid` bigint(20) UNSIGNED";
					statement.execute(sql);
				}

				if (!doesTableExist(connection, "warn_"+i)) {
					System.out.println("Creating Table warn_"+i);
					sql = "CREATE TABLE `warn_"+i+"` (" 
							+ Bean_table
							+ "`corporationname` varchar(30) DEFAULT NULL,"
							+ "`trucksid` bigint(10) DEFAULT NULL," 
							+ "`trucknumber` varchar(30) DEFAULT NULL,"
							+ "`driversid` bigint(10) DEFAULT NULL," 
							+ "`drivername` varchar(30) DEFAULT NULL,"
							+ "`ordersid` bigint(20) DEFAULT NULL," 
							+ "`status` int(4) DEFAULT NULL," 
							+ "`warntype` int(4) DEFAULT NULL,"
							+ "`warntime` bigint(13) DEFAULT NULL," 
							+ "`gpsx` double(13,9) DEFAULT NULL,"
							+ "`gpsy` double(13,9) DEFAULT NULL," 
							+ "`warnvalue` varchar(30) DEFAULT NULL,"
							+ "`warnimages` text DEFAULT NULL," 
							+ "`warndriverresp` text DEFAULT NULL,"
							+ "`warndriverimages` text DEFAULT NULL," 
							+ "PRIMARY KEY (`sid`)"
							+ ") ENGINE=InnoDB DEFAULT CHARSET=utf8;";
					statement.execute(sql);
					sql = "ALTER TABLE `warn_"+i+"` MODIFY COLUMN `sid` bigint(20) UNSIGNED";
					statement.execute(sql);
				}
				
				if (!doesTableExist(connection, "lockinfo_"+i)) {
					System.out.println("Creating Table lockinfo_"+i);
					sql = "CREATE TABLE `lockinfo_"+i+"` (" 
							+ Bean_table
							+ "`corporationname` varchar(30) DEFAULT NULL," 
							+ "`requestat` bigint(13) DEFAULT NULL," 
							+ "`requestfrom` bigint(10) DEFAULT NULL,"
							+ "`drivername` varchar(30) DEFAULT NULL," 
							+ "`trucknumber` varchar(30) DEFAULT NULL,"
							+ "`request` int(4) DEFAULT NULL," 
							+ "`requestdesc` varchar(200) DEFAULT NULL,"
							+ "`responseat` bigint(13) DEFAULT NULL,"
							+ "`response` int(4) DEFAULT NULL," 
							+ "`operatedat` bigint(13) DEFAULT NULL,"
							+ "`operate` int(4) DEFAULT NULL," 
							+ "`status` int(4) DEFAULT NULL,"
							+ "PRIMARY KEY (`sid`)" 
							+ ") ENGINE=InnoDB DEFAULT CHARSET=utf8;";
					statement.execute(sql);
					sql = "ALTER TABLE `lockinfo_"+i+"` MODIFY COLUMN `sid` bigint(20) UNSIGNED";
					statement.execute(sql);
				}
			}
			
			if (!doesTableExist(connection, "exam")) {
				System.out.println("Creating Table exam");
				sql = "CREATE TABLE `exam` (" 
						+ Bean_table
						+ "`name_` varchar(30) DEFAULT NULL,"
						+ "`desc_` varchar(30) DEFAULT NULL,"
						+ "`questions` text DEFAULT NULL,"
						+ "PRIMARY KEY (`sid`)" 
						+ ") ENGINE=InnoDB DEFAULT CHARSET=utf8;";
				statement.execute(sql);
				sql = "ALTER TABLE `exam` MODIFY COLUMN `sid` bigint(10) UNSIGNED AUTO_INCREMENT";
				statement.execute(sql);
			}
			
			if (!doesTableExist(connection, "scorehistory")) {
				System.out.println("Creating Table scorehistory");
				sql = "CREATE TABLE `scorehistory` (" 
						+ Bean_table
						+ "`user_sid` bigint(10) DEFAULT NULL,"
						+ "`exam_sid` bigint(10) DEFAULT NULL,"
						+ "`time_` bigint(13) DEFAULT NULL,"
						+ "`score_` double(10,3) DEFAULT NULL,"
						+ "PRIMARY KEY (`sid`)" 
						+ ") ENGINE=InnoDB DEFAULT CHARSET=utf8;";
				statement.execute(sql);
				sql = "ALTER TABLE `scorehistory` MODIFY COLUMN `sid` bigint(10) UNSIGNED AUTO_INCREMENT";
				statement.execute(sql);
			}
			
			if (!doesTableExist(connection, "document")) {
				System.out.println("Creating Table document");
				sql = "CREATE TABLE `document` (" 
						+ Bean_table
						+ "`title_` varchar(30) DEFAULT NULL,"
						+ "`desc_` varchar(30) DEFAULT NULL,"
						+ "`content_` text DEFAULT NULL,"
						+ "`iconsid` bigint(10) DEFAULT NULL,"
						+ "`imagesid` bigint(10) DEFAULT NULL,"
						+ "PRIMARY KEY (`sid`)" 
						+ ") ENGINE=InnoDB DEFAULT CHARSET=utf8;";
				statement.execute(sql);
				sql = "ALTER TABLE `document` MODIFY COLUMN `sid` bigint(10) UNSIGNED AUTO_INCREMENT";
				statement.execute(sql);
			}
			
			if (!doesTableExist(connection, "readdurhistory")) {
				System.out.println("Creating Table readdurhistory");
				sql = "CREATE TABLE `readdurhistory` (" 
						+ Bean_table
						+ "`driver_sid` bigint(10) DEFAULT NULL,"
						+ "`doc_sid` bigint(10) DEFAULT NULL,"
						+ "`dur` bigint(13) DEFAULT NULL,"
						+ "`drivername` varchar(30) DEFAULT NULL,"
						+ "`docname` varchar(30) DEFAULT NULL,"
						+ "PRIMARY KEY (`sid`)" 
						+ ") ENGINE=InnoDB DEFAULT CHARSET=utf8;";
				statement.execute(sql);
				sql = "ALTER TABLE `readdurhistory` MODIFY COLUMN `sid` bigint(10) UNSIGNED AUTO_INCREMENT";
				statement.execute(sql);
			}
			
			if (!doesTableExist(connection, "totaldur")) {
				System.out.println("Creating Table totaldur");
				sql = "CREATE TABLE `totaldur` (" 
						+ Bean_table
						+ "`driver_sid` bigint(10) DEFAULT NULL,"
						+ "`totaldur` bigint(13) DEFAULT NULL,"
						+ "PRIMARY KEY (`sid`)" 
						+ ") ENGINE=InnoDB DEFAULT CHARSET=utf8;";
				statement.execute(sql);
				sql = "ALTER TABLE `totaldur` MODIFY COLUMN `sid` bigint(10) UNSIGNED AUTO_INCREMENT";
				statement.execute(sql);
			}
			
			if (!doesTableExist(connection, "logininfo")) {
				System.out.println("Creating Table logininfo");
				sql = "CREATE TABLE `logininfo` (" 
						+ Bean_table
						+ "`usersid` bigint(10) DEFAULT NULL," 
						+ "`vcode` varchar(6) DEFAULT NULL,"
						+ "`status` int(4) DEFAULT NULL," 
						+ "`username` varchar(30) DEFAULT NULL,"
						+ "`type` int(4) DEFAULT NULL," 
						+ "`phone` varchar(11) DEFAULT NULL,"
						+ "`salt` varchar(10) DEFAULT NULL,"
						+ "`password` varchar(100) DEFAULT NULL," 
						+ "`lastlogintime` bigint(13) DEFAULT NULL,"
						+ "`tokentime` bigint(13) DEFAULT NULL," 
						+ "`token` varchar(32) DEFAULT NULL,"
						+ "`dept` int(4) DEFAULT NULL,"
						+ "`onlinetype` int(4) DEFAULT NULL," 
						+ "`cid` varchar(32) DEFAULT NULL,"
						+ "`wsid` varchar(32) DEFAULT NULL,"
						+ "`isdocauthd` int(4) DEFAULT 1,"
						+ "`isexmauthd` int(4) DEFAULT 1,"
						+ "PRIMARY KEY (`sid`)" 
						+ ") ENGINE=InnoDB DEFAULT CHARSET=utf8;";
				statement.execute(sql);
				sql = "ALTER TABLE `logininfo` MODIFY COLUMN `sid` bigint(10) UNSIGNED AUTO_INCREMENT";
				statement.execute(sql);
				sql = "INSERT INTO logininfo(datastatus, corporationsid, status, type, username, salt, password) values " +
						"(0, -1, 1, 233, 'superadmin', 'suger', '2fc5d84771b45a0966de6afdeac4231d')";
				//username = "superadmin" password = "qwer1234ABCD4321" salt = "suger"
				statement.execute(sql);
			}

			if (!doesTableExist(connection, "tablechange")) {
				System.out.println("Creating Table tablechange");
				sql = "CREATE TABLE `tablechange` (" 
						+ "`sid` bigint(10) NOT NULL AUTO_INCREMENT,"
						+ "`time` bigint(13) DEFAULT NULL," 
						+ "`tabletype` int(4) DEFAULT NULL,"
						+ "`opearatortype` int(4) DEFAULT NULL," 
						+ "`operatorsid` bigint(10) DEFAULT NULL,"
						+ "`oldobejct` text DEFAULT NULL," 
						+ "`newobject` text DEFAULT NULL," 
						+ "PRIMARY KEY (`sid`)"
						+ ") ENGINE=InnoDB DEFAULT CHARSET=utf8;";
				statement.execute(sql);
				sql = "ALTER TABLE `tablechange` MODIFY COLUMN `sid` bigint(10) UNSIGNED AUTO_INCREMENT";
				statement.execute(sql);
			}
//			if (!doesTableExist(connection, "filestore")) {
//				System.out.println("Creating Table filestore");
//				sql = "CREATE TABLE `filestore` (" + "`sid` bigint(10) NOT NULL AUTO_INCREMENT,"
//						+ "`datastatus` int(4) DEFAULT NULL," + "`filetype` varchar(30) DEFAULT NULL,"
//						+ "`filename` varchar(100) DEFAULT NULL," + "`time` bigint(13) DEFAULT NULL,"
//						+ "`data` longblob DEFAULT NULL," + "PRIMARY KEY (`sid`)"
//						+ ") ENGINE=InnoDB DEFAULT CHARSET=utf8;";
//				statement.execute(sql);
//				sql = "ALTER TABLE `filestore` MODIFY COLUMN `sid` bigint(10) UNSIGNED AUTO_INCREMENT";
//				statement.execute(sql);
//			}
			
			if (!doesTableExist(connection, "filestore")) {
				System.out.println("Creating Table filestore");
				sql = "CREATE TABLE `filestore` (" 
						+ Bean_table
						+ "`filetype` varchar(30) DEFAULT NULL," 
						+ "`filename` varchar(100) DEFAULT NULL,"
						+ "`time` bigint(13) DEFAULT NULL," 
						+ "`savepath` varchar(256) DEFAULT NULL,"
						+ "`url` varchar(256) DEFAULT NULL,"
						+ "PRIMARY KEY (`sid`)" 
						+ ") ENGINE=InnoDB DEFAULT CHARSET=utf8;";
				statement.execute(sql);
				sql = "ALTER TABLE `filestore` MODIFY COLUMN `sid` bigint(10) UNSIGNED AUTO_INCREMENT";
				statement.execute(sql);
			}

			if (!doesTableExist(connection, "truckarchives")) {
				System.out.println("Creating Table truckarchives");
				sql = "CREATE TABLE `truckarchives` (" 
						+ Bean_table
						+ "`trucknumber` varchar(30) DEFAULT NULL," 
						+ "`status` int(4) DEFAULT NULL," 
						+ "`validtime` varchar(30) DEFAULT NULL,"
						+ "`manname` varchar(30) DEFAULT NULL," 
						+ "`manphone` varchar(30) DEFAULT NULL,"
						+ "`dlyszh` varchar(30) DEFAULT NULL," 
						+ "`jyfw` varchar(30) DEFAULT NULL,"
						+ "`cpys` varchar(30) DEFAULT NULL," 
						+ "`ygjg` varchar(30) DEFAULT NULL,"
						+ "`cx` varchar(30) DEFAULT NULL," 
						+ "`cxfl` varchar(30) DEFAULT NULL,"
						+ "`clfl` varchar(30) DEFAULT NULL," 
						+ "`cllx` varchar(30) DEFAULT NULL,"
						+ "`cz` varchar(30) DEFAULT NULL," 
						+ "`csys` varchar(30) DEFAULT NULL,"
						+ "`dph` varchar(30) DEFAULT NULL," 
						+ "`cp` varchar(30) DEFAULT NULL,"
						+ "`fdjh` varchar(30) DEFAULT NULL," 
						+ "`cljsdj` varchar(30) DEFAULT NULL,"
						+ "`lastannualtime` bigint(13) DEFAULT NULL," 
						+ "`lastsecondtime` bigint(13) DEFAULT NULL,"
						+ "`lastsecondcontent` varchar(100) DEFAULT NULL," 
						+ "`annalduration` bigint(13) DEFAULT NULL," 
						+ "`secondduration` bigint(13) DEFAULT NULL,"
						+ "`nextannualtime` bigint(13) DEFAULT NULL," 
						+ "`nextsecondtime` bigint(13) DEFAULT NULL,"
						+ "PRIMARY KEY (`sid`)" 
						+ ") ENGINE=InnoDB DEFAULT CHARSET=utf8;";
				statement.execute(sql);
				sql = "ALTER TABLE `truckarchives` MODIFY COLUMN `sid` bigint(10) UNSIGNED AUTO_INCREMENT";
				statement.execute(sql);
			}

			if (!doesTableExist(connection, "truckmaintain")) {
				System.out.println("Creating Table truckmaintain");
				sql = "CREATE TABLE `truckmaintain` (" 
						+ Bean_table
						+ "`trucknumber` varchar(30) DEFAULT NULL," 
						+ "`wxdh` varchar(30) DEFAULT NULL," 
						+ "`cpys` varchar(30) DEFAULT NULL,"
						+ "`cllx` varchar(30) DEFAULT NULL," 
						+ "`dph` varchar(30) DEFAULT NULL,"
						+ "`txf` varchar(30) DEFAULT NULL," 
						+ "`txfdh` varchar(30) DEFAULT NULL,"
						+ "`wxnr` varchar(30) DEFAULT NULL," 
						+ "`fyhj` double(10,3) DEFAULT NULL,"
						+ "`bxq` bigint(13) DEFAULT NULL," 
						+ "PRIMARY KEY (`sid`)" 
						+ ") ENGINE=InnoDB DEFAULT CHARSET=utf8;";
				statement.execute(sql);
				sql = "ALTER TABLE `truckmaintain` MODIFY COLUMN `sid` bigint(10) UNSIGNED AUTO_INCREMENT";
				statement.execute(sql);
			}

			if (!doesTableExist(connection, "truckmaintainstatistics")) {
				System.out.println("Creating Table truckmaintainstatistics");
				sql = "CREATE TABLE `truckmaintainstatistics` (" 
						+ Bean_table
						+ "`trucknumber` varchar(30) DEFAULT NULL," 
						+ "`maintaintimes` int(4) DEFAULT NULL," 
						+ "`zjjcrq` bigint(13) DEFAULT NULL," 
						+ "`zjccrq` bigint(13) DEFAULT NULL,"
						+ "PRIMARY KEY (`sid`)" + ") ENGINE=InnoDB DEFAULT CHARSET=utf8;";
				statement.execute(sql);
				sql = "ALTER TABLE `truckmaintainstatistics` MODIFY COLUMN `sid` bigint(10) UNSIGNED AUTO_INCREMENT";
				statement.execute(sql);
			}

			if (!doesTableExist(connection, "route")) {
				System.out.println("Creating Table route");
				sql = "CREATE TABLE `route` (" 
						+ Bean_table
						+ "`name` varchar(20) DEFAULT NULL," 
						+ "`transportsrc` varchar(20) DEFAULT NULL,"
						+ "`transportdst` varchar(20) DEFAULT NULL," 
						+ "`transportdistance` Double(10,3) DEFAULT NULL," 
						+ "`viaprovince` text DEFAULT NULL,"
						+ "`price` Double(10,3) DEFAULT NULL," 
						+ "`remark` text DEFAULT NULL," 
						+ "PRIMARY KEY (`sid`)"
						+ ") ENGINE=InnoDB DEFAULT CHARSET=utf8;";
				statement.execute(sql);
				sql = "ALTER TABLE `route` MODIFY COLUMN `sid` bigint(10) UNSIGNED AUTO_INCREMENT";
				statement.execute(sql);
			}

			if (!doesTableExist(connection, "buyer")) {
				System.out.println("Creating Table buyer");
				sql = "CREATE TABLE `buyer` (" 
						+ Bean_table
						+ LoginInfo_table
						+ "`manname` varchar(20) DEFAULT NULL,"
						+ "`address` text DEFAULT NULL,"
						+ "PRIMARY KEY (`sid`)" + ") ENGINE=InnoDB DEFAULT CHARSET=utf8;";
				statement.execute(sql);
				sql = "ALTER TABLE `buyer` MODIFY COLUMN `sid` bigint(10) UNSIGNED AUTO_INCREMENT";
				statement.execute(sql);
			}

			if (!doesTableExist(connection, "seller")) {
				System.out.println("Creating Table seller");
				sql = "CREATE TABLE `seller` (" 
						+ Bean_table
						+ LoginInfo_table
						+ "`manname` varchar(20) DEFAULT NULL,"
						+ "`address` text DEFAULT NULL,"
						+ "PRIMARY KEY (`sid`)" 
						+ ") ENGINE=InnoDB DEFAULT CHARSET=utf8;";
				statement.execute(sql);
				sql = "ALTER TABLE `seller` MODIFY COLUMN `sid` bigint(10) UNSIGNED AUTO_INCREMENT";
				statement.execute(sql);
			}

			if (!doesTableExist(connection, "product")) {
				System.out.println("Creating Table product");
				sql = "CREATE TABLE `product` (" 
						+ Bean_table
						+ "`name` varchar(20) DEFAULT NULL," 
						+ "`remark` text DEFAULT NULL,"
						+ "`unnumber` varchar(20) DEFAULT NULL," 
						+ "`type` int(4) DEFAULT NULL,"
						+ "`packettype` int(4) DEFAULT NULL," 
						+ "`packetrank` int(4) DEFAULT NULL,"
						+ "PRIMARY KEY (`sid`)" 
						+ ") ENGINE=InnoDB DEFAULT CHARSET=utf8;";
				statement.execute(sql);
				sql = "ALTER TABLE `product` MODIFY COLUMN `sid` bigint(10) UNSIGNED AUTO_INCREMENT";
				statement.execute(sql);
			}

			if (!doesTableExist(connection, "driver")) {
				System.out.println("Creating Table driver");
				sql = "CREATE TABLE `driver` (" 
						+ Bean_table
						+ LoginInfo_table
						+ "`QCtype` int(4) DEFAULT NULL," 
						+ "`QCnumber` varchar(20) DEFAULT NULL,"
						+ "`QCddl` bigint(13) DEFAULT NULL," 
						+ "`QCorganization` varchar(20) DEFAULT NULL,"
						+ "`joinjobtime` bigint(13) DEFAULT NULL,"
						+ "`labourcontractddl` bigint(13) DEFAULT NULL," 
						+ "`status` int(4) DEFAULT NULL," 
						+ "PRIMARY KEY (`sid`)" 
						+ ") ENGINE=InnoDB DEFAULT CHARSET=utf8;";
				statement.execute(sql);
				sql = "ALTER TABLE `driver` MODIFY COLUMN `sid` bigint(10) UNSIGNED AUTO_INCREMENT";
				statement.execute(sql);
			}
			
			if (!doesTableExist(connection, "escort")) {
				System.out.println("Creating Table escort");
				sql = "CREATE TABLE `escort` (" 
						+ Bean_table
						+ LoginInfo_table
						+ "`QCtype` int(4) DEFAULT NULL," 
						+ "`QCnumber` varchar(20) DEFAULT NULL,"
						+ "`QCddl` bigint(13) DEFAULT NULL," 
						+ "`QCorganization` varchar(20) DEFAULT NULL,"
						+ "`joinjobtime` bigint(13) DEFAULT NULL,"
						+ "`labourcontractddl` bigint(13) DEFAULT NULL," 
						+ "`status` int(4) DEFAULT NULL," 
						+ "PRIMARY KEY (`sid`)" 
						+ ") ENGINE=InnoDB DEFAULT CHARSET=utf8;";
				statement.execute(sql);
				sql = "ALTER TABLE `escort` MODIFY COLUMN `sid` bigint(10) UNSIGNED AUTO_INCREMENT";
				statement.execute(sql);
			}

			if (!doesTableExist(connection, "admin")) {
				System.out.println("Creating Table admin");
				sql = "CREATE TABLE `admin` (" 
						+ Bean_table
						+ LoginInfo_table
						+ "`dept` int(4) DEFAULT NULL,"
						+ "`email` varchar(30) DEFAULT NULL,"
						+ "PRIMARY KEY (`sid`)" 
						+ ") ENGINE=InnoDB DEFAULT CHARSET=utf8;";
				statement.execute(sql);
				sql = "ALTER TABLE `admin` MODIFY COLUMN `sid` bigint(10) UNSIGNED AUTO_INCREMENT";
				statement.execute(sql);
			}

			if (!doesTableExist(connection, "government")) {
				System.out.println("Creating Table government");
				sql = "CREATE TABLE `government` (" 
						+ Bean_table
						+ LoginInfo_table
						+ "`dept` int(4) DEFAULT NULL," 
						+ "PRIMARY KEY (`sid`)" 
						+ ") ENGINE=InnoDB DEFAULT CHARSET=utf8;";
				statement.execute(sql);
				sql = "ALTER TABLE `government` MODIFY COLUMN `sid` bigint(10) UNSIGNED AUTO_INCREMENT";
				statement.execute(sql);
			}

			if (!doesTableExist(connection, "manufacturer")) {
				System.out.println("Creating Table manufacturer");
				sql = "CREATE TABLE `manufacturer` (" 
						+ Bean_table
						+ LoginInfo_table
						+ "PRIMARY KEY (`sid`)"
						+ ") ENGINE=InnoDB DEFAULT CHARSET=utf8;";
				statement.execute(sql);
				sql = "ALTER TABLE `manufacturer` MODIFY COLUMN `sid` bigint(10) UNSIGNED AUTO_INCREMENT";
				statement.execute(sql);
			}

			if (!doesTableExist(connection, "corporation")) {
				System.out.println("Creating Table corporation");
				sql = "CREATE TABLE `corporation` (" 
						+ Bean_table
						+ LoginInfo_table
						+ "PRIMARY KEY (`sid`)"
						+ ") ENGINE=InnoDB DEFAULT CHARSET=utf8;";
				statement.execute(sql);
				sql = "ALTER TABLE `corporation` MODIFY COLUMN `sid` bigint(10) UNSIGNED AUTO_INCREMENT";
				statement.execute(sql);
			}

			if (!doesTableExist(connection, "trailer")) {
				System.out.println("Creating Table trailer");
				sql = "CREATE TABLE `trailer` (" 
						+ Bean_table
						+ "`trailernumber` varchar(20) DEFAULT NULL," 
						+ "`trailerstatus` int(4) DEFAULT NULL,"
						+ "`model` varchar(20) DEFAULT NULL," 
						+ "`vol` Double(10,3) DEFAULT NULL,"
						+ "`weight` Double(10,3) DEFAULT NULL," 
						+ "`RTCnumber` varchar(20) DEFAULT NULL,"
						+ "`RTCtime` bigint(13) DEFAULT NULL," 
						+ "`RTCddl` bigint(13) DEFAULT NULL,"
						+ "`RTCorganization` varchar(50) DEFAULT NULL," 
						+ "`businessscope` text DEFAULT NULL,"
						+ "`nextapprovingtime` bigint(13) DEFAULT NULL," 
						+ "`type` int(4) DEFAULT NULL,"
						+ "PRIMARY KEY (`sid`)" + ") ENGINE=InnoDB DEFAULT CHARSET=utf8;";
				statement.execute(sql);
				sql = "ALTER TABLE `trailer` MODIFY COLUMN `sid` bigint(10) UNSIGNED AUTO_INCREMENT";
				statement.execute(sql);
				sql = "ALTER TABLE `trailer` ADD UNIQUE INDEX IndexName(`sid`, `trailernumber`)";
				statement.execute(sql);
			}

			if (!doesTableExist(connection, "truck")) {
				System.out.println("Creating Table truck");
				sql = "CREATE TABLE `truck` (" 
						+ Bean_table
						+ "`trucknumber` varchar(20) DEFAULT NULL," 
						+ "`truckstatus` int(4) DEFAULT NULL,"
						+ "`trucktype` int(4) DEFAULT NULL," 
						+ "`model` varchar(20) DEFAULT NULL,"
						+ "`weight` double(10,3) DEFAULT NULL,"
						+ "`vol` double(10,3) DEFAULT NULL,"
						+ "`cid` varchar(32) DEFAULT NULL," 
						+ "`RTCnumber` varchar(20) DEFAULT NULL,"
						+ "`RTCtime` bigint(13) DEFAULT NULL," 
						+ "`RTCddl` bigint(13) DEFAULT NULL,"
						+ "`RTCorganization` varchar(20) DEFAULT NULL," 
						+ "`insurancemoney` double(10,3) DEFAULT NULL," 
						+ "`insuranceddl` bigint(13) DEFAULT NULL,"
						+ "`driversid` bigint(10) DEFAULT NULL," 
						+ "`escortsid` bigint(10) DEFAULT NULL,"
						+ "`trailersid` bigint(10) DEFAULT NULL," 
						+ "`truckarchivessid` bigint(10) DEFAULT NULL,"
						+ "`truckmaintainstatisticssid` bigint(10) DEFAULT NULL,"
						+ "PRIMARY KEY (`sid`)" 
						+ ") ENGINE=InnoDB DEFAULT CHARSET=utf8;";
				statement.execute(sql);
				sql = "ALTER TABLE `truck` MODIFY COLUMN `sid` bigint(10) UNSIGNED AUTO_INCREMENT";
				statement.execute(sql);
			}

			if (!doesTableExist(connection, "ordermonthstatistics")) {
				System.out.println("Creating Table ordermonthstatistics");
				sql = "CREATE TABLE `ordermonthstatistics` (" 
						+ Bean_table
						+ "`year` varchar(4) DEFAULT NULL," 
						+ "`month` varchar(2) DEFAULT NULL,"
						+ "`objectsid` bigint(10) DEFAULT NULL," 
						+ "`objecttype` int(4) DEFAULT NULL,"
						+ "`orderamount` bigint(10) DEFAULT NULL," 
						+ "`objectname` varchar(30) DEFAULT NULL,"
						+ "`fuelused` double(10,3) DEFAULT NULL," 
						+ "`distance` double(10,3) DEFAULT NULL,"
						+ "`output` double(10,3) DEFAULT NULL," 
						+ "PRIMARY KEY (`SID`)"
						+ ") ENGINE=InnoDB DEFAULT CHARSET=utf8;";
				statement.execute(sql);
				sql = "ALTER TABLE `ordermonthstatistics` MODIFY COLUMN `SID` bigint(10) UNSIGNED AUTO_INCREMENT";
				statement.execute(sql);
			}

			if (!doesTableExist(connection, "orderyearstatistics")) {
				System.out.println("Creating Table orderyearstatistics");
				sql = "CREATE TABLE `orderyearstatistics` (" 
						+ Bean_table
						+ "`year` varchar(4) DEFAULT NULL,"
						+ "`objectsid` bigint(10) DEFAULT NULL," 
						+ "`objecttype` int(4) DEFAULT NULL,"
						+ "`orderamount` bigint(10) DEFAULT NULL," 
						+ "`fuelused` double(10,3) DEFAULT NULL," 
						+ "`distance` double(10,3) DEFAULT NULL,"
						+ "`output` double(10,3) DEFAULT NULL," 
						+ "`objectname` varchar(30) DEFAULT NULL,"
						+ "PRIMARY KEY (`SID`)" 
						+ ") ENGINE=InnoDB DEFAULT CHARSET=utf8;";
				statement.execute(sql);
				sql = "ALTER TABLE `orderyearstatistics` MODIFY COLUMN `SID` bigint(10) UNSIGNED AUTO_INCREMENT";
				statement.execute(sql);
			}

			if (!doesTableExist(connection, "warnmonthstatistics")) {
				System.out.println("Creating Table warnmonthstatistics");
				sql = "CREATE TABLE `warnmonthstatistics` (" 
						+ Bean_table
						+ "`year` varchar(4) DEFAULT NULL," 
						+ "`month` varchar(2) DEFAULT NULL,"
						+ "`objectsid` bigint(10) DEFAULT NULL," 
						+ "`objecttype` int(4) DEFAULT NULL,"
						+ "`lockamount` bigint(10) DEFAULT NULL," 
						+ "`leakamount` bigint(10) DEFAULT NULL,"
						+ "`tireamount` bigint(10) DEFAULT NULL," 
						+ "`fuelamount` bigint(10) DEFAULT NULL,"
						+ "`overspeedamount` bigint(10) DEFAULT NULL," 
						+ "`parkamount` bigint(10) DEFAULT NULL,"
						+ "`fatiguedrivingamount` bigint(10) DEFAULT NULL,"
						+ "`suddenbrakeamount` bigint(10) DEFAULT NULL,"
						+ "`suddenaccelamount` bigint(10) DEFAULT NULL," 
						+ "`accidentamount` bigint(10) DEFAULT NULL,"
						+ "`overloadamount` bigint(10) DEFAULT NULL," 
						+ "`objectname` varchar(30) DEFAULT NULL,"
						+ "PRIMARY KEY (`SID`)" 
						+ ") ENGINE=InnoDB DEFAULT CHARSET=utf8;";
				statement.execute(sql);
				sql = "ALTER TABLE `warnmonthstatistics` MODIFY COLUMN `SID` bigint(10) UNSIGNED AUTO_INCREMENT";
				statement.execute(sql);
			}

			if (!doesTableExist(connection, "warnyearstatistics")) {
				System.out.println("Creating Table warnyearstatistics");
				sql = "CREATE TABLE `warnyearstatistics` (" 
						+ Bean_table
						+ "`year` varchar(4) DEFAULT NULL,"
						+ "`objectsid` bigint(10) DEFAULT NULL," 
						+ "`objecttype` int(4) DEFAULT NULL,"
						+ "`objectname` varchar(30) DEFAULT NULL," 
						+ "`lockamount` bigint(10) DEFAULT NULL,"
						+ "`leakamount` bigint(10) DEFAULT NULL," 
						+ "`tireamount` bigint(10) DEFAULT NULL,"
						+ "`fuelamount` bigint(10) DEFAULT NULL," 
						+ "`overspeedamount` bigint(10) DEFAULT NULL,"
						+ "`parkamount` bigint(10) DEFAULT NULL," 
						+ "`fatiguedrivingamount` bigint(10) DEFAULT NULL,"
						+ "`suddenbrakeamount` bigint(10) DEFAULT NULL,"
						+ "`suddenaccelamount` bigint(10) DEFAULT NULL," 
						+ "`accidentamount` bigint(10) DEFAULT NULL,"
						+ "`overloadamount` bigint(10) DEFAULT NULL," 
						+ "PRIMARY KEY (`SID`)" + ") ENGINE=InnoDB DEFAULT CHARSET=utf8;";
				statement.execute(sql);
				sql = "ALTER TABLE `warnyearstatistics` MODIFY COLUMN `SID` bigint(10) UNSIGNED AUTO_INCREMENT";
				statement.execute(sql);
			}

			if (!doesTableExist(connection, "requesthistory")) {
				System.out.println("Creating Table requesthistory");
				sql = "CREATE TABLE `requesthistory` (" 
						+ "`SID` bigint(10) NOT NULL AUTO_INCREMENT,"
						+ "`IP` varchar(15) DEFAULT NULL," 
						+ "`time` varchar(20) DEFAULT NULL,"
						+ "`username` varchar(30) DEFAULT NULL," 
						+ "`corporation` bigint(5) DEFAULT NULL,"
						+ "`path` text DEFAULT NULL," 
						+ "`head` text DEFAULT NULL," + "`content` text DEFAULT NULL,"
						+ "`response` text DEFAULT NULL," 
						+ "PRIMARY KEY (`SID`)"
						+ ") ENGINE=InnoDB DEFAULT CHARSET=utf8;";
				statement.execute(sql);
				sql = "ALTER TABLE `requesthistory` MODIFY COLUMN `SID` bigint(10) UNSIGNED AUTO_INCREMENT";
				statement.execute(sql);
			}

			if (!doesTableExist(connection, "errormsgs")) {
				System.out.println("Creating Table errormsgs");
				sql = "CREATE TABLE `errormsgs` (" 
						+ "`sid` bigint(10) NOT NULL AUTO_INCREMENT,"
						+ "`ip` varchar(15) DEFAULT NULL," 
						+ "`addr` varchar(150) DEFAULT NULL,"
						+ "`logininfosid` bigint(10) DEFAULT NULL," 
						+ "`username` varchar(30) DEFAULT NULL," 
						+ "`corporation` bigint(5) DEFAULT NULL,"
						+ "`errormsg` text DEFAULT NULL," 
						+ "`path` text DEFAULT NULL," 
						+ "`params` text DEFAULT NULL," 
						+ "`time` varchar(20) DEFAULT NULL,"
						+ "PRIMARY KEY (`sid`)) ENGINE=InnoDB DEFAULT CHARSET=utf8;";
				statement.execute(sql);
				sql = "ALTER TABLE `errormsgs` MODIFY COLUMN `sid` bigint(10) UNSIGNED AUTO_INCREMENT";
				statement.execute(sql);
			}
		} catch (Exception e) {
			System.err.println("Exception: " + e.getMessage());
			throw e;
		} finally {
			ReleseDB(connection, statement, null);
		}
	}

	private static Boolean doesTableExist(Connection con, String tablename) {
		HashSet<String> set = new HashSet<String>();
		try {
			DatabaseMetaData meta = con.getMetaData();
			ResultSet res = meta.getTables(null, null, null, new String[] { "TABLE" });
			while (res.next()) {
				set.add(res.getString("TABLE_NAME"));
			}
			ReleseDB(null, null, res);
		} catch (Exception e) {
			System.err.println("Exception: " + e.getMessage());
		}
		return set.contains(tablename);
	}

	private static void ReleseDB(Connection conn, Statement statement, ResultSet resultSet) {
		if (resultSet != null)
			try {
				resultSet.close();
			} catch (SQLException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		if (statement != null)
			try {
				statement.close();
			} catch (SQLException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		if (conn != null)
			try {
				conn.close();
			} catch (SQLException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
	}
}

// if (!doesTableExist(connection, "componentreplacement")) {
// System.out.println("Creating Table componentreplacement");
// sql = "CREATE TABLE `componentreplacement` (" + "`sid` bigint(10) NOT NULL
// AUTO_INCREMENT,"
// + "`carnumber` varchar(30) DEFAULT NULL," + "`vinnumber` varchar(30) DEFAULT
// NULL,"
// + "`date` bigint(13) DEFAULT NULL," + "`partname` varchar(30) DEFAULT NULL,"
// + "`partts` varchar(30) DEFAULT NULL," + "`manufacturername` varchar(30)
// DEFAULT NULL,"
// + "`partnumber` varchar(30) DEFAULT NULL," + "`maintainunit` varchar(30)
// DEFAULT NULL,"
// + "`data` text DEFAULT NULL," + "`createdat` bigint(13) DEFAULT NULL,"
// + "`createdid` bigint(10) DEFAULT NULL," + "`updatedat` bigint(13) DEFAULT
// NULL,"
// + "`updatedid` bigint(10) DEFAULT NULL," + "PRIMARY KEY (`sid`)"
// + ") ENGINE=InnoDB DEFAULT CHARSET=utf8;";
// statement.execute(sql);
// sql = "ALTER TABLE `componentreplacement` MODIFY COLUMN `sid` bigint(10)
// UNSIGNED AUTO_INCREMENT";
// statement.execute(sql);
// }
//
// if (!doesTableExist(connection, "trucktest")) {
// System.out.println("Creating Table trucktest");
// sql = "CREATE TABLE `trucktest` (" + "`sid` bigint(10) NOT NULL
// AUTO_INCREMENT,"
// + "`carnumber` varchar(30) DEFAULT NULL," + "`vinnumber` varchar(30) DEFAULT
// NULL,"
// + "`testtime` bigint(13) DEFAULT NULL," + "`testtype` varchar(30) DEFAULT
// NULL,"
// + "`testunit` varchar(30) DEFAULT NULL," + "`testvalidity` varchar(30)
// DEFAULT NULL,"
// + "`reportnumber` varchar(30) DEFAULT NULL," + "`corporation` varchar(30)
// DEFAULT NULL,"
// + "`data` text DEFAULT NULL," + "`createdat` bigint(13) DEFAULT NULL,"
// + "`createdid` bigint(10) DEFAULT NULL," + "`updatedat` bigint(13) DEFAULT
// NULL,"
// + "`updatedid` bigint(10) DEFAULT NULL," + "PRIMARY KEY (`sid`)"
// + ") ENGINE=InnoDB DEFAULT CHARSET=utf8;";
// statement.execute(sql);
// sql = "ALTER TABLE `trucktest` MODIFY COLUMN `sid` bigint(10) UNSIGNED
// AUTO_INCREMENT";
// statement.execute(sql);
// }
//
// if (!doesTableExist(connection, "truckmaintain2")) {
// System.out.println("Creating Table truckmaintain2");
// sql = "CREATE TABLE `truckmaintain2` (" + "`sid` bigint(10) NOT NULL
// AUTO_INCREMENT,"
// + "`carnumber` varchar(30) DEFAULT NULL," + "`vinnumber` varchar(30) DEFAULT
// NULL,"
// + "`maintaintime` bigint(13) DEFAULT NULL," + "`certificatenumber`
// varchar(30) DEFAULT NULL,"
// + "`maintaintype` varchar(30) DEFAULT NULL," + "`corporation` varchar(30)
// DEFAULT NULL,"
// + "`maintainunit` varchar(30) DEFAULT NULL," + "`data` text DEFAULT NULL,"
// + "`createdat` bigint(13) DEFAULT NULL," + "`createdid` bigint(10) DEFAULT
// NULL,"
// + "`updatedat` bigint(13) DEFAULT NULL," + "`updatedid` bigint(10) DEFAULT
// NULL,"
// + "PRIMARY KEY (`sid`)" + ") ENGINE=InnoDB DEFAULT CHARSET=utf8;";
// statement.execute(sql);
// sql = "ALTER TABLE `truckmaintain2` MODIFY COLUMN `sid` bigint(10) UNSIGNED
// AUTO_INCREMENT";
// statement.execute(sql);
// }
//
// if (!doesTableExist(connection, "passageexpstatistics")) {
// System.out.println("Creating Table passageexpstatistics");
// sql = "CREATE TABLE `passageexpstatistics` (" + "`sid` bigint(10) NOT NULL
// AUTO_INCREMENT,"
// + "`carnumber` varchar(30) DEFAULT NULL," + "`cardnumber` varchar(30) DEFAULT
// NULL,"
// + "`totalcost` Double(10,3) DEFAULT NULL," + "`lastbalance` Double(10,3)
// DEFAULT NULL,"
// + "`lasttime` bigint(13) DEFAULT NULL," + "`createdat` bigint(13) DEFAULT
// NULL,"
// + "`createdid` bigint(10) DEFAULT NULL," + "`updatedat` bigint(13) DEFAULT
// NULL,"
// + "`updatedid` bigint(10) DEFAULT NULL," + "PRIMARY KEY (`sid`)"
// + ") ENGINE=InnoDB DEFAULT CHARSET=utf8;";
// statement.execute(sql);
// sql = "ALTER TABLE `passageexpstatistics` MODIFY COLUMN `sid` bigint(10)
// UNSIGNED AUTO_INCREMENT";
// statement.execute(sql);
// }
//
// if (!doesTableExist(connection, "passageexp")) {
// System.out.println("Creating Table passageexp");
// sql = "CREATE TABLE `passageexp` (" + "`sid` bigint(10) NOT NULL
// AUTO_INCREMENT,"
// + "`cardnumber` varchar(30) DEFAULT NULL," + "`carnumber` varchar(30) DEFAULT
// NULL,"
// + "`consumptiondate` bigint(13) DEFAULT NULL," + "`servicepart` varchar(30)
// DEFAULT NULL,"
// + "`inbox` varchar(30) DEFAULT NULL," + "`outbox` varchar(30) DEFAULT NULL,"
// + "`cost` Double(10,3) DEFAULT NULL," + "`balance` Double(10,3) DEFAULT
// NULL,"
// + "`data` text DEFAULT NULL," + "`createdat` bigint(13) DEFAULT NULL,"
// + "`createdid` bigint(10) DEFAULT NULL," + "`updatedat` bigint(13) DEFAULT
// NULL,"
// + "`updatedid` bigint(10) DEFAULT NULL," + "PRIMARY KEY (`sid`)"
// + ") ENGINE=InnoDB DEFAULT CHARSET=utf8;";
// statement.execute(sql);
// sql = "ALTER TABLE `passageexp` MODIFY COLUMN `sid` bigint(10) UNSIGNED
// AUTO_INCREMENT";
// statement.execute(sql);
// }
//
// if (!doesTableExist(connection, "trafficexp")) {
// System.out.println("Creating Table trafficexp");
// sql = "CREATE TABLE `trafficexp` (" + "`sid` bigint(10) NOT NULL
// AUTO_INCREMENT,"
// + "`carnumber` varchar(30) DEFAULT NULL," + "`drivername` varchar(30) DEFAULT
// NULL,"
// + "`loadaddr` text DEFAULT NULL," + "`unloadaddr` text DEFAULT NULL,"
// + "`returnaddr` text DEFAULT NULL," + "`loadweight` Double(10,3) DEFAULT
// NULL,"
// + "`price` Double(10,3) DEFAULT NULL," + "`loaddate` bigint(13) DEFAULT
// NULL,"
// + "`unloaddate` bigint(13) DEFAULT NULL," + "`loadtruckmile` Double(10,3)
// DEFAULT NULL,"
// + "`unloadtruckmile` Double(10,3) DEFAULT NULL," + "`totalmile` Double(10,3)
// DEFAULT NULL,"
// + "`addfuelvol` Double(10,3) DEFAULT NULL," + "`addfuelmoney` Double(10,3)
// DEFAULT NULL,"
// + "`loadtruckroadtoll` Double(10,3) DEFAULT NULL,"
// + "`unloadtruckroadtoll` Double(10,3) DEFAULT NULL," + "`date` bigint(13)
// DEFAULT NULL,"
// + "`data` text DEFAULT NULL," + "`createdat` bigint(13) DEFAULT NULL,"
// + "`createdid` bigint(10) DEFAULT NULL," + "`updatedat` bigint(13) DEFAULT
// NULL,"
// + "`updatedid` bigint(10) DEFAULT NULL," + "PRIMARY KEY (`sid`)"
// + ") ENGINE=InnoDB DEFAULT CHARSET=utf8;";
// statement.execute(sql);
// sql = "ALTER TABLE `trafficexp` MODIFY COLUMN `sid` bigint(10) UNSIGNED
// AUTO_INCREMENT";
// statement.execute(sql);
// }
//
// if (!doesTableExist(connection, "transportion")) {
// System.out.println("Creating Table transportion");
// sql = "CREATE TABLE `transportion` (" + "`sid` bigint(10) NOT NULL
// AUTO_INCREMENT,"
// + "`transportionstatus` int(4) DEFAULT NULL," + "`trucklogsid` bigint(10)
// DEFAULT NULL,"
// + "`receivetime` bigint(13) DEFAULT NULL," + "`loadtime` bigint(13) DEFAULT
// NULL,"
// + "`loadweight` double(10,3) DEFAULT NULL," + "`unloadtime` bigint(13)
// DEFAULT NULL,"
// + "`unloadweight` double(10,3) DEFAULT NULL,"
// + "`createdat` bigint(13) DEFAULT NULL," + "`createdid` bigint(10) DEFAULT
// NULL,"
// + "`updatedat` bigint(13) DEFAULT NULL," + "`updatedid` bigint(10) DEFAULT
// NULL,"
// + "PRIMARY KEY (`sid`)" + ") ENGINE=InnoDB DEFAULT CHARSET=utf8;";
// statement.execute(sql);
// sql = "ALTER TABLE `transportion` MODIFY COLUMN `sid` bigint(10) UNSIGNED
// AUTO_INCREMENT";
// statement.execute(sql);
// }

// private static Connection conn = null;

// public static boolean openConn() {
// try {
// Class.forName(driverclass);
// } catch (Exception e) {
// return false;
// }
// try {
// conn = DriverManager.getConnection(dburl, user, password);
// } catch (SQLException e) {
// System.out.println("Creating Database scsywh");
// Connection connection = null;
// Statement statement = null;
// try {
// connection = DriverManager.getConnection(mysqlurl, user, password);
// String sql = "CREATE DATABASE scsywh";
// statement = connection.createStatement();
// statement.execute(sql);
// } catch (SQLException e2) {
// System.err.println("Exception: " + e2.getMessage());
// return false;
// } finally {
// ReleseDB(connection, statement, null);
// }
// try {
// conn = DriverManager.getConnection(dburl, user, password);
// } catch (Exception e2) {
// return false;
// }
// }
// return true;
// }

// public static void closeConn() {
// ReleseDB(conn, null, null);
// }
//
// public static boolean createTable(String tablename, String sqlcreate) {
// Statement statement = null;
// if (conn == null)
// return false;
// try {
// Class.forName(driverclass);
// statement = conn.createStatement();
// if (!doesTableExist(conn, tablename)) {
// System.out.println("Creating Table " + tablename);
// statement.execute(sqlcreate);
// String sql = "ALTER TABLE `" + tablename + "` MODIFY COLUMN `sid` bigint(10)
// UNSIGNED AUTO_INCREMENT";
// statement.execute(sql);
// }
// ReleseDB(null, statement, null);
// return true;
// } catch (Exception e) {
// System.err.println("Exception: " + e.getMessage());
// ReleseDB(null, statement, null);
// return false;
// }
// }
//
// public static void UpdateDB(String sql, Object... args) {
// if (conn == null)
// return;
// PreparedStatement preparedStatement = null;
// int result;
// try {
// preparedStatement = conn.prepareStatement(sql);
// for (int i = 0; i < args.length; i++) {
// preparedStatement.setObject(i + 1, args[i]);
// }
// result = preparedStatement.executeUpdate();
// System.out.println("effect " + result + " lines");
// } catch (SQLException e) {
// e.printStackTrace();
// } finally {
// ReleseDB(null, preparedStatement, null);
// }
//
// }
//
// public static <T> List<T> QueryDB(Class<T> clazz, String sql, Object... args)
// {
//
// List<T> ret = new ArrayList<T>();
// PreparedStatement preparedStatement = null;
// ResultSet resultSet = null;
// ResultSetMetaData resultSetMetaData = null;
//
// if (conn == null)
// return null;
// try {
// preparedStatement = conn.prepareStatement(sql);
// for (int i = 0; i < args.length; i++) {
// preparedStatement.setObject(i + 1, args[i]);
// }
// resultSet = preparedStatement.executeQuery();
// resultSetMetaData = resultSet.getMetaData();
// while (resultSet.next()) {
// T entity = clazz.newInstance();
// for (int i = 0; i < resultSetMetaData.getColumnCount(); i++) {
// String columnLabel = resultSetMetaData.getColumnLabel(i + 1);
// Object columnValue = resultSet.getObject(columnLabel);
// setFieldValue(entity, columnLabel, columnValue);
// }
// ret.add(entity);
// }
// return ret;
// } catch (Exception e) {
// System.err.println("In QueryDB: " + e.getMessage());
// return null;
// } finally {
// ReleseDB(null, preparedStatement, resultSet);
// }
// }
//
// public static <T> List<T> QueryDB(Class<T> clazz, String sql, List<Object>
// preparams) {
//
// List<T> ret = new ArrayList<T>();
// PreparedStatement preparedStatement = null;
// ResultSet resultSet = null;
// ResultSetMetaData resultSetMetaData = null;
//
// if (conn == null)
// return null;
// try {
// preparedStatement = conn.prepareStatement(sql);
// for (int i = 0; i < preparams.size(); i++) {
// preparedStatement.setObject(i + 1, preparams.get(i));
// }
// resultSet = preparedStatement.executeQuery();
// resultSetMetaData = resultSet.getMetaData();
// while (resultSet.next()) {
// T entity = clazz.newInstance();
// for (int i = 0; i < resultSetMetaData.getColumnCount(); i++) {
// String columnLabel = resultSetMetaData.getColumnLabel(i + 1);
// Object columnValue = resultSet.getObject(columnLabel);
// setFieldValue(entity, columnLabel, columnValue);
// }
// ret.add(entity);
// }
// return ret;
// } catch (Exception e) {
// System.err.println("In QueryDB: " + e.getMessage());
// return null;
// } finally {
// ReleseDB(null, preparedStatement, resultSet);
// }
// }
//
// public static long QueryCount(String sql, List<Object> preparams) {
//
// PreparedStatement preparedStatement = null;
// ResultSet resultSet = null;
// ResultSetMetaData resultSetMetaData = null;
//
// if (conn == null)
// return -1;
// try {
// preparedStatement = conn.prepareStatement(sql);
// for (int i = 0; i < preparams.size(); i++) {
// preparedStatement.setObject(i + 1, preparams.get(i));
// }
// resultSet = preparedStatement.executeQuery();
// resultSetMetaData = resultSet.getMetaData();
// while (resultSet.next()) {
// for (int i = 0; i < resultSetMetaData.getColumnCount(); i++) {
// String columnLabel = resultSetMetaData.getColumnLabel(i + 1);
// Object columnValue = resultSet.getObject(columnLabel);
// if(columnLabel.equals("COUNT(*)"))
// return (long) columnValue;
// }
// }
// return 0;
// } catch (Exception e) {
// System.err.println("In QueryCount: " + e.getMessage());
// return -1;
// } finally {
// ReleseDB(null, preparedStatement, resultSet);
// }
// }
//
// public static void setFieldValue(Object object, String fieldName, Object
// fieldValue) {
// Class<?> obj = object.getClass();
// Field[] fields = obj.getDeclaredFields();
// for (int i = 0; i < fields.length; i++) {
// fields[i].setAccessible(true);
// String fieldname = fields[i].getName();
// String[] tmp = fieldname.split("_");
// fieldname = tmp[0];
// if (fieldname.equals(fieldName)) {
// if(fieldName.equals("sid") && fieldValue instanceof BigInteger){
// BigInteger bigInteger = (BigInteger) fieldValue;
// Long longvalue = bigInteger.longValue();
// try {
// fields[i].set(object, longvalue);
// break;
// } catch (Exception e) {
// System.err.println("In setFieldValue: " + e.getMessage());
// }
// }
// else {
// try {
// fields[i].set(object, fieldValue);
// break;
// } catch (Exception e) {
// System.err.println("In setFieldValue: " + e.getMessage());
// }
// }
// }
// }
// }















//public static String getConditionStrForNativeSQL(Map<String, String> map, GetKeyTypeCallback getKeyTypeCallback,
//List<Object> preparams, String tablename) {
//if (map.isEmpty()) {
//return null;
//} else {
//if (preparams == null)
//	return null;
//preparams.clear();
//
//StringBuilder sb = new StringBuilder();
//boolean flag = false;
//Iterator<Entry<String, String>> iterator = map.entrySet().iterator();
//while (iterator.hasNext()) {
//	Entry<String, String> entry = iterator.next();
//	String key = entry.getKey();
//	String value = entry.getValue();
//	int keytype = getKeyTypeCallback.getKeyType(key);
//	if (keytype == 1) // 跳过的字段
//		continue;
//	if (value != null) {
//		if (keytype == 2) { // 数字字段
//			JSONObject jsonObject = null;
//			try {
//				jsonObject = new JSONObject(value);
//			} catch (Exception e) {
//				jsonObject = null;
//			}
//			if (jsonObject == null) { // 数字相等
//				if (flag) {
//					sb.append("and " + tablename + ".`" + key + "` = ? ");
//					preparams.add(Double.parseDouble(value));
//				} else {
//					sb.append(" " + tablename + ".`" + key + "` = ? ");
//					preparams.add(Double.parseDouble(value));
//				}
//				flag = true;
//			} else { // 数字范围
//				String min, max;
//				try {
//					min = jsonObject.getString("min");
//				} catch (Exception e) {
//					min = "0";
//				}
//				try {
//					max = jsonObject.getString("max");
//				} catch (Exception e) {
//					max = String.valueOf(Utils.getCurrenttimeMills() + 10000);
//				}
//				if (flag) {
//					sb.append("and " + tablename + ".`" + key + "` >= ? and " + tablename + ".`" + key
//							+ "` <= ? ");
//					preparams.add(Double.parseDouble(min));
//					preparams.add(Double.parseDouble(max));
//				} else {
//					sb.append(" " + tablename + ".`" + key + "` >= ? and " + tablename + ".`" + key
//							+ "` <= ? ");
//					preparams.add(Double.parseDouble(min));
//					preparams.add(Double.parseDouble(max));
//				}
//				flag = true;
//			}
//		} else if (keytype == 3) { // 字符串相等字段
//			if (flag) {
//				sb.append("and " + tablename + ".`" + key + "` = ? ");
//				preparams.add(value);
//			} else {
//				sb.append(" " + tablename + ".`" + key + "` = ? ");
//				preparams.add(value);
//			}
//			flag = true;
//		}
//	}
//}
//if (!flag)
//	return null;
//else
//	return sb.toString();
//}
//}
//
//public static String getConditionStrForNativeSQL(Map<String, String> map, GetKeyTypeCallback getKeyTypeCallback,
//List<Object> preparams) {
//if (map.isEmpty()) {
//return null;
//} else {
//if (preparams == null)
//	return null;
//preparams.clear();
//
//StringBuilder sb = new StringBuilder();
//boolean flag = false;
//Iterator<Entry<String, String>> iterator = map.entrySet().iterator();
//while (iterator.hasNext()) {
//	Entry<String, String> entry = iterator.next();
//	String key = entry.getKey();
//	String value = entry.getValue();
//	int keytype = getKeyTypeCallback.getKeyType(key);
//	if (keytype == 1) // 跳过的字段
//		continue;
//	if (value != null) {
//		if (keytype == 2) { // 数字字段
//			JSONObject jsonObject = null;
//			try {
//				jsonObject = new JSONObject(value);
//			} catch (Exception e) {
//				jsonObject = null;
//			}
//			if (jsonObject == null) { // 数字相等
//				if (flag) {
//					sb.append("and " + key + " = ? ");
//					preparams.add(Double.parseDouble(value));
//				} else {
//					sb.append(" " + key + " = ? ");
//					preparams.add(Double.parseDouble(value));
//				}
//				flag = true;
//			} else { // 数字范围
//				String min, max;
//				try {
//					min = jsonObject.getString("min");
//				} catch (Exception e) {
//					min = "0";
//				}
//				try {
//					max = jsonObject.getString("max");
//				} catch (Exception e) {
//					max = String.valueOf(Utils.getCurrenttimeMills() + 10000);
//				}
//				if (flag) {
//					sb.append("and " + key + " >= ? and " + key + " <= ? ");
//					preparams.add(Double.parseDouble(min));
//					preparams.add(Double.parseDouble(max));
//				} else {
//					sb.append(" " + key + " >= ? and " + key + " <= ? ");
//					preparams.add(Double.parseDouble(min));
//					preparams.add(Double.parseDouble(max));
//				}
//				flag = true;
//			}
//		} else if (keytype == 3) { // 字符串相等字段
//			if (flag) {
//				sb.append("and " + key + " = ? ");
//				preparams.add(value);
//			} else {
//				sb.append(" " + key + " = ? ");
//				preparams.add(value);
//			}
//			flag = true;
//		}
//	}
//}
//if (!flag)
//	return null;
//else
//	return sb.toString();
//}
//}
