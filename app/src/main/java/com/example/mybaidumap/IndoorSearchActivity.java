package com.example.mybaidumap;

import android.content.Context;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.baidu.mapapi.map.BaiduMap;
import com.baidu.mapapi.map.InfoWindow;
import com.baidu.mapapi.map.MapBaseIndoorMapInfo;
import com.baidu.mapapi.map.MapStatus;
import com.baidu.mapapi.map.MapStatusUpdateFactory;
import com.baidu.mapapi.map.MapView;
import com.baidu.mapapi.model.LatLng;
import com.baidu.mapapi.search.core.SearchResult;
import com.baidu.mapapi.search.poi.OnGetPoiSearchResultListener;
import com.baidu.mapapi.search.poi.PoiDetailResult;
import com.baidu.mapapi.search.poi.PoiDetailSearchResult;
import com.baidu.mapapi.search.poi.PoiIndoorInfo;
import com.baidu.mapapi.search.poi.PoiIndoorOption;
import com.baidu.mapapi.search.poi.PoiIndoorResult;
import com.baidu.mapapi.search.poi.PoiResult;
import com.baidu.mapapi.search.poi.PoiSearch;
import com.baidu.mapapi.search.route.BikingRouteResult;
import com.baidu.mapapi.search.route.DrivingRouteResult;
import com.baidu.mapapi.search.route.IndoorPlanNode;
import com.baidu.mapapi.search.route.IndoorRouteLine;
import com.baidu.mapapi.search.route.IndoorRoutePlanOption;
import com.baidu.mapapi.search.route.IndoorRouteResult;
import com.baidu.mapapi.search.route.MassTransitRouteResult;
import com.baidu.mapapi.search.route.OnGetRoutePlanResultListener;
import com.baidu.mapapi.search.route.RoutePlanSearch;
import com.baidu.mapapi.search.route.TransitRouteResult;
import com.baidu.mapapi.search.route.WalkingRouteResult;
import com.example.mybaidumap.Adapter.InDoorSearchAdapter;
import com.example.mybaidumap.indoorview.BaseStripAdapter;
import com.example.mybaidumap.indoorview.StripListView;
import com.example.mybaidumap.overlayutil.IndoorPoiOverlay;
import com.example.mybaidumap.overlayutil.IndoorRouteOverlay;

import java.util.ArrayList;
import java.util.List;

public class IndoorSearchActivity extends AppCompatActivity implements  BaiduMap.OnBaseIndoorMapListener, OnGetPoiSearchResultListener {
    private BaiduMap mBaiduMap;
    private TextView popupText = null; // 泡泡view
    private PoiSearch mPoiSearch = null;
    StripListView stripView;
    BaseStripAdapter mFloorListAdapter;
    private MapView mMapView;
    private EditText indoorsearch_content_et;
    private TextView indoorsearch_search_tv;
    Button indoorsearch_path_bt;
    MapBaseIndoorMapInfo mMapBaseIndoorMapInfo = null;
    static List<PoiIndoorInfo> poiIndoorInfoList=new ArrayList<>();
    RecyclerView indoorsearch_rv;
    InDoorSearchAdapter inDoorSearchAdapter;
    RoutePlanSearch mSearch;
    IndoorRouteLine mIndoorRouteline;
    Button indoorRoutePlane;
    int nodeIndex = -1;
    Button mBtnPre = null; // 上一个节点
    Button mBtnNext = null; // 下一个节点
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_indoor_search);
        mMapView = (MapView) findViewById(R.id.bmapView);
        mBaiduMap = mMapView.getMap();
        LatLng centerpos = new LatLng(39.871281,116.385306); // 北京南站
        MapStatus.Builder builder = new MapStatus.Builder();
        builder.target(centerpos).zoom(19.0f);
        mBaiduMap.animateMapStatus(MapStatusUpdateFactory.newMapStatus(builder.build()));
        indoorsearch_rv=findViewById(R.id.indoorsearch_rv);
        indoorsearch_path_bt=findViewById(R.id.indoorsearch_path_bt);
        //indoorsearch_rv=findViewById(R.id.indoorsearch_rv);
        //   stripView = (StripListView) findViewById(R.id.stripView);
        stripView = new StripListView(this);
        RelativeLayout layout = (RelativeLayout) findViewById(R.id.viewStub);
        layout.addView(stripView);
        mFloorListAdapter = new BaseStripAdapter(this);
        mBaiduMap.setOnBaseIndoorMapListener(this);
        mBaiduMap.setIndoorEnable(true); // 设置打开室内图开关
        mBaiduMap.setMyLocationEnabled(true);
        mPoiSearch = PoiSearch.newInstance();
        mPoiSearch.setOnGetPoiSearchResultListener(this);
        indoorsearch_content_et = (EditText) findViewById(R.id.indoorsearch_content_et);
        indoorsearch_search_tv = findViewById(R.id.indoorsearch_search_tv);
        indoorsearch_search_tv.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                MapBaseIndoorMapInfo indoorInfo = mBaiduMap.getFocusedBaseIndoorMapInfo();
                if (indoorInfo == null) {
                    Toast.makeText(IndoorSearchActivity.this, "当前无室内图，无法搜索" , Toast.LENGTH_LONG).show();
                    return;
                }
                PoiIndoorOption option = new PoiIndoorOption().poiIndoorBid(
                        indoorInfo.getID()).poiIndoorWd(indoorsearch_content_et.getText().toString());
                mPoiSearch.searchPoiIndoor( option);
                //隐藏软键盘
                InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
                imm.toggleSoftInput(0, InputMethodManager.HIDE_NOT_ALWAYS);

            }
        });

        stripView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view,
                                    int position, long id) {
                if (mMapBaseIndoorMapInfo == null) {
                    return;
                }
                String floor = (String) mFloorListAdapter.getItem(position);
                mBaiduMap.switchBaseIndoorMapFloor(floor, mMapBaseIndoorMapInfo.getID());
                mFloorListAdapter.setSelectedPostion(position);
                mFloorListAdapter.notifyDataSetInvalidated();
            }
        });
        //室内路线规划
        mBtnPre = (Button) findViewById(R.id.pre);
        mBtnNext = (Button) findViewById(R.id.next);
        mBtnPre.setVisibility(View.INVISIBLE);
        mBtnNext.setVisibility(View.INVISIBLE);

        indoorRoutePlane = (Button)findViewById(R.id.indoorRoutePlane);
        indoorRoutePlane.setOnClickListener( new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // 发起室内路线规划检索
                IndoorPlanNode startNode = new IndoorPlanNode(new LatLng(39.917380, 116.37978), "F1");
                IndoorPlanNode endNode = new IndoorPlanNode(new LatLng(39.917239, 116.37955), "F6");
                IndoorRoutePlanOption irpo = new IndoorRoutePlanOption().from(startNode).to(endNode);
                mSearch.walkingIndoorSearch(irpo);
            }
        });
        mSearch = RoutePlanSearch.newInstance();
        mSearch.setOnGetRoutePlanResultListener(new OnGetRoutePlanResultListener() {
            @Override
            public void onGetWalkingRouteResult(WalkingRouteResult walkingRouteResult) {

            }

            @Override
            public void onGetTransitRouteResult(TransitRouteResult transitRouteResult) {

            }

            @Override
            public void onGetMassTransitRouteResult(MassTransitRouteResult massTransitRouteResult) {

            }

            @Override
            public void onGetDrivingRouteResult(DrivingRouteResult drivingRouteResult) {

            }

            @Override
            public void onGetIndoorRouteResult(IndoorRouteResult indoorRouteResult) {
                if (indoorRouteResult.error == SearchResult.ERRORNO.NO_ERROR) {
                    IndoorRouteOverlay overlay = new IndoorRouteOverlay(mBaiduMap);
                    mIndoorRouteline = indoorRouteResult.getRouteLines().get(0);
                    nodeIndex = -1;
                    mBtnPre.setVisibility(View.VISIBLE);
                    mBtnNext.setVisibility(View.VISIBLE);
                    overlay.setData(indoorRouteResult.getRouteLines().get(0));
                    overlay.addToMap();
                    overlay.zoomToSpan();
                }
            }

            @Override
            public void onGetBikingRouteResult(BikingRouteResult bikingRouteResult) {

            }
        });
    }
//    设置监听事件来监听进入和移出室内图
    @Override
    public void onBaseIndoorMapMode(boolean b, MapBaseIndoorMapInfo mapBaseIndoorMapInfo) {
        if (b) {
            stripView.setVisibility(View.VISIBLE);
            if (mapBaseIndoorMapInfo == null) {
                return;
            }
            mFloorListAdapter.setmFloorList(mapBaseIndoorMapInfo.getFloors());
            stripView.setStripAdapter(mFloorListAdapter);

        } else {
            stripView.setVisibility(View.GONE);
        }
        mMapBaseIndoorMapInfo = mapBaseIndoorMapInfo;
    }

    @Override
    public void onGetPoiResult(PoiResult poiResult) {

    }

    @Override
    public void onGetPoiDetailResult(PoiDetailResult poiDetailResult) {

    }

    @Override
    public void onGetPoiDetailResult(PoiDetailSearchResult poiDetailSearchResult) {

    }

    @Override
    public void onGetPoiIndoorResult(PoiIndoorResult result) {
        mBaiduMap.clear();
        poiIndoorInfoList.clear();
        if (result == null  || result.error == PoiIndoorResult.ERRORNO.RESULT_NOT_FOUND) {
            Toast.makeText(IndoorSearchActivity.this, "无结果" , Toast.LENGTH_LONG).show();
            return;
        }
        if (result.error == PoiIndoorResult.ERRORNO.NO_ERROR) {
            IndoorPoiOverlay overlay = new MyIndoorPoiOverlay(mBaiduMap);
            poiIndoorInfoList=result.getmArrayPoiInfo();
            indoorsearch_rv.setVisibility(View.VISIBLE);
            indoorsearch_path_bt.setVisibility(View.GONE);
            inDoorSearchAdapter=new InDoorSearchAdapter(poiIndoorInfoList,this,this);
            indoorsearch_rv.setLayoutManager(new LinearLayoutManager(this));
            indoorsearch_rv.setAdapter(inDoorSearchAdapter);
            Log.d("zjccc", poiIndoorInfoList.get(1).address);
            mBaiduMap.setOnMarkerClickListener(overlay);
            //address
            overlay.setData(result);
            overlay.addToMap();
            overlay.zoomToSpan();
        }
    }
    private class MyIndoorPoiOverlay extends IndoorPoiOverlay {

        /**
         * 构造函数
         *
         * @param baiduMap 该 IndoorPoiOverlay 引用的 BaiduMap 对象
         */
        public MyIndoorPoiOverlay(BaiduMap baiduMap) {
            super(baiduMap);
        }

        @Override
        /**
         * 响应点击室内POI点事件
         * @param i
         *            被点击的poi在
         *            {@link com.baidu.mapapi.search.poi.PoiIndoorResult#getmArrayPoiInfo()} } 中的索引
         * @return
         */
        public boolean onPoiClick(int i) {
            PoiIndoorInfo info =  getIndoorPoiResult().getmArrayPoiInfo().get(i);
            Log.v("zjc", String.valueOf(info.latLng.longitude));
            Log.v("zjc", String.valueOf(info.latLng.latitude));
            Toast.makeText(IndoorSearchActivity.this, info.name + ",在" + info.floor + "层", Toast.LENGTH_LONG).show();
            return false;
        }

    }
    public void nodeClick(View v) {
        if (mBaiduMap.isBaseIndoorMapMode()) {
            LatLng nodeLocation = null;
            String nodeTitle = null;
            IndoorRouteLine.IndoorRouteStep step = null;


            if (mIndoorRouteline == null || mIndoorRouteline.getAllStep() == null) {
                return;
            }
            if (nodeIndex == -1 && v.getId() == R.id.pre) {
                return;
            }
            // 设置节点索引
            if (v.getId() == R.id.next) {
                if (nodeIndex < mIndoorRouteline.getAllStep().size() - 1) {
                    nodeIndex++;
                } else {
                    return;
                }
            } else if (v.getId() == R.id.pre) {
                if (nodeIndex > 0) {
                    nodeIndex--;
                } else {
                    return;
                }
            }
            // 获取节结果信息
            step = mIndoorRouteline.getAllStep().get(nodeIndex);
            nodeLocation = step.getEntrace().getLocation();
            nodeTitle = step.getInstructions();

            if (nodeLocation == null || nodeTitle == null) {
                return;
            }

            // 移动节点至中心
            mBaiduMap.setMapStatus(MapStatusUpdateFactory.newLatLng(nodeLocation));
            // show popup
            popupText = new TextView(IndoorSearchActivity.this);
            popupText.setBackgroundResource(R.drawable.popup);
            popupText.setTextColor(0xFF000000);
            popupText.setText(step.getFloorId() + ":" + nodeTitle);
            mBaiduMap.showInfoWindow(new InfoWindow(popupText, nodeLocation, 0));

            // 让楼层对应变化
            mBaiduMap.switchBaseIndoorMapFloor(step.getFloorId(), mMapBaseIndoorMapInfo.getID());
//        mFloorListAdapter.setSelectedPostion();
            mFloorListAdapter.notifyDataSetInvalidated();
        }else{
            Toast.makeText(IndoorSearchActivity.this,"请打开室内图或将室内图移入屏幕内",Toast.LENGTH_SHORT).show();
        }
    }
}
