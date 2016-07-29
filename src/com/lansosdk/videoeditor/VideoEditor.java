package com.lansosdk.videoeditor;

import java.io.File;
import java.io.IOException;
import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;



import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.util.Log;



public class VideoEditor {

	
	 private static final String TAG="VideoEditor";
	 
	  public static final int VIDEO_EDITOR_EXECUTE_SUCCESS1 =0;
	  public static final int VIDEO_EDITOR_EXECUTE_SUCCESS2 =1;
	  public static final int VIDEO_EDITOR_EXECUTE_FAILED =-1;
	  
	  
	  
	  private final int VIDEOEDITOR_HANDLER_PROGRESS=203;
	  private final int VIDEOEDITOR_HANDLER_COMPLETED=204;
	  
	  
//	  #define MEDIACODEC_ERROR_NONE  0
//
////   在高通 骁龙616（MSM8939）主要是这个
//	  #define MEDIACODEC_ERROR_DEQUEUE_OUTPUT_BUFFER  0x6801  
//	  #define MEDIACODEC_ERROR_GET_OUTPUT_FORMAT 0x6802
//	  #define MEDIACODEC_ERROR_GET_OUTPUT_BUFFER 0x6803
//	  #define MEDIACODEC_ERROR_QUEUE_INPUT_BUFFER  0x6804
//	  #define MEDIACODEC_ERROR_GET_INPUT_BUFFER  0x6805
//	  #define MEDIACODEC_ERROR_DEQUEUE_INPUT_BUFFER  0x6806
	  
	  
		public VideoEditor() {
		// TODO Auto-generated constructor stub
			Looper looper;
	        if ((looper = Looper.myLooper()) != null) {
	            mEventHandler = new EventHandler(this, looper);
	        } else if ((looper = Looper.getMainLooper()) != null) {
	            mEventHandler = new EventHandler(this, looper);
	        } else {
	            mEventHandler = null;
	            Log.w(TAG,"cannot get Looper handler. may be cannot receive video editor progress!!");
	        }
		}
	
	
	    public onVideoEditorProgressListener mProgressListener=null;
	    /**
	     * @param listener
	     */
		public void setOnProgessListener(onVideoEditorProgressListener listener)
		{
			mProgressListener=listener;
		}
		private void doOnProgressListener(int timeMS)
		{
			if(mProgressListener!=null)
				mProgressListener.onProgress(this,timeMS);
		}
	 private EventHandler mEventHandler;
	 private  class EventHandler extends Handler {
	        private final WeakReference<VideoEditor> mWeakExtract;

	        public EventHandler(VideoEditor mp, Looper looper) {
	            super(looper);
	            mWeakExtract = new WeakReference<VideoEditor>(mp);
	        }

	        @Override
	        public void handleMessage(Message msg) {
	        	VideoEditor videoextract = mWeakExtract.get();
	        	if(videoextract==null){
	        		Log.e(TAG,"VideoExtractBitmap went away with unhandled events");
	        		return ;
	        	}
	        	switch (msg.what) {
				case VIDEOEDITOR_HANDLER_PROGRESS:
					videoextract.doOnProgressListener(msg.arg1);
					break;
				default:
					break;
				}
	        }
	   }
	   /**
	     * 异步线程执行的代码.
	     */
	    public int executeVideoEditor(String[] array)  {
	        return execute(array);
	    }
	    
	    @SuppressWarnings("unused") /* Used from JNI */
	    private void postEventFromNative(int what,int arg1, int arg2) {
	    	Log.i(TAG,"postEvent from native  is:"+what);
	    	
	    	  if(mEventHandler!=null){
              	  Message msg=mEventHandler.obtainMessage(VIDEOEDITOR_HANDLER_PROGRESS);
                  msg.arg1=what;
                  mEventHandler.sendMessage(msg);	
              }
	    }
	    /**
	     * 执行成功,返回0, 失败返回错误码.
	     * @param cmdArray  ffmpeg命令的字符串数组, 可参考此文件中的各种方法举例来编写.
	     * @return  执行成功,返回0, 失败返回错误码. (可当执行失败,联系我们,由我们来帮您解决)
	     */
	    private native int execute(Object cmdArray);
	
	public int vAdjustSpped(String srcPath,MediaInfo media,String dstPath)
	{
		return videoAdjustSpeed(srcPath,media.vCodecName,2,dstPath);
	}
	

	/**
	 * 把一张图片转换为视频,并有淡入淡出的效果.
	 * 适用于视频转场的场合,比如两个视频AB之间需要转场,中间需要"五分钟过去了..."这样的文字,可以用这个命令来生成一个提示的视频,然后把这个转场和两个视频拼接起来即可.
	 * 注意:这里图片生成的视频的宽高等于图片的宽高,如果用在转场的场合,需要前后两个视频的宽高一直,比如都是480x480等.
	 * 
	 * @param srcPath  输入的图片文件路径
	 * @param totalTime  转换为视频的时长,一般3--5秒为易,生成的图片每秒钟是25帧.
	 * @param fadeinstart 从第几帧开始有淡入的效果 
	 * @param fadeinCnt   淡入的效果持续多少帧
	 * @param fadeoutstart 淡出开始的帧数.
	 * @param fadeoutCnt  淡出效果持续多少帧
	 * @param dstPath  视频保存的路径.
	 * @return
	 */
	public native int  pictureFadeInOut( String srcPath,int totalTime,int fadeinstart,int fadeinCnt,int fadeoutstart,int fadeoutCnt,
		   String dstPath);
	/**
	 * 把一张图片转换为视频，视频在刚显示时，会有缓慢显示出来的动画效果。适用在视频转场的场合。
	 * @param srcPath　　原png或jpg图片
	 * @param totalTime　　转换为视频的总时长
	 * @param fadeinstart　　从第几帧开始缓慢显示出来，建议从0
	 * @param fadeinCnt  缓慢显示出来的帧数，比如效果持续２秒钟，则这里是2x25
	 * @param dstPath　　视频保存的路径，后缀需要是.mp4格式
	 * @return
	 */
	public native int  pictureFadeIn( String srcPath,int totalTime,int fadeinstart,int fadeinCnt,String dstPath);
	/**
	 * 把一张图片转换为视频，图片在显示结束的时候，有慢慢变暗下去的效果。如果png视频有透明部分，则透明部分转换为黑色
	 * @param srcPath　　图片的路径，可以是ｐｎｇ或ｊｐｇ，
	 * @param totalTime　　图片转换成视频的总时间
	 * @param fadeoutstart　　慢慢变暗下的开始帧，比如从５０帧的地方开始变暗，总帧数等于总时间x25（视频每秒钟25帧）
	 * @param fadeoutCnt　　　慢慢变暗效果的帧数，比如效果持续２秒钟，则这里是2x25
	 * @param dstPath    　　视频保存的路径，后缀需要是.mp4格式
	 * @return
	 */
	public native int  pictureFadeOut( String srcPath,int totalTime,int fadeoutstart,int fadeoutCnt,String dstPath);
	
	/**
	 * 调整视频的播放速度，　可以把视频加快速度，或放慢速度。适用在希望缩短视频中不重要的部分的场景，比如走路等
	 * @param srcPath　　源视频
	 * @param decoder　　指定视频的解码器名字
	 * @param speed　　　　源视频中　　画面和音频同时改变的倍数，比如放慢一倍，则这里是0.5;加快一倍，这里是２；建议速度在0.5--2.0之间。
	 * @param dstPath　　处理后的视频存放路径，后缀需要是.mp4
	 * @return
	 */
	public native int  videoAdjustSpeed( String srcPath,String decoder,float speed,String dstPath);
	/**
	 * 视频水平镜像，即把视频左半部分镜像显示在右半部分
	 * @param srcPath　源视频路径
	 * @param decoder　　指定解码器
	 * @param dstPath　　目标视频路径
	 * @return
	 */
	public native int  videoMirrorH( String srcPath,String decoder,String dstPath);
	/**
	 * 暂时不要使用.
	 * 两个音频文件混合，可以一个是mp3格式，另一个是aac格式。　TODO:实际测试混合后的音频长度
	 * @param srcPath1　　音频１的路径
	 * @param srcPath2　　音频２的路径
	 * @param vol1　　　音频１混合时的音量
	 * @param vol2　　　音频２混合时的音量
	 * @param dstPath　　目标音频存放的路径。建议是aac格式
	 * @return
	 */
	public native int audioAdjustVolumeMix( String srcPath1,String srcPath2,float vol1,float vol2,String dstPath);
	/**
	 * 视频转场，第二个视频从右侧渐渐的显示（可以联系我们，做更多的视频转场方法）　TODO:没有第二个视频从哪里开始转场。
	 * @param srcPath1　　第一个视频
	 * @param srcPath2　　待要显示的第二个视频
	 * @param decoder1　　第一个视频的解码器
	 * @param speed　　　　视频的转场速度
	 * @param dstPath　　目标视频存放路径　，需要是mp4
	 * @return
	 */
	public native int videoTransferRight2Left( String srcPath1,String srcPath2,String decoder1,float speed,String dstPath);
	/**
	 * 视频垂直方向反转
	 * @param srcPath1　　原视频
	 * @param decoder　　视频的解码器名字
	 * @param dstPath　　目标视频　需要是mp4格式。
	 * @return
	 */
	public native int videoRotateVertically( String srcPath1,String decoder,String dstPath);
	/**
	 * 视频水平方向反转
	 * @param srcPath1　　原视频
	 * @param decoder　　视频的解码器名字
	 * @param dstPath　　目标视频. 需要是mp4格式
	 * @return
	 */
	public native int videoRotateHorizontally( String srcPath1,String decoder,String dstPath);
	/**
	 * 视频顺时针旋转９０度
	 * @param srcPath1　　原视频
	 * @param decoder　　　视频的解码器名字
	 * @param dstPath　　　目标视频.需要是mp4格式
	 * @return
	 */
	public native int videoRotate90Clockwise( String srcPath1,String decoder,String dstPath);
	/**
	 * 视频逆时针旋转９０度
	 * @param srcPath1　原视频
	 * @param decoder　　视频的解码器名字
	 * @param dstPath　　目标视频，需要是mp4格式
	 * @return
	 */
	public native int videoRotate90CounterClockwise( String srcPath1,String decoder,String dstPath);
	/**
	 * 视频倒序；比如正常的视频画面是一个人从左边走到右边，倒序后，人从右边倒退到左边，即视频画面发生了倒序
	 * 注意：此处理会占用大量的内存，建议视频不要过长，尽量在１分钟内
	 * @param srcPath1　原视频
	 * @param decoder　　解码器的名字
	 * @param dstPath　　目标视频，需要是mp4格式
	 * @return
	 */
	public native int videoReverse( String srcPath1,String decoder,String dstPath);
	/**
	 * 音频倒序，和视频倒序类似，把原来正常的声音，处理成从后向前的声音。　适合在搞怪的一些场合。
	 * 注意：　此处理会占用大量的内存，建议时长在１分钟以内
	 *
	 * @param srcPath1 原音频
	 * @param dstPath　　目标音频
	 * @return
	 */
	public native int audioReverse( String srcPath1,String dstPath);
	/**
	 * 　把一个mp4文件中的音频部分和视频都倒序播放。
	 * @param srcPath1　　原mp4文件
	 * @param decoder　　mp4文件中的视频解码器名字
	 * @param dstPath　　目标mp4文件存放路径
	 * @return
	 */
	public native int avReverse( String srcPath1,String decoder,String dstPath);
	
	
	/**
	 * 截取一段pcm数据.
	 * 
	 * 截取一段音频,,精度是100ms
	 * @param srcPath    原音频裸数据, pcm格式
	 * @param sampleRate   pcm的采样率
	 * @param channel    通道数
	 * @param pcmBytes  每个采样点是几个字节,
	 * @param startTimeMs   截取开始时间,单位毫秒
	 * @param endTimeMs  静音的结束时间, 单位毫秒
	 * @param dstPath    截取处理后的保存路径.
	 * @return
	 */
	public static native int audioPcmCut(String srcPath,int sampleRate,int channel,int pcmBytes,int startTimeMs,int endTimeMs,String dstPath);
	/**
	 * 合并两个音频数据,精度是100ms
	 *  
	 *  一般是一小段的声音合并到主声音中的场景.
	 *  
	 * 注意:需要两个pcm的 bitrate等参数需要一直,保持一致.
	 * 
	 * @param srcMainPath  源pcm的音频, 主音频
	 * @param srcSubPath   合并的pcm音频, 
	 * @param sampleRate   两个pcm相同的采样率
	 * @param channel    相同的通道数
	 * @param pcmBytes  相同的采样点字节数
	 * @param startTimeMs  srcSubPath的pcm数据 开始合并的时间, 默认是把srcSubPath全部的数据合并到srcMainPath中.
	 * @param dstPath  合并后保存的路径.
	 * @return
	 */
	public static native int audioPcmReplace(String srcMainPath,String srcSubPath,int sampleRate,int channel,int pcmBytes,int startTimeMs,String dstPath);
	/**
	 * 把音频中的一种一段声音静音.精度是100ms
	 * 
	 * @param srcPath  原音频裸数据, pcm格式
	 * @param sampleRate  pcm的采样率
	 * @param channel  通道数
	 * @param pcmBytes  每个采样点是几个字节,
	 * @param startTimeMs  静音开始时间,单位毫秒
	 * @param endTimeMs    静音的结束时间, 单位毫秒
	 * @param dstPath   静音处理后的保存路径.
	 * @return
	 */
	public static native int audioPcmMute(String srcPath,int sampleRate,int channel,int pcmBytes,int startTimeMs,int endTimeMs,String dstPath);
	
	/**
	 * 拷贝文件, 成功返回0,失败返回-1;
	 * @param srcPath
	 * @param dstPath
	 * @return
	 */
	public static native int copyFile(String srcPath,String dstPath);
	
	 
	  public static native int getLimitYear();
	  public static native int getLimitMonth();
	  
	  //-------------------------------------------------------------------------------
		
	public int executePcmMix(String srcPach1,int samplerate,int channel,String srcPach2,int samplerate2,int channel2,
			float value1,float value2,String dstPath)
	{
		List<String> cmdList=new ArrayList<String>();
		
		String filter=String.format(Locale.getDefault(),"[0:a]volume=volume=%f[a1]; [1:a]volume=volume=%f[a2]; [a1][a2]amix=inputs=2:duration=first:dropout_transition=2",value1,value2);
		
		cmdList.add("-f");
		cmdList.add("s16le");
		cmdList.add("-ar");
		cmdList.add(String.valueOf(samplerate));
		cmdList.add("-ac");
		cmdList.add(String.valueOf(channel));
		cmdList.add("-i");
		cmdList.add(srcPach1);
		
		cmdList.add("-f");;
		cmdList.add("s16le");
		cmdList.add("-ar");
		cmdList.add(String.valueOf(samplerate2));
		cmdList.add("-ac");
		cmdList.add(String.valueOf(channel2));
		cmdList.add("-i");
		cmdList.add(srcPach2);

		cmdList.add("-y");
		cmdList.add("-filter_complex");
		cmdList.add(filter);
		cmdList.add("-f");
		cmdList.add("s16le");
		cmdList.add("-acodec");
		cmdList.add("pcm_s16le");
		cmdList.add(dstPath);
		
		
		String[] command=new String[cmdList.size()];  
	     for(int i=0;i<cmdList.size();i++){  
	    	 command[i]=(String)cmdList.get(i);  
	     }  
	     return executeVideoEditor(command);
	}
	
	public int executePcmMix(String srcPach1,String srcPach2,int samplerate,int channel,float value1,float value2,String dstPath)
	{
		List<String> cmdList=new ArrayList<String>();
	
		cmdList.add("-f");
		cmdList.add("s16le");
		cmdList.add("-ar");
		cmdList.add(String.valueOf(samplerate));
		cmdList.add("-ac");
		cmdList.add(String.valueOf(channel));
		cmdList.add("-i");
		cmdList.add(srcPach1);
		
		cmdList.add("-f");;
		cmdList.add("s16le");
		cmdList.add("-ar");
		cmdList.add(String.valueOf(samplerate));
		cmdList.add("-ac");
		cmdList.add(String.valueOf(channel));
		cmdList.add("-i");
		cmdList.add(srcPach2);

		cmdList.add("-y");
		cmdList.add("-filter_complex");
		cmdList.add("[0:a]volume=volume=0.5[a1]; [1:a]volume=volume=5[a2]; [a1][a2]amix=inputs=2:duration=first:dropout_transition=2");
		cmdList.add("-f");
		cmdList.add("s16le");
		cmdList.add("-acodec");
		cmdList.add("pcm_s16le");
		cmdList.add(dstPath);
		
		
		String[] command=new String[cmdList.size()];  
	     for(int i=0;i<cmdList.size();i++){  
	    	 command[i]=(String)cmdList.get(i);  
	     }  
	     return executeVideoEditor(command);
	}
	  public int executeAudioMix(String audioPath1,String audioPath2,int leftDelayMS,int rightDelayMS,String dstPath)
	  {
		  List<String> cmdList=new ArrayList<String>();
			String overlayXY=String.format(Locale.getDefault(),"[1:a]adelay=%d|%d[delaya1]; [0:a][delaya1]amix=inputs=2:duration=first:dropout_transition=2",leftDelayMS,rightDelayMS);
			
			
			cmdList.add("-i");
			cmdList.add(audioPath1);

			cmdList.add("-i");
			cmdList.add(audioPath2);

			cmdList.add("-filter_complex");
			cmdList.add(overlayXY);
			
			cmdList.add("-acodec");
			cmdList.add("libfaac");
			
			cmdList.add("-y");
			cmdList.add(dstPath);
			String[] command=new String[cmdList.size()];  
		     for(int i=0;i<cmdList.size();i++){  
		    	 command[i]=(String)cmdList.get(i);  
		     } 
		     return  executeVideoEditor(command);
	  }
	  public int executePcmEncodeAac(String srcPach,int samplerate,int channel,String dstPath)
	  {
			List<String> cmdList=new ArrayList<String>();
			
			cmdList.add("-f");
			cmdList.add("s16le");
			cmdList.add("-ar");
			cmdList.add(String.valueOf(samplerate));
			cmdList.add("-ac");
			cmdList.add(String.valueOf(channel));
			cmdList.add("-i");
			cmdList.add(srcPach);
		
	
			cmdList.add("-acodec");
			cmdList.add("libfaac");
			cmdList.add("-b:a");
			cmdList.add("64000");
			cmdList.add("-y");
		
			cmdList.add(dstPath);
			
			
			String[] command=new String[cmdList.size()];  
		     for(int i=0;i<cmdList.size();i++){  
		    	 command[i]=(String)cmdList.get(i);  
		     }  
		     return executeVideoEditor(command);
	}
	
	public int executePcmComposeVideo(String srcPcm,int samplerate,int channel,String srcVideo,String dstPath)
	{
		List<String> cmdList=new ArrayList<String>();
		
		cmdList.add("-f");
		cmdList.add("s16le");
		cmdList.add("-ar");
		cmdList.add(String.valueOf(samplerate));
		cmdList.add("-ac");
		cmdList.add(String.valueOf(channel));
		cmdList.add("-i");
		cmdList.add(srcPcm);
		
		cmdList.add("-i");
		cmdList.add(srcVideo);
		
		cmdList.add("-acodec");
		cmdList.add("libfaac");
		cmdList.add("-b:a");
		cmdList.add("64000");
		cmdList.add("-y");
		
		cmdList.add("-vcodec");
		cmdList.add("copy");
	
		cmdList.add(dstPath);
		
		
		String[] command=new String[cmdList.size()];  
	     for(int i=0;i<cmdList.size();i++){  
	    	 command[i]=(String)cmdList.get(i);  
	     }  
	     return executeVideoEditor(command);
	}
	
	    //--------------------------------------------------------------------------
	/**
	 * 把h264裸码流数据包装成MP4格式,因为是裸码流,未知帧率, 包装成MP4默认帧率是25帧/秒
	 * 
	 * 
	 * 注意,这里面没有音频数据.,H264裸码流是通过编码得到的数据直接写入文件的数据.
	 * @param srcPath
	 * @param dstPath
	 * @return
	 */
	public static int executeH264WrapperMp4(String srcPath,String dstPath)
	{
		if(fileExist(srcPath)){
			
					List<String> cmdList=new ArrayList<String>();
					
			    	cmdList.add("-i");
					cmdList.add(srcPath);

					cmdList.add("-vcodec");
					cmdList.add("copy");
					
					cmdList.add("-y");
					cmdList.add(dstPath);
					String[] command=new String[cmdList.size()];  
				     for(int i=0;i<cmdList.size();i++){  
				    	 command[i]=(String)cmdList.get(i);  
				     }  
				     VideoEditor veditor=new VideoEditor();
				     return veditor.executeVideoEditor(command);
	  	}
		return VIDEO_EDITOR_EXECUTE_FAILED;
	}
	    //--------------------------------------------------------------------------
	/**
	 * 视频转码.
	 * 通过调整视频的bitrate来对视频文件大小的压缩,降低视频文件的大小, 注意:压缩可能导致视频画质下降.
	 * 
	 * 此命令为单纯压缩命令, 如需对视频进行裁剪/增加水印/增加文字等需要编解码的场合, 可以在执行的方法中直接压缩,这样节省一倍的时间, 没有必要等其他命令执行完后,再执行此方法. 
	 * 比如如下方法: 
	 * {@link #executeCropOverlay(String, String, String, int, int, int, int, int, int, String, int)}
	 * {@link #executeVideoCutCropOverlay(String, String, String, float, float, int, int, int, int, int, int, String, int)}
	 * {@link #executeAddWaterMark(String, String, int, int, String, int)}
	 * {@link #executeAddWaterMark(String, String, float, float, int, int, String, int)}
	 * 
	 * @param srcPath 源视频
	 * @param dstPath 目的视频
	 * @param percent 压缩百分比.值从0--1
	 * @return
	 */
		public int executeVideoCompress(String srcPath,String dstPath,float percent)
		{
			//ffmpeg -i 2x.mp4 -acodec copy -vcodec libx264 -b:v 200k 2xpress.mp4
			if(fileExist(srcPath)){
				
				MediaInfo info=new MediaInfo(srcPath,false);
				if(info.prepare())
				{
						List<String> cmdList=new ArrayList<String>();
						
						cmdList.add("-vcodec");
						cmdList.add(info.vCodecName);
						
				    	cmdList.add("-i");
						cmdList.add(srcPath);
						cmdList.add("-acodec");
						cmdList.add("copy");

						cmdList.add("-vcodec");
						cmdList.add("lansoh264_enc");
						
						cmdList.add("-b:v");
						float bitrate=info.vBitRate*percent;
						int nbitrate=(int)bitrate;
						cmdList.add(String.valueOf(nbitrate));
						
						cmdList.add("-pix_fmt");  //<========请注意, 使用lansoh264_enc编码器编码的时候,请务必指定格式,因为底层设计只支持yuv420p的输出.
						cmdList.add("yuv420p");
						
						cmdList.add("-y");
						cmdList.add(dstPath);
						String[] command=new String[cmdList.size()];  
					     for(int i=0;i<cmdList.size();i++){  
					    	 command[i]=(String)cmdList.get(i);  
					     }  
					     return executeVideoEditor(command);
				}
		  	}
			return VIDEO_EDITOR_EXECUTE_FAILED;
		}
		/**
		 * 分离mp4文件中的音频,并返回音频的路径,这个音频路径是放到{@link SDKDir#TMP_DIR}下的以当前时间为文件名的文件路径.
		 * 
		 * @param srcMp4Path
		 * @return
		 */
			public static String spliteAudioFile(String srcMp4Path)
			{
				MediaInfo  info=new MediaInfo(srcMp4Path,false);
				info.prepare();
				
				String audioPath=null;
				if(info.aCodecName.equalsIgnoreCase("aac")){
					audioPath=SDKFileUtils.createFile(SDKDir.TMP_DIR, ".aac");
				}else if(info.aCodecName.equalsIgnoreCase("mp4"))
					audioPath=SDKFileUtils.createFile(SDKDir.TMP_DIR, ".mp3");
				
				if(audioPath!=null){
					VideoEditor veditor=new VideoEditor();
					veditor.executeDeleteVideo(srcMp4Path, audioPath);
				}
				return audioPath;
			}
	
			/**
			 * 把原视频文件中的音频部分, 增加到新的视频中,
			 * 
			 * @param oldMp4   源视频, 需要内部有音频部分, 如没有音频则则方法无动作.
			 * @param newMp4   通过视频录制后,保存的新视频.里面只有视频部分或h264裸码流,需确保里面没有音频部分.
			 * @param tmpDir  此方法处理过程中生成的临时文件存放地, 临时文件夹路径.
			 * @param dstMp4   方法处理完后, 增加音频后的文件目标路径.
			 * @return  执行成功,返回true, 失败返回false(一般源视频中没有音频会执行失败)
			 */
	public static boolean encoderAddAudio(String oldMp4,String newMp4,String tmpDir,String dstMp4)
	{
		//
		MediaInfo  info=new MediaInfo(oldMp4,false);
		if(info.prepare())
		{
			String audioPath=null;
			if(info.aCodecName!=null)  //只有在有音频的场合,才增加.
			{
				if(info.aCodecName.equalsIgnoreCase("aac")){
					audioPath=SDKFileUtils.createFile(tmpDir, ".aac");
				}else if(info.aCodecName.equalsIgnoreCase("mp4"))
					audioPath=SDKFileUtils.createFile(tmpDir, ".mp3");	
				
				if(audioPath!=null){
					VideoEditor veditor=new VideoEditor();
					veditor.executeDeleteVideo(oldMp4, audioPath);  //获得音频
					veditor.executeVideoMergeAudio(newMp4, audioPath, dstMp4);  //合并到新视频文件中.
					SDKFileUtils.deleteFile(audioPath);
					return true;
				}
			}else{
				Log.w(TAG,"old mp4 file no audio . do not add audio");
			}
		}else{
			Log.w(TAG,"old mp4 file prepare error!!,do not add audio");
		}
		return false;
	}

	 private static boolean fileExist(String absolutePath)
	 {
		 if(absolutePath==null)
			 return false;
		 else 
			 return (new File(absolutePath)).exists();
	 }
	 
	 private static boolean filesExist(String[] fileArray)
	 {
		 
		 for(String file: fileArray)
		 {
			 if(fileExist(file)==false)
				 return false;
		 }
		 return true;
	 }
	   /**
		  * 删除多媒体文件中的音频,把多媒体中的视频部分提取出来，这样提出的视频播放，就没有声音了，
		  * 适用在当想给一个多媒体文件更换声音的场合的。您可以用这个方法删除声音后，通过{@link executeVideoEditor} 重新为视频增加一个声音。
		  * @param srcFile  输入的MP4文件
		  * @param dstFile 删除音频后的多媒体文件的输出绝对路径,路径的文件名类型是.mp4
		  * @return 返回执行的结果.
		  */
		  public int executeDeleteAudio(String srcFile,String dstFile)
		  {
			  	if(fileExist(srcFile)){
				  	List<String> cmdList=new ArrayList<String>();
			    	cmdList.add("-i");
					cmdList.add(srcFile);
					cmdList.add("-vcodec");
					cmdList.add("copy");
					cmdList.add("-an");
					cmdList.add("-y");
					cmdList.add(dstFile);
					String[] command=new String[cmdList.size()];  
				     for(int i=0;i<cmdList.size();i++){  
				    	 command[i]=(String)cmdList.get(i);  
				     }  
				     return executeVideoEditor(command);
			  	}else{
			  		return VIDEO_EDITOR_EXECUTE_FAILED;
			  	}
		  }
		  /**
		   * 删除多媒体文件中的视频部分，一个mp4文件如果是音频和视频一起的，等于提取多媒体文件中的音频，
		   *  
		   * @param srcFile  要处理的多媒体文件,里面需要有视频
		   * @param dstFile  删除视频部分后的音频保存绝对路径, 注意:如果多媒体中是音频是aac压缩,则后缀必须是aac. 如果是mp3压缩,则后缀必须是mp3,
		   * @return 返回执行的结果.
		   */
		  public int executeDeleteVideo(String srcFile,String dstFile)
		  {
			  	if(fileExist(srcFile)==false)
			  		return VIDEO_EDITOR_EXECUTE_FAILED;
			  	
			  	List<String> cmdList=new ArrayList<String>();
		    	cmdList.add("-i");
				cmdList.add(srcFile);
				cmdList.add("-acodec");
				cmdList.add("copy");
				cmdList.add("-vn");
				cmdList.add("-y");
				cmdList.add(dstFile);
				String[] command=new String[cmdList.size()];  
			     for(int i=0;i<cmdList.size();i++){  
			    	 command[i]=(String)cmdList.get(i);  
			     }  
			    return  executeVideoEditor(command);
		  }
		  /**
		   * 音频和视频合成为多媒体文件，等于给视频增加一个音频。
		    当前版本近测试了，把一个没有音频的mp4文件和 一个音频合成为多媒体格式.
		   
		   * @param videoFile 输入的视频文件,需视频文件中不存储音频部分, 如有音频怎会增加两个声音.
		   * @param audioFile 输入的音频文件
		   * @param dstFile  合成后的输出，文件名的后缀是.mp4
		   * @return 返回执行的结果.
		   * 
		   * 注意:如果合并的音频是aac格式,ffmpeg -i test.mp4 -i test.aac -vcodec copy -acodec copy -absf aac_adtstoasc shanchu4.mp4
		   */
		  public int executeVideoMergeAudio(String videoFile,String audioFile,String dstFile)
		  {
			  boolean isAAC=false;
			  
			  if(fileExist(videoFile) && fileExist(audioFile)){
				  
					  if(audioFile.endsWith(".aac")){
						  isAAC=true;
					  }
				  
					List<String> cmdList=new ArrayList<String>();
			    	cmdList.add("-i");
					cmdList.add(videoFile);
					cmdList.add("-i");
					cmdList.add(audioFile);
					cmdList.add("-vcodec");
					cmdList.add("copy");
					cmdList.add("-acodec");
					cmdList.add("copy");
					if(isAAC){
						cmdList.add("-absf");
						cmdList.add("aac_adtstoasc");
					}
					cmdList.add("-y");
					cmdList.add(dstFile);
					String[] command=new String[cmdList.size()];  
				     for(int i=0;i<cmdList.size();i++){  
				    	 command[i]=(String)cmdList.get(i);  
				     }  
				    return  executeVideoEditor(command);
				  
			  }else{
				  return VIDEO_EDITOR_EXECUTE_FAILED;
			  }
		  }
		  /**
		   * 
		   * 给视频MP4增加上音频，audiostartS表示从从音频的哪个时间点开始增加，单位是秒
		   * @param videoFile  原视频文件
		   * @param audioFile  需要增加的音频文件
		   * @param dstFile  处理后保存的路径 文件名的后缀需要.mp4格式
		   * @param audiostartS  音频增加的时间点，单位秒，类型float，可以有小数，比如从音频的2.35秒开始增加到视频中。
		   * @return
		   */
		  public int executeVideoMergeAudio(String videoFile,String audioFile,String dstFile,float audiostartS)
		  {
			  boolean isAAC=false;
			  if(fileExist(videoFile) && fileExist(audioFile)){	
				  
				  if(audioFile.endsWith(".aac")){
					  isAAC=true;
				  }
					List<String> cmdList=new ArrayList<String>();
			    	cmdList.add("-i");
					cmdList.add(videoFile);
					
					cmdList.add("-ss");
					cmdList.add(String.valueOf(audiostartS));
					
					cmdList.add("-i");
					cmdList.add(audioFile);
					cmdList.add("-vcodec");
					cmdList.add("copy");
					cmdList.add("-acodec");
					cmdList.add("copy");
					if(isAAC){
						cmdList.add("-absf");
						cmdList.add("aac_adtstoasc");
					}
					cmdList.add("-y");
					cmdList.add(dstFile);
					String[] command=new String[cmdList.size()];  
				     for(int i=0;i<cmdList.size();i++){  
				    	 command[i]=(String)cmdList.get(i);  
				     }  
				    return  executeVideoEditor(command);
				  
			  }else{
				  return VIDEO_EDITOR_EXECUTE_FAILED;
			  }
		  }
		  /**
		   * 给视频文件增加一个音频, 注意,这里是因音频的时长为目标视频文件的时长.
		   * 输出文件后缀是.mp4格式.
		   * @param videoFile
		   * @param audioFile
		   * @param dstFile
		   * @param audiostartS  音频开始时间, 单位秒,可以有小数, 比如2.5秒
		   * @param audiodurationS 音频增加的总时长.您可以只增加音频中一部分，比如增加音频的2.5秒到--180秒这段声音到视频文件中，则这里的参数是180
		   * @return
		   */
		  public int executeVideoMergeAudio(String videoFile,String audioFile,String dstFile,float audiostartS,float audiodurationS)
		  {
			  boolean isAAC=false;
			  if(fileExist(videoFile) && fileExist(audioFile)){
				
				  if(audioFile.endsWith(".aac")){
					  isAAC=true;
				  }
					List<String> cmdList=new ArrayList<String>();
			    	cmdList.add("-i");
					cmdList.add(videoFile);
					
					cmdList.add("-i");
					cmdList.add(audioFile);
					
					cmdList.add("-ss");
					cmdList.add(String.valueOf(audiostartS));
					
					cmdList.add("-t");
					cmdList.add(String.valueOf(audiodurationS));
					
					cmdList.add("-vcodec");
					cmdList.add("copy");
					cmdList.add("-acodec");
					cmdList.add("copy");
					if(isAAC){
						cmdList.add("-absf");
						cmdList.add("aac_adtstoasc");
					}
					cmdList.add("-y");
					cmdList.add(dstFile);
					String[] command=new String[cmdList.size()];  
				     for(int i=0;i<cmdList.size();i++){  
				    	 command[i]=(String)cmdList.get(i);  
				     }  
				    return  executeVideoEditor(command);
				  
			  }else{
				  return VIDEO_EDITOR_EXECUTE_FAILED;
			  }
		  }
		  /**
		   * 音频裁剪,截取音频文件中的一段.
		   * 需要注意到是: 尽量保持裁剪文件的后缀名和源音频的后缀名一致.
		   * @param srcFile   源音频
		   * @param dstFile  裁剪后的音频
		   * @param startS  开始时间,单位是秒. 可以有小数
		   * @param durationS  裁剪的时长.
		   * @return
		   */
		  public int executeAudioCutOut(String srcFile,String dstFile,float startS,float durationS)
		  {
			  if(fileExist(srcFile)){
				
					List<String> cmdList=new ArrayList<String>();
					
					cmdList.add("-ss");
					cmdList.add(String.valueOf(startS));
					
					
			    	cmdList.add("-i");
					cmdList.add(srcFile);

					cmdList.add("-t");
					cmdList.add(String.valueOf(durationS));
					
					cmdList.add("-acodec");
					cmdList.add("copy");
					cmdList.add("-y");
					cmdList.add(dstFile);
					String[] command=new String[cmdList.size()];  
				     for(int i=0;i<cmdList.size();i++){  
				    	 command[i]=(String)cmdList.get(i);  
				     }
				    return  executeVideoEditor(command);
				  
			  }else{
				  return VIDEO_EDITOR_EXECUTE_FAILED;
			  }
		  }
		  /**
		   * 
		   * 剪切mp4文件.(包括视频文件中的音频部分和视频部分),即把mp4文件中的一段剪切成独立的一个视频文件, 比如把一个30分钟的视频,裁剪其中的10秒钟等.
		   * @param videoFile  原视频文件 文件格式是mp4
		   * @param dstFile   裁剪后的视频路径， 路径的后缀名是.mp4
		   * @param startS   开始裁剪位置，单位是秒，
		   * @param durationS  需要裁剪的时长，单位秒，比如您可以从原视频的8.9秒出开始裁剪，裁剪２分钟，则这里的参数是　１２０
		   * @return
		   */
		  public int executeVideoCutOut(String videoFile,String dstFile,float startS,float durationS)
		  {
			  if(fileExist(videoFile)){
				
					List<String> cmdList=new ArrayList<String>();
					
					cmdList.add("-ss");
					cmdList.add(String.valueOf(startS));
					
					
			    	cmdList.add("-i");
					cmdList.add(videoFile);

					cmdList.add("-t");
					cmdList.add(String.valueOf(durationS));
					
					cmdList.add("-vcodec");
					cmdList.add("copy");
					cmdList.add("-acodec");
					cmdList.add("copy");
					cmdList.add("-y");
					cmdList.add(dstFile);
					String[] command=new String[cmdList.size()];  
				     for(int i=0;i<cmdList.size();i++){  
				    	 command[i]=(String)cmdList.get(i);  
				     }  
				    return  executeVideoEditor(command);
				  
			  }else{
				  return VIDEO_EDITOR_EXECUTE_FAILED;
			  }
		  }
		  /**
		   * 获取视频的所有帧图片,并保存到指定路径.
		   * 这条命令是把视频中的所有帧都提取成图片，适用于视频比较短的场合，比如一秒钟是２５帧，视频总时长是10秒，则会提取250帧图片，保存到您指定的路径
		   * @param videoFile  
		   * @param dstDir  目标文件夹绝对路径.
		   * @param jpgPrefix   保存图片文件的前缀，可以是png或jpg
		   * @return
		   * 
		   * ./ffmpeg -i tenSecond.mp4 -qscale:v 2 output_%03d.jpg
		   */
		  public int executeGetAllFrames(String videoFile,String dstDir,String jpgPrefix)
		  {
			  String dstPath=dstDir+jpgPrefix+"_%3d.jpeg";
			  if(fileExist(videoFile)){
				
					List<String> cmdList=new ArrayList<String>();
					
					cmdList.add("-vcodec");
					cmdList.add("lansoh264_dec");
					
			    	cmdList.add("-i");
					cmdList.add(videoFile);

					cmdList.add("-qscale:v");
					cmdList.add("2");
					
					cmdList.add(dstPath);

					cmdList.add("-y");
					
					String[] command=new String[cmdList.size()];  
				     for(int i=0;i<cmdList.size();i++){  
				    	 command[i]=(String)cmdList.get(i);  
				     }  
				    return  executeVideoEditor(command);
				  
			  }else{
				  return VIDEO_EDITOR_EXECUTE_FAILED;
			  }
		  }
		  /**
		   * 根据设定的采样,获取视频的几行图片.
		   * 假如视频时长是30秒,想平均取5张图片,则sampleRate=5/30;
		   * @param videoFile
		   * @param dstDir
		   * @param jpgPrefix
		   * @param sampeRate  一秒钟采样几张图片. 可以是小数.
		   * @return
		   * 
		   * ./ffmpeg -i 2x.mp4 -qscale:v 2 -vsync 1 -r 5/32 -f image2 r5r-%03d.jpeg
		   */
		  public int executeGetSomeFrames(String videoFile,String dstDir,String jpgPrefix,float sampeRate)
		  {
			  String dstPath=dstDir+jpgPrefix+"_%3d.jpeg";
			  if(fileExist(videoFile)){
				
					List<String> cmdList=new ArrayList<String>();
					
					cmdList.add("-vcodec");
					cmdList.add("lansoh264_dec");
					
			    	cmdList.add("-i");
					cmdList.add(videoFile);

					cmdList.add("-qscale:v");
					cmdList.add("2");
					
					cmdList.add("-vsync");
					cmdList.add("1");
					
					cmdList.add("-r");
					cmdList.add(String.valueOf(sampeRate));
					
					cmdList.add("-f");
					cmdList.add("image2");
					
					cmdList.add("-y");
					
					cmdList.add(dstPath);
					String[] command=new String[cmdList.size()];  
				     for(int i=0;i<cmdList.size();i++){  
				    	 command[i]=(String)cmdList.get(i);  
				     }  
				    return  executeVideoEditor(command);
				  
			  }else{
				  return VIDEO_EDITOR_EXECUTE_FAILED;
			  }
		  }
		  

		  /**
		   * 把mp4文件转换位TS流，
		   * 此命令和{＠link #executeConvertTsToMp4}结合,可以实现把多个mp4文件拼接成一个mp4文件。
		   * 适用在当你需要把录制好的多段视频拼接成一个mp4的场合，或者你先把一个mp4文件裁剪成多段，然后把其中几段视频拼接在一起
		   * 或者你想把两个视频增加一个转场的效果，
		   * @param mp4Path　输入的mp4文件路径
		   * @param dstTs　转换后保存的ts路径，后缀名需要是.ts
		   * @return
		   */
		  public int executeConvertMp4toTs(String mp4Path,String dstTs)
		  {
		  	//		  ./ffmpeg -i 0.mp4 -c copy -bsf:v h264_mp4toannexb -f mpegts ts0.ts
//		  ./ffmpeg -i 1.mp4 -c copy -bsf:v h264_mp4toannexb -f mpegts ts1.ts
//		  ./ffmpeg -i 2.mp4 -c copy -bsf:v h264_mp4toannexb -f mpegts ts2.ts
//		  ./ffmpeg -i 3.mp4 -c copy -bsf:v h264_mp4toannexb -f mpegts ts3.ts
//		  ./ffmpeg -i "concat:ts0.ts|ts1.ts|ts2.ts|ts3.ts" -c copy -bsf:a aac_adtstoasc out2.mp4

			  if(fileExist(mp4Path)){
				
					List<String> cmdList=new ArrayList<String>();
					
			    	cmdList.add("-i");
					cmdList.add(mp4Path);

					cmdList.add("-c");
					cmdList.add("copy");
					
					cmdList.add("-bsf:v");
					cmdList.add("h264_mp4toannexb");
					
					cmdList.add("-f");
					cmdList.add("mpegts");
					
					cmdList.add("-y");
					cmdList.add(dstTs);
					String[] command=new String[cmdList.size()];  
				     for(int i=0;i<cmdList.size();i++){  
				    	 command[i]=(String)cmdList.get(i);  
				     }  
				    return  executeVideoEditor(command);
			  }else{
				  return VIDEO_EDITOR_EXECUTE_FAILED;
			  }
		  }
		  /**
		   * 把多段ｔｓ流拼接在一起，然后保存成mp4格式
		   * 注意:输入的各个流需要编码参数一致,
		   * 适用于断点拍照,拍照多段视频; 或者想在两段视频中增加一个转场的视频
		   * @param tsArray　多段ts流的数组
		   * @param dstFile　　处理后保存的路径,文件后缀名需要是.mp4
		   * @return
		   * ./ffmpeg -i "concat:ts0.ts|ts1.ts|ts2.ts|ts3.ts" -c copy -bsf:a aac_adtstoasc out2.mp4
		   */
		  public int executeConvertTsToMp4(String[] tsArray,String dstFile)
		  {
			  if(filesExist(tsArray)){
				
				    String concat="concat:";
				    for(int i=0;i<tsArray.length-1;i++){
				    	concat+=tsArray[i];
				    	concat+="|";
				    }
				    concat+=tsArray[tsArray.length-1];
				    	
					List<String> cmdList=new ArrayList<String>();
					
			    	cmdList.add("-i");
					cmdList.add(concat);

					cmdList.add("-c");
					cmdList.add("copy");
					
					cmdList.add("-bsf:a");
					cmdList.add("aac_adtstoasc");
					
					cmdList.add("-y");
					
					cmdList.add(dstFile);
					String[] command=new String[cmdList.size()];  
				     for(int i=0;i<cmdList.size();i++){  
				    	 command[i]=(String)cmdList.get(i);  
				     }  
				    return  executeVideoEditor(command);
			  }else{
				  return VIDEO_EDITOR_EXECUTE_FAILED;
			  }
		  }
		  
		  /**
		   * 裁剪一个mp4分辨率，把视频画面的某一部分裁剪下来，
		   * 
		   * @param videoFile　需要裁剪的视频文件
		   * @param cropWidth　裁剪的宽度
		   * @param cropHeight 　裁剪的宽度
		   * @param x  　视频画面开始的Ｘ坐标，　从画面的左上角开始是0.0坐标
		   * @param y 视频画面开始的Y坐标，
		   * @param dstFile 处理后保存的路径,后缀需要是mp4
		   * @param codecname  使用的解码器的名字
		   * @param bitrate  <============注意:这里的bitrate在设置的时候, 因为是设置编码器的恒定码率, 推荐设置为 预设值的1.5倍为准, 比如视频原有的码率是1M,则裁剪一半,预设值可能是500k, 
		   * 这里推荐是为500k的1.5,因为原有的视频大部分是动态码率VBR,可以认为通过{@link MediaInfo} 得到的 {@link MediaInfo#vBitRate}是平均码率,这里要设置,推荐是1.5倍为好.
		   * @return
		   */
		  public int executeVideoFrameCrop(String videoFile,int cropWidth,int cropHeight,int x,int y,String dstFile,String codecname,int bitrate)
		  {
			  if( fileExist(videoFile)){
					
					String cropcmd=String.format(Locale.getDefault(),"crop=%d:%d:%d:%d",cropWidth,cropHeight,x,y);
//					
					int ret=executeFrameCrop(videoFile,codecname,cropcmd,dstFile,bitrate);
					if(ret!=0){  //执行失败
						Log.w(TAG,"video editor execute video frmae crop  error,switch to software decoder...");
						ret=executeFrameCrop(videoFile,"h264",cropcmd,dstFile,bitrate);  //采用软解
					}
					return ret;
			  }else{
				  return VIDEO_EDITOR_EXECUTE_FAILED;
			  }
		  }
		  //内部使用
		  private int executeFrameCrop(String videoFile,String codecname,String filter,String dstFile,int bitrate)
		  {
			  List<String> cmdList=new ArrayList<String>();
//				
				cmdList.add("-vcodec");
				cmdList.add(codecname);
				
				cmdList.add("-i");
				cmdList.add(videoFile);

				cmdList.add("-vf");
				cmdList.add(filter);
				
				cmdList.add("-acodec");
				cmdList.add("copy");
				
				cmdList.add("-vcodec");
				cmdList.add("lansoh264_enc"); 
				
				cmdList.add("-b:v");
				cmdList.add(String.valueOf(bitrate)); 
				
				cmdList.add("-pix_fmt");  //<========请注意, 使用lansoh264_enc编码器编码的时候,请务必指定格式,因为底层设计只支持yuv420p的输出.
				cmdList.add("yuv420p");
				
				cmdList.add("-y");
				
				cmdList.add(dstFile);
				String[] command=new String[cmdList.size()];  
			     for(int i=0;i<cmdList.size();i++){  
			    	 command[i]=(String)cmdList.get(i);  
			     } 
			     return  executeVideoEditor(command);
		  }
		  
		  /**
		   *此视频缩放算法，采用是软缩放来实现，速度特慢, 不建议使用.　我们有更快速的视频缩放方法，请联系我们
		   * 视频画面缩放, 务必保持视频的缩放后的宽高比,等于原来视频的宽高比.
		   * 
		   * @param videoFile
		   * @param scaleWidth
		   * @param scaleHeight
		   * @param dstFile
		   * @param bitrate  <============注意:这里的bitrate在设置的时候, 因为是设置编码器的恒定码率, 推荐设置为 预设值的1.5倍为准, 比如视频原有的码率是1M,则裁剪一半,预设值可能是500k, 
		   * 这里推荐是为500k的1.5,因为原有的视频大部分是动态码率VBR,可以认为通过{@link MediaInfo} 得到的 {@link MediaInfo#vBitRate}是平均码率,这里要设置,推荐是1.5倍为好.
		   * @return
		   */
		  public int executeVideoFrameScale(String videoFile,int scaleWidth,int scaleHeight,String dstFile,int bitrate){
			  if(fileExist(videoFile)){
					
					List<String> cmdList=new ArrayList<String>();
					String scalecmd=String.format(Locale.getDefault(),"scale=%d:%d",scaleWidth,scaleHeight);
					
					cmdList.add("-vcodec");
					cmdList.add("lansoh264_dec");
					
					cmdList.add("-i");
					cmdList.add(videoFile);

					cmdList.add("-vf");
					cmdList.add(scalecmd);
					
					cmdList.add("-acodec");
					cmdList.add("copy");
					
					cmdList.add("-vcodec");
					cmdList.add("lansoh264_enc"); 
					
					cmdList.add("-b:v");
					cmdList.add(String.valueOf(bitrate)); 
										
					cmdList.add("-pix_fmt");   //<========请注意, 使用lansoh264_enc编码器编码的时候,请务必指定格式,因为底层设计只支持yuv420p的输出.
					cmdList.add("yuv420p");
					
					cmdList.add("-y");
					
					cmdList.add(dstFile);
					String[] command=new String[cmdList.size()];  
				     for(int i=0;i<cmdList.size();i++){  
				    	 command[i]=(String)cmdList.get(i);  
				     }  
				    return  executeVideoEditor(command);
			  }else{
				  return VIDEO_EDITOR_EXECUTE_FAILED;
			  }
		  }
		  
		  /**
		   * 
		   * 对视频画面进行裁剪,裁剪后叠加一个png类型的图片,
		   * 
		   * 等于把裁剪,叠加水印,压缩三条命令放在一次执行, 这样只解码一次,和只编码一次,极大的加快了处理速度.
		   * 
		   * @param videoFile 原视频
		   * @param decCodec 解码器, 由{@link MediaInfo#vCodecName}填入
		   * @param pngPath
		   * @param cropX   画面裁剪的X坐标, 左上角为0:0
		   * @param cropY    画面裁剪的Y坐标
		   * @param cropWidth  画面裁剪宽度. 须小于等于源视频宽度
		   * @param cropHeight  画面裁剪高度, 须小于等于源视频高度
		   * @param overX   画面和png图片开始叠加的X坐标.
		   * @param overY   画面和png图片开始叠加的Y坐标
		   * @param dstFile  保存路径.
		   * @param bitrate  在视频编码的过程中,调整视频的码率, 如降低码率, 可以压缩的效果,但如果比源画面过于小,则可能出现马赛克, 建议看我们的例子的计算方法.
		   * @return
		   */
		  public int executeCropOverlay(String videoFile,String decCodec, String pngPath,int cropX,int cropY,int cropWidth,int cropHeight,int overX,int overY,String dstFile,int bitrate)
		  {
			  ////ffmpeg -i test_720p.mp4 -i watermark.png -filter_complex "[0:v]crop=640:640:0:40 [crop];[crop][1:v] overlay=0:0" -acodec copy -y xx.mp4
			  if(fileExist(videoFile))
			  {
					String filter=String.format(Locale.getDefault(),"[0:v]crop=%d:%d:%d:%d [crop];[crop][1:v] overlay=%d:%d",cropWidth,cropHeight,cropX,cropY,overX,overY);
					int ret=framecropoverlay(videoFile, decCodec, pngPath, filter, dstFile, bitrate);
					if(ret!=0){
						ret=framecropoverlay(videoFile, "h264", pngPath, filter, dstFile, bitrate);
					}
					return ret;
			  }else{
				  return VIDEO_EDITOR_EXECUTE_FAILED;
			  }
		  }
		  //内部使用
		  private int framecropoverlay(String videoFile,String decCodec, String pngPath,String filter,String dstFile,int bitrate)
		  {
			  List<String> cmdList=new ArrayList<String>();
				
				cmdList.add("-vcodec");
				cmdList.add(decCodec);
				
				cmdList.add("-i");
				cmdList.add(videoFile);

				cmdList.add("-i");
				cmdList.add(pngPath);
				
				cmdList.add("-filter_complex");
				cmdList.add(filter);
				
				cmdList.add("-acodec");
				cmdList.add("copy");
				
				cmdList.add("-vcodec");
				cmdList.add("lansoh264_enc"); 
				
				cmdList.add("-b:v");
				cmdList.add(String.valueOf(bitrate)); 
									
				cmdList.add("-pix_fmt");  
				cmdList.add("yuv420p");
				
				cmdList.add("-y");
				
				cmdList.add(dstFile);
				String[] command=new String[cmdList.size()];  
			     for(int i=0;i<cmdList.size();i++){  
			    	 command[i]=(String)cmdList.get(i);  
			     }  
			    return  executeVideoEditor(command);
			  
		  }
		  
		  /**
		   * 同时执行 视频时长剪切, 画面裁剪和增加水印的功能.
		   * @param videoFile  源视频文件.
		   * @param decCodec   源视频解码器
		   * @param pngPath   增加的水印文件路径
		   * @param startTimeS   时长剪切的开始时间
		   * @param duationS   时长剪切的 总长度
		   * @param cropX   画面裁剪的 X坐标,(最左边坐标是0)
		   * @param cropY  画面裁剪的Y坐标,(最上面坐标是0)
		   * @param cropWidth   画面裁剪宽度
		   * @param cropHeight  画面裁剪高度
		   * @param overX   增加水印的X坐标
		   * @param overY   增加水印的Y坐标
		   * @param dstFile  目标文件路径
		   * @param bitrate   设置在压缩时采用的bitrate.
		   * @return
		   */
		  public int executeVideoCutCropOverlay(String videoFile,String decCodec, String pngPath,float startTimeS,float duationS,int cropX,int cropY,int cropWidth,int cropHeight,int overX,int overY,String dstFile,int bitrate)
		  {
			  ////ffmpeg -i test_720p.mp4 -i watermark.png -filter_complex "[0:v]crop=640:640:0:40 [crop];[crop][1:v] overlay=0:0" -acodec copy -y xx.mp4
			  if(fileExist(videoFile))
			  {
					String filter=String.format(Locale.getDefault(),"[0:v]crop=%d:%d:%d:%d [crop];[crop][1:v] overlay=%d:%d",cropWidth,cropHeight,cropX,cropY,overX,overY);
					int ret=videoCutCropOverlay(videoFile, decCodec, pngPath, startTimeS, duationS, filter, dstFile, bitrate);
					if(ret!=0){
						ret= videoCutCropOverlay(videoFile, "h264", pngPath, startTimeS, duationS, filter, dstFile, bitrate);
					}
					return ret;
			  }else{
				  return VIDEO_EDITOR_EXECUTE_FAILED;
			  }
		  }
		  //内部使用
		  private int videoCutCropOverlay(String videoFile,String decCodec, String pngPath,float startTimeS,float duationS,String filter,String dstFile,int bitrate)
		  {
			  	List<String> cmdList=new ArrayList<String>();
				cmdList.add("-vcodec");
				cmdList.add(decCodec);
				
				cmdList.add("-ss");
				cmdList.add(String.valueOf(startTimeS));
				
				cmdList.add("-t");
				cmdList.add(String.valueOf(duationS));
				
				cmdList.add("-i");
				cmdList.add(videoFile);

				cmdList.add("-i");
				cmdList.add(pngPath);
				
				cmdList.add("-filter_complex");
				cmdList.add(filter);
				
				cmdList.add("-acodec");
				cmdList.add("copy");
				
				cmdList.add("-vcodec");
				cmdList.add("lansoh264_enc"); 
				
				cmdList.add("-b:v");
				cmdList.add(String.valueOf(bitrate)); 
									
				cmdList.add("-pix_fmt");   //<========请注意, 使用lansoh264_enc编码器编码的时候,请务必指定格式,因为底层设计只支持yuv420p的输出.
				cmdList.add("yuv420p");
				
				cmdList.add("-y");
				
				cmdList.add(dstFile);
				String[] command=new String[cmdList.size()];  
			     for(int i=0;i<cmdList.size();i++){  
			    	 command[i]=(String)cmdList.get(i);  
			     }  
			    return  executeVideoEditor(command);
		  }
		  
		  
		  /**
		   * 把多张图片转换为视频
		   * 注意：　这里的多张图片必须在同一个文件夹下，并且命名需要有规律,比如名字是 r5r_001.jpeg r5r_002.jpeg, r5r_003.jpeg等
		   * 多张图片，需要统一的分辨率，如分辨率不同，则以第一张图片的分辨率为准，后面的分辨率自动缩放到第一张图片的分辨率带大小
		   * @param picDir　保存图片的文件夹
		   * @param jpgprefix　图片的文件名有规律的前缀
		   * @param framerate　每秒钟需要显示几张图片
		   * @param dstPath　　处理后保存的路径，需要文件后缀是.mp4
		   * @param bitrate  <============注意:这里的bitrate在设置的时候, 因为是设置编码器的恒定码率, 推荐设置为 预设值的1.5倍为准, 比如视频原有的码率是1M,则裁剪一半,预设值可能是500k, 
		   * 这里推荐是为500k的1.5,因为原有的视频大部分是动态码率VBR,可以认为通过{@link MediaInfo} 得到的 {@link MediaInfo#vBitRate}是平均码率,这里要设置,推荐是1.5倍为好.
		   * @return
		   */
		  //./ffmpeg -framerate 1 -i r5r-%03d.jpeg -c:v libx264 -r 25 -pix_fmt yuv420p out33.mp4
		  public int executeConvertPictureToVideo(String picDir,String jpgprefix,float framerate,String dstPath,int bitrate){
					
			  		String picSet=picDir+jpgprefix+"_%3d.jpeg";
			  
					List<String> cmdList=new ArrayList<String>();
					
					cmdList.add("-framerate");
					cmdList.add(String.valueOf(framerate));
					
					cmdList.add("-i");
					cmdList.add(picSet);

					cmdList.add("-c:v");
					cmdList.add("lansoh264_enc"); 
					
					cmdList.add("-r");
					cmdList.add("25");
					
					cmdList.add("-b:v");
					cmdList.add(String.valueOf(bitrate)); 
					
					cmdList.add("-pix_fmt"); //<========请注意, 使用lansoh264_enc编码器编码的时候,请务必指定格式,因为底层设计只支持yuv420p的输出.
					cmdList.add("yuv420p"); 
					
					cmdList.add("-y");
					
					cmdList.add(dstPath);
					String[] command=new String[cmdList.size()];  
				     for(int i=0;i<cmdList.size();i++){  
				    	 command[i]=(String)cmdList.get(i);  
				     }  
				    return  executeVideoEditor(command);
		  }
		  
		 
		  /**
		   * 为视频增加图片，图片可以是带透明的png类型，也可以是jpg类型;
		   * 适用在为视频增加logo，或增加一些好玩的图片的场合，
		   * 以下两条方法，也是叠加图片，不同的是可以指定叠加时间段
		   * 我们有另外的视频叠加图片，视频叠加视频的类，可以实现视频或图片的缩放，移动，旋转等动作，请联系我们
		   * @param videoFile　原视频
		   * @param imagePngPath　　png图片的路径
		   * @param x　　叠加图片相对于视频的Ｘ坐标，视频的左上角为坐标原点0.0
		   * @param y　　叠加图片相对于视频的Ｙ坐标
		   * @param dstFile　　处理后保存的路径，后缀需要是.mp4格式
		   * @param bitrate  <============注意:这里的bitrate在设置的时候, 因为是设置编码器的恒定码率, 推荐设置为 预设值的1.2倍为准, 比如视频原有的码率是1M,则裁剪一半,预设值可能是500k, 
		   * 这里推荐是为500k的1.5,因为原有的视频大部分是动态码率VBR,可以认为通过{@link MediaInfo} 得到的 {@link MediaInfo#vBitRate}是平均码率,这里要设置,推荐是1.5倍为好.
		   * 
		   * bitrate如果设置低一些, 可以起到压缩视频的效果.
		   * @return
		   */
		  public int executeAddWaterMark(String videoFile,String imagePngPath,int x,int y,String dstFile,int bitrate){
			  //./ffmpeg -i miaopai.mp4 -i watermark.png -filter_complex "overlay=0:0" -acodec copy out2.mp4  
			  
			  if(fileExist(videoFile)){
					String filter=String.format(Locale.getDefault(),"overlay=%d:%d",x,y);
					int ret=videoAddWatermark(videoFile,"lansoh264_dec",imagePngPath, filter, dstFile, bitrate);
					if(ret!=0){
						ret=videoAddWatermark(videoFile,"h264",imagePngPath, filter, dstFile, bitrate);
					}
					return ret;
			  }else{
				  return VIDEO_EDITOR_EXECUTE_FAILED;
			  }
		  }
		 /**
		  * 为视频增加图片，图片可以是带透明的png类型，也可以是jpg类型;
		  * 适用在为视频增加logo，或增加一些好玩的图片的场合，
		  * 以下两条方法，也是叠加图片，不同的是可以指定叠加时间段
		  * 在某段时间区间内叠加.
		  * @param videoFile
		  * @param imagePngPath
		  * @param startTimeS　　开始时间，单位是秒，类型float，比如从20.8秒处开始
		  * @param endTimeS　　　结束时间，单位是秒，类型float 比如在30秒处结束
		  * @param x　　叠加图片相对于视频的Ｘ坐标，视频的左上角为坐标原点0.0
		  * @param y　　叠加图片相对于视频的Ｙ坐标
		  * @param dstFile  处理后保存的路径，后缀需要是mp4格式
		  * @param bitrate  <============注意:这里的bitrate在设置的时候, 因为是设置编码器的恒定码率, 推荐设置为 预设值的1.5倍为准, 比如视频原有的码率是1M,则裁剪一半,预设值可能是500k, 
		   * 这里推荐是为500k的1.5,因为原有的视频大部分是动态码率VBR,可以认为通过{@link MediaInfo} 得到的 {@link MediaInfo#vBitRate}是平均码率,这里要设置,推荐是1.5倍为好.
		  * @return
		  */
		  public int executeAddWaterMark(String videoFile,String imagePngPath,float startTimeS,float endTimeS,int x,int y,String dstFile,int bitrate)
		  {
			// ./ffmpeg -i miaopai.mp4 -i test.png -filter_complex "[0:v][1:v] overlay=25:25:enable='between(t,0,5)'" -pix_fmt yuv420p -c:a copy output33.mp4
			  if(fileExist(videoFile)){
					List<String> cmdList=new ArrayList<String>();
					String filter=String.format(Locale.getDefault(),"overlay=%d:%d:enable='between(t,%f,%f)",x,y,startTimeS,endTimeS);
					int ret=videoAddWatermark(videoFile,"lansoh264_dec",imagePngPath, filter, dstFile, bitrate);
					if(ret!=0){
						ret=videoAddWatermark(videoFile,"h264",imagePngPath, filter, dstFile, bitrate);
					}
					return ret;
			  }else{
				  return VIDEO_EDITOR_EXECUTE_FAILED;
			  }
		  }
		  //内部使用, 视频上增加水印.
		  private int videoAddWatermark(String videoFile,String decName,String imagePngPath,String filter,String dstFile,int bitrate)
		  {
			  	List<String> cmdList=new ArrayList<String>();
				cmdList.add("-vcodec");
				cmdList.add(decName);
				
				cmdList.add("-i");
				cmdList.add(videoFile);

				cmdList.add("-i");
				cmdList.add(imagePngPath);

				cmdList.add("-filter_complex");
				cmdList.add(filter);
				
				cmdList.add("-acodec");
				cmdList.add("copy");
				
				cmdList.add("-vcodec");
				cmdList.add("lansoh264_enc"); 
				
				cmdList.add("-b:v");
				cmdList.add(String.valueOf(bitrate)); 
				
				cmdList.add("-pix_fmt");   //<========请注意, 使用lansoh264_enc编码器编码的时候,请务必指定格式,因为底层设计只支持yuv420p的输出.
				cmdList.add("yuv420p");
				
				cmdList.add("-y");
				cmdList.add(dstFile);
				String[] command=new String[cmdList.size()];  
			     for(int i=0;i<cmdList.size();i++){  
			    	 command[i]=(String)cmdList.get(i);  
			     }  
			    return  executeVideoEditor(command);
		  }
		
			/**
			 * 给视频旋转角度,注意这里 只是 旋转画面的的角度,而不会调整视频的宽高.
			 * @param srcPath　需要旋转角度的原视频
			 * @param decoder　　视频的解码器名字
			 * @param angle　　角度
			 * @param dstPath　　处理后的视频存放的路径,后缀需要是.mp4
			 * @return
			 */
		  public int executeRotateAngle(String srcPath,String decoder,float angle,String dstPath,int bitrate)
		  {
			  ////ffmpeg -i miaopai.mp4 -vf "rotate=45*(PI/180),format=yuv420p" -metadata:s:v rotate=0 -codec:v libx264 -codec:a copy output.mp4
			  if(fileExist(srcPath)){
					
				  String filter=String.format(Locale.getDefault(),"rotate=%f*(PI/180),format=yuv420p",angle);
				  
					List<String> cmdList=new ArrayList<String>();
					
					cmdList.add("-vcodec");
					cmdList.add(decoder);
					
					cmdList.add("-i");
					cmdList.add(srcPath);
					
					cmdList.add("-vf");
					cmdList.add(filter);
					
					cmdList.add("-metadata:s:v");
					cmdList.add("rotate=0");
					cmdList.add("-acodec");
					cmdList.add("copy");
					cmdList.add("-y");
					cmdList.add("-vcodec");
					cmdList.add("lansoh264_enc");
					
					cmdList.add("-b:v");
					cmdList.add(String.valueOf(bitrate)); 
					
					cmdList.add("-pix_fmt");
					cmdList.add("yuv420p");
					
					cmdList.add(dstPath);
					 
					String[] command=new String[cmdList.size()];  
				     for(int i=0;i<cmdList.size();i++){  
				    	 command[i]=(String)cmdList.get(i);  
				     }  
				    return  executeVideoEditor(command);
				  
			  }else{
				  return VIDEO_EDITOR_EXECUTE_FAILED;
			  }
		  }
		 

		  
}
