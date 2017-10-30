package ablecloud.matrix.tool;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.preference.PreferenceActivity;
import android.support.annotation.Nullable;
import android.support.v7.app.ActionBar;
import android.view.MenuItem;

/**
 * Created by wangkun on 04/08/2017.
 */

public class FunctionActivity extends ContainerActivity {

    public static void showFragment(Context context, String name) {
        showFragment(context, name, null);
    }

    public static void showFragment(Context context, String fragment, String title) {
        Intent intent = new Intent(context, FunctionActivity.class);
        intent.putExtra(PreferenceActivity.EXTRA_SHOW_FRAGMENT, fragment);
        if (title != null) intent.putExtra(PreferenceActivity.EXTRA_SHOW_FRAGMENT_TITLE, title);
        context.startActivity(intent);
    }

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        ActionBar actionBar = getSupportActionBar();
        actionBar.setDisplayHomeAsUpEnabled(true);
        Intent intent = getIntent();
        if (intent.hasExtra(PreferenceActivity.EXTRA_SHOW_FRAGMENT_TITLE))
            actionBar.setTitle(intent.getStringExtra(PreferenceActivity.EXTRA_SHOW_FRAGMENT_TITLE));
        addFragment(intent.getStringExtra(PreferenceActivity.EXTRA_SHOW_FRAGMENT));
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                onBackPressed();
                return true;
        }
        return super.onOptionsItemSelected(item);
    }
}
