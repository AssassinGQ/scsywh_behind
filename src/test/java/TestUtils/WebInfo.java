package TestUtils;

public class WebInfo {
    public final static String remotehost = "http://120.76.219.196:85";
    public final static String localhost = "http://127.0.0.1:85";
    public static String getHostaddr(boolean isRemote){
        return isRemote ? remotehost : localhost;
    }
}
