package com.william.utils;

import java.io.*;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

public class ConfigUtils {

	public static final String CONFIG_PATH = System.getProperty( "user.dir" )+"/"+"/config.properties";

	public static final String LOCALSTORAGE_PATH = System.getProperty( "user.dir" )+"/"+"/localstorage.properties";

	public static String getConfig(String filePath, String key) throws Exception {
		Properties props = new Properties();
		// 使用InPutStream流读取properties文件
		BufferedReader bufferedReader = new BufferedReader(new FileReader(filePath));
		String value = "";
		try {
			props.load(bufferedReader);
			// 获取key对应的value值
			value = props.getProperty(key);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			throw e;
		}finally {
			if(bufferedReader!=null) {
				bufferedReader.close();
			}
		}
		return value;
	}


	public static Map<String,String> getAllConfig(String filePath) throws Exception{
		Properties props = new Properties();
		// 使用InPutStream流读取properties文件
		BufferedReader bufferedReader = new BufferedReader(new FileReader(filePath));
		Map<String,String> result = new HashMap();
		try {
			props.load(bufferedReader);
			Enumeration en = props.propertyNames();
			System.out.println("------------config-------------");
			System.out.println("config: ");
			while (en.hasMoreElements())
			{
				String key = (String) en.nextElement();
				String value = props.getProperty(key);
				System.out.println(key + " : " + value);
				result.put(key, value);
			}
			System.out.println("------------config-------------");
			System.out.println();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			throw e;
		}finally {
			if(bufferedReader!=null) {
				bufferedReader.close();
			}
		}
		return result;
	}

	public static void setProperty(String filePath,String key,String value) throws Exception{
		Properties props = new Properties();
		// 使用InPutStream流读取properties文件
		BufferedReader bufferedReader = new BufferedReader(new FileReader(filePath));
		OutputStream fos = null;
		try {
			props.load(bufferedReader);
			fos = new FileOutputStream(filePath);
			props.setProperty(key, value);
			props.store(fos, "Update '" + key+ "' value");
		} catch (IOException e) {
			// TODO Auto-generated catch block
			throw e;
		}finally {
			if(bufferedReader!=null) {
				bufferedReader.close();
			}
			if(fos!=null) {
				fos.close();
			}
		}
	}

}
