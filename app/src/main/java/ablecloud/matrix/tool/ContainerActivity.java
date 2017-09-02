package ablecloud.matrix.tool;

import android.app.Activity;
import android.app.Fragment;
import android.os.Bundle;
import android.support.annotation.Nullable;

/**
 * Created by wangkun on 04/08/2017.
 */

public class ContainerActivity extends Activity {
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_container);
    }

    protected void addFragment(String fragmentClass) {
        try {
            addFragment((Class<? extends Fragment>) Class.forName(fragmentClass));
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        }
    }

    protected void addFragment(Class<? extends Fragment> fragmentClass) {
        try {
            Fragment fragment = fragmentClass.newInstance();
            getFragmentManager().beginTransaction().add(R.id.container, fragment).commitAllowingStateLoss();
        } catch (InstantiationException | IllegalAccessException e) {
            e.printStackTrace();
        }
    }

    protected void replaceFragment(Class<? extends Fragment> fragmentClass) {
        try {
            Fragment fragment = fragmentClass.newInstance();
            getFragmentManager().beginTransaction().addToBackStack(null).replace(R.id.container, fragment).commitAllowingStateLoss();
        } catch (InstantiationException | IllegalAccessException e) {
            e.printStackTrace();
        }
    }

    protected void replaceFragment(Fragment fragment) {
        getFragmentManager().beginTransaction().addToBackStack(null).replace(R.id.container, fragment).commitAllowingStateLoss();
    }

}
