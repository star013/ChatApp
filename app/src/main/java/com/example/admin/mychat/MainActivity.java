package com.example.admin.mychat;

import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.graphics.Color;
import android.os.Environment;
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
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.OutputStream;
import java.io.PrintStream;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.List;

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
    String received_file_id = null;
    ChatInfo receivedChatInfo = null;
    AddrInfo receivedAddrInfo = null;
    FileInfo receivedFileInfo = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        SharedPreferences settings = getSharedPreferences("setting", 0);
        CharSequence id = settings.getString("id", "NULL");
        setHost_path();
        //Toast.makeText(this,host_file_path,Toast.LENGTH_LONG).show();
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
    private static final int FILE_MESSAGE = 3;
    private static final int REPLY_ADD_FRIEND = 5;
    private static final int WRONG_LINK = 6;
    private static final int FAIL_LINK = 7;
    private static final int FAIL_LINK_FRIEND = 8;
    private static final int SUCCESS_LINK = 9;
    private Handler handler = new Handler(Looper.getMainLooper()){
        @Override
        public void handleMessage(Message message) {
            switch (message.what){
                case ADD_FRIEND:
                    //Toast.makeText(getApplicationContext(),receivedAddrInfo.getId(),Toast.LENGTH_SHORT).show();
                    // 更新界面
                    AddFriendandRefreshAddrFragment();
                    // 包装要发送的消息
                    wrapUpSendAddrInfo();
                    // 发起 IP 查询
                    new Thread(new AskFriendOnServer(receivedAddrInfo.getId())).start();
                    break;

                case REPLY_ADD_FRIEND:
                    //Toast.makeText(getApplicationContext(),"收到："+receivedAddrInfo.getId(),Toast.LENGTH_SHORT).show();
                    // 更新界面
                    AddFriendandRefreshAddrFragment();
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
                    bundle.putSerializable("receivedChatInfo", receivedChatInfo);
                    intent.putExtras(bundle);
                    MainActivity.this.sendBroadcast(intent);
                    break;

                case FILE_MESSAGE:
                    // 成功接收到文件
                    Toast.makeText(getApplicationContext(),received_file_id+"成功向您发送了文件"+receivedFileInfo.getFile_name(),Toast.LENGTH_LONG).show();
                    break;

                case SUCCESS_LINK:
                    // 向对方发送自己的用户信息
                    String friend_ip = (String)message.obj;
                    //Toast.makeText(getActivity(),friend_ip,Toast.LENGTH_SHORT).show();
                    new Thread(new SendToNewFriend(friend_ip)).start();
                    break;
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
                if (msg.equals("ADD_FRIEND")){
                    receivedAddrInfo = (AddrInfo)objIn.readObject();
                    message.what = ADD_FRIEND;
                    handler.sendMessage(message);
                }

                /**
                 * 当接收到的信息是
                 * 回复请求添加好友
                 * */
                if (msg.equals("REPLY_ADD_FRIEND")){
                    receivedAddrInfo = (AddrInfo)objIn.readObject();
                    message.what = REPLY_ADD_FRIEND;
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
                    received_file_id = (String) objIn.readObject();
                    receivedFileInfo = (FileInfo)objIn.readObject();
                    receivedFileInfo.setFile_path(host_file_path+receivedFileInfo.getFile_name());
                    receivedFileInfo.storeBytesToFile();
                    message.what = FILE_MESSAGE;
                    handler.sendMessage(message);
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
     * 获取存放收到文件的地址路径
     */
    private static String host_file_path = null;
    void setHost_path(){
        if (Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED)){
            // 如果SD卡存在，则存在SD卡中
            host_file_path = Environment.getExternalStorageDirectory() + File.separator + "MyChat";
            File file = new File(host_file_path);
            if (!file.exists()){
                file.mkdir();
            }
            host_file_path = host_file_path + File.separator + "File" ;
            file = new File(host_file_path);
            if (!file.exists()){
                file.mkdir();
            }
            host_file_path = host_file_path + File.separator;
        }else{
            // 如果SD卡不存在，则存在内存中
            host_file_path = this.getFilesDir().toString() + File.separator + "MyChat";
            File file = new File(host_file_path);
            if (!file.exists()){
                file.mkdir();
            }
            host_file_path = host_file_path + File.separator + "File" ;
            file = new File(host_file_path);
            if (!file.exists()){
                file.mkdir();
            }
            host_file_path = host_file_path + File.separator;
        }
    }

    /**
     * 向 AddressFragment 添加元素
     */
    void AddFriendandRefreshAddrFragment(){
        // 先把图片保存到文件夹中，并将AddrInfo的路径项配置正确，同时删除图片，节约内存
        Bitmap bitmap = receivedAddrInfo.getAvatar();
        String friend_id = receivedAddrInfo.getId();
        String path = null;
        if (Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED)){
            // 如果SD卡存在，则存在SD卡中
            path = Environment.getExternalStorageDirectory() + File.separator + "MyChat" + File.separator + "Avatar" + File.separator+friend_id;
            //Toast.makeText(this,"SD:"+path,Toast.LENGTH_LONG).show();
        }else{
            // 如果SD卡不存在，则存在内存中
            path = this.getFilesDir().toString() + File.separator + "MyChat" + File.separator + "Avatar" + File.separator+friend_id;
            //Toast.makeText(this,"Mem:"+path,Toast.LENGTH_LONG).show();
        }
        // 配置路径项，同时删除图片
        receivedAddrInfo.setAvatar(null);
        receivedAddrInfo.setAvatar_path(path);

        // 保存图片到指定位置
        File bitmapFile = new File(path);
        if (bitmapFile.exists()){
            bitmapFile.delete();
        }
        /**
         * Bitmap 不可序列化
         * 可以直接用文件保存
         */
        try {
            bitmapFile.createNewFile();
            FileOutputStream fos = new FileOutputStream(bitmapFile);
            bitmap.compress(Bitmap.CompressFormat.PNG,0,fos);
            fos.flush();
            fos.close();
            //Toast.makeText(this,"bitmap height = "+String.valueOf(bitmap.getHeight()),Toast.LENGTH_LONG).show();
        }catch (IOException e){
            e.printStackTrace();
        }

        //Toast.makeText(this,path,Toast.LENGTH_SHORT).show();
        File file = new File(this.getFilesDir(),"address.txt");
        List<AddrInfo> addrInfoList = null;
        try {
            ObjectInputStream objIn = new ObjectInputStream(new FileInputStream(file));
            addrInfoList = (ArrayList<AddrInfo>)objIn.readObject();
            objIn.close();
        }catch (IOException e){
            e.printStackTrace();
        }catch (ClassNotFoundException e){
            e.printStackTrace();
        }
        if (addrInfoList == null){
            addrInfoList = new ArrayList<AddrInfo>();
        }
        // 先查找有无重复 ID， 如果没有则添加新的，如果有则更新
        if (addrInfoList.size()==0){
            addrInfoList.add(receivedAddrInfo);
        }else {
            boolean repeat = false;
            for (int i=0; i<addrInfoList.size(); i++){
                AddrInfo a = addrInfoList.get(i);
                if (a.getId().equals(friend_id)){
                    repeat = true;
                    a.setName(receivedAddrInfo.getName());
                    a.setSign(receivedAddrInfo.getSign());
                    break;
                }
                if (!repeat){
                    addrInfoList.add(receivedAddrInfo);
                }
            }
        }
        try {
            ObjectOutputStream objOut = new ObjectOutputStream(new FileOutputStream(file));
            objOut.writeObject(addrInfoList);
            objOut.close();
        }catch (IOException e) {
            e.printStackTrace();
        }

        if (addressFragment != null){
            final FragmentTransaction ft = getSupportFragmentManager().beginTransaction();
            ft.detach(addressFragment);
            ft.attach(addressFragment);
            ft.commit();
        }
    }


    /**
     * 开启新线程处理网络连接
     * */
    private class AskFriendOnServer implements Runnable{
        private String friend_id;

        public AskFriendOnServer(String friend_id){
            this.friend_id = friend_id;
        }
        public void run() {
            String ipStr = settings.getString("ip", "166.111.140.14");
            String portStr = settings.getString("port","8000");
            try {
                Socket socket = new Socket(ipStr.toString(), Integer.parseInt(portStr.toString()));
                OutputStream os = socket.getOutputStream();
                PrintStream out = new PrintStream(os,true,"US-ASCII");
                BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream(),"US-ASCII"));

                String sendInfo = "q"+friend_id;
                out.print(sendInfo);
                os.write((byte)0);

                char[] bytes = new char[50];
                int length = in.read(bytes);

                String rcv = new String(bytes);
                // 截取有意义的一段
                String rcvtrim = rcv.substring(0,length);

                out.close();
                in.close();
                socket.close();

                Message message = Message.obtain();
                message.obj = rcvtrim;
                if (rcvtrim.equals("n")) {
                    message.what = FAIL_LINK;
                } else if (rcvtrim.equals("Incorrect No.") || rcvtrim.equals("Please send the correct message.")){
                    message.what = WRONG_LINK;
                } else {
                    message.what = SUCCESS_LINK;
                }
                handler.sendMessage(message);

            } catch (UnknownHostException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }


    // 发送回去的自己的消息
    AddrInfo sendingAddrInfo = null;
    String myAvatarPath;
    /**
     * 包装好要发送的信息 sendingAddrInfo
     * */
    private void wrapUpSendAddrInfo(){
        Bitmap bitmap = null;
        myAvatarPath = settings.getString("myAvatarPath",null);
        // 加载头像
        if (myAvatarPath!=null){
            bitmap = BitmapFactory.decodeFile(myAvatarPath);
        }else {
            Resources resources = getResources();
            bitmap = BitmapFactory.decodeResource(resources,R.drawable.stranger_avatar);
        }
        // 签名 和 昵称有可能更新
        String id = settings.getString("id","");
        String sign = settings.getString("sign","暂时没有个性签名");
        String name = settings.getString("name", "暂时没有昵称");
        sendingAddrInfo = new AddrInfo(id.toString(),name.toString(),sign.toString(),bitmap,"");
    }
    /**
     * 向已经获得 IP 地址的好友发送添加信息
     */
    private class SendToNewFriend implements Runnable{
        private String friend_ip;
        public SendToNewFriend(String friend_ip){
            this.friend_ip = friend_ip;
        }
        @Override
        public void run() {
            Message message = Message.obtain();
            // 包装好要发送的信息 sendingAddrInfo
            wrapUpSendAddrInfo();
            try {
                Socket socket = new Socket(friend_ip, 8000);
                ObjectOutputStream objOut = new ObjectOutputStream(socket.getOutputStream());
                // 回复添加好友请求 标识符
                String str = new String("REPLY_ADD_FRIEND");
                objOut.writeObject(str);
                // 聊天消息对象
                objOut.writeObject(sendingAddrInfo);

                objOut.close();
                socket.close();
            } catch (UnknownHostException e) {
                message.what = FAIL_LINK_FRIEND;
                message.obj = "UnknownHostException";
                handler.sendMessage(message);
                e.printStackTrace();
            } catch (IOException e) {
                message.what = FAIL_LINK_FRIEND;
                message.obj = "IOException";
                handler.sendMessage(message);
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
