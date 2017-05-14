package it.com.dragmenu.util;


import it.com.dragmenu.remote.RetrofitClient;
import it.com.dragmenu.remote.RxService;
import it.com.dragmenu.remote.SOService;

public class ApiUtils {
    public static final String BASE_URL = "https://api.stackexchange.com/2.2/";
    public static SOService getSOService() {
        return RetrofitClient.getClient(BASE_URL).create(SOService.class);
    }
    public static RxService getRxJavaSOService() {
        return RetrofitClient.getRxJavaClient(BASE_URL).create(RxService.class);
    }
}