package ablecloud.matrix.tool;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.Spinner;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnItemSelected;
import butterknife.Unbinder;

/**
 * Created by wangkun on 30/09/2017.
 */

public class WelcomeActivity extends Activity {

    @BindView(android.R.id.text1)
    EditText mainDomain;

    @BindView(android.R.id.text2)
    EditText mainDomainId;

    @BindView(R.id.mode)
    Spinner modeSpinner;

    @BindView(R.id.region)
    Spinner regionSpinner;

    private AlertDialog initDialog;
    private int mode;
    private int region;
    private Unbinder unbinder;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (MainApplication.isInited()) {
            startActivity(new Intent(this, MainActivity.class));
            finish();
        }

        View dialogLayout = getLayoutInflater().inflate(R.layout.dialog_init, (ViewGroup) getWindow().getDecorView(), false);
        unbinder = ButterKnife.bind(this, dialogLayout);

        ArrayAdapter<Mode> modeAdapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1);
        modeAdapter.addAll(Mode.values());
        modeSpinner.setAdapter(modeAdapter);

        ArrayAdapter<Region> regionAdapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1);
        regionAdapter.addAll(Region.values());
        regionSpinner.setAdapter(regionAdapter);

        initDialog = new AlertDialog.Builder(this)
                .setTitle("初始化")
                .setView(dialogLayout)
                .setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        ((MainApplication) getApplication()).init(mainDomain.getText().toString(),
                                Long.valueOf(mainDomainId.getText().toString()), mode, region);
                        startActivity(new Intent(WelcomeActivity.this, MainActivity.class));
                        finish();
                    }
                })
                .setCancelable(false)
                .create();

        initDialog.show();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        unbinder.unbind();
    }

    @OnItemSelected({R.id.mode, R.id.region})
    public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
        switch (parent.getId()) {
            case R.id.mode:
                mode = position;
                break;
            case R.id.region:
                region = position < 2 ? position : (position - 1);
                break;
        }
    }

    private enum Mode {
        MODE_TEST, MODE_PRODUCTION
    }

    private enum Region {
        REGION_CHINA, REGION_SOUTHEAST_ASIA, REGION_NORTH_AMERICA, REGION_CENTRAL_EUROPE
    }
}
