package ablecloud.matrix.tool;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.preference.PreferenceActivity;
import android.support.annotation.Nullable;
import android.support.v4.app.NavUtils;
import android.view.MenuItem;

/**
 * Created by wangkun on 04/08/2017.
 */

public class FunctionActivity extends ContainerActivity {

    public static void showFragment(Context context, String name) {
        context.startActivity(new Intent(context, FunctionActivity.class).putExtra(PreferenceActivity.EXTRA_SHOW_FRAGMENT, name));
    }

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getActionBar().setDisplayHomeAsUpEnabled(true);
        addFragment(getIntent().getStringExtra(PreferenceActivity.EXTRA_SHOW_FRAGMENT));
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                NavUtils.navigateUpFromSameTask(this);
                return true;
        }
        return super.onOptionsItemSelected(item);
    }
}
