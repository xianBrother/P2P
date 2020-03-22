package com.rdc.p2p.adapter;

import android.annotation.SuppressLint;
import android.support.annotation.NonNull;
import android.support.constraint.ConstraintLayout;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.rdc.p2p.R;
import com.rdc.p2p.app.App;
import com.rdc.p2p.base.BaseRecyclerViewAdapter;
import com.rdc.p2p.bean.MessageBean;
import com.rdc.p2p.config.Constant;
import com.rdc.p2p.config.Protocol;
import com.rdc.p2p.listener.OnItemViewClickListener;
import com.rdc.p2p.util.ImageUtil;
import com.rdc.p2p.util.SDUtil;
import com.rdc.p2p.util.ScreenUtil;

import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import de.hdodenhof.circleimageview.CircleImageView;


public class MsgRvAdapter extends BaseRecyclerViewAdapter<MessageBean> {

    private static final String TAG = "MsgRvAdapter";
    private static final int TYPE_RIGHT_TEXT = 0;
    private static final int TYPE_RIGHT_IMAGE = 1;
    private static final int TYPE_LEFT_TEXT = 3;
    private static final int TYPE_LEFT_IMAGE = 4;
    private OnItemViewClickListener mOnItemViewClickListener;
    private int mTargetPeerImageId;//对方的用户头像id
    private List<String> mFileNameList;//文件名列表

    public MsgRvAdapter(int userImageId){
        mTargetPeerImageId = userImageId;
        mFileNameList = new ArrayList<>();
    }

    @Override
    public void appendData(MessageBean messageBean) {
        super.appendData(messageBean);

    }

    @Override
    public void appendData(List<MessageBean> dataList) {
        super.appendData(dataList);

    }

    public List<String> getFileNameList(){
        return mFileNameList;
    }

    public int getPositionByFileName(String fileName){
        for (int i = 0; i < mDataList.size(); i++) {
            if (fileName.equals(mDataList.get(i).getFileName())){
                return i;
            }
        }
        return -1;
    }

    @Override
    public int getItemViewType(int position) {
        MessageBean messageBean = mDataList.get(position);
        switch (messageBean.getMsgType()){
            case Protocol.TEXT:
                return messageBean.isMine() ? TYPE_RIGHT_TEXT : TYPE_LEFT_TEXT;
            case Protocol.IMAGE:
                return messageBean.isMine() ? TYPE_RIGHT_IMAGE : TYPE_LEFT_IMAGE;
            default:
                return TYPE_RIGHT_TEXT;
        }
    }

    @NonNull
    @Override
    public BaseRvHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view;
        switch (viewType){
            case TYPE_LEFT_TEXT:
                view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_message_left_text, parent, false);
                return new LeftTextHolder(view);
            case TYPE_RIGHT_TEXT:
                view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_message_right_text, parent, false);
                return new RightTextHolder(view);
            case TYPE_LEFT_IMAGE:
                view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_message_left_image, parent, false);
                return new LeftImageHolder(view);
            case TYPE_RIGHT_IMAGE:
                view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_message_right_image, parent, false);
                return new RightImageHolder(view);
            default:
                view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_message_left_text, parent, false);
                return new LeftTextHolder(view);
        }
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
        switch (holder.getItemViewType()){
            case TYPE_LEFT_TEXT:
                ((LeftTextHolder)holder).bindView(mDataList.get(position));
                break;
            case TYPE_RIGHT_TEXT:
                ((RightTextHolder)holder).bindView(mDataList.get(position));
                break;
            case TYPE_LEFT_IMAGE:
                ((LeftImageHolder)holder).bindView(mDataList.get(position));
                break;
            case TYPE_RIGHT_IMAGE:
                ((RightImageHolder)holder).bindView(mDataList.get(position));
                break;

            default:
                ((LeftTextHolder)holder).bindView(mDataList.get(position));
        }
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position, @NonNull List payloads) {
        if (payloads.isEmpty()){
            onBindViewHolder(holder,position);
        }else {
            int payload = (int) payloads.get(0);
            MessageBean messageBean = mDataList.get(position);
            switch (payload){
                case Constant.UPDATE_SEND_MSG_STATE:
                    switch (messageBean.getMsgType()){
                        case Protocol.TEXT:
                            RightTextHolder rightTextHolder = (RightTextHolder) holder;
                            updateSendMsgStatus(rightTextHolder.mPbSending,rightTextHolder.mIvAlter,messageBean.getSendStatus());
                            break;
                        case Protocol.IMAGE:
                            RightImageHolder rightImageHolder = (RightImageHolder) holder;
                            updateSendMsgStatus(rightImageHolder.mPbSending,rightImageHolder.mIvAlter,messageBean.getSendStatus());
                            break;
                    }
            }
        }
    }

    public void setOnItemViewClickListener(OnItemViewClickListener onItemViewClickListener){
        this.mOnItemViewClickListener = onItemViewClickListener;
    }




    class RightTextHolder extends BaseRvHolder{

        @BindView(R.id.civ_head_image_right_item_message)
        CircleImageView  mCivRightHeadImage;
        @BindView(R.id.tv_text_right_item_message)
        TextView mTvRightText;
        @BindView(R.id.pb_msg_sending_right_item_message)
        ProgressBar mPbSending;
        @BindView(R.id.iv_alter_right_item_message)
        ImageView mIvAlter;
        @BindView(R.id.ll_right_text_item_message)
        LinearLayout mLlRightTextLayout;


        RightTextHolder(View itemView) {
            super(itemView);
        }

        @Override
        protected void bindView(MessageBean messageBean) {
            Glide.with(itemView.getContext())
                    .load(ImageUtil.getImageResId(App.getUserBean().getUserImageId()))
                    .into(mCivRightHeadImage);
            mTvRightText.setText(messageBean.getText());
            if (mOnItemViewClickListener != null){
                mIvAlter.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        mOnItemViewClickListener.onAlterClick(getLayoutPosition());
                    }
                });
            }
            updateSendMsgStatus(mPbSending,mIvAlter,messageBean.getSendStatus());
        }
    }

    class LeftTextHolder extends BaseRvHolder{
        @BindView(R.id.civ_head_image_left_item_message)
        CircleImageView mCivLeftHeadImage;
        @BindView(R.id.tv_text_left_item_message)
        TextView mTvLeftText;
        @BindView(R.id.ll_left_text_item_message)
        LinearLayout mLlLeftTextLayout;

        LeftTextHolder(View itemView) {
            super(itemView);
        }

        @Override
        protected void bindView(MessageBean messageBean) {
            Glide.with(itemView.getContext())
                    .load(ImageUtil.getImageResId(mTargetPeerImageId))
                    .into(mCivLeftHeadImage);
            mTvLeftText.setText(messageBean.getText());
        }
    }

    class RightImageHolder extends BaseRvHolder{

        @BindView(R.id.civ_head_image_right_item_message)
        CircleImageView  mCivRightHeadImage;
        @BindView(R.id.iv_image_right_item_message)
        ImageView mIvRightImage;
        @BindView(R.id.pb_msg_sending_right_item_message)
        ProgressBar mPbSending;
        @BindView(R.id.iv_alter_right_item_message)
        ImageView mIvAlter;//发送失败警告

        RightImageHolder(View itemView) {
            super(itemView);
        }

        @Override
        protected void bindView(MessageBean messageBean) {
            Glide.with(itemView.getContext())
                    .load(ImageUtil.getImageResId(App.getUserBean().getUserImageId()))
                    .into(mCivRightHeadImage);
            setIvLayoutParams(mIvRightImage,messageBean.getImagePath());
            Glide.with(itemView.getContext())
                    .load(messageBean.getImagePath())
                    .into(mIvRightImage);
            updateSendMsgStatus(mPbSending,mIvAlter,messageBean.getSendStatus());
            if (mOnItemViewClickListener != null){
                mIvRightImage.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        mOnItemViewClickListener.onImageClick(getLayoutPosition());
                    }
                });
                mIvAlter.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        mOnItemViewClickListener.onAlterClick(getLayoutPosition());
                    }
                });
            }
        }
    }

    class LeftImageHolder extends BaseRvHolder{

        @BindView(R.id.civ_head_image_left_item_message)
        CircleImageView mCivLeftHeadImage;
        @BindView(R.id.iv_image_left_item_message)
        ImageView mIvLeftImage;

        LeftImageHolder(View itemView) {
            super(itemView);
        }

        @Override
        protected void bindView(MessageBean messageBean) {
            Glide.with(itemView.getContext())
                    .load(ImageUtil.getImageResId(mTargetPeerImageId))
                    .into(mCivLeftHeadImage);
            setIvLayoutParams(mIvLeftImage,messageBean.getImagePath());
            Glide.with(itemView.getContext())
                    .load(messageBean.getImagePath())
                    .into(mIvLeftImage);
            if (mOnItemViewClickListener != null){
                mIvLeftImage.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        mOnItemViewClickListener.onImageClick(getLayoutPosition());
                    }
                });
            }
        }
    }






    /**
     * 根据图片的高宽比例处理ImageView的高宽
     * @param iv
     * @param path
     */
    private void setIvLayoutParams(ImageView iv,String path){
        float scale = ImageUtil.getBitmapSize(path);
        ViewGroup.LayoutParams layoutParams = iv.getLayoutParams();
        int ivWidth;
        if (scale <= 0.65f){
            //宽图
            ivWidth = ScreenUtil.dip2px(App.getContxet(),220);
        }else {
            //长图
            ivWidth = ScreenUtil.dip2px(App.getContxet(),160);
        }
        layoutParams.width = ivWidth;
        layoutParams.height = (int) (ivWidth * scale);
        iv.setLayoutParams(layoutParams);
    }

    /**
     * 更新发送 文本/图片/音频 消息的传输状态
     * @param pbSending
     * @param ivAlter
     * @param status
     */
    private void updateSendMsgStatus(ProgressBar pbSending, ImageView ivAlter, int status){
        switch (status){
            case Constant.SEND_MSG_ING:
                pbSending.setVisibility(View.VISIBLE);
                ivAlter.setVisibility(View.GONE);
                break;
            case Constant.SEND_MSG_FINISH:
                pbSending.setVisibility(View.INVISIBLE);
                ivAlter.setVisibility(View.GONE);
                break;
            case Constant.SEND_MSG_ERROR:
                pbSending.setVisibility(View.GONE);
                ivAlter.setVisibility(View.VISIBLE);
                break;
        }
    }


}
