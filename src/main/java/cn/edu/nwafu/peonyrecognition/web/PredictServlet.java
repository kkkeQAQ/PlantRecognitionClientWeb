package cn.edu.nwafu.peonyrecognition.web;

import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.math.BigInteger;
import java.net.URLEncoder;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Arrays;
import java.util.Base64;
import java.util.Comparator;
import java.util.Locale;
import java.util.concurrent.CountDownLatch;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.json.JSONObject;

import com.google.protobuf.ByteString;

import io.grpc.ManagedChannel;
import io.grpc.ManagedChannelBuilder;
import io.grpc.stub.StreamObserver;
import proto.Predict;
import proto.PredictorGrpc;
import proto.PredictorGrpc.PredictorStub;

/**
 * Servlet implementation class PredictServlet
 */
@WebServlet(description = "predict", urlPatterns = { "/Predict" })
public class PredictServlet extends HttpServlet {
	private static final long serialVersionUID = 1L;
	
	private static final String HOST = "kkke.me";
    private static final int PORT = 2764;
    private static final double THRESHOLD = 0.50;

    public static final int OK=0;
    public static final int ERR=1;

    private ManagedChannel channel;
    private static final String FLOWER_NAME[] = {"大胡红",
            "岛锦",
            "锦袍红",
            "菱花湛露",
            "墨润绝伦",
            "霓虹幻彩",
            "肉芙蓉",
            "乌龙捧盛",
            "香玉",
            "雪映桃花",
            "姚黄"};
    
    
    
	public PredictServlet() {
		super();
		channel = ManagedChannelBuilder.forAddress(HOST, PORT)
                .usePlaintext()
                .build();
	}
	
	private static void writeError(PrintWriter out) {
		final JSONObject jsonobj = new JSONObject();
		jsonobj.put("code", ERR);
		jsonobj.put("msg", "error");
		out.write(jsonobj.toString());
	}
	
	/*public static byte[] md5(String s) throws IOException {
		MessageDigest md = null;
		try {
			md = MessageDigest.getInstance("md5");
		} catch (NoSuchAlgorithmException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		Base64.Encoder encoder=Base64.getEncoder();
        byte[] bytes = md.digest(s.getBytes("UTF-8"));
		return bytes;
	}*/
	private static final String KEY_MD5 = "MD5";
    // 全局数组
    private static final String[] strDigits = { "0", "1", "2", "3", "4", "5",
            "6", "7", "8", "9", "a", "b", "c", "d", "e", "f" };
	 private static String byteToArrayString(byte bByte) {
	        int iRet = bByte;
	        if (iRet < 0) {
	            iRet += 256;
	        }
	        int iD1 = iRet / 16;
	        int iD2 = iRet % 16;
	        return strDigits[iD1] + strDigits[iD2];
	    }
	private static String byteToString(byte[] bByte) {
        StringBuffer sBuffer = new StringBuffer();
        for (int i = 0; i < bByte.length; i++) {
            sBuffer.append(byteToArrayString(bByte[i]));
        }
        return sBuffer.toString();
    }
    /**
     * MD5加密
     * @param strObj
     * @return
     * @throws Exception
     */
    public static String GetMD5Code(String strObj) throws Exception{
        MessageDigest md = MessageDigest.getInstance(KEY_MD5);
        // md.digest() 该函数返回值为存放哈希值结果的byte数组
        return byteToString(md.digest(strObj.getBytes()));
    }



	/**
	 * @see HttpServlet#doPost(HttpServletRequest request, HttpServletResponse response)
	 */
	final JSONObject jsonobj = new JSONObject();
	protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		request.setCharacterEncoding("UTF-8");
		response.setContentType("application/json; charset=utf-8");
		PrintWriter out = response.getWriter();
		
		String dataUrl = request.getParameter("image");
		String dataUr2 = request.getParameter("md5");
        String dataUr3="";
		try {
			dataUr3 = GetMD5Code("image");//???
		} catch (Exception e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
		System.out.print(dataUr2);
		System.out.print("\n");
		System.out.print(dataUr3);
		if(!dataUr2.equals(dataUr3))
		{
			jsonobj.put("code", ERR);
			
			jsonobj.put("msg", "信息被篡改");
			out.write(jsonobj.toString());
			return;
		}
		String format = "";
		String data = "";
		
		final Pattern codePattern = Pattern.compile("data:(.*);base64,(.*)");
		Matcher matcher = codePattern.matcher(dataUrl);
        if (matcher.find()) {
            format = matcher.group(1);
            data = matcher.group(2);
        } else {
        	System.out.println(dataUrl);
        	writeError(out);
        	return;
        }
        
		
		final CountDownLatch countDownLatch = new CountDownLatch(1);
		
		PredictorStub predictorStub = PredictorGrpc.newStub(channel);
        StreamObserver<Predict.PredictRequest> stream = predictorStub.predict(new StreamObserver<Predict.PredictResponse>() {
            @Override
            public void onNext(Predict.PredictResponse value) {
                double[] probability = value.getResult().getProbabilityList().stream().mapToDouble(Float::doubleValue).toArray();
                Integer[] arg = new Integer[probability.length];
                for(int i = 0; i < arg.length; ++i) {
                    arg[i] = i;
                }
                Arrays.sort(arg, Comparator.comparingDouble((Integer i) -> -probability[i]));

                if (probability[arg[0]] < THRESHOLD) {
                    jsonobj.append("result", "不是牡丹花\n");
                }
                for(int i:arg) {
                	//jsonobj.append("result", String.format(Locale.getDefault(), "%s %.2f%%\n",FLOWER_NAME[i], 100 * probability[i]));
                	jsonobj.append("result", String.format(Locale.getDefault(), "%s",FLOWER_NAME[i]));
                }
            }

            @Override
            public void onError(Throwable t) {
                t.printStackTrace();
            	jsonobj.put("code", ERR);
            	jsonobj.put("msg", t.toString());
            	countDownLatch.countDown();
            }

            @Override
            public void onCompleted() {
            	jsonobj.put("code", OK);
            	countDownLatch.countDown();
            }
        });
        Base64.Decoder decoder2=Base64.getDecoder();
        byte[] bytes = decoder2.decode(data);
        ByteString bytestring = ByteString.copyFrom(bytes);
        Predict.Image image = Predict.Image.newBuilder()
                .setData(bytestring)
                .setFormat(format)
                .build();
        Predict.PredictRequest predictRequest = Predict.PredictRequest.newBuilder().setImage(image).build();
        stream.onNext(predictRequest);
        stream.onCompleted();
    	try {
			countDownLatch.await();
		} catch (InterruptedException e) {
		}
		out.write(jsonobj.toString());
	}

}