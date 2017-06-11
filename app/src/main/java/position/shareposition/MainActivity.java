package position.shareposition;


import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.baidu.location.BDLocation;
import com.baidu.location.BDLocationListener;
import com.baidu.location.LocationClient;
import com.baidu.location.LocationClientOption;
import com.baidu.mapapi.map.BaiduMap;
import com.baidu.mapapi.map.BitmapDescriptor;
import com.baidu.mapapi.map.BitmapDescriptorFactory;
import com.baidu.mapapi.map.MapStatus;
import com.baidu.mapapi.map.MapStatusUpdateFactory;
import com.baidu.mapapi.map.MapView;
import com.baidu.mapapi.map.MarkerOptions;
import com.baidu.mapapi.map.MyLocationConfiguration.LocationMode;
import com.baidu.mapapi.map.MyLocationData;
import com.baidu.mapapi.map.OverlayOptions;
import com.baidu.mapapi.model.LatLng;
import position.entity.Bean;
import position.entity.Position;
import position.lib.HttpApi;
import position.other.MapListAdapter;
import position.thread.initData;
import position.thread.sendMyPosition;

import android.Manifest;
import android.app.Activity;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.provider.Settings;
import android.provider.Settings.Secure;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.util.DisplayMetrics;
import android.view.KeyEvent;
import android.view.View;
import android.view.Window;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.Button;
import android.widget.GridView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

public class MainActivity extends Activity {
    //	private TextView tv_option;
    private MapView mMapView;
    private BaiduMap mBaiduMap;
    private List<Bean> mapBeans;
    Button requestLocButton;
    private GridView gridView;
    private String m_szAndroidID;

    //定位相关
    BitmapDescriptor mCurrentMarker;
    private LocationClient mLocClient;
    private LocationMode mCurrentMode;
    private boolean isFirstLoc = true;//是否首次定位
    private MyLocationListenner myListener = new MyLocationListenner();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        // TODO Auto-generated method stub
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mMapView = (MapView) findViewById(R.id.bmapView);
        m_szAndroidID = Secure.getString(getContentResolver(), Secure.ANDROID_ID);

        gridView = (GridView) findViewById(R.id.grid_map_user_list);
        mBaiduMap = mMapView.getMap();
        //开启定位图层
        mBaiduMap.setMyLocationEnabled(true);
        //定位初始化
        mLocClient = new LocationClient(getApplicationContext());
        mLocClient.registerLocationListener(myListener);
        LocationClientOption option = new LocationClientOption();
        option.setOpenGps(true);// 打开GPS
        option.setCoorType("bd09ll");//设置坐标类型
        option.setScanSpan(9000);//设置扫描间隔，单位是毫秒 当<1000(1s)时，定时定位无效
        option.setIsNeedAddress(true);//设置地址信息，默认无地址信息
        mLocClient.setLocOption(option);
        mLocClient.start();
        mBaiduMap.setMapStatus(MapStatusUpdateFactory.newMapStatus(new MapStatus.Builder().zoom(13).build()));

        init_click();//初始化各种点击事件
        click_test();


    }

    public void init_click(){



        OnItemClickListener gridItemClickListener = new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {

                Position posi=(Position)gridView.getItemAtPosition(position);

                //设置中中心点
                LatLng ll = new LatLng(posi.getPosition_l(),posi.getPosition_r());
                MapStatus.Builder builder = new MapStatus.Builder();
                builder.target(ll).zoom(15);
                mBaiduMap.animateMapStatus(MapStatusUpdateFactory.newMapStatus(builder.build()));

                HttpApi.MyLog(posi.getName()+" 点击了定位");

            }
        };
        gridView.setOnItemClickListener(gridItemClickListener);

    }



    /*
     * 获取到数据源更新地图make
     * */
    Handler handler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            List<Position> json_list = (List<Position>) msg.obj;
            initData(json_list); //获取到数据
        }
    };

    /*
     * 添加marker
     * */
    BitmapDescriptor bdGround = BitmapDescriptorFactory.fromResource(R.drawable.ground_overlay);
    private void initData(List<Position>  json_list) {
        mBaiduMap.clear();
        //初始化gridView 及lsit
        List<Map<String,String>> list = new ArrayList<Map<String,String>>();

        //添加地图marker
        for (Position position : json_list) {

            //设置地图上显示的名字view
            LatLng latLng = new LatLng(position.getPosition_l(), position.getPosition_r());
            View view = View.inflate(getApplicationContext(), R.layout.item_bean, null);
            TextView tView = (TextView)view.findViewById(R.id.item_bean);
            tView.setText(position.getName() + "");

            //将View转化为Bitmap
            BitmapDescriptor descriptor = BitmapDescriptorFactory.fromView(view);
            OverlayOptions options = new MarkerOptions().position(latLng).icon(descriptor).draggable(true);
            mBaiduMap.addOverlay(options);

            //设置头部展示哪些人参与了
            Map<String,String> listItem = new HashMap<String,String>();
            listItem.put("name", position.getName());
            list.add(listItem);

            HttpApi.MyLog(position.getName()+","+position.getPosition_l()+","+position.getPosition_r());
        }
        HttpApi.MyLog("kaishi 闲1的");

        //设置哪些人加入定位
        int size = json_list.size();
        int length = 200;
        DisplayMetrics dm = new DisplayMetrics();
        getWindowManager().getDefaultDisplay().getMetrics(dm);
        float density = dm.density;
        int gridviewWidth = (int) (size * (length + 4) * density);
        int itemWidth = (int) (length * density);

        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(gridviewWidth, LinearLayout.LayoutParams.FILL_PARENT);
        gridView.setLayoutParams(params); // 设置GirdView布局参数,横向布局的关键
        gridView.setColumnWidth(itemWidth); // 设置列表项宽
        gridView.setHorizontalSpacing(5); // 设置列表项水平间距
        gridView.setStretchMode(GridView.NO_STRETCH);
        gridView.setNumColumns(size); // 设置列数量=列表集合数
        gridView.setAdapter(new MapListAdapter(this, json_list, gridView));

        HttpApi.MyLog("kaishi 闲的");

//        RecyclerView mRecyclerView = (RecyclerView) findViewById(R.id.recyclerView);
//        LinearLayoutManager layoutManager = new LinearLayoutManager(this);
//         //设置布局管理器
//        mRecyclerView.setLayoutManager(layoutManager);
//         //设置为垂直布局，这也是默认的
//        layoutManager.setOrientation(OrientationHelper.VERTICAL);
//        HttpApi.MyLog("kaishi 闲的");
//         //设置Adapter
//        mRecyclerView.setAdapter( new MapUserListAdapter(MainActivity.this , json_list ));
//         //设置增加或删除条目的动画
//        mRecyclerView.setItemAnimator(new DefaultItemAnimator());


    }

    /*
     * 定位SDK监听函数
     */
    private class MyLocationListenner implements BDLocationListener{
        @Override
        public void onReceiveLocation(BDLocation location) {
            // TODO Auto-generated method stub
            // map view 销毁后不在处理新接收的位置
            if (location == null || mMapView == null) {
                return;
            }
            MyLocationData locData = new MyLocationData.Builder()
                    .accuracy(location.getRadius())
                    // 此处设置开发者获取到的方向信息，顺时针0-360
                    .direction(100).latitude(location.getLatitude())
                    .longitude(location.getLongitude()).build();
            mBaiduMap.setMyLocationData(locData);
            if (isFirstLoc) {
                isFirstLoc = false;
                LatLng ll = new LatLng(location.getLatitude(),location.getLongitude());
//
                MapStatus.Builder builder = new MapStatus.Builder();
                builder.target(ll).zoom(13.0f);
                mBaiduMap.animateMapStatus(MapStatusUpdateFactory.newMapStatus(builder.build()));
            }

            HttpApi.MyLog(location.getLocType()+"详情地点：" + location.getAddrStr() + "   城市：" +location.getCity());

            //发送自己的定位
            Position position = new Position();
            position.setM_szAndroidID(m_szAndroidID);
            position.setPosition_r(location.getLongitude());
            position.setPosition_l(location.getLatitude());
            position.setAddress(location.getAddrStr());
            sendMyPosition sr1 = new sendMyPosition(position);
            new Thread(sr1).start();

            //加载其他人定位
            initData thr1 = new initData(handler);
            new Thread(thr1).start();
        }
    }

    @Override
    protected void onResume() {
        // TODO Auto-generated method stub
        super.onResume();
        mMapView.onResume();
    }
    @Override
    protected void onPause() {
        // TODO Auto-generated method stub
        super.onPause();
        mMapView.onPause();
    }
    @Override
    protected void onDestroy() {
        // TODO Auto-generated method stub
        super.onDestroy();
        mMapView.onDestroy();
        mBaiduMap = null;
        bdGround.recycle();
    }

    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_BACK) {
            Intent intent = new Intent(Intent.ACTION_MAIN);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            intent.addCategory(Intent.CATEGORY_HOME);
            startActivity(intent);
            return true;
        }
        return super.onKeyDown(keyCode, event);
    }

    private static final int MY_PERMISSION_REQUEST_CODE = 10000;

    /**
     * 点击按钮，将通讯录备份保存到外部存储器备。
     *
     * 需要3个权限(都是危险权限):
     *      1. 读取通讯录权限;
     *      2. 读取外部存储器权限;
     *      3. 写入外部存储器权限.
     */
    public void click_test() {
        /**
         * 第 1 步: 检查是否有相应的权限
         */
        boolean isAllGranted = checkPermissionAllGranted(
                new String[] {
                        Manifest.permission.ACCESS_COARSE_LOCATION,
                        Manifest.permission.ACCESS_FINE_LOCATION,
                        Manifest.permission.ACCESS_WIFI_STATE,
                        Manifest.permission.ACCESS_NETWORK_STATE,
                        Manifest.permission.READ_PHONE_STATE,
                        Manifest.permission.WRITE_EXTERNAL_STORAGE,
                        Manifest.permission.INTERNET,
                        Manifest.permission.VIBRATE,
                }
        );
        // 如果这3个权限全都拥有, 则直接执行备份代码
        if (isAllGranted) {
            doBackup();
            return;
        }

        /**
         * 第 2 步: 请求权限
         */
        // 一次请求多个权限, 如果其他有权限是已经授予的将会自动忽略掉
        ActivityCompat.requestPermissions(
                this,
                new String[] {
                        Manifest.permission.ACCESS_COARSE_LOCATION,
                        Manifest.permission.ACCESS_FINE_LOCATION,
                        Manifest.permission.ACCESS_WIFI_STATE,
                        Manifest.permission.ACCESS_NETWORK_STATE,
                        Manifest.permission.READ_PHONE_STATE,
                        Manifest.permission.WRITE_EXTERNAL_STORAGE,
                        Manifest.permission.INTERNET,
                        Manifest.permission.VIBRATE,
                },
                MY_PERMISSION_REQUEST_CODE
        );
    }

    /**
     * 检查是否拥有指定的所有权限
     */
    private boolean checkPermissionAllGranted(String[] permissions) {
        for (String permission : permissions) {
            if (ContextCompat.checkSelfPermission(this, permission) != PackageManager.PERMISSION_GRANTED) {
                // 只要有一个权限没有被授予, 则直接返回 false
                return false;
            }
        }
        return true;
    }

    /**
     * 第 3 步: 申请权限结果返回处理
     */
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        if (requestCode == MY_PERMISSION_REQUEST_CODE) {
            boolean isAllGranted = true;

            // 判断是否所有的权限都已经授予了
            for (int grant : grantResults) {
                if (grant != PackageManager.PERMISSION_GRANTED) {
                    isAllGranted = false;
                    break;
                }
            }

            if (isAllGranted) {
                // 如果所有的权限都授予了, 则执行备份代码
                doBackup();

            } else {
                // 弹出对话框告诉用户需要权限的原因, 并引导用户去应用权限管理中手动打开权限按钮
                openAppDetails();
            }
        }
    }

    /**
     * 第 4 步: 备份通讯录操作
     */
    private void doBackup() {
        // 本文主旨是讲解如果动态申请权限, 具体备份代码不再展示, 就假装备份一下
        Toast.makeText(this, "正在备份通讯录...", Toast.LENGTH_SHORT).show();
    }

    /**
     * 打开 APP 的详情设置
     */
    private void openAppDetails() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage("备份通讯录需要访问 “通讯录” 和 “外部存储器”，请到 “应用信息 -> 权限” 中授予！");
        builder.setPositiveButton("去手动授权", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                Intent intent = new Intent();
                intent.setAction(Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
                intent.addCategory(Intent.CATEGORY_DEFAULT);
                intent.setData(Uri.parse("package:" + getPackageName()));
                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                intent.addFlags(Intent.FLAG_ACTIVITY_NO_HISTORY);
                intent.addFlags(Intent.FLAG_ACTIVITY_EXCLUDE_FROM_RECENTS);
                startActivity(intent);
            }
        });
        builder.setNegativeButton("取消", null);
        builder.show();
    }

}