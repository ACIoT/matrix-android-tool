package ablecloud.matrix.tool;

import android.app.Activity;
import android.app.Fragment;
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

public class DeviceControlActivity extends Activity {

    public static void cloudMessage(Context context) {
        context.startActivity(new Intent(context, DeviceControlActivity.class).putExtra(PreferenceActivity.EXTRA_SHOW_FRAGMENT, CloudMessageFragment.class.getName()));
    }

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_container);
        getActionBar().setDisplayHomeAsUpEnabled(true);
        addFragment(getIntent().getStringExtra(PreferenceActivity.EXTRA_SHOW_FRAGMENT));
    }

    private void addFragment(String fragmentClass) {
        try {
            addFragment((Class<? extends Fragment>) Class.forName(fragmentClass));
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        }
    }

    private void addFragment(Class<? extends Fragment> fragmentClass) {
        try {
            Fragment fragment = fragmentClass.newInstance();
            getFragmentManager().beginTransaction().add(R.id.container, fragment).commitAllowingStateLoss();
        } catch (InstantiationException | IllegalAccessException e) {
            e.printStackTrace();
        }
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
