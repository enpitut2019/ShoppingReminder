package com.ji.shoppingreminder.ui.main;

import android.content.ContentValues;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProviders;

import com.ji.shoppingreminder.R;

import org.w3c.dom.Text;

/**
 * A placeholder fragment containing a simple view.
 */
public class PlaceholderFragment extends Fragment {

    private static final String ARG_SECTION_NUMBER = "section_number";

    private PageViewModel pageViewModel;

    private DBmanager dBmanager;

    private TextView textView;

    public interface DBmanager {
        void insertToDB(int index, String item);
        void displayDBContents(TextView textView, int index);
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
//        final TextView textView = root.findViewById(R.id.section_label);
        pageViewModel.getText().observe(this, new Observer<String>() {
            @Override
            public void onChanged(@Nullable String s) {
//                textView.setText(s);
            }  });

        dBmanager = (DBmanager) getActivity();

        EditText editText = root.findViewById(R.id.edit_text);
        TextView textView = root.findViewById(R.id.text_view);

        Button categoryDecideButton = root.findViewById(R.id.categoryDecideButton);

        //DBmanager.displayDBContents(textView, getArguments().getInt(ARG_SECTION_NUMBER) - 1);
        categoryDecideButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String item = editText.getText().toString();
                dBmanager.insertToDB(getArguments().getInt(ARG_SECTION_NUMBER) - 1, item);
            }
        });

        return root;
    }

    public void callFromOut(){
        Log.d("PlaceholderFragment", "callFromOut this method");
    }
}