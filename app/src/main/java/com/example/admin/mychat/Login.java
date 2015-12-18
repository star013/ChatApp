package com.example.admin.mychat;

import android.app.Activity;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.PrintStream;
import java.io.PrintWriter;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Login extends Activity implements View.OnClickListener{
    CharSequence ipStr,portStr,nameStr,pwdStr;
    Button blogin,bset;
    TextView prevIP,prevPort;
    EditText name,password,ip,port;
    SharedPreferences settings;
    static final int SUCCESS_LINK = 1;
    static final int FAIL_LINK = 2;
    Handler mHandler = new Handler(Looper.getMainLooper()) {
        @Override
        public void handleMessage(Message message) {
            String info = (String)message.obj;
            Toast.makeText(Login.this,info,Toast.LENGTH_LONG).show();
            switch (message.what){
                case SUCCESS_LINK:
                    //Toast.makeText(Login.this,info,Toast.LENGTH_LONG).show();
                    // 将登录的用户名储存起来
                    SharedPreferences.Editor editor = settings.edit();
                    editor.putString("name",nameStr.toString());
                    editor.commit();
                    break;

                case FAIL_LINK:
                    Toast.makeText(Login.this,info,Toast.LENGTH_LONG).show();
                    break;
            }
        }
    };


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        settings = getSharedPreferences("setting", 0);

        ipStr = settings.getString("ip", "166.111.140.14");
        portStr = settings.getString("port","8000");


        prevIP = (TextView) findViewById(R.id.prevIP);
        prevPort = (TextView) findViewById(R.id.prevPort);
        prevIP.setText("Server IP:\n"+ipStr);
        prevPort.setText("Server Port:\n"+portStr);

        ip = (EditText) findViewById(R.id.loginIP);
        port = (EditText) findViewById(R.id.loginPort);
        bset = (Button) findViewById(R.id.loginSet);


        name = (EditText) findViewById(R.id.loginUsername);
        password = (EditText) findViewById(R.id.loginPassword);
        blogin = (Button) findViewById(R.id.loginButton);

        bset.setOnClickListener(this);
        blogin.setOnClickListener(this);
    }


    @Override
    public void onClick(View v) {
        SharedPreferences.Editor editor = settings.edit();
        switch (v.getId()){
            case R.id.loginSet:
                CharSequence tempipStr = ip.getText();
                CharSequence tempPortStr = port.getText();
                Pattern ipPat = Pattern.compile("\\d{1,3}\\.\\d{1,3}\\.\\d{1,3}\\.\\d{1,3}");
                Pattern portPat = Pattern.compile("\\d{1,5}");
                Matcher match = ipPat.matcher(tempipStr);
                //判断IP地址和端口是否符合要求
                if (match.matches()){
                    prevIP.setText("Server IP:\n"+tempipStr);
                    editor.putString("ip", tempipStr.toString());
                    editor.commit();
                    ipStr = tempipStr;
                }else{
                    Toast.makeText(Login.this,"New IP is NOT valid!\nPlease input valid IP again",Toast.LENGTH_LONG).show();
                }
                match = portPat.matcher(tempPortStr);
                if (match.matches()){
                    prevPort.setText("Server Port:\n"+tempPortStr);
                    editor.putString("port", tempPortStr.toString());
                    editor.commit();
                    portStr = tempPortStr;
                }else{
                    Toast.makeText(Login.this,"New Port is NOT valid!\nPlease input valid Port again",Toast.LENGTH_LONG).show();
                }

                break;
            case R.id.loginButton:
                nameStr = name.getText();
                pwdStr = password.getText();
                new Thread(new ConnectServer()).start();

                break;
        }
    }


    public class ConnectServer implements Runnable{
        public void run() {
            try {
                Socket socket = new Socket(ipStr.toString(), Integer.parseInt(portStr.toString()));
                OutputStream os = socket.getOutputStream();
                PrintStream out = new PrintStream(os,true,"US-ASCII");
                BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream(),"US-ASCII"));

                String sendInfo = nameStr.toString() + "_" + pwdStr.toString();
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
                if (rcvtrim.equals("lol")) {
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
