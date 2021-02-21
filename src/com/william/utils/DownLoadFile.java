package com.william.utils;

import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.CloseableHttpClient;

import java.io.File;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;

public class DownLoadFile {

	public static void getFile(CloseableHttpClient httpClient, String url, String filePath, String fileName) throws Exception {
		CloseableHttpResponse response = httpClient.execute(new HttpGet(url));
		InputStream is = response.getEntity().getContent();
		try {
			//判断是否需要创建本网站的资源目录
			File realPath = new File(System.getProperty("user.dir") + File.separator + "download" + File.separator + filePath);
			if (!realPath.isDirectory()) {
				if (!realPath.mkdir()) {
					throw new Exception("mkdir fail");
				}
			}

			//保存资源
			File saveFile = new File(System.getProperty("user.dir") +
					File.separator + "download" +
					File.separator + filePath +
					File.separator + fileName);
			Files.copy(is, saveFile.toPath(), StandardCopyOption.REPLACE_EXISTING);
		} catch (Exception e) {
			throw e;
		} finally {
			if (is != null) {
				is.close();
			}
			if (response != null) {
				response.close();
			}
		}
	}
}
