package position.thread;

import java.util.Map;

import position.entity.Position;
import position.lib.HttpApi;
import position.lib.JsonUtil;

import android.os.Bundle;
import android.os.Handler;
import android.os.Message;


public class register implements Runnable {
	Position position;
	private Handler handler=null;

	public register(Position ps,Handler hand) {
		position = ps;
		handler = hand;
	}
	
	@Override  
    public void run() {  
		Message msg = new Message();  
        
		String string = HttpApi.getApi("http://106.14.254.252:83/index.php?c=mymap&a=register"+
		"&m_szAndroidID="+position.getM_szAndroidID()+
		"&name="+position.getName()
				);
		Map<String, Object> map = JsonUtil.getMapForJson(string);
		
		msg.obj = map;
		handler.sendMessage(msg);
    } 
}
