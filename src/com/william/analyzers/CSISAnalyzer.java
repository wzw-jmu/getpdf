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

public class CSISAnalyzer {

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
                Elements titles = document.getElementsByClass("teaser__title");
                if(titles == null || titles.size()==0){
                    LogUtils.log(key,"no title found");
                }
                else {
                    for (Element title : titles) {
                        Elements aEls = title.getElementsByTag("a");
                        if (aEls == null || aEls.size() == 0) {
                            LogUtils.log(key, "no <a> tag is found in title");
                            continue;
                        }
                        Element target = aEls.first();
                        String targetUrl = target.attr("href");

                        CloseableHttpClient targetHttpClient = HttpClients.custom().build();
                        CloseableHttpResponse targetResponse = targetHttpClient.execute(new HttpGet("https://www.csis.org/"+targetUrl));

                        Document targetDocument = Jsoup.parse(EntityUtils.toString(targetResponse.getEntity()));
                        Elements targetMains = targetDocument.getElementsByClass("layout-detail-page__main");
                        if (targetMains == null || targetMains.size() == 0) {
                            LogUtils.log(key, "no article found in target");
                            continue;
                        }

                        Element targetMain=targetMains.first();
                        Elements targetAEls = targetMain.getElementsByTag("a");
                        if (targetAEls == null || targetAEls.size() == 0) {
                            LogUtils.log(key, "no <a> tag is found in target main");
                            continue;
                        }
                        for(Element aEl: targetAEls){
                            if(aEl.className().equals("button button--primary")){
                                String _url = aEl.attr("href");

                                if (_url.equals(ConfigUtils.getConfig(ConfigUtils.LOCALSTORAGE_PATH, key))) {
                                    LogUtils.log(key, "stop at", _url);
                                    break;
                                }
                                if (_url.startsWith("https://")) {
                                    //以下载链接的资源名称作为文件名，额外补充下载时间避免重复
//                                    System.out.println(_url);
                                    String fileName = _url.substring(_url.lastIndexOf('/') + 1, _url.lastIndexOf('.')) + LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMddhhmmss"));

                                    DownLoadFile.getFile(httpClient, _url, key, fileName + ".pdf");
                                    ConfigUtils.setProperty(ConfigUtils.LOCALSTORAGE_PATH, key, _url);

                                    LogUtils.log(key, "download", _url, "as", key + "/" + fileName + ".pdf");

                                    continue;
                                }

                                LogUtils.log(key, "unhandle", _url);
                            }
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
