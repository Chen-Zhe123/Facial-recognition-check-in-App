1.代码结构和之前的一样，只不过将library-debug_v.x.x.aar包中一部分代码移到app/src/中，提高二次开发简单性。
　开发者不必担心，这次优化代码只是在1.0.7.3版本上。但是相比之前版本看，不会影响你的代码结构。在旧版本的基础上，
　开发者只需将com.baidu.aip.face包复制到app/src/main/java中，并且将library-debug_v1.0.7.3.arr替换
　旧版library-debug_v.XXX.aar即可。

2.已经将修改人脸识别配置移出
FaceSDKManager.getInstance().init(getApplicationContext(),new FaceSDKManager.SdkInitListener() {
			@Override
			public void initStart(FaceDetector mFaceDetector, FaceFeature mFaceFeature) {
				//开发者可以在此配置人脸识别参数,比如
				//默认
				//mFaceDetector.init(RgbIrVideoIdentifyActivity.this);
				//mFaceFeature.init(RgbIrVideoIdentifyActivity.this);
				//修改
				//mFaceDetector.init(RgbIrVideoIdentifyActivity.this, FaceEnvironment);
                //mFaceFeature.init(RgbIrVideoIdentifyActivity.this, FaceSDK.RecognizeType.RECOGNIZE_LIVE);
			}

			.....

		});

3.如果开发者不想使用gpio,可以将app/libs/中的armeabi-v7a和firefly-api.jar删掉。并且请将app/build.gradle文件中
     implementation files('libs/firefly-api.jar')注释掉

   并且将AndroidManifest.xml文件中的

     <application
             android:allowBackup="true"
             android:name=".MainApp"
             ...
     中的android:name=".MainApp"去掉。