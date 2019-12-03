package com.ji.shoppingreminder.ui.main;

import android.content.ContentValues;
import android.content.Context;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;

import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProviders;

import com.ji.shoppingreminder.R;

import java.util.ArrayList;
import java.util.List;

/**
 * A placeholder fragment containing a simple view.
 */
public class PlaceholderFragment extends Fragment implements ViewAdapter.ListViewManager {

    private static final String ARG_SECTION_NUMBER = "section_number";

    private PageViewModel pageViewModel;
    private RecyclerView recyclerView;
    private DBmanager dBmanager;
    private InputMethodManager inputMethodManager;

    private List<String> itemList;
    private List<Integer> notificationList;
    public ViewAdapter viewAdapter;

    //MainActivityのメソッドを呼び出すためのインターフェース
    public interface DBmanager {
        void insertToDB(int index, String item);
        List<String> getDBContents(int index);
        Boolean searchItem(String item);
    }

    public static PlaceholderFragment newInstance(int index) {
        PlaceholderFragment fragment = new PlaceholderFragment();
        Bundle bundle = new Bundle();
        bundle.putInt(ARG_SECTION_NUMBER, index);
        fragment.setArguments(bundle);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        pageViewModel = ViewModelProviders.of(this).get(PageViewModel.class);
        int index = 1;
        if (getArguments() != null) {
            index = getArguments().getInt(ARG_SECTION_NUMBER);
        }
        pageViewModel.setIndex(index);
        //listの初期化
        itemList = new ArrayList<>();
        notificationList = new ArrayList<>();
    }

    @Override
    public View onCreateView(
            @NonNull LayoutInflater inflater, ViewGroup container,
            Bundle savedInstanceState) {
        View root = inflater.inflate(R.layout.fragment_main, container, false);
        pageViewModel.getText().observe(this, new Observer<String>() {
            @Override
            public void onChanged(@Nullable String s) {
            }  });

        dBmanager = (DBmanager) getActivity();
        inputMethodManager = (InputMethodManager)getActivity().getSystemService(Context.INPUT_METHOD_SERVICE);

        //タブに対応するデータベース内のアイテムを表示する
        recyclerView = root.findViewById(R.id.recycler_view);
        setList(dBmanager.getDBContents(getArguments().getInt(ARG_SECTION_NUMBER) - 1));
        viewAdapter = new ViewAdapter(itemList, notificationList,  this);
        recyclerView.setLayoutManager(new LinearLayoutManager(root.getContext()));
        recyclerView.setAdapter(viewAdapter);

        //pageview内の何も表示されていないところをタップしたらキーボードを閉じる
        root.setOnTouchListener(new View.OnTouchListener() {
            public boolean onTouch(View v, MotionEvent event) {
                Log.d("test", "tap");
                if(event.getAction() == MotionEvent.ACTION_DOWN){
                    inputMethodManager.hideSoftInputFromWindow(root.getWindowToken(), InputMethodManager.HIDE_NOT_ALWAYS);
                }
                return true;
            }
        });
        return root;
    }

    /**
     * itemListとnotificationListを更新する
     * @param list
     */
    public void setList(List<String> list){
        if(itemList != null){
            itemList.clear();
            notificationList.clear();
        }
        //item, notification のかたちのデータを分割する
        for(String item: list){
            String[] state = item.split(",", 2);
            itemList.add(state[0]);
            notificationList.add(Integer.parseInt(state[1]));
        }
    }

    /**
     * チェックボックスをタップされたアイテムに対する処理
     * @param item
     */
    @Override
    public void searchItem(String item){
        dBmanager.searchItem(item);
        //recyclerviewの更新
        setList(dBmanager.getDBContents(getArguments().getInt(ARG_SECTION_NUMBER) - 1));
        viewAdapter.notifyDataSetChanged();
    }
}