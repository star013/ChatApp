package com.example.admin.mychat;

import android.app.Activity;
import android.content.Intent;
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
import java.io.OutputStream;
import java.io.PrintStream;
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
    List<ChatInfo> chatInfoList = null;
    Button bt_send;
    EditText et_content;
    String friend_id;
    String friend_name;
    String friend_ip = null;
    String send_info;
    ChatInfo sendingChatInfo = null;
    ChatInfo comingChatInfo = null;

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

        listShow();

        /**
         * 发送消息
         * */
        bt_send.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                send_info = et_content.getText().toString();
                // 发送不为空的消息
                if (send_info.equals("")==false){
                    // 没有对方ip，则需要查询
                    if (friend_ip==null){
                        new Thread(new ConnectServer()).start();
                    }else{
                        /**
                         * 获取时间
                         * */
                        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd  hh:mm");
                        String date = simpleDateFormat.format(new Date());
                        sendingChatInfo = new ChatInfo(date,send_info,false);
                    }
                }
            }
        });

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
            String info = (String)message.obj;
            //Toast.makeText(ChatActivity.this,info,Toast.LENGTH_SHORT).show();
            switch (message.what){
                case SUCCESS_LINK_SERVER:
                    //Toast.makeText(ChatActivity.this,"succeed",Toast.LENGTH_SHORT).show();
                    friend_ip = info;
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
                    chatInfoList.add(sendingChatInfo);
                    listShow();
                    break;

                default:break;
            }
        }
    };

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
     *
     * */
    private class SendMessage implements Runnable{
        @Override
        public void run() {
            try {
                Socket socket = new Socket(friend_ip, 8000);
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
                message.obj ="succeed";
                message.what = SUCCESS_LINK_FRIEND;
                mHandler.sendMessage(message);


            } catch (UnknownHostException e) {
                Message message = Message.obtain();
                message.obj ="fail to link friends";
                message.what = FAIL_LINK_FRIEND;
                mHandler.sendMessage(message);
                e.printStackTrace();
            } catch (IOException e) {
                Message message = Message.obtain();
                message.obj ="fail to link friends";
                message.what = FAIL_LINK_FRIEND;
                mHandler.sendMessage(message);
                e.printStackTrace();
            }
        }
    }
    /**
     * 向 ListView 中添加数组信息 chatInfoList
     * */
    void listShow(){
        if (chatInfoList != null){
            list = (ListView) this.findViewById(R.id.friendList);
            myChatAdapter.loadData(chatInfoList);
            list.setAdapter(myChatAdapter);
        }
    }

}
