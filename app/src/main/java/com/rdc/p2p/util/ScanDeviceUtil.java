package com.rdc.p2p.util;

import java.net.Inet4Address;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Enumeration;
import java.util.List;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import android.annotation.SuppressLint;
import android.text.TextUtils;
import android.util.Log;

import io.reactivex.Observable;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.functions.Consumer;
import io.reactivex.functions.Function;
import io.reactivex.schedulers.Schedulers;

//扫描局域网端口
public class ScanDeviceUtil {

    private static final String TAG = "ScanDeviceUtil";

    private static final int CORE_POOL_SIZE = 5;

    private static final int MAX_POOL_SIZE = 255;

    private static final int QUEUE_LENGTH = 125;

    private String mDevAddress;// 本机IP地址-完整
    private String mLocAddress;// 局域网IP地址头,如：192.168.1.
    private Runtime mRun = Runtime.getRuntime();// 获取当前运行环境，来执行ping，相当于windows的cmd
    private String mPing = "ping -c 1 -w 3 ";// 其中 -c 1为发送的次数，-w 表示发送后等待响应的时间
    private CopyOnWriteArrayList<String> mIpList;// ping成功的IP地址
    private ThreadPoolExecutor mExecutor;// 线程池对象
    private static ScanDeviceUtil mScanDeviceUtil;
    private ScanDeviceUtil(){
        mIpList = new CopyOnWriteArrayList<>();
    }
    public static ScanDeviceUtil getInstance(){
        if (mScanDeviceUtil == null){
            synchronized (ScanDeviceUtil.class){
                if (mScanDeviceUtil == null){
                    mScanDeviceUtil = new ScanDeviceUtil();
                }
            }
        }
        return mScanDeviceUtil;
    }
    public boolean isFinish(){
        return mExecutor.isTerminated();
    }

    public String getDevAddress(){
        return mDevAddress == null ? "" :mDevAddress;
    }

    public void gc(){
        mRun.gc();
    }


    public List<String> getIpList(){
        return mIpList;
    }


    public boolean getLocalAddressPrefix() {
        mDevAddress = getLocAddress();// 获取本机IP地址
        mLocAddress = getLocalAddressIndex(mDevAddress);// 获取本地ip前缀
        if (TextUtils.isEmpty(mLocAddress)){
            return false;
        }
        return true;
    }

    public void scan() {
        mIpList.clear();
        mExecutor = new ThreadPoolExecutor(CORE_POOL_SIZE, MAX_POOL_SIZE,
                1000, TimeUnit.MILLISECONDS, new ArrayBlockingQueue<Runnable>(
                QUEUE_LENGTH));
        // 新建线程池
        for (int i = 1; i < 256; i++) {
            // 创建256个线程分别去ping
            final int lastAddress = i;// 存放ip最后一位地址 1-255
            Runnable run = new Runnable() {

                @Override
                public void run() {
                    // TODO Auto-generated method stub
                    String ping = ScanDeviceUtil.this.mPing + mLocAddress
                            + lastAddress;
                    String currentIp = mLocAddress + lastAddress;
                    if (mDevAddress.equals(currentIp)){
                        // 如果与本机IP地址相同,跳过
                        return;
                    }
                    Process process = null;
                    try {
                         process = mRun.exec(ping);
                        int result = process.waitFor();
                        if (result == 0) {
                            mIpList.add(currentIp);
                        }
                    } catch (Exception e) {
                        Log.e(TAG, "扫描异常" + e.toString());
                    } finally {
                        if (process != null){
                            process.destroy();
                        }
                    }
                }
            };
            mExecutor.execute(run);
        }
        mExecutor.shutdown();
    }

//获取本地ip
    public String getLocAddress() {
        String ipAddress = "";
        try {
            Enumeration<NetworkInterface> en = NetworkInterface
                    .getNetworkInterfaces();
            // 遍历所用的网络接口
            while (en.hasMoreElements()) {
                NetworkInterface networks = en.nextElement();
                // 得到每一个网络接口绑定的所有ip
                Enumeration<InetAddress> address = networks.getInetAddresses();
                // 遍历每一个接口绑定的所有ip
                while (address.hasMoreElements()) {
                    InetAddress ip = address.nextElement();
                    if (!ip.isLoopbackAddress()
                            && ip instanceof Inet4Address) {
                        ipAddress = ip.getHostAddress();
                    }
                }
            }
        } catch (SocketException e) {
            e.printStackTrace();
        }
        return ipAddress;
    }


    private String getLocalAddressIndex(String devAddress) {
        if (!devAddress.equals("")) {
            return devAddress.substring(0, devAddress.lastIndexOf(".") + 1);
        }
        return null;
    }

}
