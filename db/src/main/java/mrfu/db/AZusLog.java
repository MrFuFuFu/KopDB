package mrfu.db;

/*
 * // 使用例子 所有打印Log的地方使用AZusLog 类来打印Log, 捕获异常的时候需要使用AZusLog.w(tag,Throwable t)方法
 *  在调用ApplicationHelper.initEnv(getApplicationContext()); 后马上调用
 * AZusLog.initLog(this, new UploadExceptionLogCallbackImpl(),true);
 * class UploadExceptionLogCallbackImpl implements UploadExceptionLogCallback {
 * @Override
	public void uploadExceptionLog(String logFileName) {
		ExceptionLogUploadRequest request=new ExceptionLogUploadRequest(LoginActivity.this);
		request.setLogFileName(logFileName);
		RequestParams reqParam=new RequestParams();
		reqParam.put("deviceid", ApplicationHelper.getDeviceId());
		request.aPostFile(logFileName, "logfile", reqParam.getUrlParams());
	}
  }
    class ExceptionLogUploadRequest extends ActivityASyncJsonHttpRequestBase{
	private String logFileName;
	public String getLogFileName() {
		return logFileName;
	}

	public void setLogFileName(String logFileName) {
		this.logFileName = logFileName;
	}
	public  void processFinish(){
		File f=new File(logFileName);
		f.delete();
	}
	@Override
	public String getUrl() {
		return "http://192.168.1.35:8080/lbslife/sys/uploadexceptionlog.json";
	}
	
}
 */

import android.content.Context;
import android.os.Environment;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.Looper;
import android.util.Log;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Timer;
import java.util.TimerTask;

public class AZusLog {
    private static boolean gbLogcatEnable=true;
    private static boolean gBInited=false;
    private static FileWriter fileWriter;
	private static PrintWriter logWriter;
	public static Handler gHandler;
	private static String gLastDay;
	private static String gLogFileName;
	private static Timer gTimer;
	
    public static boolean isLogEnable(){
    	return gbLogcatEnable;
    }
    //程序启动时上传一次，以及跨天时上传一次
    public static void initLog(Context context,String logServerUrl,boolean bLogcatEnable){
    	initLog(context,bLogcatEnable);
    }
    public static void initLog(Context context,boolean bLogcatEnable){
    	if(gBInited){
    		return;
    	}
    	gBInited=true;
    	gTimer=new Timer();
    	gbLogcatEnable=bLogcatEnable;
    	
    	HandlerThread ht = new HandlerThread("log");
		ht.start();
		gHandler = new Handler(ht.getLooper());
		
		gLastDay="";
//		gHandler = new LogHandler( ht.getLooper());
//    	String pkgName=context.getPackageName();
    	String logFileName = "";
    	
    	boolean sdCardExist = Environment.getExternalStorageState().equals(
				Environment.MEDIA_MOUNTED);
		String path = "";
		if (sdCardExist) {
			path = Environment.getExternalStorageDirectory().getAbsolutePath()
					+ "/nvshen/log/";
		} else {
			path = context.getCacheDir().getAbsolutePath() + "/nvshen/log/";
		}
		logFileName = path;
    	File f=new File(path);
    	f.mkdirs();
    	logFileName +="/exception.log";
    	gLogFileName=logFileName;
    	f=new File(logFileName);
    	
    	try {
			fileWriter = new FileWriter(logFileName, true);
			logWriter = new PrintWriter(fileWriter);
		} catch (IOException e) {
			e.printStackTrace();
		}
    	if(f.exists()){
    		gHandler.post(new Runnable(){
				@Override
				public void run() {
//					uploadLog();
				}
    			
    		});
    	} 
    	
    	
    	gTimer.scheduleAtFixedRate(new TimerTask(){
			@Override
			public void run() {
//				uploadLog();
			}
    	}, 86400*1000,86400*1000);
    }
//    public static void setLogEnable(boolean bLog){
//        gbLog=bLog;
//    }
    public static void e(final String tag,final  String msg){
    	if(gHandler == null){
    		return;
    	}
    	gHandler.post(new Runnable(){
			public void run() {
				if(gbLogcatEnable){
		            Log.e(tag, msg);
		        }
		        Log("ERROR",tag,msg);
			}
    	});
        
    }
    public static void d(final String tag,final String msg){
    	if(gHandler == null){
    		return;
    	}
		gHandler.post(new Runnable() {
			public void run() {
				if (gbLogcatEnable) {
					Log.d(tag, msg);
				}
			}
		});
    }
    public static void i(final String tag,final String msg){
    	if(gHandler == null){
    		return;
    	}
		gHandler.post(new Runnable() {
			public void run() {
				if (gbLogcatEnable) {
					Log.i(tag, msg);
				}
			}
		});
    }
    public static void v(final String tag,final String msg){
    	if(gHandler == null){
    		return;
    	}
		gHandler.post(new Runnable() {
			public void run() {
				if (gbLogcatEnable) {
					Log.v(tag, msg);
				}
			}
		});
    }
    public static void w(final String tag,final String msg){
//    	if(gHandler == null){
//    		return;
//    	}
//    	gHandler.post(new Runnable() {
//			public void run() {
//				if (gbLogcatEnable) {
//					Log.w(tag, msg);
//				}
//				Log("WARNING", tag, msg);
//			}
//		});
    	Log("WARNING", tag, msg);
    }
    public static void d(final String tag,final  Throwable tr) {
    	 d(tag,"",tr);
    }
    public static void d(final String tag, final String msg,final  Throwable tr) {
    	if(gHandler == null){
    		return;
    	}
		gHandler.post(new Runnable() {
			public void run() {
				if (gbLogcatEnable) {
					Log.d(tag, msg, tr);
				}
				Log("DEBUG", tag, msg, tr);
			}
		});
    }
    public static void v(final String tag,final  Throwable tr) {
    	v(tag,"",tr);
    }
    public static void v(final String tag, final String msg, final Throwable tr) {
    	if(gHandler == null){
    		return;
    	}
		gHandler.post(new Runnable() {
			public void run() {
				if (gbLogcatEnable) {
					Log.v(tag, msg, tr);
				}
				Log("VERBOSE", tag, msg, tr);
			}
		});
    }
    public static void i(final String tag, final Throwable tr) {
    	i(tag,"",tr);
    }
    public static void i(final String tag,final  String msg, final Throwable tr) {
    	if(gHandler == null){
    		return;
    	}
		gHandler.post(new Runnable() {
			public void run() {
				if (gbLogcatEnable) {
					Log.i(tag, msg, tr);
				}
				Log("INFO", tag, msg, tr);
			}
		});
    }
    public static void w(final String tag,final  Throwable tr) {
    	w(tag,"",tr);
    }
    public static void w(final String tag, final String msg,final  Throwable tr) {
    	if(gHandler == null){
    		return;
    	}
		gHandler.post(new Runnable() {
			public void run() {
				if (gbLogcatEnable) {
					Log.w(tag, msg, tr);
				}
				Log("WARNING", tag, msg, tr);
			}
		});
    }
    public static void e(final String tag, final  Throwable tr) {
    	e(tag,"",tr);
    }
    public static void e(final String tag,final  String msg, final Throwable tr) {
    	if(gHandler == null){
    		return;
    	}
		gHandler.post(new Runnable() {
			public void run() {
				if (gbLogcatEnable) {
					Log.e(tag, msg, tr);
				}
				Log("ERROR", tag, msg, tr);
			}
		});
    }
    public static void wtf(final String tag,final  Throwable tr) {
    	wtf(tag,"",tr);
    }
    public static void wtf(final String tag, final String msg,final  Throwable tr) {
    	if(gHandler == null){
    		return;
    	}
		gHandler.post(new Runnable() {
			public void run() {
				if (gbLogcatEnable) {
					Log.wtf(tag, msg, tr);
				}
				Log("WTF", tag, msg, tr);
			}
		});
    }
    static void Log(final String level,final String tag,final String msg ){
    	Log(level,tag,msg,null);
    }
    static void Log(final String level,final String tag,final String msg,final Throwable tr){
    	try{
			StringBuffer line = new StringBuffer();
			line.append("[").append(level).append("] [")
					.append(getSystemDateall()).append("] ");
			line.append("[").append(tag).append("] [").append(msg)
					.append("] \n");
			if (null != tr) {
				String stackString = Log.getStackTraceString(tr);
				line.append("[StackTrace] \t").append(stackString);
			}
			logWriter.println(line);
			logWriter.flush();
			
			
			//切换日期了，上传给服务器
			
    	}catch(Throwable t){
    		t.printStackTrace();
    	}
    }
    static class LogHandler extends Handler {
    	LogHandler(Looper looper){
    		super(looper);
    	}
    }

	private static final DateFormat ymdhmsFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
	/*
         * 系统时间的转换有分秒的
         */
	public static String getSystemDateall() {
		return ymdhmsFormat.format(new Date()).toString();
	}
}
