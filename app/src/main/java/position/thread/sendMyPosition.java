package position.thread;

import java.util.Map;

import position.entity.Position;
import position.lib.HttpApi;

import android.R.string;
import android.provider.Settings.Secure;

public class sendMyPosition implements Runnable {
	
	private Position position; 
	
	public sendMyPosition(Position ps) {
		position = ps;
	}
	
	@Override  
    public void run() {  
        HttpApi.getApi("http://106.14.254.252:83/index.php?c=mymap&a=setPosition&m_szAndroidID="+position.getM_szAndroidID()
	        		+"&position_r="+Double.toString(position.getPosition_r())
	        		+"&position_l="+Double.toString(position.getPosition_l())
	        		+"&address="+position.getAddress());
		HttpApi.MyLog("发送自己的位置位置yibu"+Double.toString(position.getPosition_r()));
    } 
}
