package com.william.main;

import java.lang.reflect.Method;
import java.util.Map;
import java.util.Map.Entry;

import com.william.utils.ConfigUtils;

public class MainActivity {

    public static void main(String[] args) throws Exception {
        // TODO Auto-generated method stub

        Map<String, String> configs = ConfigUtils.getAllConfig(ConfigUtils.CONFIG_PATH);

        for (Entry<String, String> obj : configs.entrySet()) {
            String key = obj.getKey();
            String value = obj.getValue();
            Class<?> _class = Class.forName("com.william.analyzers." + key + "Analyzer");
            Method method = _class.getMethod("start", String.class, String.class);
            Object obj1 = method.invoke(_class.newInstance(), key, value);
        }


//		String fileUrl = "https://www.uschina.org/sites/default/files/the_us-china_economic_relationship_-_a_crucial_partnership_at_a_critical_juncture.pdf";
//		response = httpClient.execute(new HttpGet(fileUrl));
//		InputStream is = response.getEntity().getContent();
//		Files.copy(is, new File(System.getProperty( "user.dir" )+"/test.pdf").toPath(), StandardCopyOption.REPLACE_EXISTING);
    }

}
