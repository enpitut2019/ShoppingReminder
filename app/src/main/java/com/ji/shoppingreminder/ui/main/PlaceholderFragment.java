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
import android.widget.TextView;

import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProviders;

import com.ji.shoppingreminder.R;

/**
 * A placeholder fragment containing a simple view.
 */
public class PlaceholderFragment extends Fragment {

    private static final String ARG_SECTION_NUMBER = "section_number";

    private PageViewModel pageViewModel;

    private DBmanager dBmanager;
    private InputMethodManager inputMethodManager;

    public interface DBmanager {
        void insertToDB(int index, String item);
        void displayDBContents(RecyclerView recyclerView, int index);
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

        EditText editText = root.findViewById(R.id.edit_text);
        RecyclerView recyclerView = root.findViewById(R.id.recycler_view);
        Button categoryDecideButton = root.findViewById(R.id.categoryDecideButton);
        //タブに対応するデータベース内のアイテムを表示する
        dBmanager.displayDBContents(recyclerView, getArguments().getInt(ARG_SECTION_NUMBER) - 1);

        //登録ボタンを押したときの処理
        categoryDecideButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                inputMethodManager.hideSoftInputFromWindow(editText.getWindowToken(), InputMethodManager.HIDE_NOT_ALWAYS);
                String item = editText.getText().toString().trim();
                editText.getEditableText().clear();
                if(item.length() != 0){
                    dBmanager.insertToDB(getArguments().getInt(ARG_SECTION_NUMBER) - 1, item);
                    dBmanager.displayDBContents(recyclerView, getArguments().getInt(ARG_SECTION_NUMBER) - 1);
                }
            }
        });

        //キーボード以外をタップしたらキーボードを閉じる
        root.setOnTouchListener(new View.OnTouchListener() {
            public boolean onTouch(View v, MotionEvent event) {
                if(event.getAction() == MotionEvent.ACTION_DOWN){
                    inputMethodManager.hideSoftInputFromWindow(editText.getWindowToken(), InputMethodManager.HIDE_NOT_ALWAYS);
                }
                return true;
            }
        });

        return root;
    }

    public void callFromOut(){
        Log.d("PlaceholderFragment", "callFromOut this method");
    }
}