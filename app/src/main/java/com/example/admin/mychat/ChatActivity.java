package com.example.admin.mychat;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.Window;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.Toast;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.OutputStream;
import java.io.PrintStream;
import java.io.PrintWriter;
import java.net.Socket;
import java.net.UnknownHostException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class ChatActivity extends Activity {
    ListView list;
    MyChatAdapter myChatAdapter;
    List<ChatInfo> chatInfoList = new ArrayList<ChatInfo>();
    Button bt_send;
    EditText et_content;
    String friend_id;
    String friend_name;
    String my_id;
    String friend_ip = null;
    String send_info;
    ChatInfo sendingChatInfo = null;
    ChatInfo receivedChatInfo = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat);
        bt_send = (Button)findViewById(R.id.bt_send);
        et_content = (EditText)findViewById(R.id.et_send);
        /**
         * 接收 ChatFragment 的信息
         * */
        Intent intent = getIntent();
        friend_id = intent.getStringExtra("friend_id");
        friend_name = intent.getStringExtra("friend_name");
        getActionBar().setTitle(friend_name);
        my_id = intent.getStringExtra("my_id");
        //Toast.makeText(this,my_id,Toast.LENGTH_SHORT).show();

        /**
         * 打开已有的聊天记录
         * */
        File file = new File(this.getFilesDir(),friend_id);
        ObjectInputStream objIn = null;
        try {
            objIn = new ObjectInputStream(new FileInputStream(file));
            chatInfoList = (ArrayList<ChatInfo>)objIn.readObject();
            objIn.close();
        }catch (IOException e){
            e.printStackTrace();
        }catch (ClassNotFoundException e){
            e.printStackTrace();
        }


        /**
         * 创建接口
         * 不容忽视
         * */
        list = (ListView)findViewById(R.id.chatList);
        //chatInfoList.add(new ChatInfo("1", "hi\nhello\ni am chair man oooooooh yeahhhhhhhhh\nhahaha", true));
        //chatInfoList.add(new ChatInfo("2","jin", false));
        //Toast.makeText(this,"list size="+String.valueOf(chatInfoList.size()),Toast.LENGTH_SHORT).show();
        if (chatInfoList.size()>0){
            myChatAdapter = new MyChatAdapter(this,chatInfoList);
            list.setAdapter(myChatAdapter);
        }

        /**
         * 注册广播
         */
        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(friend_id);
        registerReceiver(new TextBroadcastReceiver(),intentFilter);

        /**
         * 发送消息
         * */
        bt_send.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                send_info = et_content.getText().toString();
                // 发送不为空的消息
                if (send_info.equals("") == false) {
                    // 没有对方ip，则需要查询
                    if (friend_ip == null) {
                        new Thread(new ConnectServer()).start();
                    } else {
                        // 发送消息
                        new Thread(new SendMessage()).start();
                    }
                }
            }
        });

    }

    /**
     * 处理接收到的广播的信息
     * 处理文字信息
     */
    public class TextBroadcastReceiver extends BroadcastReceiver{

        @Override
        public void onReceive(Context context, Intent intent) {
            receivedChatInfo = (ChatInfo)intent.getSerializableExtra("receivedChatInfo");
            chatInfoList.add(receivedChatInfo);
            if (chatInfoList.size()==1){
                myChatAdapter = new MyChatAdapter(ChatActivity.this,chatInfoList);
                list.setAdapter(myChatAdapter);
            }else{
                myChatAdapter.notifyDataSetChanged();
            }
        }
    }

    static final int SUCCESS_LINK_SERVER = 1;
    static final int FAIL_LINK_SERVER = 2;
    static final int FAIL_LINK_FRIEND = 3;
    static final int SUCCESS_LINK_FRIEND = 4;
    /**
     * 处理网络线程的消息
     * */
    private Handler mHandler = new Handler(Looper.getMainLooper()) {
        @Override
        public void handleMessage(Message message) {
            switch (message.what){
                case SUCCESS_LINK_SERVER:
                    String info = (String)message.obj;
                    //Toast.makeText(ChatActivity.this,info,Toast.LENGTH_SHORT).show();
                    friend_ip = info;
                    // 发送消息
                    new Thread(new SendMessage()).start();
                    break;

                case FAIL_LINK_SERVER:
                    Toast.makeText(ChatActivity.this, "您的朋友不在线，请稍后再联系", Toast.LENGTH_SHORT).show();
                    friend_ip = null;
                    break;

                case FAIL_LINK_FRIEND:
                    Toast.makeText(ChatActivity.this,"发送失败，请重试",Toast.LENGTH_SHORT).show();
                    friend_ip = null;
                    break;

                case SUCCESS_LINK_FRIEND:
                    //Toast.makeText(ChatActivity.this,"succeed send",Toast.LENGTH_SHORT).show();
                    et_content.setText("");
                    chatInfoList.add(sendingChatInfo);
                    if (chatInfoList.size()==1){
                        myChatAdapter = new MyChatAdapter(ChatActivity.this,chatInfoList);
                        list.setAdapter(myChatAdapter);
                    }else{
                        myChatAdapter.notifyDataSetChanged();
                    }

                    break;

                default:break;
            }
        }
    };

    /**
     * 包装好要发送的信息 sendingChatInfo
     * */
    void warpUpSendMessage(){
        /**
         * 获取时间
         * */
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd  hh:mm:ss");
        String date = simpleDateFormat.format(new Date());
        sendingChatInfo = new ChatInfo(date,send_info,false);
    }

    /**
     * 向服务器查询，获取对方的ip地址
     * */
    private class ConnectServer implements Runnable{
        public void run() {
            SharedPreferences settings = getSharedPreferences("setting", 0);
            String ipStr = settings.getString("ip", "166.111.140.14");
            String portStr = settings.getString("port","8000");
            try {
                Socket socket = new Socket(ipStr.toString(), Integer.parseInt(portStr.toString()));
                OutputStream os = socket.getOutputStream();
                PrintStream out = new PrintStream(os,true,"US-ASCII");
                BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream(),"US-ASCII"));

                // 查询 id 号
                String sendInfo = "q"+friend_id;
                out.print(sendInfo);
                os.write((byte)0);

                char[] bytes = new char[30];
                int length = in.read(bytes);

                String rcv = new String(bytes);
                // 截取有意义的一段
                String rcvtrim = rcv.substring(0,length);

                out.close();
                in.close();
                socket.close();

                Message message = Message.obtain();
                message.obj = rcvtrim;
                Pattern p = Pattern.compile("\\d{1,3}\\.\\d{1,3}\\.\\d{1,3}\\.\\d{1,3}");
                Matcher m = p.matcher(rcvtrim);

                if (m.matches()) {
                    message.what = SUCCESS_LINK_SERVER;
                } else {
                    message.what = FAIL_LINK_SERVER;
                }
                mHandler.sendMessage(message);

            } catch (UnknownHostException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    /**
     * 向好友发送信息
     * */
    private class SendMessage implements Runnable{
        @Override
        public void run() {
            Message message = Message.obtain();
            // 包装好要发送的信息 sendingChatInfo
            warpUpSendMessage();
            try {
                Socket socket = new Socket(friend_ip, 8000);
                ObjectOutputStream objOut = new ObjectOutputStream(socket.getOutputStream());
                // 文字消息标识符
                String str = new String("TEXT_MESSAGE");
                objOut.writeObject(str);
                // 自己的ID
                objOut.writeObject(my_id);
                // 聊天消息对象
                objOut.writeObject(sendingChatInfo);

                objOut.close();
                socket.close();

                message.obj ="succeed";
                message.what = SUCCESS_LINK_FRIEND;
                mHandler.sendMessage(message);
            } catch (UnknownHostException e) {
                message.obj ="fail to link friends";
                message.what = FAIL_LINK_FRIEND;
                mHandler.sendMessage(message);
                e.printStackTrace();
            } catch (IOException e) {
                message.obj ="fail to link friends";
                message.what = FAIL_LINK_FRIEND;
                mHandler.sendMessage(message);
                e.printStackTrace();
            }
        }
    }

}
