package com.ji.shoppingreminder;

import android.os.Bundle;

import com.stephentuso.welcome.BasicPage;
import com.stephentuso.welcome.TitlePage;
import com.stephentuso.welcome.WelcomeActivity;
import com.stephentuso.welcome.WelcomeConfiguration;

public class TutorialActivity extends WelcomeActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    protected WelcomeConfiguration configuration() {
        return new WelcomeConfiguration.Builder(this)
                .defaultBackgroundColor(R.color.colorPrimary)
                .page(new BasicPage(R.drawable.ic_check_box_white_24dp,
                        getString(R.string.tutorial_title_1),
                        getString(R.string.tutorial_description_1))
                )
                .page(new BasicPage(R.drawable.ic_toggle_on_white_24dp,
                        getString(R.string.tutorial_title_2),
                        getString(R.string.tutorial_description_2))
                )
                .page(new BasicPage(R.drawable.ic_view_carousel_white_24dp,
                        getString(R.string.tutorial_title_3),
                        getString(R.string.tutorial_description_3))
                )
                .swipeToDismiss(true)
                .build();
    }

}
