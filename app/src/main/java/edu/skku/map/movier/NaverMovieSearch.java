package edu.skku.map.movier;

import android.util.Log;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import org.jetbrains.annotations.NotNull;
import org.json.JSONArray;
import org.json.JSONObject;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.Headers;
import okhttp3.HttpUrl;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

public class NaverMovieSearch {

    private static final String naverMovieAPIUrl = "https://openapi.naver.com/v1/search/movie";
    private static final String clientId = "m76VbpKcogPIwU2UJ9R4";
    private static final String clientSecret = "QHPUQJcKk3";

    private OkHttpClient client;
    private String searchKeyword;
    private OnReceiveMovieDataListener onReceiveMovieDataListener;
    private int startPosition;
    private int numberToSearch;


    public NaverMovieSearch(String searchKeyword, int startPosition, int numberToSearch, OnReceiveMovieDataListener onReceiveMovieDataListener) {
        this.searchKeyword = searchKeyword;
        this.startPosition = startPosition;
        this.onReceiveMovieDataListener = onReceiveMovieDataListener;
        this.numberToSearch = numberToSearch;

        sendRequest();
    }


    private void sendRequest() {
        Map<String, String> requestHeaders = new HashMap<>();
        HttpUrl.Builder urlBuilder = HttpUrl.parse(naverMovieAPIUrl).newBuilder();
        String reqUrl;
        Headers headerBuild;
        Request request;

        client = new OkHttpClient();

        requestHeaders.put("X-Naver-Client-Id", clientId);
        requestHeaders.put("X-Naver-Client-Secret", clientSecret);

        urlBuilder.addQueryParameter("query", searchKeyword);
        urlBuilder.addQueryParameter("start", String.valueOf(startPosition));
        urlBuilder.addQueryParameter("display", String.valueOf(numberToSearch));
        reqUrl = urlBuilder.build().toString();
        headerBuild = Headers.of(requestHeaders);
        request = new Request.Builder().url(reqUrl).headers(headerBuild).build();

        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(@NotNull Call call, @NotNull IOException e) {
                Log.d("TEST", e.getMessage());
            }

            @Override
            public void onResponse(@NotNull Call call, @NotNull Response response) throws IOException {
                try {
                    final String myResponse = response.body().string();
                    Gson gson = new GsonBuilder().create();
                    JSONObject jsonObject = new JSONObject(myResponse);
                    JSONArray jsonArray = jsonObject.getJSONArray("items");
                    List<MovieData> dataList = new ArrayList<>();

                    for (int i = 0; i < jsonArray.length(); i++) {
                        MovieData data = gson.fromJson(jsonArray.get(i).toString(), MovieData.class);
                        boolean isAlreadyInList = false;

                        for (MovieData dataInList : dataList) {
                            if (dataInList.getTitle().equals(data.getTitle()) && dataInList.getPubDate().equals(data.getPubDate())) {
                                isAlreadyInList = true;
                                break;
                            }
                        }

                        if (!isAlreadyInList) {
                            dataList.add(data);
                        }
                    }

                    onReceiveMovieDataListener.onReceiveMovieData(dataList);
                } catch (Exception e) {
                    Log.e("TEST", "JSON ERROR", e);
                }
            }
        });
    }
}
