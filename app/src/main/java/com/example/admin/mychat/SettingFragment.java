package com.example.admin.mychat;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.provider.MediaStore;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import java.io.BufferedReader;
import java.io.File;
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
    ImageView myAvatar;

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
        String myAvatarPath = settings.getString("myAvatarPath",null);
        /**
         * 显示需要用 view.findViewById()
         * */
        previd = (TextView) view.findViewById(R.id.previd);
        previd.setText("账号:"+id.toString());

        prevName = (TextView) view.findViewById(R.id.prevName);
        prevName.setText("昵称:"+name.toString());

        prevSign = (TextView) view.findViewById(R.id.prevSign);
        prevSign.setText(sign.toString());

        // 加载头像
        myAvatar = (ImageView) view.findViewById(R.id.avatar);
        if (myAvatarPath!=null){
            Bitmap bitmap = BitmapFactory.decodeFile(myAvatarPath);
            if (bitmap != null) {
                myAvatar.setImageBitmap(bitmap);
            }
        }


        /**
         * 退出登录
         * */
        Button logOut = (Button) view.findViewById(R.id.logOut);
        logOut.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
                builder.setTitle("友情提示");
                builder.setMessage("确认退出登录吗？");
                builder.setPositiveButton("确认", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                        new Thread(new ConnectServer()).start();
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
         * 修改昵称
         * */
        Button setName = (Button) view.findViewById(R.id.setName);
        setName.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v) {
                AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
                builder.setTitle("修改昵称");
                builder.setMessage("请输入新的昵称：");
                // 放置输入栏
                newName = new EditText(getActivity());
                CharSequence newNameStr = newName.getText();
                LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT,LinearLayout.LayoutParams.MATCH_PARENT);
                newName.setLayoutParams(lp);
                builder.setView(newName);

                builder.setPositiveButton("确定", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        CharSequence newNameStr = newName.getText();
                        if (newNameStr.length()<10){
                            SharedPreferences.Editor editor = settings.edit();
                            prevName.setText("昵称:"+newNameStr.toString());
                            editor.putString("name", newNameStr.toString());
                            editor.commit();
                        }else{
                            Toast.makeText(getActivity(),"您输入的昵称过长\n请输入长度小于10个字符的昵称",Toast.LENGTH_SHORT).show();
                        }
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
         * 重置个性签名
         * */
        Button setSign = (Button) view.findViewById(R.id.setSign);
        setSign.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v) {
                AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
                builder.setTitle("修改个性签名");
                builder.setMessage("请输入新的个性签名：");
                // 放置输入栏
                newSign = new EditText(getActivity());
                CharSequence newNameStr = newSign.getText();
                LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT,LinearLayout.LayoutParams.MATCH_PARENT);
                newSign.setLayoutParams(lp);
                builder.setView(newSign);

                builder.setPositiveButton("确定", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        CharSequence newSignStr = newSign.getText();
                        if (newSignStr.length()<20){
                            SharedPreferences.Editor editor = settings.edit();
                            prevSign.setText(newSignStr.toString());
                            editor.putString("sign", newSignStr.toString());
                            editor.commit();
                        }else{
                            Toast.makeText(getActivity(),"您输入的个性签名过长\n请输入长度小于20个字符的个性签名",Toast.LENGTH_SHORT).show();
                        }
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
         * 设置头像
         */
        Button setAvatar = (Button) view.findViewById(R.id.setAvatar);
        setAvatar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                initFile("avatar.bmp");
                openGallery();
            }
        });

        return view;
    }

    // 为了便于读取图片
    private String fileName;
    private File tempFile;
    // 裁剪大小 CROP
    private final int CROP = 100;
    private static final int CROP_PHOTO_CODE = 16;
    private static final int OPEN_GALLERY_CODE = 17;

    /**
     * 初始化文件对象
     * @param pictureName 图片的名称
     */
    public void initFile(String pictureName) {
        if (Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED)){
            // 如果SD卡存在，则存在SD卡中
            String path = Environment.getExternalStorageDirectory() + File.separator + "MyChat" + File.separator;
            File file = new File(path);
            if (!file.exists()){
                // 路径不存在则创建一个文件夹
                file.mkdir();
            }
            fileName = path + pictureName;
            tempFile = new File(fileName);
        }else{
            // 如果SD卡不存在，则存在内存中
            String path = getActivity().getFilesDir().toString() + File.separator + "MyChat" + File.separator;
            File file = new File(path);
            if (!file.exists()){
                // 路径不存在则创建一个文件夹
                file.mkdir();
            }
            fileName = path + pictureName;
            tempFile = new File(fileName);
        }

    }

    /**
     * 打开相册
     */
    public void openGallery() {
        Intent intent = new Intent(Intent.ACTION_PICK);// 打开相册
        intent.setDataAndType(MediaStore.Images.Media.INTERNAL_CONTENT_URI, "image/*");
        intent.putExtra("output", Uri.fromFile(tempFile));
        startActivityForResult(intent, OPEN_GALLERY_CODE);
    }

    /**
     * 裁剪图片
     */
    public void cropPhoto(Uri uri) {
        Intent intent = new Intent("com.android.camera.action.CROP");
        intent.setDataAndType(uri, "image/*");
        intent.putExtra("output", Uri.fromFile(tempFile));
        intent.putExtra("crop", true);
        intent.putExtra("aspectX", 1);
        intent.putExtra("aspectY", 1);
        intent.putExtra("outputX", CROP);
        intent.putExtra("outputY", CROP);
        startActivityForResult(intent, CROP_PHOTO_CODE);
    }

    /**
     * 对于Activity返回结果的处理
     * @param requestCode 如果是 PHOTO_SELECT 则处理返回的图片；如果是
     * @param resultCode 如果等于 RESULT_OK 则返回成功，其他情况不执行任何操作
     * @param data Intent类型，装载有返回的数据
     */
    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data){
        if (resultCode==MainActivity.RESULT_OK){
            switch (requestCode){
                case OPEN_GALLERY_CODE:
                    //Toast.makeText(getActivity(),"选择了一个图片",Toast.LENGTH_SHORT).show();
                    cropPhoto(data.getData());
                    break;

                case CROP_PHOTO_CODE:
                    Bitmap bitmap = BitmapFactory.decodeFile(fileName);
                    if (bitmap != null){
                        myAvatar.setImageBitmap(bitmap);
                        SharedPreferences.Editor editor = settings.edit();
                        prevSign.setText(fileName.toString());
                        editor.putString("myAvatarPath", fileName.toString());
                        editor.commit();
                    }
                    break;
                default:break;
            }
        }

    }

    private static final int SUCCESS_LINK = 11;
    private static final int FAIL_LINK = 12;
    /**
     * 处理网络线程的消息
     * */
    private Handler mHandler = new Handler(Looper.getMainLooper()) {
        @Override
        public void handleMessage(Message message) {
            String info = (String)message.obj;
            switch (message.what){
                case SUCCESS_LINK:
                    //Toast.makeText(getActivity(),info,Toast.LENGTH_SHORT).show();
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
                    Toast.makeText(getActivity(),info,Toast.LENGTH_SHORT).show();
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
