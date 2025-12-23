package com.example.cn.helloworld.ui.search;

import android.os.Bundle;
import android.text.TextUtils;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.Button;
import android.widget.EditText;

import com.example.cn.helloworld.R;
import com.example.cn.helloworld.ui.common.BaseActivity;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;

public class ProductWebSearchActivity extends BaseActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_web_search);
        setupBackButton();

        final EditText inputView = (EditText) findViewById(R.id.search_input);
        Button searchButton = (Button) findViewById(R.id.button_search);
        final WebView webView = (WebView) findViewById(R.id.web_view);

        WebSettings settings = webView.getSettings();
        settings.setJavaScriptEnabled(true);
        settings.setDomStorageEnabled(true);
        webView.setWebViewClient(new WebViewClient());

        loadUrl(webView, getString(R.string.search_default_url));

        searchButton.setOnClickListener(new android.view.View.OnClickListener() {
            @Override
            public void onClick(android.view.View v) {
                String query = inputView.getText().toString().trim();
                if (TextUtils.isEmpty(query)) {
                    loadUrl(webView, getString(R.string.search_default_url));
                    return;
                }
                if (query.startsWith("http://") || query.startsWith("https://")) {
                    loadUrl(webView, query);
                    return;
                }
                try {
                    String encoded = URLEncoder.encode(query, "UTF-8");
                    loadUrl(webView, "https://m.baidu.com/s?word=" + encoded);
                } catch (UnsupportedEncodingException e) {
                    loadUrl(webView, getString(R.string.search_default_url));
                }
            }
        });
    }

    private void loadUrl(WebView webView, String url) {
        webView.loadUrl(url);
    }
}
