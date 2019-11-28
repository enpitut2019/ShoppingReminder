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

    public ViewAdapter(List<String> data) {
        items = data;
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
        holder.checkBox.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                //set your object's last status
                Log.d("test", item);
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

        ViewHolder(View v) {
            super(v);
            itemText = (TextView) v.findViewById(R.id.item_text);
            checkBox = (CheckBox) v.findViewById(R.id.check_box);
        }
    }
}
