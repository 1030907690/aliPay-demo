package com.example.demo.controller;

import com.alipay.api.AlipayApiException;
import com.alipay.api.AlipayClient;
import com.alipay.api.DefaultAlipayClient;
import com.alipay.api.domain.AlipayTradeAppPayModel;
import com.alipay.api.request.AlipayTradeAppPayRequest;
import com.alipay.api.response.AlipayTradeAppPayResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import java.io.BufferedReader;
import java.io.CharArrayWriter;
import java.io.InputStreamReader;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Map;

/**
 * @author Zhou Zhong Qing
 * @Title: ${file_name}
 * @Package ${package_name}
 * @Description: ${todo}
 * @date 2019/9/28 0:23
 */
@RestController
@RequestMapping("/pay")
public class PayController {

    private final String APP_ID = "APPID";
    private final String APP_PRIVATE_KEY = "您的私钥";
    private final String CHARSET = "UTF-8";
    private final String ALIPAY_PUBLIC_KEY = "您的公钥";




    //支付成功后的回调地址
    private final String notifyUrl = "http://xxxxx/pay/notify";

    private final String serverUrl = "https://openapi.alipay.com/gateway.do";

    /*
    * 统一下单
    * */
    @RequestMapping("/unifiedOrder")
    public String unifiedOrder() {
        //实例化客户端
        AlipayClient alipayClient = new DefaultAlipayClient(serverUrl, APP_ID, APP_PRIVATE_KEY, "json", CHARSET, ALIPAY_PUBLIC_KEY, "RSA2");
        //实例化具体API对应的request类,类名称和接口名称对应,当前调用接口名称：alipay.trade.app.pay
        AlipayTradeAppPayRequest request = new AlipayTradeAppPayRequest();
        //SDK已经封装掉了公共参数，这里只需要传入业务参数。以下方法为sdk的model入参方式(model和biz_content同时存在的情况下取biz_content)。
        AlipayTradeAppPayModel model = new AlipayTradeAppPayModel();
        model.setBody("购买商品");
        model.setSubject("老板商品");
        String outTradeNo = String.valueOf(System.nanoTime());
        model.setOutTradeNo(outTradeNo);
        model.setTimeoutExpress("30m");
        model.setTotalAmount(String.valueOf(1));
        model.setProductCode("QUICK_MSECURITY_PAY");
        request.setBizModel(model);
        request.setNotifyUrl(notifyUrl);
        try {
            //这里和普通的接口调用不同，使用的是sdkExecute
            AlipayTradeAppPayResponse response = alipayClient.sdkExecute(request);
            //就是orderString 可以直接给客户端请求，无需再做处理。
            System.out.println("支付宝response.getBody() " + response.getBody());
            return response.getBody();
        } catch (AlipayApiException e) {
            System.out.println("支付宝app支付失败 " + e + e.getStackTrace());

        }
        return null;
    }


    /*
    * 支付成功后的回调
    * */
    @RequestMapping(value = "/notify")
    public String callback(HttpServletRequest request) {
        Enumeration enumeration = request.getParameterNames();
        Map<String, String> params = new HashMap<>();
        while (enumeration.hasMoreElements()) {
            String name = (String) enumeration.nextElement();
            params.put(name, request.getParameter(name));
        }
        params.put("body", getBody(request));

        //TODO 这里写收到支付宝回调自己的业务逻辑;收到的参数全在params里

        return "";
    }

    private String getBody(HttpServletRequest request) {
        try {
            BufferedReader in = new BufferedReader(new InputStreamReader(request.getInputStream(), "UTF-8"));
            CharArrayWriter data = new CharArrayWriter();
            char[] buf = new char[8192];
            int ret;
            while ((ret = in.read(buf, 0, 8192)) != -1) {
                data.write(buf, 0, ret);
            }
            return data.toString();
        } catch (Exception e) {
            System.out.println("接收BODY内容失败："+e);
        }
        return null;
    }
}
