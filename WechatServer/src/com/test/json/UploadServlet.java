package com.test.json;


import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.UUID;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.codec.binary.Base64;
import org.apache.commons.fileupload.FileItem;
import org.apache.commons.fileupload.FileUploadBase.FileSizeLimitExceededException;
import org.apache.commons.fileupload.ProgressListener;
import org.apache.commons.fileupload.disk.DiskFileItemFactory;
import org.apache.commons.fileupload.servlet.ServletFileUpload;
import org.json.JSONArray;
import org.json.JSONObject;

import com.baidu.aip.speech.AipSpeech;


public class UploadServlet extends HttpServlet {
    private static final long serialVersionUID = 1L;
    //public static String packageName = "/home/vtemp/";
   public static String packageName = "/Users/xujiayunew/";
    // 定义允许上传的文件扩展名
    private String Ext_Name = "gif,jpg,jpeg,png,bmp,swf,flv,mp3,wav,wma,wmv,mid,avi,mpg,asf,rm,rmvb,doc,docx,xls,xlsx,ppt,htm,html,txt,zip,rar,gz,bz2";
    public static final String APP_ID = "10037793";
    public static final String API_KEY = "1tjY14PnMCT7jCZz6HkeMlho";
    public static final String SECRET_KEY = "9SCywAatYbaDXviPPjZnfLqq8DMP1M78";
    /**录音文件存储的地方*/
    public String topName = "/home/apache-tomcat-8.5.20/webapps/JsonTest/WEB-INF/upload/";
    
    /**
     * @see HttpServlet#doGet(HttpServletRequest request, HttpServletResponse
     *      response)
     */
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

    	System.out.println("UploadServlet-doGet");
        // 得到上传文件的保存目录，将上传文件存放在WEB-INF目录下，不允许外界直接访问，保证上传文件的安全
        String savePath = this.getServletContext().getRealPath("WEB-INF/upload");
        System.out.println("savePath="+savePath);
        File saveFileDir = new File(savePath);
        if (!saveFileDir.exists()) {
            // 创建临时目录
            saveFileDir.mkdirs();
        }
        
        // 上传时生成临时文件保存目录
        String tmpPath = this.getServletContext().getRealPath("WEB-INF/tem");
        File tmpFile = new File(tmpPath);
        if (!tmpFile.exists()) {
            // 创建临时目录
            tmpFile.mkdirs();
        }

        // 消息提示
        String message = "";
        try {
            // 使用Apache文件上传组件处理文件上传步骤：
            // 1.创建一个DiskFileItemFactory工厂
            DiskFileItemFactory factory = new DiskFileItemFactory();
            // 设置工厂的缓冲区的大小，当上传的文件大小超过缓冲区的大小时，就会生成一个临时文件存放到指定的临时目录当中
            factory.setSizeThreshold(1024 * 10);// 设置缓冲区的大小为100KB，如果不指定，那么默认缓冲区的大小是10KB
            // 设置上传时生成的临时文件的保存目录
            factory.setRepository(tmpFile);
            // 2.创建一个文件上传解析器
            ServletFileUpload upload = new ServletFileUpload(factory);
            // 监听文件上传进度
            upload.setProgressListener(new ProgressListener() {

                @Override
                public void update(long readedBytes, long totalBytes, int currentItem) {
                    // TODO Auto-generated method stub
                    System.out.println("current modify：" + readedBytes + "-----------file size：" + totalBytes + "--" + currentItem);
                }
            });
            // 解决上传文件名的中文乱码问题
            upload.setHeaderEncoding("UTF-8");
            // 3.判断提交上来的数据是否是上传表单的数据
//            if (!ServletFileUpload.isMultipartContent(request)) {
//                // 按照传统方式获取数据
//                return;
//            }
            // 设置上传单个文件的最大值
            upload.setFileSizeMax(1024 * 1024 * 10);// 10M
            // 设置上传文件总量的最大值，就是本次上传的所有文件的总和的最大值
            upload.setSizeMax(1024 * 1024 * 100);// 10M
            List<FileItem> items = upload.parseRequest(request);
            Iterator<FileItem> itr = items.iterator();
            while (itr.hasNext()) {
                FileItem item = (FileItem) itr.next();
                // 如果fileitem中封装的是普通的输入想数据
                if (item.isFormField()) {
                	System.out.println("item.isFormField()");
                    String name = item.getFieldName();
                    // 解决普通输入项数据中文乱码问题
                    String value = item.getString("UTF-8");
                    // value = new String(value.getBytes("iso8859-1"),"UTF-8");
                    System.out.println(name + "=" + value);
                } else// 如果fileitem中封装的是上传文件
                {// 得到上传文件的文件名
                	System.out.println("!item.isFormField()");
                    String fileName = item.getName();
                    System.out.println("filename：" + fileName);
                    if (fileName == null && fileName.trim().length() == 0) {
                        continue;
                    }
                    // 注意：不同的浏览器提交的文件名是不一样的，有些浏览器提交上来的文件名是带有路径的
                    // 如: C:\Users\H__D\Desktop\1.txt 而有些则是 ： 1.txt
                    // 处理获取到的上传文件的文件名的路径部分，只保留文件名部分
                  //  fileName = fileName.substring(fileName.lastIndexOf("\\") + 1);
                    // 得到上传文件的扩展名
                    String fileExt = fileName.substring(fileName.lastIndexOf(".") + 1).toLowerCase();
                    // 检查扩展名
                    // 如果需要限制上传的文件类型，那么可以通过文件的扩展名来判断上传的文件类型是否合法
                    System.out.println("file .name is：" + fileExt);
                    // 检查文件大小
                    if(item.getSize() == 0) continue;
                    if(item.getSize() > 1024 * 1024 * 10){
                        System.out.println("upload file size：" + item.getSize());
                        message = message + "file：" + fileName + "，bigger than we can receive：" + upload.getFileSizeMax() + "<br/>";
                        break;
                    }
                    
                    
                    
                    String fileProname = fileName.substring(0,fileName.lastIndexOf(".") );
                    // 得到存文件的文件名
                    String saveFileName = makeFileName(fileName);
                    message = message + "file：" + fileName + "，load success<br/>";
                    JSONObject res = new JSONObject();
                    
                    //保存文件方法二
                    File file = new File(savePath, saveFileName);
                    item.write(file);
                    System.out.println("finish write file:"+file.getAbsolutePath()+" and "+file.getName());
                    //item
                    try {
        				//将微信中的webm格式的base64字符提取出来
        				BufferedReader reader = new BufferedReader(new InputStreamReader(new FileInputStream(file)));
        				StringBuilder sb = new StringBuilder("");
        				String temp;
        				while((temp=reader.readLine())!=null)
        				{
        					sb.append(temp);
        				}
        				reader.close();
        				temp = sb.toString();
        				int index = temp.indexOf("base64,");
        				temp = temp.substring(index+7);
        				System.out.println(temp.substring(0,3));
        				byte[] nfile = decodebase64(temp);
        				//以字节的	方式写入新文件
//        				File webm = new File(packageName+fileProname+".webm");
        				String webm = savePath+"/"+fileProname+".webm";
        				String wav =savePath+"/"+ fileProname+".wav";
        				System.out.println("webm = "+webm);
        				System.out.println("wav = "+wav);
        				FileOutputStream fos = new FileOutputStream(webm);
        				fos.write(nfile);
        				fos.close();
                    	
                    	
                    	
                    	//----这里开始更改，因为小程序上传的是slik格式的
        				//利用ffmpeg 将webm转化成mov
        				Runtime runtime = Runtime.getRuntime();
        				Process proce;
        			//	String cmd = "/usr/local/bin/ffmpeg -i /Users/xujiayunew/test.webm -y -vcodec libx264 -qp 0 -pix_fmt yuv420p -acodec pcm_s32le /Users/xujiayunew/video.mov";
        			//	proce = runtime.exec(cmd);
        				String cmd = "/usr/local/bin/ffmpeg -i "+webm+" -ac 1 -ar 16000 "+wav;
        			//改了
        				//	String cmd = "sh /home/apache-tomcat-8.5.20/webapps/JsonTest/converter.sh "+(topName+file.getName())+"  wav";
        				System.out.println("cmd = "+cmd);
        			//	String cmd = "/usr/local/bin/ffmpeg -i "+webm+" -ac 1 -ar 16000 "+wav;
        				
       // 				String newFileName = topName+file.getName().substring(0,file.getName().indexOf("."))+".wav";
        			//	File testFile = new File(newFileName);
        				
       // 				System.out.println("new wav file name ="+newFileName);
        				
        				proce = runtime.exec(cmd);
        				//int result = proce.waitFor();
        				proce.waitFor();
        				BufferedReader br = new BufferedReader(new InputStreamReader(proce.getInputStream()));  
       		            StringBuffer msb = new StringBuffer();  
       		            String line;  
       		            while ((line = br.readLine()) != null) {  
       		                msb.append(line).append("\n");  
       		            }  
       		            String mresult = msb.toString();  
       		            System.out.println("exec result = "+mresult);  
        				//System.out.println("result = "+mresult);
        				// 初始化一个FaceClient
        		        AipSpeech client = new AipSpeech(APP_ID, API_KEY, SECRET_KEY);
        		        // 可选：设置网络连接参数
        		        client.setConnectionTimeoutInMillis(2000);
        		        client.setSocketTimeoutInMillis(60000);
        		        System.out.println("试试看中文行不行");
       // 		         res = client.asr(newFileName, "wav", 16000, null);
        		        res = client.asr(wav, "wav", 16000, null);
        		        System.out.println(res.toString(4));
        		        response.setContentType("text/html; charset=UTF8");
        		        response.getWriter().print("start handle");
        		        JSONObject jb = new JSONObject(res.toString());
        		        JSONArray result = jb.getJSONArray("result");
        		        response.getWriter().print("get->"+result);
        		        response.getWriter().print("start handle");
        			} catch (Exception e) {
        				// TODO Auto-generated catch block
        				e.printStackTrace();
        			}
                    
                  //删除临时文件
                    item.delete();
                   
                    
                    //开始处理
                    
                    
                   // response.getWriter().print(res.toString());
                }

            }
            
        } catch (FileSizeLimitExceededException e) {
            message = message + "上传文件大小超过限制<br/>";
            e.printStackTrace();
        } catch (Exception e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } finally {
           // request.setAttribute("message", message);
            //request.getRequestDispatcher("/message.jsp").forward(request, response);
        }
    }
    public  byte[] decodebase64(String code)
	{
		return Base64.decodeBase64(code);
	}

    /**
     * @see HttpServlet#doPost(HttpServletRequest request, HttpServletResponse
     *      response)
     */
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        // TODO Auto-generated method stub
        doGet(request, response);
    }

    private String makeFileName(String fileName) {
        // 为防止文件覆盖的现象发生，要为上传文件产生一个唯一的文件名
        return UUID.randomUUID().toString().replaceAll("-", "") + "_" + fileName;

    }


}
