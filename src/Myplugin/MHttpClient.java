package Myplugin;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.util.EntityUtils;

import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.codec.Charsets;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.NameValuePair;
public class MHttpClient {
    public void get(String url) throws Exception {
        // 创建HttpClient实例
        HttpClient client =  HttpClientBuilder.create().build();
        // 根据URL创建HttpGet实例
        HttpGet get = new HttpGet(url);
        String src_method = "public Map<MetricName , ? extends Metric > metrics() {\n" +
                "                for (final StreamThread thread : threads) {\n" +
                "                    result.putAll(thread.consumerMetrics());\n" +
                "                }\n" +
                "}";
        String dst_method = "public Map<MetricName , ? extends Metric > metrics() {\n" +
                "                for (final StreamThread thread : threads) {\n" +
                "                    result.putAll(thread.producerMetrics());\n" +
                "                    result.putAll(thread.consumerMetrics());\n" +
                "                    result.putAll(thread.adminClientMetrics());\n" +
                "                }\n" +
                "             }";
        String src_comments = "Get read-only handle on global metrics registry, including streams client‘s own metrics plus its embedded consumer clients’ metrics.\n";
        get.setHeader("src_method",src_method);
        get.setHeader("dst_method",dst_method);
        get.setHeader("src_comments",src_comments);
        // 执行get请求，得到返回体
        HttpResponse response = client.execute(get);
        // 判断是否正常返回
        if (response.getStatusLine().getStatusCode() == HttpStatus.SC_OK) {
            // 解析数据
            String data = EntityUtils.toString(response.getEntity(),Charsets.UTF_8);
            System.out.println(data);
        }
    }
    public String post(String url,String src_method, String dst_method, String src_comments) throws Exception {
        // 创建HttpClient实例
        HttpClient client = HttpClientBuilder.create().build();
        // 根据URL创建HttpPost实例
        HttpPost post = new HttpPost(url);
        // 构造post参数
        List<NameValuePair> params = new ArrayList<NameValuePair>();
        params.add(new BasicNameValuePair("src_method", src_method));
        params.add(new BasicNameValuePair("dst_method", dst_method));
        params.add(new BasicNameValuePair("src_comments", src_comments));
        params.add(new BasicNameValuePair("say_hello", "hello"));
        // 编码格式转换
        UrlEncodedFormEntity entity = new UrlEncodedFormEntity(params, StandardCharsets.UTF_8);
        // 传入请求体
        post.setEntity(entity);
        // 发送请求，得到响应体
        HttpResponse response = client.execute(post);
        // 判断是否正常返回
        if (response.getStatusLine().getStatusCode() == 200) {
            // 解析数据
            HttpEntity resEntity = response.getEntity();
            String data = EntityUtils.toString(resEntity);
//            System.out.println(data);
            return data;
        }
        return "";
    }

}
