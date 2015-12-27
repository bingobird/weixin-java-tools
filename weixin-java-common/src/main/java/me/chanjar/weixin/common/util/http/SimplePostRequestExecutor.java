package me.chanjar.weixin.common.util.http;

import java.io.IOException;

import me.chanjar.weixin.common.bean.result.WxError;
import me.chanjar.weixin.common.exception.WxErrorException;
import org.apache.http.Consts;
import org.apache.http.HttpHost;
import org.apache.http.auth.AuthScope;
import org.apache.http.auth.UsernamePasswordCredentials;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.CredentialsProvider;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.BasicCredentialsProvider;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;

/**
 * 简单的POST请求执行器，请求的参数是String, 返回的结果也是String
 * @author Daniel Qian
 * 修复了部分HttpClient连接释放问题，5秒的连接及接收数据超时
 * 2015-12-27 @bingobird
 *
 */
public class SimplePostRequestExecutor implements RequestExecutor<String, String> {

  @Override
  public String execute(CloseableHttpClient httpclient, HttpHost httpProxy, String uri, String postEntity) throws WxErrorException, ClientProtocolException, IOException {
    HttpPost httpPost = new HttpPost(uri);

    //modify by bingobird,修复HttpClient连接释放问题，5秒的连接及接收数据超时
    if (httpProxy != null) {
      RequestConfig config = RequestConfig.custom().setProxy(httpProxy).setSocketTimeout(5000).setConnectTimeout(5000).build();
      httpPost.setConfig(config);
    } else {
      RequestConfig config = RequestConfig.custom().setSocketTimeout(5000).setConnectTimeout(5000).build();
      httpPost.setConfig(config);
    }

    if (postEntity != null) {
      StringEntity entity = new StringEntity(postEntity, Consts.UTF_8);
      httpPost.setEntity(entity);
    }

    try (CloseableHttpResponse response = httpclient.execute(httpPost)) {
      String responseContent = Utf8ResponseHandler.INSTANCE.handleResponse(response);
      WxError error = WxError.fromJson(responseContent);
      if (error.getErrorCode() != 0) {
        throw new WxErrorException(error);
      }
      return responseContent;
    }
  }

}
