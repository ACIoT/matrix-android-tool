package ablecloud.matrix.tool;

import android.Manifest;
import android.app.Fragment;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.TabLayout;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.Spinner;

import com.tbruyelle.rxpermissions2.RxPermissions;

import ablecloud.matrix.service.MatrixConfiguration;
import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import butterknife.OnItemSelected;
import butterknife.Unbinder;
import io.reactivex.annotations.NonNull;
import io.reactivex.functions.Consumer;

/**
 * Created by wangkun on 30/09/2017.
 */

public class WelcomeActivity extends ContainerActivity {

    private TabLayout tablayout;

    @Override
    protected int getContentLayout() {
        return R.layout.activity_welcome;
    }

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (MainApplication.isInited()) {
            startActivity(new Intent(this, MainActivity.class));
            finish();
            return;
        }

        new RxPermissions(this)
                .request(Manifest.permission.WRITE_EXTERNAL_STORAGE
                        , Manifest.permission.READ_EXTERNAL_STORAGE)
                .subscribe(new Consumer<Boolean>() {
                    @Override
                    public void accept(@NonNull Boolean aBoolean) throws Exception {
                    }
                });

        tablayout = ButterKnife.findById(this, R.id.tablayout);
        tablayout.addTab(tablayout.newTab().setText(R.string.public_service));
        tablayout.addTab(tablayout.newTab().setText(R.string.private_service));
        tablayout.addTab(tablayout.newTab().setText(R.string.global));

        tablayout.addOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
            @Override
            public void onTabSelected(TabLayout.Tab tab) {
                if (tab.getPosition() == 0) {
                    replaceFragment(PublicFragment.class, false);
                } else if (tab.getPosition() == 1) {
                    replaceFragment(PrivateFragment.class, false);
                } else {
                    replaceFragment(I18NFragment.class, false);
                }
            }

            @Override
            public void onTabUnselected(TabLayout.Tab tab) {

            }

            @Override
            public void onTabReselected(TabLayout.Tab tab) {

            }
        });
        addFragment(PublicFragment.class);
    }

    private enum Mode {
        SANDBOX_MODE(-1),
        TEST_MODE(MatrixConfiguration.TEST_MODE),
        PRODUCTION_MODE(MatrixConfiguration.PRODUCTION_MODE);

        private int value;

        Mode(int value) {
            this.value = value;
        }
    }

    private enum Region {
        REGION_CHINA(MatrixConfiguration.REGION_CHINA),
        REGION_SOUTHEAST_ASIA(MatrixConfiguration.REGION_SOUTHEAST_ASIA),
        REGION_NORTH_AMERICA(MatrixConfiguration.REGION_NORTH_AMERICA),
        REGION_CENTRAL_EUROPE(MatrixConfiguration.REGION_CENTRAL_EUROPE);

        private int value;

        Region(int value) {
            this.value = value;
        }
    }

    public static class PublicFragment extends Fragment {
        private Mode mode;
        private Region region;
        private ArrayAdapter<Mode> modeAdapter;
        private ArrayAdapter<Region> regionAdapter;

        @Override
        public void onCreate(@Nullable Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);
            modeAdapter = new ArrayAdapter<>(getActivity(), android.R.layout.simple_list_item_1);
            modeAdapter.addAll(Mode.values());
            regionAdapter = new ArrayAdapter<>(getActivity(), android.R.layout.simple_list_item_1);
            regionAdapter.addAll(Region.values());
        }

        @BindView(android.R.id.text1)
        EditText mainDomain;

        @BindView(android.R.id.text2)
        EditText mainDomainId;

        @BindView(R.id.mode)
        Spinner modeSpinner;

        @BindView(R.id.region)
        Spinner regionSpinner;

        private Unbinder unbinder;

        @Nullable
        @Override
        public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, Bundle savedInstanceState) {
            View view = inflater.inflate(R.layout.fragment_public, container, false);
            unbinder = ButterKnife.bind(this, view);
            modeSpinner.setAdapter(modeAdapter);
            modeSpinner.setSelection(1);
            regionSpinner.setAdapter(regionAdapter);
            regionSpinner.setSelection(1);


            return view;
        }

        @Override
        public void onDestroyView() {
            super.onDestroyView();
            unbinder.unbind();
        }

        @OnItemSelected({R.id.mode, R.id.region})
        public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
            switch (parent.getId()) {
                case R.id.mode:
                    mode = position >= 0 ? modeAdapter.getItem(position) : null;
                    break;
                case R.id.region:
                    region = position >= 0 ? regionAdapter.getItem(position) : null;
                    break;
            }
        }

        @OnClick(android.R.id.button1)
        public void onClick(View v) {
            ((MainApplication) getActivity().getApplication()).init(mainDomain.getText().toString(),
                    Long.valueOf(mainDomainId.getText().toString()), mode.value, region.value);
            startActivity(new Intent(getActivity(), MainActivity.class));
            getActivity().finish();
        }
    }

    public static class PrivateFragment extends Fragment {

        @BindView(android.R.id.text1)
        EditText mainDomain;

        @BindView(android.R.id.text2)
        EditText mainDomainId;

        @BindView(R.id.router)
        EditText router;

        @BindView(R.id.gateway)
        EditText gateway;

        private Unbinder unbinder;

        @Nullable
        @Override
        public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, Bundle savedInstanceState) {
            View view = inflater.inflate(R.layout.fragment_private, container, false);
            unbinder = ButterKnife.bind(this, view);
            return view;
        }

        @Override
        public void onDestroyView() {
            super.onDestroyView();
            unbinder.unbind();
        }

        @OnClick(android.R.id.button1)
        public void onClick(View v) {
            ((MainApplication) getActivity().getApplication()).init(
                    mainDomain.getText().toString(),
                    Long.valueOf(mainDomainId.getText().toString()),
                    router.getText().toString(),
                    gateway.getText().toString());
            startActivity(new Intent(getActivity(), MainActivity.class));
            getActivity().finish();
        }
    }
}
