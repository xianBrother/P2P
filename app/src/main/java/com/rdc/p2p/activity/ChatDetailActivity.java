package com.rdc.p2p.activity;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.constraint.ConstraintLayout;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v4.content.FileProvider;
import android.support.v7.app.ActionBar;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.PopupWindow;
import android.widget.TextView;

import com.rdc.p2p.R;
import com.rdc.p2p.adapter.MsgRvAdapter;
import com.rdc.p2p.base.BaseActivity;
import com.rdc.p2p.bean.PeerBean;
import com.rdc.p2p.event.LinkSocketRequestEvent;
import com.rdc.p2p.bean.MessageBean;
import com.rdc.p2p.config.Constant;
import com.rdc.p2p.config.Protocol;
import com.rdc.p2p.contract.ChatDetailContract;
import com.rdc.p2p.event.LinkSocketResponseEvent;
import com.rdc.p2p.event.RecentMsgEvent;
import com.rdc.p2p.listener.OnItemViewClickListener;
import com.rdc.p2p.manager.SocketManager;
import com.rdc.p2p.presenter.ChatDetailPresenter;
import com.rdc.p2p.util.SDUtil;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;
import org.litepal.crud.DataSupport;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;

public class ChatDetailActivity extends BaseActivity<ChatDetailPresenter> implements ChatDetailContract.View {

    private static final String TAG = "ChatDetailActivity";
    private static final int CHOOSE_PHOTO = 2;
    private static final int TAKE_PHOTO = 3;
    private static final int FILE_MANAGER = 4;
    private static final int SCROLL = -1;//滑动到底部
    private static final int SCROLL_NOW = -3;//立即滚动到底部
    private static final int HIDE_SOFT_INPUT = -2;//隐藏软键盘
    @BindView(R.id.toolbar)
    Toolbar mToolbar;
    @BindView(R.id.tv_title)
    TextView mTvTitle;
    @BindView(R.id.iv_photo_album_act_chat_detail)
    ImageView mIvPhotoAlbum;
    @BindView(R.id.iv_take_photo_act_chat_detail)
    ImageView mIvTakePhoto;
    @BindView(R.id.rv_msg_list_act_chat_detail)
    RecyclerView mRvMsgList;
    @BindView(R.id.btn_send_chat_detail)
    Button mBtnSend;
    @BindView(R.id.et_input_act_chat_detail)
    EditText mEtInput;
    @BindView(R.id.layout_root_act_chat_detail)
    ConstraintLayout mRootLayout;




    private MsgRvAdapter mMsgRvAdapter;
    private static String mTargetPeerName;
    private static String mTargetPeerIp;
    private static int mTargetPeerImageId;
    private Uri mTakePhotoUri;
    private File mTakePhotoFile;
    private ImageView mIvMicrophone;
    private TextView mTvRecordTime;
    private PopupWindow mPwMicrophone;
    private boolean isSendingFile;//是否正在发送文件

    private Handler mHandler = new Handler(new Handler.Callback() {
        @Override
        public boolean handleMessage(Message message) {
            switch (message.what) {
                case SCROLL:
                    int lastItemPosition = mMsgRvAdapter.getItemCount() - 1;
                    if (lastItemPosition >= 0 &&
                            mRvMsgList.getScrollState() == RecyclerView.SCROLL_STATE_IDLE){
                        //如果当前用户没有拖动列表，则自动滚动到最后一个
                        mRvMsgList.smoothScrollToPosition(lastItemPosition);
                    }
                    break;
                case SCROLL_NOW:
                    if (mMsgRvAdapter.getItemCount() - 1 > 0) {
                        mRvMsgList.smoothScrollToPosition(mMsgRvAdapter.getItemCount() - 1);
                    }
                    break;
                case HIDE_SOFT_INPUT:
                    hideKeyboard();
                    break;
            }
            return false;
        }
    });

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EventBus.getDefault().register(this);
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putString("peerName", mTargetPeerName);
        outState.putString("peerIp", mTargetPeerIp);
        outState.putInt("peerImageId",mTargetPeerImageId);
    }

    @Override
    protected void onRestoreInstanceState(Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);
        mTargetPeerName = savedInstanceState.getString("peerName");
        mTargetPeerIp = savedInstanceState.getString("peerIp");
        mTargetPeerImageId = savedInstanceState.getInt("peerImageId");
    }

    @Override
    protected void onDestroy() {
        EventBus.getDefault().unregister(this);

        super.onDestroy();
    }

    public static void actionStart(Context context, String peerIp, String peerName,int peerImageId) {
        mTargetPeerName = peerName;
        mTargetPeerIp = peerIp;
        mTargetPeerImageId = peerImageId;
        context.startActivity(new Intent(context, ChatDetailActivity.class));
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                onBackPressed();
                break;
        }
        return true;
    }

    @Override
    public void onBackPressed() {
            super.onBackPressed();

    }

    @Override
    public ChatDetailPresenter getInstance() {
        return new ChatDetailPresenter(this,mTargetPeerIp);
    }

    @Override
    protected int setLayoutResID() {
        return R.layout.activity_chat_detail;
    }

    @Override
    protected void initData() {
        isSendingFile = false;
    }

    @Override
    protected void initView() {
        initToolbar();
        mTvTitle.setText(mTargetPeerName);
        mMsgRvAdapter = new MsgRvAdapter(mTargetPeerImageId);
        LinearLayoutManager mLLManager = new LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false);
        mRvMsgList.setLayoutManager(mLLManager);
        mRvMsgList.setAdapter(mMsgRvAdapter);
        mRvMsgList.getItemAnimator().setChangeDuration(0);
        mMsgRvAdapter.appendData(DataSupport.where("belongIp = ?",mTargetPeerIp).find(MessageBean.class));
        mHandler.sendEmptyMessage(SCROLL_NOW);
    }

    @SuppressLint("ClickableViewAccessibility")
    @Override
    protected void initListener() {
        mMsgRvAdapter.setOnItemViewClickListener(new OnItemViewClickListener() {
            @Override
            public void onImageClick(int position) {
                List<String> list = new ArrayList<>();
                for (MessageBean messageBean : mMsgRvAdapter.getDataList()) {
                    if (messageBean.getMsgType() == Protocol.IMAGE) {
                        //获取所有的图片本地地址
                        list.add(messageBean.getImagePath());
                    }
                }
                String currentImagePath = mMsgRvAdapter.getDataList().get(position).getImagePath();
                PhotoActivity.actionStart(ChatDetailActivity.this,list,list.indexOf(currentImagePath));
            }


            @Override
            public void onAlterClick(int position) {
                if (SocketManager.getInstance().isClosedSocket(mTargetPeerIp)){
                    linkSocket();
                    showToast("连接Socket中");
                }else {
                    MessageBean messageBean = mMsgRvAdapter.getDataList().get(position);
                    messageBean.setSendStatus(Constant.SEND_MSG_ING);
                    mMsgRvAdapter.notifyItemChanged(position,Constant.UPDATE_SEND_MSG_STATE);
                    presenter.sendMsg(messageBean,position);
                }
            }


        });
        mBtnSend.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (!TextUtils.isEmpty(getString(mEtInput))) {
                    MessageBean textMsg = new MessageBean(mTargetPeerIp);
                    textMsg.setMine(true);
                    textMsg.setMsgType(Protocol.TEXT);
                    textMsg.setText(getString(mEtInput));
                    textMsg.setSendStatus(Constant.SEND_MSG_ING);
                    mMsgRvAdapter.appendData(textMsg);
                    mHandler.sendEmptyMessage(SCROLL_NOW);
                    presenter.sendMsg(textMsg,mMsgRvAdapter.getItemCount()-1);
                    EventBus.getDefault().post(new RecentMsgEvent(getString(mEtInput),mTargetPeerIp));
                    mEtInput.setText("");
                }
            }
        });
        mIvPhotoAlbum.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent("android.intent.action.GET_CONTENT");
                intent.setType("image/*");
                startActivityForResult(intent, CHOOSE_PHOTO);
            }
        });
        mIvTakePhoto.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (ContextCompat.checkSelfPermission(ChatDetailActivity.this, android.Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
                    ActivityCompat.requestPermissions(ChatDetailActivity.this,
                            new String[]{android.Manifest.permission.CAMERA}, 2);
                } else {
                    openCamera();
                }
            }
        });


    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        switch (requestCode) {
            case 2:
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    openCamera();
                } else {
                    showToast("拒绝授权，无法使用相机！");
                }
                break;
            case 3:
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {

                } else {
                }
                break;
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        switch (requestCode) {
            case CHOOSE_PHOTO:
                if (resultCode == RESULT_OK) {
                    String imagePath = SDUtil.getFilePathByUri(ChatDetailActivity.this,data.getData());
                    MessageBean imageMsg = new MessageBean(mTargetPeerIp);
                    imageMsg.setMine(true);
                    imageMsg.setMsgType(Protocol.IMAGE);
                    imageMsg.setImagePath(imagePath);
                    imageMsg.setSendStatus(Constant.SEND_MSG_ING);
                    mMsgRvAdapter.appendData(imageMsg);
                    mHandler.sendEmptyMessage(SCROLL_NOW);
                    EventBus.getDefault().post(new RecentMsgEvent("图片",mTargetPeerIp));
                    presenter.sendMsg(imageMsg,mMsgRvAdapter.getItemCount()-1);
                }
                break;
            case TAKE_PHOTO:
                if (resultCode == RESULT_OK) {
                    MessageBean imageMsg = new MessageBean(mTargetPeerIp);
                    imageMsg.setMine(true);
                    imageMsg.setMsgType(Protocol.IMAGE);
                    imageMsg.setImagePath(mTakePhotoFile.getAbsolutePath());
                    imageMsg.setSendStatus(Constant.SEND_MSG_ING);
                    mMsgRvAdapter.appendData(imageMsg);
                    mHandler.sendEmptyMessage(SCROLL_NOW);
                    EventBus.getDefault().post(new RecentMsgEvent("图片",mTargetPeerIp));
                    presenter.sendMsg(imageMsg,mMsgRvAdapter.getItemCount()-1);
                }
                break;
        }
    }

    /**
     * 隐藏键盘
     */
    private void hideKeyboard() {
        InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
        if (imm.isActive() && getCurrentFocus() != null) {
            if (getCurrentFocus().getWindowToken() != null) {
                imm.hideSoftInputFromWindow(getCurrentFocus().getWindowToken(), InputMethodManager.HIDE_NOT_ALWAYS);
            }
        }
    }
    /**
     * 打开相机
     */
    private void openCamera() {
        mTakePhotoFile = new File(getExternalCacheDir(), "take_photo.jpg");
        if (mTakePhotoFile.exists()) {
            mTakePhotoFile.delete();
            try {
                mTakePhotoFile.createNewFile();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        if (Build.VERSION.SDK_INT >= 24) {
            mTakePhotoUri = FileProvider.getUriForFile(ChatDetailActivity.this, "com.rdc.p2p.fileprovider", mTakePhotoFile);
        } else {
            mTakePhotoUri = Uri.fromFile(mTakePhotoFile);
        }
        Intent intent = new Intent("android.media.action.IMAGE_CAPTURE");
        intent.putExtra(MediaStore.EXTRA_OUTPUT, mTakePhotoUri);
        startActivityForResult(intent, TAKE_PHOTO);
    }

    @Override
    public void linkSocket() {
        LinkSocketRequestEvent linkSocketRequestEvent = new LinkSocketRequestEvent(mTargetPeerIp);
        EventBus.getDefault().post(linkSocketRequestEvent);
    }

    @Override
    public void sendMsgSuccess(int position) {
        //设置状态
        MessageBean messageBean = mMsgRvAdapter.getDataList().get(position);
        messageBean.setSendStatus(Constant.SEND_MSG_FINISH);
        mMsgRvAdapter.notifyItemChanged(position,Constant.UPDATE_SEND_MSG_STATE);
    }

    @Override
    public void sendMsgError(int position, String error) {
        //设置状态
        MessageBean messageBean = mMsgRvAdapter.getDataList().get(position);
        messageBean.setSendStatus(Constant.SEND_MSG_ERROR);
        mMsgRvAdapter.notifyItemChanged(position,Constant.UPDATE_SEND_MSG_STATE);
        showToast(error);
    }

    @Override
    public void fileSending(int position, MessageBean messageBean) {
        MessageBean dataMessage = mMsgRvAdapter.getDataList().get(position);
        dataMessage.updateFileState(messageBean);
        mMsgRvAdapter.notifyItemChanged(position,Constant.UPDATE_FILE_STATE);
    }

    @Override
    public boolean dispatchTouchEvent(MotionEvent ev) {
        if (ev.getAction() == MotionEvent.ACTION_DOWN) {
            View currentFocusView = getCurrentFocus();
            if (isShouldHideInput(currentFocusView, ev)) {
                mHandler.sendEmptyMessageDelayed(HIDE_SOFT_INPUT, 100);
            }
            return super.dispatchTouchEvent(ev);
        }
        // 必不可少，否则所有的组件都不会有TouchEvent了
        return getWindow().superDispatchTouchEvent(ev) || onTouchEvent(ev);
    }


    private boolean isShouldHideInput(View v, MotionEvent event) {
        if (v != null && (v instanceof EditText)) {
            int[] leftTop = {0, 0};
            //获取输入框当前的location位置
            v.getLocationInWindow(leftTop);
            int left = leftTop[0];
            int top = leftTop[1];
            int bottom = top + v.getHeight();
            int right = left + v.getWidth();
            if (event.getX() > left && event.getX() < right
                    && event.getY() > top && event.getY() < bottom) {
                // 点击的是输入框区域，保留点击EditText的事件
                return false;
            } else {
                return true;
            }
        }
        return false;
    }

    private void initToolbar() {
        setSupportActionBar(mToolbar);
        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(true);
            actionBar.setDisplayShowTitleEnabled(false);
        }
        mToolbar.setTitle("");
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void receiveMessage(MessageBean messageBean) {
        if (messageBean.getUserIp().equals(mTargetPeerIp)) {

                //其他消息直接添加到数据源中并更新RecyclerView界面
                mMsgRvAdapter.appendData(messageBean);
                mHandler.sendEmptyMessage(SCROLL);

        }
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void linkSocketResponse(LinkSocketResponseEvent responseEvent){
        PeerBean peerBean = responseEvent.getPeerBean();
        if (peerBean.getUserIp().equals(mTargetPeerIp)){
            if (responseEvent.isState()){
                mTargetPeerImageId = peerBean.getUserImageId();
                mTargetPeerName = peerBean.getNickName();
            }
            presenter.setLinkSocketState(responseEvent.isState());
        }
    }
}
