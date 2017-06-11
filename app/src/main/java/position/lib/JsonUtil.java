package position.lib;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.json.JSONArray;
import org.json.JSONObject;


public class JsonUtil {
	/** 
     * Json 转成 Map<> 
     * @param jsonStr 
     * @return 
     */  
    public static Map<String, Object> getMapForJson(String jsonStr){  
        JSONObject jsonObject ;  
        try {  
            jsonObject = new JSONObject(jsonStr);  
              
            Iterator<String> keyIter= jsonObject.keys();  
            String key;  
            Object value ;  
            Map<String, Object> valueMap = new HashMap<String, Object>();  
            while (keyIter.hasNext()) {  
                key = keyIter.next();  
                value = jsonObject.get(key);  
                valueMap.put(key, value);  
            }  
            return valueMap;  
        } catch (Exception e) {  
            // TODO: handle exception  
            e.printStackTrace();  
        }  
        return null;  
    }  
    
    /** 
     * Json 转成 List<Map<>> 
     * @param jsonStr 
     * @return 
     */  
    public static List<Map<String, Object>> getlistForJson(String jsonStr){  
        List<Map<String, Object>> list = null;  
        try {  
            JSONArray jsonArray = new JSONArray(jsonStr);  
            JSONObject jsonObj ;  
            list = new ArrayList<Map<String,Object>>();  
            for(int i = 0 ; i < jsonArray.length() ; i ++){  
                jsonObj = (JSONObject)jsonArray.get(i);  
                list.add(getMapForJson(jsonObj.toString()));  
            }  
        } catch (Exception e) {  
            // TODO: handle exception  
            e.printStackTrace();  
        }  
        return list;  
    }  
}
