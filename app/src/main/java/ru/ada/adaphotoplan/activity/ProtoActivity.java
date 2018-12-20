package ru.ada.adaphotoplan.activity;

import android.os.Bundle;
import android.support.annotation.LayoutRes;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.text.SpannableString;
import android.text.Spanned;
import android.text.style.TypefaceSpan;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.TextView;

import ru.ada.adaphotoplan.R;


/**
 * Created by Bitizen on 28.03.17.
 */

public abstract class  ProtoActivity extends AppCompatActivity {

    private Toolbar toolbar;
    protected TextView title;
    protected ImageView edit;

    protected FrameLayout frame;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        super.setContentView(R.layout.activity_proto);

        toolbar = (Toolbar) findViewById(R.id.toolbar);
        title = (TextView) findViewById(R.id.title);
        frame = (FrameLayout) findViewById(R.id.frame);
        edit = (ImageView) findViewById(R.id.edit);

        setSupportActionBar(toolbar);
        getSupportActionBar().setHomeAsUpIndicator(R.drawable.ic_arrow_back);
        getSupportActionBar().setTitle("");
        String name = "ADA Photo Plan";
        SpannableString span = new SpannableString(name);
        span.setSpan(new TypefaceSpan("sans-serif-black"), 0, 4, Spanned.SPAN_INCLUSIVE_EXCLUSIVE);
        title.setText(span);
    }

    @Override
    public void setContentView(@LayoutRes int layoutResID) {
        getLayoutInflater().inflate(layoutResID, frame);
    }
}
