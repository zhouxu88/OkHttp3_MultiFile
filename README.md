#### 一、简介
v1.01版本
对OKHttp的封装，可实现多文件上传（带进度值）、表单提交到服务器；下载文件保存到到本地（带进度值）。
可以在文件上传/下载时，在开始、结束、进度回调3个方法中自定义内容。

#### 二、使用步骤：

**1、Gradle添加如下2个依赖**
```
allprojects {
		repositories {
			...
			maven { url 'https://jitpack.io' }
		}
	}

dependencies {
	        compile 'com.github.zhouxu88:OkHttp3_MultiFile:v1.0'
	}
```

**2、多文件上传的调用**
```
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
        OKHttpUtils.doPostRequest(POST_FILE_URL, initUploadFile(), uiProgressRequestListener, new Callback() {
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

```

**3、文件下载的调用**
```
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
```
