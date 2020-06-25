package com.zx.okhttp3;

import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Environment;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.BaseAdapter;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.lzy.imagepicker.ImagePicker;
import com.lzy.imagepicker.bean.ImageItem;
import com.lzy.imagepicker.ui.ImageGridActivity;
import com.lzy.imagepicker.view.CropImageView;
import com.zx.uploadlibrary.listener.ProgressListener;
import com.zx.uploadlibrary.listener.impl.UIProgressListener;
import com.zx.uploadlibrary.utils.OKHttpUtils;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.Response;

public class MainActivity extends AppCompatActivity{



    //上传文件到服务器的地址(使用的时候替换成自己的服务器地址)
    private static final String POST_FILE_URL = "http://192.168.199.2:8282/multifileUpload";
    //下载文件的地址
    private static final String DOWNLOAD_TEST_URL = "http://oh0vbg8a6.bkt.clouddn.com/app-debug.apk";

    //下载的文件的存储路径
    private static final String STORE_DOWNLOAD_FILE_PATH = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS)  + File.separator +"download2.apk";
    private ProgressBar uploadProgress, downloadProgress;
    private TextView uploadTV,downloadTv,images;
    private GridView gridView;

    ImagePicker imagePicker;
    ArrayList<ImageItem> imagesList;
    private ArrayList<String> list;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        initView();
    }
    public <T extends View> T bindView(int id) {
        return (T) this.findViewById(id);
    }
    //初始化View
    private void initView() {
        uploadProgress = (ProgressBar) findViewById(R.id.upload_progress);
        downloadProgress = (ProgressBar) findViewById(R.id.download_progress);
        uploadTV = (TextView) findViewById(R.id.tv_upload_progress);
        downloadTv = (TextView) findViewById(R.id.tv_download_progress);
        gridView = bindView(R.id.gridView);
        images = bindView(R.id.images);
        findViewById(R.id.upload).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                upload();
            }
        });
        findViewById(R.id.download).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                download();
            }
        });
        imagesList = new ArrayList<>();

        imagePicker = ImagePicker.getInstance();
        imagePicker.setImageLoader(new GlideImageLoader());   //设置图片加载器
        imagePicker.setShowCamera(true);  //显示拍照按钮
        imagePicker.setCrop(true);        //允许裁剪（单选才有效）
        imagePicker.setSaveRectangle(true); //是否按矩形区域保存
        imagePicker.setSelectLimit(9);    //选中数量限制
        imagePicker.setStyle(CropImageView.Style.RECTANGLE);  //裁剪框的形状
        imagePicker.setFocusWidth(800);   //裁剪框的宽度。单位像素（圆形自动取宽高最小值）
        imagePicker.setFocusHeight(800);  //裁剪框的高度。单位像素（圆形自动取宽高最小值）
        imagePicker.setOutPutX(1000);//保存文件的宽度。单位像素
        imagePicker.setOutPutY(1000);//保存文件的高度。单位像素


        images.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(MainActivity.this, ImageGridActivity.class);
                startActivityForResult(intent, 1);
            }
        });

    }

    //多文件上传（带进度）
    private void upload() {
        //这个是非ui线程回调，不可直接操作UI
        final ProgressListener progressListener = new ProgressListener() {
            @Override
            public void onProgress(long bytesWrite, long contentLength, boolean done) {
                Log.i("TAG", "bytesWrite:" + bytesWrite);
                Log.i("TAG", "contentLength" + contentLength);
                Log.i("TAG", (100 * bytesWrite) / contentLength + " % done ");
                Log.i("TAG", "done:" + done);
                Log.i("TAG", "================================");
            }
        };


        //这个是ui线程回调，可直接操作UI
        UIProgressListener uiProgressRequestListener = new UIProgressListener() {
            @Override
            public void onUIProgress(long bytesWrite, long contentLength, boolean done) {
                Log.i("TAG", "bytesWrite:" + bytesWrite);
                Log.i("TAG", "contentLength" + contentLength);
                Log.i("TAG", (100 * bytesWrite) / contentLength + " % done ");
                Log.i("TAG", "done:" + done);
                Log.i("TAG", "================================");
                //ui层回调,设置当前上传的进度值
                int progress = (int) ((100 * bytesWrite) / contentLength);
                uploadProgress.setProgress(progress);
                uploadTV.setText("上传进度值：" + progress + "%");
            }

            //上传开始
            @Override
            public void onUIStart(long bytesWrite, long contentLength, boolean done) {
                super.onUIStart(bytesWrite, contentLength, done);
                Toast.makeText(getApplicationContext(),"开始上传",Toast.LENGTH_SHORT).show();
            }

            //上传结束
            @Override
            public void onUIFinish(long bytesWrite, long contentLength, boolean done) {
                super.onUIFinish(bytesWrite, contentLength, done);
                //uploadProgress.setVisibility(View.GONE); //设置进度条不可见
                Toast.makeText(getApplicationContext(),"上传成功",Toast.LENGTH_SHORT).show();

            }
        };


        //开始Post请求,上传文件
        OKHttpUtils.doPostRequest(POST_FILE_URL, list, uiProgressRequestListener, new Callback() {
            @Override
            public void onFailure(Call call, final IOException e) {
                Log.i("TAG", "error------> "+e.getMessage());
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        Toast.makeText(MainActivity.this, "上传失败"+e.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });

            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                Log.i("TAG", "success---->"+response.body().string());
            }
        });

    }


    //文件下载
    private void download() {
        //这个是非ui线程回调，不可直接操作UI
        final ProgressListener progressResponseListener = new ProgressListener() {
            @Override
            public void onProgress(long bytesRead, long contentLength, boolean done) {
                Log.i("TAG", "bytesRead:" + bytesRead);
                Log.i("TAG", "contentLength:" + contentLength);
                Log.i("TAG", "done:" + done);
                if (contentLength != -1) {
                    //长度未知的情况下回返回-1
                    Log.i("TAG", (100 * bytesRead) / contentLength + "% done");
                }
                Log.i("TAG", "================================");
            }
        };


        //这个是ui线程回调，可直接操作UI
        final UIProgressListener uiProgressResponseListener = new UIProgressListener() {
            @Override
            public void onUIProgress(long bytesRead, long contentLength, boolean done) {
                Log.i("TAG", "bytesRead:" + bytesRead);
                Log.i("TAG", "contentLength:" + contentLength);
                Log.i("TAG", "done:" + done);
                if (contentLength != -1) {
                    //长度未知的情况下回返回-1
                    Log.i("TAG", (100 * bytesRead) / contentLength + "% done");
                }
                Log.i("TAG", "================================");
                //ui层回调,设置下载进度
                int progress = (int) ((100 * bytesRead) / contentLength);
                downloadProgress.setProgress(progress);
                downloadTv.setText("下载进度：" + progress +"%");
            }

            @Override
            public void onUIStart(long bytesRead, long contentLength, boolean done) {
                super.onUIStart(bytesRead, contentLength, done);
                Toast.makeText(getApplicationContext(),"开始下载",Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onUIFinish(long bytesRead, long contentLength, boolean done) {
                super.onUIFinish(bytesRead, contentLength, done);
                Toast.makeText(getApplicationContext(),"下载完成",Toast.LENGTH_SHORT).show();
            }
        };

        //开启文件下载
        OKHttpUtils.downloadAndSaveFile(this,DOWNLOAD_TEST_URL,STORE_DOWNLOAD_FILE_PATH,uiProgressResponseListener);

    }

    //初始化上传文件的数据
    private List<String> initUploadFile(){
        List<String> fileNames = new ArrayList<>();
        fileNames.add(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS)
                + File.separator + "test.txt"); //txt文件
        fileNames.add(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS)
                + File.separator + "bell.png"); //图片
        fileNames.add(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_MOVIES)
                + File.separator + "kobe.mp4"); //视频
        fileNames.add(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_MUSIC)
                + File.separator + "xinnian.mp3"); //音乐
        return fileNames;
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == ImagePicker.RESULT_CODE_ITEMS) {
            if (data != null && requestCode == 1) {
                imagesList = (ArrayList<ImageItem>) data.getSerializableExtra(ImagePicker.EXTRA_RESULT_ITEMS);
                MyAdapter adapter = new MyAdapter(imagesList);
                gridView.setAdapter(adapter);
                images.setText("已选择" + imagesList.size() + "张");
                list = listConvert2(imagesList);
            } else {
                Toast.makeText(this, "没有选择图片", Toast.LENGTH_SHORT).show();
            }
        }
    }

    private class MyAdapter extends BaseAdapter {

        private List<ImageItem> items;

        public MyAdapter(List<ImageItem> items) {
            this.items = items;
        }

        public void setData(List<ImageItem> items) {
            this.items = items;
            notifyDataSetChanged();
        }

        @Override
        public int getCount() {
            return items.size();
        }

        @Override
        public ImageItem getItem(int position) {
            return items.get(position);
        }

        @Override
        public long getItemId(int position) {
            return position;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            ImageView imageView;
            int size = gridView.getWidth() / 3;
            if (convertView == null) {
                imageView = new ImageView(MainActivity.this);
                AbsListView.LayoutParams params = new AbsListView.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, size);
                imageView.setLayoutParams(params);
                imageView.setBackgroundColor(Color.parseColor("#88888888"));
            } else {
                imageView = (ImageView) convertView;
            }
            imagePicker.getImageLoader().displayImage(MainActivity.this, getItem(position).path, imageView, size, size);
            return imageView;
        }
    }


    public ArrayList<String> listConvert2(ArrayList<ImageItem> list_img) {
        ArrayList<String > list = new ArrayList<>();
        for(int i = 0 ; i < list_img.size();i ++){
            list.add(list_img.get(i).path);
        }
        return list;
    }

}
