package com.example.admin.mychat;


import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ListView;
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
import java.net.Socket;
import java.net.UnknownHostException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Iterator;
import java.util.List;

/**
 * 通讯录部分
 */
public class AddressFragment extends Fragment {
    CharSequence id,name,friendIDstr,ipStr,portStr;
    SharedPreferences settings;
    EditText friendID;
    ListView list;
    View view;
    List<AddrInfo> addrInfoList = new ArrayList<AddrInfo>();
    MyAddrAdapter myAddrAdapter = null;

    @Override
    public View onCreateView(LayoutInflater inflater,@Nullable ViewGroup container,@Nullable Bundle savedInstanceState){
        view = inflater.inflate(R.layout.fragment_address, container, false);

        settings = this.getActivity().getSharedPreferences("setting", 0);
        id = settings.getString("id", "");
        name = settings.getString("name", "暂时没有昵称");
        ipStr = settings.getString("ip", "166.111.140.14");
        portStr = settings.getString("port","8000");

        /**
         * 打开文件
         * */
        File file = new File(getActivity().getFilesDir(),"address.txt");

        /*
        ObjectOutputStream objOut = null;
        AddrInfo a1 = new AddrInfo("2013011466","金晟","主席");
        AddrInfo a2 = new AddrInfo("2013011183","金星宇","keep moving");
        addrInfoList.add(a1);
        addrInfoList.add(a2);
        try {
            objOut = new ObjectOutputStream(new FileOutputStream(file));
            objOut.writeObject(addrInfoList);
            objOut.close();
        }catch (IOException e){
            e.printStackTrace();
        }
        */
        ObjectInputStream objIn = null;
        try {
            objIn = new ObjectInputStream(new FileInputStream(file));
            addrInfoList = (ArrayList<AddrInfo>)objIn.readObject();
            objIn.close();
        }catch (IOException e){
            e.printStackTrace();
        }catch (ClassNotFoundException e){
            e.printStackTrace();
        }
        /**
         * 向 ListView 中添加数组信息 addrInfoList
         * */
        myAddrAdapter = new MyAddrAdapter(getActivity());
        listShow();

        /**
         * ListView Item 点击事件：打开聊天窗口并显示聊天记录（如果有聊天记录的话）
         * */
        list.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                AddrInfo a = addrInfoList.get(position);
                String friend_id = a.getId();
                String friend_name = a.getName();
                Intent intent =new Intent(getActivity(),ChatActivity.class);
                /**
                 * activity 之间传递信息
                 * */
                intent.putExtra("friend_id",friend_id);
                intent.putExtra("friend_name",friend_name);
                intent.putExtra("my_id",AddressFragment.this.id.toString());
                startActivity(intent);
            }
        });


        /**
         * 添加好友
         * */
        Button addFriend = (Button) view.findViewById(R.id.addFriend);
        addFriend.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
                builder.setTitle("添加好友");
                builder.setMessage("请输入好友账号：");
                // 放置输入栏
                friendID = new EditText(getActivity());
                LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT,LinearLayout.LayoutParams.MATCH_PARENT);
                friendID.setLayoutParams(lp);
                builder.setView(friendID);

                builder.setPositiveButton("添加", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        new Thread(new ConnectServer()).start();
                        dialog.dismiss();
                    }
                });
                builder.setNegativeButton("取消", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                    }
                });
                builder.create().show();

            }
        });

        /**
         * 刷新好友状态
         * */
        Button refreshFriend = (Button) view.findViewById(R.id.refreshFriend);
        refreshFriend.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                int i = Integer.parseInt("0123");
                Toast.makeText(getActivity(),String.valueOf(i),Toast.LENGTH_SHORT).show();
            }
        });

        return view;
    }

    /**
     * 向 ListView 中添加数组信息 addrInfoList
     * */
    void listShow(){
        list = (ListView) view.findViewById(R.id.friendList);
        myAddrAdapter.loadData(addrInfoList);
        list.setAdapter(myAddrAdapter);
    }

    private static final int SUCCESS_LINK = 21;
    private static final int FAIL_LINK = 22;
    private static final int WRONG_LINK = 23;
    /**
     * 处理网络线程的消息
     * */
    private Handler mHandler = new Handler(Looper.getMainLooper()) {
        @Override
        public void handleMessage(Message message) {
            String info = (String)message.obj;
            switch (message.what){
                case SUCCESS_LINK:
                    Toast.makeText(getActivity(),info,Toast.LENGTH_SHORT).show();
                    break;

                case FAIL_LINK:
                    Toast.makeText(getActivity(), "您的朋友不在线，请稍后再添加", Toast.LENGTH_SHORT).show();
                    break;

                case WRONG_LINK:
                    Toast.makeText(getActivity(), "您输入的账号不存在", Toast.LENGTH_SHORT).show();
                    break;
                default:break;
            }
        }
    };

    /**
     * 开启新线程处理网络连接
     * */
    public class ConnectServer implements Runnable{
        public void run() {
            try {
                Socket socket = new Socket(ipStr.toString(), Integer.parseInt(portStr.toString()));
                OutputStream os = socket.getOutputStream();
                PrintStream out = new PrintStream(os,true,"US-ASCII");
                BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream(),"US-ASCII"));

                friendIDstr = friendID.getText();
                String sendInfo = "q"+friendIDstr.toString();
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
                mHandler.sendMessage(message);

            } catch (UnknownHostException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
}
