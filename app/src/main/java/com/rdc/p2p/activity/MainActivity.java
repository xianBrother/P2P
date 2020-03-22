package com.rdc.p2p.activity;

import android.os.Parcelable;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.os.Bundle;
import android.support.v7.app.ActionBar;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;


import com.rdc.p2p.R;
import com.rdc.p2p.base.BaseActivity;
import com.rdc.p2p.base.BasePresenter;
import com.rdc.p2p.fragment.PeerListFragment;
import com.rdc.p2p.fragment.ScanDeviceFragment;

import butterknife.BindView;

public class MainActivity extends BaseActivity {

    @BindView(R.id.toolbar)
    Toolbar mToolbar;
    @BindView(R.id.vp_act_main)
    ViewPager mVpContent;

    private FragmentPagerAdapter mFragmentPagerAdapter;

    private PeerListFragment mPeerListFragment;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }


    @Override
    protected void onRestoreInstanceState(Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);
    }

    @Override
    public BasePresenter getInstance() {
        return null;
    }

    @Override
    protected int setLayoutResID() {
        return R.layout.activity_main;
    }

    @Override
    protected void initData() {

    }



    @Override
    protected void initView() {
        initToolbar();
        mPeerListFragment = new PeerListFragment();
        mFragmentPagerAdapter = new FragmentPagerAdapter(getSupportFragmentManager()) {
            @Override
            public Fragment getItem(int position) {
                switch (position){
                    case 0:
                        return mPeerListFragment;
                }
                return null;
            }

            @Override
            public int getCount() {
                return 1;
            }

            @Override
            public void restoreState(Parcelable state, ClassLoader loader) {
                super.restoreState(state, loader);
            }

            @Override
            public Parcelable saveState() {
                return super.saveState();
            }
        };
        mVpContent.setAdapter(mFragmentPagerAdapter);
    }

    @Override
    protected void initListener() {

    }

    private void initToolbar() {
        setSupportActionBar(mToolbar);
        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(false);
            actionBar.setDisplayShowTitleEnabled(false);
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu,menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()){
            case R.id.menu_search:
                if (mPeerListFragment.isServerSocketConnected()){
                    ScanDeviceFragment mScanDeviceFragment = new ScanDeviceFragment();
                    mScanDeviceFragment.setCancelable(false);
                    mScanDeviceFragment.show(getSupportFragmentManager(),"scanDevice");
                }else {
                    showToast("请检查WIFI");
                }
                break;
        }
        return true;
    }


}
