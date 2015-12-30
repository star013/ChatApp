package com.example.admin.mychat;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.graphics.Color;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.provider.Telephony;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.LinearLayout;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.ObjectInputStream;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;

public class MainActivity extends FragmentActivity implements OnClickListener {
    SharedPreferences settings;
    // 底部菜单3个Linearlayout
    private LinearLayout ll_chat;
    private LinearLayout ll_address;
    private LinearLayout ll_setting;

    // 底部菜单3个ImageView
    private ImageView iv_chat;
    private ImageView iv_address;
    private ImageView iv_setting;

    // 底部菜单3个菜单标题
    private TextView tv_chat;
    private TextView tv_address;
    private TextView tv_setting;

    // 3个Fragment
    private ChatFragment chatFragment;
    private AddressFragment addressFragment;
    private SettingFragment settingFragment;

    // 接收到的消息
    String receivedID = null;
    ChatInfo receivedChatInfo = null;
    AddrInfo receivedAddrInfo = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        SharedPreferences settings = getSharedPreferences("setting", 0);
        CharSequence id = settings.getString("id", "NULL");
        if (id.equals("NULL")){
            // name == NULL  no user log in
            Intent intent = new Intent();
            intent.setClass(MainActivity.this,Login.class);
            /*调用Login Activity*/
            startActivity(intent);
            // 结束MainActivity
            MainActivity.this.finish();
        }else
        {
            setContentView(R.layout.activity_main);

            // 初始化控件
            initView();
            // 初始化底部按钮事件
            initEvent();
            // 初始化并设置当前Fragment
            initFragment(0);
            // 初始化侦听socket
            new Thread(new StartReceiver()).start();
        }
    }

    private static final int ADD_FRIEND = 1;
    private static final int TEXT_MESSAGE = 2;
    private Handler handler = new Handler(Looper.getMainLooper()){
        @Override
        public void handleMessage(Message message) {
            switch (message.what){
                case ADD_FRIEND:
                    Toast.makeText(getApplicationContext(),receivedAddrInfo.getId(),Toast.LENGTH_SHORT).show();
                    Toast.makeText(getApplicationContext(),receivedAddrInfo.getName(),Toast.LENGTH_SHORT).show();
                    break;

                case TEXT_MESSAGE:
                    //Toast.makeText(getApplicationContext(),receivedChatInfo.getDate(),Toast.LENGTH_SHORT).show();
                    //Toast.makeText(getApplicationContext(),receivedChatInfo.getText(),Toast.LENGTH_SHORT).show();
                    /**
                     * 已收到的对方的 ID 作为广播的标识符
                     * 用 Bundle 传递对象
                     */
                    receivedChatInfo.setIsCome(true);
                    Intent intent = new Intent(receivedID);
                    Bundle bundle=new Bundle();
                    bundle.putSerializable("receivedChatInfo",receivedChatInfo);
                    intent.putExtras(bundle);
                    MainActivity.this.sendBroadcast(intent);
                default:break;
            }
        }
    };


    /**
     * socket 接收端
     * Created by admin on 2015/12/20.
     */
    public class Receiver extends Thread{
        Socket socket;
        public Receiver(Socket socket){
            this.socket = socket;
        }

        public void run(){
            try{
                Message message = Message.obtain();
                ObjectInputStream objIn = new ObjectInputStream(socket.getInputStream());
                String msg = (String)objIn.readObject();
                /**
                 * 当接收到的信息是
                 * 请求添加好友
                 * */
                if (msg.equals("ADD_FRIENDS")){
                    receivedAddrInfo = (AddrInfo)objIn.readObject();
                    message.what = ADD_FRIEND;
                    handler.sendMessage(message);
                }

                /**
                 * 当接收到的信息是
                 * 文字消息
                 * */
                if (msg.equals("TEXT_MESSAGE")){
                    receivedID = (String)objIn.readObject();
                    receivedChatInfo = (ChatInfo)objIn.readObject();
                    message.what = TEXT_MESSAGE;
                    handler.sendMessage(message);
                }

                /**
                 * 当接收到的信息是
                 * 文件
                 * */
                if (msg.equals("FILE_MESSAGE")){

                }

                objIn.close();
                socket.close();
            }catch (IOException e){
                e.printStackTrace();
            } catch (ClassNotFoundException e) {
                e.printStackTrace();
            }
        }
    }

    /**
     * 初始化侦听socket
     * 可以针对多个客户端连接进行侦听
     * */
    public class StartReceiver implements Runnable{
        public void run(){
            settings = getSharedPreferences("setting", 0);
            CharSequence idStr = settings.getString("id", "8000");
            String portStr = idStr.toString().substring(idStr.length() - 4, idStr.length());
            try{
                //ServerSocket serverSocket = new ServerSocket(Integer.parseInt(portStr));
                ServerSocket serverSocket = new ServerSocket(8000);
                while (true){
                    Receiver receiver = new Receiver(serverSocket.accept());
                    receiver.start();
                }
            }catch (IOException e){
                e.printStackTrace();
            }
        }
    }

    private void initFragment(int index) {
        // 使用 Support Library API 调用 getSupportFragmentManager() 以获取 FragmentManager
        FragmentManager fragmentManager = getSupportFragmentManager();
        // 然后调用 beginTransaction() 创建 FragmentTransaction，同时调用 add() 添加 Fragment
        FragmentTransaction transaction = fragmentManager.beginTransaction();
        // 隐藏所有Fragment
        hideFragment(transaction);
        switch (index) {
            case 0:
                if (chatFragment == null) {
                    chatFragment = new ChatFragment();
                    transaction.add(R.id.fl_content, chatFragment);
                } else {
                    transaction.show(chatFragment);
                }
                break;
            case 1:
                if (addressFragment == null) {
                    addressFragment = new AddressFragment();
                    transaction.add(R.id.fl_content, addressFragment);
                } else {
                    transaction.show(addressFragment);
                }
                break;
            case 2:
                if (settingFragment == null) {
                    settingFragment = new SettingFragment();
                    transaction.add(R.id.fl_content, settingFragment);
                } else {
                    transaction.show(settingFragment);
                }

                break;

            default:
                break;
        }

        // 提交事务
        transaction.commit();

    }

    //隐藏Fragment
    private void hideFragment(FragmentTransaction transaction) {
        if (chatFragment != null) {
            transaction.hide(chatFragment);
        }
        if (addressFragment != null) {
            transaction.hide(addressFragment);
        }
        if (settingFragment != null) {
            transaction.hide(settingFragment);
        }

    }

    private void initEvent() {
        // 设置按钮监听
        ll_chat.setOnClickListener(this);
        ll_address.setOnClickListener(this);
        ll_setting.setOnClickListener(this);

    }

    private void initView() {

        // 底部菜单3个Linearlayout
        this.ll_chat = (LinearLayout) findViewById(R.id.ll_chat);
        this.ll_address = (LinearLayout) findViewById(R.id.ll_address);
        this.ll_setting = (LinearLayout) findViewById(R.id.ll_setting);

        // 底部菜单3个ImageView
        this.iv_chat = (ImageView) findViewById(R.id.iv_chat);
        this.iv_address = (ImageView) findViewById(R.id.iv_address);
        this.iv_setting = (ImageView) findViewById(R.id.iv_setting);

        // 底部菜单3个菜单标题
        this.tv_chat = (TextView) findViewById(R.id.tv_chat);
        this.tv_address = (TextView) findViewById(R.id.tv_address);
        this.tv_setting = (TextView) findViewById(R.id.tv_setting);

    }

    @Override
    public void onClick(View v) {

        // 在每次点击后将所有的底部按钮(ImageView,TextView)颜色改为灰色，然后根据点击着色
        restartBotton();
        // ImageView和TetxView置为绿色，页面随之跳转
        switch (v.getId()) {
            case R.id.ll_chat:
                iv_chat.setImageResource(R.drawable.chat_pressed);
                tv_chat.setTextColor(Color.rgb(16,192,16));
                initFragment(0);
                break;
            case R.id.ll_address:
                iv_address.setImageResource(R.drawable.addrlist_pressed);
                tv_address.setTextColor(Color.rgb(16,192,16));
                initFragment(1);
                break;
            case R.id.ll_setting:
                iv_setting.setImageResource(R.drawable.setting_pressed);
                tv_setting.setTextColor(Color.rgb(16,192,16));
                initFragment(2);
                break;

            default:
                break;
        }

    }

    private void restartBotton() {
        // ImageView置为灰色
        iv_chat.setImageResource(R.drawable.chat);
        iv_address.setImageResource(R.drawable.addrlist);
        iv_setting.setImageResource(R.drawable.setting);
        // TextView置为灰色
        tv_chat.setTextColor(Color.rgb(192, 192, 192));
        tv_address.setTextColor(Color.rgb(192,192,192));
        tv_setting.setTextColor(Color.rgb(192,192,192));
    }



}
