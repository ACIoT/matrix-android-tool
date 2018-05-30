package ablecloud.matrix.tool;

import android.app.AlertDialog;
import android.app.Fragment;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListAdapter;
import android.widget.TextView;

import com.google.gson.Gson;

import java.lang.ref.SoftReference;
import java.util.List;

import ablecloud.matrix.MatrixCallback;
import ablecloud.matrix.MatrixError;
import ablecloud.matrix.app.Matrix;
import ablecloud.matrix.service.MatrixConfiguration;
import butterknife.ButterKnife;
import butterknife.Unbinder;
import fr.ganfra.materialspinner.MaterialSpinner;
import io.reactivex.Single;
import io.reactivex.SingleEmitter;
import io.reactivex.SingleOnSubscribe;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.Disposable;
import io.reactivex.functions.Consumer;
import io.reactivex.schedulers.Schedulers;

public class I18NFragment extends Fragment implements View.OnClickListener {
    EditText et_dominname;
    MaterialSpinner mode;
    MaterialSpinner country;
    EditText et_dominid;

    private ArrayAdapter<Country> mCountryArrayAdapter;
    private ArrayAdapter<Mode> mModeArrayAdapter;
    private Mode modeValue = Mode.SANDBOX_MODE;
    private Country countryValue = Country.CHINA;
    private Button bt_ok;
    private ProgressDialog mProgressDialog;
    private String mDomin;
    private Long mDominId;

    @Override
    public void onClick(View view) {

        mDomin = et_dominname.getText().toString().trim();
        mDominId = Long.parseLong(et_dominid.getText().toString().trim());

        Disposable configRegion = Single.create(new SingleOnSubscribe<RegionBean>() {
            @Override
            public void subscribe(final SingleEmitter<RegionBean> e) throws Exception {
                Matrix.configGlobal(mDomin, modeValue.value, countryValue.name, new MatrixCallback<String>() {
                    @Override
                    public void success(String s) {
                        RegionBean regionBean = new Gson().fromJson(s, RegionBean.class);
                        e.onSuccess(regionBean);
                    }

                    @Override
                    public void error(MatrixError matrixError) {
                        e.onError(matrixError);
                    }
                });
            }
        })
                .subscribeOn(Schedulers.newThread())
                .observeOn(AndroidSchedulers.mainThread())
                .doOnSubscribe(new Consumer<Disposable>() {
                    @Override
                    public void accept(Disposable disposable) throws Exception {
                        mProgressDialog.show();
                    }
                })
                .doOnSuccess(new Consumer<RegionBean>() {
                    @Override
                    public void accept(RegionBean regionBean) throws Exception {
                        mProgressDialog.dismiss();
                    }
                })
                .subscribe(new Consumer<RegionBean>() {
                    @Override
                    public void accept(RegionBean regionBean) throws Exception {
                        EnvironmentAdapter environmentAdapter = new EnvironmentAdapter(regionBean, new SoftReference<Context>(getActivity()));
                        showSingleChoose(environmentAdapter);
                    }
                }, new Consumer<Throwable>() {
                    @Override
                    public void accept(Throwable throwable) throws Exception {
                        throwable.printStackTrace();
                    }
                });
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mCountryArrayAdapter = new ArrayAdapter<>(getActivity(), android.R.layout.simple_list_item_1);
        mCountryArrayAdapter.addAll(Country.values());
        mModeArrayAdapter = new ArrayAdapter<>(getActivity(), android.R.layout.simple_list_item_1);
        mModeArrayAdapter.addAll(Mode.values());
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View i18nView = inflater.inflate(R.layout.fragment_i18n, container, false);
        et_dominid = i18nView.findViewById(R.id.et_dominid);
        et_dominname = i18nView.findViewById(R.id.et_dominname);
        mode = i18nView.findViewById(R.id.mode);
        country = i18nView.findViewById(R.id.country);
        bt_ok = i18nView.findViewById(R.id.bt_ok);
        bt_ok.setOnClickListener(this);
        mode.setSelection(0);
        country.setSelection(0);

        mode.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int position, long l) {
                modeValue = position >= 0 ? mModeArrayAdapter.getItem(position) : null;
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {

            }
        });
        country.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int position, long l) {
                countryValue = position >= 0 ? mCountryArrayAdapter.getItem(position) : null;
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {

            }
        });

        mProgressDialog = new ProgressDialog(getActivity());
        mProgressDialog.setMessage("加载中");
        return i18nView;
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        ButterKnife.bind(this, view);
        mode.setAdapter(mModeArrayAdapter);
        country.setAdapter(mCountryArrayAdapter);
    }

    private enum Country {
        CHINA("CN"),
        USA("US"),
        FRANCE("FR");
        private String name;

        Country(String name) {
            this.name = name;
        }
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

    public static class RegionBean {

        /**
         * matchedRegion : {"id":"test","redirectDomain":"test.ablecloud.cn:9100","routerDomain":"test.ablecloud.cn:9005","webSocketDomain":"test.ablecloud.cn:9001"}
         * regionList : [{"id":"test","redirectDomain":"test.ablecloud.cn:9100","routerDomain":"test.ablecloud.cn:9005","webSocketDomain":"test.ablecloud.cn:9001"}]
         */

        private MatchedRegionBean matchedRegion;
        private List<RegionListBean> regionList;

        public MatchedRegionBean getMatchedRegion() {
            return matchedRegion;
        }

        public void setMatchedRegion(MatchedRegionBean matchedRegion) {
            this.matchedRegion = matchedRegion;
        }

        public List<RegionListBean> getRegionList() {
            return regionList;
        }

        public void setRegionList(List<RegionListBean> regionList) {
            this.regionList = regionList;
        }

        public static class MatchedRegionBean {
            /**
             * id : test
             * redirectDomain : test.ablecloud.cn:9100
             * routerDomain : test.ablecloud.cn:9005
             * webSocketDomain : test.ablecloud.cn:9001
             */

            private String id;
            private String redirectDomain;
            private String routerDomain;
            private String webSocketDomain;

            public String getId() {
                return id;
            }

            public void setId(String id) {
                this.id = id;
            }

            public String getRedirectDomain() {
                return redirectDomain;
            }

            public void setRedirectDomain(String redirectDomain) {
                this.redirectDomain = redirectDomain;
            }

            public String getRouterDomain() {
                return routerDomain;
            }

            public void setRouterDomain(String routerDomain) {
                this.routerDomain = routerDomain;
            }

            public String getWebSocketDomain() {
                return webSocketDomain;
            }

            public void setWebSocketDomain(String webSocketDomain) {
                this.webSocketDomain = webSocketDomain;
            }
        }

        public static class RegionListBean {
            /**
             * id : test
             * redirectDomain : test.ablecloud.cn:9100
             * routerDomain : test.ablecloud.cn:9005
             * webSocketDomain : test.ablecloud.cn:9001
             */

            private String id;
            private String redirectDomain;
            private String routerDomain;
            private String webSocketDomain;

            public String getId() {
                return id;
            }

            public void setId(String id) {
                this.id = id;
            }

            public String getRedirectDomain() {
                return redirectDomain;
            }

            public void setRedirectDomain(String redirectDomain) {
                this.redirectDomain = redirectDomain;
            }

            public String getRouterDomain() {
                return routerDomain;
            }

            public void setRouterDomain(String routerDomain) {
                this.routerDomain = routerDomain;
            }

            public String getWebSocketDomain() {
                return webSocketDomain;
            }

            public void setWebSocketDomain(String webSocketDomain) {
                this.webSocketDomain = webSocketDomain;
            }
        }
    }

    public void showSingleChoose(final ListAdapter listAdapter) {
        AlertDialog dialog = new AlertDialog
                .Builder(getActivity())
                .setTitle("请选择环境")
                .setSingleChoiceItems(listAdapter, -1, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        dialogInterface.dismiss();
                        RegionBean.RegionListBean regionListBean = (RegionBean.RegionListBean) listAdapter.getItem(i);
                        ((MainApplication) getActivity().getApplication()).initI18N(
                                mDomin
                                , mDominId
                                , regionListBean.getRouterDomain()
                                , regionListBean.getWebSocketDomain()
                                , regionListBean.getRedirectDomain()
                                , regionListBean.getId());
                        startActivity(new Intent(getActivity(), MainActivity.class));
                        getActivity().finish();
                    }
                }).create();

        dialog.show();
    }

    private class EnvironmentAdapter extends BaseAdapter {

        private final Context context;
        private RegionBean mRegionBean;

        public EnvironmentAdapter(RegionBean regionBean, SoftReference<Context> contextSoftReference) {
            mRegionBean = regionBean;
            context = contextSoftReference.get();
        }

        @Override
        public int getCount() {
            return mRegionBean.regionList.size();
        }

        @Override
        public Object getItem(int i) {
            return mRegionBean.regionList.get(i);
        }

        @Override
        public long getItemId(int i) {
            return i;
        }

        @Override
        public View getView(int i, View view, ViewGroup viewGroup) {
            View inflate = LayoutInflater.from(context).inflate(R.layout.item_envirment, viewGroup, false);
            ((TextView) inflate.findViewById(R.id.tv_redirect)).setText("redirect:" + mRegionBean.regionList.get(i).redirectDomain);
            ((TextView) inflate.findViewById(R.id.tv_router)).setText("router:" + mRegionBean.regionList.get(i).getRouterDomain());
            ((TextView) inflate.findViewById(R.id.tv_websocket)).setText("webSocket:" + mRegionBean.regionList.get(i).getWebSocketDomain());
            return inflate;
        }
    }
}
