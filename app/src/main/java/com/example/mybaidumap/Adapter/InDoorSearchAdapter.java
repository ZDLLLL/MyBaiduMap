package com.example.mybaidumap.Adapter;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.baidu.mapapi.search.poi.PoiIndoorInfo;

import com.example.mybaidumap.IndoorSearchActivity;
import com.example.mybaidumap.R;

import java.util.List;


public class InDoorSearchAdapter extends RecyclerView.Adapter<InDoorSearchAdapter.InDoorSearchViewHolder> {
    List<PoiIndoorInfo> poiIndoorInfoList;
    IndoorSearchActivity indoorSearchActivity;
    private Context mContext;

    public InDoorSearchAdapter(List<PoiIndoorInfo> poiIndoorInfoList, IndoorSearchActivity indoorSearchActivity, Context mContext) {
        this.poiIndoorInfoList = poiIndoorInfoList;
        this.indoorSearchActivity = indoorSearchActivity;
        this.mContext = mContext;
    }

    @NonNull
    @Override
    public InDoorSearchAdapter.InDoorSearchViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i) {
        View view = LayoutInflater.from(mContext).inflate(R.layout.indoorsearch_item,viewGroup,false);
        return new InDoorSearchAdapter.InDoorSearchViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull InDoorSearchAdapter.InDoorSearchViewHolder inDoorSearchViewHolder, final int i) {
        inDoorSearchViewHolder.indoorsearch_item_name_tv.setText(poiIndoorInfoList.get(i).name);
        inDoorSearchViewHolder.indoorsearch_item_address_tv.setText(poiIndoorInfoList.get(i).address);
    }

    @Override
    public int getItemCount() {
        return poiIndoorInfoList.size();
    }

    public class InDoorSearchViewHolder extends RecyclerView.ViewHolder {
        private TextView indoorsearch_item_name_tv;
        private TextView indoorsearch_item_address_tv;
        public InDoorSearchViewHolder(@NonNull View itemView) {
            super(itemView);
            indoorsearch_item_name_tv=itemView.findViewById(R.id.indoorsearch_item_name_tv);
            indoorsearch_item_address_tv=itemView.findViewById(R.id.indoorsearch_item_address_tv);
        }
    }
}
