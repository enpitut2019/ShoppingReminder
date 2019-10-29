package com.ji.shoppingreminder;

import android.app.Activity;
import android.view.*;
import android.widget.*;

public class CategoryManager extends Activity {
    public void initSpinners() {
        Spinner categorySpinner = findViewById(R.id.categorySpinner);
        String[] labels = getResources().getStringArray(R.array.category);
        ArrayAdapter<String> adapter = new ArrayAdapter(this, android.R.layout.simple_spinner_item, labels);
        categorySpinner.setAdapter(adapter);

        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
    }

    public void initButton(){
        Button btn = (Button)findViewById(R.id.categoryDecideButton);

        View.OnClickListener clicked = new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Spinner spinner = (Spinner)findViewById(R.id.categorySpinner);
                TextView textView = (TextView)findViewById(R.id.textView);

                String str = spinner.getSelectedItem().toString();
                textView.setText(str);
            }
        };

        btn.setOnClickListener(clicked);
    }
}
