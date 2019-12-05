package com.ji.shoppingreminder.ui.main;

import androidx.recyclerview.widget.RecyclerView;
import androidx.annotation.NonNull;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.TextView;

import com.ji.shoppingreminder.R;

import java.util.List;

public class ViewAdapter extends RecyclerView.Adapter<ViewAdapter.ViewHolder>  {
    private List<String> items;
    private List<Integer> notificationList;
    private ListViewManager listViewManager;

    //親フラグメントのメソッドを呼び出すためのインターフェース
    public interface ListViewManager{
        void searchItem(String item);
        void deleteItem(String item);
    }

    public ViewAdapter(List<String> data,List<Integer> notificationList, ListViewManager listviewManager) {
        items = data;
        this.notificationList = notificationList;
        this.listViewManager = listviewManager;
    }

    @Override
    @NonNull
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        //新しいviewの生成
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.list_item, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        //データを表示する
        String item = items.get(position);
        holder.itemText.setText(item);
        holder.checkBox.setOnCheckedChangeListener(null);
        //0/1をfalse/trueに変換
        holder.checkBox.setChecked((notificationList.get(position) == 1));
        holder.checkBox.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                listViewManager.searchItem(item);
            }
        });

        holder.view.setOnLongClickListener(new View.OnLongClickListener(){
            @Override
            public boolean onLongClick(View view) {
                listViewManager.deleteItem(item);
                return false;
            }
        });
    }

    //表示するアイテムの数を返す
    @Override
    public int getItemCount() {
        return items.size();
    }

    //ViewHolderクラス
    //Adapterがインフレートした1行分のレイアウトからViewの参照を取得
    static class ViewHolder extends RecyclerView.ViewHolder {
        TextView itemText;
        CheckBox checkBox;
        View view;

        ViewHolder(View v) {
            super(v);
            itemText = (TextView) v.findViewById(R.id.item_text);
            checkBox = (CheckBox) v.findViewById(R.id.check_box);
            view = v;
        }
    }
}
