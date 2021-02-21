package com.william.analyzers;

import com.william.utils.LogUtils;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import com.william.utils.ConfigUtils;
import com.william.utils.DownLoadFile;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class UsChinaAnalyzer {

    private String key = "";

    private String url = "";

    private long sleepTime = 3000;


    public void start(String key, String _params) throws Exception {
        String[] params = _params.split("@@@");
        this.key = key;
        this.url = params[0];
        this.sleepTime = Long.parseLong(params[1]);
        _MainThread _mainThread = new _MainThread();
        new Thread(_mainThread).start();
    }

    private class _MainThread implements Runnable {

        @Override
        public void run() {
            // TODO Auto-generated method stub
            while (true) {
                try {
                    _UsChinaThread _thread = new _UsChinaThread();
                    _thread.run();
                    Thread.sleep(sleepTime);
                } catch (Exception e) {
                    // TODO Auto-generated catch block
                    e.printStackTrace();
                }
            }
        }

    }

    private class _UsChinaThread implements Runnable {

        @Override
        public void run() {
            LogUtils.log(key, "grab connect", url);
            // TODO Auto-generated method stub
            try {
                CloseableHttpClient httpClient = HttpClients.custom().build();
                CloseableHttpResponse response = httpClient.execute(new HttpGet(url));

                Document document = Jsoup.parse(EntityUtils.toString(response.getEntity()));
                Elements els = document.getElementsByClass("views-group clearfix group-1 first last");
                Element el = els.first();

                LogUtils.log(key, "grab start");
                if(el == null) {
                    LogUtils.log(key, "grab fail, empty elements");
                }
                else if(el.children() == null || el.children().size()==0){
                    LogUtils.log(key, "grab fail, empty children");
                }
                else{
                    boolean isExit = false;
                    for (int i = 0; i < el.children().size(); i++) {
                        Elements aEls = el.child(i).getElementsByTag("a");
                        for (Element _el : aEls) {
                            String _url = _el.attr("href");

                            if (_url.equals(ConfigUtils.getConfig(ConfigUtils.LOCALSTORAGE_PATH, key))) {
                                LogUtils.log(key, "stop at", _url);
                                isExit = true;
                                break;
                            }
                            if (_url.startsWith("https://")) {
                                //以下载链接的资源名称作为文件名，额外补充下载时间避免重复
                                String fileName = _url.substring(_url.lastIndexOf('/') + 1, _url.lastIndexOf('.')) + LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMddhhmmss"));

                                DownLoadFile.getFile(httpClient, _url, key, fileName + ".pdf");
                                ConfigUtils.setProperty(ConfigUtils.LOCALSTORAGE_PATH, key, _url);

                                LogUtils.log(key, "download", _url, "as", key + "/" + fileName + ".pdf");

                                continue;
                            }

                            LogUtils.log(key, "unhandle", _url);
                        }
                        if (isExit) {
                            break;
                        }
                    }
                }
                LogUtils.log(key, "grab end");
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

    }

}
