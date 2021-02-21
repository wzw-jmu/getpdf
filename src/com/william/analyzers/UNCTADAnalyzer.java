package com.william.analyzers;

import com.william.utils.ConfigUtils;
import com.william.utils.DownLoadFile;
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

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

/**
 * @program: getPDF
 * @description: 联合国网站分析类
 * @author: 吴子炜(wziiwei @ gdnybank.com)
 * @create: 2021-02-21 23:53
 **/
public class UNCTADAnalyzer {

    private String key = "";

    private String url = "";

    private Long sleepTime = 3000L;

    public void start(String key, String _params) throws Exception {
        String[] params = _params.split("@@@");
        this.key = key;
        this.url = params[0];
        this.sleepTime = Long.parseLong(params[1]);
        UNCTADAnalyzer._MainThread _mainThread = new UNCTADAnalyzer._MainThread();
        new Thread(_mainThread).start();
    }

    private class _MainThread implements Runnable {

        @Override
        public void run() {
            // TODO Auto-generated method stub
            while (true) {
                try {
                    UNCTADThread _thread = new UNCTADThread();
                    _thread.run();
                    Thread.sleep(sleepTime);
                } catch (Exception e) {
                    // TODO Auto-generated catch block
                    e.printStackTrace();
                }
            }
        }

    }

    private class UNCTADThread implements Runnable {

        @Override
        public void run() {
            LogUtils.log(key, "grab connect", url);
            // TODO Auto-generated method stub
            try {
                System.out.println("开始链接到下列网站：" + url);
                CloseableHttpClient httpClient = HttpClients.custom().build();
                CloseableHttpResponse response = httpClient.execute(new HttpGet(url));
                System.out.println("成功链接到网站：" + url);

                Document document = Jsoup.parse(EntityUtils.toString(response.getEntity()));
                Elements els = document.getElementsByClass("view-content");
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

                            //跳转到详情该书籍的详情链接，在里面重复上述操作，直到找到pdf链接\
                            String completeURL = "https://unctad.org/" + _url;
                            System.out.println("跳转至书籍详情页：" + completeURL);
                            CloseableHttpResponse bookResponse = httpClient.execute(new HttpGet(completeURL));
                            Document bookDocument = Jsoup.parse(EntityUtils.toString(bookResponse.getEntity()));
                            Elements bookElements = bookDocument.getElementsByClass("tab-content");

                            if (bookElements == null || bookElements.size() == 0) {
                                LogUtils.log(key, completeURL + "的书籍不支持下载");
                                continue;
                            }

                            Element bookMain = bookElements.first();
                            Elements targetAEls = bookMain.getElementsByTag("a");
                            if (targetAEls == null || targetAEls.size() == 0) {
                                LogUtils.log(key, targetAEls + ": no <a> tag is found in target main");
                                continue;
                            }

                            for (Element aEl : targetAEls) {

                                String dlURL = aEl.attr("href");
                                if (dlURL.equals(ConfigUtils.getConfig(ConfigUtils.LOCALSTORAGE_PATH, key))) {
                                    LogUtils.log(key, "stop at", targetAEls);
                                    isExit = true;
                                    //break;
                                    //continue;
                                }
                                if (dlURL.startsWith("https://")) {
                                    //以下载链接的资源名称作为文件名，额外补充下载时间避免重复
                                    String fileName = dlURL.substring(dlURL.lastIndexOf('/') + 1, dlURL.lastIndexOf('.')) + LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMddhhmmss"));
                                   // String fileName = dlURL + LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMddhhmmss"));

                                    DownLoadFile.getFile(httpClient, dlURL, key, fileName + ".pdf");
                                    ConfigUtils.setProperty(ConfigUtils.LOCALSTORAGE_PATH, key, dlURL);

                                    LogUtils.log(key, "download", dlURL, "as", key + "/" + fileName + ".pdf");

                                    break;
                                }

                                LogUtils.log(key, "unhandle", dlURL);
                            }
                        }
                        if (isExit) {
                            //break;

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
