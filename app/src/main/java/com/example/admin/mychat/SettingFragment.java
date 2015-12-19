package com.example.admin.mychat;

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
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.PrintStream;
import java.net.Socket;
import java.net.UnknownHostException;

/**
 * 设置部分
 */
public class SettingFragment extends Fragment {
    CharSequence id,name,ipStr,portStr,sign;
    SharedPreferences settings;
    TextView previd,prevName,prevSign;
    EditText newName,newSign;

    static final int SUCCESS_LINK = 1;
    static final int FAIL_LINK = 2;
    Handler mHandler = new Handler(Looper.getMainLooper()) {
        @Override
        public void handleMessage(Message message) {
            String info = (String)message.obj;
            switch (message.what){
                case SUCCESS_LINK:
                    //Toast.makeText(getActivity(),info,Toast.LENGTH_LONG).show();
                    id = "NULL";
                    previd.setText(id.toString());
                    SharedPreferences.Editor editor = settings.edit();
                    editor.putString("id",id.toString());
                    editor.commit();
                    Intent intent = new Intent();
                    intent.setClass(getActivity(), Login.class);
                    // 调用 Login
                    startActivity(intent);
                    // 结束 MainActivity
                    getActivity().finish();
                    break;

                case FAIL_LINK:
                    Toast.makeText(getActivity(),info,Toast.LENGTH_LONG).show();
                    break;
            }
        }
    };

    @Override
    public View onCreateView(LayoutInflater inflater,@Nullable ViewGroup container,@Nullable Bundle savedInstanceState){
        View view = inflater.inflate(R.layout.fragment_setting, container, false);
        /**
         * The method getSharedPreferences is a method of the Context object,
         * so just calling getSharedPreferences from a Fragment will not work.
         * because it is not a Context! (Activity is an extension of Context,
         * so we can call getSharedPreferences from it)
         */
        settings = this.getActivity().getSharedPreferences("setting", 0);
        id = settings.getString("id", "");
        name = settings.getString("name", "暂时没有昵称");
        ipStr = settings.getString("ip", "166.111.140.14");
        portStr = settings.getString("port","8000");
        sign = settings.getString("sign","暂时没有个性签名");
        /**
         * 显示需要用 view.findViewById()
         * */
        previd = (TextView) view.findViewById(R.id.previd);
        previd.setText(id.toString());

        prevName = (TextView) view.findViewById(R.id.prevName);
        prevName.setText(name.toString());

        prevSign = (TextView) view.findViewById(R.id.prevSign);
        prevSign.setText(sign.toString());

        newName = (EditText) view.findViewById(R.id.newName);
        newSign = (EditText) view.findViewById(R.id.newSign);

        /**
         * 退出登录
         * */
        Button logOut = (Button) view.findViewById(R.id.logOut);
        logOut.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                new Thread(new ConnectServer()).start();
            }
        });

        /**
         * 重置昵称
         * */
        Button setName = (Button) view.findViewById(R.id.setName);
        setName.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v) {
                SharedPreferences.Editor editor = settings.edit();
                CharSequence newNameStr = newName.getText();
                prevName.setText(newNameStr.toString());
                editor.putString("name", newNameStr.toString());
                editor.commit();
            }
        });

        /**
         * 重置个性签名
         * */
        Button setSign = (Button) view.findViewById(R.id.setSign);
        setSign.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v) {
                SharedPreferences.Editor editor = settings.edit();
                CharSequence newSignStr = newSign.getText();
                prevSign.setText(newSignStr.toString());
                editor.putString("sign", newSignStr.toString());
                editor.commit();
            }
        });

        return view;
    }


    public class ConnectServer implements Runnable{
        public void run() {
            try {
                Socket socket = new Socket(ipStr.toString(), Integer.parseInt(portStr.toString()));
                OutputStream os = socket.getOutputStream();
                PrintStream out = new PrintStream(os,true,"US-ASCII");
                BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream(),"US-ASCII"));

                String sendInfo = "logout"+id.toString();
                out.print(sendInfo);
                os.write((byte)0);

                char[] bytes = new char[30];
                int length = in.read(bytes);

                String rcv = new String(bytes);
                // 截取有意义的一段
                String rcvtrim = rcv.substring(0,length);
                System.out.print("From Server: "+rcvtrim);

                out.close();
                in.close();
                socket.close();

                Message message = Message.obtain();
                message.obj = rcvtrim;
                if (rcvtrim.equals("loo")) {
                    message.what = SUCCESS_LINK;
                } else {
                    message.what = FAIL_LINK;
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
