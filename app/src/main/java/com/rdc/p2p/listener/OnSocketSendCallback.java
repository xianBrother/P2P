package com.rdc.p2p.listener;

import com.rdc.p2p.bean.MessageBean;

public interface OnSocketSendCallback {

    /**
     * 发送消息成功(图片、文字、音频)
     */
    void sendMsgSuccess(int position);

    /**
     * 发送消息失败
     */
    void sendMsgError(int position);

    /**
     * 发送文件中...
     */
    void fileSending(int position,MessageBean messageBean);
}
