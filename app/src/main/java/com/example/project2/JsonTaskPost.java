package com.example.project2;

import android.os.AsyncTask;
import android.util.Log;

import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;

public class JsonTaskPost extends AsyncTask<String, String, String> {


    @Override
    protected String doInBackground(String... urls) {

        try {
            String body= urls[1];
            //JSONObject를 만들고 key value 형식으로 값을 저장해준다.


            HttpURLConnection con = null;
            BufferedReader reader = null;

            try{
                //URL url = new URL("http://192.249.19.244:1180/phonebook");
                URL url = new URL(urls[0]);
                //연결을 함
                con = (HttpURLConnection) url.openConnection();

                con.setRequestMethod("POST");//POST방식으로 보냄
                con.setRequestProperty("Cache-Control", "no-cache");//캐시 설정
                con.setRequestProperty("Content-Type", "application/x-www-form-urlencoded");//application JSON 형식으로 전송
               // con.setRequestProperty("Accept", "text/html");//서버에 response 데이터를 html로 받음
                con.setDoOutput(true);//Outstream으로 post 데이터를 넘겨주겠다는 의미
               // con.setDoInput(true);//Inputstream으로 서버로부터 응답을 받겠다는 의미
                //Log.d("josn",jsonObject.toString());
                con.connect();
                Log.d("josn",body);
                //서버로 보내기위해서 스트림 만듬

                OutputStream outStream = con.getOutputStream();

                //버퍼를 생성하고 넣음

                BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(outStream));

                writer.write(body);
                //Log.d("josn123232323",jsonObject.toString());
                writer.flush();

                writer.close();//버퍼를 받아줌

                //서버로 부터 데이터를 받음

                InputStream stream = con.getInputStream();

                reader = new BufferedReader(new InputStreamReader(stream));

                StringBuffer buffer = new StringBuffer();

                String line = "";

                while((line = reader.readLine()) != null){

                    buffer.append(line);

                }
                Log.d("output buffer", buffer.toString());
                return buffer.toString();//서버로 부터 받은 값을 리턴해줌 아마 OK!!가 들어올것임

            } catch (MalformedURLException e){

                e.printStackTrace();

            } catch (IOException e) {

                e.printStackTrace();

            } finally {

                if(con != null){

                    con.disconnect();

                }

                try {

                    if(reader != null){

                        reader.close();//버퍼를 닫아줌

                    }

                } catch (IOException e) {

                    e.printStackTrace();

                }

            }

        } catch (Exception e) {

            e.printStackTrace();

        }

        return null;

    }



    @Override
    protected void onPostExecute(String result) {
        super.onPostExecute(result);
        //Toast.makeText(this,result,0);
    }

}